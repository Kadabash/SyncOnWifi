package org.t2.synconwifi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimeActivity extends AppCompatActivity {

    // Random IDs for alarm intents:
    public final int startTimePendingIntentID = 450893;
    public final int endTimePendingIntentID = 234785;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        // Get current time settings:
        SharedPreferences timePreferences = getApplicationContext().getSharedPreferences("timePreferences", MODE_PRIVATE);
        int hourOfDayStart = timePreferences.getInt("startHour", 0);
        int minuteStart = timePreferences.getInt("startMinute", 0);
        int hourOfDayEnd = timePreferences.getInt("endHour", 23);
        int minuteEnd = timePreferences.getInt("endMinute", 59);

        // Set alarm intent IDs:
        SharedPreferences alarmPreferences = getApplicationContext().getSharedPreferences("alarmPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = alarmPreferences.edit();
        editor.putInt("startIntentID", this.startTimePendingIntentID);
        editor.putInt("endIntentID", this.endTimePendingIntentID);
        editor.apply();

        // Set time spinner buttons to current time settings:
        Button timeStartButton = (Button) findViewById(R.id.timeStartSpinnerButton);
        timeStartButton.setText(String.format("%02d", hourOfDayStart) + ":" + String.format("%02d", minuteStart));
        Button timeEndButton = (Button) findViewById(R.id.timeEndSpinnerButton);
        timeEndButton.setText(String.format("%02d", hourOfDayEnd) + ":" + String.format("%02d", minuteEnd));

        // Have buttons display their time pickers on click:
        timeStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show start time picker:
                DialogFragment startTimeFragment = new StartTimePickerFragment();
                startTimeFragment.show(getSupportFragmentManager(), "startTimePicker");
            }
        });
        timeEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show end time picker:
                DialogFragment endTimeFragment = new EndTimePickerFragment();
                endTimeFragment.show(getSupportFragmentManager(), "endTimePicker");
            }
        });

        // Get time control enable state and display/hide rest of the views in this acitivity:
        boolean timeControlEnabled = timePreferences.getBoolean("timeControlEnabled", false);
        CheckBox timeControlEnabledCheckBox = (CheckBox) findViewById(R.id.timeEnabledCheckBox);
        timeControlEnabledCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save new check state:
                SharedPreferences shp = getApplicationContext().getSharedPreferences("timePreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = shp.edit();
                editor.putBoolean("timeControlEnabled", isChecked);
                editor.apply();

                // Show/hide other views in this activity based on check state:
                toggleVisibilityTimeSettings(isChecked);

                // Set/unset alarms
                if(isChecked) {
                    // Set alarms:
                    setSyncAlarm(getApplicationContext(), shp.getInt("startHour", 0), shp.getInt("startMinute", 0), true);
                    setSyncAlarm(getApplicationContext(), shp.getInt("endHour", 23), shp.getInt("endMinute", 59), false);
                } else {
                    // Unset alarms:
                    unsetSyncAlarm(getApplicationContext(), true);
                    unsetSyncAlarm(getApplicationContext(), false);
                }
            }
        });
        timeControlEnabledCheckBox.setChecked(timeControlEnabled);

        // Set up radio group for inactive time settings:
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.inactiveTimeRadioGroup);
        final int inactivitySetting = timePreferences.getInt("inactivitySetting", 0);
        radioGroup.check(inactivitySetting);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                // Set inactivitySetting in timePreferences according to checked item:
                SharedPreferences timePreferences = getApplicationContext().getSharedPreferences("timePreferences", MODE_PRIVATE);
                SharedPreferences.Editor timePrefsEditor = timePreferences.edit();
                CheckBox checkedItem = (CheckBox) findViewById(checkedId);
                int checkBoxTag = Integer.parseInt((String) checkedItem.getTag());
                timePrefsEditor.putInt("inactivitySetting", checkBoxTag);
                timePrefsEditor.apply();
            }
        });

        // If time-based control is disabled, hide other Views on activity start:
        toggleVisibilityTimeSettings(timePreferences.getBoolean("timeControlEnabled", false));
    }


    // Show/hide other views in this acitivity based on enabled state of time-based control:
    protected void toggleVisibilityTimeSettings(boolean enabled) {
        List<View> viewsList = new ArrayList<>();
        viewsList.add((TextView) findViewById(R.id.timeSelectionTextView));
        viewsList.add((Button) findViewById(R.id.timeStartSpinnerButton));
        viewsList.add((Button) findViewById(R.id.timeEndSpinnerButton));
        viewsList.add((TextView) findViewById(R.id.timeStartHeadline));
        viewsList.add((TextView) findViewById(R.id.timeEndHeadline));
        viewsList.add((TextView) findViewById(R.id.inactiveTimeHeader));
        viewsList.add((RadioGroup) findViewById(R.id.inactiveTimeRadioGroup));
        for(View v : viewsList) {
            v.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        }
    }

    // Schedule enabling of synchronisation at chosen time:
    public static void setSyncAlarm(Context context, int hourOfDay, int minute, boolean enableOrDisable) {
        Calendar targetTime = Calendar.getInstance();
        targetTime.setTimeInMillis(System.currentTimeMillis());
        targetTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        targetTime.set(Calendar.MINUTE, minute);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Enable or disable service setting at targetTime:
        Intent intent = new Intent(context, TimeAlarmReceiver.class);
        intent.putExtra("targetState", enableOrDisable);
        PendingIntent alarmIntent;
        SharedPreferences alarmPreferences = context.getSharedPreferences("alarmPreferences", MODE_PRIVATE);
        if(enableOrDisable) {
            alarmIntent = PendingIntent.getBroadcast(context, alarmPreferences.getInt("startIntentID", 44445), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            alarmIntent = PendingIntent.getBroadcast(context, alarmPreferences.getInt("endIntentID", 44446), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, targetTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    // Remove sync alarm:
    public static void unsetSyncAlarm(Context context, boolean enableOrDisable) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Enable or disable service setting at targetTime:
        Intent intent = new Intent(context, TimeAlarmReceiver.class);
        PendingIntent alarmIntent;
        SharedPreferences alarmPreferences = context.getSharedPreferences("alarmPreferences", MODE_PRIVATE);
        if(enableOrDisable) {
            alarmIntent = PendingIntent.getBroadcast(context, alarmPreferences.getInt("startIntentID", 44445), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            alarmIntent = PendingIntent.getBroadcast(context, alarmPreferences.getInt("endIntentID", 44446), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        alarmManager.cancel(alarmIntent);
    }

    // Make start time picker class:
    public static class StartTimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Initialise default time to current time:
            final java.util.Calendar calendar = java.util.Calendar.getInstance();
            final int currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
            final int currentMinute = calendar.get(java.util.Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, currentHour, currentMinute, true);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Abort if time is not consistent:
            if(!this.checkTimeConsistency(hourOfDay, minute)) {
                this.dismiss();
                Toast.makeText(getActivity().getApplicationContext(), R.string.time_consistency_error, Toast.LENGTH_LONG).show();
                return;
            }

            // Save chosen time to shared preferences:
            this.saveTimeShp(hourOfDay, minute);

            // Dismiss, then set button text:
            this.dismiss();
            this.setButtonText(hourOfDay, minute);

            // Set sync toggle alarm:
            this.setSyncAlarm(hourOfDay, minute);
        }

        protected void setButtonText(int hourOfDay, int minute) {
            Button timeStartButton = (Button) getActivity().findViewById(R.id.timeStartSpinnerButton);
            timeStartButton.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
        }

        protected void saveTimeShp(int hourOfDay, int minute) {
            SharedPreferences shp = getActivity().getApplicationContext().getSharedPreferences("timePreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = shp.edit();
            editor.putInt("startHour", hourOfDay);
            editor.putInt("startMinute", minute);
            editor.apply();
        }

        // Give an error if the end time is before the start time or vice versa:
        protected boolean checkTimeConsistency(int hourOfDay, int minute) {
            SharedPreferences shp = getActivity().getApplicationContext().getSharedPreferences("timePreferences", MODE_PRIVATE);
            int hourOfDayEnd = shp.getInt("endHour", 23);
            int minuteEnd = shp.getInt("endMinute", 59);
            if(hourOfDay > hourOfDayEnd) { return false; }
            if(hourOfDay == hourOfDayEnd && minute >= minuteEnd) { return false; }
            else { return true; }
        }

        // Call the corresponding method that sets/resets the sync toggle alarms:
        protected void setSyncAlarm(int hourOfDay, int minute) {
            TimeActivity.setSyncAlarm(getActivity().getApplicationContext(), hourOfDay, minute, true);
        }
    }

    // Make end time picker class:
    public static class EndTimePickerFragment extends StartTimePickerFragment implements TimePickerDialog.OnTimeSetListener{

        protected void setButtonText(int hourOfDay, int minute) {
            Button timeEndButton = (Button) getActivity().findViewById(R.id.timeEndSpinnerButton);
            timeEndButton.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
        }

        protected void saveTimeShp(int hourOfDay, int minute) {
            SharedPreferences shp = getActivity().getApplicationContext().getSharedPreferences("timePreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = shp.edit();
            editor.putInt("endHour", hourOfDay);
            editor.putInt("endMinute", minute);
            editor.apply();
        }

        protected boolean checkTimeConsistency(int hourOfDay, int minute) {
            SharedPreferences shp = getActivity().getApplicationContext().getSharedPreferences("timePreferences", MODE_PRIVATE);
            int hourOfDayStart = shp.getInt("startHour", 23);
            int minuteStart = shp.getInt("startMinute", 59);
            if(hourOfDay < hourOfDayStart) { return false; }
            if(hourOfDay == hourOfDayStart && minute <= minuteStart) { return false; }
            else { return true; }
        }

        protected void setSyncAlarm(int hourOfDay, int minute) {
            TimeActivity.setSyncAlarm(getActivity().getApplicationContext(), hourOfDay, minute, false);
        }
    }
}
