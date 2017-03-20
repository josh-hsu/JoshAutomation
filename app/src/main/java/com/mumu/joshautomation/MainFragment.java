package com.mumu.joshautomation;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

/**
 * MainFragment is a type of Fragment that organizes all abstract
 * functions to be implemented by its children
 */
public class MainFragment extends Fragment {
    final static String TAG = "FGOTool";
    protected FloatingActionButton mFab;

    public void onFabClick(View view){
        // Do nothing here , only in derived classes
    }

    public void onDetailClick() {
        // Do nothing here , only in derived classes
    }

    public void onSettingClick() {
        // Do nothing here , only in derived classes
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity activity = getMainActivity();

        if (activity != null) {
            mFab = activity.getFab();
        } else {
            Log.e(TAG, "WTF, Cannot get MainActivity from MainFragment!");
        }
    }

    public void setFabAppearance() {
        // Do nothing here , only in derived classes
    }

    protected MainActivity getMainActivity() {
        final Activity activity = getActivity();
        Log.d(TAG, "MainFragment onActivityCreated building link");
        if (activity instanceof MainActivity) {
            return (MainActivity) activity;
        }

        return null;
    }
}
