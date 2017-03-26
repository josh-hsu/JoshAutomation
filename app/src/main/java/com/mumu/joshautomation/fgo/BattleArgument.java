package com.mumu.joshautomation.fgo;

import android.util.Log;

import java.sql.Array;
import java.util.ArrayList;

/*
 * BattleArgument
 *
 * Parsing argument of customized battle parameter
 *
 * Royal Card Index: 6,7,8
 * Skill Index: a,b,c  e,f,g  i,j,k
 * Round separator: #
 *
 * Example:
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
    private String[] mParsedCmd;

    public BattleArgument(String cmd) {
        mCmdString = cmd;
        parse();
    }

    public BattleArgument() {
        mCmdString = "";
    }

    public String toString() {
        return mCmdString;
    }

    private void parse() {
        mParsedCmd = mCmdString.split("#");
        for(int i = 0; i < mParsedCmd.length; i++) {
            String cmd = mParsedCmd[i];
            Log.d(TAG, "i = " + i + ", cmd = " + cmd);
        }
    }

    private int[] getParsedDataOfRound(int round, boolean isSkill) {
        if (mParsedCmd.length < round)
            return new int[] {};

        String skill = mParsedCmd[round-1];
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

        int[] retList = new int[list.size()];
        for(int i = 0; i < list.size(); i++) {
            retList[i] = list.get(i);
        }

        return retList;
    }

    /*
     * getSkillIndexOfRound
     * adc, efg, ijk will be transfer to 012,345,678
     * round should be started at 1 and positive
     */
    public int[] getSkillIndexOfRound(int round) {
        return getParsedDataOfRound(round, true);
    }

    /*
     * getRoyalIndexOfRound
     * 6,7,8 will be tansfer to 0,1,2
     */
    public int[] getRoyalIndexOfRound(int round) {
        return getParsedDataOfRound(round, false);
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
