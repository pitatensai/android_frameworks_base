package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EinkSettingsDataBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "EinkSettingsDataBaseHel";
    public static final String PACKAGE_NAME = "package_name";
    public static final String APP_DPI = "app_dpi";
    public static final String IS_DPI_SETTING = "is_dpi_setting";
    public static final String IS_REFRESH_SETTING = "is_refresh_setting";
    public static final String REFRESH_MODE = "refresh_mode";
    public static final String REFRESH_FREQUENCY = "refresh_frequency";
    public static final String APP_CONTRAST = "app_contrast";
    public static final String APP_ANIM_FILTER = "app_anim_filter";
    public static final String APP_BLEACH_MODE = "app_bleach_mode";//是否启用应用漂白
    public static final String APP_BLEACH_TEXT_PLUS = "app_bleach_text_plus";//字体增强
    public static final String APP_BLEACH_ICON_COLOR = "app_bleach_icon_color";//图标颜色
    public static final String APP_BLEACH_COVER_COLOR = "app_bleach_cover_color";//封面颜色
    public static final String APP_BLEACH_BG_COLOR = "app_bleach_bg_color";//背景颜色

    public EinkSettingsDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        Log.d(TAG, "EinkSettingsDataBaseHelper: ");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: ");
        int initDpi = Integer.parseInt(EinkSettingsManager.getProperty(
                EinkSettingsProvider.INIT_DPI_PROPERTY));
        final String CREATE_EINKSETTINGS = "create table EinkSettings (" +
                "id integer primary key autoincrement, " +
                PACKAGE_NAME + " text, " +
                /** DPI设置*/
                APP_DPI + " integer default '" + initDpi +"', " +
                IS_DPI_SETTING + " integer default '0', " +
                /** 刷新设置*/
                IS_REFRESH_SETTING + " integer default '0', " +
                REFRESH_MODE + " integer default '7', " +
                REFRESH_FREQUENCY + " integer default '20', " +
                /** 对比度设置*/
                APP_CONTRAST + " integer default '0', " +
                /** 动画过滤 */
                APP_ANIM_FILTER + " integer default '0', " +
                /** 应用漂白 */
                APP_BLEACH_MODE + " integer default '0', " +
                APP_BLEACH_TEXT_PLUS + " integer default '0', " +
                APP_BLEACH_ICON_COLOR + " integer default '0', " +
                APP_BLEACH_COVER_COLOR + " integer default '0', " +
                APP_BLEACH_BG_COLOR + " integer default '0'" +
                ")";
        db.execSQL(CREATE_EINKSETTINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: ");
    }
}
