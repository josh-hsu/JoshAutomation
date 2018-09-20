package com.mumu.joshautomation.fgo;

import android.util.Log;

import com.mumu.joshautomation.script.AutoJob;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import static com.mumu.joshautomation.fgo.FGORoutineDefine.*;

public class AutoBoxJob extends AutoJob {
    private static final String TAG = "AutoBoxJob";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;

    public static final String jobName = "Auto Box Open job"; //give your job a name

    public AutoBoxJob() {
        super(jobName);

        /* JoshGameLibrary basic initial */
        mGL = JoshGameLibrary.getInstance();
        mGL.setGameOrientation(ScreenPoint.SO_Landscape);
        mGL.setScreenDimension(1080, 1920);
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
            mListener.onEventReceived(msg, extra);
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
            int i = 0;

            while (shouldRunning) {
                sendMessage("Starting Box Opening job");

                while (i < 100) {
                    mGL.getInputService().tapOnScreen(pointBoxOpen);
                    sleep(500);
                    i++;
                }
                sendMessage("Rest Box");
                mGL.getInputService().tapOnScreen(pointBoxReset);
                sleep(2000);
                mGL.getInputService().tapOnScreen(pointBoxResetConfirm);
                sleep(2000);
                mGL.getInputService().tapOnScreen(pointBoxReseted);
                sleep(2000);

                i = 0;
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
