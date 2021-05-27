package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.os.EinkManager;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import android.content.Context;

import java.lang.reflect.Method;

public class EinkSettingsManager {
    private static final String TAG = "EinkSettingsManager";
    private static EinkManager mEinkManager;
    private Context mContext;
    public EinkSettingsManager(Context context) {
        mContext = context;
    }

    public void SetSystemDPI (final int density) {
        final int userId = UserHandle.myUserId();
        AsyncTask.execute(() -> {
            try {
                final IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
                Log.d(TAG,  "density: " + density);
                wm.setForcedDisplayDensityForUser(0, density, userId);
            } catch (RemoteException exc) {
                Log.w(TAG, "Unable to save forced display density setting");
            }
        });
    }

    public String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            value = (String)(get.invoke(c, key));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    public void setProperty(String key, String einkRefrshFrequency) {
        try {
            Class c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, einkRefrshFrequency);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEinkMode(String einkMode) {
        if (mEinkManager == null){
            mEinkManager = (EinkManager)mContext.getSystemService(Context.EINK_SERVICE);
        }
        mEinkManager.setMode(einkMode);
    }

    public String getEinkMode() {
        if (mEinkManager == null){
            mEinkManager = (EinkManager)mContext.getSystemService(Context.EINK_SERVICE);
        }
        return mEinkManager.getMode();
    }

    public void refreshAll() {
        if (mEinkManager == null){
            mEinkManager = (EinkManager)mContext.getSystemService(Context.EINK_SERVICE);
        }
        mEinkManager.sendOneFullFrame();
    }

    public int[] convertLevelToArray(int contrastLevel) {
        int contrast[] = new int[16];
        int mWhiteCount;
        int mBlackCount;
        if(contrastLevel < 80) {
            mWhiteCount = 2 + contrastLevel / 20;
            mBlackCount = 3 + contrastLevel / 10;
        }else {
            mWhiteCount = 5;
            mBlackCount = 11;
        }
        int whiteIndex = 16 - mWhiteCount;
        int remainderLevel = contrastLevel % 10;
        for(int i = 0, j = 1; i < 16; i++) {
            if(i < mBlackCount) {
                contrast[i] = 0;
            }else if(i >= whiteIndex) {
                contrast[i] = 15;
            }else {
               if(remainderLevel == 0) {
                   contrast[i] = i;
               }else if(i > remainderLevel) {
                   contrast[i] = i - remainderLevel;
               }else if(i <= remainderLevel) {
                   contrast[i] = j;
                   j++;
               }
            }
        }
        return contrast;
    }

    public String convertArrayToString(int contrast[]) {
        String strContrast = "0x";
        for(int i = 15; i >= 0; i--) {
            if(contrast[i] <= 9) {
                strContrast += contrast[i];
            }else {
                switch (contrast[i]) {
                    case 10:
                        strContrast += "a";
                        break;
                    case 11:
                        strContrast += "b";
                        break;
                    case 12:
                        strContrast += "c";
                        break;
                    case 13:
                        strContrast += "d";
                        break;
                    case 14:
                        strContrast += "e";
                        break;
                    case 15:
                        strContrast += "f";
                        break;
                }
            }
        }
        Log.d(TAG, "strContrast: " + strContrast);
        return strContrast;
    }

    public static void updateAppBleach(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append(EinkSettingsProvider.mIsAppBleach?1:0);
        sb.append(",");
        sb.append(EinkSettingsProvider.mAppBleachIconColor);
        sb.append(",");
        sb.append(EinkSettingsProvider.mAppBleachCoverColor);
        sb.append(",");
        sb.append(EinkSettingsProvider.mAppBleachBgColor);
        Settings.System.putString(context.getContentResolver(),
                "app_bleach_filter", sb.toString());
        context.sendBroadcast(new Intent("com.rockchip.eink.appcustom"));
    }
}
