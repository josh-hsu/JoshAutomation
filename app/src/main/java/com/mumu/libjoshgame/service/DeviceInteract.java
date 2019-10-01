package com.mumu.libjoshgame.service;

import com.mumu.libjoshgame.GameDevice;
import com.mumu.libjoshgame.GameDeviceHWEventListener;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

public class DeviceInteract {
    private static final String TAG = GameLibrary20.TAG;

    private GameDevice mDevice;
    private Logger Log; //the naming is just for easy use

    private int mRandomMouseInputShift = 0;
    private int mScreenWidth = -1;
    private int mScreenHeight = -1;
    private int mScreenXOffset = 0;
    private int mScreenYOffset = 0;
    private int mCurrentGameOrientation;
    private boolean mChatty = true;

    private boolean mHWEventOnChanged = false;

    public DeviceInteract(GameLibrary20 gl, GameDevice device) {
        if (device == null)
            throw new RuntimeException("Initial DeviceScreen with null device");
        else
            mDevice = device;

        int[] resolution = device.getScreenDimension();
        if (resolution == null || resolution.length != 2) {
            //throw new IllegalArgumentException("Device report illegal resolution length");
            Log.w(TAG, "Auto detect for device resolution failed, use default 1080x2340. Override this.");
            mScreenWidth = 1080;
            mScreenHeight = 2340;
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

    public void setScreenDimension(int w, int h) {
        mScreenWidth = w;
        mScreenHeight = h;
    }

    public void setScreenDimension(int[] dims) {
        if (dims.length != 2)
            throw new IllegalArgumentException("dimension should have index of exact 2.");
        setScreenDimension(dims[0], dims[1]);
    }

    public void setScreenOffset(int xOffset, int yOffset) {
        mScreenXOffset = xOffset;
        mScreenYOffset = yOffset;
    }

    public void setGameOrientation(int orientation) {
        mCurrentGameOrientation = orientation;
    }

    public void setMouseInputShift(int ran) {
        mRandomMouseInputShift = ran;
    }

    private int mouseInteract(int x, int y, int tx, int ty, int type) {
        int ret = 0;
        int x_shift = (int) (Math.random() * mRandomMouseInputShift) - mRandomMouseInputShift /2;
        int y_shift = (int) (Math.random() * mRandomMouseInputShift) - mRandomMouseInputShift /2;

        x = x + x_shift;
        y = y + y_shift;

        if (mScreenHeight > 0 && y > (mCurrentGameOrientation == ScreenPoint.SO_Portrait ? mScreenHeight : mScreenWidth))
            y = mScreenHeight;
        else if (y < 0)
            y = 0;

        if (mScreenWidth > 0 && x > (mCurrentGameOrientation == ScreenPoint.SO_Landscape ? mScreenHeight : mScreenWidth))
            x = mScreenWidth;
        else if (x < 0)
            x = 0;

        switch (type) {
            case GameDevice.MOUSE_TAP:
            case GameDevice.MOUSE_DOUBLE_TAP:
            case GameDevice.MOUSE_TRIPLE_TAP:
            case GameDevice.MOUSE_PRESS:
            case GameDevice.MOUSE_MOVE_TO:
            case GameDevice.MOUSE_RELEASE:
                ret = mDevice.mouseInteract(x, y, type);
                break;
            case GameDevice.MOUSE_SWIPE:
                ret = mDevice.mouseInteract(x, y, tx, ty, type);
                break;
            default:
                Log.w(TAG, "touchOnScreen: type " + type + "is invalid.");
        }

        return ret;
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

    private int mouseInteractSingleCoord(ScreenCoord coord1, int type) {
        int ret;
        ScreenCoord coord = getCalculatedOffsetCoord(coord1);

        if (mCurrentGameOrientation != coord.orientation)
            ret = mouseInteract(coord.y, mScreenWidth - coord.x, 0, 0, type);
        else
            ret = mouseInteract(coord.x, coord.y, 0, 0, type);

        return ret;
    }

    public int mouseSwipe(ScreenCoord start, ScreenCoord end) {
        int ret;
        ScreenCoord coordStart = getCalculatedOffsetCoord(start);
        ScreenCoord coordEnd = getCalculatedOffsetCoord(end);

        if (mCurrentGameOrientation != start.orientation)
            ret = mouseInteract(coordStart.y, mScreenWidth - coordStart.x, coordEnd.y, mScreenWidth - coordEnd.x, GameDevice.MOUSE_SWIPE);
        else
            ret = mouseInteract(coordStart.x, coordStart.y, coordEnd.x, coordEnd.y, GameDevice.MOUSE_SWIPE);

        return ret;
    }

    public int mouseClick(ScreenCoord coord) {
        return mouseInteractSingleCoord(coord, GameDevice.MOUSE_TAP);
    }

    public int mouseDoubleClick(ScreenCoord coord) {
        return mouseInteractSingleCoord(coord, GameDevice.MOUSE_DOUBLE_TAP);
    }

    public int mouseTripleClick(ScreenCoord coord) {
        return mouseInteractSingleCoord(coord, GameDevice.MOUSE_TRIPLE_TAP);
    }

    public int mouseDown(ScreenCoord coord) {
        return mouseInteractSingleCoord(coord, GameDevice.MOUSE_PRESS);
    }

    public int mouseMoveTo(ScreenCoord coord) {
        return mouseInteractSingleCoord(coord, GameDevice.MOUSE_MOVE_TO);
    }

    public int mouseUp(ScreenCoord coord) {
        return mouseInteractSingleCoord(coord, GameDevice.MOUSE_RELEASE);
    }

    /**
     * busy waiting for the first vibration event
     * Note that this is only supported by some devices
     * If this device doesn't support, it will return immediately
     * @param timeoutMs The time in milliseconds to give up
     * @throws InterruptedException if the main thread is interrupted, throws it.
     */
    public void waitUntilDeviceVibrate(int timeoutMs) throws InterruptedException {
        int threadSleepTimeMs = 100;
        int threadLoopCount = timeoutMs / threadSleepTimeMs + 1;
        GameDeviceHWEventListener eventListener = null;
        mHWEventOnChanged = false;

        try {
            eventListener = new GameDeviceHWEventListener() {
                @Override
                public void onEvent(int event, Object data) {
                    if (event == GameDevice.HW_EVENT_CB_ONCHANGE) {
                        int value = (Integer) data;
                        Log.d(TAG, "On change callback event for vibrator: " + value);
                        if (value == 1) {
                            mHWEventOnChanged = true;
                        }
                    } else {
                        Log.w(TAG, "Unknown callback event for vibrator: " + event);
                    }
                }
            };

            if (mDevice.registerHardwareEvent(GameDevice.HW_EVENT_VIBRATOR, eventListener) < 0) {
                Log.e(TAG, "could not register vibrator event.");
                return;
            }

            while (threadLoopCount-- > 0) {
                if (mHWEventOnChanged) {
                    Log.d(TAG, "detect vibration, end event pulling looping");
                    return;
                }
                Thread.sleep(threadSleepTimeMs);
            }
        } finally { // note that the exception will be rethrown
            mHWEventOnChanged = false;
            mDevice.deregisterHardwareEvent(GameDevice.HW_EVENT_VIBRATOR, eventListener);
        }
    }
}
