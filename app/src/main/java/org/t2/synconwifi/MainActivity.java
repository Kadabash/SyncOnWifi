package org.t2.synconwifi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity {

    public static final int PERMISSION_GRANTED_GET_CONTACTS = 1;
    private AccountListAdapter listAdapter = null;

    private LinearLayout mainActivityLayout;
    private ListView accountListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create checkbox list to select accounts to control:
        this.mainActivityLayout = (LinearLayout) findViewById(R.id.mainActivityLayout);
        this.accountListView = new ListView(mainActivityLayout.getContext());

        //Ask for runtime permission to access accounts:
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.GET_ACCOUNTS);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
            //TODO: explain why the permission is needed (as in https://developer.android.com/training/permissions/requesting.html#perm-request)

            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.GET_ACCOUNTS}, PERMISSION_GRANTED_GET_CONTACTS);
        } else {
            setUpListView();
        }

        //Program flow continues in function setUpListView().
    }

    private void setUpListView() {
        //Get Accounts:
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccounts();
        ArrayList<Account> accountArrayList = new ArrayList<Account>();
        for(Account account : accounts) {
            accountArrayList.add(account);
        }

        //Set list adapter and add list to layout:
        this.listAdapter = new AccountListAdapter(this, R.layout.account_list_item, accountArrayList);
        this.accountListView.setAdapter(listAdapter);
        this.mainActivityLayout.addView(this.accountListView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_GRANTED_GET_CONTACTS: {

                //If permission has been granted:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpListView();
                } else {
                    //If permission has NOT been granted:
                    Toast.makeText(this, "This app will not function without permission to read accounts. Please open the app again and grant it.", Toast.LENGTH_LONG).show(); //TODO: Internationalise string
                }
            }
            break;
            //More cases can be added to this switch if more permissions are required.
        }
    }
}
