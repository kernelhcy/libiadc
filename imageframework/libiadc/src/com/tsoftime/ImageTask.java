package com.tsoftime;

import android.graphics.Bitmap;
import com.tsoftime.messeage.params.TaskPriority;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Describe an image task.
 *
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:12
 */
class ImageTask
{
    public ImageTask(String url, HashMap<String, Object> params, ImageTaskCallBack callBack
                    , TaskPriority priority, long expire, int maxSize)
    {
        this.mUrl = url;
        this.mCallBacks = new ArrayList<TaskParamsPair>();
        this.mCallBacks.add(new TaskParamsPair(callBack, params));
        this.mPriority = priority;
        this.mExpire = expire;
        this.mMaxSize = maxSize;
    }

    public ImageTask(String url, HashMap<String, Object> params, ImageTaskCallBack callBack, int maxSize)
    {
        this(url, params, callBack, TaskPriority.DEFAULT_PRIORITY, Long.MAX_VALUE, maxSize);
    }

    /**
     * Notify the downloading progress
     * @param total     the total length of the image
     * @param hasRead   the length of the part we have gotten
     */
    public void onDownloadingProgress(int total, int hasRead)
    {
        this.mTotal = total;
        this.mHasRead = hasRead;
        for (TaskParamsPair tpp : mCallBacks) {
            tpp.mCallBack.onGettingProgress(total, hasRead, tpp.mParams);
        }
    }

    /**
     * Notify the image has been downloaded.
     * @param bmp       the image
     * @param status    the status
     */
    public void onDownloadingDone(int status, Bitmap bmp)
    {
        for (TaskParamsPair tpp : mCallBacks) {
            tpp.mCallBack.onDownloadingDone(status, bmp, tpp.mParams);
        }
    }

    public void addCallBack(ImageTaskCallBack callBack, HashMap<String, Object> params)
    {
        this.mCallBacks.add(new TaskParamsPair(callBack, params));
    }

    public String getUrl()
    {
        return mUrl;
    }

    public void setUrl(String mUrl)
    {
        this.mUrl = mUrl;
    }

    public long getTotal()
    {
        return mTotal;
    }

    public void setTotal(long mTotal)
    {
        this.mTotal = mTotal;
    }

    public long getHasRead()
    {
        return mHasRead;
    }

    public void setHasRead(long hasRead)
    {
        this.mHasRead = mHasRead;
    }

    public TaskPriority getPriority()
    {
        return mPriority;
    }

    public void setPriority(TaskPriority priority)
    {
        this.mPriority = mPriority;
    }

    public long getExpire()
    {
        return mExpire;
    }

    public void setExpire(long expire)
    {
        this.mExpire = mExpire;
    }

    public int getMaxSize()
    {
        return mMaxSize;
    }

    public void setImageQuality(int maxSize)
    {
        this.mMaxSize = maxSize;
    }

    private ArrayList<TaskParamsPair> mCallBacks;
    private String mUrl;
    private long mTotal, mHasRead;
    private TaskPriority mPriority;
    private long mExpire;
    private int mMaxSize;

    private class TaskParamsPair
    {
        ImageTaskCallBack mCallBack;
        HashMap<String, Object> mParams;

        private TaskParamsPair(ImageTaskCallBack callBack, HashMap<String, Object> params)
        {
            this.mCallBack = callBack;
            this.mParams = params;
        }
    }
}
