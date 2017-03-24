package com.mumu.joshautomation.script;

import android.util.Log;

import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

import static com.mumu.joshautomation.script.AutoBattleJobDefine.*;

public class AutoBattleJob extends AutoJobHandler.AutoJob {
    private static final String TAG = "AutoBattleJob";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;

    AutoBattleJob(String jobName, int jobIndex) {
        super(jobName, jobIndex);

        /* JoshGameLibrary basic initial */
        mGL = JoshGameLibrary.getInstance();
        mGL.setGameOrientation(ScreenPoint.SO_Landscape);
        mGL.setScreenDimension(1080, 1920);
    }

    @Override
    public void start() {
        super.start();
        Log.d(TAG, "starting job " + getJobName());
        mRoutine = null;
        mRoutine = new MainJobRoutine();
        mRoutine.start();
    }

    @Override
    public void stop() {
        super.stop();
        Log.d(TAG, "stopping job " + getJobName());

        if (mRoutine != null)
            mRoutine.interrupt();
    }

    @Override
    public void setExtra(Object object) {

    }

    public void setJobEventListener(AutoJobEventListener el) {
        mListener = el;
    }

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

    private class MainJobRoutine extends Thread {

        private void main() throws Exception {
            boolean shouldRunning = true;

            mGL.setGameOrientation(ScreenPoint.SO_Landscape);
            mGL.setAmbiguousRange(0x0A);

            while (shouldRunning) {
                /*sleep(1000);
                sendMessage("Wait for Home");

                mGL.getCaptureService().waitOnColor(pointIntroPage, 60, this);*/

                sendMessage("Checking ...");
                sleep(500);

                String cardInfo = "";

                for(int i = 0; i < 5; i++) {
                    if (mGL.getCaptureService().findColorInRange(
                            cardPositionStart.get(i),
                            cardPositionEnd.get(i),
                            cardArt)) {
                        cardInfo += "A";
                    } else if (mGL.getCaptureService().findColorInRange(
                            cardPositionStart.get(i),
                            cardPositionEnd.get(i),
                            cardBurst)) {
                        cardInfo += "B";
                    } else if (mGL.getCaptureService().findColorInRange(
                            cardPositionStart.get(i),
                            cardPositionEnd.get(i),
                            cardQuick)) {
                        cardInfo += "Q";
                    } else {
                        cardInfo += "Cannot find it";
                    }
                }

                sendMessage(cardInfo);
                sleep(5000);
            }
        }

        public void run() {
            try {
                main();
            } catch (Exception e) {
                Log.e(TAG, "Routine caught an exception or been interrupted: " + e.getMessage());
            }
        }
    }
}
