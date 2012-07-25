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
import java.util.Random;

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
        datas = new ArrayList<DownloadProgress>();
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        date = new int[300];
        date[0] = 1;
    }

    public void addURL(String url)
    {
        urls.add(url);
        datas.add(new DownloadProgress());
        callBacks.add(new MyCallBack(callBacks.size()));
        if (urls.size() > 1) {
            Random r = new Random();
            float  f = r.nextFloat();
            if (f < 0.2) {
                date[urls.size() - 1] = date[urls.size() - 2] + 1;
            } else {
                date[urls.size() - 1] = date[urls.size() - 2];
            }
        }
    }

    public int getDate(int index)
    {
        return date[index];
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
        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.listview_imageitem, null);
            holder = new ViewHolder();
            holder.iv = (ImageView) view.findViewById(R.id.listview_item_image);
            holder.pb = (ProgressBar) view.findViewById(R.id.listview_item_progressbar);
            holder.tv = (TextView) view.findViewById(R.id.listview_item_progressbar_label);
            holder.urlTv = (TextView) view.findViewById(R.id.listview_item_url_label);
            holder.dateLabel = (TextView) view.findViewById(R.id.listview_item_date_label);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.iv.setImageBitmap(null);

        DownloadProgress data = datas.get(i);
        holder.pb.setMax(data.total);
        holder.pb.setProgress(data.hasRead);

        holder.tv.setText(String.format("%d%%        %d/%d", (int)((float)data.hasRead * 100 / (float)data.total)
                                    , data.hasRead, data.total));
        holder.urlTv.setText(urls.get(i));
        holder.dateLabel.setVisibility(View.VISIBLE);
        if (i > 0 && date[i] != date[i - 1]){
            holder.dateLabel.setText(String.format("第%d天", getDate(i)));
        } else {
            if (i == 0) {
                holder.dateLabel.setText(String.format("第%d天", getDate(i)));
            } else {
                holder.dateLabel.setVisibility(View.GONE);
            }
        }
        // 调用getImage获取图片
        ImageManager imageManager = ImageManager.instance();
        imageManager.dispatchImageTask(urls.get(i), null, callBacks.get(i)
                                            , ImageTask.TaskPriority.DEFAULT_PRIORITY, 60);
        return view;
    }

    /**
     * 下载第index张图片
     * @param index
     */
    public void downloadImage(int index)
    {
        if (index < 0) return;
        ImageManager imageManager = ImageManager.instance();
        imageManager.dispatchImageTask(urls.get(index), null, callBacks.get(index)
                                            , ImageTask.TaskPriority.DEFAULT_PRIORITY, 60);
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
            DownloadProgress data = datas.get(index);
            // 保存下载进度
            if (data != null) {
                data.hasRead = hasGotten;
                data.total = total;
            }

            // 获取对应的list view item。
            int wantedPosition;
            wantedPosition = index - (listView.getFirstVisiblePosition() - listView.getHeaderViewsCount());
            View view = listView.getChildAt(wantedPosition);

            if (view!= null) {
                ViewHolder holder = (ViewHolder) view.getTag();
                holder.pb.setMax(total);
                holder.pb.setProgress(hasGotten);
                holder.tv.setText(String.format("%d%%        %d/%d",
                                                        (int)((float)data.hasRead * 100 / (float)data.total)
                                                        , data.hasRead, data.total));
            }
        }
        @Override
        public void onDownloadingDone(int status, Bitmap bmp, HashMap<String, Object> params)
        {
            int wantedPosition;
            wantedPosition = index - (listView.getFirstVisiblePosition() - listView.getHeaderViewsCount());
            View view = listView.getChildAt(wantedPosition);
            if (view!= null) {
                // 下载完成。显示图片。
                ViewHolder holder = (ViewHolder) view.getTag();
                holder.iv.setImageBitmap(bmp);
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
    private class DownloadProgress
    {
        public int total = 0, hasRead = 0;
    }

    /*
     * view holder
     */
    private class ViewHolder
    {
        ProgressBar pb;
        ImageView iv;
        TextView tv, urlTv, dateLabel;
    }

    private ArrayList<DownloadProgress> datas;
    private ListView listView;
    private LayoutInflater inflater;
    private int[] date;
}
