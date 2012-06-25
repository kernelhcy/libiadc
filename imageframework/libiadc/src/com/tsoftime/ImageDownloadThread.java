package com.tsoftime;

import android.content.Context;
import android.os.Looper;

import android.os.Handler;
import android.util.Log;

/**
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:39
 */
public class ImageDownloadThread extends Thread
{
    public static final int IDLE_STATUS = 100;      // the thread is idle.
    public static final int RUNNING_STATUS = 99;    // the thread is downloading an image.
    public static final int QUITING_STATUS = 98;    // the thread is quiting.

    public ImageDownloadThread(ImageManagerHandler h, Context context)
    {
        this.imageManagerHandler = h;
        this.status = IDLE_STATUS;
        this.context = context;
    }

    @Override
    public void run()
    {
        Log.d(TAG, String.format("Start a download thread... %d", this.getId()));
        Looper.prepare();
        Log.d(TAG, String.format("Download thread %d start handle the message...", this.getId()));
        handler = new ImageDownloadThreadHandler(this, imageManagerHandler);
        Looper.loop();
        Log.d(TAG, String.format("Download tread %d quit.", this.getId()));
    }

    public Handler getHandler()
    {
        return handler;
    }

    /**
     * Get the status of the thread.
     * @return the status
     */
    public synchronized int getStatus()
    {
        return status;
    }

    /**
     * Set the status of the thread.
     * @param status new status.
     */
    public synchronized void setStatus(int status)
    {
        this.status = status;
    }

    public Context getContext()
    {
        return context;
    }

    private int status;             // the status of the download thread.
    private Context context;
    private ImageDownloadThreadHandler handler;
    private ImageManagerHandler imageManagerHandler;
    private static final String TAG = ImageDownloadThread.class.getSimpleName();
}
