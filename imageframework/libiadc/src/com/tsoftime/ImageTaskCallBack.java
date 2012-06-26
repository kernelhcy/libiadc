package com.tsoftime;

import android.graphics.Bitmap;
import android.os.Bundle;

import java.util.HashMap;

/**
 * ImageTaskCallBack
 *
 * Describe a image task callback.
 * You should extend this abstract class to create you own callback.
 *
 * User: huangcongyu2006
 * Date: 12-6-23 AM10:20
 */
public interface ImageTaskCallBack
{
    /**
     * When get some part of the image, the image manager will call this function to tell you the downloading progress.
     * @param total         the total size of the image
     * @param hasGotten     the size of the part we have gotten
     * @param params        the parameters you have passed to the image manager
     */
    public void onGettingProgress(int total, int hasGotten, HashMap<String, Object> params);

    /**
     * When the image is downloaded, the image manager call this function to tell you.
     * The status:
     *      1   => success
     *      -1  => error
     *      -2  => no such image
     *
     * @param status        the status
     * @param bmp           the image
     * @param params        the parameters you have passed to the image manager
     */
    public abstract  void onDownloadingDone(int status, Bitmap bmp, HashMap<String, Object> params);
}
