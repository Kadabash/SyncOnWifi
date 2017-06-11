package org.t2.synconwifi;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

public class TimeAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Set target state of sync service from intent extra:
        boolean targetState = intent.getBooleanExtra("targetState", true);
        SharedPreferences shp = context.getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putBoolean("ServiceEnabled", targetState);
        editor.apply();
    }
}
