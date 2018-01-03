package com.mumu.joshautomation.fgo;

import android.util.Log;

import java.util.ArrayList;

/*
 * BattleArgument
 *
 * Parsing argument of customized battle parameter
 *
 * Royal Card Index: 6,7,8
 * Skill Index: a,b,c  e,f,g  i,j,k
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
                Log.d(TAG, "=>    round(" + j + "): " + cmd);
            }
        }
    }

    /**
     * outOfBoundCheck
     * @param stage stage to be check
     * @param round round to be check
     * @return true if index out of bound
     */
    private boolean outOfBoundCheck(int stage, int round) {
        Log.d(TAG, "Checking out of bound: " + stage + ":" + round);
        if (mParsedCmd.length < stage - 1) {
            return true;
        } else if (mParsedCmd[stage - 1].length < round) {
            return true;
        }

        return false;
    }

    private int[] getParsedDataOfRound(int stage, int round, boolean isSkill) {
        if (outOfBoundCheck(stage, round))
            return new int[] {};

        String skill = mParsedCmd[stage - 1][round - 1]; //stage and round are started from 1
        ArrayList<Integer> list = new ArrayList<>();
        int length = skill.length();

        for(int i = 0; i < length; i++) {
            char seg = skill.charAt(i);
            int parsedInt;
            if (isSkill)
                parsedInt = parseSkill(seg);
            else
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
     * round and stage should be started at 1 and positive
     */
    public int[] getSkillIndexOfStage(int stage, int round) {
        if (!mSupportStage) {
            return getParsedDataOfRound(1, round, true);
        } else {
            return getParsedDataOfRound(stage, round, true);
        }
    }

    /**
     * getRoyalIndexOfStage (added in 1.40)
     * 6,7,8 will be transfer to 0,1,2
     * round and stage should be started at 1 and positive
     */
    public int[] getRoyalIndexOfStage(int stage, int round) {
        if (!mSupportStage) {
            return getParsedDataOfRound(1, round, false);
        } else {
            return getParsedDataOfRound(stage, round, false);
        }
    }

    /**
     * getSkillIndexOfRound
     * Deprecated in version 1.40
     * adc, efg, ijk will be transfer to 012,345,678
     * round should be started at 1 and positive
     *
     * @deprecated use getSkillIndexOfStage(int stage, int round) instead.
     */
    @Deprecated
    public int[] getSkillIndexOfRound(int round) {
        return getParsedDataOfRound(1, round, true);
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
        return getParsedDataOfRound(1, round, false);
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
                Log.e(TAG, "parse failed, illegal parameter " + i);
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
                Log.e(TAG, "parse failed, illegal parameter " + i);
                return -1;
        }
    }
}
