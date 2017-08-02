package com.mumu.joshautomation.fgo;

import android.util.Log;

import com.mumu.joshautomation.AppPreferenceValue;
import com.mumu.joshautomation.script.AutoJob;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

public class PureBattleJob extends AutoJob {
    private static final String TAG = "PureBattleJob";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;

    private FGORoutine mFGO;
    private PureBattleJob mSelf;
    private BattleArgument mBattleArg;

    public static final String jobName = "FGO one time battle job";

    public PureBattleJob() {
        super(jobName);

        /* JoshGameLibrary basic initial */
        mGL = JoshGameLibrary.getInstance();
        mGL.setGameOrientation(ScreenPoint.SO_Landscape);
        mGL.setScreenDimension(1080, 1920);

        mFGO = new FGORoutine(mGL, mListener);
        mSelf = this;
    }

    @Override
    public void start() {
        super.start();
        Log.d(TAG, "starting job " + getJobName());

        String battleString = AppPreferenceValue.getInstance().
                getPrefs().getString("battleArgPref", "");
        mBattleArg = new BattleArgument(battleString);

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

    /*
     * In PureBattleJob, extra data will be BattleArgument
     */
    @Override
    public void setExtra(Object object) {
        if (object instanceof BattleArgument) {
            mBattleArg = (BattleArgument)object;
        }
    }

    public void setJobEventListener(AutoJobEventListener el) {
        mListener = el;
        mFGO = new FGORoutine(mGL, mListener);
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
            int[] ambRange = new int[] {0x0A, 0x0A, 0x0A};

            mGL.setGameOrientation(ScreenPoint.SO_Landscape);
            mGL.setAmbiguousRange(ambRange);

            sendMessage("開始單次戰鬥");
            if (mFGO.waitForSkip(30, this) < 0) { //wait skip 7 seconds
                sendMessage("等不到SKIP，當作正常");
            }

            if (mFGO.battleRoutine(this, mBattleArg) < 0) {
                sendMessage("戰鬥出現錯誤");
                mShouldJobRunning = false;
                return;
            }

            sleep(1000);
            if (mFGO.battleHandleFriendRequest(this) < 0) {
                sendMessage("沒有朋友請求，可能正常");
            }

            if (mFGO.waitForSkip(30, this) < 0) { //wait skip 7 seconds
                sendMessage("等不到SKIP，當作正常");
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
                Log.e(TAG, "Routine caught an exception or been interrupted: " + e.getMessage());
            }
        }
    }
}
