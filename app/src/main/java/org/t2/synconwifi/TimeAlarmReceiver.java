package org.t2.synconwifi;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

public class TimeAlarmReceiver extends BroadcastReceiver {

    Context appContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.appContext = context;

        // Set target state of sync service from intent extra:
        boolean targetState = intent.getBooleanExtra("targetState", true);
        SharedPreferences shp = context.getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putBoolean("ServiceEnabled", targetState);
        editor.apply();

        // Set inactivity options if sync service is disabled:
        if(!targetState) {
            SharedPreferences timePrefs = context.getSharedPreferences("timePreferences", MODE_PRIVATE);
            final int inactivitySetting = timePrefs.getInt("inactivitySetting", 0);
            switch(inactivitySetting) {
                case 0:
                    setSyncForManagedAccounts(true);
                    break;
                case 1:
                    setSyncForManagedAccounts(false);
                    break;
                default:
                    break;
            }
        }
    }

    // Set sync for all managed accounts:
    public void setSyncForManagedAccounts(boolean enableOrDisable) {
        AccountManager accountManager = AccountManager.get(appContext);
        SharedPreferences sharedPreferencesActiveAccounts = appContext.getSharedPreferences("AccountsActive", MODE_PRIVATE);
        for(Account account : accountManager.getAccounts()) {
            String accountTypeAndName = account.type + ";" + account.name;
            if(sharedPreferencesActiveAccounts.getBoolean(accountTypeAndName, false)) {
                WifiStateChangeReceiver.setSyncAutomatically(account, enableOrDisable);
            }
        }
    }
}
