package com.mumu.android.joshautomation.scripts.fgo;

import com.mumu.android.joshautomation.autojob.AutoJob;
import com.mumu.android.joshautomation.autojob.AutoJobEventListener;
import com.mumu.android.joshautomation.content.AppPreferenceValue;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenPoint;

public class FGOLoopBattleJob extends AutoJob {
    private static final String TAG = "LoopBattleJob";
    private MainJobRoutine mRoutine;
    private GameLibrary20 mGL;
    private AutoJobEventListener mListener;

    private FGORoutine mFGO;
    private BattleArgument mBattleArg;
    private boolean mWaitSkip = false;

    public static final String jobName = "FGO 循環戰鬥";

    public FGOLoopBattleJob() { super(jobName); }

    @Override
    public void start() {
        super.start();
        //Log.d(TAG, "starting job " + getJobName());
        mFGO = new FGORoutine(mGL, mListener);
        mGL.setScreenMainOrientation(ScreenPoint.SO_Landscape);
        refreshSetting();
        mWaitSkip = AppPreferenceValue.getInstance().getPrefs().getBoolean("battleWaitSkip", false);
        mRoutine = null;
        mRoutine = new MainJobRoutine();
        mRoutine.start();
    }

    @Override
    public void stop() {
        super.stop();
        //Log.d(TAG, "stopping job " + getJobName());

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
            mBattleArg = (BattleArgument) object;
        }
        if (object instanceof GameLibrary20)
            mGL = (GameLibrary20) object;
    }

    public void setJobEventListener(AutoJobEventListener el) {
        mListener = el;

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
    }

    private void sendEvent(String msg, Object extra) {
        if (mListener != null) {
            mListener.onMessageReceived(msg, extra);
        } else {
            //Log.w(TAG, "There is no event listener registered.");
        }
    }

    private void sendMessage(String msg) {
        sendEvent(msg, this);
    }

    private void refreshSetting() {
        String battleString = AppPreferenceValue.getInstance().
                getPrefs().getString("battleArgPref", "");
        mBattleArg = new BattleArgument(battleString);
    }
/*
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
*/
    private class MainJobRoutine extends Thread {

        private void main() throws Exception {
            boolean stageCleared = false;
            int nextOngoingState;
            //int battleCountLimit = getBattleCountLimit();

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
                            mShouldJobRunning = false;
                            return;
                        }

                        mFGO.tapOnLoopStage();
                        sleep(2000);

                        // handle AP not enough
                        if (mFGO.battleHandleAPSupply() < 0) {
                            mShouldJobRunning = false;
                            return;
                        }
                        sleep(1000);

                        if (mFGO.battlePreSetup(false) < 0) {
                            sendMessage("進入關卡錯誤");
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
                        //mBattleArg = new BattleArgument("e8|af1cg16|kji1b2w31ij1k16|||");
                        if (mFGO.battleRoutine(mBattleArg) < 0) {
                            sendMessage("戰鬥出現錯誤");
                            mShouldJobRunning = false;
                            return;
                        }
/*
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
*/
                        stageCleared = true;
                        nextOngoingState = FGORoutine.STATE_IN_BATTLE_OVER; //Next step: In home
                        break;
                    /*
                     * STATE_IN_BATTLE_OVER
                     */
                    case FGORoutine.STATE_IN_BATTLE_OVER:
                        mFGO.tapOnContinue();
                        sleep(2000);

                        // handle AP not enough
                        if (mFGO.battleHandleAPSupply() < 0) {
                            mShouldJobRunning = false;
                            return;
                        }
                        sleep(4000);

                        mFGO.battleContinueSetup();
                        nextOngoingState = FGORoutine.STATE_IN_BATTLE; //Next step: In battle
                        break;
                    default:
                        break;
                }
            }

            sleep(1000);
            sendMessage("結束循環戰鬥");
            mListener.onJobDone(getJobName());
            sleep(1000);
        }

        public void run() {
            try {
                main();
            } catch (Exception e) {
                sendMessage("任務已停止");
                //Log.e(TAG, "Routine caught an exception or been interrupted: " + e.getMessage());
            }
        }
    }

}
