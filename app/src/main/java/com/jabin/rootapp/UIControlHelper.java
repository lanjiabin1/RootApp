package com.jabin.rootapp;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

/**
 * UI控制助手类，提供导航栏、状态栏的隐藏/显示以及滑动手势控制功能
 */
public class UIControlHelper {

    private static final String TAG = "UIControlHelper";
    private Activity mActivity;

    public UIControlHelper(Activity activity) {
        this.mActivity = activity;
        Log.d(TAG, "UIControlHelper initialized for API level: " + Build.VERSION.SDK_INT);
    }

    /**
     * 隐藏导航栏和状态栏
     */
    public void hideSystemBars() {
        Log.d(TAG, "hideSystemBars called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本使用WindowInsetsController
            Log.d(TAG, "Using WindowInsetsController for hiding system bars");
            WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.systemBars());
                Log.d(TAG, "System bars hidden, setting behavior to BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE");
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            } else {
                Log.e(TAG, "WindowInsetsController is null");
            }
        } else {
            // Android 11以下版本使用SYSTEM_UI_FLAG
            Log.d(TAG, "Using SYSTEM_UI_FLAG for hiding system bars");
            View decorView = mActivity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
            Log.d(TAG, "System bars hidden with immersive sticky mode");
        }
    }

    /**
     * 显示导航栏和状态栏
     */
    public void showSystemBars() {
        Log.d(TAG, "showSystemBars called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本使用WindowInsetsController
            Log.d(TAG, "Using WindowInsetsController for showing system bars");
            WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.show(WindowInsets.Type.systemBars());
                Log.d(TAG, "System bars shown");
            } else {
                Log.e(TAG, "WindowInsetsController is null");
            }
        } else {
            // Android 11以下版本使用SYSTEM_UI_FLAG
            Log.d(TAG, "Using SYSTEM_UI_FLAG for showing system bars");
            View decorView = mActivity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            decorView.setSystemUiVisibility(uiOptions);
            Log.d(TAG, "System bars shown with visible mode");
        }
    }

    /**
     * 隐藏导航栏
     */
    public void hideNavigationBar() {
        Log.d(TAG, "hideNavigationBar called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本使用WindowInsetsController
            Log.d(TAG, "Using WindowInsetsController for hiding navigation bar");
            WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.navigationBars());
                Log.d(TAG, "Navigation bar hidden");
            } else {
                Log.e(TAG, "WindowInsetsController is null");
            }
        } else {
            // Android 11以下版本使用SYSTEM_UI_FLAG
            Log.d(TAG, "Using SYSTEM_UI_FLAG for hiding navigation bar");
            View decorView = mActivity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
            Log.d(TAG, "Navigation bar hidden with immersive sticky mode");
        }
    }

    /**
     * 隐藏状态栏
     */
    public void hideStatusBar() {
        Log.d(TAG, "hideStatusBar called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本使用WindowInsetsController
            Log.d(TAG, "Using WindowInsetsController for hiding status bar");
            WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
                Log.d(TAG, "Status bar hidden");
            } else {
                Log.e(TAG, "WindowInsetsController is null");
            }
        } else {
            // Android 11以下版本使用SYSTEM_UI_FLAG
            Log.d(TAG, "Using SYSTEM_UI_FLAG for hiding status bar");
            View decorView = mActivity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
            Log.d(TAG, "Status bar hidden with immersive sticky mode");
        }
    }

    /**
     * 允许/禁止滑动呼出状态栏
     * @param allowed 是否允许
     */
    public void setStatusBarSwipeAllowed(boolean allowed) {
        Log.d(TAG, "setStatusBarSwipeAllowed called with allowed: " + allowed);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本使用WindowInsetsController
            Log.d(TAG, "Using WindowInsetsController for swipe control");
            WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
            if (insetsController != null) {
                if (allowed) {
                    Log.d(TAG, "Allowing status bar swipe: setting behavior to BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE");
                    insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    
                    // 额外尝试显示再隐藏，确保行为设置生效
                    insetsController.show(WindowInsets.Type.statusBars());
                    insetsController.hide(WindowInsets.Type.statusBars());
                    Log.d(TAG, "Re-applied hide after behavior change to ensure it takes effect");
                } else {
                    Log.d(TAG, "Disallowing status bar swipe: setting behavior to BEHAVIOR_DEFAULT");
                    // 对于Android 12+，尝试多种方法禁止滑动
                    insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_DEFAULT);
                    
                    // 尝试仅隐藏状态栏，可能有助于控制滑动
                    Log.d(TAG, "Hiding only status bar to control swipe");
                    insetsController.hide(WindowInsets.Type.statusBars());
                    
                    // 对于Android 12+，尝试设置更严格的标志
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Log.d(TAG, "Android 12+ detected, applying additional swipe restrictions");
                        // 尝试隐藏所有系统UI，包括手势栏
                        insetsController.hide(WindowInsets.Type.systemGestures());
                        Log.d(TAG, "System gestures hidden");
                    }
                }
            } else {
                Log.e(TAG, "WindowInsetsController is null");
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0-10，使用SYSTEM_UI_FLAG
            Log.d(TAG, "Using SYSTEM_UI_FLAG for swipe control on Android 8-10");
            View decorView = mActivity.getWindow().getDecorView();
            int uiOptions;
            if (allowed) {
                // 允许滑动，使用IMMERSIVE_STICKY模式
                Log.d(TAG, "Allowing swipe: using immersive sticky mode");
                uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            } else {
                // 禁止滑动，使用IMMERSIVE模式（非粘性）
                Log.d(TAG, "Disallowing swipe: using immersive non-sticky mode");
                uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE;
            }
            decorView.setSystemUiVisibility(uiOptions);
            Log.d(TAG, "System UI visibility set: " + uiOptions);
        } else {
            Log.w(TAG, "Status bar swipe control not fully supported on API level: " + Build.VERSION.SDK_INT);
        }
        
        // 记录最终状态
        Log.d(TAG, "setStatusBarSwipeAllowed completed with allowed: " + allowed);
    }

    /**
     * 设置全屏模式
     * @param fullscreen 是否全屏
     */
    public void setFullscreen(boolean fullscreen) {
        Log.d(TAG, "setFullscreen called with fullscreen: " + fullscreen);
        if (fullscreen) {
            Log.d(TAG, "Enabling fullscreen mode");
            mActivity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            Log.d(TAG, "Disabling fullscreen mode");
            mActivity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * 获取当前系统栏行为
     * @return 当前行为描述
     */
    public String getCurrentSystemBarsBehavior() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
            if (insetsController != null) {
                int behavior = insetsController.getSystemBarsBehavior();
                switch (behavior) {
                    case WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_TOUCH:
                        return "BEHAVIOR_SHOW_BARS_BY_TOUCH";
                    case WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE:
                        return "BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE";
                    case WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE:
                        return "BEHAVIOR_SHOW_BARS_BY_SWIPE";
                    default:
                        return "Unknown behavior: " + behavior;
                }
            }
        }
        return "Not supported on this API level";
    }

}