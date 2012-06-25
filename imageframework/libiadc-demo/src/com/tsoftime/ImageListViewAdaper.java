package com.tsoftime;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: huangcongyu2006
 * Date: 12-6-23 AM10:08
 */
public class ImageListViewAdaper extends BaseAdapter
{
    public ImageListViewAdaper(Context ctx, ListView listView)
    {
        this.listView = listView;
        urls = new ArrayList<String>();
        callBacks = new ArrayList<ImageTaskCallBack>();
        datas = new ArrayList<Data>();
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addURL(String url)
    {
        urls.add(url);
        datas.add(new Data());
        callBacks.add(new MyCallBack(callBacks.size()));
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
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.listview_item_progressbar);
        ImageManager imageManager = ImageManager.instance();
        Data data = datas.get(i);
        progressBar.setMax((int)data.total);
        progressBar.setProgress((int)data.hasRead);
        TextView tv = (TextView) view.findViewById(R.id.listview_item_progressbar_label);
        tv.setText(String.format("%d%%        %d/%d", (int)((float)data.hasRead * 100 / (float)data.total)
                                    , data.hasRead, data.total));
        iv.setImageBitmap(data.image);
        if (!data.hasAdded) {
            imageManager.getImage(urls.get(i), null, callBacks.get(i));
            data.hasAdded = true;
        }
        return view;
    }

    private ArrayList<String> urls;

    private class MyCallBack extends ImageTaskCallBack
    {
        public MyCallBack(int index)
        {
            this.index = index;
        }
        @Override
        public void onGettingProgress(int total, int hasGotten, HashMap<String, Object> params)
        {
            Data data = datas.get(index);
            if (data != null) {
                data.hasRead = hasGotten;
                data.total = total;
            }

            int wantedPosition;
            wantedPosition = index - (listView.getFirstVisiblePosition() - listView.getHeaderViewsCount());
            if (wantedPosition < 0 || wantedPosition >= listView.getChildCount() - listView.getHeaderViewsCount()) {
                //Log.w("ImageListViewAdapter", "wantedPosition < 0 || wantedPosition > listView.getChildCount()");
                return;
            }
            View view = listView.getChildAt(wantedPosition);
            if (view!= null) {
                ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.listview_item_progressbar);
                progressBar.setMax((int)total);
                progressBar.setProgress((int)hasGotten);
                TextView tv = (TextView) view.findViewById(R.id.listview_item_progressbar_label);
                tv.setText(String.format("%d%%        %d/%d", (int)((float)data.hasRead * 100 / (float)data.total)
                                                                            , data.hasRead, data.total));
            }
        }
        @Override
        public void onDownloadingDone(int status, Bitmap bmp, HashMap<String, Object> params)
        {
            Data data = datas.get(index);
            if (data != null) {
                data.image = bmp;
            }

            int wantedPosition;
            wantedPosition = index - (listView.getFirstVisiblePosition() - listView.getHeaderViewsCount());
            if (wantedPosition < 0 || wantedPosition >= listView.getChildCount() - listView.getHeaderViewsCount()) {
                //Log.w("ImageListViewAdapter", "wantedPosition < 0 || wantedPosition > listView.getChildCount()");
                return;
            }
            View view = listView.getChildAt(wantedPosition);
            if (view!= null) {
                ImageView iv = (ImageView) view.findViewById(R.id.listview_item_image);
                iv.setImageBitmap(bmp);
            }
        }
        private int index;
    }
    private ArrayList<ImageTaskCallBack> callBacks;

    private class Data
    {
        public int total = 0, hasRead = 0;
        public boolean hasAdded = false;
        public Bitmap image;
    }
    private ArrayList<Data> datas;
    private ListView listView;
    private LayoutInflater inflater;
}
