package dngsoftware.spoolid;

import static androidx.core.app.ActivityCompat.requestPermissions;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import androidx.core.content.ContextCompat;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
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
import java.util.Objects;
import java.util.Properties;
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

    public static String[] printerTypes = {
            "K2",
            "K1",
            "HI",
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

    private static String getAssetDB(Context context, String pType) {
        try {
            StringBuilder sb = new StringBuilder();
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(pType +".json");
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

    public static long getDBVersion(Context context, String pType) {
        try {
            JSONObject materials = new JSONObject(getAssetDB(context, pType));
            JSONObject result = new JSONObject(materials.getString("result"));
            return result.getLong("version");
        } catch (Exception ignored) {
            return 0;
        }
    }

    public static void populateDatabase(Context context, MatDB db, String json, String pType) {
        try {
            JSONObject materials;
            if (json != null && !json.isEmpty()) {
                materials = new JSONObject(json);
            }else {
                materials = new JSONObject(getAssetDB(context, pType));
            }
            JSONObject result = new JSONObject(materials.getString("result"));
            SaveSetting(context, "version_" + pType, result.getLong("version"));
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

    public static String getJsonDB(String pType, boolean fromPrinter)
    {
        URL url;
        HttpURLConnection urlConnection;
        String server_response;
        try {
            if (fromPrinter)
            {
                url = new URL( "http://" + pType +  "/downloads/defData/material_database.json");
            }
            else
            {
                url = new URL( "https://raw.githubusercontent.com/DnG-Crafts/K2-RFID/refs/heads/main/db/" + pType + ".json");
            }
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

    public static String getJsonDB(String psw, String host, String pType) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession("root", host, 22);
            session.setPassword(psw);
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);
            session.connect();
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channel.setOutputStream(baos);
            if (pType.equalsIgnoreCase("k1")) {
                channel.setCommand("cat /usr/data/creality/userdata/box/material_database.json");
            }else {
                channel.setCommand("cat /mnt/UDISK/creality/userdata/box/material_database.json");
            }
            channel.connect(5000);
            while (true) {
                if (channel.isClosed()) {
                    channel.disconnect();
                    session.disconnect();
                    return baos.toString();
                }
            }
        } catch (Exception ignored) {
            return null;
        }
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

    public static void restartApp(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
            assert intent != null;
            ComponentName componentName = intent.getComponent();
            Intent mainIntent = Intent.makeRestartActivityTask(componentName);
            mainIntent.setPackage(context.getPackageName());
            context.startActivity(mainIntent);
            Runtime.getRuntime().exit(0);
        }
        catch (Exception ignored) {
            Runtime.getRuntime().exit(0);
        }
    }

    public static int getPositionByValue(Spinner spinner, String value) {
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (Objects.requireNonNull(adapter.getItem(i)).toString().equals(value)) {
                    return i;
                }
            }
        }
        return 0;
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