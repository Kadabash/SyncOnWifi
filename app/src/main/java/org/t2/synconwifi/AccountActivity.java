package org.t2.synconwifi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
            //TODO: explain why the permission is needed (as in https://developer.android.com/training/permissions/requesting.html#perm-request)
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.GET_ACCOUNTS}, PERMISSION_GRANTED_GET_CONTACTS);
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
                    Toast.makeText(this, "This app will not function without permission to read accounts. Please open the app again and grant it.", Toast.LENGTH_LONG).show(); //TODO: Internationalise string
                }
            } break;
        }
    }
}
