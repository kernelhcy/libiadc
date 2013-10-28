package com.tsoftime;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Describe an image task.
 *
 * User: huangcongyu2006
 * Date: 12-6-23 AM11:12
 */
public class ImageTask implements Runnable, Comparable<ImageTask>
{
    // 优先级
    public static final int LOW_PRIORITY = -1;
    public static final int DEFAULT_PRIORITY = 0;
    public static final int HIGH_PRIORITY = 1;

    public ImageTask(String url, HashMap<String, Object> params, ImageTaskCallBack callBack, Handler handler
                    , int priority, long expire)
    {
        this.mHandler = handler;
        this.mUrl = url;
        this.mPriority = priority;
        this.mExpire = expire;
        this.mCallBacks = new ArrayList<ImageTaskCallBackAndParams>();
        this.addCallback(callBack, params);

        this.mImageCacheManager = ImageCacheManager.getInstance();
    }


    public void addCallback(ImageTaskCallBack callBack, HashMap<String, Object> params)
    {
        ImageTaskCallBackAndParams cp = new ImageTaskCallBackAndParams();
        cp.mCallBack = callBack;
        cp.mParams = params;
        this.mCallBacks.add(cp);
    }

    /**
     * 返回任务的id。用于唯一标识该任务。
     * @return
     */
    public int getTaskId()
    {
        return this.hashCode();
    }

    @Override
    public int compareTo(ImageTask imageTask)
    {
        return mPriority - imageTask.mPriority;
    }

    @Override
    public void run()
    {
        download();
    }


    /**
     * Download the image...
     */
    private void download()
    {
        String filePath = ImageCacheManager.getInstance().getImageFilePath(mUrl);
        if (filePath == null) {
            sendError(ImageManager.ERROR, "No storage");
            return;
        }

        // find the image from the cache.
        boolean cachedInFileSystem = mImageCacheManager.isCachedInFileSystem(mUrl, mExpire);
        // try to download the image...
        if (!cachedInFileSystem) {
            Log.d(TAG, String.format("Downloading %s, store in %s", mUrl, filePath));
            if (!downloadImage(filePath)) return;
        } else {
            Log.d(TAG, String.format("Cached %s, store in %s", mUrl, filePath));
        }

        // prevent OOM
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);
        int maxSize = opts.outWidth > opts.outHeight ? opts.outWidth : opts.outHeight;
        if (maxSize > ImageManager.instance().getImageMaxSize()) {
            opts.inSampleSize = maxSize / ImageManager.instance().getImageMaxSize();
        }
        opts.inJustDecodeBounds = false;
        Log.d(TAG, String.format("image size %dx%d, sample size %d", opts.outWidth, opts.outHeight, opts.inSampleSize));

        Bitmap image = BitmapFactory.decodeFile(filePath, opts);

        if (image == null) {
            sendError(ImageManager.ERROR, "Deocde image from file error.");
            return;
        }

        mHandler.post(new DoneReporter(ImageManager.SUCCESS, image, filePath));
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
            URL url = new URL(mUrl);
            File outFile = new File(filePath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                sendError(ImageManager.ERROR, "No such file.");
                return false;
            }else if (responseCode != HttpURLConnection.HTTP_OK) {
                sendError(ImageManager.ERROR, String.format("Http error %d %s"
                                                    , responseCode, connection.getResponseMessage()));
                return false;
            }

            total = connection.getContentLength();
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
                mHandler.post(new ProgressReporter(total, hasRead));
            }
            is.close();
            fos.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            sendError(ImageManager.ERROR, e.getMessage());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            sendError(ImageManager.ERROR, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * send error.
     * @param status
     * @param errMsg
     */
    private void sendError(int status, String errMsg)
    {
        mHandler.post(new ErrorReporter(status, errMsg));
    }

    /**
     * Error reporter.
     */
    private class ErrorReporter implements Runnable
    {
        public ErrorReporter(int status, String msg)
        {
            this.mMsg= msg;
            this.mStatus = status;
        }

        @Override
        public void run()
        {
            for (ImageTaskCallBackAndParams callBack : mCallBacks) {
                callBack.mParams.put("error_msg", mMsg);
                callBack.mCallBack.onDownloadingDone(mStatus, null, callBack.mParams);
                Log.d(TAG, String.format("error: %s url: %s", mMsg, mUrl));
            }
        }

        private int mStatus;
        private String mMsg;
    }

    /**
     * Downloading progress reporter.
     */
    private class ProgressReporter implements Runnable
    {
        public ProgressReporter(int total, int hasRead)
        {
            this.mTotal = total;
            this.mHasRead = hasRead;
        }
        @Override
        public void run()
        {
            for (ImageTaskCallBackAndParams callBack : mCallBacks) {
                callBack.mCallBack.onGettingProgress(mTotal, mHasRead, callBack.mParams);
            }
        }

        private int mTotal, mHasRead;
    }

    /**
     * Download done reporter.
     */
    private class DoneReporter implements Runnable
    {
        public DoneReporter(int status, Bitmap bmp, String filePath)
        {
            this.mStatus = status;
            this.mBmp = bmp;
            this.mFilePath = filePath;
        }

        @Override
        public void run()
        {
            for (ImageTaskCallBackAndParams callBack : mCallBacks) {
                callBack.mCallBack.onDownloadingDone(mStatus, mBmp, callBack.mParams);
            }
        }

        private int mStatus;
        private Bitmap mBmp;
        private String mFilePath;
    }

    private class ImageTaskCallBackAndParams
    {
        ImageTaskCallBack mCallBack;
        HashMap<String, Object> mParams;
    }

    private String mUrl;
    private int mPriority;
    private long mExpire;
    private ArrayList<ImageTaskCallBackAndParams> mCallBacks;

    private ImageCacheManager mImageCacheManager;

    private Handler mHandler;           // the handler used to send message to main thread.

    private static final String TAG = ImageTask.class.getSimpleName();
}
