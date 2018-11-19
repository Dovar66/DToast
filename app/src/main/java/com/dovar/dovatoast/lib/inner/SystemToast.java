package com.dovar.dovatoast.lib.inner;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.dovar.dovatoast.lib.DovaToast;

/**
 * @Date: 2018/11/19
 * @Author: heweizong
 * @Description: {@link android.widget.Toast}
 */
class SystemToast {
    /**
     * 在{@link SystemTN#displayToast(SystemToast)}中才被初始化
     */
    private Toast mToast;
    private int priority;//优先级

    private Context mContext;
    private View contentView;
    private int animation = android.R.style.Animation_Toast;
    private int gravity = Gravity.BOTTOM | Gravity.CENTER;
    private int xOffset;
    private int yOffset;
    private @DovaToast.Duration
    int duration = DovaToast.DURATION_SHORT;

    //外部调用
    void show(Context mContext) {
        if (mContext == null) return;
        this.mContext = mContext;
        SystemTN.instance().add(this);
    }

    //不允许被外部调用
    void show() {
        mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
        Util.hookHandler(mToast);
        copyToToast(mToast);
        mToast.show();
    }

    void cancel() {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
    }

    private void copyToToast(Toast toast) {
        if (toast == null) return;
        if (contentView != null) {
            toast.setView(this.contentView);
        }
        toast.setGravity(this.gravity, xOffset, yOffset);
        Util.setupToastAnim(toast, this.animation);
        if (duration == DovaToast.DURATION_SHORT) {
            toast.setDuration(Toast.LENGTH_SHORT);
        } else if (duration == DovaToast.DURATION_LONG) {
            toast.setDuration(Toast.LENGTH_LONG);
        }

        //重置参数
       /* // TODO: 2018/11/19
        this.contentView = null;
        animation = android.R.style.Animation_Toast;
        gravity = Gravity.BOTTOM | Gravity.CENTER;
        xOffset = 0;
        yOffset = 0;
        width = WindowManager.LayoutParams.WRAP_CONTENT;
        height = WindowManager.LayoutParams.WRAP_CONTENT;
        priority = 0;
        this.duration = DovaToast.DURATION_SHORT;*/
    }

    public SystemToast setView(View mView) {
        this.contentView = mView;
        return this;
    }

    public View getView() {
        return this.contentView;
    }

    public SystemToast setDuration(@DovaToast.Duration int duration) {
        this.duration = duration;
        return this;
    }

    public int getDuration() {
        return this.duration;
    }

    public SystemToast setAnimation(int animation) {
        this.animation = animation;
        return this;
    }

    /**
     * @param gravity {@link android.view.Gravity#TOP etc..}
     * @param xOffset pixel
     * @param yOffset pixel
     */
    public SystemToast setGravity(int gravity, int xOffset, int yOffset) {
        this.gravity = gravity;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        return this;
    }

    public SystemToast setGravity(int gravity) {
        setGravity(gravity, 0, 0);
        return this;
    }

    public int getGravity() {
        return this.gravity;
    }

    public int getXOffset() {
        return this.xOffset;
    }

    public int getYOffset() {
        return this.yOffset;
    }

    public int getPriority() {
        return this.priority;
    }

    public SystemToast setPriority(int mPriority) {
        this.priority = mPriority;
        return this;
    }

    @Override
    protected SystemToast clone() {
        SystemToast mToast = null;
        try {
            mToast = (SystemToast) super.clone();
            mToast.mContext = this.mContext;
            mToast.contentView = this.contentView;
            mToast.duration = this.duration;
            mToast.animation = this.animation;
            mToast.gravity = this.gravity;
            mToast.xOffset = this.xOffset;
            mToast.yOffset = this.yOffset;
            mToast.priority = this.priority;
        } catch (CloneNotSupportedException mE) {
            mE.printStackTrace();
        }
        return mToast;
    }
}
