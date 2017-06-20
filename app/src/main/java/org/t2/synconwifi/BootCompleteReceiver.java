package org.t2.synconwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;

public class BootCompleteReceiver extends BroadcastReceiver{

    enum timePosition { BEFORE, WITHIN, AFTER };

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get current time:
        Calendar currentTimeTmp = Calendar.getInstance();
        currentTimeTmp.setTimeInMillis(System.currentTimeMillis());
        Calendar currentTime = Calendar.getInstance();  // This holds only the hour and minute of current time.
        currentTime.set(Calendar.HOUR_OF_DAY, currentTimeTmp.get(Calendar.HOUR_OF_DAY));
        currentTime.set(Calendar.MINUTE, currentTimeTmp.get(Calendar.MINUTE));
        currentTime.set(Calendar.SECOND, currentTimeTmp.get(Calendar.SECOND));

        // Get list of accounts for which to schedule alarms:
        SharedPreferences accountPrefs = context.getApplicationContext().getSharedPreferences(Preferences.AccountsActive._NAME_, Context.MODE_PRIVATE);
        SharedPreferences timePrefs = context.getApplicationContext().getSharedPreferences(Preferences.AccountTimes._NAME_, Context.MODE_PRIVATE);
        Map<String, ?> allAccounts = accountPrefs.getAll();
        Set<String> accountIDs = allAccounts.keySet();
        for(String id : accountIDs) {
            final int startHour = timePrefs.getInt(id + Preferences.AccountTimes.START_HOUR_SUFFIX, 0);
            final int startMinute = timePrefs.getInt(id + Preferences.AccountTimes.START_MINUTE_SUFFIX, 0);
            final int endHour = timePrefs.getInt(id + Preferences.AccountTimes.END_HOUR_SUFFIX, 0);
            final int endMinute = timePrefs.getInt(id + Preferences.AccountTimes.END_MINUTE_SUFFIX, 0);
            if(startHour == 0 && startMinute == 0 && endHour == 0 && endMinute == 0) { // If this account has no time settings:
                continue;
            }
            // Set alarm on the appropriate day:
            Calendar startTime = Calendar.getInstance();
            startTime.set(Calendar.HOUR_OF_DAY, startHour);
            startTime.set(Calendar.MINUTE, startMinute);
            startTime.set(Calendar.SECOND, 0);
            Calendar endTime = Calendar.getInstance();
            endTime.set(Calendar.HOUR_OF_DAY, endHour);
            endTime.set(Calendar.MINUTE, endMinute);
            endTime.set(Calendar.SECOND, 0);
            int daysInFutureStart = 0;
            int daysInFutureEnd = 0;
            if(currentTime.before(startTime) && currentTime.before(endTime)) {
                daysInFutureStart = 0;
                daysInFutureEnd = 0;
            } else if(currentTime.after(startTime) && currentTime.before(endTime)) {
                daysInFutureStart = 1;
                daysInFutureEnd = 0;
            } else if(currentTime.after(startTime) && currentTime.after(endTime)) {
                daysInFutureStart = 1;
                daysInFutureEnd = 1;
            }
            TimeAccountDetailActivity.setSyncAlarm(TimeAccountDetailActivity.AlarmType.START_ALARM, context.getApplicationContext(), id, daysInFutureStart);
            TimeAccountDetailActivity.setSyncAlarm(TimeAccountDetailActivity.AlarmType.END_ALARM, context.getApplicationContext(), id, daysInFutureEnd);
        }
    }
}
