package com.jabin.rootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

/**
 * 状态栏控制Fragment - 提供导航栏、状态栏的隐藏/显示以及滑动手势控制功能
 */
public class StatusBarControlFragment extends Fragment {

    private UIControlHelper mUIControlHelper;
    private Button btnHideSystemBars;
    private Button btnShowSystemBars;
    private Switch swStatusBarSwipe;

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
        btnHideSystemBars = view.findViewById(R.id.btn_hide_system_bars);
        btnShowSystemBars = view.findViewById(R.id.btn_show_system_bars);
        swStatusBarSwipe = view.findViewById(R.id.sw_status_bar_swipe);
        
        // 设置点击事件
        btnHideSystemBars.setOnClickListener(v -> hideSystemBars());
        btnShowSystemBars.setOnClickListener(v -> showSystemBars());
        swStatusBarSwipe.setOnCheckedChangeListener((buttonView, isChecked) -> setStatusBarSwipeAllowed(isChecked));
        
        return view;
    }
    
    /**
     * 隐藏导航栏和状态栏
     */
    private void hideSystemBars() {
        mUIControlHelper.hideSystemBars();
        Toast.makeText(getActivity(), "已隐藏导航栏和状态栏", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 显示导航栏和状态栏
     */
    private void showSystemBars() {
        mUIControlHelper.showSystemBars();
        Toast.makeText(getActivity(), "已显示导航栏和状态栏", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 设置是否允许滑动呼出状态栏
     * @param allowed 是否允许
     */
    private void setStatusBarSwipeAllowed(boolean allowed) {
        mUIControlHelper.setStatusBarSwipeAllowed(allowed);
        String behavior = mUIControlHelper.getCurrentSystemBarsBehavior();
        Toast.makeText(getActivity(), "状态栏滑动已" + (allowed ? "允许" : "禁止") + ", 当前行为: " + behavior, Toast.LENGTH_SHORT).show();
    }
}