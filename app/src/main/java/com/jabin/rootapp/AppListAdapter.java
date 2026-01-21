package com.jabin.rootapp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 应用列表适配器，用于显示可卸载应用列表
 */
public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {

    private Context mContext;
    private List<AppInfo> mAppList;
    private OnAppItemClickListener mListener;
    private OnUninstallClickListener mUninstallListener;

    public interface OnAppItemClickListener {
        void onAppItemClick(AppInfo appInfo);
    }
    
    public interface OnUninstallClickListener {
        void onUninstallClick(AppInfo appInfo);
    }

    public AppListAdapter(Context context, List<AppInfo> appList, OnAppItemClickListener listener, OnUninstallClickListener uninstallListener) {
        this.mContext = context;
        this.mAppList = appList;
        this.mListener = listener;
        this.mUninstallListener = uninstallListener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_app_list, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo appInfo = mAppList.get(position);
        
        // 设置应用图标
        holder.ivAppIcon.setImageDrawable(appInfo.getAppIcon());
        
        // 设置应用名称
        holder.tvAppName.setText(appInfo.getAppName());
        
        // 设置应用包名
        holder.tvAppPackage.setText(appInfo.getPackageName());
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onAppItemClick(appInfo);
            }
        });
        
        // 设置卸载按钮点击事件
        holder.btnUninstall.setOnClickListener(v -> {
            if (mUninstallListener != null) {
                mUninstallListener.onUninstallClick(appInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAppList != null ? mAppList.size() : 0;
    }

    /**
     * 更新应用列表
     * @param appList 新的应用列表
     */
    public void updateAppList(List<AppInfo> appList) {
        this.mAppList = appList;
        notifyDataSetChanged();
    }

    /**
     * 应用列表ViewHolder
     */
    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAppIcon;
        TextView tvAppName;
        TextView tvAppPackage;
        Button btnUninstall;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAppIcon = itemView.findViewById(R.id.iv_app_icon);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
            tvAppPackage = itemView.findViewById(R.id.tv_app_package);
            btnUninstall = itemView.findViewById(R.id.btn_uninstall);
        }
    }
}