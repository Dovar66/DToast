package com.dovar.dovatoast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToastUtil.show(this, "1");
        ToastUtil.showAtCenter(this, "2");
        ToastUtil.show(this, "3");
        ToastUtil.show(this, "4");
        ToastUtil.show(this, "5");
        ToastUtil.showAtCenter(this, "6");
        ToastUtil.showAtCenter(this, "7");
        ToastUtil.showAtCenter(this, "8");
    }
}
