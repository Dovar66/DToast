package com.dovar.dovatoast;

import android.app.Application;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.dovar.dovatoast.lib.DovaToast;

/**
 * @Date: 2018/11/13
 * @Author: heweizong
 * @Description:
 */
public class ToastUtil {
    private static DovaToast mToast;
    private static Application appContext;
    /**
     * 修改mToast 的Gravity或者使用不同contentView时，请设置不同的Tag，勿使用Tag_default
     */
    private static final int Tag_null = 0;
    private static final int Tag_default = 1;//只能用于默认样式的Toast
    private static final int Tag_showAtCenter = 2;

    public static void init(Application app) {
        appContext = app;
    }

    private static DovaToast ensureToastNotNull() {
        return new DovaToast(appContext);
    }

    //统一管理
    private static void showToast(DovaToast mToast) {
        try {
            if (mToast != null) {
                mToast.show();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static int getTagIgnoreException(DovaToast toast) {
        try {
            return (int) toast.getView().getTag();
        } catch (Exception e) {
            e.printStackTrace();
            return Tag_null;
        }
    }

    public static void show(String msg) {
        if (msg == null) return;
        int tag = getTagIgnoreException(mToast);
        View toastRoot;
        if (tag != Tag_default) {
            mToast = ensureToastNotNull();
            toastRoot = View.inflate(appContext, R.layout.layout_toast, null);
            toastRoot.setTag(Tag_default);
            mToast.setView(toastRoot);
        } else {
            toastRoot = mToast.getView();
        }
        TextView tv_text = (TextView) toastRoot.findViewById(R.id.tv_content);
        if (tv_text != null) {
            tv_text.setText(msg);
        }
        showToast(mToast);
    }


    public static void showAtCenter(String msg) {
        if (msg == null) return;
        int tag = getTagIgnoreException(mToast);
        View toastRoot;
        if (tag != Tag_showAtCenter) {
            mToast = ensureToastNotNull();
            toastRoot = View.inflate(appContext, R.layout.layout_toast_center, null);
            toastRoot.setTag(Tag_showAtCenter);
            mToast.setView(toastRoot);
            mToast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toastRoot = mToast.getView();
        }
        TextView tv_text = (TextView) toastRoot.findViewById(R.id.tv_content);
        if (tv_text != null) {
            tv_text.setText(msg);
        }
        showToast(mToast);
    }
}
