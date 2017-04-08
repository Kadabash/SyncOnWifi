package org.t2.synconwifi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
    public static final int PERMISSION_GRANTED_GET_CONTACTS = 1;
    public static final int PERMISSION_GRANTED_WRITE_SYNC_SETTIGNS = 2;
    private AccountListAdapter listAdapter = null;

    private ListView accountListView;

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

        //Create checkbox list to select accounts to control:
        this.accountListView = (ListView) findViewById(R.id.accountSettingListView);

        //Ask for runtime permissions:
        int permissionCheckGetAccounts = ContextCompat.checkSelfPermission(this, android.Manifest.permission.GET_ACCOUNTS);
        if(permissionCheckGetAccounts != PackageManager.PERMISSION_GRANTED) {
            //TODO: explain why the permission is needed (as in https://developer.android.com/training/permissions/requesting.html#perm-request)
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.GET_ACCOUNTS}, PERMISSION_GRANTED_GET_CONTACTS);
        } else {
            setUpListView();
        }

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

    private void setUpListView() {
        //Get Accounts:
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccounts();
        ArrayList<Account> accountArrayList = new ArrayList<>(Arrays.asList(accounts));

        //Set list adapter and add list to layout:
        this.listAdapter = new AccountListAdapter(this, R.layout.account_list_item, accountArrayList);
        this.accountListView.setAdapter(listAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_GRANTED_GET_CONTACTS: {
                //If permission has been granted:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpListView();
                } else {
                    //If permission has NOT been granted:
                    Toast.makeText(this, "This app will not function without permission to read accounts. Please open the app again and grant it.", Toast.LENGTH_LONG).show(); //TODO: Internationalise string
                }
            } break;
        }
    }
}
