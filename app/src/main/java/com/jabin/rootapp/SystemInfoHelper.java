package com.jabin.rootapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * 系统信息助手类，提供设备SN号、MAC地址、IP地址等信息的获取功能
 */
public class SystemInfoHelper {

    private static final String TAG = "SystemInfoHelper";
    private Context mContext;
    private ConnectivityManager mConnectivityManager;
    private WifiManager mWifiManager;

    public SystemInfoHelper(Context context) {
        this.mContext = context;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 获取设备SN号
     * @return 设备SN号
     */
    public String getSN() {
        try {
            // 通过反射获取Build类的SERIAL字段
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Build.getSerial();
            } else {
                Method getSerialMethod = Build.class.getMethod("getSerial");
                return (String) getSerialMethod.invoke(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    /**
     * 获取以太网MAC地址
     * @return 以太网MAC地址
     */
    public String getEthernetMacAddress() {
        try {
            // 遍历所有网络接口，查找以太网接口
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface intf : Collections.list(interfaces)) {
                if (intf.getName().equals("eth0") || intf.getName().contains("eth")) {
                    byte[] mac = intf.getHardwareAddress();
                    if (mac != null) {
                        StringBuilder buf = new StringBuilder();
                        for (byte aMac : mac) {
                            buf.append(String.format("%02X:", aMac));
                        }
                        if (buf.length() > 0) {
                            buf.deleteCharAt(buf.length() - 1);
                        }
                        return buf.toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    /**
     * 获取WLAN MAC地址
     * @return WLAN MAC地址
     */
    public String getWlanMacAddress() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10及以上版本需要使用NetworkInterface获取
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                for (NetworkInterface intf : Collections.list(interfaces)) {
                    if (intf.getName().equals("wlan0")) {
                        byte[] mac = intf.getHardwareAddress();
                        if (mac != null) {
                            StringBuilder buf = new StringBuilder();
                            for (byte aMac : mac) {
                                buf.append(String.format("%02X:", aMac));
                            }
                            if (buf.length() > 0) {
                                buf.deleteCharAt(buf.length() - 1);
                            }
                            return buf.toString();
                        }
                    }
                }
            } else {
                // Android 10以下版本可以直接从WifiInfo获取
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                return wifiInfo.getMacAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    /**
     * 获取以太网IP地址
     * @return 以太网IP地址
     */
    public String getEthernetIpAddress() {
        try {
            // 遍历所有网络接口，查找以太网接口
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface intf : Collections.list(interfaces)) {
                if (intf.getName().equals("eth0") || intf.getName().contains("eth")) {
                    Enumeration<InetAddress> addresses = intf.getInetAddresses();
                    for (InetAddress addr : Collections.list(addresses)) {
                        if (!addr.isLoopbackAddress() && addr.getAddress().length == 4) {
                            return addr.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    /**
     * 获取WLAN IP地址
     * @return WLAN IP地址
     */
    public String getWlanIpAddress() {
        try {
            if (mWifiManager.isWifiEnabled()) {
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                return String.format("%d.%d.%d.%d",
                        (ipAddress & 0xff),
                        (ipAddress >> 8 & 0xff),
                        (ipAddress >> 16 & 0xff),
                        (ipAddress >> 24 & 0xff));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

}