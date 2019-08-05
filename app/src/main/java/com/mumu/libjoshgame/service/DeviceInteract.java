package com.mumu.libjoshgame.service;

import com.mumu.libjoshgame.GameDevice;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

public class DeviceInteract {
    private static final String TAG = GameLibrary20.TAG;

    private GameDevice mDevice;
    private Logger Log; //the naming is just for easy use

    private int mRandomInputShift = 0;
    private int mScreenWidth = -1;
    private int mScreenHeight = -1;
    private int mScreenXOffset = 0;
    private int mScreenYOffset = 0;
    private int mCurrentGameOrientation;
    private boolean mChatty = true;

    public DeviceInteract(GameLibrary20 gl, GameDevice device) {
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

    public void setInputShift(int ran) {
        mRandomInputShift = ran;
    }

    private int deviceInteract(int x, int y, int tx, int ty, int type) {
        int ret = 0;
        int x_shift = (int) (Math.random() * mRandomInputShift) - mRandomInputShift /2;
        int y_shift = (int) (Math.random() * mRandomInputShift) - mRandomInputShift /2;

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
                ret = mDevice.mouseInteract(x, y, type);
                break;
            case GameDevice.MOUSE_PRESS:
            case GameDevice.MOUSE_MOVE_TO:
            case GameDevice.MOUSE_RELEASE:
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

    private int mouseInteractSingle(ScreenCoord coord1, int type) {
        int ret;
        ScreenCoord coord = getCalculatedOffsetCoord(coord1);

        if (mCurrentGameOrientation != coord.orientation)
            ret = deviceInteract(coord.y, mScreenWidth - coord.x, 0, 0, type);
        else
            ret = deviceInteract(coord.x, coord.y, 0, 0, type);

        return ret;
    }

    public int mouseSwipe(ScreenCoord start, ScreenCoord end) {
        int ret;
        ScreenCoord coordStart = getCalculatedOffsetCoord(start);
        ScreenCoord coordEnd = getCalculatedOffsetCoord(end);

        if (mCurrentGameOrientation != start.orientation)
            ret = deviceInteract(coordStart.y, mScreenWidth - coordStart.x, coordEnd.y, mScreenWidth - coordEnd.x, GameDevice.MOUSE_SWIPE);
        else
            ret = deviceInteract(coordStart.x, coordStart.y, coordEnd.x, coordEnd.y, GameDevice.MOUSE_SWIPE);

        return ret;
    }

    public int mouseClick(ScreenCoord coord) {
        return mouseInteractSingle(coord, GameDevice.MOUSE_TAP);
    }

    public int mouseDoubleClick(ScreenCoord coord) {
        return mouseInteractSingle(coord, GameDevice.MOUSE_DOUBLE_TAP);
    }

    public int mouseTripleClick(ScreenCoord coord) {
        return mouseInteractSingle(coord, GameDevice.MOUSE_TRIPLE_TAP);
    }

    public int mouseDown(ScreenCoord coord) {
        return mouseInteractSingle(coord, GameDevice.MOUSE_PRESS);
    }

    public int mouseMoveTo(ScreenCoord coord) {
        return mouseInteractSingle(coord, GameDevice.MOUSE_MOVE_TO);
    }

    public int mouseUp(ScreenCoord coord) {
        return mouseInteractSingle(coord, GameDevice.MOUSE_RELEASE);
    }
}
