package com.mumu.android.joshautomation.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mumu.android.joshautomation.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "JATool";
    public static final int FRAG_IDX_OUTLINE = 0;
    public static final int FRAG_IDX_GL20 = 1;

    private FloatingActionButton mFab;
    private View mCoordinateLayoutView;
    private List<MainFragment> mFragmentList;
    private MainFragment mCurrentPresentFragment;
    private boolean mShouldStartBugReport = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Save the coordinate layout view for animated elements
        mCoordinateLayoutView = findViewById(R.id.coordinator_layout);

        // construct fragment list
        initFragmentList();

        mFab = (FloatingActionButton) findViewById(R.id.fab);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        showOutlineFragment();

        requestPermissionsOnStart();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startPreferenceActivity();
            return true;
        } else if (id == R.id.action_detail) {
            if (mCurrentPresentFragment != null) {
                mCurrentPresentFragment.onDetailClick();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        // Create a new fragment and specify the fragment to show based on nav item clicked
        final MainFragment fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        ActionBar actionBar = getSupportActionBar();

        int id = item.getItemId();

        if (id == R.id.nav_outline) {
            fragment = mFragmentList.get(FRAG_IDX_OUTLINE);
            if (actionBar != null) actionBar.setTitle(getString(R.string.drawer_outline));
            mFab.setVisibility(View.INVISIBLE);
        } else if (id == R.id.nav_share) {
            fragment = null;
            showSnackBarMessage("Share function implementing");
        } else if (id == R.id.nav_gl20) {
            fragment = mFragmentList.get(FRAG_IDX_GL20);
            if (actionBar != null) actionBar.setTitle(getString(R.string.drawer_gl20));
            mFab.setVisibility(View.VISIBLE);
            showSnackBarMessage("GL20 test is now beta");
        } else if (id == R.id.nav_send) {
            fragment = null;
            mShouldStartBugReport = true;
            showSnackBarMessage(getString(R.string.bugreport_permission_request));
            requestPermissions();
        } else {
            return false;
        }

        // null fragment means this action doesn't need to transfer to other fragment
        if (fragment != null) {
            mCurrentPresentFragment = fragment;

            // Insert the fragment by replacing any existing fragment
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
            // Setting onClickListener
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fragment.onFabClick(view);
                }
            });
        }

        // Dismiss drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 10) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted
                    Toast.makeText(MainActivity.this, R.string.startup_permit_system_alarm_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (permsRequestCode) {
            case 200:
                boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (writeAccepted && mShouldStartBugReport) {
                    startBugReportActivity();
                } else {
                    Toast.makeText(this, getString(R.string.bugreport_permission_not_grant), Toast.LENGTH_LONG).show();
                    Log.w(TAG, "User didn't give us permission to send out bugreport");
                }
                break;
            case 201:
                writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!writeAccepted) {
                    Toast.makeText(this, getString(R.string.bugreport_permission_not_grant), Toast.LENGTH_LONG).show();
                    Log.w(TAG, "User didn't give us permission to send out bugreport");
                    requestPermissionsOnStart();
                }
                break;
            default:
                Toast.makeText(this, "No handle permission grant", Toast.LENGTH_LONG).show();
        }
    }

    private void initFragmentList() {
        mFragmentList = new ArrayList<>();
        try {
            mFragmentList.add(FRAG_IDX_OUTLINE, OutlineFragment.class.newInstance());
            mFragmentList.add(FRAG_IDX_GL20, GL20Fragment.class.newInstance());
        } catch (Exception e) {
            Log.e(TAG, "initOnce fragment list failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public FloatingActionButton getFab() {
        return mFab;
    }

    public void showSnackBarMessage(String msg) {
        Snackbar.make(mCoordinateLayoutView, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void startPreferenceActivity() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, AppPreferenceActivity.class);
        startActivity(intent);
    }

    private void requestPermissions() {
        String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.FOREGROUND_SERVICE"};
        int permsRequestCode = 200;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d(TAG, "This is device software version above Marshmallow, requesting permission of external storage");
            requestPermissions(perms, permsRequestCode);
        }
    }

    private void requestPermissionsOnStart() {
        String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.FOREGROUND_SERVICE"};
        int permsRequestCode = 201;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d(TAG, "This is device software version above Marshmallow, requesting permission of external storage");
            requestPermissions(perms, permsRequestCode);
        }
    }

    private void showOutlineFragment() {
        final MainFragment fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        ActionBar actionBar = getSupportActionBar();

        fragment = mFragmentList.get(FRAG_IDX_OUTLINE);
        if (actionBar != null) actionBar.setTitle(getString(R.string.drawer_outline));

        if (fragment != null) {
            mCurrentPresentFragment = fragment;

            // Insert the fragment by replacing any existing fragment
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
            // Setting onClickListener
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fragment.onFabClick(view);
                }
            });
            mFab.setVisibility(View.INVISIBLE);
        }
    }

    private void startBugReportActivity() {
        File file_electricity = new File(Environment.getExternalStorageDirectory(), getString(R.string.electric_data_file_name));
        Uri path_electricity = Uri.fromFile(file_electricity);
        File file_log = new File(Environment.getExternalStorageDirectory(), "log.txt");
        Uri path_log = Uri.fromFile(file_log);
        String to[] = {"alenbos0517@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        saveDataFileToSdcard(getString(R.string.electric_data_file_name));
        saveDataFileToSdcard("log.txt");
        emailIntent.setType("vnd.android.cursor.dir/email");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.bugreport_subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.bugreport_context));

        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(path_electricity);
        uris.add(path_log);
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

        startActivity(Intent.createChooser(emailIntent , getString(R.string.bugreport_send_out_via)));

        mShouldStartBugReport = false;
    }

    private void saveDataFileToSdcard(String filename) {
        String userDataPath = getFilesDir().getAbsolutePath() + "/" + filename;
        File srcFile = new File(userDataPath);
        String destFilePath = Environment.getExternalStorageDirectory() + "/" + filename;

        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(new File(destFilePath));

            int read;
            byte[] bytes = new byte[1024];

            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        } catch (IOException e) {
            Log.e(TAG, "Save " + filename + " to sdcard failed: " + e.getMessage());
            e.printStackTrace();
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
