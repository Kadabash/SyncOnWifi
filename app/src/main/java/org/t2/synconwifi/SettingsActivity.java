package org.t2.synconwifi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
                    //Disable service:
                    SharedPreferences.Editor editor = sharedPreferencesSettings.edit();
                    editor.putBoolean("ServiceEnabled", false);
                    editor.apply();

                    //Prompt user to save sync settings:
                    ConfigRestoreDialogFragment configRestoreDialog = new ConfigRestoreDialogFragment();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    configRestoreDialog.show(fragmentManager, getString(R.string.config_restore_alert_dialogue_tag));
                }
                if(!serviceEnabled && isChecked) {
                    //Enable service:
                    SharedPreferences.Editor editor = sharedPreferencesSettings.edit();
                    editor.putBoolean("ServiceEnabled", true);
                    editor.apply();

                    //Prompt user to save sync settings:
                    ConfigSaveDialogFragment configSaveDialog = new ConfigSaveDialogFragment();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    configSaveDialog.show(fragmentManager, getString(R.string.config_save_alert_dialogue_tag));
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

        //Add button to change to SsidActivity:
        Button ssidActivityButton = (Button) findViewById(R.id.ssidSettingsButton);
        ssidActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SsidActivity.class);
                startActivity(intent);
            }
        });
    }

    //Copied from https://developer.android.com/guide/topics/ui/dialogs.html
    public static class ConfigSaveDialogFragment extends DialogFragment {
        @Override @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.config_save_alert_dialogue)
                    .setPositiveButton(R.string.alert_dialogue_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO: save sync settings
                            SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences("ConfigBackup", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            //Put an entry into "ConfigBackup" for each account:
                            AccountManager accountManager = AccountManager.get(getActivity().getApplicationContext());
                            Account[] accounts = accountManager.getAccounts();
                            for(Account account : accounts) {
                                String accountTypeAndName = account.type + ";" + account.name;
                                editor.putBoolean(accountTypeAndName, WifiStateChangeReceiver.getSyncAutomatically(account));
                            }

                            //Save that a backup exists:
                            editor.putBoolean("BackupExists", true);

                            editor.apply();

                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.alert_dialogue_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            //If a previous backup exists, show overwrite notice:
            SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences("ConfigBackup", MODE_PRIVATE);
            if(sharedPreferences.getBoolean("BackupExists", false)) {
                builder.setMessage(getString(R.string.config_save_alert_dialogue) + "\n" + getString(R.string.config_save_alert_dialogue_overwrite_notice));
            }

            return builder.create();
        }
    }

    //Copied from https://developer.android.com/guide/topics/ui/dialogs.html
    public static class ConfigRestoreDialogFragment extends DialogFragment {
        @Override @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.config_restore_alert_dialogue)
                    .setPositiveButton(R.string.alert_dialogue_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO: save sync settings
                            SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences("ConfigBackup", MODE_PRIVATE);

                            //Show error toast and abort if no backup exists:
                            if(!sharedPreferences.getBoolean("BackupExists", false)) {
                                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.config_restore_alert_dialogue_no_backup_notice), Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                                return;
                            }

                            //Get the entry from "ConfigBackup" for each account:
                            AccountManager accountManager = AccountManager.get(getActivity().getApplicationContext());
                            Account[] accounts = accountManager.getAccounts();
                            for(Account account : accounts) {
                                String accountTypeAndName = account.type + ";" + account.name;
                                WifiStateChangeReceiver.setSyncAutomatically(account, sharedPreferences.getBoolean(accountTypeAndName, false));
                            }

                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.alert_dialogue_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            return builder.create();
        }
    }
}
