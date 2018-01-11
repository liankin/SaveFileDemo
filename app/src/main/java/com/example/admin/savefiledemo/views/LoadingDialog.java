package com.example.admin.savefiledemo.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;


import com.example.admin.savefiledemo.R;

import java.util.Date;

/**
 * Created by Administrator on 2017/4/21 0021.
 */
public class LoadingDialog extends Dialog {

    private Date curDate;
    public LoadingDialog(Context context) {
        super(context, R.style.alert_dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        setCanceledOnTouchOutside(false);

        // 将对话框的大小按屏幕大小的百分比设置
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        final WindowManager.LayoutParams p = getWindow().getAttributes();
        p.height = (int) (d.getHeight() * 0.6);
        p.width = (int) (d.getHeight() * 0.8);
        getWindow().setAttributes(p);
    }

}
