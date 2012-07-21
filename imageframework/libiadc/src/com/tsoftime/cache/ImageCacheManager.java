package com.tsoftime.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
 * The image cache manager.
 * User: huangcongyu2006
 * Date: 12-6-25 PM3:27
 */
public class ImageCacheManager
{
    public ImageCacheManager(Context ctx)
    {
        context = ctx;
    }

    /**
     * Get the image of url.
     * @param url   the url of the image
     * @return      the bitmap of the image or null for error or not found.
     */
    public Bitmap getImage(String url)
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
     * Save this image to cache
     * @param url       the url of the image
     * @param filePath  the path of the image
     * @param expire    the expire time
     * @return          > 0 for success, and the return is the id.  < 0 for error.
     */
    public long saveToCache(String url, String filePath, long expire)
    {
        ImageURLPathPair pair = ImageURLPathPair.select(url, context);
        if (pair == null) {
            pair = new ImageURLPathPair();
        }
        pair.setPath(filePath);
        pair.setUrl(url);
        pair.setExpire(expire);
        return pair.save(context);
    }

    private Context context;
    private static final String TAG = ImageCacheManager.class.getSimpleName();
}
