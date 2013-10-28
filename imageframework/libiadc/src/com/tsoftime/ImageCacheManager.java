package com.tsoftime;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * The image cache manager.
 * User: huangcongyu2006
 * Date: 12-6-25 PM3:27
 */
public class ImageCacheManager
{
    public static final int IMAGE_CACHE_CLEAR_DONE = 1;             //
    public static final int IMAGE_CACHE_SIZE_CALCULATE_DONE = 2;    //

    private ImageCacheManager(Context ctx)
    {
        context = ctx;
        //cache = new HashMap<String, SoftReference<Bitmap>>();
        mFilePathCache = new HashMap<String, String>();
    }

    /**
     * Initial the ImageCacheManager.
     * This method MUST be called before any other methods of the ImageCacheManager.
     *
     * @param context the context
     */
    public static void init(Context context)
    {
        mInstance = new ImageCacheManager(context);
    }

    public static ImageCacheManager getInstance()
    {
        return mInstance;
    }

    /**
     * Get the image file path.
     *
     * We use the md5 value of the url to create the file path.
     * If the md5 value of the url is '5d41402abc4b2a76b9719d911017c592', the file path will be
     * /5/d/4/1402abc4b2a76b9719d911017c592.
     * @param url the url eg. http://server.com/hello.jpg
     * @return if the storage is not available, null will be returned;
     */
    synchronized public String getImageFilePath(String url)
    {
        // find from the cache
        String filePath = mFilePathCache.get(url);
        if (filePath != null) return filePath;

        // generate the file path
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(url.getBytes());
            byte[] result = md.digest();
            StringBuilder sb = new StringBuilder(result.length * 2);
            for (byte b : result) {
                sb.append(Character.forDigit((b >>> 4) & 15, 16)).append(Character.forDigit(b & 15, 16));
            }
            // create the path
            StringBuilder path = new StringBuilder();
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir == null) {
                externalCacheDir = context.getCacheDir();
                if (externalCacheDir == null) return null;
            }
            path.append(externalCacheDir.getAbsolutePath());                // external cache dir
            path.append(mImageStoreDir);                                    // image cache dir
            path.append('/').append(sb.charAt(0));
            path.append('/').append(sb.charAt(1));
            path.append('/').append(sb.charAt(2));
            path.append('/').append(sb.substring(3, sb.length()));
            if (url.lastIndexOf('.') > 0) {
                path.append(url.substring(url.lastIndexOf('.')).toLowerCase());           // append the file ext
            } else {
                path.append(".jpg");
            }
            filePath = path.toString();
            mFilePathCache.put(url, filePath);
            return filePath;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Is the image cached in the file system.
     *
     * NOTE:
     *  This method is called in the download thread!
     *
     * @param url       the url of the image
     * @param expire    the expire time of this image
     * @return
     */
    synchronized public boolean isCachedInFileSystem(String url, long expire)
    {
        String filePath = getImageFilePath(url);
        if (filePath == null) return false;

        File imageFile = new File(filePath);
        if (!imageFile.exists()) return false;

        // test whether the image cache expire time exceeded
        Date now = Calendar.getInstance().getTime();
        long timeHasGone = (now.getTime() - imageFile.lastModified()) / 1000;
        // the expire time has exceeded, return null to reload download the image.
        if (timeHasGone > expire) {
            return false;
        }
        return true;
    }

    /**
     * Start clearing the cache
     */
    public void startClearing(Handler handler)
    {
        new CacheCleaner(handler).start();
    }

    /**
     * Start calculating the cache size
     */
    public void startCalculatingTheCacheSize(Handler handler)
    {
        new CacheSizeCalculator(handler).start();
    }

    public boolean isCalculatingCacheSize()
    {
        return mIsCalculatingCacheSize;
    }

    /**
     * The cache size calculator
     *
     * This thread will calculate the cache size in the background.
     */
    private class CacheSizeCalculator extends Thread
    {
        public CacheSizeCalculator(Handler handler)
        {
            setName("CacheSizeCalculator");
            mResultHandler = handler;
        }
        @Override
        public void run()
        {
            synchronized (ImageCacheManager.this) {
                if (mIsCalculatingCacheSize) {
                    if (mResultHandler != null) {
                        mResultHandler.obtainMessage(IMAGE_CACHE_SIZE_CALCULATE_DONE, 0l).sendToTarget();
                    }
                    return;
                }
                mIsCalculatingCacheSize = true;
            }
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir == null) {
                externalCacheDir = context.getCacheDir();
                if (externalCacheDir == null) {
                    if (mResultHandler != null) {
                        mResultHandler.obtainMessage(IMAGE_CACHE_SIZE_CALCULATE_DONE, 0l).sendToTarget();
                    }
                    return ;
                }
            }
            File cacheDir = new File(externalCacheDir.getAbsolutePath() + mImageStoreDir);
            if (!cacheDir.exists()) {
                if (mResultHandler != null) {
                    mResultHandler.obtainMessage(IMAGE_CACHE_SIZE_CALCULATE_DONE, 0l).sendToTarget();
                }
                return;
            }

            mCacheSize = size(cacheDir);
            mIsCalculatingCacheSize = false;
            Log.d(TAG, String.format("Cache size %d", mCacheSize));
            if (mResultHandler != null) {
                mResultHandler.obtainMessage(IMAGE_CACHE_SIZE_CALCULATE_DONE, mCacheSize).sendToTarget();
            }
        }

        /**
         * Delete file or directory
         * @param f
         */
        private long size(File f)
        {
            if (f.isDirectory()) {
                File[] subfiles = f.listFiles();
                long tmp = 0;
                for (File subf : subfiles) {
                    tmp += size(subf);
                }
                Log.d(TAG, String.format("%s : %d", f.getAbsoluteFile(), tmp));
                return tmp;
            } else if (f.isFile()) {
                Log.d(TAG, String.format("%s : %d", f.getAbsoluteFile(), f.length()));
                return f.length();
            }
            return 0;
        }

        private Handler mResultHandler;
    }

    /**
     * Cache cleaner
     */
    private class CacheCleaner extends Thread
    {
        public CacheCleaner(Handler handler)
        {
            setName("CacheCleaner");
            mResultHandler = handler;
        }
        @Override
        public void run()
        {
            synchronized (ImageCacheManager.this) {
                if (mIsClearing) {
                    if (mResultHandler != null) {
                        mResultHandler.obtainMessage(IMAGE_CACHE_CLEAR_DONE).sendToTarget();
                    }
                    return;
                }
                mIsClearing = true;
            }
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir == null) {
                externalCacheDir = context.getCacheDir();
                if (externalCacheDir == null) {
                    if (mResultHandler != null) {
                        mResultHandler.obtainMessage(IMAGE_CACHE_CLEAR_DONE).sendToTarget();
                    }
                    return ;
                }
            }
            File cacheDir = new File(externalCacheDir.getAbsolutePath() + mImageStoreDir);
            if (!cacheDir.exists()) {
                if (mResultHandler != null) {
                    mResultHandler.obtainMessage(IMAGE_CACHE_CLEAR_DONE).sendToTarget();
                }
                return;
            }

            delete(cacheDir);
            mIsClearing = false;
            Log.d(TAG, "cache clearing done.");
            if (mResultHandler != null) {
                mResultHandler.obtainMessage(IMAGE_CACHE_CLEAR_DONE).sendToTarget();
            }
        }

        /**
         * Delete file or directory
         * @param f
         */
        private void delete(File f)
        {
            Log.d(TAG, String.format("delete... %s", f.getAbsolutePath()));
            if (f.isDirectory()) {
                // delete all the files in this directory
                File[] subfiles = f.listFiles();
                for (File subf : subfiles) {
                    delete(subf);
                }
            }
            f.delete();
        }

        private Handler mResultHandler;
    }

    private Context context;
    private long mCacheSize = -1;
    private boolean mIsCalculatingCacheSize;
    private boolean mIsClearing;

    private String mImageStoreDir = "/iadc/cache/";
    private HashMap<String, String> mFilePathCache;

    private static ImageCacheManager mInstance = null;
    private static final String TAG = ImageCacheManager.class.getSimpleName();
}
