package com.mumu.libjoshgame;

import com.mumu.libjoshgame.service.Logger;

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
    private static final String TAG = GameLibrary20.TAG;

    public static final int LOG_VERBOSE = 0;
    public static final int LOG_DEBUG   = 1;
    public static final int LOG_WARNING = 2;
    public static final int LOG_ERROR   = 3;
    public static final int LOG_FATAL   = 4;

    public static final int DEVICE_SYS_WINDOWS = 0;
    public static final int DEVICE_SYS_LINUX   = 1;
    public static final int DEVICE_SYS_DARWIN  = 2;

    /**
     * SCREENSHOT_EMPTY screenshot slot is fully released
     * SCREENSHOT_OPENED screenshot is opened and acquired by Application
     * SCREENSHOT_CLOSED screenshot is ready for use but not opened
     */
    public static final int SCREENSHOT_EMPTY  = 0;
    public static final int SCREENSHOT_OPENED = 1;
    public static final int SCREENSHOT_CLOSED = 2;

    public static final int SCREENSHOT_IN_USE      = -10;
    public static final int SCREENSHOT_CLOSE_FAIL  = -9;
    public static final int SCREENSHOT_DUMP_FAIL   = -8;
    public static final int SCREENSHOT_INDEX_ERROR = -3;
    public static final int SCREENSHOT_NO_ERROR    = 0;

    public static final int MOUSE_TAP        = 0;
    public static final int MOUSE_DOUBLE_TAP = 1;
    public static final int MOUSE_TRIPLE_TAP = 2;
    public static final int MOUSE_PRESS      = 3;
    public static final int MOUSE_RELEASE    = 4;
    public static final int MOUSE_MOVE_TO    = 5;
    public static final int MOUSE_SWIPE      = 6;
    public static final int MOUSE_EVENT_MAX  = 7;

    protected boolean mInitialized = false;
    private String mDeviceName;
    private IGameDevice mDeviceInterface;
    private Logger mLogger;

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
            log(LOG_ERROR, TAG, "Initial for NULL device name is not allowed");
            return -1;
        }
        mDeviceName = deviceName;

        if (deviceInterface == null) {
            log(LOG_FATAL, TAG, "Initial for NULL device implement is not allowed");
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

        mLogger = new Logger(this);
        mInitialized = true;

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

    /**
     * Get the system type of device
     * @return The system type or -1 upon failure
     */
    public int getDeviceSystemType() {
        if (mDeviceInterface != null)
            return mDeviceInterface.getSystemType();
        else
            return -1;
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
     * Get the transaction waiting time of this device
     * @return The wait transaction time needed in milliseconds
     */
    public int getWaitTransactionTimeMs() {
        if (mDeviceInterface != null)
            return mDeviceInterface.getWaitTransactionTimeMs();
        else
            return -1;
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
     * Override the device wait transaction time do it on your own risk
     * @param ms The wait transaction time in milliseconds
     */
    public void setWaitTransactionTimeMs(int ms) {
        if (ms >= 0 && mDeviceInterface != null) {
            mDeviceInterface.setWaitTransactionTimeMsOverride(ms);
        }
    }

    /**
     * sendDeviceCommand
     * send out device command, normally this should be used in test not release version
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
     * If the previous screenshot is not closed yet, it will return an error. If forced
     * is not set. Note this will not open a file description for use, just doing dump
     * after a screen dump command is sent, it will sleep a period of time defined in function
     * getWaitTransactionTimeMs() to make screenshot ready to use.
     *
     * @param index The index of screenshot slot to save in
     * @param forced True if ignoring the screenshot is in use
     * @return 0 upon success
     */
    public int screenDump(int index, boolean forced) throws InterruptedException {
        int ret;
        // checking if index legal
        if (index < 0 || index > mFilePathCount) {
            return SCREENSHOT_INDEX_ERROR;
        }

        if (mFileState[index] == SCREENSHOT_OPENED) {
            if (forced) {
                log(LOG_DEBUG, TAG, "screenshot is in use, force close it.");
                ret = screenshotClose(index);
                if (ret < 0) {
                    log(LOG_ERROR, TAG, "screenshot in slot " + index + " is not able to close, error: " + ret);
                    return SCREENSHOT_CLOSE_FAIL;
                }
            } else {
                log(LOG_WARNING, TAG, "screenshot in slot " + index + " is in use.");
                return SCREENSHOT_IN_USE;
            }
        }

        log(LOG_DEBUG, TAG, "trying to dump at index " + index + ", path is " + mFilePaths[index]);
        ret = mDeviceInterface.dumpScreen(mFilePaths[index]);
        if (ret < 0) {
            Log.e(TAG, "dumpscreen failed, ret = " + ret);
            mFileState[index] = SCREENSHOT_EMPTY;
            return SCREENSHOT_DUMP_FAIL;
        }
        mFileState[index] = SCREENSHOT_CLOSED;

        // sleep a waiting time for screenshot truly ready
        Thread.sleep(getWaitTransactionTimeMs());

        return SCREENSHOT_NO_ERROR;
    }

    /**
     * query all screenshot slot state
     * @return All screenshot slot state
     */
    public int[] screenshotState() {
        return mFileState;
    }

    /**
     * query single screen shot state
     * @param index The index of the slot
     * @return The screenshot slot state of the index
     */
    public int screenshotState(int index) {
        return mFileState[index];
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
            log(LOG_WARNING, TAG, "index " + index + " is not legal");
            return null;
        }

        if (mFileState[index] == SCREENSHOT_EMPTY) {
            log(LOG_WARNING, TAG, "screenshot is empty at index " + index);
            return null;
        }

        if (mFileState[index] == SCREENSHOT_CLOSED) {
            try {
                dumpFile = new RandomAccessFile(mFilePaths[index], "rw");
                mFileState[index] = SCREENSHOT_OPENED;
                mFileSlot[index] = dumpFile;
            } catch (FileNotFoundException e) {
                log(LOG_ERROR, TAG, "screenshot not found! file state might be wrong");
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
            log(LOG_WARNING, TAG, "index " + index + " is not legal");
            return SCREENSHOT_INDEX_ERROR;
        }

        if (mFileState[index] == SCREENSHOT_EMPTY ||
                mFileState[index] == SCREENSHOT_CLOSED) {
            // already closed, do noting.
            return SCREENSHOT_NO_ERROR;
        }

        try {
            mFileSlot[index].close();
            mFileState[index] = SCREENSHOT_CLOSED;
        } catch (IOException e) {
            log(LOG_ERROR, TAG, "close this file error, release it.");
            mFileSlot[index] = null;
            mFileState[index] = SCREENSHOT_EMPTY;
        }

        return SCREENSHOT_NO_ERROR;
    }

    /**
     * screenshotRelease
     * release and free the slot of screenshot
     * make the slot to SCREENSHOT_EMPTY state
     *
     * @param index The index of the slot you want to release
     * @return 0 upon success
     */
    public int screenshotRelease(int index) {
        // checking if index legal
        if (index < 0 || index > mFilePathCount) {
            log(LOG_WARNING, TAG, "index " + index + " is not legal");
            return SCREENSHOT_INDEX_ERROR;
        }

        if (mFileState[index] == SCREENSHOT_EMPTY ||
                mFileState[index] == SCREENSHOT_CLOSED) {
            mFileSlot[index] = null;                //nullify the file slot as free the space
            mFileState[index] = SCREENSHOT_EMPTY;   //mark the state as EMPTY
            return SCREENSHOT_NO_ERROR;
        }

        log(LOG_WARNING, TAG, "screenshot at index " + index + " is in use. please close it first");
        return SCREENSHOT_IN_USE;
    }

    /**
     * query preloaded screenshot path count for indexing
     * @return Total length of screenshot path count
     */
    public int getScreenshotSlotCount() {
        if (mDeviceInterface == null) {
            throw new RuntimeException("Fatal exception that device interface is null");
        }

        return mFilePaths.length;
    }


    public int mouseInteract(int x, int y, int dx, int dy, int type) {
        if (mDeviceInterface == null) {
            throw new RuntimeException("Fatal exception that device interface is null");
        }

        return mDeviceInterface.mouseEvent(x, y, dx, dy, type);
    }

    public int mouseInteract(int x, int y, int type) {
        if (mDeviceInterface == null) {
            throw new RuntimeException("Fatal exception that device interface is null");
        }

        return mDeviceInterface.mouseEvent(x, y, 0, 0, type);
    }

    public String runShellCommand(String cmd) {
        if (mDeviceInterface == null) {
            throw new RuntimeException("Fatal exception that device interface is null");
        }

        return mDeviceInterface.runShellCommand(cmd);
    }

    /**
     * log to device
     * @param level The level defined in {@link GameLibrary20}
     * @param tag The tag of this message
     * @param msg Message to be logged
     */
    public void log(int level, String tag, String msg) {
        if (mDeviceInterface == null)
            throw new RuntimeException("Fatal exception that device interface is null");

        mDeviceInterface.logDevice(level, tag, msg);
        if (level == LOG_FATAL)
            throw new RuntimeException("Fatal exception detected, throwing stack trace.");
   }

    /**
     * get the {@link Logger} wrapper for this device
     * @return The Logger wrapper
     */
    public Logger getLogger() {
        return mLogger;
   }


    public int registerVibratorEvent(GameDeviceHWEventListener el) {
        if (mDeviceInterface == null)
            throw new RuntimeException("Fatal exception that device interface is null");

        return mDeviceInterface.registerVibratorEvent(el);
    }

    public int deregisterVibratorEvent(GameDeviceHWEventListener el) {
        if (mDeviceInterface == null)
            throw new RuntimeException("Fatal exception that device interface is null");

        return mDeviceInterface.deregisterVibratorEvent(el);
    }
}
