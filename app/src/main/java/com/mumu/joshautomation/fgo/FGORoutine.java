package com.mumu.joshautomation.fgo;

import com.mumu.joshautomation.AppPreferenceValue;
import com.mumu.joshautomation.R;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.joshautomation.script.DefinitionLoader;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.Log;
import com.mumu.libjoshgame.ScreenColor;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.ArrayList;

import static com.mumu.joshautomation.fgo.FGORoutineDefine.*;

class FGORoutine {
    private static final String TAG = "FGORoutine";
    private JoshGameLibrary mGL;
    private AutoJobEventListener mCallbacks;
    private DefinitionLoader.DefData mDef;

    private static final int sCardBurst = 0;
    private static final int sCardArt = 1;
    private static final int sCardQuick = 2;
    private static final int sCardUnknown = 3;

    private static final int sBattleDone = 0;
    private static final int sBattleWaitTimeout = -1;
    private static final int sBattleWaitResultTimeout = -2;
    private static final int sBattleDie = -3;

    FGORoutine(JoshGameLibrary gl, AutoJobEventListener el) {
        mGL = gl;
        mCallbacks = el;

        mDef = DefinitionLoader.getInstance().requestDefData(R.raw.fgo_definitions,
                gl.getScreenWidth() + "x" + gl.getScreenHeight());
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

    // Definition helper functions
    private ScreenPoint SPT(String name) { return mDef.getScreenPoint(name);}
    private ScreenCoord SCD(String name) { return mDef.getScreenCoord(name);}
    private ScreenColor SCL(String name) { return mDef.getScreenColor(name);}
    private ArrayList<ScreenPoint> SPTList(String name) {return mDef.getScreenPoints(name);}
    private ArrayList<ScreenCoord> SCDList(String name) {return mDef.getScreenCoords(name);}
    private ArrayList<ScreenColor> SCLList(String name) {return mDef.getScreenColors(name);}


    /* =======================
     * Battle Card Checking and Tapping
     * =======================
     */
    private int[] getCurrentCardPresent() {
        int ret[] = new int[5];

        for(int i = 0; i < 5; i++) {
            if (mGL.getCaptureService().checkColorIsInRegion(
                    SCDList("cardPositionStart").get(i),
                    SCDList("cardPositionEnd").get(i),
                    SCLList("cardArt"))) {
                ret[i] = sCardArt;
            } else if (mGL.getCaptureService().checkColorIsInRegion(
                    SCDList("cardPositionStart").get(i),
                    SCDList("cardPositionEnd").get(i),
                    SCLList("cardBurst"))) {
                ret[i] = sCardBurst;
            } else if (mGL.getCaptureService().checkColorIsInRegion(
                    SCDList("cardPositionStart").get(i),
                    SCDList("cardPositionEnd").get(i),
                    SCLList("cardQuick"))) {
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
            if (mGL.getCaptureService().colorIs(SPTList("char100NPChars").get(i)))
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

    public void tapOnCard(int[] cardIndex) throws InterruptedException {
        for(int i : cardIndex) {
            ScreenCoord coord = ScreenCoord.getTwoPointCenter(
                    SCDList("cardPositionStart").get(i),
                    SCDList("cardPositionEnd").get(i));
            mGL.getInputService().tapOnScreen(coord);
            sleep(200);
        }
    }

    public void tapOnSkill(ArrayList<BattleArgument.BattleSkill> skillIndex) throws InterruptedException {
        if (skillIndex.size() < 1)
            return;

        for(BattleArgument.BattleSkill sk : skillIndex) {

            if (sk.skill < 9) { //Normal Skills
                mGL.getInputService().tapOnScreen(SCDList("cardSkills").get(sk.skill));
                sendMessage("點技能" + sk.skill);
                sleep(1000);

                if (sk.target > 0) {
                    sendMessage("點目標" + sk.target);
                    tapOnTarget(sk.target);
                } else {
                    sendMessage("此技能無目標");
                }
            } else if (sk.skill > 9 && sk.skill <= 12) { //Normal Master Skills
                mGL.getInputService().tapOnScreen(SPT("masterSkillButton").coord);
                sendMessage("點Master技能");
                sleep(500);
                mGL.getInputService().tapOnScreen(SCDList("masterSkills").get(sk.skill - 10));
                sendMessage("點Master技能" + (sk.skill - 9));
                sleep(500);

                if (sk.target > 0) {
                    sendMessage("點目標" + sk.target);
                    tapOnTarget(sk.target);
                } else {
                    sendMessage("此技能無目標");
                }
            } else if (sk.skill == 90) { //Master skill: change servants
                if (sk.change_target > 0 && sk.target > 0) {

                    mGL.getInputService().tapOnScreen(SPT("masterSkillButton").coord);
                    sendMessage("點Master技能");
                    sleep(500);
                    mGL.getInputService().tapOnScreen(SCDList("masterSkills").get(2));
                    sendMessage("點換人");
                    sleep(500);

                    //tap on left side
                    if (mGL.getCaptureService().colorIs(inChangingServant)) {
                        mGL.getInputService().tapOnScreen(SCDList("changeServants").get(sk.change_target - 1));
                        sleep(250);
                        mGL.getInputService().tapOnScreen(SCDList("changeServants").get(sk.target + 2));
                        sleep(750);

                        //tap confirm
                        mGL.getInputService().tapOnScreen(SPT("inChangingServant").coord);
                        sleep(2000);
                        if (mGL.getCaptureService().waitOnColor(SPT("pointBattleButton"), 100) < 0)
                            sendMessage("等不到戰鬥按鈕");
                    } else {
                        sendMessage("換人頁面出不來");
                    }
                } else {
                    sendMessage("換人技能要有目標");
                }
            } else { //No such skill, should never happened
                sendMessage("不合法的技能" + sk.skill);
            }

            sleep(2400);
        }
    }

    public void tapOnRoyal(int[] royal) throws InterruptedException {
        if (royal.length < 1)
            return;

        for(int i : royal) {
            mGL.getInputService().tapOnScreen(SCDList("cardRoyals").get(i));
            sendMessage("點寶具" + i);
            sleep(500);
        }
    }

    public void tapOnTarget(int target) throws InterruptedException {
        if (target > 0 && target <= 3) {
            if (mGL.getCaptureService().colorIs(SPT("inSelectTarget")))
                mGL.getInputService().tapOnScreen(SCDList("cardTargets").get(target - 1));
        }
    }

    private int selectFriendSupport(int maxSwipe) throws InterruptedException {
        ScreenCoord coordFound;

        do {
            coordFound = mGL.getCaptureService().findColorSegment(SCD("pointFriendSupStart"),
                    SCD("pointFriendSupEnd"), SPTList("pointFriendSupPoints"));

            sleep(500);
            if (coordFound == null) {
                mGL.getInputService().swipeOnScreen(SCD("pointSwipeStart"), SCD("pointSwipeEnd"));
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

    public int battlePreSetup(boolean swipeFriend) throws InterruptedException {
        sleep(3000);

        //try to find friend's servant, if not found, touch first one
        boolean useFriendEnabled = AppPreferenceValue.getInstance().getPrefs().getBoolean("battleUseFriendOnly", false);
        if (useFriendEnabled) {
            if (selectFriendSupport(2) < 0) {
                mGL.getInputService().swipeOnScreen(SCD("pointSwipeEnd"), SCD("pointSwipeStart"));
                sleep(200);
                mGL.getInputService().swipeOnScreen(SCD("pointSwipeEnd"), SCD("pointSwipeStart"));
                sleep(200);
                mGL.getInputService().tapOnScreen(SCD("pointFriendSelect"));
            }
        } else {
            sendMessage("選擇第一位好友");
            mGL.getInputService().tapOnScreen(SCD("pointFriendSelectDefault"));
        }

        sleep(1500);
        if (mGL.getCaptureService().waitOnColor(SPT("pointEnterStage"), 20) < 0) {
            return -1;
        }

        sendMessage("進入關卡");
        mGL.getInputService().tapOnScreen(SPT("pointEnterStage").coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(SPT("pointEnterStage").coord); // just be safe

        return 0;
    }

    public int battleGetStage() {
        for(int i = 0; i < SPTList("battleStages").size(); i++) {
            if (mGL.getCaptureService().colorIs(SPTList("battleStages").get(i)))
                return i;
        }

        return -1;
    }

    public int battleRoutine(BattleArgument arg) throws InterruptedException {
        String cardInfo;
        int[] optimizedDraw, cardStatusNow;
        ArrayList<BattleArgument.BattleSkill> skillDraw;
        int[] royalDraw = new int[0];
        int[] royalAvail = new int[0];
        int resultTry = 20; //fail retry of waiting result
        int battleTry = 150; // fail retry of waiting battle button (150 * 1 = 150 secs)
        int checkCardTry = 20; // fail retry of waiting card recognize
        int battleStage = 0; //indicate which stage of battle (start from 0 but it will start from 1 when displaying)
        int battleRound = 0; //indicate which round of battle in a stage (start from 0 but it will start from 1 when displaying)
        boolean useRoyalIfAvailable = AppPreferenceValue.getInstance().getPrefs().getBoolean("battleUseRoyal", true);
        boolean useOptimizeDraw = AppPreferenceValue.getInstance().getPrefs().getBoolean("battleOptPref", true);

        sendMessage("這次戰鬥參數：" + (arg == null ?  "無" : arg.toString() ) );
        sleep(500);
        while(!mGL.getCaptureService().colorIs(SPT("pointBattleResult")) && battleTry > 0) {
            sleep(500);
            sendMessage("在等Battle按鈕" + (150 - battleTry));

            //detect die
            if (battleDieCheckAndHandle() == 0) {
                Log.d(TAG, "Battle failed. return here");
                return sBattleDie;
            }

            //wait for battle button
            if (mGL.getCaptureService().waitOnColor(SPT("pointBattleButton"), 10) < 0) {
                Log.d(TAG, "Cannot find battle button, checking if finished");
                battleTry--;
                continue;
            }

            //found battle button, reset try count
            battleTry = 150;
            checkCardTry = 20;

            //check for stage, default 1
            int thisStage = battleGetStage();
            if (thisStage < 0) {
                thisStage = 0;
                sendMessage("回合判斷失敗");
                sleep(1000);
            }

            if (thisStage != battleStage) {
                battleStage = thisStage;
                battleRound = 0;
            }
            sendMessage("戰鬥(" + (battleStage+1) + "," + (battleRound+1) + ")");
            sleep(1000);

            //check skill
            if (arg != null) {
                skillDraw = arg.getSkillIndexOfStage(battleStage, battleRound);
                sendMessage("技能需求");
                tapOnSkill(skillDraw);
            }

            //check royal available
            if (useRoyalIfAvailable) {
                royalAvail = getRoyalAvailability();
                sendMessage("寶具可用數" + royalAvail.length);
            }

            //tap battle
            mGL.getInputService().tapOnScreen(SPT("pointBattleButton").coord);
            sleep(500);
            if (mGL.getCaptureService().colorIs(SPT("pointBattleButton"))) {
                sendMessage("沒點到戰鬥?再點一次");
                mGL.getInputService().tapOnScreen(SPT("pointBattleButton").coord);
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
                royalDraw = arg.getRoyalIndexOfStage(battleStage, battleRound);
                tapOnRoyal(royalDraw);
            } else if (useRoyalIfAvailable) { //if arg doesn't specific royal and use royal is enabled
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
        while (!mGL.getCaptureService().colorIs(SPT("pointBattleNext"))
                && !mGL.getCaptureService().colorIs(FGORoutineDefineTW.pointBattleNext)
                && resultTry > 0) {
            mGL.getInputService().tapOnScreen(SPT("pointBattleResult").coord);
            resultTry--;
            sleep(500);
        }

        if (resultTry == 0)
            return sBattleWaitResultTimeout;

        sendMessage("點下一步");
        mGL.getInputService().tapOnScreen(SPT("pointBattleNext").coord);
        sleep(1000);

        return sBattleDone;
    }

    public int battleDieCheckAndHandle() throws InterruptedException {
        if (mGL.getCaptureService().colorIs(SPT("pointBattleDieDetect"))) {
            //double confirm backoff button present
            sendMessage("似乎全軍覆沒了");
            if (mGL.getCaptureService().colorIs(SPT("pointBattleDieBackoff"))) {
                mGL.getInputService().tapOnScreen(SPT("pointBattleDieBackoff").coord);
                sleep(1000);
                mGL.getInputService().tapOnScreen(SPT("pointBattleDieConfirm").coord);
                sleep(1000);
                mGL.getInputService().tapOnScreen(SPT("pointBattleDieClose").coord);
                sleep(1000);
                return 0;
            }
        }

        return -1;
    }

    public int battleHandleFriendRequest() throws InterruptedException{
        sleep(500);
        if (mGL.getCaptureService().waitOnColor(SPT("pointDenyFriend"), 20) < 0) {
            sendMessage("沒出現朋友請求");
        } else {
            mGL.getInputService().tapOnScreen(SPT("pointDenyFriend").coord);
            sleep(500);
        }
        return 0;
    }

    public int battleHandleFriendRequestTW() throws InterruptedException {
        sleep(500);
        if (mGL.getCaptureService().waitOnColor(FGORoutineDefineTW.pointDenyFriend, 20) < 0) {
            sendMessage("沒出現朋友請求");
        } else {
            mGL.getInputService().tapOnScreen(FGORoutineDefineTW.pointDenyFriend.coord);
            sleep(500);
        }
        return 0;
    }

    public int battlePostSetup() throws InterruptedException {

        int retry = 40;
        while(retry-- > 0) {
            if (mGL.getCaptureService().colorIs(SPT("pointQuestClearCube")) ||
                    mGL.getCaptureService().colorIs(SPT("pointQuestClearStone"))) {
                sendMessage("破關獎勵出現");
                mGL.getInputService().tapOnScreen(SPT("pointQuestClearStone").coord);
                sleep(100);
                return 0;
            }

            sleep(100);
        }
        sendMessage("沒出現破關獎勵");

        return 0;
    }

    public int battlePostSetupTW() throws InterruptedException {

        if (mGL.getCaptureService().waitOnColor(FGORoutineDefineTW.pointQuestClear, 60) < 0) {
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
    public int waitForSkip(int maxTry) throws InterruptedException {
        if (mGL.getCaptureService().waitOnColor(SPT("pointSkipDialog"), maxTry) < 0) {
            Log.w(TAG, "Skip not found.");
            return -1;
        } else {
            sendMessage("找到SKIP但是等一下");
            sleep(3000);
            mGL.getInputService().tapOnScreen(SPT("pointSkipDialog").coord);
            sleep(1000);
            mGL.getInputService().tapOnScreen(SPT("pointSkipConfirm").coord);
            sleep(3000);
            return 0;
        }
    }

    public int waitForUserMode(int maxTry) throws InterruptedException {
        while (maxTry-- > 0) {
            if (mGL.getCaptureService().colorIs(SPT("pointHomeApAdd")) ||
                    mGL.getCaptureService().colorIs(SPT("pointHomeApAddV2"))) {
                return 0;
            }
            sleep(100);
        }

        return -1;
    }

    /* =======================
     * Home Info
     * =======================
     */
    public boolean isInHomeScreen() {
        return mGL.getCaptureService().colorIs(SPT("pointHomeOSiRaSe"));
    }

    public boolean isInUserMode() {
        return mGL.getCaptureService().colorIs(SPT("pointHomeApAdd"));
    }

    public int findNextAndClick(int retry, boolean enableGlobal) throws InterruptedException {
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
            coordFound = mGL.getCaptureService().findColorSegment(SCD("pointRightNextStart"),
                    SCD("pointRightNextEnd"), SPTList("pointRightNextPoints"));
            if (coordFound == null) {
                coordFound = mGL.getCaptureService().findColorSegment(SCD("pointLeftNextStart"),
                        SCD("pointLeftNextEnd"), SPTList("pointLeftNextPoints"));
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

    public int returnToHome(int retry) throws InterruptedException {
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
