package com.tsoftime;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

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
        ImageCacheManager.init(context);
        mInstance = new ImageManager();
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
                                    , TaskPriority priority, long expireTime)
    {
        if (url == null) {
            callBack.onDownloadingDone(NO_SUCH_IMAGE, null, params);
            return;
        }

        // If we already have this task, just return
        if (newTasksQueues.findTask(url, priority) != null) return;
        for (ImageTask t : mRunningTasks) {
            if (t.getUrl().equals(url) && t.getPriority() == priority) {
                t.addCallback(callBack, params);
                return;
            }
        }

        // create a new task
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
        dispatchImageTask(url, params, callBack, TaskPriority.DEFAULT_PRIORITY, Long.MAX_VALUE);
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
                // only here, the thread can be set to RUNNING
                t.setStatus(ImageDownloadThread.RUNNING_STATUS);
                ImageTask task = newTasksQueues.dequeue();
                if(task == null) break;     // no more task.
                Message msg = t.getHandler().obtainMessage(ImageDownloadThreadHandler.DOWNLOAD_IMAGE);
                msg.obj = task;
                t.getHandler().sendMessage(msg);
                mRunningTasks.add(task);
            }
        }
    }

    /**
     * You SHOUlD NEVER create an instance by yourself.
     */
    private ImageManager()
    {
        this.newTasksQueues = new ImageTaskQueues();
        this.mRunningTasks = new ArrayList<ImageTask>();
        this.handler = new ImageManagerHandler(this);

        this.mDownloadThreadNumber = 1;
        this.threads = new HashMap<String, ImageDownloadThread>();

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
        this.mDownloadThreadNumber = number;
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
        for(int i = 0; i < mDownloadThreadNumber; ++i) {
            ImageDownloadThread t = new ImageDownloadThread(String.format("ImageDownloadThread-%d", i), handler);
            threads.put(t.getName(), t);
            Log.d(TAG, String.format("Create thread %s.", t.getName()));
            t.start();
            t.getLooper();  // wait the thread to start up
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
     *
     * @param task      the image task
     * @param total     the total length
     * @param hasRead   the length has read
     */
    void onDownloadingProgress(ImageTask task, int total, int hasRead)
    {
        if (task == null) return;
        task.onDownloadingProgress(total, hasRead);
    }

    /**
     * Called by the ImageManagerHandler when it received DOWNLOADED_DONE message.
     *
     * @param task      the image task
     * @param status    the status
     * @param bmp       the image
     */
    void onDownloadDone(ImageTask task, int status, Bitmap bmp)
    {
        if (task == null) return;
        task.onDownloadingDone(status, bmp);
        mRunningTasks.remove(task);
        runTasks();
    }


    private ImageTaskQueues newTasksQueues;                         // The new tasks queue.
    private ArrayList<ImageTask> mRunningTasks;                     // the running tasks

    private ImageManagerHandler handler;

    private int mDownloadThreadNumber;                               // the number of the download threads.
    private HashMap<String, ImageDownloadThread> threads;            // download threads

    private static ImageManager mInstance = null;

    private static final String TAG = ImageManager.class.getSimpleName();
}
