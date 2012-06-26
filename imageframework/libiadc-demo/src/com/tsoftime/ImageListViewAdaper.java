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
 * List View Adapter
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
        datas = new ArrayList<DownlaodProgress>();
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addURL(String url)
    {
        urls.add(url);
        datas.add(new DownlaodProgress());
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
        iv.setImageBitmap(null);

        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.listview_item_progressbar);
        DownlaodProgress data = datas.get(i);
        progressBar.setMax(data.total);
        progressBar.setProgress(data.hasRead);

        TextView tv = (TextView) view.findViewById(R.id.listview_item_progressbar_label);
        tv.setText(String.format("%d%%        %d/%d", (int)((float)data.hasRead * 100 / (float)data.total)
                                    , data.hasRead, data.total));
        tv = (TextView) view.findViewById(R.id.listview_item_url_label);
        tv.setText(urls.get(i));

        // 调用getImage获取图片
        ImageManager imageManager = ImageManager.instance();
        imageManager.getImage(urls.get(i), null, callBacks.get(i));
        return view;
    }

    private ArrayList<String> urls;

    /**
     * 获取图片的回调函数。
     */
    private class MyCallBack implements ImageTaskCallBack
    {
        public MyCallBack(int index)
        {
            this.index = index;
        }
        @Override
        public void onGettingProgress(int total, int hasGotten, HashMap<String, Object> params)
        {
            DownlaodProgress data = datas.get(index);
            // 保存下载进度
            if (data != null) {
                data.hasRead = hasGotten;
                data.total = total;
            }

            // 获取对应的list view item。
            int wantedPosition;
            wantedPosition = index - (listView.getFirstVisiblePosition() - listView.getHeaderViewsCount());
            if (wantedPosition < 0 || wantedPosition >= listView.getChildCount() - listView.getHeaderViewsCount()) {
                //Log.w("ImageListViewAdapter", "wantedPosition < 0 || wantedPosition > listView.getChildCount()");
                return;
            }
            View view = listView.getChildAt(wantedPosition);

            if (view!= null) {
                ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.listview_item_progressbar);
                progressBar.setMax(total);
                progressBar.setProgress(hasGotten);
                TextView tv = (TextView) view.findViewById(R.id.listview_item_progressbar_label);
                tv.setText(String.format("%d%%        %d/%d", (int)((float)data.hasRead * 100 / (float)data.total)
                                                                            , data.hasRead, data.total));
            }
        }
        @Override
        public void onDownloadingDone(int status, Bitmap bmp, HashMap<String, Object> params)
        {
            int wantedPosition;
            wantedPosition = index - (listView.getFirstVisiblePosition() - listView.getHeaderViewsCount());
            if (wantedPosition < 0 || wantedPosition >= listView.getChildCount() - listView.getHeaderViewsCount()) {
                Log.w("ImageListViewAdapter", "wantedPosition < 0 || wantedPosition > listView.getChildCount()");
                return;
            }
            View view = listView.getChildAt(wantedPosition);
            if (view!= null) {
                // 下载完成。显示图片。
                ImageView iv = (ImageView) view.findViewById(R.id.listview_item_image);
                iv.setImageBitmap(bmp);
                /*
                 * 这里不对bmp参数做任何保存！让libiadc进行图片缓存处理。
                 */
            }
        }
        private int index;
    }
    private ArrayList<ImageTaskCallBack> callBacks;

    /**
     * 保存每张图片的下载进度。
     * 在滑动listview的时候，需要重新设置图片的下载进度。
     */
    private class DownlaodProgress
    {
        public int total = 0, hasRead = 0;
    }
    private ArrayList<DownlaodProgress> datas;
    private ListView listView;
    private LayoutInflater inflater;
}
