package com.tsoftime;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.tsoftime.messeage.params.TaskPriority;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: huangcongyu2006
 * Date: 12-7-27 PM4:23
 */
public class ViewPagerAdapter extends BaseAdapter
{
    public ViewPagerAdapter(Context context, ViewPager viewPager)
    {
        this.mContext = context;
        mUrls = new ArrayList<String>();
        mViewPager = viewPager;
        mCallback = new MyCallback();
        mViews = new View[1];
    }

    public void addUrl(String url)
    {
        mUrls.add(url);
        View[] news = new View[mUrls.size()];
        System.arraycopy(mViews, 0, news, 0, mViews.length);
        mViews = news;
    }

    @Override
    public int getCount()
    {
        return mUrls.size();
    }

    @Override
    public Object getItem(int i)
    {
        return mUrls.get(i);
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
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_imageitem, null);
            holder = new ViewHolder();
            holder.iv = (ImageView) view.findViewById(R.id.listview_item_image);
            holder.pb = (ProgressBar) view.findViewById(R.id.listview_item_progressbar);
            holder.tv = (TextView) view.findViewById(R.id.listview_item_progressbar_label);
            holder.urlTv = (TextView) view.findViewById(R.id.listview_item_url_label);
            holder.dateLabel = (TextView) view.findViewById(R.id.listview_item_date_label);
            view.setTag(holder);
            mViews[i] = view;
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.dateLabel.setText(String.format("第%d天", i + 1));

        // 释放所有的Bitmap，降低内存使用
        for(int j = 0; j < mViews.length; ++j) {
            if (Math.abs(j - mViewPager.getCurScreen()) > 1 && mViews[j] != null) {
                holder = (ViewHolder) mViews[j].getTag();
                holder.iv.setImageBitmap(null);
            }
        }

        ImageManager imageManager = ImageManager.instance();
        imageManager.removeAllTasks();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("index", i);
        imageManager.dispatchImageTask(mUrls.get(i), params, mCallback
                            , TaskPriority.DEFAULT_PRIORITY, 60 * 60, 500);
        if (i - 1 >= 0) {
            params = new HashMap<String, Object>();
            params.put("index", i - 1);
            imageManager.dispatchImageTask(mUrls.get(i - 1), params, mCallback
                , TaskPriority.DEFAULT_PRIORITY, 60 * 60, 500);
        }
        if (i + 1 < mUrls.size()) {
            params = new HashMap<String, Object>();
            params.put("index", i + 1);
            imageManager.dispatchImageTask(mUrls.get(i + 1), params, mCallback
                , TaskPriority.DEFAULT_PRIORITY, 60 * 60, 500);
        }

        return view;
    }

    private class MyCallback implements ImageTaskCallBack
    {

        @Override
        public void onGettingProgress(int total, int hasGotten, HashMap<String, Object> params)
        {
        }

        @Override
        public void onDownloadingDone(int status, Bitmap bmp, HashMap<String, Object> params)
        {
            int currScreen = mViewPager.getCurScreen();
            int index = (Integer)params.get("index");
            if (Math.abs(index - currScreen) <= 1 && mViews[index] != null) {
                ViewHolder holder = (ViewHolder) mViews[index].getTag();
                holder.iv.setImageBitmap(bmp);
            }
        }
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
    private Context mContext;
    private ArrayList<String> mUrls;
    private ViewPager mViewPager;
    private MyCallback mCallback;
    private View[] mViews;
}
