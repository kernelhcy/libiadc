package com.tsoftime;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Describe an image task.
 *
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:12
 */
class ImageTask implements Comparable
{
    public ImageTask(String url, HashMap<String, Object> params, ImageTaskCallBack callBack, int priority)
    {
        this.url = url;
        this.params = params;
        this.callBack = callBack;
        this.priority = priority;
    }

    public ImageTask(String url, HashMap<String, Object> params, ImageTaskCallBack callBack)
    {
        this(url, params, callBack, 0);
    }

    /**
     * Notify the downloading progress
     * @param total     the total length of the image
     * @param hasRead   the length of the part we have gotten
     */
    public void onDownloadingProgress(long total, long hasRead)
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

    @Override
    public int compareTo(Object o)
    {
        ImageTask it = (ImageTask)o;
        if (priority > 0 && it.priority > 0) {
            return priority - it.priority;
        }

        return (int)(secondaryPriority - it.secondaryPriority);
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

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public void setSecondaryPriority(long secondaryPriority)
    {
        this.secondaryPriority = secondaryPriority;
    }

    private ImageTaskCallBack callBack;
    private String url;
    private HashMap<String, Object> params;
    private long total, hasRead;
    private int priority;               // the main priority, MUST > 0
    private long secondaryPriority;     // the second priority, if the main priority <= 0, use this as the priority.
}
