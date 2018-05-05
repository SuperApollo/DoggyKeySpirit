package com.apollo.keyspirit.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.apollo.keyspirit.app.AppConfig;
import com.apollo.keyspirit.app.MyApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * SharedPreferences 工具类
 */
public class SharedPreferencesUtils {
    private static final String SHARED_PATH = AppConfig.SHARED_PATH;
    private static final String SYN_SHARED_PATH = AppConfig.SYN_SHARED_PATH;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences synSharedPreferences;
    private static Context context = MyApplication.getInstance().getContext();
    private static Crypto crypto = new Crypto("discount_hunter");

    private SharedPreferencesUtils() {
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(SHARED_PATH, Context.MODE_MULTI_PROCESS);
        }
        return sharedPreferences;
    }

    public static void putInt(String key, int value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        Editor edit = sharedPreferences.edit();
        edit.putInt(crypto.encrypt(key), value);
        edit.commit();
    }

    public static int getInt(String key) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(crypto.encrypt(key), 0);
    }

    public static int getInt(String key, int defValue) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(crypto.encrypt(key), defValue);
    }

    public static void putString(String key, String value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        Editor edit = sharedPreferences.edit();
        edit.putString(key, crypto.encrypt(value));
        edit.commit();
    }

    public static String getString(String key) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        String value = sharedPreferences.getString(key, null);
        if (!TextUtils.isEmpty(value)) {
            value = crypto.decrypt(value);
        }
        return value;


    }

    public static String getString(String key, String defValue) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        String value = sharedPreferences.getString(key, defValue);
        if (!TextUtils.isEmpty(value)) {
            value = crypto.decrypt(value);
        }
        return value;
    }

    public static void putBoolean(String key, boolean value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        Editor edit = sharedPreferences.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public static boolean getBoolean(String key, boolean defValue) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, defValue);
    }

    public static void putLong(String key, long value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        Editor edit = sharedPreferences.edit();
        edit.putLong(key, value);
        edit.commit();
    }

    public static long getLong(String key, long defValue) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        long value = sharedPreferences.getLong(key, defValue);
        return value;
    }

    public static void putFloat(String key, float value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        Editor edit = sharedPreferences.edit();
        edit.putFloat(key, value);
        edit.commit();
    }

    public static float getFloat(String key, float defValue) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        float value = sharedPreferences.getFloat(key, defValue);
        return value;
    }

    public static long getLong(String key) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        long value = sharedPreferences.getLong(key, -1);
        return value;
    }

    public static void remove(String key) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        Editor edit = sharedPreferences.edit();
        edit.remove(crypto.encrypt(key));
        edit.commit();
    }

    public static SharedPreferences getSynDefaultSharedP(Context context) {

        if (synSharedPreferences == null) {
            synSharedPreferences = context.getSharedPreferences(SYN_SHARED_PATH, Context.MODE_MULTI_PROCESS);
        }
        return synSharedPreferences;
    }

    public static String getSynString(String key, String defValue) {
        SharedPreferences synShare = getSynDefaultSharedP(context);
        String value = synShare.getString(crypto.encrypt(key), defValue);
        if (!TextUtils.isEmpty(value)) {
            value = crypto.decrypt(value);
        }
        return value;
    }

    public static void putSynString(String key, String value) {
        SharedPreferences synShare = getSynDefaultSharedP(context);
        Editor editor = synShare.edit();
        editor.putString(crypto.encrypt(key), crypto.encrypt(value));
        editor.commit();
    }

    public static void putObject(String name, Object obj) throws IOException {
        putString(name, serialize(obj));
    }

    public static <T extends Object> T getObject(String name) throws IOException, ClassNotFoundException {
        String strObj = getString(name);
        return deSerialization(strObj);
    }

    /**
     * 序列化对象
     *
     * @param obj
     * @return
     * @throws IOException
     */
    public static String serialize(Object obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                byteArrayOutputStream);
        objectOutputStream.writeObject(obj);
        String serStr = byteArrayOutputStream.toString("ISO-8859-1");
        serStr = java.net.URLEncoder.encode(serStr, "UTF-8");
        objectOutputStream.close();
        byteArrayOutputStream.close();

        return serStr;
    }

    /**
     * 反序列化对象
     *
     * @param str
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static <T extends Object> T deSerialization(String str) throws IOException,
            ClassNotFoundException {
        if (str == null)
            return null;
        String redStr = java.net.URLDecoder.decode(str, "UTF-8");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                redStr.getBytes("ISO-8859-1"));
        ObjectInputStream objectInputStream = new ObjectInputStream(
                byteArrayInputStream);
        T object = (T) objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();

        return object;
    }


    /****
     * 清除所有的SharedPreference
     *
     * @param context
     */
    public static void cleanAllSharedPreference(Context context) {
        SharedPreferences sp = getDefaultSharedPreferences(context);
        final Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }
}
