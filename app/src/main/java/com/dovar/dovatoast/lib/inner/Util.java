package com.dovar.dovatoast.lib.inner;

import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;

/**
 * @Date: 2018/11/19
 * @Author: heweizong
 * @Description:
 */
public class Util {

    //捕获8.0之前Toast的BadTokenException，Google在Android 8.0的代码提交中修复了这个问题
    static void hookHandler(Toast toast) {
        try {
            Field sField_TN = Toast.class.getDeclaredField("mTN");
            sField_TN.setAccessible(true);
            Field sField_TN_Handler = sField_TN.getType().getDeclaredField("mHandler");
            sField_TN_Handler.setAccessible(true);

            Object tn = sField_TN.get(toast);
            Handler preHandler = (Handler) sField_TN_Handler.get(tn);
            sField_TN_Handler.set(tn, new SafelyHandlerWrapper(preHandler));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置金币提示框的动画
    static void setupToastAnim(Toast toast, int animRes) {
        try {
            Object mTN = getField(toast, "mTN");
            if (mTN != null) {
                Object mParams = getField(mTN, "mParams");
                if (mParams != null && mParams instanceof WindowManager.LayoutParams) {
                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) mParams;
                    params.windowAnimations = animRes;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 反射字段
     *
     * @param object    要反射的对象
     * @param fieldName 要反射的字段名称
     */
    private static Object getField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        if (field != null) {
            field.setAccessible(true);
            return field.get(object);
        }
        return null;
    }
}
