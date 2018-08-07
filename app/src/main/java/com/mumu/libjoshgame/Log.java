package com.mumu.libjoshgame;

import android.os.Environment;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Log saves logs to local buffer file and also outputs to logcat
 */

public final class Log {
    private static final int DEBUG = 3;
    private static final int ERROR = 6;
    private static final int INFO = 4;
    private static final int VERBOSE = 2;
    private static final int WARN = 5;

    public static void v(String tag, String msg) {
        android.util.Log.v(tag, msg);
        saveLogToFile(tag, msg, VERBOSE);
    }

    public static void d(String tag, String msg) {
        android.util.Log.d(tag, msg);
        saveLogToFile(tag, msg, DEBUG);
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(tag, msg);
        saveLogToFile(tag, msg, ERROR);
    }

    public static void i(String tag, String msg) {
        android.util.Log.i(tag, msg);
        saveLogToFile(tag, msg, INFO);
    }

    public static void w(String tag, String msg) {
        android.util.Log.w(tag, msg);
        saveLogToFile(tag, msg, WARN);
    }

    private static void saveLogToFile(String tag, String msg, int level) {
        String log = logFormatted(tag, msg, level);
        try {
            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/ja.log", true);
            fos.write(log.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String logFormatted(String tag, String msg, int level) {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd a hh:mm:ss", Locale.getDefault());
        String thisTime = df.format(Calendar.getInstance().getTime());
        return String.format("%18s: <%s> %s: %s\n", thisTime, getLevelString(level), tag, msg);
    }

    private static String getLevelString(int level) {
        String ret = "";

        switch (level) {
            case DEBUG:
                ret = "D";
                break;
            case WARN:
                ret = "W";
                break;
            case INFO:
                ret = "I";
                break;
            case ERROR:
                ret = "E";
                break;
            case VERBOSE:
                ret = "V";
                break;
        }
        return ret;
    }

}

