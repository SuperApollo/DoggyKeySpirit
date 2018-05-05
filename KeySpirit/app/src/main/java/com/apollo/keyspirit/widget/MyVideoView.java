package com.apollo.keyspirit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.apollo.keyspirit.app.MyApplication;
import com.apollo.keyspirit.util.AppUtil;
import com.apollo.keyspirit.util.LogUtil;
import com.apollo.keyspirit.util.UIUtil;

public class MyVideoView extends VideoView {
    public static int mVideoWidth;

    public MyVideoView(Context context) {
        super(context);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mVideoWidth <= 0) {
            int screenWidth = AppUtil.getScreenWidth(MyApplication.getInstance().getContext());
            mVideoWidth = screenWidth;
            LogUtil.d("apollo","screenWidth: "+screenWidth);
        }


        super.onMeasure(MeasureSpec.makeMeasureSpec(mVideoWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mVideoWidth * 4 / 3, MeasureSpec.EXACTLY));
    }
}
