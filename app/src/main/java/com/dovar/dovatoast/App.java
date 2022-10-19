package com.dovar.dovatoast;

import android.app.Application;

/**
 * Created by 贺伟宗 on 2022/10/18.
 */
public class App extends Application {

    public static Application app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }
}
