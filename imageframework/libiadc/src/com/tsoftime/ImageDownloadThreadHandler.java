package com.tsoftime;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.tsoftime.cache.ImageCacheManager;
import com.tsoftime.messeage.params.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The image downloading mThread handler
 * All the downloading work is in this class.
 *
 * <p>
 * The image download mThread can receive the following types of messages:
 * <ol>
 *     <li>
 *         <b>DOWNLOAD_IMAGE</b>: Download an image. This message need two parameters, the parameters are stored in
 *         a bundle. The bundle is store in the message data. These two parameters are:
 *         <ul>
 *             <li>"urlStr" : the urlStr of the image to be downloaded.</li>
 *             <li>"save_file_path" : the path to which the image will be stored.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         <b>QUIT</b>: Tell the download mThread to quit. When the download mThread receive this message, it will
 *         download all the left images and than quit.
 *     </li>
 * </ol>
 * </p>
 *
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:40
 */
class ImageDownloadThreadHandler extends Handler
{
    public static final int DOWNLOAD_IMAGE = 1;     // the message type of downloading an image
    public static final int QUIT = 2;               // the message type of quit

    public ImageDownloadThreadHandler(ImageDownloadThread thread, ImageManagerHandler handler)
    {
        this.mThread = thread;
        this.mImageManagerHandler = handler;
        this.mImageCacheManager = ImageCacheManager.getInstance();
    }

    /**
     * Handler the message.
     *
     * The image manager send a DOWNLOAD_IMAGE message to the download mThread to download a image.
     * The parameters are stored in the message's data member. Use msg.getDate() to get the parameters bundle.
     * The parameters contain:
     *      "urlStr"            => the urlStr of the image.
     *      "save_file_path"    => the path to which the image will be stored.
     *
     * @param msg the message sent by the image manager.
     */
    @Override
    public void handleMessage(Message msg)
    {
        Message resultMsg;
        mThread.setStatus(ImageDownloadThread.RUNNING_STATUS);
        switch (msg.what)
        {
            case DOWNLOAD_IMAGE:
                handleTheDownloadMessage(msg);
                break;
            case QUIT:
                resultMsg = mImageManagerHandler.obtainMessage(ImageManagerHandler.THREAD_QUITED);
                resultMsg.obj = new ThreadQuitedParams(getLooper().getThread().getName());
                mImageManagerHandler.sendMessage(resultMsg);
                getLooper().quit();
                return;
            default:
                sendError(-1, String.format("Unknown message type : %d", msg.what), mCurrTask);
                break;
        }
    }

    /**
     * Download the image...
     * @param msg
     */
    private void handleTheDownloadMessage(Message msg)
    {
        mCurrTask = (ImageTask) msg.obj;
        if (mCurrTask == null) {
            sendError(-1, "msg.obj == null", mCurrTask);
            return;
        }
        String url = mCurrTask.getUrl();
        String filePath = ImageCacheManager.getInstance().getImageFilePath(url);
        if (filePath != null) {
            Log.d(TAG, String.format("%s downloads %s, store in %s",
                                        getLooper().getThread().getName(), url, filePath));
        } else {
            sendError(-1, "No storage", mCurrTask);
        }

        // find the image from the cache.
        boolean cachedInFileSystem = mImageCacheManager.isCachedInFileSystem(url, mCurrTask.getExpire());
        // try to download the image...
        if (!cachedInFileSystem && !downloadImage(filePath)) return;

        Bitmap image = BitmapFactory.decodeFile(filePath);

        if (image == null) {
            sendError(-1, "Deocde image from file error.", mCurrTask);
            return;
        }

        sendDownloadDown(image, filePath, mCurrTask);
    }

    /**
     * Download the image.
     * @param filePath the file path which the image will be stored to.
     * @return true for success and false for error.
     */
    private boolean downloadImage(String filePath)
    {
        int total, hasRead = 0;
        try {
            URL url = new URL(mCurrTask.getUrl());
            File outFile = new File(filePath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                sendError(-1, "No such file.", mCurrTask);
                return false;
            }else if (responseCode != HttpURLConnection.HTTP_OK) {
                sendError(-1, String.format("Http error %d %s", responseCode, connection.getResponseMessage())
                        , mCurrTask);
                return false;
            }

            total = connection.getContentLength();
            String contentType = connection.getContentType();
            Log.d(TAG, contentType);
            InputStream is = (InputStream) connection.getContent();

            File dir = outFile.getParentFile();

            // loop until the os has created the directory.
            // We found that the mkdirs function had a delay before the directory was really created!
            // WTF...
            int i = 0;
            while (!dir.exists() && !dir.mkdirs() && (++i) < 100);

            if (!outFile.exists()) outFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buf = new byte[1024];
            int read;
            while((read = is.read(buf)) != -1) {
                fos.write(buf, 0, read);
                hasRead += read;
                sendDownloadingProgress(total, hasRead, mCurrTask);
            }
            is.close();
            fos.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            sendError(-1, e.getMessage(), mCurrTask);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            sendError(-1, e.getMessage(), mCurrTask);
            return false;
        }
        return true;
    }

    /**
     * Send download done message to the image manager.
     * @param image the image.
     * @param path the path
     */
    private void sendDownloadDown(Bitmap image, String path, ImageTask task)
    {
        Message msg;
        msg = mImageManagerHandler.obtainMessage(ImageManagerHandler.DOWNLOAD_DONE);
        DownloadDoneParams params = new DownloadDoneParams();
        params.threadName = getLooper().getThread().getName();
        params.task = task;
        params.bmp = image;
        params.path = path;
        msg.obj = params;
        mThread.setStatus(ImageDownloadThread.IDLE_STATUS);
        mImageManagerHandler.sendMessage(msg);
    }

    /**
     * Send downloading progress message to the image manager.
     * @param total     the total length
     * @param hasRead   has read length
     */
    private void sendDownloadingProgress(int total, int hasRead, ImageTask task)
    {
        Message msg;
        msg = mImageManagerHandler.obtainMessage(ImageManagerHandler.DOWNLOADING_PROGRESS);
        DownloadingProgressParams params = new DownloadingProgressParams();
        params.threadName = getLooper().getThread().getName();
        params.total = total;
        params.hasRead = hasRead;
        params.task = task;
        msg.obj = params;
        mImageManagerHandler.sendMessage(msg);
    }

    /**
     * Send error message to the image manager.
     * @param code sendError code
     * @param desc sendError description
     */
    private void sendError(int code, String desc, ImageTask task)
    {
        Message msg;
        msg = mImageManagerHandler.obtainMessage(ImageManagerHandler.ERROR);
        ErrorParams params = new ErrorParams();
        params.threadName = getLooper().getThread().getName();
        params.code = code;
        params.desc = desc;
        params.task = task;
        msg.obj = params;
        mThread.setStatus(ImageDownloadThread.IDLE_STATUS);
        mImageManagerHandler.sendMessage(msg);
    }

    private ImageTask mCurrTask;
    private ImageCacheManager mImageCacheManager;
    private ImageDownloadThread mThread;
    private ImageManagerHandler mImageManagerHandler;
    private static final String TAG = ImageDownloadThreadHandler.class.getSimpleName();
}
