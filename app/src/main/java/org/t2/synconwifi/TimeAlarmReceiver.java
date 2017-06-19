package org.t2.synconwifi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class TimeAlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_TARGET_STATE = "targetState";
    public static final String EXTRA_ACCOUNT_ID = "accountIdentifier";
    public static final String EXTRA_ALARM_TYPE = "alarmType";
    private Context context = null;
    private boolean targetState;
    private String accountIdentifier = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        //TODO: Re-set alarms on boot

        // Get data intent extras:
        this.targetState = intent.getBooleanExtra(EXTRA_TARGET_STATE, false);
        this.accountIdentifier = intent.getStringExtra(EXTRA_ACCOUNT_ID);
        if(accountIdentifier == null || accountIdentifier.equals("")) {
            Log.e(context.getPackageName(), " in TimeAlarmReceiver.onReceive(): empty account id");
            return;
        }

        // Set account sync state:
        setAccountsSyncState(targetState);

        // Re-set one-shot alarms:
        final int alarmType = intent.getIntExtra(EXTRA_ALARM_TYPE, 0);
        if(alarmType == TimeAccountDetailActivity.AlarmType.START_ALARM) {
            // If this receives the start alarm, set the end alarm for this day:
            TimeAccountDetailActivity.setSyncAlarm(alarmType, context, this.accountIdentifier, 1);
            TimeAccountDetailActivity.setSyncAlarm(TimeAccountDetailActivity.AlarmType.END_ALARM, context, this.accountIdentifier, 0);
        } else if (alarmType == TimeAccountDetailActivity.AlarmType.END_ALARM) {
            // If this received the end alarm, set both alarms for the next day:
            TimeAccountDetailActivity.setSyncAlarm(TimeAccountDetailActivity.AlarmType.START_ALARM, context, this.accountIdentifier, 1);
            TimeAccountDetailActivity.setSyncAlarm(alarmType, context, this.accountIdentifier, 1);
        }
    }

    private void setAccountsSyncState(boolean targetState) {
        // Check if service is activated:
        SharedPreferences sharedPreferencesSettings = this.context.getApplicationContext().getSharedPreferences(Preferences.Settings._NAME_, Context.MODE_PRIVATE);
        if(!sharedPreferencesSettings.getBoolean(Preferences.Settings.SERVICE_ENABLED, false)) {
            return;
        }

        // Obtain network information:
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        ssid = ssid.substring(1, ssid.length() - 1);
        SharedPreferences ssidPreferences = context.getApplicationContext().getSharedPreferences(Preferences.TrustedSSID._NAME_, Context.MODE_PRIVATE);
        // If targetState is "sync enabled", then check whether trusted SSID is connected:
        if(targetState && !(ssidPreferences.getBoolean(ssid, false))) {
            return;
        }

        // Get accounts and toggle synchronisation:
        AccountManager accountManager = AccountManager.get(context.getApplicationContext());
        SharedPreferences sharedPreferencesActiveAccounts = context.getApplicationContext().getSharedPreferences(Preferences.AccountsActive._NAME_, Context.MODE_PRIVATE);
        // Check whether each account is in the shared preferences of active accounts:
        for(Account account : accountManager.getAccounts()) {
            String accountTypeAndName = account.type + ";" + account.name;
            if(sharedPreferencesActiveAccounts.getBoolean(accountTypeAndName, false)) {
                WifiStateChangeReceiver.setSyncAutomatically(account, targetState);
            }
        }
    }
}
