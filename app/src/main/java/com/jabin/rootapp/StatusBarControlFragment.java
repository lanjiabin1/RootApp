package com.jabin.rootapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

/**
 * 状态栏控制Fragment - 提供导航栏、状态栏的隐藏/显示以及滑动手势控制功能
 */
public class StatusBarControlFragment extends Fragment {

    private UIControlHelper mUIControlHelper;
    private Switch swSystemNavigation;
    private TextView tvStatusBarControl;

    public StatusBarControlFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_status_bar_control, container, false);
        
        // 初始化UI控制助手
        mUIControlHelper = new UIControlHelper(getActivity());
        
        // 初始化UI组件
        swSystemNavigation = view.findViewById(R.id.sw_system_navigation);
        tvStatusBarControl = view.findViewById(R.id.tv_status_bar_control);
        
        // 设置初始状态：默认禁止系统导航栏显示
        boolean isAllowed = false;
        swSystemNavigation.setChecked(isAllowed);
        updateSystemNavigationStatus(isAllowed);
        
        // 设置点击事件
        swSystemNavigation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setSystemNavigationAllowed(isChecked);
            updateSystemNavigationStatus(isChecked);
        });
        
        return view;
    }
    
    /**
     * 设置是否允许系统导航栏显示
     * @param allowed 是否允许
     */
    private void setSystemNavigationAllowed(boolean allowed) {
        Log.d("StatusBarControlFragment", "设置系统导航栏显示状态: " + (allowed ? "允许" : "禁止"));
        mUIControlHelper.setEnhancedStatusBarSwipeAllowed(allowed);
        Toast.makeText(getActivity(), "系统导航栏显示已" + (allowed ? "允许" : "禁止"), Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 更新系统导航栏显示状态文本
     * @param isAllowed 是否允许
     */
    private void updateSystemNavigationStatus(boolean isAllowed) {
        if (isAllowed) {
            tvStatusBarControl.setText("当前状态：允许系统导航栏显示，可通过顶部下滑呼出");
        } else {
            tvStatusBarControl.setText("当前状态：系统导航栏已隐藏，无法呼出");
        }
    }

}