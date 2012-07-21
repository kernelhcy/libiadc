package com.tsoftime;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Describe an image task.
 *
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:12
 */
class ImageTask
{
    // the priority
    public enum TaskPriority{
        LOW_PRIORITY            // low priority
            {
                @Override
                public String toString()
                {
                    return "LowPriority";
                }
            },
        DEFAULT_PRIORITY        // default priority
            {
                @Override
                public String toString()
                {
                    return "DefaultPriority";
                }
            },
        HIGH_PRIORITY           // high priority
            {
                @Override
                public String toString()
                {
                    return "HighPriority";
                }
            };

        public abstract String toString();
    }

    public ImageTask(String url, HashMap<String, Object> params, ImageTaskCallBack callBack
                    , TaskPriority priority, long expire)
    {
        this.url = url;
        this.params = params;
        this.callBack = callBack;
        this.priority = priority;
        this.expire = expire;
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
        this.total = total;
        this.hasRead = hasRead;
        if (callBack != null) callBack.onGettingProgress(total, hasRead, params);
    }

    /**
     * Notify the image has been downloaded.
     * @param bmp       the image
     */
    public void onDownloadingDown(Bitmap bmp)
    {
        if (callBack != null) callBack.onDownloadingDone(1, bmp, params);
    }

    public ImageTaskCallBack getCallBack()
    {
        return callBack;
    }

    public void setCallBack(ImageTaskCallBack callBack)
    {
        this.callBack = callBack;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public HashMap<String, Object> getParams()
    {
        return params;
    }

    public void setParams(HashMap<String, Object> params)
    {
        this.params = params;
    }

    public long getTotal()
    {
        return total;
    }

    public void setTotal(long total)
    {
        this.total = total;
    }

    public long getHasRead()
    {
        return hasRead;
    }

    public void setHasRead(long hasRead)
    {
        this.hasRead = hasRead;
    }

    public TaskPriority getPriority()
    {
        return priority;
    }

    public void setPriority(TaskPriority priority)
    {
        this.priority = priority;
    }

    public long getExpire()
    {
        return expire;
    }

    public void setExpire(long expire)
    {
        this.expire = expire;
    }

    private ImageTaskCallBack callBack;
    private String url;
    private HashMap<String, Object> params;
    private long total, hasRead;
    private TaskPriority priority;
    private long expire;
}
