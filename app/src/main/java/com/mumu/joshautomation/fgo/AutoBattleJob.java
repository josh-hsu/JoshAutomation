package com.mumu.joshautomation.fgo;

import android.util.Log;

import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.joshautomation.script.AutoJobHandler;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import static com.mumu.joshautomation.fgo.FGORoutineDefine.*;

public class AutoBattleJob extends AutoJobHandler.AutoJob implements AutoJobEventListener {
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

        mFGO = new FGORoutine(mGL, this);
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

    @Override
    public void onEventReceived(String msg, Object extra) {
        sendMessage(msg);
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

            mGL.setGameOrientation(ScreenPoint.SO_Landscape);
            mGL.setAmbiguousRange(0x0A);

            while (isShouldJobRunning()) {

                if (!mFGO.isInHomeScreen()) {
                    sendMessage("這不是主畫面誒");
                    mShouldJobRunning = false;
                    return;
                }

                if (mFGO.findNextAndClick(20) < 0) {
                    sendMessage("找不到下一關");
                    mShouldJobRunning = false;
                    return;
                }

                if (mFGO.battlePreSetup(this, true) < 0) {
                    sendMessage("進入關卡錯誤");
                    mShouldJobRunning = false;
                    return;
                }

                if (mFGO.waitForSkip(70, this) < 0) { //wait skip 7 seconds
                    sendMessage("等不到SKIP，當作正常");
                }

                if (mFGO.battleRoutine(this) < 0) {
                    sendMessage("戰鬥出現錯誤");
                    mShouldJobRunning = false;
                    return;
                }

                sleep(1000);
                if (mFGO.battleHandleFriendRequest(this) < 0) {
                    sendMessage("沒有朋友請求，可能正常");
                }

                if (mFGO.waitForSkip(70, this) < 0) { //wait skip 7 seconds
                    sendMessage("等不到SKIP，當作正常");
                }

                sleep(3000);
                if (mFGO.battlePostSetup(this) < 0) {
                    sendMessage("離開戰鬥錯誤");
                    mShouldJobRunning = false;
                    return;
                }

                sleep(2000);
                sendMessage("回到主畫面中");
                if (mFGO.returnToHome(this, 5) < 0) {
                    sendMessage("回主畫面出錯");
                    mShouldJobRunning = false;
                    return;
                }

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
