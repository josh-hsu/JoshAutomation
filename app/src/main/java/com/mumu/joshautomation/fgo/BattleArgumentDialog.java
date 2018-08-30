package com.mumu.joshautomation.fgo;

import android.app.Activity;
import android.os.Bundle;

import com.mumu.joshautomation.R;
import com.mumu.libjoshgame.Log;

public class BattleArgumentDialog extends Activity {
    private static final String TAG = "BattleArgumentDialog";
    public static String bundlePreferenceKey = "preferenceKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.battle_argument_dialog);
        Log.d(TAG, "Get preference key: " + bundle.getString(bundlePreferenceKey));
    }


    private void onStageChanged(int currentStage) {

    }

    private void onSkillSelected(int skill) {

    }

    private void onChangeServant() {

    }

    private void onTargetSelected(int target, int countRequest) {

    }

}
