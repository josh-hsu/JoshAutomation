package com.mumu.joshautomation.fgo;

import com.mumu.joshautomation.AppPreferenceValue;
import com.mumu.joshautomation.HeadService;
import com.mumu.joshautomation.script.AutoJob;
import com.mumu.joshautomation.script.AutoJobAction;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.Log;
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

    /*
     * LoopBattleJob Auto Correction procedure
     * 1. stop current routine if job is running
     * 2. request user proceeding to home screen
     * 3. start calculate possible offset
     * 4.
     */
    @Override
    public void onAutoCorrection(Object object) {
        new AutoCorrectionRoutine().start();
    }

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

    private void playNotificationSound() {
        mGL.getInputService().playNotificationSound();
    }

    private void refreshSetting() {
        String battleString = AppPreferenceValue.getInstance().
                getPrefs().getString("battleArgPref", "");
        mBattleArg = new BattleArgument(battleString);
    }

    private int getBattleCountLimit() {
        String limitString = AppPreferenceValue.getInstance().
                getPrefs().getString("battleCountLimit", "0");
        int ret = 0;

        try {
            ret = Integer.parseInt(limitString);
        } catch (NumberFormatException e) {
            sendMessage("戰鬥場數限制設定有誤");
        }

        return ret;
    }

    private class MainJobRoutine extends Thread {

        private void main() throws Exception {
            boolean stageCleared = false;
            int nextOngoingState;
            int battleCountLimit = getBattleCountLimit();

            sendMessage("開始循環戰鬥");

            // pre-condition check
            nextOngoingState = mFGO.getGameState();

            while (mShouldJobRunning) {
                refreshSetting();

                switch (nextOngoingState) {
                    /*
                     * STATE_IN_HOME
                     */
                    case FGORoutine.STATE_IN_HOME:
                    case FGORoutine.STATE_UNKNOWN:
                        if (mFGO.waitForUserMode(100) < 0) {
                            sendMessage("錯誤:不在主畫面上");
                            playNotificationSound();
                            mShouldJobRunning = false;
                            return;
                        }

                        mGL.getInputService().tapOnScreen(FGORoutineDefine.pointLoopBattleStage.coord);
                        sleep(1000);

                        // handle AP not enough
                        if (mFGO.battleHandleAPSupply() < 0) {
                            playNotificationSound();
                            mShouldJobRunning = false;
                            return;
                        }
                        sleep(1000);

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

                        nextOngoingState = FGORoutine.STATE_IN_BATTLE; //Next step: In battle
                        break;
                    /*
                     * STATE_IN_BATTLE
                     */
                    case FGORoutine.STATE_IN_BATTLE:
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

                        if (battleCountLimit-- < 0) {
                            sendMessage("戰鬥次數達成，離開戰鬥");
                            mShouldJobRunning = false;
                            return;
                        }

                        stageCleared = true;
                        nextOngoingState = FGORoutine.STATE_IN_HOME; //Next step: In home
                        break;
                    default:
                        break;
                }
            }

            sleep(1000);
            sendMessage("結束循環戰鬥");
            playNotificationSound();
            mListener.onJobDone(mSelf.getJobName());
            sleep(1000);
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

    private class AutoCorrectionRoutine extends Thread {
        public void run() {
            int interactResult;

            if (mShouldJobRunning) {
                Log.d(TAG, "onAutoCorrection: stopping current job");
                if (mRoutine != null)
                    mRoutine.interrupt();
            }

            // test for Action <ACTION_SHOW_DIALOG>
            String[] options = new String[] {"是", "取消"};
            String title = "座標自動校正";
            String summary = "請移動至主頁面，就是有關卡選擇，禮物盒以及AP經驗條的畫面";
            AutoJobAction action = new AutoJobAction("ACTION", null, title, summary, options);
            interactResult =action.sendActionWaited(mListener, HeadService.ACTION_SHOW_DIALOG);
            Log.d(TAG, "Receive " + action.toString() + ", result = " + interactResult);

            // test for Action <ACTION_SHOW_PROGRESS>
            options = new String[] {};
            title = "請稍後";
            summary = "開始中...";
            action = new AutoJobAction("NEW", null, title, summary, options);
            interactResult = action.sendActionWaited(mListener, HeadService.ACTION_SHOW_PROGRESS);
            Log.d(TAG, "Receive " + action.toString() + ", result = " + interactResult);

            summary = "分析中";
            for (int i = 0; i <= 10; i++) {
                try {
                    Thread.sleep(300);
                } catch (Exception e) {}

                options = new String[] {};
                title = "請稍後";
                summary += ".";
                action = new AutoJobAction("UPDATE:" + i*10, null, title, summary, options);
                interactResult = action.sendActionWaited(mListener, HeadService.ACTION_SHOW_PROGRESS);
                Log.d(TAG, "Receive " + action.toString() + ", result = " + interactResult);
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {}

            options = new String[] {};
            title = "ACTION_SHOW_PROGRESS title";
            summary = "ACTION_SHOW_PROGRESS summary";
            action = new AutoJobAction("CLOSE", null, title, summary, options);
            interactResult = action.sendActionWaited(mListener, HeadService.ACTION_SHOW_PROGRESS);
            Log.d(TAG, "Receive " + action.toString() + ", result = " + interactResult);

        }
    }
}
