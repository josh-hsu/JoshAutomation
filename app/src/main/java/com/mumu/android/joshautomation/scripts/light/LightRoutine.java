package com.mumu.android.joshautomation.scripts.light;

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

public class LightRoutine {
    private static final String TAG = "LightRoutine";
    private GameLibrary20 mGL;
    private AutoJobEventListener mCallbacks;
    private DefinitionLoader.DefData mDef;

    LightRoutine(GameLibrary20 gl, AutoJobEventListener el) {
        mGL = gl;
        mCallbacks = el;

        String resolution = mGL.getDeviceResolution()[0] + "x" + mGL.getDeviceResolution()[1];
        mDef = DefinitionLoader.getInstance().requestDefData(R.raw.light_definitions, "light_definitions.xml", resolution);

        if (mGL != null) {
            mGL.setScreenMainOrientation(ScreenPoint.SO_Landscape);
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

    public boolean battleRoutine() throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        // all battle
        int count = 0;
        int waitTimeoutMs = 30 * 1000;
        int waitBattleEndMs = 30 * 60 * 1000;
        ArrayList<ScreenPoint>[] waitList;
        int waitListEvent = -1;

        waitList = new ArrayList[]{SPTList("pPreBattle1"), SPTList("pPreBattle1_0")};
        waitListEvent = mGL.waitOnOneOfColors(waitList, waitTimeoutMs);
        if(waitListEvent < 0) {
            sendMessage("找不到按鈕1");
            return false;
        }
        sendMessage("找到按鈕1: " + waitListEvent);
        mGL.mouseClick(SPTList("pPreBattle1").get(0).coord);
        sleep(1600);

        if (waitListEvent == 0) {
            if (!mGL.waitOnColors(SPTList("pPreBattle2"), waitTimeoutMs)) {
                sendMessage("找不到按鈕2");
                return false;
            }
            sendMessage("找到按鈕2");
            mGL.mouseClick(SPTList("pPreBattle2").get(0).coord);
            sleep(1600);
        }

        if(!mGL.waitOnColors(SPTList("pPreBattle3"), waitTimeoutMs)) {
            sendMessage("找不到按鈕3");
            return false;
        }
        sendMessage("找到按鈕3");
        mGL.mouseClick(SPTList("pPreBattle3").get(0).coord);
        sleep(2500);

        // start battle
        sendMessage("戰鬥進入");

        if(!mGL.waitOnColors(SPTList("pAutoBattle"), waitTimeoutMs)) {
            sendMessage("找不到自動戰鬥");
            return false;
        }
        sendMessage("找到按鈕AUTO");
        mGL.mouseClick(SPTList("pAutoBattle").get(0).coord);

        sleep(5*1000);

        waitList = new ArrayList[]{SPTList("pTapScreen"), SPTList("pPreBattle1")};
        int eventHappened = mGL.waitOnOneOfColors(waitList, waitBattleEndMs);
        switch (eventHappened) {
            case 0:
                sendMessage("找到按鈕tap");
                mGL.mouseClick(SPTList("pTapScreen").get(0).coord);

                if(!mGL.waitOnColors(SPTList("pSkipResult"), waitTimeoutMs)) {
                    sendMessage("找不到result");
                    return false;
                }
                sendMessage("找到按鈕skip");
                mGL.mouseClick(SPTList("pSkipResult").get(0).coord);
                sleep(2000);
                break;
            case 1:
                sendMessage("回到頭了");
                break;
            default:
                sendMessage("狀態不明");
                return false;
        }

        return true;
    }

    public boolean battleRoutineDisordered() throws GameLibrary20.ScreenshotErrorException, InterruptedException {
        ArrayList<ScreenPoint>[] waitList;
        int waitListEvent = -1;
        int waitBattleEndMs = 30 * 60 * 1000;

        // it needs to prioritize these points
        waitList = new ArrayList[] {
                SPTList("pTapScreen"),
                SPTList("pSkipResult"),
                SPTList("pAutoBattle"),
                SPTList("pPreBattle1"),
                SPTList("pPreBattle1_0"),
                SPTList("pPreBattle2"),
                SPTList("pPreBattle3"),
        };

        waitListEvent = mGL.waitOnOneOfColors(waitList, waitBattleEndMs);
        if (waitListEvent >= 0) {
            mGL.mouseClick(waitList[waitListEvent].get(0).coord);
            sleep(1000);
        } else {
            sendMessage("等待逾時");
            return false;
        }

        return true;
    }
}
