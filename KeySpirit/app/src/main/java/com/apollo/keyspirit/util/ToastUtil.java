package com.apollo.keyspirit.util;

import android.widget.Toast;

import com.apollo.keyspirit.app.MyApplication;

public class ToastUtil {
    private static Toast mToast;

    public static void show(int resId) {
        show(resId, Toast.LENGTH_SHORT);
    }

    public static void show(int resId, int duration) {
        show(MyApplication.getInstance().getContext().getResources().getText(resId), duration);
    }

    public static void show(CharSequence text) {
        show(text, Toast.LENGTH_SHORT);
    }

    public static void show(final CharSequence text, final int duration) {
        UIUtil.getInstance().post2MainThread(new Runnable() {
            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(MyApplication.getInstance().getContext(), text, duration);
                } else {
                    mToast.setText(text);
                }
                mToast.show();
            }
        });
    }
}
