package com.android.systemui.statusbar.phone;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import android.os.EinkManager;
import java.lang.reflect.Method;

public class EinkSettingsProvider extends ContentProvider {
    private static final String TAG = "EinkSettingsProvider";
    public static final int EINKSETTINGS = 0;
    public static final int EINKSETTINGS_UPDATE = 1;
    public static final int INIT_PROGRASS_DPI = 320;
    public static final int INIT_PROGRASS_REFRESH_FREQUENCY = 20;
    public static final String INIT_PROGRASS_CONTRAST = "0xffccba9876543000";
    private static final String COMMON_REFRESH_MODE = "7";
    private static final String AUTO_REFRESH_MODE = "0";
    private static final String A2_REFRESH_MODE = "12";
    public static String packageName = "";
    public static final String EINK_REFRESH_FREQUENCY = "persist.vendor.fullmode_cnt";
    public static final String EINK_CONTRAST = "persist.vendor.hwc.contrast_key";
    public static final String AUTHORITY = "com.android.systemui.eink";
    public static final String EINKSETTINGS_TABLE = "EinkSettings";
    public static final Uri URI_EINK_SETTINGS = Uri.parse("content://com.android.systemui.eink/einksettings");
    public static int DPI = 320;
    public static int refreshFrequency = 20;
    public static int contrast = 0;
    public static String strContrast = "0xffccba9876543000";
    public static String refreshMode = "7";
    public static int isRefreshSetting = 0;
    public static int mAppAnimFilter;//动画过滤
    public static boolean mIsAppBleach;//是否启用应用漂白
    public static boolean mIsAppBleachTextPlus;//字体增强
    public static int mAppBleachIconColor;//图标颜色
    public static int mAppBleachCoverColor;//封面颜色
    public static int mAppBleachBgColor;//背景颜色

    private static UriMatcher mUriMatcher;
    private EinkSettingsDataBaseHelper mEinkSettingsDataBaseHelper;
    private SQLiteDatabase mDB;
    private EinkSettingsManager mEinkSettingsManager;
    static {
        mUriMatcher = new UriMatcher((UriMatcher.NO_MATCH));
        mUriMatcher.addURI(AUTHORITY, "einksettings", EINKSETTINGS);
        mUriMatcher.addURI(AUTHORITY, "einksettingsupdate", EINKSETTINGS_UPDATE);
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate: ");
        mEinkSettingsDataBaseHelper = new EinkSettingsDataBaseHelper(getContext(), "Eink", null, 1);
        if (mEinkSettingsDataBaseHelper != null) {
            Log.d(TAG, "mEinkSettingsDataBaseHelper != null ");
            mDB = mEinkSettingsDataBaseHelper.getWritableDatabase();
            Log.d(TAG, "mDB: " + mDB);
        }
        if(mEinkSettingsManager == null) {
            mEinkSettingsManager = new EinkSettingsManager(getContext());
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query uri: " + uri);
        mDB = mEinkSettingsDataBaseHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (mUriMatcher.match(uri)) {
            case EINKSETTINGS:
                cursor = mDB.query(EINKSETTINGS_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case EINKSETTINGS_UPDATE:
                packageName = selectionArgs[0];
                Log.d(TAG, "packageName: " + packageName);
                cursor = mDB.query(EINKSETTINGS_TABLE, projection, selection,
                        selectionArgs, null, null, sortOrder);
                if(cursor.getCount() > 0) {
                    Log.d(TAG, "cursor.getCount() > 0 DPI: " + DPI);
                    if(cursor.moveToFirst()) {
                        //设置查到的DPI
                        DPI = cursor.getInt(cursor.getColumnIndex("app_dpi"));
                        Log.d(TAG, "DPI: " + DPI);
                        mEinkSettingsManager.SetSystemDPI(DPI);
                        //赋值查到的是否开启刷新模式
                        isRefreshSetting = cursor.getInt(cursor.getColumnIndex("is_refresh_setting"));
                        Log.d(TAG, "isRefreshSetting: " + isRefreshSetting);
                        //赋值查到的刷新模式
                        refreshMode = cursor.getString(cursor.getColumnIndex("refresh_mode"));
                        Log.d(TAG, "refreshMode: " + refreshMode);
                        //赋值查到的全刷频率
                        refreshFrequency = cursor.getInt(cursor.getColumnIndex("refresh_frequency"));
                        Log.d(TAG, "refreshFrequency: " + refreshFrequency);
                        //赋值查到的对比度
                        contrast = cursor.getInt(cursor.getColumnIndex("app_contrast"));
                        Log.d(TAG, "contrast: " + contrast);
                        if(contrast == 0) {
                            mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_CONTRAST, INIT_PROGRASS_CONTRAST);
                        } else {
                            strContrast = mEinkSettingsManager.convertArrayToString(mEinkSettingsManager.convertLevelToArray(contrast));
                            mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_CONTRAST, strContrast);
                        }
                        //刷新设置开启下才设置
                        if(isRefreshSetting == 1) {
                            //设置默认刷新模式
                            mEinkSettingsManager.setEinkMode(refreshMode);
                            //设置默认全刷频率
                            mEinkSettingsManager.setProperty(EINK_REFRESH_FREQUENCY, "" + refreshFrequency);
                        }
                        //动画过滤
                        mAppAnimFilter = cursor.getInt(cursor.getColumnIndex(
                                EinkSettingsDataBaseHelper.APP_ANIM_FILTER));
                        //应用漂白
                        mIsAppBleach = 1 == cursor.getInt(cursor.getColumnIndex(
                                EinkSettingsDataBaseHelper.APP_BLEACH_MODE));
                        mIsAppBleachTextPlus = 1 == cursor.getInt(cursor.getColumnIndex(
                                EinkSettingsDataBaseHelper.APP_BLEACH_TEXT_PLUS));
                        mAppBleachIconColor = cursor.getInt(cursor.getColumnIndex(
                                EinkSettingsDataBaseHelper.APP_BLEACH_ICON_COLOR));
                        mAppBleachCoverColor = cursor.getInt(cursor.getColumnIndex(
                                EinkSettingsDataBaseHelper.APP_BLEACH_COVER_COLOR));
                        mAppBleachBgColor = cursor.getInt(cursor.getColumnIndex(
                                EinkSettingsDataBaseHelper.APP_BLEACH_BG_COLOR));
                    }
                } else {
                    //设置默认DPI
                    mEinkSettingsManager.SetSystemDPI(INIT_PROGRASS_DPI);
                    //设置默认对比度
                    //strContrast = mEinkSettingsManager.convertContrastToString(EinkSettingsProvider.contrast);
                    mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_CONTRAST, INIT_PROGRASS_CONTRAST);
                    //设置默认刷新模式
                    mEinkSettingsManager.setEinkMode(EinkManager.EinkMode.EPD_PART_GC16);
                    //设置默认全刷频率
                    mEinkSettingsManager.setProperty(EINK_REFRESH_FREQUENCY, "" + INIT_PROGRASS_REFRESH_FREQUENCY);
                    mAppAnimFilter = 0;
                    mIsAppBleach = false;
                    mIsAppBleachTextPlus = false;
                    mAppBleachIconColor = 0;
                    mAppBleachCoverColor = 0;
                    mAppBleachBgColor = 0;
                }
                ContentResolver contentResolver = getContext().getContentResolver();
                if (mIsAppBleach && mIsAppBleachTextPlus) {
                    Settings.Secure.putInt(contentResolver,
                            Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED, 1);
                } else {
                    Settings.Secure.putInt(contentResolver,
                            Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED, 0);
                }
                EinkSettingsManager.updateAppBleach(getContext());
                break;
        }
        Log.d(TAG, "systemui query done");
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert ");
        mDB = mEinkSettingsDataBaseHelper.getWritableDatabase();
        switch (mUriMatcher.match(uri)) {
            case EINKSETTINGS:
                mDB.insert(EINKSETTINGS_TABLE, null, values);
                break;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        mDB = mEinkSettingsDataBaseHelper.getWritableDatabase();
        int updatedRows = 0;
        switch (mUriMatcher.match(uri)) {
            case EINKSETTINGS:
                updatedRows = mDB.delete(EINKSETTINGS_TABLE, selection, selectionArgs);
                mDB.close();
                break;
        }
        return updatedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        mDB = mEinkSettingsDataBaseHelper.getWritableDatabase();
        int updatedRows = 0;
        switch (mUriMatcher.match(uri)) {
            case EINKSETTINGS:
                updatedRows = mDB.update(EINKSETTINGS_TABLE, values, selection, selectionArgs);
                Log.d(TAG, "updatedRows: " + updatedRows);
                //mDB.close();
                break;
        }
        return updatedRows;
    }
}
