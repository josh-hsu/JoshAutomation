package com.mumu.android.joshautomation.scripts.claudia;

import android.util.Log;

import com.mumu.android.joshautomation.R;
import com.mumu.android.joshautomation.autojob.AutoJobEventListener;
import com.mumu.android.joshautomation.content.AppPreferenceValue;
import com.mumu.android.joshautomation.content.DefinitionLoader;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenColor;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;
import com.mumu.libjoshgame.service.DeviceScreen;

import java.util.ArrayList;

public class ClaudiaRoutine {
    private static final String TAG = "ClaudiaRoutine";
    private GameLibrary20 mGL;
    private AutoJobEventListener mCallbacks;
    private DefinitionLoader.DefData mDef;

    public static final int STAGE_IN_GO_BATTLE = 0;
    public static final int STAGE_IN_BATTLE = 1;
    public static final int STAGE_IN_BATTLE_RESULT = 2;
    public static final int STAGE_IN_SELECT_FRIEND = 3;
    public static final int STAGE_IN_BATTLE_AGAIN = 4;
    public static final int STAGE_IN_NETWORK_ERROR = 5;
    public static final int STAGE_IN_FRIEND_REQUEST = 6;

    ClaudiaRoutine(GameLibrary20 gl, AutoJobEventListener el) {
        mGL = gl;
        mCallbacks = el;

        String resolution = mGL.getDeviceResolution()[0] + "x" + mGL.getDeviceResolution()[1];
        mDef = DefinitionLoader.getInstance().requestDefData(R.raw.claudia_definitions, "claudia_definitions.xml", resolution);

        if (mGL != null) {
            mGL.setScreenMainOrientation(ScreenPoint.SO_Portrait);
        }
    }

    private void sendMessage(String msg) {
        boolean verboseMode = AppPreferenceValue.getInstance().getPrefs().getBoolean("debugLogPref", true);

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
            double sleepMultiplyValue = Double.parseDouble(sleepMultiplier);
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

    /*
     * Stage determine
     */
    public int getCurrentStage() throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        if (mGL.colorsAre(SPTList("pBattleGo")))
            return STAGE_IN_GO_BATTLE;
        else if (mGL.colorsAre(SPTList("pBattleAttack")))
            return STAGE_IN_BATTLE;
        else if (mGL.colorsAre(SPTList("pBattleResult")))
            return STAGE_IN_BATTLE_RESULT;
        else if (mGL.colorsAre(SPTList("pBattleFriend")))
            return STAGE_IN_SELECT_FRIEND;
        else if (mGL.colorsAre(SPTList("pBattleAgain")))
            return STAGE_IN_BATTLE_AGAIN;
        else if (mGL.colorsAre(SPTList("pNetworkError")))
            return STAGE_IN_NETWORK_ERROR;
        else if (mGL.colorsAre(SPTList("pFriendRequest")))
            return STAGE_IN_FRIEND_REQUEST;
        else
            return -1;
    }

    private void selectFriendWithMonsterHunter(boolean useMHFriend) throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        ArrayList<ArrayList<ScreenPoint>> friendMHArrays = new ArrayList<>();
        int index = 1;
        friendMHArrays.add(SPTList("pBattleFriend1"));
        friendMHArrays.add(SPTList("pBattleFriend2"));
        friendMHArrays.add(SPTList("pBattleFriend3"));
        friendMHArrays.add(SPTList("pBattleFriend4"));
        friendMHArrays.add(SPTList("pBattleFriend5"));
        friendMHArrays.add(SPTList("pBattleFriend6"));
        friendMHArrays.add(SPTList("pBattleFriend7"));

        if (!useMHFriend) {
            mGL.mouseClick(SPTList("pBattleFriend").get(0).coord);
            sleep(1000);
            return;
        }

        mGL.setScreenshotPolicy(DeviceScreen.POLICY_MANUAL);
        mGL.requestRefresh();
        for(ArrayList<ScreenPoint> friendPoint : friendMHArrays) {
            if (mGL.colorsAre(friendPoint)) {
                ScreenCoord screenCoord = new ScreenCoord(friendPoint.get(0).coord.x - 400,
                        friendPoint.get(0).coord.y, friendPoint.get(0).coord.orientation);
                sendMessage("找到第" + index + "朋友有MH");
                mGL.mouseClick(screenCoord);
                sleep(1000);
                break;
            }
            index++;
        }
        if (index > friendMHArrays.size()) {
            sendMessage("你沒MH朋友");
            mGL.mouseClick(SPTList("pBattleFriend").get(0).coord);
            sleep(1000);
        }
        mGL.setScreenshotPolicy(DeviceScreen.POLICY_DEFAULT);
    }

    private void detectMonsterHunter() throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        boolean p1HasMH = false;
        sendMessage("偵測魔物獵人");
        mGL.mouseClick(SPTList("pBattleMagic").get(0).coord);
        sleep(800);
        if (mGL.colorsAre(SPTList("pBattleMonsterHunter"))) {
            sendMessage("有魔獸獵人自己");
            mGL.mouseClick(SPTList("pBattleMonsterHunter").get(0).coord);
            sleep(100);
            mGL.mouseClick(SPTList("pBattleMonsterHunter").get(0).coord);
            p1HasMH = true;
        } else {
            sendMessage("沒有魔獸獵人自己");
        }

        sendMessage("換隊友");
        sleep(500);
        mGL.mouseClick(SCDList("cBattleMembers").get(3));
        sleep(500);
        mGL.mouseClick(SCDList("cBattleMembers").get(3));
        sleep(500);
        if (p1HasMH) {
            mGL.mouseClick(SPTList("pBattleMagic").get(0).coord);
            sleep(1000);
        }
        if (mGL.colorsAre(SPTList("pBattleMonsterHunter"))) {
            sendMessage("有魔獸獵人戰友");
            mGL.mouseClick(SPTList("pBattleMonsterHunter").get(0).coord);
        } else {
            sendMessage("沒有魔獸獵人戰友");
            if (mGL.colorsAre(SPTList("pBattleCancelMagic")))
                mGL.mouseClick(SPTList("pBattleCancelMagic").get(0).coord);
        }
        sleep(500);
        mGL.mouseClick(SCDList("cBattleMembers").get(0));
        sleep(500);
        mGL.mouseClick(SCDList("cBattleMembers").get(0));
        sleep(500);
        mGL.mouseClick(SCDList("cBattleMembers").get(0));
        sleep(500);
    }

    private void randomClickSkills() throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        if (mGL.colorsAre(SPTList("pBattleCancelMagic"))) {
            sendMessage("亂點到魔法了");
            mGL.mouseClick(SPTList("pBattleCancelMagic").get(0).coord);
            sleep(1000);
        }
        mGL.mouseClick(SPTList("pBattleAttack").get(0).coord);
        sleep(200);
        mGL.mouseClick(SCDList("cBattleSkills").get(0));
        sleep(200);
        mGL.mouseClick(SPTList("pBattleAttack").get(0).coord);
        sleep(200);
        mGL.mouseClick(SCDList("cBattleSkills").get(1));
        sleep(200);
        mGL.mouseClick(SPTList("pBattleAttack").get(0).coord);
        sleep(200);
        mGL.mouseClick(SCDList("cBattleSkills").get(2));
        sleep(200);
        mGL.mouseClick(SPTList("pBattleAttack").get(0).coord);
        sleep(200);
        mGL.mouseClick(SCDList("cBattleSkills").get(3));
    }

    public void handleNetworkError() {
        mGL.mouseClick(SPTList("pNetworkError").get(0).coord);
    }

    public void handleFriendRequest() {
        mGL.mouseClick(SPTList("pFriendRequest").get(0).coord);
    }

    /*
     * Handle ATTACK -> RESULT
     */
    public int battleRoutine(boolean autoAttack, boolean useMonsterHunter) throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        boolean monsterHunterUsed = false;
        sendMessage("戰鬥開始");
        sleep(1000);

        sendMessage("開始亂點");
        while (!mGL.colorsAre(SPTList("pBattleResult")) && !mGL.colorsAre(SPTList("pBattleAgain"))) {
            if (!monsterHunterUsed && useMonsterHunter && mGL.colorsAre(SPTList("pBattleBossGauge"))) {
                sendMessage("BOSS出現");
                sleep(1200);
                detectMonsterHunter();
                monsterHunterUsed = true;
            }
            randomClickSkills();
        }

        sendMessage("結果出現");
        return 0;
    }

    /*
     * Handle RESULT -> AGAIN
     */
    public int postBattle() throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        int timeout = 6000; //10 seconds
        int sleepInterval = 250; // 250 ms
        int retryCount = timeout / sleepInterval;

        sendMessage("開始處理結果");
        while (!mGL.colorsAre(SPTList("pBattleAgain")) && retryCount-- > 0) {
            mGL.mouseClick(SPTList("pBattleResult").get(0).coord);
            sleep(sleepInterval);
        }

        if (retryCount <= 0) {
            sendMessage("處理結果失敗了，你可能升級或斷線了");
            return -1;
        }

        return 0;
    }

    /*
     * Handle AGAIN -> SELECT_FRIEND
     */
    public int battleAgain() throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        int timeout = 20000; //20 seconds
        int sleepInterval = 1000; // 1 second
        int retryCount = timeout / sleepInterval;

        sendMessage("按再戰");
        while (mGL.colorsAre(SPTList("pBattleAgain")) && retryCount-- > 0) {
            mGL.mouseClick(SPTList("pBattleAgain").get(0).coord);
            sleep(sleepInterval);
        }

        if (retryCount <= 0) {
            sendMessage("處理結果失敗了，你可能升級或斷線了");
            return -1;
        }

        sleep(1000);
        return 0;
    }

    public void preBattleSelectFriend(boolean useMHFriend) throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        if (mGL.colorsAre(SPTList("pBattleFriend"))) {
            selectFriendWithMonsterHunter(useMHFriend);
            sleep(1000);
        }
    }

    /*
     * Handle Go -> BATTLE
     */
    public int preBattle() throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        int timeout = 10000; //10 seconds
        int sleepInterval = 500; // 500 ms
        int retryCount = timeout / sleepInterval;

        while (mGL.colorsAre(SPTList("pBattleGo")) && retryCount-- > 0) {
            mGL.mouseClick(SPTList("pBattleGo").get(0).coord);
            sleep(sleepInterval);
        }

        while (!mGL.colorsAre(SPTList("pBattleAttack")) && retryCount-- > 0) {
            sleep(sleepInterval);
        }

        if (retryCount <= 0) {
            sendMessage("逾時");
            return -1;
        }

        sendMessage("進入戰鬥了");
        sleep(500);
        return 0;
    }
}
