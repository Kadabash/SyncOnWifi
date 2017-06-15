package org.t2.synconwifi;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class SsidListAdapter extends ArrayAdapter<WifiConfiguration> {

    private ArrayList<WifiConfiguration> wifiConfigurationList;
    private Context mContext;

    SsidListAdapter(Context context, int textViewId, ArrayList<WifiConfiguration> wifiConfigurationList) {
        super(context, textViewId, wifiConfigurationList);
        this.mContext = context;
        this.wifiConfigurationList = new ArrayList<>();
        this.wifiConfigurationList.addAll(wifiConfigurationList);
    }

    private class ViewHolder {
        CheckBox checkBox;
        TextView textViewSsid;
    }

    @Override @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext.getApplicationContext());
            convertView = layoutInflater.inflate(R.layout.ssid_list_item, parent, false);

            holder = new ViewHolder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.ssidListItemCheckBox);
            holder.textViewSsid = (TextView) convertView.findViewById(R.id.ssidListItemTextView);
            convertView.setTag(holder);

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences sharedPreferences = mContext.getApplicationContext().getSharedPreferences(Preferences.TrustedSSID._NAME_, Context.MODE_PRIVATE);
                    String assignedSsid = (String) buttonView.getTag();
                    boolean wifiIsEnabled = sharedPreferences.getBoolean(assignedSsid, false);
                    if(isChecked) {
                        if(!wifiIsEnabled) {
                            //Set SSID as enabled:
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(assignedSsid, true);
                            editor.apply();
                        }
                    } else {
                        if(wifiIsEnabled) {
                            //Set SSID as disabled:
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(assignedSsid, false);
                            editor.apply();
                        }
                    }
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //Assign Wifi network to list item:
        WifiConfiguration wifiConfiguration = wifiConfigurationList.get(position);
        String wifiSsid = wifiConfiguration.SSID;
        wifiSsid = wifiSsid.substring(1, wifiSsid.length() - 1); //Removes surrounding quotation marks.
        holder.textViewSsid.setText(wifiSsid);
        holder.checkBox.setTag(wifiSsid);

        //Set CheckBox to correct state:
        SharedPreferences sharedPreferencesSsid = mContext.getApplicationContext().getSharedPreferences(Preferences.TrustedSSID._NAME_, Context.MODE_PRIVATE);
        holder.checkBox.setChecked(sharedPreferencesSsid.getBoolean(wifiSsid, false));

        return convertView;
    }
}
