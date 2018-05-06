package com.apollo.keyspirit.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.apollo.keyspirit.R;
import com.apollo.keyspirit.app.AppConfig;
import com.apollo.keyspirit.app.MyApplication;
import com.apollo.keyspirit.constants.Constants;
import com.apollo.keyspirit.retrofit.model.AppUpdateInfoModel;
import com.apollo.keyspirit.service.SwipeService;
import com.apollo.keyspirit.util.ApkUpdateUtil;
import com.apollo.keyspirit.util.AppUtil;
import com.apollo.keyspirit.util.MyPopUtil;
import com.apollo.keyspirit.util.SharedPreferencesUtils;
import com.apollo.keyspirit.util.TapEventUtil;
import com.apollo.keyspirit.util.ToastUtil;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    @BindView(R.id.et_input_x)
    EditText mEtInputX;
    @BindView(R.id.et_input_y)
    EditText mEtInputY;
    @BindView(R.id.btn_start)
    Button mBtnStart;
    @BindView(R.id.btn_stop)
    Button mBtnStop;
    @BindView(R.id.animation_top_test)
    LottieAnimationView mAnimTopTest;
    @BindView(R.id.animation_after_test)
    LottieAnimationView mAnimAfterTest;
    @BindView(R.id.et_package_name)
    EditText mEtPackageName;
    @BindView(R.id.et_input_swipe_H)
    EditText mEtInputH;
    @BindView(R.id.et_input_swipe_W)
    EditText mEtinputW;

    private final int SERVER_VERSION_ERROR = 0x00010086;
    private final int HAS_UPDATE = 0x00010087;
    private final int NO_UPDATE = 0x00010088;

    private View mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        checkUpdate();
//        test();

        Intent intent = new Intent(this, SwipeService.class);
        startService(intent);
    }

    /**
     * 检查更新
     */
    private void checkUpdate() {
        new CheckUpdateTask().execute();

    }

    private void test() {
        TapEventUtil.execShellCmd("getevent -p");
        TapEventUtil.execShellCmd("sendevent /dev/input/event0 1 158 1");
        TapEventUtil.execShellCmd("sendevent /dev/input/event0 1 158 0");
        TapEventUtil.execShellCmd("input keyevent 3");//home
        TapEventUtil.execShellCmd("input text  'helloworld!' ");
        TapEventUtil.execShellCmd("input tap 168 252");
        TapEventUtil.execShellCmd("input swipe 100 250 200 280");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        mRootView = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_main, null);
    }

    @OnClick(R.id.btn_start)
    public void start() {
        if (!hasRootPermission) {
            ToastUtil.show("啊哦，您的手机似乎没有 root 权限！");
            return;
        }
//        handleInput();
//        //清空之前的消息
//        mHandler.removeCallbacksAndMessages(null);
//        Message msg = Message.obtain();
//        msg.what = LAUNCH_APP;
//        mHandler.sendMessageDelayed(msg, 3000);
//        //复位
//        stopSwipeAndTap = false;
//        swipTimes = 0;
//        tapTimes = 0;
//        detailSwipeTimes = 0;

        Intent intent = new Intent(Constants.MY_BROADCUST_RECEIVER_ACTION);
        intent.putExtra(Constants.BROADCUST_COMMAND, Constants.COMMAND_START);
        sendBroadcast(intent);
    }

    @OnClick(R.id.btn_next_page)
    public void nextPage() {
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_to_surface)
    public void toSurface() {
        Intent intent = new Intent(MainActivity.this, SurfaceViewTestActivity.class);
        startActivity(intent);
    }

    /**
     * 处理输入
     */
//    private void handleInput() {
//        String inputPackageName = mEtPackageName.getText().toString().trim();
//        if (!TextUtils.isEmpty(inputPackageName)) {
//            packageName = inputPackageName;
//        }
//        String inputX = mEtInputX.getText().toString().trim();
//        if (!TextUtils.isEmpty(inputX)) {
//            int xInt = Integer.parseInt(inputX);
//            if (xInt > 0) {
//                tapX = xInt;
//            }
//        }
//
//        String inputY = mEtInputY.getText().toString().trim();
//        if (!TextUtils.isEmpty(inputY)) {
//            int yInt = Integer.parseInt(inputY);
//            if (yInt > 0) {
//                tapY = yInt;
//            }
//        }
//        String inputH = mEtInputH.getText().toString().trim();
//        if (!TextUtils.isEmpty(inputH)) {
//            int hInt = Integer.parseInt(inputH);
//            if (hInt != 0) {
//                swipeH = -Math.abs(hInt);
//            }
//        }
//        String inputW = mEtinputW.getText().toString().trim();
//        if (!TextUtils.isEmpty(inputW)) {
//            int wInt = Integer.parseInt(inputW);
//            if (wInt != 0) {
//                swipeW = wInt;
//            }
//        }
//    }
    @OnClick(R.id.btn_stop)
    public void stop() {
        Intent intent = new Intent(Constants.MY_BROADCUST_RECEIVER_ACTION);
        intent.putExtra(Constants.BROADCUST_COMMAND, Constants.COMMAND_STOP);
        sendBroadcast(intent);
    }


    private class CheckUpdateTask extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String json = null;
            try {
                //从URL加载document对象
                Document document = Jsoup.connect(Constants.CHECK_UPDATE_GITHUB_URL).get();
                Element body = document.body();
                json = body.text();

            } catch (IOException e) {
                e.printStackTrace();
            }


            return json;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            parseData((String) o);
        }
    }

    private void parseData(String json) {
        AppUpdateInfoModel updateInfoModel = null;
        try {
            Gson gson = new Gson();
            if (json != null) {
                updateInfoModel = gson.fromJson(json, AppUpdateInfoModel.class);
            }

            if (updateInfoModel == null) {
                ToastUtil.show("服务器开小差");
                return;
            }
            String serverVersion = updateInfoModel.getAppVersion();
            String localVersion = AppUtil.getAppVersionName(MyApplication.getInstance().getContext());
            SharedPreferencesUtils.putString(AppConfig.APK_URL, updateInfoModel.getAppUrl());
            int toUpdate = toUpdate(serverVersion, localVersion);
            switch (toUpdate) {
                case HAS_UPDATE:
                    chooseDialogShow(serverVersion, updateInfoModel.getAppUrl(), updateInfoModel.getAppSize(), updateInfoModel.getAppDescription());
                    SharedPreferencesUtils.putBoolean(AppConfig.HAS_UPDATE, true);
                    break;
                case NO_UPDATE:
                    ToastUtil.show("当前已是最新版本");
                    SharedPreferencesUtils.putBoolean(AppConfig.HAS_UPDATE, false);
                    break;
                case SERVER_VERSION_ERROR:
                    ToastUtil.show("服务器版本号错误");
                    SharedPreferencesUtils.putBoolean(AppConfig.HAS_UPDATE, false);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 判断是否升级
     *
     * @param appVersion
     * @return
     */
    private int toUpdate(String appVersion, String currentVersionName) {

        if (!TextUtils.isEmpty(appVersion) && !TextUtils.isEmpty(currentVersionName)) {
            String[] versionStr = new String[]{
                    appVersion, currentVersionName
            };
            int[] versionInt = versionName2Int(versionStr);

            if (versionInt[0] == -1) {
                return SERVER_VERSION_ERROR;
            } else if (versionInt[0] > versionInt[1]) {
                return HAS_UPDATE;
            } else {
                return NO_UPDATE;
            }
        }
        return NO_UPDATE;
    }

    /**
     * 版本号转换为int，便于比较
     *
     * @param versions
     * @return
     */
    private int[] versionName2Int(String[] versions) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        String[] s = versions[0].split("\\.");
        String[] s1 = versions[1].split("\\.");
        for (String ss : s) {
            sb.append(ss);
        }
        for (String ss1 : s1) {
            sb1.append(ss1);
        }
        int var = sb.length() - sb1.length();
        if (var > 0) {
            for (int i = 0; i < var; i++) {
                sb1.append("0");
            }
        } else if (var < 0) {
            for (int i = 0; i < -var; i++) {
                sb.append("0");
            }
        }

        int versino1 = -1;
        int version2 = -1;
        try {
            versino1 = Integer.parseInt(sb.toString());
            version2 = Integer.parseInt(sb1.toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        int[] versionsInt = new int[]{versino1, version2};

        return versionsInt;
    }

    /**
     * 是否升级弹框
     *
     * @param curVersion
     * @param appUrl
     * @param appSize
     * @param appDescription
     */
    private void chooseDialogShow(String curVersion, final String appUrl, String appSize, String appDescription) {
        MyPopUtil myPopUtil = MyPopUtil.getInstance(MainActivity.this);
        myPopUtil.initView(R.layout.new_update_pop, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                R.style.add_pop_tv_style);
        if (mRootView == null) {
            return;
        }

        myPopUtil.showAtLoacation(mRootView, Gravity.CENTER, 0, 0);
        TextView tv_new_update_num = myPopUtil.getmPopView().findViewById(R.id.tv_new_update_num);
        tv_new_update_num.setText("版本号：" + curVersion);
        TextView tvDescription = myPopUtil.getmPopView().findViewById(R.id.tv_new_update_description);
        tvDescription.setMovementMethod(ScrollingMovementMethod.getInstance());
        LinearLayout ll_new_update_description = myPopUtil.getmPopView().findViewById(R.id.ll_new_update_description);
        if (!TextUtils.isEmpty(appDescription)) {
            tvDescription.setText(appDescription + "\n【更新包大小】" + appSize);
            ll_new_update_description.setVisibility(View.VISIBLE);
        }

        TextView tv_cancel = myPopUtil.getmPopView().findViewById(R.id.tv_new_update_pop_cancel);
        TextView tv_ok = myPopUtil.getmPopView().findViewById(R.id.tv_new_update_pop_ok);
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyPopUtil.getInstance(MainActivity.this).dismiss();
            }
        });
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyPopUtil.getInstance(MainActivity.this).dismiss();
                try {//防止apprUrl错误造成崩溃
                    ApkUpdateUtil apkUpdateUtil = new ApkUpdateUtil(MainActivity.this, appUrl);
                    apkUpdateUtil.startDown();
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.show(e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
