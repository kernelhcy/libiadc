package com.tsoftime.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * The is the database helper for sqlite.
 * We just create the table `image_cache_maps` to store the map between the url of the image and the file path.
 * ===========================================================================
 * |  _id    |     url       |        path        |        created_at        |
 * ===========================================================================
 * |    1    | http://1.jpg  | /2012/3/2/1.jpg    |   2012-12-12 12:23:21    |
 * |    2    | http://2.jpg  | /2012/3/2/2.jpg    |   2012-12-12 12:23:21    |
 * |    3    | http://3.jpg  | /2012/3/2/3.jpg    |   2012-12-12 12:23:21    |
 * ===========================================================================
 *
 * User: huangcongyu2006
 * Date: 12-6-25 PM2:36
 */
public class ImageCacheDatabaseHelper extends SQLiteOpenHelper
{
    public ImageCacheDatabaseHelper(Context context)
    {
        super(context, "image_cache_db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL(createTablesSqls);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {

    }

    private final String createTablesSqls = "CREATE TABLE image_cache_maps " +
                                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT, path TEXT, created_at TIMESTAMP);";
}
