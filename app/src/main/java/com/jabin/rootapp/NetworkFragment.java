package com.jabin.rootapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

/**
 * 网络管理Fragment - 提供WiFi、蓝牙、以太网、热点等网络功能的管理
 */
public class NetworkFragment extends Fragment {

    private Button btnScanWifi;
    private Button btnEthernetSettings;
    private Button btnWlanSettings;
    private Button btnHotspotSettings;
    private Switch swWifi;
    private Switch swBluetooth;
    private Switch swEthernet;
    private Switch swHotspot;
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
        initViews(view);
        
        // 设置初始状态
        updateSwitchStates();
        
        // 设置点击事件
        setListeners();
        
        return view;
    }
    
    /**
     * 初始化UI组件
     */
    private void initViews(View view) {
        // 按钮
        btnScanWifi = view.findViewById(R.id.btn_scan_wifi);
        btnEthernetSettings = view.findViewById(R.id.btn_ethernet_settings);
        btnWlanSettings = view.findViewById(R.id.btn_wlan_settings);
        btnHotspotSettings = view.findViewById(R.id.btn_hotspot_settings);
        
        // 开关
        swWifi = view.findViewById(R.id.sw_wifi);
        swBluetooth = view.findViewById(R.id.sw_bluetooth);
        swEthernet = view.findViewById(R.id.sw_ethernet);
        swHotspot = view.findViewById(R.id.sw_hotspot);
    }
    
    /**
     * 设置事件监听器
     */
    private void setListeners() {
        // WiFi开关
        swWifi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean result = mNetworkManager.setWifiEnabled(isChecked);
            if (result) {
                Toast.makeText(getActivity(), "WiFi已" + (isChecked ? "开启" : "关闭"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "WiFi切换失败", Toast.LENGTH_SHORT).show();
                swWifi.setChecked(!isChecked); // 恢复原状
            }
        });
        
        // 蓝牙开关
        swBluetooth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean result = mNetworkManager.setBluetoothEnabled(isChecked);
            if (result) {
                Toast.makeText(getActivity(), "蓝牙已" + (isChecked ? "开启" : "关闭"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "蓝牙切换失败", Toast.LENGTH_SHORT).show();
                swBluetooth.setChecked(!isChecked); // 恢复原状
            }
        });
        
        // 热点开关
        swHotspot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d("NetworkFragment", "Attempting to set hotspot enabled: " + isChecked);
            boolean result = mNetworkManager.setHotspotEnabled(isChecked);
            if (result) {
                Toast.makeText(getActivity(), "热点已" + (isChecked ? "开启" : "关闭"), Toast.LENGTH_SHORT).show();
                Log.d("NetworkFragment", "Hotspot " + (isChecked ? "enabled" : "disabled") + " successfully");
                // 热点开启成功后，弹出热点设置对话框
                if (isChecked) {
                    showHotspotSettingsDialog();
                }
            } else {
                Toast.makeText(getActivity(), "热点切换失败", Toast.LENGTH_SHORT).show();
                Log.e("NetworkFragment", "Failed to " + (isChecked ? "enable" : "disable") + " hotspot");
                swHotspot.setChecked(!isChecked); // 恢复原状
            }
        });
        
        // 以太网开关
        swEthernet.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean result = mNetworkManager.setEthernetEnabled(isChecked);
            if (result) {
                Toast.makeText(getActivity(), "以太网已" + (isChecked ? "开启" : "关闭"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "以太网切换失败", Toast.LENGTH_SHORT).show();
                swEthernet.setChecked(!isChecked); // 恢复原状
            }
        });
        
        // 热点设置按钮
        btnHotspotSettings.setOnClickListener(v -> {
            showHotspotSettingsDialog();
        });
        
        // 扫描WiFi
        btnScanWifi.setOnClickListener(v -> ((MainActivity) getActivity()).showWifiList());
        
        // 以太网设置
        btnEthernetSettings.setOnClickListener(v -> {
            showEthernetSettingsDialog();
        });
        
        // WLAN设置
        btnWlanSettings.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "WLAN设置功能开发中", Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * 更新开关状态
     */
    private void updateSwitchStates() {
        swWifi.setChecked(mNetworkManager.isWifiEnabled());
        swBluetooth.setChecked(mNetworkManager.isBluetoothEnabled());
        swHotspot.setChecked(mNetworkManager.isHotspotEnabled());
        // 以太网状态需要额外实现获取方法
    }
    
    /**
     * 显示以太网设置对话框
     */
    private void showEthernetSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("以太网设置");
        
        // 加载自定义布局
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_ethernet_settings, null);
        builder.setView(view);
        
        // 初始化UI组件
        RadioGroup rgIpMode = view.findViewById(R.id.rg_ip_mode);
        RadioButton rbDhcp = view.findViewById(R.id.rb_dhcp);
        RadioButton rbStatic = view.findViewById(R.id.rb_static);
        LinearLayout llStaticSettings = view.findViewById(R.id.ll_static_settings);
        EditText etIpAddress = view.findViewById(R.id.et_ip_address);
        EditText etGateway = view.findViewById(R.id.et_gateway);
        EditText etNetmask = view.findViewById(R.id.et_netmask);
        EditText etDns1 = view.findViewById(R.id.et_dns1);
        EditText etDns2 = view.findViewById(R.id.et_dns2);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnSave = view.findViewById(R.id.btn_save);
        
        // 默认禁用静态IP设置区域
        llStaticSettings.setVisibility(View.GONE);
        
        // 设置IP模式切换监听
        rgIpMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_static) {
                // 选择静态IP，显示静态IP设置区域
                llStaticSettings.setVisibility(View.VISIBLE);
            } else {
                // 选择DHCP，隐藏静态IP设置区域
                llStaticSettings.setVisibility(View.GONE);
            }
        });
        
        // 创建并显示对话框
        AlertDialog dialog = builder.create();
        
        // 设置取消按钮点击事件
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        // 设置保存按钮点击事件
        btnSave.setOnClickListener(v -> {
            boolean result;
            
            if (rbStatic.isChecked()) {
                // 保存静态IP设置
                String ipAddress = etIpAddress.getText().toString().trim();
                String gateway = etGateway.getText().toString().trim();
                String netmask = etNetmask.getText().toString().trim();
                String dns1 = etDns1.getText().toString().trim();
                String dns2 = etDns2.getText().toString().trim();
                
                // 验证IP地址格式
                if (ipAddress.isEmpty() || gateway.isEmpty() || netmask.isEmpty()) {
                    Toast.makeText(getActivity(), "请填写完整的IP设置信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                result = mNetworkManager.setEthernetStaticIp(ipAddress, gateway, netmask, dns1, dns2);
                if (result) {
                    Toast.makeText(getActivity(), "静态IP设置保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "静态IP设置保存失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 保存DHCP设置
                result = mNetworkManager.setEthernetDhcp();
                if (result) {
                    Toast.makeText(getActivity(), "DHCP设置保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "DHCP设置保存失败", Toast.LENGTH_SHORT).show();
                }
            }
            
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    /**
     * 显示热点设置对话框
     */
    private void showHotspotSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("热点设置");
        
        // 加载自定义布局
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_hotspot_settings, null);
        builder.setView(view);
        
        // 初始化UI组件
        EditText etHotspotSsid = view.findViewById(R.id.et_hotspot_ssid);
        EditText etHotspotPassword = view.findViewById(R.id.et_hotspot_password);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnSave = view.findViewById(R.id.btn_save);
        
        // 创建并显示对话框
        AlertDialog dialog = builder.create();
        
        // 设置取消按钮点击事件
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        // 设置保存按钮点击事件
        btnSave.setOnClickListener(v -> {
            String ssid = etHotspotSsid.getText().toString().trim();
            String password = etHotspotPassword.getText().toString().trim();
            
            if (ssid.isEmpty()) {
                Toast.makeText(getActivity(), "请输入热点名称", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (password.isEmpty() || password.length() < 8) {
                Toast.makeText(getActivity(), "密码长度不能少于8位", Toast.LENGTH_SHORT).show();
                return;
            }
            
            boolean result = mNetworkManager.setHotspotConfig(ssid, password);
            if (result) {
                Toast.makeText(getActivity(), "热点设置保存成功", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getActivity(), "热点设置保存失败", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }
}