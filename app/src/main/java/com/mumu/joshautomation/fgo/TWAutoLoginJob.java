package com.mumu.joshautomation.fgo;

import android.util.Log;

import com.mumu.joshautomation.AppPreferenceValue;
import com.mumu.joshautomation.script.AutoJob;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

import static com.mumu.joshautomation.fgo.FGORoutineDefineTW.*;

public class TWAutoLoginJob extends AutoJob {
    private static final String TAG = "TWAutoLoginJob";
    private MainJobRoutine mRoutine;
    private JoshGameLibrary mGL;
    private AutoJobEventListener mListener;

    private TWAutoLoginJob mSelf;

    public static final String jobName = "FGO TW Auto Login";

    private static int mCurrentIndex = 1;
    private static int mTotalIndex = 40;
    private static String mAccountIDPrefix = "lazypig";
    private static String mAccountPassword = ""; //no need

    public TWAutoLoginJob() {
        super(jobName);

        /* JoshGameLibrary basic initial */
        mGL = JoshGameLibrary.getInstance();
        mGL.setGameOrientation(ScreenPoint.SO_Landscape);
        mGL.setScreenDimension(1080, 1920);

        mSelf = this;
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

        if (mRoutine != null)
            mRoutine.interrupt();
    }

    /*
     * We don't accept extra here
     */
    @Override
    public void setExtra(Object object) {

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
        Log.d(TAG, "MSG: " + msg);
        sendEvent(msg, this);
    }

    private class MainJobRoutine extends Thread {

        private void main() throws Exception {
            int[] ambRange = new int[] {0x0A, 0x0A, 0x0A};
            boolean skipBulletin = false;
            int startSerial = Integer.parseInt(AppPreferenceValue.getInstance().getPrefs().getString("twLoginStart", "2"));

            mGL.setGameOrientation(ScreenPoint.SO_Landscape);
            mGL.setAmbiguousRange(ambRange);

            sendMessage("開始自動登入_台版");
            for(int i = startSerial; i <= mTotalIndex + 1; i++) {
                //in title screen
                if (mGL.getCaptureService().waitOnColor(titleScreenPoint, 50, mRoutine) < 0) {
                    sendMessage("不是登入畫面捏");
                    mShouldJobRunning = false;
                    return;
                }
                mGL.getInputService().tapOnScreen(titleScreenPoint.coord);
                Thread.sleep(6000);
                sendMessage("開始 i = " + i);

                if (!skipBulletin) {
                    if (mGL.getCaptureService().waitOnColor(pointBulletinExit, 30, mRoutine) < 0) {
                        sendMessage("沒公告，跳過");
                    } else {
                        mGL.getInputService().tapOnScreen(pointBulletinExit.coord);
                    }
                    skipBulletin = true;
                }

                Thread.sleep(2000);
                while(true) {
                    if (mGL.getCaptureService().waitOnColor(pointLoginBonusButton, 12, mRoutine) < 0) {
                        sendMessage("訊息點完");
                        break;
                    } else {
                        mGL.getInputService().tapOnScreen(pointLoginBonusButton.coord);
                    }
                }

                if (i > mTotalIndex) {
                    sendMessage("結束囉");
                    mShouldJobRunning = false;
                    return;
                }

                Thread.sleep(1500);

                //press menu
                if (mGL.getCaptureService().waitOnColor(pointMenuButton, 10, mRoutine) < 0) {
                    sendMessage("找不到MENU");
                    mShouldJobRunning = false;
                    return;
                } else {
                    mGL.getInputService().tapOnScreen(pointMenuButton.coord);
                    Thread.sleep(1000);
                }

                if (mGL.getCaptureService().waitOnColor(pointMyRoom, 10, mRoutine) < 0) {
                    sendMessage("找不到MY ROOM");
                    mShouldJobRunning = false;
                    return;
                } else {
                    mGL.getInputService().tapOnScreen(pointMyRoom.coord);
                }

                //swipe menu
                if (mGL.getCaptureService().waitOnColor(pointMyRoomDetect, 100, mRoutine) < 0) {
                    sendMessage("進不去my room?");
                    mShouldJobRunning = false;
                    return;
                }
                mGL.getInputService().swipeOnScreen(pointMyRoomSwipeStart.coord, pointMyRoomSwipeEnd.coord);

                Thread.sleep(1500);
                mGL.getInputService().tapOnScreen(pointMyRoomReturnTitle.coord);
                Thread.sleep(200);
                mGL.getInputService().tapOnScreen(pointMyRoomReturnTitle.coord);

                if (mGL.getCaptureService().waitOnColor(pointMyRoomReturnTitleConfirm, 30, mRoutine) < 0) {
                    sendMessage("沒有確定?");
                    mShouldJobRunning = false;
                    return;
                } else {
                    mGL.getInputService().tapOnScreen(pointMyRoomReturnTitleConfirm.coord);
                }

                //wait account
                if (mGL.getCaptureService().waitOnColor(loginAccountID, 600, mRoutine) < 0) {
                    sendMessage("帳號輸入畫面沒出來");
                    mShouldJobRunning = false;
                    return;
                } else {
                    Thread.sleep(2000);
                    mGL.getInputService().tapOnScreen(loginAccountID.coord);
                }
                Thread.sleep(1000);

                if (mGL.getCaptureService().waitOnColor(loginKeyBackspace, 40, mRoutine) < 0) {
                    sendMessage("帳號輸入?");
                    mShouldJobRunning = false;
                    return;
                } else {
                    mGL.getInputService().tapOnScreen(loginKeyBackspace.coord);
                    Thread.sleep(200);
                    mGL.getInputService().tapOnScreen(loginKeyBackspace.coord);
                    Thread.sleep(125);
                    mGL.getInputService().tapOnScreen(loginKeyBackspace.coord);
                }

                //skip accounts
                if (i == 13)
                    i+=2;
                else if (i == 20 || i == 32)
                    i+=1;

                //input account number
                String formattedAcc = String.format("%03d", i);
                mGL.getInputService().inputText(formattedAcc);
                Thread.sleep(1000);
                mGL.getInputService().tapOnScreen(loginKeyNext.coord);
                Thread.sleep(1000);
                mGL.getInputService().tapOnScreen(loginKeyNext.coord);
                Thread.sleep(1000);

                if (mGL.getCaptureService().waitOnColor(loginLogin, 100, mRoutine) < 0) {
                    sendMessage("回不去LOGIN");
                } else {
                    mGL.getInputService().tapOnScreen(loginLogin.coord);
                }

                Thread.sleep(6000);
            }

            mShouldJobRunning = false;
            sleep(1000);
            sendMessage("結束啦");
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
