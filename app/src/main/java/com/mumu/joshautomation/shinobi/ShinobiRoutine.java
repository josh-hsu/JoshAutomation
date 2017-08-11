package com.mumu.joshautomation.shinobi;

import com.mumu.joshautomation.AppPreferenceValue;
import static com.mumu.joshautomation.shinobi.ShinobiRoutineDefine.*;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;

class ShinobiRoutine {
    private static final String TAG = "FGORoutine";
    private JoshGameLibrary mGL;
    private AutoJobEventListener mCallbacks;

    private int mDefaultMaxRetry = 50;
    private boolean mPreviousDied = false;

    ShinobiRoutine(JoshGameLibrary gl, AutoJobEventListener el) {
        mGL = gl;
        mCallbacks = el;
    }

    private void sendMessage(String msg) {
        if (mCallbacks != null)
            mCallbacks.onEventReceived(msg, this);
    }

    private void sendMessageVerbose(String msg) {
        boolean verboseMode = AppPreferenceValue.getInstance().getPrefs().getBoolean("debugLogPref", false);
        if (verboseMode)
            sendMessage(msg);
    }

    private void sleep(int time) throws InterruptedException {
        Thread.sleep(time);
    }

    /*
     *  Battle session
     */
    public int preBattleSetup() throws InterruptedException {
        int retry = mDefaultMaxRetry;
        boolean firstTime = true;

        if (mPreviousDied) {
            mPreviousDied = false;
            while(retry > 0 && !mGL.getCaptureService().colorIs(pointBattleEnter)) {
                sleep(100);
                retry --;
            }
            if (retry < 0) {
                sendMessage("找不到進關");
                return -1;
            }
            sleep(1500);
            mGL.getInputService().tapOnScreen(pointBattleEnter.coord);
            sleep(500);
            mGL.getInputService().tapOnScreen(pointBattleEnter.coord); //for safety
            return 0;
        }

        if (AppPreferenceValue.getInstance().getPrefs().getBoolean("shinobiFromStagePref", false)) {
            sendMessage("等待關卡按鈕");
            while(retry > 0 && !mGL.getCaptureService().colorIs(pointBattleSubstageButton)) {
                sleep(100);
                retry --;
            }
            if (retry < 0) {
                sendMessage("等不到關卡");
                return -1;
            } else {
                retry = mDefaultMaxRetry;
            }

            sleep(1000);
            mGL.getInputService().tapOnScreen(pointBattleSubstageButton.coord);
            sleep(3500);
        }

        // find friend select button and determine which kind of page is current displaying
        while (retry > 0) {
            if (mGL.getCaptureService().colorIs(pointBattleSelectFriendButton)) {
                firstTime = true;
                break;
            } else if (mGL.getCaptureService().colorIs(pointBattleSelectFriendAgainButton)) {
                firstTime = false;
                break;
            }
            sleep(100);
            retry --;
        }
        if (retry > 0) {
            retry = mDefaultMaxRetry;
            if (AppPreferenceValue.getInstance().getPrefs().getBoolean("shinobiUseFriend", false)) {
                mGL.getInputService().tapOnScreen(firstTime ?
                        pointBattleSelectFriendButton.coord : pointBattleSelectFriendAgainButton.coord);
                sleep(2000);
                mGL.getInputService().swipeOnScreen(pointSwipeStart, pointSwipeEnd);
                sleep(1000);
                mGL.getInputService().tapOnScreen(pointBattleSelectFriendLast);
                sleep(2000);
            }
        } else {
            sendMessage("找不到朋友");
        }
        sleep(2000);

        // enter battle
        while(retry > 0 && !mGL.getCaptureService().colorIs(firstTime ? pointBattleEnter : pointBattleEnterAgain)) {
            sleep(100);
            retry --;
        }
        if (retry < 0) {
            sendMessage("找不到進關");
            return -1;
        }
        sleep(1500);
        mGL.getInputService().tapOnScreen(firstTime ? pointBattleEnter.coord : pointBattleEnterAgain.coord);
        sleep(500);
        mGL.getInputService().tapOnScreen(firstTime ? pointBattleEnter.coord : pointBattleEnterAgain.coord); //for safety

        return 0;
    }

    // wait for battle hint show once, wait for max retry*100 ms
    public boolean waitForBattleStarted(int retry) throws InterruptedException {
        while(retry > 0 && !isBattleOngoing()) {
            sleep(100);
            retry --;
        }

        return retry > 0;
    }

    public boolean isBattleOngoing() {
        return mGL.getCaptureService().colorIs(pointBattleOngoing);
    }

    public boolean isBattleResultShowed() throws InterruptedException {
        sleep(20);
        return mGL.getCaptureService().colorIs(pointBattleResultClearReward) ||
                mGL.getCaptureService().colorIs(pointBattleNext);
    }

    private boolean isCurrentBattleDied() throws InterruptedException {
        sleep(20);
        if (mGL.getCaptureService().colorIs(pointBattleDied)) {
            mGL.getInputService().tapOnScreen(pointBattleDied.coord);
            sleep(2000);
            mGL.getInputService().tapOnScreen(pointBattleDiedExit.coord);
            sleep(2000);
        }
        return false;
    }

    public boolean isBattleDied() {
        return mPreviousDied;
    }

    public int battleRoutine(int retry) throws InterruptedException {
        while(retry > 0 && !isBattleResultShowed()) {
            int randomDelay = (int) (Math.random() * 50) + 1000;

            // is ongoing then wait for a while
            if (isBattleOngoing()) {
                sleep(randomDelay);
            }
            mGL.setTouchShift(800);
            mGL.getInputService().tapOnScreen(pointBattleResultClearReward.coord);
            mGL.setTouchShift(6);


            if (isCurrentBattleDied()) {
                mPreviousDied = true;
                sleep(12000);
                return 0;
            }

            retry--;
        }

        if (retry <= 0) {
            sendMessage("戰鬥逾時");
            return -1;
        }

        sendMessage("戰鬥結束");
        return 0;
    }

    public int postBattleSetup(int loopMode) throws InterruptedException {

        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(
                pointBattleResultClearReward, pointBattleAgain, 70, 50) < 0) {
            sendMessage("結果skip失敗");
        }
        sleep(1000);

        if (loopMode == sBattleLoopModeNext)
            mGL.getInputService().tapOnScreen(pointBattleNext.coord);
        else if (loopMode == sBattleLoopModeAgain)
            mGL.getInputService().tapOnScreen(pointBattleAgain.coord);

        return 0;
    }

}
