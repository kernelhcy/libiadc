package com.tsoftime;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.tsoftime.cache.ImageCacheManager;
import com.tsoftime.messeage.params.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
 *             <li>"urlStr" : the urlStr of the image to be downloaded.</li>
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
class ImageDownloadThreadHandler extends Handler
{
    public static final int DOWNLOAD_IMAGE = 1;     // the message type of downloading an image
    public static final int QUIT = 2;               // the message type of quit

    public ImageDownloadThreadHandler(ImageDownloadThread thread, ImageManagerHandler handler)
    {
        this.thread = thread;
        this.imageManagerHandler = handler;
        this.imageCacheManager = ImageCacheManager.getInstance();
    }

    /**
     * Handler the message.
     *
     * The image manager send a DOWNLOAD_IMAGE message to the download thread to download a image.
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
        switch (msg.what)
        {
            case DOWNLOAD_IMAGE:
                dealTheDownloadMessage(msg);
                break;
            case QUIT:
                resultMsg = imageManagerHandler.obtainMessage(ImageManagerHandler.THREAD_QUITED);
                resultMsg.obj = new ThreadQuitedParams(getLooper().getThread().getName());
                imageManagerHandler.sendMessage(resultMsg);
                getLooper().quit();
                return;
            default:
                sendError(-1, String.format("Unknown message type : %d", msg.what));
                break;
        }

    }

    /**
     * Download the image...
     * @param msg
     */
    private void dealTheDownloadMessage(Message msg)
    {
        Bundle params = msg.getData();
        if (params == null) {
            sendError(-1, "msg.getData() == null");
            return;
        }
        String imageQuality = params.getString("image_quality");
        urlStr = params.getString("url");
        String filePath = ImageCacheManager.getInstance().getImageFilePath(urlStr);
        Log.d(TAG, String.format("%s downloads %s, store in %s", getLooper().getThread().getName(), urlStr, filePath));

        // find the image from the cache.
        Bitmap bmp = imageCacheManager.getImageFromFileSystemCache(urlStr
                                                        , params.getLong("expire", Long.MAX_VALUE)
                                                        , imageQuality);
        if (bmp != null) {
            File imageFile = new File(filePath);
            sendDownloadingProgress((int)imageFile.length(), (int)imageFile.length());
            sendDownloadDown(bmp, filePath);
            return;
        }

        // try to download the image...
        if (!downloadImage(filePath)) return;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        if (imageQuality.equals(ImageQuality.QUALITY_MEDIUM.toString())) {
            options.inSampleSize = 2;
        } else if (imageQuality.equals(ImageQuality.QUALITY_LOW.toString())) {
            options.inSampleSize = 4;
        }

        Bitmap image = BitmapFactory.decodeFile(filePath, options);

        if (image == null) {
            sendError(-1, "Deocde image from file error.");
            return;
        }

        sendDownloadDown(image, filePath);
    }

    /**
     * Download the image.
     * @param filePath the file path which the image will be stored to.
     * @return true for success and false for error.
     */
    private boolean downloadImage(String filePath)
    {
        int total = 0, hasRead = 0;
        try {
            URL url = new URL(urlStr);
            File outFile = new File(filePath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                sendNoSuchImage(urlStr);
                return false;
            }else if (responseCode != HttpURLConnection.HTTP_OK) {
                sendError(-1, String.format("Http error %d %s", responseCode, connection.getResponseMessage()));
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
                sendDownloadingProgress(total, hasRead);
            }
            is.close();
            fos.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            sendError(-1, e.getMessage());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            sendError(-1, e.getMessage());
            return false;
        }
        return true;
    }


    /**
     * Send no such image message to the image manager.
     * @param url the url of the image
     */
    private void sendNoSuchImage(String url)
    {
        Message msg;
        msg = imageManagerHandler.obtainMessage(ImageManagerHandler.NO_SUCH_IMAGE);
        NoSuchImageParams params = new NoSuchImageParams();
        params.threadName = getLooper().getThread().getName();
        params.url = urlStr;
        msg.obj = params;
        imageManagerHandler.sendMessage(msg);
    }

    /**
     * Send download done message to the image manager.
     * @param image the image.
     * @param path the path
     */
    private void sendDownloadDown(Bitmap image, String path)
    {
        Message msg;
        msg = imageManagerHandler.obtainMessage(ImageManagerHandler.DOWNLOAD_DONE);
        ImageDownloadDoneParams params = new ImageDownloadDoneParams();
        params.threadName = getLooper().getThread().getName();
        params.url = urlStr;
        params.bmp = image;
        params.path = path;
        msg.obj = params;
        imageManagerHandler.sendMessage(msg);
    }

    /**
     * Send downloading progress message to the image manager.
     * @param total     the total length
     * @param hasRead   has read length
     */
    private void sendDownloadingProgress(int total, int hasRead)
    {
        Message msg;
        msg = imageManagerHandler.obtainMessage(ImageManagerHandler.DOWNLOADING_PROGRESS);
        DownloadingProgressParams params = new DownloadingProgressParams();
        params.threadName = getLooper().getThread().getName();
        params.total = total;
        params.hasRead = hasRead;
        params.url = urlStr;
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

    private String urlStr;
    private ImageCacheManager imageCacheManager;
    private ImageDownloadThread thread;
    private ImageManagerHandler imageManagerHandler;
    private static final String TAG = ImageDownloadThreadHandler.class.getSimpleName();
}
