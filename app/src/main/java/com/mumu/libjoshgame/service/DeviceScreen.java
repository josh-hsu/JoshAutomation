package com.mumu.libjoshgame.service;

import com.mumu.libjoshgame.GameDevice;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenColor;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import com.mumu.libjoshgame.GameLibrary20.ScreenshotErrorException;

import java.io.IOException;
import java.io.RandomAccessFile;

public class DeviceScreen {
    private static final String TAG = GameLibrary20.TAG;
    private GameDevice mDevice;
    private GameLibrary20 mGL;
    private Logger Log; //the naming is just for easy use

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenXOffset = 0;
    private int mScreenYOffset = 0;
    private int mCurrentGameOrientation;
    private int[] mAmbiguousRange = {0x05, 0x05, 0x05};
    private final int mMaxColorFinding = 10;
    private boolean mChatty = true;

    public DeviceScreen(GameDevice device) {
        if (device == null)
            throw new RuntimeException("Initial DeviceScreen with null device");
        else
            mDevice = device;

        int[] resolution = device.getScreenDimension();
        if (resolution.length != 2) {
            throw new IllegalArgumentException("Device report illegal resolution length");
        } else {
            mScreenWidth = resolution[0];
            mScreenHeight = resolution[1];
        }

        int orientation = device.getScreenMainOrientation();
        if (orientation < 0)
            throw new IllegalArgumentException("Device report illegal default screen orientation");
        else
            mCurrentGameOrientation = orientation;

        Log = mDevice.getLogger();
    }

    //
    // Screen setting override
    //
    public void setScreenDimension(int w, int h) {
        mScreenHeight = h;
        mScreenWidth = w;
    }

    public void setScreenOrientation(int o) {
        mCurrentGameOrientation = o;
    }

    /*
     * setScreenOffset (added in 1.34)
     * this function can be called by JoshGameLibrary only
     * shift an amount of offset for every point input
     * we will treat this as portrait orientation
     */
    public void setScreenOffset(int xOffset, int yOffset) {
        mScreenXOffset = xOffset;
        mScreenYOffset = yOffset;
    }

    public void setAmbiguousRange(int[] range) {
        mAmbiguousRange = range;
    }

    public void setChatty(boolean chatty) {
        mChatty = chatty;
    }

    //
    // Main functions
    //

    /*
     * calculateOffset
     * This function calculate the offset of dump file for
     * retrieving color of the specific point
     */
    private int calculateOffset(ScreenCoord coord) {
        int offset = 0;
        final int bpp = 4;

        if (mChatty) {
            if (coord.orientation == ScreenPoint.SO_Landscape)
                Log.d(TAG, "Mapping (" + coord.x + ", " + coord.y +
                        ") to ("  + (coord.x + mScreenYOffset) + ", " + (coord.y + mScreenXOffset) + ")");
            else
                Log.d(TAG, "Mapping (" + coord.x + ", " + coord.y +
                        ") to ("  + (coord.x + mScreenXOffset) + ", " + (coord.y + mScreenYOffset) + ")");
        }

        //if Android version is 7.0 or higher, the dump orientation will obey the device status
        if (mCurrentGameOrientation == ScreenPoint.SO_Portrait) {
            if (coord.orientation == ScreenPoint.SO_Portrait) {
                offset = (mScreenWidth * (coord.y + mScreenYOffset) + (coord.x + mScreenXOffset)) * bpp;
            } else if (coord.orientation == ScreenPoint.SO_Landscape) {
                offset = (mScreenWidth * (coord.x + mScreenYOffset) + (mScreenWidth - (coord.y + mScreenXOffset))) * bpp;
            }
        } else {
            if (coord.orientation == ScreenPoint.SO_Portrait) {
                offset = (mScreenHeight * (mScreenWidth - (coord.x + mScreenXOffset)) + (coord.y + mScreenYOffset)) * bpp;
            } else if (coord.orientation == ScreenPoint.SO_Landscape) {
                offset = (mScreenHeight * (coord.y + mScreenXOffset) + (coord.x + mScreenYOffset)) * bpp;
            }
        }

        return offset;
    }

    /**
     * Get the color on a new dump at index
     * If the slot is in use, force it.
     * @param index The index of the screenshot slot
     * @param dest The color object to receive the color info
     * @param src The source coordinate location
     * @throws InterruptedException When interrupted or error happened
     */
    public synchronized void getColorOnDump(int index, ScreenColor dest, ScreenCoord src)
            throws InterruptedException, ScreenshotErrorException {

        RandomAccessFile dumpFile;
        int offset, ret = 0;
        byte[] colorInfo = new byte[4];

        offset = calculateOffset(src);

        try {
            ret = mDevice.screenDump(index, false);
            if (ret < 0) {
                if (ret == GameDevice.SCREENSHOT_IN_USE)
                    ret = mDevice.screenDump(index, true);
                else if (ret == GameDevice.SCREENSHOT_DUMP_FAIL)
                    throw new InterruptedException();

                if (ret == GameDevice.SCREENSHOT_CLOSE_FAIL)
                    throw new InterruptedException();
            }

            dumpFile = mDevice.screenshotOpen(index);
            if (dumpFile == null) {
                throw new InterruptedException();
            }
            dumpFile.seek(offset);
            dumpFile.read(colorInfo);
            dest.r = colorInfo[0];
            dest.g = colorInfo[1];
            dest.b = colorInfo[2];
            dest.t = colorInfo[3];
            dumpFile.close();
        } catch (InterruptedException e) {
            Log.d(TAG, "File operation failed: " + e.toString());
            throw e;
        } catch (IOException e) {
            Log.d(TAG, "File operation failed: " + e.toString());
            throw new ScreenshotErrorException("screenshot error", ret);
        }
    }

}
