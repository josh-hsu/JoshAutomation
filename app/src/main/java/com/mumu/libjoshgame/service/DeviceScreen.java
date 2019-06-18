package com.mumu.libjoshgame.service;

import com.mumu.libjoshgame.GameDevice;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenColor;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import com.mumu.libjoshgame.GameLibrary20.ScreenshotErrorException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * This class implements previous CaptureService in GL10
 * Note that following APIs has been changed for continuous color fetch
 * The concurrent thread is not supported by now
 */
public class DeviceScreen {
    private static final String TAG = GameLibrary20.TAG;
    public static final int POLICY_AUTO = 0;
    public static final int POLICY_STRICT = 1;
    public static final int POLICY_FIFO = 2;

    private GameDevice mDevice;
    private Logger Log; //the naming is just for easy use

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenXOffset = 0;
    private int mScreenYOffset = 0;
    private int mCurrentGameOrientation;
    private int[] mAmbiguousRange = {0x05, 0x05, 0x05};
    private final int mMaxColorFinding = 10;
    private int mCurrentScreenshotPolicy = POLICY_AUTO;
    private int mScreenshotSlotCount;
    private int mScreenshotCurrentSlot;
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

        mScreenshotSlotCount = mDevice.getScreenshotSlotCount();
        mScreenshotCurrentSlot = 0;
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

    public void setScreenshotPolicy(int policy) {
        if (policy < 0 || policy > POLICY_FIFO)
            mCurrentScreenshotPolicy = POLICY_AUTO;
        else
            mCurrentScreenshotPolicy = policy;
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
     * compare two colors if they are the same
     * @param src First color
     * @param dest Second color for compare with
     * @return True if they have almost same color, i.e., the color
     *         differences are within than the ambiguous range.
     */
    public boolean colorCompare(ScreenColor src, ScreenColor dest) {
        boolean result = colorWithinRange(src.r, dest.r, mAmbiguousRange[0]) &&
                colorWithinRange(src.b, dest.b, mAmbiguousRange[1]) &&
                colorWithinRange(src.g, dest.g, mAmbiguousRange[2]);

        if (mChatty) {
            Log.d(TAG, "Source: (" + src.r + ", " + src.g + ", " + src.b + "), " +
                    " Destination: (" + dest.r + ", " + dest.g + ", " + dest.b + ") ");
        }

        return result;
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

        //Logger.d(TAG, "compare range " + upperBound + " > " + lowerBound + " with " + dst);
        return (dst <= upperBound) && (dst >= lowerBound);
    }

    /**
     * change the current slot for further screenshot
     * @param index The target index of the slot
     * @param closeOld True if need to close previous screenshot; False if preserved previous
     *                 screenshot for future need.
     * @return 0 if success
     */
    public int changeSlot(int index, boolean closeOld) {
        int ret = 0;

        if (index < 0 || index >= mScreenshotSlotCount)
            throw new IndexOutOfBoundsException("index " + index + " is not legal");

        if (mScreenshotCurrentSlot != index && closeOld) {
            Log.d(TAG, "Change screenshot slot from " + mScreenshotCurrentSlot + " to " + index);

            // close opened screenshot slot, but even if release failed, we don't need to handle it
            if (mDevice.screenshotState(mScreenshotCurrentSlot) == GameDevice.SCREENSHOT_OPENED) {
                ret = mDevice.screenshotClose(mScreenshotCurrentSlot);
                if (ret != GameDevice.SCREENSHOT_NO_ERROR) {
                    Log.w(TAG, "close screenshot failed: " + ret);
                }
                ret = mDevice.screenshotRelease(mScreenshotCurrentSlot);
                if (ret != GameDevice.SCREENSHOT_NO_ERROR) {
                    Log.w(TAG, "release screenshot failed " + ret);
                }
            }
        }

        mScreenshotCurrentSlot = index;
        return ret;
    }

    /**
     * Get the color on screenshot at index
     * If refresh is needed and the slot is in use, force it.
     * @param index The index of the screenshot slot
     * @param src The source coordinate location
     * @param refresh True if we need to take new screenshot before fetching color
     * @return The {@link ScreenColor} of the src coordination
     * @throws InterruptedException When interrupted or error happened
     * @throws ScreenshotErrorException When screenshot error happened
     */
    public synchronized ScreenColor getColorOnPoint(int index, ScreenCoord src, boolean refresh)
            throws InterruptedException, ScreenshotErrorException {

        RandomAccessFile dumpFile;
        int offset, ret = 0;
        byte[] colorInfo = new byte[4];

        offset = calculateOffset(src);
        ScreenColor dest = new ScreenColor();

        try {
            if (refresh) {
                ret = mDevice.screenDump(index, false);
                if (ret < 0) {
                    if (ret == GameDevice.SCREENSHOT_IN_USE)
                        ret = mDevice.screenDump(index, true);
                    else if (ret == GameDevice.SCREENSHOT_DUMP_FAIL)
                        throw new InterruptedException();

                    if (ret == GameDevice.SCREENSHOT_CLOSE_FAIL)
                        throw new InterruptedException();
                }
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

        return dest;
    }

    /**
     * Get the color on a screenshot at current index
     * @param src The source coordinate location
     * @param refresh True if we need to take new screenshot before fetching color
     * @return The {@link ScreenColor} of the src coordination
     * @throws InterruptedException When interrupted or error happened
     * @throws ScreenshotErrorException When screenshot error happened
     */
    public ScreenColor getColorOnPoint(ScreenCoord src, boolean refresh)
            throws InterruptedException, ScreenshotErrorException {
        return getColorOnPoint(mScreenshotCurrentSlot, src, refresh);
    }

    public boolean colorIs(ScreenPoint point) throws InterruptedException, ScreenshotErrorException {
        if (point == null)
            throw new NullPointerException("point is null");

        ScreenColor currentColor = getColorOnPoint(point.coord, false);
        return colorCompare(currentColor, point.color);
    }

    public boolean colorsAre(ArrayList<ScreenPoint> points) throws InterruptedException, ScreenshotErrorException {
        if (points == null)
            throw new NullPointerException("point array is null");

        // since we are not doing refresh in this process
        // calling colorIs repeatedly will not affect the performance
        for(ScreenPoint point : points) {
            if (!colorIs(point))
                return false;
        }

        return true;
    }

}
