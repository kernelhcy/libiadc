package com.tsoftime;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;

import android.os.Handler;
import android.util.Log;

/**
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:39
 */
class ImageDownloadThread extends HandlerThread
{
    public static final int IDLE_STATUS = 100;      // the thread is idle.
    public static final int RUNNING_STATUS = 99;    // the thread is downloading an image.
    public static final int QUITING_STATUS = 98;    // the thread is quiting.

    public ImageDownloadThread(ImageManagerHandler h)
    {
        super("ImageDownloadThread");
        this.imageManagerHandler = h;
        this.status = IDLE_STATUS;
    }

    @Override
    protected void onLooperPrepared()
    {
        Log.d(TAG, String.format("Start a download thread... %d", this.getId()));
        Log.d(TAG, String.format("Download thread %d start handle the message...", this.getId()));
        handler = new ImageDownloadThreadHandler(this, imageManagerHandler);
    }

    public Handler getHandler()
    {
        return handler;
    }

    /**
     * Get the status of the thread.
     * @return the status
     */
    public int getStatus()
    {
        return status;
    }

    /**
     * Set the status of the thread.
     * @param status new status.
     */
    public void setStatus(int status)
    {
        this.status = status;
    }

    private int status;             // the status of the download thread.
    private ImageDownloadThreadHandler handler;
    private ImageManagerHandler imageManagerHandler;
    private static final String TAG = ImageDownloadThread.class.getSimpleName();
}
