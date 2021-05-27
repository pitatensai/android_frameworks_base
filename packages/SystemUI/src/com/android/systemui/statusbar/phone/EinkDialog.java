package com.android.systemui.statusbar.phone;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
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

public class EinkDialog extends Dialog implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "EinkDialog";
    private Context mContext;
    private Button mRefreshButton;
    private SeekBar mDpiSeekbar, mAnimationSeekbar, mContrastSeekbar;
    private TextView mDpiText, mAnimationText, mContrastText;
    private CheckBox mRefreshCheckbox;
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
                case SET_DPI_TEXT:
                    mDpiText.setText(""+EinkSettingsProvider.DPI);
                    break;
                case SET_DPI_SEEKBAR:
                    mDpiSeekbar.setProgress(EinkSettingsProvider.DPI - EinkSettingsProvider.INIT_PROGRASS_DPI);
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
            }
        }
    };

    public EinkDialog(Context context) {
        super(context);
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
        mDpiSeekbar = (SeekBar) findViewById(R.id.eink_dialog_dpi_seekbar);
        mDpiText = (TextView) findViewById(R.id.eink_dialog_dpi_edit);
        mContrastSeekbar = (SeekBar) findViewById(R.id.eink_dialog_contrast_seekbar);
        mContrastText = (TextView) findViewById(R.id.eink_dialog_contrast_edit);
        mRefreshButton = (Button) findViewById(R.id.eink_dialog_refresh_button);
        mRefreshCheckbox = (CheckBox) findViewById(R.id.eink_dialog_refresh_checkbox);
        Message setDPITextMessage = new Message();
        setDPITextMessage.what = SET_DPI_TEXT;
        EinkDialogHandler.sendMessage(setDPITextMessage);
        Message setDPISeekbarMessage = new Message();
        setDPISeekbarMessage.what = SET_DPI_SEEKBAR;
        EinkDialogHandler.sendMessage(setDPISeekbarMessage);
        Message setContrastTextMessage = new Message();
        setContrastTextMessage.what = SET_CONTRAST_TEXT;
        EinkDialogHandler.sendMessage(setContrastTextMessage);
        Message setContrastSeekbarMessage = new Message();
        setContrastSeekbarMessage.what = SET_CONTRAST_SEEKBAR;
        EinkDialogHandler.sendMessage(setContrastSeekbarMessage);
        Message setRefreshModeButtonMessage = new Message();
        setRefreshModeButtonMessage.what = SET_IS_REFRESH_SETTING;
        EinkDialogHandler.sendMessage(setRefreshModeButtonMessage);
        mDpiSeekbar.setOnSeekBarChangeListener(this);
        mContrastSeekbar.setOnSeekBarChangeListener(this);
        mRefreshButton.setOnClickListener(this);
        mRefreshCheckbox.setOnCheckedChangeListener(this);
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
        Log.d(TAG, "EinkSettingsProvider.isRefreshSetting: " + EinkSettingsProvider.isRefreshSetting);
        if(id == R.id.eink_dialog_refresh_button) {
            if(EinkSettingsProvider.isRefreshSetting == 1) {
                mEinkRefreshDialog = new EinkRefreshDialog(mContext);
                mEinkRefreshDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG));
                mEinkRefreshDialog.setCanceledOnTouchOutside(true);
                mEinkRefreshDialog.show();
            }
        } else if (id == R.id.eink_dialog_bleach_button) {
            if (EinkSettingsProvider.mIsAppBleach) {
                mEinkAppBleachDialog = new EinkAppBleachDialog(mContext);
                mEinkAppBleachDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG));
                mEinkAppBleachDialog.setCanceledOnTouchOutside(true);
                mEinkAppBleachDialog.show();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = seekBar.getId();
        if(id == R.id.eink_dialog_dpi_seekbar) {
            //从seekbar取dpi
            EinkSettingsProvider.DPI = seekBar.getProgress() + EinkSettingsProvider.INIT_PROGRASS_DPI;
            Log.d(TAG, "EinkSettingsProvider.DPI: " + EinkSettingsProvider.DPI);
            Message setDPITextMessage = new Message();
            setDPITextMessage.what = SET_DPI_TEXT;
            EinkDialogHandler.sendMessage(setDPITextMessage);
        } else if(id == R.id.eink_dialog_animation_seekbar) {
        } else if(id == R.id.eink_dialog_contrast_seekbar) {
            EinkSettingsProvider.contrast = seekBar.getProgress();
            Message setContrastTextMessage = new Message();
            setContrastTextMessage.what = SET_CONTRAST_TEXT;
            EinkDialogHandler.sendMessage(setContrastTextMessage);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int id = seekBar.getId();
        if(id == R.id.eink_dialog_dpi_seekbar) {
            Log.d(TAG, "eink_dialog_dpi_seekbar is onClick ");
            //设置dpi
            mEinkSettingsManager.SetSystemDPI(EinkSettingsProvider.DPI);
            //把dpi更新到数据库
            Log.d(TAG, "packageName: " + EinkSettingsProvider.packageName);
            ContentValues values = new ContentValues();
            values.put("app_dpi", EinkSettingsProvider.DPI);
            int updatedRows = mContext.getContentResolver().update(EinkSettingsProvider.URI_EINK_SETTINGS,
                    values, "package_name = ?", new String[]{EinkSettingsProvider.packageName});
            Log.d(TAG, "updatedRows: " + updatedRows);
            /*//全刷
            new Thread(){
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mEinkManager.sendOneFullFrame();
                }
            }.start();*/
        } else if(id == R.id.eink_dialog_animation_seekbar) {
        }else if(id == R.id.eink_dialog_contrast_seekbar) {
            Log.d(TAG, "eink_dialog_contrast_seekbar is onClick ");
            //设置对比度
            if(EinkSettingsProvider.contrast == 0) {
                mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_CONTRAST, EinkSettingsProvider.INIT_PROGRASS_CONTRAST);
            }else {
                EinkSettingsProvider.strContrast = mEinkSettingsManager.convertArrayToString(
                        mEinkSettingsManager.convertLevelToArray(EinkSettingsProvider.contrast));
                mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_CONTRAST, EinkSettingsProvider.strContrast);
            }
            //把对比度更新到数据库
            Log.d(TAG, "packageName: " + EinkSettingsProvider.packageName);
            ContentValues values = new ContentValues();
            values.put("app_contrast", EinkSettingsProvider.contrast);
            int updatedRows = mContext.getContentResolver().update(EinkSettingsProvider.URI_EINK_SETTINGS,
                    values, "package_name = ?", new String[]{EinkSettingsProvider.packageName});
            Log.d(TAG, "updatedRows: " + updatedRows);
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        Log.d(TAG, "id: " + id);
        if(id == R.id.eink_dialog_refresh_checkbox) {
            if(isChecked) {
                //修改标志位
                EinkSettingsProvider.isRefreshSetting = 1;
                //把标志位更新到数据库
                Log.d(TAG, "packageName: " + EinkSettingsProvider.packageName);
                ContentValues values = new ContentValues();
                values.put("is_refresh_setting", EinkSettingsProvider.isRefreshSetting);
                int updatedRows = mContext.getContentResolver().update(EinkSettingsProvider.URI_EINK_SETTINGS,
                        values, "package_name = ?", new String[]{EinkSettingsProvider.packageName});
                Log.d(TAG, "updatedRows: " + updatedRows);
                //根据属性值设置
                mEinkSettingsManager.setEinkMode(EinkSettingsProvider.refreshMode);
                mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_REFRESH_FREQUENCY, "" + EinkSettingsProvider.refreshFrequency);
            } else {
                //修改标志位
                EinkSettingsProvider.isRefreshSetting = 0;
                //把dpi更新到数据库
                Log.d(TAG, "packageName: " + EinkSettingsProvider.packageName);
                ContentValues values = new ContentValues();
                values.put("is_refresh_setting", EinkSettingsProvider.isRefreshSetting);
                int updatedRows = mContext.getContentResolver().update(EinkSettingsProvider.URI_EINK_SETTINGS,
                        values, "package_name = ?", new String[]{EinkSettingsProvider.packageName});
                Log.d(TAG, "updatedRows: " + updatedRows);
                //根据默认属性值设置
                //设置默认刷新模式
                mEinkSettingsManager.setEinkMode(EinkManager.EinkMode.EPD_PART_GC16);
                //设置默认全刷频率
                mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_REFRESH_FREQUENCY, "" + EinkSettingsProvider.INIT_PROGRASS_REFRESH_FREQUENCY);
            }
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
        }
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
