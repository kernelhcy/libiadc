package com.tsoftime;

import android.app.Activity;
import android.os.Bundle;
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
        for (int i = 1; i < 258; ++i) {
            adaper.addURL("http://vm-192-168-18-243.shengyun.grandcloud.cn/mig31/" + i + ".jpg");
        }

        listView.setAdapter(adaper);
    }

    private ListView listView;
    private ImageListViewAdaper adaper;
}
