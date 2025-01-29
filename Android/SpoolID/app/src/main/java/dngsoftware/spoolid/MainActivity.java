package dngsoftware.spoolid;

import static java.lang.String.format;
import static dngsoftware.spoolid.Utils.GetMaterialID;
import static dngsoftware.spoolid.Utils.GetMaterialLength;
import static dngsoftware.spoolid.Utils.GetMaterialName;
import static dngsoftware.spoolid.Utils.GetMaterialWeight;
import static dngsoftware.spoolid.Utils.GetSetting;
import static dngsoftware.spoolid.Utils.SaveSetting;
import static dngsoftware.spoolid.Utils.SetPermissions;
import static dngsoftware.spoolid.Utils.createKey;
import static dngsoftware.spoolid.Utils.decData;
import static dngsoftware.spoolid.Utils.dp2Px;
import static dngsoftware.spoolid.Utils.encData;
import static dngsoftware.spoolid.Utils.playBeep;
import static dngsoftware.spoolid.Utils.materialBrands;
import static dngsoftware.spoolid.Utils.bytesToHex;
import static dngsoftware.spoolid.Utils.canMfc;
import static dngsoftware.spoolid.Utils.getMaterials;
import static dngsoftware.spoolid.Utils.getPixelColor;
import static dngsoftware.spoolid.Utils.materialWeights;
import android.annotation.SuppressLint;
import android.app.Dialog;
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
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.materialswitch.MaterialSwitch;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{

    ArrayAdapter<String> badapter, sadapter, madapter;
    Spinner brand, spoolsize, material;
    nAdapter nfcReader = null;
    Tag currentTag = null;
    int SelectedSize,SelectedBrand;
    String MaterialName, MaterialWeight, MaterialColor;
    TextView tagID;
    View colorView;
    Dialog pickerDialog, customDialog;
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
        colorView = findViewById(R.id.colorview);
        Spinner colorspin = findViewById(R.id.colorspin);
        autoread = findViewById(R.id.autoread);
        tagID = findViewById(R.id.tagid);
        brand = findViewById(R.id.brand);
        spoolsize = findViewById(R.id.spoolsize);
        material = findViewById(R.id.material);


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

        wbtn.setOnClickListener(view -> WriteSpoolData(GetMaterialID(MaterialName), MaterialColor, GetMaterialLength(MaterialWeight)));

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
            public void onNothingSelected(AdapterView<?> parentView) {}
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
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        madapter = new ArrayAdapter<>(this, R.layout.spinner_item, getMaterials(  badapter.getItem(brand.getSelectedItemPosition()))   );
        material.setAdapter(madapter);
        material.setSelection(madapter.getPosition(MaterialName));
        material.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                MaterialName = madapter.getItem(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        ReadTagUID(getIntent());
    }

    void setMaterial(String brand)
    {
        madapter = new ArrayAdapter<>(this, R.layout.spinner_item, getMaterials(brand));
        material.setAdapter(madapter);
        material.setSelection(madapter.getPosition(MaterialName));
        material.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                MaterialName = madapter.getItem(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
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
        }catch (Exception ignored){}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pickerDialog != null && pickerDialog.isShowing())
        {
            pickerDialog.dismiss();
        }
        if (customDialog != null && customDialog.isShowing())
        {
            customDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            nfcReader.disableForeground();
        }catch (Exception ignored){}
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ReadTagUID(intent);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (pickerDialog != null && pickerDialog.isShowing())
        {
            pickerDialog.dismiss();
            openPicker();
        }
    }

    void ReadTagUID(Intent intent){
        if (intent != null) {
            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
                currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                assert currentTag != null;
                Toast.makeText(getApplicationContext(), getString(R.string.tag_found) + bytesToHex(currentTag.getId()), Toast.LENGTH_SHORT).show();
                tagID.setText(bytesToHex(currentTag.getId()));
                encKey = createKey(currentTag.getId());
                CheckTag();
                if(GetSetting(this, "autoread", false))
                {
                    ReadSpoolData();
                }
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
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.invalid_tag_type, Toast.LENGTH_SHORT).show();
            }
        }
    }

    String ReadEncTag() {
        if (currentTag != null) {
            MifareClassic mfc = MifareClassic.get(currentTag);
            if (mfc != null && mfc.getType() ==  MifareClassic.TYPE_CLASSIC) {
                try {
                    mfc.connect();
                    boolean auth = mfc.authenticateSectorWithKeyA(1, encKey);
                    if (auth) {
                        byte[] data = new byte[48];
                        ByteBuffer buff = ByteBuffer.wrap(data);
                        buff.put(mfc.readBlock(4));
                        buff.put(mfc.readBlock(5));
                        buff.put(mfc.readBlock(6));
                        mfc.close();
                        return decData(buff.array());
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                    }
                    mfc.close();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), R.string.error_reading_tag, Toast.LENGTH_SHORT).show();
                    Log.e("Error", Log.getStackTraceString(e));
                }
                try {
                    mfc.close();
                } catch (Exception ignored) {}
            }else{
                Toast.makeText(getApplicationContext(), R.string.invalid_tag_type, Toast.LENGTH_SHORT).show();
            }
            return null;
        }
        return null;
    }

    String ReadTag() {
        if (currentTag != null) {
            MifareClassic mfc = MifareClassic.get(currentTag);
            if (mfc != null && mfc.getType() == MifareClassic.TYPE_CLASSIC) {
                try {
                    mfc.connect();
                    boolean auth = mfc.authenticateSectorWithKeyA(1, MifareClassic.KEY_DEFAULT);
                    if (auth) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 4; i < 7; i++) {
                            sb.append(new String(mfc.readBlock(i), StandardCharsets.UTF_8));
                        }
                        mfc.close();
                        return sb.toString();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                    }
                    mfc.close();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), R.string.error_reading_tag, Toast.LENGTH_SHORT).show();
                    Log.e("Error", Log.getStackTraceString(e));
                }
                try {
                    mfc.close();
                } catch (Exception ignored) {}
            }else{
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
                    if (encrypted)
                    {
                        key = encKey;
                    }
                    boolean auth = mfc.authenticateSectorWithKeyA(1, key);
                    if (auth) {
                        byte[] sectorData = encData((tagData+"00000000").getBytes());
                        int blockIndex = 4;
                        for (int i = 0; i < sectorData.length; i += MifareClassic.BLOCK_SIZE) {
                            byte[] block = Arrays.copyOfRange(sectorData, i, i + MifareClassic.BLOCK_SIZE);
                            mfc.writeBlock(blockIndex, block);
                            blockIndex++;
                        }
                        if (!encrypted)
                        {
                            byte[]  data = mfc.readBlock(7);
                            System.arraycopy(encKey, 0, data, 0, encKey.length);
                            System.arraycopy(encKey, 0, data, 10, encKey.length);
                            mfc.writeBlock(7, data);
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
                } catch (Exception ignored) {}
            } else {
                Toast.makeText(getApplicationContext(), R.string.invalid_tag_type, Toast.LENGTH_SHORT).show();
            }
        }
    }

    void ReadSpoolData() {
        String tagData;
        if (encrypted)
        {
            tagData = ReadEncTag();
        }
        else {
             tagData = ReadTag();
        }
        if (tagData != null) {
            String MaterialID = tagData.substring(12, 17);
            if (GetMaterialName(MaterialID) != null) {
                MaterialColor = tagData.substring(18, 24);
                String Length = tagData.substring(24, 28);
                colorView.setBackgroundColor(Color.parseColor("#" + MaterialColor));
                MaterialName = Objects.requireNonNull(GetMaterialName(MaterialID))[0];
                material.setSelection(madapter.getPosition(MaterialName));
                brand.setSelection(badapter.getPosition(Objects.requireNonNull(GetMaterialName(MaterialID))[1]));
                spoolsize.setSelection(sadapter.getPosition(GetMaterialWeight(Length)));
                Toast.makeText(getApplicationContext(), R.string.data_read_from_tag, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.unknown_or_empty_tag, Toast.LENGTH_SHORT).show();
            }
        }
    }

    void WriteSpoolData(String MaterialID, String Color, String Length) {
        SecureRandom random = new SecureRandom();
        String filamentId = "1" + MaterialID; //material_database.json
        String vendorId = "0276"; //0276 creality
        String color = "0" + Color;
        String filamentLen = Length;
        String serialNum = format(Locale.getDefault(), "%06d", random.nextInt(100000)); //000001
        String reserve = "000000";
        WriteTag("AB124" + vendorId + "A2" + filamentId + color + filamentLen + serialNum + reserve);
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
            btnCls.setOnClickListener(v -> pickerDialog.dismiss());
            View dcolorView = pickerDialog.findViewById(R.id.dcolorview);
            ImageView picker = pickerDialog.findViewById(R.id.picker);
            dcolorView.setBackgroundColor(Color.parseColor("#" + MaterialColor));
            picker.setOnTouchListener((v, event) -> {
                final int currPixel = getPixelColor(event, picker);
                if (currPixel != 0) {
                    MaterialColor = format("%02x%02x%02x", Color.red(currPixel), Color.green(currPixel), Color.blue(currPixel)).toUpperCase();
                    colorView.setBackgroundColor(Color.argb(255, Color.red(currPixel), Color.green(currPixel), Color.blue(currPixel)));
                    dcolorView.setBackgroundColor(Color.argb(255, Color.red(currPixel), Color.green(currPixel), Color.blue(currPixel)));
                    pickerDialog.dismiss();
                }
                return false;
            });
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            float scrWwidth = displayMetrics.widthPixels;
            if (scrWwidth > dp2Px(this, 500) ) scrWwidth = dp2Px(this, 500);
            SeekBar seekBarFont = pickerDialog.findViewById(R.id.seekbar_font);
            LinearGradient test = new LinearGradient(50.f, 0.f, scrWwidth -250 , 0.0f, new int[]{0xFF000000, 0xFF0000FF, 0xFF00FF00, 0xFF00FFFF, 0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00, 0xFFFFFFFF}, null, Shader.TileMode.CLAMP);
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
                        colorView.setBackgroundColor(Color.argb(255, r, g, b));
                        dcolorView.setBackgroundColor(Color.argb(255, r, g, b));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            pickerDialog.show();
        }catch (Exception ignored){}
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
            final EditText txtcolor = customDialog.findViewById(R.id.txtcolor);
            final EditText txtlength = customDialog.findViewById(R.id.txtlength);
            final EditText txtserial = customDialog.findViewById(R.id.txtserial);
            final EditText txtreserve = customDialog.findViewById(R.id.txtreserve);
            btncls.setOnClickListener(v -> customDialog.dismiss());
            btnread.setOnClickListener(v -> {
                String tagData;
                if (encrypted)
                {
                    tagData = ReadEncTag();
                }
                else {
                    tagData = ReadTag();
                }
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
                } else {
                    Toast.makeText(getApplicationContext(), R.string.incorrect_tag_data_length, Toast.LENGTH_SHORT).show();
                }
            });
            customDialog.show();
        }catch (Exception ignored){}
    }

}