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

package com.mumu.libjoshgame.service;

import com.mumu.libjoshgame.GameDevice;

/**
 * This class is for logging in {@link com.mumu.libjoshgame.GameLibrary20}
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
