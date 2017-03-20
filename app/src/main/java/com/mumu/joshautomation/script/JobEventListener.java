package com.mumu.joshautomation.script;

/**
 * JobEventListener
 * For FGO Job, starter can response anything from this listener
 */

public interface JobEventListener {
    void onEventReceived(String msg, Object extra);
}
