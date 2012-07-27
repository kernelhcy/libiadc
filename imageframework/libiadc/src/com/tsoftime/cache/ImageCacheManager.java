package com.tsoftime.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.*;

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
        cache = new HashMap<String, SoftReference<Bitmap>>();
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
     * Get the image from the file system cache.
     *
     * NOTE:
     *  This method is called in the download thread!
     *
     * @param url   the url of the image
     * @return      the bitmap of the image or null for error or not found.
     */
    public Bitmap getImageFromFileSystemCache(String url)
    {
        ImageURLPathPair pair = ImageURLPathPair.select(url, context);
        if (pair == null) return null;

        // test whether the image cache expire time exceeded
        Date now = Calendar.getInstance().getTime();
        long timeHasGone = (now.getTime() - pair.getCreatedAt().getTime()) / 1000;
        // the expire time has exceeded, return null to reload download the image.
        if (timeHasGone > pair.getExpire()) {
            Log.d(TAG, String.format("image %s expire time exceed : %d", pair.getUrl(), timeHasGone));
            // remove the old cach image.
            File oldFile = new File(pair.getPath());
            if (oldFile.exists()) oldFile.delete();
            cache.remove(url);
            return null;
        }

        Log.d(TAG, String.format("%s: %s", pair.getUrl(), pair.getPath()));
        Bitmap bmp = BitmapFactory.decodeFile(pair.getPath());

        // update the use count
        pair.setUseCount(pair.getUseCount() + 1);
        pair.save(context);

        if (bmp == null){
            Log.d(TAG, String.format("Image decode failed : %s", pair.getPath()));
        }
        return bmp;
    }

    /**
     * Get the image from the cache
     *
     * @param url the url of the image
     * @return if found, return the image or null.
     */
    public Bitmap getImageFromCache(String url)
    {
        // Get the image from the bitmap cache
        // If the bitmap has recycled by the gc. Get it from the file cache.
        SoftReference<Bitmap> ref = cache.get(url);
        if (ref != null) {
            if (ref.get() == null) {
                cache.remove(url);
            } else {
                return ref.get();
            }
        }
        return null;
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
        Log.d(TAG, String.format("cache bmp %s", url));
        SoftReference<Bitmap> ref = new SoftReference<Bitmap>(bmp);
        // remove the old cache
        if (cache.get(url) != null) cache.remove(url);
        // cache it
        cache.put(url, ref);
        printCache();
    }

    private void printCache()
    {
        int refCount = 0, bmpCount = 0;
        HashMap.Entry<String, SoftReference<Bitmap>> entry;
        Set<HashMap.Entry<String, SoftReference<Bitmap>>> entries = cache.entrySet();
        Iterator<HashMap.Entry<String, SoftReference<Bitmap>>> iterator;
        iterator = entries.iterator();
        while (iterator.hasNext()) {
            entry = iterator.next();
            refCount++;
            if (entry.getValue().get() != null) bmpCount++;
        }
        Log.d(TAG, String.format("Cache status, size : %d, bmp count : %d", refCount, bmpCount));
    }

    /**
     * Save this image to cache
     *
     * NOTE:
     *  This method is called in the download thread!
     *
     * @param url       the url of the image
     * @param filePath  the path of the image
     * @param expire    the expire time
     * @return          > 0 for success, and the return is the id.  < 0 for error.
     */
    public long saveToFilesystemCache(String url, String filePath, long expire)
    {
        ImageURLPathPair pair = ImageURLPathPair.select(url, context);
        if (pair == null) {
            pair = new ImageURLPathPair();
        }
        pair.setPath(filePath);
        pair.setUrl(url);
        pair.setExpire(expire);

        long id = pair.save(context);
        return  id;
    }

    private Context context;
    private static final String TAG = ImageCacheManager.class.getSimpleName();

    /*
     * Save the soft references of the bitmaps.
     */
    private HashMap<String, SoftReference<Bitmap>> cache;

    private static ImageCacheManager mInstance = null;
}
