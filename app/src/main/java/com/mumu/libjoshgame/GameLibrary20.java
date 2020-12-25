/*
 * Copyright (C) 2020 The Josh Tool Project
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

import com.mumu.libjoshgame.device.AndroidInternal;
import com.mumu.libjoshgame.device.NoxPlayer;
import com.mumu.libjoshgame.service.DeviceInteract;
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

    private boolean        mDeviceReady;
    private GameDevice     mDevice;
    private DeviceScreen   mScreenService;
    private DeviceInteract mInteractService;

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
        mInteractService = new DeviceInteract(this, mDevice);

        //this is for debug only
        //TODO: remove this when release
        //initGameLibraryInternalDefaultParams();
    }

    private void initGameLibraryInternalDefaultParams() {
        mScreenService.setScreenshotPolicy(DeviceScreen.POLICY_DEFAULT);
        mScreenService.setAmbiguousRange(new int[] {0x0A, 0x0A, 0x0A});
        mInteractService.setGameOrientation(ScreenPoint.SO_Landscape);
        mInteractService.setMouseInputShift(0x06);
    }

    // =============================
    //  DEVICE FUNCTIONS
    // =============================
    private boolean getDeviceInitialized() {
        return mDeviceReady && mDevice.getInitialized();
    }

    public GameDevice getDevice() {
        return mDevice;
    }

    public String getDeviceName() {
        if (!checkInit())
            return null;
        else
            return mDevice.getName();
    }

    public int[] getDeviceResolution() {
        if (!checkInit())
            return null;
        else
            return mDevice.getScreenDimension();
    }

    public int getDeviceMainOrientation() {
        if (!checkInit())
            return -1;
        else
            return mDevice.getScreenMainOrientation();
    }

    public int getDeviceSystemType() {
        if (!checkInit())
            return -1;
        else
            return mDevice.getDeviceSystemType();
    }

    public int setChatty(boolean chatty) {
        if (!checkInit())
            return -1;

        mScreenService.setChatty(chatty);
        return 0;
    }

    public int setScreenResolution(int width, int height) {
        if (!checkInit())
            return -1;

        mScreenService.setScreenDimension(width, height);
        mInteractService.setScreenDimension(width, height);
        return 0;
    }

    public int setScreenMainOrientation(int orient) {
        if (!checkInit())
            return -1;

        mScreenService.setGameOrientation(orient);
        mInteractService.setGameOrientation(orient);
        return 0;
    }

    public int setScreenAmbiguousRange(int[] range) {
        if (!checkInit())
            return -1;

        if(range.length != 3)
            return -2;

        mScreenService.setAmbiguousRange(range);
        return 0;
    }

    public int setScreenOffset(int xOffset, int yOffset, int offsetOrientation) {
        if (!checkInit())
            return -1;

        if (offsetOrientation == ScreenPoint.SO_Landscape) {
            mInteractService.setScreenOffset(yOffset, xOffset);
            mScreenService.setScreenOffset(yOffset, xOffset);
        } else {
            mInteractService.setScreenOffset(xOffset, yOffset);
            mScreenService.setScreenOffset(xOffset, yOffset);
        }

        return 0;
    }

    public int setMouseShift(int ran) {
        if (!checkInit())
            return -1;

        mInteractService.setMouseInputShift(ran);
        return 0;
    }

    public int setDeviceCommandTransactionTime(int transTimeMs) {
        if (!checkInit())
            return -1;

        mDevice.setWaitTransactionTimeMs(transTimeMs);
        return 0;
    }

    public int useHardwareSimulatedInput(boolean enable) {
        if (!checkInit())
            return -1;

        mDevice.setHardwareSimulatedInput(enable);
        return 0;
    }

    // =============================
    //  DEVICE SCREEN GL
    // =============================
    public int getScreenshotSlotCount() {
        return mScreenService.getScreenshotSlotCount();
    }

    public int setActiveSlot(int index, boolean closeOld) {
        return mScreenService.setActiveSlot(index, closeOld);
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

    public boolean waitOnColor(ScreenPoint point, int timeoutMs) throws InterruptedException, ScreenshotErrorException {
        final int checkInterval = 100; //100 ms check frequency
        int checkCount = timeoutMs / 100;

        while (!mScreenService.colorIs(point) && checkCount > 0) {
            //colors are not matched, sleep in..
            Thread.sleep(checkInterval);
        }

        // return true is waiting successful
        return checkCount > 0;
    }

    /**
     * waiting on screen matching all colors in the points
     * @param points The point set of target coordination and color
     * @param timeoutMs Timeout in milliseconds
     * @return Ture if matching, False if timed out
     * @throws InterruptedException If user has stopped the script
     * @throws ScreenshotErrorException If screenshot cannot be done
     */
    public boolean waitOnColors(ArrayList<ScreenPoint> points, int timeoutMs) throws InterruptedException, ScreenshotErrorException {
        final int checkInterval = 100; //100 ms check frequency
        int checkCount = timeoutMs / checkInterval;

        while (!mScreenService.colorsAre(points) && checkCount-- > 0) {
            //colors are not matched, sleep in..
            Thread.sleep(checkInterval);
        }

        // return true is waiting successful
        return checkCount > 0;
    }

    /**
     * Added in 2.02
     * waiting on screen to match one of the ScreenPoint set
     * and returns the index of the set
     * @param points The ArrayList of the point set
     * @param timeoutMs Timeout in milliseconds
     * @return The index of matching set, -1 if timeout happened
     * @throws InterruptedException If user has stopped the script
     * @throws ScreenshotErrorException If screenshot cannot be done
     */
    public int waitOnMatchingColorSets(ArrayList<ArrayList<ScreenPoint>> points, int timeoutMs) throws InterruptedException, ScreenshotErrorException {
        final int checkInterval = 100; //100 ms check frequency
        int checkCount = timeoutMs / checkInterval;

        while (checkCount-- > 0) {
            requestRefresh();
            setScreenshotPolicy(DeviceScreen.POLICY_MANUAL);
            for(int i = 0; i < points.size(); i++) {
                if (mScreenService.colorsAre(points.get(i)))
                    return i;
            }
            setScreenshotPolicy(DeviceScreen.POLICY_DEFAULT);
            //colors are not matched, sleep in..
            Thread.sleep(checkInterval);
        }

        // return true is waiting successful
        return -1;
    }

    public void dumpScreenshotManual(String path) {
        mDevice.dumpScreenManual(path);
    }

    // =============================
    //  DEVICE INPUT GL
    // =============================
    public int mouseClick(ScreenCoord coord) {
        return mInteractService.mouseClick(coord);
    }

    public int mouseDoubleClick(ScreenCoord coord) {
        return mInteractService.mouseDoubleClick(coord);
    }

    public int mouseTripleClick(ScreenCoord coord) {
        return mInteractService.mouseTripleClick(coord);
    }

    public int mouseDown(ScreenCoord coord) {
        return mInteractService.mouseDown(coord);
    }

    public int mouseMoveTo(ScreenCoord coord) {
        return mInteractService.mouseMoveTo(coord);
    }

    public int mouseUp(ScreenCoord coord) {
        return mInteractService.mouseUp(coord);
    }

    public int mouseSwipe(ScreenCoord coordStart, ScreenCoord coordEnd) {
        return mInteractService.mouseSwipe(coordStart, coordEnd);
    }

    public void waitUntilVibrate(int timeoutMs) throws InterruptedException {
        mInteractService.waitUntilDeviceVibrate(timeoutMs);
    }

    //
    // utils
    //
    private boolean checkInit() {
        return getDeviceInitialized();
    }

    //
    // exceptions
    //
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

    public static class DeviceInputErrorException extends Exception {
        public static final int ERROR_NO_SUCH_METHOD = 0;
        public static final int ERROR_MOUSE_EVENT_TIMEOUT = 1;
        public static final int ERROR_OTHERS = 2;
        int failReason;

        public DeviceInputErrorException(String message, int code) {
            super(message);
            failReason = code;
        }

        public int getFailReason() {
            return failReason;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Device input error ");
            switch (failReason) {
                case ERROR_NO_SUCH_METHOD:
                    sb.append("No such method.");
                    break;
                case ERROR_MOUSE_EVENT_TIMEOUT:
                    sb.append("Mouse event timeout or failure, please check your device.");
                    break;
                case ERROR_OTHERS:
                    sb.append("Other type of error check log to find out.");
                    break;
                default:
                    sb.append("Unknown type of error.");
                    break;
            }
            return sb.toString();
        }
    }
}
