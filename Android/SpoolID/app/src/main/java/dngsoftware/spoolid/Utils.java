package dngsoftware.spoolid;

import static androidx.core.app.ActivityCompat.requestPermissions;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.view.MotionEvent;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import java.util.Objects;

public class Utils {

    public static String[] materialBrands = {
            "Generic",
            "Creality"
    };

    public static String[] materialWeights = {
            "1 KG",
            "750 G",
            "600 G",
            "500 G",
            "250 G"
    };

    public static String GetMaterialLength(String materialWeight) {
        switch (materialWeight) {
            case "1 KG":
                return "0330";
            case "750 G":
                return "0247";
            case "600 G":
                return "0198";
            case "500 G":
                return "0165";
            case "250 G":
                return "0082";
        }
        return "0330";
    }

    public static String GetMaterialWeight(String materialLength) {
        switch (materialLength) {
            case "0330":
                return "1 KG";
            case "0247":
                return "750 G";
            case "0198":
                return "600 G";
            case "0165":
                return "500 G";
            case "0082":
                return "250 G";
        }
        return "1 KG";
    }

    public static String GetMaterialID(String materialName) {
        switch (materialName) {
            case "Hyper PLA":
                return "01001";
            case "Hyper PLA-CF":
                return "02001";
            case "Hyper PETG":
                return "06002";
            case "Hyper ABS":
                return "03001";
            case "CR-PLA":
                return "04001";
            case "CR-Silk":
                return "05001";
            case "CR-PETG":
                return "06001";
            case "CR-ABS":
                return "07001";
            case "Ender-PLA":
                return "08001";
            case "EN-PLA+":
                return "09001";
            case "HP-TPU":
                return "10001";
            case "CR-Nylon":
                return "11001";
            case "CR-PLA Carbon":
                return "13001";
            case "CR-PLA Matte":
                return "14001";
            case "CR-PLA Fluo":
                return "15001";
            case "CR-TPU":
                return "16001";
            case "CR-Wood":
                return "17001";
            case "HP Ultra PLA":
                return "18001";
            case "HP-ASA":
                return "19001";
            case "Generic PLA":
                return "00001";
            case "Generic PLA-Silk":
                return "00002";
            case "Generic PETG":
                return "00003";
            case "Generic ABS":
                return "00004";
            case "Generic TPU":
                return "00005";
            case "Generic PLA-CF":
                return "00006";
            case "Generic ASA":
                return "00007";
            case "Generic PA":
                return "00008";
            case "Generic PA-CF":
                return "00009";
            case "Generic BVOH":
                return "00010";
            case "Generic PVA":
                return "00011";
            case "Generic HIPS":
                return "00012";
            case "Generic PET-CF":
                return "00013";
            case "Generic PETG-CF":
                return "00014";
            case "Generic PA6-CF":
                return "00015";
            case "Generic PAHT-CF":
                return "00016";
            case "Generic PPS":
                return "00017";
            case "Generic PPS-CF":
                return "00018";
            case "Generic PP":
                return "00019";
            case "Generic PET":
                return "00020";
            case "Generic PC":
                return "00021";
        }
        return "00000";
    }

    public static String[] GetMaterialName(String materialID) {
        String[] arrRet = new String[2];
        switch (materialID) {
            case "01001":
                arrRet[0] = "Hyper PLA";
                arrRet[1] = "Creality";
                return arrRet;
            case "02001":
                arrRet[0] = "Hyper PLA-CF";
                arrRet[1] = "Creality";
                return arrRet;
            case "06002":
                arrRet[0] = "Hyper PETG";
                arrRet[1] = "Creality";
                return arrRet;
            case "03001":
                arrRet[0] = "Hyper ABS";
                arrRet[1] = "Creality";
                return arrRet;
            case "04001":
                arrRet[0] = "CR-PLA";
                arrRet[1] = "Creality";
                return arrRet;
            case "05001":
                arrRet[0] = "CR-Silk";
                arrRet[1] = "Creality";
                return arrRet;
            case "06001":
                arrRet[0] = "CR-PETG";
                arrRet[1] = "Creality";
                return arrRet;
            case "07001":
                arrRet[0] = "CR-ABS";
                arrRet[1] = "Creality";
                return arrRet;
            case "08001":
                arrRet[0] = "Ender-PLA";
                arrRet[1] = "Creality";
                return arrRet;
            case "09001":
                arrRet[0] = "EN-PLA+";
                arrRet[1] = "Creality";
                return arrRet;
            case "10001":
                arrRet[0] = "HP-TPU";
                arrRet[1] = "Creality";
                return arrRet;
            case "11001":
                arrRet[0] = "CR-Nylon";
                arrRet[1] = "Creality";
                return arrRet;
            case "13001":
                arrRet[0] = "CR-PLA Carbon";
                arrRet[1] = "Creality";
                return arrRet;
            case "14001":
                arrRet[0] = "CR-PLA Matte";
                arrRet[1] = "Creality";
                return arrRet;
            case "15001":
                arrRet[0] = "CR-PLA Fluo";
                arrRet[1] = "Creality";
                return arrRet;
            case "16001":
                arrRet[0] = "CR-TPU";
                arrRet[1] = "Creality";
                return arrRet;
            case "17001":
                arrRet[0] = "CR-Wood";
                arrRet[1] = "Creality";
                return arrRet;
            case "18001":
                arrRet[0] = "HP Ultra PLA";
                arrRet[1] = "Creality";
                return arrRet;
            case "19001":
                arrRet[0] = "HP-ASA";
                arrRet[1] = "Creality";
                return arrRet;
            case "00001":
                arrRet[0] = "Generic PLA";
                arrRet[1] = "Generic";
                return arrRet;
            case "00002":
                arrRet[0] = "Generic PLA-Silk";
                arrRet[1] = "Generic";
                return arrRet;
            case "00003":
                arrRet[0] = "Generic PETG";
                arrRet[1] = "Generic";
                return arrRet;
            case "00004":
                arrRet[0] = "Generic ABS";
                arrRet[1] = "Generic";
                return arrRet;
            case "00005":
                arrRet[0] = "Generic TPU";
                arrRet[1] = "Generic";
                return arrRet;
            case "00006":
                arrRet[0] = "Generic PLA-CF";
                arrRet[1] = "Generic";
                return arrRet;
            case "00007":
                arrRet[0] = "Generic ASA";
                arrRet[1] = "Generic";
                return arrRet;
            case "00008":
                arrRet[0] = "Generic PA";
                arrRet[1] = "Generic";
                return arrRet;
            case "00009":
                arrRet[0] = "Generic PA-CF";
                arrRet[1] = "Generic";
                return arrRet;
            case "00010":
                arrRet[0] = "Generic BVOH";
                arrRet[1] = "Generic";
                return arrRet;
            case "00011":
                arrRet[0] = "Generic PVA";
                arrRet[1] = "Generic";
                return arrRet;
            case "00012":
                arrRet[0] = "Generic HIPS";
                arrRet[1] = "Generic";
                return arrRet;
            case "00013":
                arrRet[0] = "Generic PET-CF";
                arrRet[1] = "Generic";
                return arrRet;
            case "00014":
                arrRet[0] = "Generic PETG-CF";
                arrRet[1] = "Generic";
                return arrRet;
            case "00015":
                arrRet[0] = "Generic PA6-CF";
                arrRet[1] = "Generic";
                return arrRet;
            case "00016":
                arrRet[0] = "Generic PAHT-CF";
                arrRet[1] = "Generic";
                return arrRet;
            case "00017":
                arrRet[0] = "Generic PPS";
                arrRet[1] = "Generic";
                return arrRet;
            case "00018":
                arrRet[0] = "Generic PPS-CF";
                arrRet[1] = "Generic";
                return arrRet;
            case "00019":
                arrRet[0] = "Generic PP";
                arrRet[1] = "Generic";
                return arrRet;
            case "00020":
                arrRet[0] = "Generic PET";
                arrRet[1] = "Generic";
                return arrRet;
            case "00021":
                arrRet[0] = "Generic PC";
                arrRet[1] = "Generic";
                return arrRet;
        }
        return null;
    }


    public static String[] getMaterials(String brandName) {
        String[] materials;
        if (Objects.equals(brandName, "Creality")) {
            materials = new String[]{"Hyper PLA", "Hyper PLA-CF", "Hyper PETG", "Hyper ABS", "CR-PLA", "CR-Silk", "CR-PETG", "CR-ABS", "Ender-PLA", "EN-PLA+", "HP-TPU", "CR-Nylon", "CR-PLA Carbon", "CR-PLA Matte", "CR-PLA Fluo", "CR-TPU", "CR-Wood", "HP Ultra PLA", "HP-ASA"};
        } else {
            materials = new String[]{"Generic PLA", "Generic PLA-Silk", "Generic PETG", "Generic ABS", "Generic TPU", "Generic PLA-CF", "Generic ASA", "Generic PA", "Generic PA-CF", "Generic BVOH", "Generic PVA", "Generic HIPS", "Generic PET-CF", "Generic PETG-CF", "Generic PA6-CF", "Generic PAHT-CF", "Generic PPS", "Generic PPS-CF", "Generic PP", "Generic PET", "Generic PC"};
        }
        return materials;
    }


    public static boolean canMfc(Context context) {
        FeatureInfo[] info = context.getPackageManager().getSystemAvailableFeatures();
        for (FeatureInfo i : info) {
            String name = i.name;
            if (name != null && name.equals("com.nxp.mifare")) {
                return true;
            }
        }
        return false;
    }

    public static String byteToHex(byte data) {
        return String.valueOf(toHexChar((data >>> 4) & 0x0F)) +
                toHexChar(data & 0x0F);
    }

    public static char toHexChar(int i) {
        if ((0 <= i) && (i <= 9)) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + (i - 10));
        }
    }

    public static String bytesToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte datum : data) {
            buf.append(byteToHex(datum).toUpperCase());
            buf.append(" ");
        }
        return (buf.toString());
    }

    public static int getPixelColor(MotionEvent event, ImageView picker) {
        int viewX = (int) event.getX();
        int viewY = (int) event.getY();
        int viewWidth = picker.getWidth();
        int viewHeight = picker.getHeight();
        Bitmap image = ((BitmapDrawable) picker.getDrawable()).getBitmap();
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int imageX = (int) ((float) viewX * ((float) imageWidth / (float) viewWidth));
        int imageY = (int) ((float) viewY * ((float) imageHeight / (float) viewHeight));
        return image.getPixel(imageX, imageY);
    }

    public static void SetPermissions(Context context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.NFC};
            int permsRequestCode = 200;
            requestPermissions((Activity) context, perms, permsRequestCode);
        }
    }

    public static boolean GetSetting(Context context, String sKey, boolean bDefault) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return sharedPref.getBoolean(sKey, bDefault);
    }

    public static void SaveSetting(Context context, String sKey, boolean bValue) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(sKey, bValue);
        editor.apply();
    }

    public static void playBeep() {
        new Thread(() -> {
            try {
                ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50);
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_HIGH_L,300);
                toneGenerator.stopTone();
                toneGenerator.release();
            } catch (Exception ignored) {}
        }).start();
    }
}
