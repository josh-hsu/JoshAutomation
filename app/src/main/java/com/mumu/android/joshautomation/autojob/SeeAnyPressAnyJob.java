package com.mumu.android.joshautomation.autojob;

import android.util.Log;

import com.mumu.android.joshautomation.content.AppPreferenceValue;
import com.mumu.android.joshautomation.content.DefinitionLoader;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenColor;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.ArrayList;

public class SeeAnyPressAnyJob extends AutoJob {
    protected String TAG;
    private MainJobRoutine mRoutine;
    private GameLibrary20 mGL;
    private DefinitionLoader.DefData mDef;
    private AutoJobEventListener mListener;

    private int mRawDefId;
    private String mDefName;
    private int mMainOrientation;
    private int mWaitTimeout;
    private boolean mInitialized = false;

    public SeeAnyPressAnyJob(String name, int rawDefId, String defName, int mainOrientation, int timeout) {
        super(name);
        TAG = name;
        mRawDefId = rawDefId;
        mDefName = defName;
        mMainOrientation = mainOrientation;
        mWaitTimeout = timeout;
    }

    @Override
    public void start() {
        super.start();
        Log.d(TAG, "start");
        initOnce();
        mGL.useHardwareSimulatedInput(false);
        mRoutine = new MainJobRoutine();
        mRoutine.start();
    }

    @Override
    public void stop() {
        super.stop();
        Log.d(TAG, "stopping job " + getJobName());

        if (mRoutine != null) {
            mRoutine.interrupt();
            mRoutine = null;
        }
    }


    @Override
    public void setExtra(Object object) {
        if (object instanceof GameLibrary20) {
            mGL = (GameLibrary20) object;
        }
    }

    private void initOnce() {
        if (!mInitialized) {
            String resolution = mGL.getDeviceResolution()[0] + "x" + mGL.getDeviceResolution()[1];
            mDef = DefinitionLoader.getInstance().requestDefData(mRawDefId, mDefName, resolution);
            mGL.setScreenMainOrientation(mMainOrientation);
            mInitialized = true;
        }
    }

    public void setJobEventListener(AutoJobEventListener el) {
        Log.d(TAG, "setJobEventListener");
        mListener = el;
    }

    private void sendMessage(String msg) {
        boolean verboseMode = AppPreferenceValue.getInstance().getPrefs().getBoolean("debugLogPref", true);

        // Send message to screen
        if (mListener != null)
            mListener.onMessageReceived(msg, this);

        // Send message to log txt file under /sdcard/ja.log
        if (verboseMode)
            Log.d(TAG, msg);
    }

    private void sleep(int time) throws InterruptedException {
        try {
            String sleepMultiplier = AppPreferenceValue.getInstance().getPrefs().getString("battleSpeed", "1.0");
            Double sleepMultiplyValue = Double.parseDouble(sleepMultiplier);
            Thread.sleep((long) (time * sleepMultiplyValue));
        } catch (NumberFormatException e) {
            Thread.sleep(time);
        }
    }

    // Definition helper functions
    private ScreenPoint SPT(String name) { if (mDef.getScreenPoint(name) == null) { sendMessage("找不到"+name); } return mDef.getScreenPoint(name);}
    private ScreenCoord SCD(String name) { if (mDef.getScreenCoord(name) == null) { sendMessage("找不到"+name); } return mDef.getScreenCoord(name);}
    private ScreenColor SCL(String name) { if (mDef.getScreenColor(name) == null) { sendMessage("找不到"+name); } return mDef.getScreenColor(name);}
    private ArrayList<ScreenPoint> SPTList(String name) {if (mDef.getScreenPoints(name) == null) { sendMessage("找不到"+name); } return mDef.getScreenPoints(name);}
    private ArrayList<ScreenCoord> SCDList(String name) {if (mDef.getScreenCoords(name) == null) { sendMessage("找不到"+name); } return mDef.getScreenCoords(name);}
    private ArrayList<ScreenColor> SCLList(String name) {if (mDef.getScreenColors(name) == null) { sendMessage("找不到"+name); } return mDef.getScreenColors(name);}

    /*
     * MainJobRoutine
     * Your script implementation should be here
     */
    private class MainJobRoutine extends Thread {

        public void seePointAndPressPoint() throws GameLibrary20.ScreenshotErrorException, InterruptedException {
            ArrayList<ArrayList<ScreenPoint>> waitList;
            int waitListEvent;
            int waitBattleEndMs = mWaitTimeout * 1000;

            // it needs to prioritize these points
            // lower index has higher priority
            waitList = new ArrayList<ArrayList<ScreenPoint>>();
            for(String sapaName : mDef.getSapaList()) {
                waitList.add(SPTList(sapaName));
            }

            waitListEvent = mGL.waitOnMatchingColorSets(waitList, waitBattleEndMs);
            if (waitListEvent >= 0) {
                mGL.mouseClick(waitList.get(waitListEvent).get(0).coord);
                sleep(1000);
            } else {
                sendMessage("等待逾時");
            }
        }

        private void main() throws Exception {
            boolean shouldRunning = true;

            while (shouldRunning) {
                // setup gl for game spec
                mGL.setScreenMainOrientation(ScreenPoint.SO_Landscape);
                mGL.useHardwareSimulatedInput(false);
                mGL.setScreenAmbiguousRange(new int[]{20,20,20});

                seePointAndPressPoint();
            }
            mListener.onJobDone(TAG);
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
