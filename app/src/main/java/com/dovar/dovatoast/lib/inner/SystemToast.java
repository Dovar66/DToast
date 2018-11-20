package com.dovar.dovatoast.lib.inner;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.dovar.dovatoast.R;
import com.dovar.dovatoast.lib.DovaToast;

/**
 * @Date: 2018/11/19
 * @Author: heweizong
 * @Description: 使用 {@link android.widget.Toast}
 */
class SystemToast implements IToast,Cloneable{
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

    public SystemToast(@NonNull Context mContext) {
        this.mContext = mContext;
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater == null) throw new RuntimeException("LayoutInflater is null!");
        this.contentView = layoutInflater.inflate(R.layout.layout_toast, null);
    }

    //外部调用
    @Override
    public void show() {
        SystemTN.instance().add(this);
    }

    @Override
    public void cancel() {
        SystemTN.instance().cancelAll();
    }

    //不允许被外部调用
    void showInternal() {
        mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
        Util.hookHandler(mToast);
        copyToToast(mToast);
        mToast.show();
    }

    void cancelInternal(){
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
