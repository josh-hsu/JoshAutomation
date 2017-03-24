package com.mumu.joshautomation.fgo;

import com.mumu.libjoshgame.ScreenColor;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.ArrayList;

//static ScreenPoint pointSkipDialog = new ScreenPoint(0x,0x,0x,0xff,,,ScreenPoint.SO_Portrait);

class FGORoutineDefine {
    static final int sCardBust = 0;
    static final int sCardArt = 1;
    static final int sCardQuick = 2;
    static final int sCardUnknown = 3;

    static ScreenPoint pointIntroPage = new ScreenPoint(0x44,0x44,0x75,0xFF,824,1035,ScreenPoint.SO_Portrait);

    //intro
    static ScreenPoint pointSkipDialog = new ScreenPoint(0xff,0xff,0xff,0xff,982,1743,ScreenPoint.SO_Portrait);
    static ScreenPoint pointSkipConfirm = new ScreenPoint(0xDA,0xDA,0xDB,0xff,233,1166,ScreenPoint.SO_Portrait);
    static ScreenPoint pointSkipCancel = new ScreenPoint(0xD3,0xD4,0xD4,0xff,232,807,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleResult = new ScreenPoint(0xEF,0xC6,0x2F,0xff,810,192,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleNext = new ScreenPoint(0xD7,0xD7,0xD7,0xff,72,1526,ScreenPoint.SO_Portrait);

    //battle cards
    static ScreenPoint pointBattleButton = new ScreenPoint(0x01,0xCE,0xF0,0xFF,218,1699,ScreenPoint.SO_Portrait);

    static private ScreenCoord card1start = new ScreenCoord(80  , 770, ScreenPoint.SO_Landscape);
    static private ScreenCoord card1end   = new ScreenCoord(312 , 920, ScreenPoint.SO_Landscape);
    static private ScreenCoord card2start = new ScreenCoord(460 , 770, ScreenPoint.SO_Landscape);
    static private ScreenCoord card2end   = new ScreenCoord(684 , 920, ScreenPoint.SO_Landscape);
    static private ScreenCoord card3start = new ScreenCoord(842 , 770, ScreenPoint.SO_Landscape);
    static private ScreenCoord card3end   = new ScreenCoord(1074, 920, ScreenPoint.SO_Landscape);
    static private ScreenCoord card4start = new ScreenCoord(1230, 770, ScreenPoint.SO_Landscape);
    static private ScreenCoord card4end   = new ScreenCoord(1470, 920, ScreenPoint.SO_Landscape);
    static private ScreenCoord card5start = new ScreenCoord(1630, 770, ScreenPoint.SO_Landscape);
    static private ScreenCoord card5end   = new ScreenCoord(1845, 920, ScreenPoint.SO_Landscape);
    static ArrayList<ScreenCoord> cardPositionStart = new ArrayList<ScreenCoord>() {{
        add(card1start);add(card2start);add(card3start);add(card4start);add(card5start);}};
    static ArrayList<ScreenCoord> cardPositionEnd = new ArrayList<ScreenCoord>() {{
        add(card1end);add(card2end);add(card3end);add(card4end);add(card5end);}};

    static private ScreenColor cardBlue1 = new ScreenColor(0,  61,  209, 0xff);
    static private ScreenColor cardBlue2 = new ScreenColor(28, 119, 255, 0xff);
    static private ScreenColor cardBlue3 = new ScreenColor(98, 222, 255, 0xff);
    static private ScreenColor cardBlue4 = new ScreenColor(64, 163, 255, 0xff);
    static ArrayList<ScreenColor> cardArt = new ArrayList<ScreenColor>() {{
        add(cardBlue1);add(cardBlue2);add(cardBlue3);add(cardBlue4);}};

    static private ScreenColor cardGreen1 = new ScreenColor( 2, 147, 12, 0xff);
    static private ScreenColor cardGreen2 = new ScreenColor(238, 254, 136, 0xff);
    static private ScreenColor cardGreen3 = new ScreenColor(48, 223, 30, 0xff);
    static private ScreenColor cardGreen4 = new ScreenColor(51, 233, 59, 0xff);
    static ArrayList<ScreenColor> cardQuick = new ArrayList<ScreenColor>() {{
        add(cardGreen1);add(cardGreen2);add(cardGreen3);add(cardGreen4);}};

    static private ScreenColor cardRed1 = new ScreenColor(255, 22, 6, 0xff);
    static private ScreenColor cardRed2 = new ScreenColor(254, 226, 67, 0xff);
    static private ScreenColor cardRed3 = new ScreenColor(250, 91, 28, 0xff);
    static private ScreenColor cardRed4 = new ScreenColor(253, 134, 39, 0xff);
    static ArrayList<ScreenColor> cardBurst = new ArrayList<ScreenColor>() {{
        add(cardRed1);add(cardRed2);add(cardRed3);add(cardRed4);}};
}
