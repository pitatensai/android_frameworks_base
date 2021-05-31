package com.android.systemui.statusbar.phone;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.KeyEvent;
import android.view.WindowManager;

public class EinkBaseDialog extends Dialog {
    private Dialog mParentDialog;

    public EinkBaseDialog(Context context, Dialog parentDialog) {
        super(context);
        getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG));
        setCanceledOnTouchOutside(true);

        mParentDialog = parentDialog;
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
        if (null != mParentDialog && mParentDialog.isShowing()) {
            mParentDialog.cancel();
            mParentDialog = null;
        }
        return super.onKeyDown(keyCode, event);
    }
}
