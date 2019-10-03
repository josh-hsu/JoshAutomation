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

package com.mumu.android.joshautomation.service;

import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class HeadIconView {
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;
    private OnTapListener mOnTapListener = null;
    private OnMoveListener mOnMoveListener = null;
    private View mView;
    private final HeadIconView mSelf = this;

    private int mOffsetX, mOffsetY;
    private boolean mIsMovable = true;

    private static final long mTouchLongPressThreshold = 1500;
    private static final int mTouchTapThreshold = 200;
    private static final int mInitialPositionX = 20;
    private static final int mInitialPositionY = 300;

    public static final int VISIBLE = View.VISIBLE;
    public static final int INVISIBLE = View.INVISIBLE;

    public HeadIconView(View view, WindowManager wm, int offsetX, int offsetY) {
        if (view == null)
            return;

        mView = view;
        mWindowManager = wm;
        mOffsetX = offsetX;
        mOffsetY = offsetY;

        mLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        mLayoutParams.gravity = Gravity.TOP | Gravity.START;
        mLayoutParams.x = mInitialPositionX + mOffsetX;
        mLayoutParams.y = mInitialPositionY + mOffsetY;

        mView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private long touchDownTime;
            private long touchUpTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchDownTime = System.currentTimeMillis();
                        initialX = mLayoutParams.x;
                        initialY = mLayoutParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        touchUpTime = System.currentTimeMillis();
                        long elapsedTime = touchUpTime - touchDownTime;
                        if (elapsedTime < mTouchTapThreshold) {
                            if (mOnTapListener != null)
                                mOnTapListener.onTap(mView);
                        } else if (elapsedTime > mTouchLongPressThreshold) {
                            if (mOnTapListener != null)
                                mOnTapListener.onLongPress(mView);
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (mIsMovable && mOnMoveListener != null) {
                            mOnMoveListener.onMove(mSelf, initialX, initialY, initialTouchX, initialTouchY, event);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    public void moveIconDefault(int initialX, int initialY, float initialTouchX, float initialTouchY, MotionEvent event) {
        mLayoutParams.x = initialX + (int) (event.getRawX() - initialTouchX) + mOffsetX;
        mLayoutParams.y = initialY + (int) (event.getRawY() - initialTouchY) + mOffsetY;
        mWindowManager.updateViewLayout(mView, mLayoutParams);
    }

    /*
     * View getter
     */
    public View getView() {
        return mView;
    }

    public ImageView getImageView() {
        return (ImageView) mView;
    }

    public TextView getTextView() {
        return (TextView) mView;
    }

    /*
     * View control
     */
    public void addView() {
        mWindowManager.addView(mView, mLayoutParams);
    }

    public void setVisibility(int visibility) {
        mView.setVisibility(visibility);
    }

    public void setGravity(int gravity, boolean changeNow) {
        mLayoutParams.gravity = gravity;
        if (changeNow)
            mWindowManager.updateViewLayout(mView, mLayoutParams);
    }

    public void removeView() {
        if (mView != null && mWindowManager != null)
            mWindowManager.removeView(mView);
    }

    /*
     * Motion detection listener
     */
    public void setOnTapListener (OnTapListener o) {
        mOnTapListener = o;
    }

    public void setOnMoveListener (OnMoveListener o) {
        mOnMoveListener = o;
    }

    public interface OnTapListener {
        void onTap(View view);
        void onLongPress(View view);
    }

    public interface OnMoveListener {
        void onMove(HeadIconView view, int initialX, int initialY, float initialTouchX, float initialTouchY, MotionEvent event);
    }
}
