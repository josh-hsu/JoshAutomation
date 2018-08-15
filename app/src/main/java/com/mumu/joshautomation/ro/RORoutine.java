package com.mumu.joshautomation.ro;

import android.support.annotation.IntegerRes;
import android.util.Log;

import com.mumu.joshautomation.AppPreferenceValue;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import static com.mumu.joshautomation.ro.RORoutineDefinition.*;

/**
 * RO Routine
 * RO game helper functions
 */

class RORoutine {
    private static final String TAG = "RORoutine";
    private JoshGameLibrary mGL;
    private AutoJobEventListener mCallbacks;

    public RORoutine(JoshGameLibrary gl, AutoJobEventListener el) {
        mGL = gl;
        mCallbacks = el;
    }

    private void sendMessage(String msg) {
        if (mCallbacks != null)
            mCallbacks.onEventReceived(msg, this);
    }

    private void sendMessageVerbose(String msg) {
        boolean verboseMode = AppPreferenceValue.getInstance().getPrefs().getBoolean("debugLogPref", false);
        if (verboseMode)
            sendMessage(msg);
    }

    private void sleep(int time) throws InterruptedException {
        Thread.sleep(time);
    }

    /* --------------
     * Supply routine
     * -------------- */
    private ScreenCoord getTargetGaugeCoord(float percent, int gauge) {
        if (gauge == GAUGE_TYPE_HP) {
            int length = (int)((pointHPEnd.x - pointHPStart.x) * percent) / 100;
            Log.d(TAG, "Calculate offset " + length);
            return new ScreenCoord(pointHPStart.x + length, pointHPStart.y, pointHPStart.orientation);
        } else if (gauge == GAUGE_TYPE_MP) {
            int length = (int)((pointMPEnd.x - pointMPStart.x) * percent) / 100;
            Log.d(TAG, "Calculate offset " + length);
            return new ScreenCoord(pointMPStart.x + length, pointMPStart.y, pointMPStart.orientation);
        } else {
            Log.d(TAG, "Unknown gauge type " + gauge);
        }

        return null;
    }

    boolean isMPLowerThan(float percent) throws InterruptedException {
        ScreenPoint point = new ScreenPoint(getTargetGaugeCoord(percent, GAUGE_TYPE_MP), pointMPColor);
        return !mGL.getCaptureService().colorIs(point);
    }

    boolean isHPLowerThan(float percent) throws InterruptedException {
        ScreenPoint point = new ScreenPoint(getTargetGaugeCoord(percent, GAUGE_TYPE_HP), pointHPColor);
        return !mGL.getCaptureService().colorIs(point);
    }

    public float getCurrentHPValue() {
        return 0.0f;
    }

    public float getCurrentMPValue() {
        return 0.0f;
    }

    void tapOnQuickItem(int index) {
        ScreenCoord itemCoord = pointItems.get(index);
        mGL.getInputService().tapOnScreen(itemCoord);
    }

    void tapOnSkill(int index) {
        ScreenCoord skillCoord = pointSkills.get(index);
        mGL.getInputService().tapOnScreen(skillCoord);
    }

    void checkBattleSupply() throws InterruptedException {
        // check on character HP and MP
        if (AppPreferenceValue.getInstance().getPrefs().getBoolean("roAutoHPPref", false)) {
            int hpTargetPercent = Integer.parseInt(AppPreferenceValue.getInstance().getPrefs().getString("roAutoHPValuePref", "50"));
            int hpTargetItem = Integer.parseInt(AppPreferenceValue.getInstance().getPrefs().getString("roAutoHPItemPref", "1"));
            if (isHPLowerThan((float)hpTargetPercent)) {
                tapOnQuickItem(hpTargetItem - 1);
            }
        }

        if (AppPreferenceValue.getInstance().getPrefs().getBoolean("roAutoMPPref", false)) {
            int mpTargetPercent = Integer.parseInt(AppPreferenceValue.getInstance().getPrefs().getString("roAutoMPValuePref", "50"));
            int mpTargetItem = Integer.parseInt(AppPreferenceValue.getInstance().getPrefs().getString("roAutoMPItemPref", "1"));
            if (isMPLowerThan((float)mpTargetPercent)) {
                tapOnQuickItem(mpTargetItem - 1);
            }
        }
    }
}
