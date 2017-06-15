package org.t2.synconwifi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


class AccountListAdapter extends ArrayAdapter<Account> {

    private ArrayList<Account> accountList;
    private Context mContext;

    AccountListAdapter(Context context, int textViewId, ArrayList<Account> accountList) {
        super(context, textViewId, accountList);
        this.mContext = context;
        this.accountList = new ArrayList<>();
        this.accountList.addAll(accountList);
    }

    private class ViewHolder {
        CheckBox checkBox;
        ImageView imageViewAccountImage;
        TextView textViewAccountType;
        TextView textViewAccountName;
    }

    @Override @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext.getApplicationContext());
            convertView = layoutInflater.inflate(R.layout.account_list_item, parent, false);

            holder = new ViewHolder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.accountListItemCheckBox);
            holder.imageViewAccountImage = (ImageView) convertView.findViewById(R.id.accountImageView);
            holder.textViewAccountType = (TextView) convertView.findViewById(R.id.accountListItemTextViewType);
            holder.textViewAccountName = (TextView) convertView.findViewById(R.id.accountListItemTextViewName);
            convertView.setTag(holder);

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences sharedPreferences = mContext.getApplicationContext().getSharedPreferences(Preferences.AccountsActive._NAME_, Context.MODE_PRIVATE);
                    Account associatedAccount = (Account) buttonView.getTag();
                    String accountTypeAndName = associatedAccount.type + ";" + associatedAccount.name;     //Accounts are identified in AccountsActive preferences by their type and name, separated by a ";".
                    boolean isActiveAccount = sharedPreferences.getBoolean(accountTypeAndName, false);
                    if(isActiveAccount) {
                        if(!isChecked) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(accountTypeAndName, false);        //Set account inactive.
                            editor.apply();
                        }
                    } else {
                        if(isChecked) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(accountTypeAndName, true);       //Set account active.
                            editor.apply();
                        }
                    }

                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //Assign account to list item:
        Account account = accountList.get(position);
        holder.checkBox.setTag(account);
        holder.textViewAccountName.setTag(account);
        holder.textViewAccountType.setTag(account);

        //Set checked state from shared preferences:
        SharedPreferences sharedPreferences = mContext.getApplicationContext().getSharedPreferences(Preferences.AccountsActive._NAME_, Context.MODE_PRIVATE);
        String accountTypeAndName = account.type + ";" + account.name;     //Accounts are identified in AccountsActive preferences by their type and name, separated by a ";".
        holder.checkBox.setChecked(sharedPreferences.getBoolean(accountTypeAndName, false));

        //Set account icon:
        Drawable accountIcon = null;
        PackageManager packageManager = mContext.getApplicationContext().getPackageManager();
        AccountManager accountManager = AccountManager.get(mContext.getApplicationContext());
        AuthenticatorDescription[] descriptions = accountManager.getAuthenticatorTypes();
        for(AuthenticatorDescription description : descriptions) {
            if(description.type.equals(account.type)) {
                accountIcon = packageManager.getDrawable(description.packageName, description.iconId, null);
            }
        }
        holder.imageViewAccountImage.setImageDrawable(accountIcon);

        //Set account name and type text:
        holder.textViewAccountName.setText(account.name);
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(account.type, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(applicationInfo != null)
            holder.textViewAccountType.setText(packageManager.getApplicationLabel(applicationInfo));
        else
            holder.textViewAccountType.setText(account.type);

        //Add image description for accessibility:
        holder.imageViewAccountImage.setContentDescription(holder.textViewAccountType.getText()
                + mContext.getApplicationContext().getString(R.string.account_image_description)
                + holder.textViewAccountName.getText());

        return convertView;
    }
}
