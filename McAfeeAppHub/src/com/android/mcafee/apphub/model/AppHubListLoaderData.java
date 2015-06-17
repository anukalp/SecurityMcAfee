
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

    private final String name;

    private final double price;

    private final double rating;

    public AppHubListLoaderData(Parcel in) {
        name = in.readString();
        price = in.readDouble();
        rating = in.readDouble();
    }

    public double getRating() {
        return rating;
    }

    public double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "AppHubListLoaderData [mName=" + name + ", mPrice=" + price + ", mRating="
                + rating + "]";
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(price);
        dest.writeDouble(rating);
    }
}
