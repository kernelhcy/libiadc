package com.tsoftime;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Describe an image task.
 *
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:12
 */
public class ImageTask
{
    public ImageTask(String url, HashMap<String, Object> params, ImageTaskCallBack callBack
                    , TaskPriority priority, long expire)
    {
        this.mUrl = url;
        this.mPriority = priority;
        this.mExpire = expire;
        this.mCallBack = callBack;
        this.mParams = params;
    }

    public ImageTask(String url, HashMap<String, Object> params, ImageTaskCallBack callBack)
    {
        this(url, params, callBack, TaskPriority.DEFAULT_PRIORITY, Long.MAX_VALUE);
    }

    /**
     * Notify the downloading progress
     * @param total     the total length of the image
     * @param hasRead   the length of the part we have gotten
     */
    public void onDownloadingProgress(int total, int hasRead)
    {
        mCallBack.onGettingProgress(total, hasRead, mParams);
    }

    /**
     * Notify the image has been downloaded.
     * @param bmp       the image
     * @param status    the status
     */
    public void onDownloadingDone(int status, Bitmap bmp)
    {
        mCallBack.onDownloadingDone(status, bmp, mParams);
    }


    public String getUrl()
    {
        return mUrl;
    }

    public TaskPriority getPriority()
    {
        return mPriority;
    }

    public long getExpire()
    {
        return mExpire;
    }

    private String mUrl;
    private TaskPriority mPriority;
    private long mExpire;
    private HashMap<String, Object> mParams;
    private ImageTaskCallBack mCallBack;
}
