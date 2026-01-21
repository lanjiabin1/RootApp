package com.jabin.rootapp;

import android.app.Activity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

/**
 * UI控制助手类，提供导航栏、状态栏的隐藏/显示以及滑动手势控制功能
 */
public class UIControlHelper {

    private Activity mActivity;

    public UIControlHelper(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * 隐藏导航栏和状态栏
     */
    public void hideSystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11及以上版本使用WindowInsetsController
            WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.systemBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // Android 11以下版本使用SYSTEM_UI_FLAG
            View decorView = mActivity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * 显示导航栏和状态栏
     */
    public void showSystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11及以上版本使用WindowInsetsController
            WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.show(WindowInsets.Type.systemBars());
            }
        } else {
            // Android 11以下版本使用SYSTEM_UI_FLAG
            View decorView = mActivity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * 隐藏导航栏
     */
    public void hideNavigationBar() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11及以上版本使用WindowInsetsController
            WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.navigationBars());
            }
        } else {
            // Android 11以下版本使用SYSTEM_UI_FLAG
            View decorView = mActivity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * 隐藏状态栏
     */
    public void hideStatusBar() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11及以上版本使用WindowInsetsController
            WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
            }
        } else {
            // Android 11以下版本使用SYSTEM_UI_FLAG
            View decorView = mActivity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * 允许/禁止滑动呼出状态栏
     * @param allowed 是否允许
     */
    public void setStatusBarSwipeAllowed(boolean allowed) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11及以上版本使用WindowInsetsController
            WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
            if (insetsController != null) {
                if (allowed) {
                    insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                } else {
                    insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_DEFAULT);
                }
            }
        }
    }

    /**
     * 设置全屏模式
     * @param fullscreen 是否全屏
     */
    public void setFullscreen(boolean fullscreen) {
        if (fullscreen) {
            mActivity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            mActivity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

}