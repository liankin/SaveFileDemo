package com.example.admin.savefiledemo;

import android.app.Application;
import android.support.think.util.CacheUtil;
import android.support.think.util.CrashHandler;

import org.xutils.x;

/**
 * Created by admin on 2017/11/15.
 */

public class BaseApplication extends Application{

    private static BaseApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        //初始化xUtils
        x.Ext.init(this);
        //异常捕获（因为要捕获全局异常，所以需在这里初始化）
        CacheUtil.build(this);
        CrashHandler.build(this);
    }
    public static BaseApplication getAppContext() {
        return mInstance;
    }
}
