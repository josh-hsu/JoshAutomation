/*
 * Copyright (C) 2017 The Josh Tool Project
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

import android.os.Environment;
import android.util.Log;
import com.mumu.libjoshgame.ScreenPoint.*;

import java.io.RandomAccessFile;

public class CaptureService extends JoshGameLibrary.GLService {
    private final String TAG = "LibJG";
    private final String mInternalDumpFile = Environment.getExternalStorageDirectory().toString() + "/internal.dump";
    private int mScreenWidth = -1;
    private int mScreenHeight = -1;
    private int mCurrentGameOrientation = ScreenPoint.SO_Portrait;
    private int mAmbiguousRange = 0x05;
    
    CaptureService() {
        Log.d(TAG, "CaptureService has been created.");
    }

    void setScreenDimension(int w, int h) {
        mScreenHeight = h;
        mScreenWidth = w;
    }

    void setScreenOrientation(int o) {
        mCurrentGameOrientation = o;
    }

    void setAmbiguousRange(int range) {
        mAmbiguousRange = range;
    }

    public void dumpScreenPNG(String filename) {
        super.runCommand("screencap -p " + filename);
    }

    public void dumpScreen(String filename) {
        super.runCommand("screencap " + filename);
    }

    private void dumpScreen() {
        super.runCommand("screencap " + mInternalDumpFile);
        super.runCommand("chmod 666 " + mInternalDumpFile);
    }

    /*
     * getColorOnDumpInternal
     * This is used only insides this class
     */
    private void getColorOnDumpInternal(ScreenColor sc, ScreenCoord coord) {
        getColorOnDump(sc, mInternalDumpFile, coord);
    }

    /*
     * getColorOnDump
     * sc: ScreenColor to be saved into
     * filename: dump file path
     * coord: ScreenCoord to be used
     */
    public void getColorOnDump(ScreenColor sc, String filename, ScreenCoord coord) {
        RandomAccessFile dumpFile;
        int offset = 0;
        int bpp = 4;
        byte[] colorInfo = new byte[4];

        //if Android version is 7.0 or higher, the dump orientation will be obeyed device
        if (mCurrentGameOrientation == ScreenPoint.SO_Portrait) {
            if (coord.orientation == ScreenPoint.SO_Portrait)
                offset = (mScreenWidth * coord.y + coord.x) * bpp;
            else if (coord.orientation == ScreenPoint.SO_Landscape)
                offset = (mScreenWidth * coord.x + (mScreenWidth - coord.y)) * bpp;
        } else {
            if (coord.orientation == ScreenPoint.SO_Portrait) {
                offset = (mScreenHeight * (mScreenWidth - coord.x) + coord.y) * bpp;
            } else if (coord.orientation == ScreenPoint.SO_Landscape) {
                offset = (mScreenHeight * coord.y + coord.x) * bpp;
            }
        }

        try {
            dumpFile = new RandomAccessFile(filename, "rw");
            dumpFile.seek(offset);
            dumpFile.read(colorInfo);
            sc.r = colorInfo[0];
            sc.g = colorInfo[1];
            sc.b = colorInfo[2];
            sc.t = colorInfo[3];
            dumpFile.close();
        } catch (Exception e) {
            Log.d(TAG, "File opened failed." + e.toString());
        }
    }

    /*
     * getColorOnScreen
     * sc: ScreenColor to be saved
     * coord: ScreenCoord to be used to get color
     */
    public void getColorOnScreen(ScreenColor sc, ScreenCoord coord) {
        dumpScreen();
        getColorOnDumpInternal(sc, coord);
    }

    /*
     * waitOnColor
     * sc: ScreenColor used to be compared
     * coord: ScreenCoord used to get color
     * threshold: Timeout, 1 threshold equals to 100 ms
     * kThread: Executing thread of your task
     */
    public int waitOnColor(ScreenColor sc, ScreenCoord coord, int threshold, Thread kThread) {
        ScreenPoint currentPoint = new ScreenPoint();
        Log.d(TAG, "now busy waiting for ( " + coord.x  + "," + coord.y + ") turn into 0x"
                + Integer.toHexString(sc.r & 0xFF) + Integer.toHexString(sc.g & 0xFF)
                + Integer.toHexString(sc.b & 0xFF));

        while(threshold-- > 0) {
            getColorOnScreen(currentPoint.color, coord);
            if (colorCompare(sc, currentPoint.color)) {
                Log.d(TAG, "CaptureService: Matched!");
                return 0;
            } else {
                try {
                    kThread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        return -1;
    }

    public int waitOnColor(ScreenPoint sp, int threshold, Thread kThread) {
        return waitOnColor(sp.color, sp.coord, threshold, kThread);
    }

    public int waitOnColorNotEqual(ScreenColor sc, ScreenCoord coord, int threshold, Thread kThread) {
        ScreenPoint currentPoint = new ScreenPoint();
        Log.d(TAG, "now busy waiting for ( " + coord.x  + "," + coord.y + ") is not 0x"
                + Integer.toHexString(sc.r & 0xFF) + Integer.toHexString(sc.g & 0xFF)
                + Integer.toHexString(sc.b & 0xFF));

        while(threshold-- > 0) {
            getColorOnScreen(currentPoint.color, coord);
            if (!colorCompare(sc, currentPoint.color)) {
                Log.d(TAG, "CaptureService: Matched!");
                return 0;
            } else {
                try {
                    kThread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return -1;
    }

    public boolean colorIs(ScreenPoint point) {
        if (point == null) {
            Log.w(TAG, "Point is null");
            return false;
        }
        ScreenPoint currentPoint = new ScreenPoint();
        getColorOnScreen(currentPoint.color, point.coord);
        return colorCompare(currentPoint.color, point.color);
    }

    public boolean colorCompare(ScreenColor src, ScreenColor dest) {
        Log.d(TAG, "Compare source: " + src.toString() + " to dest: " + dest.toString());
        return colorWithinRange(src.r, dest.r, mAmbiguousRange) &&
                colorWithinRange(src.b, dest.b, mAmbiguousRange) &&
                colorWithinRange(src.g, dest.g, mAmbiguousRange);
    }

    private boolean colorWithinRange(byte a, byte b, int range) {
        int src = (int) a;
        int dst = (int) b;
        int upperBound = src + range;
        int lowerBound = src - range;

        if (upperBound > 0xff)
            upperBound = 0xff;

        if (lowerBound < 0x00)
            lowerBound = 0x00;

        return ((dst <= upperBound) && (dst >= lowerBound));
    }
}
