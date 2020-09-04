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

    public void battleRoutineDisordered() throws GameLibrary20.ScreenshotErrorException, InterruptedException {
        ArrayList<ArrayList<ScreenPoint>> waitList;
        int waitListEvent;
        int waitBattleEndMs = 30 * 60 * 1000;

        // it needs to prioritize these points
        // lower index has higher priority
        waitList = new ArrayList<ArrayList<ScreenPoint>>() {{
                add(SPTList("pTapScreen"));
                add(SPTList("pSkipResult"));
                add(SPTList("pAutoBattle"));
                add(SPTList("pPreBattle1"));
                add(SPTList("pPreBattle1_0"));
                add(SPTList("pPreBattle2"));
                add(SPTList("pPreBattle3"));
        }};

        waitListEvent = mGL.waitOnMatchingColorSets(waitList, waitBattleEndMs);
        if (waitListEvent >= 0) {
            mGL.mouseClick(waitList.get(waitListEvent).get(0).coord);
            sleep(1000);
        } else {
            sendMessage("等待逾時");
        }
    }
}
