package org.t2.synconwifi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncAdapterType;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Vector;

public class WifiStateChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiStateChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Check if service is activated:
        SharedPreferences sharedPreferencesSettings = context.getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        if(!sharedPreferencesSettings.getBoolean("ServiceEnabled", false)) {
            return;
        }

        //Obtain network information:
        NetworkInfo newNetworkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        SupplicantState supplicantState = wifiInfo.getSupplicantState();

        AccountManager accountManager = AccountManager.get(context.getApplicationContext());
        SharedPreferences sharedPreferencesActiveAccounts = context.getApplicationContext().getSharedPreferences("AccountsActive", Context.MODE_PRIVATE);

        //If we are disconnected/disconnecting from any network, disable account synchronisation:
        if((wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) || (wifiManager.getWifiState() ==  WifiManager.WIFI_STATE_DISABLING) || supplicantState == SupplicantState.DISCONNECTED) {
            //Check whether each account is in the shared preferences of active accounts:
            for(Account account : accountManager.getAccounts()) {
                String accountTypeAndName = account.type + ";" + account.name;
                if(sharedPreferencesActiveAccounts.getBoolean(accountTypeAndName, false)) {
                    setSyncAutomatically(account, false);
                }
            }
        }

        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        SharedPreferences sharedPreferencesSSID = context.getApplicationContext().getSharedPreferences("TrustedSSID", Context.MODE_PRIVATE);

        //If we are connected to a trusted network, enable account synchronisation:
        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) && networkInfo.isConnected()) {
            //Check if the connected SSID is in shared preferences "TrustedSSID", key "SSID":
            String ssid = wifiInfo.getSSID();   //SSID is surrounded by quotation marks here. Remove them in the next line.
            ssid = ssid.substring(1, ssid.length() - 1);
            if(sharedPreferencesSSID.getBoolean(ssid, false)) {
                //Check whether each account is in the shared preferences of active accounts:
                for(Account account : accountManager.getAccounts()) {
                    String accountTypeAndName = account.type + ";" + account.name;
                    if(sharedPreferencesActiveAccounts.getBoolean(accountTypeAndName, false)) {
                        setSyncAutomatically(account, true);
                    }
                }
            }
        }
    }

    public static void setSyncAutomatically(Account account, boolean enabled) {
        //Determine sync authority for the given account:
        SyncAdapterType[] syncAdapterTypes = ContentResolver.getSyncAdapterTypes();
        for(SyncAdapterType syncAdapterType : syncAdapterTypes) {
            if(ContentResolver.getIsSyncable(account, syncAdapterType.authority) > 0) {
                //Actually switch syncing:
                ContentResolver.setSyncAutomatically(account, syncAdapterType.authority, enabled);
            }
        }
    }

    public static boolean getSyncAutomatically(Account account) {
        //Determine sync authority for the given account:
        SyncAdapterType[] syncAdapterTypes = ContentResolver.getSyncAdapterTypes();
        for(SyncAdapterType syncAdapterType : syncAdapterTypes) {
            if(ContentResolver.getIsSyncable(account, syncAdapterType.authority) > 0) {
                //Actually read sync setting:
                return ContentResolver.getSyncAutomatically(account, syncAdapterType.authority);
            }
        }
        return false;
    }
}
