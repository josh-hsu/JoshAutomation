package com.mumu.joshautomation.script;

import android.util.Log;

import com.mumu.joshautomation.records.UserRecordHandler;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

/**
 * AutoAccountTraverseJob
 * Traverse all accounts in list
 */

class AutoAccountTraverseJob extends AutoJobHandler.AutoJob {
    private static final String TAG = "AutoAccountTraverseJob";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;

    AutoAccountTraverseJob(String jobName, int jobIndex) {
        super(jobName, jobIndex);

        /* JoshGameLibrary basic initial */
        mGL = JoshGameLibrary.getInstance();
        mGL.setGameOrientation(ScreenPoint.SO_Landscape);
        mGL.setScreenDimension(1080, 1920);
    }

    @Override
    public void start() {
        super.start();
        Log.d(TAG, "starting job " + getJobName());
        mRoutine = null;
        mRoutine = new MainJobRoutine();
        mRoutine.start();
    }

    @Override
    public void stop() {
        super.stop();
        Log.d(TAG, "stopping job " + getJobName());

        mRoutine.interrupt();
    }

    @Override
    public void setExtra(Object object) {
        if (object instanceof UserRecordHandler) {
            Log.d(TAG, "Receive extra object from initiator");
        } else {
            Log.e(TAG, "Set extra for AutoAccountTraverseJob failed, wrong data type");
        }
    }

    public void setJobEventListener(AutoJobEventListener el) {
        mListener = el;
    }

    private void sendEvent(String msg, Object extra) {
        if (mListener != null) {
            mListener.onEventReceived(msg, extra);
        } else {
            Log.w(TAG, "There is no event listener registered.");
        }
    }

    private void sendMessage(String msg) {
        sendEvent(msg, null);
    }

    private void stopFGO() {
        mGL.runCommand("am force-stop com.aniplex.fategrandorder");
    }

    private void startFGO() {
        mGL.runCommand("am start \"com.aniplex.fategrandorder/jp.delightworks.Fgo.player.AndroidPlugin\"");
    }

    private class MainJobRoutine extends Thread {
        ScreenPoint pointScreenCenter = new ScreenPoint(0,0,0,0,500,1090,ScreenPoint.SO_Portrait);
        ScreenPoint pointExitBulletin = new ScreenPoint(0,0,0,0,1871,37,ScreenPoint.SO_Landscape);

        private void main() throws Exception {
            boolean shouldRunning = true;
            stopFGO();

            while (shouldRunning) {
                sleep(1000);
                startFGO();
                sleep(48000);
                mGL.getInputService().tapOnScreen(pointScreenCenter.coord);
                sleep(500);
                mGL.getInputService().tapOnScreen(pointScreenCenter.coord);
                sleep(500);
                mGL.getInputService().tapOnScreen(pointScreenCenter.coord);
                sleep(500);
                mGL.getInputService().tapOnScreen(pointScreenCenter.coord);
                sleep(10000);
                mGL.getInputService().tapOnScreen(pointScreenCenter.coord);
                sleep(7000);
                mGL.getInputService().tapOnScreen(pointExitBulletin.coord);
                sleep(2000);
                stopFGO();
                sleep(1000);

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
