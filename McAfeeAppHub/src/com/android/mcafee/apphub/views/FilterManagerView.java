
package com.android.mcafee.apphub.views;

import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.android.mcafee.apphub.HomeListActivity;
import com.android.mcafee.apphub.R;
import com.android.mcafee.apphub.filter.CustomFilter;
import com.android.mcafee.apphub.filter.FilterUtils;

/**
 * Manager class for managing child view's listeners and callback
 * {@link TimePickerDialog} to input time associated changes
 * 
 * @author Anukalp
 */
public class FilterManagerView extends LinearLayout implements OnClickListener {

    private ContentValues mContentValues = new ContentValues();

    private EditText mMinPrice;

    private EditText mMaxPrice;

    private EditText mMinRating;

    private EditText mMaxRating;

    private Button mClear;

    private Button mApply;

    private CheckBox mPriceCheckBox;

    private CheckBox mRatingCheckBox;

    public FilterManagerView(Context context) {
        super(context);
    }

    public FilterManagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMinPrice = (EditText)findViewById(R.id.min_price);
        mMaxPrice = (EditText)findViewById(R.id.max_price);
        mMinRating = (EditText)findViewById(R.id.min_rating);
        mMaxRating = (EditText)findViewById(R.id.max_rating);
        mClear = (Button)findViewById(R.id.clear);
        mApply = (Button)findViewById(R.id.apply);
        mPriceCheckBox = (CheckBox)findViewById(R.id.button);
        mRatingCheckBox = (CheckBox)findViewById(R.id.button1);
        mClear.setOnClickListener(this);
        mApply.setOnClickListener(this);
        findViewById(R.id.price_container).setOnClickListener(this);
        findViewById(R.id.rating_container).setOnClickListener(this);

        initValues();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.price_container:
                mPriceCheckBox.toggle();
                break;
            case R.id.rating_container:
                mRatingCheckBox.toggle();
                break;
            case R.id.clear:
                clearFilter();
                break;
            case R.id.apply:
                buildFilter();
                break;

            default:
                break;
        }
    }

    public void initValues() {
        ContentValues cv = CustomFilter.getInstance().getCurrentFilter();
        for (String key : cv.keySet()) {
            switch (key) {
                case FilterUtils.TAG_FILTER_PRICE_START:
                    mMinPrice.setText(cv.getAsString(key));
                    break;
                case FilterUtils.TAG_FILTER_PRICE_END:
                    mMaxPrice.setText(cv.getAsString(key));
                    break;
                case FilterUtils.TAG_FILTER_RATING_START:
                    mMinRating.setText(cv.getAsString(key));
                    break;
                case FilterUtils.TAG_FILTER_RATING_END:
                    mMaxRating.setText(cv.getAsString(key));
                    break;
                case FilterUtils.TAG_FILTER_TYPE_PRICE:
                    mPriceCheckBox.setChecked(cv.getAsBoolean(key));
                    break;
                case FilterUtils.TAG_FILTER_TYPE_RATING:
                    mRatingCheckBox.setChecked(cv.getAsBoolean(key));
                    break;

                default:
                    break;
            }
        }
    }

    private void clearFilter() {
        CustomFilter.getInstance().applyDefaultFilter();
        if (getContext() instanceof HomeListActivity) {
            ((HomeListActivity)getContext()).restFilters();
        }
    }

    private void buildFilter() {
        mContentValues.clear();
        mContentValues.put(FilterUtils.TAG_FILTER_PRICE_START, mMinPrice.getText().toString());
        mContentValues.put(FilterUtils.TAG_FILTER_PRICE_END, mMaxPrice.getText().toString());
        mContentValues.put(FilterUtils.TAG_FILTER_RATING_START, mMinRating.getText().toString());
        mContentValues.put(FilterUtils.TAG_FILTER_RATING_END, mMaxRating.getText().toString());
        mContentValues.put(FilterUtils.TAG_FILTER_TYPE_PRICE, mPriceCheckBox.isChecked());
        mContentValues.put(FilterUtils.TAG_FILTER_TYPE_RATING, mRatingCheckBox.isChecked());
        CustomFilter.getInstance().buildFilter(mContentValues);
        if (getContext() instanceof HomeListActivity) {
            ((HomeListActivity)getContext()).restFilters();
        }
    }
}
