package dngsoftware.spoolid;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;


public class nAdapter {
    private final NfcAdapter nfcAdapter;
    private final Activity activity;

    public nAdapter(Activity activity) {
        super();
        this.activity = activity;
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity.getApplicationContext());
    }

    public void enableForeground() {
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            Intent intent = new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_MUTABLE);
            IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
            try {
                intentFilter.addDataType("*/*");
            } catch (Exception ignored) {
            }
            IntentFilter[] mFilters = new IntentFilter[]{intentFilter};
            String[][] techLists = {new String[]{MifareClassic.class.getName(), NfcA.class.getName()}};
            try {
                nfcAdapter.enableForegroundDispatch(activity, pendingIntent, mFilters, techLists);
            } catch (Exception ignored) {
            }
        }
    }

    public void disableForeground() {
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            try {
                nfcAdapter.disableForegroundDispatch(activity);
            } catch (Exception ignored) {
            }
        }
    }

    public NfcAdapter getNfc() {
        return nfcAdapter;
    }

}