package com.android.systemui.statusbar.phone;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.systemui.R;

public class EinkAppBleachDialog extends Dialog implements
        SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "EinkAppBleachDialog";

    private CheckBox cb_text;
    private TextView txt_icon;
    private SeekBar sb_icon;
    private TextView txt_cover;
    private SeekBar sb_cover;
    private TextView txt_bg;
    private SeekBar sb_bg;

    private Context mContext;
    private EinkSettingsManager mEinkSettingsManager;

    public EinkAppBleachDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eink_app_bleach_dialog);

        initView();

        if (mEinkSettingsManager == null) {
            mEinkSettingsManager = new EinkSettingsManager(mContext);
        }
    }

    private void initView() {
        cb_text = (CheckBox) findViewById(R.id.cb_eink_app_bleach_text_plus);
        cb_text.setChecked(EinkSettingsProvider.mIsAppBleachTextPlus);
        cb_text.setOnCheckedChangeListener(this);

        txt_icon = (TextView) findViewById(R.id.txt_eink_app_bleach_icon_color);
        txt_icon.setText(String.valueOf(EinkSettingsProvider.mAppBleachIconColor));
        sb_icon = (SeekBar) findViewById(R.id.sb_eink_app_bleach_icon_color);
        sb_icon.setProgress(EinkSettingsProvider.mAppBleachIconColor);
        sb_icon.setOnSeekBarChangeListener(this);

        txt_cover = (TextView) findViewById(R.id.txt_eink_app_bleach_cover_color);
        txt_cover.setText(String.valueOf(EinkSettingsProvider.mAppBleachCoverColor));
        sb_cover = (SeekBar) findViewById(R.id.sb_eink_app_bleach_cover_color);
        sb_cover.setProgress(EinkSettingsProvider.mAppBleachCoverColor);
        sb_cover.setOnSeekBarChangeListener(this);

        txt_bg = (TextView) findViewById(R.id.txt_eink_app_bleach_bg_color);
        txt_bg.setText(String.valueOf(EinkSettingsProvider.mAppBleachBgColor));
        sb_bg = (SeekBar) findViewById(R.id.sb_eink_app_bleach_bg_color);
        sb_bg.setProgress(EinkSettingsProvider.mAppBleachBgColor);
        sb_bg.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = seekBar.getId();
        if (id == R.id.sb_eink_app_bleach_icon_color) {
            txt_icon.setText(String.valueOf(progress));
        } else if (id == R.id.sb_eink_app_bleach_cover_color) {
            txt_cover.setText(String.valueOf(progress));
        } else if (id == R.id.sb_eink_app_bleach_bg_color) {
            txt_bg.setText(String.valueOf(progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int id = seekBar.getId();
        ContentValues values = new ContentValues();
        if (id == R.id.sb_eink_app_bleach_icon_color) {
            EinkSettingsProvider.mAppBleachIconColor = seekBar.getProgress();
            txt_icon.setText(String.valueOf(EinkSettingsProvider.mAppBleachIconColor));
            values.put(EinkSettingsDataBaseHelper.APP_BLEACH_ICON_COLOR,
                    EinkSettingsProvider.mAppBleachIconColor);
        } else if (id == R.id.sb_eink_app_bleach_cover_color) {
            EinkSettingsProvider.mAppBleachCoverColor = seekBar.getProgress();
            txt_cover.setText(String.valueOf(EinkSettingsProvider.mAppBleachCoverColor));
            values.put(EinkSettingsDataBaseHelper.APP_BLEACH_COVER_COLOR,
                    EinkSettingsProvider.mAppBleachCoverColor);
        } else if (id == R.id.sb_eink_app_bleach_bg_color) {
            EinkSettingsProvider.mAppBleachBgColor = seekBar.getProgress();
            txt_bg.setText(String.valueOf(EinkSettingsProvider.mAppBleachBgColor));
            values.put(EinkSettingsDataBaseHelper.APP_BLEACH_BG_COLOR,
                    EinkSettingsProvider.mAppBleachBgColor);
        }
        int result = mContext.getContentResolver().update(
                EinkSettingsProvider.URI_EINK_SETTINGS,
                values, EinkSettingsDataBaseHelper.PACKAGE_NAME + " = ?",
                new String[]{EinkSettingsProvider.packageName});
        Log.d(TAG, EinkSettingsProvider.packageName + " updated " + values
                + ", result=" + result);
        EinkSettingsManager.updateAppBleach(mContext);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        EinkSettingsProvider.mIsAppBleachTextPlus = isChecked;
        ContentValues values = new ContentValues();
        values.put(EinkSettingsDataBaseHelper.APP_BLEACH_TEXT_PLUS,
                EinkSettingsProvider.mIsAppBleachTextPlus ? 1 : 0);
        int result = mContext.getContentResolver().update(
                EinkSettingsProvider.URI_EINK_SETTINGS,
                values, EinkSettingsDataBaseHelper.PACKAGE_NAME + " = ?",
                new String[]{EinkSettingsProvider.packageName});
        Log.d(TAG, EinkSettingsProvider.packageName + " updated " + values
                + ", result=" + result);
        //mEinkSettingsManager.updateAppBleach();
        if (isChecked) {
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED, 1);
        } else {
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED, 0);
        }
    }

    @Override
    public void show() {
        super.show();
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        //layoutParams.gravity = Gravity.CENTER;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(layoutParams);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        cancel();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
