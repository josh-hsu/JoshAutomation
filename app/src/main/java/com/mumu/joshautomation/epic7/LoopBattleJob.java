package com.mumu.joshautomation.epic7;

import android.util.Log;

import com.mumu.joshautomation.R;
import com.mumu.joshautomation.script.AutoJob;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.joshautomation.script.DefinitionLoader;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.ArrayList;

/**
 * AutoJobExample
 * An example workable script implementation
 */

public class LoopBattleJob extends AutoJob {
    private static final String TAG = "LoopBattleJob";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;
    private Epic7Routine mEpic;

    public static final String jobName = "第七史詩 連續戰鬥"; //give your job a name

    public LoopBattleJob() {
        super(jobName);

        /* JoshGameLibrary basic initial */
        mGL = JoshGameLibrary.getInstance();
        mGL.setGameOrientation(ScreenPoint.SO_Landscape);
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


    @Override
    public void onAutoCorrection(Object object) {
    }
    /*
     * setJobEventListener
     * called by caller to receiver your message
     */
    public void setJobEventListener(AutoJobEventListener el) {
        mListener = el;
        mEpic = new Epic7Routine(mGL, mListener);
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

            sendMessage("開始");
            while (shouldRunning) {
                if (mEpic.battleRoutine(100) == 0) {
                    sendMessage("完成100次判斷");
                } else {
                    shouldRunning = false;
                    sendMessage("錯誤!!");
                }
            }
        }

        public void run() {
            try {
                main();
            } catch (Exception e) {
                sendMessage("工作結束");
                Log.e(TAG, "Routine caught an exception " + e.getMessage());
            }
        }
    }
}
