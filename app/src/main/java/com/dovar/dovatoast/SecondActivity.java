package com.dovar.dovatoast;

import android.os.Handler;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isDestroyed() || isFinishing()) return;
                ToastUtil.show("Hello SecondActivity!");
            }
        }, 500);
    }



    @Override
    public void onClick(View v) {
        ToastUtil.show("我就是来搞笑的");
    }

}
