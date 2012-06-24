package com.tsoftime;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
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
        Log.d(TAG, String.format("get image %s %d", url, priority));
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

        this.downloadThreadNumber = 3;
        this.threads = new ArrayList<ImageDownloadThread>();
        initDownloadTreads();
    }

    /**
     * Set the download thread number.
     *
     * This function will kill all the old threads and create `number` new threads.
     * The old threads will finish all the downloading tasks which are being executed.
     *
     * @param number
     */
    public void setDownloadThreadNumber(int number)
    {
        this.downloadThreadNumber = number;
        initDownloadTreads();
    }

    /**
     * Initial the download threads
     */
    private void initDownloadTreads()
    {
        // quit the old threads.
        for(ImageDownloadThread t : threads) {
            t.getHandler().sendEmptyMessage(ImageDownloadThreadHandler.QUIT);
        }
        threads.clear();

        // create new download threads
        for(int i = 0; i < downloadThreadNumber; ++i) {
            ImageDownloadThread t = new ImageDownloadThread(handler);
            t.setName(String.format("ImageDownloadThread-%d", i));
            threads.add(t);
            t.start();
        }
    }

    /**
     * Get the task queue
     * @return
     */
    ImageTaskQueue getTaskQueue()
    {
        return taskQueue;
    }

    private Context context;
    private ImageTaskQueue taskQueue;
    private ImageManagerHandler handler;

    private int downloadThreadNumber;                    // 下载线程的数量
    private ArrayList<ImageDownloadThread> threads;     // 下载线程

    private static ImageManager mInstance = null;

    private static final String TAG = ImageManager.class.getSimpleName();
}
