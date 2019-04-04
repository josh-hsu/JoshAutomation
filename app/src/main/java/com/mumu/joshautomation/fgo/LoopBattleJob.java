package com.mumu.joshautomation.fgo;

import android.content.SharedPreferences;

import com.mumu.joshautomation.AppPreferenceValue;
import com.mumu.joshautomation.HeadService;
import com.mumu.joshautomation.script.AutoJob;
import com.mumu.joshautomation.script.AutoJobAction;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.joshautomation.script.DefinitionLoader;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.Log;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.ArrayList;

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

                        mFGO.tapOnLoopStage();
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
        private int sX = 0, sY = 0;
        private int sWidth = 2340, sHeight = 1080;
        private int resultX = 0, resultY = 0;
        private boolean sFinish = false;
        private DefinitionLoader.DefData sDef;
        private ArrayList<ScreenPoint> targetPoints;

        private void init() {
            sDef = mFGO.getDef();
            targetPoints = new ArrayList<>();
            targetPoints.add(sDef.getScreenPoint("pointHomeApAdd"));
            targetPoints.add(sDef.getScreenPoint("pointHomeApAddV2"));
        }

        private int startAutoCorrection() throws InterruptedException {
            ArrayList<ScreenPoint> tryPoints = new ArrayList<>();
            boolean found = false;

            for (ScreenPoint point : targetPoints) {
                ScreenCoord coord = new ScreenCoord(point.coord.x + sX,
                        point.coord.y + sY, point.coord.orientation);
                tryPoints.add(new ScreenPoint(coord, point.color));
            }

            for(ScreenPoint point : tryPoints) {
                if (mGL.getCaptureService().colorIs(point)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                return 100; // means it reach the end
            }

            // reach X max, increase Y
            if (sX++ >= sWidth) {
                sX = 0;
                sY++;
            }

            if (sY >= sHeight) {
                sFinish = true;
                return 100;
            }

            return (sX * 100) / (sHeight);
        }

        private void applyCorrection() {
            SharedPreferences.Editor editor = AppPreferenceValue.getInstance().getPrefs().edit();
            if (mGL.getGameOrientation() == ScreenPoint.SO_Landscape) {
                editor.putString("userSetScreenXOffset", "" + sY);
                editor.putString("userSetScreenYOffset", "" + sX);
                mGL.setScreenOffset(sY, sX, ScreenPoint.SO_Portrait);
            } else {
                editor.putString("userSetScreenXOffset", "" + sX);
                editor.putString("userSetScreenYOffset", "" + sY);
                mGL.setScreenOffset(sX, sY, ScreenPoint.SO_Portrait);
            }
            editor.commit();
        }

        public void run() {
            int interactResult;

            if (mShouldJobRunning) {
                Log.d(TAG, "onAutoCorrection: stopping current job");
                if (mRoutine != null)
                    mRoutine.interrupt();
            }

            // initial for routine
            init();

            // test for Action <ACTION_SHOW_DIALOG>
            String[] options = new String[] {"現在進行", "取消"};
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
            while (interactResult != 100) {
                try {
                    interactResult = startAutoCorrection();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                options = new String[] {};
                title = "分析中，碰觸即取消";
                summary = "目前 sX: " + sX + ", sY:" + sY +
                        (sX > 200 ? "\n您是不是不在此畫面上??" : "");
                action = new AutoJobAction("UPDATE:" + interactResult, null, title, summary, options);
                action.sendActionWaited(mListener, HeadService.ACTION_SHOW_PROGRESS);
                Log.d(TAG, "Receive " + action.toString() + ", result = " + interactResult);
            }

            options = new String[] {};
            title = "ACTION_SHOW_PROGRESS title";
            summary = "ACTION_SHOW_PROGRESS summary";
            action = new AutoJobAction("CLOSE", null, title, summary, options);
            interactResult = action.sendActionWaited(mListener, HeadService.ACTION_SHOW_PROGRESS);
            Log.d(TAG, "Receive " + action.toString() + ", result = " + interactResult);

            // test for Action <ACTION_SHOW_DIALOG>
            options = new String[] {"套用", "保持原設定"};
            title = "座標自動校正";
            summary = "校正完成\n新的 offset X = " + sX + "\n新的 offset Y = " + sY +
                    "\n建議色彩接受範圍 = 0x0a";
            action = new AutoJobAction("ACTION", null, title, summary, options);
            interactResult = action.sendActionWaited(mListener, HeadService.ACTION_SHOW_DIALOG);
            Log.d(TAG, "Receive " + action.toString() + ", result = " + interactResult);

            if (action.getReaction().equals("true")) {
                applyCorrection();

                options = new String[] {"完成"};
                title = "座標自動校正　（完成）";
                summary = "已成功套用以下設定\n新的 offset X = " + sX + "\n新的 offset Y = " + sY +
                        "\n建議色彩接受範圍 = 0x0a";
                action = new AutoJobAction("ACTION", null, title, summary, options);
                interactResult = action.sendActionWaited(mListener, HeadService.ACTION_SHOW_DIALOG);
                Log.d(TAG, "Receive " + action.toString() + ", result = " + interactResult);
            } else {
                options = new String[] {"完成"};
                title = "座標自動校正　（取消）";
                summary = "校正取消";
                action = new AutoJobAction("ACTION", null, title, summary, options);
                interactResult = action.sendActionWaited(mListener, HeadService.ACTION_SHOW_DIALOG);
                Log.d(TAG, "Receive " + action.toString() + ", result = " + interactResult);
            }

        }
    }
}
