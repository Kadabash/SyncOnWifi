package org.t2.synconwifi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Calendar;

public class TimeAccountDetailActivity extends AppCompatActivity {

    public static final String ACCOUNT_EXTRA_NAME = "account";
    public static class AlarmType {
        public static final int START_ALARM = 1;
        public static final int END_ALARM = 2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_account_detail);

        // Get account identifier from intent extra:
        Intent intent = getIntent();
        final String accountIdentifier = intent.getStringExtra(ACCOUNT_EXTRA_NAME);

        // Set account name and type on top of this page:
        String accountType = accountIdentifier.substring(0, accountIdentifier.indexOf(";"));
        String accountName = accountIdentifier.substring(accountIdentifier.indexOf(";") + 1);
        Drawable accountIcon = null;
        PackageManager packageManager = getApplicationContext().getPackageManager();
        AccountManager accountManager = AccountManager.get(getApplicationContext());
        AuthenticatorDescription[] descriptions = accountManager.getAuthenticatorTypes();
        for(AuthenticatorDescription description : descriptions) {
            if(description.type.equals(accountType)) {
                accountIcon = packageManager.getDrawable(description.packageName, description.iconId, null);
            }
        }
        ImageView accountIconView = (ImageView) findViewById(R.id.timeDetailAccountImageView);
        accountIconView.setImageDrawable(accountIcon);
        TextView accountTypeTextView = (TextView) findViewById(R.id.timeDetailAccountTextViewType);
        accountTypeTextView.setText(accountType);
        TextView accountNameTextView = (TextView) findViewById(R.id.timeDetailAccountTextViewName);
        accountNameTextView.setText(accountName);

        // Set start and end times for this account:
        updateTimeDisplay(TimeAccountDetailActivity.this, accountIdentifier);

        // Have buttons display their time pickers on click:
        Button startTimeButton = (Button) findViewById(R.id.timeStartSpinnerButton);
        Button endTimeButton = (Button) findViewById(R.id.timeEndSpinnerButton);
        startTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show start time picker:
                DialogFragment startTimeFragment = new StartTimePickerFragment();
                startTimeFragment.show(getSupportFragmentManager(), accountIdentifier);
            }
        });
        endTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show end time picker:
                DialogFragment endTimeFragment = new EndTimePickerFragment();
                endTimeFragment.show(getSupportFragmentManager(), accountIdentifier);
            }
        });

        // Add button to remove time settings and disable alarm:
        Button removeSettingsButton = (Button) findViewById(R.id.removeTimeSettingsButton);
        removeSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSyncAlarms(TimeAccountDetailActivity.this, accountIdentifier);
                toggleViewVisibility(TimeAccountDetailActivity.this, accountIdentifier);
            }
        });

        toggleViewVisibility(TimeAccountDetailActivity.this, accountIdentifier);
    }

    // Toggle visibility of deletion button and text:
    private static void toggleViewVisibility(Activity activity, String accountIdentifier) {
        Button removeSettingsButton = (Button) activity.findViewById(R.id.removeTimeSettingsButton);
        TextView timeControlDetailHeadline = (TextView) activity.findViewById(R.id.timeDetailHeadlineTextView);

        // Fetch time settings:
        SharedPreferences accountTimePrefs = activity.getApplicationContext().getSharedPreferences(Preferences.AccountTimes._NAME_, MODE_PRIVATE);
        final int startHour = accountTimePrefs.getInt(accountIdentifier + Preferences.AccountTimes.START_HOUR_SUFFIX, 0);
        final int startMinute = accountTimePrefs.getInt(accountIdentifier + Preferences.AccountTimes.START_MINUTE_SUFFIX, 0);
        final int endHour = accountTimePrefs.getInt(accountIdentifier + Preferences.AccountTimes.END_HOUR_SUFFIX, 0);
        final int endMinute = accountTimePrefs.getInt(accountIdentifier + Preferences.AccountTimes.END_MINUTE_SUFFIX, 0);

        // If all times are zero, set Button invisible and add message to headline:
        if(startHour == 0 && startMinute == 0 && endHour == 0 && endMinute == 0) {
            removeSettingsButton.setVisibility(View.INVISIBLE);
            String newHeadlineText = activity.getApplicationContext().getString(R.string.time_nothing_set)
                    + " " + activity.getApplicationContext().getString(R.string.time_detail_headline);
            timeControlDetailHeadline.setText(newHeadlineText);
        } else {
            removeSettingsButton.setVisibility(View.VISIBLE);
            timeControlDetailHeadline.setText(R.string.time_detail_headline);
        }
    }

    private static void updateTimeDisplay(Activity activity, String accountIdentifier) {
        // Set start and end times for this account:
        Button startTimeButton = (Button) activity.findViewById(R.id.timeStartSpinnerButton);
        Button endTimeButton = (Button) activity.findViewById(R.id.timeEndSpinnerButton);
        SharedPreferences accountTimePrefs = activity.getApplicationContext().getSharedPreferences(Preferences.AccountTimes._NAME_, MODE_PRIVATE);
        final int startHour = accountTimePrefs.getInt(accountIdentifier + Preferences.AccountTimes.START_HOUR_SUFFIX, 0);
        final int startMinute = accountTimePrefs.getInt(accountIdentifier + Preferences.AccountTimes.START_MINUTE_SUFFIX, 0);
        final int endHour = accountTimePrefs.getInt(accountIdentifier + Preferences.AccountTimes.END_HOUR_SUFFIX, 0);
        final int endMinute = accountTimePrefs.getInt(accountIdentifier + Preferences.AccountTimes.END_MINUTE_SUFFIX, 0);
        startTimeButton.setText(String.format("%02d", startHour) + ":" + String.format("%02d", startMinute));
        endTimeButton.setText(String.format("%02d", endHour) + ":" + String.format("%02d", endMinute));
    }

    // Set alarm for account:
    public static void setSyncAlarm(int alarmType, Context context, String accountIdentifier, int daysInFuture) {
        // If setting sync alarms for the first time, set alarm IDs:
        SharedPreferences alarmPreferences = context.getSharedPreferences(Preferences.AccountTimes._NAME_, MODE_PRIVATE);
        if(alarmPreferences.getInt(accountIdentifier + Preferences.AccountTimes.START_INTENT_SUFFIX, 0) == 0) {
            SharedPreferences.Editor editor = alarmPreferences.edit();
            editor.putInt(accountIdentifier + Preferences.AccountTimes.START_INTENT_SUFFIX, 44445);
            editor.apply();
        }
        if(alarmPreferences.getInt(accountIdentifier + Preferences.AccountTimes.END_INTENT_SUFFIX, 0) == 0) {
            SharedPreferences.Editor editor = alarmPreferences.edit();
            editor.putInt(accountIdentifier + Preferences.AccountTimes.START_INTENT_SUFFIX, 44446);
            editor.apply();
        }

        // Get times from shared preferences:
        final int startHour = alarmPreferences.getInt(accountIdentifier + Preferences.AccountTimes.START_HOUR_SUFFIX, 0);
        final int startMinute = alarmPreferences.getInt(accountIdentifier + Preferences.AccountTimes.START_MINUTE_SUFFIX, 0);
        final int endHour = alarmPreferences.getInt(accountIdentifier + Preferences.AccountTimes.END_HOUR_SUFFIX, 0);
        final int endMinute = alarmPreferences.getInt(accountIdentifier + Preferences.AccountTimes.END_MINUTE_SUFFIX, 0);

        // Set target times:
        Calendar targetTimeStart = Calendar.getInstance();
        targetTimeStart.setTimeInMillis(System.currentTimeMillis());
        targetTimeStart.set(Calendar.HOUR_OF_DAY, startHour);
        targetTimeStart.set(Calendar.MINUTE, startMinute);
        targetTimeStart.set(Calendar.SECOND, 0);
        targetTimeStart.set(Calendar.MILLISECOND, 0);
        Calendar targetTimeEnd = Calendar.getInstance();
        targetTimeEnd.setTimeInMillis(System.currentTimeMillis());
        targetTimeEnd.set(Calendar.HOUR_OF_DAY, endHour);
        targetTimeEnd.set(Calendar.MINUTE, endMinute);
        targetTimeEnd.set(Calendar.SECOND, 0);
        targetTimeEnd.set(Calendar.MILLISECOND, 0);

        // Set day offset on times:
        Calendar currentTime = Calendar.getInstance();
        int currentDayOfYear = currentTime.get(Calendar.DAY_OF_YEAR);
        int daysInThisYear = currentTime.getActualMaximum(Calendar.DAY_OF_YEAR);
        if(currentDayOfYear + daysInFuture > daysInThisYear) { // Handle new year rollover:
            targetTimeStart.set(Calendar.DAY_OF_YEAR, (currentDayOfYear + daysInFuture - daysInThisYear));
            targetTimeEnd.set(Calendar.DAY_OF_YEAR, (currentDayOfYear + daysInFuture - daysInThisYear));
        } else {
            targetTimeStart.set(Calendar.DAY_OF_YEAR, currentDayOfYear + daysInFuture);
            targetTimeEnd.set(Calendar.DAY_OF_YEAR, currentDayOfYear + daysInFuture);
        }

        // Set both alarms:
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intentStart = new Intent(context, TimeAlarmReceiver.class);
        intentStart.putExtra(TimeAlarmReceiver.EXTRA_TARGET_STATE, true);
        intentStart.putExtra(TimeAlarmReceiver.EXTRA_ACCOUNT_ID, accountIdentifier);
        intentStart.putExtra(TimeAlarmReceiver.EXTRA_ALARM_TYPE, alarmType);
        PendingIntent alarmIntentStart = PendingIntent.getBroadcast(context,
                    alarmPreferences.getInt(accountIdentifier + Preferences.AccountTimes.START_INTENT_SUFFIX, 0),
                    intentStart, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intentEnd = new Intent(context, TimeAlarmReceiver.class);
        intentEnd.putExtra(TimeAlarmReceiver.EXTRA_TARGET_STATE, false);
        intentEnd.putExtra(TimeAlarmReceiver.EXTRA_ACCOUNT_ID, accountIdentifier);
        intentEnd.putExtra(TimeAlarmReceiver.EXTRA_ALARM_TYPE, alarmType);
        PendingIntent alarmIntentEnd = PendingIntent.getBroadcast(context,
                    alarmPreferences.getInt(accountIdentifier + Preferences.AccountTimes.END_INTENT_SUFFIX, 0),
                    intentEnd, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set alarm to fire once. This MUST be repeated every time the alarm is fired!
        if(Build.VERSION.SDK_INT >= 19) {
            if(alarmType == AlarmType.START_ALARM) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetTimeStart.getTimeInMillis(), alarmIntentStart);
            } else if(alarmType == AlarmType.END_ALARM) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetTimeEnd.getTimeInMillis(), alarmIntentEnd);
            }
        } else {
            if(alarmType == AlarmType.START_ALARM) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, targetTimeStart.getTimeInMillis(), alarmIntentStart);
            } else if(alarmType == AlarmType.END_ALARM) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, targetTimeEnd.getTimeInMillis(), alarmIntentEnd);
            }
        }
    }

    // Delete alarms for account:
    private static void deleteSyncAlarms(Activity activity, String accountIdentifier) {
        Context context = activity.getApplicationContext();
        SharedPreferences alarmPreferences = context.getSharedPreferences(Preferences.AccountTimes._NAME_, MODE_PRIVATE);
        final int startIntentID = alarmPreferences.getInt(accountIdentifier + Preferences.AccountTimes.START_INTENT_SUFFIX, 0);
        final int endIntentID = alarmPreferences.getInt(accountIdentifier + Preferences.AccountTimes.END_INTENT_SUFFIX, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TimeAlarmReceiver.class);
        PendingIntent alarmIntent;

        // Cancel start alarm:
        alarmIntent = PendingIntent.getBroadcast(context, startIntentID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(alarmIntent);
        // Overwrite preferences entry:
        SharedPreferences.Editor editor = alarmPreferences.edit();
        editor.remove(accountIdentifier + Preferences.AccountTimes.START_INTENT_SUFFIX);
        editor.remove(accountIdentifier + Preferences.AccountTimes.START_HOUR_SUFFIX);
        editor.remove(accountIdentifier + Preferences.AccountTimes.START_MINUTE_SUFFIX);
        editor.apply();

        // Cancel end alarm:
        alarmIntent = PendingIntent.getBroadcast(context, endIntentID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(alarmIntent);
        // Overwrite preferences entry:
        editor.remove(accountIdentifier + Preferences.AccountTimes.END_INTENT_SUFFIX);
        editor.remove(accountIdentifier + Preferences.AccountTimes.END_HOUR_SUFFIX);
        editor.remove(accountIdentifier + Preferences.AccountTimes.END_MINUTE_SUFFIX);
        editor.apply();

        // Update time display:
        updateTimeDisplay(activity, accountIdentifier);
    }

    // Make start time picker class:
    public static class StartTimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        String accountIdentifier = null;

        @Override @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Initialise default time to current time:
            final java.util.Calendar calendar = java.util.Calendar.getInstance();
            final int currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
            final int currentMinute = calendar.get(java.util.Calendar.MINUTE);

            // Get account identifier from tag:
            this.accountIdentifier = this.getTag();

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
            TimeAccountDetailActivity.updateTimeDisplay(getActivity(), this.accountIdentifier);
            // Set sync toggle alarm:
            this.setSyncAlarm(hourOfDay, minute, AlarmType.START_ALARM);
            // Set disable settings button visibility:
            TimeAccountDetailActivity.toggleViewVisibility(getActivity(), this.accountIdentifier);
        }

        void saveTimeShp(int hourOfDay, int minute) {
            SharedPreferences shp = getActivity().getApplicationContext().getSharedPreferences(Preferences.AccountTimes._NAME_, MODE_PRIVATE);
            SharedPreferences.Editor editor = shp.edit();
            editor.putInt(this.accountIdentifier + Preferences.AccountTimes.START_HOUR_SUFFIX, hourOfDay);
            editor.putInt(this.accountIdentifier + Preferences.AccountTimes.START_MINUTE_SUFFIX, minute);
            editor.apply();
        }

        // Give an error if the end time is before the start time or vice versa:
        boolean checkTimeConsistency(int hourOfDay, int minute) {
            SharedPreferences shp = getActivity().getApplicationContext().getSharedPreferences(Preferences.AccountTimes._NAME_, MODE_PRIVATE);
            int hourOfDayEnd = shp.getInt(this.accountIdentifier + Preferences.AccountTimes.END_HOUR_SUFFIX, 0);
            int minuteEnd = shp.getInt(this.accountIdentifier + Preferences.AccountTimes.END_MINUTE_SUFFIX, 0);
            if(hourOfDayEnd == 0 && minuteEnd == 0) { // If start time is set for the first time:
                // Set end time to start time + 1 minute:
                SharedPreferences.Editor editor = shp.edit();
                editor.putInt(this.accountIdentifier + Preferences.AccountTimes.END_HOUR_SUFFIX, hourOfDay);
                editor.putInt(this.accountIdentifier + Preferences.AccountTimes.END_MINUTE_SUFFIX, minute + 1);
                editor.apply();
                return true;
            }
            if(hourOfDay > hourOfDayEnd) { return false; }
            if(hourOfDay == hourOfDayEnd && minute >= minuteEnd) { return false; }
            else { return true; }
        }

        // Call the corresponding method that sets/resets the sync toggle alarms:
        void setSyncAlarm(int hourOfDay, int minute, int alarmType) {
            // If one (or both) of the times set is before the present time, set the day offset to 1:
            int dayOffset = 0;
            final java.util.Calendar calendar = java.util.Calendar.getInstance();
            final int currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
            final int currentMinute = calendar.get(java.util.Calendar.MINUTE);
            if((hourOfDay < currentHour) || (hourOfDay == currentHour && minute < currentMinute)) {
                dayOffset = 1;
            }
            TimeAccountDetailActivity.setSyncAlarm(alarmType, getActivity().getApplicationContext(), this.accountIdentifier, dayOffset);
        }
    }

    // Make end time picker class:
    public static class EndTimePickerFragment extends StartTimePickerFragment implements TimePickerDialog.OnTimeSetListener{

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
            TimeAccountDetailActivity.updateTimeDisplay(getActivity(), this.accountIdentifier);
            // Set sync toggle alarm:
            this.setSyncAlarm(hourOfDay, minute, AlarmType.END_ALARM);
            // Set disable settings button visibility:
            TimeAccountDetailActivity.toggleViewVisibility(getActivity(), this.accountIdentifier);
        }

        void saveTimeShp(int hourOfDay, int minute) {
            SharedPreferences shp = getActivity().getApplicationContext().getSharedPreferences(Preferences.AccountTimes._NAME_, MODE_PRIVATE);
            SharedPreferences.Editor editor = shp.edit();
            editor.putInt(this.accountIdentifier + Preferences.AccountTimes.END_HOUR_SUFFIX, hourOfDay);
            editor.putInt(this.accountIdentifier + Preferences.AccountTimes.END_MINUTE_SUFFIX, minute);
            editor.apply();
        }

        boolean checkTimeConsistency(int hourOfDay, int minute) {
            SharedPreferences shp = getActivity().getApplicationContext().getSharedPreferences(Preferences.AccountTimes._NAME_, MODE_PRIVATE);
            int hourOfDayStart = shp.getInt(this.accountIdentifier + Preferences.AccountTimes.START_HOUR_SUFFIX, 0);
            int minuteStart = shp.getInt(this.accountIdentifier + Preferences.AccountTimes.START_MINUTE_SUFFIX, 0);
            if(hourOfDay < hourOfDayStart) { return false; }
            if(hourOfDay == hourOfDayStart && minute <= minuteStart) { return false; }
            else { return true; }
        }
    }
}
