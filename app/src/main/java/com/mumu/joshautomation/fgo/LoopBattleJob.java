package com.mumu.joshautomation.fgo;

import android.util.Log;

import com.mumu.joshautomation.AppPreferenceValue;
import com.mumu.joshautomation.script.AutoJob;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

public class LoopBattleJob extends AutoJob {
    private static final String TAG = "LoopBattleJob";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;

    private FGORoutine mFGO;
    private LoopBattleJob mSelf;
    private BattleArgument mBattleArg;
    private boolean mWaitSkip = false;

    public static final String jobName = "FGO 循環戰鬥";

    public LoopBattleJob() {
        super(jobName);

        /* JoshGameLibrary basic initial */
        mGL = JoshGameLibrary.getInstance();
        mGL.setGameOrientation(ScreenPoint.SO_Landscape);
        mGL.setTouchShift(6);

        // FGO game specific point offset
        if (mGL.getScreenWidth() == 1080) {
            if (mGL.getScreenHeight() == 2160) {
                mGL.setScreenOffset(42, 0, ScreenPoint.SO_Landscape);
            }
        }

        mFGO = new FGORoutine(mGL, mListener); //listener might be null before assigning
        mSelf = this;
    }

    @Override
    public void start() {
        super.start();
        Log.d(TAG, "starting job " + getJobName());

        refreshSetting();
        mWaitSkip = AppPreferenceValue.getInstance().getPrefs().getBoolean("battleWaitSkip", false);
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
     * In LoopBattleJob, extra data will be BattleArgument
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

    private void playNotificationSound() {
        mGL.getInputService().playNotificationSound();
    }

    private void refreshSetting() {
        String battleString = AppPreferenceValue.getInstance().
                getPrefs().getString("battleArgPref", "");
        mBattleArg = new BattleArgument(battleString);
    }

    private class MainJobRoutine extends Thread {

        private void main() throws Exception {
            int[] ambRange = new int[] {0x0A, 0x0A, 0x10};
            boolean stageCleared = false;

            mGL.setGameOrientation(ScreenPoint.SO_Landscape);
            mGL.setAmbiguousRange(ambRange);

            sendMessage("開始循環戰鬥");

            while (mShouldJobRunning) {
                refreshSetting();

                if (mFGO.waitForUserMode(40) < 0) {
                    sendMessage("錯誤:不在主畫面上");
                    playNotificationSound();
                    mShouldJobRunning = false;
                    return;
                }

                mGL.getInputService().tapOnScreen(FGORoutineDefine.pointLoopBattleStage.coord);
                sleep(2000);

                if (mFGO.battlePreSetup(false) < 0) {
                    sendMessage("進入關卡錯誤");
                    playNotificationSound();
                    mShouldJobRunning = false;
                    return;
                }

                if (mWaitSkip) {
                    if (mFGO.waitForSkip(30) < 0) {
                        sendMessage("等不到SKIP，當作正常");
                    }
                }

                if (mFGO.battleRoutine(mBattleArg) < 0) {
                    sendMessage("戰鬥出現錯誤");
                    playNotificationSound();
                    mShouldJobRunning = false;
                    return;
                }

                sleep(1000);
                if (mFGO.battleHandleFriendRequest() < 0) {
                    sendMessage("沒有朋友請求，可能正常");
                }

                if (mWaitSkip) {
                    if (mFGO.waitForSkip(30) < 0) { //wait skip 7 seconds
                        sendMessage("等不到SKIP，當作正常");
                    }
                }


                if (!stageCleared) {
                    if (mFGO.battlePostSetup() < 0) {
                        sendMessage("離開戰鬥錯誤");
                        mShouldJobRunning = false;
                        return;
                    }
                }

                stageCleared = true;
            }

            sleep(1000);
            sendMessage("結束循環戰鬥");
            playNotificationSound();
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
