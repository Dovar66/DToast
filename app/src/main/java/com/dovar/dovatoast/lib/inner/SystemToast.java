package com.dovar.dovatoast.lib.inner;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.dovar.dovatoast.R;
import com.dovar.dovatoast.lib.DToast;

import java.lang.reflect.Field;

/**
 * @Date: 2018/11/19
 * @Author: heweizong
 * @Description: 使用 {@link android.widget.Toast}
 */
public class SystemToast implements IToast, Cloneable {
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
    private @DToast.Duration
    int duration = DToast.DURATION_SHORT;

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

    public static void cancelAll() {
        SystemTN.instance().cancelAll();
    }

    //不允许被外部调用
    void showInternal() {
        mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
        hookHandler(mToast);
        copyToToast(mToast);
        mToast.show();
    }

    void cancelInternal() {
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
        setupToastAnim(toast, this.animation);
        if (duration == DToast.DURATION_SHORT) {
            toast.setDuration(Toast.LENGTH_SHORT);
        } else if (duration == DToast.DURATION_LONG) {
            toast.setDuration(Toast.LENGTH_LONG);
        }
    }

    @Override
    public SystemToast setView(View mView) {
        this.contentView = mView;
        return this;
    }

    public View getView() {
        return this.contentView;
    }

    @Override
    public SystemToast setDuration(@DToast.Duration int duration) {
        this.duration = duration;
        return this;
    }

    public int getDuration() {
        return this.duration;
    }

    @Override
    public SystemToast setAnimation(int animation) {
        this.animation = animation;
        return this;
    }

    @Override
    public SystemToast setGravity(int gravity, int xOffset, int yOffset) {
        this.gravity = gravity;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        return this;
    }

    @Override
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

    @Override
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

    //捕获8.0之前Toast的BadTokenException，Google在Android 8.0的代码提交中修复了这个问题
    static void hookHandler(Toast toast) {
        if (Build.VERSION.SDK_INT >= 26) return;
        try {
            Field sField_TN = Toast.class.getDeclaredField("mTN");
            sField_TN.setAccessible(true);
            Field sField_TN_Handler = sField_TN.getType().getDeclaredField("mHandler");
            sField_TN_Handler.setAccessible(true);

            Object tn = sField_TN.get(toast);
            Handler preHandler = (Handler) sField_TN_Handler.get(tn);
            sField_TN_Handler.set(tn, new SafelyHandlerWrapper(preHandler));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置Toast动画
    static void setupToastAnim(Toast toast, int animRes) {
        try {
            Object mTN = getField(toast, "mTN");
            if (mTN != null) {
                Object mParams = getField(mTN, "mParams");
                if (mParams != null && mParams instanceof WindowManager.LayoutParams) {
                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) mParams;
                    params.windowAnimations = animRes;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 反射字段
     *
     * @param object    要反射的对象
     * @param fieldName 要反射的字段名称
     */
    private static Object getField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        if (field != null) {
            field.setAccessible(true);
            return field.get(object);
        }
        return null;
    }
}
