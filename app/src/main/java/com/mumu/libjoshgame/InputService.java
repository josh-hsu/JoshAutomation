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

package com.mumu.libjoshgame;

import android.util.Log;

import com.mumu.libjoshgame.ScreenPoint.*;


public class InputService extends JoshGameLibrary.GLService {
    private static final String TAG = "LibJG";
    private static int mGameOrientation = ScreenPoint.SO_Landscape;
    private static int mScreenWidth = -1;
    private static int mScreenHeight = -1;
    public static final int INPUT_TYPE_TAP = 0;
    public static final int INPUT_TYPE_DOUBLE_TAP = 1;
    public static final int INPUT_TYPE_TRIPLE_TAP = 2;
    public static final int INPUT_TYPE_CONT_TAPS =3;
    public static final int INPUT_TYPE_SWIPE = 4;
    public static final int INPUT_TYPE_MAX = 5;
    public boolean mUseSu = false;
    CaptureService mCaptureService = null;

    public InputService(CaptureService cs) {
        Log.d(TAG, "InputService instance is created. \n");
        mCaptureService = cs;
    }

    public void SetScreenDimension(int w, int h) {
        mScreenWidth = w;
        mScreenHeight = h;
    }

    public void SetGameOrientation(int orientation) {
        mGameOrientation = orientation;
    }

    /*
     * Touch on screen with type
     * This function will not consider the screen orientation
     * TODO: need to find out why input binary takes a long time to execute
     */
    public int TouchOnScreen(int x, int y, int tx, int ty, int type) {
        switch (type) {
            case INPUT_TYPE_TAP:
                super.runCommand("input tap " + x + " " + y);
                break;
            case INPUT_TYPE_DOUBLE_TAP:
                super.runCommand("input tap " + x + " " + y);
                //usleep(100 * 1000);
                super.runCommand("input tap " + x + " " + y);
                break;
            case INPUT_TYPE_TRIPLE_TAP:
                super.runCommand("input tap " + x + " " + y);
                //usleep(100 * 1000);
                super.runCommand("input tap " + x + " " + y);
                //usleep(100 * 1000);
                super.runCommand("input tap " + x + " " + y);
                break;
            case INPUT_TYPE_CONT_TAPS:
                break;
            case INPUT_TYPE_SWIPE:
                super.runCommand("input swipe " + x + " " + y + " " + tx + " " + ty);
                break;
            default:
                Log.e(TAG, "TouchOnScreen: type " + type + "is invalid.");
        }
        return 0;
    }

    public int TapOnScreen(ScreenCoord coord1)
    {
        if (mGameOrientation != coord1.orientation)
            TouchOnScreen(coord1.y, mScreenWidth - coord1.x, 0, 0, INPUT_TYPE_TAP);
        else
            TouchOnScreen(coord1.x, coord1.y, 0, 0, INPUT_TYPE_TAP);

        return 0;
    }

    /*
     * Tap on screen amount of times until the color on the screen is not in point->color
     */
    public int TapOnScreenUntilColorChanged(ScreenPoint point, int interval, int retry, Thread kThread)
    {
        if (point == null) {
            Log.e(TAG, "null point");
            return -1;
        }

        while(retry-- > 0) {
            TapOnScreen(point.coord);
            try {
                kThread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mCaptureService.ColorIs(point)) {
                Log.d(TAG, "color didn't change, try again");
            } else {
                Log.d(TAG, "color changed. exiting..");
                return 0;
            }
        }

        return -1;
    }

    public int TapOnScreenUntilColorChangedTo(ScreenPoint point,
                                       ScreenPoint to, int interval, int retry, Thread kThread)
    {
        if ((point == null) || (to == null)) {
            Log.e(TAG, "InputService: null points.\n");
            return -1;
        }

        while(retry-- > 0) {
            TapOnScreen(point.coord);
            try {
                kThread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mCaptureService.ColorIs(to)) {
                Log.d(TAG, "color changed to specific point. exiting..");
            } else {
                Log.d(TAG, "color didn't change to specific point, try again");
                return 0;
            }
        }

        return -1;
    }

    public int TouchOnScreenAsync(int x, int y, int tx, int ty, int type) {
        //TODO: implement required
        TouchOnScreen(x, y, tx, ty, type);
        return 0;
    }

    public void ConfigTouchScreen(boolean enable) {
        if (enable)
            super.runCommand("echo 1 > /proc/touch-enable");
        else
            super.runCommand("echo 0 > /proc/touch-enable");
    }

    public void ConfigGyroSensor(boolean enable) {
        if (enable)
            super.runCommand("echo 1 > /proc/sensor-enable");
        else
            super.runCommand("echo 0 > /proc/sensor-enable");
    }

    public void SetBacklightLow() {
        super.runCommand("echo 0 > /sys/class/leds/lcd-backlight/brightness");
    }

    public void SetBacklight(int bl) {
        super.runCommand("echo " + bl + " > /sys/class/leds/lcd-backlight/brightness");
    }
}
