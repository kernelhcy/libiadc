package com.tsoftime.barcodelib.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.tsoftime.barcodelib.Test;

public class MainActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TextView tv = (TextView) findViewById(R.id.tv);
        Test test = new Test();
        tv.setText(test.test());
    }
}
