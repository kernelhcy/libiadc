package com.tsoftime;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.AbsListView;
import android.widget.ListView;

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
                switch (scrollState)
                {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        Log.d(DemoActivity.class.getSimpleName(), "Scroll view idle.");
                        imageManager.removeAllTasks();
                        for (int i = 0, j = firstVisibleItem; i < visibleItemCount; ++i, ++j) {
                            adaper.downloadImage(j);
                            Log.d(DemoActivity.class.getSimpleName(), String.format("downloading ... %d.jpg", j));
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
                Thread.sleep(2000);
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
}
