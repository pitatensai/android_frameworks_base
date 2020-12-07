/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.server.eink;
import android.os.IBinder;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;


import android.os.IEinkManager;
import android.os.EinkManager;

import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.os.RemoteException;
import android.os.Message;
import android.os.Handler;
import android.os.SystemProperties;
import android.text.TextUtils;

public class EinkService extends IEinkManager.Stub {

    static {
        /*
         * Load the library.  If it's already loaded, this does nothing.
         */
        //zj rm
        //System.loadLibrary("rockchip_fm_jni");
    }
    private static final String TAG = "EinkService";
    private static final boolean DEBUG = true;
    private static final long delaytime = 10000;
    private static void LOG(String msg) {
        if ( DEBUG ) {
            Log.d(TAG, msg);
        }
    }
    private static final int SYSTEM_PROPERTY_MAX_LENGTH = 92;

    private Context mContext;
    private static final int SET_EINK = 1;
    private static Handler mPolicyHandler;
    private Listener binderListener;

    public static IBinder  mBinder;
    private class PolicyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SET_EINK:

            }
        }
    }
    private Runnable mEinkTask = new Runnable() {
        public void run() {
            LOG("EinkTask runing");
        }
    };
    private final class Listener implements IBinder.DeathRecipient {

        public void binderDied() {
            mBinder.unlinkToDeath(binderListener, 0);
            binderListener = null;
        }
    }

    public static IBinder getBinder() {
        return mBinder;
    }

    /*-------------------------------------------------------*/

    /**
     * Ctor.
     */
    public EinkService(Context context) {
        LOG("EinkService() : EinkService starting!.");
        mContext = context;
        mPolicyHandler = new PolicyHandler();
        return;
    }
    public void setProperty(String key, String value) {
        // Check if need to clear the property
        if (value == null) {
            // It's impossible to remove system property, therefore we check previous value to
            // avoid setting an empty string if the property wasn't set.
            if (TextUtils.isEmpty(SystemProperties.get(key))) {
                return;
            }
            value = "";
        } else if (value.length() > SYSTEM_PROPERTY_MAX_LENGTH) {
            LOG(value + " exceeds system property max length.");
            return;
        }

        try {
            SystemProperties.set(key, value);
        } catch (Exception e) {
            // Failure to set a property can be caused by SELinux denial. This usually indicates
            // that the property wasn't allowlisted in sepolicy.
            // No need to report it on all user devices, only on debug builds.
            LOG("Unable to set property " + key + " value '" + value + "'");
        }
    }

    public int init() {
        //mPolicyHandler.postDelayed(mEinkTask,delaytime);
        //mPolicyHandler.post(mEinkTask);
        init_native();
        return 0;
    }

    public int kill() {
        kill_native();
        return 0;
    }

    public int standby() {
           standby_native();
           return 0;
       }

    public int quitStandby() {
           quitStandby_native();
           return 0;
       }


/*jni interface*/
    public native int init_native();
    public native int kill_native();
    public native int standby_native();
    
    public native int quitStandby_native();
}


