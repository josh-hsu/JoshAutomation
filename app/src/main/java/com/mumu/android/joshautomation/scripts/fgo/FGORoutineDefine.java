/*
 * FGORoutineDefine
 * This class has been deprecated.
 * You should update your point in fgo_definitions.xml and use DefinitionLoader to load your point
 */
package com.mumu.android.joshautomation.scripts.fgo;

import com.mumu.libjoshgame.ScreenColor;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.ArrayList;

@Deprecated
class FGORoutineDefine {


    static ScreenPoint pointIntroPage = new ScreenPoint(0x44,0x44,0x75,0xFF,824,1035,ScreenPoint.SO_Portrait);

    //intro
    static ScreenPoint pointSkipDialog = new ScreenPoint(0xff,0xff,0xff,0xff,982,1743,ScreenPoint.SO_Portrait);
    static ScreenPoint pointSkipConfirm = new ScreenPoint(0xDA,0xDA,0xDB,0xff,233,1166,ScreenPoint.SO_Portrait);
    static ScreenPoint pointSkipCancel = new ScreenPoint(0xD3,0xD4,0xD4,0xff,232,807,ScreenPoint.SO_Portrait);

    static ScreenPoint pointCloseButton = new ScreenPoint(45,61,108,0xff,156,63,ScreenPoint.SO_Landscape);
    static ScreenPoint pointLoopBattleStage = new ScreenPoint(0xff,0xff,0xff,0xff,718,1420,ScreenPoint.SO_Portrait);

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

    // skill and royal
    static private ScreenCoord cardSkill1 = new ScreenCoord(107 , 870, ScreenPoint.SO_Landscape);
    static private ScreenCoord cardSkill2 = new ScreenCoord(250 , 870, ScreenPoint.SO_Landscape);
    static private ScreenCoord cardSkill3 = new ScreenCoord(400 , 870, ScreenPoint.SO_Landscape);
    static private ScreenCoord cardSkill4 = new ScreenCoord(570 , 870, ScreenPoint.SO_Landscape);
    static private ScreenCoord cardSkill5 = new ScreenCoord(720 , 870, ScreenPoint.SO_Landscape);
    static private ScreenCoord cardSkill6 = new ScreenCoord(860 , 870, ScreenPoint.SO_Landscape);
    static private ScreenCoord cardSkill7 = new ScreenCoord(1050, 870, ScreenPoint.SO_Landscape);
    static private ScreenCoord cardSkill8 = new ScreenCoord(1200, 870, ScreenPoint.SO_Landscape);
    static private ScreenCoord cardSkill9 = new ScreenCoord(1350, 870, ScreenPoint.SO_Landscape);
    static ArrayList<ScreenCoord> cardSkills = new ArrayList<ScreenCoord>() {{
        add(cardSkill1);add(cardSkill2);add(cardSkill3);add(cardSkill4);add(cardSkill5);
        add(cardSkill6);add(cardSkill7);add(cardSkill8);add(cardSkill9);
    }};

    static private ScreenCoord cardRoyal1 = new ScreenCoord(620 , 289, ScreenPoint.SO_Landscape);
    static private ScreenCoord cardRoyal2 = new ScreenCoord(976 , 293, ScreenPoint.SO_Landscape);
    static private ScreenCoord cardRoyal3 = new ScreenCoord(1320, 289, ScreenPoint.SO_Landscape);
    static ArrayList<ScreenCoord> cardRoyals = new ArrayList<ScreenCoord>() {{
        add(cardRoyal1);add(cardRoyal2);add(cardRoyal3);
    }};

    static ScreenPoint inSelectTarget = new ScreenPoint(0xD4, 0xD4, 0xD4, 0xFF, 859, 1625, ScreenPoint.SO_Portrait);
    static private ScreenCoord cardTarget1 = new ScreenCoord(434 , 492, ScreenPoint.SO_Portrait);
    static private ScreenCoord cardTarget2 = new ScreenCoord(355 , 945, ScreenPoint.SO_Portrait);
    static private ScreenCoord cardTarget3 = new ScreenCoord(430, 1418, ScreenPoint.SO_Portrait);
    static ArrayList<ScreenCoord> cardTargets = new ArrayList<ScreenCoord>() {{
        add(cardTarget1);add(cardTarget2);add(cardTarget3);
    }};

    static ScreenPoint masterSkillButton = new ScreenPoint(0x18, 0x31, 0x5A, 0xFF, 615, 1772, ScreenPoint.SO_Portrait);
    static private ScreenCoord masterSkill1 = new ScreenCoord(600, 1354, ScreenPoint.SO_Portrait);
    static private ScreenCoord masterSkill2 = new ScreenCoord(600, 1495, ScreenPoint.SO_Portrait);
    static private ScreenCoord masterSkill3 = new ScreenCoord(600, 1620, ScreenPoint.SO_Portrait);
    static ArrayList<ScreenCoord> masterSkills = new ArrayList<ScreenCoord>() {{
        add(masterSkill1);add(masterSkill2);add(masterSkill3);
    }};

    static ScreenPoint inChangingServant = new ScreenPoint(0x14, 0x1F, 0x56, 0xFF, 131, 946, ScreenPoint.SO_Portrait); //also confirm button
    static private ScreenCoord changeServant1 = new ScreenCoord(550, 205, ScreenPoint.SO_Portrait);
    static private ScreenCoord changeServant2 = new ScreenCoord(550, 502, ScreenPoint.SO_Portrait);
    static private ScreenCoord changeServant3 = new ScreenCoord(550, 819, ScreenPoint.SO_Portrait);
    static private ScreenCoord changeServant4 = new ScreenCoord(550, 1115, ScreenPoint.SO_Portrait);
    static private ScreenCoord changeServant5 = new ScreenCoord(550, 1420, ScreenPoint.SO_Portrait);
    static private ScreenCoord changeServant6 = new ScreenCoord(550, 1700, ScreenPoint.SO_Portrait);
    static ArrayList<ScreenCoord> changeServants = new ArrayList<ScreenCoord>() {{
        add(changeServant1);add(changeServant2);add(changeServant3);
        add(changeServant4);add(changeServant5);add(changeServant6);
    }};

    // NP 100% detect
    static private ScreenPoint char100NPChar1 = new ScreenPoint(0xFF,0xFF,0xFF,0xFF,362 , 1000, ScreenPoint.SO_Landscape);
    static private ScreenPoint char100NPChar2 = new ScreenPoint(0xFF,0xFF,0xFF,0xFF,838 , 1000, ScreenPoint.SO_Landscape);
    static private ScreenPoint char100NPChar3 = new ScreenPoint(0xFF,0xFF,0xFF,0xFF,1317, 1000, ScreenPoint.SO_Landscape);
    static ArrayList<ScreenPoint> char100NPChars = new ArrayList<ScreenPoint>() {{
        add(char100NPChar1);add(char100NPChar2);add(char100NPChar3);
    }};

    // Stage detect
    static private ScreenPoint battleStage1 = new ScreenPoint(204,204,204,0xFF,1304, 29, ScreenPoint.SO_Landscape);
    static private ScreenPoint battleStage2 = new ScreenPoint(240,240,240,0xFF,1318, 49, ScreenPoint.SO_Landscape);
    static private ScreenPoint battleStage3 = new ScreenPoint(211,211,211,0xFF,1304, 49, ScreenPoint.SO_Landscape);
    static ArrayList<ScreenPoint> battleStages = new ArrayList<ScreenPoint>() {{
        add(battleStage1);add(battleStage2);add(battleStage3);
    }};

    // home screen
    static ScreenPoint pointHomeGiftBox = new ScreenPoint(229,64,39,0xff,646,1013,ScreenPoint.SO_Landscape);
    static ScreenPoint pointHomeOSiRaSe = new ScreenPoint(0,0,4,0xff,219,78,ScreenPoint.SO_Landscape);
    static ScreenPoint pointHomeApAdd = new ScreenPoint(251,249,246,0xff,380,979,ScreenPoint.SO_Landscape);
    static ScreenPoint pointHomeApAddV2 = new ScreenPoint(10,18,48,0xff,197,65,ScreenPoint.SO_Portrait);
    static ScreenPoint pointMenuDownButton = new ScreenPoint(0x31,0x39,0x65,0xff,53,1772,ScreenPoint.SO_Portrait);

    // NEXT button location
    static ScreenCoord pointRightNextStart = new ScreenCoord(1640, 156, ScreenPoint.SO_Landscape);
    static ScreenCoord pointRightNextEnd = new ScreenCoord(1640, 821, ScreenPoint.SO_Landscape);
    static private ScreenPoint pointRightNextPoint1 = new ScreenPoint(253,223,106,0xff,1640,184,ScreenPoint.SO_Landscape);
    static private ScreenPoint pointRightNextPoint3 = new ScreenPoint(255,223,103,0xff,1712,184,ScreenPoint.SO_Landscape);
    static private ScreenPoint pointRightNextPoint4 = new ScreenPoint(255,223,104,0xff,1750,184,ScreenPoint.SO_Landscape);
    static ArrayList<ScreenPoint> pointRightNextPoints = new ArrayList<ScreenPoint>() {{
        add(pointRightNextPoint1);
        add(pointRightNextPoint3);add(pointRightNextPoint4);}};

    static ScreenCoord pointLeftNextStart = new ScreenCoord(1141, 156, ScreenPoint.SO_Landscape);
    static ScreenCoord pointLeftNextEnd = new ScreenCoord(1141, 821, ScreenPoint.SO_Landscape);
    static private ScreenPoint pointLeftNextPoint1 = new ScreenPoint(253,223,105,0xff,1141,657,ScreenPoint.SO_Landscape);
    static private ScreenPoint pointLeftNextPoint3 = new ScreenPoint(254,221,92,0xff,1213,657,ScreenPoint.SO_Landscape);
    static private ScreenPoint pointLeftNextPoint4 = new ScreenPoint(255,223,104,0xff,1249,657,ScreenPoint.SO_Landscape);
    static ArrayList<ScreenPoint> pointLeftNextPoints = new ArrayList<ScreenPoint>() {{
        add(pointLeftNextPoint1);add(pointLeftNextPoint3);add(pointLeftNextPoint4);}};

    static ScreenCoord pointSubStageNextStart = new ScreenCoord(1036, 138, ScreenPoint.SO_Landscape);
    static ScreenCoord pointSubStageNextEnd = new ScreenCoord(1036, 1049, ScreenPoint.SO_Landscape);
    static private ScreenPoint pointSubStageNextPoint1 = new ScreenPoint(252,221,109,0xff,1036,166,ScreenPoint.SO_Landscape);
    static private ScreenPoint pointSubStageNextPoint3 = new ScreenPoint(255,223,102,0xff,1109,166,ScreenPoint.SO_Landscape);
    static private ScreenPoint pointSubStageNextPoint4 = new ScreenPoint(255,223,104,0xff,1145,166,ScreenPoint.SO_Landscape);
    static ArrayList<ScreenPoint> pointSubStageNextPoints = new ArrayList<ScreenPoint>() {{
        add(pointSubStageNextPoint1);add(pointSubStageNextPoint3);add(pointSubStageNextPoint4);}};

    static ScreenCoord pointMapNextStart = new ScreenCoord(909, 156, ScreenPoint.SO_Landscape);
    static ScreenCoord pointMapNextEnd = new ScreenCoord(909, 821, ScreenPoint.SO_Landscape);
    static private ScreenPoint pointMapNextPoint1 = new ScreenPoint(253,223,106,0xff,909,184,ScreenPoint.SO_Landscape);
    static private ScreenPoint pointMapNextPoint3 = new ScreenPoint(255,223,103,0xff,981,184,ScreenPoint.SO_Landscape);
    static private ScreenPoint pointMapNextPoint4 = new ScreenPoint(255,223,104,0xff,1019,184,ScreenPoint.SO_Landscape);
    static ArrayList<ScreenPoint> pointMapNextPoints = new ArrayList<ScreenPoint>() {{
        add(pointMapNextPoint1);add(pointMapNextPoint3);add(pointMapNextPoint4);}};

    static ScreenCoord pointSwipeStart = new ScreenCoord(1440,700,ScreenPoint.SO_Landscape);
    static ScreenCoord pointSwipeEnd = new ScreenCoord(1440,508,ScreenPoint.SO_Landscape);

    //Battle Pre-setup
    static ScreenCoord pointFriendSelect = new ScreenCoord(979,746,ScreenPoint.SO_Landscape);
    static ScreenCoord pointFriendSelectDefault = new ScreenCoord(624,787,ScreenPoint.SO_Portrait);
    static ScreenPoint pointEnterStage = new ScreenPoint(37,47,75,0xff,1735,1010,ScreenPoint.SO_Landscape);

    //Friend Select
    static ScreenCoord pointFriendSupStart = new ScreenCoord(1558, 300,  ScreenPoint.SO_Landscape);
    static ScreenCoord pointFriendSupEnd   = new ScreenCoord(1558, 1000, ScreenPoint.SO_Landscape);
    static private ScreenPoint pointFriendSupPoint1 = new ScreenPoint(232,178,59,0xff,1558,713,ScreenPoint.SO_Landscape);
    static private ScreenPoint pointFriendSupPoint3 = new ScreenPoint(250,191,63,0xff,1589,713,ScreenPoint.SO_Landscape);
    static private ScreenPoint pointFriendSupPoint4 = new ScreenPoint(240,185,62,0xff,1609,713,ScreenPoint.SO_Landscape);
    static ArrayList<ScreenPoint> pointFriendSupPoints = new ArrayList<ScreenPoint>() {{
        add(pointFriendSupPoint1);add(pointFriendSupPoint3);add(pointFriendSupPoint4);}};

    //Battle Die Detect
    static ScreenPoint pointBattleDieDetect  = new ScreenPoint(255,0  ,0  ,0xff,1586,131,ScreenPoint.SO_Landscape);
    static ScreenPoint pointBattleDieBackoff = new ScreenPoint(0  ,0  ,0  ,0xff, 492,475,ScreenPoint.SO_Landscape);
    static ScreenPoint pointBattleDieConfirm = new ScreenPoint(250,250,250,0xff,1301,543,ScreenPoint.SO_Landscape);
    static ScreenPoint pointBattleDieClose   = new ScreenPoint(217,217,217,0xff, 960,843,ScreenPoint.SO_Landscape);

    //Battle results
    static ScreenPoint pointBattleResult = new ScreenPoint(236,236,235,0xff,832,74,ScreenPoint.SO_Landscape);
    static ScreenPoint pointBattleNext = new ScreenPoint(211,211,211,0xff,1527,1018,ScreenPoint.SO_Landscape);
    static ScreenPoint pointQuestClearStone = new ScreenPoint(0xFF,0xCD,0x00,0xff,261,885,ScreenPoint.SO_Portrait);
    static ScreenPoint pointQuestClearCube = new ScreenPoint(0xFF,0xCD,0x00,0xff,265,1266,ScreenPoint.SO_Portrait);
    static ScreenPoint pointDenyFriend = new ScreenPoint(115,115,115,0xff,487,921,ScreenPoint.SO_Landscape);

    //BoxOpen
    static ScreenCoord pointBoxOpen = new ScreenCoord(625,640,ScreenPoint.SO_Landscape);
    static ScreenCoord pointBoxReset = new ScreenCoord(1700,375,ScreenPoint.SO_Landscape);
    static ScreenCoord pointBoxResetConfirm = new ScreenCoord(1270,820,ScreenPoint.SO_Landscape);
    static ScreenCoord pointBoxReseted = new ScreenCoord(1010,850,ScreenPoint.SO_Landscape);
}
