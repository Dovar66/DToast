package com.dovar.dovatoast;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.dovar.dovatoast.lib.DToast;

/**
 * @Date: 2018/11/13
 * @Author: heweizong
 * @Description:
 */
public class ToastUtil {

    public static void show(Context appContext, String msg) {
        if (appContext == null) return;
        if (msg == null) return;
        View toastRoot = View.inflate(appContext, R.layout.layout_toast, null);
        TextView tv_text = (TextView) toastRoot.findViewById(R.id.tv_content);
        if (tv_text != null) {
            tv_text.setText(msg);
        }
        DToast.make(appContext)
                .setView(toastRoot)
                .show();
    }


    public static void showAtCenter(Context appContext, String msg) {
        if (appContext == null) return;
        if (msg == null) return;
        View toastRoot = View.inflate(appContext, R.layout.layout_toast_center, null);
        TextView tv_text = (TextView) toastRoot.findViewById(R.id.tv_content);
        if (tv_text != null) {
            tv_text.setText(msg);
        }
        DToast.make(appContext)
                .setView(toastRoot)
                .setGravity(Gravity.CENTER, 0, 0)
                .show();
    }

    public static void cancelAll() {
        DToast.cancel();
    }
}
