package com.tsoftime;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.tsoftime.messeage.params.*;

/**
 * Handle the messages sent by the download threads.
 *
 * <p>
 *     The image manager is running in the main loop.<br/>
 *     The image manager can handle the following types of messages:
 *     <ol>
 *         <li>
 *             <b>DOWNLOAD_DONE</b>: When a download thread has downloaded an image, it will send this message to the
 *             image manager. <br/>
 *             The parameters object of this message is ImageDownloadedDoneParams.
 *         </li>
 *         <li>
 *             <b>DOWNLOADING_PROGRESS</b>: This message is used to notify the image manager the downloading
 *             progress of the image.<br/>
 *             The parameters object of this message is DownloadingProgressParams.
 *         </li>
 *         <li>
 *             <b>NO_SUCH_IMAGE</b>: If the download thread can not find the image to be downloaded, it will
 *             send this message to the image manager.<br/>
 *             The parameters object of this message is NoSuchImageParams.
 *         </li>
 *         <li>
 *             <b>ERROR</b>: If an error occurs, the download thread will send this message to the image manager.<br/>
 *             The parameters object of this message is ErrorParams.
 *         </li>
 *         <li>
 *             <b>THREAD_QUITED</b>: If a download thread is ready to quit, it will send this message to notify the
 *             image manager. When the image manager receive this message, it will remove all the data of this thread.
 *             After sending this message, the download must have quited.
 *         </li>
 *     </ol>
 *     The parameters object of these message is stored in the message's obj member.
 * </p>
 *
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:35
 */
class ImageManagerHandler extends Handler
{
    public static final int DOWNLOAD_DONE = 1;          // The image has been downloaded.
    public static final int DOWNLOADING_PROGRESS = 2;   // The image is being downloaded. Notify the progress.
    public static final int ERROR = -1;                 // error occurs.
    public static final int THREAD_QUITED = 3;          // The download thread has quited.

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
            {
                DownloadDoneParams params = (DownloadDoneParams) msg.obj;
                imageManager.setThreadStatus(params.threadName, ImageDownloadThread.IDLE_STATUS);
                Log.d(TAG, String.format("Receive DOWNLOAD_DONE message. %s. %s", params.threadName
                                                                                , params.task.getUrl()));
                imageManager.onDownloadDone(params.task
                                            , ImageManager.SUCCESS
                                            , params.bmp);
                imageManager.runTasks();
                break;
            }
            case DOWNLOADING_PROGRESS:
            {
                DownloadingProgressParams params = (DownloadingProgressParams) msg.obj;
                imageManager.onDownloadingProgress(params.task
                                                    , params.total
                                                    , params.hasRead);
                break;
            }
            case ERROR:
            {
                ErrorParams params = (ErrorParams) msg.obj;
                Log.d(TAG, String.format("Receive ERROR message. %s. %s. %s", params.threadName
                                                                            , params.desc, params.task.getUrl()));
                imageManager.onDownloadDone(params.task, ImageManager.ERROR, null);
                imageManager.setThreadStatus(params.threadName, ImageDownloadThread.IDLE_STATUS);
                imageManager.runTasks();
                break;
            }
            case THREAD_QUITED:
            {
                ThreadQuitedParams params = (ThreadQuitedParams) msg.obj;
                imageManager.removeThread(params.threadName);
                Log.d(TAG, String.format("Receive THREAD_QUITED message. %s", params.threadName));
                break;
            }
            default:
                Log.e(TAG, String.format("Unknown message type %d.", msg.what));
                imageManager.runTasks();
                break;
        }
    }
    private static final String TAG = ImageManagerHandler.class.getSimpleName();
    private ImageManager imageManager;
}
