package com.mumu.joshautomation.caocao;

import android.app.Service;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.mumu.joshautomation.script.AutoJob;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

import static com.mumu.joshautomation.caocao.CaoCaoDefine.*;

public class FlushMoneyJob extends AutoJob {
    private static final String TAG = "CaoCaoJob";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;

    private CaoCaoRoutine mCaoCao;
    private FlushMoneyJob mSelf;
    private Service mRootService;

    public static final String jobName = "尻尻刷錢錢";

    public FlushMoneyJob() {
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

        private void onError(String msg) {
            sendMessage("E: " + msg);
            playNotificationSound();
            mListener.onJobDone(mSelf.getJobName());
        }

        private void main() throws Exception {
            int[] ambRange = new int[] {0x0E, 0x0E, 0x0E};
            boolean stageCleared = false;
            int loop_time = 0;

            mGL.setGameOrientation(ScreenPoint.SO_Landscape);
            mGL.setAmbiguousRange(ambRange);

            sendMessage("開始尻尻");

            while (mShouldJobRunning) {
                sendMessage("等大地圖出現");
                if (mGL.getCaptureService().waitOnColor(pointInTheMainMap, 200) < 0) {
                    onError("不在地圖上");
                    return;
                }

                sendMessage("點戰鬥直到窗格出現");
                if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointEnterCenter, pointDetectingButton, 1000, 20) < 0) {
                    onError("點到天黑也沒看到窗格");
                    return;
                }
                sleep(1000);

                if (mGL.getCaptureService().waitOnColor(pointDetectingButton, 130) < 0) {
                    onError("沒出現偵測窗格");
                    return;
                }
                mGL.getInputService().tapOnScreen(pointDetectingButton.coord);

                sendMessage("等戰鬥按鈕");
                if (mGL.getCaptureService().waitOnColor(pointAttackButton, 180) < 0) {
                    onError("沒看到戰鬥按鈕");
                    return;
                }
                mGL.getInputService().tapOnScreen(pointAttackButton.coord);

                sendMessage("等出征按鈕");
                if (mGL.getCaptureService().waitOnColor(pointAttackFireButton, 80) < 0) {
                    onError("沒出征紐");
                    return;
                }
                mGL.getInputService().tapOnScreen(pointMember1Select);
                sleep(500);
                mGL.getInputService().tapOnScreen(pointMember2Select);
                sleep(1000);
                mGL.getInputService().tapOnScreen(pointAttackFireButton.coord);
                sleep(500);

                sendMessage("等出征確認");
                if (mGL.getCaptureService().waitOnColor(pointAttackConfirmButton, 50) < 0) {
                    onError("等不到確認");
                    return;
                }
                mGL.getInputService().tapOnScreen(pointAttackConfirmButton.coord);
                sleep(500);
                mGL.getInputService().tapOnScreen(pointAttackConfirmButton.coord);
                sleep(1000);

                sendMessage("點螢幕點到離開戰鬥紐");
                if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointBattleCenterTap, pointBattleOutButton, 300, 100) < 0) {
                    onError("點到天黑也沒看到離開戰鬥");
                    return;
                }
                sleep(2000);
                mGL.getInputService().tapOnScreen(pointBattleOutButton.coord);

                sendMessage("等確認");
                if (mGL.getCaptureService().waitOnColor(pointBattleOutConfirmButton, 50) < 0) {
                    onError("等不到確認");
                    return;
                }
                sleep(1000);
                mGL.getInputService().tapOnScreen(pointBattleOutConfirmButton.coord);

                sendMessage("等戰鬥離開按鈕");
                if (mGL.getCaptureService().waitOnColor(pointBattleCloseButton, 50) < 0) {
                    onError("沒看到戰鬥離開紐");
                    return;
                }
                sleep(2000);
                mGL.getInputService().tapOnScreen(pointBattleCloseButton.coord);

                sendMessage("完成了" + (++loop_time) + "次");
                sleep(2000);

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
