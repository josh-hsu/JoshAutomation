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

    //battle results
    static ScreenPoint pointBattleResult = new ScreenPoint(0xEF,0xC6,0x2F,0xff,810,192,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleNext = new ScreenPoint(0xD7,0xD7,0xD7,0xff,72,1526,ScreenPoint.SO_Portrait);
    static ScreenPoint pointQuestClear = new ScreenPoint(0xFF,0xCD,0x00,0xff,261,885,ScreenPoint.SO_Portrait);

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

    // home screen
    static ScreenPoint pointHomeAddAP = new ScreenPoint(0xD2,0x99,0x61,0xff,25,258,ScreenPoint.SO_Portrait);
    static ScreenPoint pointMenuDownButton = new ScreenPoint(0x31,0x39,0x65,0xff,53,1772,ScreenPoint.SO_Portrait);

    static ScreenCoord pointRightNextStart = new ScreenCoord(1640, 156, ScreenPoint.SO_Landscape);
    static ScreenCoord pointRightNextEnd = new ScreenCoord(1640, 821, ScreenPoint.SO_Landscape);
    static private ScreenPoint pointRightNextPoint1 = new ScreenPoint(253,223,106,0xff,1640,184,ScreenPoint.SO_Landscape);
    //static private ScreenPoint pointRightNextPoint2 = new ScreenPoint(255,255, 89,0xff,1668,184,ScreenPoint.SO_Landscape);
    static private ScreenPoint pointRightNextPoint3 = new ScreenPoint(255,223,103,0xff,1712,184,ScreenPoint.SO_Landscape);
    static private ScreenPoint pointRightNextPoint4 = new ScreenPoint(255,223,104,0xff,1750,184,ScreenPoint.SO_Landscape);
    static ArrayList<ScreenPoint> pointRightNextPoints = new ArrayList<ScreenPoint>() {{
        add(pointRightNextPoint1);/*add(pointRightNextPoint2);*/
        add(pointRightNextPoint3);add(pointRightNextPoint4);}};


}
