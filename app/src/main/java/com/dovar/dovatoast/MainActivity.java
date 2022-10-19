package com.dovar.dovatoast;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isDestroyed() || isFinishing()) return;
                ToastUtil.show("Hello MainActivity!");
            }
        }, 500);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_show) {
            if (new Random().nextInt() % 2 == 0) {
                ToastUtil.show("你是来搞笑的吗");
            } else {
                ToastUtil.showAtCenter("哇咔咔。。");
            }
        } else {
            startActivity(new Intent(this, SecondActivity.class));
        }
    }
}
