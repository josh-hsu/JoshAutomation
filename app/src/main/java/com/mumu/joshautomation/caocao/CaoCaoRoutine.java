package com.mumu.joshautomation.caocao;

import android.util.Log;

import com.mumu.joshautomation.AppPreferenceValue;
import com.mumu.joshautomation.screencapture.PointSelectionActivity;
import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;

import static com.mumu.joshautomation.caocao.CaoCaoDefine.*;

/**
 * CaoCaoRoutine
 * Responsible of every little things
 */

public class CaoCaoRoutine {
    private static final String TAG = "CaoCaoRoutine";
    private JoshGameLibrary mGL;
    private AutoJobEventListener mCallbacks;

    public boolean mDevMode = true;

    CaoCaoRoutine(JoshGameLibrary gl, AutoJobEventListener el) {
        mGL = gl;
        mCallbacks = el;
    }

    private void sendMessage(String msg) {
        Log.d(TAG, "MSG: " + msg);
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

    public boolean loginAsGuest() throws InterruptedException {
        if (mGL.getCaptureService().waitOnColor(pointLoginGuest, 20) < 0) {
            sendMessage("不是登入使用者畫面");
            return false;
        }

        mGL.getInputService().tapOnScreen(pointLoginGuest.coord);

        if (mGL.getCaptureService().waitOnColor(pointAgreementDetect, 60) < 0) {
            sendMessage("同意畫面未出現");
            return false;
        }

        mGL.getInputService().tapOnScreen(pointAgreementFirst.coord);
        sleep(100);
        mGL.getInputService().tapOnScreen(pointAgreementSecond.coord);
        sleep(100);
        mGL.getInputService().tapOnScreen(pointAgreementAll.coord);

        if (mGL.getCaptureService().waitOnColor(pointAdultConfirm, 60) < 0) {
            sendMessage("Adult未出現");
            return false;
        }
        sleep(4000);
        mGL.getInputService().tapOnScreen(pointAdultConfirm.coord);

        return true;
    }

    public boolean createCountry() throws InterruptedException {
        if (mGL.getCaptureService().waitOnColor(pointCountryNameEnter, 60) < 0) {
            sendMessage("建國未出現");
            return false;
        }
        mGL.getInputService().tapOnScreen(pointCountryNameEnter.coord);
        sleep(2000);

        if (mGL.getCaptureService().waitOnColor(pointCountryNameInputDone, 60) < 0) {
            sendMessage("輸入未出現");
            return false;
        }
        sleep(1000);

        mGL.getInputService().inputText("ynn" + ((int)(Math.random()*1000)) + "new" + ((int)(Math.random()*1000)));
        sleep(200);
        mGL.getInputService().tapOnScreen(pointCountryNameInputDone.coord);
        sleep(500);
        mGL.getInputService().tapOnScreen(pointCountryRegister.coord);
        sleep(1000);
        return true;
    }

    public boolean firstBattleRoutine() throws InterruptedException {

        //current wait for user finish first battle
        if (mGL.getCaptureService().waitOnColor(pointEventReward, 1200) < 0) {
            sendMessage("初戰未能完成");
            return false;
        } else if (mDevMode) {
            sendMessage("初戰完成");
            return true;
        }

        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointBattleSkip,
                pointBattleCharShouldMove, 100, 50) < 0) {
            sendMessage("戰鬥未開始?");
            return false;
        }

        mGL.getInputService().tapOnScreen(pointBattleCharShouldMove.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointBattleCharShouldMove.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointBattleCharMoveTarget.coord);

        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointBattleSkip,
                pointBattleCharShouldMove, 100, 50) < 0) {
            sendMessage("戰鬥未開始?");
            return false;
        }

        mGL.getInputService().tapOnScreen(pointBattleCharShouldMove.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointBattleCharMoveTarget.coord);

        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointBattleSkip,
                pointBattleCharShouldMove, 100, 50) < 0) {
            sendMessage("戰鬥未開始?");
            return false;
        }

        mGL.getInputService().tapOnScreen(pointBattleCharShouldMove.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointBattleCharMoveTarget.coord);

        if (mGL.getCaptureService().waitOnColor(pointEventReward, 300) < 0) {
            sendMessage("初戰未能完成");
            return false;
        }
        sendMessage("初戰完成");

        return true;
    }

    public boolean process1st() throws InterruptedException {
        if (!mGL.getCaptureService().colorIs(pointEventReward)) {
            sendMessage("非處理一起始");
            return false;
        }
        sendMessage("處理一開始");
        sleep(3000);

        mGL.getInputService().tapOnScreen(pointEventReward.coord);
        mGL.getInputService().tapOnScreen(pointEventReward.coord);
        sleep(3000);

        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointRegisterMan, 100, 30) < 0) {
            sendMessage("登用未出現");
            return false;
        }
        sleep(100);
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointRegisterMan, pointRegisterManConfirm, 100, 30) < 0) {
            sendMessage("確認燈用未出現");
            return false;
        }
        sleep(500);
        mGL.getInputService().tapOnScreen(pointRegisterManConfirm.coord);

        //wait for back
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointExitTownHint, 100, 30) < 0) {
            sendMessage("離開未出現");
            return false;
        }
        sleep(100);
        if (mGL.getInputService().tapOnScreenUntilColorChanged(pointExitTown, 100, 30) < 0) {
            sendMessage("離開按不掉");
            return false;
        }

        //wait for tax
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointTeachShowFood, 100, 280) < 0) {
            sendMessage("教學未出現");
            return false;
        }
        sleep(4000);
        mGL.getInputService().tapOnScreen(pointRetrieveFood.coord);
        mGL.getInputService().tapOnScreen(pointRetrieveFood.coord);
        mGL.getInputService().tapOnScreen(pointRetrieveFood.coord);

        //wait for manage
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointTeachShowManage, 100, 130) < 0) {
            sendMessage("管理未出現");
            return false;
        }
        sleep(100);
        mGL.getInputService().tapOnScreen(pointManage.coord);

        //wait for man
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointTeachShowManMap, 100, 130) < 0) {
            sendMessage("人才未出現");
            return false;
        }
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointManMap.coord);

        //wait for select first man
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointTeachShowSummonMan, 100, 130) < 0) {
            sendMessage("召喚人未出現");
            return false;
        }
        sleep(100);

        if (mGL.getInputService().tapOnScreenUntilColorChanged(pointSummonManMap, 100, 130) < 0) {
            sendMessage("召喚人未出現");
            return false;
        }
        sleep(100);

        if (mGL.getInputService().tapOnScreenUntilColorChanged(pointSummonMan, 100, 130) < 0) {
            sendMessage("召喚人未出現");
            return false;
        }
        sleep(1000);

        if (mGL.getInputService().tapOnScreenUntilColorChanged(pointSummonManRegister, 100, 130) < 0) {
            sendMessage("召喚人未出現");
            return false;
        }
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointSummonManRegister.coord);

        sleep(4000);
        mGL.getInputService().tapOnScreen(pointRegisterManConfirm.coord);
        mGL.getInputService().tapOnScreen(pointRegisterManConfirm.coord);
        sleep(1000);

        mGL.getInputService().tapOnScreen(pointCenter.coord);
        sleep(500);
        mGL.getInputService().tapOnScreen(pointCenter.coord);
        sleep(500);
        mGL.getInputService().tapOnScreen(pointCenter.coord);
        sleep(500);
        mGL.getInputService().tapOnScreen(pointSummonManSecond.coord);
        sleep(1000);
        if (!mGL.getCaptureService().colorIs(pointSummonManRegister)) {
            sendMessage("沒點到人");
            return false;
        }
        mGL.getInputService().tapOnScreen(pointSummonManRegister.coord);
        mGL.getInputService().tapOnScreen(pointSummonManRegister.coord);
        sleep(4000);
        mGL.getInputService().tapOnScreen(pointRegisterManConfirm.coord);
        mGL.getInputService().tapOnScreen(pointRegisterManConfirm.coord);
        sleep(1000);

        if (!mGL.getCaptureService().colorIs(pointSummonExit)) {
            sendMessage("召喚結束未出現");
            return false;
        }
        sleep(500);
        mGL.getInputService().tapOnScreen(pointSummonExit.coord);

        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointBattleEnterLinZhi, 100, 60) < 0) {
            sendMessage("臨淄未出現");
            return false;
        }
        sleep(100);
        mGL.getInputService().tapOnScreen(pointBattleEnterLinZhi.coord);
        sleep(500);
        mGL.getInputService().tapOnScreen(pointBattleEnterLinZhiConfirm.coord);

        //wait dialog end
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointPreBattleDialogEndDetect, 100, 270) < 0) {
            sendMessage("登用未出現");
            return false;
        }
        sleep(100);
        mGL.getInputService().tapOnScreen(pointPreBattleEnter.coord);

        //wait auto
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointAutoArrange, 100, 270) < 0) {
            sendMessage("自動未出現");
            return false;
        }
        sleep(1000);
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointAutoArrange, 100, 270) < 0) {
            sendMessage("自動未出現");
            return false;
        }
        mGL.getInputService().tapOnScreen(pointAutoArrange.coord);
        sleep(500);
        mGL.getInputService().tapOnScreen(pointGoBattle.coord);
        sleep(2000);
        mGL.getInputService().tapOnScreen(pointGoBattleConfirm.coord);
        sleep(2000);
        mGL.getInputService().tapOnScreen(pointGoBattleConfirm.coord);
        sleep(4000);

        return true;
    }

    public boolean secondBattleRoutine() throws InterruptedException {
        //wait auto
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointSecondBattleAsk, 100, 270) < 0) {
            sendMessage("自動未出現");
            return false;
        }
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointSecondBattleAsk.coord);

        //wait auto
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointBattleAuto, 100, 270) < 0) {
            sendMessage("自動未出現");
            return false;
        }
        sleep(4000);
        mGL.getInputService().tapOnScreen(pointBattleSpeedUp.coord);
        mGL.getInputService().tapOnScreen(pointBattleRapid.coord);
        mGL.getInputService().tapOnScreen(pointBattleRapidConfirm.coord);
        sleep(3000);
        mGL.getInputService().tapOnScreen(pointBattleAuto.coord);

        //wait auto
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointBattleClose, 100, 270) < 0) {
            sendMessage("自動未出現");
            return false;
        }
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointBattleClose.coord);

        return true;
    }

    public boolean process2nd() throws InterruptedException {
        //wait for tax
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointManageQuestTeach, 100, 70) < 0) {
            sendMessage("管理教學未出現");
            return false;
        }
        sleep(1000);
        if (mGL.getInputService().tapOnScreenUntilColorChanged(pointManageButton, 100, 70) < 0) {
            sendMessage("管理教學未出現");
            return false;
        }
        mGL.getInputService().tapOnScreen(pointManageQuest.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointManageQuest.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointManageQuest.coord);

        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointManageQuestSkip, 100, 70) < 0) {
            sendMessage("SKIP未出現");
            return false;
        }
        sleep(1000);
        if (mGL.getInputService().tapOnScreenUntilColorChanged(pointManageQuestSkip, 100, 170) < 0) {
            sendMessage("SKIP點不掉");
            return false;
        }

        //wait duchang

        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointBattleEnterDuCang, 200, 160) < 0) {
            sendMessage("都昌未出現");
            return false;
        }
        sleep(100);
        mGL.getInputService().tapOnScreen(pointBattleEnterDuCang.coord);
        sleep(500);
        mGL.getInputService().tapOnScreen(pointBattleEnterLinZhiConfirm.coord);

        //wait auto
        int[] ambRange = new int[] {0x1E, 0x1E, 0x1E};
        mGL.setAmbiguousRange(ambRange);
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointAutoArrange2, 100, 70) < 0) {
            sendMessage("自動未出現");
            return false;
        }
        ambRange = new int[] {0x0E, 0x0E, 0x0E};
        mGL.setAmbiguousRange(ambRange);
        sleep(4000);
        mGL.getInputService().tapOnScreen(pointAutoArrange2.coord);
        sleep(500);
        mGL.getInputService().tapOnScreen(pointGoBattle.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointGoBattleConfirm.coord);
        sleep(4000);

        return true;
    }

    public boolean thirdBattleRoutine() throws InterruptedException {
        //wait auto
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointBattleAuto, 100, 170) < 0) {
            sendMessage("自動未出現");
            return false;
        }
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointBattleRapid.coord);
        mGL.getInputService().tapOnScreen(pointBattleRapidConfirm.coord);
        sleep(3000);
        mGL.getInputService().tapOnScreen(pointBattleAuto.coord);

        //wait auto
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointBattleClose, 100, 270) < 0) {
            sendMessage("自動未出現");
            return false;
        }
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointBattleClose.coord);

        return true;
    }

    public boolean process3rd() throws InterruptedException {
        //wait for manager
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointTeachShowManageCity, 100, 110) < 0) {
            sendMessage("管理未出現");
            return false;
        }
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointManageCity.coord);

        //wait for plant
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointPlantIt, 100, 110) < 0) {
            sendMessage("耕作未出現");
            return false;
        }
        if (mGL.getInputService().tapOnScreenUntilColorChanged(pointPlantIt, 100, 110) < 0) {
            sendMessage("耕作未出現");
            return false;
        }

        sleep(1000);
        mGL.getInputService().tapOnScreen(pointPlantGrowConfirm.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointPlantGrowConfirm.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointPlantGrowConfirm.coord);

        //wait for exit
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointPlantExit, 100, 110) < 0) {
            sendMessage("耕作未出現");
            return false;
        }
        sleep(2000);
        mGL.getInputService().tapOnScreen(pointPlantExit.coord);
        sleep(5000);

        //wait for changeYang
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointBattleEnterCangYang, 100, 60) < 0) {
            sendMessage("昌楊未出現");
            return false;
        }
        sleep(100);
        mGL.getInputService().tapOnScreen(pointBattleEnterCangYang.coord);
        sleep(500);
        mGL.getInputService().tapOnScreen(pointBattleEnterLinZhiConfirm.coord);

        //wait auto
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointAutoArrange, 100, 70) < 0) {
            sendMessage("自動未出現");
            return false;
        }
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointAutoArrange.coord);
        sleep(500);
        mGL.getInputService().tapOnScreen(pointGoBattle.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointGoBattleConfirm.coord);
        sleep(4000);

        return true;
    }

    public boolean fourthBattleRoutine() throws InterruptedException {
        //wait auto
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointTeachStrategy, 100, 70) < 0) {
            sendMessage("自動未出現");
            return false;
        }
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointTeachStrategy.coord);

        //wait auto
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointBattleAuto, 100, 70) < 0) {
            sendMessage("自動未出現");
            return false;
        }
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointBattleRapid.coord);
        mGL.getInputService().tapOnScreen(pointBattleRapidConfirm.coord);
        sleep(3000);
        mGL.getInputService().tapOnScreen(pointBattleAuto.coord);

        //wait auto
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointBattleClose, 100, 270) < 0) {
            sendMessage("關閉未出現");
            return false;
        }
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointBattleClose.coord);

        return true;
    }

    public boolean process4th() throws InterruptedException {
        //wait for manager
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointTeachShowManageZiNan, 100, 210) < 0) {
            sendMessage("管理未出現");
            return false;
        }

        if (mGL.getInputService().tapOnScreenUntilColorChanged(pointManageZiNan, 100, 110) < 0) {
            sendMessage("管理未出現");
            return false;
        }

        //wait for plant
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointTeachShowManageFood, 100, 110) < 0) {
            sendMessage("耕作未出現");
            return false;
        }
        sleep(1000);
        if (mGL.getInputService().tapOnScreenUntilColorChanged(pointManageFood, 100, 110) < 0) {
            sendMessage("管理未出現");
            return false;
        }

        sleep(2000);
        mGL.getInputService().tapOnScreen(pointPlantGrowConfirm.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointPlantGrowConfirm.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointManageFoodSkip.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointManageFoodSkip.coord);
        sleep(200);
        mGL.getInputService().tapOnScreen(pointManageFoodSkip.coord);
        sleep(200);
        mGL.getInputService().tapOnScreen(pointManageFoodSkip.coord);
        sleep(200);
        mGL.getInputService().tapOnScreen(pointManageFoodSkip.coord);
        sleep(200);
        mGL.getInputService().tapOnScreen(pointPlantExit.coord);

        //wait for exit
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointTeachNorthSea, 100, 110) < 0) {
            sendMessage("北海未出現");
            return false;
        }
        sleep(2000);
        mGL.getInputService().tapOnScreen(pointBattleEnterNorthSea.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointBattleEnterLinZhiConfirm.coord);

        //wait auto
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointAutoArrange, 100, 200) < 0) {
            sendMessage("自動未出現");
            return false;
        }
        sleep(4000);
        mGL.getInputService().tapOnScreen(pointAutoArrange.coord);
        sleep(500);
        mGL.getInputService().tapOnScreen(pointGoBattle.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointGoBattleConfirm.coord);
        sleep(4000);

        return true;
    }

    public boolean fifthBattleRoutine() throws InterruptedException {
        //wait auto
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointBattleAuto, 100, 70) < 0) {
            sendMessage("自動未出現");
            return false;
        }
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointBattleRapid.coord);
        mGL.getInputService().tapOnScreen(pointBattleRapidConfirm.coord);
        sleep(3000);
        mGL.getInputService().tapOnScreen(pointBattleAuto.coord);

        //wait auto
        if (mGL.getInputService().tapOnScreenUntilColorChangedTo(pointCenter, pointTeachMayerConfirm, 100, 600) < 0) {
            sendMessage("自動未出現");
            return false;
        }
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointTeachMayerConfirm.coord);
        mGL.getInputService().tapOnScreen(pointTeachMayerConfirm.coord);
        sleep(1000);
        mGL.getInputService().tapOnScreen(pointBattleClose.coord);

        return true;
    }

}
