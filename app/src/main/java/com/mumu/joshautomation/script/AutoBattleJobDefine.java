package com.mumu.joshautomation.script;

import com.mumu.libjoshgame.ScreenColor;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.ArrayList;

class AutoBattleJobDefine {
    static ScreenPoint pointIntroPage = new ScreenPoint(0x44,0x44,0x75,0xFF,824,1035,ScreenPoint.SO_Portrait);

    //battle cards
    static private ScreenCoord card1start = new ScreenCoord(88  , 745, ScreenPoint.SO_Landscape);
    static private ScreenCoord card1end   = new ScreenCoord(344 , 905, ScreenPoint.SO_Landscape);
    static private ScreenCoord card2start = new ScreenCoord(469 , 751, ScreenPoint.SO_Landscape);
    static private ScreenCoord card2end   = new ScreenCoord(680 , 902, ScreenPoint.SO_Landscape);
    static private ScreenCoord card3start = new ScreenCoord(863 , 760, ScreenPoint.SO_Landscape);
    static private ScreenCoord card3end   = new ScreenCoord(1061, 905, ScreenPoint.SO_Landscape);
    static private ScreenCoord card4start = new ScreenCoord(1252, 761, ScreenPoint.SO_Landscape);
    static private ScreenCoord card4end   = new ScreenCoord(1450, 909, ScreenPoint.SO_Landscape);
    static private ScreenCoord card5start = new ScreenCoord(1626, 749, ScreenPoint.SO_Landscape);
    static private ScreenCoord card5end   = new ScreenCoord(1850, 908, ScreenPoint.SO_Landscape);
    static ArrayList<ScreenCoord> cardPositionStart = new ArrayList<ScreenCoord>() {{
        add(card1start);add(card2start);add(card3start);add(card4start);add(card5start);}};
    static ArrayList<ScreenCoord> cardPositionEnd = new ArrayList<ScreenCoord>() {{
        add(card1end);add(card2end);add(card3end);add(card4end);add(card5end);}};

    static private ScreenColor cardBlue1 = new ScreenColor(0, 72, 229, 0xff);
    static private ScreenColor cardBlue2 = new ScreenColor(1, 90, 254, 0xff);
    static private ScreenColor cardBlue3 = new ScreenColor(20, 111, 255, 0xff);
    static private ScreenColor cardBlue4 = new ScreenColor(16, 99, 254, 0xff);
    static ArrayList<ScreenColor> cardArt = new ArrayList<ScreenColor>() {{
        add(cardBlue1);add(cardBlue2);add(cardBlue3);add(cardBlue4);}};

    static private ScreenColor cardGreen1 = new ScreenColor(44, 216, 19, 0xff);
    static private ScreenColor cardGreen2 = new ScreenColor(75, 239, 35, 0xff);
    static private ScreenColor cardGreen3 = new ScreenColor(63, 236, 54, 0xff);
    static private ScreenColor cardGreen4 = new ScreenColor(28, 184, 36, 0xff);
    static ArrayList<ScreenColor> cardQuick = new ArrayList<ScreenColor>() {{
        add(cardGreen1);add(cardGreen2);add(cardGreen3);add(cardGreen4);}};

    static private ScreenColor cardRed1 = new ScreenColor(255, 10, 2, 0xff);
    static private ScreenColor cardRed2 = new ScreenColor(254, 45, 7, 0xff);
    static private ScreenColor cardRed3 = new ScreenColor(255, 137, 35, 0xff);
    static private ScreenColor cardRed4 = new ScreenColor(253, 66, 31, 0xff);
    static ArrayList<ScreenColor> cardBurst = new ArrayList<ScreenColor>() {{
        add(cardRed1);add(cardRed2);add(cardRed3);add(cardRed4);}};
}
