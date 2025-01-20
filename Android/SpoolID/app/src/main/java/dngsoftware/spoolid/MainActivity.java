package dngsoftware.spoolid;

import static java.lang.String.format;
import static dngsoftware.spoolid.Utils.GetMaterialID;
import static dngsoftware.spoolid.Utils.GetMaterialLength;
import static dngsoftware.spoolid.Utils.GetMaterialName;
import static dngsoftware.spoolid.Utils.GetMaterialWeight;
import static dngsoftware.spoolid.Utils.GetSetting;
import static dngsoftware.spoolid.Utils.SaveSetting;
import static dngsoftware.spoolid.Utils.SetPermissions;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.materialswitch.MaterialSwitch;
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
    float scrHeight, scrWwidth;
    Dialog dialog;
    MaterialSwitch autoread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button rbtn = findViewById(R.id.readbutton);
        Button wbtn = findViewById(R.id.writebutton);
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

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        scrHeight = displayMetrics.heightPixels;
        scrWwidth = displayMetrics.widthPixels;

        colorView.setBackgroundColor(Color.argb(255, 0, 0, 255));
        MaterialColor = "0000FF";

        colorView.setOnClickListener(view -> openPicker());

        rbtn.setOnClickListener(view -> ReadSpoolData());

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
                startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
            }
        }catch (Exception ignored){}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            nfcReader.disableForeground();
        }catch (Exception ignored){}
        if (dialog != null && dialog.isShowing())
        {
            assert dialog != null;
            dialog.dismiss();
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
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        scrHeight = displayMetrics.heightPixels;
        scrWwidth = displayMetrics.widthPixels;
        if (dialog != null && dialog.isShowing())
        {
            assert dialog != null;
            dialog.dismiss();
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
                if(GetSetting(this, "autoread", false))
                {
                    ReadSpoolData();
                }
            }
        }
    }


    void ReadSpoolData() {
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
                        String MaterialID = sb.toString().substring(12, 17);
                        if (GetMaterialName(MaterialID) != null) {
                            MaterialColor = sb.toString().substring(18, 24);
                            String Length = sb.toString().substring(24, 28);
                            colorView.setBackgroundColor(Color.parseColor("#" + MaterialColor));
                            MaterialName = Objects.requireNonNull(GetMaterialName(MaterialID))[0];
                            material.setSelection(madapter.getPosition(MaterialName));
                            brand.setSelection(badapter.getPosition(Objects.requireNonNull(GetMaterialName(MaterialID))[1]));
                            spoolsize.setSelection(sadapter.getPosition(GetMaterialWeight(Length)));
                            Toast.makeText(getApplicationContext(), R.string.data_read_from_tag, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), R.string.unknown_or_empty_tag, Toast.LENGTH_SHORT).show();
                        }
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
        }
    }


    void WriteSpoolData(String MaterialID, String Color, String Length) {
        if (currentTag != null) {
            SecureRandom random = new SecureRandom();

            String filamentId = "1" + MaterialID; //material_database.json
            String vendorId = "0276"; //0276 creality
            String color = "0" + Color;
            String filamentLen = Length;
            String serialNum = format(Locale.getDefault(),"%06d", random.nextInt(100000)); //000001
            String reserve = "000000";

            String spoolData = "AB124" + vendorId + "A2" + filamentId + color + filamentLen + serialNum + reserve + "00000000";

            MifareClassic mfc = MifareClassic.get(currentTag);
            if (mfc != null && mfc.getType() == MifareClassic.TYPE_CLASSIC) {
                try {
                    mfc.connect();
                    boolean auth = mfc.authenticateSectorWithKeyA(1, MifareClassic.KEY_DEFAULT);
                    if (auth) {
                        byte[] sectorData = spoolData.getBytes();
                        int blockIndex = 4;
                        for (int i = 0; i < sectorData.length; i+=MifareClassic.BLOCK_SIZE) {
                            byte[] block = Arrays.copyOfRange(sectorData, i, i+MifareClassic.BLOCK_SIZE);
                            mfc.writeBlock(blockIndex, block);
                            blockIndex++;
                        }
                        playBeep();
                        Toast.makeText(getApplicationContext(), R.string.data_written_to_tag, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                    }
                    mfc.close();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), R.string.error_writing_to_tag, Toast.LENGTH_SHORT).show();
                }
                try {
                    mfc.close();
                } catch (Exception ignored) {}
            }else{
                Toast.makeText(getApplicationContext(), R.string.invalid_tag_type, Toast.LENGTH_SHORT).show();
            }
        }
    }



    @SuppressLint("ClickableViewAccessibility")
    void openPicker() {
        try {
            dialog = new Dialog(this, R.style.Theme_SpoolID);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.picker_dialog);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setTitle(R.string.pick_color);
            final Button btnCls = dialog.findViewById(R.id.btncls);
            btnCls.setOnClickListener(v -> dialog.dismiss());
            View dcolorView = dialog.findViewById(R.id.dcolorview);
            ImageView picker = dialog.findViewById(R.id.picker);
            dcolorView.setBackgroundColor(Color.parseColor("#" + MaterialColor));
            picker.setOnTouchListener((v, event) -> {
                final int currPixel = getPixelColor(event, picker);
                if (currPixel != 0) {
                    MaterialColor = String.format("%02x%02x%02x", Color.red(currPixel), Color.green(currPixel), Color.blue(currPixel)).toUpperCase();
                    colorView.setBackgroundColor(Color.argb(255, Color.red(currPixel), Color.green(currPixel), Color.blue(currPixel)));
                    dcolorView.setBackgroundColor(Color.argb(255, Color.red(currPixel), Color.green(currPixel), Color.blue(currPixel)));
                    dialog.dismiss();
                }
                return false;
            });
            SeekBar seekBarFont = dialog.findViewById(R.id.seekbar_font);
            LinearGradient test = new LinearGradient(50.f, 0.f, scrWwidth - 250.0f, 0.0f, new int[]{0xFF000000, 0xFF0000FF, 0xFF00FF00, 0xFF00FFFF, 0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00, 0xFFFFFFFF}, null, Shader.TileMode.CLAMP);
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
                        MaterialColor = String.format("%02x%02x%02x", r, g, b).toUpperCase();
                        colorView.setBackgroundColor(Color.argb(255, r, g, b));
                        dcolorView.setBackgroundColor(Color.argb(255, r, g, b));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            dialog.show();
        }catch (Exception ignored){}
    }

}