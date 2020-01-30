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

package com.mumu.android.joshautomation.scripts.epic7;

import android.util.Log;

import com.mumu.android.joshautomation.autojob.AutoJob;
import com.mumu.android.joshautomation.autojob.AutoJobEventListener;
import com.mumu.android.joshautomation.content.AppPreferenceValue;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

public class Epic7AutoReplayJob extends AutoJob {
    private static final String TAG = "Epic7AutoReplay";
    private MainJobRoutine mRoutine;
    private GameLibrary20 mGL;
    private Epic7Routine mEpic7;
    private AutoJobEventListener mListener;

    public Epic7AutoReplayJob() {
        super(TAG);
    }

    /*
     * start
     * called by AutoJobHandler to start MainJobRoutine
     */
    @Override
    public void start() {
        super.start();
        Log.d(TAG, "starting job " + getJobName());
        mEpic7 = new Epic7Routine(mGL, mListener);
        mGL.useHardwareSimulatedInput(false);
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
        if (object instanceof GameLibrary20)
            mGL = (GameLibrary20) object;
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

        private void main() throws Exception {
            boolean shouldRunning = true;
            int battleCount = Integer.parseInt(AppPreferenceValue.getInstance().getPrefs().getString("epic7PerfBattleCount", "10"));
            int battleTimeout = Integer.parseInt(AppPreferenceValue.getInstance().getPrefs().getString("epic7PerfBattleTimeout", "120"));

            while (shouldRunning) {
                // setup gl for game spec
                mGL.setScreenMainOrientation(ScreenPoint.SO_Landscape);
                mGL.useHardwareSimulatedInput(false);
                mGL.setScreenAmbiguousRange(new int[]{25,25,25});

                mEpic7.battleRoutine(battleCount, battleTimeout*1000);

                shouldRunning = false;
                mListener.onJobDone(TAG);
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
