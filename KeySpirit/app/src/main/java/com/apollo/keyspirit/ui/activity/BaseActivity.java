package com.apollo.keyspirit.ui.activity;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import com.apollo.keyspirit.listener.PermissionListener;
import com.apollo.keyspirit.util.ToastUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public abstract class BaseActivity extends FragmentActivity {
    protected final String TAG = this.getClass().getSimpleName();
    PermissionListener mListener;
    protected boolean hasRootPermission = false;

    private String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.KILL_BACKGROUND_PROCESSES"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        initView();

        // 申请获取root权限，这一步很重要，不然会没有作用
        try {
            Process process = Runtime.getRuntime().exec("su");
            hasRootPermission = true;
        } catch (IOException e) {
            e.printStackTrace();
            hasRootPermission = false;
            ToastUtil.show("没有 root 权限！");
        }

        if (Build.VERSION.SDK_INT >= 23) {//判断当前系统是不是Android6.0
            requestRuntimePermissions(PERMISSIONS_STORAGE, new PermissionListener() {
                @Override
                public void granted() {
                    //权限申请通过
                }

                @Override
                public void denied(List<String> deniedList) {
                    //权限申请未通过
                    for (String denied : deniedList) {
                        if (denied.equals("android.permission.READ_EXTERNAL_STORAGE")) {
                            ToastUtil.show("没有文件读取权限，请检查是否打开！");
                        } else if (denied.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
                            ToastUtil.show("没有文件读写权限,请检查是否打开！");
                        }
                    }
                }
            });
        }

    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 申请权限
     */
    public void requestRuntimePermissions(
            String[] permissions, PermissionListener listener) {
        mListener = listener;
        List<String> permissionList = new ArrayList<>();
        // 遍历每一个申请的权限，把没有通过的权限放在集合中
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(BaseActivity.this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            } else {
                mListener.granted();
            }
        }
        // 申请权限
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(BaseActivity.this,
                    permissionList.toArray(new String[permissionList.size()]), 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            List<String> deniedList = new ArrayList<>();
            // 遍历所有申请的权限，把被拒绝的权限放入集合
            for (int i = 0; i < grantResults.length; i++) {
                int grantResult = grantResults[i];
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    mListener.granted();
                } else {
                    deniedList.add(permissions[i]);
                }
            }
            if (!deniedList.isEmpty()) {
                mListener.denied(deniedList);
            }
        }

    }
}
