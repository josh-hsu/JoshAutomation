package com.mumu.joshautomation.fgo;

import com.mumu.libjoshgame.JoshGameLibrary;
import com.mumu.libjoshgame.ScreenCoord;

import static com.mumu.joshautomation.fgo.FGORoutineDefine.*;

public class FGORoutine {
    private JoshGameLibrary mGL;

    public FGORoutine(JoshGameLibrary gl) {
        mGL = gl;
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
     * Story Info
     * =======================
     */
    public void waitForSkip(int maxTry, Thread kThread) {
        mGL.getCaptureService().waitOnColor(pointSkipDialog, maxTry, kThread);
    }
}
