package com.tsoftime.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.tsoftime.ImageMangerConfig;

import java.io.File;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        filePathCache = new HashMap<String, String>();
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
        String filePath = filePathCache.get(url);
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
            filePathCache.put(url, filePath);
            return filePath;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the image from the file system cache.
     *
     * NOTE:
     *  This method is called in the download thread!
     *
     * @param url   the url of the image
     * @param expire the expire time of this image
     * @return      the bitmap of the image or null for error or not found.
     */
    public Bitmap getImageFromFileSystemCache(String url, long expire)
    {
        String filePath = getImageFilePath(url);
        if (filePath == null) return null;

        File imageFile = new File(filePath);
        if (!imageFile.exists()) return null;

        // test whether the image cache expire time exceeded
        Date now = Calendar.getInstance().getTime();
        long timeHasGone = (now.getTime() - imageFile.lastModified()) / 1000;
        // the expire time has exceeded, return null to reload download the image.
        if (timeHasGone > expire) {
            Log.d(TAG, String.format("image %s expire time exceed : %d", url, timeHasGone));
            // remove the old cach image.
            if (imageFile.exists()) imageFile.delete();
            cache.remove(url);
            return null;
        }

        Log.d(TAG, String.format("%s: %s", url, filePath));
        Bitmap bmp = BitmapFactory.decodeFile(filePath);

        if (bmp == null){
            Log.d(TAG, String.format("Image decode failed : %s", filePath));
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
    }

    private Context context;
    private static final String TAG = ImageCacheManager.class.getSimpleName();

    /*
     * Save the soft references of the bitmaps.
     */
    private HashMap<String, SoftReference<Bitmap>> cache;
    private HashMap<String, String> filePathCache;
    private static ImageCacheManager mInstance = null;
}
