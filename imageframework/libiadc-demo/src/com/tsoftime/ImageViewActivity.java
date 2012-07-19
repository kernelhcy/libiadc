package com.tsoftime;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

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
        iv = (ImageView) findViewById(R.id.image_view);
        tv = (TextView) findViewById(R.id.text_view);

        String url = getIntent().getStringExtra("url");
        urls = getIntent().getExtras();
        index = getIntent().getIntExtra("index", 0);

        ImageManager imageManager = ImageManager.instance();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("url", url);
        params.put("index", index);
        imageManager.dispatchImageTask(url, params, callBack);


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String url;
        ImageManager imageManager = ImageManager.instance();
        switch (keyCode)
        {
            case 21:    //left
                finish();
                break;
            case 22:    //right
                url = (String) urls.get(String.valueOf(++index));
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("url", url);
                params.put("index", index);
                imageManager.dispatchImageTask(url, params, callBack);
                break;
            case 19:    //up
                url = (String) urls.get(String.valueOf(--index));
                params = new HashMap<String, Object>();
                params.put("url", url);
                params.put("index", index);
                imageManager.dispatchImageTask(url, params, callBack);
                break;
            case 20:    //down
                url = (String) urls.get(String.valueOf(++index));
                params = new HashMap<String, Object>();
                params.put("url", url);
                params.put("index", index);
                imageManager.dispatchImageTask(url, params, callBack);
                break;
        }
        return false;
    }

    private Bundle urls;
    private int index;
    private ImageView iv;
    private TextView tv;
    private ImageTaskCallBack callBack = new ImageTaskCallBack()
    {
        @Override
        public void onGettingProgress(int total, int hasGotten, HashMap<String, Object> params)
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void onDownloadingDone(int status, Bitmap bmp, HashMap<String, Object> params)
        {
            if (status == ImageManager.SUCCESS){
                iv.setImageBitmap(bmp);
                String url = (String) params.get("url");
                tv.setText("" + params.get("index") + ": " + url);
            }
        }
    };
}