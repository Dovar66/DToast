package com.dovar.dovatoast.lib;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewManager;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * @Date: 2018/11/13
 * @Author: heweizong
 * @Description:
 */
public class TN extends Handler {
    private final static int REMOVE = 2;

    private final LinkedList<DovaToast> toastQueue;//列表中成员要求非空
//    private int priority;//Toast分优先级

    private TN() {
        //默认队列中最多存放8个Toast，不够的话可以自行调整到合适的值
        toastQueue = new LinkedList<>();
       /* toastQueue = new PriorityQueue<>(8, new Comparator<DovaToast>() {
            @Override
            public int compare(DovaToast o1, DovaToast o2) {
                if (o1.isShowing()) return -1;
                if (o1.timestamp == o2.timestamp) {
                    return 0;
                }
                return o1.timestamp <= o2.timestamp ? -1 : 1;
            }
        });*/
    }

    static TN instance() {
        return SingletonHolder.mTn;
    }

    private static class SingletonHolder {
        private static final TN mTn = new TN();
    }

    /**
     * 新增Toast任务加入队列
     *
     */
    void add(DovaToast toast) {
        if (toast == null) return;
        DovaToast mToast = toast.clone();
        if (mToast == null) return;
        //加入队列
        toastQueue.add(mToast);

        //判断是否要唤起下一个Toast任务
        if (toastQueue.size() == 1) { //队列只有当前一个任务
            showNextToast();
        } else if (toastQueue.size() == 2 && mToast.isShowing()) {//队列有两个Toast但两个Toast的contentView相同
            //先将当前正在展示的移出队列
            toastQueue.poll();
            //用新Toast布局参数更新视图,并重置剩余展示时长
            displayToast(mToast);
        }
    }

    private void updateWMLP(DovaToast toast) {
        WindowManager windowManager = getWMManager(toast);
        if (windowManager != null) {
            windowManager.updateViewLayout(toast.getView(), toast.getWMParams());
        }
    }

    private void remove(DovaToast toast) {
        toastQueue.remove(toast);
        if (toast != null && toast.isShowing()) {
            WindowManager windowManager = getWMManager(toast);
            if (windowManager != null) {
                windowManager.removeView(toast.getView());
            }
        }
        // 展示下一个Toast
        showNextToast();
    }

    void cancelAll() {
        removeMessages(REMOVE);

        for (DovaToast toast : toastQueue) {
            if (toast != null && toast.isShowing()) {
                WindowManager windowManager = getWMManager(toast);
                if (windowManager != null) {
                    windowManager.removeView(toast.getView());
                }
            }
        }
        toastQueue.clear();
    }

    /**
     * 相同contentView的多个Toast连续出现时,新的Toast会覆盖旧的
     */
    private void showNextToast() {
        if (toastQueue.isEmpty()) return;
        DovaToast toast = toastQueue.peek();
        if (null == toast) {
            toastQueue.remove(null);
            showNextToast();
        } else {
            if (toastQueue.size() > 1) {
                DovaToast next = toastQueue.get(1);
                if (toast.getView() == next.getView()) {
                    toastQueue.remove(toast);
                    showNextToast();
                } else {
                    displayToast(toast);
                }
            } else {
                displayToast(toast);
            }
        }
    }

    private void sendRemoveMsgDelay(DovaToast toast) {
        removeMessages(REMOVE);
        Message message = obtainMessage(REMOVE);
        message.obj = toast;
        sendMessageDelayed(message, toast.getDuration());
    }

    private void displayToast(@NonNull DovaToast toast) {
        if (toast.isShowing()) {
            //用新Toast布局参数更新视图,并重置剩余展示时长
            updateWMLP(toast);

            sendRemoveMsgDelay(toast);
        } else {
            WindowManager windowManager = getWMManager(toast);
            if (windowManager != null) {
                View toastView = toast.getView();
                if (toastView != null) {
                    //从父容器中移除contentView
                    ViewParent parent = toastView.getParent();
                    if (parent instanceof ViewManager) {
                        ((ViewManager) parent).removeView(toastView);
                    }
                    //再将contentView添加到WindowManager
                    try {
                        windowManager.addView(toastView, toast.getWMParams());
                    } catch (Exception e) {
                        e.printStackTrace();
                        //Android从7.1版本开始，Google对WindowManager做了一些限制和修改，特别是TYPE_TOAST类型的窗口，必须要传递一个token用于权限校验才允许添加。
                        //Toast源码在7.1及以上也有了变化，Toast的WindowManager.LayoutParams参数额外添加了一个token属性，它是在NMS中被初始化的，用于对添加的窗口类型进行校验
                        //7.1以上版本不允许同时展示多个TYPE_TOAST窗口，第二个TYPE_TOAST的WindowManager.addView()会抛出异常
                        //此时可考虑使用系统Toast
                        Log.d("DovaToast", "displayToast: windowManager.addView Error!");
                        if (e.getMessage() != null && e.getMessage().contains("token null is not valid")) {
                            Toast mToast = new Toast(toast.getContext());
                            hook(mToast);
                            mToast.setView(toastView);
                            if (toast.getDuration() == DovaToast.DURATION_SHORT) {
                                mToast.setDuration(Toast.LENGTH_SHORT);
                            } else if (toast.getDuration() == DovaToast.DURATION_LONG) {
                                mToast.setDuration(Toast.LENGTH_LONG);
                            }
                            mToast.setGravity(toast.getGravity(), toast.getXOffset(), toast.getYOffset());
                            mToast.show();
                        }
                    }

                    //展示到时间后移除
                    sendRemoveMsgDelay(toast);
                } else {
                    //没有ContentView时直接移除
                    toastQueue.remove(toast);
                    //移除一个未在展示的Toast任务后，主动唤起下一个Toast任务
                    showNextToast();
                }
            }
        }
    }

    @Override
    public void handleMessage(Message message) {
        if (message == null) return;
        switch (message.what) {
            case REMOVE:
                remove((DovaToast) message.obj);
                break;
            default:
                break;
        }
    }

    /**
     * 使用ApplicationContext
     */
    private WindowManager getWMManager(DovaToast toast) {
        if (toast == null || toast.getContext() == null) return null;
        return (WindowManager) toast.getContext().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    }

    //捕获8.0之前Toast的BadTokenException，Google在Android 8.0的代码提交中修复了这个问题
    private void hook(Toast toast) {
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
