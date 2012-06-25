package com.tsoftime.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

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
     * @return      the bitmap of the image or null for error and not found.
     */
    public Bitmap getImage(String url)
    {
        ImageURLPathPair pair = ImageURLPathPair.select(url, context);
        if (pair == null) return null;
        Log.d(TAG, String.format("%s: %s", pair.getUrl(), pair.getPath()));
        Bitmap bmp = BitmapFactory.decodeFile(pair.getPath());
        return bmp;
    }

    private Context context;
    private static final String TAG = ImageCacheManager.class.getSimpleName();
}
