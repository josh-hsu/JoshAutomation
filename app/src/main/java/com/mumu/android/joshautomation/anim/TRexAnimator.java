package com.mumu.android.joshautomation.anim;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.mumu.android.joshautomation.R;

import java.util.Random;

public class TRexAnimator {
    private static final String TAG = "TRexAnimator";
    private ImageView mTRexView, mBirdView, mCloudView, mCactusView;
    private boolean mTriggerRunning = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    TRexAnimationTrigger mTrigger;

    public TRexAnimator(ImageView trex, ImageView bird, ImageView cloud, ImageView cactus) {
        mTRexView = trex;
        mBirdView = bird;
        mCloudView = cloud;
        mCactusView = cactus;
    }

    public void startMovie() {
        if (!mTriggerRunning) {
            mTrigger = new TRexAnimationTrigger();
            mTrigger.start();
            mTriggerRunning = true;
        }
    }

    public void stopMovie() {
        if (mTriggerRunning) {
            mTrigger.pause();
            mTrigger.interrupt();
            mTriggerRunning = false;
            mTrigger = null;
        }
    }

    private class TRexAnimationTrigger extends Thread {
        final int EVENT_DO_NOTHING = 0;
        final int EVENT_BIRD_FLYING = 1;
        final int EVENT_CLOUD_PRESENT = 2;
        final int EVENT_CACTUS_PRESENT = 3;
        final int EVENT_MAX = 4;
        int eventTriggerPeriodMs = 800;
        int birdFlyingPeriodMs = 4000;
        int cloudFlyingPeriodMs = 8000;

        boolean running = true;
        boolean birdFlying = false;
        boolean cloudFlying = false;

        public void pause() {
            running = false;
        }

        private void runTRexRun(boolean run) {
            if (run) {
                mTRexView.setImageResource(R.drawable.t_rex_running_anim);
                AnimationDrawable animationDrawable = (AnimationDrawable) mTRexView.getDrawable();
                animationDrawable.start();
            } else {
                mTRexView.setImageResource(R.mipmap.rex2);
            }
        }

        private void cloudFlyingOnce() {
            cloudFlying = true;
            mCloudView.setVisibility(View.VISIBLE);
            ObjectAnimator movingAnimation = ObjectAnimator.ofFloat(mCloudView, "translationX", -1080f);
            movingAnimation.setDuration(cloudFlyingPeriodMs);
            movingAnimation.start();
            movingAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mCloudView.animate().translationX(0).translationY(0);
                    mCloudView.setVisibility(View.INVISIBLE);
                    cloudFlying = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }

        private void birdFlyingOnce() {
            birdFlying = true;
            mBirdView.setVisibility(View.VISIBLE);
            mBirdView.setImageResource(R.drawable.bird_flying_anim);
            ObjectAnimator movingAnimation = ObjectAnimator.ofFloat(mBirdView, "translationX", -1080f);
            movingAnimation.setDuration(birdFlyingPeriodMs);

            AnimationDrawable animationDrawable = (AnimationDrawable) mBirdView.getDrawable();
            animationDrawable.start();
            movingAnimation.start();
            movingAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mBirdView.animate().translationX(0).translationY(0);
                    mBirdView.setVisibility(View.INVISIBLE);
                    birdFlying = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }

        private void eventTrigger(int event) {
            switch (event) {
                case EVENT_DO_NOTHING:
                    break;
                case EVENT_BIRD_FLYING:
                    if (!birdFlying) {
                        birdFlyingOnce();
                    }
                    break;
                case EVENT_CLOUD_PRESENT:
                    if (!cloudFlying)
                        cloudFlyingOnce();
                    break;
                case EVENT_CACTUS_PRESENT:
                    break;
                default:
                    Log.w(TAG, "Unknown event " + event);
            }
        }

        public void run() {
            running = true;

            // start running
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    runTRexRun(true);
                }
            });

            while(running) {
                try {
                    sleep(eventTriggerPeriodMs);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Thread interrupted");
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Random rand = new Random();
                        int randomEvent = rand.nextInt(EVENT_MAX);
                        eventTrigger(randomEvent);
                    }
                });
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    runTRexRun(false);
                }
            });
        }
    }
}
