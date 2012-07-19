package com.tsoftime;

import android.app.Activity;
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
        ImageManager.instance().setDownloadThreadNumber(2);

        listView = (ListView) findViewById(R.id.listview);
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
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        Log.d("DemoActivity", String.format("keycode %d", keyCode));
        int position = listView.getFirstVisiblePosition();
        int count = listView.getChildCount();
        switch (keyCode)
        {
            case 21:    //left
                position -= count;
                scrollToPosition(position);
                return true;
            case 22:    //right
                position += count;
                scrollToPosition(position);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void scrollToPosition(int position)
    {
        if (position < 0) position = 0;
        if (position > listView.getCount()) position = listView.getCount();
        Log.d("DemoActivity", String.format("Scroll to %d", position));
        listView.smoothScrollToPosition(position);
        listView.setSelection(position);
    }

    private ListView listView;
    private ImageListViewAdaper adaper;
}
