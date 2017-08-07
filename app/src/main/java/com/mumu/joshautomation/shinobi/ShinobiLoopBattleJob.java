package com.mumu.joshautomation.shinobi;

import android.app.Service;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.mumu.joshautomation.AppPreferenceValue;
import com.mumu.joshautomation.script.AutoJob;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

public class ShinobiLoopBattleJob extends AutoJob {
    private static final String TAG = "ShinobiLoopJob";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;

    private ShinobiLoopBattleJob mSelf;
    private ShinobiRoutine mSR;
    private Service mRootService;
    private boolean mWaitSkip = false;

    public static final String jobName = "Shinobi Loop Battle Job";

    public ShinobiLoopBattleJob() {
        super(jobName);

        /* JoshGameLibrary basic initial */
        mGL = JoshGameLibrary.getInstance();
        mGL.setGameOrientation(ScreenPoint.SO_Landscape);
        mGL.setScreenDimension(1080, 1920);
        mGL.setTouchShift(6);

        mSR = new ShinobiRoutine(mGL, mListener); //listener might be null before assigning
        mSelf = this;
    }

    @Override
    public void start() {
        super.start();
        Log.d(TAG, "starting job " + getJobName());

        refreshSetting();
        mWaitSkip = AppPreferenceValue.getInstance().getPrefs().getBoolean("battleWaitSkip", false);
        mRoutine = null;
        mRoutine = new MainJobRoutine();
        mRoutine.start();
    }

    @Override
    public void stop() {
        super.stop();
        Log.d(TAG, "stopping job " + getJobName());

        if (mRoutine != null)
            mRoutine.interrupt();
    }

    /*
     * In LoopBattleJob, extra data will be BattleArgument
     */
    @Override
    public void setExtra(Object object) {
        if (object instanceof Service) {
            mRootService = (Service)object;
        }
    }

    public void setJobEventListener(AutoJobEventListener el) {
        mListener = el;
        mSR = new ShinobiRoutine(mGL, mListener);
    }

    private void sendEvent(String msg, Object extra) {
        if (mListener != null) {
            mListener.onEventReceived(msg, extra);
        } else {
            Log.w(TAG, "There is no event listener registered.");
        }
    }

    private void sendMessage(String msg) {
        sendEvent(msg, this);
    }

    private void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(mRootService, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshSetting() {

    }

    private class MainJobRoutine extends Thread {

        private void main() throws Exception {
            int[] ambRange = new int[] {0x0A, 0x0A, 0x0A};
            boolean firstTime = true;

            mGL.setGameOrientation(ScreenPoint.SO_Landscape);
            mGL.setAmbiguousRange(ambRange);

            sendMessage("開始循環戰鬥");

            while (mShouldJobRunning) {
                refreshSetting();

                if (mSR.preBattleSetup(false, firstTime) < 0) {
                    mShouldJobRunning = false;
                    playNotificationSound();
                    return;
                }

                if (!mSR.waitForBattleStarted(200)) {
                    sendMessage("戰鬥從未開始");
                    mShouldJobRunning = false;
                    playNotificationSound();
                    return;
                }

                if (mSR.postBattleSetup(2000, ShinobiRoutineDefine.sBattleLoopModeAgain) < 0) {
                    mShouldJobRunning = false;
                    playNotificationSound();
                    return;
                }

                sleep(4000);

                firstTime = false;
            }

            sleep(2000);
            sendMessage("結束循環戰鬥");
            playNotificationSound();
            mListener.onJobDone(mSelf.getJobName());
        }

        public void run() {
            try {
                main();
            } catch (Exception e) {
                Log.e(TAG, "Routine caught an exception or been interrupted: " + e.getMessage());
            }
        }
    }
}
