package com.mumu.joshautomation.fgo;

import android.util.Log;

import java.util.ArrayList;

/*
 * BattleArgument
 *
 * Parsing argument of customized battle parameter
 *
 * Royal Card Index: 6,7,8
 * Skill Index: a,b,c  e,f,g  i,j,k  master(o,p,q)
 * Skill target: 1, 2, 3 (0 means no target)
 * Round separator: #
 * Stage separator: |
 *
 * No Stage Format Example:
 * #ae6##f#jk8
 * means
 * 1st round: no parameter
 * 2nd round: skill a and e, royal selected of 1st servant
 * 3rd round: no parameter
 * 4th round: skill f
 * 5th round: skill j and k
 */
public class BattleArgument {
    private static final String TAG = "BattleArgument";
    private String mCmdString;
    private String[][] mParsedCmd; //if not support stage, left index will always be 1
    private boolean mSupportStage = false;

    public BattleArgument(String cmd) {
        mCmdString = cmd;
        parse();
    }

    public BattleArgument() {
        mCmdString = "";
        parse();
    }

    public String toString() {
        return mCmdString;
    }

    public static class BattleSkill {
        public int skill;
        public int target;

        public BattleSkill() {
            skill = -1;
            target = 0;
        }

        public BattleSkill(int sk, int tg) {
            skill = sk;
            target = tg;
        }

        public String toString() {
            return "(Skill: " + skill + " Target: " + target + ")";
        }
    }

    /*
     * parse
     * This function handle parsing of battle argument
     */
    private void parse() {
        String[] mParsedStageCmd = mCmdString.split("\\|");

        if (mParsedStageCmd.length > 1) {
            mSupportStage = true;
            mParsedCmd = new String[mParsedStageCmd.length][];
            Log.d(TAG, "stage supported. Length: " + mParsedStageCmd.length);
            for(int i = 0; i < mParsedStageCmd.length; i++) {
                mParsedCmd[i] = mParsedStageCmd[i].split("#");
            }
        } else {
            mSupportStage = false;
            String[] wholeCmdArray = mCmdString.split("#");
            mParsedCmd = new String[1][wholeCmdArray.length];
            mParsedCmd[0] = wholeCmdArray;
        }

        // debug print
        for(int i = 0; i < mParsedCmd.length; i++) {
            Log.d(TAG, "Battle stage: " + i);
            for(int j = 0; j < mParsedCmd[i].length; j++) {
                String cmd = mParsedCmd[i][j];
                Log.d(TAG, "=>    round(" + j + "): " + getParsedSkill(i, j).toString() + " cmd: "+ cmd);
            }
        }
    }

    /**
     * outOfBoundCheck
     * @param stage stage to be check (started with 0)
     * @param round round to be check (started with 0)
     * @return true if index out of bound
     */
    private boolean outOfBoundCheck(int stage, int round) {
        Log.d(TAG, "Checking out of bound: " + stage + ":" + round);
        if (mParsedCmd.length <= stage) {
            Log.d(TAG, "Stage length = " + mParsedCmd.length + " <= stage request = " + stage);
            return true;
        } else if (mParsedCmd[stage].length <= round) {
            Log.d(TAG, "Round length = " + mParsedCmd[stage].length + " <= round request = " + round);
            return true;
        }
        Log.d(TAG, "Not out of bound, Stage length = " + mParsedCmd.length + " Round length = " + mParsedCmd[stage].length);

        return false;
    }

    private ArrayList<BattleSkill> getParsedSkill(int stage, int round) {
        ArrayList<BattleSkill> list = new ArrayList<>();

        if (outOfBoundCheck(stage, round))
            return list;

        String cmd = mParsedCmd[stage][round];
        int length = cmd.length();
        boolean parseForTarget = false;
        BattleSkill thisSkill = new BattleSkill(); //new a object here is not necessary but it keeps inspector quiet

        for(int i = 0; i < length; i++) {
            char seg = cmd.charAt(i);
            int parsedData;

            if (!parseForTarget) {
                parsedData = parseSkill(seg);
                if (parsedData >= 0) {
                    thisSkill = new BattleSkill(); // new a skill for use later
                    thisSkill.skill = parsedData;
                    parseForTarget = true;
                }
            } else {
                parsedData = parseTarget(seg);
                if (parsedData >= 0) { //target did declare
                    thisSkill.target = parsedData;
                    parseForTarget = false;
                    list.add(thisSkill);
                } else { //no target but anything else
                    thisSkill.target = 0;
                    i--; //ignore this and fallback to parsing skill
                    parseForTarget = false;
                    list.add(thisSkill);
                }
            }
        }

        return list;
    }

    private int[] getParsedRoyal(int stage, int round) {
        if (outOfBoundCheck(stage, round))
            return new int[] {};

        String cmd = mParsedCmd[stage][round];
        ArrayList<Integer> list = new ArrayList<>();
        int length = cmd.length();

        for(int i = 0; i < length; i++) {
            char seg = cmd.charAt(i);
            int parsedInt;
            parsedInt = parseRoyal(seg);

            if (parsedInt >= 0) {
                list.add(parsedInt);
            }
        }

        // transform ArrayList to native array
        int[] retList = new int[list.size()];
        for(int i = 0; i < list.size(); i++) {
            retList[i] = list.get(i);
        }

        return retList;
    }

    /**
     * getSkillIndexOfStage (added in 1.40)
     * adc, efg, ijk will be transfer to 012,345,678
     * round and stage should be started at 0
     */
    public ArrayList<BattleSkill> getSkillIndexOfStage(int stage, int round) {
        if (!mSupportStage) {
            return getParsedSkill(0, round);
        } else {
            return getParsedSkill(stage, round);
        }
    }

    /**
     * getRoyalIndexOfStage (added in 1.40)
     * 6,7,8 will be transfer to 0,1,2
     * round and stage should be started at 0
     */
    public int[] getRoyalIndexOfStage(int stage, int round) {
        if (!mSupportStage) {
            return getParsedRoyal(0, round);
        } else {
            return getParsedRoyal(stage, round);
        }
    }

    /**
     * getSkillIndexOfRound
     * Deprecated in version 1.40
     * adc, efg, ijk will be transfer to 012,345,678
     * round should be started at 0
     *
     * @deprecated use getSkillIndexOfStage(int stage, int round) instead.
     */
    @Deprecated
    public int[] getSkillIndexOfRound(int round) {
        return new int[] {};
    }

    /**
     * getRoyalIndexOfRound
     * Deprecated in version 1.40
     * 6,7,8 will be transfer to 0,1,2
     *
     * @deprecated use getRoyalIndexOfRound(int stage, int round) instead.
     */
    @Deprecated
    public int[] getRoyalIndexOfRound(int round) {
        return getParsedRoyal(0, round);
    }

    private int parseSkill(char i) {
        switch (i) {
            case 'a':
                return 0;
            case 'b':
                return 1;
            case 'c':
                return 2;
            case 'e':
                return 3;
            case 'f':
                return 4;
            case 'g':
                return 5;
            case 'i':
                return 6;
            case 'j':
                return 7;
            case 'k':
                return 8;
            default:
                return -1;
        }
    }

    private int parseTarget(char i) {
        switch (i) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            default:
                return -1;
        }
    }

    private int parseRoyal(char i) {
        switch (i) {
            case '6':
                return 0;
            case '7':
                return 1;
            case '8':
                return 2;
            default:
                return -1;
        }
    }
}
