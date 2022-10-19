package com.dovar.dtoast;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.dovar.dtoast.inner.ActivityToast;
import com.dovar.dtoast.inner.DovaToast;
import com.dovar.dtoast.inner.IToast;
import com.dovar.dtoast.inner.SystemToast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Date: 2018/11/26
 * @Author: heweizong
 * @Description: 使用系统Toast的问题 {@link android.widget.Toast}：当通知权限被关闭时在华为等手机上Toast不显示。因此采取自定义Toast解决.
 * 优先使用系统Toast，如果通知权限被关闭，则使用DovaToast.
 */
public class DToast {

    private static Application application;
    private static Activity topActivity;

    public static void init(@NonNull Application app) {
        if (application != null) return;
        application = app;
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                topActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if (activity == topActivity) {
                    topActivity = null;
                }
                //清除与activity关联的ActivityToast，避免窗口泄漏
                DovaToast.cancelActivityToast(activity);
            }
        });
    }

    public static void enableLog(boolean enable) {
        DUtil.enableLog = enable;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DURATION_SHORT, DURATION_LONG})
    public @interface Duration {
    }

    public static final int DURATION_SHORT = 2000;
    public static final int DURATION_LONG = 3500;

    public static IToast make() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new SystemToast(application);
        } else {
            if (NotificationManagerCompat.from(application).areNotificationsEnabled() || DUtil.isWhiteList()) {
                //如果有通知权限，直接使用系统Toast
                //白名单中的机型没有通知权限时系统Toast也能正常展示
                return new SystemToast(application);
            } else {
                //否则使用自定义Toast
                if (DovaToast.isBadChoice() && topActivity != null) {
                    //检测到DovaToast连续多次抛出token null is not valid异常时，直接启用ActivityToast
                    return new ActivityToast(topActivity);
                }
                return new DovaToast(application);
            }
        }
    }

    /**
     * 终止并清除所有弹窗
     */
    public static void cancel() {
        DovaToast.cancelAll();
        SystemToast.cancelAll();
    }
}
