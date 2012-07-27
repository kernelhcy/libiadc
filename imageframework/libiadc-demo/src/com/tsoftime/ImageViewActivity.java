package com.tsoftime;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * User: huangcongyu2006
 * Date: 12-7-10 PM5:30
 */
public class ImageViewActivity extends Activity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view_activity);

        mUrls = getIntent().getStringArrayListExtra("urls");
        String url1 = getIntent().getStringExtra("url");
        index = mUrls.indexOf(url1);

        mViewPager = (ViewPager) findViewById(R.id.scroll_layout);
        Log.d(ImageViewActivity.class.getSimpleName(), String.format("url count %d", mUrls.size()));
        mAdapter = new ViewPagerAdapter(getApplicationContext(), mViewPager);

        for(String url : mUrls) {
            mAdapter.addUrl(url);
        }
        mViewPager.setAdapter(mAdapter);
        mViewPager.setToScreen(index);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(R.anim.zoomout, R.anim.zoomin);
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    private ArrayList<String> mUrls;
    private ViewPager mViewPager;
    private ViewPagerAdapter mAdapter;
    private int index;

}