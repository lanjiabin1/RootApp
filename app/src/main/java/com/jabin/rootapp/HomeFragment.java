package com.jabin.rootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * 首页Fragment - 显示系统概览信息
 */
public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // 初始化UI组件
        TextView tvWelcome = view.findViewById(R.id.tv_welcome);
        TextView tvDescription = view.findViewById(R.id.tv_description);
        
        // 设置文本内容
        tvWelcome.setText("欢迎使用RootApp");
        tvDescription.setText("RootApp是一个功能强大的系统管理工具，提供多种系统级功能。\n\n点击底部导航栏切换到不同的功能页面。");
        
        return view;
    }
}