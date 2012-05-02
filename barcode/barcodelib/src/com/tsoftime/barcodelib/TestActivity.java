package com.tsoftime.barcodelib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * User: huangcongyu2006
 * Date: 12-5-2 PM5:10
 */
public class TestActivity extends Activity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        btn = (Button) findViewById(R.id.barcode_btn);
        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(TestActivity.this, CaptureActivity.class);
                startActivityForResult(intent, CaptureActivity.DECODE_REQUEST_CODE);
            }
        });
        resultTV = (TextView) findViewById(R.id.barcode_result);
        typeTV = (TextView) findViewById(R.id.barcode_type);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CaptureActivity.DECODE_REQUEST_CODE:
                    String code = intent.getStringExtra(CaptureActivity.BARCODE_CODE);
                    String type = intent.getStringExtra(CaptureActivity.BARCODE_TYPE);
                    resultTV.setText(code);
                    typeTV.setText(type);
                    break;
            }
        }
    }

    private TextView resultTV, typeTV;
    private Button btn;
}