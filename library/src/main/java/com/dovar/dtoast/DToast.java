package com.dovar.dtoast;

import android.content.Context;

import androidx.annotation.IntDef;

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

    public static void enableLog(boolean enable) {
        DUtil.enableLog = enable;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DURATION_SHORT, DURATION_LONG})
    public @interface Duration {
    }

    public static final int DURATION_SHORT = 2000;
    public static final int DURATION_LONG = 3500;

    public static IToast make(Context context) {
        return new SystemToast(context.getApplicationContext());
    }

    /**
     * 终止并清除所有弹窗
     */
    public static void cancel() {
        SystemToast.cancelAll();
    }
}
