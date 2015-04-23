
package com.android.mcafee.apphub.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

public abstract class PhotoManager implements ComponentCallbacks2 {

    public static final String PHOTO_SERVICE = "photo_service";

    protected static final String TAG = "PhotoManager";

    public static PhotoManager getInstance(Context context) {
        Context applicationContext = context.getApplicationContext();
        PhotoManager service = (PhotoManager)applicationContext.getSystemService(PHOTO_SERVICE);
        if (service == null) {
            service = createPhotoManager(applicationContext);
            Log.e(TAG, "No photo service in context: " + applicationContext);
        }
        return service;
    }

    public static synchronized PhotoManager createPhotoManager(Context context) {
        return new PhotoManagerImpl(context);
    }

    public abstract void preparePhotoUris(WeakReference<ImageView> view, String request);

}

class PhotoManagerImpl extends PhotoManager implements Callback {

    private final LruCache<Object, Bitmap> mBitmapCache;

    private static final int BITMAP_CACHE_SIZE = 36864 * 48; // 1728K

    private static final int MESSAGE_REQUEST_LOADING = 0;

    private static final int MESSAGE_PHOTOS_LOADED = 1;

    /**
     * Handler for messages sent to the UI thread.
     */
    private final Handler mMainThreadHandler = new Handler(this);

    private final ConcurrentHashMap<String, WeakReference<ImageView>> mPendingRequests = new ConcurrentHashMap<String, WeakReference<ImageView>>();

    private BitmapLoaderThread mLoaderThread;

    public PhotoManagerImpl(Context context) {
        final ActivityManager am = ((ActivityManager)context
                .getSystemService(Context.ACTIVITY_SERVICE));

        final float cacheSizeAdjustment = (am.isLowRamDevice()) ? 0.5f : 1.0f;

        final int bitmapCacheSize = (int)(cacheSizeAdjustment * BITMAP_CACHE_SIZE);
        mBitmapCache = new LruCache<Object, Bitmap>(bitmapCacheSize) {
            @Override
            protected int sizeOf(Object key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTrimMemory(int level) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            // Clear the caches. Note all pending requests will be removed too.
            mBitmapCache.evictAll();
        }
    }

    @Override
    public void preparePhotoUris(WeakReference<ImageView> view, String request) {
        boolean loaded = loadCachedPhoto(view, request);
        if (loaded) {
            mPendingRequests.remove(request);
        } else {
            mPendingRequests.put(request, view);
            mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
        }
    }

    private boolean loadCachedPhoto(WeakReference<ImageView> imageViewReference, String request) {
        Bitmap bitmap = mBitmapCache.get(request);
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
            return true;
        }
        return false;
    }

    private void processLoadedImages() {
        Iterator<String> iterator = mPendingRequests.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            WeakReference<ImageView> view = mPendingRequests.get(key);
            boolean loaded = loadCachedPhoto(view, key);
            if (loaded) {
                iterator.remove();
            }
        }

        if (!mPendingRequests.isEmpty()) {
            mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_REQUEST_LOADING:
                ensureLoaderThread();
                mLoaderThread.requestLoading();
                break;
            case MESSAGE_PHOTOS_LOADED:
                processLoadedImages();
                break;

            default:
                break;
        }
        return false;
    }

    public void ensureLoaderThread() {
        if (mLoaderThread == null) {
            mLoaderThread = new BitmapLoaderThread();
            mLoaderThread.start();
        }
    }

    public class BitmapLoaderThread extends HandlerThread implements Callback {
        private static final int IO_BUFFER_SIZE = 8 * 1024;

        private static final String LOADER_THREAD_NAME = "BitmapLoaderThread";

        private static final int MESSAGE_LOAD_PHOTOS = 0;

        private Handler mLoaderThreadHandler;

        private final HashMap<String, Integer> mPhotoUris = new HashMap<String, Integer>();

        public BitmapLoaderThread() {
            super(LOADER_THREAD_NAME);
        }

        public void ensureHandler() {
            if (mLoaderThreadHandler == null) {
                mLoaderThreadHandler = new Handler(getLooper(), this);
            }
        }

        public void requestLoading() {
            ensureHandler();
            mLoaderThreadHandler.removeMessages(MESSAGE_LOAD_PHOTOS);
            mLoaderThreadHandler.sendEmptyMessage(MESSAGE_LOAD_PHOTOS);
        }

        private int calculateInSampleSize(int originalSmallerExtent, int targetExtent) {
            // If we don't know sizes, we can't do sampling.
            if (targetExtent < 1)
                return 1;
            if (originalSmallerExtent < 1)
                return 1;
            int extent = originalSmallerExtent;
            int sampleSize = 1;
            while ((extent >> 1) >= targetExtent * 0.8f) {
                sampleSize <<= 1;
                extent >>= 1;
            }
            return sampleSize;

        }

        /**
         * Receives the above message, loads photos and then sends a message to
         * the main thread to process them.
         */
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_LOAD_PHOTOS:
                    loadPhotosInBackground();
                    break;
            }
            return true;
        }

        private Bitmap decodeSampledBitmapFrombytes(byte[] photoData, int targetExtent) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = calculateInSampleSize(
                    Math.min(options.outWidth, options.outHeight), targetExtent);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
        }

        /**
         * Download a bitmap from a URL and prepare a Bitmap from stream.
         *
         * @param urlString The URL to fetch
         * @return bitmap if successful, null otherwise
         */
        private Bitmap downloadUrlToBitmap(String urlString, int targetExtent) {
            HttpURLConnection urlConnection = null;
            InputStream in = null;
            try {
                final URL url = new URL(urlString);
                urlConnection = (HttpURLConnection)url.openConnection();
                in = urlConnection.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[IO_BUFFER_SIZE];

                while ((nRead = in.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                return decodeSampledBitmapFrombytes(buffer.toByteArray(), targetExtent);
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

        public void loadPhotosInBackground() {
            obtainPhotoIdsAndUrisToLoad(mPhotoUris);
            loadPhotoUris();
            mMainThreadHandler.sendEmptyMessage(MESSAGE_PHOTOS_LOADED);
        }

        private void loadPhotoUris() {
            Iterator<String> uris = mPhotoUris.keySet().iterator();
            while (uris.hasNext()) {
                String photoData = uris.next();
                int targetExtent = mPhotoUris.get(photoData);
                Bitmap bitmap = downloadUrlToBitmap(photoData, targetExtent);
                if (null != mBitmapCache.get(photoData) || null == bitmap)
                    continue;
                mBitmapCache.put(photoData, bitmap);
            }
        }

    }

    /**
     * Populates an array of photo IDs that need to be loaded. Also decodes
     * bitmaps that we have already loaded
     */
    private void obtainPhotoIdsAndUrisToLoad(HashMap<String, Integer> photoUris) {
        photoUris.clear();

        Iterator<String> iterator = mPendingRequests.keySet().iterator();
        while (iterator.hasNext()) {
            String uri = iterator.next();
            WeakReference<ImageView> view = mPendingRequests.get(uri);
            if (null == view || null == view.get())
                continue;
            photoUris.put(uri, Math.min(view.get().getHeight(), view.get().getWidth()));
        }
    }

}
