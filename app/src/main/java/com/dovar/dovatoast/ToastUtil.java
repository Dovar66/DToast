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
    public static void show(String msg) {
        if (msg == null) return;
        DToast.make()
                .setText(msg)
                .setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 30)
                .show();
    }


    /**
     * 通过setView()设置自定义的Toast布局
     */
    public static void showAtCenter(String msg) {
        if (msg == null) return;
        DToast.make()
                .setView(View.inflate(App.app, R.layout.layout_toast_center, null))
                .setText(msg)
                .setGravity(Gravity.CENTER, 0, 0)
                .showLong();
    }
}
