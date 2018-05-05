package com.apollo.keyspirit.util;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * 模拟点击、触摸事件 shell 命令工具类
 * 是否执行成功
 */
public class TapEventUtil {

    public static boolean execShellCmd(String cmd) {
        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(
                    outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
}
