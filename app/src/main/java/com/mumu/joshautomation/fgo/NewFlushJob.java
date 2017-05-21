package com.mumu.joshautomation.fgo;

import android.util.Log;

import com.mumu.joshautomation.AppPreferenceValue;
import com.mumu.joshautomation.script.AutoJob;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

import static com.mumu.joshautomation.fgo.FGORoutineDefine.pointBattleButton;
import static com.mumu.joshautomation.fgo.FGORoutineDefineTW.*;

public class NewFlushJob extends AutoJob {
    private static final String TAG = "NewFlushJob";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;

    private FGORoutine mFGO;
    private NewFlushJob mSelf;
    private BattleArgument mBattleArg;

    public static final String jobName = "FGO New Flush Job";

    public NewFlushJob() {
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

    private int battleOnce(int preWaitTime) throws Exception {
        if (mGL.getCaptureService().waitOnColor(pointBattleButton, preWaitTime, mRoutine) < 0) {
            Log.d(TAG, "Cannot find battle button, checking if finished");
            sendMessage("Cannot find battle button");
            return -1;
        }
        mGL.getInputService().tapOnScreen(pointBattleButton.coord);
        Thread.sleep(2000);
        mFGO.tapOnCard(new int[]{1, 0, 2});

        return 0;
    }

    private int battlePreSetupTW(boolean tutorial) throws Exception {
        Thread.sleep(2000);

        if (tutorial)
            mGL.getInputService().tapOnScreen(FGORoutineDefineTW.pointSelectFriendButton.coord);

        Thread.sleep(2000);
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.pointSelectFriendButton.coord);

        Thread.sleep(1500);
        if (mGL.getCaptureService().waitOnColor(FGORoutineDefineTW.pointEnterStageButton, 20, mRoutine) < 0) {
            return -1;
        }
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.pointEnterStageButton.coord);
        Thread.sleep(100);

        return 0;
    }

    private int tutorialBattle() throws Exception {
        //1st
        if (battleOnce(400) < 0) {
            return -1;
        }

        //2nd
        if (battleOnce(400) < 0) {
            return -1;
        }

        //3rd
        if (battleOnce(400) < 0) {
            return -1;
        }

        //4th
        if (mGL.getCaptureService().waitOnColor(pointBattleButton, 400, mRoutine) < 0) {
            Log.d(TAG, "Cannot find battle button, checking if finished");
            sendMessage("Cannot find battle button");
            return -1;
        }
        mGL.getInputService().tapOnScreen(pointBattleButton.coord);
        Thread.sleep(2000);
        mGL.getInputService().tapOnScreen(pointBattleButton.coord);
        Thread.sleep(2000);
        mFGO.tapOnCard(new int[]{1, 0, 2});

        //5th
        if (mGL.getCaptureService().waitOnColor(pointBattleButton, 400, mRoutine) < 0) {
            Log.d(TAG, "Cannot find battle button, checking if finished");
            sendMessage("Cannot find battle button");
            return -1;
        }
        mGL.getInputService().tapOnScreen(pointBattleButton.coord);
        Thread.sleep(1000);
        mFGO.tapOnRoyal(new int[] {0});
        Thread.sleep(500);
        mFGO.tapOnCard(new int[]{1, 0, 2});

        return 0;
    }

    private int createCharacter() throws Exception {
        sendMessage("等待取名字");
        if(!mGL.getCaptureService().colorIs(FGORoutineDefineTW.nameFieldScreenPoint)) {
            sendMessage("等不到取名，離開囉");
            mShouldJobRunning = false;
            return -1;
        }
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.nameFieldScreenPoint.coord);

        sendMessage("取名...");
        mGL.getInputService().inputText("c5" + ((int)(Math.random()*10)) + "new" + ((int)(Math.random()*100)));
        Thread.sleep(2000);
        mGL.getInputService().tapOnScreen(nameConfirmScreenPoint.coord);
        Thread.sleep(2000);
        mGL.getInputService().tapOnScreen(nameConfirmDecidePoint.coord);
        Thread.sleep(3000);
        mGL.getInputService().tapOnScreen(nameConfirmFinalPoint.coord);

        return 0;
    }

    private int battleXARoutine() throws Exception {
        sendMessage("開始XA只能在地圖上開始");
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.XAStageScreenPoint.coord);
        Thread.sleep(1500);
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.XASubStageScreenPoint.coord);
        Thread.sleep(1500);

        if (mFGO.waitForSkip(30, mRoutine) < 0) { //wait skip 3 seconds
            sendMessage("等不到SKIP呢");
            return -1;
        }

        if (battleOnce(500) < 0) {
            return -1;
        }

        //wait 30 seconds for battle end
        Thread.sleep(20000);

        mFGO.tapOnSkill(new int[] {1});
        Thread.sleep(2000);
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.skillConfirmScreenPoint.coord);
        Thread.sleep(2000);
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.skillTargetScreenPoint.coord);

        if (mGL.getCaptureService().waitOnColor(pointBattleButton, 100, mRoutine) < 0) {
            Log.d(TAG, "Cannot find battle button, checking if finished");
            sendMessage("Cannot find battle button");
            return -1;
        }
        mGL.getInputService().tapOnScreen(pointBattleButton.coord);
        Thread.sleep(2000);
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.battle2XScreenPoint.coord);
        Thread.sleep(1000);
        mFGO.tapOnCard(new int[]{1, 0, 2});

        if(mFGO.battleRoutine(mRoutine, null) < 0) {
            return -1;
        }

        if (mFGO.waitForSkip(70, mRoutine) < 0) { //wait skip 3 seconds
            sendMessage("等不到SKIP呢");
            return -1;
        }

        if (mFGO.battlePostSetupTW(mRoutine) < 0) {
            sendMessage("離開戰鬥錯誤");
            return -1;
        }

        return 0;
    }

    private int battleXBRoutine() throws Exception {
        sendMessage("開始XB只能在地圖上開始");
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.XAStageScreenPoint.coord);
        Thread.sleep(1500);
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.XASubStageScreenPoint.coord);
        Thread.sleep(1500);

        if (mFGO.waitForSkip(30, mRoutine) < 0) { //wait skip 3 seconds
            sendMessage("等不到SKIP呢");
            return -1;
        }

        if (battleOnce(500) < 0) {
            return -1;
        }

        //change target wait
        if (mGL.getCaptureService().waitOnColor(FGORoutineDefineTW.pointChangeHint, 500, mRoutine) < 0) {
            return -1;
        }
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.pointChangeButton.coord);

        if(mFGO.battleRoutine(mRoutine, null) < 0) {
            return -1;
        }

        if (mFGO.waitForSkip(70, mRoutine) < 0) { //wait skip 3 seconds
            sendMessage("等不到SKIP呢");
            return -1;
        }

        if (mFGO.battlePostSetupTW(mRoutine) < 0) {
            sendMessage("離開戰鬥錯誤");
            return -1;
        }

        return 0;
    }

    private int battleXCRoutine(int level) throws Exception {
        sendMessage("開始XC只能在地圖上開始");
        if (level == 1) {
            mGL.getInputService().tapOnScreen(FGORoutineDefineTW.XAStageScreenPoint.coord);
            Thread.sleep(1500);
        }
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.XASubStageScreenPoint.coord);
        Thread.sleep(1500);

        if (battlePreSetupTW(level == 1) < 0) {
            sendMessage("進入關卡錯誤");
            return -1;
        }

        if (mFGO.waitForSkip(70, mRoutine) < 0) { //wait skip 7 seconds
            sendMessage("等不到SKIP???");
            return -1;
        }

        if (level == 2) {
            Thread.sleep(7000);
            if (mGL.getCaptureService().waitOnColor(pointBattleButton, 150, mRoutine) < 0) {
                Log.d(TAG, "Cannot find battle button, checking if finished");
                sendMessage("Cannot find battle button");
                return -1;
            }
            mGL.getInputService().tapOnScreen(pointBattleButton.coord);
            Thread.sleep(2000);
            mGL.getInputService().tapOnScreen(FGORoutineDefineTW.battle2XScreenPoint.coord);
            Thread.sleep(2000);
            mFGO.tapOnCard(new int[]{1, 0, 2});
        }

        if (mFGO.battleRoutine(mRoutine, null) < 0) {
            sendMessage("戰鬥錯誤:");
            return -1;
        }

        if (mFGO.battleHandleFriendRequestTW(mRoutine) < 0) {
            sendMessage("沒有朋友請求?");
            return -1;
        }

        if (level != 2) {
            if (mFGO.waitForSkip(70, mRoutine) < 0) { //wait skip 7 seconds
                sendMessage("等不到SKIP，當作正常");
            }
        }

        if (level == 3) {
            if (mFGO.battlePostSetupTW(mRoutine) < 0) {
                sendMessage("離開戰鬥錯誤");
                return -1;
            }
        }

        return 0;
    }

    private int doTenSummon() throws Exception {
        //wait for menu show up
        if (mGL.getCaptureService().waitOnColor(FGORoutineDefineTW.pointMenuButton, 500, mRoutine) < 0) {
            return -1;
        }
        Thread.sleep(3000);
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.pointMenuButton.coord);

        if (mGL.getCaptureService().waitOnColor(FGORoutineDefineTW.pointSummonButton, 50, mRoutine) < 0) {
            return -1;
        }
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.pointSummonButton.coord);

        if (mGL.getCaptureService().waitOnColor(FGORoutineDefineTW.pointTenSummonButton, 50, mRoutine) < 0) {
            return -1;
        }
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.pointTenSummonButton.coord);

        if (mGL.getCaptureService().waitOnColor(FGORoutineDefineTW.pointSummonConfirmButton, 50, mRoutine) < 0) {
            return -1;
        }
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.pointSummonConfirmButton.coord);

        //start summoning, keep touching
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointSummonSkipButton,
                pointSummonNextButton, 100, 700, mRoutine) < 0) {
            return -1;
        }
        mGL.getInputService().tapOnScreen(pointSummonNextButton.coord);

        //start servant talking
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointSummonSkipButton,
                pointSummonSummonButton, 100, 700, mRoutine) < 0) {
            return -1;
        }
        mGL.getInputService().tapOnScreen(pointSummonSummonButton.coord);

        return 0;
    }

    private int doFormation() throws Exception {
        //wait for menu show up
        if (mGL.getCaptureService().waitOnColor(FGORoutineDefineTW.pointMenuButton, 500, mRoutine) < 0) {
            return -1;
        }
        mGL.getInputService().tapOnScreen(FGORoutineDefineTW.pointMenuButton.coord);

        if (mGL.getCaptureService().waitOnColor(pointFormationButton, 50, mRoutine) < 0) {
            return -1;
        }
        mGL.getInputService().tapOnScreen(pointFormationButton.coord);

        if (mGL.getCaptureService().waitOnColor(pointTeamFormationButton, 50, mRoutine) < 0) {
            return -1;
        }
        mGL.getInputService().tapOnScreen(pointTeamFormationButton.coord);

        if (mGL.getCaptureService().waitOnColor(pointTeamMem2Button, 50, mRoutine) < 0) {
            return -1;
        }
        mGL.getInputService().tapOnScreen(pointTeamMem2Button.coord);

        Thread.sleep(3000);
        mGL.getInputService().tapOnScreen(pointMemTargetButton.coord);
        Thread.sleep(2000);
        mGL.getInputService().tapOnScreen(pointMemTargetButton.coord);

        if (mGL.getCaptureService().waitOnColor(pointFormationConfirmButton, 50, mRoutine) < 0) {
            return -1;
        }
        mGL.getInputService().tapOnScreen(pointFormationConfirmButton.coord);

        Thread.sleep(6000);
        mGL.getInputService().tapOnScreen(pointFormationCloseButton.coord);
        Thread.sleep(3000);
        mGL.getInputService().tapOnScreen(pointFormationCloseButton.coord);
        Thread.sleep(3000);
        return 0;
    }

    private class MainJobRoutine extends Thread {

        private void main() throws Exception {
            int[] ambRange = new int[] {0x0A, 0x0A, 0x0A};

            mGL.setGameOrientation(ScreenPoint.SO_Landscape);
            mGL.setAmbiguousRange(ambRange);

            sendMessage("開始刷首抽_TW_VERSION");
            if(!mGL.getCaptureService().colorIs(titleScreenPoint)) {
                sendMessage("不是登入畫面捏，忽略");
            }
            mGL.getInputService().tapOnScreen(titleScreenPoint.coord);
            Thread.sleep(6000);

            if (mFGO.waitForSkip(60, this) < 0) { //wait skip 7 seconds
                sendMessage("等不到SKIP呢");
            }

            //start tutorial battle
            if (tutorialBattle() < 0) {
                sendMessage("教學戰鬥錯誤，掰掰");
                mShouldJobRunning = false;
                return;
            }
            Thread.sleep(39000); //there's all white screen might mislead our script

            //wait for skip including last battle overhead
            if (mFGO.waitForSkip(30, this) < 0) { //wait skip 3 seconds
                sendMessage("等不到SKIP呢");
                mShouldJobRunning = false;
                return;
            }

            //get into create name
            if (createCharacter() < 0) {
                sendMessage("取名有問題");
                mShouldJobRunning = false;
                return;
            }

            //wait for skip including last battle overhead
            if (mFGO.waitForSkip(100, this) < 0) { //wait skip 30 seconds
                sendMessage("等不到SKIP呢");
                mShouldJobRunning = false;
                return;
            }
            sleep(20000);

            if (battleXARoutine() < 0) {
                mShouldJobRunning = false;
                return;
            }
            sleep(7000);

            if (battleXBRoutine() < 0) {
                mShouldJobRunning = false;
                return;
            }
            sleep(5000);

            if (doTenSummon() < 0) {
                mShouldJobRunning = false;
                return;
            }

            if (doFormation() < 0) {
                mShouldJobRunning = false;
                return;
            }
            sleep(5000);

            if (battleXCRoutine(1) < 0) {
                mShouldJobRunning = false;
                return;
            }
            sleep(5000);

            if (battleXCRoutine(2) < 0) {
                mShouldJobRunning = false;
                return;
            }
            sleep(5000);

            if (battleXCRoutine(3) < 0) {
                mShouldJobRunning = false;
                return;
            }

            mShouldJobRunning = false;
            sleep(1000);
            sendMessage("結束啦");
            mListener.onJobDone(mSelf.getJobName());
        }

        public void run() {
            try {
                main();
                mGL.getInputService().playSound();
            } catch (Exception e) {
                Log.e(TAG, "Routine caught an exception or been interrupted: " + e.getMessage());
            }
        }
    }
}
