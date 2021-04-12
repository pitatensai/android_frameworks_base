package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EinkSettingsDataBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "EinkSettingsDataBaseHel";
    private static final String CREATE_EINKSETTINGS = "create table EinkSettings (" +
            "id integer primary key autoincrement, " +
            "package_name text, " +
            "app_dpi integer, " +
            "is_refresh_setting integer, " +
            "refresh_mode text, " +
            "refresh_frequency integer, " +
            "app_contrast integer)";

    public EinkSettingsDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        Log.d(TAG, "EinkSettingsDataBaseHelper: ");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: ");
        db.execSQL(CREATE_EINKSETTINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: ");
    }
}
