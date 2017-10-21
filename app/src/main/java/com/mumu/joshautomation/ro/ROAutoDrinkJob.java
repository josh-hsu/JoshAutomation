package com.mumu.joshautomation.ro;

import android.util.Log;

import com.mumu.joshautomation.script.AutoJob;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

public class ROAutoDrinkJob extends AutoJob {
    private static final String TAG = "ROAutoDrinkJob";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;

    private RORoutine mRO;
    private ROAutoDrinkJob mSelf;

    public static final String jobName = "RO 自動喝水";

    public ROAutoDrinkJob() {
        super(jobName);

        /* JoshGameLibrary basic initial */
        mGL = JoshGameLibrary.getInstance();
        mGL.setGameOrientation(ScreenPoint.SO_Landscape);

        // FGO game specific point offset
        if (mGL.getScreenWidth() == 1080) {
            if (mGL.getScreenHeight() == 2160) {
                mGL.setScreenOffset(42, 0, ScreenPoint.SO_Landscape);
            }
        }

        mRO = new RORoutine(mGL, mListener);
        mSelf = this;
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

        if (mRoutine != null) {
            mRoutine.interrupt();
        }
    }

    /*
     * In PureBattleJob, extra data will be BattleArgument
     */
    @Override
    public void setExtra(Object object) {

    }

    public void setJobEventListener(AutoJobEventListener el) {
        mListener = el;
        mRO = new RORoutine(mGL, mListener);
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
            int[] ambRange = new int[] {0x05, 0x05, 0x05};

            mGL.setGameOrientation(ScreenPoint.SO_Landscape);
            mGL.setAmbiguousRange(ambRange);

            sendMessage("開始偵測");

            while(mShouldJobRunning) {
                mRO.checkBattleSupply();
                sleep(1500);
            }

            mShouldJobRunning = false;
            sleep(1000);
            sendMessage("結束啦");
            mListener.onJobDone(mSelf.getJobName());
        }

        public void run() {
            try {
                main();
            } catch (Exception e) {
                sendMessage("任務已停止");
                Log.e(TAG, "Routine caught an exception or been interrupted: " + e.getMessage());
            }
        }
    }
}
