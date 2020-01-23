/*
 * Copyright (C) 2020 The Josh Tool Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mumu.android.joshautomation.scripts.epic7;

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

public class Epic7Routine {
    private static final String TAG = "Epic7Routine";
    private GameLibrary20 mGL;
    private AutoJobEventListener mCallbacks;
    private DefinitionLoader.DefData mDef;

    Epic7Routine(GameLibrary20 gl, AutoJobEventListener el) {
        mGL = gl;
        mCallbacks = el;

        String resolution = mGL.getDeviceResolution()[0] + "x" + mGL.getDeviceResolution()[1];
        mDef = DefinitionLoader.getInstance().requestDefData(R.raw.epic7_definitions, "epic7_definitions.xml", resolution);

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

    private int battleCheckActivity() throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        if (mGL.waitOnColors(SPTList("pPreBattle_noActivity"), 5*1000)) {
            sendMessage("沒體了，吃葉子");
            mGL.mouseClick(SPTList("pPreBattle_noActivity").get(2).coord);
            sleep(1000);
            return 1;
        }

        return 0;
    }

    public int battleRoutine(int loop, int timeoutMs) throws InterruptedException, GameLibrary20.ScreenshotErrorException {
        int loopingBattle = loop;

        while (loopingBattle-- > 0) {
            int waitForResultCount = 100; //10 seconds
            int winOrFail = 0; //0: unknown, 1: win, 2: fail

            if (mGL.waitOnColors(SPTList("pPreBattle"), 10*1000)) {
                sendMessage("戰鬥準備畫面確認");
                mGL.mouseClick(SPTList("pPreBattle").get(0).coord); //enter battle
                mGL.mouseClick(SPTList("pPreBattle").get(0).coord); //enter battle
                mGL.mouseClick(SPTList("pPreBattle").get(0).coord); //enter battle

                //check if no activity
                if (battleCheckActivity() > 0) {
                    loopingBattle++; //restore a looping count
                    continue;
                }

                //waiting for vibration event
                mGL.waitUntilVibrate(timeoutMs);

                //waiting for victory or fail
                while(waitForResultCount-- > 0) {
                    if (mGL.colorsAre(SPTList("pPostBattle1_Victory"))) {
                        winOrFail = 1;
                        break;
                    }
                    if (mGL.colorsAre(SPTList("pPostBattle4_failed"))) {
                        winOrFail = 2;
                        break;
                    }
                    Thread.sleep(100);
                }

                if (winOrFail == 1) {
                    sendMessage("打贏了");
                    sleep(1000);
                    mGL.mouseClick(SPTList("pPostBattle1_Victory").get(0).coord); //press anywhere

                    //check if MVP shown
                    if (!mGL.waitOnColors(SPTList("pPostBattle2_MVP"), 3 * 1000)) {
                        sendMessage("MVP沒出現");
                        return -3;
                    }
                    sleep(1000);
                    mGL.mouseClick(SPTList("pPostBattle2_MVP").get(0).coord); //press next

                    //check rebattle
                    if (!mGL.waitOnColors(SPTList("pPostBattle3_reBattle"), 5 * 1000)) {
                        sendMessage("再戰按鈕未出現");
                        return -4;
                    }
                    sleep(1000);
                    mGL.mouseClick(SPTList("pPostBattle3_reBattle").get(0).coord); //press next
                } else if (winOrFail == 2) {
                    sendMessage("打輸了");
                    sleep(1000);
                    mGL.mouseClick(SPTList("pPostBattle4_failed").get(0).coord);
                } else {
                    sendMessage("沒輸沒贏WTF?");
                    sleep(1000);
                    return -2;
                }
            } else {
                sendMessage("不在戰鬥準備畫面");
                sleep(1000);
                return -1;
            }
        }

        return 0;
    }
}
