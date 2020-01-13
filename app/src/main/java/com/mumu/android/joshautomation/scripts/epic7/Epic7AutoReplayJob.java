package com.mumu.android.joshautomation.scripts.epic7;

import android.util.Log;

import com.mumu.android.joshautomation.autojob.AutoJob;
import com.mumu.android.joshautomation.autojob.AutoJobEventListener;
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
        ScreenCoord pointScreenCenter = new ScreenCoord(500, 1090, ScreenPoint.SO_Portrait);
        ScreenCoord pointScreen1 = new ScreenCoord(100, 1090, ScreenPoint.SO_Portrait);
        ScreenCoord pointScreen2 = new ScreenCoord(900, 1990, ScreenPoint.SO_Portrait);

        private void main() throws Exception {
            boolean shouldRunning = true;

            while (shouldRunning) {
                // do your job here
                sendMessage("Starting job");

                // tap a screen coordination
                sleep(3000);
                mGL.mouseClick(pointScreenCenter);

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
