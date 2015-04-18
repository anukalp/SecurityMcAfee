
package com.android.mcafee.apphub.model;

import android.os.Parcel;

/**
 * {@link AppHubListLoaderData} is used to removed redundancy while inflating
 * list/details
 * 
 * @author Anukalp
 **/
public class AppHubListLoaderData {

    public static final String TAG_NAME = "name";

    public static final String TAG_PRICE = "price";

    public static final String TAG_RATING = "rating";

    private final String mName;

    private final double mPrice;

    private final double mRating;

    public AppHubListLoaderData(String mName, double mPrice, double mRating) {
        this.mName = mName;
        this.mPrice = mPrice;
        this.mRating = mRating;
    }

    public AppHubListLoaderData(Parcel in) {
        mName = in.readString();
        mPrice = in.readDouble();
        mRating = in.readDouble();
    }

    public double getRating() {
        return mRating;
    }

    public double getPrice() {
        return mPrice;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return "AppHubListLoaderData [mName=" + mName + ", mPrice=" + mPrice + ", mRating="
                + mRating + "]";
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeDouble(mPrice);
        dest.writeDouble(mRating);
    }
}
