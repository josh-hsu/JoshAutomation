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

package com.mumu.android.joshautomation.content;

import com.mumu.libjoshgame.GameLibrary20;

/**
 * App Shared Object
 * This class holds the objects initialized by HeadService and available for
 * all classes in this domain
 */
public class AppSharedObject {
    private static AppSharedObject mSelf;
    private GameLibrary20 mGL;

    private AppSharedObject() {

    }

    public static AppSharedObject getInstance() {
        if (mSelf == null)
            mSelf = new AppSharedObject();
        return mSelf;
    }

    public GameLibrary20 getGL20() {
        return mGL;
    }

    public void setGL20(GameLibrary20 gl20) {
        mGL = gl20;
    }
}
