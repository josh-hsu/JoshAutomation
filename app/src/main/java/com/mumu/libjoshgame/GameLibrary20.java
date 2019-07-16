package com.mumu.libjoshgame;

import com.mumu.libjoshgame.device.AndroidInternal;
import com.mumu.libjoshgame.device.NoxPlayer;
import com.mumu.libjoshgame.service.DeviceInput;
import com.mumu.libjoshgame.service.DeviceScreen;

import java.util.ArrayList;

/**
 * Josh Game Library - Version 2.0
 *
 * JoshGameLibrary (GL)
 * This game control library require the following initial phase
 *
 * GameLibrary20 mGL = new GameLibrary20();
 * mGL.chooseDevice(DEVICE_TYPE);      //DEVICE_TYPE could be DEVICE_TYPE_ANDROID_INTERNAL or
 *                                     //DEVICE_TYPE_NOX_PLAYER
 * mGL.setDeviceEssentials(Objects);   //optional
 * mGL.initDevice(Objects);            //if this return NO_ERROR, GL will mark mDeviceReady flag
 */
public class GameLibrary20 {
    public static final String TAG = "GL20";
    public static final int DEVICE_TYPE_ANDROID_INTERNAL = 1;
    public static final int DEVICE_TYPE_NOX_PLAYER = 9;

    private boolean      mDeviceReady;
    private GameDevice   mDevice;
    private DeviceScreen mScreenService;
    private DeviceInput  mInputService;

    public GameLibrary20() {
        mDeviceReady = false;
        mDevice = null;
    }

    // =============================
    //  DEVICE SELECTION AND INIT
    // =============================

    /**
     * Choose the device for this game library
     *
     * @param deviceType Device type supported by GameLibrary
     * @return 0 upon success
     */
    public int chooseDevice(int deviceType) {
        switch (deviceType) {
            case DEVICE_TYPE_ANDROID_INTERNAL:
                mDevice = new AndroidInternal();
                break;
            case DEVICE_TYPE_NOX_PLAYER:
                mDevice = new NoxPlayer();
                break;
            default:
                mDevice = null;
                return -1;
        }
        return 0;
    }

    /**
     * Set the device essentials for initialization
     * Might not need for some devices
     *
     * @param object The essentials for device
     * @return 0 upon success
     */
    public int setDeviceEssentials(Object object) {
        if (mDevice != null) {
            mDevice.setDeviceEssentials(object);
        } else {
            return -1;
        }
        return 0;
    }

    /**
     * Initialize the device
     *
     * @param objects Object array needed for initialization
     * @return 0 upon success
     */
    public int initDevice(Object[] objects) {
        int ret;
        if (mDevice != null) {
            ret = mDevice.init(objects);
        } else {
            return -1;
        }

        mDeviceReady = ret == 0;

        if (mDeviceReady)
            initGameLibraryInternal();

        return ret;
    }

    private void initGameLibraryInternal() {
        mScreenService = new DeviceScreen(mDevice);
        mInputService = new DeviceInput(this, mDevice);
    }

    // =============================
    //  DEVICE FUNCTIONS
    // =============================
    private boolean getDeviceInitialized() {
        return mDeviceReady && mDevice.getInitialized();
    }

    public String getDeviceName() throws DeviceNotInitializedException {
        if (!checkInit())
            return null;
        else
            return mDevice.getName();
    }

    public int[] getDeviceResolution() throws DeviceNotInitializedException {
        if (!checkInit())
            return null;
        else
            return mDevice.getScreenDimension();
    }

    public int getDeviceMainOrientation() throws DeviceNotInitializedException {
        if (!checkInit())
            return -1;
        else
            return mDevice.getScreenMainOrientation();
    }

    public int getDeviceSystemType() throws DeviceNotInitializedException {
        if (!checkInit())
            return -1;
        else
            return mDevice.getDeviceSystemType();
    }

    // =============================
    //  DEVICE SCREEN GL
    // =============================
    public int getScreenshotSlotCount() {
        return mScreenService.getScreenshotSlotCount();
    }

    public int changeSlot(int index, boolean closeOld) {
        return mScreenService.changeSlot(index, closeOld);
    }

    public int getCurrentSlot() {
        return mScreenService.getCurrentSlot();
    }

    public void setScreenshotPolicy(int policy) {
        mScreenService.setScreenshotPolicy(policy);
    }

    public int requestRefresh() throws InterruptedException {
        return mScreenService.requestRefresh();
    }

    public ScreenColor getColorOnScreen(int index, ScreenCoord src, boolean refreshNeeded)
            throws InterruptedException, ScreenshotErrorException {
        return mScreenService.getColorOnScreen(index, src, refreshNeeded);
    }

    public ArrayList<ScreenColor> getMultiColorOnScreen(int index, ArrayList<ScreenCoord> coords, boolean refresh)
            throws InterruptedException, ScreenshotErrorException {
        return mScreenService.getMultiColorOnScreen(index, coords, refresh);
    }

    public boolean colorIs(ScreenPoint point) throws InterruptedException, ScreenshotErrorException {
        return mScreenService.colorIs(point);
    }

    public boolean colorsAre(ArrayList<ScreenPoint> points) throws InterruptedException, ScreenshotErrorException {
        return mScreenService.colorsAre(points);
    }

    public boolean colorsAreInRect(ScreenCoord rectLeftTop, ScreenCoord rectRightBottom, ArrayList<ScreenColor> colors)
            throws InterruptedException, ScreenshotErrorException  {
        return mScreenService.colorsAreInRect(rectLeftTop, rectRightBottom, colors);
    }


    //
    // utils
    //
    private boolean checkInit() throws DeviceNotInitializedException {
        boolean ret;
        ret = getDeviceInitialized();

        if (!ret) {
            throw new DeviceNotInitializedException("No legal initialized device associated with the GL. " +
                    "Have you called chooseDevice and initDevice correctly?");
        }

        return true;
    }

    public static class DeviceNotInitializedException extends Exception {
        public DeviceNotInitializedException(String message) {
            super(message);
        }
    }

    public static class ScreenshotErrorException extends Exception {
        int failReason;

        public ScreenshotErrorException(String message, int code) {
            super(message);
            failReason = code;

        }

        int getFailReason() {return failReason;}
    }
}
