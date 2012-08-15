package com.tsoftime;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.*;
import com.tsoftime.messeage.params.TaskPriority;

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
        callBack = new MyCallBack();
        datas = new ArrayList<DownloadProgress>();
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        date = new int[300];
        firstTime = new boolean[300];
        for(int i = 0; i < firstTime.length; ++i) firstTime[i] = true;
        date[0] = 1;
    }

    public void addURL(String url)
    {
        urls.add(url);
        datas.add(new DownloadProgress());
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

        DownloadProgress data = datas.get(i);
        holder.pb.setMax(data.total);
        holder.pb.setProgress(data.hasRead);
        holder.pb.setVisibility(View.VISIBLE);

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
        holder.iv.setImageResource(R.drawable.default_bg);
        ImageManager imageManager = ImageManager.instance();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("index", i);
        imageManager.dispatchImageTask(urls.get(i), params, callBack
                                    , TaskPriority.DEFAULT_PRIORITY, 60 * 60);

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
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("index", index);
        imageManager.dispatchImageTask(urls.get(index), params, callBack
                                            , TaskPriority.DEFAULT_PRIORITY, 60 * 60);
    }

    private ArrayList<String> urls;

    /**
     * 获取图片的回调函数。
     */
    private class MyCallBack implements ImageTaskCallBack
    {
        public MyCallBack()
        {
        }
        @Override
        public void onGettingProgress(int total, int hasGotten, HashMap<String, Object> params)
        {
            int index = (Integer)params.get("index");
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
            int index = (Integer)params.get("index");
            wantedPosition = index - (listView.getFirstVisiblePosition() - listView.getHeaderViewsCount());
            View view = listView.getChildAt(wantedPosition);
            if (view!= null) {
                // 下载完成。显示图片。
                ViewHolder holder = (ViewHolder) view.getTag();
                holder.iv.setImageBitmap(bmp);
                if (firstTime[index]){
                    animation = new AlphaAnimation(0f, 1.0f);
                    animation.setDuration(200);
                    animation.setFillAfter(true);
                    holder.iv.startAnimation(animation);
                    firstTime[index] = false;
                }
                holder.pb.setVisibility(View.GONE);
                /*
                 * 这里不对bmp参数做任何保存！让libiadc进行图片缓存处理。
                 */
            }
        }
        private AlphaAnimation animation;
    }
    private ImageTaskCallBack callBack;

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
    private boolean[] firstTime;
}
