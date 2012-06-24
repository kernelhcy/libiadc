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
        return imageStoreDir;
    }

    public void setImageStoreDir(String imageStoreDir)
    {
        this.imageStoreDir = imageStoreDir;
    }

    public int getDownloadThreadsNumber()
    {
        return downloadThreadsNumber;
    }

    public void setDownloadThreadsNumber(int downloadThreadsNumber)
    {
        this.downloadThreadsNumber = downloadThreadsNumber;
    }

    private String imageStoreDir;       // the directory to store the images.
    private int downloadThreadsNumber;  // the number of the download threads.

    private ImageMangerConfig()
    {
        imageStoreDir = "/imagemanager/cache/";
    }
    private static ImageMangerConfig mInstance = new ImageMangerConfig();
}
