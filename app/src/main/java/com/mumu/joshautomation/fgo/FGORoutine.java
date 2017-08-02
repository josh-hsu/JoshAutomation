package com.mumu.joshautomation.fgo;

import android.util.Log;

import com.mumu.joshautomation.AppPreferenceValue;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenCoord;

import java.util.ArrayList;

import static com.mumu.joshautomation.fgo.FGORoutineDefine.*;

class FGORoutine {
    private static final String TAG = "FGORoutine";
    private JoshGameLibrary mGL;
    private AutoJobEventListener mCallbacks;

    FGORoutine(JoshGameLibrary gl, AutoJobEventListener el) {
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

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* =======================
     * Battle Card Checking and Tapping
     * =======================
     */
    private int[] getCurrentCardPresent() {
        int ret[] = new int[5];

        for(int i = 0; i < 5; i++) {
            if (mGL.getCaptureService().checkColorIsInRegion(
                    cardPositionStart.get(i),
                    cardPositionEnd.get(i),
                    cardArt)) {
                ret[i] = sCardArt;
            } else if (mGL.getCaptureService().checkColorIsInRegion(
                    cardPositionStart.get(i),
                    cardPositionEnd.get(i),
                    cardBurst)) {
                ret[i] = sCardBurst;
            } else if (mGL.getCaptureService().checkColorIsInRegion(
                    cardPositionStart.get(i),
                    cardPositionEnd.get(i),
                    cardQuick)) {
                ret[i] = sCardQuick;
            } else {
                ret[i] = sCardUnknown;
            }
        }

        return ret;
    }

    private boolean isCardValid(int[] cards) {
        for(int i: cards) {
            if (i == sCardUnknown)
                return false;
        }
        return true;
    }

    private String getCardName(int i) {
        switch (i) {
            case sCardArt:
                return "A";
            case sCardBurst:
                return "B";
            case sCardQuick:
                return "Q";
            case sCardUnknown:
            default:
                return "U";
        }
    }

    private String getCardNameSeries(int[] series) {
        String cardInfo = "";
        for (int i : series) {
            if (i == sCardUnknown) {
                cardInfo = "Recognize failed";
                break;
            }
            cardInfo += getCardName(i);
        }

        return cardInfo;
    }

    private int[] getOptimizeDraw(int[] pattern) {
        int[] select = new int[3];
        int selected = 0;

        for(int i = 0; i < pattern.length; i++) {
            if (pattern[i] == sCardBurst) {
                select[selected] = i;
                selected++;

                if(selected == 3)
                    return select;
            }
        }

        for(int i = 0; i < pattern.length; i++) {
            boolean alreadyIn = false;
            for(int j = 0; j < selected; j++) {
                if (select[j] == i)
                    alreadyIn = true;
            }

            if(!alreadyIn) {
                select[selected] = i;
                selected++;

                if (selected == 3)
                    return select;
            }
        }

        return null;
    }

    /*
     * getRoyalAvailability
     * returns the index array of which char has royal ready to use
     */
    private int[] getRoyalAvailability() {
        ArrayList<Integer> retSet = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            if (mGL.getCaptureService().colorIs(char100NPChars.get(i)))
                retSet.add(i);
        }

        if (retSet.size() > 0) {
            int[] ret = new int[retSet.size()];
            for(int i = 0; i < retSet.size(); i++)
                ret[i] = retSet.get(i);
            return ret;
        } else {
            return new int[] {};
        }
    }

    public void tapOnCard(int[] cardIndex) {
        for(int i : cardIndex) {
            ScreenCoord coord = ScreenCoord.getTwoPointCenter(cardPositionStart.get(i),
                    cardPositionEnd.get(i));
            mGL.getInputService().tapOnScreen(coord);
            sleep(200);
        }
    }

    public void tapOnSkill(int[] skillIndex) {
        if (skillIndex.length < 1)
            return;

        for(int i : skillIndex) {
            mGL.getInputService().tapOnScreen(cardSkills.get(i));
            sendMessage("點技能" + i);
            sleep(2400);
        }
    }

    public void tapOnRoyal(int[] royal) {
        if (royal.length < 1)
            return;

        for(int i : royal) {
            mGL.getInputService().tapOnScreen(cardRoyals.get(i));
            sendMessage("點寶具" + i);
            sleep(500);
        }
    }

    private int selectFriendSupport(int maxSwipe) {
        ScreenCoord coordFound;

        do {
            coordFound = mGL.getCaptureService().findColorSegment(pointFriendSupStart,
                    pointFriendSupEnd, pointFriendSupPoints);

            sleep(500);
            if (coordFound == null) {
                mGL.getInputService().swipeOnScreen(pointSwipeStart, pointSwipeEnd);
            }

            if(maxSwipe-- < 0 && coordFound == null) {
                return -1;
            }
        } while (coordFound == null);

        sendMessage("找到+25朋友");
        coordFound.x = pointFriendSelect.x;
        mGL.getInputService().tapOnScreen(coordFound);

        return 0;
    }

    /* =======================
     * Battle Info
     * =======================
     */

    public int battlePreSetup(Thread kThread, boolean swipeFriend) {
        sleep(3000);

        //try to find friend's servant, if not found, touch first one
        boolean useFriendEnabled = AppPreferenceValue.getInstance().getPrefs().getBoolean("battleUseFriendOnly", false);
        if (useFriendEnabled) {
            if (selectFriendSupport(2) < 0) {
                mGL.getInputService().swipeOnScreen(pointSwipeEnd, pointSwipeStart);
                sleep(200);
                mGL.getInputService().swipeOnScreen(pointSwipeEnd, pointSwipeStart);
                sleep(200);
                mGL.getInputService().tapOnScreen(pointFriendSelect);
            }
        } else {
            sendMessage("選擇第一位好友");
            mGL.getInputService().tapOnScreen(pointFriendSelectDefault);
        }

        sleep(1500);
        if (mGL.getCaptureService().waitOnColor(pointEnterStage, 20, kThread) < 0) {
            return -1;
        }

        sendMessage("進入關卡");
        mGL.getInputService().tapOnScreen(pointEnterStage.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointEnterStage.coord); // just be safe

        return 0;
    }

    public int battleRoutine(Thread kThread, BattleArgument arg) {
        String cardInfo;
        int[] optimizedDraw, cardStatusNow, skillDraw;
        int[] royalDraw = new int[0];
        int[] royalAvail = new int[0];
        int resultTry = 20; //fail retry of waiting result
        int battleTry = 150; // fail retry of waiting battle button (150 * 1 = 150 secs)
        int checkCardTry = 20; // fail retry of waiting card recognize
        int battleRound = 1; //indicate which round of battle
        boolean useRoyalIfAvailable = AppPreferenceValue.getInstance().getPrefs().getBoolean("battleUseRoyal", true);
        boolean useOptimizeDraw = AppPreferenceValue.getInstance().getPrefs().getBoolean("battleOptPref", true);

        sendMessage("這次戰鬥參數：" + (arg == null ?  "無" : arg.toString() ) );
        sleep(500);
        while(!mGL.getCaptureService().colorIs(pointBattleResult) && battleTry > 0) {
            sleep(500);
            sendMessage("在等Battle按鈕" + (150 - battleTry));

            //detect die
            if (battleDieCheckAndHandle() == 0) {
                Log.d(TAG, "Battle failed. return here");
                return sBattleDie;
            }

            //wait for battle button
            if (mGL.getCaptureService().waitOnColor(pointBattleButton, 10, kThread) < 0) {
                Log.d(TAG, "Cannot find battle button, checking if finished");
                battleTry--;
                continue;
            }

            //found battle button, reset try count
            battleTry = 150;
            checkCardTry = 20;

            //check skill
            if (arg != null) {
                skillDraw = arg.getSkillIndexOfRound(battleRound);
                sendMessage("技能需求");
                tapOnSkill(skillDraw);
            }

            //check royal available
            if (useRoyalIfAvailable) {
                royalAvail = getRoyalAvailability();
                sendMessage("寶具可用數" + royalAvail.length);
            }

            //tap battle
            mGL.getInputService().tapOnScreen(pointBattleButton.coord);
            sleep(500);
            if (mGL.getCaptureService().colorIs(pointBattleButton)) {
                sendMessage("沒點到戰鬥?再點一次");
                mGL.getInputService().tapOnScreen(pointBattleButton.coord);
                sleep(500);
            }
            sendMessage("辨識卡片");

            if (useOptimizeDraw) {
                cardStatusNow = getCurrentCardPresent();
                while (!isCardValid(cardStatusNow) && checkCardTry > 0) {
                    cardStatusNow = getCurrentCardPresent();
                    checkCardTry--;
                }

                if (isCardValid(cardStatusNow)) {
                    cardInfo = getCardNameSeries(cardStatusNow);
                    sendMessage(cardInfo);
                } else {
                    sendMessage("卡片無法辨識，隨便按");
                    cardStatusNow = new int[] {sCardBurst, sCardBurst, sCardBurst, sCardBurst, sCardBurst};
                }
            } else {
                sendMessage("不辨識卡片，按照順序按");
                cardStatusNow = new int[] {sCardBurst, sCardBurst, sCardBurst, sCardBurst, sCardBurst};
                sleep(1500);
            }

            //check royal request if any
            if (arg != null) {
                royalDraw = arg.getRoyalIndexOfRound(battleRound);
                tapOnRoyal(royalDraw);
            }

            //if arg doesn't specific royal at all
            if (useRoyalIfAvailable) {
                if (royalDraw.length == 0 && royalAvail.length > 0) {
                    sendMessage("沒有寶具指定，自動使用寶具");
                    tapOnRoyal(royalAvail);
                }
            }

            optimizedDraw = getOptimizeDraw(cardStatusNow);
            tapOnCard(optimizedDraw);

            battleRound++;
            sleep(8000);
        }

        // check if this is a timeout
        if (battleTry == 0)
            return sBattleWaitTimeout;

        // tap on screen until NEXT button to exit battle
        sendMessage("戰鬥結果出現點到下一步");
        while (!mGL.getCaptureService().colorIs(FGORoutineDefine.pointBattleNext)
                && !mGL.getCaptureService().colorIs(FGORoutineDefineTW.pointBattleNext)
                && resultTry > 0) {
            mGL.getInputService().tapOnScreen(pointBattleResult.coord);
            resultTry--;
            sleep(500);
        }

        if (resultTry == 0)
            return sBattleWaitResultTimeout;

        sendMessage("點下一步");
        mGL.getInputService().tapOnScreen(pointBattleNext.coord);
        sleep(1000);

        return sBattleDone;
    }

    public int battleDieCheckAndHandle() {
        if (mGL.getCaptureService().colorIs(pointBattleDieDetect)) {
            //double confirm backoff button present
            sendMessage("似乎全軍覆沒了");
            if (mGL.getCaptureService().colorIs(pointBattleDieBackoff)) {
                mGL.getInputService().tapOnScreen(pointBattleDieBackoff.coord);
                sleep(1000);
                mGL.getInputService().tapOnScreen(pointBattleDieConfirm.coord);
                sleep(1000);
                mGL.getInputService().tapOnScreen(pointBattleDieClose.coord);
                sleep(1000);
                return 0;
            }
        }

        return -1;
    }

    public int battleHandleFriendRequest(Thread kThread) {
        sleep(500);
        if (mGL.getCaptureService().waitOnColor(pointDenyFriend, 20, kThread) < 0) {
            sendMessage("沒出現朋友請求");
        } else {
            mGL.getInputService().tapOnScreen(pointDenyFriend.coord);
            sleep(500);
        }
        return 0;
    }

    public int battleHandleFriendRequestTW(Thread kThread) {
        sleep(500);
        if (mGL.getCaptureService().waitOnColor(FGORoutineDefineTW.pointDenyFriend, 20, kThread) < 0) {
            sendMessage("沒出現朋友請求");
        } else {
            mGL.getInputService().tapOnScreen(FGORoutineDefineTW.pointDenyFriend.coord);
            sleep(500);
        }
        return 0;
    }

    public int battlePostSetup(Thread kThread) {

        if (mGL.getCaptureService().waitOnColor(pointQuestClear, 30, kThread) < 0) {
            sendMessage("沒出現破關魔法石");
        } else {
            mGL.getInputService().tapOnScreen(pointQuestClear.coord);
        }

        return 0;
    }

    public int battlePostSetupTW(Thread kThread) {

        if (mGL.getCaptureService().waitOnColor(FGORoutineDefineTW.pointQuestClear, 60, kThread) < 0) {
            sendMessage("沒出現破關魔法石");
        } else {
            mGL.getInputService().tapOnScreen(FGORoutineDefineTW.pointQuestClear.coord);
        }

        return 0;
    }

    public String battleGetErrorMsg(int errorCode) {
        switch (errorCode) {
            case sBattleDone:
                return "戰鬥完成";
            case sBattleWaitTimeout:
                return "戰鬥逾時";
            case sBattleWaitResultTimeout:
                return "等不到戰鬥結果";
            case sBattleDie:
                return "全軍覆沒";
            default:
                return "未知錯誤";
        }
    }

    /* =======================
     * Story Info
     * =======================
     */
    public int waitForSkip(int maxTry, Thread kThread) {
        if (mGL.getCaptureService().waitOnColor(pointSkipDialog, maxTry, kThread) < 0) {
            Log.w(TAG, "Skip not found.");
            return -1;
        } else {
            sendMessage("找到SKIP但是等一下");
            sleep(3000);
            mGL.getInputService().tapOnScreen(pointSkipDialog.coord);
            sleep(1000);
            mGL.getInputService().tapOnScreen(pointSkipConfirm.coord);
            sleep(3000);
            return 0;
        }
    }

    public int waitForUserMode(int maxTry, Thread kThread) {
        if (mGL.getCaptureService().waitOnColor(pointHomeApAdd, maxTry, kThread) < 0) {
            Log.w(TAG, "Skip not found.");
            return -1;
        }

        return 0;
    }

    /* =======================
     * Home Info
     * =======================
     */
    public boolean isInHomeScreen() {
        return mGL.getCaptureService().colorIs(pointHomeOSiRaSe);
    }

    public boolean isInUserMode() {
        return mGL.getCaptureService().colorIs(pointHomeApAdd);
    }

    public int findNextAndClick(int retry, boolean enableGlobal) {
        ScreenCoord coordFound;
        boolean searchGlobalFailOnce = false;
        int maxTry = retry;
        int[] oldAmbiguousRange;
        int[] findNextAmbRange = new int[] {0x06, 0x06, 0x35};

        //change ambiguous range
        oldAmbiguousRange = mGL.getCaptureService().getCurrentAmbiguousRange();
        mGL.setAmbiguousRange(findNextAmbRange);

        sendMessage("尋找NEXT");
        do {
            coordFound = mGL.getCaptureService().findColorSegment(pointRightNextStart,
                    pointRightNextEnd, pointRightNextPoints);
            if (coordFound == null) {
                coordFound = mGL.getCaptureService().findColorSegment(pointLeftNextStart,
                        pointLeftNextEnd, pointLeftNextPoints);
            }

            sleep(500);
            if (coordFound == null) {
                mGL.getInputService().swipeOnScreen(pointSwipeStart, pointSwipeEnd);
            }

            if(retry-- < 0 && coordFound == null) {
                mGL.setAmbiguousRange(oldAmbiguousRange);
                return -1;
            }
        } while (coordFound == null);

        sendMessage("找到選單的NEXT");
        coordFound.y += 100;
        mGL.getInputService().tapOnScreen(coordFound);
        retry = maxTry;
        sleep(4000);

        sendMessage("找下一關");
        do {
            coordFound = mGL.getCaptureService().findColorSegment(pointMapNextStart,
                    pointMapNextEnd, pointMapNextPoints);
            if (coordFound == null && enableGlobal && !searchGlobalFailOnce) {
                sendMessage("不在中間，全域搜尋一次");
                coordFound = mGL.getCaptureService().findColorSegmentGlobal(pointMapNextPoints);

                if (coordFound == null) {
                    searchGlobalFailOnce = true;
                    sendMessage("中間搜尋也失敗了");
                }
            }
            sleep(1000);

            if(retry-- < 0 && coordFound == null) {
                mGL.setAmbiguousRange(oldAmbiguousRange);
                return -1;
            }
        } while (coordFound == null);

        sendMessage("找到下一關");
        coordFound.y += 200;
        mGL.getInputService().tapOnScreen(coordFound);
        retry = maxTry;
        sleep(1500);

        sendMessage("找子關卡");
        do {
            coordFound = mGL.getCaptureService().findColorSegment(pointSubStageNextStart,
                    pointSubStageNextEnd, pointSubStageNextPoints);
            sleep(1000);

            if(retry-- < 0 && coordFound == null) {
                mGL.setAmbiguousRange(oldAmbiguousRange);
                sendMessage("找不到子關卡");
                return -1;
            }
        } while (coordFound == null);

        sendMessage("找到子關卡");
        coordFound.y += 150;
        mGL.getInputService().tapOnScreen(coordFound);
        mGL.setAmbiguousRange(oldAmbiguousRange);

        return 0;
    }

    public int returnToHome(Thread kThread, int retry) {
        int maxRetry = retry;

        while(!isInUserMode() && retry >= 0) {
            sleep(700);
            retry--;
        }

        retry = maxRetry;
        while(!isInHomeScreen() && retry >= 0) {
            mGL.getInputService().tapOnScreen(pointCloseButton.coord);
            sleep(700);
            retry--;
        }

        if (retry < 0)
            return -1;

        return 0;
    }

}
