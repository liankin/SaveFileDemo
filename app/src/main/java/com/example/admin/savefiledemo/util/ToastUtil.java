package com.example.admin.savefiledemo.util;

import android.text.TextUtils;
import android.widget.Toast;

import com.example.admin.savefiledemo.BaseApplication;

/**
 * 统一管理Toast
 * Created by admin on 2017/11/15.
 */

public class ToastUtil {
    public static void showMessage(CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            Toast toast = Toast.makeText(BaseApplication.getAppContext(), text,
                    Toast.LENGTH_SHORT);
//            toast.getView().setBackgroundResource(R.mipmap.ic_launcher_round);
//            toast.getView().setPadding(45, 45, 45, 45);
            toast.show();
        }

    }
}
