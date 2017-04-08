package org.t2.synconwifi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Add checkbox to disable app functionality:
        CheckBox serviceCheckBox = (CheckBox) findViewById(R.id.serviceSettingCheckBox);
        SharedPreferences sharedPreferencesSettings = getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
        serviceCheckBox.setChecked(sharedPreferencesSettings.getBoolean("ServiceEnabled", false));
        serviceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPreferencesSettings = getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
                boolean serviceEnabled = sharedPreferencesSettings.getBoolean("ServiceEnabled", false);
                if(serviceEnabled && !isChecked) {
                    SharedPreferences.Editor editor = sharedPreferencesSettings.edit();
                    editor.putBoolean("ServiceEnabled", false);
                    editor.apply();
                }
                if(!serviceEnabled && isChecked) {
                    SharedPreferences.Editor editor = sharedPreferencesSettings.edit();
                    editor.putBoolean("ServiceEnabled", true);
                    editor.apply();
                }
            }
        });

        //Add button to change to AccountActivity:
        Button accountActivityButton = (Button) findViewById(R.id.accountSettingsButton);
        accountActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AccountActivity.class);
                startActivity(intent);
            }
        });

        //Add an input field for Wifi SSID:
        EditText editTextSSID = (EditText) findViewById(R.id.ssidSettingEditText);
        SharedPreferences sharedPreferencesSSID = getApplicationContext().getSharedPreferences("TrustedSSID", MODE_PRIVATE);
        String ssidFromSharedPreferences = sharedPreferencesSSID.getString("SSID", "");
        if(!ssidFromSharedPreferences.equals("")) {
            editTextSSID.setText(ssidFromSharedPreferences);
        }
        editTextSSID.setHint("SSID of trusted WIFI network");
        editTextSSID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("TrustedSSID", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("SSID", s.toString());
                editor.apply();
            }
        });
    }
}
