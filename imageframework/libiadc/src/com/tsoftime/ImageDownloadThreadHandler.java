package com.tsoftime;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * The image downloading thread handler
 * All the downloading work is in this class.
 *
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:40
 */
public class ImageDownloadThreadHandler extends Handler
{
    public static final int DOWNLOAD_IMAGE = 1;
    public static final int QUIT = 2;

    public ImageDownloadThreadHandler(ImageDownloadThread thread, ImageManagerHandler handler)
    {
        this.thread = thread;
        this.imageManagerHandler = handler;
    }

    /**
     * Handler the message.
     *
     * The image manager send a DOWNLOAD_IMAGE message to the download thread to download a image.
     * The parameters are stored in the message's data member. Use msg.getDate() to get the parameters bundle.
     * The parameters contain:
     *      "url"               => the url of the image.
     *      "save_file_path"    => the path which the image will be stored to
     *
     * @param msg the message sent by the image manager.
     */
    @Override
    public void handleMessage(Message msg)
    {
        switch (msg.what)
        {
            case DOWNLOAD_IMAGE:
                download(msg);
                break;
            case QUIT:
                getLooper().quit();
                return;
            default:
                Message resultMsg = imageManagerHandler.obtainMessage(ImageManagerHandler.ERROR);
                resultMsg.obj = String.format("Unknown message type : %d", msg.what);
                imageManagerHandler.sendMessage(resultMsg);
                break;
        }
    }

    /**
     * Download the image...
     * @param msg
     */
    private void download(Message msg)
    {
        Message resultMsg;
        Bundle params = msg.getData();
        if (params == null) {
            resultMsg = imageManagerHandler.obtainMessage(ImageManagerHandler.ERROR);
            resultMsg.obj = "msg.getData() == null";
            imageManagerHandler.sendMessage(resultMsg);
            return;
        }

        String url = params.getString("url");
        String fildPath = params.getString("save_file_path");
        long total = 0, hasRead = 0;
        // download...


        resultMsg = imageManagerHandler.obtainMessage(ImageManagerHandler.DOWNLOADING_PROGRESS);
        imageManagerHandler.sendMessage(resultMsg);

        resultMsg = imageManagerHandler.obtainMessage(ImageManagerHandler.DOWNLOAD_DONE);
        imageManagerHandler.sendMessage(resultMsg);
    }
    private ImageDownloadThread thread;
    private ImageManagerHandler imageManagerHandler;
}
