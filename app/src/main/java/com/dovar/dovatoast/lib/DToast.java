package com.dovar.dovatoast.lib;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationManagerCompat;

import com.dovar.dovatoast.lib.inner.DovaToast;
import com.dovar.dovatoast.lib.inner.IToast;
import com.dovar.dovatoast.lib.inner.SystemToast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Date: 2018/11/26
 * @Author: heweizong
 * @Description:
 */
public class DToast {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DURATION_SHORT, DURATION_LONG})
    public @interface Duration {
    }

    public static final int DURATION_SHORT = 2000;
    public static final int DURATION_LONG = 3500;

    public static IToast make(Context mContext) {
        if (mContext == null) return null;
        //如果有通知权限，直接使用系统Toast
        if (NotificationManagerCompat.from(mContext).areNotificationsEnabled()) {
            return new SystemToast(mContext);
        } else {//否则使用自定义Toast
            return new DovaToast(mContext);
        }
    }

    /**
     * 终止并清除所有弹窗
     */
    public static void cancel() {
        DovaToast.cancelAll();
        SystemToast.cancelAll();
    }

    /**
     * 清除与{@param mActivity}关联的ActivityToast，避免窗口泄漏
     */
    public static void cancelActivityToast(Activity mActivity) {
        DovaToast.cancelActivityToast(mActivity);
    }
}
