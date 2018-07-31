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

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Cmd implements ServiceConnection {
    private String TAG = JoshGameLibrary.TAG;
    private boolean mInitialized = false;
    private Context mContext;
    private Method mRunCmdMethod;
    private boolean mPMPathAvailable = false;

    private boolean mHacked = false;
    private boolean mHackConnected = false;
    private IBinder mHackBinder;
    private String mSSPackageName, mSSServiceName, mSSInterfaceName;
    private int mSSCode = 0;

    Cmd(Context ctx) {
        mContext = ctx;
        mInitialized = true;

        try {
            Class<?>[] run_types = new Class[]{String.class, String.class};
            mRunCmdMethod = ctx.getPackageManager().getClass().getMethod("joshCmd", run_types);
            mPMPathAvailable = true;
        } catch (NoSuchMethodException e) {
            mPMPathAvailable = false;
            Log.e(TAG, "Sorry, your device is not support PackageManager command runner. Fix your sw or try HackBinder.");
        }
    }

    public String runCommand(String cmd) {
        try {
            if (!mHacked && mPMPathAvailable)
                runCmd(cmd);
            else if (mHacked && mHackConnected)
                runCmdHacked(cmd);
            else
                Log.d(TAG, "command " + cmd + " ignored, no way to set it.");
        } catch (Exception e)  {
            e.printStackTrace();
        }
        return null;
    }

    public void setHackParams(String pn, String sn, String in, int code) {
        mSSPackageName = pn;
        mSSServiceName = sn;
        mSSInterfaceName = in;
        mSSCode = code;
    }

    public void setHackSS(boolean hack) {
        mHacked = hack;
        if (mHacked && !mHackConnected)
            connectToHackSS();
        else if (!mHacked && mHackConnected)
            disconnectToHackSS();
    }

    public boolean isDeviceHacked() {
        boolean hackable;
        connectToHackSS();
        hackable = mHackConnected;
        if (hackable)
            disconnectToHackSS();
        return hackable;
    }

    public boolean isDeviceSupportPMPath() {
        return mPMPathAvailable;
    }

    private void runCmd(String cmd) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (mInitialized) {
            mRunCmdMethod.invoke(mContext, cmd, "");
        }
    }

    synchronized private void runCmdHacked(String cmd) {
        if (mHackConnected && mHackBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            if (mSSInterfaceName != null && !mSSInterfaceName.equals(""))
                data.writeInterfaceToken(mSSPackageName + mSSInterfaceName);
            data.writeString(cmd);
            try {
                mHackBinder.transact(mSSCode, data, reply, 0);
            } catch (RemoteException e) {
                Log.d(TAG, "transact failed " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    synchronized private void connectToHackSS() {
        if (!mHackConnected) {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(mSSPackageName,
                        mSSPackageName + mSSServiceName));
                if (!mContext.bindService(intent, this, Service.BIND_AUTO_CREATE)) {
                    Log.d(TAG, "Cannot hack this device, parameter wrong or you don't have implement service");
                    mHackConnected = false;
                }
            } catch (SecurityException e) {
                Log.e(TAG, "can't bind to Service. Your service is not implemented correctly");
                return;
            }
            mHackConnected = true;
        } else {
            Log.d(TAG, "Hack service is already connected");
        }
    }

    private void disconnectToHackSS() {
        if (mHackConnected)
            mContext.unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mHackConnected = true;
        mHackBinder = service;
        Log.d(TAG, "Hack service connected.");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mHackConnected = false;
        Log.d(TAG, "Hack service disconnected.");
    }
}
