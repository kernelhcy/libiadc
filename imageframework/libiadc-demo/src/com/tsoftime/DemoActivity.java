package com.tsoftime;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * 示例Activity
 */
public class DemoActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // 初始化ImageManager。
        ImageManager.init(getApplicationContext());
        //ImageManager.instance().setDownloadThreadNumber(2);

        listView = (PullToRefreshListView) findViewById(R.id.listview);
        adaper = new ImageListViewAdaper(getApplicationContext(), listView);
        dateTextView = (TextView) findViewById(R.id.up_date_label);

        final ArrayList<String> urls = new ArrayList<String>();
        //http://vm-192-168-18-243.shengyun.grandcloud.cn/mig31/
        String host = "http://vm-192-168-18-243.shengyun.grandcloud.cn/big/";
        for (int i = 0; i < 10; ++i) {
            String url =  host + (i + 1) + ".jpg";
            adaper.addURL(url);
            urls.add(url);
        }
        Log.d(DemoActivity.class.getSimpleName(), String.format("url count %d", urls.size()));

        listView.setAdapter(adaper);
        listView.setFastScrollEnabled(true);
        listView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            private int firstVisibleItem;
            private int visibleItemCount;

            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState)
            {
                ImageManager imageManager = ImageManager.instance();
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        //Log.d(DemoActivity.class.getSimpleName(), "Scroll view idle.");
                        imageManager.removeAllTasks();
                        for (int i = 0, j = firstVisibleItem - 1; i < visibleItemCount; ++i, ++j) {
                            Log.d(DemoActivity.class.getSimpleName(), String.format("downloading ... %d.jpg", j));
                            adaper.downloadImage(j - 1);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem
                , int visibleItemCount, int totalItemCount)
            {
                this.firstVisibleItem = firstVisibleItem;
                this.visibleItemCount = visibleItemCount;
                int position = firstVisibleItem;
                if (position <= 0) {
                    dateTextView.setVisibility(View.GONE);
                } else {
                    dateTextView.setVisibility(View.VISIBLE);
                    dateTextView.setText(String.format("第%d天", adaper.getDate(position - 1)));
                }
            }
        });
        listView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                new LoginTask().execute();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Intent intent = new Intent(DemoActivity.this, ImageViewActivity.class);
                intent.putExtra("urls", urls);
                intent.putExtra("url", (String)adaper.getItem(i - 1));
                startActivity(intent);
                overridePendingTransition(R.anim.zoomout, R.anim.zoomin);
            }
        });
    }

    private class LoginTask extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids)
        {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(Void params)
        {
            listView.onRefreshComplete();
        }
    }


    private PullToRefreshListView listView;
    private ImageListViewAdaper adaper;
    private TextView dateTextView;

}
