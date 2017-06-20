package org.t2.synconwifi;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class AccountActivity extends AppCompatActivity {

    public static final int PERMISSION_GRANTED_GET_CONTACTS = 1;
    private AccountListAdapter listAdapter = null;
    private ListView accountListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        //Create checkbox list to select accounts to control:
        this.accountListView = (ListView) findViewById(R.id.accountList);

        //Ask for runtime permissions:
        int permissionCheckGetAccounts = ContextCompat.checkSelfPermission(this, android.Manifest.permission.GET_ACCOUNTS);
        if(permissionCheckGetAccounts != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS)) {
                //Prepare user for allowing runtime permissions:
                FragmentManager fragmentManager = getSupportFragmentManager();
                PermissionsExplanationDialogFragment dialogFragment = new PermissionsExplanationDialogFragment();
                dialogFragment.show(fragmentManager, getString(R.string.permissions_explanation_dialogue_tag));
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.GET_ACCOUNTS}, PERMISSION_GRANTED_GET_CONTACTS);
            }
        } else {
            setUpListView();
        }
    }

    private void setUpListView() {
        //Get Accounts:
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccounts();
        ArrayList<Account> accountArrayList = new ArrayList<>(Arrays.asList(accounts));

        //Set list adapter:
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
                    Toast.makeText(this, getString(R.string.permission_not_granted_notice), Toast.LENGTH_LONG).show();
                }
            } break;
        }
    }

    public static class PermissionsExplanationDialogFragment extends DialogFragment {
        @Override @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.permissions_explanation_dialogue)
                    .setPositiveButton(R.string.alert_dialogue_understood, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });


            return builder.create();
        }
    }
}
