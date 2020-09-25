package com.mumu.android.joshautomation.scripts.konosuba;

import com.mumu.android.joshautomation.R;
import com.mumu.android.joshautomation.autojob.SeeAnyPressAnyJob;
import com.mumu.libjoshgame.ScreenPoint;

public class KonosubaAutoJob extends SeeAnyPressAnyJob {
    private final static String JOB_NAME = "KonosubaSeeAnyPressAny";
    private final static int RAW_DEF_XML_RES_ID = R.raw.konosuba_definitions;
    private final static String DEF_XML_NAME = "konosuba_definitions.xml";
    private final static int MAIN_ORIENTATION = ScreenPoint.SO_Landscape;
    private final static int TIMEOUT_SEC = 60;

    public KonosubaAutoJob() {
        super(JOB_NAME, RAW_DEF_XML_RES_ID, DEF_XML_NAME, MAIN_ORIENTATION, TIMEOUT_SEC);
    }
}
