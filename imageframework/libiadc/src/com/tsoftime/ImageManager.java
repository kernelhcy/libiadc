package com.tsoftime;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import com.tsoftime.cache.ImageCacheManager;

import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * ImageManager
 *
 * Manage the images' downloading and caching.
 * The image manager will start some download threads to download images.
 * The default number of download threads is one.
 * You can set the number of download threads by yourself. More download threads, more memory is needed!
 *
 * User: huangcongyu2006
 * Date: 12-6-23 AM9:40
 */
public class ImageManager
{
    // the status code
    public static final int SUCCESS = 1;            // the image has been successfully downloaded
    public static final int ERROR = -1;             // an error occurs.
    public static final int NO_SUCH_IMAGE = -2;     // Can not find the image.

    /**
     * Initial the ImageManger.
     * You MUST call this function before any operation on the ImageManager.
     * We suggest you to call this function at the initialization stage of your app.
     * @param context
     */
    public static void init(Context context)
    {
        mInstance = new ImageManager(context);
        ImageCacheManager.init(context);
    }

    /**
     * Get the ONLY instance of the ImageManager.
     * @return the only instance of the image manager
     */
    public static ImageManager instance()
    {
        return mInstance;
    }

    /**
     * Dispatch an image task.
     *
     * The task will be appended to the end of the downloading queue.
     *
     * @param url           the url of the image. like : "http://www.google.com/images/1.png"
     * @param params        the parameters you want to receive in the ImageTaskCallBack callbacks.
     * @param callBack      the callback. Used to notify you the progress.
     * @param priority      the task priority.
     * @param expireTime    the expire time(second). The image will be cached until the expire time exceeds.
     *                      After the expire time exceeds, the image will be removed from the cache and get a new
     *                      copy from the remote server.
     */
    public void dispatchImageTask(String url, HashMap<String, Object> params, ImageTaskCallBack callBack
        , ImageTask.TaskPriority priority, long expireTime)
    {
        if (url == null) {
            callBack.onDownloadingDone(NO_SUCH_IMAGE, null, params);
            return;
        }

        // try to get the image from the cache
        ImageCacheManager cacheManager = ImageCacheManager.getInstance();
        Bitmap bmp = cacheManager.getImageFromCache(url);
        if (bmp != null) {
            Log.d(TAG, String.format("Cached! %s", url));
            callBack.onGettingProgress(bmp.getWidth() * bmp.getHeight(), bmp.getWidth() * bmp.getHeight(), params);
            callBack.onDownloadingDone(SUCCESS, bmp, params);
            return;
        }

        // try to get the image from the network
        Log.d(TAG, String.format("get image %s %s", url, priority.toString()));
        ImageTask task = new ImageTask(url, params, callBack, priority, expireTime);
        newTasksQueues.enqueue(task);

        // run the tasks.
        runTasks();
    }

    /**
     * Dispatch an image task using default priority and will never expire.
     *
     * @param url       the url of the image. like : "http://www.google.com/images/1.png"
     * @param params    the parameters you want to receive in the ImageTaskCallBack callbacks.
     * @param callBack  the callback. Used to notify you the progress.
     */
    public void dispatchImageTask(String url, HashMap<String, Object> params, ImageTaskCallBack callBack)
    {
        dispatchImageTask(url, params, callBack, ImageTask.TaskPriority.DEFAULT_PRIORITY, Long.MAX_VALUE);
    }

    /**
     * Remove all the download tasks.
     *
     * The tasks which are running will run go on.
     */
    public void removeAllTasks()
    {
        newTasksQueues.clear();
    }

    /*
     * *************************************
     *  Above is ALL the public interfaces.
     * *************************************
     */

    /**
     * Find a idle thread and run an image task on it.
     * If no thread is idle, just do nothing.
     * When some download thread is idle, it will send some message to the image manager. When the image manager
     * receive the message, it will call this function to run task again.
     */
    void runTasks()
    {
        for(ImageDownloadThread t : threads.values())
        {
            if (newTasksQueues.size() <= 0) break;
            if (t.getStatus() == ImageDownloadThread.IDLE_STATUS) {
                t.setStatus(ImageDownloadThread.RUNNING_STATUS);
                ImageTask task = newTasksQueues.dequeue();
                if(task == null) break;     // no more task.
                Message msg = t.getHandler().obtainMessage(ImageDownloadThreadHandler.DOWNLOAD_IMAGE);
                Bundle bundle = new Bundle();
                bundle.putString("url", task.getUrl());
                bundle.putLong("expire", task.getExpire());
                msg.setData(bundle);
                t.getHandler().sendMessage(msg);
                runningTasksMap.put(task.getUrl(), task);
            }
        }
    }

    /**
     * You SHOUlD NEVER create an instance by yourself.
     * @param context
     */
    private ImageManager(Context context)
    {
        this.context = context;
        this.newTasksQueues = new ImageTaskQueues();
        this.runningTasksMap = new HashMap<String, ImageTask>();
        this.handler = new ImageManagerHandler(this);

        this.downloadThreadNumber = 1;
        this.threads = new HashMap<String, ImageDownloadThread>();

        this.config = ImageMangerConfig.instance();

        initDownloadTreads();
    }

    /**
     * Set the download thread number.
     *
     * This function will kill all the old threads and create `number` new threads.
     * The old threads will finish all the downloading tasks which are being executed.
     *
     * <b>NOTE:</b>
     *  If you start more download threads, the images will be downloaded faster. And more memory will be used.
     *  If the images are too large, when the download threads have downloaded them, they will eat many memory
     *  after being converted to bitmap.
     *  So, you should have a deep look at how many download threads you need.
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
        for(ImageDownloadThread t : threads.values()) {
            if (t.getHandler() == null) continue;
            t.getHandler().sendEmptyMessage(ImageDownloadThreadHandler.QUIT);
        }
        threads.clear();

        // create new download threads
        for(int i = 0; i < downloadThreadNumber; ++i) {
            ImageDownloadThread t = new ImageDownloadThread(handler, context);
            t.setName(String.format("ImageDownloadThread-%d-%d", System.currentTimeMillis(), i));
            PriorityQueue<ImageTask> queue = new PriorityQueue<ImageTask>();
            threads.put(t.getName(), t);
            t.start();
        }
    }

    /**
     * Remove a thread.
     * This thread MUST has quited.
     *
     * @param threadName the name of the thread.
     */
    void removeThread(String threadName)
    {
        ImageDownloadThread t = threads.remove(threadName);
    }

    /**
     * Set the status of thread `threadName`
     * @param threadName
     * @param newStatus
     */
    void setThreadStatus(String threadName, int newStatus)
    {
        ImageDownloadThread t = threads.get(threadName);
        if (t != null) {
            t.setStatus(newStatus);
        }
    }

    /**
     * Called by the ImageMangerHandler when it receives DOWNLOADING_PROGRESS message.
     * @param url       the url of the image, used to find the image task.
     * @param total     the total length
     * @param hasRead   the length has read
     */
    void onDownloadingProgress(String url, int total, int hasRead)
    {
        ImageTask task = runningTasksMap.get(url);
        if (task == null) return;
        task.getCallBack().onGettingProgress(total, hasRead, task.getParams());
    }

    /**
     * Called by the ImageManagerHandler when it received DOWNLOADED_DONE message.
     * @param status    the status
     * @param url       the url of the image, used to find the image task. When an error occurs, this parameter is
     *                  the description of this error.
     */
    void onDownloadDone(int status, String url, Bitmap bmp)
    {
        ImageTask task = runningTasksMap.get(url);
        if (task == null) return;
        task.getCallBack().onDownloadingDone(status, bmp, task.getParams());

        if (status == SUCCESS && bmp != null) {
            // save the cache
            Log.d(TAG, String.format("Cache bitmap, %s", url));
            ImageCacheManager cacheManager = ImageCacheManager.getInstance();
            cacheManager.saveToCache(url, bmp);
        }
    }

    Context getContext()
    {
        return context;
    }

    private Context context;
    private ImageTaskQueues newTasksQueues;                         // The new tasks queue.
    private HashMap<String, ImageTask> runningTasksMap;             // The running tasks map.

    private ImageManagerHandler handler;

    private int downloadThreadNumber;                               // the number of the download threads.
    private HashMap<String, ImageDownloadThread> threads;           // download threads

    private static ImageManager mInstance = null;

    private ImageMangerConfig config;

    private static final String TAG = ImageManager.class.getSimpleName();
}
