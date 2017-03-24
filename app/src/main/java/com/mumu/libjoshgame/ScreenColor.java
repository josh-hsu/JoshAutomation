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

public class ScreenColor {
    public byte b;	/* blue */
    public byte g;	/* green */
    public byte r;	/* red */
    public byte t;	/* transparent */

    public String toString() {
        return "The color is "
                + String.format("0x%02X ", r) + String.format("0x%02X ", g)
                + String.format("0x%02X ", b) + String.format("0x%02X", t);
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

    public ScreenColor() {
        b = 0;
        g = 0;
        r = 0;
        t = 0;
    }
}
