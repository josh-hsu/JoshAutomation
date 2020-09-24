package com.mumu.android.joshautomation.scripts.konosuba;

import android.util.Log;

import com.mumu.android.joshautomation.autojob.AutoJob;
import com.mumu.android.joshautomation.autojob.AutoJobEventListener;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenPoint;

public class KonosubaAutoJob extends AutoJob {
    private static final String TAG = "KonosubaRoutine";
    private MainJobRoutine mRoutine;
    private GameLibrary20 mGL;
    private KonosubaRoutine mKonosuba;
    private AutoJobEventListener mListener;

    public KonosubaAutoJob() {
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
        mKonosuba = new KonosubaRoutine(mGL, mListener);
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

            while (shouldRunning) {
                // setup gl for game spec
                mGL.setScreenMainOrientation(ScreenPoint.SO_Landscape);
                mGL.useHardwareSimulatedInput(false);
                mGL.setScreenAmbiguousRange(new int[]{20,20,20});

                mKonosuba.battleRoutineDisordered();
            }
            mListener.onJobDone(TAG);
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
