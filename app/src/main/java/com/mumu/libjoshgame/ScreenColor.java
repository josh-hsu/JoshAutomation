/*
 * Copyright (C) 2020 The Josh Tool Project
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

public class ScreenColor {
    public byte b;	/* blue */
    public byte g;	/* green */
    public byte r;	/* red */
    public byte t;	/* transparent */

    public String toString() {
        return "0x "
                + String.format("%02X ", r) + String.format("%02X ", g)
                + String.format("%02X ", b) + String.format("%02X", t);
    }

    public ScreenColor(byte rr, byte gg, byte bb, byte tt) {
        b = bb;
        g = gg;
        r = rr;
        t = tt;
    }

    public ScreenColor(int rr, int gg, int bb, int tt) {
        b = (byte) bb;
        g = (byte) gg;
        r = (byte) rr;
        t = (byte) tt;
    }

    public ScreenColor(String formattedString) {
        String data[] = formattedString.split(",");
        if (data.length == 4) {
            try {
                r = (byte) (Integer.decode(data[0])& 0xFF);
                g = (byte) (Integer.decode(data[1])& 0xFF);
                b = (byte) (Integer.decode(data[2])& 0xFF);
                t = (byte) (Integer.decode(data[3])& 0xFF);
            } catch (NumberFormatException e) {
                b = 0;
                g = 0;
                r = 0;
                t = 0;
            }
        } else {
            b = 0;
            g = 0;
            r = 0;
            t = 0;
        }
    }

    public ScreenColor() {
        b = 0;
        g = 0;
        r = 0;
        t = 0;
    }

    public ScreenPoint toScreenPoint() {
        return new ScreenPoint(r,g,b,t,0,0, ScreenPoint.SO_Landscape);
    }
}
