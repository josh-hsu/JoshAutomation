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

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.mumu.joshautomation.fgo.BattleArgument;
import com.mumu.joshautomation.screencapture.PointSelectionActivity;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.joshautomation.script.AutoJobHandler;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.ArrayList;

public class HeadService extends Service implements AutoJobEventListener{
    private static final String TAG = "JATool";
    private final Handler mHandler = new Handler();
    private String mPngFilePath = Environment.getExternalStorageDirectory().toString() + "/select.png";
    private String mDumpFilePath = Environment.getExternalStorageDirectory().toString() + "/select.dump";
    private Context mContext;

    // View objects
    private WindowManager mWindowManager;
    private ArrayList<HeadIconView> mHeadIconList;
    private static final int IDX_HEAD_ICON = 0;
    private static final int IDX_MSG_TEXT = 1;
    private static final int IDX_CAPTURE_ICON = 2;
    private static final int IDX_HOME_ICON = 3;
    private static final int IDX_SETTING_ICON = 4;
    private static final int IDX_PLAY_ICON = 5;

    private int mTouchHeadIconCount = 0;
    private int mSameMsgCount = 0;
    private String mMessageText = "";
    private String mLastMessage = "";
    private boolean mScriptRunning = false;
    private boolean mHomeRunning = false;
    private boolean mMessageThreadRunning = false;
    private static int mDumpCount = 0;

    private JoshGameLibrary mGL;
    private AutoJobHandler mAutoJobHandler;

    /*
     * Runnable threads
     */
    private final Runnable updateRunnable = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    private void updateUI() {
        if (mLastMessage.equals(mMessageText)) {
            mSameMsgCount++;
            if (mSameMsgCount > 20) { //a same message will last for 2 second on screen
                ((TextView) mHeadIconList.get(IDX_MSG_TEXT).getView()).setText("");
            }
        } else {
            mLastMessage = mMessageText;
            mSameMsgCount = 0;
            ((TextView) mHeadIconList.get(IDX_MSG_TEXT).getView()).setText(mMessageText);
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

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        initGamePanelViews();
        mMessageThreadRunning = true;
        new GetMessageThread().start();

        mGL = JoshGameLibrary.getInstance();
        mGL.setContext(mContext);
        mGL.setScreenDimension(1080, 1920);
        mGL.setGameOrientation(ScreenPoint.SO_Portrait);

        mAutoJobHandler = AutoJobHandler.getHandler();
        mAutoJobHandler.setJobEventListener(AutoJobHandler.FGO_BATTLE_JOB, this);
        mAutoJobHandler.setJobEventListener(AutoJobHandler.FGO_PURE_BATTLE_JOB, this);
    }

    private void initGamePanelViews() {
        mHeadIconList = new ArrayList<>();

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

        // Home Icon
        HeadIconView homeIcon = new HeadIconView(new ImageView(this), mWindowManager, 120, 120);
        homeIcon.getImageView().setImageResource(R.drawable.ic_menu_home_outline);
        homeIcon.setOnTapListener(new HeadIconView.OnTapListener() {
            @Override
            public void onTap(View view) {
                Log.d(TAG, "config home icon");
                configHome();
            }

            @Override
            public void onLongPress(View view) {

            }
        });
        mHeadIconList.add(homeIcon);

        // Setting Icon
        HeadIconView settingIcon = new HeadIconView(new ImageView(this), mWindowManager, 240, 120);
        settingIcon.getImageView().setImageResource(R.drawable.ic_menu_settings);
        settingIcon.setOnTapListener(new HeadIconView.OnTapListener() {
            @Override
            public void onTap(View view) {
                Log.d(TAG, "config setting icon");
                configSettings();
            }

            @Override
            public void onLongPress(View view) {

            }
        });
        mHeadIconList.add(settingIcon);

        // Start and Stop control Icon
        HeadIconView startIcon = new HeadIconView(new ImageView(this), mWindowManager, 360, 120);
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

        // Share the same on move listener for moving in the same time
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
        return START_NOT_STICKY;
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

    private void configHome() {

        if(!mHomeRunning) {
            mAutoJobHandler.startJob(AutoJobHandler.FGO_PURE_BATTLE_JOB);
            mHeadIconList.get(IDX_HOME_ICON).getImageView().setImageResource(R.drawable.ic_pause);
        } else {
            mAutoJobHandler.stopJob(AutoJobHandler.FGO_PURE_BATTLE_JOB);
            mHeadIconList.get(IDX_HOME_ICON).getImageView().setImageResource(R.drawable.ic_menu_home_outline);
        }

        mHomeRunning = !mHomeRunning;
    }

    private void configSettings() {
        String filename = mDumpFilePath + mDumpCount;
        mGL.getCaptureService().dumpScreen(filename);
        mMessageText = "Dump count = " + mDumpCount;
        mDumpCount++;

    }

    private void configScriptStatus() {
        if(!mScriptRunning) {
            mAutoJobHandler.startJob(AutoJobHandler.FGO_BATTLE_JOB);
            mHeadIconList.get(IDX_PLAY_ICON).getImageView().setImageResource(R.drawable.ic_pause);
        } else {
            mAutoJobHandler.stopJob(AutoJobHandler.FGO_BATTLE_JOB);
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
        Log.d(TAG, "Get event message " + msg);
        mMessageText = msg;
    }

    @Override
    public void onJobDone(String job) {
        Log.d(TAG, "Job " + job + " has done");

        if (job.equals(mAutoJobHandler.getJobName(AutoJobHandler.FGO_PURE_BATTLE_JOB))) {
            mHomeRunning = false;
            mHeadIconList.get(IDX_HOME_ICON).getImageView().setImageResource(R.drawable.ic_menu_home_outline);
            mMessageText = "完成單次戰鬥";
        } else if (job.equals(mAutoJobHandler.getJobName(AutoJobHandler.FGO_BATTLE_JOB))) {
            mScriptRunning = false;
            mHeadIconList.get(IDX_PLAY_ICON).getImageView().setImageResource(R.drawable.ic_play);
            mMessageText = "循環戰鬥結束";
        }
    }

    class GetMessageThread extends Thread {
        public void run() {
            while(mMessageThreadRunning) {
                mHandler.post(updateRunnable);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
