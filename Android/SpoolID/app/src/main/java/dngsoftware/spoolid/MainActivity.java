package dngsoftware.spoolid;

import static java.lang.String.format;
import dngsoftware.spoolid.databinding.ActivityMainBinding;
import dngsoftware.spoolid.databinding.InfoDialogBinding;
import dngsoftware.spoolid.databinding.UpdateDialogBinding;
import dngsoftware.spoolid.databinding.PickerDialogBinding;
import dngsoftware.spoolid.databinding.ManualDialogBinding;
import static dngsoftware.spoolid.Utils.GetMaterialID;
import static dngsoftware.spoolid.Utils.GetMaterialInfo;
import static dngsoftware.spoolid.Utils.GetMaterialLength;
import static dngsoftware.spoolid.Utils.GetMaterialName;
import static dngsoftware.spoolid.Utils.GetMaterialWeight;
import static dngsoftware.spoolid.Utils.GetSetting;
import static dngsoftware.spoolid.Utils.SaveSetting;
import static dngsoftware.spoolid.Utils.SetPermissions;
import static dngsoftware.spoolid.Utils.createKey;
import static dngsoftware.spoolid.Utils.cipherData;
import static dngsoftware.spoolid.Utils.dp2Px;
import static dngsoftware.spoolid.Utils.getDBVersion;
import static dngsoftware.spoolid.Utils.getJsonDB;
import static dngsoftware.spoolid.Utils.getMaterialBrands;
import static dngsoftware.spoolid.Utils.playBeep;
import static dngsoftware.spoolid.Utils.bytesToHex;
import static dngsoftware.spoolid.Utils.canMfc;
import static dngsoftware.spoolid.Utils.getMaterials;
import static dngsoftware.spoolid.Utils.getPixelColor;
import static dngsoftware.spoolid.Utils.materialWeights;
import static dngsoftware.spoolid.Utils.populateDatabase;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
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
import androidx.room.Room;
import org.json.JSONObject;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback{
    private MatDB matDb;
    ArrayAdapter<String> badapter, sadapter, madapter;
    private NfcAdapter nfcAdapter;
    Tag currentTag = null;
    int SelectedSize, SelectedBrand;
    String MaterialName, MaterialWeight, MaterialColor;
    Dialog pickerDialog, customDialog, infoDialog, updateDialog;
    boolean encrypted = false;
    byte[] encKey;
    private ActivityMainBinding main;
    private ManualDialogBinding manual; ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = ActivityMainBinding.inflate(getLayoutInflater());
        View rv = main.getRoot();
        setContentView(rv);

        filamentDB rdb = Room.databaseBuilder(this, filamentDB.class, "filament_database")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
        matDb = rdb.matDB();

        if (matDb.getItemCount() == 0) {
            populateDatabase(this, matDb,null);
        } else {
            long dbVersion = GetSetting(this, "version", -1L);
            if (getDBVersion(this) > dbVersion) {
                matDb.deleteAll();
                populateDatabase(this, matDb,null);
            }
        }

        SetPermissions(this);
        if (!canMfc(this)) {
            Toast.makeText(getApplicationContext(), R.string.this_device_does_not_support_mifare_classic_tags, Toast.LENGTH_SHORT).show();
        }

        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                Bundle options = new Bundle();
                options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);
                nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A, options);
            }else {
                Toast.makeText(getApplicationContext(), R.string.please_activate_nfc, Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                finish();
            }
        } catch (Exception ignored) {}

        main.colorview.setBackgroundColor(Color.argb(255, 0, 0, 255));
        MaterialColor = "0000FF";

        main.colorview.setOnClickListener(view -> openPicker());
        main.readbutton.setOnClickListener(view -> ReadSpoolData());
        main.cbtn.setOnClickListener(view -> openCustom());
        main.writebutton.setOnClickListener(view -> WriteSpoolData(GetMaterialID(matDb, MaterialName), MaterialColor, GetMaterialLength(MaterialWeight)));
        main.ibtn.setOnClickListener(view -> openMaterialInfo());
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

        badapter = new ArrayAdapter<>(this, R.layout.spinner_item,  getMaterialBrands(matDb));
        main.brand.setAdapter(badapter);
        main.brand.setSelection(SelectedBrand);

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

        madapter = new ArrayAdapter<>(this, R.layout.spinner_item, getMaterials(matDb, badapter.getItem(main.brand.getSelectedItemPosition())));
        main. material.setAdapter(madapter);
        main.material.setSelection(madapter.getPosition(MaterialName));
        main.material.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                MaterialName = madapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        ReadTagUID(getIntent());
    }

    void setMaterial(String brand) {
        madapter = new ArrayAdapter<>(this, R.layout.spinner_item, getMaterials(matDb, brand));
        main.material.setAdapter(madapter);
        main.material.setSelection(madapter.getPosition(MaterialName));
        main.material.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                MaterialName = madapter.getItem(position);
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
        if (infoDialog != null && infoDialog.isShowing()) {
            infoDialog.dismiss();
        }
        if (updateDialog != null && updateDialog.isShowing()) {
            updateDialog.dismiss();
        }
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            try {
                nfcAdapter.disableReaderMode(this);
            } catch (Exception ignored) {}
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
        } catch (Exception ignored) {}
    }

    void ReadTagUID(Intent intent) {
        if (intent != null) {
            try {
                if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                    currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    assert currentTag != null;
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
        }
        else {
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
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.error_formatting_tag, Toast.LENGTH_SHORT).show();
        }
    }

    void ReadSpoolData() {
        String tagData = ReadTag();
        if (tagData != null && tagData.length() >= 40) {
            String MaterialID = tagData.substring(12, 17);
            if (GetMaterialName(matDb, MaterialID) != null) {
                MaterialColor = tagData.substring(18, 24);
                String Length = tagData.substring(24, 28);
                main.colorview.setBackgroundColor(Color.parseColor("#" + MaterialColor));
                MaterialName = Objects.requireNonNull(GetMaterialName(matDb, MaterialID))[0];
                main.material.setSelection(madapter.getPosition(MaterialName));
                main.brand.setSelection(badapter.getPosition(Objects.requireNonNull(GetMaterialName(matDb, MaterialID))[1]));
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
                }else {
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

                    } catch (Exception ignored) {}
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
        } catch (Exception ignored) {}
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
                }
                else {
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
        } catch (Exception ignored) {}
    }

    void openMaterialInfo() {
        try {
            infoDialog = new Dialog(this, R.style.Theme_SpoolID);
            infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            infoDialog.setCanceledOnTouchOutside(false);
            infoDialog.setTitle(R.string.filament_info);
            InfoDialogBinding dl = InfoDialogBinding.inflate(getLayoutInflater());
            View rv = dl.getRoot();
            infoDialog.setContentView(rv);
            dl.btncls.setOnClickListener(v -> infoDialog.dismiss());
            StringBuilder sb = new StringBuilder();
            sb.append(MaterialName);
            sb.append("\n\n");
            JSONObject info = new JSONObject(GetMaterialInfo(matDb, MaterialName));
            for (Iterator<String> it = info.keys(); it.hasNext(); ) {
                String key = it.next();
                Object value = info.get(key);
                sb.append(key);
                sb.append(": ");
                sb.append(value);
                sb.append("\n");
            }
            dl.txtinfo.setText(sb.toString());
            infoDialog.show();
        } catch (Exception ignored) {}
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

            dl.btncls.setOnClickListener(v -> updateDialog.dismiss());
            dl.btnupd.setVisibility(View.INVISIBLE);
            String hostString = GetSetting(this, "host", "");
            dl.txtaddress.setText(hostString);
            dl.txtcurver.setText(String.format(Locale.getDefault(), getString(R.string.current_version), GetSetting(this, "version", -1L)));

            dl.btnchk.setOnClickListener(v -> {
                String host = dl.txtaddress.getText().toString();
                long version = GetSetting(this, "version", -1L);
                dl.txtcurver.setText(String.format(Locale.getDefault(), getString(R.string.current_version), version));
                if (!host.isEmpty()) {
                    SaveSetting(this, "host", host);
                    new Thread(() -> {
                        try {
                            String json = getJsonDB(host);
                            if (json != null && json.contains("\"kvParam\":{")) {
                                JSONObject materials = new JSONObject(json);
                                JSONObject result = new JSONObject(materials.getString("result"));
                                long newVer = result.getLong("version");
                                runOnUiThread(() -> dl.txtnewver.setText(format(Locale.getDefault(), getString(R.string.printer_version), newVer)));
                                runOnUiThread(() -> {
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
                            }else {
                                runOnUiThread(() -> {
                                    dl.txtmsg.setTextColor(Color.RED);
                                    dl.txtmsg.setText(R.string.unable_to_download_file_from_printer);
                                });
                            }
                        } catch (Exception ignored) {}
                    }).start();
                }
            });

            dl.btnupd.setOnClickListener(v -> {
                String host = dl.txtaddress.getText().toString();
                if (!host.isEmpty()) {
                    SaveSetting(this, "host", host);
                    new Thread(() -> {
                        try {
                            String json = getJsonDB(host);
                            if (json != null && json.contains("\"kvParam\":{")) {
                                JSONObject materials = new JSONObject(json);
                                JSONObject result = new JSONObject(materials.getString("result"));
                                long newVer = result.getLong("version");
                                matDb.deleteAll();
                                populateDatabase(this, matDb, json);
                                SaveSetting(this, "version", newVer);
                                runOnUiThread(() -> {
                                    dl.txtcurver.setText(String.format(Locale.getDefault(), getString(R.string.current_version), newVer));
                                    dl.btnupd.setVisibility(View.INVISIBLE);
                                    dl.txtmsg.setTextColor(ContextCompat.getColor(this, R.color.text_color));
                                    dl.txtmsg.setText(R.string.update_successful);
                                    setMaterial(badapter.getItem(SelectedBrand));
                                });
                            }else {
                                runOnUiThread(() -> {
                                    dl.txtmsg.setTextColor(Color.RED);
                                    dl.txtmsg.setText(R.string.unable_to_download_file_from_printer);
                                });
                            }
                        } catch (Exception ignored) {}
                    }).start();
                }
            });
            updateDialog.show();
        } catch (Exception ignored) {}
    }

}