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
public class ImageManagerHandler extends Handler
{
    public static final int DOWNLOAD_DONE = 1;          // The image has been downloaded.
    public static final int DOWNLOADING_PROGRESS = 2;   // The image is being downloaded. Notify the progress.
    public static final int NO_SUCH_IMAGE = -2;         // No such image found.
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
                ImageDownloadDoneParams imageDownlodDoneParams = (ImageDownloadDoneParams) msg.obj;
                Log.d(TAG, String.format("Receive DOWNLOAD_DONE message. %s",imageDownlodDoneParams.threadName));
                imageManager.onDownloadDonw(ImageManager.SUCCESS,
                                            imageDownlodDoneParams.url, imageDownlodDoneParams.image);
                imageManager.setThreadStatus(imageDownlodDoneParams.threadName, ImageDownloadThread.IDLE_STATUS);
                break;
            case DOWNLOADING_PROGRESS:
                DownloadingProgressParams downloadingProgressParams = (DownloadingProgressParams) msg.obj;
//                Log.d(TAG, String.format("Receive DOWNLOADING_PROGRESS message. %s"
//                                , downloadingProgressParams.threadName));
                imageManager.onDownloadingProgress(downloadingProgressParams.url
                                                    , downloadingProgressParams.total
                                                    , downloadingProgressParams.hasRead);
                break;
            case NO_SUCH_IMAGE:
                NoSuchImageParams noSuchImageParams = (NoSuchImageParams) msg.obj;
                Log.d(TAG, String.format("Receive NO_SUCH_IMAGE message. %s", noSuchImageParams.threadName));
                imageManager.onDownloadDonw(ImageManager.NO_SUCH_IMAGE, null, null);
                imageManager.setThreadStatus(noSuchImageParams.threadName, ImageDownloadThread.IDLE_STATUS);
                break;
            case ERROR:
                ErrorParams errorParams = (ErrorParams) msg.obj;
                Log.d(TAG, String.format("Receive ERROR message. %s %s", errorParams.threadName, errorParams.desc));
                imageManager.onDownloadDonw(ImageManager.ERROR, errorParams.desc, null);
                imageManager.setThreadStatus(errorParams.threadName, ImageDownloadThread.IDLE_STATUS);
                break;
            case THREAD_QUITED:
                ThreadQuitedParams threadQuitedParams = (ThreadQuitedParams) msg.obj;
                imageManager.removeThread(threadQuitedParams.threadName);
                Log.d(TAG, String.format("Receive THREAD_QUITED message. %s", threadQuitedParams.threadName));
                break;
            default:
                Log.e(TAG, String.format("Unknown message type %d.", msg.what));
                break;
        }

        // Maybe some download thread is idle, try to run tasks on these idle threads if there are more tasks.
        imageManager.runTasks();
    }


    private static final String TAG = ImageManagerHandler.class.getSimpleName();
    private ImageManager imageManager;
}
