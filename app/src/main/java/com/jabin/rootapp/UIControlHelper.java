package com.jabin.rootapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.List;

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
     * 允许/禁止滑动呼出状态栏和退出应用
     * @param allowed 是否允许
     */
    public void setStatusBarSwipeAllowed(boolean allowed) {
        Log.d(TAG, "setStatusBarSwipeAllowed called with allowed: " + allowed);
        
        // 1. 控制窗口标志
        controlWindowFlags(allowed);
        
        // 2. 控制系统UI可见性和手势
        controlSystemUiVisibility(allowed);
        
        // 3. 对于Android 11+，使用WindowInsetsController控制
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            controlWindowInsetsController(allowed);
        }
        
        // 4. 控制任务锁定
        controlTaskLocking(allowed);
        
        // 5. 控制触摸事件处理
        controlTouchEvents(allowed);
        
        // 记录最终状态
        Log.d(TAG, "setStatusBarSwipeAllowed completed with allowed: " + allowed);
    }
    
    /**
     * 控制窗口标志
     */
    private void controlWindowFlags(boolean allowed) {
        Log.d(TAG, "controlWindowFlags called with allowed: " + allowed);
        WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
        
        if (allowed) {
            // 允许滑动和退出时，清除所有限制标志
            Log.d(TAG, "Allowing interaction: clearing window flags");
            params.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            params.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
            params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            params.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            // 禁止滑动和退出时，设置严格的窗口标志
            Log.d(TAG, "Disallowing interaction: setting restrictive window flags");
            params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            params.flags |= WindowManager.LayoutParams.FLAG_SECURE; // 防止截屏，也可能有助于锁定UI
            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN; // 全屏模式
        }
        
        mActivity.getWindow().setAttributes(params);
        Log.d(TAG, "Window flags updated: " + params.flags);
    }
    
    /**
     * 控制系统UI可见性和手势
     */
    private void controlSystemUiVisibility(boolean allowed) {
        Log.d(TAG, "controlSystemUiVisibility called with allowed: " + allowed);
        View decorView = mActivity.getWindow().getDecorView();
        
        int uiOptions = 0;
        
        if (allowed) {
            // 允许滑动和退出时，使用正常的UI可见性
            Log.d(TAG, "Allowing interaction: using normal UI visibility");
            uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        } else {
            // 禁止滑动和退出时，设置严格的沉浸式模式
            Log.d(TAG, "Disallowing interaction: setting strict immersive mode");
            uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            
            // 对于Android 11+，尝试添加更多标志
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Log.d(TAG, "Android 11+: using immersive sticky flags");
            }
        }
        
        decorView.setSystemUiVisibility(uiOptions);
        Log.d(TAG, "System UI visibility set: " + uiOptions);
    }
    
    /**
     * 控制WindowInsetsController（Android 11+）
     */
    private void controlWindowInsetsController(boolean allowed) {
        Log.d(TAG, "controlWindowInsetsController called with allowed: " + allowed);
        WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
        
        if (insetsController != null) {
            if (allowed) {
                // 允许滑动和退出时的设置
                Log.d(TAG, "Allowing interaction: setting normal WindowInsets behavior");
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                insetsController.show(WindowInsets.Type.systemBars() | WindowInsets.Type.systemGestures());
            } else {
                // 禁止滑动和退出时的设置
                Log.d(TAG, "Disallowing interaction: setting restrictive WindowInsets behavior");
                
                // 隐藏所有系统UI元素
                insetsController.hide(WindowInsets.Type.systemBars() | WindowInsets.Type.systemGestures() | WindowInsets.Type.navigationBars() | WindowInsets.Type.statusBars());
                
                // 设置行为为默认，可能会禁用滑动
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_DEFAULT);
                
                // 对于Android 12+，尝试更多控制
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Log.d(TAG, "Android 12+: applying additional restrictions");
                    
                    // 尝试禁用系统手势导航
                    try {
                        // 使用反射调用setSystemGestureExclusionRects方法，禁止所有手势区域
                        View decorView = mActivity.getWindow().getDecorView();
                        Method setSystemGestureExclusionRectsMethod = View.class.getMethod("setSystemGestureExclusionRects", java.util.List.class);
                        java.util.List<android.graphics.Rect> rects = new java.util.ArrayList<>();
                        // 添加整个屏幕作为手势排除区域
                        rects.add(new android.graphics.Rect(0, 0, decorView.getWidth(), decorView.getHeight()));
                        setSystemGestureExclusionRectsMethod.invoke(decorView, rects);
                        Log.d(TAG, "Set system gesture exclusion rects to cover entire screen");
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to set system gesture exclusion rects: " + e.getMessage(), e);
                    }
                    
                    // 尝试使用更严格的隐藏方式
                    try {
                        // 反射调用hide方法，传入所有可能的类型
                        int allTypes = WindowInsets.Type.systemBars() | WindowInsets.Type.systemGestures() | WindowInsets.Type.tappableElement() | WindowInsets.Type.ime();
                        insetsController.hide(allTypes);
                        Log.d(TAG, "Hid all window insets types: " + allTypes);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to hide all window insets types: " + e.getMessage(), e);
                    }
                    
                    // 尝试禁用IME显示
                    insetsController.hide(WindowInsets.Type.ime());
                    Log.d(TAG, "Hid IME to prevent keyboard-related gestures");
                }
            }
        } else {
            Log.e(TAG, "WindowInsetsController is null");
        }
    }
    
    /**
     * 控制任务锁定
     */
    private void controlTaskLocking(boolean allowed) {
        Log.d(TAG, "controlTaskLocking called with allowed: " + allowed);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                ActivityManager am = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    if (!allowed) {
                        // 锁定任务，防止退出
                        Log.d(TAG, "Locking task to prevent exit");
                        mActivity.startLockTask();
                        
                        // 禁用最近任务列表显示
                        Log.d(TAG, "Disabling recent tasks list");
                        List<ActivityManager.AppTask> tasks = am.getAppTasks();
                        if (tasks != null && !tasks.isEmpty()) {
                            // 尝试设置排除从最近任务列表
                            try {
                                Method setExcludeFromRecentsMethod = ActivityManager.AppTask.class.getMethod("setExcludeFromRecents", boolean.class);
                                setExcludeFromRecentsMethod.invoke(tasks.get(0), true);
                                Log.d(TAG, "Set app exclude from recents");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to set exclude from recents: " + e.getMessage(), e);
                            }
                        }
                    } else {
                        // 解锁任务
                        Log.d(TAG, "Unlocking task to allow exit");
                        // 检查是否在锁定任务模式下
                        boolean isLocked = false;
                        try {
                            // 尝试使用ActivityManager检查锁定状态
                            Method isInLockTaskModeMethod = ActivityManager.class.getMethod("isInLockTaskMode");
                            isLocked = (boolean) isInLockTaskModeMethod.invoke(am);
                        } catch (Exception e) {
                            // 尝试使用Activity的isInLockTaskMode方法（兼容旧版本）
                            try {
                                Method isInLockTaskModeMethod = Activity.class.getMethod("isInLockTaskMode");
                                isLocked = (boolean) isInLockTaskModeMethod.invoke(mActivity);
                            } catch (Exception e2) {
                                Log.e(TAG, "Failed to check lock task mode: " + e2.getMessage(), e2);
                                // 无法检查时，尝试直接解锁
                                isLocked = true;
                            }
                        }
                        
                        if (isLocked) {
                            try {
                                mActivity.stopLockTask();
                                Log.d(TAG, "Stopped lock task mode");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to stop lock task mode: " + e.getMessage(), e);
                            }
                        }
                        
                        // 允许最近任务列表显示
                        Log.d(TAG, "Allowing recent tasks list");
                        List<ActivityManager.AppTask> tasks = am.getAppTasks();
                        if (tasks != null && !tasks.isEmpty()) {
                            try {
                                Method setExcludeFromRecentsMethod = ActivityManager.AppTask.class.getMethod("setExcludeFromRecents", boolean.class);
                                setExcludeFromRecentsMethod.invoke(tasks.get(0), false);
                                Log.d(TAG, "Set app include in recents");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to set include in recents: " + e.getMessage(), e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to control task locking: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 控制触摸事件处理
     */
    private void controlTouchEvents(boolean allowed) {
        Log.d(TAG, "controlTouchEvents called with allowed: " + allowed);
        
        if (!allowed) {
            // 对于Android 14+，尝试使用更严格的方法
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                Log.d(TAG, "Android 14+: applying strict touch event restrictions");
                
                // 添加不可触摸标志，防止任务栏划出
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                
                // 短暂延迟后移除该标志，只禁止初始滑动
                mActivity.getWindow().getDecorView().postDelayed(() -> {
                    Log.d(TAG, "Removing FLAG_NOT_TOUCHABLE after delay");
                    mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }, 100);
            }
        }
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
     * 禁止应用退出（通过各种方式）
     * @param disable 是否禁止退出
     */
    public void disableAppExit(boolean disable) {
        Log.d(TAG, "disableAppExit called with disable: " + disable);
        
        if (disable) {
            // 禁止退出时，设置各种标志和监听
            Log.d(TAG, "Disabling app exit: setting up exit prevention");
            
            // 1. 设置窗口始终在最前
            try {
                WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
                params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                params.flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
                params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
                params.flags |= WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
                mActivity.getWindow().setAttributes(params);
                Log.d(TAG, "Set window flags for keeping app on top");
            } catch (Exception e) {
                Log.e(TAG, "Failed to set window flags: " + e.getMessage(), e);
            }
            
            // 2. 尝试使用反射禁用返回按钮和任务管理器
            try {
                // 使用反射获取ActivityManager
                Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
                Object am = mActivity.getSystemService("activity");
                if (am != null) {
                    // 尝试禁用任务管理器
                    Method setUserRestrictionsMethod = activityManagerClass.getMethod("setUserRestrictions", java.util.Set.class);
                    java.util.Set<String> restrictions = new java.util.HashSet<>();
                    restrictions.add("no_close_application");
                    restrictions.add("no_task_manager");
                    restrictions.add("no_recents");
                    restrictions.add("no_screenshot");
                    setUserRestrictionsMethod.invoke(am, restrictions);
                    Log.d(TAG, "Set user restrictions to disable exit and task manager");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to set user restrictions: " + e.getMessage(), e);
            }
            
            // 3. 尝试禁用系统导航按钮
            try {
                // 使用反射禁用系统导航按钮
                Class<?> navigationBarManagerClass = Class.forName("android.view.NavigationBarManager");
                Object navigationBarManager = mActivity.getSystemService("navigation_bar");
                if (navigationBarManager != null) {
                    Method setNavigationBarHiddenMethod = navigationBarManagerClass.getMethod("setNavigationBarHidden", boolean.class);
                    setNavigationBarHiddenMethod.invoke(navigationBarManager, true);
                    Log.d(TAG, "Set navigation bar hidden");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to hide navigation bar via manager: " + e.getMessage(), e);
            }
        } else {
            // 允许退出时，清除所有限制
            Log.d(TAG, "Allowing app exit: clearing exit prevention");
            
            // 清除窗口标志
            try {
                WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
                params.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                params.flags &= ~WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
                params.flags &= ~WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
                params.flags &= ~WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
                mActivity.getWindow().setAttributes(params);
                Log.d(TAG, "Cleared window flags for exit prevention");
            } catch (Exception e) {
                Log.e(TAG, "Failed to clear window flags: " + e.getMessage(), e);
            }
            
            // 清除用户限制
            try {
                Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
                Object am = mActivity.getSystemService("activity");
                if (am != null) {
                    Method setUserRestrictionsMethod = activityManagerClass.getMethod("setUserRestrictions", java.util.Set.class);
                    setUserRestrictionsMethod.invoke(am, new java.util.HashSet<String>());
                    Log.d(TAG, "Cleared user restrictions");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to clear user restrictions: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 增强版禁止滑动呼出状态栏和退出应用
     * @param allowed 是否允许
     */
    public void setEnhancedStatusBarSwipeAllowed(boolean allowed) {
        Log.d(TAG, "setEnhancedStatusBarSwipeAllowed called with allowed: " + allowed);
        
        // 1. 调用基础方法
        setStatusBarSwipeAllowed(allowed);
        
        // 2. 调用禁止退出应用的方法
        disableAppExit(!allowed);
        
        // 3. 对于Android 12+，尝试使用系统API禁止退出
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                // 使用反射调用setAppExitAllowed方法
                Class<?> activityClass = Class.forName("android.app.Activity");
                Method setAppExitAllowedMethod = activityClass.getMethod("setAppExitAllowed", boolean.class);
                setAppExitAllowedMethod.invoke(mActivity, allowed);
                Log.d(TAG, "Set app exit allowed: " + allowed);
            } catch (Exception e) {
                Log.e(TAG, "Failed to set app exit allowed: " + e.getMessage(), e);
            }
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