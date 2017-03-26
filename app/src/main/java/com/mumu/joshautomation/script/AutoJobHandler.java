package com.mumu.joshautomation.script;

import android.util.Log;

import com.mumu.joshautomation.fgo.AutoBattleJob;
import com.mumu.joshautomation.fgo.PureBattleJob;

/**
 * AutoJobHandler
 * Start or Stop any Job of FGO
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
        mJobList[0] = new AutoJobExample("example_job", EXAMPLE_JOB);
        mJobList[1] = new AutoBattleJob("fgo_battle_job", FGO_BATTLE_JOB);
        mJobList[2] = new PureBattleJob("fgo_pure_battle_job", FGO_PURE_BATTLE_JOB);
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

    static public class AutoJob {
        private String mJobName;
        private int mJobIndex;
        public boolean mShouldJobRunning;

        public AutoJob(String name, int idx) {
            mJobIndex = idx;
            mJobName = name;
            mShouldJobRunning = false;
        }

        public String getJobName() {
            return mJobName;
        }

        public int getJobIndex() {
            return mJobIndex;
        }

        public boolean isShouldJobRunning() {
            return mShouldJobRunning;
        }

        public void start() {
            mShouldJobRunning = true;
            Log.d(TAG, "calling super start");
        }

        public void stop() {
            mShouldJobRunning = false;
            Log.d(TAG, "calling super stop");
        }

        public void setExtra(Object object) {
            Log.d(TAG, "calling super setExtra");
        }

        public void setJobEventListener(AutoJobEventListener el) {
            Log.d(TAG, "calling super setJobEventListener");
        }
    }
}
