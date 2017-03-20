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

import android.os.Environment;
import android.util.Log;
import com.mumu.libjoshgame.ScreenPoint.*;

import java.io.RandomAccessFile;

public class CaptureService extends JoshGameLibrary.GLService {
    private final String TAG = "LibJG";
    private final String mInternalDumpFile = Environment.getExternalStorageDirectory().toString() + "/internal.dump";
    private int mScreenWidth = -1;
    private int mScreenHeight = -1;
    
    CaptureService()
    {
        Log.d(TAG, "CaptureService has been created.");
    }

    public void SetScreenDimension(int w, int h) {
        mScreenHeight = h;
        mScreenWidth = w;
    }

    public void DumpScreenPNG(String filename) {
        super.runCommand("screencap -p " + filename);
        //Cmd.runCommand("chmod 664 "+ filename);
        //Cmd.runCommand("chown sdcard_rw:media_rw "+ filename);
    }

    public void DumpScreen(String filename)
    {
        super.runCommand("screencap " + filename);
        //Cmd.runCommand("chmod 664 "+ filename);
        //Cmd.runCommand("chown sdcard_rw:media_rw "+ filename);
    }

    private void DumpScreen()
    {
        super.runCommand("screencap " + mInternalDumpFile);
        super.runCommand("chmod 666 " + mInternalDumpFile);
    }

    private void GetColorOnDumpInternal(ScreenColor sc, ScreenCoord coord)
    {
        GetColorOnDump(sc, mInternalDumpFile, coord);
    }

    public void GetColorOnDump(ScreenColor sc, String filename, ScreenCoord coord)
    {
        RandomAccessFile dumpFile;
        int offset = 0;
        int bpp = 4;
        byte[] colorInfo = new byte[4];

        if (coord.orientation == ScreenPoint.SO_Portrait)
            offset = (mScreenWidth * coord.y + coord.x) * bpp;
        else if (coord.orientation == ScreenPoint.SO_Landscape)
            offset = (mScreenWidth * coord.x + (mScreenWidth - coord.y)) * bpp;

        try {
            dumpFile = new RandomAccessFile(filename, "rw");
            dumpFile.seek(offset);
            dumpFile.read(colorInfo);
            sc.b = colorInfo[0];
            sc.g = colorInfo[1];
            sc.r = colorInfo[2];
            sc.t = colorInfo[3];
            dumpFile.close();
        } catch (Exception e) {
            Log.d(TAG, "File opened failed." + e.toString());
            return;
        }
    }

    /* This should be called after DumpScreen, otherwise you will get old dumps */
    private void GetColor(ScreenColor sc, ScreenCoord coord)
    {
        GetColorOnDumpInternal(sc, coord);
    }

    public void GetColorOnScreen(ScreenColor sc, ScreenCoord coord)
    {
        DumpScreen();
        GetColor(sc, coord);
    }

    public int WaitOnColor(ScreenColor sc, ScreenCoord coord, int thres, Thread kThread)
    {
        ScreenPoint currentPoint = new ScreenPoint();
        Log.d(TAG, "now busy waiting for ( " + coord.x  + "," + coord.y + ") turn into 0x"
                + Integer.toHexString(sc.r & 0xFF) + Integer.toHexString(sc.g & 0xFF)
                + Integer.toHexString(sc.b & 0xFF));

        while(thres-- > 0) {
            GetColorOnScreen(currentPoint.color, coord);
            if (ColorCompare(sc, currentPoint.color)) {
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

    public int WaitOnColor(ScreenPoint sp, int thres, Thread kThread)
    {
        return WaitOnColor(sp.color, sp.coord, thres, kThread);
    }

    public boolean ColorIs(ScreenPoint point)
    {
        if (point == null) {
            Log.w(TAG, "Point is null");
            return false;
        }
        ScreenPoint currentPoint = new ScreenPoint();
        GetColorOnScreen(currentPoint.color, point.coord);
        return ColorCompare(currentPoint.color, point.color);
    }

    private int WaitOnColorAsync(ScreenColor sc, ScreenCoord coord, int thres)
    {
        return 0;
    }

    public int WaitOnColorNotEqual(ScreenColor sc, ScreenCoord coord, int thres, Thread kThread)
    {
        ScreenPoint currentPoint = new ScreenPoint();
        Log.d(TAG, "now busy waiting for ( " + coord.x  + "," + coord.y + ") is not 0x"
                + Integer.toHexString(sc.r & 0xFF) + Integer.toHexString(sc.g & 0xFF)
                + Integer.toHexString(sc.b & 0xFF));

        while(thres-- > 0) {
            GetColorOnScreen(currentPoint.color, coord);
            if (!ColorCompare(sc, currentPoint.color)) {
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

    public boolean ColorCompare(ScreenColor src, ScreenColor dest)
    {
        int ambiguousRange = 0x05;
        Log.d(TAG, "Compare source: " + src.toString() + " to dest: " + dest.toString());
        if( ColorWithIn(src.r, dest.r, ambiguousRange) &&
                ColorWithIn(src.b, dest.b, ambiguousRange) &&
                ColorWithIn(src.g, dest.g, ambiguousRange)) {
            return true;
        }

        return false;
    }

    private boolean ColorWithIn(byte a, byte b, int range) {
        int src = (int) a;
        int dst = (int) b;

        if ( (dst < (src + range)) && (dst > (src - range))) {
            return true;
        }

        return false;
    }
}
