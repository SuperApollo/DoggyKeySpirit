package com.apollo.keyspirit.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.widget.Toast;

import com.apollo.keyspirit.R;
import com.apollo.keyspirit.app.MyApplication;
import com.apollo.keyspirit.service.DetectService;
import com.apollo.keyspirit.util.processutil.ProcessManager;
import com.apollo.keyspirit.util.processutil.models.AndroidAppProcess;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SystemUtil {
    /**
     * 判断当前的 APP 是否在前台运行
     *
     * @param context     上下文
     * @param packageName 包名
     * @return
     */
    public static boolean isMyAppOnForeground(Context context, String packageName) {
        ActivityManager manager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
            if (runningAppProcesses != null && runningAppProcesses.size() > 0) {
                for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcesses) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (String activeProcess : processInfo.pkgList) {
                            if (activeProcess.equals(packageName))
                                return true;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
            ComponentName componentName = runningTasks.get(0).topActivity;
            if (componentName.getPackageName().equals(packageName)) {
                return true;
            }
        }


        return false;
    }

    /**
     * 判断其它 APP 是否在前台
     *
     * @param context     上下文
     * @param packageName 包名
     * @return
     */
    public static boolean isOtherAppOnForeground(Context context, String packageName) {
        List<AndroidAppProcess> processes = ProcessManager.getRunningForegroundApps(context);
        for (AndroidAppProcess appProcess : processes) {
            if (appProcess.getPackageName().equals(packageName)
                    && appProcess.foreground) {
                return true;
            }
        }
        return false;
    }

    public static final int BKGMETHOD_GETRUNNING_TASK = 0;
    public static final int BKGMETHOD_GETRUNNING_PROCESS = 1;
    public static final int BKGMETHOD_GETAPPLICATION_VALUE = 2;
    public static final int BKGMETHOD_GETUSAGESTATS = 3;
    public static final int BKGMETHOD_GETACCESSIBILITYSERVICE = 4;
    public static final int BKGMETHOD_GETLINUXPROCESS = 5;


    /**
     * 自动根据参数选择判断前后台的方法
     *
     * @param context     上下文参数
     * @param packageName 需要检查是否位于栈顶的App的包名
     * @return
     */
    public static boolean isForeground(Context context, int methodID, String packageName) {
        switch (methodID) {
            case BKGMETHOD_GETRUNNING_TASK:
                return getRunningTask(context, packageName);
            case BKGMETHOD_GETRUNNING_PROCESS:
                return getRunningAppProcesses(context, packageName);
            case BKGMETHOD_GETAPPLICATION_VALUE:
                return getApplicationValue(MyApplication.getInstance());
            case BKGMETHOD_GETUSAGESTATS:
                return queryUsageStats(context, packageName);
            case BKGMETHOD_GETACCESSIBILITYSERVICE:
                return getFromAccessibilityService(context, packageName);
            case BKGMETHOD_GETLINUXPROCESS:
                return getLinuxCoreInfo(context, packageName);
            default:
                return false;
        }
    }

    /**
     * 方法1：通过getRunningTasks判断App是否位于前台，此方法在5.0以上失效
     *
     * @param context     上下文参数
     * @param packageName 需要检查是否位于栈顶的App的包名
     * @return
     */
    public static boolean getRunningTask(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        return !TextUtils.isEmpty(packageName) && packageName.equals(cn.getPackageName());
    }


    /**
     * 方法2：通过getRunningAppProcesses的IMPORTANCE_FOREGROUND属性判断是否位于前台，当service需要常驻后台时候，此方法失效,
     * 在小米 Note上此方法无效，在Nexus上正常
     *
     * @param context     上下文参数
     * @param packageName 需要检查是否位于栈顶的App的包名
     * @return
     */
    public static boolean getRunningAppProcesses(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 方法3：通过ActivityLifecycleCallbacks来批量统计Activity的生命周期，来做判断，此方法在API 14以上均有效，但是需要在Application中注册此回调接口
     * 必须：
     * 1. 自定义Application并且注册ActivityLifecycleCallbacks接口
     * 2. AndroidManifest.xml中更改默认的Application为自定义
     * 3. 当Application因为内存不足而被Kill掉时，这个方法仍然能正常使用。虽然全局变量的值会因此丢失，但是再次进入App时候会重新统计一次的
     *
     * @param myApplication
     * @return
     */

    public static boolean getApplicationValue(MyApplication myApplication) {
        return myApplication.getAppCount() > 0;
    }

    /**
     * 方法4：通过使用UsageStatsManager获取，此方法是ndroid5.0A之后提供的API
     * 必须：
     * 1. 此方法只在android5.0以上有效
     * 2. AndroidManifest中加入此权限<uses-permission xmlns:tools="http://schemas.android.com/tools" android:name="android.permission.PACKAGE_USAGE_STATS"
     * tools:ignore="ProtectedPermissions" />
     * 3. 打开手机设置，点击安全-高级，在有权查看使用情况的应用中，为这个App打上勾
     *
     * @param context     上下文参数
     * @param packageName 需要检查是否位于栈顶的App的包名
     * @return
     */

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean queryUsageStats(Context context, String packageName) {
        class RecentUseComparator implements Comparator<UsageStats> {
            @Override
            public int compare(UsageStats lhs, UsageStats rhs) {
                return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
            }
        }
        RecentUseComparator mRecentComp = new RecentUseComparator();
        long ts = System.currentTimeMillis();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService("usagestats");
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 1000 * 10, ts);
        if (usageStats == null || usageStats.size() == 0) {
            if (HavaPermissionForTest(context) == false) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Toast.makeText(context, "权限不够\n请打开手机设置，点击安全-高级，在有权查看使用情况的应用中，为这个App打上勾", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        Collections.sort(usageStats, mRecentComp);
        String currentTopPackage = usageStats.get(0).getPackageName();
        if (currentTopPackage.equals(packageName)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否有用权限
     *
     * @param context 上下文参数
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean HavaPermissionForTest(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    /**
     * 方法5：通过Android自带的无障碍功能，监控窗口焦点的变化，进而拿到当前焦点窗口对应的包名
     * 必须：
     * 1. 创建ACCESSIBILITY SERVICE INFO 属性文件
     * 2. 注册 DETECTION SERVICE 到 ANDROIDMANIFEST.XML
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean getFromAccessibilityService(Context context, String packageName) {
        if (DetectService.isAccessibilitySettingsOn(context) == true) {
            DetectService detectService = DetectService.getInstance();
            String foreground = detectService.getForegroundPackage();
            LogUtil.d("wenming", "**方法五** 当前窗口焦点对应的包名为： =" + foreground);
            return packageName.equals(foreground);
        } else {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Toast.makeText(context, R.string.accessbiliityNo, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static String getTopApp(Context context) {
        if (DetectService.isAccessibilitySettingsOn(context) == true) {
            DetectService detectService = DetectService.getInstance();
            String foreground = detectService.getForegroundPackage();
            LogUtil.d("wenming", "**方法五** 当前窗口焦点对应的包名为： =" + foreground);
            return foreground;
        }
        return null;
    }


    /**
     * 判断指定是否在前台运行
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean getLinuxCoreInfo(Context context, String packageName) {

        List<AndroidAppProcess> processes = ProcessManager.getRunningForegroundApps(context);
        for (AndroidAppProcess appProcess : processes) {
            if (appProcess.getPackageName().equals(packageName) && appProcess.foreground) {
                return true;
            }
        }
        return false;

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean isAppRunningForgrond(Context context, String packageName) {
        long ts = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 2000, ts);
        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
            return false;
        }
        UsageStats recentStats = null;
        for (UsageStats usageStats : queryUsageStats) {
            if (recentStats == null || recentStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                recentStats = usageStats;
            }
        }
        return recentStats.getPackageName().equals(packageName);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
