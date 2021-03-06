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

import android.util.Log;

import java.util.ArrayList;

/**
 * AutoJobHandler (see also AutoJob)
 * Controller of jobs
 *
 * To add your job (you can copy an example from AutoJobExample.java)
 * 1. Make your job class extends AutoJob
 * 2. Override these three method: start(), stop(), setExtra()
 * 3. Initial your job by give it an index, such as
 *    public static final int YOUR_JOB = 3;
 *    and you will need to increase TOTAL_JOB by 1
 * 4. add mJobList[YOUR_JOB] = new YourJob("your_great_job", YOUR_JOB);
 *
 * To get AutoJobHandler
 * You can simply use AutoJobHandler h = AutoJobHandler.getHandler() to get a handler instance
 * And use h.start(job_index) to start any job you want
 */

public class AutoJobHandler {
    public static final String TAG = "AutoJobHandler";
    private static AutoJobHandler mHandler;

    private ArrayList<AutoJob> mJobList;

    private AutoJobHandler() {
        mJobList = new ArrayList<>();
    }

    public static AutoJobHandler getHandler() {
        if (mHandler == null)
            mHandler = new AutoJobHandler();

        return mHandler;
    }

    public void addJob(AutoJob job) {
        mJobList.add(job);
    }

    public int getJobCount() {
        return mJobList.size();
    }

    public AutoJob getJob(int idx) {
        if (idx >= getJobCount()) {
            return null;
        } else {
            return mJobList.get(idx);
        }
    }

    public AutoJob getJob(String name) {
        for(AutoJob a: mJobList) {
            if (a.getJobName().equals(name))
                return a;
        }

        return null;
    }

    public void startJob(int idx) {
        if (idx >= getJobCount()) {
            Log.d(TAG, "Fail to start job " + idx + ", no such index.");
        } else {
            getJob(idx).start();
        }
    }

    public void startJob(String name) {
        AutoJob a = getJob(name);
        if (a != null)
            a.start();
        else
            Log.d(TAG, "Fail to start job " + name + ", no such job.");
    }

    public void stopJob(int idx) {
        if (idx >= getJobCount()) {
            Log.d(TAG, "Fail to stop job " + idx + ", no such index.");
        } else {
            getJob(idx).stop();
        }
    }

    public void stopJob(String name) {
        AutoJob a = getJob(name);
        if (a != null)
            a.stop();
        else
            Log.d(TAG, "Fail to stop job " + name + ", no such job.");
    }

    public void setExtra(int idx, Object object) {
        if (idx >= getJobCount()) {
            Log.d(TAG, "Setting extra data for job " + idx + " failed, no such index.");
        } else {
            getJob(idx).setExtra(object);
        }
    }

    public void setExtra(String job, Object obj) {
        AutoJob a = getJob(job);
        if (a != null)
            a.setExtra(obj);
        else
            Log.d(TAG, "Setting extra data for job " + job + " failed, no such job.");
    }

    public void setJobEventListener(int idx, AutoJobEventListener el) {
        if (idx >= getJobCount()) {
            Log.d(TAG, "Setting AutoJobEventListener for job " + idx + " failed, no such index.");
        } else {
            getJob(idx).setJobEventListener(el);
        }
    }

    public void setJobEventListener(String name, AutoJobEventListener el) {
        AutoJob a = getJob(name);
        if (a != null) {
            a.setJobEventListener(el);
        } else {
            Log.d(TAG, "Setting AutoJobEventListener for job " + name + " failed, no such index.");
        }
    }

    public String getJobName(int idx) {
        if (idx >= getJobCount()) {
            Log.d(TAG, "Fail to start job " + idx + ", no such index.");
            return null;
        } else {
            return getJob(idx).getJobName();
        }
    }

    public int getJobIndex(String name) {
        AutoJob a = getJob(name);
        if (a != null)
            return mJobList.indexOf(a);
        else
            return -1;
    }

    public void requestAutoCorrection(int idx, Object obj) {
        AutoJob job = getJob(idx);
        if (job != null) {
            job.onAutoCorrection(obj);
        } else {
            Log.w(TAG, "request auto correction failed, no such job in index " + idx);
        }
    }
}
