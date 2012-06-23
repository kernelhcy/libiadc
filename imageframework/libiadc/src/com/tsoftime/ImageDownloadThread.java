package com.tsoftime;

import android.os.Looper;

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
        Looper.prepare();
        handler = new ImageDownloadThreadHandler(this, imageManagerHandler);
        Looper.loop();
    }

    private ImageDownloadThreadHandler handler;
    private ImageManagerHandler imageManagerHandler;
}
