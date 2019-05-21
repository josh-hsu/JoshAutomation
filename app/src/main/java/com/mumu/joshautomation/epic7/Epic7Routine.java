package com.mumu.joshautomation.epic7;

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

public class Epic7Routine {
    private static final String TAG = "Epic7Routine";
    private JoshGameLibrary mGL;
    private AutoJobEventListener mCallbacks;
    private DefinitionLoader.DefData mDef;

    private static final int sPreBattleStage1 = 0;
    private static final int sInBattleStage1  = 1;
    private static final int sPreBattleStage2 = 2;
    private static final int sInBattleStage2  = 3;
    private static final int sPreBattleStage3 = 4;
    private static final int sInBattleStage3  = 5;
    private static final int sBattleResult    = 6;
    private static final int sBattleUnknown   = -1;

    Epic7Routine(JoshGameLibrary gl, AutoJobEventListener el) {
        mGL = gl;
        mCallbacks = el;

        String resolution = mGL.getScreenWidth() + "x" + mGL.getScreenHeight();
        if (mGL.getScreenWidth() == 1080)
            mDef = DefinitionLoader.getInstance().requestDefData(R.raw.epic7_definitions, "epic7_definitions.xml", "1080x2340");
        else
            mDef = DefinitionLoader.getInstance().requestDefData(R.raw.epic7_definitions, "epic7_definitions.xml", resolution);
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

    public int getBattleStage() throws InterruptedException {
        if (mGL.getCaptureService().colorsAre(SPTList("pointInBattleToken"))) {
            if (mGL.getCaptureService().colorIs(SPT("pointInBattleStage1")))
                return sInBattleStage1;
            else if (mGL.getCaptureService().colorIs(SPT("pointInBattleStage2")))
                return sInBattleStage2;
            else if (mGL.getCaptureService().colorIs(SPT("pointInBattleStage3")))
                return sInBattleStage3;
            return sInBattleStage1;
        } else if (mGL.getCaptureService().colorsAre(SPTList("pointPreBattleToken"))) {
            return sPreBattleStage1;
        } else if (mGL.getCaptureService().colorsAre(SPTList("pointBattleClearToken"))) {
            return sBattleResult;
        }

        return sBattleUnknown;
    }

    public int battleRoutine(int loop) throws InterruptedException {
        int loopingCounter = 100; // 10 second for preBattle status
        int loopingBattle = loop;
        boolean hasAutoBattle = false;

        while (loopingBattle-- > 0) {
            switch (getBattleStage()) {
                case sInBattleStage1:
                case sInBattleStage2:
                case sInBattleStage3:
                    sendMessage("戰鬥內");
                    if (!hasAutoBattle) {
                        hasAutoBattle = true;
                        mGL.getInputService().tapOnScreen(SCD("pointAutoBattleButton"));
                    }
                    sleep(100);
                    break;
                case sPreBattleStage1: //out of stage keep tap on forward
                    sendMessage("戰鬥外");
                    if (hasAutoBattle) {
                        hasAutoBattle = false;
                        mGL.getInputService().tapOnScreen(SCD("pointAutoBattleButton"));
                    }

                    while (getBattleStage() == sPreBattleStage1 && loopingCounter-- > 0) {
                        mGL.getInputService().tapOnScreen(SCD("pointForwardStage"));
                        sleep(100);
                    }

                    if (loopingCounter <= 0) {
                        sendMessage("花太多時間在戰鬥外");
                        return -1;
                    }
                    break;
                case sBattleResult:
                    sendMessage("戰鬥結束");
                    break;
                case sBattleUnknown:
                default:
                    sendMessage("例外發生，等他一下");
                    sleep(2000);
            }
        }

        return 0;
    }
}
