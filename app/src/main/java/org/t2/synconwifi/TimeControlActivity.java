package org.t2.synconwifi;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

import static org.t2.synconwifi.AccountActivity.PERMISSION_GRANTED_GET_CONTACTS;

public class TimeControlActivity extends AppCompatActivity {

    private TimeAccountListAdapter listAdapter = null;
    private ListView accountListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_control);

        // Set up account list view:
        this.accountListView = (ListView) findViewById(R.id.accountListTimeControl);

        // Ask for runtime permissions to access accounts:
        int permissionCheckGetAccounts = ContextCompat.checkSelfPermission(this, android.Manifest.permission.GET_ACCOUNTS);
        if(permissionCheckGetAccounts != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS)) {
                // Prepare user for allowing runtime permissions:
                FragmentManager fragmentManager = getSupportFragmentManager();
                AccountActivity.PermissionsExplanationDialogFragment dialogFragment = new AccountActivity.PermissionsExplanationDialogFragment();
                dialogFragment.show(fragmentManager, getString(R.string.permissions_explanation_dialogue_tag));
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.GET_ACCOUNTS}, PERMISSION_GRANTED_GET_CONTACTS);
            }
        } else {
            setUpListView();
        }

        // Open detail activity on list item click:
        this.accountListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), TimeAccountDetailActivity.class);
                Account tagAccount = (Account) ((TimeAccountListAdapter.ViewHolder)view.getTag()).textViewAccountName.getTag();
                final String accountIdentifier = tagAccount.type + ";" + tagAccount.name;
                intent.putExtra(TimeAccountDetailActivity.ACCOUNT_EXTRA_NAME, accountIdentifier);
                startActivity(intent);
            }
        });
    }

    private void setUpListView() {
        // Get Accounts:
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccounts();
        ArrayList<Account> accountArrayList = new ArrayList<>(Arrays.asList(accounts));

        // Select only those accounts which are controlled by the app:
        ArrayList<Account> controlledAccountArrayList = new ArrayList<>();
        SharedPreferences accountPreferences = getApplicationContext().getSharedPreferences(Preferences.AccountsActive._NAME_, MODE_PRIVATE);
        for(Account account : accountArrayList) {
            String accountIdentifier = account.type + ";" + account.name;
            if(accountPreferences.getBoolean(accountIdentifier, false)) {
                controlledAccountArrayList.add(account);
            }
        }

        // Set list adapter:
        this.listAdapter = new TimeAccountListAdapter(this, R.layout.time_account_list_item, controlledAccountArrayList);
        this.accountListView.setAdapter(listAdapter);
    }
}
