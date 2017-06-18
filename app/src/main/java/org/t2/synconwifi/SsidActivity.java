package org.t2.synconwifi;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

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
        //Check if WiFi is off, since WifiManager is not available when it is disabled:
        if(!(manager.getWifiState() == WifiManager.WIFI_STATE_ENABLING || manager.getWifiState() == WifiManager.WIFI_STATE_ENABLED))
        {
            Toast.makeText(getApplicationContext(), getString(R.string.alert_wifi_is_disabled), Toast.LENGTH_LONG).show();
            //Return to main activity:
            this.finish();
            return;
        }
        ArrayList<WifiConfiguration> configurations = new ArrayList<>();
        configurations.addAll(manager.getConfiguredNetworks());

        //Set list adapter:
        this.listAdapter = new SsidListAdapter(this, R.layout.ssid_list_item, configurations);
        this.ssidListView.setAdapter(listAdapter);
    }
}
