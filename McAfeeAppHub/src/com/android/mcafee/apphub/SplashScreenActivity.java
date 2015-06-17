
package com.android.mcafee.apphub;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.android.mcafee.apphub.filter.CustomFilter;
import com.android.mcafee.apphub.filter.FilterUtils;
import com.android.mcafee.apphub.loader.PhotoManager;
import com.android.mcafee.apphub.model.AppHubDetailsJsonData;
import com.android.mcafee.apphub.model.CustomJSONWrapper;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

public class SplashScreenActivity extends Activity {

    public static final String DATA_EXTRA = "data";

    public static final String TAG_SPLASH_SCREEN = "splash_screen";

    protected static final int ZERO_VALUE = 0;

    protected static final int ONE_VALUE = 1;

    protected static final int TWO_VALUE = 2;

    private ArrayList<AppHubDetailsJsonData> mData;

    private int mCount;

    public SplashScreenActivity() {
    }

    private TimerTask mTask = new TimerTask() {

        @Override
        public void run() {
            final ShapeDrawable shapeDrawable = new ShapeDrawable();
            shapeDrawable.setShape(new OvalShape());
            int colorId = 0;

            switch (mCount) {
                case ZERO_VALUE:
                    colorId = Color.BLUE;
                    mViewId = R.id.image;
                    mOldViewId = R.id.image2;
                    mCount++;
                    break;
                case ONE_VALUE:
                    colorId = Color.CYAN;
                    mViewId = R.id.image1;
                    mOldViewId = R.id.image;
                    mCount++;
                    break;
                case TWO_VALUE:
                    colorId = Color.GREEN;
                    mCount = 0;
                    mViewId = R.id.image2;
                    mOldViewId = R.id.image1;
                    break;

                default:
                    break;
            }
            shapeDrawable.getPaint().setColor(colorId);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    ImageView oldImageView = (ImageView)findViewById(mOldViewId);
                    oldImageView.animate().alpha(0f).setDuration(300);
                    ImageView mImageView = (ImageView)findViewById(mViewId);
                    mImageView.animate().alpha(1f).setDuration(300);
                    mImageView.setBackground(shapeDrawable);
                }
            });
        }

    };

    private int mOldViewId;

    private int mViewId;

    private Response.Listener<AppHubDetailsJsonData[]> createSuccessListener() {
        return new Response.Listener<AppHubDetailsJsonData[]>() {
            @Override
            public void onResponse(AppHubDetailsJsonData[] response) {
                try {
                    Log.d("anu", response.toString());
                    mData = new ArrayList<AppHubDetailsJsonData>();
                    mData.addAll(Arrays.asList(response));
                    ContentValues cv = CustomFilter.getInstance().getCurrentFilter();

                    if (null != mData && null != mData) {
                        Collections.sort(mData);
                        int length = mData.size();
                        if (length > 0) {
                            cv.put(FilterUtils.TAG_FILTER_PRICE_START, mData.get(0).getPrice());
                            cv.put(FilterUtils.TAG_FILTER_PRICE_END, mData.get(length - 1)
                                    .getPrice());
                            cv.put(FilterUtils.TAG_FILTER_RATING_START, 0.0);
                            cv.put(FilterUtils.TAG_FILTER_RATING_END, 5.0);
                            cv.put(FilterUtils.TAG_FILTER_TYPE_PRICE, true);
                            cv.put(FilterUtils.TAG_FILTER_TYPE_RATING, false);
                            CustomFilter.getInstance().setDefaultFilter(cv);
                            CustomFilter.getInstance().applyDefaultFilter();
                        }
                    }

                    if (null != mData) {
                        Intent mainIntent = new Intent(SplashScreenActivity.this,
                                HomeListActivity.class);
                        mainIntent.putParcelableArrayListExtra(DATA_EXTRA, mData);
                        startActivity(mainIntent);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        PhotoManager manager = (PhotoManager)getApplicationContext().getSystemService(
                PhotoManager.PHOTO_SERVICE);
        manager.startRequest("http://mcafee.0x10.info/api/app?type=json", createSuccessListener());
        new Timer(TAG_SPLASH_SCREEN).scheduleAtFixedRate(mTask,
                new Date(System.currentTimeMillis()), 300);
    }

}
