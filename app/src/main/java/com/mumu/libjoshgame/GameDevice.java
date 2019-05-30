package com.mumu.libjoshgame;

import java.io.RandomAccessFile;

/**
 * The interface describe how GL interacts with each device
 * <p>
 * the basic device is {@link com.mumu.libjoshgame.device.AndroidInternal} and
 * {@link com.mumu.libjoshgame.device.NoxPlayer} is also supported in this version
 * </p>
 */
public class GameDevice {
    private static final String TAG = JoshGameLibrary.TAG;

    protected boolean mInitialized = false;
    private String mDeviceName;
    private IGameDevice mDeviceInterface;
    private String[] mFileSlots;
    private int mFileSlotIndex;

    /**
     * Initial function should be override
     *
     * @param objects The object array for specific device initialization
     * @return 0 upon success
     */
    public int init(Object[] objects) {
        /* Override needed */
        return 0;
    }

    /**
     * Initial of the GameDevice, this method must be called in extended device
     *
     * @param deviceInterface The IGameDevice implementation of specific device
     * @param deviceName The name of the device
     * @return 0 if success, -1 if illegal arguments
     */
    protected int init(String deviceName, IGameDevice deviceInterface) {
        // verify the input parameters
        if (deviceName == null) {
            Log.e(TAG, "Initial for NULL device name is not allowed");
            return -1;
        }
        mDeviceName = deviceName;

        if (deviceInterface == null) {
            Log.e(TAG, "Initial for NULL device implement is not allowed");
            return -1;
        }
        mDeviceInterface = deviceInterface;

        return 0;
    }

    /**
     * Get the name of the device
     *
     * @return String of the name of device
     */
    public String getName() {
        return mDeviceName;
    }

    /**
     * Get if the device is full initialized
     * @return True if initial has been done
     */
    public boolean getInitialized() {
        return mInitialized;
    }

    public int[] getScreenDimension() {
        /* Override needed */
        return new int[] {0, 0};
    }

    public int getScreenMainOrientation() {
        /* Override needed */
        return 0;
    }

    /**
     * setDeviceEssentials
     * set special object that keep this device functional
     * this function might be useful for extended initialization
     *
     * @param object The object for extended initialization
     */
    public void setDeviceEssentials(Object object) {
        /* Override needed */
    }

    /**
     * sendDeviceCommand
     * send out device command, normally this should be used in test not publish
     *
     * @param synced Determine if you want to wait for command to finish.
     * @param cmd The command string.
     * @return The index of command results you can further query later
     */
    public int sendDeviceCommand(boolean synced, String cmd) {
        return 0;
    }

    /**
     * getDeviceCommandResult
     * return the command result in the index of the command result slot
     *
     * @param index The index of the command result slot
     * @return The result of the index in the command result slot
     */
    public String getDeviceCommandResult(int index) {
        return null;
    }

    /**
     * screenDump
     * make a screenshot at specific index of slot
     * if the previous screenshot is not closed yet, it will return an error if forced
     * is not set
     *
     * @param index The index of screenshot slot to save in
     * @param forced True if ignoring the screenshot is in use
     * @return 0 upon success
     */
    public int screenDump(int index, boolean forced) {
        return 0;
    }

    /**
     * screenshotOpen
     * get the file handle of specific screenshot at index of the slot
     * if the screenshot cannot be returned, an Exception will be thrown
     *
     * @param index The index of screenshot slot to open
     * @return 0 upon success
     */
    public RandomAccessFile screenshotOpen(int index) {
        return null;
    }

    /**
     * screenshotClose
     * close the file handle of specific screenshot at index of the slot
     * if the screenshot cannot be closed, an Exception will be thrown
     * if the screenshot is already closed, return error code.
     *
     * @param index The index of screenshot slot to close
     * @return 0 upon success
     */
    public int screenshotClose(int index) {
        return 0;
    }
}
