package com.mumu.joshautomation.fgo;

import android.util.Log;

import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.joshautomation.script.AutoJobHandler;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

import static com.mumu.joshautomation.fgo.FGORoutineDefine.*;

public class AutoBattleJob extends AutoJobHandler.AutoJob {
    private static final String TAG = "AutoBattleJob";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;

    private FGORoutine mFGO;

    public AutoBattleJob(String jobName, int jobIndex) {
        super(jobName, jobIndex);

        /* JoshGameLibrary basic initial */
        mGL = JoshGameLibrary.getInstance();
        mGL.setGameOrientation(ScreenPoint.SO_Landscape);
        mGL.setScreenDimension(1080, 1920);

        mFGO = new FGORoutine(mGL);
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
            String cardInfo;
            int[] optimizedDraw, cardStatusNow;
            int maxTry = 20;
            int currentTry = 0;

            mGL.setGameOrientation(ScreenPoint.SO_Landscape);
            mGL.setAmbiguousRange(0x0A);

            while (isShouldJobRunning()) {
                sleep(500);
                sendMessage("Wait for Battle Button");
                currentTry = maxTry;

                mGL.getCaptureService().waitOnColor(pointBattleButton, 600, this);
                mGL.getInputService().tapOnScreen(pointBattleButton.coord);

                sendMessage("Now checking cards");
                cardStatusNow = mFGO.getCurrentCardPresent();
                while(!mFGO.isCardValid(cardStatusNow) && currentTry > 0) {
                    cardStatusNow = mFGO.getCurrentCardPresent();
                    currentTry--;
                }

                if(mFGO.isCardValid(cardStatusNow)) {
                    cardInfo = mFGO.getCardNameSeries(cardStatusNow);
                    sendMessage(cardInfo);
                } else {
                    sendMessage("Sorry card is not valid.");
                    continue;
                }

                optimizedDraw = mFGO.getOptimizeDraw(cardStatusNow);
                mFGO.tapOnCard(optimizedDraw);

                sleep(1000);
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
