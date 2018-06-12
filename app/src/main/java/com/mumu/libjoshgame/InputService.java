/*
 * Copyright (C) 2018 The Josh Tool Project
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

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

public class InputService {
    private static final String TAG = "LibGame";
    private int mGameOrientation = ScreenPoint.SO_Landscape;
    private int mRandomTouchShift = 0;
    private int mScreenWidth = -1;
    private int mScreenHeight = -1;
    private int mScreenXOffset = 0;
    private int mScreenYOffset = 0;
    private CaptureService mCaptureService = null;
    private Context mContext = null;
    private Cmd mCmd = null;

    private static final int INPUT_TYPE_TAP = 0;
    private static final int INPUT_TYPE_DOUBLE_TAP = 1;
    private static final int INPUT_TYPE_TRIPLE_TAP = 2;
    private static final int INPUT_TYPE_CONT_TAPS =3;
    private static final int INPUT_TYPE_SWIPE = 4;


    InputService(CaptureService cs) {
        Log.d(TAG, "InputService instance is created. \n");
        mCaptureService = cs;
    }

    void setScreenDimension(int w, int h) {
        mScreenWidth = w;
        mScreenHeight = h;
    }

    void setContext(Context context) {
        mContext = context;
    }

    void setGameOrientation(int orientation) {
        mGameOrientation = orientation;
    }

    /*
     * setTouchShift (added in 1.22)
     * this function can be called by JoshGameLibrary only
     * any touch point will be slightly shift by  -ran/2 ~ ran/2 in both x and y
     */
    void setTouchShift(int ran) {
        mRandomTouchShift = ran;
    }


    /*
     * setScreenOffset (added in 1.34)
     * this function can be called by JoshGameLibrary only
     * shift an amount of offset for every point input
     * this will apply touch shift as well
     */
    void setScreenOffset(int xOffset, int yOffset) {
        mScreenXOffset = xOffset;
        mScreenYOffset = yOffset;
    }

    /*
     * setCmd (added in 1.50)
     * now mCmd is no longer static, make sure every Service has mCmd of GameLibrary
     */
    void setCmd(Cmd cmd) {
        mCmd = cmd;
    }

    private void runCommand(String cmd) {
        if (mCmd != null) {
            mCmd.runCommand(cmd);
        } else {
            Log.d(TAG, "Command service is not initialized");
        }
    }

    /*
     * Touch on screen with type (added in 1.0)
     * This function will not consider the screen orientation
     * apply random shift in touch point (added in 1.23)
     */
    private void touchOnScreen(int x, int y, int tx, int ty, int type) {
        int x_shift = (int) (Math.random() * mRandomTouchShift) - mRandomTouchShift/2;
        int y_shift = (int) (Math.random() * mRandomTouchShift) - mRandomTouchShift/2;

        x = x + x_shift;
        y = y + y_shift;

        if (mScreenHeight > 0 && y > (mGameOrientation == ScreenPoint.SO_Portrait ? mScreenHeight : mScreenWidth))
            y = mScreenHeight;
        else if (y < 0)
            y = 0;

        if (mScreenWidth > 0 && x > (mGameOrientation == ScreenPoint.SO_Landscape ? mScreenHeight : mScreenWidth))
            x = mScreenWidth;
        else if (x < 0)
            x = 0;

        switch (type) {
            case INPUT_TYPE_TAP:
                runCommand("input tap " + x + " " + y);
                break;
            case INPUT_TYPE_DOUBLE_TAP:
                runCommand("input tap " + x + " " + y);
                runCommand("input tap " + x + " " + y);
                break;
            case INPUT_TYPE_TRIPLE_TAP:
                runCommand("input tap " + x + " " + y);
                runCommand("input tap " + x + " " + y);
                runCommand("input tap " + x + " " + y);
                break;
            case INPUT_TYPE_CONT_TAPS:
                break;
            case INPUT_TYPE_SWIPE:
                runCommand("input swipe " + x + " " + y + " " + tx + " " + ty);
                break;
            default:
                Log.e(TAG, "touchOnScreen: type " + type + "is invalid.");
        }
    }

    private ScreenCoord getCalculatedOffsetCoord(ScreenCoord coord1) {
        ScreenCoord coord;

        if (coord1.orientation == ScreenPoint.SO_Portrait) {
            coord = new ScreenCoord(coord1.x + mScreenXOffset, coord1.y + mScreenYOffset, coord1.orientation);
        } else {
            coord = new ScreenCoord(coord1.x + mScreenYOffset, coord1.y + mScreenXOffset, coord1.orientation);
        }

        return coord;
    }

    public void tapOnScreen(ScreenCoord coord1) {
        ScreenCoord coord = getCalculatedOffsetCoord(coord1);

        if (mGameOrientation != coord.orientation)
            touchOnScreen(coord.y, mScreenWidth - coord.x, 0, 0, INPUT_TYPE_TAP);
        else
            touchOnScreen(coord.x, coord.y, 0, 0, INPUT_TYPE_TAP);
    }

    public void swipeOnScreen(ScreenCoord start, ScreenCoord end) {
        ScreenCoord coord_start = getCalculatedOffsetCoord(start);
        ScreenCoord coord_end = getCalculatedOffsetCoord(end);

        if (mGameOrientation != start.orientation)
            touchOnScreen(coord_start.y, mScreenWidth - coord_start.x, coord_end.y, mScreenWidth - coord_end.x, INPUT_TYPE_SWIPE);
        else
            touchOnScreen(coord_start.x, coord_start.y, coord_end.x, coord_end.y, INPUT_TYPE_SWIPE);

    }

    /*
     * Tap on screen amount of times until the color on the screen is not in point->color
     */
    public int tapOnScreenUntilColorChanged(ScreenPoint point, int interval, int retry) throws InterruptedException {
        if (point == null) {
            Log.e(TAG, "null point");
            return -1;
        }

        while(retry-- > 0) {
            tapOnScreen(point.coord);
            Thread.sleep(interval);
            if (!mCaptureService.colorIs(point)) {
                Log.d(TAG, "color changed. exiting..");
                return 0;
            }
        }

        return -1;
    }

    public int tapOnScreenUntilColorChangedTo(ScreenPoint point,
                                       ScreenPoint to, int interval, int retry) throws InterruptedException {
        if ((point == null) || (to == null)) {
            Log.e(TAG, "InputService: null points.\n");
            return -1;
        }

        while(retry-- > 0) {
            tapOnScreen(point.coord);
            Thread.sleep(interval);
            if (mCaptureService.colorIs(to)) {
                Log.d(TAG, "color changed to specific point. exiting..");
                return 0;
            }
        }

        return -1;
    }

    public int tapOnScreenUntilColorChangedTo(ScreenCoord point,
                                              ScreenPoint to, int interval, int retry) throws InterruptedException {
        if ((point == null) || (to == null)) {
            Log.e(TAG, "InputService: null points.\n");
            return -1;
        }

        while(retry-- > 0) {
            tapOnScreen(point);
            Thread.sleep(interval);
            if (mCaptureService.colorIs(to)) {
                Log.d(TAG, "color changed to specific point. exiting..");
                return 0;
            }
        }

        return -1;
    }

    public void inputText(String text) {
        runCommand("input text " + text);
    }


    /*
     * playNotificationSound (added in 1.32)
     * Play default notification sound, it needs the JoshGameLibrary
     * initialized with setContext
     */
    public void playNotificationSound() {
        if (mContext == null) {
            Log.w(TAG, "Context has not been assigned, aborting play sound.");
        } else {
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(mContext, notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setBacklightLow() {
        setBacklight(0);
    }

    public void setBacklight(int bl) {
        runCommand("echo " + bl + " > /sys/class/leds/lcd-backlight/brightness");
    }

    public String toString() {
        return "InputService:\nScreen Dimension: w = " + mScreenWidth + " h=" + mScreenHeight +
                "\nGame Orientation: " + mGameOrientation;
    }
}
