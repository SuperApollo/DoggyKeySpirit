package com.apollo.keyspirit.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.apollo.keyspirit.BuildConfig;
import com.apollo.keyspirit.util.LogUtil;
import com.apollo.keyspirit.util.UIUtil;

public class MyApplication extends Application {
    private static MyApplication instance;
    private int appCount;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        init();
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public Context getContext() {
        return this.getApplicationContext();
    }

    /**
     * 一些初始化工作
     */
    private void init() {
        initLog();
        initUIUtil();
        initLisener();
    }

    private void initLisener() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                appCount++;
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                appCount--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public int getAppCount() {
        return appCount;
    }

    public void setAppCount(int appCount) {
        this.appCount = appCount;
    }


    /**
     * 初始化主线程切换工具类
     */
    private void initUIUtil() {
        UIUtil.getInstance().init();
    }

    /**
     * 初始化 log 打印
     */
    private void initLog() {
        LogUtil.logEnable = BuildConfig.LOG_ENABLE;
    }
}
