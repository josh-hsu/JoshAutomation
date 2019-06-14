package com.mumu.libjoshgame.service;

import com.mumu.libjoshgame.GameDevice;

/**
 * This class is the for logging in {@link com.mumu.libjoshgame.GameLibrary20}
 * Every log will be saved in text file, console and device itself.
 */
public class Logger {
    private GameDevice mDevice;

    public Logger(GameDevice device) {
        if (device == null)
            throw new RuntimeException("Logger: initial with null GameDevice");

        mDevice = device;
    }

    public void d(String tag, String msg) {
        mDevice.log(GameDevice.LOG_DEBUG, tag, msg);
    }

    public void e(String tag, String msg) {
        mDevice.log(GameDevice.LOG_ERROR, tag, msg);
    }

    public void w(String tag, String msg) {
        mDevice.log(GameDevice.LOG_WARNING, tag, msg);
    }

    public void f(String tag, String msg) {
        mDevice.log(GameDevice.LOG_FATAL, tag, msg);
    }

    public void i(String tag, String msg) {
        mDevice.log(GameDevice.LOG_VERBOSE, tag, msg);
    }

    public void v(String tag, String msg) {
        mDevice.log(GameDevice.LOG_VERBOSE, tag, msg);
    }
}
