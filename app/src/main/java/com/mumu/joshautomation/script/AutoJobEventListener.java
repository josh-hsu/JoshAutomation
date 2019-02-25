package com.mumu.joshautomation.script;

/**
 * AutoJobEventListener
 * For FGO Job, starter can response anything from this listener
 */

public interface AutoJobEventListener {
    void onEventReceived(String msg, Object extra);
    void onInteractFromScript(int what, AutoJobAction action);
    void onJobDone(String jobName);
}
