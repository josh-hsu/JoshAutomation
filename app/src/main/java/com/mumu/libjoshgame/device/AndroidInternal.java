package com.mumu.libjoshgame.device;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;

import com.mumu.libjoshgame.GameDevice;
import com.mumu.libjoshgame.IGameDevice;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.Log;

import java.lang.reflect.Method;
import java.util.Map;

public class AndroidInternal extends GameDevice implements IGameDevice, ServiceConnection {
    private static final String TAG = JoshGameLibrary.TAG;
    private static String DEVICE_NAME = "AndroidInternal";
    private static String PRELOAD_PATH_INTERNAL = Environment.getExternalStorageDirectory().toString() + "/internal.dump";
    private static String PRELOAD_PATH_FIND_COLOR = Environment.getExternalStorageDirectory().toString() + "/find_color.dump";
    private static String PRELOAD_PATH_USER_SLOT_1 = Environment.getExternalStorageDirectory().toString() + "/user_slot_1.dump";
    private static String PRELOAD_PATH_USER_SLOT_2 = Environment.getExternalStorageDirectory().toString() + "/user_slot_2.dump";
    private static String PRELOAD_PATH_USER_SLOT_3 = Environment.getExternalStorageDirectory().toString() + "/user_slot_3.dump";
    private static String PRELOAD_PATH_USER_SLOT_4 = Environment.getExternalStorageDirectory().toString() + "/user_slot_4.dump";
    private static String PRELOAD_PATH_USER_SLOT_5 = Environment.getExternalStorageDirectory().toString() + "/user_slot_5.dump";
    private static String PRELOAD_PATH_USER_SLOT_6 = Environment.getExternalStorageDirectory().toString() + "/user_slot_6.dump";

    private String[] mPreloadedPath;
    private int mPreloadedPathCount;
    private Context mContext;

    private boolean mPMPathAvailable = false;
    private Method mRunCmdMethod;
    private boolean mHacked = false;
    private boolean mHackConnected = false;
    private IBinder mHackBinder;
    private String mSSPackageName, mSSServiceName, mSSInterfaceName;
    private int mSSCode = 0;

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

        if (objects[0] instanceof Context) {
            mContext = (Context) objects[0];
        } else {
            Log.e(TAG, "Initial for " + DEVICE_NAME + " error: the 1st object should be a Context");
            return -2;
        }

        if (objects[1] instanceof Map) {
            Map map = (Map) objects[1];
            String codeString = "0";
            if (map.containsKey("packageName")) mSSPackageName = (String)map.get("packageName");
            if (map.containsKey("serviceName")) mSSServiceName = (String)map.get("serviceName");
            if (map.containsKey("interfaceName")) mSSInterfaceName = (String)map.get("interfaceName");
            if (map.containsKey("code")) codeString = (String)map.get("code");

            try {
                mSSCode = Integer.parseInt(codeString);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Initial for " + DEVICE_NAME + " error: SSHack params code is not valid " + codeString);
                mSSCode = 0;
                return -3;
            }
        } else {
            Log.e(TAG, "Initial for " + DEVICE_NAME + " error: the 2nd object should be a Map<String,String>");
            return -3;
        }

        ret = initCmdProxy();
        if (ret < 0) {
            Log.e(TAG, "Initial for " + DEVICE_NAME + " error: command proxy init failed with " + ret);
            return -3;
        }

        ret = super.init(DEVICE_NAME, this);

        if (ret != 0) {
            mInitialized = false;
            return ret;
        }

        mPreloadedPath = new String[] {
                PRELOAD_PATH_INTERNAL,
                PRELOAD_PATH_FIND_COLOR,
                PRELOAD_PATH_USER_SLOT_1,
                PRELOAD_PATH_USER_SLOT_2,
                PRELOAD_PATH_USER_SLOT_3,
                PRELOAD_PATH_USER_SLOT_4,
                PRELOAD_PATH_USER_SLOT_5,
                PRELOAD_PATH_USER_SLOT_6,
        };
        mPreloadedPathCount = mPreloadedPath.length;

        return ret;
    }


    private int initCmdProxy() {
        try {
            Class<?>[] run_types = new Class[]{String.class, String.class};
            mRunCmdMethod = mContext.getPackageManager().getClass().getMethod("joshCmd", run_types);
            mPMPathAvailable = true;
        } catch (NoSuchMethodException e) {
            mPMPathAvailable = false;
            Log.e(TAG, "Sorry, your device is not support PackageManager command runner. Fix your sw or try HackBinder.");
        }

        // try to use binder connection
        if (!mPMPathAvailable) {

        }

        return 0;
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
    public int dumpScreen(String path) {
        return 0;
    }

    @Override
    public int runCommand(String command) {
        return 0;
    }

    /*
     * Implement of ServiceConnection
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
