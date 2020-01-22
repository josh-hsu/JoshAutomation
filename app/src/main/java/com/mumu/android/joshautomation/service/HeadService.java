/*
 * Copyright (C) 2019 The Josh Tool Project
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
package com.mumu.android.joshautomation.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mumu.android.joshautomation.R;
import com.mumu.android.joshautomation.activity.AppPreferenceActivity;
import com.mumu.android.joshautomation.activity.MainActivity;
import com.mumu.android.joshautomation.activity.PointSelectionActivity;
import com.mumu.android.joshautomation.content.AppPreferenceValue;
import com.mumu.android.joshautomation.content.AutoJobClasses;
import com.mumu.android.joshautomation.content.DefinitionLoader;
import com.mumu.android.joshautomation.autojob.AutoJob;
import com.mumu.android.joshautomation.autojob.AutoJobAction;
import com.mumu.android.joshautomation.autojob.AutoJobEventListener;
import com.mumu.android.joshautomation.autojob.AutoJobHandler;
import com.mumu.libjoshgame.GameLibrary20;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.ArrayList;
import java.util.HashMap;

public class HeadService extends Service implements AutoJobEventListener {
    private static final String TAG = "HeadService";
    private final Handler mHandler = new Handler();
    private final String mPngFilePath = Environment.getExternalStorageDirectory().toString() + "/select.png";
    private final String mDumpFilePath = Environment.getExternalStorageDirectory().toString() + "/select.dump";
    private static final int mUpdateUIInterval = 100;
    private static final int mMessageLastTime = 3; //3 seconds
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

    private GameLibrary20 mGL;
    private AppPreferenceValue mAPV;
    private AutoJobHandler mAutoJobHandler;
    private static boolean mAutoJobAdded = false;

    // actions from job
    private static final long mDefaultTimeoutSecond = 15;
    public static final int ACTION_SHOW_DIALOG = 0;
    public static final int ACTION_SHOW_INPUT = 1;
    public static final int ACTION_SHOW_WARNING = 2;
    public static final int ACTION_SHOW_PROGRESS = 3;

    private AlertDialog mActionProgressDialog;

    /* ==========================
     * Update UI Thread
     * ==========================
     */
    private final Runnable updateRunnable = new Runnable() {
        public void run() {
            updateUIMessageText();
        }
    };

    private void updateUIMessageText() {
        if (mAPV.getPrefs().getBoolean("userInteractTextView", true)) {
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
        }

        if (!mScriptRunning) {
            mHeadIconList.get(IDX_PLAY_ICON).getImageView().setImageResource(R.drawable.ic_play);
        }
    }

    private final Runnable mDumpScreenRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mGL.dumpScreenshotManual(mDumpFilePath);
                mGL.requestRefresh();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            configAllIconShowing(HeadIconView.VISIBLE);

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

        mAPV = AppPreferenceValue.getInstance();
        mAPV.init(mContext);

        // initial game panel view
        initGamePanelViews();

        // initial game library, this should never fail and follow up initGamePanelViews
        initGameLibrary();

        // initial auto jobs, this should never fail and follow up by initGameLibrary
        initAutoJobs();

        // initial a notification
        initNotification();

        mMessageThreadRunning = true;
        new GetMessageThread().start();
    }

    // provide our service not be able to kill
    private void initNotification() {
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "com.mumu.android.joshautomation";
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_bookmarked)
                    .setContentTitle("自動腳本服務")
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_bookmarked)
                            .setContentTitle("自動腳本服務")
                            .setContentText("服務已經啟用")
                            .setContentIntent(contentIntent); //Required on Gingerbread and below

            notification = mBuilder.build();
        }

        startForeground(1235, notification);
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
        HeadIconView msgText = new HeadIconView(new TextView(this), mWindowManager, 120, 20);
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
        HeadIconView settingIcon = new HeadIconView(new ImageView(this), mWindowManager, 0, 240);
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
        HeadIconView startIcon = new HeadIconView(new ImageView(this), mWindowManager, 0, 360);
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

        // auto correction control Icon
        HeadIconView acIcon = new HeadIconView(new ImageView(this), mWindowManager, 0, 480);
        acIcon.getImageView().setImageResource(R.drawable.ic_menu_slideshow);
        acIcon.setOnTapListener(new HeadIconView.OnTapListener() {
            @Override
            public void onTap(View view) {
                configAutoCorrection();
            }

            @Override
            public void onLongPress(View view) {

            }
        });
        mHeadIconList.add(acIcon);

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

    private GameLibrary20 initGameLibrary20() {
        int ret;
        GameLibrary20 gl20 = new GameLibrary20();
        String hackSSPackageName = "com.mumu.joshautomationservice";
        String hackSSServiceName = ".CommandService";
        String hackSSIntfName = "";
        String hackSSTransactCode = "1";
        HashMap<String, String> hackSSParameters = new HashMap<>();

        if (mAPV.getPrefs().getBoolean("ssEnabled", false)) {
            hackSSPackageName = mAPV.getPrefs().getString("ssPackageName", "com.mumu.joshautomationservice");
            hackSSServiceName = mAPV.getPrefs().getString("ssServiceName", ".CommandService");
            hackSSIntfName = mAPV.getPrefs().getString("ssInterfaceName", "");
            hackSSTransactCode = mAPV.getPrefs().getString("ssTransactCode", "1");
        }

        hackSSParameters.put("packageName", hackSSPackageName);
        hackSSParameters.put("serviceName", hackSSServiceName);
        hackSSParameters.put("interfaceName", hackSSIntfName);
        hackSSParameters.put("code", hackSSTransactCode);

        Object[] initObjects = new Object[] {this, hackSSParameters};

        ret = gl20.chooseDevice(GameLibrary20.DEVICE_TYPE_ANDROID_INTERNAL);
        if (ret < 0) {
            Log.e(TAG, "Device is not here?");
            return null;
        }

        ret = gl20.setDeviceEssentials(null);
        if (ret < 0) {
            Log.e(TAG, "Set device essentials failed");
            return null;
        }

        ret = gl20.initDevice(initObjects);
        if (ret < 0) {
            Log.e(TAG, "Initial AndroidInternal failed");
            return null;
        }

        return gl20;
    }

    private void initGameLibrary() {
        int w, h;
        int userWidth, userHeight, userAmbValue, userTouchShift;
        int userScreenXOffset, userScreenYOffset, userWaitTransactTime;

        // try to get user's setting
        try {
            userWidth = Integer.parseInt(mAPV.getPrefs().getString("userSetWidth", "0"));
            userHeight = Integer.parseInt(mAPV.getPrefs().getString("userSetHeight", "0"));
            userAmbValue = Integer.parseInt(mAPV.getPrefs().getString("userAmbValue", "0"));
            userTouchShift = Integer.parseInt(mAPV.getPrefs().getString("userSetTouchShift", "0"));
            userScreenXOffset = Integer.parseInt(mAPV.getPrefs().getString("userSetScreenXOffset", "0"));
            userScreenYOffset = Integer.parseInt(mAPV.getPrefs().getString("userSetScreenYOffset", "0"));
            userWaitTransactTime = Integer.parseInt(mAPV.getPrefs().getString("userSetWaitTransactionDoneTime", "0"));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Setting value format error: " + e.getMessage());
            userWidth = 0;
            userHeight = 0;
            userAmbValue = 0;
            userTouchShift = 0;
            userScreenXOffset = 0;
            userScreenYOffset = 0;
            userWaitTransactTime = 0;
        }

        Log.d(TAG, "APV: [w:" + userWidth + "],[h:" + userHeight + "],[amb:" + userAmbValue + "]");
        Log.d(TAG, "APV: [ts:" + userTouchShift + "],[trans:" + userWaitTransactTime + "]");

        // Initial DefinitionLoader
        DefinitionLoader.getInstance().setResources(mContext.getResources());

        // Initial display size
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        w = size.x;
        h = size.y;

        mGL = initGameLibrary20();

        // Check if user override settings
        if (userWidth != 0 && userHeight != 0)
            mGL.setScreenResolution(userWidth, userHeight);
        else
            mGL.setScreenResolution(w, h);

        mGL.setChatty(mAPV.getPrefs().getBoolean("captureServiceChatty", false));

        if (userAmbValue != 0)
            mGL.setScreenAmbiguousRange(new int[]{userAmbValue, userAmbValue, userAmbValue});

        if (userScreenXOffset != 0 || userScreenYOffset != 0)
            mGL.setScreenOffset(userScreenXOffset, userScreenYOffset, ScreenPoint.SO_Portrait);

        if (userTouchShift != 0)
            mGL.setMouseShift(userTouchShift);

        if (userWaitTransactTime != 0)
            mGL.setDeviceCommandTransactionTime(userWaitTransactTime);

    }

    private void initAutoJobs() {
        mAutoJobHandler = AutoJobHandler.getHandler();

        if (!mAutoJobAdded) {
            try {
                for (Class clazz : AutoJobClasses.autoJobLists) {
                    Object object = clazz.newInstance();
                    if (object instanceof AutoJob) {
                        AutoJob job = (AutoJob) object;
                        mAutoJobHandler.addJob(job);
                        mAutoJobHandler.setJobEventListener(job.getJobName(), this);
                        mAutoJobHandler.setExtra(job.getJobName(), mGL); //set game library to jobs
                    } else {
                        Log.e(TAG, "class " + clazz.getName() + " is not an AutoJob");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "AutoJob list contains an error, aborting message: " + e.getMessage());
                return;
            }

            Log.d(TAG, "AutoJob initialized successfully");
            mAutoJobAdded = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGL != null) {
            mGL.getDevice().destroyDevice();
        }

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
        if (win != null) win.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
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
            mGL.setScreenMainOrientation(ScreenPoint.SO_Portrait);
        else
            mGL.setScreenMainOrientation(ScreenPoint.SO_Landscape);

        configAllIconShowing(HeadIconView.INVISIBLE);
        mHandler.postDelayed(mDumpScreenRunnable, 100);
    }

    private void configSettings(boolean isLongPress) {
        if (isLongPress) {
            String filename = mDumpFilePath + mDumpCount;
            try {
                mGL.dumpScreenshotManual(filename);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

    private void configAutoCorrection() {
        String currentSelectIndexPref = mAPV.getPrefs().getString("scriptSelectPref", "0");
        int currentSelectIndex = Integer.parseInt(currentSelectIndexPref);

        mAutoJobHandler.requestAutoCorrection(currentSelectIndex, null);
    }

    /* ==========================
     * Message handler
     * ==========================
     */
    @Override
    public void onMessageReceived(String msg, Object extra) {
        mMessageText = msg;
    }

    @Override
    public void onJobDone(String job) {
        Log.d(TAG, "Job " + job + " has done");

        //TODO: implement here
        mMessageText = "腳本結束" + job;
        mScriptRunning = false;
    }

    @Override
    public void onActionReceived(final int what, final AutoJobAction action) {
        Log.d(TAG, "Interact request from script, what=" + what);

        // handle action
        // this function will only be called in script context
        action.handleAction(mHandler, new Runnable() {
            @Override
            public void run() {
                doingScriptAction(what, action);
            }
        });

        // finish waiting
        Log.d(TAG, "Interact done");
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

    /* ==========================
     * Action handler
     * ==========================
     */
    private void doingScriptAction(int what, AutoJobAction action) {
        switch (what) {
            case ACTION_SHOW_DIALOG:
                actionShowDialog(action);
                break;
            case ACTION_SHOW_INPUT:
                actionShowInputDialog(action);
                break;
            case ACTION_SHOW_WARNING:
                break;
            case ACTION_SHOW_PROGRESS:
                actionShowProgressDialog(action);
                break;
            default:
                Log.d(TAG, "Unknown request.");
                break;
        }
    }

    private void actionShowDialog(final AutoJobAction action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.MyDialogStyle))
                .setTitle(action.getTitle())
                .setMessage(action.getSummary())
                .setPositiveButton(action.getOptions().length >= 1 ? action.getOptions()[0] : "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        action.doReaction("true", null);
                    }
                });

        if (action.getOptions().length > 1)
                builder.setNegativeButton(action.getOptions().length >= 2 ? action.getOptions()[1] : "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        action.doReaction("false", null);
                    }
                });

        AlertDialog alert = builder.create();
        Window win = alert.getWindow();
        if (win != null) win.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        alert.show();
    }

    private void actionShowInputDialog(final AutoJobAction action) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.action_dialog_input, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.MyDialogStyle)).create();
        alertDialog.setTitle(action.getTitle());
        alertDialog.setCancelable(false);
        alertDialog.setMessage(action.getSummary());

        final EditText inputEditText = view.findViewById(R.id.inputEditText);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                action.doReaction(inputEditText.getText().toString(), null);
            }
        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(view);
        Window win = alertDialog.getWindow();
        if (win != null) win.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        alertDialog.show();
    }

    private void actionShowProgressDialog(final AutoJobAction action) {
        String command = action.getAction();

        // skip any operation except NEW when no progress bar dialog present
        if (mActionProgressDialog == null && !command.equals("NEW")) {
            Log.w(TAG, "Progress bar dialog is not shown, aborting");
            action.doReaction("ERROR:cannot get progress bar", null);
        }

        if (command.startsWith("NEW")) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.action_dialog_progress, null);
            mActionProgressDialog = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.MyDialogStyle)).create();
            mActionProgressDialog.setView(view);
            mActionProgressDialog.setTitle(action.getTitle());
            mActionProgressDialog.setCancelable(true);
            mActionProgressDialog.setMessage(action.getSummary());
            Window win = mActionProgressDialog.getWindow();
            if (win != null) win.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            mActionProgressDialog.show();
        } else if (command.startsWith("UPDATE:")) {
            int progress;
            String progressString;
            try {
                progressString = command.split(":")[1];
                progress = Integer.parseInt(progressString);
            } catch (Exception e) {
                e.printStackTrace();
                action.doReaction("ERROR:exception=" + e.getMessage(), null);
                return;
            }
            Log.d(TAG, "Update to " + progress);
            ProgressBar pb = mActionProgressDialog.findViewById(R.id.progressBar);
            if (pb != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    pb.setProgress(progress, true);
                } else {
                    pb.setProgress(progress);
                }
                TextView tv = mActionProgressDialog.findViewById(R.id.progressText);
                tv.setText(progress + " %");
            } else {
                action.doReaction("ERROR:cannot get progress bar", null);
                return;
            }
            mActionProgressDialog.setTitle(action.getTitle());
            mActionProgressDialog.setCancelable(true);
            mActionProgressDialog.setMessage(action.getSummary());
        } else if (command.startsWith("CLOSE")) {
            mActionProgressDialog.dismiss();
            mActionProgressDialog = null;
        }

        action.doReaction("SUCCESS", null);
    }
}
