package com.tsoftime;

import android.content.Context;
import android.util.Log;

/**
 * ImageManager
 *
 * Manage the images' downloading and caching.
 *
 * User: huangcongyu2006
 * Date: 12-6-23 AM9:40
 */
public class ImageManager
{
    /**
     * Initial the ImageManger.
     * You MUST call this function before any operation on the ImageManager.
     * We suggest you to call this function at the initialization stage of your app.
     * @param context
     */
    public static void init(Context context)
    {
        mInstance = new ImageManager(context);
    }

    /**
     * Get the ONLY instance of the ImageManager.
     * @return
     */
    public static ImageManager instance()
    {
        return mInstance;
    }

    /**
     * You SHOUlD NEVER create an instance by yourself.
     * @param context
     */
    public ImageManager(Context context)
    {
        this.context = context;
    }

    private Context context;

    private static ImageManager mInstance = null;

    private static final String TAG = ImageManager.class.getSimpleName();
}
