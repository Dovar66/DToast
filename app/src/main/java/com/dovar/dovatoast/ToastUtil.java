package com.dovar.dovatoast;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import com.dovar.dtoast.DToast;


/**
 * @Date: 2018/11/13
 * @Author: heweizong
 * @Description: 简单封装
 */
public class ToastUtil {

    /**
     * 使用默认布局
     */
    public static void show(Context mContext, String msg) {
        if (mContext == null || msg == null) return;
        DToast.make(mContext)
                .setText(msg)
                .setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 30)
                .show();
    }


    /**
     * 通过setView()设置自定义的Toast布局
     */
    public static void showAtCenter(Context mContext, String msg) {
        if (mContext == null || msg == null) return;
        DToast.make(mContext)
                .setView(View.inflate(mContext, R.layout.layout_toast_center, null))
                .setText(msg)
                .setGravity(Gravity.CENTER, 0, 0)
                .showLong();
    }

    //退出APP时调用
    public static void cancelAll() {
        DToast.cancel();
    }
}
