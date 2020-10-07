package com.mumu.android.joshautomation.autojob;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mumu.android.joshautomation.content.AppPreferenceValue;
import com.mumu.android.joshautomation.content.DefinitionLoader;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenPoint;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * SelectableSAPAJob
 * This job use Preference Page to select an legal XML file
 */
public class SelectableSAPAJob extends AutoJob {
    protected static final String TAG = "XMLSeeAnyPressAny";
    private MainJobRoutine mRoutine;
    private GameLibrary20 mGL;
    private DefinitionLoader.DefData mDef;
    private AutoJobEventListener mListener;
    private Context mContext;

    private int mMainOrientation = ScreenPoint.SO_Landscape;
    private int mWaitTimeout = 60;

    public SelectableSAPAJob() {
        super(TAG);
    }

    @Override
    public void start() {
        super.start();
        Log.d(TAG, "started job " + getJobName());
        if (init() < 0) {
            Log.e(TAG, "init fail, stop it");
            super.stop();
        }
        mGL.useHardwareSimulatedInput(false);
        mGL.setScreenMainOrientation(mMainOrientation);
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
        } else if (object instanceof Context) {
            mContext = (Context) object;
        }
    }

    private int init() {
        String resolution = mGL.getDeviceResolution()[0] + "x" + mGL.getDeviceResolution()[1];
        String rawFileUri = AppPreferenceValue.getInstance().getPrefs().getString("selectSAPAJobScript", "");
        Uri fileUri = Uri.parse(rawFileUri);
        try {
            InputStream fileInputStream = mContext.getContentResolver().openInputStream(fileUri);
            mDef = DefinitionLoader.getInstance().requestDefData(fileInputStream, resolution);
            mMainOrientation = mDef.getOrientation();
            mWaitTimeout = mDef.getSapaTimeout();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + fileUri.getPath());
            return -1;
        }
        return 0;
    }

    public void setJobEventListener(AutoJobEventListener el) {
        Log.d(TAG, "setJobEventListener " + getJobName());
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

    private void sleepMs(int time) throws InterruptedException {
        try {
            String sleepMultiplier = AppPreferenceValue.getInstance().getPrefs().getString("battleSpeed", "1.0");
            Double sleepMultiplyValue = Double.parseDouble(sleepMultiplier);
            Thread.sleep((long) (time * sleepMultiplyValue));
        } catch (NumberFormatException e) {
            Thread.sleep(time);
        }
    }

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
                waitList.add(mDef.getScreenPoints(sapaName));
            }

            waitListEvent = mGL.waitOnMatchingColorSets(waitList, waitBattleEndMs);
            if (waitListEvent >= 0) {
                mGL.mouseClick(waitList.get(waitListEvent).get(0).coord);
                sleepMs(1000);
            } else {
                sendMessage("等待逾時");
            }
        }

        private void main() throws Exception {
            while (isShouldJobRunning()) {
                // setup gl for game spec
                mGL.setScreenMainOrientation(mMainOrientation);
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
                Log.e(TAG, "Script " + getJobName() + " routine caught an exception " + e.getMessage());
            }
        }
    }
}
