package com.dovar.dtoast.inner;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.dovar.dtoast.DToast;
import com.dovar.dtoast.DUtil;
import com.dovar.dtoast.R;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Date: 2018/11/19
 * @Author: heweizong
 * @Description: 使用 {@link Toast}
 */
public class SystemToast implements IToast, Cloneable {
    /**
     * mToast在{@link SystemTN#displayToast(SystemToast)}中才被初始化
     */
    private Toast mToast;
    private Context mContext;
    private View contentView;
    private int priority;//优先级
    private int animation = android.R.style.Animation_Toast;
    private int gravity = Gravity.BOTTOM | Gravity.CENTER;
    private int xOffset;
    private int yOffset;
    private int duration = DToast.DURATION_SHORT;

    public SystemToast(@NonNull Context mContext) {
        this.mContext = mContext;
    }

    //外部调用
    @Override
    public void show() {
        //此时如果还未设置contentView则使用内置布局
        assertContentViewNotNull();
        SystemTN.instance().add(this);
    }

    @Override
    public void showLong() {
        this.setDuration(DToast.DURATION_LONG).show();
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
        if (mContext == null || contentView == null) return;
        mToast = new Toast(mContext);
        mToast.setView(contentView);
        mToast.setGravity(gravity, xOffset, yOffset);
        if (duration == DToast.DURATION_LONG) {
            mToast.setDuration(Toast.LENGTH_LONG);
        } else {
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        hookHandler(mToast);
        hookINotificationManager(mToast, mContext);
        setupToastAnim(mToast, this.animation);
        mToast.show();
    }

    void cancelInternal() {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
    }

    @Override
    public SystemToast setView(View mView) {
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

    private View assertContentViewNotNull() {
        if (contentView == null) {
            contentView = View.inflate(mContext, R.layout.layout_toast, null);
        }
        return contentView;
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
    public IToast setText(int id, String text) {
        TextView tv = assertContentViewNotNull().findViewById(id);
        if (tv != null) {
            tv.setText(text);
        }
        return this;
    }

    @Override
    public SystemToast clone() {
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
    private static void hookHandler(Toast toast) {
        if (toast == null || Build.VERSION.SDK_INT >= 26) return;
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
    private static void setupToastAnim(Toast toast, int animRes) {
        try {
            Object mTN = getField(toast, "mTN");
            if (mTN != null) {
                Object mParams = getField(mTN, "mParams");
                if (mParams instanceof WindowManager.LayoutParams) {
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

    private static Object iNotificationManagerProxy;

    /**
     * hook INotificationManager
     */
    private static void hookINotificationManager(Toast toast, @NonNull Context mContext) {
        if (toast == null) return;
        if (NotificationManagerCompat.from(mContext).areNotificationsEnabled() || DUtil.isMIUI())
            return;
        if (isValid4HookINotificationManager()) {
            if (iNotificationManagerProxy != null) return;//代理不为空说明之前已设置成功
            try {
                //生成INotificationManager代理
                Method getServiceMethod = Toast.class.getDeclaredMethod("getService");
                getServiceMethod.setAccessible(true);
                final Object iNotificationManagerObj = getServiceMethod.invoke(null);

                Class iNotificationManagerCls = Class.forName("android.app.INotificationManager");
                iNotificationManagerProxy = Proxy.newProxyInstance(toast.getClass().getClassLoader(), new Class[]{iNotificationManagerCls}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        DUtil.log(method.getName());
                        if ("enqueueToast".equals(method.getName()) || "enqueueToastEx".equals(method.getName())//华为p20 pro上为enqueueToastEx
                                || "cancelToast".equals(method.getName())
                                ) {
                            args[0] = "android";
                        }
                        return method.invoke(iNotificationManagerObj, args);
                    }
                });

                //使INotificationManager代理生效
                Field sServiceFiled = Toast.class.getDeclaredField("sService");
                sServiceFiled.setAccessible(true);
                sServiceFiled.set(toast, iNotificationManagerProxy);
            } catch (Exception e) {
                iNotificationManagerProxy = null;
                DUtil.log("hook INotificationManager error:" + e.getMessage());
            }
        }
    }

    /**
     * 建议只在8.0和8.1版本下采用Hook INotificationManager的方案
     * 因为8.0以下时DovaToast可以完美处理，而9.0及以上时Android不允许使用非公开api
     */
    public static boolean isValid4HookINotificationManager() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1;
    }

}
