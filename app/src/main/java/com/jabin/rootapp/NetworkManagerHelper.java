package com.jabin.rootapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
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
        
        if (enabled) {
            Log.d(TAG, "Starting soft AP...");
            
            // 查找并调用正确的startSoftAp方法
            Method[] methods = mWifiManager.getClass().getMethods();
            Log.d(TAG, "Searching for startSoftAp method...");
            
            for (Method method : methods) {
                String methodName = method.getName();
                if (methodName.equals("startSoftAp")) {
                    Log.d(TAG, "Found method: " + method.getName());
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Log.d(TAG, "  Parameter types: ");
                    for (Class<?> paramType : parameterTypes) {
                        Log.d(TAG, "    - " + paramType.getName());
                    }
                    
                    try {
                        // 根据参数类型调用不同的方法
                        if (parameterTypes.length == 1) {
                            if (parameterTypes[0] == SoftApConfiguration.class) {
                                // 方法签名: startSoftAp(SoftApConfiguration)
                                Log.d(TAG, "Calling startSoftAp with SoftApConfiguration");
                                Method getSoftApConfigurationMethod = mWifiManager.getClass().getMethod("getSoftApConfiguration");
                                SoftApConfiguration config = (SoftApConfiguration) getSoftApConfigurationMethod.invoke(mWifiManager);
                                method.invoke(mWifiManager, config);
                                Log.d(TAG, "startSoftAp called successfully");
                                return true;
                            } else if (parameterTypes[0].getName().equals("android.net.wifi.WifiConfiguration")) {
                                // 方法签名: startSoftAp(WifiConfiguration)
                                Log.d(TAG, "Calling startSoftAp with WifiConfiguration");
                                // 获取WifiConfiguration
                                Method getWifiApConfigurationMethod = mWifiManager.getClass().getMethod("getWifiApConfiguration");
                                Object wifiConfig = getWifiApConfigurationMethod.invoke(mWifiManager);
                                method.invoke(mWifiManager, wifiConfig);
                                Log.d(TAG, "startSoftAp called successfully");
                                return true;
                            }
                        } else if (parameterTypes.length == 2) {
                            if (parameterTypes[0] == SoftApConfiguration.class) {
                                if (parameterTypes[1].isInterface()) {
                                    // 方法签名: startSoftAp(SoftApConfiguration, SoftApCallback)
                                    Log.d(TAG, "Calling startSoftAp with SoftApConfiguration and callback");
                                    Method getSoftApConfigurationMethod = mWifiManager.getClass().getMethod("getSoftApConfiguration");
                                    SoftApConfiguration config = (SoftApConfiguration) getSoftApConfigurationMethod.invoke(mWifiManager);
                                    // 创建SoftApCallback代理
                                    Object callback = Proxy.newProxyInstance(
                                            parameterTypes[1].getClassLoader(),
                                            new Class<?>[]{parameterTypes[1]},
                                            new InvocationHandler() {
                                                @Override
                                                public Object invoke(Object proxy, Method cbMethod, Object[] args) {
                                                    if (cbMethod.getName().equals("onStateChanged")) {
                                                        int state = (int) args[0];
                                                        int failureReason = (int) args[1];
                                                        Log.d(TAG, "Soft AP state changed: " + state + ", failureReason: " + failureReason);
                                                    }
                                                    return null;
                                                }
                                            });
                                    method.invoke(mWifiManager, config, callback);
                                    Log.d(TAG, "startSoftAp called successfully");
                                    return true;
                                } else if (parameterTypes[1] == int.class) {
                                    // 方法签名: startSoftAp(SoftApConfiguration, int)
                                    Log.d(TAG, "Calling startSoftAp with SoftApConfiguration and int");
                                    Method getSoftApConfigurationMethod = mWifiManager.getClass().getMethod("getSoftApConfiguration");
                                    SoftApConfiguration config = (SoftApConfiguration) getSoftApConfigurationMethod.invoke(mWifiManager);
                                    method.invoke(mWifiManager, config, 0);
                                    Log.d(TAG, "startSoftAp called successfully");
                                    return true;
                                }
                            } else if (parameterTypes[0].getName().equals("android.net.wifi.WifiConfiguration")) {
                                // 方法签名: startSoftAp(WifiConfiguration, int) 或 startSoftAp(WifiConfiguration, callback)
                                Log.d(TAG, "Calling startSoftAp with WifiConfiguration and second parameter");
                                Method getWifiApConfigurationMethod = mWifiManager.getClass().getMethod("getWifiApConfiguration");
                                Object wifiConfig = getWifiApConfigurationMethod.invoke(mWifiManager);
                                
                                if (parameterTypes[1].isInterface()) {
                                    // 创建代理回调
                                    Object callback = Proxy.newProxyInstance(
                                            parameterTypes[1].getClassLoader(),
                                            new Class<?>[]{parameterTypes[1]},
                                            new InvocationHandler() {
                                                @Override
                                                public Object invoke(Object proxy, Method cbMethod, Object[] args) {
                                                    Log.d(TAG, "Soft AP callback invoked: " + cbMethod.getName());
                                                    return null;
                                                }
                                            });
                                    method.invoke(mWifiManager, wifiConfig, callback);
                                } else if (parameterTypes[1] == int.class) {
                                    method.invoke(mWifiManager, wifiConfig, 0);
                                } else {
                                    // 尝试传递null作为第二个参数
                                    method.invoke(mWifiManager, wifiConfig, null);
                                }
                                Log.d(TAG, "startSoftAp called successfully");
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to call startSoftAp: " + e.getMessage());
                        // 继续尝试其他方法
                        continue;
                    }
                }
            }
            
            Log.e(TAG, "No suitable startSoftAp method found");
            return false;
        } else {
            Log.d(TAG, "Stopping soft AP");
            
            // 查找并调用stopSoftAp方法
            Method[] methods = mWifiManager.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().equals("stopSoftAp")) {
                    Log.d(TAG, "Found stopSoftAp method");
                    method.invoke(mWifiManager);
                    Log.d(TAG, "stopSoftAp called successfully");
                    return true;
                }
            }
            
            Log.e(TAG, "No stopSoftAp method found");
            return false;
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
            Log.d(TAG, "setEthernetStaticIp called with ip: " + ipAddress + ", gateway: " + gateway + ", netmask: " + netmask + ", dns1: " + dns1 + ", dns2: " + dns2);
            
            if (mEthernetManager == null || mEthernetManagerClass == null) {
                Log.e(TAG, "EthernetManager is null");
                return false;
            }
            
            Log.d(TAG, "EthernetManager: " + mEthernetManager + ", Class: " + mEthernetManagerClass);
            
            // 获取可用的以太网接口列表
            List<String> interfaces = new ArrayList<>();
            try {
                // 尝试获取接口列表
                Method getInterfaceListMethod = mEthernetManagerClass.getMethod("getInterfaceList");
                Object result = getInterfaceListMethod.invoke(mEthernetManager);
                if (result instanceof List) {
                    interfaces = (List<String>) result;
                    Log.d(TAG, "Available interfaces: " + interfaces);
                } else {
                    Log.d(TAG, "getInterfaceList returned: " + result);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to get interface list: " + e.getMessage());
                // 如果获取接口列表失败，默认使用eth0
                interfaces.add("eth0");
            }
            
            // 如果没有可用接口，添加默认eth0
            if (interfaces.isEmpty()) {
                interfaces.add("eth0");
                Log.d(TAG, "Added default interface eth0");
            }
            
            // 尝试设置静态IP配置
            for (String iface : interfaces) {
                Log.d(TAG, "Processing interface: " + iface);
                
                try {
                    // 获取当前配置
                    Method getConfigurationMethod = mEthernetManagerClass.getMethod("getConfiguration", String.class);
                    Object config = getConfigurationMethod.invoke(mEthernetManager, iface);
                    Log.d(TAG, "Got configuration for " + iface + ": " + config);
                    
                    if (config != null) {
                        // 直接使用配置对象，日志显示它已经是IpConfiguration类型
                        Class<?> ipConfigClass = config.getClass();
                        Log.d(TAG, "Configuration class: " + ipConfigClass.getName());
                        
                        // 设置IP分配方式为STATIC
                        try {
                            // 动态查找setIpAssignment方法并获取正确的参数类型
                            Method setIpAssignmentMethod = null;
                            Class<?> correctParamType = null;
                            
                            // 遍历所有方法，找到setIpAssignment方法
                            for (Method method : ipConfigClass.getMethods()) {
                                if (method.getName().equals("setIpAssignment")) {
                                    Class<?>[] params = method.getParameterTypes();
                                    if (params.length == 1) {
                                        setIpAssignmentMethod = method;
                                        correctParamType = params[0];
                                        Log.d(TAG, "Found setIpAssignment method with param type: " + correctParamType.getName());
                                        break;
                                    }
                                }
                            }
                            
                            if (setIpAssignmentMethod != null && correctParamType != null) {
                                // 获取IpAssignment枚举类型和STATIC值
                                Class<?> ipAssignmentClass = correctParamType;
                                Object staticValue;
                                
                                try {
                                    // 尝试直接获取STATIC枚举值
                                    staticValue = ipAssignmentClass.getField("STATIC").get(null);
                                    Log.d(TAG, "Got STATIC value: " + staticValue);
                                } catch (Exception e) {
                                    // 尝试通过valueOf方法获取
                                    Method valueOfMethod = ipAssignmentClass.getMethod("valueOf", String.class);
                                    staticValue = valueOfMethod.invoke(null, "STATIC");
                                    Log.d(TAG, "Got STATIC value via valueOf: " + staticValue);
                                }
                                
                                // 调用setIpAssignment方法，设置为STATIC
                                setIpAssignmentMethod.invoke(config, staticValue);
                                Log.d(TAG, "Set IpAssignment to STATIC");
                                
                                // 设置静态IP地址信息
                                // 首先查找setStaticIpConfiguration方法
                                Method setStaticIpConfigurationMethod = null;
                                for (Method method : ipConfigClass.getMethods()) {
                                    if (method.getName().equals("setStaticIpConfiguration")) {
                                        setStaticIpConfigurationMethod = method;
                                        break;
                                    }
                                }
                                
                                if (setStaticIpConfigurationMethod != null) {
                                    // 获取StaticIpConfiguration类
                                    Class<?> staticIpConfigClass = Class.forName("android.net.StaticIpConfiguration");
                                    Object staticIpConfig = staticIpConfigClass.newInstance();
                                    
                                    // 设置IP地址
                                    InetAddress ipAddr = InetAddress.getByName(ipAddress);
                                    InetAddress gatewayAddr = InetAddress.getByName(gateway);
                                    InetAddress dns1Addr = dns1.isEmpty() ? null : InetAddress.getByName(dns1);
                                    InetAddress dns2Addr = dns2.isEmpty() ? null : InetAddress.getByName(dns2);
                                    
                                    // 设置IP地址和前缀长度
                                    int prefixLength = 24; // 默认子网掩码255.255.255.0对应的前缀长度
                                    try {
                                        // 从子网掩码计算前缀长度
                                        String[] maskParts = netmask.split("\\.");
                                        if (maskParts.length == 4) {
                                            int mask = 0;
                                            for (String part : maskParts) {
                                                mask = (mask << 8) | Integer.parseInt(part);
                                            }
                                            prefixLength = 32 - Integer.numberOfTrailingZeros(mask);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Failed to calculate prefix length, using default 24: " + e.getMessage());
                                    }
                                    
                                    // 设置StaticIpConfiguration
                                    java.lang.reflect.Field linkPropertiesField = staticIpConfigClass.getDeclaredField("ipAddress");
                                    linkPropertiesField.setAccessible(true);
                                    
                                    // 使用反射创建和配置LinkAddress对象
                                    try {
                                        // 获取LinkAddress类
                                        Class<?> linkAddressClass = Class.forName("android.net.LinkAddress");
                                        
                                        // 尝试查找合适的构造器
                                        java.lang.reflect.Constructor<?> constructor = null;
                                        Object linkAddress = null;
                                        
                                        // 尝试使用有参构造器
                                        try {
                                            // 尝试使用InetAddress和int参数的构造器
                                            constructor = linkAddressClass.getDeclaredConstructor(InetAddress.class, int.class);
                                            constructor.setAccessible(true);
                                            linkAddress = constructor.newInstance(ipAddr, prefixLength);
                                            Log.d(TAG, "Successfully created LinkAddress with InetAddress and int constructor");
                                        } catch (NoSuchMethodException e) {
                                            Log.e(TAG, "No InetAddress and int constructor found: " + e.getMessage());
                                            
                                            // 尝试使用String和int参数的构造器
                                            try {
                                                constructor = linkAddressClass.getDeclaredConstructor(String.class, int.class);
                                                constructor.setAccessible(true);
                                                linkAddress = constructor.newInstance(ipAddress, prefixLength);
                                                Log.d(TAG, "Successfully created LinkAddress with String and int constructor");
                                            } catch (NoSuchMethodException e2) {
                                                Log.e(TAG, "No String and int constructor found: " + e2.getMessage());
                                                
                                                // 尝试使用单个String参数的构造器
                                                try {
                                                    constructor = linkAddressClass.getDeclaredConstructor(String.class);
                                                    constructor.setAccessible(true);
                                                    // 格式: "ip/prefix" 例如: "192.168.1.100/24"
                                                    String ipWithPrefix = ipAddress + "/" + prefixLength;
                                                    linkAddress = constructor.newInstance(ipWithPrefix);
                                                    Log.d(TAG, "Successfully created LinkAddress with String constructor: " + ipWithPrefix);
                                                } catch (NoSuchMethodException e3) {
                                                    Log.e(TAG, "No suitable constructor found for LinkAddress: " + e3.getMessage());
                                                }
                                            }
                                        }
                                        
                                        if (linkAddress != null) {
                                            // 将创建好的LinkAddress对象设置到静态IP配置中
                                            linkPropertiesField.set(staticIpConfig, linkAddress);
                                            Log.d(TAG, "Successfully set LinkAddress to static IP configuration");
                                        } else {
                                            // 如果无法创建LinkAddress对象，尝试设置为null
                                            linkPropertiesField.set(staticIpConfig, null);
                                            Log.d(TAG, "Set LinkAddress to null due to creation failure");
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Failed to handle LinkAddress via reflection: " + e.getMessage());
                                        // 如果反射失败，尝试其他方法
                                        // 直接使用null或其他默认值
                                        linkPropertiesField.set(staticIpConfig, null);
                                    }
                                    
                                    java.lang.reflect.Field gatewayField = staticIpConfigClass.getDeclaredField("gateway");
                                    gatewayField.setAccessible(true);
                                    gatewayField.set(staticIpConfig, gatewayAddr);
                                    
                                    java.lang.reflect.Field dnsServersField = staticIpConfigClass.getDeclaredField("dnsServers");
                                    dnsServersField.setAccessible(true);
                                    ArrayList<InetAddress> dnsList = new ArrayList<>();
                                    if (dns1Addr != null) dnsList.add(dns1Addr);
                                    if (dns2Addr != null) dnsList.add(dns2Addr);
                                    dnsServersField.set(staticIpConfig, dnsList);
                                    
                                    // 设置域名服务器搜索路径（可选）
                                    try {
                                        java.lang.reflect.Field domainsField = staticIpConfigClass.getDeclaredField("domains");
                                        domainsField.setAccessible(true);
                                        domainsField.set(staticIpConfig, "");
                                    } catch (Exception e) {
                                        // 忽略，该字段可能不存在
                                    }
                                    
                                    // 调用setStaticIpConfiguration方法
                                    setStaticIpConfigurationMethod.invoke(config, staticIpConfig);
                                    Log.d(TAG, "Set StaticIpConfiguration: " + staticIpConfig);
                                }
                                
                                // 保存配置
                                Method setConfigurationMethod = mEthernetManagerClass.getMethod("setConfiguration", String.class, ipConfigClass);
                                boolean setResult = (boolean) setConfigurationMethod.invoke(mEthernetManager, iface, config);
                                Log.d(TAG, "setConfiguration for " + iface + " result: " + setResult);
                                
                                if (setResult) {
                                    return true;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to update IP configuration: " + e.getMessage(), e);
                            // 尝试其他方法
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to process interface " + iface + ": " + e.getMessage());
                    // 继续尝试下一个接口
                }
            }
            
            Log.e(TAG, "Failed to set Ethernet static IP using all available methods");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Failed to set Ethernet static IP: " + e.getMessage(), e);
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
            
            // 获取可用的以太网接口列表
            List<String> interfaces = new ArrayList<>();
            try {
                // 尝试获取接口列表
                Method getInterfaceListMethod = mEthernetManagerClass.getMethod("getInterfaceList");
                Object result = getInterfaceListMethod.invoke(mEthernetManager);
                if (result instanceof List) {
                    interfaces = (List<String>) result;
                    Log.d(TAG, "Available interfaces: " + interfaces);
                } else {
                    Log.d(TAG, "getInterfaceList returned: " + result);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to get interface list: " + e.getMessage());
                // 如果获取接口列表失败，默认使用eth0
                interfaces.add("eth0");
            }
            
            // 如果没有可用接口，添加默认eth0
            if (interfaces.isEmpty()) {
                interfaces.add("eth0");
                Log.d(TAG, "Added default interface eth0");
            }
            
            // 尝试设置DHCP配置
            for (String iface : interfaces) {
                Log.d(TAG, "Processing interface: " + iface);
                
                try {
                    // 获取当前配置
                    Method getConfigurationMethod = mEthernetManagerClass.getMethod("getConfiguration", String.class);
                    Object config = getConfigurationMethod.invoke(mEthernetManager, iface);
                    Log.d(TAG, "Got configuration for " + iface + ": " + config);
                    
                    if (config != null) {
                        // 直接使用配置对象，日志显示它已经是IpConfiguration类型
                        Class<?> ipConfigClass = config.getClass();
                        Log.d(TAG, "Configuration class: " + ipConfigClass.getName());
                        
                        // 设置IP分配方式为DHCP
                        try {
                            // 动态查找setIpAssignment方法并获取正确的参数类型
                            Method setIpAssignmentMethod = null;
                            Class<?> correctParamType = null;
                            
                            // 遍历所有方法，找到setIpAssignment方法
                            for (Method method : ipConfigClass.getMethods()) {
                                if (method.getName().equals("setIpAssignment")) {
                                    Class<?>[] params = method.getParameterTypes();
                                    if (params.length == 1) {
                                        setIpAssignmentMethod = method;
                                        correctParamType = params[0];
                                        Log.d(TAG, "Found setIpAssignment method with param type: " + correctParamType.getName());
                                        break;
                                    }
                                }
                            }
                            
                            if (setIpAssignmentMethod != null && correctParamType != null) {
                                // 获取IpAssignment枚举类型和DHCP值
                                Class<?> ipAssignmentClass = correctParamType;
                                Object dhcpValue;
                                
                                try {
                                    // 尝试直接获取DHCP枚举值
                                    dhcpValue = ipAssignmentClass.getField("DHCP").get(null);
                                    Log.d(TAG, "Got DHCP value: " + dhcpValue);
                                } catch (Exception e) {
                                    // 尝试通过valueOf方法获取
                                    Method valueOfMethod = ipAssignmentClass.getMethod("valueOf", String.class);
                                    dhcpValue = valueOfMethod.invoke(null, "DHCP");
                                    Log.d(TAG, "Got DHCP value via valueOf: " + dhcpValue);
                                }
                                
                                // 调用setIpAssignment方法，使用正确的参数类型
                                setIpAssignmentMethod.invoke(config, dhcpValue);
                                Log.d(TAG, "Set IpAssignment to DHCP");
                                
                                // 保存配置
                                Method setConfigurationMethod = mEthernetManagerClass.getMethod("setConfiguration", String.class, ipConfigClass);
                                boolean setResult = (boolean) setConfigurationMethod.invoke(mEthernetManager, iface, config);
                                Log.d(TAG, "setConfiguration for " + iface + " result: " + setResult);
                                
                                if (setResult) {
                                    return true;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to update IP configuration: " + e.getMessage(), e);
                            // 尝试其他方法
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to process interface " + iface + ": " + e.getMessage());
                    // 继续尝试下一个接口
                }
            }
            
            Log.e(TAG, "Failed to set Ethernet DHCP using all available methods");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Failed to set Ethernet DHCP: " + e.getMessage(), e);
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