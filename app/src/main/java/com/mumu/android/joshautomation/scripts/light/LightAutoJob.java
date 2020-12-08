package com.mumu.android.joshautomation.scripts.light;

import android.util.Log;

import com.mumu.android.joshautomation.R;
import com.mumu.android.joshautomation.autojob.SeeAnyPressAnyJob;
import com.mumu.libjoshgame.ScreenPoint;

public class LightAutoJob extends SeeAnyPressAnyJob {
    private final static String JOB_NAME = "LightAutoReplay";
    private final static int RAW_DEF_XML_RES_ID = R.raw.light_definitions;
    private final static String DEF_XML_NAME = "light_definitions.xml";
    private final static int MAIN_ORIENTATION = ScreenPoint.SO_Landscape;
    private final static int TIMEOUT_SEC = 30 * 60;

    public LightAutoJob() {
        super(JOB_NAME, RAW_DEF_XML_RES_ID, DEF_XML_NAME, MAIN_ORIENTATION, TIMEOUT_SEC);
    }
}
