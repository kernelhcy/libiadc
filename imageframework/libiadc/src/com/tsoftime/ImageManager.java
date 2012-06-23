package com.tsoftime;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;

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
     * Get an image.
     * @param url       the url of the image. like : "http://www.google.com/images/1.png"
     * @param params    the parameters you want to receive in the ImageTaskCallBack callbacks.
     * @param callBack  the callback. Used to notify you the progress.
     * @param priority  the priority. The bigger, the higher priority to downloading.
     */
    public void getImage(String url, HashMap<String, Object> params, ImageTaskCallBack callBack, int priority)
    {
        ImageTask task = new ImageTask(url, params, callBack, priority);
        taskQueue.enqueue(task);
    }

    /**
     * Get an image Use default priority.
     *
     * @param url       the url of the image. like : "http://www.google.com/images/1.png"
     * @param params    the parameters you want to receive in the ImageTaskCallBack callbacks.
     * @param callBack  the callback. Used to notify you the progress.
     */
    public void getImage(String url, HashMap<String, Object> params, ImageTaskCallBack callBack)
    {
        getImage(url, params, callBack, 0);
    }


    /**
     * You SHOUlD NEVER create an instance by yourself.
     * @param context
     */
    public ImageManager(Context context)
    {
        this.context = context;
        this.taskQueue = new ImageTaskQueue(10);
        this.handler = new ImageManagerHandler(this);
    }

    ImageTaskQueue getTaskQueue()
    {
        return taskQueue;
    }

    private Context context;
    private ImageTaskQueue taskQueue;
    private ImageManagerHandler handler;

    private static ImageManager mInstance = null;

    private static final String TAG = ImageManager.class.getSimpleName();
}
