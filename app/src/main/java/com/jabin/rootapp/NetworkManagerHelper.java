package com.jabin.rootapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 网络管理助手类，提供以太网、WiFi、蓝牙、热点等网络功能的管理
 */
public class NetworkManagerHelper {

    private static final String TAG = "NetworkManagerHelper";
    private Context mContext;
    private ConnectivityManager mConnectivityManager;
    private WifiManager mWifiManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Object mEthernetManager;
    private Class<?> mEthernetManagerClass;

    public NetworkManagerHelper(Context context) {
        this.mContext = context;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // 使用反射方式获取EthernetManager实例
        try {
            mEthernetManagerClass = Class.forName("android.net.EthernetManager");
            mEthernetManager = mContext.getSystemService("ethernet");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to get EthernetManager: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用以太网
     * @param enabled 是否启用
     * @return 是否执行成功
     */
    public boolean setEthernetEnabled(boolean enabled) {
        try {
            if (mEthernetManager != null && mEthernetManagerClass != null) {
                Method setEnabledMethod = mEthernetManagerClass.getMethod("setEthernetEnabled", boolean.class);
                setEnabledMethod.invoke(mEthernetManager, enabled);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to set Ethernet enabled: " + e.getMessage());
            return false;
        }
    }

    /**
     * 启用/禁用WiFi
     * @param enabled 是否启用
     * @return 是否执行成功
     */
    public boolean setWifiEnabled(boolean enabled) {
        try {
            return mWifiManager.setWifiEnabled(enabled);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to set WiFi enabled: " + e.getMessage());
            return false;
        }
    }

    /**
     * 启用/禁用蓝牙
     * @param enabled 是否启用
     * @return 是否执行成功
     */
    public boolean setBluetoothEnabled(boolean enabled) {
        try {
            if (enabled) {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return false;
                }
                return mBluetoothAdapter.enable();
            } else {
                return mBluetoothAdapter.disable();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to set Bluetooth enabled: " + e.getMessage());
            return false;
        }
    }

    /**
     * 连接到指定WiFi网络
     * @param ssid WiFi名称
     * @param password WiFi密码
     * @return 是否执行成功
     */
    public boolean connectToWifi(String ssid, String password) {
        try {
            // 创建WiFi配置
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = "\"" + ssid + "\"";
            wifiConfig.preSharedKey = "\"" + password + "\"";

            // 添加网络并连接
            int netId = mWifiManager.addNetwork(wifiConfig);
            if (netId != -1) {
                return mWifiManager.enableNetwork(netId, true);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to connect to WiFi: " + e.getMessage());
            return false;
        }
    }

    /**
     * 启用/禁用热点
     * @param enabled 是否启用
     * @return 是否执行成功
     */
    public boolean setHotspotEnabled(boolean enabled) {
        try {
            // 通过反射调用WifiManager的setWifiApEnabled方法
            Method getWifiApConfigurationMethod = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            Object wifiConfig = getWifiApConfigurationMethod.invoke(mWifiManager);

            Method setWifiApEnabledMethod = mWifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);
            return (boolean) setWifiApEnabledMethod.invoke(mWifiManager, wifiConfig, enabled);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to set hotspot enabled: " + e.getMessage());
            return false;
        }
    }

    /**
     * 设置热点信息
     * @param ssid 热点名称
     * @param password 热点密码
     * @return 是否执行成功
     */
    public boolean setHotspotConfig(String ssid, String password) {
        try {
            // 创建热点配置
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = ssid;
            wifiConfig.preSharedKey = password;
            wifiConfig.hiddenSSID = false;
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            // 通过反射设置热点配置
            Method setWifiApConfigurationMethod = mWifiManager.getClass().getMethod("setWifiApConfiguration",
                    WifiConfiguration.class);
            return (boolean) setWifiApConfigurationMethod.invoke(mWifiManager, wifiConfig);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to set hotspot config: " + e.getMessage());
            return false;
        }
    }

    /**
     * 设置以太网静态IP
     * @param ipAddress IP地址
     * @param gateway 网关
     * @param netmask 子网掩码
     * @param dns1 首选DNS
     * @param dns2 备用DNS
     * @return 是否执行成功
     */
    public boolean setEthernetStaticIp(String ipAddress, String gateway, String netmask, String dns1, String dns2) {
        try {
            if (mEthernetManager != null && mEthernetManagerClass != null) {
                // 通过反射设置静态IP
                // 这里需要根据实际的EthernetManager实现来调整
                Log.d(TAG, "Setting Ethernet static IP is not fully implemented");
                return false;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to set Ethernet static IP: " + e.getMessage());
            return false;
        }
    }

    /**
     * 设置以太网动态IP（DHCP）
     * @return 是否执行成功
     */
    public boolean setEthernetDhcp() {
        try {
            if (mEthernetManager != null && mEthernetManagerClass != null) {
                // 通过反射设置DHCP
                // 这里需要根据实际的EthernetManager实现来调整
                Log.d(TAG, "Setting Ethernet DHCP is not fully implemented");
                return false;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to set Ethernet DHCP: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前WiFi状态
     * @return WiFi是否启用
     */
    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    /**
     * 获取当前蓝牙状态
     * @return 蓝牙是否启用
     */
    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * 获取当前热点状态
     * @return 热点是否启用
     */
    public boolean isHotspotEnabled() {
        try {
            Method isWifiApEnabledMethod = mWifiManager.getClass().getMethod("isWifiApEnabled");
            return (boolean) isWifiApEnabledMethod.invoke(mWifiManager);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to check hotspot enabled: " + e.getMessage());
            return false;
        }
    }

}