package com.tsoftime;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.TextView;

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

        //http://vm-192-168-18-243.shengyun.grandcloud.cn/mig31/
        for (int i = 1; i < 258; ++i) {
            String url = "http://vm-192-168-18-243.shengyun.grandcloud.cn/mig31/" + i + ".jpg";
            adaper.addURL(url);
        }

        listView.setAdapter(adaper);
        listView.setFastScrollEnabled(true);
        listView.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            private int firstVisibleItem, visibleItemCount;

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
                            adaper.downloadImage(j);
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
            //listView.setSelection(1);
            //listView.smoothScrollToPosition(1);
            listView.onRefreshComplete();
        }
    }


    private PullToRefreshListView listView;
    private ImageListViewAdaper adaper;
    private TextView dateTextView;
    private boolean isDateChanging;
}
