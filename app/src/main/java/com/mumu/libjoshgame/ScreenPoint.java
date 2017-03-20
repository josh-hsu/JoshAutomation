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
    public final static int SO_Portrait = 0;
    public final static int SO_Landscape = 1;
    public final static int SO_Max = 2;
    public ScreenCoord coord;
    public ScreenColor color;

    public ScreenPoint(int r, int g, int b, int t, int x, int y, int orientation) {
        coord = new ScreenCoord(x, y, orientation);
        color = new ScreenColor((byte)(r & 0xFF), (byte)(g & 0xFF), (byte)(b & 0xFF), (byte)(t & 0xFF));
    }

    public ScreenPoint() {
        coord = new ScreenCoord(0,0,0);
        color = new ScreenColor((byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00);
    }

    public String toString() {
        return "The color of point (" + coord.x + "," + coord.y + ") is "
                + String.format("0x%02X ", color.r) + String.format("0x%02X ", color.g)
                + String.format("0x%02X ", color.b) + String.format("0x%02X", color.t);
    }

    /*
     * Landscape view (xl, yl) v.s. Portrait view
     *
     *   xl = yp
     *   yl = screen_width - xp
     */

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

        public ScreenColor(byte bb, byte gg, byte rr, byte tt) {
            b = bb;
            g = gg;
            r = rr;
            t = tt;
        }
    }

    public class ScreenCoord {
        public int x;
        public int y;
        public int orientation;

        public ScreenCoord(int xx, int yy, int oo) {
            x = xx;
            y = yy;
            orientation = oo;
        }

        public String toString() {
            return "The color of point (" + x + "," + y + ")";
        }
    }
}
