package com.mumu.joshautomation.fgo;

import com.mumu.libjoshgame.ScreenPoint;

public class FGORoutineDefineTW {
    static ScreenPoint titleScreenPoint = new ScreenPoint(0x0,0x6,0xe,0xFF,631 , 405, ScreenPoint.SO_Portrait);
    //skip once
    //test battle started
    //press battle, 123
    //press battle, 123
    //press battle, 123
    //press battle, delay, press center, 123
    //press battle, royal1,1,2
    //skip once
    //tap name field
    static ScreenPoint nameFieldScreenPoint = new ScreenPoint(0x6d,0x6d,0x72,0xFF,535 , 1402, ScreenPoint.SO_Portrait);
    //input name
    static ScreenPoint nameConfirmScreenPoint = new ScreenPoint(0xd6,0xd7,0xd7,0xFF,831 , 1690, ScreenPoint.SO_Portrait);
    //tap ok .. missed
    static ScreenPoint nameConfirmDecidePoint = new ScreenPoint(0xd4,0xd5,0xd5,0xFF,362 , 1235, ScreenPoint.SO_Portrait);
    //tap confirm .. missed
    static ScreenPoint nameConfirmFinalPoint = new ScreenPoint(0xd7,0xd7,0xd7,0xFF,239 , 1185, ScreenPoint.SO_Portrait);
    //skip once

    //x-a
    static ScreenPoint XAStageScreenPoint = new ScreenPoint(0x6b,0x1b,0x15,0xFF,473 , 963, ScreenPoint.SO_Portrait);
    static ScreenPoint XASubStageScreenPoint = new ScreenPoint(0x17,0x46,0x81,0xFF,835 , 1592, ScreenPoint.SO_Portrait);

    //skip-once
    //enter battle
    //press battle 213
    //press skill 2
    //press confirm
    static ScreenPoint skillConfirmScreenPoint = new ScreenPoint(0xd3,0xd3,0xd3,0xFF, 439, 1426, ScreenPoint.SO_Portrait);
    static ScreenPoint skillTargetScreenPoint = new ScreenPoint(0x4c,0x51,0x65,0xFF, 358, 973, ScreenPoint.SO_Portrait);
    //press battle
    //press 2x
    static ScreenPoint battle2XScreenPoint = new ScreenPoint(0xE2,0xe0,0xe0,0xFF, 982, 1699, ScreenPoint.SO_Portrait);
    //press 123
    //battle to the end
    static ScreenPoint pointBattleNext = new ScreenPoint(0xd7,0xd6, 0xd6,0xff, 71, 1510,ScreenPoint.SO_Portrait);
    //skip once
    static ScreenPoint pointQuestClear = new ScreenPoint(0xFA,0xCA,0x02,0xff,293,1166,ScreenPoint.SO_Portrait);

    //x-b
    //press XAstage, press XASubstage
    //skip once
    //free battle once
    //wait
    static ScreenPoint pointChangeHint = new ScreenPoint(0xfc,0xfb, 0xfb,0xff, 787, 1326,ScreenPoint.SO_Portrait);
    static ScreenPoint pointChangeButton = new ScreenPoint(0x4e, 0x25, 0x1d,0xff, 581, 455,ScreenPoint.SO_Portrait);
    //press battle 123
    //battle to end
    //skip once, stage clear

    //press menu
    static ScreenPoint pointMenuButton = new ScreenPoint(0x55, 0x55, 0x67, 0xff, 46, 1735, ScreenPoint.SO_Portrait);
    static ScreenPoint pointSummonButton = new ScreenPoint(0x2b, 0x3c, 0xaf, 0xff, 99, 815, ScreenPoint.SO_Portrait);
    static ScreenPoint pointTenSummonButton = new ScreenPoint(0xa9, 0x9e, 0x19, 0xff, 184, 1010, ScreenPoint.SO_Portrait);
    static ScreenPoint pointSummonConfirmButton = new ScreenPoint(0xd4, 0xd5, 0xd6, 0xff, 216, 1150, ScreenPoint.SO_Portrait);
    //keep press until
    static ScreenPoint pointSummonSkipButton = new ScreenPoint(0x00, 0x00, 0x00, 0xff, 329, 1700, ScreenPoint.SO_Portrait);
    static ScreenPoint pointSummonNextButton = new ScreenPoint(0xce, 0xd2, 0xd5, 0xff, 59, 1513, ScreenPoint.SO_Portrait);
    //keep press skip until
    static ScreenPoint pointSummonSummonButton = new ScreenPoint(0x16, 0x82, 0xc4, 0xff, 60, 1081, ScreenPoint.SO_Portrait);

    //press menu
    //press formation
    static ScreenPoint pointFormationButton = new ScreenPoint(0x35, 0x46, 0x8b, 0xff, 94, 293, ScreenPoint.SO_Portrait);
    static ScreenPoint pointTeamFormationButton = new ScreenPoint(0xe7, 0xdf, 0xd6, 0xff, 832, 1298, ScreenPoint.SO_Portrait);
    static ScreenPoint pointTeamMem2Button = new ScreenPoint(0x96, 0x96, 0x96, 0xff, 535, 532, ScreenPoint.SO_Portrait);
    //tap following twice, color independent
    static ScreenPoint pointMemTargetButton = new ScreenPoint(0x00, 0x00, 0x00, 0xff, 640, 518, ScreenPoint.SO_Portrait);
    static ScreenPoint pointFormationConfirmButton = new ScreenPoint(0xd4, 0xd4, 0xd4, 0xff, 61, 1725, ScreenPoint.SO_Portrait);
    //tap following twice
    static ScreenPoint pointFormationCloseButton = new ScreenPoint(0xd4, 0xd5, 0xd6, 0xff, 1012, 120, ScreenPoint.SO_Portrait);

    //x-c1
    //press XAstage, press XASubstage
    //press friend twice
    static ScreenPoint pointSelectFriendButton = new ScreenPoint(0x69, 0x82, 0xab, 0xff, 618, 922, ScreenPoint.SO_Portrait);
    static ScreenPoint pointEnterStageButton = new ScreenPoint(0xe1, 0xe2, 0xe4, 0xff, 91, 1768, ScreenPoint.SO_Portrait);
    //skip once
    //tap battle, to end
    //not apply friend
    static ScreenPoint pointDenyFriend = new ScreenPoint(0xd3, 0xd3, 0xd3, 0xff,161,417,ScreenPoint.SO_Portrait);
    //skip once

    //x-c2
    //tap XASub
    //select friend
    //enter stage
    //skip once
    //battle, tap battle2x, 123
    //battle to end

    //x-c3
    //tap XASub

    static ScreenPoint pointBulletinExit = new ScreenPoint(0x0a, 0x23, 0x33, 0xff,999,1837,ScreenPoint.SO_Portrait);
    static ScreenPoint pointLoginBonusButton = new ScreenPoint(0xd6, 0xd6, 0xd7, 0xff,225,1070,ScreenPoint.SO_Portrait);

    //tap until menu show
    static ScreenPoint pointMyRoom = new ScreenPoint(0x20, 0x30, 0x9c, 0xff,141,1646,ScreenPoint.SO_Portrait);
    //wait 8 seconds
    static ScreenPoint pointMyRoomBarEnd = new ScreenPoint(0x66, 0x68, 0x71, 0xff,189,1895,ScreenPoint.SO_Portrait);
    //tap return to title (no color)
    static ScreenPoint pointMyRoomReturnTitle = new ScreenPoint(0x00, 0x00, 0x00, 0xff,318,1467,ScreenPoint.SO_Portrait);
    static ScreenPoint pointMyRoomReturnTitleConfirm = new ScreenPoint(0xdf, 0xe0, 0xe0, 0xff,211,1177,ScreenPoint.SO_Portrait);

    //wait until
    static ScreenPoint loginAccountID = new ScreenPoint(0xf2, 0xf2, 0xf2, 0xff,771,899,ScreenPoint.SO_Portrait);
    static ScreenPoint loginKeyBackspace = new ScreenPoint(0x48, 0xa5, 0xd4, 0xff,139,1685,ScreenPoint.SO_Portrait);
    static ScreenPoint loginKeyNext = new ScreenPoint(0xd6, 0xd7, 0xd7, 0xff,836,1677,ScreenPoint.SO_Portrait);
    static ScreenPoint loginLogin = new ScreenPoint(0x23, 0xad, 0xe5, 0xff,514,1259,ScreenPoint.SO_Portrait);
}
