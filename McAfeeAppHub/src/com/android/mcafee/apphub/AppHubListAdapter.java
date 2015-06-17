
package com.android.mcafee.apphub;

import java.util.ArrayList;
import java.util.List;

import com.android.mcafee.apphub.filter.CustomFilter;
import com.android.mcafee.apphub.filter.FilterUtils;
import com.android.mcafee.apphub.model.AppHubDetailsJsonData;
import com.android.mcafee.apphub.model.AppHubListLoaderData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter for custom data set and uses ViewHolder internally to improve
 * bindView timings, acts as a bridge between adapter view and data set passed
 * 
 * @author Anukalp
 */
public class AppHubListAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflterService;

    private String mDollar;

    private List<AppHubDetailsJsonData> mData;

    private String mFree;

    public AppHubListAdapter(Context context) {
        mLayoutInflterService = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDollar = context.getResources().getString(R.string.dollar);
        mFree = context.getResources().getString(R.string.free);
    }

    public void setFlightData(List<AppHubDetailsJsonData> list) {
        mData = list;
    }

    static class ViewHolder {
        TextView name;

        TextView value;

        ImageView image;

        int position;
    }

    @Override
    public int getCount() {
        return null != mData ? mData.size() : 0;
    }

    @Override
    public AppHubDetailsJsonData getItem(int position) {
        return null != mData ? mData.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (null == convertView) {
            convertView = mLayoutInflterService.inflate(R.layout.list_item, null, false);
            holder = new ViewHolder();
            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.value = (TextView)convertView.findViewById(R.id.value);
            holder.image = (ImageView)convertView.findViewById(R.id.image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        final AppHubListLoaderData data = mData.get(position);
        holder.name.setText(data.getName());
        if (CustomFilter.getInstance().getCurrentFilter()
                .getAsBoolean(FilterUtils.TAG_FILTER_TYPE_PRICE)) {
            if (data.getPrice() == 0) {
                holder.value.setText(mFree);
            } else {
                holder.value.setText(mDollar + Double.toString(data.getPrice()));
            }
        } else {
            holder.value.setText(Double.toString(data.getRating()));
        }
        holder.image.setImageResource(R.drawable.greater_image);
        return convertView;
    }
}
