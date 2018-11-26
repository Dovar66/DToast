package com.dovar.dovatoast.lib.inner;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.dovar.dovatoast.R;
import com.dovar.dovatoast.lib.DToast;

/**
 * @Date: 2018/11/13
 * @Author: heweizong
 * @Description: 解决通知权限被关闭时系统Toast无法正常展示的问题.
 * 使用{@link DovaToast}出现{@link WindowManager.BadTokenException}时，再尝试使用{@link com.dovar.dovatoast.lib.inner.ActivityToast}
 */
public class DovaToast implements Cloneable, IToast {
    Context mContext;
    private View contentView;
    private int animation = android.R.style.Animation_Toast;
    private int gravity = Gravity.BOTTOM | Gravity.CENTER;
    private int xOffset;
    private int yOffset;
    private int width = WindowManager.LayoutParams.WRAP_CONTENT;
    private int height = WindowManager.LayoutParams.WRAP_CONTENT;
    private int priority;//优先级
    private @DToast.Duration
    int duration = DToast.DURATION_SHORT;
    boolean isShowing;//TN标记为正在展示

    /**
     * @param mContext 建议使用Activity。如果使用AppContext则当通知权限被禁用且TYPE_TOAST被WindowManager.addView()抛出异常时，无法正常显示弹窗。
     *                 在API25+的部分手机上TYPE_TOAST被WindowManager.addView()时会抛出异常
     */
    public DovaToast(@NonNull Context mContext) {
        this.mContext = mContext;
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater == null) throw new RuntimeException("LayoutInflater is null!");
        this.contentView = layoutInflater.inflate(R.layout.layout_toast, null);
    }

    public WindowManager.LayoutParams getWMParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.type = WindowManager.LayoutParams.TYPE_TOAST;
        lp.height = this.height;
        lp.width = this.width;
        lp.windowAnimations = this.animation;
        lp.gravity = this.gravity;
        lp.x = this.xOffset;
        lp.y = this.yOffset;
        return lp;
    }

    public WindowManager getWMManager() {
        if (mContext == null) return null;
        return (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    }

    //展示Toast
    @Override
    public void show() {
        DovaTN.instance().add(this);
    }

    /**
     * 取消Toast,会清除队列中所有Toast任务
     * 因为TN中使用的是{@link this#clone()}，外部没有Toast队列中单个任务的引用，所以外部无法单独取消一个Toast任务
     */
    @Override
    public void cancel() {
        DovaTN.instance().cancelAll();
    }

    public static void cancelAll() {
        DovaTN.instance().cancelAll();
    }

    public static void cancelActivityToast(Activity mActivity) {
        DovaTN.instance().cancelActivityToast(mActivity);
    }

    public Context getContext() {
        return this.mContext;
    }

    @Override
    public DovaToast setView(View mView) {
        this.contentView = mView;
        return this;
    }

    public View getView() {
        return this.contentView;
    }

    @Override
    public DovaToast setDuration(@DToast.Duration int duration) {
        this.duration = duration;
        return this;
    }

    public int getDuration() {
        return this.duration;
    }

    @Override
    public DovaToast setAnimation(int animation) {
        this.animation = animation;
        return this;
    }

    /**
     * @param gravity {@link android.view.Gravity#TOP etc..}
     * @param xOffset pixel
     * @param yOffset pixel
     */
    @Override
    public DovaToast setGravity(int gravity, int xOffset, int yOffset) {
        this.gravity = gravity;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        return this;
    }

    @Override
    public DovaToast setGravity(int gravity) {
        return setGravity(gravity, 0, 0);
    }

    public int getGravity() {
        return gravity;
    }

    public int getXOffset() {
        return this.xOffset;
    }

    public int getYOffset() {
        return this.yOffset;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public DovaToast setPriority(int mPriority) {
        this.priority = mPriority;
        return this;
    }

    /**
     * Toast引用的contentView的可见性
     *
     * @return toast是否正在展示
     */
    public boolean isShowing() {
        return isShowing && contentView != null && contentView.isShown();
    }

    @Override
    public DovaToast clone() {
        DovaToast mToast = null;
        try {
            mToast = (DovaToast) super.clone();
            mToast.mContext = this.mContext;
            mToast.contentView = this.contentView;
            mToast.duration = this.duration;
            mToast.animation = this.animation;
            mToast.gravity = this.gravity;
            mToast.height = this.height;
            mToast.width = this.width;
            mToast.xOffset = this.xOffset;
            mToast.yOffset = this.yOffset;
            mToast.priority = this.priority;
        } catch (CloneNotSupportedException mE) {
            mE.printStackTrace();
        }
        return mToast;
    }
}
