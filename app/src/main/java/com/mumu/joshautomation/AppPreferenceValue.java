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

package com.mumu.joshautomation;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferenceValue {
    private static AppPreferenceValue mAppPreferenceValue = new AppPreferenceValue();
    private static boolean mInitialized = false;
    public static final int PREF_APP = 0;
    public static final int PREF_FGO = 1;

    private Context mContext;
    private SharedPreferences mAppPrefs;
    private SharedPreferences mFgoPrefs;

    private AppPreferenceValue() {

    }

    public static AppPreferenceValue getInstance() {
        return mAppPreferenceValue;
    }

    public void init(Context ctx) {
        if (ctx != null) {
            mContext = ctx;
            mAppPrefs = mContext.getSharedPreferences("com.mumu.joshautomation_app_preferences", Context.MODE_PRIVATE);
            mFgoPrefs = mContext.getSharedPreferences("com.mumu.joshautomation_preferences", Context.MODE_PRIVATE);
            mInitialized = true;
        }
    }

    public SharedPreferences getPrefs(int pref_type) {
        switch (pref_type) {
            case PREF_APP:
                return mAppPrefs;
            case PREF_FGO:
                return mFgoPrefs;
            default:
                break;
        }

        return null;
    }
}
