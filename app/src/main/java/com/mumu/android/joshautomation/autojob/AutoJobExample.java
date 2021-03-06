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

package com.mumu.android.joshautomation.autojob;

import android.util.Log;

import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

/**
 * AutoJobExample
 * An example workable script implementation for GL20
 */

public class AutoJobExample extends AutoJob {
    private static final String TAG = "AutoJobExample";
    private MainJobRoutine mRoutine;
    private GameLibrary20 mGL;
    private AutoJobEventListener mListener;

    public static final String jobName = "GL20 Example Job"; //give your job a name

    public AutoJobExample(GameLibrary20 gl) {
        super(jobName);

        /* JoshGameLibrary basic initial */
        mGL = gl;

        if (mGL == null) {
            Log.e(TAG, "initial GL20 failed.");
        }
    }

    /*
     * start
     * called by AutoJobHandler to start MainJobRoutine
     */
    @Override
    public void start() {
        super.start();
        Log.d(TAG, "starting job " + getJobName());
        mRoutine = null;
        mRoutine = new MainJobRoutine();
        mRoutine.start();
    }

    /*
     * stop
     * called by AutoJobHandler to stop MainJobRoutine
     */
    @Override
    public void stop() {
        super.stop();
        Log.d(TAG, "stopping job " + getJobName());

        mRoutine.interrupt();
    }

    /*
     * setExtra
     * called by caller to set any data to you
     */
    @Override
    public void setExtra(Object object) {
        // You can receive any object from your caller
    }

    /*
     * setJobEventListener
     * called by caller to receiver your message
     */
    public void setJobEventListener(AutoJobEventListener el) {
        mListener = el;
    }

    /*
     * SendEvent
     * Your can send anything back to caller whoever register listener
     */
    private void sendEvent(String msg, Object extra) {
        if (mListener != null) {
            mListener.onMessageReceived(msg, extra);
        } else {
            Log.w(TAG, "There is no event listener registered.");
        }
    }

    private void sendMessage(String msg) {
        sendEvent(msg, this);
    }

    /*
     * MainJobRoutine
     * Your script implementation should be here
     */
    private class MainJobRoutine extends Thread {
        ScreenCoord pointScreenCenter = new ScreenCoord(500, 1090, ScreenPoint.SO_Portrait);
        ScreenCoord pointScreen1 = new ScreenCoord(100, 1090, ScreenPoint.SO_Portrait);
        ScreenCoord pointScreen2 = new ScreenCoord(900, 1990, ScreenPoint.SO_Portrait);

        private void main() throws Exception {
            boolean shouldRunning = true;

            while (shouldRunning) {
                // do your job here
                sendMessage("Starting job");

                // tap a screen coordination
                sleep(5000);
                mGL.mouseClick(pointScreenCenter);
                mGL.mouseSwipe(pointScreen1, pointScreen2);

                shouldRunning = false;
                sendMessage("Job is done");
            }
        }

        public void run() {
            try {
                main();
            } catch (Exception e) {
                Log.e(TAG, "Routine caught an exception " + e.getMessage());
            }
        }
    }
}
