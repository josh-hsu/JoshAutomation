/*
 * Copyright (C) 2016 The Josh Tool Project
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

import android.content.pm.PackageManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Cmd {
    private boolean mInitialized = false;
    private PackageManager mPM;
    private Method mRunCmdMethod;

    Cmd(PackageManager pm) {
        try {
            Class<?>[] run_types = new Class[]{String.class, String.class};
            mRunCmdMethod = pm.getClass().getMethod("joshCmd", run_types);
            mPM = pm;
            mInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String runCommand(String cmd) {
        try {
            runCmd(cmd);
        } catch (Exception e)  {
            e.printStackTrace();
        }
        return null;
    }

    private void runCmd(String cmd) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (mInitialized) {
            mRunCmdMethod.invoke(mPM, cmd, "");
        }
    }
}
