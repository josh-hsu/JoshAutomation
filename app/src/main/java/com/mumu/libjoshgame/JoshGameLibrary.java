/*
 * Copyright (C) 2017 The Josh Tool Project
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

package com.mumu.libjoshgame;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

/*
 * Josh Game Library - Version 1.34
 */
/*
   JoshGameLibrary (GL)
   This game control library require the following initial phase

   JoshGameLibrary mGL;
   mGL = JoshGameLibrary.getInstance();               //this make sure there will be only one instance
   mGL.setContext(this);                              //this can also be setPackageManager
   mGL.setGameOrientation(ScreenPoint.SO_Landscape);  //setting game orientation for point check
   mGL.setScreenDimension(1080, 1920);                //setting the dimension of screen for point check
   mGL.setTouchShift(6)                               //setting the touch random shift size

   Note: with version 1.30 or higher, all the waiting functions are throwing InterruptExceptions
   Note: JoshGameLibrary support minimal SDK version of Android 7.0, if you are using Android 6.0 or below
         you should see Josh-Tool instead.
 */
public class JoshGameLibrary {
    private InputService mInputService;
    private CaptureService mCaptureService;
    private static Cmd mCmd;
    private static boolean mFullInitialized = false;
    private int width, height;

    private static JoshGameLibrary currentRuntime = new JoshGameLibrary();

    public static JoshGameLibrary getInstance() {
        return currentRuntime;
    }

    private JoshGameLibrary() {
        mCaptureService = new CaptureService();
        mInputService = new InputService(mCaptureService);
    }

    public void setContext(Context context) {
        if (context == null) {
            mFullInitialized = false;
        } else {
            mInputService.setContext(context);
            mCmd = new Cmd(context.getPackageManager());
            mFullInitialized = true;
        }
    }

    public void setPackageManager(PackageManager pm) {
        mCmd = new Cmd(pm);
        mFullInitialized = true;
    }

    public void setScreenDimension(int w, int h) {
        width = w;
        height = h;
        mCaptureService.setScreenDimension(w, h);
        mInputService.setScreenDimension(w, h);
    }

    public void setGameOrientation(int orientation) {
        mInputService.setGameOrientation(orientation);
        mCaptureService.setScreenOrientation(orientation);
    }

    /*
     * setScreenOffset (added in 1.34)
     * screen offset is used for various height screen, especially for
     * the same set of 1920*1080, 2160*1080, 2240*1080
     * Internal service will only treat this value as portrait orientation
     */
    public void setScreenOffset(int xOffset, int yOffset, int offsetOrientation) {
        if (offsetOrientation == ScreenPoint.SO_Landscape) {
            mInputService.setScreenOffset(yOffset, xOffset);
            mCaptureService.setScreenOffset(yOffset, xOffset);
        } else {
            mInputService.setScreenOffset(xOffset, yOffset);
            mCaptureService.setScreenOffset(xOffset, yOffset);
        }
    }

    public void setAmbiguousRange(int[] range) {
        mCaptureService.setAmbiguousRange(range);
    }

    public void setTouchShift(int ran) {
        mInputService.setTouchShift(ran);
    }

    public CaptureService getCaptureService() {
        return mCaptureService;
    }

    public InputService getInputService() {
        return mInputService;
    }

    public int getScreenWidth () {
        return width;
    }

    public int getScreenHeight() {
        return height;
    }

    static class GLService {
        /*
         * this eases the pain of accessing Cmd for GLServices
         */
        void runCommand(String cmd) {
            if (mFullInitialized) {
                mCmd.runCommand(cmd);
            } else {
                Log.d("LibGame", "Command service is not initialized");
            }
        }
    }

}
