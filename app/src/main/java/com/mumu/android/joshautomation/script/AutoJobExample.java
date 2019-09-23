package com.mumu.android.joshautomation.script;

import android.content.Context;
import android.util.Log;

import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.HashMap;

/**
 * AutoJobExample
 * An example workable script implementation for GL20
 */

public class AutoJobExample extends AutoJob {
    private static final String TAG = "AutoJobExample";
    private MainJobRoutine mRoutine;
    private GameLibrary20 mGL;
    private AutoJobEventListener mListener;

    public static final String jobName = "Example job"; //give your job a name

    public AutoJobExample(Context context) {
        super(jobName);

        /* JoshGameLibrary basic initial */
        mGL = initGL20(context);

        if (mGL == null) {
            Log.e(TAG, "initial GL20 failed.");
        }
    }

    private GameLibrary20 initGL20(Context context) {
        int ret;
        GameLibrary20 mGL = new GameLibrary20();

        ret = mGL.chooseDevice(GameLibrary20.DEVICE_TYPE_ANDROID_INTERNAL);
        if (ret < 0) {
            Log.e(TAG, "Device is not here?");
            return null;
        }

        ret = mGL.setDeviceEssentials(null);
        if (ret < 0) {
            Log.e(TAG, "Set device essentials failed");
            return null;
        }

        ret = mGL.initDevice(prepareInitAndroidInternal(context));
        if (ret < 0) {
            Log.e(TAG, "Initial AndroidInternal failed");
            return null;
        }

        return mGL;
    }

    private Object[] prepareInitAndroidInternal(Context context) {
        String hackSSPackageName = "com.mumu.joshautomationservice";
        String hackSSServiceName = ".CommandService";
        String hackSSIntfName = "";
        String hackSSTransactCode = "1";

        HashMap<String, String> hackSSParameters = new HashMap<>();
        hackSSParameters.put("packageName", hackSSPackageName);
        hackSSParameters.put("serviceName", hackSSServiceName);
        hackSSParameters.put("interfaceName", hackSSIntfName);
        hackSSParameters.put("code", hackSSTransactCode);

        return new Object[] {context, hackSSParameters};
    }

    /*
     * start
     * called by AutoJobHandler to start MainJobRoutine
     */
    @Override
    public void start() {
        super.start();
        Log.d(TAG, "starting job " + getJobName());
        mRoutine = null;
        mRoutine = new MainJobRoutine();
        mRoutine.start();
    }

    /*
     * stop
     * called by AutoJobHandler to stop MainJobRoutine
     */
    @Override
    public void stop() {
        super.stop();
        Log.d(TAG, "stopping job " + getJobName());

        mRoutine.interrupt();
    }

    /*
     * setExtra
     * called by caller to set any data to you
     */
    @Override
    public void setExtra(Object object) {
        // You can receive any object from your caller
    }

    /*
     * setJobEventListener
     * called by caller to receiver your message
     */
    public void setJobEventListener(AutoJobEventListener el) {
        mListener = el;
    }

    /*
     * SendEvent
     * Your can send anything back to caller whoever register listener
     */
    private void sendEvent(String msg, Object extra) {
        if (mListener != null) {
            mListener.onMessageReceived(msg, extra);
        } else {
            Log.w(TAG, "There is no event listener registered.");
        }
    }

    private void sendMessage(String msg) {
        sendEvent(msg, this);
    }

    /*
     * MainJobRoutine
     * Your script implementation should be here
     */
    private class MainJobRoutine extends Thread {
        ScreenCoord pointScreenCenter = new ScreenCoord(500, 1090, ScreenPoint.SO_Portrait);
        ScreenCoord pointScreen1 = new ScreenCoord(100, 1090, ScreenPoint.SO_Portrait);
        ScreenCoord pointScreen2 = new ScreenCoord(900, 1990, ScreenPoint.SO_Portrait);

        private void main() throws Exception {
            boolean shouldRunning = true;

            while (shouldRunning) {
                // do your job here
                sendMessage("Starting job");

                // tap a screen coordination
                sleep(5000);
                mGL.mouseClick(pointScreenCenter);
                mGL.mouseSwipe(pointScreen1, pointScreen2);

                shouldRunning = false;
                sendMessage("Job is done");
            }
        }

        public void run() {
            try {
                main();
            } catch (Exception e) {
                Log.e(TAG, "Routine caught an exception " + e.getMessage());
            }
        }
    }
}
