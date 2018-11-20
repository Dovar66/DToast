package com.dovar.dovatoast;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.dovar.dovatoast.lib.DovaToast;
import com.dovar.dovatoast.lib.inner.ActivityToast;

/**
 * @Date: 2018/11/13
 * @Author: heweizong
 * @Description:
 */
public class ToastUtil {

    public static void show(Context appContext, String msg) {
        if (appContext == null) return;
        if (msg == null) return;
        DovaToast mToast = new ActivityToast(appContext);
        mToast.setDuration(DovaToast.DURATION_LONG);
        View toastRoot = View.inflate(appContext, R.layout.layout_toast, null);
        mToast.setView(toastRoot);
        TextView tv_text = (TextView) toastRoot.findViewById(R.id.tv_content);
        if (tv_text != null) {
            tv_text.setText(msg);
        }
        mToast.show();
    }


    public static void showAtCenter(Context appContext, String msg) {
        if (appContext == null) return;
        if (msg == null) return;
        DovaToast mToast = new ActivityToast(appContext);
        mToast.setDuration(DovaToast.DURATION_LONG);
        View toastRoot = View.inflate(appContext, R.layout.layout_toast_center, null);
        mToast.setView(toastRoot);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        TextView tv_text = (TextView) toastRoot.findViewById(R.id.tv_content);
        if (tv_text != null) {
            tv_text.setText(msg);
        }
        mToast.show();
    }
}
