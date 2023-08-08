package com.poorld.badget.app;

import android.app.Application;
import android.content.Context;

import com.poorld.badget.utils.ConfigUtils;

public class MyApp extends Application {

    private static Context mContext;

    public static boolean isModuleActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        //ConfigUtils.initConfig();
    }


    public static Context getContext() {
        return mContext;
    }
}
