package com.mumu.joshautomation.fgo;

import android.util.Log;

import com.mumu.joshautomation.AppPreferenceValue;
import com.mumu.joshautomation.script.AutoJob;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

public class AutoBattleJob extends AutoJob {
    private static final String TAG = "AutoBattleJob";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;

    private FGORoutine mFGO;
    private AutoBattleJob mSelf;
    private BattleArgument mBattleArg;

    public static final String jobName = "FGO main story battle job";

    public AutoBattleJob() {
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
                getPrefs().getString("battleArgPref", "j#####ijk#####j#####ijk#####j#####ijk");
        mBattleArg = new BattleArgument(battleString);

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

    @Override
    public void setExtra(Object object) {
        if (object instanceof BattleArgument) {
            mBattleArg = (BattleArgument)object;
        }
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
            int ret;
            int[] ambRange = new int[] {0x0A, 0x0A, 0x0A};

            mGL.setGameOrientation(ScreenPoint.SO_Landscape);
            mGL.setAmbiguousRange(ambRange);

            while (isShouldJobRunning()) {

                if (!mFGO.isInHomeScreen()) {
                    sendMessage("這不是主畫面誒");
                    mShouldJobRunning = false;
                    return;
                }

                if (mFGO.findNextAndClick(20, true) < 0) {
                    sendMessage("找不到下一關");
                    mShouldJobRunning = false;
                    return;
                }

                if (mFGO.battlePreSetup(false) < 0) {
                    sendMessage("進入關卡錯誤");
                    mShouldJobRunning = false;
                    return;
                }

                if (mFGO.waitForSkip(70) < 0) { //wait skip 7 seconds
                    sendMessage("等不到SKIP，當作正常");
                }

                ret = mFGO.battleRoutine(mBattleArg);
                if (ret < 0) {
                    sendMessage("戰鬥錯誤:" + mFGO.battleGetErrorMsg(ret));
                    mShouldJobRunning = false;
                    return;
                }

                if (mFGO.battleHandleFriendRequest() < 0) {
                    sendMessage("沒有朋友請求，可能正常");
                }

                if (mFGO.waitForSkip(70) < 0) { //wait skip 7 seconds
                    sendMessage("等不到SKIP，當作正常");
                }

                if (mFGO.battlePostSetup() < 0) {
                    sendMessage("離開戰鬥錯誤");
                    mShouldJobRunning = false;
                    return;
                }

                sleep(2000);
                sendMessage("回到主畫面中");
                if (mFGO.returnToHome(5) < 0) {
                    sendMessage("回主畫面出錯");
                    mShouldJobRunning = false;
                    return;
                }

                sleep(1000);
            }

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
