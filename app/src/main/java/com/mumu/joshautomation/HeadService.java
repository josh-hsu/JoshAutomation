/*
 * Copyright (C) 2017 The Josh Tool Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mumu.joshautomation;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v4.app.NotificationCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.mumu.joshautomation.caocao.FlushJob;
import com.mumu.joshautomation.caocao.FlushMoneyJob;
import com.mumu.joshautomation.fgo.AutoBattleJob;
import com.mumu.joshautomation.fgo.LoopBattleJob;
import com.mumu.joshautomation.fgo.NewFlushJob;
import com.mumu.joshautomation.fgo.PureBattleJob;
import com.mumu.joshautomation.fgo.TWAutoLoginJob;
import com.mumu.joshautomation.ro.ROAutoDrinkJob;
import com.mumu.joshautomation.screencapture.PointSelectionActivity;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.joshautomation.script.AutoJobHandler;
import com.mumu.joshautomation.shinobi.ShinobiLoopBattleJob;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.ArrayList;

public class HeadService extends Service implements AutoJobEventListener{
    private static final String TAG = "JATool";
    private final Handler mHandler = new Handler();
    private final String mPngFilePath = Environment.getExternalStorageDirectory().toString() + "/select.png";
    private final String mDumpFilePath = Environment.getExternalStorageDirectory().toString() + "/select.dump";
    private static final int mUpdateUIInterval = 100;
    private static final int mMessageLastTime = 20; //20 seconds
    private Context mContext;

    // View objects
    private WindowManager mWindowManager;
    private ArrayList<HeadIconView> mHeadIconList;
    private static final int IDX_HEAD_ICON = 0;
    private static final int IDX_MSG_TEXT = 1;
    private static final int IDX_CAPTURE_ICON = 2;
    private static final int IDX_SETTING_ICON = 3;
    private static final int IDX_PLAY_ICON = 4;

    private int mTouchHeadIconCount = 0;
    private int mSameMsgCount = 0;
    private String mMessageText = "";
    private String mLastMessage = "";
    private boolean mScriptRunning = false;
    private boolean mMessageThreadRunning = false;
    private static int mDumpCount = 0;

    private JoshGameLibrary mGL;
    private AppPreferenceValue mAPV;
    private AutoJobHandler mAutoJobHandler;

    /* ==========================
     * Update UI Thread
     * ==========================
     */
    private final Runnable updateRunnable = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    private void updateUI() {
        if (mLastMessage.equals(mMessageText)) {
            mSameMsgCount++;
            if (mSameMsgCount > mMessageLastTime * 10) { //a same message will last for mMessageLastTime second on screen
                ((TextView) mHeadIconList.get(IDX_MSG_TEXT).getView()).setText("");
            }
        } else {
            mLastMessage = mMessageText;
            mSameMsgCount = 0;
            ((TextView) mHeadIconList.get(IDX_MSG_TEXT).getView()).setText(mMessageText);
        }

        if (!mScriptRunning) {
            mHeadIconList.get(IDX_PLAY_ICON).getImageView().setImageResource(R.drawable.ic_play);
        }
    }

    private final Runnable mDumpScreenRunnable = new Runnable() {
        @Override
        public void run() {
            /* Call our library to dump screen, this might take a while */
            mGL.getCaptureService().dumpScreenPNG(mPngFilePath);
            mGL.getCaptureService().dumpScreen(mDumpFilePath);

            /* show icon view back */
            configAllIconShowing(HeadIconView.VISIBLE);

            /* show result screen */
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(HeadService.this, PointSelectionActivity.class);
            startActivity(intent);
        }
    };

    /* ==========================
     * Service Basic
     * ==========================
     */
    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        // initial game panel view
        initGamePanelViews();

        // initial game library, this should never fail and follow up initGamePanelViews
        initGameLibrary();

        // initial a notification
        initNotification();

        mMessageThreadRunning = true;
        new GetMessageThread().start();
    }

    // provide our service not be able to kill
    private void initNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_bookmarked)
                        .setContentTitle("自動腳本服務")
                        .setContentText("服務已經啟用")
                        .setContentIntent(contentIntent); //Required on Gingerbread and below

        startForeground(1235, mBuilder.build());
    }

    private void initGamePanelViews() {
        mHeadIconList = new ArrayList<>();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Head Icon
        HeadIconView headIcon = new HeadIconView(new ImageView(this), mWindowManager, 0, 0);
        headIcon.getImageView().setImageResource(R.mipmap.ic_launcher);
        headIcon.setOnTapListener(new HeadIconView.OnTapListener() {
            @Override
            public void onTap(View view) {
                configHeadIconShowing((
                        getCurrentHeadIconVisibility() == HeadIconView.VISIBLE ?
                        HeadIconView.INVISIBLE : HeadIconView.VISIBLE));
            }

            @Override
            public void onLongPress(View view) {
                showExitConfirmDialog();
            }
        });
        mHeadIconList.add(headIcon);

        // Message Text Icon
        HeadIconView msgText = new HeadIconView(new TextView(this), mWindowManager, 140, 45);
        msgText.getTextView().setTextColor(Color.BLACK);
        msgText.getView().setBackgroundColor(Color.WHITE);
        mHeadIconList.add(msgText);

        // Capture Icon
        HeadIconView captureIcon = new HeadIconView(new ImageView(this), mWindowManager, 0, 120);
        captureIcon.getImageView().setImageResource(R.drawable.ic_menu_camera);
        captureIcon.setOnTapListener(new HeadIconView.OnTapListener() {
            @Override
            public void onTap(View view) {
                configCapture();
            }

            @Override
            public void onLongPress(View view) {

            }
        });
        mHeadIconList.add(captureIcon);

        // Setting Icon
        HeadIconView settingIcon = new HeadIconView(new ImageView(this), mWindowManager, 120, 120);
        settingIcon.getImageView().setImageResource(R.drawable.ic_menu_settings);
        settingIcon.setOnTapListener(new HeadIconView.OnTapListener() {
            @Override
            public void onTap(View view) {
                configSettings(false);
            }

            @Override
            public void onLongPress(View view) {
                Log.d(TAG, "config setting icon");
                configSettings(true);
            }
        });
        mHeadIconList.add(settingIcon);

        // Start and Stop control Icon
        HeadIconView startIcon = new HeadIconView(new ImageView(this), mWindowManager, 240, 120);
        startIcon.getImageView().setImageResource(R.drawable.ic_play);
        startIcon.setOnTapListener(new HeadIconView.OnTapListener() {
            @Override
            public void onTap(View view) {
                configScriptStatus();
            }

            @Override
            public void onLongPress(View view) {

            }
        });
        mHeadIconList.add(startIcon);

        // Share the same on move listener for moving at the same time
        HeadIconView.OnMoveListener moveListener = new HeadIconView.OnMoveListener() {
            @Override
            public void onMove(HeadIconView view, int initialX, int initialY, float initialTouchX, float initialTouchY, MotionEvent event) {
                // we limit the initiator of moving to only head icon
                if (view == mHeadIconList.get(IDX_HEAD_ICON)) {
                    for (HeadIconView icon : mHeadIconList) {
                        icon.moveIconDefault(initialX, initialY, initialTouchX, initialTouchY, event);
                    }
                }
            }
        };

        // Set all to add
        for(HeadIconView icon : mHeadIconList) {
            icon.addView();
            icon.setOnMoveListener(moveListener);
        }

        // Set default visibility
        configHeadIconShowing(HeadIconView.VISIBLE);
    }

    private void initGameLibrary() {
        int w = 1080;
        int h = 1920;
        // App Preference Value should be initialized here
        mAPV = AppPreferenceValue.getInstance();
        mAPV.init(mContext);

        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // we always treat the short edge as width
        // TODO: we need to find a new way to get actual panel width and height
        if (size.x > size.y) {
            w = size.y;
            if (size.x > 2000)
                h = 2160;
            else
                h = size.x;
        } else {
            w = size.x;
            if (size.y > 2000)
                h = 2160;
            else
                h = size.y;
        }

        mGL = JoshGameLibrary.getInstance();
        mGL.setContext(mContext);
        mGL.setScreenDimension(w, h);
        mGL.setGameOrientation(ScreenPoint.SO_Portrait);
        mGL.getCaptureService().setChatty(false);

        mAutoJobHandler = AutoJobHandler.getHandler();

        mAutoJobHandler.addJob(new LoopBattleJob());
        mAutoJobHandler.addJob(new AutoBattleJob());
        mAutoJobHandler.addJob(new PureBattleJob());
        mAutoJobHandler.addJob(new NewFlushJob());
        mAutoJobHandler.addJob(new TWAutoLoginJob());
        mAutoJobHandler.addJob(new ShinobiLoopBattleJob());
        mAutoJobHandler.addJob(new FlushJob());
        mAutoJobHandler.addJob(new FlushMoneyJob());
        mAutoJobHandler.addJob(new ROAutoDrinkJob());

        mAutoJobHandler.setJobEventListener(LoopBattleJob.jobName, this);
        mAutoJobHandler.setJobEventListener(PureBattleJob.jobName, this);
        mAutoJobHandler.setJobEventListener(AutoBattleJob.jobName, this);
        mAutoJobHandler.setJobEventListener(NewFlushJob.jobName, this);
        mAutoJobHandler.setJobEventListener(TWAutoLoginJob.jobName, this);
        mAutoJobHandler.setJobEventListener(ShinobiLoopBattleJob.jobName, this);
        mAutoJobHandler.setJobEventListener(FlushJob.jobName, this);
        mAutoJobHandler.setJobEventListener(FlushMoneyJob.jobName, this);

        //add service itself to job
        mAutoJobHandler.setExtra(LoopBattleJob.jobName, this);
        mAutoJobHandler.setExtra(ShinobiLoopBattleJob.jobName, this);
        mAutoJobHandler.setExtra(FlushJob.jobName, this);
        mAutoJobHandler.setExtra(FlushMoneyJob.jobName, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Game tool
        for (HeadIconView icon : mHeadIconList) {
            icon.removeView();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                configHeadIconShowing(HeadIconView.VISIBLE);
            }
        }
        return START_STICKY;
    }

    /* ==========================
     * Icon visibility
     * ==========================
     */
    public int getCurrentHeadIconVisibility() {
        if (mTouchHeadIconCount % 2 == 0)
            return HeadIconView.INVISIBLE;
        else
            return HeadIconView.VISIBLE;
    }

    private void configHeadIconShowing(int visible) {
        // If current status is what we want, return here
        if (getCurrentHeadIconVisibility() == visible)
            return;

        // Increase icon touch count
        mTouchHeadIconCount++;

        for (HeadIconView view : mHeadIconList) {
            if (view == mHeadIconList.get(IDX_HEAD_ICON) || view == mHeadIconList.get(IDX_MSG_TEXT))
                continue;
            view.setVisibility(visible);
        }
    }

    private void configAllIconShowing(int visible) {
        for (HeadIconView view : mHeadIconList) {
            view.setVisibility(visible);
        }
    }

    private void showExitConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.MyDialogStyle))
                .setTitle(getString(R.string.headservice_stop_title))
                .setMessage(getString(R.string.headservice_stop_info))
                .setPositiveButton(getString(R.string.headservice_stop_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Let's do some background stuff
                        stopSelf();
                    }
                })
                .setNegativeButton(getString(R.string.startup_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        AlertDialog alert = builder.create();
        Window win = alert.getWindow();
        if (win != null) win.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }

    /* ==========================
     * Tap behavior of icons
     * ==========================
     */
    private void configCapture() {
        // head service is responsible for setting orientation
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == 1)
            mGL.setGameOrientation(ScreenPoint.SO_Portrait);
        else
            mGL.setGameOrientation(ScreenPoint.SO_Landscape);

        configAllIconShowing(HeadIconView.INVISIBLE);
        mHandler.postDelayed(mDumpScreenRunnable, 100);
    }

    private void configSettings(boolean isLongPress) {
        if (isLongPress) {
            String filename = mDumpFilePath + mDumpCount;
            mGL.getCaptureService().dumpScreen(filename);
            mMessageText = "Dump count = " + mDumpCount;
            mDumpCount++;
        } else {
            mMessageText = "開啟中";
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(HeadService.this, AppPreferenceActivity.class);
            startActivity(intent);
        }
    }

    private void configScriptStatus() {
        String currentSelectIndexPref = mAPV.getPrefs().getString("scriptSelectPref", "0");
        int currentSelectIndex = Integer.parseInt(currentSelectIndexPref);
        Log.d(TAG, "select " + currentSelectIndex);

        if(!mScriptRunning) {
            mAutoJobHandler.startJob(currentSelectIndex);
            mHeadIconList.get(IDX_PLAY_ICON).getImageView().setImageResource(R.drawable.ic_pause);
        } else {
            mAutoJobHandler.stopJob(currentSelectIndex);
            mHeadIconList.get(IDX_PLAY_ICON).getImageView().setImageResource(R.drawable.ic_play);
        }

        mScriptRunning = !mScriptRunning;
    }

    /* ==========================
     * Message handler
     * ==========================
     */
    @Override
    public void onEventReceived(String msg, Object extra) {
        mMessageText = msg;
    }

    @Override
    public void onJobDone(String job) {
        Log.d(TAG, "Job " + job + " has done");

        if (job.equals(PureBattleJob.jobName)) {
            mMessageText = "完成單次戰鬥";
        } else if (job.equals(AutoBattleJob.jobName)) {
            mScriptRunning = false;
            mMessageText = "循環戰鬥結束";
        }
    }

    private class GetMessageThread extends Thread {
        public void run() {
            while(mMessageThreadRunning) {
                mHandler.post(updateRunnable);
                try {
                    Thread.sleep(mUpdateUIInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
