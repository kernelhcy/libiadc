package com.tsoftime;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
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

        listView = (ListView) findViewById(R.id.listview);
        adaper = new ImageListViewAdaper(getApplicationContext(), listView);

        //http://vm-192-168-18-243.shengyun.grandcloud.cn/mig31/
        final Bundle urls = new Bundle();
        for (int i = 1; i < 258; ++i) {
            String url = "http://vm-192-168-18-243.shengyun.grandcloud.cn/mig31/" + i + ".jpg";
            adaper.addURL(url);
            urls.putString(String.format("%d", i), url);
        }

        listView.setAdapter(adaper);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                String url = (String) adaper.getItem(i);
                Intent intent = new Intent(DemoActivity.this, ImageViewActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("index", i);
                intent.putExtras(urls);
                startActivity(intent);
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
