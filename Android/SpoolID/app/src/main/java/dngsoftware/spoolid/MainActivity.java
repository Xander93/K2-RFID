package dngsoftware.spoolid;

import static java.lang.String.format;
import static dngsoftware.spoolid.Utils.GetMaterialBrand;
import static dngsoftware.spoolid.Utils.GetMaterialInfo;
import static dngsoftware.spoolid.Utils.GetMaterialLength;
import static dngsoftware.spoolid.Utils.GetMaterialName;
import static dngsoftware.spoolid.Utils.GetMaterialWeight;
import static dngsoftware.spoolid.Utils.GetSetting;
import static dngsoftware.spoolid.Utils.SaveSetting;
import static dngsoftware.spoolid.Utils.SetPermissions;
import static dngsoftware.spoolid.Utils.addFilament;
import static dngsoftware.spoolid.Utils.bytesToHex;
import static dngsoftware.spoolid.Utils.canMfc;
import static dngsoftware.spoolid.Utils.cipherData;
import static dngsoftware.spoolid.Utils.createKey;
import static dngsoftware.spoolid.Utils.dp2Px;
import static dngsoftware.spoolid.Utils.getDBVersion;
import static dngsoftware.spoolid.Utils.getJsonDB;
import static dngsoftware.spoolid.Utils.getMaterialBrands;
import static dngsoftware.spoolid.Utils.getMaterialPos;
import static dngsoftware.spoolid.Utils.getMaterials;
import static dngsoftware.spoolid.Utils.getPixelColor;
import static dngsoftware.spoolid.Utils.getPositionByValue;
import static dngsoftware.spoolid.Utils.materialWeights;
import static dngsoftware.spoolid.Utils.playBeep;
import static dngsoftware.spoolid.Utils.populateDatabase;
import static dngsoftware.spoolid.Utils.printerTypes;
import static dngsoftware.spoolid.Utils.removeFilament;
import static dngsoftware.spoolid.Utils.restartApp;
import static dngsoftware.spoolid.Utils.restorePrinterDB;
import static dngsoftware.spoolid.Utils.saveDBToPrinter;
import static dngsoftware.spoolid.Utils.setMaterialInfo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import dngsoftware.spoolid.databinding.ActivityMainBinding;
import dngsoftware.spoolid.databinding.AddDialogBinding;
import dngsoftware.spoolid.databinding.EditDialogBinding;
import dngsoftware.spoolid.databinding.ManualDialogBinding;
import dngsoftware.spoolid.databinding.PickerDialogBinding;
import dngsoftware.spoolid.databinding.SaveDialogBinding;
import dngsoftware.spoolid.databinding.UpdateDialogBinding;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {
    private MatDB matDb;
    private filamentDB rdb;
    jsonItem[] jsonItems;
    ArrayAdapter<String> badapter, sadapter, padapter;
    ArrayAdapter<MaterialItem> madapter;
    private NfcAdapter nfcAdapter;
    Tag currentTag = null;
    int SelectedSize, SelectedBrand;
    String MaterialName, MaterialID, MaterialWeight, MaterialColor, PrinterType;
    Dialog pickerDialog, customDialog, saveDialog, updateDialog, editDialog, addDialog;
    boolean encrypted = false;
    byte[] encKey;
    private ActivityMainBinding main;
    private ManualDialogBinding manual;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        main = ActivityMainBinding.inflate(getLayoutInflater());
        View rv = main.getRoot();
        setContentView(rv);

        SetPermissions(this);

        PrinterType = GetSetting(this, "printer", "k2");

        padapter = new ArrayAdapter<>(this, R.layout.spinner_item, printerTypes);
        main.type.setAdapter(padapter);
        main.type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                SaveSetting(context, "printer", Objects.requireNonNull(padapter.getItem(position)).toLowerCase());
                PrinterType = Objects.requireNonNull(padapter.getItem(position)).toLowerCase();
                setMatDb(PrinterType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        main.type.setSelection(getPositionByValue(main.type, PrinterType.toUpperCase()));

        main.colorview.setBackgroundColor(Color.argb(255, 0, 0, 255));
        MaterialColor = "0000FF";

        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                Bundle options = new Bundle();
                options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);
                nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A, options);
                if (!canMfc(this)) {
                    Toast.makeText(getApplicationContext(), R.string.this_device_does_not_support_mifare_classic_tags, Toast.LENGTH_SHORT).show();
                    main.readbutton.setEnabled(false);
                    main.writebutton.setEnabled(false);
                    main.cbtn.setEnabled(false);
                    main.autoread.setChecked(false);
                    main.autoread.setEnabled(false);
                    main.colorspin.setEnabled(false);
                    main.spoolsize.setEnabled(false);
                    main.colorview.setEnabled(false);
                    main.colorview.setBackgroundColor(Color.parseColor("#D3D3D3"));
                    ImageViewCompat.setImageTintList(main.cbtn, ColorStateList.valueOf(Color.parseColor("#D3D3D3")));
                    main.lbltagid.setVisibility(View.INVISIBLE);
                    main.tagid.setVisibility(View.INVISIBLE);
                    main.txtmsg.setVisibility(View.VISIBLE);
                    main.txtmsg.setText(R.string.rfid_functions_disabled);
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.please_activate_nfc, Toast.LENGTH_LONG).show();
                main.readbutton.setEnabled(false);
                main.writebutton.setEnabled(false);
                main.readbutton.setVisibility(View.INVISIBLE);
                main.writebutton.setVisibility(View.INVISIBLE);
                main.cbtn.setEnabled(false);
                main.autoread.setChecked(false);
                main.autoread.setEnabled(false);
                main.colorspin.setEnabled(false);
                main.spoolsize.setEnabled(false);
                main.colorview.setEnabled(false);
                main.colorview.setBackgroundColor(Color.parseColor("#D3D3D3"));
                ImageViewCompat.setImageTintList(main.cbtn, ColorStateList.valueOf(Color.parseColor("#D3D3D3")));
                main.lbltagid.setVisibility(View.INVISIBLE);
                main.tagid.setVisibility(View.INVISIBLE);
                SpannableString spannableString = new SpannableString(getString(R.string.rfid_disabled_tap_here_to_enable_nfc));
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#1976D2")),  getString(R.string.rfid_disabled_tap_here_to_enable_nfc).indexOf("Tap"),
                        getString(R.string.rfid_disabled_tap_here_to_enable_nfc).indexOf("Tap")+22, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                main.txtmsg.setVisibility(View.VISIBLE);
                main.txtmsg.setText(spannableString);
                main.txtmsg.setGravity(Gravity.CENTER);
                main.txtmsg.setOnClickListener(view -> {
                      startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                      finish();
                });
            }
        } catch (Exception ignored) {
        }

        main.colorview.setOnClickListener(view -> openPicker());
        main.readbutton.setOnClickListener(view -> ReadSpoolData());
        main.cbtn.setOnClickListener(view -> openCustom());

        main.addbutton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Create Filament?");
            builder.setMessage("Using " + MaterialName + " as a template");
            builder.setPositiveButton("Create", (dialog, which) -> {
                loadAdd();
                dialog.dismiss();
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
            AlertDialog alert = builder.create();
            alert.show();
        });

        main.editbutton.setOnClickListener(view -> loadEdit());

        main.deletebutton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Filament?");
            builder.setMessage("Brand:  " + GetMaterialBrand(matDb, MaterialID) + "\nType:    " + MaterialName);
            builder.setPositiveButton("Delete", (dialog, which) -> {
                removeFilament(matDb, MaterialID);
                setMatDb(PrinterType);
                dialog.dismiss();
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
            AlertDialog alert = builder.create();
            alert.show();
        });


        main.uploadbutton.setOnClickListener(view -> openUpload());


        main.ubtn.setOnClickListener(view -> openUpdate());

        main.colorspin.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    openPicker();
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    break;
                default:
                    break;
            }
            return false;
        });

        main.autoread.setChecked(GetSetting(this, "autoread", false));
        main.autoread.setOnCheckedChangeListener((buttonView, isChecked) -> SaveSetting(this, "autoread", isChecked));

        sadapter = new ArrayAdapter<>(this, R.layout.spinner_item, materialWeights);
        main.spoolsize.setAdapter(sadapter);
        main.spoolsize.setSelection(SelectedSize);
        main.spoolsize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                SelectedSize = main.spoolsize.getSelectedItemPosition();
                MaterialWeight = sadapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        ReadTagUID(getIntent());
    }


    void setMatDb(String pType) {
        try {
            if (rdb != null && rdb.isOpen()) {
                rdb.close();
            }

            rdb = Room.databaseBuilder(this, filamentDB.class, "material_database_" + pType)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
            matDb = rdb.matDB();


            if (matDb.getItemCount() == 0) {
                populateDatabase(this, matDb, null, pType);
            } else {
                long dbVersion = GetSetting(this, "version_" + pType, -1L);
                if (getDBVersion(this, pType) > dbVersion) {
                    matDb.deleteAll();
                    populateDatabase(this, matDb, null, pType);
                }
            }

            main.writebutton.setOnClickListener(view -> WriteSpoolData(MaterialID, MaterialColor, GetMaterialLength(MaterialWeight)));

            badapter = new ArrayAdapter<>(this, R.layout.spinner_item, getMaterialBrands(matDb));
            main.brand.setAdapter(badapter);
            if (SelectedBrand < main.brand.getCount()) {
                main.brand.setSelection(SelectedBrand);
            }
            main.brand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    SelectedBrand = main.brand.getSelectedItemPosition();
                    setMaterial(badapter.getItem(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });


            madapter = new ArrayAdapter<>(this, R.layout.spinner_item, getMaterials(matDb, badapter.getItem(main.brand.getSelectedItemPosition())));
            main.material.setAdapter(madapter);
            main.material.setSelection(getMaterialPos(madapter, MaterialID));
            main.material.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    MaterialItem selectedItem = (MaterialItem) parentView.getItemAtPosition(position);
                    MaterialName = selectedItem.getMaterialBrand();
                    MaterialID = selectedItem.getMaterialID();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
        } catch (Exception ignored) {
        }
    }


    void setMaterial(String brand) {
        madapter = new ArrayAdapter<>(this, R.layout.spinner_item, getMaterials(matDb, brand));
        main.material.setAdapter(madapter);
        main.material.setSelection(getMaterialPos(madapter, MaterialID));
        main.material.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                MaterialItem selectedItem = (MaterialItem) parentView.getItemAtPosition(position);
                MaterialName = selectedItem.getMaterialBrand();
                MaterialID = selectedItem.getMaterialID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pickerDialog != null && pickerDialog.isShowing()) {
            pickerDialog.dismiss();
        }
        if (customDialog != null && customDialog.isShowing()) {
            customDialog.dismiss();
        }
        if (saveDialog != null && saveDialog.isShowing()) {
            saveDialog.dismiss();
        }
        if (updateDialog != null && updateDialog.isShowing()) {
            updateDialog.dismiss();
        }
        if (editDialog != null && editDialog.isShowing()) {
            editDialog.dismiss();
        }
        if (addDialog != null && addDialog.isShowing()) {
            addDialog.dismiss();
        }
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            try {
                nfcAdapter.disableReaderMode(this);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (pickerDialog != null && pickerDialog.isShowing()) {
            pickerDialog.dismiss();
            openPicker();
        }
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        try {
            currentTag = tag;
            runOnUiThread(() -> {
                if (currentTag.getId().length > 4)
                {
                    Toast.makeText(getApplicationContext(), R.string.tag_not_compatible, Toast.LENGTH_SHORT).show();
                    main.tagid.setText(R.string.error);
                    return;
                }
                Toast.makeText(getApplicationContext(), getString(R.string.tag_found) + bytesToHex(currentTag.getId()), Toast.LENGTH_SHORT).show();
                main.tagid.setText(bytesToHex(currentTag.getId()));
                encKey = createKey(currentTag.getId());
                CheckTag();
                if (encrypted) {
                    main.tagid.setText(String.format("\uD83D\uDD10 %s", bytesToHex(currentTag.getId())));
                }
                if (GetSetting(this, "autoread", false)) {
                    ReadSpoolData();
                }
            });
        } catch (Exception ignored) {
        }
    }

    void ReadTagUID(Intent intent) {
        if (intent != null) {
            try {
                if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                    currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    assert currentTag != null;
                    if (currentTag.getId().length > 4)
                    {
                        Toast.makeText(getApplicationContext(), R.string.tag_not_compatible, Toast.LENGTH_SHORT).show();
                        main.tagid.setText(R.string.error);
                        return;
                    }
                    Toast.makeText(getApplicationContext(), getString(R.string.tag_found) + bytesToHex(currentTag.getId()), Toast.LENGTH_SHORT).show();
                    main.tagid.setText(bytesToHex(currentTag.getId()));
                    encKey = createKey(currentTag.getId());
                    CheckTag();
                    if (encrypted) {
                        main.tagid.setText(String.format("\uD83D\uDD10 %s", bytesToHex(currentTag.getId())));
                    }
                    if (GetSetting(this, "autoread", false)) {
                        ReadSpoolData();
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    void CheckTag() {
        if (currentTag != null) {
            MifareClassic mfc = MifareClassic.get(currentTag);
            if (mfc != null && mfc.getType() == MifareClassic.TYPE_CLASSIC) {
                try {
                    mfc.connect();
                    encrypted = mfc.authenticateSectorWithKeyA(1, encKey);
                    mfc.close();
                } catch (Exception ignored) {
                    Toast.makeText(getApplicationContext(), R.string.error_reading_tag, Toast.LENGTH_SHORT).show();
                    encrypted = false;
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.invalid_tag_type, Toast.LENGTH_SHORT).show();
            }
        }
    }

    String ReadTag() {
        if (currentTag != null) {
            MifareClassic mfc = MifareClassic.get(currentTag);
            if (mfc != null && mfc.getType() == MifareClassic.TYPE_CLASSIC) {
                try {
                    mfc.connect();
                    byte[] key = MifareClassic.KEY_DEFAULT;
                    if (encrypted) {
                        key = encKey;
                    }
                    boolean auth = mfc.authenticateSectorWithKeyA(1, key);
                    if (auth) {
                        byte[] data = new byte[48];
                        ByteBuffer buff = ByteBuffer.wrap(data);
                        buff.put(mfc.readBlock(4));
                        buff.put(mfc.readBlock(5));
                        buff.put(mfc.readBlock(6));
                        mfc.close();
                        if (encrypted) {
                            return new String(cipherData(2, buff.array()), StandardCharsets.UTF_8);
                        }
                        return new String(buff.array(), StandardCharsets.UTF_8);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                    }
                    mfc.close();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), R.string.error_reading_tag, Toast.LENGTH_SHORT).show();
                }
                try {
                    mfc.close();
                } catch (Exception ignored) {
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.invalid_tag_type, Toast.LENGTH_SHORT).show();
            }
            return null;
        }
        return null;
    }

    void WriteTag(String tagData) {
        if (currentTag != null && tagData.length() == 40) {
            MifareClassic mfc = MifareClassic.get(currentTag);
            if (mfc != null && mfc.getType() == MifareClassic.TYPE_CLASSIC) {
                try {
                    mfc.connect();
                    byte[] key = MifareClassic.KEY_DEFAULT;
                    if (encrypted) {
                        key = encKey;
                    }
                    boolean auth = mfc.authenticateSectorWithKeyA(1, key);
                    if (auth) {
                        byte[] sectorData = cipherData(1, (tagData + "00000000").getBytes());
                        if (sectorData == null) {
                            mfc.close();
                            Toast.makeText(getApplicationContext(), R.string.failed_to_encrypt_data, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int blockIndex = 4;
                        for (int i = 0; i < sectorData.length; i += MifareClassic.BLOCK_SIZE) {
                            byte[] block = Arrays.copyOfRange(sectorData, i, i + MifareClassic.BLOCK_SIZE);
                            mfc.writeBlock(blockIndex, block);
                            blockIndex++;
                        }
                        if (!encrypted) {
                            byte[] data = mfc.readBlock(7);
                            System.arraycopy(encKey, 0, data, 0, encKey.length);
                            System.arraycopy(encKey, 0, data, 10, encKey.length);
                            mfc.writeBlock(7, data);
                            encrypted = true;
                            main.tagid.setText(String.format("\uD83D\uDD10 %s", bytesToHex(currentTag.getId())));
                        }
                        playBeep();
                        Toast.makeText(getApplicationContext(), R.string.data_written_to_tag, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                    }
                    mfc.close();
                } catch (Exception ignored) {
                    Toast.makeText(getApplicationContext(), R.string.error_writing_to_tag, Toast.LENGTH_SHORT).show();
                }
                try {
                    mfc.close();
                } catch (Exception ignored) {
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.invalid_tag_type, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.error_writing_to_tag, Toast.LENGTH_SHORT).show();
        }
    }

    void FormatTag() {
        if (currentTag != null) {
            MifareClassic mfc = MifareClassic.get(currentTag);
            if (mfc != null && mfc.getType() == MifareClassic.TYPE_CLASSIC) {
                try {
                    mfc.connect();
                    byte[] key = MifareClassic.KEY_DEFAULT;
                    if (encrypted) {
                        key = encKey;
                    }
                    boolean auth = mfc.authenticateSectorWithKeyA(1, key);
                    if (auth) {
                        byte[] sectorData = new byte[48];
                        Arrays.fill(sectorData, (byte) 0);
                        int blockIndex = 4;
                        for (int i = 0; i < sectorData.length; i += MifareClassic.BLOCK_SIZE) {
                            byte[] block = Arrays.copyOfRange(sectorData, i, i + MifareClassic.BLOCK_SIZE);
                            mfc.writeBlock(blockIndex, block);
                            blockIndex++;
                        }
                        if (encrypted) {
                            byte[] data = mfc.readBlock(7);
                            System.arraycopy(MifareClassic.KEY_DEFAULT, 0, data, 0, MifareClassic.KEY_DEFAULT.length);
                            System.arraycopy(MifareClassic.KEY_DEFAULT, 0, data, 10, MifareClassic.KEY_DEFAULT.length);
                            mfc.writeBlock(7, data);
                            encrypted = false;
                            main.tagid.setText(bytesToHex(currentTag.getId()));
                        }
                        playBeep();
                        Toast.makeText(getApplicationContext(), R.string.tag_formatted, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                    }
                    mfc.close();
                } catch (Exception ignored) {
                    Toast.makeText(getApplicationContext(), R.string.error_formatting_tag, Toast.LENGTH_SHORT).show();
                }
                try {
                    mfc.close();
                } catch (Exception ignored) {
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.invalid_tag_type, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.error_formatting_tag, Toast.LENGTH_SHORT).show();
        }
    }

    void ReadSpoolData() {
        final Handler handler = new Handler(Looper.getMainLooper());
        String tagData = ReadTag();
        if (tagData != null && tagData.length() >= 40) {
            String MaterialId = tagData.substring(12, 17);
            if (GetMaterialName(matDb, MaterialId) != null) {
                MaterialColor = tagData.substring(18, 24);
                String Length = tagData.substring(24, 28);
                main.colorview.setBackgroundColor(Color.parseColor("#" + MaterialColor));
                MaterialName = Objects.requireNonNull(GetMaterialName(matDb, MaterialId))[0];
                main.brand.setSelection(badapter.getPosition(Objects.requireNonNull(GetMaterialName(matDb, MaterialId))[1]));
                handler.postDelayed(() -> main.material.setSelection(getMaterialPos(madapter, MaterialId)), 200);
                main.spoolsize.setSelection(sadapter.getPosition(GetMaterialWeight(Length)));
                Toast.makeText(getApplicationContext(), R.string.data_read_from_tag, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.unknown_or_empty_tag, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.error_reading_tag, Toast.LENGTH_SHORT).show();
        }
    }

    void WriteSpoolData(String MaterialID, String Color, String Length) {
        //SecureRandom random = new SecureRandom();
        String filamentId = "1" + MaterialID; //material_database.json
        String vendorId = "0276"; //0276 creality
        String color = "0" + Color;
        String serialNum = "000001"; //format(Locale.getDefault(), "%06d", random.nextInt(900000));
        String reserve = "000000";
        WriteTag("AB124" + vendorId + "A2" + filamentId + color + Length + serialNum + reserve);
    }

    @SuppressLint("ClickableViewAccessibility")
    void openPicker() {
        try {
            pickerDialog = new Dialog(this, R.style.Theme_SpoolID);
            pickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            pickerDialog.setCanceledOnTouchOutside(false);
            pickerDialog.setTitle(R.string.pick_color);
            PickerDialogBinding dl = PickerDialogBinding.inflate(getLayoutInflater());
            View rv = dl.getRoot();
            pickerDialog.setContentView(rv);

            dl.dcolorview.setBackgroundColor(Color.parseColor("#" + MaterialColor));
            dl.txtcolor.setText(MaterialColor);
            dl.btncls.setOnClickListener(v -> {
                if (customDialog != null && customDialog.isShowing()) {
                    manual.txtcolor.setText(String.format("0%s", MaterialColor));
                } else {
                    if (dl.txtcolor.getText().toString().length() == 6) {
                        try {
                            MaterialColor = dl.txtcolor.getText().toString();
                            main.colorview.setBackgroundColor(Color.parseColor("#" + MaterialColor));
                            dl.dcolorview.setBackgroundColor(Color.parseColor("#" + MaterialColor));
                        } catch (Exception ignored) {
                        }
                    }
                }
                pickerDialog.dismiss();
            });
            dl.txtcolor.setOnEditorActionListener((v, actionId, event) -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(dl.txtcolor.getWindowToken(), 0);
                if (dl.txtcolor.getText().toString().length() == 6) {
                    try {
                        MaterialColor = dl.txtcolor.getText().toString();
                        main.colorview.setBackgroundColor(Color.parseColor("#" + MaterialColor));
                        dl.dcolorview.setBackgroundColor(Color.parseColor("#" + MaterialColor));

                    } catch (Exception ignored) {
                    }
                }
                return true;
            });
            dl.picker.setOnTouchListener((v, event) -> {
                final int currPixel = getPixelColor(event, dl.picker);
                if (currPixel != 0) {
                    MaterialColor = format("%02x%02x%02x", Color.red(currPixel), Color.green(currPixel), Color.blue(currPixel)).toUpperCase();
                    if (customDialog != null && customDialog.isShowing()) {
                        manual.txtcolor.setText(String.format("0%s", MaterialColor));
                    } else {
                        main.colorview.setBackgroundColor(Color.argb(255, Color.red(currPixel), Color.green(currPixel), Color.blue(currPixel)));
                        dl.dcolorview.setBackgroundColor(Color.argb(255, Color.red(currPixel), Color.green(currPixel), Color.blue(currPixel)));
                    }
                    pickerDialog.dismiss();
                }
                return false;
            });
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            float scrWwidth = displayMetrics.widthPixels;
            if (scrWwidth > dp2Px(this, 500)) scrWwidth = dp2Px(this, 500);
            LinearGradient test = new LinearGradient(50.f, 0.f, scrWwidth - 250, 0.0f, new int[]{0xFF000000, 0xFF0000FF, 0xFF00FF00, 0xFF00FFFF, 0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00, 0xFFFFFFFF}, null, Shader.TileMode.CLAMP);
            ShapeDrawable shape = new ShapeDrawable(new RectShape());
            shape.getPaint().setShader(test);
            dl.seekbarFont.setProgressDrawable(shape);
            dl.seekbarFont.setMax(256 * 7 - 1);
            dl.seekbarFont.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        int r = 0;
                        int g = 0;
                        int b = 0;

                        if (progress < 256) {
                            b = progress;
                        } else if (progress < 256 * 2) {
                            g = progress % 256;
                            b = 256 - progress % 256;
                        } else if (progress < 256 * 3) {
                            g = 255;
                            b = progress % 256;
                        } else if (progress < 256 * 4) {
                            r = progress % 256;
                            g = 256 - progress % 256;
                            b = 256 - progress % 256;
                        } else if (progress < 256 * 5) {
                            r = 255;
                            b = progress % 256;
                        } else if (progress < 256 * 6) {
                            r = 255;
                            g = progress % 256;
                            b = 256 - progress % 256;
                        } else if (progress < 256 * 7) {
                            r = 255;
                            g = 255;
                            b = progress % 256;
                        }
                        MaterialColor = format("%02x%02x%02x", r, g, b).toUpperCase();
                        dl.txtcolor.setText(MaterialColor);
                        main.colorview.setBackgroundColor(Color.argb(255, r, g, b));
                        dl.dcolorview.setBackgroundColor(Color.argb(255, r, g, b));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            pickerDialog.show();
        } catch (Exception ignored) {
        }
    }

    void openCustom() {
        try {
            customDialog = new Dialog(this, R.style.Theme_SpoolID);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setCanceledOnTouchOutside(false);
            customDialog.setTitle(R.string.custom_tag_data);
            manual = ManualDialogBinding.inflate(getLayoutInflater());
            View rv = manual.getRoot();
            customDialog.setContentView(rv);
            manual.txtmonth.setText(GetSetting(this, "mon", getResources().getString(R.string.def_mon)));
            manual.txtday.setText(GetSetting(this, "day", getResources().getString(R.string.def_day)));
            manual.txtyear.setText(GetSetting(this, "yr", getResources().getString(R.string.def_yr)));
            manual.txtvendor.setText(GetSetting(this, "ven", getResources().getString(R.string.def_ven)));
            manual.txtbatch.setText(GetSetting(this, "bat", getResources().getString(R.string.def_bat)));
            manual.txtmaterial.setText(GetSetting(this, "mat", getResources().getString(R.string.def_mat)));
            manual.txtcolor.setText(GetSetting(this, "col", getResources().getString(R.string.def_col)));
            manual.txtlength.setText(GetSetting(this, "len", getResources().getString(R.string.def_len)));
            manual.txtserial.setText(GetSetting(this, "ser", getResources().getString(R.string.def_ser)));
            manual.txtreserve.setText(GetSetting(this, "res", getResources().getString(R.string.def_res)));
            manual.btncls.setOnClickListener(v -> customDialog.dismiss());
            manual.btncol.setOnClickListener(view -> openPicker());
            manual.btnrnd.setOnClickListener(v -> {
                SecureRandom random = new SecureRandom();
                manual.txtserial.setText(format(Locale.getDefault(), "%06d", random.nextInt(900000)));
            });
            manual.btnread.setOnClickListener(v -> {
                String tagData = ReadTag();
                if (tagData != null && tagData.length() >= 40) {
                    if (!tagData.startsWith("\0")) {
                        manual.txtmonth.setText(tagData.substring(0, 1).toUpperCase());
                        manual.txtday.setText(tagData.substring(1, 3).toUpperCase());
                        manual.txtyear.setText(tagData.substring(3, 5).toUpperCase());
                        manual.txtvendor.setText(tagData.substring(5, 9).toUpperCase());
                        manual.txtbatch.setText(tagData.substring(9, 11).toUpperCase());
                        manual.txtmaterial.setText(tagData.substring(11, 17).toUpperCase());
                        manual.txtcolor.setText(tagData.substring(17, 24).toUpperCase());
                        manual.txtlength.setText(tagData.substring(24, 28).toUpperCase());
                        manual.txtserial.setText(tagData.substring(28, 34).toUpperCase());
                        manual.txtreserve.setText(tagData.substring(34, 40).toUpperCase());
                        Toast.makeText(getApplicationContext(), R.string.data_read_from_tag, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.unknown_or_empty_tag, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.error_reading_tag, Toast.LENGTH_SHORT).show();
                }
            });
            manual.btnwrite.setOnClickListener(v -> {
                if (manual.txtmonth.getText().length() == 1 && manual.txtday.getText().length() == 2 && manual.txtyear.getText().length() == 2
                        && manual.txtvendor.getText().length() == 4 && manual.txtbatch.getText().length() == 2 && manual.txtmaterial.getText().length() == 6
                        && manual.txtcolor.getText().length() == 7 && manual.txtlength.getText().length() == 4
                        && manual.txtserial.getText().length() == 6 && manual.txtreserve.getText().length() == 6) {
                    WriteTag(manual.txtmonth.getText().toString() + manual.txtday.getText().toString() + manual.txtyear.getText().toString()
                            + manual.txtvendor.getText().toString() + manual.txtbatch.getText().toString() + manual.txtmaterial.getText().toString() + manual.txtcolor.getText().toString()
                            + manual.txtlength.getText().toString() + manual.txtserial.getText().toString() + manual.txtreserve.getText().toString());
                    SaveSetting(this, "mon", manual.txtmonth.getText().toString().toUpperCase());
                    SaveSetting(this, "day", manual.txtday.getText().toString().toUpperCase());
                    SaveSetting(this, "yr", manual.txtyear.getText().toString().toUpperCase());
                    SaveSetting(this, "ven", manual.txtvendor.getText().toString().toUpperCase());
                    SaveSetting(this, "bat", manual.txtbatch.getText().toString().toUpperCase());
                    SaveSetting(this, "mat", manual.txtmaterial.getText().toString().toUpperCase());
                    SaveSetting(this, "col", manual.txtcolor.getText().toString().toUpperCase());
                    SaveSetting(this, "len", manual.txtlength.getText().toString().toUpperCase());
                    SaveSetting(this, "ser", manual.txtserial.getText().toString().toUpperCase());
                    SaveSetting(this, "res", manual.txtreserve.getText().toString().toUpperCase());
                } else {
                    Toast.makeText(getApplicationContext(), R.string.incorrect_tag_data_length, Toast.LENGTH_SHORT).show();
                }
            });
            manual.btnfmt.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.format_tag_q);
                builder.setMessage(R.string.erase_message);
                builder.setPositiveButton(R.string.format, (dialog, which) -> {
                    FormatTag();
                    dialog.dismiss();
                });
                builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
                AlertDialog alert = builder.create();
                alert.show();
            });
            manual.btnrst.setOnClickListener(v -> {
                manual.txtmonth.setText(R.string.def_mon);
                manual.txtday.setText(R.string.def_day);
                manual.txtyear.setText(R.string.def_yr);
                manual.txtvendor.setText(R.string.def_ven);
                manual.txtbatch.setText(R.string.def_bat);
                manual.txtmaterial.setText(R.string.def_mat);
                manual.txtcolor.setText(R.string.def_col);
                manual.txtlength.setText(R.string.def_len);
                manual.txtserial.setText(R.string.def_ser);
                manual.txtreserve.setText(R.string.def_res);
                SaveSetting(this, "mon", manual.txtmonth.getText().toString().toUpperCase());
                SaveSetting(this, "day", manual.txtday.getText().toString().toUpperCase());
                SaveSetting(this, "yr", manual.txtyear.getText().toString().toUpperCase());
                SaveSetting(this, "ven", manual.txtvendor.getText().toString().toUpperCase());
                SaveSetting(this, "bat", manual.txtbatch.getText().toString().toUpperCase());
                SaveSetting(this, "mat", manual.txtmaterial.getText().toString().toUpperCase());
                SaveSetting(this, "col", manual.txtcolor.getText().toString().toUpperCase());
                SaveSetting(this, "len", manual.txtlength.getText().toString().toUpperCase());
                SaveSetting(this, "ser", manual.txtserial.getText().toString().toUpperCase());
                SaveSetting(this, "res", manual.txtreserve.getText().toString().toUpperCase());
                Toast.makeText(getApplicationContext(), R.string.values_reset, Toast.LENGTH_SHORT).show();
            });
            customDialog.show();
        } catch (Exception ignored) {
        }
    }

    void openUpdate() {
        try {
            updateDialog = new Dialog(this, R.style.Theme_SpoolID);
            updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            updateDialog.setCanceledOnTouchOutside(false);
            updateDialog.setTitle(R.string.update);
            UpdateDialogBinding dl = UpdateDialogBinding.inflate(getLayoutInflater());
            View rv = dl.getRoot();
            updateDialog.setContentView(rv);

            SpannableString spannableString = new SpannableString(String.format(Locale.getDefault(), getString(R.string.update_desc_printer), PrinterType.toUpperCase()));
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#1976D2")), 41, 43, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            dl.chkprnt.setChecked(GetSetting(this, "fromprinter_" + PrinterType, false));
            dl.chkprnt.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SaveSetting(this, "fromprinter_" + PrinterType, isChecked);
                if (isChecked) {
                    dl.txtaddress.setVisibility(View.VISIBLE);
                    dl.txtpsw.setVisibility(View.VISIBLE);
                    dl.lblpip.setVisibility(View.VISIBLE);
                    dl.lblpsw.setVisibility(View.VISIBLE);
                    dl.updatedesc.setText(spannableString);
                } else {
                    dl.txtaddress.setVisibility(View.INVISIBLE);
                    dl.txtpsw.setVisibility(View.INVISIBLE);
                    dl.lblpip.setVisibility(View.INVISIBLE);
                    dl.lblpsw.setVisibility(View.INVISIBLE);
                    dl.updatedesc.setText(getString(R.string.update_desc));
                }
                dl.btnupd.setVisibility(View.INVISIBLE);
                dl.txtmsg.setText("");
                dl.txtnewver.setText("");
            });

            if (dl.chkprnt.isChecked()) {
                dl.txtaddress.setVisibility(View.VISIBLE);
                dl.txtpsw.setVisibility(View.VISIBLE);
                dl.lblpip.setVisibility(View.VISIBLE);
                dl.lblpsw.setVisibility(View.VISIBLE);
                dl.updatedesc.setText(spannableString);
            } else {
                dl.txtaddress.setVisibility(View.INVISIBLE);
                dl.txtpsw.setVisibility(View.INVISIBLE);
                dl.lblpip.setVisibility(View.INVISIBLE);
                dl.lblpsw.setVisibility(View.INVISIBLE);
                dl.updatedesc.setText(getString(R.string.update_desc));
            }

            String sshDefault;
            if (PrinterType.equalsIgnoreCase("hi")) {
                sshDefault = "Creality2024";
            } else if (PrinterType.equalsIgnoreCase("k1")) {
                sshDefault = "creality_2023";
            } else {
                sshDefault = "creality_2024";
            }

            dl.txtpsw.setText(GetSetting(this, "psw_" + PrinterType, sshDefault));
            dl.txtaddress.setText(GetSetting(this, "host_" + PrinterType, ""));
            dl.btncls.setOnClickListener(v -> updateDialog.dismiss());
            dl.btnupd.setVisibility(View.INVISIBLE);
            dl.txtcurver.setText(String.format(Locale.getDefault(), getString(R.string.current_version), GetSetting(this, "version_" + PrinterType, -1L)));
            dl.txtprinter.setText(String.format(getString(R.string.creality_type), PrinterType.toUpperCase()));

            dl.btnchk.setOnClickListener(v -> {
                String host = dl.txtaddress.getText().toString();
                String psw = dl.txtpsw.getText().toString();
                dl.txtmsg.setTextColor(getResources().getColor(R.color.text_color));
                dl.txtmsg.setText(R.string.checking_for_updates);
                long version = GetSetting(this, "version_" + PrinterType, -1L);
                dl.txtcurver.setText(String.format(Locale.getDefault(), getString(R.string.current_version), version));
                new Thread(() -> {
                    try {
                        String json;
                        if (GetSetting(this, "fromprinter_" + PrinterType, false)) {
                            SaveSetting(this, "host_" + PrinterType, host);
                            SaveSetting(this, "psw_" + PrinterType, psw);

                            if (host.isEmpty()) {
                                runOnUiThread(() -> {
                                    dl.txtmsg.setTextColor(Color.RED);
                                    dl.txtmsg.setText(R.string.please_enter_printer_ip_address);
                                    dl.btnupd.setVisibility(View.INVISIBLE);
                                    dl.txtnewver.setText("");
                                });
                                return;
                            }
                            if (psw.isEmpty()) {
                                runOnUiThread(() -> {
                                    dl.txtmsg.setTextColor(Color.RED);
                                    dl.txtmsg.setText(R.string.please_enter_ssh_password);
                                    dl.btnupd.setVisibility(View.INVISIBLE);
                                    dl.txtnewver.setText("");
                                });
                                return;
                            }
                            json = getJsonDB(psw, host, PrinterType, "material_database.json");
                        } else {
                            json = getJsonDB(PrinterType, false);
                        }
                        if (json != null && json.contains("\"kvParam\"")) {
                            JSONObject materials = new JSONObject(json);
                            JSONObject result = new JSONObject(materials.getString("result"));
                            long newVer = result.getLong("version");
                            runOnUiThread(() -> {
                                dl.txtnewver.setText(format(Locale.getDefault(), getString(R.string.printer_version), newVer));
                                if (newVer > version) {
                                    dl.btnupd.setVisibility(View.VISIBLE);
                                    dl.txtmsg.setTextColor(ContextCompat.getColor(this, R.color.text_color));
                                    dl.txtmsg.setText(R.string.update_available);
                                } else {
                                    dl.btnupd.setVisibility(View.INVISIBLE);
                                    dl.txtmsg.setTextColor(ContextCompat.getColor(this, R.color.text_color));
                                    dl.txtmsg.setText(R.string.no_update_available);
                                }
                            });
                        } else {
                            runOnUiThread(() -> {
                                dl.txtmsg.setTextColor(Color.RED);
                                dl.txtmsg.setText(R.string.unable_to_download_file_from_printer);
                            });
                        }
                    } catch (Exception ignored) {
                    }
                }).start();
            });

            dl.btnupd.setOnClickListener(v -> {
                String host = GetSetting(this, "host_" + PrinterType, "");
                String psw = GetSetting(this, "psw_" + PrinterType, sshDefault);
                dl.txtmsg.setTextColor(getResources().getColor(R.color.text_color));
                dl.txtmsg.setText(R.string.downloading_update);
                final Handler handler = new Handler(Looper.getMainLooper());
                new Thread(() -> {
                    try {
                        String json;
                        if (GetSetting(this, "fromprinter_" + PrinterType, false)) {
                            if (host.isEmpty()) {
                                runOnUiThread(() -> {
                                    dl.txtmsg.setTextColor(Color.RED);
                                    dl.txtmsg.setText(R.string.please_enter_printer_ip_address);
                                    dl.btnupd.setVisibility(View.INVISIBLE);
                                    dl.txtnewver.setText("");
                                });
                                return;
                            }
                            if (psw.isEmpty()) {
                                runOnUiThread(() -> {
                                    dl.txtmsg.setTextColor(Color.RED);
                                    dl.txtmsg.setText(R.string.please_enter_ssh_password);
                                    dl.btnupd.setVisibility(View.INVISIBLE);
                                    dl.txtnewver.setText("");
                                });
                                return;
                            }
                            json = getJsonDB(psw, host, PrinterType, "material_database.json");
                        } else {
                            json = getJsonDB(PrinterType, false);
                        }
                        if (json != null && json.contains("\"kvParam\"")) {
                            JSONObject materials = new JSONObject(json);
                            JSONObject result = new JSONObject(materials.getString("result"));
                            long newVer = result.getLong("version");
                            matDb.deleteAll();
                            populateDatabase(this, matDb, json, PrinterType);
                            SaveSetting(this, "version_" + PrinterType, newVer);
                            runOnUiThread(() -> {
                                dl.txtcurver.setText(format(Locale.getDefault(), getString(R.string.current_version), newVer));
                                dl.btnupd.setVisibility(View.INVISIBLE);
                                dl.txtmsg.setTextColor(ContextCompat.getColor(this, R.color.text_color));
                                dl.txtmsg.setText(R.string.update_successful);
                                handler.postDelayed(() -> restartApp(context), 2000);
                            });
                        } else {
                            runOnUiThread(() -> {
                                dl.txtmsg.setTextColor(Color.RED);
                                dl.txtmsg.setText(R.string.unable_to_download_file_from_printer);
                            });
                        }
                    } catch (Exception ignored) {
                    }
                }).start();
            });
            updateDialog.show();
        } catch (Exception ignored) {
        }
    }


    void loadEdit() {
        try {
            RecyclerView recyclerView;
            jsonAdapter recycleAdapter;
            editDialog = new Dialog(this, R.style.Theme_SpoolID);
            editDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            editDialog.setCanceledOnTouchOutside(false);
            editDialog.setTitle(R.string.filament_info);
            EditDialogBinding edl = EditDialogBinding.inflate(getLayoutInflater());
            View rv = edl.getRoot();
            editDialog.setContentView(rv);
            edl.btncls.setOnClickListener(v -> editDialog.dismiss());

            edl.btnsave.setOnClickListener(v -> {
                try {
                    JSONObject info = new JSONObject(GetMaterialInfo(matDb, MaterialID));
                    JSONObject param = info.getJSONObject("kvParam");
                    for (jsonItem jsonItem : jsonItems) {
                        param.put(jsonItem.jKey, jsonItem.jValue);
                    }
                    setMaterialInfo(matDb, MaterialID, info.toString());
                } catch (Exception ignored) {
                }
                editDialog.dismiss();
            });

            edl.lbldesc.setText(MaterialName);
            recyclerView = edl.recyclerView;
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            layoutManager.scrollToPosition(0);
            recyclerView.setLayoutManager(layoutManager);

            JSONObject info = new JSONObject(GetMaterialInfo(matDb, MaterialID));
            JSONObject param = info.getJSONObject("kvParam");
            jsonItems = new jsonItem[param.length()];
            int i = 0;
            for (Iterator<String> it = param.keys(); it.hasNext(); ) {
                String key = it.next();
                jsonItems[i] = new jsonItem();
                jsonItems[i].jKey = key;
                jsonItems[i].jValue = param.get(key);
                i++;
            }

            recycleAdapter = new jsonAdapter(getBaseContext(), jsonItems);
            recycleAdapter.setHasStableIds(true);
            runOnUiThread(() -> {
                recyclerView.removeAllViewsInLayout();
                recyclerView.setAdapter(null);
                recyclerView.setAdapter(recycleAdapter);
            });
            editDialog.show();
        } catch (Exception ignored) {
        }
    }


    void loadAdd() {
        try {

            RecyclerView recyclerView;
            jsonAdapter recycleAdapter;

            addDialog = new Dialog(this, R.style.Theme_SpoolID);
            addDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            addDialog.setCanceledOnTouchOutside(false);
            addDialog.setTitle(R.string.filament_info);
            AddDialogBinding adl = AddDialogBinding.inflate(getLayoutInflater());
            View rv = adl.getRoot();
            addDialog.setContentView(rv);
            adl.btncls.setOnClickListener(v -> addDialog.dismiss());

            adl.btnadd.setOnClickListener(v -> {
                try {
                    JSONObject info = new JSONObject(GetMaterialInfo(matDb, MaterialID));
                    JSONObject base = info.getJSONObject("base");

                    for (jsonItem jsonItem : jsonItems) {
                        if (jsonItem.jKey.equals("brand") || jsonItem.jKey.equals("name") || jsonItem.jKey.equals("meterialType") || jsonItem.jKey.equals("colors")) {
                            base.put(jsonItem.jKey, jsonItem.jValue);
                        }
                        else {
                            if (jsonItem.jValue instanceof Number) {
                                Number num = (Number) jsonItem.jValue;
                                if (num instanceof Float || num instanceof Double) {
                                    base.put(jsonItem.jKey, jsonItem.jValue);
                                } else if (num instanceof Integer || num instanceof Long || num instanceof Short || num instanceof Byte) {
                                    base.put(jsonItem.jKey, num);
                                } else {
                                    base.put(jsonItem.jKey, jsonItem.jValue);
                                }
                            } else if (jsonItem.jValue instanceof String) {
                                String stringValue = (String) jsonItem.jValue;
                                try {
                                    if (stringValue.equalsIgnoreCase("false") || stringValue.equalsIgnoreCase("true")) {
                                        boolean booleanValue = Boolean.parseBoolean(stringValue);
                                        base.put(jsonItem.jKey, booleanValue);
                                    } else if (stringValue.contains(".") || stringValue.contains("e") || stringValue.contains("E")) {
                                        base.put(jsonItem.jKey, jsonItem.jValue);
                                    } else {
                                        int intValue = Integer.parseInt(stringValue);
                                        base.put(jsonItem.jKey, intValue);
                                    }
                                } catch (Exception ignored) {
                                    base.put(jsonItem.jKey, jsonItem.jValue);
                                }
                            } else if (jsonItem.jValue instanceof Boolean) {
                                boolean booleanValue = (Boolean) jsonItem.jValue;
                                base.put(jsonItem.jKey, booleanValue);
                            } else {
                                base.put(jsonItem.jKey, jsonItem.jValue);
                            }
                        }
                    }

                    if (GetMaterialName(matDb, base.get("id").toString()) != null) {
                        Toast.makeText(getApplicationContext(), "ID: " + base.get("id") + " already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (base.get("id").toString().isBlank() || base.get("id").toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "ID cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (base.get("id").toString().length() != 5) {
                        Toast.makeText(getApplicationContext(), "ID must be 5 digits", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (base.get("brand").toString().isBlank() || base.get("brand").toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Brand cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (base.get("name").toString().isBlank() || base.get("name").toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (base.get("meterialType").toString().isBlank() || base.get("meterialType").toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "MeterialType cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    info.put("base", base);
                    addFilament(matDb, info);
                    setMatDb(PrinterType);
                } catch (Exception ignored) {
                }
                addDialog.dismiss();
            });

            recyclerView = adl.recyclerView;

            LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
            layoutManager1.setOrientation(LinearLayoutManager.VERTICAL);
            layoutManager1.scrollToPosition(0);
            recyclerView.setLayoutManager(layoutManager1);

            JSONObject info = new JSONObject(GetMaterialInfo(matDb, MaterialID));
            JSONObject base = info.getJSONObject("base");

            jsonItems = new jsonItem[base.length()];
            int i = 0;
            for (Iterator<String> it = base.keys(); it.hasNext(); ) {
                String key = it.next();
                jsonItems[i] = new jsonItem();
                jsonItems[i].jKey = key;
                jsonItems[i].jValue = base.get(key);
                jsonItems[i].hintValue = base.get(key).toString();
                i++;
            }
            recycleAdapter = new jsonAdapter(getBaseContext(), jsonItems);
            recycleAdapter.setHasStableIds(true);

            runOnUiThread(() -> {
                recyclerView.removeAllViewsInLayout();
                recyclerView.setAdapter(null);
                recyclerView.setAdapter(recycleAdapter);
            });

            addDialog.show();
        } catch (Exception ignored) {
        }
    }


    void openUpload() {
        try {
            saveDialog = new Dialog(this, R.style.Theme_SpoolID);
            saveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            saveDialog.setCanceledOnTouchOutside(false);
            saveDialog.setTitle("Upload to Printer");
            SaveDialogBinding sdl = SaveDialogBinding.inflate(getLayoutInflater());
            View rv = sdl.getRoot();
            saveDialog.setContentView(rv);
            SpannableStringBuilder spannableString = new SpannableStringBuilder(String.format(Locale.getDefault(), getString(R.string.upload_desc_printer), PrinterType.toUpperCase()));
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#1976D2")), 41, 43, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            sdl.updatedesc.setText(spannableString);


            sdl.chkprevent.setOnCheckedChangeListener((buttonView, isChecked) -> SaveSetting(this, "prevent_" + PrinterType, isChecked));
            sdl.chkprevent.setChecked(GetSetting(this, "prevent_" + PrinterType, true));


            sdl.chkreboot.setOnCheckedChangeListener((buttonView, isChecked) -> SaveSetting(this, "reboot_" + PrinterType, isChecked));
            sdl.chkreboot.setChecked(GetSetting(this, "reboot_" + PrinterType, true));

            sdl.chkreset.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SpannableStringBuilder ss = new SpannableStringBuilder(spannableString);
                if (isChecked) {
                    sdl.btnupload.setText(R.string.reset);
                    sdl.chkreboot.setVisibility(View.INVISIBLE);
                    sdl.chkprevent.setVisibility(View.INVISIBLE);
                    sdl.chkresetapp.setVisibility(View.VISIBLE);
                    sdl.updatedesc.setText(ss.replace(72, 78, "reset"));
                } else {
                    sdl.btnupload.setText(R.string.upload);
                    sdl.chkreboot.setVisibility(View.VISIBLE);
                    sdl.chkprevent.setVisibility(View.VISIBLE);
                    sdl.chkresetapp.setVisibility(View.INVISIBLE);
                    sdl.updatedesc.setText(ss.replace(72, 78, "update"));
                }
            });

            String sshDefault;
            if (PrinterType.equalsIgnoreCase("hi")) {
                sshDefault = "Creality2024";
            } else if (PrinterType.equalsIgnoreCase("k1")) {
                sshDefault = "creality_2023";
            } else {
                sshDefault = "creality_2024";
            }
            sdl.txtpsw.setText(GetSetting(this, "psw_" + PrinterType, sshDefault));
            sdl.txtaddress.setText(GetSetting(this, "host_" + PrinterType, ""));

            sdl.btncls.setOnClickListener(v -> saveDialog.dismiss());

            sdl.btnupload.setOnClickListener(v -> {
                String host = sdl.txtaddress.getText().toString();
                String psw = sdl.txtpsw.getText().toString();
                SaveSetting(this, "host_" + PrinterType, host);
                SaveSetting(this, "psw_" + PrinterType, psw);
                boolean reboot = sdl.chkreboot.isChecked();
                boolean resetapp = sdl.chkresetapp.isChecked();
                sdl.txtmsg.setTextColor(getResources().getColor(R.color.text_color));
                if (sdl.chkreset.isChecked()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Warning!");
                    if (resetapp) {
                        builder.setMessage("This will restore the default printer and app databases");
                    } else {
                        builder.setMessage("This will restore the default printer database");
                    }
                    builder.setPositiveButton("Reset", (dialog, which) -> {
                        sdl.txtmsg.setText(R.string.resetting);
                        new Thread(() -> {
                            try {
                                if (resetapp) {
                                    matDb.deleteAll();
                                    populateDatabase(this, matDb, null, PrinterType);
                                    runOnUiThread(() -> setMatDb(PrinterType));
                                }
                                restorePrinterDB(this, psw, host, PrinterType);
                                runOnUiThread(() -> {
                                    sdl.txtmsg.setTextColor(ContextCompat.getColor(this, R.color.text_color));
                                    if (resetapp) {
                                        sdl.txtmsg.setText(R.string.printer_and_app_databases_have_been_reset);
                                    } else {
                                        sdl.txtmsg.setText(R.string.printer_database_has_been_reset);
                                    }
                                });

                            } catch (Exception ignored) {
                                runOnUiThread(() -> {
                                    sdl.txtmsg.setTextColor(Color.RED);
                                    sdl.txtmsg.setText(R.string.error_resetting_database);
                                });
                            }
                        }).start();
                        dialog.dismiss();
                    });
                    builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
                    AlertDialog alert = builder.create();
                    alert.show();
                    return;
                }

                final String version;
                if (sdl.chkprevent.isChecked()) {
                    version = "9876543210";
                } else {
                    version = String.format("%s", GetSetting(this, "version_" + PrinterType, -1L));
                }
                new Thread(() -> {
                    try {
                        if (host.isEmpty()) {
                            runOnUiThread(() -> {
                                sdl.txtmsg.setTextColor(Color.RED);
                                sdl.txtmsg.setText(R.string.please_enter_printer_ip_address);
                            });
                            return;
                        }
                        if (psw.isEmpty()) {
                            runOnUiThread(() -> {
                                sdl.txtmsg.setTextColor(Color.RED);
                                sdl.txtmsg.setText(R.string.please_enter_ssh_password);
                            });
                            return;
                        }
                        runOnUiThread(() -> sdl.txtmsg.setText(R.string.uploading));
                        saveDBToPrinter(matDb, psw, host, PrinterType, version, reboot);
                        runOnUiThread(() -> {
                            sdl.txtmsg.setTextColor(ContextCompat.getColor(this, R.color.text_color));
                            sdl.txtmsg.setText(R.string.upload_successful);
                        });

                    } catch (Exception ignored) {

                        runOnUiThread(() -> {
                            sdl.txtmsg.setTextColor(Color.RED);
                            sdl.txtmsg.setText(R.string.error_uploading_to_printer);
                        });

                    }
                }).start();
            });
            saveDialog.show();
        } catch (Exception ignored) {
        }
    }
}