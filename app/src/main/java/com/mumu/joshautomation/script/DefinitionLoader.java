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

import android.util.Log;

import com.mumu.libjoshgame.ScreenPoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DefinitionLoader {
    private static final String TAG = "DefinitionLoader";

    public void ReadXML(InputStream in) throws Exception{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(in);
        Element root = document.getDocumentElement();
        NodeList nodes = root.getElementsByTagName("defset");

        Log.d(TAG, "There are " + nodes.getLength() + " defset in XML.");

        for(int i = 0; i < nodes.getLength(); i++){
            Element element = (Element)nodes.item(i);
            String resolution = element.getAttribute("resolution");
            Log.d(TAG, "Resolution is " + resolution + " for defset " + i);
        }
    }
}
