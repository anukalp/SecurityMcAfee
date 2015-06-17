
package com.android.mcafee.apphub.loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.android.mcafee.apphub.model.AppHubDetailsJsonData;
import com.android.mcafee.apphub.model.CustomJSONWrapper;
import com.android.mcafee.apphub.model.GsonRequest;
import com.android.volley.Cache.Entry;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

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

    public abstract void preparePhotoUris(WeakReference<NetworkImageView> view, String request);

    public abstract void startRequest(String url, Listener<AppHubDetailsJsonData[]> listener);
}

class PhotoManagerImpl extends PhotoManager implements Callback, ImageCache {

    private final LruCache<Object, Bitmap> mBitmapCache;
    
    /** Default on-disk cache directory. */
    private static final String DEFAULT_CACHE_DIR = "voley";

    private static final int BITMAP_CACHE_SIZE = 36864 * 48; // 1728K

    private static final int MESSAGE_REQUEST_LOADING = 0;

    private static final int MESSAGE_PHOTOS_LOADED = 1;

    /**
     * Handler for messages sent to the UI thread.
     */
    private final Handler mMainThreadHandler = new Handler(this);

    private final ConcurrentHashMap<String, WeakReference<ImageView>> mPendingRequests = new ConcurrentHashMap<String, WeakReference<ImageView>>();

    private BitmapLoaderThread mLoaderThread;

    private RequestQueue mRequestQueue;

    private RequestQueue mJsonQueue;

    private ImageLoader mImageLoaderTask;

    private DiskBasedCache mJsonCache;

    private final Context mContext;

    public PhotoManagerImpl(Context context) {
        mContext = context;
        final ActivityManager am = ((ActivityManager)context
                .getSystemService(Context.ACTIVITY_SERVICE));
        mRequestQueue = Volley.newRequestQueue(context);
        final float cacheSizeAdjustment = (am.isLowRamDevice()) ? 0.5f : 1.0f;

        final int bitmapCacheSize = (int)(cacheSizeAdjustment * BITMAP_CACHE_SIZE);
        mBitmapCache = new LruCache<Object, Bitmap>(bitmapCacheSize) {
            @Override
            protected int sizeOf(Object key, Bitmap value) {
                return value.getByteCount();
            }
        };
        // DiskCache for json Request
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);
        mJsonCache = new DiskBasedCache(cacheDir);
        mJsonQueue = getRequestQueue();
        mImageLoaderTask = new ImageLoader(mRequestQueue, this);
    }

    public RequestQueue getRequestQueue() {
        HttpStack stack = null;
        String userAgent = "volley/0";
        try {
            String packageName = mContext.getPackageName();
            PackageInfo info = mContext.getPackageManager().getPackageInfo(packageName, 0);
            userAgent = packageName + "/" + info.versionCode;
        } catch (NameNotFoundException e) {
        }

        if (stack == null) {
            if (Build.VERSION.SDK_INT >= 9) {
                stack = new HurlStack();
            } else {
                // Prior to Gingerbread, HttpUrlConnection was unreliable.
                // See:
                // http://android-developers.blogspot.com/2011/09/androids-http-clients.html
                stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
            }
        }

        Network network = new BasicNetwork(stack);
        RequestQueue lQueue = new RequestQueue(mJsonCache, network);
        // Start the queue
        lQueue.start();
        return lQueue;
    }

    /**
     * add the request to the request queue
     * 
     * @param req
     */
    private <T> void addToRequestQueue(Request<T> req) {
        req.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mJsonQueue.add(req);
    }

    /**
     * Start Request Uses the Custom GSON request to get and parse the json from
     * server and also GSON will loads the data into the POJO.
     * 
     * @param url
     */
    public void startRequest(String url, Listener<AppHubDetailsJsonData[]> response) {
        Log.i(TAG, "startRequest reponse" + url);
        Entry entry = mJsonCache.get(url);
        if (entry != null) {
            try {
                String data = new String(entry.data, "UTF-8");
                Log.i(TAG, "Cache Hit");
                if (null != data) {
                    Gson gson = new Gson();
                    AppHubDetailsJsonData[] result = gson.fromJson(data,
                            AppHubDetailsJsonData[].class);
                    response.onResponse(result);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            GsonRequest<AppHubDetailsJsonData[]> jsObjRequest = new GsonRequest<AppHubDetailsJsonData[]>(
                    url, AppHubDetailsJsonData[].class, null, response, createErrorListener());
            addToRequestQueue(jsObjRequest);
        }
    }

    private Response.ErrorListener createErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMsg = error.getMessage();
                Log.i(TAG, "Cache miss " + errorMsg);
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
    public void preparePhotoUris(WeakReference<NetworkImageView> view, String request) {
        /*
         * boolean loaded = loadCachedPhoto(view, request); if (loaded) {
         * mPendingRequests.remove(request); } else {
         * mPendingRequests.put(request, view);
         * mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING); }
         */
        view.get().setImageUrl(request, mImageLoaderTask);
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

    @Override
    public Bitmap getBitmap(String url) {
        // TODO Auto-generated method stub
        return mBitmapCache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        mBitmapCache.put(url, bitmap);
    }

}
