package org.t2.synconwifi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class TimeAccountListAdapter extends ArrayAdapter<Account> {

    private ArrayList<Account> accountList;
    private Context mContext;

    TimeAccountListAdapter(Context context, int textViewId, ArrayList<Account> accountList) {
        super(context, textViewId, accountList);
        this.mContext = context;
        this.accountList = new ArrayList<>();
        this.accountList.addAll(accountList);
    }

    private class ViewHolder {
        ImageView imageViewAccountImage;
        TextView textViewAccountType;
        TextView textViewAccountName;
    }

    @Override @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TimeAccountListAdapter.ViewHolder holder;

        if(convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext.getApplicationContext());
            convertView = layoutInflater.inflate(R.layout.time_account_list_item, parent, false);

            holder = new TimeAccountListAdapter.ViewHolder();
            holder.imageViewAccountImage = (ImageView) convertView.findViewById(R.id.accountTimeImageView);
            holder.textViewAccountType = (TextView) convertView.findViewById(R.id.accountTimeListItemTextViewType);
            holder.textViewAccountName = (TextView) convertView.findViewById(R.id.accountTimeListItemTextViewName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //Assign account to list item:
        Account account = accountList.get(position);
        holder.textViewAccountName.setTag(account);
        holder.textViewAccountType.setTag(account);

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
