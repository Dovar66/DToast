package com.dovar.dtoast.inner;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.dovar.dtoast.DToast;
import com.dovar.dtoast.DUtil;
import com.dovar.dtoast.R;

import androidx.annotation.NonNull;


/**
 * @Date: 2018/11/13
 * @Author: heweizong
 * @Description: 解决通知权限被关闭时系统Toast无法正常展示的问题.
 * 使用{@link DovaToast}出现{@link WindowManager.BadTokenException}时，再尝试使用{@link ActivityToast}
 */
public class DovaToast implements IToast, Cloneable {
    static long Count4BadTokenException = 0;//记录DovaToast连续抛出token null is not valid异常的次数

    Context mContext;
    private View contentView;
    private int priority;//优先级
    private long timestamp;//时间戳
    private int animation = android.R.style.Animation_Toast;
    private int gravity = Gravity.BOTTOM | Gravity.CENTER;
    private int xOffset;
    private int yOffset;
    private int width = WindowManager.LayoutParams.WRAP_CONTENT;
    private int height = WindowManager.LayoutParams.WRAP_CONTENT;
    private int duration = DToast.DURATION_SHORT;

    boolean isShowing;//TN标记为正在展示

    public DovaToast(@NonNull Context mContext) {
        this.mContext = mContext;
    }

    //展示Toast
    @Override
    public void show() {
        //此时如果还未设置contentView则使用内置布局
        assertContentViewNotNull();
        DovaTN.instance().add(this);
    }

    @Override
    public void showLong() {
        this.setDuration(DToast.DURATION_LONG).show();
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

    protected WindowManager.LayoutParams getWMParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.format = PixelFormat.TRANSLUCENT;
        //targetSdkVersion>=26且运行在8.0以上系统时，TYPE_TOAST可能会addView()失败，所以如果此条件下应用已获取到悬浮窗权限则使用TYPE_APPLICATION_OVERLAY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Settings.canDrawOverlays(mContext)) {
            //为什么是使用TYPE_APPLICATION_OVERLAY？
            //因为8.0+系统，使用SYSTEM_ALERT_WINDOW 权限的应用无法再使用TYPE_PHONE、TYPE_SYSTEM_ALERT、TYPE_SYSTEM_OVERLAY等窗口类型来显示弹窗(permission denied for this window type)
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        lp.height = this.height;
        lp.width = this.width;
        lp.windowAnimations = this.animation;
        lp.gravity = this.gravity;
        lp.x = this.xOffset;
        lp.y = this.yOffset;
        return lp;
    }

    protected WindowManager getWMManager() {
        if (mContext == null) return null;
        return (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    }

    public Context getContext() {
        return this.mContext;
    }

    @Override
    public DovaToast setView(View mView) {
        if (mView == null) {
            DUtil.log("contentView cannot be null!");
            return this;
        }
        this.contentView = mView;
        return this;
    }

    @Override
    public View getView() {
        return assertContentViewNotNull();
    }

    View getViewInternal() {
        return this.contentView;
    }

    private View assertContentViewNotNull() {
        if (contentView == null) {
            contentView = View.inflate(mContext, R.layout.layout_toast, null);
        }
        return contentView;
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

    @Override
    public IToast setText(int id, String text) {
        TextView tv = assertContentViewNotNull().findViewById(id);
        if (tv != null) {
            tv.setText(text);
        }
        return this;
    }

    @Override
    public IToast setText(String text) {
        int id = mContext.getResources().getIdentifier("message", "id", "com.android.internal");
        return setText(id,text);
    }

    long getTimestamp() {
        return timestamp;
    }

    DovaToast setTimestamp(long mTimestamp) {
        timestamp = mTimestamp;
        return this;
    }

    /**
     * Toast引用的contentView的可见性
     *
     * @return toast是否正在展示
     */
    boolean isShowing() {
        return isShowing && contentView != null && contentView.isShown();
    }

    //当DovaToast连续出现token null is not valid异常时，不再推荐使用DovaToast
    public static boolean isBadChoice() {
        return Count4BadTokenException >= 5;
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
