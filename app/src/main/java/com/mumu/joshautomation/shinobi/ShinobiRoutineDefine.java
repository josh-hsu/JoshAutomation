package com.mumu.joshautomation.shinobi;

import com.mumu.libjoshgame.ScreenColor;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

//static ScreenPoint pointBattleEnter = new ScreenPoint(0x,0x,0x,0xFF,,,ScreenPoint.SO_Portrait);

public class ShinobiRoutineDefine {
    static public int sBattleLoopModeNext = 1;
    static public int sBattleLoopModeAgain = 2;

    // pre-battle configuration
    static ScreenPoint pointBattlePrepareSubstage;
    static ScreenPoint pointBattleSubstageButton = new ScreenPoint(0xFF,0xD4,0x06,0xFF,90,1341,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleSelectFriendButton = new ScreenPoint(0x2F,0x92,0x00,0xFF,640,1779,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleSelectFriendAgainButton = new ScreenPoint(0x18,0xB3,0x18,0xFF,597,1637,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleInFriendSelect = new ScreenPoint(0xF1,0xFA,0x00,0xFF,899,1412,ScreenPoint.SO_Portrait);
    static ScreenCoord pointBattleSelectFriendLast = new ScreenCoord(178, 1298, ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleEnter = new ScreenPoint(0x40,0x00,0x69,0xFF,178,1818,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleEnterAgain = new ScreenPoint(0x56,0x01,0x88,0xFF,185,1668,ScreenPoint.SO_Portrait);

    static ScreenCoord pointSwipeStart = new ScreenCoord(1440,700,ScreenPoint.SO_Landscape);
    static ScreenCoord pointSwipeEnd = new ScreenCoord(1440,508,ScreenPoint.SO_Landscape);

    // battle stage
    static ScreenPoint pointBattleOngoing = new ScreenPoint(0xED,0xCC,0x00,0xFF,965,1834,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleDied = new ScreenPoint(0x00,0x2F,0x82,0xFF,123,820,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleDiedExit = new ScreenPoint(0xD5,0x1A,0x4D,0xFF,89,1285,ScreenPoint.SO_Portrait);

    // post-battle configuration
    static ScreenPoint pointBattleResultClearReward = new ScreenPoint(0x5E,0x4F,0x03,0xFF,477,1688,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleAgain = new ScreenPoint(0xFD,0x14,0x3D,0xFF,146,172,ScreenPoint.SO_Portrait);
    static ScreenPoint pointBattleNext = new ScreenPoint(0xB2,0xB2,0x64,0xFF,104,1710,ScreenPoint.SO_Portrait);

    static ScreenPoint pointFriendApply = new ScreenPoint(0xD0,0x1A,0x44,0xFF,214,1062,ScreenPoint.SO_Portrait);
}
