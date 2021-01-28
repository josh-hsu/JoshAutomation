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

import java.util.ArrayList;

public class ClaudiaRoutine {
    private static final String TAG = "ClaudiaRoutine";
    private GameLibrary20 mGL;
    private AutoJobEventListener mCallbacks;
    private DefinitionLoader.DefData mDef;

    public static final int STAGE_IN_GO_BATTLE = 0;
    public static final int STAGE_IN_BATTLE = 1;
    public static final int STAGE_IN_BATTLE_RESULT = 2;

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
        else
            return -1;
    }

    /*
     * Handle ATTACK -> RESULT
     */
    public int battleRoutine(boolean autoAttack, boolean useMonsterHunter) throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        if (mGL.colorsAre(SPTList("pBattleMagic")) && useMonsterHunter) {
            sendMessage("有魔法");
            sleep(1000);
            mGL.mouseClick(SPTList("pBattleMagic").get(0).coord);
            sleep(1000);
            if (mGL.colorsAre(SPTList("pBattleMonsterHunter"))) {
                sendMessage("有魔獸獵人");
                mGL.mouseClick(SPTList("pBattleMonsterHunter").get(0).coord);
            } else {
                sendMessage("沒有魔獸獵人, bye");
                mGL.mouseClick(SPTList("pBattleMagic").get(0).coord);
            }
            sleep(1000);
        }

        sendMessage("開始亂點");
        while (!mGL.colorsAre(SPTList("pBattleResult"))) {
            mGL.mouseClick(SPTList("pBattleAttack").get(0).coord);
            sleep(250);
            mGL.mouseClick(SCDList("cBattleSkills").get(0));
            sleep(250);
            mGL.mouseClick(SCDList("cBattleSkills").get(1));
            sleep(250);
            mGL.mouseClick(SCDList("cBattleSkills").get(2));
            sleep(250);
            mGL.mouseClick(SCDList("cBattleSkills").get(3));
        }

        sendMessage("結果出現");
        return 0;
    }

    /*
     * Handle RESULT -> BATTLE_GO
     */
    public int postBattle() throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        int timeout = 10000; //10 seconds
        int sleepInterval = 100; // 100 ms
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

        sendMessage("按再戰");
        mGL.mouseClick(SPTList("pBattleAgain").get(0).coord);
        sleep(2000);

        if (mGL.colorsAre(SPTList("pBattleFriend"))) {
            sendMessage("選一號朋友");
            mGL.mouseClick(SPTList("pBattleFriend").get(0).coord);
            sleep(1000);
        }

        if (mGL.colorsAre(SPTList("pBattleGo"))) {
            sendMessage("出現出發");
            return 0;
        }

        return -1;
    }

    /*
     * Handle Go -> BATTLE
     */
    public int preBattle() throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        int timeout = 10000; //10 seconds
        int sleepInterval = 100; // 100 ms
        int retryCount = timeout / sleepInterval;

        while (!mGL.colorsAre(SPTList("pBattleAttack")) && retryCount-- > 0) {
            mGL.mouseClick(SPTList("pBattleGo").get(0).coord);
            sleep(sleepInterval);
        }

        if (retryCount <= 0) {
            sendMessage("處理結果失敗了，你可能升級或斷線了");
            return -1;
        }

        sendMessage("進入戰鬥了");
        sleep(500);
        return 0;
    }
}
