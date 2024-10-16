package com.poorld.badget.app;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.BuildConfig;

import com.poorld.badget.utils.ConfigUtils;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;

public class MyApp extends Application {

    private static Context mContext;

    public static boolean isModuleActive = false;

    static {
        // Set settings before the main shell can be created
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_MOUNT_MASTER)
                .setTimeout(10));
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        ShellUtils.fastCmd("pm grant com.poorld.badget android.permission.READ_LOGS");
        //ConfigUtils.initConfig();
    }


    public static Context getContext() {
        return mContext;
    }
}
