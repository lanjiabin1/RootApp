package com.jabin.rootapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkRequest;
import android.net.wifi.SoftApConfiguration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
            Log.d(TAG, "=== setHotspotEnabled called with enabled: " + enabled + " ===");
            
            // 先检查WiFi状态，开启热点前需要关闭WiFi
            if (enabled && mWifiManager.isWifiEnabled()) {
                Log.d(TAG, "Turning off WiFi before enabling hotspot");
                boolean wifiDisabled = mWifiManager.setWifiEnabled(false);
                Log.d(TAG, "Wifi disabled result: " + wifiDisabled);
                // 等待WiFi关闭
                Thread.sleep(1000);
            }
            
            // 根据Android版本使用不同的API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "Using Android O+ API (SoftApConfiguration)");
                return setHotspotEnabledOreo(enabled);
            } else {
                Log.d(TAG, "Using legacy API");
                return setHotspotEnabledLegacy(enabled);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to set hotspot enabled: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Android 8.0及以上版本启用/禁用热点
     */
    private boolean setHotspotEnabledOreo(boolean enabled) throws Exception {
        Log.d(TAG, "setHotspotEnabledOreo called with enabled: " + enabled);
        
        // 获取SoftApConfiguration
        Method getSoftApConfigurationMethod = mWifiManager.getClass().getMethod("getSoftApConfiguration");
        SoftApConfiguration config = (SoftApConfiguration) getSoftApConfigurationMethod.invoke(mWifiManager);
        Log.d(TAG, "Current SoftApConfiguration: " + config);
        
        if (enabled) {
            Log.d(TAG, "Starting soft AP with config: " + config);
            
            // 使用反射获取SoftApCallback类
            Class<?> softApCallbackClass = Class.forName("android.net.wifi.WifiManager$SoftApCallback");
            
            // 调用startSoftAp方法
            Method startSoftApMethod = mWifiManager.getClass().getMethod("startSoftAp", 
                    SoftApConfiguration.class, softApCallbackClass);
            
            // 创建简单的callback实例（使用反射）
            Object callback = Proxy.newProxyInstance(
                    softApCallbackClass.getClassLoader(),
                    new Class<?>[]{softApCallbackClass},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) {
                            if (method.getName().equals("onStateChanged")) {
                                int state = (int) args[0];
                                int failureReason = (int) args[1];
                                Log.d(TAG, "Soft AP state changed: " + state + ", failureReason: " + failureReason);
                            }
                            return null;
                        }
                    });
            
            // 调用startSoftAp
            startSoftApMethod.invoke(mWifiManager, config, callback);
            Log.d(TAG, "startSoftAp called successfully");
            return true;
        } else {
            Log.d(TAG, "Stopping soft AP");
            Method stopSoftApMethod = mWifiManager.getClass().getMethod("stopSoftAp");
            stopSoftApMethod.invoke(mWifiManager);
            Log.d(TAG, "stopSoftAp called successfully");
            return true;
        }
    }

    /**
     * Android 8.0以下版本启用/禁用热点
     */
    private boolean setHotspotEnabledLegacy(boolean enabled) throws Exception {
        Log.d(TAG, "setHotspotEnabledLegacy called with enabled: " + enabled);
        
        // 通过反射获取当前热点配置
        Method getWifiApConfigurationMethod = mWifiManager.getClass().getMethod("getWifiApConfiguration");
        Object wifiConfig = getWifiApConfigurationMethod.invoke(mWifiManager);
        Log.d(TAG, "Got legacy wifi config: " + wifiConfig);

        // 通过反射调用setWifiApEnabled方法
        Method setWifiApEnabledMethod = mWifiManager.getClass().getMethod("setWifiApEnabled",
                Class.forName("android.net.wifi.WifiConfiguration"), boolean.class);
        boolean result = (boolean) setWifiApEnabledMethod.invoke(mWifiManager, wifiConfig, enabled);
        Log.d(TAG, "setWifiApEnabled returned: " + result);
        
        return result;
    }

    /**
     * 设置热点信息
     * @param ssid 热点名称
     * @param password 热点密码
     * @return 是否执行成功
     */
    public boolean setHotspotConfig(String ssid, String password) {
        try {
            Log.d(TAG, "=== setHotspotConfig called with ssid: " + ssid + ", password: " + password + " ===");
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "Using Android O+ API for hotspot config");
                return setHotspotConfigOreo(ssid, password);
            } else {
                Log.d(TAG, "Using legacy API for hotspot config");
                return setHotspotConfigLegacy(ssid, password);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to set hotspot config: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Android 8.0及以上版本设置热点信息
     */
    private boolean setHotspotConfigOreo(String ssid, String password) throws Exception {
        Log.d(TAG, "setHotspotConfigOreo called with ssid: " + ssid + ", password: " + password);
        
        // 使用反射创建SoftApConfiguration
        SoftApConfiguration config = createSoftApConfiguration(ssid, password);
        Log.d(TAG, "Created SoftApConfiguration: " + config);
        
        // 调用setSoftApConfiguration方法
        Method setSoftApConfigurationMethod = mWifiManager.getClass().getMethod("setSoftApConfiguration", SoftApConfiguration.class);
        boolean result = (boolean) setSoftApConfigurationMethod.invoke(mWifiManager, config);
        Log.d(TAG, "setSoftApConfiguration result: " + result);
        
        return result;
    }
    
    /**
     * 使用反射创建SoftApConfiguration
     */
    private SoftApConfiguration createSoftApConfiguration(String ssid, String password) throws Exception {
        Log.d(TAG, "createSoftApConfiguration called with ssid: " + ssid + ", password: " + password);
        
        // 使用反射创建SoftApConfiguration.Builder
        Class<?> builderClass = Class.forName("android.net.wifi.SoftApConfiguration$Builder");
        Object builder = builderClass.getConstructor().newInstance();
        
        // 反射调用setSsid方法
        Method setSsidMethod = builderClass.getMethod("setSsid", String.class);
        setSsidMethod.invoke(builder, ssid);
        Log.d(TAG, "Set SSID to: " + ssid);
        
        // 反射调用setPassphrase方法
        Method setPassphraseMethod = builderClass.getMethod("setPassphrase", String.class, int.class);
        // 获取SECURITY_TYPE_WPA2_PSK常量
        int securityTypeWpa2Psk = (int) SoftApConfiguration.class.getField("SECURITY_TYPE_WPA2_PSK").get(null);
        setPassphraseMethod.invoke(builder, password, securityTypeWpa2Psk);
        Log.d(TAG, "Set passphrase and security type to WPA2_PSK");
        
        // 反射调用build方法
        Method buildMethod = builderClass.getMethod("build");
        return (SoftApConfiguration) buildMethod.invoke(builder);
    }

    /**
     * Android 8.0以下版本设置热点信息
     */
    private boolean setHotspotConfigLegacy(String ssid, String password) throws Exception {
        Log.d(TAG, "setHotspotConfigLegacy called with ssid: " + ssid + ", password: " + password);
        
        // 获取WifiConfiguration类
        Class<?> wifiConfigClass = Class.forName("android.net.wifi.WifiConfiguration");
        Object wifiConfig = wifiConfigClass.newInstance();
        
        // 设置基本属性
        wifiConfigClass.getField("SSID").set(wifiConfig, ssid);
        wifiConfigClass.getField("preSharedKey").set(wifiConfig, password);
        wifiConfigClass.getField("hiddenSSID").set(wifiConfig, false);
        
        // 设置安全配置
        // 获取AuthAlgorithm枚举
        Class<?> authAlgorithmClass = Class.forName("android.net.wifi.WifiConfiguration$AuthAlgorithm");
        Object authOpen = authAlgorithmClass.getField("OPEN").get(null);
        wifiConfigClass.getMethod("setAllowedAuthAlgorithms", int.class).invoke(wifiConfig, 1 << (int) authAlgorithmClass.getMethod("ordinal").invoke(authOpen));
        
        // 设置协议
        Class<?> protocolClass = Class.forName("android.net.wifi.WifiConfiguration$Protocol");
        Object protocolRSN = protocolClass.getField("RSN").get(null);
        Object protocolWPA = protocolClass.getField("WPA").get(null);
        wifiConfigClass.getMethod("setAllowedProtocols", int.class).invoke(wifiConfig, 
                (1 << (int) protocolClass.getMethod("ordinal").invoke(protocolRSN)) | 
                (1 << (int) protocolClass.getMethod("ordinal").invoke(protocolWPA)));
        
        // 设置密钥管理
        Class<?> keyMgmtClass = Class.forName("android.net.wifi.WifiConfiguration$KeyMgmt");
        Object keyMgmtWPA_PSK = keyMgmtClass.getField("WPA_PSK").get(null);
        wifiConfigClass.getMethod("setAllowedKeyManagement", int.class).invoke(wifiConfig, 
                1 << (int) keyMgmtClass.getMethod("ordinal").invoke(keyMgmtWPA_PSK));
        
        // 设置加密方式
        Class<?> pairwiseCipherClass = Class.forName("android.net.wifi.WifiConfiguration$PairwiseCipher");
        Object pairwiseCCMP = pairwiseCipherClass.getField("CCMP").get(null);
        Object pairwiseTKIP = pairwiseCipherClass.getField("TKIP").get(null);
        wifiConfigClass.getMethod("setAllowedPairwiseCiphers", int.class).invoke(wifiConfig, 
                (1 << (int) pairwiseCipherClass.getMethod("ordinal").invoke(pairwiseCCMP)) | 
                (1 << (int) pairwiseCipherClass.getMethod("ordinal").invoke(pairwiseTKIP)));
        
        Class<?> groupCipherClass = Class.forName("android.net.wifi.WifiConfiguration$GroupCipher");
        Object groupCCMP = groupCipherClass.getField("CCMP").get(null);
        Object groupTKIP = groupCipherClass.getField("TKIP").get(null);
        wifiConfigClass.getMethod("setAllowedGroupCiphers", int.class).invoke(wifiConfig, 
                (1 << (int) groupCipherClass.getMethod("ordinal").invoke(groupCCMP)) | 
                (1 << (int) groupCipherClass.getMethod("ordinal").invoke(groupTKIP)));
        
        // 通过反射设置热点配置
        Method setWifiApConfigurationMethod = mWifiManager.getClass().getMethod("setWifiApConfiguration", wifiConfigClass);
        boolean result = (boolean) setWifiApConfigurationMethod.invoke(mWifiManager, wifiConfig);
        Log.d(TAG, "setWifiApConfiguration result: " + result);
        
        return result;
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
            Log.d(TAG, "setEthernetDhcp called");
            
            if (mEthernetManager == null || mEthernetManagerClass == null) {
                Log.e(TAG, "EthernetManager is null");
                return false;
            }
            
            Log.d(TAG, "EthernetManager: " + mEthernetManager + ", Class: " + mEthernetManagerClass);
            
            // 尝试通过反射获取所有方法，查看可用方法
            Method[] methods = mEthernetManagerClass.getMethods();
            Log.d(TAG, "EthernetManager methods:");
            for (Method method : methods) {
                Log.d(TAG, "  " + method.getName());
            }
            
            // 尝试设置DHCP
            try {
                // 方法1: 尝试使用setConfiguration方法设置DHCP
                Method getIpConfigurationMethod = mEthernetManagerClass.getMethod("getIpConfiguration");
                Object ipConfig = getIpConfigurationMethod.invoke(mEthernetManager);
                Log.d(TAG, "Got IP configuration: " + ipConfig);
                
                // 尝试设置IP配置类型为DHCP
                Class<?> ipConfigClass = ipConfig.getClass();
                Method setIpAssignmentMethod = ipConfigClass.getMethod("setIpAssignment", Object.class);
                // 获取IpAssignment枚举类型并设置DHCP
                Class<?> ipAssignmentClass = Class.forName("android.net.IpConfiguration$IpAssignment");
                // 使用反射获取DHCP枚举值
                Method valueOfMethod = ipAssignmentClass.getMethod("valueOf", String.class);
                Object dhcpValue = valueOfMethod.invoke(null, "DHCP");
                setIpAssignmentMethod.invoke(ipConfig, dhcpValue);
                
                // 设置代理类型为NONE
                Method setProxySettingsMethod = ipConfigClass.getMethod("setProxySettings", Object.class);
                Class<?> proxySettingsClass = Class.forName("android.net.IpConfiguration$ProxySettings");
                // 使用反射获取NONE枚举值
                Method proxyValueOfMethod = proxySettingsClass.getMethod("valueOf", String.class);
                Object noneValue = proxyValueOfMethod.invoke(null, "NONE");
                setProxySettingsMethod.invoke(ipConfig, noneValue);
                
                // 保存IP配置
                Method setConfigurationMethod = mEthernetManagerClass.getMethod("setConfiguration", ipConfigClass);
                boolean result = (boolean) setConfigurationMethod.invoke(mEthernetManager, ipConfig);
                Log.d(TAG, "setConfiguration result: " + result);
                return result;
            } catch (Exception e) {
                Log.e(TAG, "Method 1 failed: " + e.getMessage());
                
                // 方法2: 尝试使用setDhcpMethod方法（如果存在）
                try {
                    Method setDhcpMethod = mEthernetManagerClass.getMethod("setDhcp");
                    boolean result = (boolean) setDhcpMethod.invoke(mEthernetManager);
                    Log.d(TAG, "setDhcp result: " + result);
                    return result;
                } catch (Exception e2) {
                    Log.e(TAG, "Method 2 failed: " + e2.getMessage());
                    
                    // 方法3: 尝试重置以太网配置
                    try {
                        Method resetConfigurationMethod = mEthernetManagerClass.getMethod("resetConfiguration");
                        boolean result = (boolean) resetConfigurationMethod.invoke(mEthernetManager);
                        Log.d(TAG, "resetConfiguration result: " + result);
                        return result;
                    } catch (Exception e3) {
                        Log.e(TAG, "Method 3 failed: " + e3.getMessage());
                    }
                }
            }
            
            Log.e(TAG, "Failed to set Ethernet DHCP using all available methods");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Failed to set Ethernet DHCP: " + e.getMessage(), e);
            e.printStackTrace();
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