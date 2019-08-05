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

    /**
     * screenshot policies determine when or how to refresh a new screenshot
     * POLICY_DEFAULT: refresh policy default is manual mode
     * POLICY_STRICT: refresh every time
     * POLICY_MANUAL: refresh by caller itself
     */
    public static final int POLICY_STRICT = 1;
    public static final int POLICY_MANUAL = 2;
    public static final int POLICY_DEFAULT = POLICY_MANUAL;

    private GameDevice mDevice;
    private Logger Log; //the naming is just for easy use

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenXOffset = 0;
    private int mScreenYOffset = 0;
    private int mCurrentGameOrientation;
    private int[] mAmbiguousRange = {0x05, 0x05, 0x05};
    private final int mMaxColorFinding = 10;
    private int mScreenshotPolicy = POLICY_DEFAULT;
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

    public void setScreenDimension(int[] dims) {
        if (dims.length != 2)
            throw new IllegalArgumentException("dimension should have index of exact 2.");
        setScreenDimension(dims[0], dims[1]);
    }

    public void setGameOrientation(int o) {
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
        if (policy < 0 || policy > POLICY_MANUAL)
            mScreenshotPolicy = POLICY_DEFAULT;
        else
            mScreenshotPolicy = policy;
    }

    //
    // Main functions
    //

    // calculate the offset of dump file for
    // retrieving color of the specific point
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
     * return the slot count of screenshot slots
     * @return The slot count of screenshot slots
     */
    public int getScreenshotSlotCount() {
        return mScreenshotSlotCount;
    }

    /**
     * request a refresh of screenshot in current slot
     * @return 0 upon success
     * @throws InterruptedException if screenshot cannot be done
     */
    public int requestRefresh() throws InterruptedException {
        int ret;
        int index = mScreenshotCurrentSlot;

        mDevice.screenshotClose(index);

        ret = mDevice.screenDump(index, false);
        if (ret < 0) {
            if (ret == GameDevice.SCREENSHOT_IN_USE)
                ret = mDevice.screenDump(index, true);
            else if (ret == GameDevice.SCREENSHOT_DUMP_FAIL)
                throw new InterruptedException();

            if (ret == GameDevice.SCREENSHOT_CLOSE_FAIL)
                throw new InterruptedException();
        }

        return ret;
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

    /**
     * change the current slot for further screenshot
     * @param index The target index of the slot
     * @param closeOld True if need to close previous screenshot; False if you like to preserve
     *                 previous screenshot for future need.
     * @return 0 if success
     */
    public int changeSlot(int index, boolean closeOld) {
        int ret = 0;

        if (index < 0 || index >= mScreenshotSlotCount)
            throw new IndexOutOfBoundsException("index " + index + " is not legal");

        Log.d(TAG, "Change screenshot slot from " + mScreenshotCurrentSlot + " to " + index);
        if (mScreenshotCurrentSlot != index && closeOld) {
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
     * get current slot index
     * @return The current slot index
     */
    public int getCurrentSlot() {
        return mScreenshotCurrentSlot;
    }

    /**
     * Get the color on screenshot at index
     * If refresh is needed and the slot is in use, force it.
     * [synced function]
     * @param index The index of the screenshot slot
     * @param src The source coordinate location
     * @param refresh True if we need to take new screenshot before fetching color
     * @return The {@link ScreenColor} of the src coordination
     * @throws InterruptedException When interrupted or error happened
     * @throws ScreenshotErrorException When screenshot error happened
     */
    public synchronized ScreenColor getColorOnScreen(int index, ScreenCoord src, boolean refresh)
            throws InterruptedException, ScreenshotErrorException {

        RandomAccessFile dumpFile;
        int offset, ret = 0;
        byte[] colorInfo = new byte[4];

        offset = calculateOffset(src);
        ScreenColor dest = new ScreenColor();

        try {
            if (refresh)
                ret = requestRefresh();

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
     * get multiple colors on screen
     * @param index The slot index
     * @param coords The coordination of colors
     * @param refresh True if request a new screenshot first
     * @return The array list of screen colors
     * @throws InterruptedException When interrupted or error happened
     * @throws ScreenshotErrorException When screenshot error happened
     */
    public ArrayList<ScreenColor> getMultiColorOnScreen(int index, ArrayList<ScreenCoord> coords, boolean refresh)
            throws InterruptedException, ScreenshotErrorException {
        ArrayList<ScreenColor> colors = new ArrayList<>();

        if (index < 0 || index >= mScreenshotSlotCount)
            throw new IndexOutOfBoundsException("index " + index + " is not legal.");

        if (coords == null || coords.size() == 0)
            throw new IllegalArgumentException("coords is null or size is zero");

        if(refresh)
            requestRefresh();

        for(ScreenCoord coord: coords) {
            colors.add(getColorOnScreen(coord, false));
        }

        return colors;
    }

    /**
     * Get the color on a screenshot at current index
     * @param src The source coordinate location
     * @param refresh True if we need to take new screenshot before fetching color
     * @return The {@link ScreenColor} of the src coordination
     * @throws InterruptedException When interrupted or error happened
     * @throws ScreenshotErrorException When screenshot error happened
     */
    public ScreenColor getColorOnScreen(ScreenCoord src, boolean refresh)
            throws InterruptedException, ScreenshotErrorException {
        return getColorOnScreen(mScreenshotCurrentSlot, src, refresh);
    }

    /**
     * get multiple colors on screen on current slot
     * @param coords The coordination of colors
     * @param refresh True if request a new screenshot first
     * @return The array list of screen colors
     * @throws InterruptedException When interrupted or error happened
     * @throws ScreenshotErrorException When screenshot error happened
     */
    public ArrayList<ScreenColor> getMultiColorOnScreen(ArrayList<ScreenCoord> coords, boolean refresh)
            throws InterruptedException, ScreenshotErrorException {
        return getMultiColorOnScreen(mScreenshotCurrentSlot, coords, refresh);
    }

    /**
     * Check if color at the point.coord is equal to point.color
     * @param point The point includes the coord to fetch color and determine if it's same as in color
     * @return True if color is the same or False if the colors are different
     * @throws InterruptedException When interrupted happened usually the signal from script
     * @throws ScreenshotErrorException When screenshot error happened
     */
    public boolean colorIs(ScreenPoint point) throws InterruptedException, ScreenshotErrorException {
        boolean refreshNeeded = false;
        if (point == null)
            throw new NullPointerException("point is null");

        if (mScreenshotPolicy == POLICY_STRICT)
            refreshNeeded = true;

        ScreenColor currentColor = getColorOnScreen(point.coord, refreshNeeded);
        return colorCompare(currentColor, point.color);
    }

    /**
     * Check if colors in the points array are the same as in the screen
     * @param points The point includes the coord to fetch color and determine if it's same as in color
     * @return True if colors are the same or False if at least one of the colors are different
     * @throws InterruptedException When interrupted happened usually the signal from script
     * @throws ScreenshotErrorException When screenshot error happened
     */
    public boolean colorsAre(ArrayList<ScreenPoint> points) throws InterruptedException, ScreenshotErrorException {
        if (points == null)
            throw new NullPointerException("point array is null");

        // refresh first, we do not like to refresh every point we'd like to check
        if (mScreenshotPolicy == POLICY_STRICT)
            requestRefresh();

        for(ScreenPoint point: points) {
            ScreenColor currentColor = getColorOnScreen(point.coord, false);
            if (!colorCompare(currentColor, point.color))
                return false;
        }

        return true;
    }

    /**
     * Check if all colors in the array are all in the specific region rect
     * Note that the colors in the array in unordered
     * @param rectLeftTop The LT of rect
     * @param rectRightBottom The RB of rect
     * @param colors The set of {@link ScreenColor} in match
     * @return True if all colors are in the rect. False if at least one color is not in the rect.
     * @throws InterruptedException When interrupted happened usually the signal from script
     * @throws ScreenshotErrorException When screenshot error happened
     */
    public boolean colorsAreInRect(ScreenCoord rectLeftTop, ScreenCoord rectRightBottom, ArrayList<ScreenColor> colors)
            throws InterruptedException, ScreenshotErrorException  {

        ArrayList<ScreenCoord> coordList = new ArrayList<>();
        ArrayList<ScreenColor> colorsReturned;
        ArrayList<Boolean> checkList = new ArrayList<>();
        int colorCount, orientation;
        int x_start, x_end, y_start, y_end;

        // sanity check
        if (colors == null || rectLeftTop == null || rectRightBottom == null) {
            Log.w(TAG, "checkColorIsInRegion: colors cannot be null");
            throw new NullPointerException("checkColorIsInRegion: colors cannot be null");
        } else {
            orientation = rectLeftTop.orientation;
            colorCount = colors.size();
        }

        if (rectLeftTop.orientation != rectRightBottom.orientation) {
            Log.w(TAG, "checkColorIsInRegion: Src and Dest must in same orientation");
            throw new IllegalArgumentException("checkColorIsInRegion: Src and Dest must in same orientation");
        }

        if (colorCount < 1 || colorCount > mMaxColorFinding) {
            Log.w(TAG, "checkColorIsInRegion: colors size should be bigger than 0 and smaller than " +
                    mMaxColorFinding);
            throw new IllegalArgumentException("checkColorIsInRegion: colors size should be bigger than 0 and smaller than " +
                    mMaxColorFinding);
        }

        for(int i = 0; i < colorCount; i++)
            checkList.add(false);

        if (rectLeftTop.x > rectRightBottom.x) {
            x_start = rectRightBottom.x;
            x_end = rectLeftTop.x;
        } else {
            x_start = rectLeftTop.x;
            x_end = rectRightBottom.x;
        }

        if (rectLeftTop.y > rectRightBottom.y) {
            y_start = rectRightBottom.y;
            y_end = rectLeftTop.y;
        } else {
            y_start = rectLeftTop.y;
            y_end = rectRightBottom.y;
        }

        for(int x = x_start; x <= x_end; x++) {
            for(int y = y_start; y <= y_end; y++) {
                coordList.add(new ScreenCoord(x, y, orientation));
            }
        }

        if (mChatty) Log.d(TAG, "FindColorInRange: now checking total " + coordList.size() + " points");

        colorsReturned = getMultiColorOnScreen(coordList, true);
        for(ScreenColor color : colorsReturned) {
            for (int i = 0; i < colorCount; i++) {
                ScreenColor sc = colors.get(i);

                if (checkList.get(i))
                    continue;

                if (colorCompare(color, sc)) {
                    if (mChatty) Log.d(TAG, "FindColorInRange: Found color " + color.toString());
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



}
