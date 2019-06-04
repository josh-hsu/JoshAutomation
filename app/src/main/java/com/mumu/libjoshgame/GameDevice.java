package com.mumu.libjoshgame;

import java.io.FileNotFoundException;
import java.io.IOException;
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

    /**
     * SCREENSHOT_EMPTY screenshot slot is fully released
     * SCREENSHOT_OPENED screenshot is opened and acquired by Application
     * SCREENSHOT_CLOSED screenshot is ready for use but not opened
     */
    public static final int SCREENSHOT_EMPTY  = 0;
    public static final int SCREENSHOT_OPENED = 1;
    public static final int SCREENSHOT_CLOSED = 2;

    protected boolean mInitialized = false;
    private String mDeviceName;
    private IGameDevice mDeviceInterface;

    private String[] mFilePaths;
    private int mFilePathCount;
    private RandomAccessFile[] mFileSlot;
    private int[] mFileState;

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

        mFilePaths = deviceInterface.queryPreloadedPaths();
        mFilePathCount = deviceInterface.queryPreloadedPathCount();
        mFileSlot = new RandomAccessFile[mFilePathCount];
        mFileState = new int[mFilePathCount];
        for(int i = 0; i < mFilePathCount; i++) {
            mFileState[i] = SCREENSHOT_EMPTY;
        }

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
     * is not set. Note this will not open a file description for use, just doing dump
     *
     * @param index The index of screenshot slot to save in
     * @param forced True if ignoring the screenshot is in use
     * @return 0 upon success
     */
    public int screenDump(int index, boolean forced) {
        int ret;
        // checking if index legal
        if (index < 0 || index > mFilePathCount) {
            return -3;
        }

        if (mFileState[index] == SCREENSHOT_OPENED) {
            if (forced) {
                Log.i(TAG, "screenshot is in use, force close it.");
                ret = screenshotClose(index);
                if (ret < 0) {
                    Log.e(TAG, "screenshot in slot " + index + " is not able to close, error: " + ret);
                    return -9;
                }
            } else {
                Log.w(TAG, "screenshot in slot " + index + " is in use.");
                return -10;
            }
        }

        Log.i(TAG, "trying to dump at index " + index + ", path is " + mFilePaths[index]);
        ret = mDeviceInterface.dumpScreen(mFilePaths[index]);
        if (ret < 0) {
            Log.e(TAG, "dumpscreen failed, ret = " + ret);
            mFileState[index] = SCREENSHOT_EMPTY;
            return -1;
        }
        mFileState[index] = SCREENSHOT_CLOSED;

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
        RandomAccessFile dumpFile;

        // checking if index legal
        if (index < 0 || index > mFilePathCount) {
            Log.w(TAG, "index " + index + " is not legal");
            return null;
        }

        if (mFileState[index] == SCREENSHOT_EMPTY) {
            Log.w(TAG, "screenshot is empty at index " + index);
            return null;
        }

        if (mFileState[index] == SCREENSHOT_CLOSED) {
            try {
                dumpFile = new RandomAccessFile(mFilePaths[index], "rw");
                mFileState[index] = SCREENSHOT_OPENED;
                mFileSlot[index] = dumpFile;
            } catch (FileNotFoundException e) {
                Log.e(TAG, "screenshot not found! file state might be wrong");
                return null;
            }
        }

        return mFileSlot[index];
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
        // checking if index legal
        if (index < 0 || index > mFilePathCount) {
            Log.w(TAG, "index " + index + " is not legal");
            return -3;
        }

        if (mFileState[index] == SCREENSHOT_EMPTY ||
                mFileState[index] == SCREENSHOT_CLOSED) {
            // already closed, do noting.
            return 0;
        }

        try {
            mFileSlot[index].close();
            mFileState[index] = SCREENSHOT_CLOSED;
        } catch (IOException e) {
            Log.e(TAG, "close this file error, release it.");
            mFileSlot[index] = null;
            mFileState[index] = SCREENSHOT_EMPTY;
        }

        return 0;
    }

    /**
     * screenshotRelease
     *
     * release and free the slot of screenshot
     * make the slot to SCREENSHOT_EMPTY state
     *
     * @param index The index of the slot you want to release
     * @return 0 upon success
     */
    public int screenshotRelease(int index) {
        // checking if index legal
        if (index < 0 || index > mFilePathCount) {
            Log.w(TAG, "index " + index + " is not legal");
            return -3;
        }

        if (mFileState[index] == SCREENSHOT_EMPTY ||
                mFileState[index] == SCREENSHOT_CLOSED) {
            mFileSlot[index] = null;
            mFileState[index] = SCREENSHOT_EMPTY;
            return 0;
        }

        Log.w(TAG, "screenshot at index " + index + " is in use. please close it first");
        return -2;
    }
}
