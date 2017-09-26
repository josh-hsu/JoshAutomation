package com.mumu.joshautomation.caocao;

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

    public static final String jobName = "尻尻刷錢錢";

    public FlushMoneyJob() {
        super(jobName);

        /* JoshGameLibrary basic initial */
        mGL = JoshGameLibrary.getInstance();
        mGL.setGameOrientation(ScreenPoint.SO_Landscape);
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
        mGL.getInputService().playNotificationSound();
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

                sendMessage("點偵測直到戰鬥出現");
                if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointDetectingButton, pointAttackButton, 1000, 20) < 0) {
                    onError("點到天黑也沒看到戰鬥");
                    return;
                }

                sendMessage("點戰鬥直到出征出現");
                if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointAttackButton, pointAttackFireButton, 1000, 20) < 0) {
                    onError("點到天黑也沒看到出征");
                    return;
                }

                sendMessage("等出征按鈕");
                if (mGL.getCaptureService().waitOnColor(pointAttackFireButton, 80) < 0) {
                    onError("沒出征紐");
                    return;
                }

                if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointMember1Select, pointMember1DidSelect, 1000, 20) < 0) {
                    onError("選第一人失敗");
                    return;
                }

                if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointMember2Select, pointMember2DidSelect, 1000, 20) < 0) {
                    onError("選第二人失敗");
                    return;
                }

                sendMessage("點出征直到確認出現");
                if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointAttackFireButton, pointAttackConfirmButton, 1000, 20) < 0) {
                    onError("點到天黑也沒看到確認");
                    return;
                }
                sleep(1000);

                sendMessage("等出征確認");
                if (mGL.getCaptureService().waitOnColor(pointAttackConfirmButton, 50) < 0) {
                    onError("等不到確認");
                    return;
                }

                if (mGL.getInputService().tapOnScreenUntilColorChanged(pointAttackConfirmButton, 1500, 10) < 0) {
                    onError("戰鬥無法開始");
                    return;
                }

                sendMessage("點螢幕點到離開戰鬥紐");
                if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointBattleCenterTap, pointBattleOutButton, 300, 100) < 0) {
                    onError("點到天黑也沒看到離開戰鬥");
                    return;
                }

                sendMessage("點離開直到確認按鈕");
                if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointBattleOutButton, pointBattleOutConfirmButton, 1000, 20) < 0) {
                    onError("點到天黑沒見確認");
                    return;
                }

                sendMessage("點確認直到離開按鈕");
                if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointBattleOutConfirmButton, pointBattleCloseButton, 1000, 20) < 0) {
                    onError("點到天黑沒見離開");
                    return;
                }

                sendMessage("點離開直到大地圖");
                if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointBattleCloseButton, pointInTheMainMap, 1000, 20) < 0) {
                    onError("點到天黑沒見地圖");
                    return;
                }

                sendMessage("完成了" + (++loop_time) + "次");
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
