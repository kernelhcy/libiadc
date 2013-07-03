package com.tsoftime.cache;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import com.tsoftime.ImageMangerConfig;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * The image cache manager.
 * User: huangcongyu2006
 * Date: 12-6-25 PM3:27
 */
public class ImageCacheManager
{
    private ImageCacheManager(Context ctx)
    {
        context = ctx;
        //cache = new HashMap<String, SoftReference<Bitmap>>();
        mFilePathCache = new HashMap<String, String>();
    }

    /**
     * Initial the ImageCacheManager.
     * This method MUST be called before any other methods of the ImageCacheManager.
     *
     * @param context the context
     */
    public static void init(Context context)
    {
        mInstance = new ImageCacheManager(context);
    }

    public static ImageCacheManager getInstance()
    {
        return mInstance;
    }

    /**
     * Get the image file path.
     *
     * We use the md5 value of the url to create the file path.
     * If the md5 value of the url is '5d41402abc4b2a76b9719d911017c592', the file path will be
     * /5/d/4/1402abc4b2a76b9719d911017c592.
     * @param url
     * @return if the storage is not available, null will be returned;
     */
    public String getImageFilePath(String url)
    {
        // find from the cache
        String filePath = mFilePathCache.get(url);
        if (filePath != null) return filePath;

        // generate the file path
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(url.getBytes());
            byte[] result = md.digest();
            StringBuilder sb = new StringBuilder(result.length * 2);
            for (byte b : result) {
                sb.append(Character.forDigit((b >>> 4) & 15, 16)).append(Character.forDigit(b & 15, 16));
            }
            // create the path
            StringBuilder path = new StringBuilder();
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir == null) {
                externalCacheDir = context.getCacheDir();
                if (externalCacheDir == null) return null;
            }
            path.append(externalCacheDir.getAbsolutePath());                // external cache dir
            path.append(ImageMangerConfig.instance().getImageStoreDir());   // image cache dir
            path.append('/').append(sb.charAt(0));
            path.append('/').append(sb.charAt(1));
            path.append('/').append(sb.charAt(2));
            path.append('/').append(sb.substring(3, sb.length()));
            filePath = path.toString();
            mFilePathCache.put(url, filePath);
            return filePath;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Is the image cached in the file system.
     *
     * NOTE:
     *  This method is called in the download thread!
     *
     * @param url       the url of the image
     * @param expire    the expire time of this image
     * @return
     */
    public boolean isCachedInFileSystem(String url, long expire)
    {
        String filePath = getImageFilePath(url);
        if (filePath == null) return false;

        File imageFile = new File(filePath);
        if (!imageFile.exists()) return false;

        // test whether the image cache expire time exceeded
        Date now = Calendar.getInstance().getTime();
        long timeHasGone = (now.getTime() - imageFile.lastModified()) / 1000;
        // the expire time has exceeded, return null to reload download the image.
        if (timeHasGone > expire) {
            return false;
        }
        return true;
    }

    private Context context;
    private static final String TAG = ImageCacheManager.class.getSimpleName();

    private HashMap<String, String> mFilePathCache;
    private static ImageCacheManager mInstance = null;
}
