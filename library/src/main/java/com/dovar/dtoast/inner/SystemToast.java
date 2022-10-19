package com.dovar.dtoast.inner;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.dovar.dtoast.DToast;
import com.dovar.dtoast.DUtil;

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
    @NonNull
    private Context mContext;
    private String text = "";
    private View contentView;
    private int priority;//优先级
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
        int realDuration = (duration == DToast.DURATION_LONG) ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        if (contentView != null) {
            mToast = new Toast(mContext);
            mToast.setView(contentView);
            mToast.setDuration(realDuration);
            findValidTextView(contentView).setText(text);
        } else {
            mToast = Toast.makeText(mContext, text, realDuration);
        }
        mToast.setGravity(gravity, xOffset, yOffset);
        hookHandler(mToast);
        hookINotificationManager(mToast, mContext);
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
        if (mToast != null) {
            return mToast.getView();
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
        //do nothing
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
        return setText(text);
    }

    @Override
    public IToast setText(String text) {
        this.text = text;
        return this;
    }

    TextView findValidTextView(View view) {
        TextView result = null;
        if (view instanceof TextView) {
            if (view.getId() == View.NO_ID) {
                view.setId(android.R.id.message);
            }
            if (view.getId() == android.R.id.message) {
                result = (TextView) view;
            }
        } else {
            result = view.findViewById(android.R.id.message);
        }
        if (result == null) {
            //TextView的id必须是android:id="@android:id/message"
            throw new RuntimeException("android.R.id.message not found in contentView");
        }
        return result;
    }

    @Override
    public SystemToast clone() {
        SystemToast mToast;
        try {
            mToast = (SystemToast) super.clone();
        } catch (CloneNotSupportedException mE) {
            mE.printStackTrace();
            mToast = new SystemToast(mContext);
        }
        mToast.mContext = this.mContext;
        mToast.text = this.text;
        mToast.contentView = this.contentView;
        mToast.duration = this.duration;
        mToast.gravity = this.gravity;
        mToast.xOffset = this.xOffset;
        mToast.yOffset = this.yOffset;
        mToast.priority = this.priority;
        return mToast;
    }

    //捕获8.0之前Toast的BadTokenException，Google在Android 8.0的代码提交中修复了这个问题
    private static void hookHandler(Toast toast) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
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
    }

    private static Object iNotificationManagerProxy;

    /**
     * hook INotificationManager
     * android10应用在前台时，toast可以正常展示
     */
    private static void hookINotificationManager(Toast toast, @NonNull Context mContext) {
        if (toast == null) return;
        if (iNotificationManagerProxy != null) return;//代理不为空说明之前已设置成功
        //如果有通知权限，直接使用系统Toast
        //白名单中的机型没有通知权限时系统Toast也能正常展示
        if (NotificationManagerCompat.from(mContext).areNotificationsEnabled() || DUtil.isWhiteList()) return;
        //android10开始被标记为私有api，禁止反射调用
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            try {
                //生成INotificationManager代理
                Method getServiceMethod = Toast.class.getDeclaredMethod("getService");
                getServiceMethod.setAccessible(true);
                final Object iNotificationManagerObj = getServiceMethod.invoke(null);
                iNotificationManagerProxy = Proxy.newProxyInstance(toast.getClass().getClassLoader(), new Class[]{Class.forName("android.app.INotificationManager")}, new InvocationHandler() {
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
}
