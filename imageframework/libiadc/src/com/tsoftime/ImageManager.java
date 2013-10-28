package com.tsoftime;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.HashMap;
import java.util.concurrent.*;

/**
 * ImageManager
 *
 * Manage the images' downloading and caching.
 * The image manager will start some download mThreads to download images.
 * The default number of download mThreads is one.
 * You can set the number of download mThreads by yourself. More download mThreads, more memory is needed!
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
        if (Thread.currentThread() != context.getMainLooper().getThread()) {
            throw new RuntimeException("ImageMamager MUST be initialed in main thread.");
        }
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
     * @param expireTime    the expire time(second). The image will be cached until the expire time exceeds.
     *                      After the expire time exceeds, the image will be removed from the cache and get a new
     *                      copy from the remote server.
     * @return the id of the task, or -1 for error.
     */
    public int dispatchImageTask(String url, HashMap<String, Object> params, ImageTaskCallBack callBack
                                    , long expireTime)
    {
        if (url == null) {
            callBack.onDownloadingDone(NO_SUCH_IMAGE, null, params);
            return -1;
        }

        // create a new task
        Log.d(TAG, String.format("get image %s", url));
        ImageTask task = new ImageTask(url, params, callBack, mHandler, expireTime);
        mExecutor.execute(task);
        return task.getTaskId();
    }

    /**
     * Dispatch an image task using default priority and will never expire.
     *
     *
     * @param url       the url of the image. like : "http://www.google.com/images/1.png"
     * @param params    the parameters you want to receive in the ImageTaskCallBack callbacks.
     * @param callBack  the callback. Used to notify you the progress.
     * @return the id of the task, or -1 for error.
     */
    public int dispatchImageTask(String url, HashMap<String, Object> params, ImageTaskCallBack callBack)
    {
        return dispatchImageTask(url, params, callBack, Long.MAX_VALUE);
    }

    /**
     * The max size of the image.
     * @return
     */
    public int getImageMaxSize()
    {
        return mImageMaxSize;
    }

    public void setImageMaxSize(int size)
    {
        mImageMaxSize = size;
    }

    /**
     * 取消一个任务
     * @param taskId
     */
    public void cancelTask(int taskId)
    {
        ImageTask task = null;
        for (Runnable r : mTaskQueue) {
            if (r instanceof ImageTask) {
                task = (ImageTask) r;
                if (task.getTaskId() == taskId) {
                    break;
                } else {
                    task = null;
                }
            }
        }
        if (task != null) mTaskQueue.remove(task);
    }

    /**
     * Remove all the download tasks.
     *
     * The tasks which are running will run go on.
     */
    public void removeAllTasks()
    {
        mTaskQueue.clear();
    }

    /**
     * You SHOUlD NEVER create an instance by yourself.
     */
    private ImageManager()
    {
        this.mTaskQueue = new LinkedBlockingQueue<Runnable>();
        this.mExecutor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, this.mTaskQueue);

        this.mHandler = new Handler();
    }

    private int mImageMaxSize = 1024;

    private BlockingQueue<Runnable> mTaskQueue;             // THe task queue;
    private Handler mHandler;

    private static ImageManager mInstance = null;
    private Executor mExecutor;                                     // the thread executor

    private static final String TAG = ImageManager.class.getSimpleName();
}
