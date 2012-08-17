package com.tsoftime;

/**
 * The configuration of image manager.
 *
 * User: huangcongyu2006
 * Date: 12-6-24 PM8:37
 */
public class ImageMangerConfig
{
    public static ImageMangerConfig instance()
    {
        return mInstance;
    }

    public String getImageStoreDir()
    {
        return mImageStoreDir;
    }

    public void setImageStoreDir(String imageStoreDir)
    {
        this.mImageStoreDir = imageStoreDir;
    }

    public int getDownloadThreadsNumber()
    {
        return mDownloadThreadsNumber;
    }

    public void setDownloadThreadsNumber(int downloadThreadsNumber)
    {
        this.mDownloadThreadsNumber = downloadThreadsNumber;
    }

    public int getmMaxMemCacheSize()
    {
        return mMaxMemCacheSize;
    }

    public void setMaxMemCacheSize(int mMaxMemCacheSize)
    {
        this.mMaxMemCacheSize = mMaxMemCacheSize;
    }

    private String mImageStoreDir;       // the directory to store the images.
    private int mDownloadThreadsNumber;  // the number of the download threads.
    private int mMaxMemCacheSize;       // the max memory cache size(M).

    private ImageMangerConfig()
    {
        mImageStoreDir = "/imagemanager/cache/";
        mMaxMemCacheSize = -1;
        mDownloadThreadsNumber = 1;
    }
    private static ImageMangerConfig mInstance = new ImageMangerConfig();
}
