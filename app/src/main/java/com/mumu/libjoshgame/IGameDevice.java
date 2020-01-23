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

package com.mumu.libjoshgame;

public interface IGameDevice {

    /**
     * query preloaded screenshot paths for further screen dump
     * @return String array of screenshot paths supported
     */
    String[] queryPreloadedPaths();

    /**
     * query the preloaded screenshot paths count
     * @return The path count
     */
    int queryPreloadedPathCount();

    /**
     * dump screen for the path
     * @param path The path should be in the list of preloaded paths
     * @return 0 upon success
     */
    int dumpScreen(String path);

    /**
     * dump screen for the path and convert to png format
     * @param path The path should be in the list of preloaded paths
     * @return 0 upon success
     */
    int dumpScreenPng(String path);

    /**
     * deal with the mouse or touch screen events
     * @param x1 source x-axis coordination
     * @param y1 source y-axis coordination
     * @param x2 destination x-axis coordination (may not need)
     * @param y2 destination y-axis coordination (may not need)
     * @param event The event type
     * @return 0 upon success
     */
    int mouseEvent(int x1, int y1, int x2, int y2, int event);

    /**
     * run privileged command such as dump screen or others
     * the result will be ignored
     * TODO: Redirect the pipe of output to internal path
     * @param command The command string send to device
     * @return 0 upon success
     */
    int runCommand(String command);

    /**
     * run normal shell command
     * the result will be returned
     * @param command The command string send to device
     * @return The result of the command
     */
    String runShellCommand(String command);

    /**
     * get the version description of this device
     * @return The version string
     */
    String getVersion();

    /**
     * get the system type of this device
     * @return The system type of the device, can be one of Windows, Linux or Darwin.
     */
    int getSystemType();

    /**
     * get the transaction time in milliseconds after every command
     * such as runCommand and dumpScreen
     * @return The transaction time in milliseconds
     */
    int getWaitTransactionTimeMs();

    /**
     * set the transaction time in milliseconds after every command
     * such as runCommand and dumpScreen
     */
    void setWaitTransactionTimeMsOverride(int ms);

    /**
     * use hardware simulated way to send input command
     * it is used to prevent our tool been detected by games or apps
     * @param enable True if we want to use hardware simulation otherwise False can be set
     * @return 0 if both supported and switched to selected mode, -9 if not supported, otherwise
     *         -1 will be returned.
     */
    int setHWSimulatedInput(boolean enable);

    /**
     * when the device has been activated, this method should be called
     * @return 0 upon success
     */
    int onStart();

    /**
     * when the device has been disabled, this method should be called
     * @return 0 upon success
     */
    int onExit();

    /**
     * log information into device
     * @param level Log level defined in {@link GameLibrary20}
     * @param tag Log tag label String
     * @param log Log text String
     */
    void logDevice(int level, String tag, String log);

    /**
     * register vibrator event such as on, off
     * @return 0 upon success or
     */
    int registerEvent(int type, GameDeviceHWEventListener el);

    /**
     * deregister vibrator event use the same listener object
     */
    int deregisterEvent(int type, GameDeviceHWEventListener el);
}
