package com.mumu.joshautomation.script;

import android.util.Log;

import com.mumu.joshautomation.fgo.AutoBattleJob;
import com.mumu.joshautomation.fgo.PureBattleJob;

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
    public static final int EXAMPLE_JOB = 0;
    public static final int FGO_BATTLE_JOB = 1;
    public static final int FGO_PURE_BATTLE_JOB = 2;
    public static final int TOTAL_JOB = 3;

    private AutoJob[] mJobList;

    private AutoJobHandler() {
        mJobList = new AutoJob[TOTAL_JOB];
        mJobList[EXAMPLE_JOB] = new AutoJobExample("example_job", EXAMPLE_JOB);
        mJobList[FGO_BATTLE_JOB] = new AutoBattleJob("fgo_battle_job", FGO_BATTLE_JOB);
        mJobList[FGO_PURE_BATTLE_JOB] = new PureBattleJob("fgo_pure_battle_job", FGO_PURE_BATTLE_JOB);
    }

    public static AutoJobHandler getHandler() {
        if (mHandler == null)
            mHandler = new AutoJobHandler();

        return mHandler;
    }

    public void startJob(int idx) {
        if (idx >= TOTAL_JOB) {
            Log.d(TAG, "Fail to start job " + idx + ", no such index.");
        } else {
            mJobList[idx].start();
        }
    }

    public void stopJob(int idx) {
        if (idx >= TOTAL_JOB) {
            Log.d(TAG, "Fail to stop job " + idx + ", no such index.");
        } else {
            mJobList[idx].stop();
        }
    }

    public void setExtra(int idx, Object object) {
        if (idx >= TOTAL_JOB) {
            Log.d(TAG, "Setting extra data for job " + idx + " failed, no such index.");
        } else {
            mJobList[idx].setExtra(object);
        }
    }

    public void setJobEventListener(int idx, AutoJobEventListener el) {
        if (idx >= TOTAL_JOB) {
            Log.d(TAG, "Setting AutoJobEventListener for job " + idx + " failed, no such index.");
        } else {
            mJobList[idx].setJobEventListener(el);
        }
    }

    public String getJobName(int idx) {
        if (idx >= TOTAL_JOB) {
            Log.d(TAG, "Fail to start job " + idx + ", no such index.");
            return null;
        } else {
            return mJobList[idx].getJobName();
        }
    }
}
