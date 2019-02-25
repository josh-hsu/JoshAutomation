package com.mumu.joshautomation.script;

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

    // this should be only called from script, because script is not in UI Thread
    public void waitReaction() {
        waiting = true;
        try {
            while (waiting) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doReaction(String reaction, Object oo) {
        waiting = false;
        mReaction = reaction;
        mOutputObject = oo;
    }

    public String toString() {
        return "Action=" + mAction + ", Reaction=" + mReaction;
    }
}
