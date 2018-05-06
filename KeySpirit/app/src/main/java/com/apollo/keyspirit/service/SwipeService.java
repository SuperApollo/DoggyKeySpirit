package com.apollo.keyspirit.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.apollo.keyspirit.app.MyApplication;
import com.apollo.keyspirit.constants.Constants;
import com.apollo.keyspirit.util.LogUtil;
import com.apollo.keyspirit.util.SystemUtil;
import com.apollo.keyspirit.util.TapEventUtil;
import com.apollo.keyspirit.util.ToastUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class SwipeService extends Service {
    private final int SERVER_VERSION_ERROR = 0x00010086;
    private final int HAS_UPDATE = 0x00010087;
    private final int NO_UPDATE = 0x00010088;
    final String TAG = this.getClass().getSimpleName();

    MyHandler mHandler = new MyHandler(this);
    MyBroadcustReceiver mReceiver = new MyBroadcustReceiver();
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
    public void onCreate() {
        super.onCreate();
        swipeH = -SystemUtil.dp2px(MyApplication.getInstance().getContext(), 133);
        swipeW = SystemUtil.dp2px(MyApplication.getInstance().getContext(), 66);
        registerReceiver(mReceiver, new IntentFilter(Constants.MY_BROADCUST_RECEIVER_ACTION));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        unregisterReceiver(mReceiver);
    }

    private void start() {
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
        ToastUtil.show("开始狗只服务");
    }

    private void stop() {
        stopSwipeAndTap = true;
        stopSelf();
        ToastUtil.show("停止狗只服务");
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
        private final WeakReference<SwipeService> mService;

        public MyHandler(SwipeService service) {
            this.mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            boolean isRunning = mService.get().checkRunningApp();
            switch (msg.what) {
                case LAUNCH_APP:
                    mService.get().launchAppFromPackageName(mService.get().packageName);
                    ToastUtil.show("成功打开 " + mService.get().packageName);
                    removeCallbacksAndMessages(null);
                    Message msg1 = Message.obtain();
                    msg1.what = EXEC_SWIPE_ITEM;
                    sendMessageDelayed(msg1, 10000);
                    break;
                case EXEC_SWIPE_ITEM:
                    if (!isRunning)
                        break;
                    mService.get().execSwipeH();
                    ToastUtil.show("第" + mService.get().swipTimes + "次滑动" + mService.get().swipeH + "像素");
                    mService.get().swipTimes++;
                    if (!mService.get().stopSwipeAndTap) {
                        removeCallbacksAndMessages(null);
                        Message msg2 = Message.obtain();
                        msg2.what = EXEC_TAP_ITEM;
                        sendMessageDelayed(msg2, 2000);
                    }
                    break;
                case EXEC_SWIPE_DETAIL:
                    if (!isRunning)
                        break;
                    mService.get().execSwipeH();
                    ToastUtil.show("第" + mService.get().detailSwipeTimes + "次新闻详情滑动");
                    mService.get().detailSwipeTimes++;
                    if (!mService.get().stopSwipeAndTap) {
                        removeCallbacksAndMessages(null);
                        Message msg4 = Message.obtain();
                        int detailSwipeTimes = mService.get().detailSwipeTimes;
                        if (detailSwipeTimes != 0 && detailSwipeTimes % 15 != 0) {//滑动新闻详情页不到15次
                            msg4.what = EXEC_SWIPE_DETAIL;
                        } else {
                            mService.get().detailSwipeTimes = 0;//复位
                            msg4.what = EXEC_TAP_BACK;
                        }
                        sendMessageDelayed(msg4, 1500);
                    }
                    break;
                case EXEC_TAP_ITEM:
                    if (!isRunning)
                        break;
                    mService.get().checkRunningApp();
                    mService.get().execTapItem();
                    ToastUtil.show("第" + mService.get().tapTimes + "次点击");
                    mService.get().tapTimes++;
                    if (!mService.get().stopSwipeAndTap) {
                        removeCallbacksAndMessages(null);
                        Message msg3 = Message.obtain();
                        msg3.what = EXEC_SWIPE_DETAIL;
                        sendMessageDelayed(msg3, 2000);
                    }
                    break;
                case EXEC_TAP_BACK:
                    if (!isRunning)
                        break;
                    mService.get().checkRunningApp();
                    Message msg5 = Message.obtain();
                    mService.get().execTapBack();
                    ToastUtil.show("点击返回");
                    mService.get().tapbackTimes++;
                    if (mService.get().tapbackTimes <= 1) {//点两次返回键，防止文字选中状态返回不了
                        msg5.what = EXEC_TAP_BACK;
                    } else {
                        mService.get().tapbackTimes = 0;
                        if (!mService.get().stopSwipeAndTap) {
                            removeCallbacksAndMessages(null);
                            msg5.what = EXEC_SWIPE_ITEM;
                        }
                    }
                    sendMessageDelayed(msg5, 2000);
                    break;

            }
        }

    }

    //==========================广播===================================
    private class MyBroadcustReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra(Constants.BROADCUST_COMMAND);
            if (TextUtils.equals(command, Constants.COMMAND_START)) {
                start();
            } else if (TextUtils.equals(command, Constants.COMMAND_STOP)) {
                stop();
            }

        }
    }
}
