
package com.android.mcafee.apphub.loader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class AppHubDetailDrawableLoader extends AsyncTask<String, Void, Bitmap> {
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    private static final String TAG = "AppHubDetailDrawableLoader";

    private final WeakReference<ImageView> mImageViewReference;

    private String data = null;

    public AppHubDetailDrawableLoader(ImageView imageView) {
        mImageViewReference = new WeakReference<ImageView>(imageView);
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        data = params[0];
        return downloadUrlToBitmap(data, 240, 240);
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        Log.d(TAG, "onPostExecute :: " + bitmap);
        if (mImageViewReference != null && bitmap != null) {
            final ImageView imageView = mImageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private Bitmap decodeSampledBitmapFromStream(InputStream is, int reqWidth, int reqHeight) {

        Log.d(TAG, "decodeSampledBitmapFromStream :: " + is);
/*        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // Calculate inSampleSize
        // options.inSampleSize = 2;//calculateInSampleSize(options, reqWidth,
        // reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;*/
        return BitmapFactory.decodeStream(is);
    }

    /**
     * Download a bitmap from a URL and prepare a Bitmap from stream.
     *
     * @param urlString The URL to fetch
     * @return bitmap if successful, null otherwise
     */
    private Bitmap downloadUrlToBitmap(String urlString, int reqWidth, int reqHeight) {
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        Log.d(TAG, "doInBackground :: " + data + " width :: " + reqHeight + " height :  "
                + reqWidth);
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection)url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            return decodeSampledBitmapFromStream(in, reqWidth, reqHeight);
        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
            }
        }
        return null;
    }

}
