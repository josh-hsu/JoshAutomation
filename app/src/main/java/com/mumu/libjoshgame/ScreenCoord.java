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

public class ScreenCoord {
    public int x;
    public int y;
    public int orientation;

    public ScreenCoord(int xx, int yy, int oo) {
        x = xx;
        y = yy;
        orientation = oo;
    }

    public ScreenCoord() {
        x = 0;
        y = 0;
        orientation = 0;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public static ScreenCoord getTwoPointCenter(ScreenCoord src, ScreenCoord dest) {

        if (src.orientation != dest.orientation) {
            return null;
        }

        return new ScreenCoord((src.x + dest.x)/2, (src.y + dest.y)/2, src.orientation);
    }

    public ScreenPoint toScreenPoint() {
        return new ScreenPoint(0,0,0,0,x,y,orientation);
    }
}
