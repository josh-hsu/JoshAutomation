package com.mumu.libjoshgame;

import com.mumu.libjoshgame.device.AndroidInternal;
import com.mumu.libjoshgame.device.NoxPlayer;

/**
 * Josh Game Library - Version 2.0
 *
 * JoshGameLibrary (GL)
 * This game control library require the following initial phase
 *
 * GameLibrary20 mGL = new GameLibrary20();
 * mGL.chooseDevice(DEVICE_TYPE);
 * mGL.setDeviceEssentials(Objects); //if needed
 * mGL.initDevice(Objects);
 */
public class GameLibrary20 {
    public static final String TAG = "GL20";
    public static final int DEVICE_TYPE_ANDROID_INTERNAL = 1;
    public static final int DEVICE_TYPE_NOX_PLAYER = 9;

    private GameDevice mDevice;

    public GameLibrary20() {

    }

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
        return ret;
    }
}
