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
     * run privileged command such as dump screen or others
     * the result will be ignored
     * TODO: Redirect the pipe of output to internal path
     * @param command The command string send to device
     * @return 0 upon success
     */
    int runCommand(String command);

    /**
     * run normal shell command
     * the result we be returned
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
     * @param level
     * @param tag
     * @param log
     */
    void logDevice(int level, String tag, String log);
}
