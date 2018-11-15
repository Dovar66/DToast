package com.dovar.dovatoast.lib;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

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
                    try {
                        windowManager.addView(toastView, toast.getWMParams());
                    } catch (Exception e) {
                        e.printStackTrace();
                        // TODO: 2018/11/13
                        //多TYPE_TOAST窗口同时展示时在7.1以上版本会抛出异常
                        //此时可考虑使用系统Toast
                        Log.d("DovaToast", "displayToast: windowManager.addView Error!");
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
}
