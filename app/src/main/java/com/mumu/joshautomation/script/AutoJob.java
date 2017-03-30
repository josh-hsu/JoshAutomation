package com.mumu.joshautomation.script;

/**
 * AutoJob
 * A object interface for AutoJobHandler to start or stop a task
 */
public class AutoJob {
    private String mJobName;
    private int mJobIndex;
    public boolean mShouldJobRunning;

    /*
     * AutoJob
     * constructor of AutoJob
     * Assign it a name and an index for easy access for your job
     */
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
}
