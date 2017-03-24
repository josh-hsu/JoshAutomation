package com.mumu.joshautomation.script;

import com.mumu.libjoshgame.ScreenColor;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.ArrayList;

class AutoBattleJobDefine {
    static ScreenPoint pointIntroPage = new ScreenPoint(0x44,0x44,0x75,0xFF,824,1035,ScreenPoint.SO_Portrait);

    //battle cards
    static ScreenPoint pointTest = new ScreenPoint(98,204,255,255,226,905,ScreenPoint.SO_Landscape);

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
        add(cardBlue1);add(cardBlue2);/*add(cardBlue3);add(cardBlue4);*/}};

    static private ScreenColor cardGreen1 = new ScreenColor( 2, 147, 12, 0xff);
    static private ScreenColor cardGreen2 = new ScreenColor(238, 254, 136, 0xff);
    static private ScreenColor cardGreen3 = new ScreenColor(48, 223, 30, 0xff);
    static private ScreenColor cardGreen4 = new ScreenColor(51, 233, 59, 0xff);
    static ArrayList<ScreenColor> cardQuick = new ArrayList<ScreenColor>() {{
        add(cardGreen1);add(cardGreen2);/*add(cardGreen3);add(cardGreen4);*/}};

    static private ScreenColor cardRed1 = new ScreenColor(255, 22, 6, 0xff);
    static private ScreenColor cardRed2 = new ScreenColor(254, 226, 67, 0xff);
    static private ScreenColor cardRed3 = new ScreenColor(250, 91, 28, 0xff);
    static private ScreenColor cardRed4 = new ScreenColor(253, 134, 39, 0xff);
    static ArrayList<ScreenColor> cardBurst = new ArrayList<ScreenColor>() {{
        add(cardRed1);add(cardRed2);/*add(cardRed3);add(cardRed4);*/}};
}
