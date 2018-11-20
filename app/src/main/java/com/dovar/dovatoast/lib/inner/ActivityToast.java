package com.dovar.dovatoast.lib.inner;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.view.WindowManager;

import com.dovar.dovatoast.lib.DovaToast;

/**
 * @Date: 2018/11/20
 * @Author: heweizong
 * @Description: 只展示在当前Activity的弹窗
 */
public class ActivityToast extends DovaToast {

    //context非Activity时会抛出异常:Unable to add window -- token null is not valid; is your activity running?
    public ActivityToast(@NonNull Context mContext) {
        super(mContext);
    }

    public WindowManager.LayoutParams getWMParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.format = PixelFormat.TRANSLUCENT;

      /*  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            mParams.y = mToast.getYOffset();
        } else {*/
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
//            mParams.y = mToast.getYOffset() + getNavBarHeight();
//        }

        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.windowAnimations = android.R.style.Animation_Toast;
        // TODO: 2018/11/20 考虑是否需要引入windowToken
//        lp.token=((Activity)getContext()).getWindow().getDecorView().getWindowToken();
        lp.gravity = getGravity();
        lp.x = getXOffset();
        lp.y = getYOffset();
        return lp;
    }
}
