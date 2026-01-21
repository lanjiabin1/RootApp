package com.jabin.rootapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity - 应用主界面，集成所有系统管理功能
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int REQUEST_SELECT_APK = 2;

    // 辅助类实例
    private PowerManagerHelper mPowerManager;
    private SystemInfoHelper mSystemInfoHelper;
    private UIControlHelper mUIControlHelper;
    private NetworkManagerHelper mNetworkManager;
    private AppManagerHelper mAppManager;
    private SystemControlHelper mSystemControl;

    // Fragment实例
    private HomeFragment mHomeFragment;
    private PowerControlFragment mPowerControlFragment;
    private SystemInfoFragment mSystemInfoFragment;
    private NetworkFragment mNetworkFragment;
    private AppManagerFragment mAppManagerFragment;

    // 底部导航栏
    private BottomNavigationView mBottomNavigationView;

    // 当前显示的Fragment
    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 初始化辅助类
        initHelpers();
        
        // 初始化视图
        initViews();
        
        // 初始化Fragment
        initFragments();
        
        // 初始化底部导航栏
        initBottomNavigation();
    }

    /**
     * 初始化辅助类
     */
    private void initHelpers() {
        mPowerManager = new PowerManagerHelper(this);
        mSystemInfoHelper = new SystemInfoHelper(this);
        mUIControlHelper = new UIControlHelper(this);
        mNetworkManager = new NetworkManagerHelper(this);
        mAppManager = new AppManagerHelper(this);
        mSystemControl = new SystemControlHelper(this);
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    /**
     * 初始化Fragment
     */
    private void initFragments() {
        mHomeFragment = new HomeFragment();
        mPowerControlFragment = new PowerControlFragment();
        mSystemInfoFragment = new SystemInfoFragment();
        mNetworkFragment = new NetworkFragment();
        mAppManagerFragment = new AppManagerFragment();
        
        // 默认显示首页Fragment
        switchFragment(mHomeFragment);
    }

    /**
     * 初始化底部导航栏
     */
    private void initBottomNavigation() {
        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> {
//            switch (item.getItemId()) {
//                case R.id.nav_home:
//                    switchFragment(mHomeFragment);
//                    return true;
//                case R.id.nav_power:
//                    switchFragment(mPowerControlFragment);
//                    return true;
//                case R.id.nav_system_info:
//                    switchFragment(mSystemInfoFragment);
//                    return true;
//                case R.id.nav_network:
//                    switchFragment(mNetworkFragment);
//                    return true;
//                case R.id.nav_app_manager:
//                    switchFragment(mAppManagerFragment);
//                    return true;
//                default:
//                    return false;
//            }
            // 改为if
            if (item.getItemId() == R.id.nav_home) {
                switchFragment(mHomeFragment);
                return true;
            } else if (item.getItemId() == R.id.nav_power) {
                switchFragment(mPowerControlFragment);
                return true;
            } else if (item.getItemId() == R.id.nav_system_info) {
                switchFragment(mSystemInfoFragment);
                return true;
            } else if (item.getItemId() == R.id.nav_network) {
                switchFragment(mNetworkFragment);
                return true;
            } else if (item.getItemId() == R.id.nav_app_manager) {
                switchFragment(mAppManagerFragment);
                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * 切换Fragment
     * @param fragment 要显示的Fragment
     */
    private void switchFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        if (mCurrentFragment != null) {
            transaction.hide(mCurrentFragment);
        }
        
        if (!fragment.isAdded()) {
            transaction.add(R.id.fragment_container, fragment);
        } else {
            transaction.show(fragment);
        }
        
        transaction.commit();
        mCurrentFragment = fragment;
    }

    // 电源控制功能实现
    public void shutdown() {
        showConfirmDialog("确认关机", "确定要关闭设备吗？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean result = mPowerManager.shutdown();
                if (result) {
                    Toast.makeText(MainActivity.this, "关机指令已发送", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "关机失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void reboot() {
        showConfirmDialog("确认重启", "确定要重启设备吗？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean result = mPowerManager.reboot();
                if (result) {
                    Toast.makeText(MainActivity.this, "重启指令已发送", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "重启失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void factoryReset() {
        showConfirmDialog("确认恢复出厂设置", "确定要恢复出厂设置吗？此操作将清除所有数据，不可恢复！", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean result = mPowerManager.factoryReset();
                if (result) {
                    Toast.makeText(MainActivity.this, "恢复出厂设置指令已发送", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "恢复出厂设置失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 系统信息功能实现
    public void getSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("设备SN号: ").append(mSystemInfoHelper.getSN()).append("\n");
        info.append("以太网MAC地址: ").append(mSystemInfoHelper.getEthernetMacAddress()).append("\n");
        info.append("WLAN MAC地址: ").append(mSystemInfoHelper.getWlanMacAddress()).append("\n");
        info.append("以太网IP地址: ").append(mSystemInfoHelper.getEthernetIpAddress()).append("\n");
        info.append("WLAN IP地址: ").append(mSystemInfoHelper.getWlanIpAddress()).append("\n");
        
        mSystemInfoFragment.updateSystemInfo(info.toString());
        Toast.makeText(this, "系统信息已获取", Toast.LENGTH_SHORT).show();
    }

    // 网络管理功能实现
    public void showWifiList() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        
        // 确保WiFi已开启
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "请先开启WiFi", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 开始扫描WiFi
        wifiManager.startScan();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        
        if (scanResults.isEmpty()) {
            Toast.makeText(this, "未扫描到WiFi网络", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建WiFi列表对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("可用WiFi网络");
        
        // 提取WiFi名称列表
        final List<String> wifiNames = new ArrayList<>();
        for (ScanResult result : scanResults) {
            wifiNames.add(result.SSID);
        }
        
        // 创建适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, wifiNames);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedWifi = wifiNames.get(which);
                showWifiConnectDialog(selectedWifi);
            }
        });
        
        builder.show();
    }

    /**
     * 显示WiFi连接对话框
     * @param ssid WiFi名称
     */
    private void showWifiConnectDialog(final String ssid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("连接WiFi: " + ssid);
        
        // 创建自定义布局
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_wifi_connect, null);
        final EditText etPassword = view.findViewById(R.id.et_password);
        builder.setView(view);
        
        builder.setPositiveButton("连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = etPassword.getText().toString();
                boolean result = mNetworkManager.connectToWifi(ssid, password);
                if (result) {
                    Toast.makeText(MainActivity.this, "正在连接WiFi: " + ssid, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "连接WiFi失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    // 应用管理功能实现
    public void selectApkFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.android.package-archive");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "选择APK文件"), REQUEST_SELECT_APK);
    }

    public void showUninstallableApps() {
        List<PackageInfo> packageInfos = getPackageManager().getInstalledPackages(0);
        List<String> uninstallableApps = new ArrayList<>();
        
        for (PackageInfo info : packageInfos) {
            // 过滤系统应用，只显示可卸载的第三方应用
            if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                uninstallableApps.add(info.applicationInfo.loadLabel(getPackageManager()) + " (" + info.packageName + ")");
            }
        }
        
        if (uninstallableApps.isEmpty()) {
            Toast.makeText(this, "没有可卸载的应用", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showAppListDialog("可卸载应用列表", uninstallableApps, true);
    }

    public void showAllInstalledApps() {
        List<PackageInfo> packageInfos = getPackageManager().getInstalledPackages(0);
        List<String> allApps = new ArrayList<>();
        
        for (PackageInfo info : packageInfos) {
            String appName = info.applicationInfo.loadLabel(getPackageManager()).toString();
            String packageName = info.packageName;
            String appType = (info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 ? "系统应用" : "第三方应用";
            allApps.add(appName + " (" + packageName + ") - " + appType);
        }
        
        if (allApps.isEmpty()) {
            Toast.makeText(this, "没有已安装的应用", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showAppListDialog("所有已安装应用", allApps, false);
    }

    /**
     * 显示应用列表对话框
     * @param title 对话框标题
     * @param appList 应用列表
     * @param canUninstall 是否可以卸载
     */
    private void showAppListDialog(String title, final List<String> appList, final boolean canUninstall) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appList);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (canUninstall) {
                    String selectedApp = appList.get(which);
                    // 提取包名
                    String packageName = selectedApp.substring(selectedApp.lastIndexOf("(") + 1, selectedApp.lastIndexOf(")"));
                    uninstallApp(packageName);
                }
            }
        });
        
        builder.show();
    }

    /**
     * 卸载应用
     * @param packageName 应用包名
     */
    private void uninstallApp(final String packageName) {
        showConfirmDialog("确认卸载", "确定要卸载应用吗？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean result = mAppManager.silentUninstall(packageName);
                if (result) {
                    Toast.makeText(MainActivity.this, "应用卸载成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "应用卸载失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 系统控制功能实现
    public void takeScreenshot() {
        // 请求媒体投影权限
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
    }

    public void setSystemTime() {
        // 设置当前时间为2024-01-21 12:00:00
        boolean result = mSystemControl.setSystemTime(2024, 1, 21, 12, 0, 0);
        if (result) {
            Toast.makeText(this, "系统时间已设置", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "系统时间设置失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示确认对话框
     * @param title 对话框标题
     * @param message 对话框消息
     * @param positiveListener 确认按钮点击监听器
     */
    private void showConfirmDialog(String title, String message, DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("确定", positiveListener);
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode == RESULT_OK) {
            // 获取MediaProjection并进行截图
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            android.media.projection.MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            mSystemControl.takeScreenshot(mediaProjection, new SystemControlHelper.ScreenshotCallback() {
                @Override
                public void onScreenshotSuccess(String screenshotPath) {
                    Toast.makeText(MainActivity.this, "截图成功，保存路径：" + screenshotPath, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onScreenshotFailed(Exception e) {
                    Toast.makeText(MainActivity.this, "截图失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Screenshot failed: " + e.getMessage());
                }
            });
        } else if (requestCode == REQUEST_SELECT_APK && resultCode == RESULT_OK) {
            // 处理APK文件选择结果
            if (data != null && data.getData() != null) {
                String apkPath = data.getData().getPath();
                boolean result = mAppManager.silentInstall(apkPath);
                if (result) {
                    Toast.makeText(this, "应用安装成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "应用安装失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}