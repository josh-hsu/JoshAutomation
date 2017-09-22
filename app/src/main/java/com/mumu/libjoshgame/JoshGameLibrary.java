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
 * Josh Game Library - Version 1.3.1
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

   Note: with version 1.3 or higher, all the waiting functions are throwing InterruptExceptions
 */
public class JoshGameLibrary {
    private InputService mInputService;
    private CaptureService mCaptureService;
    private static Cmd mCmd;
    private static boolean mFullInitialized = false;

    private static JoshGameLibrary currentRuntime = new JoshGameLibrary();

    public static JoshGameLibrary getInstance() {
        return currentRuntime;
    }

    private JoshGameLibrary() {
        mCaptureService = new CaptureService();
        mInputService = new InputService(mCaptureService);
    }

    public void setContext(Context context) {
        mCmd = new Cmd(context.getPackageManager());
        mFullInitialized = true;
    }

    public void setPackageManager(PackageManager pm) {
        mCmd = new Cmd(pm);
        mFullInitialized = true;
    }

    public void setScreenDimension(int w, int h) {
        mCaptureService.setScreenDimension(w, h);
        mInputService.setScreenDimension(w, h);
    }

    public void setGameOrientation(int orientation) {
        mInputService.setGameOrientation(orientation);
        mCaptureService.setScreenOrientation(orientation);
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

    public void runCommand(String cmd) {
        if (mFullInitialized) {
            mCmd.runCommand(cmd);
        } else {
            Log.d("LibGame", "Command service is not initialized");
        }
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
