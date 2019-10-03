/*
 * Copyright (C) 2017 The Josh Tool Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mumu.libjoshgame;

public class ScreenPoint {
    private static final String TAG = "LibGame";
    public final static int SO_Portrait = 0;
    public final static int SO_Landscape = 1;
    public ScreenCoord coord;
    public ScreenColor color;

    public ScreenPoint(int r, int g, int b, int t, int x, int y, int orientation) {
        coord = new ScreenCoord(x, y, orientation);
        color = new ScreenColor((byte)(r & 0xFF), (byte)(g & 0xFF), (byte)(b & 0xFF), (byte)(t & 0xFF));
    }

    public ScreenPoint(ScreenCoord screenCoord, ScreenColor screenColor) {
        coord = screenCoord;
        color = screenColor;
    }

    /*
     * ScreenPoint can now work with PointSelectionActivity for easily adding point (added in 1.33)
     *
     * 1. Example 8-digits formatted string: 4-Aj5Gr0
     *    first byte: 4-Aj is for coordination (up to 2400x2400)
     *    second byte: 5Gr0 is for color (FF000000 ~ FFFFFFFF)
     *
     *    char to int table
     *    |-------------------------------------------------------------------|
     *    |Decimal   Value |  0 - 9  |  10 - 35 | 36 - 61 | 62 | 63 | 64 | 65 |
     *    |--------------------------------------------------------------------
     *    |Character Value |  0 - 9  |  A  - Z  | a  - z  | +  |  - |  * |  / |
     *    |-------------------------------------------------------------------|
     *
     * 2. Example XML formatted string: 236,236,235,0xff,832,74,Landscape
     *    There is no space in string and separated by 6 commas
     *    There are 7 data, first 6 are numbers, last one is string
     *    First 4 data: 236,236,235,0xff are color
     *    Last 3 data: 832,74,Landscape are coordination
     */
    public ScreenPoint(String formattedString) {
        int[] parsedArray = new int[8];
        int unit = 66;

        if (formattedString == null) {
            coord = null;
            color = null;
        } else if (formattedString.length() == 8) { //8-digits format
            for(int i = 0; i < formattedString.length(); i++) {
                char targetChar = formattedString.charAt(i);
                int parsedInt = parseFormattedChar(targetChar);
                parsedArray[i] = parsedInt;
            }

            int coordX = parsedArray[0] * unit + parsedArray[1];
            int coordY = parsedArray[2] * unit + parsedArray[3];
            coord = new ScreenCoord(coordX, coordY, SO_Portrait); //in this case, we only use portrait orientation

            int rawColor = parsedArray[4] * unit*unit*unit + parsedArray[5] * unit*unit + parsedArray[6] * unit + parsedArray[7];
            int colorR = (rawColor >> 16) & 0xff;
            int colorG = (rawColor >> 8)  & 0xff;
            int colorB = rawColor & 0xff;
            color = new ScreenColor(colorR, colorG, colorB, 0xFF); //we force transparent value to 0xff
        } else { //XML format
            String data[] = formattedString.split(",");
            if (data.length == 7) {
                int r, g, b, t, x, y, o;
                String orientation = data[6];
                try {
                    r = Integer.decode(data[0]);
                    g = Integer.decode(data[1]);
                    b = Integer.decode(data[2]);
                    t = Integer.decode(data[3]);
                    x = Integer.decode(data[4]);
                    y = Integer.decode(data[5]);

                    if (orientation.equals("Portrait") || orientation.equals("P") ||
                            orientation.equals("p") || orientation.equals("0")) {
                        o = SO_Portrait;
                    } else if (orientation.equals("Landscape") || orientation.equals("L") ||
                            orientation.equals("l") || orientation.equals("1")) {
                        o = SO_Landscape;
                    } else {
                        throw new NumberFormatException("Orientation " + orientation + " not legal");
                    }

                    coord = new ScreenCoord(x, y, o);
                    color = new ScreenColor((byte)(r & 0xFF), (byte)(g & 0xFF), (byte)(b & 0xFF), (byte)(t & 0xFF));
                } catch (NumberFormatException e) {
                    //Log.e(TAG, "Data not legal " + e.getMessage());
                    coord = null;
                    color = null;
                }
            } else {
                //Log.w(TAG, "ScreenPoint: String " + formattedString + " is not legal.");
                coord = null;
                color = null;
            }
        }
    }

    public ScreenPoint() {
        coord = new ScreenCoord(0,0,0);
        color = new ScreenColor((byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00);
    }

    public String toString() {
        return "ScreenPoint (" + coord.x + "," + coord.y + ") color "
                + String.format("0x%02X ", color.r) + String.format("0x%02X ", color.g)
                + String.format("0x%02X ", color.b) + String.format("0x%02X", color.t);
    }

    public int getColor() {
        return ((color.t & 0xff) << 24 | (color.r & 0xff) << 16 | (color.g & 0xff) << 8 | (color.b & 0xff));
    }

    public String getFormattedString() {
        int unit = 66;

        char coordX1 = genFormattedChar((int)(coord.x / unit));
        char coordX2 = genFormattedChar((int)(coord.x % unit));
        char coordY1 = genFormattedChar((int)(coord.y / unit));
        char coordY2 = genFormattedChar((int)(coord.y % unit));

        int rawColor = (color.r & 0xff) << 16 | (color.g & 0xff) << 8 | (color.b & 0xff);
        char color1 = genFormattedChar((int)(rawColor % unit));
        rawColor = rawColor / unit;
        char color2 = genFormattedChar((int)(rawColor % unit));
        rawColor = rawColor / unit;
        char color3 = genFormattedChar((int)(rawColor % unit));
        char color4 = genFormattedChar((int)(rawColor / unit));

        String coordString = "" + coordX1 + coordX2 + coordY1 + coordY2;
        String colorString = "" + color4 + color3 + color2 + color1;

        return coordString + colorString;
    }

    private char genFormattedChar(int value) {
        int charBaseN = 48; //this is ASCII for number 0
        int charBaseU = 65; //this is ASCII for letter A
        int charBaseL = 97; //this is ASCII for letter B

        if (value >= 0 && value <= 9) {
            return (char)(charBaseN + value);
        } else if (value >= 10 && value <= 35) {
            return (char)(charBaseU + value - 10);
        } else if (value >= 36 && value <= 61) {
            return (char)(charBaseL + value - 36);
        } else if (value >= 62 && value <= 65) {
            switch (value) {
                case 62:
                    return '+';
                case 63:
                    return '-';
                case 64:
                    return '*';
                case 65:
                    return '/';
            }
        }

        return ' ';
    }

    private int parseFormattedChar(char value) {
        int charBaseN = 48; //this is ASCII for number 0
        int charBaseU = 65; //this is ASCII for letter A
        int charBaseL = 97; //this is ASCII for letter B

        int castValue = (int) value;
        if (castValue >= charBaseN && castValue < charBaseN + 10) { //target is a number
            return castValue - charBaseN;
        } else if (castValue >= charBaseU && castValue < charBaseU + 26) { //target is a upper case alphabet
            return (castValue - charBaseU) + 10;
        } else if (castValue >= charBaseL && castValue < charBaseL + 26) { //target is a lower case alphabet
            return (castValue - charBaseL) + 36;
        }

        switch (value) {
            case '+':
                return 62;
            case '-':
                return 63;
            case '*':
                return 64;
            case '/':
                return 65;
            default:
                return -1;
        }
    }
}
