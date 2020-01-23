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

package com.mumu.android.joshautomation.autojob;

/**
 * AutoJob
 * A object interface for AutoJobHandler to start or stop a task
 */
public class AutoJob {
    private String mJobName;
    public boolean mShouldJobRunning;

    /*
     * AutoJob
     * constructor of AutoJob
     * Assign it a name and an index for easy access for your job
     */
    public AutoJob(String name) {
        mJobName = name;
        mShouldJobRunning = false;
    }

    public String getJobName() {
        return mJobName;
    }

    public boolean isShouldJobRunning() {
        return mShouldJobRunning;
    }

    /*
     * start()
     *
     * start point of your task
     * Override this function and your should call super.start() at first
     */
    public void start() {
        mShouldJobRunning = true;
    }

    /*
     * stop()
     *
     * stop the current running task
     * Override this function and your should call super.stop() at first
     */
    public void stop() {
        mShouldJobRunning = false;
    }

    /*
     * setExtra(Object)
     *
     * your starter(which may hold AutoJobHandler) may need to send data
     * to your job for correctly functioning
     * Override this function to deal with data you want
     */
    public void setExtra(Object object) {

    }

    /*
     * setJobEventListener(AutoJobEventListener)
     *
     * You can send message or send data back to the starter
     * if they have register an AutoJobEventListener to you
     */
    public void setJobEventListener(AutoJobEventListener el) {

    }

    /*
     * onAutoCorrection(Object object)
     *
     * When user hits button to do auto correction
     * this function will be called
     * Auto Correction is usually used in following scenarios:
     * 1. Screen size correction, to auto detect the offset of height or width
     * 2. User interacts with script, the argument object will throw in this function
     *    including tap location and more.
     */
    public void onAutoCorrection(Object object) {

    }
}
