
package com.android.mcafee.apphub;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.mcafee.apphub.loader.PhotoManager;
import com.android.mcafee.apphub.model.AppHubDetailsJsonData;
import com.android.volley.toolbox.NetworkImageView;

public class AppHubDetailActivity extends Activity implements OnClickListener {

    public static final String TAG = "MyTag";

    public static final String DETAIL_DATA = "detail_data";

    private AppHubDetailsJsonData mData;

    private TextView mName;

    private TextView mRating;

    private ListView mListView;

    private TextView mDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        if (null != intent) {
            mData = intent.getParcelableExtra(DETAIL_DATA);
        } else if (null != savedInstanceState) {
            mData = savedInstanceState.getParcelable(DETAIL_DATA);
        }
        if (null == mData)
            finish();
        setContentView(R.layout.detail_layout);
        String imageURL = mData.getImageURL();
        NetworkImageView imageView = (NetworkImageView)findViewById(R.id.imageView);
        ((PhotoManager)getApplicationContext().getSystemService(PhotoManager.PHOTO_SERVICE))
                .preparePhotoUris(new WeakReference<NetworkImageView>(imageView), imageURL);
        setupLayout();
    }

    private void setupLayout() {
        mName = (TextView)findViewById(R.id.product_name);
        mRating = (TextView)findViewById(R.id.product_rating);
        mDescription = ((TextView)findViewById(R.id.description));
        mRating.setText(mData.getRating() + "");
        mName.setText(mData.getName());
        mDescription.setText(mData.getDescription());
        Button shareButton = (Button)findViewById(R.id.share);
        shareButton.setOnClickListener(this);
        Button appStore = (Button)findViewById(R.id.app_store);
        appStore.setOnClickListener(this);
        Button back = (Button)findViewById(R.id.back);
        back.setOnClickListener(this);
        Button sms = (Button)findViewById(R.id.sms);
        sms.setOnClickListener(this);
        mListView = (ListView)findViewById(R.id.details_list);
        mListView.setAdapter(new AppHubDetailListAdapter(getApplicationContext(), mData));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(DETAIL_DATA, mData);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mData.getDescription());
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, mData.getGooglePlayURL());
                startActivity(Intent.createChooser(sendIntent,
                        getResources().getText(R.string.send_to)));
                break;
            case R.id.app_store:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mData.getGooglePlayURL()));
                startActivity(intent);
                break;
            case R.id.back:
                onBackPressed();
                break;
            case R.id.sms:
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setData(Uri.parse("sms:"));
                smsIntent.putExtra(
                        "sms_body",
                        mData.getGooglePlayURL() + System.getProperty("line.separator")
                                + mData.getDescription());
                startActivity(smsIntent);
                break;

            default:
                break;
        }

    }
}
