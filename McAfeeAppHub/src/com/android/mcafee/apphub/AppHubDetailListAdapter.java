
package com.android.mcafee.apphub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.mcafee.apphub.model.AppHubDetailsJsonData;

/**
 * Adapter for custom data set and uses ViewHolder internally to improve
 * bindView timings, acts as a bridge between adapter view and data set passed
 * 
 * @author Anukalp
 */
public class AppHubDetailListAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflterService;

    private String mDollar;

    private AppHubDetailsJsonData mData;

    private String mFree;

    public static String[] mNames = new String[] {
            "Type/Price", "Users", "Last Updated"
    };

    public AppHubDetailListAdapter(Context context, AppHubDetailsJsonData data) {
        mLayoutInflterService = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDollar = context.getResources().getString(R.string.dollar);
        mFree = context.getResources().getString(R.string.free);
        mData = data;
    }

    static class ViewHolder {
        TextView name;

        TextView value;

        int position;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (null == convertView) {
            convertView = mLayoutInflterService.inflate(R.layout.detail_list_item, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.value = (TextView)convertView.findViewById(R.id.value);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.name.setText(mNames[position]);
        String value = null;
        switch (position) {
            case 0:
                if (mData.getPrice() == 0) {
                    value = mData.getType() + " / " + mFree;
                } else {
                    value = mData.getType() + " / " + mDollar + mData.getPrice();
                }
                break;
            case 1:
                value = mData.getUsers() + "";
                break;
            case 2:
                value = mData.getLastUpdate();
                break;

            default:
                break;
        }
        holder.value.setText(value);

        return convertView;
    }

    @Override
    public String getItem(int position) {
        return mNames[position];
    }
}
