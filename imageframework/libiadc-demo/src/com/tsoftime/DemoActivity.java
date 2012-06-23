package com.tsoftime;

import android.app.Activity;
import android.os.Bundle;
import com.tsoftime.ImageManager;

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
    }

    private ImageManager imageManager;
}
