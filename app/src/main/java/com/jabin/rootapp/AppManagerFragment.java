package com.jabin.rootapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 应用管理Fragment - 提供静默安装和卸载应用功能
 */
public class AppManagerFragment extends Fragment {

    private static final String TAG = "AppManagerFragment";
    private Button btnSilentInstall;
    private Button btnRefreshApps;
    private RecyclerView rvAppList;
    private AppListAdapter mAppListAdapter;
    private List<AppInfo> mAppList;
    private PackageManager mPackageManager;

    public AppManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_app_manager, container, false);
        
        // 初始化PackageManager
        mPackageManager = getActivity().getPackageManager();
        
        // 初始化UI组件
        initViews(view);
        
        // 初始化应用列表
        initAppList();
        
        // 设置点击事件
        setListeners();
        
        return view;
    }
    
    /**
     * 初始化UI组件
     */
    private void initViews(View view) {
        btnSilentInstall = view.findViewById(R.id.btn_silent_install);
        btnRefreshApps = view.findViewById(R.id.btn_refresh_apps);
        rvAppList = view.findViewById(R.id.rv_app_list);
        
        // 配置RecyclerView
        rvAppList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAppList = new ArrayList<>();
        mAppListAdapter = new AppListAdapter(getActivity(), mAppList, new AppListAdapter.OnAppItemClickListener() {
            @Override
            public void onAppItemClick(AppInfo appInfo) {
                showAppDetails(appInfo);
            }
        }, new AppListAdapter.OnUninstallClickListener() {
            @Override
            public void onUninstallClick(AppInfo appInfo) {
                silentUninstallApp(appInfo);
            }
        });
        rvAppList.setAdapter(mAppListAdapter);
    }
    
    /**
     * 初始化应用列表
     */
    private void initAppList() {
        loadUninstallableApps();
    }
    
    /**
     * 设置点击事件
     */
    private void setListeners() {
        // 静默安装应用
        btnSilentInstall.setOnClickListener(v -> ((MainActivity) getActivity()).selectApkFile());
        
        // 刷新应用列表
        btnRefreshApps.setOnClickListener(v -> loadUninstallableApps());
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 进入fragment时自动刷新应用列表
        loadUninstallableApps();
    }
    
    /**
     * 加载可卸载的应用列表
     */
    private void loadUninstallableApps() {
        mAppList.clear();
        
        try {
            // 获取所有已安装的应用
            List<PackageInfo> packageInfos = mPackageManager.getInstalledPackages(0);
            
            for (PackageInfo packageInfo : packageInfos) {
                // 过滤系统应用，只显示可卸载的第三方应用
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    AppInfo appInfo = new AppInfo();
                    appInfo.setAppName(packageInfo.applicationInfo.loadLabel(mPackageManager).toString());
                    appInfo.setPackageName(packageInfo.packageName);
                    appInfo.setAppIcon(packageInfo.applicationInfo.loadIcon(mPackageManager));
                    appInfo.setApplicationInfo(packageInfo.applicationInfo);
                    appInfo.setVersionName(packageInfo.versionName);
                    appInfo.setVersionCode(packageInfo.versionCode);
                    appInfo.setInstallTime(packageInfo.firstInstallTime);
                    appInfo.setUpdateTime(packageInfo.lastUpdateTime);
                    
                    mAppList.add(appInfo);
                }
            }
            
            // 更新应用列表
            mAppListAdapter.notifyDataSetChanged();
            
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load installed apps: " + e.getMessage());
            Toast.makeText(getActivity(), "加载应用列表失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 静默卸载应用
     * @param appInfo 应用信息
     */
    private void silentUninstallApp(AppInfo appInfo) {
        try {
            AppManagerHelper appManagerHelper = new AppManagerHelper(getActivity());
            boolean result = appManagerHelper.silentUninstall(appInfo.getPackageName());
            if (result) {
                Toast.makeText(getActivity(), "卸载成功", Toast.LENGTH_SHORT).show();
                // 刷新应用列表
                loadUninstallableApps();
            } else {
                Toast.makeText(getActivity(), "卸载失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to uninstall app: " + e.getMessage());
            Toast.makeText(getActivity(), "卸载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 显示应用详情
     * @param appInfo 应用信息
     */
    private void showAppDetails(AppInfo appInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(appInfo.getAppName());
        
        // 创建自定义布局
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_app_details, null);
        
        // 初始化详情UI组件
        ImageView ivAppIcon = view.findViewById(R.id.iv_app_icon);
        TextView tvAppName = view.findViewById(R.id.tv_app_name);
        TextView tvAppPackage = view.findViewById(R.id.tv_app_package);
        TextView tvAppVersion = view.findViewById(R.id.tv_app_version);
        TextView tvAppInstallTime = view.findViewById(R.id.tv_app_install_time);
        TextView tvAppUpdateTime = view.findViewById(R.id.tv_app_update_time);
        Button btnUninstall = view.findViewById(R.id.btn_uninstall);
        
        // 设置应用信息
        ivAppIcon.setImageDrawable(appInfo.getAppIcon());
        tvAppName.setText(appInfo.getAppName());
        tvAppPackage.setText("包名: " + appInfo.getPackageName());
        tvAppVersion.setText("版本: " + appInfo.getVersionName() + " (" + appInfo.getVersionCode() + ")");
        tvAppInstallTime.setText("安装时间: " + formatDate(appInfo.getInstallTime()));
        tvAppUpdateTime.setText("更新时间: " + formatDate(appInfo.getUpdateTime()));
        
        // 设置卸载按钮点击事件
        btnUninstall.setOnClickListener(v -> {
            silentUninstallApp(appInfo);
        });
        
        builder.setView(view);
        builder.setNegativeButton("关闭", null);
        builder.show();
    }
    
    /**
     * 格式化日期
     * @param timeInMillis 时间戳
     * @return 格式化后的日期字符串
     */
    private String formatDate(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timeInMillis));
    }
}