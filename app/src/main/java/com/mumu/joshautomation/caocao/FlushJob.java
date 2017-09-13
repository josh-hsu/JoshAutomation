package com.mumu.joshautomation.caocao;

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
import com.mumu.joshautomation.caocao.CaoCaoDefine.*;

public class FlushJob extends AutoJob {
    private static final String TAG = "CaoCaoJob";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;

    private CaoCaoRoutine mCaoCao;
    private FlushJob mSelf;
    private Service mRootService;

    public static final String jobName = "尻尻刷首抽";

    public FlushJob() {
        super(jobName);

        /* JoshGameLibrary basic initial */
        mGL = JoshGameLibrary.getInstance();
        mGL.setGameOrientation(ScreenPoint.SO_Landscape);
        mGL.setScreenDimension(1080, 1920);
        mGL.setTouchShift(6);

        mCaoCao = new CaoCaoRoutine(mGL, mListener); //listener might be null before assigning
        mSelf = this;
    }

    @Override
    public void start() {
        super.start();
        Log.d(TAG, "starting job " + getJobName());

        refreshSetting();
        mRoutine = null;
        mRoutine = new MainJobRoutine();
        mRoutine.start();
    }

    @Override
    public void stop() {
        super.stop();
        Log.d(TAG, "stopping job " + getJobName());

        if (mRoutine != null) {
            mRoutine.interrupt();
        }
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
        mCaoCao = new CaoCaoRoutine(mGL, mListener);
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
        Log.d(TAG, "MSG: " + msg);
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
            int[] ambRange = new int[] {0x0E, 0x0E, 0x0E};
            boolean stageCleared = false;

            mGL.setGameOrientation(ScreenPoint.SO_Landscape);
            mGL.setAmbiguousRange(ambRange);

            sendMessage("開始循環戰鬥");

            while (mShouldJobRunning) {
                if (!mCaoCao.loginAsGuest()) {
                    sendMessage("Login fail");
                    mShouldJobRunning = false;
                    playNotificationSound();
                    return;
                }

                if (!mCaoCao.createCountry()) {
                    mShouldJobRunning = false;
                    playNotificationSound();
                    return;
                }

                if (!mCaoCao.firstBattleRoutine()) {
                    mShouldJobRunning = false;
                    playNotificationSound();
                    return;
                }

                if (!mCaoCao.process1st()) {
                    mShouldJobRunning = false;
                    playNotificationSound();
                    return;
                }

                if (!mCaoCao.secondBattleRoutine()) {
                    mShouldJobRunning = false;
                    playNotificationSound();
                    return;
                }

                if (!mCaoCao.process2nd()) {
                    mShouldJobRunning = false;
                    playNotificationSound();
                    return;
                }

                if (!mCaoCao.thirdBattleRoutine()) {
                    mShouldJobRunning = false;
                    playNotificationSound();
                    return;
                }

                if (!mCaoCao.process3rd()) {
                    mShouldJobRunning = false;
                    playNotificationSound();
                    return;
                }

                if (!mCaoCao.fourthBattleRoutine()) {
                    mShouldJobRunning = false;
                    playNotificationSound();
                    return;
                }

                if (!mCaoCao.process4th()) {
                    mShouldJobRunning = false;
                    playNotificationSound();
                    return;
                }

                if (!mCaoCao.fifthBattleRoutine()) {
                    mShouldJobRunning = false;
                    playNotificationSound();
                    return;
                }

            }

            sleep(1000);
            sendMessage("結束");
            playNotificationSound();
            mListener.onJobDone(mSelf.getJobName());
        }

        public void run() {
            try {
                main();
            } catch (Exception e) {
                sendMessage("任務已停止");
                Log.e(TAG, "Routine caught an exception or been interrupted: " + e.getMessage());
            }
        }
    }
}
