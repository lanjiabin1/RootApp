package com.jabin.rootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

/**
 * 网络管理Fragment - 提供WiFi、蓝牙、以太网、热点等网络功能的管理
 */
public class NetworkFragment extends Fragment {

    private Button btnToggleWifi;
    private Button btnToggleBluetooth;
    private Button btnToggleEthernet;
    private Button btnToggleHotspot;
    private Button btnScanWifi;
    private NetworkManagerHelper mNetworkManager;

    public NetworkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_network, container, false);
        
        // 初始化网络管理助手
        mNetworkManager = new NetworkManagerHelper(getActivity());
        
        // 初始化UI组件
        btnToggleWifi = view.findViewById(R.id.btn_toggle_wifi);
        btnToggleBluetooth = view.findViewById(R.id.btn_toggle_bluetooth);
        btnToggleEthernet = view.findViewById(R.id.btn_toggle_ethernet);
        btnToggleHotspot = view.findViewById(R.id.btn_toggle_hotspot);
        btnScanWifi = view.findViewById(R.id.btn_scan_wifi);
        
        // 设置初始状态文本
        updateButtonTexts();
        
        // 设置点击事件
        btnToggleWifi.setOnClickListener(v -> toggleWifi());
        btnToggleBluetooth.setOnClickListener(v -> toggleBluetooth());
        btnToggleEthernet.setOnClickListener(v -> toggleEthernet());
        btnToggleHotspot.setOnClickListener(v -> toggleHotspot());
        btnScanWifi.setOnClickListener(v -> ((MainActivity) getActivity()).showWifiList());
        
        return view;
    }
    
    /**
     * 更新按钮文本，反映当前状态
     */
    private void updateButtonTexts() {
        btnToggleWifi.setText(mNetworkManager.isWifiEnabled() ? "关闭WiFi" : "开启WiFi");
        btnToggleBluetooth.setText(mNetworkManager.isBluetoothEnabled() ? "关闭蓝牙" : "开启蓝牙");
        btnToggleHotspot.setText(mNetworkManager.isHotspotEnabled() ? "关闭热点" : "开启热点");
    }
    
    /**
     * 切换WiFi状态
     */
    private void toggleWifi() {
        boolean isEnabled = mNetworkManager.isWifiEnabled();
        boolean result = mNetworkManager.setWifiEnabled(!isEnabled);
        if (result) {
            updateButtonTexts();
            Toast.makeText(getActivity(), "WiFi已" + (!isEnabled ? "开启" : "关闭"), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "WiFi切换失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 切换蓝牙状态
     */
    private void toggleBluetooth() {
        boolean isEnabled = mNetworkManager.isBluetoothEnabled();
        boolean result = mNetworkManager.setBluetoothEnabled(!isEnabled);
        if (result) {
            updateButtonTexts();
            Toast.makeText(getActivity(), "蓝牙已" + (!isEnabled ? "开启" : "关闭"), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "蓝牙切换失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 切换以太网状态
     */
    private void toggleEthernet() {
        // 这里简单实现，实际需要根据当前状态切换
        boolean result = mNetworkManager.setEthernetEnabled(true);
        if (result) {
            Toast.makeText(getActivity(), "以太网已切换", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "以太网切换失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 切换热点状态
     */
    private void toggleHotspot() {
        boolean isEnabled = mNetworkManager.isHotspotEnabled();
        boolean result = mNetworkManager.setHotspotEnabled(!isEnabled);
        if (result) {
            updateButtonTexts();
            Toast.makeText(getActivity(), "热点已" + (!isEnabled ? "开启" : "关闭"), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "热点切换失败", Toast.LENGTH_SHORT).show();
        }
    }
}