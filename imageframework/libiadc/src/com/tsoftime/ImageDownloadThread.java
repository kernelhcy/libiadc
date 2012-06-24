package com.tsoftime;

import android.os.Looper;

import android.os.Handler;
import android.util.Log;

/**
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:39
 */
public class ImageDownloadThread extends Thread
{
    public ImageDownloadThread(ImageManagerHandler h)
    {
        this.imageManagerHandler = h;
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

    private ImageDownloadThreadHandler handler;
    private ImageManagerHandler imageManagerHandler;
    private static final String TAG = ImageDownloadThread.class.getSimpleName();
}
