/*
 * Copyright (C) 2020 The Josh Tool Project
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

package com.mumu.android.joshautomation.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.mumu.android.joshautomation.R;
import com.mumu.android.joshautomation.content.AppPreferenceValue;
import com.mumu.android.joshautomation.content.AppSharedObject;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenPoint;

public class PointSelectionActivity extends AppCompatActivity {
    private static final String TAG = "PointSelect";
    private static final int UI_ANIMATION_DELAY = 300;
    public static final int ZOOM_FLAG_START = 0;
    public static final int ZOOM_FLAG_MOVE = 1;
    public static final int ZOOM_FLAG_END = 2;
    private String mPngFilePath = Environment.getExternalStorageDirectory().toString() + "/select.dump.png";
    private String mDumpFilePath = Environment.getExternalStorageDirectory().toString() + "/select.dump";
    private int mScreenSizeX, mScreenSizeY;

    private final Handler mHideHandler = new Handler();
    private GameLibrary20 mGL;
    private String mPointInfo;
    private ScreenPoint mPointTouched;
    private int mSlot;

    /* View declaration */
    private WindowManager mWindowManager;
    private View mContentView;
    private ImageView mImageView;
    private ImageView mUpZoomImageView, mDownZoomImageView;
    private TextView mInfoTextView;

    /* Runnable declaration */
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_point_selection);

        mContentView = findViewById(R.id.idFullScreenImageView);
        mImageView = (ImageView) mContentView;
        mUpZoomImageView = (ImageView) findViewById(R.id.idZoomViewTop);
        mDownZoomImageView = (ImageView) findViewById(R.id.idZoomViewBottom);
        mInfoTextView = (TextView) findViewById(R.id.idInfoTextView);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mSlot = getIntent().getIntExtra("slot", 0);
        mGL = AppSharedObject.getInstance().getGL20();

        Bitmap pngFileMap = BitmapFactory.decodeFile(mPngFilePath);

        if (pngFileMap == null) {
            new AlertDialog.Builder(this)
                    .setTitle("截圖錯誤")
                    .setMessage("您的裝置軟體未經過客製化，無法使用此功能。請考慮按照ReadMe，建立您自己的軟體。(這可能會失去保固)")
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        } else {
            int w = pngFileMap.getWidth();
            int h = pngFileMap.getHeight();
            if (pngFileMap.getWidth() > pngFileMap.getHeight()) {
                Matrix mtx = new Matrix();
                mtx.postRotate(90);
                pngFileMap = Bitmap.createBitmap(pngFileMap, 0, 0, w, h, mtx, true);
            }
            mImageView.setImageBitmap(pngFileMap);
            mImageView.setOnTouchListener(mPointTouchListener);
            mInfoTextView.setBackgroundColor(Color.WHITE);
            mUpZoomImageView.setVisibility(View.INVISIBLE);
            mDownZoomImageView.setVisibility(View.INVISIBLE);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mScreenSizeX = size.x;
            mScreenSizeY = size.y;
            mGL.setScreenResolution(mScreenSizeX, mScreenSizeY);

            hide();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private View.OnTouchListener mPointTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int touchX = (int)event.getRawX();
            int touchY = (int)event.getRawY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    controlZoomView(ZOOM_FLAG_START, touchX, touchY);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    controlZoomView(ZOOM_FLAG_MOVE, touchX, touchY);
                    return true;
                case MotionEvent.ACTION_POINTER_UP:
                    controlZoomView(ZOOM_FLAG_END, touchX, touchY);
                    return true;
            }
            return false;
        }
    };

    private void getUserTouchColor(int x, int y) {
        ScreenPoint kUserPoint = new ScreenPoint();
        kUserPoint.coord.orientation = ScreenPoint.SO_Portrait;
        kUserPoint.coord.x = x;
        kUserPoint.coord.y = y;
        try {
            kUserPoint.color = mGL.getColorOnScreen(mSlot, kUserPoint.coord, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPointTouched = kUserPoint;
    }

    private Bitmap prepareZoomedViewCanvas(int w, int h) {
        ShapeDrawable drawable;
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(bmp);

        drawable = new ShapeDrawable(new RectShape());
        drawable.getPaint().setColor(mPointTouched.getColor());
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);

        return bmp;
    }

    private void controlZoomView(int flag, int x, int y) {
        switch (flag) {
            case ZOOM_FLAG_START:
                getUserTouchColor(x, y);
                controlZoomViewUpdatePoint(x, y);
                controlZoomViewUpdateInfo(x, y);
                break;
            case ZOOM_FLAG_MOVE:
                getUserTouchColor(x, y);
                controlZoomViewUpdatePoint(x, y);
                controlZoomViewUpdateInfo(x, y);
                break;
            case ZOOM_FLAG_END:
                break;
        }
    }

    /*
     * make zoom view show what user tapped in bigger zoom image
     */
    private void controlZoomViewUpdatePoint(int x, int y) {
        /* set which zoom view should be visible */
        if (y > mScreenSizeY / 2) {
            mDownZoomImageView.setVisibility(View.INVISIBLE);
            mUpZoomImageView.setVisibility(View.VISIBLE);
        } else {
            mDownZoomImageView.setVisibility(View.VISIBLE);
            mUpZoomImageView.setVisibility(View.INVISIBLE);
        }

        Bitmap bmp = prepareZoomedViewCanvas(140, 140);
        mDownZoomImageView.setImageBitmap(bmp);
        mUpZoomImageView.setImageBitmap(bmp);
    }

    /*
     * update text view that shows where user tapped and what color is on
     * that point.
     */
    private void controlZoomViewUpdateInfo(int x, int y) {
        String kColorOnPoint;

        if (mPointTouched == null) {
            Log.e(TAG, "Touch point is null.");
        } else {
            kColorOnPoint = "R:0x" + Integer.toHexString(mPointTouched.color.r & 0xFF).toUpperCase() +
                    "  G:0x" + Integer.toHexString(mPointTouched.color.g & 0xFF).toUpperCase() +
                    "  B:0x" + Integer.toHexString(mPointTouched.color.b & 0xFF).toUpperCase() +
                    "  A:0x"+ Integer.toHexString(mPointTouched.color.t & 0xFF).toUpperCase();

            if (AppPreferenceValue.getInstance().getPrefs().
                    getBoolean("stringFormattedPointEnable", false)) {
                mPointInfo = "Format: " + mPointTouched.getFormattedString();
            } else {
                mPointInfo = String.valueOf("X=" + x + "   Y=" + y + "\n" + kColorOnPoint);
            }

            mInfoTextView.setText(mPointInfo);
        }
    }
}