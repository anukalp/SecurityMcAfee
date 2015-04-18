
package com.android.mcafee.apphub.model;

import com.android.mcafee.apphub.filter.CustomFilter;
import com.android.mcafee.apphub.filter.FilterUtils;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * {@link AppHubDetailsJsonData} is used to removed redundancy while inflating
 * list/details
 * 
 * @author Anukalp
 **/
public class AppHubDetailsJsonData extends AppHubListLoaderData implements
        Comparable<AppHubDetailsJsonData>, Parcelable {

    public static final String TAG_IMAGE = "imagee";

    public static final String TAG_TYPE = "type";

    public static final String TAG_USERS = "users";

    public static final String TAG_UPDATE = "last_update";

    public static final String TAG_DESCRIPTION = "description";

    public static final String TAG_GOOGLE_PLAY = "url";

    private final String mImageURL;

    private final String mType;

    private final String mLastUpdate;

    private final long mUsers;

    private final String mDescription;

    private final String mGooglePlayURL;

    public AppHubDetailsJsonData(String mName, double mPrice, double mRating, String mImageURL,
            String mType, String mLastUpdate, long mUsers, String mDescrption, String mGooglePlayURL) {
        super(mName, mPrice, mRating);
        this.mImageURL = mImageURL;
        this.mType = mType;
        this.mLastUpdate = mLastUpdate;
        this.mUsers = mUsers;
        this.mDescription = mDescrption;
        this.mGooglePlayURL = mGooglePlayURL;
    }

    private AppHubDetailsJsonData(Parcel in) {
        super(in);
        mImageURL = in.readString();
        mType = in.readString();
        mUsers = in.readLong();
        mLastUpdate = in.readString();
        mDescription = in.readString();
        mGooglePlayURL = in.readString();
    }

    public String getImageURL() {
        return mImageURL;
    }

    public String getType() {
        return mType;
    }

    public String getLastUpdate() {
        return mLastUpdate;
    }

    public long getUsers() {
        return mUsers;
    }

    public String getGooglePlayURL() {
        return mGooglePlayURL;
    }

    public String getDescription() {
        return mDescription;
    }

    @Override
    public String toString() {
        return "AppHubDetailsJsonData [mImageURL=" + mImageURL + ", mType=" + mType
                + ", mLastUpdate=" + mLastUpdate + ", mUsers=" + mUsers + ", mDescription="
                + mDescription + ", mGooglePlayURL=" + mGooglePlayURL + ", toString()="
                + super.toString() + "]";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mImageURL);
        dest.writeString(mType);
        dest.writeLong(mUsers);
        dest.writeString(mLastUpdate);
        dest.writeString(mDescription);
        dest.writeString(mGooglePlayURL);
    }

    @Override
    public int compareTo(AppHubDetailsJsonData another) {
        ContentValues values = CustomFilter.getInstance().getCurrentFilter();
        if (!values.containsKey(FilterUtils.TAG_FILTER_TYPE_PRICE) || values.getAsBoolean(FilterUtils.TAG_FILTER_TYPE_PRICE)) {
            return another.getPrice() > this.getPrice() ? -1 : 0;
        } else {
            return another.getRating() > this.getRating() ? -1 : 0;
        }

    }

    public static final Parcelable.Creator<AppHubDetailsJsonData> CREATOR = new Parcelable.Creator<AppHubDetailsJsonData>() {
        public AppHubDetailsJsonData createFromParcel(Parcel in) {
            return new AppHubDetailsJsonData(in);
        }

        public AppHubDetailsJsonData[] newArray(int size) {
            return new AppHubDetailsJsonData[size];
        }
    };

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }
}
