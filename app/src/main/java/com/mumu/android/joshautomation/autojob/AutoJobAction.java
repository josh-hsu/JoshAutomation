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

import android.os.Handler;

public class AutoJobAction {
    private String mAction;
    private String mReaction;
    private Object mInputObject;
    private Object mOutputObject;
    private boolean waiting = false;

    private String mTitle;
    private String mSummary;
    private String[] mOptions; // could be [yes, no] or [opt1, opt2, opt3]

    public AutoJobAction(String action, String io, String title, String summary, String[] options) {
        waiting = false;
        mAction = action;
        mInputObject = io;
        mTitle = title;
        mSummary = summary;
        mOptions = options;
    }

    public String getAction() { return mAction; }
    public String getTitle() { return mTitle; }
    public String getSummary() { return mSummary; }
    public String[] getOptions() { return mOptions; }
    public String getReaction() {return mReaction; }

    // called by client
    public int sendActionWaited(AutoJobEventListener receiver, int what) {
        int ret = 0;

        if (receiver == null) {
            return -3; //receiver is empty
        }

        // flag the waiting
        waiting = true;

        // send out action to receiver
        receiver.onActionReceived(what, this);

        // hang here until doReaction is called by receiver

        return ret;
    }

    // called by server (receiver)
    public void handleAction(Handler handler, Runnable actionHandleRunnable) {
        // doing script request must run on UI Thread
        // we need to postpone action process to prevent the wait after action has done
        handler.postDelayed(actionHandleRunnable, 100);

        // acknowledge client to start hang waiting
        waitReaction();
    }

    // this should be only called from script, because script is not on UI Thread
    private void waitReaction() {
        try {
            while (waiting) {
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doReaction(String reaction, Object oo) {
        if (waiting) {
            waiting = false;
            mReaction = reaction;
            mOutputObject = oo;
        } else {
            // no waiting reaction, this might be an error
            mReaction = reaction;
            mOutputObject = oo;
        }
    }

    public String toString() {
        return "Action=" + mAction + ", Reaction=" + mReaction;
    }
}
