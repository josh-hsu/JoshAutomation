/*
 * Copyright (C) 2016 The Josh Tool Project
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

package com.mumu.joshautomation.screencapture;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.mumu.joshautomation.R;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PointSelectionActivity extends AppCompatActivity {
    private static final String TAG = "FGOTool";
    private static final int UI_ANIMATION_DELAY = 300;
    public static final int ZOOM_FLAG_START = 0;
    public static final int ZOOM_FLAG_MOVE = 1;
    public static final int ZOOM_FLAG_END = 2;
    private String mPngFilePath = Environment.getExternalStorageDirectory().toString() + "/select.png";
    private String mDumpFilePath = Environment.getExternalStorageDirectory().toString() + "/select.dump";
    private int mScreenSizeX, mScreenSizeY;

    private final Handler mHideHandler = new Handler();
    private JoshGameLibrary mGL;
    private String mPointInfo;

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

        mGL = JoshGameLibrary.getInstance();
        mGL.setContext(this);
        mGL.setGameOrientation(ScreenPoint.SO_Landscape);
        mGL.setScreenDimension(1080, 1920);
        mGL.getCaptureService().DumpScreenPNG(mPngFilePath);
        mGL.getCaptureService().DumpScreen(mDumpFilePath);

        mImageView.setImageBitmap(BitmapFactory.decodeFile(mPngFilePath));
        mImageView.setOnTouchListener(mPointTouchListener);
        mInfoTextView.setBackgroundColor(Color.WHITE);
        mUpZoomImageView.setVisibility(View.INVISIBLE);
        mDownZoomImageView.setVisibility(View.INVISIBLE);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenSizeX = size.x;
        mScreenSizeY = size.y;
        mGL.setScreenDimension(mScreenSizeX, mScreenSizeY);

        hide();
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

    private void controlZoomView(int flag, int x, int y) {
        switch (flag) {
            case ZOOM_FLAG_START:
                controlZoomViewUpdatePoint(x, y);
                controlZoomViewUpdateInfo(x, y);
                return;
            case ZOOM_FLAG_MOVE:
                controlZoomViewUpdatePoint(x, y);
                controlZoomViewUpdateInfo(x, y);
                return;
            case ZOOM_FLAG_END:
                return;
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
    }

    /*
     * update text view that shows where user tapped and what color is on
     * that point.
     */
    private void controlZoomViewUpdateInfo(int x, int y) {
        String kColorOnPoint;
        ScreenPoint kUserPoint = new ScreenPoint();
        kUserPoint.coord.orientation = ScreenPoint.SO_Portrait;
        kUserPoint.coord.x = x;
        kUserPoint.coord.y = y;
        mGL.getCaptureService().GetColorOnDump(kUserPoint.color, mDumpFilePath, kUserPoint.coord);
        kColorOnPoint = "0x" + Integer.toHexString(kUserPoint.color.r & 0xFF) + " "
                + Integer.toHexString(kUserPoint.color.g & 0xFF) + " "
                + Integer.toHexString(kUserPoint.color.b & 0xFF) + " "
                + Integer.toHexString(kUserPoint.color.t & 0xFF);

        mPointInfo = String.valueOf("X=" + x + ", Y=" + y + ", Color=" + kColorOnPoint);
        mInfoTextView.setText(mPointInfo);
    }
}
