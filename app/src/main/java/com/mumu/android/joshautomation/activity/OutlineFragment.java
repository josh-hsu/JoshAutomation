/*
 * Copyright (C) 2020 The Josh Tool Project
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

package com.mumu.android.joshautomation.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mumu.android.joshautomation.R;
import com.mumu.android.joshautomation.service.HeadService;

public class OutlineFragment extends MainFragment {
    private static final String TAG = "JATool";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Button mStartServiceButton;
    private TextView mBarTextView;
    private ImageView mCircleImageView;
    private ImageView mTRexImageView;
    private ImageView mBirdImageView;

    private RotateAnimation mCircleAnimation;

    public OutlineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static OutlineFragment newInstance(String param1, String param2) {
        OutlineFragment fragment = new OutlineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ontline, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onFabClick(View view) {
        Log.d(TAG, "Fab click from outline");
        final Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            final MainActivity mainActivity = (MainActivity) activity;
            mainActivity.showSnackBarMessage("Test for outline");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        prepareView(view);
        updateView();
    }

    @Override
    public void onDetailClick() {
        Log.d(TAG, "Detail click on electricity fragment");
    }

    private void prepareView(View view) {
        mCircleAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mStartServiceButton = (Button) view.findViewById(R.id.button_start_service);
        mCircleImageView = (ImageView) view.findViewById(R.id.imageViewCircle);
        mCircleImageView.setImageResource(R.drawable.ic_circle);
        mTRexImageView = (ImageView) view.findViewById(R.id.imageTRex);
        mBirdImageView = (ImageView) view.findViewById(R.id.imageBird);
        mBirdImageView.setVisibility(View.INVISIBLE);

        mStartServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isChatHeadServiceRunning()) {
                    startChatHeadService();
                    spinTheCircle(true);
                    runTRexRun(true);
                    birdFlyingOnce();
                    ((Button) view).setText(R.string.outline_stop_service);
                } else {
                    stopChatHeadService();
                    spinTheCircle(false);
                    runTRexRun(false);
                    ((Button) view).setText(R.string.outline_start_service);
                }
            }
        });
        if (!isChatHeadServiceRunning()) {
            mStartServiceButton.setText(R.string.outline_start_service);
            spinTheCircle(false);
            runTRexRun(false);
        } else {
            mStartServiceButton.setText(R.string.outline_stop_service);
            spinTheCircle(true);
            runTRexRun(true);
        }

        mBarTextView = (TextView) view.findViewById(R.id.textViewElectricBarView);
        mBarTextView.setText("App 版本: " + getVersionName(getContext()));
    }

    /*
     * updateView will be called when mUpdateRunnable is triggered
     */
    private void updateView() {

    }

    private void spinTheCircle(boolean start) {
        mCircleAnimation.setDuration(2000);
        mCircleAnimation.setRepeatCount(-1);
        mCircleAnimation.setInterpolator(new LinearInterpolator());

        if (start) {
            mCircleImageView.startAnimation(mCircleAnimation);
        } else {
            mCircleImageView.clearAnimation();
        }
    }

    private void runTRexRun(boolean run) {
        if (run) {
            mTRexImageView.setImageResource(R.drawable.t_rex_running_anim);
            AnimationDrawable animationDrawable = (AnimationDrawable) mTRexImageView.getDrawable();
            animationDrawable.start();
        } else {
            mTRexImageView.setImageResource(R.mipmap.rex2);
        }
    }

    private void birdFlyingOnce() {
        mBirdImageView.setVisibility(View.VISIBLE);
        mBirdImageView.setImageResource(R.drawable.bird_flying_anim);
        AnimationDrawable animationDrawable = (AnimationDrawable) mBirdImageView.getDrawable();
        animationDrawable.start();
    }

    private boolean isChatHeadServiceRunning() {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (HeadService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void stopChatHeadService() {
        if (isChatHeadServiceRunning()) {
            getContext().stopService(new Intent(getContext(), HeadService.class));
        }
    }

    private void startChatHeadService() {
        if (Build.VERSION.SDK_INT >= 23) {
            //Toast.makeText(getContext(), R.string.startup_permit_system_alarm, Toast.LENGTH_SHORT).show();
            if (!Settings.canDrawOverlays(getContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getContext().getPackageName()));
                startActivityForResult(intent, 10);
                Log.d(TAG, "No permission for drawing on screen, prompt one.");
            } else {
                //Toast.makeText(getContext(), R.string.headservice_how_to_stop, Toast.LENGTH_SHORT).show();
                getContext().startService(new Intent(getContext(), HeadService.class));
                //returnHomeScreen();
            }
        } else {
            Log.d(TAG, "Permission granted, starting service.");
            Toast.makeText(getContext(), R.string.headservice_how_to_stop, Toast.LENGTH_SHORT).show();
            getContext().startService(new Intent(getContext(), HeadService.class));
            //returnHomeScreen();
        }
    }

    private void startPreferenceActivity() {
        Intent intent = new Intent();
        intent.setClass(this.getActivity(), AppPreferenceActivity.class);
        startActivity(intent);
    }

    private void returnHomeScreen() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    public String getVersionName(Context context){
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(),0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

}
