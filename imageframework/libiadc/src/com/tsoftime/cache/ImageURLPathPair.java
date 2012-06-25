package com.tsoftime.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.SyncStateContract;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * The image url and the file path pair.
 * User: huangcongyu2006
 * Date: 12-6-25 PM3:05
 */
public class ImageURLPathPair
{
    /**
     * Get the url path pair of url
     * @param url   the url
     * @param ctx   the context
     * @return      an instance of ImageURLPathPair. or null for error.
     */
    public static ImageURLPathPair select(String url, Context ctx)
    {
        SQLiteDatabase db = (new ImageCacheDatabaseHelper(ctx)).getWritableDatabase();
        Cursor c = db.query(tableName, columns, "url=?", new String[]{url}, null, null, null, "1");
        c.moveToFirst();
        if (c.isAfterLast()) {
            c.close();
            db.close();
            return null;
        }
        ImageURLPathPair pair = new ImageURLPathPair();
        pair.id = c.getLong(0);
        pair.url = c.getString(1);
        pair.path = c.getString(2);
        try {
            pair.createdAt = sdf.parse(c.getString(3));
        } catch (ParseException e) {
            Log.e(TAG, String.format("%s %s", c.getString(3), e.getMessage()));
            e.printStackTrace();
            c.close();
            db.close();
            return null;
        }
        c.close();
        db.close();
        return pair;
    }

    /**
     * Save this pair to the database.
     * @param ctx   the context
     * @return      -1 for failed, or return the id of this row in the database.
     */
    public long save(Context ctx)
    {
        ContentValues cv = new ContentValues();
        cv.put("url", url);
        cv.put("path", path);
        cv.put("created_at", sdf.format(Calendar.getInstance().getTime()));
        if (id > 0) {
            cv.put("_id", id);
        }
        SQLiteDatabase db = (new ImageCacheDatabaseHelper(ctx)).getWritableDatabase();
        id = db.replace(tableName, null, cv);
        db.close();
        return id;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public long getId()
    {
        return id;
    }

    private long id = -1;
    private String url;
    private String path;
    private Date createdAt;

    private static final String tableName = "image_cache_maps";
    private static final String[] columns = new String[]{"_id", "url", "path", "created_at"};
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String TAG = ImageURLPathPair.class.getSimpleName();
}
