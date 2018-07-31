/*
 * Copyright (C) 2018 The Josh Tool Project
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

package com.mumu.joshautomation.script;

import android.content.res.Resources;

import com.mumu.libjoshgame.Log;
import com.mumu.libjoshgame.ScreenColor;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/*
 * DefinitionLoader (added in version 1.52)
 * Load a specific resolution of definition set
 * Usage:
 * DefinitionLoader mDF = DefinitionLoader.getInstance();
 */
public class DefinitionLoader {
    private static final String TAG = "DefinitionLoader";

    private final String TAG_ROOT = "definitions";
    private final String TAG_DEFSET = "defset";
    private final String TAG_SCREENPOINT = "screenpoint";
    private final String TAG_SCREENCOORD = "screencoord";
    private final String TAG_SCREENCOLOR = "screencolor";
    private final String TAG_SCREENPOINTS = "screenpoints";
    private final String TAG_SCREENCOORDS = "screencoords";
    private final String TAG_SCREENCOLORS = "screencolors";
    private final String ATTR_VERSION = "version";
    private final String ATTR_RESOLUTION = "resolution";
    private final String ATTR_NAME = "name";

    private Resources mRes;
    private static DefinitionLoader mSelf;
    private boolean mLoaderInitialized = false;

    private DefinitionLoader() {

    }

    public static DefinitionLoader getInstance() {
        if (mSelf == null) {
            mSelf = new DefinitionLoader();
        }

        return mSelf;
    }

    public void setResources(Resources res) {
        if (res != null) {
            mRes = res;
            mLoaderInitialized = true;
        }
    }

    public boolean getAvailable() {
        return mLoaderInitialized;
    }

    public DefData requestDefData(int rawFileId, String resolution) {
        InputStream inputStream;

        // check if service is initialized or aborting
        if (!getAvailable()) {
            Log.e(TAG, "Not yet initialized, aborting.");
            return null;
        }

        // get input stream from resources
        try {
            inputStream = mRes.openRawResource(rawFileId);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "resource id " + rawFileId + " not found!");
            return null;
        }

        try {
            return parse(inputStream, resolution);
        } catch (Exception e) {
            Log.e(TAG, "Parse input stream failed. " + e.getMessage());
        }

        return null;
    }

    private DefData parse(InputStream in, String resolutionRequested) throws Exception {
        String documentVersion = "unknown_version";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(in);

        // parse root <definitions>
        Element root = document.getDocumentElement();
        documentVersion = getVersionFromTag(root);
        Log.d(TAG, "document version is " + documentVersion);

        // parse defset
        NodeList defsets = root.getElementsByTagName(TAG_DEFSET);
        Node targetDefset = null;
        Log.d(TAG, "There are " + defsets.getLength() + " defset in XML.");

        for(int i = 0; i < defsets.getLength(); i++){
            Element element = (Element)defsets.item(i);
            String resolution = element.getAttribute(ATTR_RESOLUTION);
            Log.d(TAG, "Resolution is " + resolution + " for defset " + i);
            if (resolution.equals(resolutionRequested)) {
                targetDefset = defsets.item(i);
                Log.d(TAG, "Resolution matched at index " + i);
            }
        }

        // parse matched resolution data if any
        if (targetDefset != null) {
            return parseDefData((Element)targetDefset, resolutionRequested);
        } else {
            Log.w(TAG, "No defset found for resolution " + resolutionRequested);
        }

        return null;
    }

    private DefData parseDefData(Element target, String resolutionRequested) {
        DefData defData = new DefData();
        NodeList screenpointList = target.getElementsByTagName(TAG_SCREENPOINT);
        NodeList screencoordList = target.getElementsByTagName(TAG_SCREENCOORD);
        NodeList screencolorList = target.getElementsByTagName(TAG_SCREENCOLOR);
        NodeList screenpointsList = target.getElementsByTagName(TAG_SCREENPOINTS);
        NodeList screencoordsList = target.getElementsByTagName(TAG_SCREENCOORDS);
        NodeList screencolorsList = target.getElementsByTagName(TAG_SCREENCOLORS);

        defData.setResolution(resolutionRequested);

        Log.d(TAG, "This resolution has " + screenpointList.getLength() + " " + TAG_SCREENPOINT);
        Log.d(TAG, "This resolution has " + screencoordList.getLength() + " " + TAG_SCREENCOORD);
        Log.d(TAG, "This resolution has " + screencolorList.getLength() + " " + TAG_SCREENCOLOR);
        Log.d(TAG, "This resolution has " + screenpointsList.getLength() + " " + TAG_SCREENPOINTS);
        Log.d(TAG, "This resolution has " + screencoordsList.getLength() + " " + TAG_SCREENCOORDS);
        Log.d(TAG, "This resolution has " + screencolorsList.getLength() + " " + TAG_SCREENCOLORS);

        // parse screenpoint (with name only)
        for(int i = 0; i < screenpointList.getLength(); i++) {
            Element element = (Element) screenpointList.item(i);
            String name = element.getAttribute(ATTR_NAME);
            String data = getElementValue(element);
            if (!name.equals("")) {
                ScreenPoint sp = new ScreenPoint(data);
                Log.d(TAG, "Name " + name + ":" + sp.toString());
                defData.addScreenPoint(name, sp);
            }
        }

        // parse screencoord (with name only)
        for(int i = 0; i < screencoordList.getLength(); i++) {
            Element element = (Element) screencoordList.item(i);
            String name = element.getAttribute(ATTR_NAME);
            String data = getElementValue(element);
            if (!name.equals("")) {
                ScreenCoord sc = new ScreenCoord(data);
                Log.d(TAG, "Name " + name + ":" + sc.toString());
                defData.addScreenCoord(name, sc);
            }
        }

        // parse screencolor (with name only)
        for(int i = 0; i < screencolorList.getLength(); i++) {
            Element element = (Element) screencolorList.item(i);
            String name = element.getAttribute(ATTR_NAME);
            String data = getElementValue(element);
            if (!name.equals("")) {
                ScreenColor sc = new ScreenColor(data);
                Log.d(TAG, "Name " + name + ":" + sc.toString());
                defData.addScreenColor(name, sc);
            }
        }

        // parse screenpoints array
        for(int i = 0; i < screenpointsList.getLength(); i++) {
            Element element = (Element) screenpointsList.item(i);
            String name = element.getAttribute(ATTR_NAME);
            NodeList points = element.getElementsByTagName(TAG_SCREENPOINT);
            int pointCount = points.getLength();
            ArrayList<ScreenPoint> spSet = new ArrayList<>();

            Log.d(TAG, "Point set name = " + name);
            for(int j = 0; j < pointCount; j++) {
                String data = getElementValue(points.item(j));
                ScreenPoint sp = new ScreenPoint(data);
                spSet.add(j, sp);
                Log.d(TAG, "     <" + j + "> " + sp.toString());
            }

            defData.addScreenPoints(name, spSet);
        }

        // parse screencoords array
        for(int i = 0; i < screencoordsList.getLength(); i++) {
            Element element = (Element) screencoordsList.item(i);
            String name = element.getAttribute(ATTR_NAME);
            NodeList coords = element.getElementsByTagName(TAG_SCREENCOORD);
            int coordCount = coords.getLength();
            ArrayList<ScreenCoord> scSet = new ArrayList<>();

            Log.d(TAG, "Coord set name = " + name);
            for(int j = 0; j < coordCount; j++) {
                String data = getElementValue(coords.item(j));
                ScreenCoord sp = new ScreenCoord(data);
                scSet.add(j, sp);
                Log.d(TAG, "     <" + j + "> " + sp.toString());
            }

            defData.addScreenCoords(name, scSet);
        }

        // parse screencolors array
        for(int i = 0; i < screencolorsList.getLength(); i++) {
            Element element = (Element) screencolorsList.item(i);
            String name = element.getAttribute(ATTR_NAME);
            NodeList colors = element.getElementsByTagName(TAG_SCREENCOLOR);
            int colorCount = colors.getLength();
            ArrayList<ScreenColor> scSet = new ArrayList<>();

            Log.d(TAG, "Color set name = " + name);
            for(int j = 0; j < colorCount; j++) {
                String data = getElementValue(colors.item(j));
                ScreenColor sc = new ScreenColor(data);
                scSet.add(j, sc);
                Log.d(TAG, "     <" + j + "> " + sc.toString());
            }

            defData.addScreenColors(name, scSet);
        }

        return defData;
    }

    public class DefData {
        private String resolution; //defset resolution

        private HashMap<String, ScreenPoint> screenpoint = new HashMap<>();
        private HashMap<String, ScreenCoord> screencoord = new HashMap<>();
        private HashMap<String, ScreenColor> screencolor = new HashMap<>();
        private HashMap<String, ArrayList<ScreenPoint>> screenpoints = new HashMap<>();
        private HashMap<String, ArrayList<ScreenCoord>> screencoords = new HashMap<>();
        private HashMap<String, ArrayList<ScreenColor>> screencolors = new HashMap<>();

        public void setResolution(String res) {
            resolution = res;
        }

        public String getResolution() {
            return resolution;
        }

        public void addScreenPoint(String name, ScreenPoint data) {
            screenpoint.put(name, data);
        }

        public void addScreenCoord(String name, ScreenCoord data) {
            screencoord.put(name, data);
        }

        public void addScreenColor(String name, ScreenColor data) {
            screencolor.put(name, data);
        }

        public void addScreenPoints(String name, ArrayList<ScreenPoint> data) {
            screenpoints.put(name, data);
        }

        public void addScreenCoords(String name, ArrayList<ScreenCoord> data) {
            screencoords.put(name, data);
        }

        public void addScreenColors(String name, ArrayList<ScreenColor> data) {
            screencolors.put(name, data);
        }

        public ScreenPoint getScreenPoint(String name) {
            return screenpoint.get(name);
        }

        public ScreenCoord getScreenCoord(String name) {
            return screencoord.get(name);
        }

        public ScreenColor getScreenColor(String name) {
            return screencolor.get(name);
        }

        public ArrayList<ScreenPoint> getScreenPoints(String name) {
            return screenpoints.get(name);
        }

        public ArrayList<ScreenCoord> getScreenCoords(String name) {
            return screencoords.get(name);
        }

        public ArrayList<ScreenColor> getScreenColors(String name) {
            return screencolors.get(name);
        }
    }

    /*
     * Parsing utilities
     */
    private String getVersionFromTag(Element element) {
        return element.getAttribute(ATTR_VERSION);
    }

    private String getElementValue(Element element) {
        return element.getChildNodes().item(0).getNodeValue();
    }

    private String getElementValue(Node node) {
        return node.getChildNodes().item(0).getNodeValue();
    }
}
