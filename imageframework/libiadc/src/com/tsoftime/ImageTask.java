package com.tsoftime;

import android.graphics.Bitmap;

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
        this.callBacks = new ArrayList<TaskParamsPair>();
        this.callBacks.add(new TaskParamsPair(callBack, params));
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
        for (TaskParamsPair tpp : callBacks) {
            tpp.callBack.onGettingProgress(total, hasRead, tpp.params);
        }
    }

    /**
     * Notify the image has been downloaded.
     * @param bmp       the image
     * @param status    the status
     */
    public void onDownloadingDone(int status, Bitmap bmp)
    {
        for (TaskParamsPair tpp : callBacks) {
            tpp.callBack.onDownloadingDone(status, bmp, tpp.params);
        }
    }

    public void addCallBack(ImageTaskCallBack callBack, HashMap<String, Object> params)
    {
        this.callBacks.add(new TaskParamsPair(callBack, params));
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
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

    private ArrayList<TaskParamsPair> callBacks;
    private String url;
    private long total, hasRead;
    private TaskPriority priority;
    private long expire;

    private class TaskParamsPair
    {
        ImageTaskCallBack callBack;
        HashMap<String, Object> params;

        private TaskParamsPair(ImageTaskCallBack callBack, HashMap<String, Object> params)
        {
            this.callBack = callBack;
            this.params = params;
        }
    }
}
