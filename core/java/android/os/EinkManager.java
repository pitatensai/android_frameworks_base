/*
 * Copyright (C) 2007 The Android Open Source Project
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

package android.os;
import android.os.IBinder;
import android.annotation.CallbackExecutor;
import android.annotation.CurrentTimeMillisLong;
import android.annotation.IntDef;
import android.annotation.IntRange;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.annotation.SdkConstant;
import android.annotation.SystemApi;
import android.annotation.SystemService;
import android.annotation.TestApi;
import android.compat.annotation.UnsupportedAppUsage;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.os.IEinkManager;
import android.os.SystemProperties;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ServiceManager;
@SystemService(Context.EINK_SERVICE)

public  class EinkManager {

    private static final String TAG = "EinkManager";
    private static final boolean DEBUG = true;
    private static boolean EINK = true;
    private static int num =1;
public class EinkMode {

    private EinkMode(){
    }
    
    public static final String EPD_NULL ="-1";
    public static final String EPD_AUTO ="0";
    public static final String EPD_OVERLAY ="1";
    public static final String EPD_FULL_GC16 ="2";
    public static final String EPD_FULL_GL16 ="3";
    public static final String EPD_FULL_GLR16 ="4";
    public static final String EPD_FULL_GLD16 ="5";
    public static final String EPD_FULL_GCC16 ="6";
    public static final String EPD_PART_GC16 ="7";
    public static final String EPD_PART_GL16 ="8";
    public static final String EPD_PART_GLR16 ="9";
    public static final String EPD_PART_GLD16 ="10";
    public static final String EPD_PART_GCC16 ="11";
    public static final String EPD_A2 ="12";
    public static final String EPD_DU ="13";
    public static final String EPD_DU4 ="14";
    public static final String EPD_A2_ENTER ="15";
    public static final String EPD_RESET ="16";
}


    private static void LOG(String msg) {
        if ( DEBUG ) {
            Log.d(TAG, msg);
        }
    }

    /*-------------------------------------------------------*/
    IEinkManager mService;
    final Context mContext;
    final Handler mHandler;

    /*-------------------------------------------------------*/


    /*
    public EinkManager(IEinkManager service) {
        mService = service;
    }*/

    /**
     * {@hide}
     */
    public EinkManager(@Nullable Context context,@Nullable IEinkManager service,@Nullable Handler handler) {
        LOG("EinkManager constructor");
        mContext = context;
        mService = service;
        mHandler = handler;
    }

    public static void setEinkEnabled(){
            EINK = true;
        }

    public static boolean getEinkEnabled(){
            return EINK;
        }

    public void sendOneFullFrame(){
        try {
            LOG("sendOneFullFrame");
            num = ++num;
            if(num > Integer.MAX_VALUE-100){
                num =1;
                }
            String numStr = num +"";
            mService.setProperty("sys.eink.one_full_mode_timeline",numStr);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
}
    public void setMode(@Nullable String einkMode){
        try {
            LOG("EinkManager.setMode");
            mService.setProperty("sys.eink.mode",einkMode);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    @Nullable public String getMode(){
		LOG("EinkManager.getMode");
        return SystemProperties.get("sys.eink.mode",EinkMode.EPD_PART_GC16);
    }

    public int init(){
        try {
            LOG("EinkManager.init()");
            return mService.init();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return -1;
        }
    }

    public int kill(){
        try {
            LOG("EinkManager.kill()");
            return mService.kill();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return -1;
        }
    }

        public int standby(){
        try {
            LOG("EinkManager.standby()");
            return mService.standby();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return -1;
        }
    }
        public int quitStandby(){
        try {
            LOG("EinkManager.quitStandby()");
            return mService.quitStandby();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return -1;
        }
    }
}
