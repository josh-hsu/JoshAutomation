package com.mumu.joshautomation.script;

/**
 * JobEventListener
 * For FGO Job, starter can response anything from this listener
 */

public interface JobEventListener {
    public void onEventReceived(String msg, Object extra);
}
