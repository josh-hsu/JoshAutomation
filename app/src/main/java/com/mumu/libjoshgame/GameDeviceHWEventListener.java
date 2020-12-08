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

package com.mumu.libjoshgame;

/**
 * GameDeviceHWEventListener
 *
 * The event listener from game device hardware such as vibrator, sensors or others.
 */
public interface GameDeviceHWEventListener {
    /**
     * Called when there is an event from sender
     * @param event Type of events
     * @param data Optional data from sender
     */
    void onEvent(int event, Object data);
}
