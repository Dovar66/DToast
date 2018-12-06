package com.dovar.dtoast.inner;

import android.os.Handler;
import android.os.Message;

/**
 * @Date: 2018/11/16
 * @Author: heweizong
 * @Description:
 */
class SafelyHandlerWrapper extends Handler {
    private Handler impl;

    public SafelyHandlerWrapper(Handler impl) {
        this.impl = impl;
    }

    @Override
    public void dispatchMessage(Message msg) {
        try {
            impl.dispatchMessage(msg);
        } catch (Exception e) {
        }
    }

    @Override
    public void handleMessage(Message msg) {
        impl.handleMessage(msg);//需要委托给原Handler执行
    }
}
