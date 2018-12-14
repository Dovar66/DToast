package com.dovar.dtoast.inner;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewManager;
import android.view.ViewParent;
import android.view.WindowManager;

import com.dovar.dtoast.DUtil;

import java.util.Comparator;

/**
 * @Date: 2018/11/13
 * @Author: heweizong
 * @Description:
 */
class DovaTN extends Handler {
    final static int REMOVE = 2;

    private final DPriorityQueue<DovaToast> toastQueue;//列表中成员要求非空

    private DovaTN() {
        toastQueue = new DPriorityQueue<>(new Comparator<DovaToast>() {
            @Override
            public int compare(DovaToast x, DovaToast y) {
                //往队列中add元素时，x为新增，y为原队列中元素
                // skip showing DToast
                if (y.isShowing()) return 1;
                if (x.getTimestamp() == y.getTimestamp()) return 0;
                return x.getTimestamp() < y.getTimestamp() ? -1 : 1;//值小的排队首
            }
        });
    }

    static DovaTN instance() {
        return SingletonHolder.mTn;
    }

    private static class SingletonHolder {
        private static final DovaTN mTn = new DovaTN();
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
        //检查有没有时间戳，没有则一定要打上时间戳
        if (mToast.getTimestamp() <= 0) {
            mToast.setTimestamp(System.currentTimeMillis());
        }
        //然后加入队列
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

    private void remove(DovaToast toast) {
        toastQueue.remove(toast);
        removeInternal(toast);
    }

    void cancelAll() {
        removeMessages(REMOVE);
        if (!toastQueue.isEmpty()) {
            removeInternal(toastQueue.peek());
        }
        toastQueue.clear();
    }

    void cancelActivityToast(Activity activity) {
        if (activity == null) return;
        for (DovaToast t : toastQueue
                ) {
            if (t instanceof ActivityToast && t.getContext() == activity) {
                remove(t);
            }
        }
    }

    private void removeInternal(DovaToast toast) {
        if (toast != null && toast.isShowing()) {
            // 2018/11/26 逻辑存在问题：队列中多个Toast使用相同ContentView时可能造成混乱。
            // 不过，不同时展示多个Toast的话，也不会出现此问题.因为next.show()在last.removeView()动作之后。
            // DToast不会同时展示多个Toast，因此成功避免了此问题
            WindowManager windowManager = toast.getWMManager();
            if (windowManager != null) {
                try {
                    DUtil.log("removeInternal: removeView");
                    windowManager.removeView(toast.getView());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            toast.isShowing = false;
        }
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
        WindowManager windowManager = toast.getWMManager();
        if (windowManager == null) return;
        View toastView = toast.getView();
        if (toastView == null) {
            //没有ContentView时直接移除
            toastQueue.remove(toast);
            //移除一个未在展示的Toast任务后，主动唤起下一个Toast任务
            showNextToast();
            return;
        }
        //从父容器中移除contentView
        ViewParent parent = toastView.getParent();
        if (parent instanceof ViewManager) {
            ((ViewManager) parent).removeView(toastView);
        }
        //再将contentView添加到WindowManager
        try {
            DUtil.log("displayToast: addView");
            windowManager.addView(toastView, toast.getWMParams());

            //确定展示成功后
            toast.isShowing = true;
            //展示到时间后移除
            sendRemoveMsgDelay(toast);
        } catch (Exception e) {
            if (e instanceof WindowManager.BadTokenException &&
                    e.getMessage() != null && e.getMessage().contains("token null is not valid")) {
                if (toast instanceof ActivityToast) {
                    //如果ActivityToast也无法展示的话，暂时只能选择放弃治疗了，难受...
                    DovaToast.Count4BadTokenException = 0;
                } else {
                    DovaToast.Count4BadTokenException++;
                    //尝试使用ActivityToast
                    if (toast.getContext() instanceof Activity) {
                        //因为DovaToast未展示成功，需要主动移除,然后再尝试使用ActivityToast
                        toastQueue.remove(toast);//从队列移除
                        removeMessages(REMOVE);//清除已发送的延时消息
                        toast.isShowing = false;//更新toast状态
                        try {
                            //尝试从窗口移除toastView，虽然windowManager.addView()抛出异常，但toastView仍然可能已经被添加到窗口父容器中(具体看ROM实现)，所以需要主动移除
                            //因为toastView也可能没有被添加到窗口父容器，所以需要增加try-catch
                            windowManager.removeViewImmediate(toastView);
                        } catch (Exception me) {
                            DUtil.log("windowManager removeViewImmediate error.Do not care this!");
                        }
                        new ActivityToast(toast.getContext())
                                .setTimestamp(toast.getTimestamp())
                                .setView(toastView)
                                .setDuration(toast.getDuration())
                                .setGravity(toast.getGravity(), toast.getXOffset(), toast.getYOffset())
                                .show();
                        return;
                    }
                }
            }
            e.printStackTrace();
        }
    }


    @Override
    public void handleMessage(Message message) {
        if (message == null) return;
        switch (message.what) {
            case REMOVE:
                //移除当前
                remove((DovaToast) message.obj);
                // 展示下一个Toast
                showNextToast();
                break;
            default:
                break;
        }
    }
}
