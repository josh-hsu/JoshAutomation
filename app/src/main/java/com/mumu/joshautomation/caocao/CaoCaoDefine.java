package com.mumu.joshautomation.caocao;

import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

/**
 * CaoCaoDefine
 */

public class CaoCaoDefine {
    //
    //  For Flush Money
    //

    // determine if it's on main map by setting gear icon  (should wait)
    static ScreenPoint pointInTheMainMap = new ScreenPoint(0x68,0x51,0x47,0xff,1031,1680,ScreenPoint.SO_Portrait);
    // enter point of battle field (no color)
    static ScreenCoord pointEnterCenter = new ScreenCoord(545,916,ScreenPoint.SO_Portrait);
    // confirm enter window (should wait)
    static ScreenPoint pointDetectingButton = new ScreenPoint(0xb2,0x0,0x0,0xff,80,1693,ScreenPoint.SO_Portrait);
    // attack button (should wait)
    static ScreenPoint pointAttackButton = new ScreenPoint(0x66,0x70,0x60,0xff,89,1799,ScreenPoint.SO_Portrait);
    // attack fire (should wait), but tap two members first
    static ScreenPoint pointAttackFireButton = new ScreenPoint(0xba,0x8b,0x52,0xff,75,1589,ScreenPoint.SO_Portrait);
    static ScreenCoord pointMember1Select = new ScreenCoord(752,285,ScreenPoint.SO_Portrait);
    static ScreenCoord pointMember2Select = new ScreenCoord(740,451,ScreenPoint.SO_Portrait);
    // attack confirm 1 & 2
    static ScreenPoint pointAttackConfirmButton = new ScreenPoint(0x3f,0x20,0x00,0xff,320,1340,ScreenPoint.SO_Portrait);

    //keep tap
    static ScreenCoord pointBattleCenterTap = new ScreenCoord(869,1020,ScreenPoint.SO_Portrait);
    //until out battle show
    static ScreenPoint pointBattleOutButton = new ScreenPoint(0xfb,0xcc,0x3f,0xff,1011,94,ScreenPoint.SO_Portrait);
    //confirm exit battle
    static ScreenPoint pointBattleOutConfirmButton = new ScreenPoint(0x60,0x31,0x00,0xff,346,1342,ScreenPoint.SO_Portrait);
    //battle close
    static ScreenPoint pointBattleCloseButton = new ScreenPoint(0x17,0x3C,0x61,0xff,87,1828,ScreenPoint.SO_Portrait);



    //
    //  For Flush Account
    //

    // detect this point and touch it
    static ScreenPoint pointLoginGuest = new ScreenPoint(0x7,0x6,0x4,0xff,319,490,ScreenPoint.SO_Portrait);

    //wait for windows
    static ScreenPoint pointAgreementDetect = new ScreenPoint(0x4,0xB7,0xEC,0xff,379,927,ScreenPoint.SO_Portrait);
    static ScreenPoint pointAgreementFirst = new ScreenPoint(0xDE,0xDE,0xDE,0xff,624,565,ScreenPoint.SO_Portrait);
    static ScreenPoint pointAgreementSecond = new ScreenPoint(0xDF,0xDF,0xDF,0xff,498,567,ScreenPoint.SO_Portrait);
    //don't detect its color
    static ScreenPoint pointAgreementAll = new ScreenPoint(0xDF,0xDF,0xDF,0xff,344,1293,ScreenPoint.SO_Portrait);

    //wait for adult confirm
    static ScreenPoint pointAdultConfirm = new ScreenPoint(0x3B,0x1F,0x0,0xff,305,1347,ScreenPoint.SO_Portrait);

    //Google Game may show up
    static ScreenPoint pointCountryNameEnter = new ScreenPoint(0x6F,0x59,0x37,0xff,523,1086,ScreenPoint.SO_Portrait);
    //Enter name
    static ScreenPoint pointCountryNameInputDone = new ScreenPoint(0xD6,0xD7,0xD7,0xff,848,1699,ScreenPoint.SO_Portrait);
    //Register country
    static ScreenPoint pointCountryRegister = new ScreenPoint(0x3E,0x20,0x00,0xff,318,1150,ScreenPoint.SO_Portrait);

    //wait for first battle about 15 seconds

    static ScreenPoint pointBattleCharShouldMove = new ScreenPoint(0xE4,0xE4,0xE4,0xff,551,973,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleCharMoveTarget = new ScreenPoint(0x47,0x50,0x3E,0xff,504,1465,ScreenPoint.SO_Portrait);

    //end first battle close event reward window
    static ScreenPoint pointEventReward = new ScreenPoint(0xD4,0x86,0x07,0xff,863,1807,ScreenPoint.SO_Portrait);

    //keep touch center
    static ScreenPoint pointCenter = new ScreenPoint(0x0B,0x1E,0x31,0xff,867,980,ScreenPoint.SO_Portrait);

    //register man
    static ScreenPoint pointRegisterMan = new ScreenPoint(0x0B,0x1E,0x31,0xff,219,1068,ScreenPoint.SO_Portrait);
    static ScreenPoint pointRegisterManConfirm = new ScreenPoint(0xA4,0x00,0x00,0xff,310,1055,ScreenPoint.SO_Portrait);

    //keep touch center
    //wait teach??
    static ScreenPoint pointExitTownHint = new ScreenPoint(0x03,0x03,0x03,0xff,153,1246,ScreenPoint.SO_Portrait);
    static ScreenPoint pointExitTown = new ScreenPoint(0xCB,0x85,0x17,0xff,1048,66,ScreenPoint.SO_Portrait);

    //keep touch center
    //wait teach
    static ScreenPoint pointTeachShowFood = new ScreenPoint(0x04,0x04,0x04,0xff,150,1241,ScreenPoint.SO_Portrait);
    static ScreenPoint pointRetrieveFood = new ScreenPoint(0x5F,0x38,0x11,0xff,749,947,ScreenPoint.SO_Portrait);

    //wait teach
    static ScreenPoint pointTeachShowManage = new ScreenPoint(0x02,0x03,0x02,0xff,891,1238,ScreenPoint.SO_Portrait);
    static ScreenPoint pointManage = new ScreenPoint(0xBF,0x7C,0x4E,0xff,91,1658,ScreenPoint.SO_Portrait);

    //keep touch
    static ScreenPoint pointTeachShowManMap = new ScreenPoint(0x01,0x01,0x01,0xff,924,722,ScreenPoint.SO_Portrait);
    static ScreenPoint pointManMap = new ScreenPoint(0xA6,0x81,0x3C,0xff,631,694,ScreenPoint.SO_Portrait);

    //keep touch
    static ScreenPoint pointTeachShowSummonMan = new ScreenPoint(0x02,0x02,0x02,0xff,911,656,ScreenPoint.SO_Portrait);
    static ScreenPoint pointSummonManMap = new ScreenPoint(0xD7,0xC6,0x90,0xff,608,662,ScreenPoint.SO_Portrait);
    static ScreenPoint pointSummonMan = new ScreenPoint(0x80,0x0,0x0,0xff,575,988,ScreenPoint.SO_Portrait);
    //tap random once
    static ScreenPoint pointSummonManRegister = new ScreenPoint(0x3F,0x20,0x0,0xff,178,1541,ScreenPoint.SO_Portrait);
    //tap pointRegisterManConfirm
    //tap twice
    //dont detect color
    static ScreenPoint pointSummonManSecond = new ScreenPoint(0x0,0x0,0x0,0xff,581,110,ScreenPoint.SO_Portrait);
    //tap pointSummonManRegister
    //tap pointRegisterManConfirm
    //Exit
    static ScreenPoint pointSummonExit = new ScreenPoint(0xBB,0x78,0x15,0xff,1033,45,ScreenPoint.SO_Portrait);
    //tap center

    //second battle
    static ScreenPoint pointBattleEnterLinZhi = new ScreenPoint(0xFF,0xB3,0x1,0xff,537,984,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleEnterLinZhiConfirm = new ScreenPoint(0x6D,0x0,0x0,0xff,73,1658,ScreenPoint.SO_Portrait);

    //tap follow
    static ScreenPoint pointPreBattleDialogEndDetect = new ScreenPoint(0x8c,0x4b,0x7b,0xff,99,1484,ScreenPoint.SO_Portrait);
    //wait 5 seconds tap follow, no color
    static ScreenPoint pointPreBattleEnter = new ScreenPoint(0x61,0x3e,0x13,0xff,107,1820,ScreenPoint.SO_Portrait);

    //tap center
    static ScreenPoint pointAutoArrange = new ScreenPoint(0x93,0x6e,0x00,0xff,51,1144,ScreenPoint.SO_Portrait);
    static ScreenPoint pointAutoArrange2 = new ScreenPoint(0x56,0x40,0x00,0xff,64,1224,ScreenPoint.SO_Portrait);
    static ScreenPoint pointGoBattle = new ScreenPoint(0x8A,0x0,0x00,0xff,60,1542,ScreenPoint.SO_Portrait);
    static ScreenPoint pointGoBattleConfirm = new ScreenPoint(0x38,0x1D,0x00,0xff,329,1356,ScreenPoint.SO_Portrait);

    //tap following until
    static ScreenPoint pointBattleSkip = new ScreenPoint(0x0,0x0,0x00,0xff,840,1421,ScreenPoint.SO_Portrait);
    static ScreenPoint pointSecondBattleAsk = new ScreenPoint(0x32,0x2B,0x1D,0xff,421,1110,ScreenPoint.SO_Portrait);

    //tap battle skip until follow
    static ScreenPoint pointBattleSpeedUp = new ScreenPoint(0x55,0x58,0x59,0xff,787,86,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleAuto = new ScreenPoint(0x4B,0x0,0x39,0xff,510,102,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleRapid = new ScreenPoint(0x63,0x0,0x0,0xff,667,87,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleRapidConfirm = new ScreenPoint(0x46,0x24,0x0,0xff,336,1334,ScreenPoint.SO_Portrait);

    //tap battle skip until follow
    static ScreenPoint pointBattleClose = new ScreenPoint(0x17,0x3C,0x60,0xff,90,1812,ScreenPoint.SO_Portrait);

    //tap center
    //wait teach manage
    //tap manage
    static ScreenPoint pointManageQuestTeach= new ScreenPoint(0x02,0x02,0x02,0xff,902,1279,ScreenPoint.SO_Portrait);
    static ScreenPoint pointManageButton= new ScreenPoint(0xD7,0xD0,0xBF,0xff,91,1675,ScreenPoint.SO_Portrait);
    static ScreenPoint pointManageQuest= new ScreenPoint(0xD7,0xD0,0xBF,0xff,358,821,ScreenPoint.SO_Portrait);
    //tap following
    static ScreenPoint pointManageQuestSkip= new ScreenPoint(0xB9,0x78,0x7,0xff,870,1798,ScreenPoint.SO_Portrait);
    //tap center until
    static ScreenPoint pointBattleEnterDuCang = new ScreenPoint(0xFE,0xDF,0x0,0xff,563,987,ScreenPoint.SO_Portrait);
    //tap pointBattleEnterLinZhiConfirm
    //tap pointAutoArrange
    //tap pointGoBattle
    //tap pointGoBattleConfirm

    //tap pointBattleSkip until pointBattleRapid
    //tap pointBattleRapid
    //tap pointBattleRapidConfirm
    //tap pointBattleAuto
    //tap pointBattleSkip until pointBattleClose
    //tap pointBattleClose

    //tap center
    //wait teach
    static ScreenPoint pointTeachShowManageCity = new ScreenPoint(0x01,0x01,0x01,0xff,901,1287,ScreenPoint.SO_Portrait);
    static ScreenPoint pointManageCity = new ScreenPoint(0x1F,0x36,0x0,0xff,66,1400,ScreenPoint.SO_Portrait);
    //tap center
    //wait plant
    static ScreenPoint pointPlantIt = new ScreenPoint(0x6C,0x3E,0x11,0xff,520,571,ScreenPoint.SO_Portrait);
    //tap twice follow
    static ScreenPoint pointPlantGrow = new ScreenPoint(0x41,0x22,0x0,0xff,331,1095,ScreenPoint.SO_Portrait);
    static ScreenPoint pointPlantGrowConfirm = new ScreenPoint(0x63,0x33,0x0,0xff,346,1338,ScreenPoint.SO_Portrait);
    //tap 3 sec
    static ScreenPoint pointPlantExit = new ScreenPoint(0xEc,0xb5,0x17,0xff,1048,49,ScreenPoint.SO_Portrait);
    //wait 5 sec and tap center

    //battle cangYang
    static ScreenPoint pointBattleEnterCangYang = new ScreenPoint(0xFF,0xa3,0x0,0xff,537,1051,ScreenPoint.SO_Portrait);
    //tap pointBattleEnterLinZhiConfirm
    //tap pointAutoArrange
    //tap pointGoBattle
    //tap pointGoBattleConfirm

    //tap pointBattleSkip until strategy
    static ScreenPoint pointTeachStrategy = new ScreenPoint(0x2E,0x28,0x1A,0xff,426,1315,ScreenPoint.SO_Portrait);
    //tap pointBattleSkip until pointBattleRapid
    //tap pointBattleRapid
    //tap pointBattleRapidConfirm
    //tap pointBattleAuto
    //tap pointBattleSkip until pointBattleClose
    //tap pointBattleClose


    //tap center until
    //wait teach
    static ScreenPoint pointTeachShowManageZiNan = new ScreenPoint(0x00,0x00,0x00,0xff,889,1477,ScreenPoint.SO_Portrait);
    static ScreenPoint pointManageZiNan = new ScreenPoint(0x32,0x57,0x0,0xff,72,1449,ScreenPoint.SO_Portrait);
    //tap center until
    //wait teach
    static ScreenPoint pointTeachShowManageFood = new ScreenPoint(0x04,0x03,0x02,0xff,881,1321,ScreenPoint.SO_Portrait);
    static ScreenPoint pointManageFood = new ScreenPoint(0x6C,0x52,0x30,0xff,75,1185,ScreenPoint.SO_Portrait);
    //wait 2 sec
    //tap pointPlantGrowConfirm twice
    //tap center 4 times
    static ScreenPoint pointManageFoodSkip = new ScreenPoint(0x6C,0x52,0x30,0xff,348,755,ScreenPoint.SO_Portrait);
    //tap pointPlantExit

    //wait 5 sec
    static ScreenPoint pointTeachNorthSea = new ScreenPoint(0x92,0x05,0x6,0xff,662,971,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleEnterNorthSea = new ScreenPoint(0xFF,0xD2,0x94,0xff,575,982,ScreenPoint.SO_Portrait);
    //tap pointBattleEnterLinZhiConfirm
    //tap pointAutoArrange
    //tap pointGoBattle
    //tap pointGoBattleConfirm

    //tap pointBattleSkip until pointBattleRapid
    //tap pointBattleRapid
    //tap pointBattleRapidConfirm
    //tap pointBattleAuto
    //tap pointBattleSkip until
    static ScreenPoint pointTeachMayerConfirm = new ScreenPoint(0x39,0x1D,0x0,0xff,215,1325,ScreenPoint.SO_Portrait);
    //tap pointBattleClose
    //wait 10 sec
    static ScreenPoint pointTeachOccupyReward = new ScreenPoint(0x3E,0x20,0x0,0xff,141,1096,ScreenPoint.SO_Portrait);
    //tap center until
    static ScreenPoint pointTeachCityConfig = new ScreenPoint(0x02,0x02,0x2,0xff,889,426,ScreenPoint.SO_Portrait);
    static ScreenPoint pointCityConfig = new ScreenPoint(0x68,0x8A,0xAC,0xff,102,315,ScreenPoint.SO_Portrait);
    //tap center 3 times
    static ScreenPoint pointRequireTax = new ScreenPoint(0xC3,0x89,0x2E,0xff,59,707,ScreenPoint.SO_Portrait);
    //wait 3 sec
    //tap pointPlantExit

    //tap until
    static ScreenPoint pointExplore = new ScreenPoint(0xA,0xA,0xA,0xff,556,928,ScreenPoint.SO_Portrait);
    static ScreenPoint pointExploreEnter = new ScreenPoint(0x31,0x19,0x0,0xff,310,1363,ScreenPoint.SO_Portrait);

    //tap 10 time and notify

}
