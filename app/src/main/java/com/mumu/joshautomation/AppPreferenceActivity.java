/*
 * Copyright (C) 2016 The Josh Tool Project
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

package com.mumu.joshautomation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mumu.joshautomation.script.AutoJobHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AppPreferenceActivity extends PreferenceActivity {
    public static final String TAG = "JATool";
    public boolean mHideOption = true;

    private static Context mContext; //this is a workaround, should be fixed later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        // Add a button to the header list.
        if (hasHeaders()) {
            Log.d(TAG, "Launched App preference header");
            Button button = new Button(this);
            button.setText(getString(R.string.settings_restore_data_from_sdcard));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mHideOption)
                        restoreDataFromSdcard();
                    else
                        openService();
                }
            });
            setListFooter(button);
        }
    }

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
     * This fragment shows the app_preferences_fgo for the first header.
     */
    public static class Prefs1Fragment extends PreferenceFragment {
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

            // Add start service button listener
            Preference myPref = (Preference) findPreference("enableServicePref");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    openService();
                    return true;
                }
            });
        }
    }

    public static class Prefs2Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.app_preferences, false);

            // Load the app_preferences_fgo from an XML resource
            addPreferencesFromResource(R.xml.app_preferences);

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
    }

    public static class Prefs3Fragment extends PreferenceFragment {
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

    public static class Prefs4Fragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
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
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

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

    static void openService() {
        mContext.startService(new Intent(mContext, HeadService.class));
    }

    void restoreDataFromSdcard() {
        MaterialDialog builder = new MaterialDialog.Builder(this)
                .title(getString(R.string.settings_restore_title))
                .content(getString(R.string.settings_restore_subtitle))
                .positiveText(getString(R.string.action_confirm))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        doRestoreDataFromSdcard();
                    }
                })
                .negativeText(getString(R.string.action_cancel)).show();
    }

    void doRestoreDataFromSdcard() {
        try {
            saveSdcardFileToData(getString(R.string.electric_data_file_name));
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Try to restore data from sdcard but no backup file found");
            new MaterialDialog.Builder(this)
                    .title(getString(R.string.settings_restore_failed))
                    .content(getString(R.string.settings_restore_not_found))
                    .negativeText(getString(R.string.action_cancel)).show();
            return;
        }

        new MaterialDialog.Builder(this)
                .title(getString(R.string.settings_restore_title))
                .content(getString(R.string.settings_restore_finished))
                .negativeText(getString(R.string.action_confirm)).show();
    }

    public void saveSdcardFileToData(String filename) throws FileNotFoundException {
        String userSdcardPath = Environment.getExternalStorageDirectory() + "/" + filename;
        File srcFile = new File(userSdcardPath);
        String destFilePath = getFilesDir().getAbsolutePath() + "/" + filename;

        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(new File(destFilePath));

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            Log.e(TAG, "Save " + filename + " to data failed: " + e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

}

