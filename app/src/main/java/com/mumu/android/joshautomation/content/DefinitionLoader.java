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

package com.mumu.android.joshautomation.content;

import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.mumu.libjoshgame.ScreenColor;
import com.mumu.libjoshgame.ScreenCoord;
import com.mumu.libjoshgame.ScreenPoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/*
 * DefinitionLoader
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
    private final String ATTR_ORIENTATION = "orientation";
    private final String ATTR_SAPA_TIMEOUT = "sapaTimeout";
    private final String ATTR_NAME = "name";
    private final String ATTR_SAPA = "sapa";

    private Resources mRes;
    private static DefinitionLoader mSelf;
    private static SparseArray<DefData> mLoadedData;
    private boolean mLoaderInitialized = false;

    private DefinitionLoader() {
        mLoadedData = new SparseArray<>();
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

    /**
     * request DefData of specific resolution from resource ID and given a file name of copy one
     * @param rawFileId Resource ID such as R.raw.some_definitions
     * @param rawFileName File name for a copy of definition such as some_definitions.xml
     * @param resolution Resolution of DefSet in definition XML such as 1080x2340
     * @return DefSet of request or null if not found
     */
    public DefData requestDefData(int rawFileId , String rawFileName, String resolution) {
        DefData defDataFromResource, defDataFromFile = null;
        InputStream resInputStream = null, fileInputStream = null;

        // check if service is initialized or aborting
        if (!getAvailable()) {
            Log.e(TAG, "Not yet initialized, aborting.");
            return null;
        }

        // check if defData has been loaded
        defDataFromResource = mLoadedData.get(rawFileId);
        if (defDataFromResource != null) {
            Log.d(TAG, "resource id " + rawFileId + " has been loaded, return it.");
            return defDataFromResource;
        }

        // request defData from In-App resource
        // get input stream from resources
        try {
            resInputStream = mRes.openRawResource(rawFileId);
            defDataFromResource = requestDefData(resInputStream, resolution);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "resource id " + rawFileId + " not found!");
            return null;
        } finally {
            if (resInputStream != null) {
                try {
                    resInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // request defData from External storage see if someone want to overlay it
        String filePath = Environment.getExternalStorageDirectory().toString() + "/" + rawFileName;
        try {
            fileInputStream = new FileInputStream(filePath);
            defDataFromFile = requestDefData(fileInputStream, resolution);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "file " + filePath + " not found! copy one to external storage");
            copyResourceToFile(rawFileId, filePath);
        }

        // select the newest defData to put in database
        if (defDataFromFile == null) {
            mLoadedData.put(rawFileId, defDataFromResource);
        } else {
            if (defDataFromResource.getVersion() >= defDataFromFile.getVersion()) {
                Log.d(TAG, "Use def xml file in resource");
                mLoadedData.put(rawFileId, defDataFromResource);
            } else {
                Log.d(TAG, "Use def xml file in storage");
                mLoadedData.put(rawFileId, defDataFromFile);
                return defDataFromFile;
            }
        }

        return defDataFromResource;
    }

    public DefData requestDefData(InputStream inputStream, String resolution) {
        DefData defData = null;

        try {
            defData = parse(inputStream, resolution);
        } catch (Exception e) {
            Log.e(TAG, "Parse input stream failed. " + e.getMessage());
        }

        return defData;
    }

    private void copyResourceToFile(int resId, String destFilePath) {
        InputStream in = mRes.openRawResource(resId);
        OutputStream out = null;

        try {
            out = new FileOutputStream(new File(destFilePath));

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private DefData parse(InputStream in, String resolutionRequested) throws Exception {
        String documentVersion, documentOrientation = "", documentSapaTimeout = "";

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
                documentOrientation = element.getAttribute(ATTR_ORIENTATION);
                documentSapaTimeout = element.getAttribute(ATTR_SAPA_TIMEOUT);
                Log.d(TAG, "Resolution matched at index " + i + ", ori: " +
                        documentOrientation + ", timeout: " + documentSapaTimeout);
            }
        }

        // parse matched resolution data if any
        if (targetDefset != null) {
            return parseDefData((Element)targetDefset, resolutionRequested, documentVersion,
                    documentOrientation, documentSapaTimeout);
        } else {
            Log.w(TAG, "No defset found for resolution " + resolutionRequested);
        }

        return null;
    }

    private DefData parseDefData(Element target, String resolutionRequested, String xmlVersion, String xmlOrientation, String xmlSapaTimeout) {
        DefData defData = new DefData();
        NodeList screenpointList = target.getElementsByTagName(TAG_SCREENPOINT);
        NodeList screencoordList = target.getElementsByTagName(TAG_SCREENCOORD);
        NodeList screencolorList = target.getElementsByTagName(TAG_SCREENCOLOR);
        NodeList screenpointsList = target.getElementsByTagName(TAG_SCREENPOINTS);
        NodeList screencoordsList = target.getElementsByTagName(TAG_SCREENCOORDS);
        NodeList screencolorsList = target.getElementsByTagName(TAG_SCREENCOLORS);

        defData.setResolution(resolutionRequested);
        defData.setVersion(xmlVersion);
        defData.setOrientation(xmlOrientation);
        defData.setSapaTimeout(xmlSapaTimeout);

        Log.v(TAG, "This resolution has " + screenpointList.getLength() + " " + TAG_SCREENPOINT);
        Log.v(TAG, "This resolution has " + screencoordList.getLength() + " " + TAG_SCREENCOORD);
        Log.v(TAG, "This resolution has " + screencolorList.getLength() + " " + TAG_SCREENCOLOR);
        Log.v(TAG, "This resolution has " + screenpointsList.getLength() + " " + TAG_SCREENPOINTS);
        Log.v(TAG, "This resolution has " + screencoordsList.getLength() + " " + TAG_SCREENCOORDS);
        Log.v(TAG, "This resolution has " + screencolorsList.getLength() + " " + TAG_SCREENCOLORS);

        // parse screenpoint (with name only)
        for(int i = 0; i < screenpointList.getLength(); i++) {
            Element element = (Element) screenpointList.item(i);
            String name = element.getAttribute(ATTR_NAME);
            String data = getElementValue(element);
            if (!name.equals("")) {
                ScreenPoint sp = new ScreenPoint(data);
                Log.v(TAG, "Name " + name + ":" + sp.toString());
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
                Log.v(TAG, "Name " + name + ":" + sc.toString());
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
                Log.v(TAG, "Name " + name + ":" + sc.toString());
                defData.addScreenColor(name, sc);
            }
        }

        // parse screenpoints array
        for(int i = 0; i < screenpointsList.getLength(); i++) {
            Element element = (Element) screenpointsList.item(i);
            String name = element.getAttribute(ATTR_NAME);
            String sapa = element.getAttribute(ATTR_SAPA);
            NodeList points = element.getElementsByTagName(TAG_SCREENPOINT);
            int pointCount = points.getLength();
            ArrayList<ScreenPoint> spSet = new ArrayList<>();

            Log.d(TAG, "Point set name = " + name);
            for(int j = 0; j < pointCount; j++) {
                String data = getElementValue(points.item(j));
                ScreenPoint sp = new ScreenPoint(data);
                spSet.add(j, sp);
                Log.v(TAG, "     <" + j + "> " + sp.toString());
            }

            defData.addScreenPoints(name, spSet);
            if (sapa.equals("true")) {
                defData.addScreenPointsSapa(name);
                Log.v(TAG, "     SAPA: true");
            }
        }

        // parse screencoords array
        for(int i = 0; i < screencoordsList.getLength(); i++) {
            Element element = (Element) screencoordsList.item(i);
            String name = element.getAttribute(ATTR_NAME);
            NodeList coords = element.getElementsByTagName(TAG_SCREENCOORD);
            int coordCount = coords.getLength();
            ArrayList<ScreenCoord> scSet = new ArrayList<>();

            Log.v(TAG, "Coord set name = " + name);
            for(int j = 0; j < coordCount; j++) {
                String data = getElementValue(coords.item(j));
                ScreenCoord sp = new ScreenCoord(data);
                scSet.add(j, sp);
                Log.v(TAG, "     <" + j + "> " + sp.toString());
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

            Log.v(TAG, "Color set name = " + name);
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
        private double version; //defset version
        private int orientation = ScreenPoint.SO_Landscape;
        private int sapaTimeout = 60;

        private HashMap<String, ScreenPoint> screenpoint = new HashMap<>();
        private HashMap<String, ScreenCoord> screencoord = new HashMap<>();
        private HashMap<String, ScreenColor> screencolor = new HashMap<>();
        private HashMap<String, ArrayList<ScreenPoint>> screenpoints = new HashMap<>();
        private HashMap<String, ArrayList<ScreenCoord>> screencoords = new HashMap<>();
        private HashMap<String, ArrayList<ScreenColor>> screencolors = new HashMap<>();
        private ArrayList<String> sapaList = new ArrayList<>();

        public void setResolution(String res) {
            resolution = res;
        }

        public String getResolution() {
            return resolution;
        }

        public void setVersion(String ver) {
            try {
                version = Double.parseDouble(ver);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Version string " + ver + " is not a legal floating point number");
                version = 1.0;
            }
        }

        public double getVersion() {
            return version;
        }

        public void setOrientation(String orient) {
            if (orient.equals("landscape") || orient.equals("Landscape"))
                orientation = ScreenPoint.SO_Landscape;
            else if (orient.equals("portrait") || orient.equals("Portrait"))
                orientation = ScreenPoint.SO_Portrait;
        }

        public int getOrientation() {
            return orientation;
        }

        public void setSapaTimeout(String timeout) {
            try {
                sapaTimeout = Integer.parseInt(timeout);
            } catch (NumberFormatException e) {
                Log.e(TAG, "SapaTimeout " + timeout + " is not an integer");
                sapaTimeout = 60;
            }
        }

        public int getSapaTimeout() {
            return sapaTimeout;
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

        public void addScreenPointsSapa(String name) {
            sapaList.add(name);
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

        public ArrayList<String> getSapaList() { return sapaList; }

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
