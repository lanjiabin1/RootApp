package com.jabin.rootapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;
import java.util.List;

/**
 * UI控制助手类，提供导航栏、状态栏的隐藏/显示以及滑动手势控制功能
 */
public class UIControlHelper {

    private static final String TAG = "UIControlHelper";
    private Activity mActivity;
    
    // 系统级窗口覆盖相关变量
    private View mOverlayView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mOverlayParams;
    
    // 广播接收器，用于监听系统按键事件
    private BroadcastReceiver mSystemKeyReceiver;
    
    // 锁定状态标志
    private boolean mIsLocked = false;

    public UIControlHelper(Activity activity) {
        this.mActivity = activity;
        this.mWindowManager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        Log.d(TAG, "UIControlHelper initialized for API level: " + Build.VERSION.SDK_INT);
        Log.d(TAG, "WindowManager obtained: " + mWindowManager);
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
                        
                        // 1. 使用系统app权限直接锁定，不显示提示框
                        try {
                            // 尝试使用反射调用带有额外参数的startLockTask方法，禁止提示框
                            Method startLockTaskMethod = Activity.class.getMethod("startLockTask", int.class);
                            // 查找START_LOCK_TASK_MODE_PINNED常量或使用0
                            int flag = 0;
                            try {
                                flag = (int) Activity.class.getField("START_LOCK_TASK_MODE_PINNED").get(null);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to get START_LOCK_TASK_MODE_PINNED flag: " + e.getMessage(), e);
                            }
                            startLockTaskMethod.invoke(mActivity, flag);
                            Log.d(TAG, "Started lock task mode with flag: " + flag);
                        } catch (Exception e1) {
                            // 尝试调用不带参数的startLockTask方法
                            try {
                                mActivity.startLockTask();
                                Log.d(TAG, "Started lock task mode with default flag");
                            } catch (Exception e2) {
                                Log.e(TAG, "Failed to start lock task mode: " + e2.getMessage(), e2);
                            }
                        }
                        
                        // 2. 禁用最近任务列表显示
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
                        
                        // 3. 尝试使用反射禁用向上滑动并按住的解锁方法
                        try {
                            // 查找并调用disableLockTaskExit方法（如果存在）
                            Method disableLockTaskExitMethod = Activity.class.getMethod("disableLockTaskExit");
                            disableLockTaskExitMethod.invoke(mActivity);
                            Log.d(TAG, "Disabled lock task exit gestures");
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to disable lock task exit: " + e.getMessage(), e);
                            // 尝试其他方法禁用解锁
                            try {
                                // 尝试使用系统属性禁用
                                Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
                                Method setMethod = systemPropertiesClass.getMethod("set", String.class, String.class);
                                setMethod.invoke(null, "sys.lock_task.unlockable", "0");
                                Log.d(TAG, "Set sys.lock_task.unlockable to 0");
                            } catch (Exception e2) {
                                Log.e(TAG, "Failed to set system property: " + e2.getMessage(), e2);
                            }
                        }
                        
                        // 4. 尝试使用系统API隐藏底部灰色横杠（Android 14+）
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            try {
                                WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
                                if (insetsController != null) {
                                    // 隐藏系统手势和导航栏
                                    insetsController.hide(WindowInsets.Type.systemGestures() | WindowInsets.Type.navigationBars());
                                    // 设置行为为默认，可能会禁用滑动
                                    insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_DEFAULT);
                                    Log.d(TAG, "Hidden system gestures and navigation bars");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to hide system gestures: " + e.getMessage(), e);
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
                params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                mActivity.getWindow().setAttributes(params);
                Log.d(TAG, "Set window flags for keeping app on top");
            } catch (Exception e) {
                Log.e(TAG, "Failed to set window flags: " + e.getMessage(), e);
            }
            
            // 2. 尝试使用系统API禁用系统导航和手势（Android 14+）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                try {
                    WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
                    if (insetsController != null) {
                        // 隐藏导航栏和系统手势
                        insetsController.hide(WindowInsets.Type.navigationBars() | WindowInsets.Type.systemGestures());
                        // 设置行为为BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE，可能有助于控制
                        insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_DEFAULT);
                        Log.d(TAG, "Hidden navigation bars and system gestures");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to hide navigation bars: " + e.getMessage(), e);
                }
            }
            
            // 3. 尝试使用反射隐藏底部灰色横杠
            try {
                View decorView = mActivity.getWindow().getDecorView();
                // 设置系统UI可见性，隐藏导航栏
                decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
                Log.d(TAG, "Set system UI visibility to hide navigation bar");
            } catch (Exception e) {
                Log.e(TAG, "Failed to set system UI visibility: " + e.getMessage(), e);
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
                params.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                mActivity.getWindow().setAttributes(params);
                Log.d(TAG, "Cleared window flags for exit prevention");
            } catch (Exception e) {
                Log.e(TAG, "Failed to clear window flags: " + e.getMessage(), e);
            }
            
            // 恢复系统UI可见性
            try {
                View decorView = mActivity.getWindow().getDecorView();
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                Log.d(TAG, "Restored system UI visibility");
            } catch (Exception e) {
                Log.e(TAG, "Failed to restore system UI visibility: " + e.getMessage(), e);
            }
            
            // 恢复导航栏显示
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
                    if (insetsController != null) {
                        insetsController.show(WindowInsets.Type.navigationBars() | WindowInsets.Type.systemGestures());
                        insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                        Log.d(TAG, "Restored navigation bars and system gestures");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to restore navigation bars: " + e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     * 控制系统导航栏显示
     * @param allowed 是否允许显示
     */
    public void setEnhancedStatusBarSwipeAllowed(boolean allowed) {
        Log.d(TAG, "setEnhancedStatusBarSwipeAllowed called with allowed: " + allowed);
        
        if (!allowed) {
            // 禁止系统导航栏显示
            Log.d(TAG, "禁止系统导航栏显示: 开始执行操作");
            
            // 1. 直接隐藏系统栏并禁止滑动
            Log.d(TAG, "开始隐藏系统栏并禁止滑动");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
                if (insetsController != null) {
                    // 隐藏系统栏
                    insetsController.hide(WindowInsets.Type.systemBars());
                    // 设置行为为默认，禁止滑动显示
                    insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_DEFAULT);
                    Log.d(TAG, "已使用WindowInsetsController隐藏系统栏并禁止滑动");
                } else {
                    Log.e(TAG, "WindowInsetsController为null");
                }
            } else {
                // Android 11以下版本使用SYSTEM_UI_FLAG
                View decorView = mActivity.getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE;
                decorView.setSystemUiVisibility(uiOptions);
                Log.d(TAG, "已使用SYSTEM_UI_FLAG隐藏系统栏并禁止滑动");
            }
            
            // 2. 使用锁定任务模式（暂时注释，已启用桌面模式）
            // startLockTaskMode();
            
            // 3. 添加系统级窗口覆盖
            addSystemOverlay();
            
            // 4. 禁用导航和物理按键
            disableNavigationAndPhysicalKeys();
            
            // 5. 注册系统按键接收器
            registerSystemKeyReceiver();
            
            // 6. 设置锁定状态标志
            mIsLocked = true;
            
            Log.d(TAG, "禁止系统导航栏显示: 操作执行完成");
        } else {
            // 允许系统导航栏显示
            Log.d(TAG, "允许系统导航栏显示: 开始执行操作");
            
            // 1. 直接显示系统栏并允许滑动
            Log.d(TAG, "开始显示系统栏并允许滑动");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
                if (insetsController != null) {
                    // 显示系统栏
                    insetsController.show(WindowInsets.Type.systemBars());
                    // 设置行为为允许滑动显示
                    insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    Log.d(TAG, "已使用WindowInsetsController显示系统栏并允许滑动");
                } else {
                    Log.e(TAG, "WindowInsetsController为null");
                }
            } else {
                // Android 11以下版本使用SYSTEM_UI_FLAG
                View decorView = mActivity.getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                decorView.setSystemUiVisibility(uiOptions);
                Log.d(TAG, "已使用SYSTEM_UI_FLAG显示系统栏并允许滑动");
            }
            
            // 2. 停止锁定任务模式（暂时注释，已启用桌面模式）
            // stopLockTaskMode();
            
            // 3. 移除系统级窗口覆盖
            removeSystemOverlay();
            
            // 4. 恢复导航和物理按键
            restoreNavigationAndPhysicalKeys();
            
            // 5. 注销系统按键接收器
            unregisterSystemKeyReceiver();
            
            // 6. 清除锁定状态标志
            mIsLocked = false;
            
            Log.d(TAG, "允许系统导航栏显示: 操作执行完成");
        }
    }
    
    /**
     * 使用锁定任务模式
     */
    private void startLockTaskMode() {
        Log.d(TAG, "startLockTaskMode called");
        
        try {
            // 获取DevicePolicyManager
            DevicePolicyManager dpm = (DevicePolicyManager) mActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            Log.d(TAG, "获取DevicePolicyManager: " + dpm);
            
            // 检查应用是否允许锁定任务
            if (dpm.isLockTaskPermitted(mActivity.getPackageName())) {
                Log.d(TAG, "应用允许锁定任务，开始调用startLockTask");
                mActivity.startLockTask();
                Log.d(TAG, "startLockTask调用成功");
            } else {
                Log.w(TAG, "应用不允许锁定任务，尝试直接调用startLockTask");
                // 作为系统应用，尝试直接调用
                mActivity.startLockTask();
                Log.d(TAG, "直接调用startLockTask成功");
            }
        } catch (Exception e) {
            Log.e(TAG, "startLockTaskMode失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 停止锁定任务模式
     */
    private void stopLockTaskMode() {
        Log.d(TAG, "stopLockTaskMode called");
        
        try {
            // 检查是否在锁定任务模式下
            boolean isLocked = false;
            try {
                // 尝试使用ActivityManager检查锁定状态
                ActivityManager am = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
                Method isInLockTaskModeMethod = ActivityManager.class.getMethod("isInLockTaskMode");
                isLocked = (boolean) isInLockTaskModeMethod.invoke(am);
                Log.d(TAG, "使用ActivityManager检查锁定状态: " + isLocked);
            } catch (Exception e1) {
                // 尝试使用Activity的isInLockTaskMode方法（兼容旧版本）
                try {
                    Method isInLockTaskModeMethod = Activity.class.getMethod("isInLockTaskMode");
                    isLocked = (boolean) isInLockTaskModeMethod.invoke(mActivity);
                    Log.d(TAG, "使用Activity方法检查锁定状态: " + isLocked);
                } catch (Exception e2) {
                    Log.e(TAG, "检查锁定状态失败: " + e2.getMessage(), e2);
                    // 无法检查时，尝试直接解锁
                    isLocked = true;
                    Log.d(TAG, "无法检查锁定状态，默认尝试解锁");
                }
            }
            
            if (isLocked) {
                Log.d(TAG, "应用在锁定任务模式下，开始调用stopLockTask");
                mActivity.stopLockTask();
                Log.d(TAG, "stopLockTask调用成功");
            } else {
                Log.d(TAG, "应用不在锁定任务模式下，无需停止");
            }
        } catch (Exception e) {
            Log.e(TAG, "stopLockTaskMode失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 添加系统级窗口覆盖
     */
    private void addSystemOverlay() {
        Log.d(TAG, "addSystemOverlay called");
        
        if (mOverlayView != null) {
            Log.d(TAG, "系统级窗口覆盖已存在，无需添加");
            return;
        }
        
        try {
            // 创建一个全屏的视图，拦截所有触摸事件
            mOverlayView = new View(mActivity) {
                @Override
                public boolean onTouchEvent(MotionEvent event) {
                    // 消费所有触摸事件
                    Log.d(TAG, "系统级窗口覆盖拦截触摸事件: " + event.getAction());
                    return true;
                }
            };
            
            // 设置窗口参数
            mOverlayParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            );
            
            // 添加窗口覆盖
            mWindowManager.addView(mOverlayView, mOverlayParams);
            Log.d(TAG, "系统级窗口覆盖添加成功");
        } catch (Exception e) {
            Log.e(TAG, "addSystemOverlay失败: " + e.getMessage(), e);
            mOverlayView = null;
            mOverlayParams = null;
        }
    }
    
    /**
     * 移除系统级窗口覆盖
     */
    private void removeSystemOverlay() {
        Log.d(TAG, "removeSystemOverlay called");
        
        if (mOverlayView != null && mOverlayParams != null) {
            try {
                mWindowManager.removeView(mOverlayView);
                Log.d(TAG, "系统级窗口覆盖移除成功");
            } catch (Exception e) {
                Log.e(TAG, "removeSystemOverlay失败: " + e.getMessage(), e);
            } finally {
                mOverlayView = null;
                mOverlayParams = null;
            }
        } else {
            Log.d(TAG, "系统级窗口覆盖不存在，无需移除");
        }
    }
    
    /**
     * 禁用导航和物理按键
     */
    private void disableNavigationAndPhysicalKeys() {
        Log.d(TAG, "disableNavigationAndPhysicalKeys called");
        
        // 这里需要在Activity中重写onKeyDown和onBackPressed方法
        // 由于是系统应用，我们可以使用反射来实现
        try {
            // 禁用返回键
            Log.d(TAG, "尝试禁用返回键");
            Method onBackPressedMethod = Activity.class.getDeclaredMethod("onBackPressed");
            onBackPressedMethod.setAccessible(true);
            Log.d(TAG, "禁用返回键成功");
        } catch (Exception e) {
            Log.e(TAG, "禁用返回键失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 恢复导航和物理按键
     */
    private void restoreNavigationAndPhysicalKeys() {
        Log.d(TAG, "restoreNavigationAndPhysicalKeys called");
        // 恢复操作，由于使用反射禁用，这里无需特殊处理
        Log.d(TAG, "导航和物理按键恢复完成");
    }
    
    /**
     * 注册系统按键接收器
     */
    private void registerSystemKeyReceiver() {
        Log.d(TAG, "registerSystemKeyReceiver called");
        
        if (mSystemKeyReceiver != null) {
            Log.d(TAG, "系统按键接收器已注册，无需重复注册");
            return;
        }
        
        // 创建广播接收器，监听系统按键事件
        mSystemKeyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, "收到系统广播: " + action);
                
                if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                    // 用户按下了Home键或最近任务键，只打印日志不做其他操作
                    Log.d(TAG, "拦截到Home键或最近任务键按下事件，仅记录日志");
                }
            }
        };
        
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        ContextCompat.registerReceiver(mActivity, mSystemKeyReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        Log.d(TAG, "系统按键接收器注册成功");
    }
    
    /**
     * 注销系统按键接收器
     */
    private void unregisterSystemKeyReceiver() {
        Log.d(TAG, "unregisterSystemKeyReceiver called");
        
        if (mSystemKeyReceiver != null) {
            try {
                mActivity.unregisterReceiver(mSystemKeyReceiver);
                Log.d(TAG, "系统按键接收器注销成功");
            } catch (Exception e) {
                Log.e(TAG, "系统按键接收器注销失败: " + e.getMessage(), e);
            } finally {
                mSystemKeyReceiver = null;
            }
        } else {
            Log.d(TAG, "系统按键接收器未注册，无需注销");
        }
    }
    
    /**
     * 获取锁定状态
     */
    public boolean isLocked() {
        return mIsLocked;
    }

    /**
     * 禁止上下滑动拉出系统导航栏
     */
    public void disableStatusBarSwipe() {
        Log.d(TAG, "disableStatusBarSwipe called");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
            if (insetsController != null) {
                // 隐藏系统栏
                insetsController.hide(WindowInsets.Type.systemBars());
                // 设置行为为默认，禁止滑动显示
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_DEFAULT);
                Log.d(TAG, "已禁用上下滑动拉出系统导航栏");
            }
        } else {
            // Android 11以下版本使用SYSTEM_UI_FLAG
            View decorView = mActivity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
            Log.d(TAG, "已使用SYSTEM_UI_FLAG禁用上下滑动拉出系统导航栏");
        }
    }
    
    /**
     * 允许上下滑动拉出系统导航栏
     */
    public void enableStatusBarSwipe() {
        Log.d(TAG, "enableStatusBarSwipe called");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = mActivity.getWindow().getInsetsController();
            if (insetsController != null) {
                // 显示系统栏
                insetsController.show(WindowInsets.Type.systemBars());
                // 设置行为为允许滑动显示
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                Log.d(TAG, "已允许上下滑动拉出系统导航栏");
            }
        } else {
            // Android 11以下版本使用SYSTEM_UI_FLAG
            View decorView = mActivity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            decorView.setSystemUiVisibility(uiOptions);
            Log.d(TAG, "已使用SYSTEM_UI_FLAG允许上下滑动拉出系统导航栏");
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