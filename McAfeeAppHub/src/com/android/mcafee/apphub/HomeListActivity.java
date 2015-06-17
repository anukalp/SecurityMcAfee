
package com.android.mcafee.apphub;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.mcafee.apphub.filter.CustomFilter;
import com.android.mcafee.apphub.filter.FilterUtils;
import com.android.mcafee.apphub.loader.AppHubListLoader;
import com.android.mcafee.apphub.model.AppHubDetailsJsonData;
import com.android.mcafee.apphub.model.CustomJSONWrapper;
import com.android.mcafee.apphub.views.FilterManagerView;

/**
 * List_Activity for basic initialization and setup of UI, manages loaders for
 * JSON parsing and filter related changes Drawer Layout is used to display the
 * filter content
 * 
 * @author Anukalp
 */
public class HomeListActivity extends Activity implements LoaderCallbacks<CustomJSONWrapper>,
        DrawerListener, OnItemClickListener {

    private static final String TAG_FILTER = "filter";

    private ListView mListView;

    private DrawerLayout mDrawerLayout;

    private AppHubListAdapter mAdapter;

    public List<AppHubDetailsJsonData> mData;

    public ArrayList<AppHubDetailsJsonData> mIntentData;

    private TextView mProductTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != savedInstanceState) {
            ContentValues cv = (ContentValues)savedInstanceState.get(TAG_FILTER);
            if (null != cv) {
                CustomFilter.getInstance().setDefaultFilter(cv);
            }
        }
        setContentView(R.layout.activity_home);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mListView = (ListView)findViewById(R.id.listView);
        mAdapter = new AppHubListAdapter(getApplicationContext());
        mListView.setAdapter(mAdapter);
        mDrawerLayout.setDrawerShadow(android.R.drawable.alert_light_frame, GravityCompat.START);
        mDrawerLayout.setDrawerListener(this);
        mProductTextView = new TextView(this);
        mListView.addFooterView(mProductTextView);
        ImageButton image = (ImageButton)findViewById(R.id.filter_button);
        image.setVisibility(View.VISIBLE);
        image.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerVisible(GravityCompat.END)) {
                    mDrawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.END);
                }
            }
        });
        mListView.setOnItemClickListener(this);
        if (null != getIntent()) {
            mIntentData = getIntent().getParcelableArrayListExtra(SplashScreenActivity.DATA_EXTRA);
            if (null != mIntentData) {
                mAdapter.setFlightData(mIntentData);
                mAdapter.notifyDataSetChanged();
                mProductTextView.setText(getResources().getText(R.string.count) + ""
                        + mIntentData.size());
            }
        }
    }

    @Override
    public void onStart() {
        getLoaderManager().initLoader(0, null, this);
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        // TODO : apply fix properly this is just not to load the activity again
        // To solve when back pressed to home it reads json data again from launcher icon
        // Having a local Database will be a good option
        if (!moveTaskToBack(true)) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sort_by) {
            if (mDrawerLayout.isDrawerVisible(GravityCompat.END)) {
                mDrawerLayout.closeDrawer(GravityCompat.END);
            } else {
                mDrawerLayout.openDrawer(GravityCompat.END);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerSlide(View arg0, float arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDrawerStateChanged(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public Loader<CustomJSONWrapper> onCreateLoader(int id, Bundle args) {
        return new AppHubListLoader(getApplicationContext());
    }

    @Override
    public void onLoadFinished(Loader<CustomJSONWrapper> loader, CustomJSONWrapper data) {
        if (null != data) {
            if (null != mIntentData) {
                mData = mIntentData;
                mIntentData = null;
                return;
            }
            mAdapter.setFlightData(data.getListData());
            mAdapter.notifyDataSetChanged();
            mData = data.getListData();
            mProductTextView.setText(getResources().getText(R.string.count) + ""
                    + data.getListData().size());
        }
    }

    @Override
    public void onLoaderReset(Loader<CustomJSONWrapper> loader) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDrawerClosed(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != imm) {
            imm.hideSoftInputFromWindow(
                    ((FilterManagerView)findViewById(R.id.filter_manager)).getWindowToken(), 0);
        }
        ((FilterManagerView)findViewById(R.id.filter_manager)).initValues();
    }

    @Override
    public void onDrawerOpened(View view) {
        ((FilterManagerView)findViewById(R.id.filter_manager)).initValues();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(TAG_FILTER, CustomFilter.getInstance().getCurrentFilter());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, AppHubDetailActivity.class);
        intent.putExtra(AppHubDetailActivity.DETAIL_DATA, mAdapter.getItem(position));
        startActivity(intent);
    }

    public void restFilters() {
        if (null != mData)
            new FilterAsyncTask().execute();
    }

    /**
     * AyncTask : to load latest filter details Original data is copied into
     * other map and then passed to adapter. Since, we don't have a local
     * database we need to keep two copies to avoid parsing JSON
     **/
    public class FilterAsyncTask extends AsyncTask<Void, Void, ArrayList<AppHubDetailsJsonData>> {

        /**
         * WeakReference to avoid context/window leak from progress dialog
         */
        private WeakReference<ProgressDialog> mProgress;

        private ContentValues mFilter;

        private ArrayList<AppHubDetailsJsonData> mLocalDataSet;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("anu", "OnPreExecute");
            mProgress = new WeakReference<ProgressDialog>(new ProgressDialog(HomeListActivity.this));
            mProgress.get().show();
            mFilter = CustomFilter.getInstance().getCurrentFilter();
            mLocalDataSet = new ArrayList<AppHubDetailsJsonData>(mData);
        }

        @Override
        protected ArrayList<AppHubDetailsJsonData> doInBackground(Void... params) {
            boolean isChanged = false;
            ArrayList<AppHubDetailsJsonData> removedData = new ArrayList<AppHubDetailsJsonData>();
            if (null != mLocalDataSet) {
                for (AppHubDetailsJsonData flightsJSONData : mLocalDataSet) {
                    if (FilterUtils.shouldRemoveEntry(mFilter, flightsJSONData)) {
                        removedData.add(flightsJSONData);
                        isChanged = true;
                    }
                }
            }
            if (isChanged) {
                mLocalDataSet.removeAll(removedData);
            }
            Collections.sort(mLocalDataSet);
            return mLocalDataSet;
        }

        @Override
        protected void onPostExecute(ArrayList<AppHubDetailsJsonData> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (null != mProgress.get()) {
                mProgress.get().dismiss();
                mProgress.clear();
                mProgress = null;
            }
            if (mDrawerLayout.isDrawerVisible(GravityCompat.END)) {
                mDrawerLayout.closeDrawer(GravityCompat.END);
            } else {
                mDrawerLayout.openDrawer(GravityCompat.END);
            }
            if (null != result) {
                mAdapter.setFlightData(result);
                mAdapter.notifyDataSetChanged();
                mProductTextView.setText(getResources().getText(R.string.count) + ""
                        + result.size());
            }
        }
    }

}
