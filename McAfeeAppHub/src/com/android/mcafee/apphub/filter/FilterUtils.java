
package com.android.mcafee.apphub.filter;

import android.content.ContentValues;
import android.util.Log;

import com.android.mcafee.apphub.model.AppHubListLoaderData;

/**
 * Utility class for filter related changes validate list data based on filter
 * requirements
 * 
 * @author Anukalp
 */
public class FilterUtils {

    public static final String TAG_FILTER_PRICE_START = "filter_price_start";

    public static final String TAG_FILTER_RATING_START = "filter_rating_start";

    public static final String TAG_FILTER_PRICE_END = "filter_price_end";

    public static final String TAG_FILTER_RATING_END = "filter_rating_end";

    public static final String TAG_FILTER_TYPE_PRICE = "filter_type_price";

    public static final String TAG_FILTER_TYPE_RATING = "filter_type_rating";

    public static boolean isFilterChanged(ContentValues newValues, ContentValues oldValues) {
        boolean filterChanged = false;
        Log.d("anu", "isFilterChanged :: " + newValues.valueSet() + " :: " + oldValues.valueSet());
        if (null == oldValues || newValues == null)
            return true;
        for (final String key : newValues.keySet()) {
            if (!oldValues.containsKey(key)) {
                filterChanged = true;
                break;
            } else if (!newValues.get(key).equals(oldValues.get(key))) {
                filterChanged = true;
                break;
            }
        }
        return filterChanged;
    }

    public static boolean shouldRemoveEntry(ContentValues value, AppHubListLoaderData data) {
        boolean remove = false;
        double price = data.getPrice();
        double rating = data.getRating();

        if ((value.getAsBoolean(FilterUtils.TAG_FILTER_TYPE_PRICE) && (price < value
                .getAsDouble(FilterUtils.TAG_FILTER_PRICE_START) || price > value
                .getAsDouble(FilterUtils.TAG_FILTER_PRICE_END)))
                || (value.getAsBoolean(FilterUtils.TAG_FILTER_TYPE_RATING) && (rating < value
                        .getAsDouble(FilterUtils.TAG_FILTER_RATING_START) || rating > value
                        .getAsDouble(FilterUtils.TAG_FILTER_RATING_END)))) {
            remove = true;
        }
        return remove;
    }

}
