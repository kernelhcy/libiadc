package com.tsoftime;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * User: huangcongyu2006
 * Date: 12-6-23 AM10:08
 */
public class ImageListViewAdaper extends BaseAdapter
{
    public ImageListViewAdaper(Context ctx)
    {
        urls = new ArrayList<String>();
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addURL(String url)
    {
        urls.add(url);
    }

    @Override
    public int getCount()
    {
        return urls.size();
    }

    @Override
    public Object getItem(int i)
    {
        return urls.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        if (view == null) {
            view = inflater.inflate(R.layout.listview_imageitem, null);
        }
        ImageView iv = (ImageView) view.findViewById(R.id.listview_item_image);

        ImageManager imageManager = ImageManager.instance();
        imageManager.getImage(urls.get(i), null, null);

        return view;
    }

    private ArrayList<String> urls;
    private LayoutInflater inflater;
}
