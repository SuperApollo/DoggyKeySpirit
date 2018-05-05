package com.apollo.keyspirit.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;


import java.lang.ref.WeakReference;

/**
 * 弹框工具类
 * Created by apollo on 17-3-15.
 */

public class MyPopUtil {
    private volatile static MyPopUtil mMyPopUtil = null;
    private WeakReference<Context> mContext;//必须是activity
    private int mLayoutId;//填充布局id
    private int mWidth;//显示位置
    private int mHeight;//显示位置
    private int mAnim;//显示的动画效果
    private View mPopView;//填充的布局
    private PopupWindow mMyPopupWindow;
    private final String TAG = MyPopUtil.this.getClass().getSimpleName();

    public Context getmContext() {
        return mContext.get();
    }

    public void setContext(Context mContext) {
        this.mContext = new WeakReference<>(mContext);
    }

    public View getmPopView() {
        return mPopView;
    }

    public void setmPopView(View mPopView) {
        this.mPopView = mPopView;
    }


    private MyPopUtil(Context context) {

        this.mContext = new WeakReference<>(context);
    }


    public static MyPopUtil getInstance(Context context) {
        if (mMyPopUtil == null) {
            synchronized (MyPopUtil.class) {
                if (mMyPopUtil == null) {
                    mMyPopUtil = new MyPopUtil(context);
                }
            }
        }
        return mMyPopUtil;
    }

    public View initView(int layoutId, int width, int height, int anim) {
        View popView = LayoutInflater.from(mContext.get()).inflate(layoutId, null);
        if (mMyPopupWindow == null)
            mMyPopupWindow = new PopupWindow(popView);
        mMyPopupWindow.setContentView(popView);//确保最新的popview
        mMyPopupWindow.setWidth(width);
        mMyPopupWindow.setHeight(height);
        mMyPopupWindow.setOutsideTouchable(true);
        mMyPopupWindow.setFocusable(true);
        //为了使点击popupwindow以外的地方使其能消失
        mMyPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mMyPopupWindow.setAnimationStyle(anim);

        mMyPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0f);
            }
        });
        mPopView = popView;
        return popView;
    }

    /**
     * 指定位置显示
     *
     * @param parent  父布局
     * @param gravity 布局
     * @param x       相对
     * @param y       相对
     */
    public void showAtLoacation(View parent, int gravity, int x, int y) {
        if (mMyPopupWindow != null) {
            this.mContext = new WeakReference<>(parent.getContext());//确保当前context引用指向最新的activity，防止半透明不生效
            mMyPopupWindow.showAtLocation(parent, gravity, x, y);
            backgroundAlpha(0.5f);
        }
    }

    /**
     * 显示在指定view下面
     *
     * @param anchor
     * @param x
     * @param y
     */
    public void showAsDropDown(View anchor, int x, int y) {
        if (mMyPopupWindow != null) {
            mMyPopupWindow.showAsDropDown(anchor, x, y);
            backgroundAlpha(0.5f);
        }
    }

    public void dismiss() {
        if (mMyPopupWindow != null) {
            if (mMyPopupWindow.isShowing())
                mMyPopupWindow.dismiss();
            System.gc();
            LogUtil.i(TAG, String.valueOf(mContext.get() == null));
        }
    }

    /**
     * 设置popupwindow以外背景透明度
     *
     * @param bgAlpha
     */
    private void backgroundAlpha(float bgAlpha) {
        Activity activity = (Activity) mContext.get();
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }
}
