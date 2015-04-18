
package com.android.mcafee.apphub.filter;

import android.content.ContentValues;

/**
 * CustomFilter : Singleton class to hold filter value across different
 * application components ContentValues concept is used to later support DB
 * related changes Filter will be derived through content key value pairs Basic
 * Filter will be set by loader with the supplied data for Min and Max price
 * TODO: SharedPreference to save data for basic interaction or database support
 * and fetch filter details in separate handler thread Never push view listeners
 * or callbacks inside this class otherwise it will not be available for garbage
 * collection
 * 
 * @author Anukalp
 **/
public abstract class CustomFilter {

    private static CustomFilter mFilter;

    public static CustomFilter getInstance() {
        if (null == mFilter) {
            mFilter = createcustomFilter();
        }
        return mFilter;
    }

    private static synchronized CustomFilter createcustomFilter() {
        return new CustomFilterImpl();
    }

    public abstract void buildFilter(ContentValues newValues);

    public abstract ContentValues getCurrentFilter();

    public abstract void setDefaultFilter(ContentValues defaultValue);

    public abstract void applyDefaultFilter();
}

class CustomFilterImpl extends CustomFilter {
    private ContentValues mValues;

    private ContentValues mDefaultValues;

    public CustomFilterImpl() {
        mValues = new ContentValues();
    }

    @Override
    public void buildFilter(ContentValues newValues) {
        if (FilterUtils.isFilterChanged(newValues, mValues)) {
            mValues = newValues;
        }
    }

    @Override
    public ContentValues getCurrentFilter() {
        return mValues;
    }

    @Override
    public void setDefaultFilter(ContentValues defaultValue) {
        mDefaultValues = defaultValue;
    }

    @Override
    public void applyDefaultFilter() {
        mValues = mDefaultValues;
    }
}
