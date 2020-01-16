package com.mumu.libjoshgame.device;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import com.mumu.libjoshgame.GameDevice;
import com.mumu.libjoshgame.GameDeviceHWEventListener;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.IGameDevice;
import com.mumu.libjoshgame.ScreenPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * AndroidInternal
 * this class implements the old school JoshAutomation method of Android command executor
 * import of Android related files here only
 */
public class AndroidInternal extends GameDevice implements IGameDevice, ServiceConnection {
    private static final String TAG = GameLibrary20.TAG;
    private static final String DEVICE_NAME              = "AndroidInternal";
    private static final String DEVICE_VERSION           = "1.0.200113";
    private static final int    DEVICE_SYS_TYPE          = DEVICE_SYS_LINUX;
    private static final String PRELOAD_PATH_INTERNAL    = Environment.getExternalStorageDirectory().toString() + "/internal.dump";
    private static final String PRELOAD_PATH_FIND_COLOR  = Environment.getExternalStorageDirectory().toString() + "/find_color.dump";
    private static final String PRELOAD_PATH_USER_SLOT_0 = Environment.getExternalStorageDirectory().toString() + "/user_slot_0.dump";
    private static final String PRELOAD_PATH_USER_SLOT_1 = Environment.getExternalStorageDirectory().toString() + "/user_slot_1.dump";
    private static final String PRELOAD_PATH_USER_SLOT_2 = Environment.getExternalStorageDirectory().toString() + "/user_slot_2.dump";
    private static final String PRELOAD_PATH_USER_SLOT_3 = Environment.getExternalStorageDirectory().toString() + "/user_slot_3.dump";
    private static final String PRELOAD_PATH_USER_SLOT_4 = Environment.getExternalStorageDirectory().toString() + "/user_slot_4.dump";
    private static final String PRELOAD_PATH_USER_SLOT_5 = Environment.getExternalStorageDirectory().toString() + "/user_slot_5.dump";
    private static final String PRELOAD_PATH_USER_SLOT_6 = Environment.getExternalStorageDirectory().toString() + "/user_slot_6.dump";

    private String[] mPreloadedPath;
    private int mPreloadedPathCount;
    private Context mContext;

    private boolean mPMPathAvailable = false;
    private Method mRunCmdMethod;
    private boolean mHackRequest = false;
    private boolean mHackConnected = false;
    private IBinder mHackBinder;
    private String mSSPackageName, mSSServiceName, mSSInterfaceName;
    private int mSSCode = 0;

    private int mWaitTransactTime = 150;
    private boolean mUseHWSimulatedInput = false;

    private AndroidHardwareEventMonitor mVibratorMonitor;
    private AndroidHardwareInputHelper mHWInputHelper;

    /**
     * init for AndroidInternal device
     *
     * @param objects The object array for AndroidInternal requires the following sequence.
     *                objects[0]: Must be Context send from Activity or Service.
     *                objects[1]: Must be a Map<String, String> for HackSS initialization
     *                            the key should contains the following
     *                            {packageName, serviceName, interfaceName, code}.
     * @return 0 upon success
     */
    @Override
    public int init(Object[] objects) {
        int ret;

        if (objects.length != 2) {
            Log.e(TAG, "Initial for " + DEVICE_NAME + " error: should include 2 objects");
            return -1;
        }

        /* Android Context initial */
        if (objects[0] instanceof Context) {
            mContext = (Context) objects[0];
        } else {
            Log.e(TAG, "Initial for " + DEVICE_NAME + " error: the 1st object should be a Context");
            return -2;
        }

        /* HackSS initial */
        if (objects[1] instanceof Map) {
            Map map = (Map) objects[1];
            String codeString;

            if (map.containsKey("packageName") && map.containsKey("serviceName") &&
                    map.containsKey("interfaceName") && map.containsKey("code")) {
                mSSPackageName = (String)map.get("packageName");
                mSSServiceName = (String)map.get("serviceName");
                mSSInterfaceName = (String)map.get("interfaceName");
                codeString = (String)map.get("code");

                try {
                    mSSCode = Integer.parseInt(codeString);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Initial for " + DEVICE_NAME + " error: SSHack params code is not valid " + codeString);
                    mSSCode = 0;
                    return -3;
                }
            } else {
                Log.e(TAG, "Initial for " + DEVICE_NAME + " error: illegal hack SS parameters");
                return -3;
            }
        } else {
            Log.e(TAG, "Initial for " + DEVICE_NAME + " error: the 2nd object should be a Map<String,String>");
            return -4;
        }

        /* initial for command proxy, we have PMPath and HackSS two ways to send command */
        ret = initCmdProxy();
        if (ret < 0) {
            Log.e(TAG, "Initial for " + DEVICE_NAME + " error: command proxy init failed with " + ret);
            return -3;
        }

        ret = initDeviceHWInterface();
        if (ret < 0) {
            Log.e(TAG, "Initial for " + DEVICE_NAME + " error: device hw interface init failed with " + ret);
            return -5;
        }

        mPreloadedPath = new String[] {
                PRELOAD_PATH_USER_SLOT_0,
                PRELOAD_PATH_USER_SLOT_1,
                PRELOAD_PATH_USER_SLOT_2,
                PRELOAD_PATH_USER_SLOT_3,
                PRELOAD_PATH_USER_SLOT_4,
                PRELOAD_PATH_USER_SLOT_5,
                PRELOAD_PATH_USER_SLOT_6,
                //PRELOAD_PATH_INTERNAL,   //internal path is deprecated
                //PRELOAD_PATH_FIND_COLOR, //find color path is deprecated
        };
        mPreloadedPathCount = mPreloadedPath.length;

        /* initial for this device is fully done, calling super's init */
        ret = super.init(DEVICE_NAME, this);

        if (ret != 0) {
            mInitialized = false;
            return ret;
        }

        mInitialized = true;
        return ret;
    }

    private int initCmdProxy() {
        int ret = 0;

        try {
            Class<?>[] run_types = new Class[]{String.class, String.class};
            mRunCmdMethod = mContext.getPackageManager().getClass().getMethod("joshCmd", run_types);
            mPMPathAvailable = true;
            return 0;
        } catch (NoSuchMethodException e) {
            mPMPathAvailable = false;
            Log.w(TAG, "Sorry, your device is not support PackageManager command runner. Fix your sw or try HackBinder.");
        }

        // try to use binder connection
        // from here, the hackSS parameters should be ready
        if (!mPMPathAvailable) {
            Log.d(TAG, "Try to set HackSS true");
            ret = setHackSS(true);
        }

        return ret;
    }

    private int initDeviceHWInterface() {
        // currently we only support vibrator
        mVibratorMonitor = new AndroidHardwareEventMonitor("/sys/devices/platform/soc/c440000.qcom,spmi/spmi-0/spmi0-03/c44*haptics*/state",
                50,
                HW_EVENT_CB_ONCHANGE
                );
        mVibratorMonitor.startMonitoring();

        // initial hardware input helper
        mHWInputHelper = new AndroidHardwareInputHelper();
        return 0;
    }

    @Override
    public int[] getScreenDimension() {
        String wmResult = runShellCommand("wm size");
        String[] wmSize = wmResult.split(":");

        if (wmSize.length == 2) {
            String sizeString = wmSize[1];
            String[] sizeXY = sizeString.split("x");

            if (sizeXY.length == 2) {
                if (sizeXY[0].startsWith(" "))
                    sizeXY[0] = sizeXY[0].substring(1);

                try {
                    int[] ret = new int[2];
                    ret[0] = Integer.parseInt(sizeXY[0]);
                    ret[1] = Integer.parseInt(sizeXY[1]);
                    Log.d(TAG, "screen dimension is [" + ret[0] + "] x [" + ret[1] + "]");

                    return ret;
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Parse size error " + sizeString);
                }
            }
        }

        Log.e(TAG, "wm size returned error " + wmResult);
        return null;
    }

    @Override
    public int getScreenMainOrientation() {
        // This simulates an Android phone device, the real orientation will be override by user
        return ScreenPoint.SO_Portrait;
    }

    @Override
    public boolean getInitialized() {
        if (!mInitialized)
            return false;

        if (mPMPathAvailable)
            return true;

        return mHackRequest && mHackConnected;
    }

    /*
     * Implement of IGameDevice
     */
    @Override
    public String[] queryPreloadedPaths() {
        return mPreloadedPath;
    }

    @Override
    public int queryPreloadedPathCount() {
        return mPreloadedPathCount;
    }

    @Override
    public String getVersion() {
        return DEVICE_VERSION;
    }

    @Override
    public int getSystemType() {
        return DEVICE_SYS_TYPE;
    }

    @Override
    public int getWaitTransactionTimeMs() {
        return mWaitTransactTime;
    }

    @Override
    public void setWaitTransactionTimeMsOverride(int ms) {
        mWaitTransactTime = ms;
    }

    @Override
    public int setHWSimulatedInput(boolean enable) {
        mUseHWSimulatedInput = enable;
        return 0;
    }

    @Override
    public int dumpScreen(String path) {
        return runCommand("screencap " + path);
    }

    @Override
    public int dumpScreenPng(String path) {
        return runCommand("screencap -p " + path);
    }

    private int mouseEventHWSimulated(int x, int y, int tx, int ty, int event) {
        switch (event) {
            case MOUSE_TAP:
                mHWInputHelper.tap(x, y);
                break;
            case MOUSE_DOUBLE_TAP:
                mHWInputHelper.tap(x, y);
                mHWInputHelper.tap(x, y);
                break;
            case MOUSE_TRIPLE_TAP:
                mHWInputHelper.tap(x, y);
                mHWInputHelper.tap(x, y);
                mHWInputHelper.tap(x, y);
                break;
            case MOUSE_PRESS:
                mHWInputHelper.press(x, y);
                break;
            case MOUSE_RELEASE:
                mHWInputHelper.release(x, y);
                break;
            case MOUSE_MOVE_TO:
                mHWInputHelper.moveTo(x, y);
                break;
            case MOUSE_SWIPE:
                mHWInputHelper.swipe(x, y, tx, ty);
                break;
            default: //should not happen
                break;
        }
        return 0;
    }

    @Override
    public int mouseEvent(int x, int y, int tx, int ty, int event) {
        if (event < 0 || event >= MOUSE_EVENT_MAX) {
            throw new IllegalArgumentException("Unknown mouse event " + event);
        }

        if (mUseHWSimulatedInput)
            return mouseEventHWSimulated(x, y, tx, ty, event);

        switch (event) {
            case MOUSE_TAP:
                runCommand("input tap " + x + " " + y);
                break;
            case MOUSE_DOUBLE_TAP:
                runCommand("input tap " + x + " " + y);
                runCommand("input tap " + x + " " + y);
                break;
            case MOUSE_TRIPLE_TAP:
                runCommand("input tap " + x + " " + y);
                runCommand("input tap " + x + " " + y);
                runCommand("input tap " + x + " " + y);
                break;
            case MOUSE_PRESS:
                break;
            case MOUSE_RELEASE:
                break;
            case MOUSE_MOVE_TO:
                break;
            case MOUSE_SWIPE:
                runCommand("input swipe " + x + " " + y + " " + tx + " " + ty);
                break;
            default: //should not happen
                break;
        }

        return 0;
    }

    @Override
    public int registerEvent(int type, GameDeviceHWEventListener el) {
        switch (type) {
            case HW_EVENT_VIBRATOR:
                mVibratorMonitor.addListener(el);
                return 0;
            case HW_EVENT_PROXIMITY:
                break;
            case HW_EVENT_SOUND:
                break;
            default:
                break;
        }

        Log.w(TAG, "AndroidInternal doesn't support hardware type " + type);
        return -3;
    }

    @Override
    public int deregisterEvent(int type, GameDeviceHWEventListener el) {
        switch (type) {
            case HW_EVENT_VIBRATOR:
                mVibratorMonitor.removeListener(el);
                return 0;
            case HW_EVENT_PROXIMITY:
                break;
            case HW_EVENT_SOUND:
                break;
            default:
                break;
        }

        Log.w(TAG, "AndroidInternal doesn't support hardware type " + type);
        return -3;
    }

    @Override
    public String runShellCommand(String shellCmd) {
        String[] cmd = {"/system/bin/sh", "-c", shellCmd};
        StringBuilder sb = new StringBuilder();

        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String line;
            // append newline at each readLine
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            // delete the last newline for consistent
            if (sb.length() > 1)
                sb.deleteCharAt(sb.length() - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    @Override
    public int runCommand(String cmd) {
        Log.d(TAG, "cmd: " + cmd);
        try {
            if (!mHackRequest && mPMPathAvailable) {
                if (mInitialized) {
                    mRunCmdMethod.invoke(mContext, cmd, "");
                } else {
                    return -100;
                }
            } else if (mHackRequest && mHackConnected) {
                if (mHackBinder != null) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    if (mSSInterfaceName != null && !mSSInterfaceName.equals(""))
                        data.writeInterfaceToken(mSSPackageName + mSSInterfaceName);
                    data.writeString(cmd);
                    try {
                        mHackBinder.transact(mSSCode, data, reply, 0);
                    } catch (RemoteException e) {
                        Log.w(TAG, "transact failed " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    return -200;
                }
            } else {
                Log.e(TAG, "No command sending path, neither PPM path nor HackSS path is available.");
                return -1;
            }
        } catch (Exception e)  {
            e.printStackTrace();
            return -2;
        }

        return 0;
    }

    @Override
    public int onStart() {
        return 0;
    }

    @Override
    public int onExit() {
        setHackSS(false);
        mVibratorMonitor.stopMonitoring();
        return 0;
    }

    @Override
    public void logDevice(int level, String tag, String msg) {
        switch(level) {
            case LOG_VERBOSE:
                android.util.Log.v(tag, msg);
                break;
            case LOG_DEBUG:
                android.util.Log.d(tag, msg);
                break;
            case LOG_WARNING:
                android.util.Log.w(tag, msg);
                break;
            case LOG_ERROR:
                android.util.Log.e(tag, msg);
                break;
            case LOG_FATAL:
                android.util.Log.wtf(tag, msg);
                break;
            default:
                android.util.Log.i(tag, msg);
                break;
        }
    }

    /*
     * HackSS basic
     */
    private int setHackSS(boolean hack) {
        int ret = 0;
        mHackRequest = hack;
        if (mHackRequest && !mHackConnected)
            ret = connectToHackSS();
        else if (!mHackRequest && mHackConnected)
            ret = disconnectToHackSS();

        return ret;
    }

    synchronized private int connectToHackSS() {
        if (!mHackConnected) {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(mSSPackageName,
                        mSSPackageName + mSSServiceName));
                if (!mContext.bindService(intent, this, Service.BIND_AUTO_CREATE)) {
                    Log.w(TAG, "Cannot hack this device, parameter wrong or you don't have a implemented service");
                    mHackConnected = false;
                }
            } catch (SecurityException e) {
                Log.e(TAG, "can't bind to Service. Your service is not implemented correctly");
                return -1;
            }
        } else {
            Log.d(TAG, "Hack service is already connected");
        }
        return 0;
    }

    synchronized private int disconnectToHackSS() {
        if (mHackConnected && mHackBinder != null) {
            mContext.unbindService(this);
            mHackBinder = null;
        }

        return 0;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mHackConnected = true;
        mHackBinder = service;
        Log.d(TAG, "Hack service connected.");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mHackConnected = false;
        Log.d(TAG, "Hack service disconnected.");
    }

    /**
     * Android Hardware Event Monitor
     * created for monitoring supported hardware event and callback to listeners
     */
    private class AndroidHardwareEventMonitor extends Thread {
        private String fileMonitoringPath;
        private int monitorPeriodMs;
        private int monitorCallbackType;
        private ArrayList<GameDeviceHWEventListener> listeners;
        private boolean isLooping;
        private int returnedValue;

        AndroidHardwareEventMonitor(String path, int period, int cbType) {
            fileMonitoringPath = path;
            monitorPeriodMs = period;
            monitorCallbackType = cbType;

            returnedValue = 0;
            isLooping = false;
            listeners = new ArrayList<>();
        }

        void startMonitoring() {
            this.start();
        }

        void stopMonitoring() {
            this.interrupt();
        }

        boolean isMonitoring() {
            return isLooping;
        }

        void addListener(GameDeviceHWEventListener l) {
            if (!listeners.contains(l)) {
                listeners.add(l);
            } else {
                Log.w(TAG, "This listener is already in list");
            }

            listeners.add(l);
        }

        void removeListener(GameDeviceHWEventListener l) {
            listeners.remove(l);
        }

        void sendEventToListener(int type, Object data) {
            for(GameDeviceHWEventListener l : listeners) {
                l.onEvent(type, data);
            }
        }

        @Override
        public void run() {
            String cmd = "cat " + fileMonitoringPath;
            String value = "";

            while (isLooping) {
                try {
                    if(!getInitialized()) {
                        Log.d(TAG, "Device is not connected to hackSS, wait for next second.");
                        Thread.sleep(1000);
                        continue;
                    }

                    value = runShellCommand(cmd);
                    Integer intValue = Integer.parseInt(value);

                    switch (monitorCallbackType) {
                        case HW_EVENT_CB_ONCHANGE:
                            if (intValue != returnedValue) {
                                Log.d(TAG, "Hardware event on changed: value = " + intValue);
                                returnedValue = intValue;
                                sendEventToListener(HW_EVENT_CB_ONCHANGE, intValue);
                            }
                            break;
                        case HW_EVENT_CB_NEW_VALUE:
                            Log.d(TAG, "Hardware event new value: value = " + intValue);
                            sendEventToListener(HW_EVENT_CB_NEW_VALUE, intValue);
                            break;
                        default:
                            Log.e(TAG, "Unsupported callback type, abort here.");
                            isLooping = false;
                            break;
                    }

                    Thread.sleep(monitorPeriodMs);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "The path " + fileMonitoringPath + " returned unsupported value " + value);
                    isLooping = false;
                } catch (InterruptedException e) {
                    Log.w(TAG, "The path " + fileMonitoringPath + " monitor was interrupted.");
                    isLooping = false;
                }
            }
        }

        @Override
        public void start() {
            isLooping = true;
            super.start();
        }
    }

    /**
     * Android Hardware Input Helper
     * created for hardware simulate touch event
     */
    private class AndroidHardwareInputHelper {
        int EV_SYN = 0;
        int EV_ABS = 3;
        int EV_BTN = 1;
        int ABS_MT_TRACKING_ID = 0x0039; //57
        int ABS_MT_PRESSURE    = 0x0030; //48
        int ABS_MT_POSITION_X  = 0x0035; //53
        int ABS_MT_POSITION_Y  = 0x0036; //54
        int BTN_TOUCH          = 0x014a; //330
        int SYN_REPORT         = 0x0000;
        int TOUCH_DOWN = 1;
        int TOUCH_UP   = 0;
        int SYNC       = 0;

        private String devicePath = "/dev/input/event6"; //default path

        AndroidHardwareInputHelper() {
            String path = getTouchDevicePath();
            if (path != null)
                devicePath = path;
        }

        AndroidHardwareInputHelper(String path) {
            devicePath = path;
        }

        private String getTouchDevicePath() {
            String getEvent = "getevent -i";
            String grepCmd = "grep -B 10 KEY | grep -B 10 0011 | grep device | awk '{split($0,a,\":\"); print a[2]}'";
            String result = runShellCommand(getEvent + " | " + grepCmd);
            Log.d(TAG, "The touch device path of this Android device is " + result);

            if (result.contains("/dev/input"))
                return result;

            return null;
        }

        private int getPressure() {
            int max = 0x23;
            int min = 0x13;
            Random r = new Random();
            return r.nextInt((max - min) + 1) + min;
        }

        private void delayMs(int milli) {
            try {
                Thread.sleep(milli);
            } catch (InterruptedException e) {
                Log.d(TAG, "Tap delay interrupted.");
            }
        }

        private void sendTouchSync() {
            String syncCommand = "sendevent " + devicePath + " " + EV_SYN + " " + SYN_REPORT + " " + SYNC;
            runCommand(syncCommand);
        }

        private void updateTouchPos(int x, int y) {
            String pressureCmd = "sendevent " + devicePath + " " + EV_ABS + " " + ABS_MT_PRESSURE + " " + getPressure();
            String tapCommandX = "sendevent " + devicePath + " " + EV_ABS + " " + ABS_MT_POSITION_X + " " + x;
            String tapCommandY = "sendevent " + devicePath + " " + EV_ABS + " " + ABS_MT_POSITION_Y + " " + y;
            runCommand(pressureCmd);
            runCommand(tapCommandX);
            runCommand(tapCommandY);
        }

        private void sendTouchDown() {
            String downCommand = "sendevent " + devicePath + " " + EV_BTN + " " + BTN_TOUCH + " " + TOUCH_DOWN;
            runCommand(downCommand);
        }

        private void sendTouchUp() {
            String downCommand = "sendevent " + devicePath + " " + EV_BTN + " " + BTN_TOUCH + " " + TOUCH_UP;
            runCommand(downCommand);
        }

        public void tap(int x, int y) {
            press(x, y);
            release();
            delayMs(50);
        }

        public void swipe(int x1, int y1, int x2, int y2) {
            Log.e(TAG, "swipe not supported currently might cause issue");
            press(x1, y2);
            moveTo(x2, y2);
            release();
        }

        public void press(int x, int y) {
            updateTouchPos(x, y);
            sendTouchDown();
            sendTouchSync();
            delayMs(30);
        }

        public void moveTo(int x, int y) {
            updateTouchPos(x, y);
            sendTouchSync();
            delayMs(30);
        }

        public void release(int x, int y) {
            updateTouchPos(x, y);
            sendTouchUp();
            sendTouchSync();
            delayMs(30);
        }

        public void release() {
            sendTouchUp();
            sendTouchSync();
            delayMs(30);
        }
    }
}
