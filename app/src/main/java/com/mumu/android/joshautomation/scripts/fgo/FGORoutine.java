package com.mumu.android.joshautomation.scripts.fgo;

import android.util.Log;
import com.mumu.android.joshautomation.R;
import com.mumu.android.joshautomation.autojob.AutoJobEventListener;
import com.mumu.android.joshautomation.content.AppPreferenceValue;
import com.mumu.android.joshautomation.content.DefinitionLoader;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenColor;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.ArrayList;

class FGORoutine {
    private static final String TAG = "FGORoutine";
    private GameLibrary20 mGL;
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

    public static final int STATE_UNKNOWN = -1;
    public static final int STATE_IN_HOME = 0;
    public static final int STATE_IN_BATTLE = 1;
    public static final int STATE_IN_BATTLE_OVER = 2;

    FGORoutine(GameLibrary20 gl, AutoJobEventListener el) {
        mGL = gl;
        mCallbacks = el;

        // FGO game 1080p related resolution should treat as the same
        // i.e., 1080x1920, 1080x2160, 1080x2246 ... etc are the same.
        String resolution = gl.getDeviceResolution()[0] + "x" + gl.getDeviceResolution()[1];
        if (gl.getDeviceResolution()[0] == 1080)
            mDef = DefinitionLoader.getInstance().requestDefData(R.raw.fgo_definitions, "fgo_definitions.xml", "1080x1920");
        else
            mDef = DefinitionLoader.getInstance().requestDefData(R.raw.fgo_definitions, "fgo_definitions.xml", resolution);
    }

    private void sendMessage(String msg) {
        boolean verboseMode = AppPreferenceValue.getInstance().getPrefs().getBoolean("debugLogPref", false);

        // Send message to screen
        if (mCallbacks != null)
            mCallbacks.onMessageReceived(msg, this);

        // Send message to log txt file under /sdcard/ja.log
        if (verboseMode)
            Log.d(TAG, msg);
    }

    private void sleep(int time) throws InterruptedException {
        try {
            String sleepMultiplier = AppPreferenceValue.getInstance().getPrefs().getString("battleSpeed", "1.0");
            Double sleepMultiplyValue = Double.parseDouble(sleepMultiplier);
            Thread.sleep((long) (time * sleepMultiplyValue));
        } catch (NumberFormatException e) {
            Thread.sleep(time);
        }
    }

    // Definition helper functions
    private ScreenPoint SPT(String name) { if (mDef.getScreenPoint(name) == null) { sendMessage("找不到"+name); } return mDef.getScreenPoint(name);}
    private ScreenCoord SCD(String name) { if (mDef.getScreenCoord(name) == null) { sendMessage("找不到"+name); } return mDef.getScreenCoord(name);}
    private ScreenColor SCL(String name) { if (mDef.getScreenColor(name) == null) { sendMessage("找不到"+name); } return mDef.getScreenColor(name);}
    private ArrayList<ScreenPoint> SPTList(String name) {if (mDef.getScreenPoints(name) == null) { sendMessage("找不到"+name); } return mDef.getScreenPoints(name);}
    private ArrayList<ScreenCoord> SCDList(String name) {if (mDef.getScreenCoords(name) == null) { sendMessage("找不到"+name); } return mDef.getScreenCoords(name);}
    private ArrayList<ScreenColor> SCLList(String name) {if (mDef.getScreenColors(name) == null) { sendMessage("找不到"+name); } return mDef.getScreenColors(name);}

    // Definition getter
    public DefinitionLoader.DefData getDef() {return mDef;}


    /* =======================
     * Battle Card Checking and Tapping
     * =======================
     */
    private int[] getCurrentCardPresent() throws Exception {
        int ret[] = new int[5];

        for(int i = 0; i < 5; i++) {
            if (mGL.colorsAreInRect(
                    SCDList("cardPositionStart").get(i),
                    SCDList("cardPositionEnd").get(i),
                    SCLList("cardArt"))) {
                ret[i] = sCardArt;
            } else if (mGL.colorsAreInRect(
                    SCDList("cardPositionStart").get(i),
                    SCDList("cardPositionEnd").get(i),
                    SCLList("cardBurst"))) {
                ret[i] = sCardBurst;
            } else if (mGL.colorsAreInRect(
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
        StringBuilder cardInfo = new StringBuilder("");
        for (int i : series) {
            if (i == sCardUnknown) {
                cardInfo = new StringBuilder("Recognize failed");
                break;
            }
            cardInfo.append(getCardName(i));
        }

        return cardInfo.toString();
    }

    /*
     * getOptimizeDraw
     * this allows user to use their policy and decide which card should be selected
     * pattern: current card present on screen
     * policy: user select policy
     * royalSelected: if this round has royal selected, than we skip chain forming rule
     */
    private int[] getOptimizeDraw(int[] pattern, int policy, boolean royalSelected) {
        int[] select = new int[3];
        int selected = 0;
        int cardTypeFirst;
        boolean chainAvailFirst;
        boolean minimizeQuickUsage = false;

        // Parsing policy
        switch (policy) {
            case 1:
                cardTypeFirst = sCardBurst;
                chainAvailFirst = false;
                break;
            case 2:
                cardTypeFirst = sCardArt;
                chainAvailFirst = false;
                break;
            case 3:
                cardTypeFirst = sCardQuick;
                chainAvailFirst = false;
                break;
            case 4:
                cardTypeFirst = sCardBurst;
                chainAvailFirst = true;
                break;
            case 5:
                cardTypeFirst = sCardArt;
                chainAvailFirst = true;
                break;
            case 6:
                cardTypeFirst = sCardQuick;
                chainAvailFirst = true;
                break;
            case 7:
                cardTypeFirst = sCardBurst;
                chainAvailFirst = true;
                minimizeQuickUsage = true;
                break;
            case 8:
                cardTypeFirst = sCardArt;
                chainAvailFirst = true;
                minimizeQuickUsage = true;
                break;
            default:
                Log.e(TAG, "No policy " + policy + " return default card select");
            case 0:
                return new int[]{0, 1, 2};
        }

        if (royalSelected)
            chainAvailFirst = false;

        // Pick chain available card, if there's a number of card at least 3, use it first
        if (chainAvailFirst) {
            int[] cardPresentCount = new int[]{0, 0, 0}; //Burst(0), Art(1), Quick(2) card count
            for (int card : pattern) {
                cardPresentCount[card]++;
            }

            for (int i = 0; i < 3; i++)
                if (cardPresentCount[i] >= 3 && !(minimizeQuickUsage && i == sCardQuick))
                    cardTypeFirst = i;
        }

        // Pick wanted card
        for(int i = 0; i < pattern.length; i++) {
            if (pattern[i] == cardTypeFirst) {
                select[selected] = i;
                selected++;

                if(selected == 3)
                    return select;
            }
        }

        // Pick last card without QuickCard if specific
        if (minimizeQuickUsage) {
            for(int i = 0; i < pattern.length; i++) {
                boolean alreadyIn = false;
                for(int j = 0; j < selected; j++) {
                    if (select[j] == i)
                        alreadyIn = true;
                }

                if(!alreadyIn && (pattern[i] != sCardQuick)) {
                    select[selected] = i;
                    selected++;

                    if (selected == 3)
                        return select;
                }
            }
        }

        // Pick last card
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
    private int[] getRoyalAvailability() throws Exception {
        ArrayList<Integer> retSet = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            if (mGL.colorIs(SPTList("char100NPChars").get(i)))
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

    public void tapOnLoopStage() {
        mGL.mouseClick(mDef.getScreenCoord("pointLoopBattleStage"));
    }

    public void tapOnContinue() {
        mGL.mouseClick(mDef.getScreenCoord("pointContinueBattle"));
    }

    public void tapOnCard(int[] cardIndex) throws InterruptedException {
        for(int i : cardIndex) {
            ScreenCoord coord = ScreenCoord.getTwoPointCenter(
                    SCDList("cardPositionStart").get(i),
                    SCDList("cardPositionEnd").get(i));
            mGL.mouseClick(coord);
            sleep(200);
        }
    }

    public void tapOnSkill(ArrayList<BattleArgument.BattleSkill> skillIndex) throws Exception {
        if (skillIndex.size() < 1)
            return;

        for(BattleArgument.BattleSkill sk : skillIndex) {

            if (sk.skill < 9) { //Normal Skills
                mGL.mouseClick(SCDList("cardSkills").get(sk.skill));
                sendMessage("點技能" + (sk.skill + 1));
                sleep(1000);

                if (sk.target > 0) {
                    sendMessage("點目標" + sk.target);
                    tapOnTarget(sk.target);
                } else {
                    sendMessage("此技能無目標");
                }
            } else if (sk.skill >= 10 && sk.skill <= 12) { //Normal Master Skills
                mGL.mouseClick(SPT("masterSkillButton").coord);
                sendMessage("點Master技能");
                sleep(1000);
                mGL.mouseClick(SCDList("masterSkills").get(sk.skill - 10));
                sendMessage("點Master技能" + (sk.skill - 10 + 1));
                sleep(1000);

                if (sk.target > 0) {
                    sendMessage("點目標" + sk.target);
                    tapOnTarget(sk.target);
                } else {
                    sendMessage("此技能無目標");
                }
            } else if (sk.skill >= 20 && sk.skill <= 22) {
                mGL.mouseClick(SCDList("enemyTargets").get(sk.skill - 20));
                sendMessage("點敵方" + (sk.skill - 20 + 1));
                sleep(500);
            } else if (sk.skill == 90) { //Master skill: change servants
                if (sk.change_target > 0 && sk.target > 0) {

                    mGL.mouseClick(SPT("masterSkillButton").coord);
                    sendMessage("點Master技能");
                    sleep(1000);
                    mGL.mouseClick(SCDList("masterSkills").get(2));
                    sendMessage("點換人");
                    sleep(1500);

                    //tap on left side
                    if (mGL.colorIs(SPT("inChangingServant"))) {
                        mGL.mouseClick(SCDList("changeServants").get(sk.change_target - 1));
                        sleep(500);
                        mGL.mouseClick(SCDList("changeServants").get(sk.target + 2));
                        sleep(1000);

                        //tap confirm
                        mGL.mouseClick(SPT("inChangingServant").coord);
                        sleep(2000);
                        if (mGL.waitOnColor(SPT("pointBattleButton"), 100) == false)
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

            sleep(2500);
        }
    }

    public void tapOnRoyal(int[] royal) throws InterruptedException {
        if (royal.length < 1)
            return;

        for(int i : royal) {
            mGL.mouseClick(SCDList("cardRoyals").get(i));
            sendMessage("點寶具" + (i + 1));
            sleep(500);
        }
    }

    public void tapOnTarget(int target) throws Exception {
        if (target > 0 && target <= 3) {
            if (mGL.colorIs(SPT("inSelectTarget"))) {
                mGL.mouseClick(SCDList("cardTargets").get(target - 1));
            } else {
                sendMessage("目標視窗無法辨識，隨便點");
                mGL.mouseClick(SCDList("cardTargets").get(target - 1));
            }
        }
    }

    private int selectFriendSupport(int maxSwipe) throws Exception {
/*        ScreenCoord coordFound;

        do {
            coordFound = mGL.findColorSegment(SCD("pointFriendSupStart"),
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
        coordFound.x = SCD("pointFriendSelect").x;
        mGL.mouseClick(coordFound);
*/
        return 0;
    }

    /* =======================
     * Battle Info
     * =======================
     */

    public int battleHandleAPSupply() throws Exception {
        int shouldEatAppleIfNeeded = Integer.parseInt(AppPreferenceValue.getInstance().getPrefs().getString("battleEatApple", "0"));

        if (mGL.colorsAre(SPTList("pointAPChargeGoldApple"))) {
            if (shouldEatAppleIfNeeded > 0) {
                sendMessage("嘗試吃蘋果");
                sleep(500);

                switch (shouldEatAppleIfNeeded) {
                    case 1: //Gold apple required
                        mGL.mouseClick(SPTList("pointAPChargeGoldApple").get(0).coord);
                        break;
                    case 2:
                        mGL.mouseClick(SPTList("pointAPChargeSilverApple").get(0).coord);
                        break;
                    case 3:
                        mGL.mouseClick(SPTList("pointAPChargeTanApple").get(0).coord);
                        break;
                    default:
                        break;
                }

                sleep(2000);
                if (mGL.colorIs(SPT("pointAPChargeGoldAppleConfirm"))) {
                    mGL.mouseClick(SPT("pointAPChargeGoldAppleConfirm").coord);
                    sleep(1000);
                } else {
                    sendMessage("吃蘋果失敗");
                    return -1;
                }
            } else {
                sendMessage("AP不足，腳本結束");
                return -1;
            }
        } else {
            sendMessage("AP足夠");
        }
        sleep(2000);
        return 0;
    }

    public int battlePreSetup(boolean swipeFriend) throws Exception {

        //try to find friend's servant, if not found, touch first one
        boolean useFriendEnabled = AppPreferenceValue.getInstance().getPrefs().getBoolean("battleUseFriendOnly", false);
        if (useFriendEnabled) {
            if (selectFriendSupport(2) < 0) {
                mGL.mouseSwipe(SCD("pointSwipeEnd"), SCD("pointSwipeStart"));
                sleep(200);
                mGL.mouseSwipe(SCD("pointSwipeEnd"), SCD("pointSwipeStart"));
                sleep(200);
                mGL.mouseClick(SCD("pointFriendSelect"));
            }
        } else {
            sendMessage("選擇第一位好友");
            mGL.mouseClick(SCD("pointFriendSelectDefault"));
        }

        sleep(1500);
        /*
        if (mGL.waitOnColor(SPT("pointEnterStage"), 20) == false) {
            return -1;
        }*/

        sendMessage("進入關卡");
        mGL.mouseClick(SPT("pointEnterStage").coord);
        sleep(1000);
        mGL.mouseClick(SPT("pointEnterStage").coord); // just be safe

        return 0;
    }

    public int battleContinueSetup() throws InterruptedException {

        sendMessage("選擇第一位好友");
        mGL.mouseClick(SCD("pointFriendSelectDefault"));

        return 0;
    }
    private int battleGetStage() throws Exception {
        for(int i = 0; i < SPTList("battleStages").size(); i++) {
            sleep(100);
            if (mGL.colorIs(SPTList("battleStages").get(i)))
                return i;
        }

        return -1;
    }

    public int battleRoutine(BattleArgument arg) throws Exception {
        String cardInfo;
        int[] optimizedDraw, cardStatusNow;
        ArrayList<BattleArgument.BattleSkill> skillDraw;
        final int battleMaxTries = 300; // fail retry of waiting battle button (300 * (0.1+0.2) = 90 secs)
        int[] royalDraw = new int[0];
        int[] royalAvail = new int[0];
        int resultTry = 20; //fail retry of waiting result
        int battleTry = battleMaxTries;
        int checkCardTry = 20; // fail retry of waiting card recognize
        int battleStage = 0; //indicate which stage of battle (start from 0 but it will start from 1 when displaying)
        int battleRound = 0; //indicate which round of battle in a stage (start from 0 but it will start from 1 when displaying)
        boolean useRoyalIfAvailable = AppPreferenceValue.getInstance().getPrefs().getBoolean("battleUseRoyal", true);
        int optimizeDrawPolicy = Integer.parseInt(AppPreferenceValue.getInstance().getPrefs().getString("battlePolicyPrefs", "0"));

        sendMessage("這次戰鬥參數：" + (arg == null ?  "無" : arg.toString() ) );
        sleep(500);
        while(!(mGL.colorsAre(SPTList("pointBattleResults"))
                || mGL.colorsAre(SPTList("pointBattleResults_2")))
              && battleTry > 0) {
            sleep(80);
            sendMessage("在等Battle按鈕" + (battleMaxTries - battleTry));
/*
            //detect die
            if (battleDieCheckAndHandle() == 0) {
                Log.d(TAG, "Battle failed. return here");
                return sBattleDie;
            }
*/
            //wait for battle button
            if (!mGL.colorsAre(SPTList("pointBattleButtons"))) {
                battleTry--;
                continue;
            }

            //found battle button, reset try count
            battleTry = battleMaxTries;
            checkCardTry = 20;

            sleep(1000);

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
            mGL.mouseClick(SPT("pointBattleButton").coord);
            sleep(1500);

            sendMessage("辨識卡片");
            if (optimizeDrawPolicy > 0) {
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
            boolean useRoyalInThisRound = false;
            if (arg != null) {
                royalDraw = arg.getRoyalIndexOfStage(battleStage, battleRound);
                if (royalDraw.length > 0) {
                    tapOnRoyal(royalDraw);
                    useRoyalInThisRound = true;
                } else if (useRoyalIfAvailable && royalAvail.length > 0 &&
                        arg.isNoMoreRoyalSpecify(battleStage, battleRound)) {
                    sendMessage("此後無參數設定，自動寶具");
                    tapOnRoyal(royalAvail);
                    useRoyalInThisRound = true;
                }
            } else if (useRoyalIfAvailable) { //if arg doesn't specific royal and use royal is enabled
                if (royalAvail.length > 0) {
                    sendMessage("沒有寶具指定，自動使用寶具");
                    tapOnRoyal(royalAvail);
                    useRoyalInThisRound = true;
                }
            }

            optimizedDraw = getOptimizeDraw(cardStatusNow, optimizeDrawPolicy, useRoyalInThisRound);
            tapOnCard(optimizedDraw);

            battleRound++;
            sleep(4000);
        }

        // check if this is a timeout
        if (battleTry == 0)
            return sBattleWaitTimeout;

        // tap on screen until NEXT button to exit battle
        sendMessage("戰鬥結果出現點到下一步");
        while (!mGL.colorIs(SPT("pointBattleNext"))
                && resultTry > 0) {
            mGL.mouseClick(SPT("pointBattleResult").coord);
            resultTry--;
            sleep(500);
        }

        if (resultTry == 0)
            return sBattleWaitResultTimeout;

        sendMessage("點下一步");
        mGL.mouseClick(SPT("pointBattleNext").coord);
        sleep(1000);
        mGL.mouseClick(SPT("pointBattleNext").coord);
        sleep(500);
        mGL.mouseClick(SPT("pointBattleNext").coord);
        sleep(500);

        return sBattleDone;
    }

    public int battleDieCheckAndHandle() throws Exception {
        if (mGL.colorIs(SPT("pointBattleDieDetect"))) {
            //double confirm backoff button present
            sendMessage("似乎全軍覆沒了");
            if (mGL.colorIs(SPT("pointBattleDieBackoff"))) {
                mGL.mouseClick(SPT("pointBattleDieBackoff").coord);
                sleep(1000);
                mGL.mouseClick(SPT("pointBattleDieConfirm").coord);
                sleep(1000);
                mGL.mouseClick(SPT("pointBattleDieClose").coord);
                sleep(1000);
                return 0;
            }
        }

        return -1;
    }

    public int battleHandleFriendRequest() throws Exception{
        sleep(500);
        if (mGL.waitOnColor(SPT("pointDenyFriend"), 20) == false) {
            sendMessage("沒出現朋友請求");
        } else {
            mGL.mouseClick(SPT("pointDenyFriend").coord);
            sleep(500);
        }
        return 0;
    }

    public int battlePostSetup() throws Exception {

        int retry = 40;
        while(retry-- > 0) {
            if (mGL.colorIs(SPT("pointQuestClearCube")) ||
                    mGL.colorIs(SPT("pointQuestClearStone"))) {
                sendMessage("破關獎勵出現");
                mGL.mouseClick(SPT("pointQuestClearStone").coord);
                sleep(100);
                return 0;
            }

            sleep(100);
        }
        sendMessage("沒出現破關獎勵");

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
    public int waitForSkip(int maxTry) throws Exception {
        if (mGL.waitOnColor(SPT("pointSkipDialog"), maxTry) == false) {
            Log.w(TAG, "Skip not found.");
            return -1;
        } else {
            sendMessage("找到SKIP但是等一下");
            sleep(3000);
            mGL.mouseClick(SPT("pointSkipDialog").coord);
            sleep(1000);
            mGL.mouseClick(SPT("pointSkipConfirm").coord);
            sleep(3000);
            return 0;
        }
    }

    public int waitForUserMode(int maxTry) throws Exception {
        return 0;/*
        while (maxTry-- > 0) {
            if (mGL.colorIs(SPT("pointHomeApAdd"))) {
                return 0;
            }
            sleep(100);
        }

        return -1;*/
    }

    /* =======================
     * Home Info
     * =======================
     */
    public boolean isInHomeScreen() throws Exception {
        return mGL.colorIs(SPT("pointHomeOSiRaSe"));
    }

    public boolean isInUserMode() throws Exception {
        return true;
        //return mGL.colorIs(SPT("pointHomeApAdd"));
    }

    /*
     * getGameState
     * To let script knows current game status
     */
    public int getGameState() throws Exception {
        if (isInUserMode()) {
            sendMessage("在主畫面");
            return STATE_IN_HOME;
        } else if (mGL.colorsAre(SPTList("pointBattleButtons"))) {
            sendMessage("在戰鬥");
            return STATE_IN_BATTLE;
        }
        sendMessage("未知畫面");

        return STATE_UNKNOWN;
    }

    /* ===================
     * Auto Box
     * ===================
     */
    public int runAutoBox() throws Exception {
        int i = 0;

        while (i < 100) {
            mGL.mouseClick(SCD("pointBoxOpen"));
            sleep(500);
            i++;
        }
        sendMessage("Rest Box");
        mGL.mouseClick(SCD("pointBoxReset"));
        sleep(2000);
        mGL.mouseClick(SCD("pointBoxResetConfirm"));
        sleep(2000);
        mGL.mouseClick(SCD("pointBoxReseted"));
        sleep(2000);

        return 0;
    }

}
