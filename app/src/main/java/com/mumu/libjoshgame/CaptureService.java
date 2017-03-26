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

import java.io.RandomAccessFile;
import java.util.ArrayList;

public class CaptureService extends JoshGameLibrary.GLService {
    private final String TAG = "LibJG";
    private final String mInternalDumpFile = Environment.getExternalStorageDirectory().toString() + "/internal.dump";
    private final String mFindColorDumpFile = Environment.getExternalStorageDirectory().toString() + "/find_color.dump";
    private int mScreenWidth = -1;
    private int mScreenHeight = -1;
    private int mCurrentGameOrientation = ScreenPoint.SO_Portrait;
    private int mAmbiguousRange = 0x05;
    private final int mMaxColorFinding = 10;
    
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
     * getColorOnDump
     * sc: ScreenColor to be saved into
     * filename: dump file path
     * coord: ScreenCoord to be used
     */
    public void getColorsOnDump(ArrayList<ScreenColor> colors,
                                String filename, ArrayList<ScreenCoord> coords) {
        RandomAccessFile dumpFile;
        int offset = 0;
        int bpp = 4;
        byte[] colorInfo;

        try {
            dumpFile = new RandomAccessFile(filename, "rw");
        } catch (Exception e) {
            Log.d(TAG, "getColorsOnDump: File opened failed." + e.getMessage());
            return;
        }

        for(int i = 0; i < coords.size(); i++) {
            ScreenCoord coord = coords.get(i);
            ScreenColor color = colors.get(i);

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
                colorInfo = new byte[4];
                dumpFile.seek(offset);
                dumpFile.read(colorInfo);
                color.r = colorInfo[0];
                color.g = colorInfo[1];
                color.b = colorInfo[2];
                color.t = colorInfo[3];
            } catch (Exception e) {
                Log.d(TAG, "File seek error: " + e.toString());
            }
        }

        try {
            dumpFile.close();
        } catch (Exception e) {
            Log.d(TAG, "File close failed: " + e.toString());
        }
    }

    /*
     * checkColorInList
     * this function requires file opened to improve performance
     * i.e., the file open/close should be handled outside
     */
    private boolean checkColorInList(RandomAccessFile dumpFileOpened, ArrayList<ScreenPoint> points) {
        int offset = 0;
        int bpp = 4;
        byte[] colorInfo;

        for(int i = 0; i < points.size(); i++) {
            ScreenPoint point = points.get(i);
            ScreenCoord coord = point.coord;
            ScreenColor nowColor = new ScreenColor();

            //if Android version is 7.0 or higher, the dump orientation will be obeyed device
            if (mCurrentGameOrientation == ScreenPoint.SO_Portrait) {
                if (coord.orientation == ScreenPoint.SO_Portrait)
                    offset = (mScreenWidth * coord.y + coord.x) * bpp;
                else if (point.coord.orientation == ScreenPoint.SO_Landscape)
                    offset = (mScreenWidth * coord.x + (mScreenWidth - coord.y)) * bpp;
            } else {
                if (point.coord.orientation == ScreenPoint.SO_Portrait) {
                    offset = (mScreenHeight * (mScreenWidth - coord.x) + coord.y) * bpp;
                } else if (point.coord.orientation == ScreenPoint.SO_Landscape) {
                    offset = (mScreenHeight * coord.y + coord.x) * bpp;
                }
            }

            try {
                colorInfo = new byte[4];
                dumpFileOpened.seek(offset);
                dumpFileOpened.read(colorInfo);
                nowColor.r = colorInfo[0];
                nowColor.g = colorInfo[1];
                nowColor.b = colorInfo[2];
                nowColor.t = colorInfo[3];
            } catch (Exception e) {
                Log.d(TAG, "File seek error: " + e.toString());
            }

            //compare the color
            if(!colorCompare(point.color, nowColor))
                return false;
        }

        return true;
    }

    /*
     * findColorInRange (added in 1.20)
     * src: Source ScreenCoord (must smaller than dest)
     * dest: Destination ScreenCoord
     * colors: ScreenColor array to be found
     */
    public boolean findColorInRange(ScreenCoord src, ScreenCoord dest, ArrayList<ScreenColor> colors) {
        ArrayList<ScreenCoord> coordList = new ArrayList<>();
        ArrayList<ScreenColor> colorsReturned = new ArrayList<>();
        ArrayList<Boolean> checkList = new ArrayList<>();
        int colorCount;
        int orientation;
        int x_start, x_end, y_start, y_end;

        // sanity check
        if (colors == null || src == null || dest == null) {
            Log.w(TAG, "findColorInRange: colors cannot be null");
            return false;
        } else {
            orientation = src.orientation;
            colorCount = colors.size();
        }

        if (src.orientation != dest.orientation) {
            Log.w(TAG, "findColorInRange: Src and Dest must in same orientation");
            return false;
        }

        if (colorCount < 1 || colorCount > mMaxColorFinding) {
            Log.w(TAG, "findColorInRange: colors size should be bigger than 0 and smaller than " +
                    mMaxColorFinding);
            return false;
        }

        for(int i = 0; i < colorCount; i++)
            checkList.add(false);

        if (src.x > dest.x) {
            x_start = dest.x;
            x_end = src.x;
        } else {
            x_start = src.x;
            x_end = dest.x;
        }

        if (src.y > dest.y) {
            y_start = dest.y;
            y_end = src.y;
        } else {
            y_start = src.y;
            y_end = dest.y;
        }

        for(int x = x_start; x <= x_end; x++) {
            for(int y = y_start; y <= y_end; y++) {
                coordList.add(new ScreenCoord(x, y, orientation));
                colorsReturned.add(new ScreenColor());
            }
        }

        Log.d(TAG, "FindColorInRange: now checking total " + coordList.size() + " points");
        dumpScreen(mFindColorDumpFile);
        getColorsOnDump(colorsReturned, mFindColorDumpFile, coordList);
        for(ScreenColor color : colorsReturned) {
            for (int i = 0; i < colorCount; i++) {
                ScreenColor sc = colors.get(i);

                if (checkList.get(i))
                    continue;

                if (colorCompare(color, sc)) {
                    Log.d(TAG, "Found color " + color.toString());
                    checkList.set(i, true);
                }
            }
        }

        for(Boolean b : checkList) {
            if (!b)
                return false;
        }

        return true;
    }

    /*
     * findColorSegment
     * colorPoints: segment of colors
     * start: start point
     * end: end point
     *
     * return: the coordination of segment started or null if not found
     *
     * NOTICE: the color segment can be horizontal raw or
     * vertical column
     */
    public ScreenCoord findColorSegment(ScreenCoord start, ScreenCoord end, ArrayList<ScreenPoint> colorPoints) {
        boolean searchX;
        boolean found = false;
        RandomAccessFile dumpFile;
        ScreenCoord ret = new ScreenCoord();

        if (start == null || end == null || colorPoints == null) {
            Log.e(TAG, "findColorSegment: null pointer of inputs");
            return null;
        }

        if (start.orientation != end.orientation) {
            Log.e(TAG, "findColorSegment: start and end has different orientation, abort");
            return null;
        }

        if(start.x == end.x) {
            Log.d(TAG, "findColorSegment: X axis is fixed, colorPoints is y determined");
            searchX = false;
        } else if (start.y == end.y) {
            Log.d(TAG, "findColorSegment: Y axis is fixed, colorPoints is x determined");
            searchX = true;
        } else {
            Log.e(TAG, "findColorSegment: No axis is fixed, abort here.");
            return null;
        }

        try {
            dumpScreen(mFindColorDumpFile);
            dumpFile = new RandomAccessFile(mFindColorDumpFile, "rw");
        } catch (Exception e) {
            Log.d(TAG, "findColorSegment: File opened failed." + e.getMessage());
            return null;
        }

        ArrayList<ScreenPoint> points = new ArrayList<>();
        if (searchX) {
            for(int x = start.x; x <= end.x; x++) {
                points.clear();

                for(ScreenPoint point: colorPoints) {
                    ScreenPoint insert = new ScreenPoint();
                    insert.coord.x = x;
                    insert.coord.y = point.coord.y;
                    insert.coord.orientation = point.coord.orientation;
                    insert.color = point.color;
                    points.add(insert);
                }

                if(checkColorInList(dumpFile, points)) {
                    Log.d(TAG, "findColorSegment: Found! ");
                    ret.x = x;
                    ret.y = points.get(0).coord.y;
                    ret.orientation = start.orientation;
                    found = true;
                    break;
                }
            }
        } else {
            for(int y = start.y; y <= end.y; y++) {
                points.clear();

                for(ScreenPoint point: colorPoints) {
                    ScreenPoint insert = new ScreenPoint();
                    insert.coord.x = point.coord.x;
                    insert.coord.y = y;
                    insert.coord.orientation = point.coord.orientation;
                    insert.color = point.color;
                    points.add(insert);
                }

                if(checkColorInList(dumpFile, points)) {
                    Log.d(TAG, "findColorSegment: Found! ");
                    ret.x = points.get(0).coord.x;
                    ret.y = y;
                    ret.orientation = start.orientation;
                    found = true;
                    break;
                }
            }
        }

        try {
            dumpFile.close();
        } catch (Exception e) {
            Log.d(TAG, "findColorSegment: File close failed: " + e.toString());
        }

        if (found)
            return ret;

        return null;
    }

    /*
     * findColorSegmentGlobal
     *
     * colorPoints: segment of colors and coordination used to find in entire screen
     */
    public ScreenCoord findColorSegmentGlobal(ArrayList<ScreenPoint> colorPoints) {
        boolean searchX;
        boolean found = false;
        int orientation;
        RandomAccessFile dumpFile;
        ScreenCoord ret = new ScreenCoord();

        if (colorPoints == null || colorPoints.get(0) == null || colorPoints.get(1) == null) {
            Log.e(TAG, "findColorSegmentGlobal: require at least 2 points in colorPoints");
            return null;
        }

        orientation = colorPoints.get(0).coord.orientation;
        for(ScreenPoint point: colorPoints) {
            if (point.coord.orientation != orientation) {
                Log.e(TAG, "findColorSegmentGlobal: start and end has different orientation, abort");
                return null;
            }
        }

        // Use two points to decide search direction
        ScreenCoord start = colorPoints.get(0).coord;
        ScreenCoord end = colorPoints.get(colorPoints.size()-1).coord;
        if(start.x == end.x) {
            Log.d(TAG, "findColorSegmentGlobal: X axis is fixed, colorPoints is y determined");
            searchX = false;
        } else if (start.y == end.y) {
            Log.d(TAG, "findColorSegmentGlobal: Y axis is fixed, colorPoints is x determined");
            searchX = true;
        } else {
            Log.e(TAG, "findColorSegmentGlobal: No axis is fixed, abort here.");
            return null;
        }

        try {
            dumpScreen(mFindColorDumpFile);
            dumpFile = new RandomAccessFile(mFindColorDumpFile, "rw");
        } catch (Exception e) {
            Log.d(TAG, "findColorSegmentGlobal: File opened failed." + e.getMessage());
            return null;
        }

        ArrayList<ScreenPoint> points = new ArrayList<>();
        int xBound = (orientation == ScreenPoint.SO_Portrait ? mScreenWidth : mScreenHeight);
        int yBound = (orientation == ScreenPoint.SO_Portrait ? mScreenHeight : mScreenWidth);

        if (searchX) {
            for(int x = 1; x < xBound; x++) {
                points.clear();

                for(int y = 1; y < yBound - (end.y - start.y); y++) {
                    for(int i = 0; i < colorPoints.size(); i++) {
                        ScreenPoint pointInList = colorPoints.get(i);
                        ScreenPoint insert = new ScreenPoint();
                        insert.coord.x = x;
                        if (i != 0)
                            insert.coord.y = y + (pointInList.coord.y - colorPoints.get(0).coord.y);
                        else
                            insert.coord.y = y;

                        insert.coord.orientation = pointInList.coord.orientation;
                        insert.color = pointInList.color;
                        points.add(insert);
                    }

                    if(checkColorInList(dumpFile, points)) {
                        Log.d(TAG, "findColorSegmentGlobal: Found! ");
                        ret.x = x;
                        ret.y = y;
                        ret.orientation = start.orientation;
                        found = true;
                        break;
                    }
                }

                if (found)
                    break;
            }
        } else {
            for(int y = 1; y < yBound; y++) {
                points.clear();

                for(int x = 1; x < xBound - (end.x - start.x); x++) {
                    for(int i = 0; i < colorPoints.size(); i++) {
                        ScreenPoint pointInList = colorPoints.get(i);
                        ScreenPoint insert = new ScreenPoint();
                        insert.coord.y = y;
                        if (i != 0)
                            insert.coord.x = x + (pointInList.coord.x - colorPoints.get(0).coord.x);
                        else
                            insert.coord.x = x;
                        insert.coord.orientation = pointInList.coord.orientation;
                        insert.color = pointInList.color;
                        points.add(insert);
                    }

                    if(checkColorInList(dumpFile, points)) {
                        Log.d(TAG, "findColorSegmentGlobal: Found! ");
                        ret.y = y;
                        ret.x = x;
                        ret.orientation = start.orientation;
                        found = true;
                        break;
                    }
                }

                if (found)
                    break;
            }
        }

        try {
            dumpFile.close();
        } catch (Exception e) {
            Log.d(TAG, "findColorSegmentGlobal: File close failed: " + e.toString());
        }

        if (found)
            return ret;

        return null;
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
        //Log.d(TAG, "compare " + src.toString() + " with " + dest.toString());
        return colorWithinRange(src.r, dest.r, mAmbiguousRange) &&
                colorWithinRange(src.b, dest.b, mAmbiguousRange) &&
                colorWithinRange(src.g, dest.g, mAmbiguousRange);
    }

    private boolean colorWithinRange(byte a, byte b, int range) {
        Byte byteA = a;
        Byte byteB = b;
        int src = byteA.intValue() & 0xFF;
        int dst = byteB.intValue() & 0xFF;
        int upperBound = src + range;
        int lowerBound = src - range;

        if (upperBound > 0xFF)
            upperBound = 0xFF;

        if (lowerBound < 0)
            lowerBound = 0;

        //Log.d(TAG, "compare range " + upperBound + " > " + lowerBound + " with " + dst);
        return (dst <= upperBound) && (dst >= lowerBound);
    }
}
