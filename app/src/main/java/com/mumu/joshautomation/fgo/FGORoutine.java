package com.mumu.joshautomation.fgo;

import com.mumu.joshautomation.script.AutoJobEventListener;
import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenCoord;

import static com.mumu.joshautomation.fgo.FGORoutineDefine.*;

public class FGORoutine {
    private JoshGameLibrary mGL;
    private AutoJobEventListener mCallbacks;

    public FGORoutine(JoshGameLibrary gl, AutoJobEventListener el) {
        mGL = gl;
        mCallbacks = el;
    }

    private void sendMessage(String msg) {
        if (mCallbacks != null)
            mCallbacks.onEventReceived(msg, this);
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* =======================
     * Battle Card Checking
     * =======================
     */
    public int[] getCurrentCardPresent() {
        int ret[] = new int[5];

        for(int i = 0; i < 5; i++) {
            if (mGL.getCaptureService().findColorInRange(
                    cardPositionStart.get(i),
                    cardPositionEnd.get(i),
                    cardArt)) {
                ret[i] = sCardArt;
            } else if (mGL.getCaptureService().findColorInRange(
                    cardPositionStart.get(i),
                    cardPositionEnd.get(i),
                    cardBurst)) {
                ret[i] = sCardBust;
            } else if (mGL.getCaptureService().findColorInRange(
                    cardPositionStart.get(i),
                    cardPositionEnd.get(i),
                    cardQuick)) {
                ret[i] = sCardQuick;
            } else {
                ret[i] = sCardUnknown;
            }
        }

        return ret;
    }

    public boolean isCardValid(int[] cards) {
        for(int i: cards) {
            if (i == sCardUnknown)
                return false;
        }
        return true;
    }

    public String getCardName(int i) {
        switch (i) {
            case sCardArt:
                return "A";
            case sCardBust:
                return "B";
            case sCardQuick:
                return "Q";
            case sCardUnknown:
            default:
                return "U";
        }
    }

    public String getCardNameSeries(int[] series) {
        String cardInfo = "";
        for (int i : series) {
            if (i == sCardUnknown) {
                cardInfo = "Recognize failed";
                break;
            }
            cardInfo += getCardName(i);
        }

        return cardInfo;
    }

    public int[] getOptimizeDraw(int[] pattern) {
        int[] select = new int[3];
        int selected = 0;

        for(int i = 0; i < pattern.length; i++) {
            if (pattern[i] == sCardBust) {
                select[selected] = i;
                selected++;

                if(selected == 3)
                    return select;
            }
        }

        for(int i = 0; i < pattern.length; i++) {
            boolean alreadyIn = false;
            for(int j = 0; j < selected; j++) {
                if (select[j] == i)
                    alreadyIn = true;
            }

            if(!alreadyIn) {
                select[selected] = i;
                selected++;

                if (selected == 3)
                    return select;
            }
        }

        return null;
    }

    public void tapOnCard(int[] cardIndex) {
        for(int i : cardIndex) {
            ScreenCoord coord = ScreenCoord.getTwoPointCenter(cardPositionStart.get(i),
                    cardPositionEnd.get(i));
            mGL.getInputService().tapOnScreen(coord);
        }
    }

    /* =======================
     * Battle Info
     * =======================
     */
    public void battleRoutine(Thread kThread) {
        String cardInfo;
        int[] optimizedDraw, cardStatusNow;
        int maxTry = 20;
        int currentTry = 0;

        while(!mGL.getCaptureService().colorIs(pointBattleResult)) {
            sleep(500);
            sendMessage("在等Battle按鈕");
            currentTry = maxTry;

            mGL.getCaptureService().waitOnColor(pointBattleButton, 100, kThread);
            mGL.getInputService().tapOnScreen(pointBattleButton.coord);

            sendMessage("辨識卡片");
            cardStatusNow = getCurrentCardPresent();
            while (!isCardValid(cardStatusNow) && currentTry > 0) {
                cardStatusNow = getCurrentCardPresent();
                currentTry--;
            }

            if (isCardValid(cardStatusNow)) {
                cardInfo = getCardNameSeries(cardStatusNow);
                sendMessage(cardInfo);
            } else {
                sendMessage("卡片無法辨識");
                continue;
            }

            optimizedDraw = getOptimizeDraw(cardStatusNow);
            tapOnCard(optimizedDraw);
            sleep(8000);
        }

        while (!mGL.getCaptureService().colorIs(pointBattleNext)) {
            mGL.getInputService().tapOnScreen(pointBattleResult.coord);
            sleep(500);
        }
    }

    /* =======================
     * Story Info
     * =======================
     */
    public void waitForSkip(int maxTry, Thread kThread) {
        mGL.getCaptureService().waitOnColor(pointSkipDialog, maxTry, kThread);
    }


    /* =======================
     * Home Info
     * =======================
     */
    public void findNextAndClick() {
        sendMessage("尋找NEXT");
        ScreenCoord x = null;
        do {
            x = mGL.getCaptureService().findColorSegment(pointRightNextStart, pointRightNextEnd, pointRightNextPoints);
            sleep(1000);
            if (x == null) {
                mGL.getInputService().swipeOnScreen(pointSwipeStart, pointSwipeEnd);
            }
        } while (x == null);

        sendMessage("找到選單的NEXT");
        x.y += 100;
        mGL.getInputService().tapOnScreen(x);
        sleep(2000);

        sendMessage("找下一關");
        do {
            x = mGL.getCaptureService().findColorSegment(pointMapNextStart, pointMapNextEnd, pointMapNextPoints);
            sleep(1000);
        } while (x == null);

        sendMessage("找到下一關");
        x.y += 200;
        mGL.getInputService().tapOnScreen(x);
        sleep(2000);
    }
}
