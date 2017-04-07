package org.t2.synconwifi;

import android.accounts.Account;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by t2 on 07.04.17.
 */

public class AccountListAdapter extends ArrayAdapter<Account> {
    private ArrayList<Account> accountList;
    private Context mContext;

    public AccountListAdapter(Context context, int textViewId, ArrayList<Account> accountList) {
        super(context, textViewId, accountList);
        this.mContext = context;
        this.accountList = new ArrayList<Account>();
        this.accountList.addAll(accountList);
    }

    private class ViewHolder {
        CheckBox checkBox;
        TextView textViewAccountType;
        TextView textViewAccountName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.account_list_item, null);

            holder = new ViewHolder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.accountListItemCheckBox);
            holder.textViewAccountType = (TextView) convertView.findViewById(R.id.accountListItemTextViewType);
            holder.textViewAccountName = (TextView) convertView.findViewById(R.id.accountListItemTextViewName);
            convertView.setTag(holder);

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //TODO: Implement saving of checked list items in shared preferences
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Account account = accountList.get(position);
        holder.textViewAccountName.setText(account.name);
        holder.textViewAccountType.setText(account.type);
        holder.textViewAccountName.setTag(account);
        holder.textViewAccountType.setTag(account);

        return convertView;
    }
}
