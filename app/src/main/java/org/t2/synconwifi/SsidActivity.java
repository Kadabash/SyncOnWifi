package org.t2.synconwifi;

import android.accounts.AccountManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SsidActivity extends AppCompatActivity {

    private SsidListAdapter listAdapter = null;
    private ListView ssidListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssid);

        //Create Checkbox list to select WiFi networks:
        this.ssidListView = (ListView) findViewById(R.id.ssidList);

        setUpListView();
    }

    private void setUpListView() {
        //Get WiFi networks:
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        ArrayList<WifiConfiguration> configurations = new ArrayList<>();
        configurations.addAll(manager.getConfiguredNetworks());

        //Set list adapter:
        this.listAdapter = new SsidListAdapter(this, R.layout.ssid_list_item, configurations);
        this.ssidListView.setAdapter(listAdapter);
    }
}
