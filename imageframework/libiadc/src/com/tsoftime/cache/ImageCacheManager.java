package com.tsoftime.cache;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;
import com.tsoftime.ImageMangerConfig;

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
    private ImageCacheManager(Context ctx)
    {
        context = ctx;
        //cache = new HashMap<String, SoftReference<Bitmap>>();
        mFilePathCache = new HashMap<String, String>();

        // Get memory class of this device, exceeding this amount will throw an
        // OutOfMemory exception.
        final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        Log.d(TAG, String.format("Max memory %dm", memClass));
        // Use 1/2th of the available memory for this memory cache.
        final int cacheSize = 1024 * 1024 * memClass / 4;
        mMemCache  = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in bytes rather than number of items.
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
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
     * @param url
     * @return
     */
    public String getImageFilePath(String url)
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
            path.append(externalCacheDir.getAbsolutePath());                // external cache dir
            path.append(ImageMangerConfig.instance().getImageStoreDir());   // image cache dir
            path.append('/').append(sb.charAt(0));
            path.append('/').append(sb.charAt(1));
            path.append('/').append(sb.charAt(2));
            path.append('/').append(sb.substring(3, sb.length()));
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
    public boolean isCachedInFileSystem(String url, long expire)
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
     * Get the image from the cache
     *
     * @param url the url of the image
     * @return if found, return the image or null.
     */
    public Bitmap getImageFromCache(String url)
    {
        Log.d(TAG, String.format("Get image from cache: %s", url));
        return mMemCache.get(url);
    }

    /**
     * save the bitmap to cache.
     *
     * @param url the url of the image
     * @param bmp the bitmap
     */
    public void saveToCache(String url, Bitmap bmp)
    {
        if (url == null || bmp == null) return;
        mMemCache.put(url, bmp);
    }

    private Context context;
    private static final String TAG = ImageCacheManager.class.getSimpleName();

    private HashMap<String, String> mFilePathCache;
    private static ImageCacheManager mInstance = null;
    private LruCache<String, Bitmap> mMemCache;
}
