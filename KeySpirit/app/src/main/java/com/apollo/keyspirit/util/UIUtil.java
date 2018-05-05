package com.apollo.keyspirit.util;

import android.os.Handler;
import android.os.Looper;

public class UIUtil {
    private static volatile UIUtil instance;
    private Handler mainHandler;

    private UIUtil() {
    }

    public static UIUtil getInstance() {
        if (instance == null) {
            synchronized (UIUtil.class) {
                if (instance == null) {
                    instance = new UIUtil();
                }
            }
        }
        return instance;
    }

    public void init() {
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void post2MainThread(Runnable runnable) {
        if (mainHandler != null) {
            mainHandler.post(runnable);
        }
    }
}
