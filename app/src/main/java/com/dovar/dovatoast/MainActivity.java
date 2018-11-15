package com.dovar.dovatoast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToastUtil.show("1");
        ToastUtil.showAtCenter("2");
        ToastUtil.show("3");
        ToastUtil.show("4");
        ToastUtil.show("5");
        ToastUtil.showAtCenter("6");
        ToastUtil.showAtCenter("7");
        ToastUtil.showAtCenter("8");
    }
}
