package org.t2.synconwifi;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TimeActivity extends AppCompatActivity {

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

                // Show/hide other views in this activity based on check state:
                toggleVisibilityTimeSettings(isChecked);
            }
        });
        timeControlEnabledCheckBox.setChecked(timeControlEnabled);

        // If time-based control is disabled, hide other Views on activity start:
        toggleVisibilityTimeSettings(timePreferences.getBoolean("timeControlEnabled", false));

    }

    // Show/hide other views in this acitivity based on enabled state of time-based control:
    public void toggleVisibilityTimeSettings(boolean enabled) {
        List<View> viewsList = new ArrayList<>();
        viewsList.add((TextView) findViewById(R.id.timeSelectionTextView));
        viewsList.add((Button) findViewById(R.id.timeStartSpinnerButton));
        viewsList.add((Button) findViewById(R.id.timeEndSpinnerButton));
        viewsList.add((TextView) findViewById(R.id.timeStartHeadline));
        viewsList.add((TextView) findViewById(R.id.timeEndHeadline));
        for(View v : viewsList) {
            v.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        }
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
            editor.commit();
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
            editor.commit();
        }

        protected boolean checkTimeConsistency(int hourOfDay, int minute) {
            SharedPreferences shp = getActivity().getApplicationContext().getSharedPreferences("timePreferences", MODE_PRIVATE);
            int hourOfDayStart = shp.getInt("startHour", 23);
            int minuteStart = shp.getInt("startMinute", 59);
            if(hourOfDay < hourOfDayStart) { return false; }
            if(hourOfDay == hourOfDayStart && minute <= minuteStart) { return false; }
            else { return true; }
        }
    }
}
