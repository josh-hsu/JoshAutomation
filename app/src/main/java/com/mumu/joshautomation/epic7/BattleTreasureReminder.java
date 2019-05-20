package com.mumu.joshautomation.epic7;

import android.util.Log;

import com.mumu.joshautomation.HeadService;
import com.mumu.joshautomation.R;
import com.mumu.joshautomation.script.AutoJob;
import com.mumu.joshautomation.script.AutoJobAction;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.joshautomation.script.DefinitionLoader;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.ArrayList;

/**
 * AutoJobExample
 * An example workable script implementation
 */

public class BattleTreasureReminder extends AutoJob {
    private static final String TAG = "BattleTreasureReminder";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;

    private DefinitionLoader.DefData mDef;

    public static final String jobName = "第七史詩寶箱提醒"; //give your job a name

    public BattleTreasureReminder() {
        super(jobName);

        /* JoshGameLibrary basic initial */
        mGL = JoshGameLibrary.getInstance();
        mGL.setGameOrientation(ScreenPoint.SO_Landscape);

        // FGO game 1080p related resolution should treat as the same
        // i.e., 1080x1920, 1080x2160, 1080x2246 ... etc are the same.
        String resolution = mGL.getScreenWidth() + "x" + mGL.getScreenHeight();
        if (mGL.getScreenWidth() == 1080)
            mDef = DefinitionLoader.getInstance().requestDefData(R.raw.epic7_definitions, "epic7_definitions.xml", "1080x2340");
        else
            mDef = DefinitionLoader.getInstance().requestDefData(R.raw.epic7_definitions, "epic7_definitions.xml", resolution);
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
        ArrayList<ScreenPoint> pointPost2ndBattle = mDef.getScreenPoints("pointPost2ndBattle");

        private void main() throws Exception {
            boolean shouldRunning = true;

            while (shouldRunning) {
                // do your job here
                sendMessage("偵測中..");

                if (mGL.getCaptureService().colorsAre(pointPost2ndBattle)) {
                    sendMessage("打完第二關了");
                    mGL.getInputService().tapOnScreen(mDef.getScreenCoord("pointAutoBattleButton"));
                    mGL.getInputService().playVibrateTime(3000);
                    sleep(3000);
                }

                sleep(100);
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
