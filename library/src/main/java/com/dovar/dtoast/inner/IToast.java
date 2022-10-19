package com.dovar.dtoast.inner;

import android.view.View;

import com.dovar.dtoast.DToast;

import androidx.annotation.IdRes;


/**
 * @Date: 2018/11/20
 * @Author: heweizong
 * @Description:
 */
public interface IToast {
    void show();

    void showLong();

    void cancel();

    //TextView的id必须是android:id="@android:id/message"
    IToast setView(View mView);

    View getView();

    IToast setDuration(@DToast.Duration int duration);

    IToast setGravity(int gravity);

    IToast setGravity(int gravity, int xOffset, int yOffset);

    //do not support
    @Deprecated
    IToast setAnimation(int animation);

    IToast setPriority(int mPriority);

    @Deprecated
    IToast setText(@IdRes int id, String text);

    IToast setText(String text);
}
