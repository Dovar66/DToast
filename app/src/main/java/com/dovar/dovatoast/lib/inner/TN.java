package com.dovar.dovatoast.lib.inner;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewManager;
import android.view.ViewParent;
import android.view.WindowManager;

import com.dovar.dovatoast.lib.DovaToast;

import java.util.LinkedList;

/**
 * @Date: 2018/11/13
 * @Author: heweizong
 * @Description:
 */
public class TN extends Handler {
    final static int REMOVE = 2;

    private final LinkedList<DovaToast> toastQueue;//列表中成员要求非空

    private TN() {
        //默认队列中最多存放8个Toast，不够的话可以自行调整到合适的值
        toastQueue = new LinkedList<>();
    }

    public static TN instance() {
        return SingletonHolder.mTn;
    }

    private static class SingletonHolder {
        private static final TN mTn = new TN();
    }

    /**
     * 新增Toast任务加入队列
     */
    public void add(DovaToast toast) {
        if (toast == null) return;
        DovaToast mToast = toast.clone();
        if (mToast == null) return;

        notifyNewToastComeIn(mToast);
    }

    //当前有toast在展示
    private boolean isShowing() {
        return toastQueue.size() > 0;
    }

    private void notifyNewToastComeIn(@NonNull DovaToast mToast) {
        boolean isShowing = isShowing();
        //加入队列
        toastQueue.add(mToast);

        //如果有toast正在展示
        if (isShowing) {
            if (toastQueue.size() == 2) {
                //获取当前正在展示的toast
                DovaToast showing = toastQueue.peek();
                //允许新加入的toast终止当前的展示
                if (mToast.getPriority() >= showing.getPriority()) {
                    //立即终止当前正在展示toast,并开始展示下一个
                    sendRemoveMsg(showing);
                } else {
                    //do nothing ...
                    return;
                }
            } else {
                //do nothing ...
                return;
            }
        } else {
            showNextToast();
        }
    }

  /*  private void updateWMLP(DovaToast toast) {
        WindowManager windowManager = getWMManager(toast);
        if (windowManager != null) {
            windowManager.updateViewLayout(toast.getView(), toast.getWMParams());
        }
    }*/

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

    public void cancelAll() {
        removeMessages(REMOVE);
        if (!toastQueue.isEmpty()) {
            DovaToast toast = toastQueue.peek();
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
     * 多个弹窗连续出现时：
     * 1.相同优先级时，会终止上一个，直接展示后一个；
     * 2.不同优先级时，如果后一个的优先级更高则会终止上一个，直接展示后一个。
     */
    private void showNextToast() {
        if (toastQueue.isEmpty()) return;
        DovaToast toast = toastQueue.peek();
        if (null == toast) {
            toastQueue.poll();
            showNextToast();
        } else {
            if (toastQueue.size() > 1) {
                DovaToast next = toastQueue.get(1);
                if (next.getPriority() >= toast.getPriority()) {
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

    private void sendRemoveMsg(DovaToast toast) {
        removeMessages(REMOVE);
        Message message = obtainMessage(REMOVE);
        message.obj = toast;
        sendMessage(message);
    }

    private void displayToast(@NonNull DovaToast toast) {
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
                    if (e.getCause() instanceof WindowManager.BadTokenException) {
                        if (e.getMessage() != null && e.getMessage().contains("token null is not valid")) {
                            new SystemToast()
                                    .setView(toastView)
                                    .setDuration(toast.getDuration())
                                    .setGravity(toast.getGravity(), toast.getXOffset(), toast.getYOffset())
                                    .show(toast.getContext());
                        }
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


}
