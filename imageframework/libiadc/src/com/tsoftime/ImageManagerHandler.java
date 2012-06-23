package com.tsoftime;

import android.os.Handler;
import android.os.Message;

/**
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:35
 */
public class ImageManagerHandler extends Handler
{
    public static final int DOWNLOAD_DONE = 1;          // The image has been downloaded.
    public static final int DOWNLOADING_PROGRESS = 2;   // The image is being downloaded. Notify the progress.
    public static final int NO_SUCH_IMAGE = -2;         // No such image found.
    public static final int ERROR = -1;                 // error occurs.

    public ImageManagerHandler(ImageManager imageManager)
    {
        this.imageManager = imageManager;
    }

    @Override
    public void handleMessage (Message msg)
    {
        switch (msg.what)
        {
            case DOWNLOAD_DONE:
                break;
            case DOWNLOADING_PROGRESS:
                break;
            case NO_SUCH_IMAGE:
                break;
            case ERROR:
                break;
            default:
                break;
        }
    }



    private ImageManager imageManager;
}
