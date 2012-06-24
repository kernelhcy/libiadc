package com.tsoftime;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.tsoftime.messeage.params.DownloadingProgressParams;
import com.tsoftime.messeage.params.ErrorParams;
import com.tsoftime.messeage.params.ImageDownloadDoneParams;
import com.tsoftime.messeage.params.ThreadQuitedParams;

import java.util.Random;

/**
 * The image downloading thread handler
 * All the downloading work is in this class.
 *
 * <p>
 * The image download thread can receive the following types of messages:
 * <ol>
 *     <li>
 *         <b>DOWNLOAD_IMAGE</b>: Download an image. This message need two parameters, the parameters are stored in
 *         a bundle. The bundle is store in the message data. These two parameters are:
 *         <ul>
 *             <li>"url" : the url of the image to be downloaded.</li>
 *             <li>"save_file_path" : the path to which the image will be stored.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         <b>QUIT</b>: Tell the download thread to quit. When the download thread receive this message, it will
 *         download all the left images and than quit.
 *     </li>
 * </ol>
 * </p>
 *
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:40
 */
public class ImageDownloadThreadHandler extends Handler
{
    public static final int DOWNLOAD_IMAGE = 1;     // the message type of downloading an image
    public static final int QUIT = 2;               // the message type of quit

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
     *      "save_file_path"    => the path to which the image will be stored.
     *
     * @param msg the message sent by the image manager.
     */
    @Override
    public void handleMessage(Message msg)
    {
        Message resultMsg;
        switch (msg.what)
        {
            case DOWNLOAD_IMAGE:
                download(msg);
                break;
            case QUIT:
                resultMsg = imageManagerHandler.obtainMessage(ImageManagerHandler.THREAD_QUITED);
                resultMsg.obj = new ThreadQuitedParams(getLooper().getThread().getName());
                imageManagerHandler.sendMessage(resultMsg);
                getLooper().quit();
                return;
            default:
                resultMsg = imageManagerHandler.obtainMessage(ImageManagerHandler.ERROR);
                ErrorParams eparams = new ErrorParams();
                eparams.threadName = getLooper().getThread().getName();
                eparams.code = -1;
                eparams.desc = String.format("Unknown message type : %d", msg.what);
                resultMsg.obj = eparams;
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
            sendError(-1, "msg.getData() == null");
            return;
        }

        url = params.getString("url");
        String fildPath = params.getString("save_file_path");
        Log.d(TAG, String.format("%s downloads %s, store in %s", getLooper().getThread().getName(), url, fildPath));
        long total = 0, hasRead = 0;
        // download...

        Random r = new Random();
        total = r.nextInt(10000);
        hasRead = 0;
        while (hasRead < total)
        {
            try { Thread.sleep(300);} catch (InterruptedException e) {}
            hasRead += r.nextInt((int)total / 10 + 10);
            sendDownloadingProgress(total, hasRead);
        }

        sendDownloadDown(null);
    }

    /**
     * Send download done message to the image manager.
     * @param image the image.
     */
    private void sendDownloadDown(Bitmap image)
    {
        Message msg;
        msg = imageManagerHandler.obtainMessage(ImageManagerHandler.DOWNLOAD_DONE);
        ImageDownloadDoneParams params = new ImageDownloadDoneParams();
        params.threadName = getLooper().getThread().getName();
        params.url = url;
        params.image = image;
        msg.obj = params;
        imageManagerHandler.sendMessage(msg);
    }

    /**
     * Send downloading progress message to the image manager.
     * @param total     the total length
     * @param hasRead   has read length
     */
    private void sendDownloadingProgress(long total, long hasRead)
    {
        Message msg;
        msg = imageManagerHandler.obtainMessage(ImageManagerHandler.DOWNLOADING_PROGRESS);
        DownloadingProgressParams params = new DownloadingProgressParams();
        params.threadName = getLooper().getThread().getName();
        params.total = total;
        params.hasRead = hasRead;
        params.url = url;
        msg.obj = params;
        imageManagerHandler.sendMessage(msg);
    }

    /**
     * Send error message to the image manager.
     * @param code sendError code
     * @param desc sendError description
     */
    private void sendError(int code, String desc)
    {
        Message msg;
        msg = imageManagerHandler.obtainMessage(ImageManagerHandler.ERROR);
        ErrorParams params = new ErrorParams();
        params.threadName = getLooper().getThread().getName();
        params.code = code;
        params.desc = desc;
        msg.obj = params;
        imageManagerHandler.sendMessage(msg);
    }

    private String url;
    private ImageDownloadThread thread;
    private ImageManagerHandler imageManagerHandler;
    private static final String TAG = ImageDownloadThreadHandler.class.getSimpleName();
}
