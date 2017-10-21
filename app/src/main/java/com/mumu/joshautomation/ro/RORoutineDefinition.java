package com.mumu.joshautomation.ro;

import com.mumu.libjoshgame.ScreenColor;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import java.util.ArrayList;

/**
 * RO Routine Definition
 * Sample screen color and coordination for 1080p screen
 */

public class RORoutineDefinition {

    // Gauge Type
    public static final int GAUGE_TYPE_HP = 0;
    public static final int GAUGE_TYPE_MP = 1;

    // HP supply gauge point info
    static ScreenCoord pointHPStart = new ScreenCoord(64,185,ScreenPoint.SO_Landscape);
    static ScreenCoord pointHPEnd   = new ScreenCoord(209,185,ScreenPoint.SO_Landscape);
    static ScreenColor pointHPColor = new ScreenColor(137,214,37,0xff);

    // MP supply gauge point info
    static ScreenCoord pointMPStart = new ScreenCoord(64,199,ScreenPoint.SO_Landscape);
    static ScreenCoord pointMPEnd   = new ScreenCoord(209,199,ScreenPoint.SO_Landscape);
    static ScreenColor pointMPColor = new ScreenColor(113,145,232,0xff);

    /*
     *    Item5   Item4    Item3    Item2    Item1    Auto
     *    Skill6  Skill5   Skill4   Skill3   Skill2   Skill1
     */
    // Item supply points
    static private ScreenCoord pointItem1 = new ScreenCoord(1700, 850, ScreenPoint.SO_Landscape);
    static private ScreenCoord pointItem2 = new ScreenCoord(1560, 850, ScreenPoint.SO_Landscape);
    static private ScreenCoord pointItem3 = new ScreenCoord(1400, 850, ScreenPoint.SO_Landscape);
    static private ScreenCoord pointItem4 = new ScreenCoord(1260, 850, ScreenPoint.SO_Landscape);
    static private ScreenCoord pointItem5 = new ScreenCoord(1110, 850, ScreenPoint.SO_Landscape);
    static ArrayList<ScreenCoord> pointItems = new ArrayList<ScreenCoord>() {{
        add(pointItem1);add(pointItem2);add(pointItem3);add(pointItem4);add(pointItem5);
    }};

    // Skill quick points
    static private ScreenCoord pointSkill1 = new ScreenCoord(1850, 1000, ScreenPoint.SO_Landscape);
    static private ScreenCoord pointSkill2 = new ScreenCoord(1700, 1000, ScreenPoint.SO_Landscape);
    static private ScreenCoord pointSkill3 = new ScreenCoord(1560, 1000, ScreenPoint.SO_Landscape);
    static private ScreenCoord pointSkill4 = new ScreenCoord(1400, 1000, ScreenPoint.SO_Landscape);
    static private ScreenCoord pointSkill5 = new ScreenCoord(1260, 1000, ScreenPoint.SO_Landscape);
    static private ScreenCoord pointSkill6 = new ScreenCoord(1110, 1000, ScreenPoint.SO_Landscape);
    static ArrayList<ScreenCoord> pointSkills = new ArrayList<ScreenCoord>() {{
        add(pointSkill1);add(pointSkill2);add(pointSkill3);add(pointSkill4);add(pointSkill5);add(pointSkill6);
    }};
}

