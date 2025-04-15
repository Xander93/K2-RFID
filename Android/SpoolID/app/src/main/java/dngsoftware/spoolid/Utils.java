package dngsoftware.spoolid;

import static androidx.core.app.ActivityCompat.requestPermissions;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.nfc.tech.MifareClassic;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

@SuppressLint("GetInstance")
public class Utils {

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

    public static String GetMaterialID(MatDB db, String materialName) {
        Filament item = db.getFilamentByName(materialName);
        return item.filamentID;
    }

    public static String GetMaterialInfo(MatDB db, String materialName) {
        Filament item = db.getFilamentByName(materialName);
        return item.filamentParam;
    }

    public static String[] GetMaterialName(MatDB db, String materialId) {
        String[] arrRet = new String[2];
        Filament item = db.getFilamentById(materialId);
        if (item == null) {
            return null;
        } else {
            arrRet[0] = item.filamentName;
            arrRet[1] = item.filamentVendor;
            return arrRet;
        }
    }

    public static String[] getMaterials(MatDB db, String brandName) {
        List<Filament> items = db.getFilamentsByVendor(brandName);
        String[] materials = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            materials[i] = items.get(i).filamentName;
        }
        return materials;
    }

    public static String[] getMaterialBrands(MatDB db) {
        List<Filament> items = db.getAllItems();
        Set<String> uniqueBrandsSet = new HashSet<>();
        for (Filament item : items) {
            uniqueBrandsSet.add(item.filamentVendor);
        }
        return uniqueBrandsSet.toArray(new String[0]);
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

    public static String bytesToHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
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
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {Manifest.permission.NFC, Manifest.permission.INTERNET};
            int permsRequestCode = 200;
            requestPermissions((Activity) context, perms, permsRequestCode);
        }
    }

    public static void playBeep() {
        new Thread(() -> {
            try {
                ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50);
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 300);
                toneGenerator.stopTone();
                toneGenerator.release();
            } catch (Exception ignored) {
            }
        }).start();
    }

    public static float dp2Px(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public static byte[] createKey(byte[] tagId) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(new byte[]
                    {113, 51, 98, 117, 94, 116, 49, 110, 113, 102, 90, 40, 112, 102, 36, 49}, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            int x = 0;
            byte[] encB = new byte[16];
            for (int i = 0; i < 16; i++) {
                if (x >= 4) x = 0;
                encB[i] = tagId[x];
                x++;
            }
            return Arrays.copyOfRange(cipher.doFinal(encB), 0, 6);
        } catch (Exception ignored) {
            return MifareClassic.KEY_DEFAULT;
        }
    }

    public static byte[] cipherData(int mode, byte[] tagData) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(new byte[]
                    {72, 64, 67, 70, 107, 82, 110, 122, 64, 75, 65, 116, 66, 74, 112, 50}, "AES");
            cipher.init(mode, secretKeySpec);
            return cipher.doFinal(tagData);
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String getAssetDB(Context context) {
        try {
            StringBuilder sb = new StringBuilder();
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("material_database.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            return sb.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static long getDBVersion(Context context) {
        try {
            JSONObject materials = new JSONObject(getAssetDB(context));
            JSONObject result = new JSONObject(materials.getString("result"));
            return result.getLong("version");
        } catch (Exception ignored) {
            return 0;
        }
    }

    public static void populateDatabase(Context context, MatDB db, String json) {
        try {
            JSONObject materials;
            if (json != null && !json.isEmpty()) {
                materials = new JSONObject(json);
            }else {
                materials = new JSONObject(getAssetDB(context));
            }
            JSONObject result = new JSONObject(materials.getString("result"));
            SaveSetting(context, "version", result.getLong("version"));
            JSONArray list = result.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                JSONObject base = new JSONObject(item.getString("base"));
                JSONObject kvParam = new JSONObject(item.getString("kvParam"));
                Filament filament = new Filament();
                filament.position = i;
                filament.filamentID = base.getString("id");
                filament.filamentName = base.getString("name");
                filament.filamentVendor = base.getString("brand");
                filament.filamentParam = kvParam.toString();
                db.addItem(filament);
            }
        } catch (Exception ignored) {
        }
    }

    public static String getJsonDB(final String Host)
    {
        URL url;
        HttpURLConnection urlConnection;
        String server_response;
        try {
            url = new URL( "http://" + Host +  "/downloads/defData/material_database.json");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setInstanceFollowRedirects(false);
            final int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                server_response = readStream(urlConnection.getInputStream());
           } else {
                server_response = null;
            }
        }
        catch (Exception e)
        {
            server_response = null;
        }
        return server_response;
    }

    private static String readStream(InputStream in)
    {
        try {
            int len;
            byte[] buf = new byte[ 1024 ];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while((len = in.read(buf)) > 0)
            {
                outputStream.write(buf, 0, len);
            }
            in.close();
            return outputStream.toString();
        } catch (Exception ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    public static String GetSetting(Context context, String sKey, String sDefault) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return sharedPref.getString(sKey, sDefault);
    }

    public static boolean GetSetting(Context context, String sKey, boolean bDefault) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return sharedPref.getBoolean(sKey, bDefault);
    }

    public static int GetSetting(Context context, String sKey, int iDefault) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return sharedPref.getInt(sKey, iDefault);
    }

    public static long GetSetting(Context context, String sKey, long lDefault) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        return sharedPref.getLong(sKey, lDefault);
    }

    public static void SaveSetting(Context context, String sKey, String sValue) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(sKey, sValue);
        editor.apply();
    }

    public static void SaveSetting(Context context, String sKey, boolean bValue) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(sKey, bValue);
        editor.apply();
    }

    public static void SaveSetting(Context context, String sKey, int iValue) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(sKey, iValue);
        editor.apply();
    }

    public static void SaveSetting(Context context, String sKey, long lValue) {
        SharedPreferences sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(sKey, lValue);
        editor.apply();
    }
}