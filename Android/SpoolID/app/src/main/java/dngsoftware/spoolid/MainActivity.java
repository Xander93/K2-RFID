package dngsoftware.spoolid;

import static java.lang.String.format;
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
import static dngsoftware.spoolid.Utils.playBeep;
import static dngsoftware.spoolid.Utils.materialBrands;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.google.android.material.materialswitch.MaterialSwitch;
import org.json.JSONObject;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private MatDB matDb;
    ArrayAdapter<String> badapter, sadapter, madapter;
    Spinner brand, spoolsize, material;
    nAdapter nfcReader = null;
    Tag currentTag = null;
    int SelectedSize, SelectedBrand;
    String MaterialName, MaterialWeight, MaterialColor;
    TextView tagID;
    View colorView;
    EditText txtcolor;
    Dialog pickerDialog, customDialog, infoDialog, updateDialog;
    MaterialSwitch autoread;
    boolean encrypted = false;
    byte[] encKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button rbtn = findViewById(R.id.readbutton);
        Button wbtn = findViewById(R.id.writebutton);
        ImageView cbtn = findViewById(R.id.cbtn);
        ImageView ibtn = findViewById(R.id.ibtn);
        ImageView ubtn = findViewById(R.id.ubtn);
        colorView = findViewById(R.id.colorview);
        Spinner colorspin = findViewById(R.id.colorspin);
        autoread = findViewById(R.id.autoread);
        tagID = findViewById(R.id.tagid);
        brand = findViewById(R.id.brand);
        spoolsize = findViewById(R.id.spoolsize);
        material = findViewById(R.id.material);

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

        nfcReader = new nAdapter(this);

        colorView.setBackgroundColor(Color.argb(255, 0, 0, 255));
        MaterialColor = "0000FF";

        colorView.setOnClickListener(view -> openPicker());
        rbtn.setOnClickListener(view -> ReadSpoolData());
        cbtn.setOnClickListener(view -> openCustom());
        wbtn.setOnClickListener(view -> WriteSpoolData(GetMaterialID(matDb, MaterialName), MaterialColor, GetMaterialLength(MaterialWeight)));
        ibtn.setOnClickListener(view -> openMaterialInfo());
        ubtn.setOnClickListener(view -> openUpdate());

        colorspin.setOnTouchListener((v, event) -> {
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

        autoread.setChecked(GetSetting(this, "autoread", false));
        autoread.setOnCheckedChangeListener((buttonView, isChecked) -> SaveSetting(this, "autoread", isChecked));

        badapter = new ArrayAdapter<>(this, R.layout.spinner_item, materialBrands);
        brand.setAdapter(badapter);
        brand.setSelection(SelectedBrand);

        brand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                SelectedBrand = brand.getSelectedItemPosition();
                setMaterial(badapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        sadapter = new ArrayAdapter<>(this, R.layout.spinner_item, materialWeights);
        spoolsize.setAdapter(sadapter);
        spoolsize.setSelection(SelectedSize);
        spoolsize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                SelectedSize = spoolsize.getSelectedItemPosition();
                MaterialWeight = sadapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        madapter = new ArrayAdapter<>(this, R.layout.spinner_item, getMaterials(matDb, badapter.getItem(brand.getSelectedItemPosition())));
        material.setAdapter(madapter);
        material.setSelection(madapter.getPosition(MaterialName));
        material.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        material.setAdapter(madapter);
        material.setSelection(madapter.getPosition(MaterialName));
        material.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
    public void onResume() {
        super.onResume();
        try {
            nfcReader.enableForeground();
            if (!nfcReader.getNfc().isEnabled()) {
                Toast.makeText(getApplicationContext(), R.string.please_activate_nfc, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        } catch (Exception ignored) {
        }
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
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            nfcReader.disableForeground();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ReadTagUID(intent);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (pickerDialog != null && pickerDialog.isShowing()) {
            pickerDialog.dismiss();
            openPicker();
        }
    }

    void ReadTagUID(Intent intent) {
        if (intent != null) {
            try {
                if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
                    currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    assert currentTag != null;
                    Toast.makeText(getApplicationContext(), getString(R.string.tag_found) + bytesToHex(currentTag.getId()), Toast.LENGTH_SHORT).show();
                    tagID.setText(bytesToHex(currentTag.getId()));
                    encKey = createKey(currentTag.getId());
                    CheckTag();
                    if (encrypted) {
                        tagID.setText(String.format("\uD83D\uDD10 %s", bytesToHex(currentTag.getId())));
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
                            tagID.setText(String.format("\uD83D\uDD10 %s", bytesToHex(currentTag.getId())));
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
                            tagID.setText(bytesToHex(currentTag.getId()));
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
    }

    void ReadSpoolData() {
        String tagData = ReadTag();
        if (tagData != null && tagData.length() >= 40) {
            String MaterialID = tagData.substring(12, 17);
            if (GetMaterialName(matDb, MaterialID) != null) {
                MaterialColor = tagData.substring(18, 24);
                String Length = tagData.substring(24, 28);
                colorView.setBackgroundColor(Color.parseColor("#" + MaterialColor));
                MaterialName = Objects.requireNonNull(GetMaterialName(matDb, MaterialID))[0];
                material.setSelection(madapter.getPosition(MaterialName));
                brand.setSelection(badapter.getPosition(Objects.requireNonNull(GetMaterialName(matDb, MaterialID))[1]));
                spoolsize.setSelection(sadapter.getPosition(GetMaterialWeight(Length)));
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
            pickerDialog.setContentView(R.layout.picker_dialog);
            pickerDialog.setCanceledOnTouchOutside(false);
            pickerDialog.setTitle(R.string.pick_color);
            final Button btnCls = pickerDialog.findViewById(R.id.btncls);
            EditText colorTxt = pickerDialog.findViewById(R.id.txtcolor);
            View dcolorView = pickerDialog.findViewById(R.id.dcolorview);
            ImageView picker = pickerDialog.findViewById(R.id.picker);
            dcolorView.setBackgroundColor(Color.parseColor("#" + MaterialColor));
            colorTxt.setText(MaterialColor);
            btnCls.setOnClickListener(v -> {
                if (customDialog != null && customDialog.isShowing()) {
                    txtcolor.setText(String.format("0%s", MaterialColor));
                }else {
                    if (colorTxt.getText().toString().length() == 6) {
                        try {
                            MaterialColor = colorTxt.getText().toString();
                            colorView.setBackgroundColor(Color.parseColor("#" + MaterialColor));
                            dcolorView.setBackgroundColor(Color.parseColor("#" + MaterialColor));
                        } catch (Exception ignored) {
                        }
                    }
                }
                pickerDialog.dismiss();
            });
            colorTxt.setOnEditorActionListener((v, actionId, event) -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(colorTxt.getWindowToken(), 0);
                if (colorTxt.getText().toString().length() == 6) {
                    try {
                        MaterialColor = colorTxt.getText().toString();
                        colorView.setBackgroundColor(Color.parseColor("#" + MaterialColor));
                        dcolorView.setBackgroundColor(Color.parseColor("#" + MaterialColor));

                    } catch (Exception ignored) {}
                }
                return true;
            });
            picker.setOnTouchListener((v, event) -> {
                final int currPixel = getPixelColor(event, picker);
                if (currPixel != 0) {
                    MaterialColor = format("%02x%02x%02x", Color.red(currPixel), Color.green(currPixel), Color.blue(currPixel)).toUpperCase();
                    if (customDialog != null && customDialog.isShowing()) {
                        txtcolor.setText(String.format("0%s", MaterialColor));
                    } else {
                        colorView.setBackgroundColor(Color.argb(255, Color.red(currPixel), Color.green(currPixel), Color.blue(currPixel)));
                        dcolorView.setBackgroundColor(Color.argb(255, Color.red(currPixel), Color.green(currPixel), Color.blue(currPixel)));
                    }
                    pickerDialog.dismiss();
                }
                return false;
            });
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            float scrWwidth = displayMetrics.widthPixels;
            if (scrWwidth > dp2Px(this, 500)) scrWwidth = dp2Px(this, 500);
            SeekBar seekBarFont = pickerDialog.findViewById(R.id.seekbar_font);
            LinearGradient test = new LinearGradient(50.f, 0.f, scrWwidth - 250, 0.0f, new int[]{0xFF000000, 0xFF0000FF, 0xFF00FF00, 0xFF00FFFF, 0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00, 0xFFFFFFFF}, null, Shader.TileMode.CLAMP);
            ShapeDrawable shape = new ShapeDrawable(new RectShape());
            shape.getPaint().setShader(test);
            seekBarFont.setProgressDrawable(shape);
            seekBarFont.setMax(256 * 7 - 1);
            seekBarFont.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                        colorTxt.setText(MaterialColor);
                        colorView.setBackgroundColor(Color.argb(255, r, g, b));
                        dcolorView.setBackgroundColor(Color.argb(255, r, g, b));
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
            customDialog.setContentView(R.layout.manual_dialog);
            customDialog.setCanceledOnTouchOutside(false);
            customDialog.setTitle(R.string.custom_tag_data);
            final Button btnread = customDialog.findViewById(R.id.btnread);
            final Button btnwrite = customDialog.findViewById(R.id.btnwrite);
            final Button btncls = customDialog.findViewById(R.id.btncls);
            final EditText txtmonth = customDialog.findViewById(R.id.txtmonth);
            final EditText txtday = customDialog.findViewById(R.id.txtday);
            final EditText txtyear = customDialog.findViewById(R.id.txtyear);
            final EditText txtvendor = customDialog.findViewById(R.id.txtvendor);
            final EditText txtbatch = customDialog.findViewById(R.id.txtbatch);
            final EditText txtmaterial = customDialog.findViewById(R.id.txtmaterial);
            txtcolor = customDialog.findViewById(R.id.txtcolor);
            final EditText txtlength = customDialog.findViewById(R.id.txtlength);
            final EditText txtserial = customDialog.findViewById(R.id.txtserial);
            final EditText txtreserve = customDialog.findViewById(R.id.txtreserve);
            final ImageView btnfmt = customDialog.findViewById(R.id.btnfmt);
            final ImageView btnrst = customDialog.findViewById(R.id.btnrst);
            final ImageView btnrnd = customDialog.findViewById(R.id.btnrnd);
            final ImageView btncol = customDialog.findViewById(R.id.btncol);
            txtmonth.setText(GetSetting(this, "mon", getResources().getString(R.string.def_mon)));
            txtday.setText(GetSetting(this, "day", getResources().getString(R.string.def_day)));
            txtyear.setText(GetSetting(this, "yr", getResources().getString(R.string.def_yr)));
            txtvendor.setText(GetSetting(this, "ven", getResources().getString(R.string.def_ven)));
            txtbatch.setText(GetSetting(this, "bat", getResources().getString(R.string.def_bat)));
            txtmaterial.setText(GetSetting(this, "mat", getResources().getString(R.string.def_mat)));
            txtcolor.setText(GetSetting(this, "col", getResources().getString(R.string.def_col)));
            txtlength.setText(GetSetting(this, "len", getResources().getString(R.string.def_len)));
            txtserial.setText(GetSetting(this, "ser", getResources().getString(R.string.def_ser)));
            txtreserve.setText(GetSetting(this, "res", getResources().getString(R.string.def_res)));
            btncls.setOnClickListener(v -> customDialog.dismiss());
            btncol.setOnClickListener(view -> openPicker());
            btnrnd.setOnClickListener(v -> {
                SecureRandom random = new SecureRandom();
                txtserial.setText(format(Locale.getDefault(), "%06d", random.nextInt(900000)));
            });
            btnread.setOnClickListener(v -> {
                String tagData = ReadTag();
                if (tagData != null) {
                    if (!tagData.startsWith("\0")) {
                        txtmonth.setText(tagData.substring(0, 1).toUpperCase());
                        txtday.setText(tagData.substring(1, 3).toUpperCase());
                        txtyear.setText(tagData.substring(3, 5).toUpperCase());
                        txtvendor.setText(tagData.substring(5, 9).toUpperCase());
                        txtbatch.setText(tagData.substring(9, 11).toUpperCase());
                        txtmaterial.setText(tagData.substring(11, 17).toUpperCase());
                        txtcolor.setText(tagData.substring(17, 24).toUpperCase());
                        txtlength.setText(tagData.substring(24, 28).toUpperCase());
                        txtserial.setText(tagData.substring(28, 34).toUpperCase());
                        txtreserve.setText(tagData.substring(34, 40).toUpperCase());
                        Toast.makeText(getApplicationContext(), R.string.data_read_from_tag, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.unknown_or_empty_tag, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            btnwrite.setOnClickListener(v -> {
                if (txtmonth.getText().length() == 1 && txtday.getText().length() == 2 && txtyear.getText().length() == 2
                        && txtvendor.getText().length() == 4 && txtbatch.getText().length() == 2 && txtmaterial.getText().length() == 6
                        && txtcolor.getText().length() == 7 && txtlength.getText().length() == 4
                        && txtserial.getText().length() == 6 && txtreserve.getText().length() == 6) {
                    WriteTag(txtmonth.getText().toString() + txtday.getText().toString() + txtyear.getText().toString()
                            + txtvendor.getText().toString() + txtbatch.getText().toString() + txtmaterial.getText().toString() + txtcolor.getText().toString()
                            + txtlength.getText().toString() + txtserial.getText().toString() + txtreserve.getText().toString());
                    SaveSetting(this, "mon", txtmonth.getText().toString().toUpperCase());
                    SaveSetting(this, "day", txtday.getText().toString().toUpperCase());
                    SaveSetting(this, "yr", txtyear.getText().toString().toUpperCase());
                    SaveSetting(this, "ven", txtvendor.getText().toString().toUpperCase());
                    SaveSetting(this, "bat", txtbatch.getText().toString().toUpperCase());
                    SaveSetting(this, "mat", txtmaterial.getText().toString().toUpperCase());
                    SaveSetting(this, "col", txtcolor.getText().toString().toUpperCase());
                    SaveSetting(this, "len", txtlength.getText().toString().toUpperCase());
                    SaveSetting(this, "ser", txtserial.getText().toString().toUpperCase());
                    SaveSetting(this, "res", txtreserve.getText().toString().toUpperCase());
                } else {
                    Toast.makeText(getApplicationContext(), R.string.incorrect_tag_data_length, Toast.LENGTH_SHORT).show();
                }
            });
            btnfmt.setOnClickListener(v -> {
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
            btnrst.setOnClickListener(v -> {
                txtmonth.setText(R.string.def_mon);
                txtday.setText(R.string.def_day);
                txtyear.setText(R.string.def_yr);
                txtvendor.setText(R.string.def_ven);
                txtbatch.setText(R.string.def_bat);
                txtmaterial.setText(R.string.def_mat);
                txtcolor.setText(R.string.def_col);
                txtlength.setText(R.string.def_len);
                txtserial.setText(R.string.def_ser);
                txtreserve.setText(R.string.def_res);
                SaveSetting(this, "mon", txtmonth.getText().toString().toUpperCase());
                SaveSetting(this, "day", txtday.getText().toString().toUpperCase());
                SaveSetting(this, "yr", txtyear.getText().toString().toUpperCase());
                SaveSetting(this, "ven", txtvendor.getText().toString().toUpperCase());
                SaveSetting(this, "bat", txtbatch.getText().toString().toUpperCase());
                SaveSetting(this, "mat", txtmaterial.getText().toString().toUpperCase());
                SaveSetting(this, "col", txtcolor.getText().toString().toUpperCase());
                SaveSetting(this, "len", txtlength.getText().toString().toUpperCase());
                SaveSetting(this, "ser", txtserial.getText().toString().toUpperCase());
                SaveSetting(this, "res", txtreserve.getText().toString().toUpperCase());
                Toast.makeText(getApplicationContext(), R.string.values_reset, Toast.LENGTH_SHORT).show();
            });
            customDialog.show();
        } catch (Exception ignored) {}
    }

    void openMaterialInfo() {
        try {
            infoDialog = new Dialog(this, R.style.Theme_SpoolID);
            infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            infoDialog.setContentView(R.layout.info_dialog);
            infoDialog.setCanceledOnTouchOutside(false);
            infoDialog.setTitle(R.string.filament_info);
            final ImageView btncls = infoDialog.findViewById(R.id.btncls);
            final TextView txtinfo = infoDialog.findViewById(R.id.txtinfo);
            btncls.setOnClickListener(v -> infoDialog.dismiss());
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
            txtinfo.setText(sb.toString());
            infoDialog.show();
        } catch (Exception ignored) {}
    }

    void openUpdate() {
        try {
            updateDialog = new Dialog(this, R.style.Theme_SpoolID);
            updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            updateDialog.setContentView(R.layout.update_dialog);
            updateDialog.setCanceledOnTouchOutside(false);
            updateDialog.setTitle(R.string.update);
            final Button btncls = updateDialog.findViewById(R.id.btncls);
            final Button btnupd = updateDialog.findViewById(R.id.btnupd);
            final Button btnchk = updateDialog.findViewById(R.id.btnchk);
            final TextView txtcurver = updateDialog.findViewById(R.id.txtcurver);
            final TextView txtnewver = updateDialog.findViewById(R.id.txtnewver);
            final TextView txtmsg = updateDialog.findViewById(R.id.txtmsg);
            final EditText txtaddress = updateDialog.findViewById(R.id.txtaddress);
            btncls.setOnClickListener(v -> updateDialog.dismiss());
            btnupd.setVisibility(View.INVISIBLE);
            String hostString = GetSetting(this, "host", "");
            txtaddress.setText(hostString);
            txtcurver.setText(String.format(Locale.getDefault(), getString(R.string.current_version), GetSetting(this, "version", -1L)));

            btnchk.setOnClickListener(v -> {
                String host = txtaddress.getText().toString();
                long version = GetSetting(this, "version", -1L);
                txtcurver.setText(String.format(Locale.getDefault(), getString(R.string.current_version), version));
                if (!host.isEmpty()) {
                    SaveSetting(this, "host", host);
                    new Thread(() -> {
                        try {
                            String json = getJsonDB(host);
                            if (json != null && json.contains("\"kvParam\":{")) {
                                JSONObject materials = new JSONObject(json);
                                JSONObject result = new JSONObject(materials.getString("result"));
                                long newVer = result.getLong("version");
                                runOnUiThread(() -> txtnewver.setText(format(Locale.getDefault(), getString(R.string.printer_version), newVer)));
                                runOnUiThread(() -> {
                                    if (newVer > version) {
                                        btnupd.setVisibility(View.VISIBLE);
                                        txtmsg.setTextColor(getColor(R.color.text_color));
                                        txtmsg.setText(R.string.update_available);
                                    } else {
                                        btnupd.setVisibility(View.INVISIBLE);
                                        txtmsg.setTextColor(getColor(R.color.text_color));
                                        txtmsg.setText(R.string.no_update_available);
                                    }
                                });
                            }else {
                                runOnUiThread(() -> {
                                    txtmsg.setTextColor(Color.RED);
                                    txtmsg.setText(R.string.unable_to_download_file_from_printer);
                                });
                            }
                        } catch (Exception ignored) {}
                    }).start();
                }
            });

            btnupd.setOnClickListener(v -> {
                String host = txtaddress.getText().toString();
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
                                    txtcurver.setText(String.format(Locale.getDefault(), getString(R.string.current_version), newVer));
                                    btnupd.setVisibility(View.INVISIBLE);
                                    txtmsg.setTextColor(getColor(R.color.text_color));
                                    txtmsg.setText(R.string.update_successful);
                                    setMaterial(badapter.getItem(SelectedBrand));
                                });
                            }else {
                                runOnUiThread(() -> {
                                    txtmsg.setTextColor(Color.RED);
                                    txtmsg.setText(R.string.unable_to_download_file_from_printer);
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