package com.mumu.libjoshgame.device;

import com.mumu.libjoshgame.GameDevice;
import com.mumu.libjoshgame.GameDeviceHWEventListener;
import com.mumu.libjoshgame.IGameDevice;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NoxPlayer extends GameDevice implements IGameDevice {
    private static final String TAG = JoshGameLibrary.TAG;
    private static final String DEVICE_NAME = "NoxPlayer";
    private static final String DEVICE_VERSION = "1.0";
    private static final int    DEVICE_SYS_TYPE = DEVICE_SYS_LINUX;

    private String[] mPreloadedPath;
    private int mPreloadedPathCount;

    @Override
    public int init(Object[] objects) {
        mPreloadedPath = new String[] {
                "Unused",
        };
        mPreloadedPathCount = mPreloadedPath.length;
        return super.init(DEVICE_NAME, this);
    }

    @Override
    public int[] getScreenDimension() {
        return new int[] {1080, 1920};
    }

    @Override
    public int getScreenMainOrientation() {
        return ScreenPoint.SO_Portrait;
    }

    @Override
    public boolean getInitialized() {
        return mInitialized;
    }

    /*
     * Implement of IGameDevice
     */
    @Override
    public String[] queryPreloadedPaths() {
        return mPreloadedPath;
    }

    @Override
    public int queryPreloadedPathCount() {
        return mPreloadedPathCount;
    }

    @Override
    public String getVersion() {
        return DEVICE_VERSION;
    }

    @Override
    public int getSystemType() {
        return DEVICE_SYS_TYPE;
    }

    @Override
    public void setWaitTransactionTimeMsOverride(int ms) {

    }

    @Override
    public int getWaitTransactionTimeMs() {
        return 0;
    }

    @Override
    public int dumpScreen(String path) {
        return 0;
    }

    @Override
    public int mouseEvent(int x1, int y1, int x2, int y2, int event) {
        return 0;
    }

    @Override
    public int runCommand(String cmd) {
        return 0;
    }

    @Override
    public String runShellCommand(String shellCmd) {
        String[] cmd = {"/system/bin/sh", "-c", shellCmd};
        StringBuilder sb = new StringBuilder();

        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String line;
            // append newline at each readLine
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            // delete the last newline for consistent
            if (sb.length() > 1)
                sb.deleteCharAt(sb.length() - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    @Override
    public int onStart() {
        return 0;
    }

    @Override
    public int onExit() {
        return 0;
    }

    @Override
    public void logDevice(int level, String tag, String msg) {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd a hh:mm:ss", Locale.getDefault());
        String thisTime = df.format(Calendar.getInstance().getTime());
        String thisLevel = "U";

        switch(level) {
            case LOG_VERBOSE:
                thisLevel = "V";
                break;
            case LOG_DEBUG:
                thisLevel = "D";
                break;
            case LOG_WARNING:
                thisLevel = "W";
                break;
            case LOG_ERROR:
                thisLevel = "E";
                break;
            case LOG_FATAL:
                thisLevel = "F";
                break;
            default:
                break;
        }

        System.out.println(String.format("%18s: <%s> %s: %s", thisTime, thisLevel, tag, msg));
    }

    @Override
    public int registerEvent(int type, GameDeviceHWEventListener el) {
        return 0;
    }

    @Override
    public int deregisterEvent(int type, GameDeviceHWEventListener el) {
        return 0;
    }
}
