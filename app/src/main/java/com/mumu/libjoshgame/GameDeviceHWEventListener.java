package com.mumu.libjoshgame;

/**
 * GameDeviceHWEventListener
 *
 * The event listener from game device hardware such as vibrator, sensors or others.
 */
public interface GameDeviceHWEventListener {
    /**
     * Called when there is an event from sender
     * @param event Type of events
     * @param data Optional data from sender
     */
    void onEvent(int event, Object data);
}
