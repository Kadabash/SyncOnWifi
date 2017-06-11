package org.t2.synconwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Set alarms at boot:
        SharedPreferences timePreferences = context.getSharedPreferences("timePreferences", MODE_PRIVATE);
        if(!timePreferences.getBoolean("timeControlEnabled", false))
        {
            return;
        }
        int hourOfDayStart = timePreferences.getInt("startHour", 0);
        int minuteStart = timePreferences.getInt("startMinute", 0);
        int hourOfDayEnd = timePreferences.getInt("endHour", 23);
        int minuteEnd = timePreferences.getInt("endMinute", 59);

        TimeActivity.setSyncAlarm(context, hourOfDayStart, minuteStart, true);
        TimeActivity.setSyncAlarm(context, hourOfDayEnd, minuteEnd, false);
    }
}
