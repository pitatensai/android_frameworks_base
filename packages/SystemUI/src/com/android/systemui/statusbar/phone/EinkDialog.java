package com.android.systemui.statusbar.phone;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.EinkManager;

import com.android.systemui.R;

/**
 * 创建自定义的Dialog，主要学习实现原理
 * Created by admin on 2017/8/30.
 */

public class EinkDialog extends EinkBaseDialog implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "EinkDialog";
    private Context mContext;
    private Button mRefreshButton;
    private SeekBar mDpiSeekbar, mAnimationSeekbar, mContrastSeekbar;
    private TextView mDpiText, mAnimationText, mContrastText;
    private CheckBox mDpiCheckbox, mContrastCheckbox, mRefreshCheckbox;
    private CheckBox mAppBleachCheckbox;
    private Button mAppBleachSetButton;
    private static final int SET_DPI_TEXT = 0;
    private static final int SET_DPI_SEEKBAR = 1;
    private static final int SET_IS_REFRESH_SETTING = 2;
    private static final int SET_CONTRAST_TEXT = 3;
    private static final int SET_CONTRAST_SEEKBAR = 4;
    private EinkRefreshDialog mEinkRefreshDialog;
    private EinkSettingsManager mEinkSettingsManager;
    private EinkAppBleachDialog mEinkAppBleachDialog;

    public Handler EinkDialogHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                /*case SET_DPI_TEXT:
                    mDpiText.setText(""+EinkSettingsProvider.DPI);
                    break;
                case SET_DPI_SEEKBAR:
                    mDpiSeekbar.setProgress(EinkSettingsProvider.DPI - EinkSettingsProvider.INIT_PROGRASS_DPI);
                    mDpiSeekbar.setEnabled(isChecked);
                    break;
                case SET_IS_REFRESH_SETTING:
                    if(EinkSettingsProvider.isRefreshSetting == 0)
                        mRefreshCheckbox.setChecked(false);
                    else if(EinkSettingsProvider.isRefreshSetting == 1)
                        mRefreshCheckbox.setChecked(true);
                    break;
                case SET_CONTRAST_TEXT:
                    mContrastText.setText("" + EinkSettingsProvider.contrast);
                    break;
                case SET_CONTRAST_SEEKBAR:
                    mContrastSeekbar.setProgress(EinkSettingsProvider.contrast);
                    break;
                case SET_IS_DPI_SETTING:
                    if(EinkSettingsProvider.isDpiSetting == 0)
                        mDpiCheckbox.setChecked(false);
                    else if(EinkSettingsProvider.isDpiSetting == 1)
                        mDpiCheckbox.setChecked(true);
                    break;*/
                case SET_DPI_SEEKBAR:
                    mDpiSeekbar.setEnabled(EinkSettingsProvider.isDpiSetting);
                    break;
                case SET_CONTRAST_SEEKBAR:
                    mContrastSeekbar.setEnabled(EinkSettingsProvider.isContrastSetting);
                    break;
            }
        }
    };

    public EinkDialog(Context context) {
        super(context, null);
        mContext = context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eink_menu_dialog);
        NavigationBarFragment.isShowEinkDialog = true;
        if(mEinkSettingsManager == null) {
            mEinkSettingsManager = new EinkSettingsManager(mContext);
        }
        //DPI设置
        mDpiText = (TextView) findViewById(R.id.eink_dialog_dpi_edit);
        mDpiText.setText(String.valueOf(EinkSettingsProvider.DPI));
        mDpiCheckbox = (CheckBox) findViewById(R.id.eink_dialog_dpi_checkbox);
        mDpiCheckbox.setChecked(EinkSettingsProvider.isDpiSetting);
        mDpiCheckbox.setOnCheckedChangeListener(this);
        mDpiSeekbar = (SeekBar) findViewById(R.id.eink_dialog_dpi_seekbar);
        mDpiSeekbar.setOnSeekBarChangeListener(this);
        mDpiSeekbar.setProgress(EinkSettingsProvider.DPI - EinkSettingsProvider.INIT_PROGRASS_DPI);
        mDpiSeekbar.setEnabled(mDpiCheckbox.isChecked());
        //对比度设置
        mContrastText = (TextView) findViewById(R.id.eink_dialog_contrast_edit);
        mContrastText.setText(String.valueOf(EinkSettingsProvider.contrast));
        mContrastCheckbox = (CheckBox) findViewById(R.id.eink_dialog_contrast_checkbox);
        mContrastCheckbox.setOnCheckedChangeListener(this);
        mContrastCheckbox.setChecked(EinkSettingsProvider.isContrastSetting);
        mContrastSeekbar = (SeekBar) findViewById(R.id.eink_dialog_contrast_seekbar);
        mContrastSeekbar.setOnSeekBarChangeListener(this);
        mContrastSeekbar.setProgress(EinkSettingsProvider.contrast);
        mContrastSeekbar.setEnabled(mContrastCheckbox.isChecked());
        //刷新设置
        mRefreshCheckbox = (CheckBox) findViewById(R.id.eink_dialog_refresh_checkbox);
        mRefreshCheckbox.setChecked(EinkSettingsProvider.isRefreshSetting);
        mRefreshCheckbox.setOnCheckedChangeListener(this);
        mRefreshButton = (Button) findViewById(R.id.eink_dialog_refresh_button);
        mRefreshButton.setOnClickListener(this);
        mRefreshButton.setEnabled(mRefreshCheckbox.isChecked());
        //应用漂白
        mAppBleachCheckbox = (CheckBox) findViewById(R.id.eink_dialog_bleach_checkbox);
        mAppBleachCheckbox.setChecked(EinkSettingsProvider.mIsAppBleach);
        mAppBleachCheckbox.setOnCheckedChangeListener(this);
        mAppBleachSetButton = (Button) findViewById(R.id.eink_dialog_bleach_button);
        mAppBleachSetButton.setOnClickListener(this);
        mAppBleachSetButton.setEnabled(mAppBleachCheckbox.isChecked());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.eink_dialog_refresh_button) {
            if(EinkSettingsProvider.isRefreshSetting) {
                if (null != mEinkRefreshDialog && mEinkRefreshDialog.isShowing()) {
                    return;
                }
                mEinkRefreshDialog = new EinkRefreshDialog(mContext, this);
                mEinkRefreshDialog.show();
            }
        } else if (id == R.id.eink_dialog_bleach_button) {
            if (EinkSettingsProvider.mIsAppBleach) {
                if (null != mEinkAppBleachDialog && mEinkAppBleachDialog.isShowing()) {
                    return;
                }
                mEinkAppBleachDialog = new EinkAppBleachDialog(mContext, this);
                mEinkAppBleachDialog.show();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = seekBar.getId();
        if(id == R.id.eink_dialog_dpi_seekbar) {
            if(EinkSettingsProvider.isDpiSetting) {
                EinkSettingsProvider.DPI = seekBar.getProgress() + EinkSettingsProvider.INIT_PROGRASS_DPI;
                mDpiText.setText(String.valueOf(EinkSettingsProvider.DPI));
            }
        } else if(id == R.id.eink_dialog_animation_seekbar) {
        } else if(id == R.id.eink_dialog_contrast_seekbar) {
            EinkSettingsProvider.contrast = seekBar.getProgress();
            mContrastText.setText(String.valueOf(EinkSettingsProvider.contrast));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int id = seekBar.getId();
        if(id == R.id.eink_dialog_dpi_seekbar) {
            if(EinkSettingsProvider.isDpiSetting) {
                //把dpi更新到数据库
                ContentValues values = new ContentValues();
                values.put(EinkSettingsDataBaseHelper.APP_DPI, EinkSettingsProvider.DPI);
                mContext.getContentResolver().update(EinkSettingsProvider.URI_EINK_SETTINGS,
                        values, EinkSettingsDataBaseHelper.PACKAGE_NAME + " = ?",
                        new String[]{EinkSettingsProvider.packageName});
                //设置dpi
                mEinkSettingsManager.setAppDPI(mContext);
            }
        } else if(id == R.id.eink_dialog_animation_seekbar) {
        } else if(id == R.id.eink_dialog_contrast_seekbar) {
            //设置对比度
            if(EinkSettingsProvider.contrast == 0) {
                mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_CONTRAST, EinkSettingsProvider.INIT_PROGRASS_CONTRAST);
            }else {
                EinkSettingsProvider.strContrast = mEinkSettingsManager.convertArrayToString(
                        mEinkSettingsManager.convertLevelToArray(EinkSettingsProvider.contrast));
                mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_CONTRAST, EinkSettingsProvider.strContrast);
            }
            //把对比度更新到数据库
            ContentValues values = new ContentValues();
            values.put(EinkSettingsDataBaseHelper.APP_CONTRAST, EinkSettingsProvider.contrast);
            mContext.getContentResolver().update(EinkSettingsProvider.URI_EINK_SETTINGS,
                    values, EinkSettingsDataBaseHelper.PACKAGE_NAME + " = ?",
                    new String[]{EinkSettingsProvider.packageName});
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if(id == R.id.eink_dialog_refresh_checkbox) {
            EinkSettingsProvider.isRefreshSetting = isChecked;
            ContentValues values = new ContentValues();
            values.put(EinkSettingsDataBaseHelper.IS_REFRESH_SETTING,
                    EinkSettingsProvider.isRefreshSetting?1:0);
            mContext.getContentResolver().update(EinkSettingsProvider.URI_EINK_SETTINGS,
                    values, EinkSettingsDataBaseHelper.PACKAGE_NAME + " = ?",
                    new String[]{EinkSettingsProvider.packageName});
            if(EinkSettingsProvider.isRefreshSetting) {
                mEinkSettingsManager.setEinkMode(String.valueOf(EinkSettingsProvider.refreshMode));
                mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_REFRESH_FREQUENCY,
                        String.valueOf(EinkSettingsProvider.refreshFrequency));
            } else {
                mEinkSettingsManager.setEinkMode(String.valueOf(EinkSettingsDataBaseHelper.INIT_REFRESH_MODE));
                mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_REFRESH_FREQUENCY,
                        String.valueOf(EinkSettingsDataBaseHelper.INIT_REFRESH_FREQUENCY));
            }
            mRefreshButton.setEnabled(isChecked);
        } else if (id == R.id.eink_dialog_bleach_checkbox) {
            EinkSettingsProvider.mIsAppBleach = isChecked;
            ContentValues values = new ContentValues();
            values.put(EinkSettingsDataBaseHelper.APP_BLEACH_MODE,
                    EinkSettingsProvider.mIsAppBleach?1:0);
            int result = mContext.getContentResolver().update(
                    EinkSettingsProvider.URI_EINK_SETTINGS,
                    values, EinkSettingsDataBaseHelper.PACKAGE_NAME + " = ?",
                    new String[]{ EinkSettingsProvider.packageName });
            Log.d(TAG, EinkSettingsProvider.packageName + " updated " + values
                + ", result=" + result);
            if (EinkSettingsProvider.mIsAppBleach && EinkSettingsProvider.mIsAppBleachTextPlus) {
                Settings.Secure.putInt(mContext.getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED, 1);
            } else {
                Settings.Secure.putInt(mContext.getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED, 0);
            }
            EinkSettingsManager.updateAppBleach(mContext);
            mAppBleachSetButton.setEnabled(isChecked);
        } else if(id == R.id.eink_dialog_dpi_checkbox) {
            EinkSettingsProvider.isDpiSetting = isChecked;
            ContentValues values = new ContentValues();
            values.put(EinkSettingsDataBaseHelper.IS_DPI_SETTING,
                    EinkSettingsProvider.isDpiSetting?1:0);
            mContext.getContentResolver().update(EinkSettingsProvider.URI_EINK_SETTINGS,
                    values, EinkSettingsDataBaseHelper.PACKAGE_NAME + " = ?",
                    new String[]{EinkSettingsProvider.packageName});
            //修改APP DPI
            mEinkSettingsManager.setAppDPI(mContext);
            Message setDPISeekbarMessage = new Message();
            setDPISeekbarMessage.what = SET_DPI_SEEKBAR;
            EinkDialogHandler.sendMessage(setDPISeekbarMessage);
        } else if(id == R.id.eink_dialog_contrast_checkbox) {
            EinkSettingsProvider.isContrastSetting = isChecked;
            ContentValues values = new ContentValues();
            values.put(EinkSettingsDataBaseHelper.IS_CONTRAST_SETTING,
                    EinkSettingsProvider.isContrastSetting?1:0);
            mContext.getContentResolver().update(EinkSettingsProvider.URI_EINK_SETTINGS,
                    values, EinkSettingsDataBaseHelper.PACKAGE_NAME + " = ?",
                    new String[]{EinkSettingsProvider.packageName});
            if(EinkSettingsProvider.isContrastSetting) {
                if(EinkSettingsProvider.contrast == 0) {
                    mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_CONTRAST, EinkSettingsProvider.INIT_PROGRASS_CONTRAST);
                } else {
                    EinkSettingsProvider.strContrast = mEinkSettingsManager.convertArrayToString(
                            mEinkSettingsManager.convertLevelToArray(EinkSettingsProvider.contrast));
                    mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_CONTRAST, EinkSettingsProvider.strContrast);
                }
            } else {
                mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_CONTRAST, EinkSettingsProvider.SYS_CONTRAST);
            }
            Message setContrastSeekbarMessage = new Message();
            setContrastSeekbarMessage.what = SET_CONTRAST_SEEKBAR;
            EinkDialogHandler.sendMessage(setContrastSeekbarMessage);
        }
    }

    public void dismissAllDialog() {
        if (null != mEinkRefreshDialog && mEinkRefreshDialog.isShowing()) {
            mEinkRefreshDialog.cancel();
            mEinkRefreshDialog = null;
        }
        if (null != mEinkAppBleachDialog && mEinkAppBleachDialog.isShowing()) {
            mEinkAppBleachDialog.cancel();
            mEinkAppBleachDialog = null;
        }
        cancel();
    }
}
