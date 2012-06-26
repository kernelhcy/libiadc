package com.tsoftime;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import com.tsoftime.cache.ImageCacheManager;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
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
     * Get an image.
     *
     * NOTE:
     *  If you call getImage with the priority parameter, you MUST always call it with priority parameter.
     *  If you call getImage without the priority parameter, you MUST always call it without priority parameter.
     *  If you call these two functions in your app, you will get a puzzle result...
     *
     * @param url       the url of the image. like : "http://www.google.com/images/1.png"
     * @param params    the parameters you want to receive in the ImageTaskCallBack callbacks.
     * @param callBack  the callback. Used to notify you the progress.
     * @param priority  the priority. The bigger, the higher priority to downloading. MUST > 0!
     */
    public void getImage(String url, HashMap<String, Object> params, ImageTaskCallBack callBack, int priority)
    {
        Log.d(TAG, String.format("get image %s %d", url, priority));
        ImageTask task = new ImageTask(url, params, callBack, priority);
        newTasksQueue.enqueue(task);
        runTasks();
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
     * Find a idle thread and run an image task on it.
     * If no thread is idle, just do nothing.
     * When some download thread is idle, it will send some message to the image manager. When the image manager
     * receive the message, it will call this function to run task again.
     */
    void runTasks()
    {
        for(ImageDownloadThread t : threads.values())
        {
            if (newTasksQueue.size() <= 0) break;
            if (t.getStatus() == ImageDownloadThread.IDLE_STATUS) {
                t.setStatus(ImageDownloadThread.RUNNING_STATUS);
                ImageTask task = newTasksQueue.dequeue();
                if(task == null) break;     // no more task.
                Message msg = t.getHandler().obtainMessage(ImageDownloadThreadHandler.DOWNLOAD_IMAGE);
                Bundle bundle = new Bundle();
                bundle.putString("url", task.getUrl());
                bundle.putString("save_file_path", getImageStorePath());
                msg.setData(bundle);
                t.getHandler().sendMessage(msg);
                runningTasksMap.put(task.getUrl(), task);
            }
        }
    }

    /**
     * Get the path to which the image is stored.
     * @return
     */
    private String getImageStorePath()
    {
        StringBuilder sb = new StringBuilder();
        File externalCacheDir = context.getExternalCacheDir();
        sb.append(externalCacheDir.getAbsolutePath());              // external cache dir
        sb.append(config.getImageStoreDir());                       // image cache dir
        Date now = Calendar.getInstance().getTime();
        sb.append("/").append(now.getYear());
        sb.append("/").append(now.getMonth());
        sb.append("/").append(now.getDay());
        sb.append("/").append(now.getHours());
        sb.append("/").append(now.getMinutes());
        sb.append("/image_").append(System.currentTimeMillis());     // image name with ext
        return sb.toString();
    }

    /**
     * You SHOUlD NEVER create an instance by yourself.
     * @param context
     */
    private ImageManager(Context context)
    {
        this.context = context;
        this.newTasksQueue = new ImageTaskQueue(10);
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
     * Get the task queue
     * @return
     */
    ImageTaskQueue getNewTasksQueue()
    {
        return newTasksQueue;
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
    }

    Context getContext()
    {
        return context;
    }

    private Context context;
    private ImageTaskQueue newTasksQueue;                           // The new tasks queue.
    private HashMap<String, ImageTask> runningTasksMap;             // The running tasks map.

    private ImageManagerHandler handler;

    private int downloadThreadNumber;                               // the number of the download threads.
    private HashMap<String, ImageDownloadThread> threads;           // download threads

    private static ImageManager mInstance = null;

    private ImageMangerConfig config;

    private static final String TAG = ImageManager.class.getSimpleName();
}
