/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tsoftime.barcodelib;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.tsoftime.barcodelib.camera.CameraManager;
import com.tsoftime.barcodelib.decode.IDCode;
import com.tsoftime.barcodelib.decode.ViewfinderView;

import java.io.IOException;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * 用户扫描解码二维码和一维码。
 * 扫描成功之后，返回的结果为Activity.RESULT_OK，扫描的结果放在intent中，其键为CaptureActivity.BARCODE_CODE。
 * 如果扫描失败，或者用户停止扫描，返回的结果是Activity.RESULT_CANCELED。
 *
 * 在AndroidManifest.xml文件中添加如下部分：
 * 在<Application></Application>部分中添加：
 * <activity android:name="com.tsoftime.barcodelib.CaptureActivity"
 *           android:screenOrientation="landscape"
 *           android:stateNotNeeded="true"
 *           android:configChanges="orientation|keyboardHidden"
 *           android:theme="@android:style/Theme.NoTitleBar.Fullscreen"
 *           android:windowSoftInputMode="stateAlwaysHidden"/>
 * 添加权限：
 *      <uses-permission android:name="android.permission.CAMERA"/>
 *      <uses-permission android:name="android.permission.FLASHLIGHT"/>
 *
 * 调用这个activityj进行解码：
 *          Intent intent = new Intent(SomeActivity.this, CaptureActivity.class);
 *          startActivityForResult(intent, CaptureActivity.DECODE_REQUEST_CODE);
 *
 * 在onActivityResult中获取解码结果：
 *      @Override
 *      public void onActivityResult(int requestCode, int resultCode, Intent intent)
 *      {
 *          if (resultCode == RESULT_OK) {
 *              switch (requestCode) {
 *              case CaptureActivity.DECODE_REQUEST_CODE:
 *                  String code = intent.getStringExtra(CaptureActivity.BARCODE_CODE);
 *                  String type = intent.getStringExtra(CaptureActivity.BARCODE_TYPE);
 *                  // do something on the result
 *                  break;
 *              // other code...
 *              }
 *          }
 *          // other code...
 *      }
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback
{
    public static final String BARCODE_CODE = "barcode_result";
    public static final String BARCODE_TYPE = "barcode_type";
    public static Bitmap resultBmp = null;

    public static final int DECODE_REQUEST_CODE = 1;

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private Result savedResultToShow;
    private ViewfinderView viewfinderView;
    private TextView statusView;
    private View resultView;
    private boolean hasSurface;

    public Handler getHandler()
    {
        return handler;
    }

    public CameraManager getCameraManager()
    {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.capture);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.capture);

        hasSurface = false;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);

        resultView = findViewById(R.id.result_view);
        statusView = (TextView) findViewById(R.id.status_view);

        handler = null;

        resetStatusView();

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }


    @Override
    protected void onPause()
    {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // Handle these events so they don't launch the Camera app
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result)
    {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler, IDCode.decode_succeeded, savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {

    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult The contents of the barcode.
     * @param barcode   A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode)
    {
        if (barcode != null) {
            drawResultPoints(barcode, rawResult);
        }
        Intent intent = getIntent();
        intent.putExtra(BARCODE_CODE, rawResult.getText());
        intent.putExtra(BARCODE_TYPE, rawResult.getBarcodeFormat().toString());
        resultBmp = barcode;
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
     *
     * @param barcode   A bitmap of the captured image.
     * @param rawResult The decoded results which contains the points to draw.
     */
    private void drawResultPoints(Bitmap barcode, Result rawResult)
    {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.result_image_border));
            paint.setStrokeWidth(3.0f);
            paint.setStyle(Paint.Style.STROKE);
            Rect border = new Rect(2, 2, barcode.getWidth() - 2, barcode.getHeight() - 2);
            canvas.drawRect(border, paint);

            paint.setColor(getResources().getColor(R.color.result_points));
            if (points.length == 2) {
                paint.setStrokeWidth(4.0f);
                drawLine(canvas, paint, points[0], points[1]);
            } else if (points.length == 4 &&
                (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
                    rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                // Hacky special case -- draw two lines, for the barcode and metadata
                drawLine(canvas, paint, points[0], points[1]);
                drawLine(canvas, paint, points[2], points[3]);
            } else {
                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    canvas.drawPoint(point.getX(), point.getY(), paint);
                }
            }
        }
    }

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b)
    {
        canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
    }

    private void initCamera(SurfaceHolder surfaceHolder)
    {
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, null, null, cameraManager);
            }
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }


    private void resetStatusView()
    {
        resultView.setVisibility(View.GONE);
        statusView.setText(R.string.msg_default_status);
        statusView.setVisibility(View.VISIBLE);
        viewfinderView.setVisibility(View.VISIBLE);
    }

    public void drawViewfinder()
    {
        viewfinderView.drawViewfinder();
    }

    public ViewfinderView getViewfinderView()
    {
        return viewfinderView;
    }
}
