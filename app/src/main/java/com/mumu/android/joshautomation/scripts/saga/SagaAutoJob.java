package com.mumu.android.joshautomation.scripts.saga;

import com.mumu.android.joshautomation.R;
import com.mumu.android.joshautomation.autojob.SeeAnyPressAnyJob;
import com.mumu.libjoshgame.ScreenPoint;

public class SagaAutoJob extends SeeAnyPressAnyJob {
    private final static String JOB_NAME = "SagaSeeAnyPressAny";
    private final static int RAW_DEF_XML_RES_ID = R.raw.saga_definitions;
    private final static String DEF_XML_NAME = "saga_definitions.xml";
    private final static int MAIN_ORIENTATION = ScreenPoint.SO_Portrait;
    private final static int TIMEOUT_SEC = 60;

    public SagaAutoJob() {
        super(JOB_NAME, RAW_DEF_XML_RES_ID, DEF_XML_NAME, MAIN_ORIENTATION, TIMEOUT_SEC);
    }
}
