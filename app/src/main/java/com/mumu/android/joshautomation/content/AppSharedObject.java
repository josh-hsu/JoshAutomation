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

package com.mumu.android.joshautomation.content;

import com.mumu.libjoshgame.GameLibrary20;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * App Shared Object
 * This class holds the objects initialized by HeadService and available for
 * all classes in this domain
 */
public class AppSharedObject {
    private static AppSharedObject mSelf;
    private GameLibrary20 mGL;
    private ArrayList<LogMessage> mLogBuffer;
    private Lock mBufferLock;

    private AppSharedObject() {
        mLogBuffer = new ArrayList<>();
        mBufferLock = new ReentrantLock();
    }

    public static AppSharedObject getInstance() {
        if (mSelf == null)
            mSelf = new AppSharedObject();
        return mSelf;
    }

    public GameLibrary20 getGL20() {
        return mGL;
    }

    public void setGL20(GameLibrary20 gl20) {
        mGL = gl20;
    }

    public ArrayList<LogMessage> getLogBuffer() {
        return mLogBuffer;
    }

    public void cleanLogBuffer() {
        mBufferLock.lock();
        try {
            mLogBuffer.clear();
        } finally {
            mBufferLock.unlock();
        }
    }

    public void addLog(String log) {
        mBufferLock.lock();
        try {
            if (mLogBuffer != null)
                mLogBuffer.add(new LogMessage(log));
        } finally {
            mBufferLock.unlock();
        }
    }

    public static class LogMessage {
        private String dateString;
        private String logMsg;

        public LogMessage (String msg) {
            Date date = new Date();
            DateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.TAIWAN);
            dateString = sdf.format(date);
            logMsg = msg;
        }

        @Override
        public String toString() {
            return dateString + " " + logMsg;
        }
    }
}
