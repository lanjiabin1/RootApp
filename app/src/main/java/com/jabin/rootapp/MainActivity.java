package com.jabin.rootapp;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    Button btnShutdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initOnClick();
    }

    public void initView() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnShutdown = findViewById(R.id.btn_shutdown);

    }
    public void initOnClick() {
        Toast.makeText(this, "点击了关机按钮", Toast.LENGTH_SHORT).show();
        btnShutdown.setOnClickListener(v -> shutdown());
    }

    public void shutdown() {
        try {
            // 方法1：通过反射调用（适用于各种系统版本）
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            Method shutdownMethod = pm.getClass().getMethod("shutdown",
                    boolean.class, String.class, boolean.class);
            shutdownMethod.invoke(pm, false, null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}