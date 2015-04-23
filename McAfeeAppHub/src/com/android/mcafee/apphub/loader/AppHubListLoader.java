
package com.android.mcafee.apphub.loader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;

import com.android.mcafee.apphub.filter.CustomFilter;
import com.android.mcafee.apphub.filter.FilterUtils;
import com.android.mcafee.apphub.model.CustomJSONWrapper;

public class AppHubListLoader extends AsyncTaskLoader<CustomJSONWrapper> {

    private CustomJSONWrapper mJsonWrapper;

    private CustomJSONWrapper sCachedResult;

    public AppHubListLoader(Context context) {
        super(context);
    }

    @Override
    public CustomJSONWrapper loadInBackground() {
        CustomJSONWrapper mData = sCachedResult;
        sCachedResult = null;
        if (null != mData)
            return mData;
        ContentValues cv = CustomFilter.getInstance().getCurrentFilter();
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL("http://mcafee.0x10.info/api/app?type=json");
            urlConnection = (HttpURLConnection)url.openConnection();
            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
            mData = new CustomJSONWrapper();
            mData.populateJsonData(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != urlConnection) {
                urlConnection.disconnect();
            }
            if (null != mData && null != mData.getListData()) {
                Collections.sort(mData.getListData());
                int length = mData.getListData().size();
                if (length > 0) {
                    cv.put(FilterUtils.TAG_FILTER_PRICE_START, mData.getListData().get(0)
                            .getPrice());
                    cv.put(FilterUtils.TAG_FILTER_PRICE_END, mData.getListData().get(length - 1)
                            .getPrice());
                    cv.put(FilterUtils.TAG_FILTER_RATING_START, 0.0);
                    cv.put(FilterUtils.TAG_FILTER_RATING_END, 5.0);
                    cv.put(FilterUtils.TAG_FILTER_TYPE_PRICE, true);
                    cv.put(FilterUtils.TAG_FILTER_TYPE_RATING, false);
                    CustomFilter.getInstance().setDefaultFilter(cv);
                    CustomFilter.getInstance().applyDefaultFilter();
                }
            }
        }
        return mData;
    }

    public void cacheResult() {
        if (mJsonWrapper == null) {
            sCachedResult = null;
        } else {
            sCachedResult = mJsonWrapper;
        }
    }

    @Override
    protected void onStartLoading() {
        if (mJsonWrapper != null) {
            deliverResult(mJsonWrapper);
        }
        if (takeContentChanged() || mJsonWrapper == null) {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(CustomJSONWrapper data) {
        if (isReset()) {
            mJsonWrapper = null;
            return;
        }
        CustomJSONWrapper oldValue = mJsonWrapper;
        mJsonWrapper = data;

        if (isStarted()) {
            super.deliverResult(mJsonWrapper);
        }

        if (null != oldValue && oldValue != mJsonWrapper) {
            oldValue = null;
        }
        super.deliverResult(data);
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(CustomJSONWrapper data) {
        data = null;
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();
        mJsonWrapper = null;
    }

}
