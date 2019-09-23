package com.mumu.android.joshautomation.script;

/**
 * AutoJobEventListener
 * For FGO Job, starter can response anything from this listener
 */

public interface AutoJobEventListener {
    void onMessageReceived(String msg, Object extra);
    void onActionReceived(int what, AutoJobAction action);
    void onJobDone(String jobName);
}
