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

package com.mumu.android.joshautomation.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mumu.android.joshautomation.R;
import com.mumu.android.joshautomation.content.AppPreferenceValue;
import com.mumu.android.joshautomation.service.HeadService;
import com.mumu.android.joshautomation.scripts.fgo.BattleArgument;
import com.mumu.android.joshautomation.scripts.fgo.BattleArgumentDialog;
import com.mumu.android.joshautomation.autojob.AutoJobHandler;

import java.util.ArrayList;
import java.util.List;

public class AppPreferenceActivity extends PreferenceActivity {
    public static final String TAG = "JoshAutomation";

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        /* This is for action bar */
        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        bar.setTitleTextColor(Color.WHITE);
        bar.setTitle(getTitle());
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_header, target);
    }

    /*
     * Since API 19, PreferenceActivity must implement isValidFragment if it is initiated from
     * PACKAGE_NAME.PREF_ACTIVITY$FRAG_SUBCLASS
     */
    @Override
    protected boolean isValidFragment (String fragmentName) {
        return true;
    }

    /**
     * System Preference Fragment
     */
    public static class SystemPrefFragment extends PreferenceFragment {
        static final int PICK_FILE_RESULT_CODE = 1302;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.app_preferences, false);

            // Load the app_preferences from an XML resource
            addPreferencesFromResource(R.xml.app_preferences);

            // Add start service button listener
            Preference myPref = findPreference("enableServicePref");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    getContext().startService(new Intent(getContext(), HeadService.class));
                    return true;
                }
            });

            // Add SAPA XML selector
            myPref = findPreference("selectSAPAJobScript");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    startFileSelector();
                    return true;
                }
            });

            // Fill out script
            ListPreference scriptSelectPref = (ListPreference) findPreference("scriptSelectPref");
            AutoJobHandler jobHandler = AutoJobHandler.getHandler();
            ArrayList<CharSequence> entriesArray = new ArrayList<>();
            ArrayList<CharSequence> entriesValueArray = new ArrayList<>();

            if (jobHandler.getJobCount() <= 0) {
                entriesArray.add("沒有工作，請確定服務打開");
                entriesValueArray.add("0");
            } else {
                for(int i = 0; i < jobHandler.getJobCount(); i++) {
                    entriesArray.add(jobHandler.getJobName(i));
                    entriesValueArray.add(""+i);
                }
            }

            scriptSelectPref.setEntries(entriesArray.toArray(new CharSequence[entriesArray.size()]));
            scriptSelectPref.setDefaultValue("0");
            scriptSelectPref.setEntryValues(entriesValueArray.toArray(new CharSequence[entriesValueArray.size()]));
        }

        void startFileSelector() {
            Intent filePickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            filePickerIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            filePickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            filePickerIntent.setType("text/xml");
            filePickerIntent = Intent.createChooser(filePickerIntent, "請選擇腳本 XML 檔案");
            startActivityForResult(filePickerIntent, PICK_FILE_RESULT_CODE);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case PICK_FILE_RESULT_CODE:
                    if (resultCode == Activity.RESULT_OK) {
                        Uri fileUri = data.getData();
                        Log.d(TAG, "Save path " + fileUri);
                        getContext().getSharedPreferences("com.mumu.android.joshautomation_preferences", Context.MODE_PRIVATE).edit()
                                .putString("selectSAPAJobScript", fileUri.toString()).apply();
                    }
                    break;
            }
        }
    }

    /**
     * FGO Preference Fragment
     */
    public static class FGOPrefFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final int battleArgCount = 5;
        private boolean battleArgDialogPressed = false;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.app_preferences_fgo, false);

            // Load the app_preferences_fgo from an XML resource
            addPreferencesFromResource(R.xml.app_preferences_fgo);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            onSharedPreferenceChanged(sharedPrefs, "battleArgPref");
            onSharedPreferenceChanged(sharedPrefs, "battlePolicyPrefs");
            onSharedPreferenceChanged(sharedPrefs, "battleEatApple");

            // Set on click on single battle arg
            Preference.OnPreferenceClickListener singleBattleArgClicked = new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // TODO: must implement
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    String prefKey = preference.getKey();
                    Intent intent = new Intent(getContext(), BattleArgumentDialog.class);

                    Bundle bundle = new Bundle();
                    bundle.putString(BattleArgumentDialog.bundlePreferenceKey, prefKey);
                    bundle.putString(BattleArgumentDialog.bundleLastArg, sharedPrefs.getString(prefKey, ""));

                    intent.putExtras(bundle);
                    startActivity(intent);
                    battleArgDialogPressed = true;
                    return false;
                }
            };

            for (int i = 0; i < battleArgCount; i++) {
                String key = "battleArgSaved" + i;
                findPreference(key).setOnPreferenceClickListener(singleBattleArgClicked);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
            refreshAllBattleArgs();
            if (battleArgDialogPressed)
                Toast.makeText(getActivity(), "修改後請重新在「戰鬥參數選擇」做選擇", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            super.onPreferenceTreeClick(preferenceScreen, preference);

            // If the user has clicked on a preference screen, set up the screen
            if (preference instanceof PreferenceScreen) {
                setUpNestedScreen((PreferenceScreen) preference);
            }

            return false;
        }

        public void setUpNestedScreen(PreferenceScreen preferenceScreen) {
            final Dialog dialog = preferenceScreen.getDialog();

            Toolbar bar;

            LinearLayout root = (LinearLayout) dialog.findViewById(android.R.id.list).getParent().getParent();
            bar = (Toolbar) LayoutInflater.from(getContext()).inflate(R.layout.settings_toolbar, root, false);
            root.addView(bar, 0); // insert at top

            bar.setTitle(preferenceScreen.getTitle());
            bar.setTitleTextColor(Color.WHITE);

            bar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);

            if (pref instanceof  ListPreference) {
                ListPreference listPref = (ListPreference) pref;

                // TODO: implement here
                if (key.equals("battleArgPref")) {
                    BattleArgument arg = new BattleArgument(listPref.getValue());
                    String valueDisplayed = arg.getName() + ": " + arg.getArgs();
                    pref.setSummary(valueDisplayed);
                } else if (key.equals("battlePolicyPrefs")) {
                    pref.setSummary(listPref.getValue() + ": " + listPref.getEntry());
                } else if (key.equals("battleEatApple")) {
                    pref.setSummary(listPref.getEntry());
                }
            } else if (pref instanceof EditTextPreference) {
                EditTextPreference textPref = (EditTextPreference) pref;
                if (key.equals("battleCountLimit") || key.equals("battleSpeed")) {
                    pref.setSummary("" + textPref.getText());
                }
            }
        }

        private void refreshAllBattleArgs() {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            String keyPrefix = "battleArgSaved";

            for(int i = 0; i < battleArgCount; i++) {
                String key = keyPrefix + i;
                String value = sharedPrefs.getString(key, "");
                // TODO: implement here
                if (!value.equals("")) {
                    BattleArgument arg = new BattleArgument(value);
                    String valueDisplayed = arg.getName() + ": " + arg.getArgs();
                    findPreference(key).setSummary(valueDisplayed);
                } else {
                    findPreference(key).setSummary("無");
                }
            }

            // call refresh for arg list pref
            refreshBattleArgs();
        }

        private void refreshBattleArgs() {
            // TODO: must implement
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            ListPreference selectPref = (ListPreference) findPreference("battleArgPref");
            CharSequence[] battleArgEntries = new CharSequence[battleArgCount];
            CharSequence[] battleArgValues = new CharSequence[battleArgCount];

            for(int i = 0; i < battleArgCount; i++) {
                String key = "battleArgSaved" + i;
                String value = sharedPrefs.getString(key, "");
                BattleArgument arg = new BattleArgument(value);
                battleArgEntries[i] = arg.getName() + ":  " + arg.getArgs();
                battleArgValues[i] = value;
            }

            selectPref.setEntries(battleArgEntries);
            selectPref.setEntryValues(battleArgValues);
        }
    }

    /**
     * Shinobi Preference Fragment
     */
    public static class ShinobiPrefFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.app_preferences_shinobi, false);

            // Load the app_preferences_fgo from an XML resource
            addPreferencesFromResource(R.xml.app_preferences_shinobi);
        }
    }

    /**
     * RO Preference Fragment
     */
    public static class ROPrefFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.app_preferences_ro, false);

            // Load the app_preferences_fgo from an XML resource
            addPreferencesFromResource(R.xml.app_preferences_ro);

            //if you are using default SharedPreferences
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            onSharedPreferenceChanged(sharedPrefs, "roAutoHPValuePref");
            onSharedPreferenceChanged(sharedPrefs, "roAutoHPItemPref");
            onSharedPreferenceChanged(sharedPrefs, "roAutoMPValuePref");
            onSharedPreferenceChanged(sharedPrefs, "roAutoMPItemPref");
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            if (pref instanceof EditTextPreference) {
                EditTextPreference listPref = (EditTextPreference) pref;
                pref.setSummary(listPref.getText());
            }
        }
    }

    /**
     * Epic 7 Preference Fragment
     */
    public static class Epic7PrefFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.app_preferences_epic7, false);

            // Load the app_preferences_fgo from an XML resource
            addPreferencesFromResource(R.xml.app_preferences_epic7);

            //if you are using default SharedPreferences
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            onSharedPreferenceChanged(sharedPrefs, "epic7PerfBattleCount");
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            if (pref instanceof EditTextPreference) {
                EditTextPreference listPref = (EditTextPreference) pref;
                pref.setSummary(listPref.getText());
            }
        }
    }

    /**
     * Claudia Preference Fragment
     */
    public static class ClaudiaPrefFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.app_preferences_claudia, false);

            // Load the app_preferences_fgo from an XML resource
            addPreferencesFromResource(R.xml.app_preferences_claudia);

            //if you are using default SharedPreferences
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            onSharedPreferenceChanged(sharedPrefs, "claudiaPerfBattleTimeout");
            onSharedPreferenceChanged(sharedPrefs, "claudiaPerfBattleCount");
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            if (pref instanceof EditTextPreference) {
                EditTextPreference listPref = (EditTextPreference) pref;
                pref.setSummary(listPref.getText());
            }
        }
    }
}

