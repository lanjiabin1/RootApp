package com.jabin.rootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

/**
 * 应用管理Fragment - 提供静默安装和卸载应用功能
 */
public class AppManagerFragment extends Fragment {

    private Button btnSilentInstall;
    private Button btnSilentUninstall;
    private Button btnShowInstalledApps;

    public AppManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_app_manager, container, false);
        
        // 初始化UI组件
        btnSilentInstall = view.findViewById(R.id.btn_silent_install);
        btnSilentUninstall = view.findViewById(R.id.btn_silent_uninstall);
        btnShowInstalledApps = view.findViewById(R.id.btn_show_installed_apps);
        
        // 设置点击事件
        btnSilentInstall.setOnClickListener(v -> ((MainActivity) getActivity()).selectApkFile());
        btnSilentUninstall.setOnClickListener(v -> ((MainActivity) getActivity()).showUninstallableApps());
        btnShowInstalledApps.setOnClickListener(v -> ((MainActivity) getActivity()).showAllInstalledApps());
        
        return view;
    }
}