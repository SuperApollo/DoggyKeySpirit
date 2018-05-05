package com.apollo.keyspirit.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.apollo.keyspirit.util.ApkUpdateUtil;
import com.apollo.keyspirit.util.AppUtil;
import com.apollo.keyspirit.util.LogUtil;
import com.apollo.keyspirit.util.MyPopUtil;
import com.apollo.keyspirit.util.SharedPreferencesUtils;
import com.apollo.keyspirit.util.SystemUtil;
import com.apollo.keyspirit.util.TapEventUtil;
import com.apollo.keyspirit.util.ToastUtil;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

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


    MyHandler mHandler = new MyHandler(this);
    private static final int LAUNCH_APP = 0x01;
    private static final int EXEC_SWIPE_ITEM = 0x02;//滑动列表
    private static final int EXEC_TAP_ITEM = 0x03;//点击新闻列表条目
    private static final int EXEC_TAP_BACK = 0x04;//点击返回按钮
    private static final int EXEC_SWIPE_DETAIL = 0x05;//滑动新闻详情
    private int swipTimes;//滑动的次数
    private int tapTimes;//点击的次数
    private int detailSwipeTimes;//新闻详情页滑动的次数
    private boolean stopSwipeAndTap = false;
    private final String NETEASE_PACKAGE = "com.netease.newsreader.activity";
    private final String DISCOUNT_PACKAGE = "com.apollo.discounthunter";
    private final String CASH_RED_PACKET = "com.martian.hbnews";
    private String packageName = CASH_RED_PACKET;//要打开的 APP 包名，默认值网易
    private int tapX = 0;//默认点击的点 x 坐标
    private int tapY = 0;//默认点击的点 y坐标
    private int swipeH = -400;//默认垂直方向滑动距离
    private int swipeW = 200;//默认水平方向滑动距离
    private View mRootView;
    private int tapbackTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        checkUpdate();
//        test();
        swipeH = -SystemUtil.dp2px(MyApplication.getInstance().getContext(), 133);
        swipeW = SystemUtil.dp2px(MyApplication.getInstance().getContext(), 66);
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
        handleInput();
        //清空之前的消息
        mHandler.removeCallbacksAndMessages(null);
        Message msg = Message.obtain();
        msg.what = LAUNCH_APP;
        mHandler.sendMessageDelayed(msg, 3000);
        //复位
        stopSwipeAndTap = false;
        swipTimes = 0;
        tapTimes = 0;
        detailSwipeTimes = 0;
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
    private void handleInput() {
        String inputPackageName = mEtPackageName.getText().toString().trim();
        if (!TextUtils.isEmpty(inputPackageName)) {
            packageName = inputPackageName;
        }
        String inputX = mEtInputX.getText().toString().trim();
        if (!TextUtils.isEmpty(inputX)) {
            int xInt = Integer.parseInt(inputX);
            if (xInt > 0) {
                tapX = xInt;
            }
        }

        String inputY = mEtInputY.getText().toString().trim();
        if (!TextUtils.isEmpty(inputY)) {
            int yInt = Integer.parseInt(inputY);
            if (yInt > 0) {
                tapY = yInt;
            }
        }
        String inputH = mEtInputH.getText().toString().trim();
        if (!TextUtils.isEmpty(inputH)) {
            int hInt = Integer.parseInt(inputH);
            if (hInt != 0) {
                swipeH = -Math.abs(hInt);
            }
        }
        String inputW = mEtinputW.getText().toString().trim();
        if (!TextUtils.isEmpty(inputW)) {
            int wInt = Integer.parseInt(inputW);
            if (wInt != 0) {
                swipeW = wInt;
            }
        }
    }

    @OnClick(R.id.btn_stop)
    public void stop() {
        stopSwipeAndTap = true;
    }

    /**
     * 模拟点击新闻条目
     */
    private void execTapItem() {
        String inputX = tapX + SystemUtil.dp2px(MyApplication.getInstance().getContext(), 170) + "";
        String inputY = tapY + SystemUtil.dp2px(MyApplication.getInstance().getContext(), 170) + "";
        ;
        TapEventUtil.execShellCmd("input tap " + inputX + " " + inputY);
        Log.d("apollo", "tapX: " + inputX + ",tapY: " + inputY);
    }

    /**
     * 模拟点击返回按钮
     */
    private void execTapBack() {
        String inputX = SystemUtil.dp2px(MyApplication.getInstance().getContext(), 17) + "";
        String inputY = SystemUtil.dp2px(MyApplication.getInstance().getContext(), 33) + "";
        TapEventUtil.execShellCmd("input tap " + inputX + " " + inputY);
        Log.d("apollo", "tapX: " + inputX + ",tapY: " + inputY);
    }

    /**
     * 通过包名启动 APP
     *
     * @param packageName
     */
    private void launchAppFromPackageName(String packageName) {
        PackageManager packageManager = getPackageManager();
        Intent intent = null;
        try {
            intent = packageManager.getLaunchIntentForPackage(packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (intent == null) {
            Log.e(TAG, "APP not found!");
            ToastUtil.show("APP 没找到！");
            return;
        }
        startActivity(intent);
    }


    /**
     * 模拟滑动
     */
    private void execSwipeH() {
//        if (!SystemUtil.getLinuxCoreInfo(MyApplication.getInstance().getContext(), packageName)) {
//            return;
//        }

        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();
        //滑动起始点为屏幕中心点
        int startY = screenHeight / 2;
        int startX = screenWidth / 2;
        int targetX = startX;
        int targetY = startY + swipeH;

        TapEventUtil.execShellCmd("input swipe " + startX + " " + startY + " " + targetX + " " + targetY + " 300");
    }

    /**
     * 检查前台 APP，若不是红包头条则进入
     */
    private boolean checkRunningApp() {
        boolean stats = SystemUtil.getFromAccessibilityService(MyApplication.getInstance().getContext(), CASH_RED_PACKET);
        LogUtil.d("apollo", CASH_RED_PACKET + "是否在前台运行：" + stats + "");
        if (!stats) {
            start();
            if (SystemUtil.getTopApp(MyApplication.getInstance().getContext()) != null) {
                killAppBypackage(SystemUtil.getTopApp(MyApplication.getInstance().getContext()));
            }

        }
        return stats;
    }


    private void killAppBypackage(String packageTokill) {
        if (!TextUtils.equals(packageTokill, packageName)
                && !TextUtils.equals(packageTokill, MyApplication.getInstance().getPackageName())) {
            execRootCmdSilent("am force-stop " + packageTokill);
            LogUtil.d("apollo", "杀死进程：" + packageTokill);
//            String[] commands = new String[]{"am force-stop " + packageTokill};
//            ShellUtils.CommandResult result = ShellUtils.execCommand(commands, true);
//            LogUtil.d("apollo", "result: " + result.errorMsg + ":" + result.successMsg);
        }

    }


    /**
     * 执行命令但不关注结果输出
     */
    private int execRootCmdSilent(String cmd) {
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());

            Log.i(TAG, cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            boolean isRunning = mActivity.get().checkRunningApp();
            switch (msg.what) {
                case LAUNCH_APP:
                    mActivity.get().launchAppFromPackageName(mActivity.get().packageName);
                    ToastUtil.show("成功打开 " + mActivity.get().packageName);
                    removeCallbacksAndMessages(null);
                    Message msg1 = Message.obtain();
                    msg1.what = EXEC_SWIPE_ITEM;
                    sendMessageDelayed(msg1, 10000);
                    break;
                case EXEC_SWIPE_ITEM:
                    if (!isRunning)
                        break;
                    mActivity.get().execSwipeH();
                    ToastUtil.show("第" + mActivity.get().swipTimes + "次滑动" + mActivity.get().swipeH + "像素");
                    mActivity.get().swipTimes++;
                    if (!mActivity.get().stopSwipeAndTap) {
                        removeCallbacksAndMessages(null);
                        Message msg2 = Message.obtain();
                        msg2.what = EXEC_TAP_ITEM;
                        sendMessageDelayed(msg2, 2000);
                    }
                    break;
                case EXEC_SWIPE_DETAIL:
                    if (!isRunning)
                        break;
                    mActivity.get().execSwipeH();
                    ToastUtil.show("第" + mActivity.get().detailSwipeTimes + "次新闻详情滑动");
                    mActivity.get().detailSwipeTimes++;
                    if (!mActivity.get().stopSwipeAndTap) {
                        removeCallbacksAndMessages(null);
                        Message msg4 = Message.obtain();
                        int detailSwipeTimes = mActivity.get().detailSwipeTimes;
                        if (detailSwipeTimes != 0 && detailSwipeTimes % 15 != 0) {//滑动新闻详情页不到15次
                            msg4.what = EXEC_SWIPE_DETAIL;
                        } else {
                            mActivity.get().detailSwipeTimes = 0;//复位
                            msg4.what = EXEC_TAP_BACK;
                        }
                        sendMessageDelayed(msg4, 1500);
                    }
                    break;
                case EXEC_TAP_ITEM:
                    if (!isRunning)
                        break;
                    mActivity.get().checkRunningApp();
                    mActivity.get().execTapItem();
                    ToastUtil.show("第" + mActivity.get().tapTimes + "次点击");
                    mActivity.get().tapTimes++;
                    if (!mActivity.get().stopSwipeAndTap) {
                        removeCallbacksAndMessages(null);
                        Message msg3 = Message.obtain();
                        msg3.what = EXEC_SWIPE_DETAIL;
                        sendMessageDelayed(msg3, 2000);
                    }
                    break;
                case EXEC_TAP_BACK:
                    if (!isRunning)
                        break;
                    mActivity.get().checkRunningApp();
                    Message msg5 = Message.obtain();
                    mActivity.get().execTapBack();
                    ToastUtil.show("点击返回");
                    mActivity.get().tapbackTimes++;
                    if (mActivity.get().tapbackTimes <= 1) {//点两次返回键，防止文字选中状态返回不了
                        msg5.what = EXEC_TAP_BACK;
                    } else {
                        mActivity.get().tapbackTimes = 0;
                        if (!mActivity.get().stopSwipeAndTap) {
                            removeCallbacksAndMessages(null);
                            msg5.what = EXEC_SWIPE_ITEM;
                        }
                    }
                    sendMessageDelayed(msg5, 2000);
                    break;

            }
        }

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
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }


}
