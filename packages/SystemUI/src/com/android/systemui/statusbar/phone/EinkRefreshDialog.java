package com.android.systemui.statusbar.phone;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.EinkManager;

import com.android.systemui.R;

public class EinkRefreshDialog extends EinkBaseDialog implements View.OnClickListener, SeekBar.OnSeekBarChangeListener{
    private static final String TAG = "EinkRefreshDialog";
    private Context mContext;
    private Button mCommonButton,mAutoButton,mA2Button;
    private SeekBar mRefreshFrequencySeekbar;
    private TextView mRefreshFrequencyText;
    private EinkSettingsManager mEinkSettingsManager;
    private static final int SET_REFRESH_FREQUENCY_TEXT = 0;
    private static final int SET_REFRESH_FREQUENCY_SEEKBAR = 1;
    private static final int SET_REFRESH_MODE_BUTTON = 2;

    public Handler EinkRefreshDialogHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_REFRESH_FREQUENCY_TEXT:
                    mRefreshFrequencyText.setText(""+EinkSettingsProvider.refreshFrequency);
                    break;
                case SET_REFRESH_FREQUENCY_SEEKBAR:
                    mRefreshFrequencySeekbar.setProgress(EinkSettingsProvider.refreshFrequency);
                    break;
                case SET_REFRESH_MODE_BUTTON:
                    switch (EinkSettingsProvider.refreshMode) {
                        case EinkManager.EinkMode.EPD_PART_GC16:
                            mCommonButton.setBackgroundColor(Color.LTGRAY);
                            mAutoButton.setBackgroundColor(Color.WHITE);
                            mA2Button.setBackgroundColor(Color.WHITE);
                            break;
                        case EinkManager.EinkMode.EPD_AUTO:
                            mCommonButton.setBackgroundColor(Color.WHITE);
                            mAutoButton.setBackgroundColor(Color.LTGRAY);
                            mA2Button.setBackgroundColor(Color.WHITE);
                            break;
                        case EinkManager.EinkMode.EPD_A2:
                            mCommonButton.setBackgroundColor(Color.WHITE);
                            mAutoButton.setBackgroundColor(Color.WHITE);
                            mA2Button.setBackgroundColor(Color.LTGRAY);
                            break;
                    }
                    break;
            }
        }
    };

    public EinkRefreshDialog(Context context, Dialog parent) {
        super(context, parent);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eink_refresh_dialog);
        mCommonButton = (Button) findViewById(R.id.eink_refresh_dialog_mode_common_button);
        mAutoButton = (Button) findViewById(R.id.eink_refresh_dialog_mode_auto_button);
        mA2Button = (Button) findViewById(R.id.eink_refresh_dialog_mode_a2_button);
        mRefreshFrequencySeekbar = (SeekBar) findViewById(R.id.eink_refresh_dialog_frequency_seekbar);
        mRefreshFrequencyText = (TextView) findViewById(R.id.eink_refresh_dialog_frequency_text);
        if(mEinkSettingsManager == null) {
            mEinkSettingsManager = new EinkSettingsManager(mContext);
        }
        Message setRefreshFrequencyTextMessage = new Message();
        setRefreshFrequencyTextMessage.what = SET_REFRESH_FREQUENCY_TEXT;
        EinkRefreshDialogHandler.sendMessage(setRefreshFrequencyTextMessage);
        Message RefreshFrequencySeekbarMessage = new Message();
        RefreshFrequencySeekbarMessage.what = SET_REFRESH_FREQUENCY_SEEKBAR;
        EinkRefreshDialogHandler.sendMessage(RefreshFrequencySeekbarMessage);
        Message setRefreshModeButtonMessage = new Message();
        setRefreshModeButtonMessage.what = SET_REFRESH_MODE_BUTTON;
        EinkRefreshDialogHandler.sendMessage(setRefreshModeButtonMessage);
        mRefreshFrequencySeekbar.setOnSeekBarChangeListener(this);
        mCommonButton.setOnClickListener(this);
        mAutoButton.setOnClickListener(this);
        mA2Button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.d(TAG, "id: " + id);
        if(id == R.id.eink_refresh_dialog_mode_common_button) {
            String curMode = mEinkSettingsManager.getEinkMode();
            if(!EinkManager.EinkMode.EPD_PART_GC16.equals(curMode)){
                //更新refreshMode
                EinkSettingsProvider.refreshMode = EinkManager.EinkMode.EPD_PART_GC16;
                setRefreshUIandMode();
            } else {
                Log.d(TAG, "curMode: " + curMode);
            }
        } else if(id == R.id.eink_refresh_dialog_mode_auto_button) {
            String curMode = mEinkSettingsManager.getEinkMode();
            if(!EinkManager.EinkMode.EPD_AUTO.equals(curMode)){
                EinkSettingsProvider.refreshMode = EinkManager.EinkMode.EPD_AUTO;
                setRefreshUIandMode();
            } else {
                Log.d(TAG, "curMode: " + curMode);
            }
        } else if(id == R.id.eink_refresh_dialog_mode_a2_button) {
            String curMode = mEinkSettingsManager.getEinkMode();
            if(!EinkManager.EinkMode.EPD_A2.equals(curMode)){
                EinkSettingsProvider.refreshMode = EinkManager.EinkMode.EPD_A2;
                setRefreshUIandMode();
            } else {
                Log.d(TAG, "curMode: " + curMode);
            }
        }
        Log.d(TAG, "packageName: " + EinkSettingsProvider.packageName);
        ContentValues values = new ContentValues();
        values.put("refresh_mode", EinkSettingsProvider.refreshMode);
        int updatedRows = mContext.getContentResolver().update(EinkSettingsProvider.URI_EINK_SETTINGS,
                values, "package_name = ?", new String[]{EinkSettingsProvider.packageName});
        Log.d(TAG, "updatedRows: " + updatedRows);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = seekBar.getId();
        if(id == R.id.eink_refresh_dialog_frequency_seekbar) {
            EinkSettingsProvider.refreshFrequency = seekBar.getProgress();
            Log.d(TAG, "EinkSettingsProvider.refreshFrequency: " + EinkSettingsProvider.refreshFrequency);
            Message setRefreshFrequencyTextMessage = new Message();
            setRefreshFrequencyTextMessage.what = SET_REFRESH_FREQUENCY_TEXT;
            EinkRefreshDialogHandler.sendMessage(setRefreshFrequencyTextMessage);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int id = seekBar.getId();
        if(id == R.id.eink_refresh_dialog_frequency_seekbar) {
            Log.d(TAG, "eink_refresh_dialog_frequency_seekbar is onClick ");
            //设置全刷频率
            mEinkSettingsManager.setProperty(EinkSettingsProvider.EINK_REFRESH_FREQUENCY, "" + EinkSettingsProvider.refreshFrequency);
            //把全刷频率更新到数据库
            Log.d(TAG, "packageName: " + EinkSettingsProvider.packageName);
            ContentValues values = new ContentValues();
            values.put("refresh_frequency", EinkSettingsProvider.refreshFrequency);
            int updatedRows = mContext.getContentResolver().update(EinkSettingsProvider.URI_EINK_SETTINGS,
                    values, "package_name = ?", new String[]{EinkSettingsProvider.packageName});
            Log.d(TAG, "updatedRows: " + updatedRows);
        }
    }

    private void setRefreshUIandMode() {
        //设置UI
        Message setRefreshModeButtonMessage = new Message();
        setRefreshModeButtonMessage.what = SET_REFRESH_MODE_BUTTON;
        EinkRefreshDialogHandler.sendMessage(setRefreshModeButtonMessage);
        //设置刷新模式
        mEinkSettingsManager.setEinkMode(EinkSettingsProvider.refreshMode);
        mEinkSettingsManager.refreshAll();
    }
}
