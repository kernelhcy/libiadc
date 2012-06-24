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
        adaper = new ImageListViewAdaper(getApplicationContext());

        adaper.addURL("http://www.google.com/1");
        adaper.addURL("http://www.google.com/2");
        adaper.addURL("http://www.google.com/3");
        adaper.addURL("http://www.google.com/4");
        adaper.addURL("http://www.google.com/5");

        listView.setAdapter(adaper);
    }

    private ImageManager imageManager;

    private ListView listView;
    private ImageListViewAdaper adaper;
}
