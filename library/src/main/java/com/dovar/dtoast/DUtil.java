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
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code"; //小米
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    /**
     * 小米系统
     *
     * @return
     */
    public static boolean isMIUI() {
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

    private static boolean enableLog = false;

    public static void log(String info) {
        if (enableLog && !TextUtils.isEmpty(info)) {
            Log.d("DToast", info);
        }
    }
}
