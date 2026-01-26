package com.jabin.rootapp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

/**
 * 电源控制Fragment - 提供关机、重启、恢复出厂设置功能
 */
public class PowerControlFragment extends Fragment {

    private Button btnShutdown;
    private Button btnReboot;
    private Button btnFactoryReset;
    private Button btnOpenSettings;
    private PowerManagerHelper mPowerManager;

    public PowerControlFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_power_control, container, false);
        
        // 初始化电源管理助手
        mPowerManager = new PowerManagerHelper(getActivity());
        
        // 初始化UI组件
        btnShutdown = view.findViewById(R.id.btn_shutdown);
        btnReboot = view.findViewById(R.id.btn_reboot);
        btnFactoryReset = view.findViewById(R.id.btn_factory_reset);
        btnOpenSettings = view.findViewById(R.id.btn_open_settings);
        
        // 设置点击事件
        btnShutdown.setOnClickListener(v -> ((MainActivity) getActivity()).shutdown());
        btnReboot.setOnClickListener(v -> ((MainActivity) getActivity()).reboot());
        btnFactoryReset.setOnClickListener(v -> ((MainActivity) getActivity()).factoryReset());
        btnOpenSettings.setOnClickListener(v -> openSystemSettings());
        
        return view;
    }
    
    /**
     * 打开系统设置
     */
    private void openSystemSettings() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        startActivity(intent);
    }
}