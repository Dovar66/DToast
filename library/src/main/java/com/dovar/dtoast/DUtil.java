package com.dovar.dtoast;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * @Date: 2018/11/26
 * @Author: heweizong
 * @Description:
 */
public class DUtil {
    protected static boolean enableLog = false;

    public static void log(String info) {
        if (enableLog && !TextUtils.isEmpty(info)) {
            Log.d("DToast", info);
        }
    }

    /**
     * 检查当前设备是否在白名单列表中
     * <p>
     * 白名单中的设备在没有通知权限时系统Toast也能正常展示
     */
    public static boolean isWhiteList() {
        return isMIUI() || is1707A01();
    }

    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code"; //小米
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static int OS_MIUI = -1;//1为true 0为false

    /**
     * 小米系统
     */
    private static boolean isMIUI() {
        if (OS_MIUI != -1) return OS_MIUI == 1;
        boolean isMIUI = false;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            try {
                if (!TextUtils.isEmpty(getSystemProperty(KEY_MIUI_VERSION_CODE, ""))
                        || !TextUtils.isEmpty(getSystemProperty(KEY_MIUI_VERSION_NAME, ""))
                        || !TextUtils.isEmpty(getSystemProperty(KEY_MIUI_INTERNAL_STORAGE, ""))) {
                    isMIUI = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Properties prop = new Properties();
            try {
                prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            isMIUI = prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
        }
        OS_MIUI = isMIUI ? 1 : 0;
        return isMIUI;
    }

    private static String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String) get.invoke(clz, key, defaultValue);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (IllegalArgumentException e) {
        } catch (InvocationTargetException e) {
        }
        return defaultValue;
    }

    /**
     * 360手机
     */
    private static boolean is360() {
        String manufacturer = Build.MANUFACTURER.toUpperCase();
        return "360".equals(manufacturer) || "QIKU".equals(manufacturer);
    }

    /**
     * 机型：360 1707-A01
     * 在该机型上关闭通知权限并不会导致系统Toast无法弹出，与MIUI类似，所以也加入白名单
     * 关联issue#6
     */
    private static boolean is1707A01() {
        return is360() && "1707-A01".equals(Build.MODEL.toUpperCase());
    }
}
