package com.tsoftime;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class DemoActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ImageManager.init(getApplicationContext());
        imageManager = ImageManager.instance();

        listView = (ListView) findViewById(R.id.listview);
        adaper = new ImageListViewAdaper(getApplicationContext(), listView);

        //http://vm-192-168-18-243.shengyun.grandcloud.cn/mig31/
        for(int i = 0; i < 90; ++i){
            adaper.addURL("http://vm-192-168-18-243.shengyun.grandcloud.cn/mig31/" + i + ".jpg");
        }

        listView.setAdapter(adaper);
    }

    private ImageManager imageManager;

    private ListView listView;
    private ImageListViewAdaper adaper;
}
