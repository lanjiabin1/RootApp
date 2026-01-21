package com.jabin.rootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * 系统信息Fragment - 显示设备的系统信息，包括SN号、MAC地址、IP地址等
 */
public class SystemInfoFragment extends Fragment {

    private Button btnGetSystemInfo;
    private TextView tvSystemInfo;
    private SystemInfoHelper mSystemInfoHelper;

    public SystemInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_system_info, container, false);
        
        // 初始化系统信息助手
        mSystemInfoHelper = new SystemInfoHelper(getActivity());
        
        // 初始化UI组件
        btnGetSystemInfo = view.findViewById(R.id.btn_get_system_info);
        tvSystemInfo = view.findViewById(R.id.tv_system_info);
        
        // 设置点击事件
        btnGetSystemInfo.setOnClickListener(v -> ((MainActivity) getActivity()).getSystemInfo());
        
        // 自动加载系统信息
        ((MainActivity) getActivity()).getSystemInfo();
        
        return view;
    }
    
    /**
     * 更新系统信息显示
     * @param info 系统信息字符串
     */
    public void updateSystemInfo(String info) {
        tvSystemInfo.setText(info);
    }
}