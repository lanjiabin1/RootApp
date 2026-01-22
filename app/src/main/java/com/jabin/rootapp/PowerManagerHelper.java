package com.jabin.rootapp;

import android.content.Context;
import android.os.PowerManager;

import java.lang.reflect.Method;

/**
 * 电源管理助手类，提供关机、重启、恢复出厂设置功能
 */
public class PowerManagerHelper {

    private Context mContext;
    private PowerManager mPowerManager;

    public PowerManagerHelper(Context context) {
        this.mContext = context;
        this.mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }

    /**
     * 关机功能
     * @return 是否执行成功
     */
    public boolean shutdown() {
        try {
            // 通过反射调用PowerManager的shutdown方法
            Method shutdownMethod = mPowerManager.getClass().getMethod("shutdown",
                    boolean.class, String.class, boolean.class);
            shutdownMethod.invoke(mPowerManager, false, null, false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 重启功能
     * @return 是否执行成功
     */
    public boolean reboot() {
        try {
            // 通过反射调用PowerManager的reboot方法
            Method rebootMethod = mPowerManager.getClass().getMethod("reboot",
                    String.class);
            rebootMethod.invoke(mPowerManager, new Object[] { null });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 恢复出厂设置
     * @return 是否执行成功
     */
    public boolean factoryReset() {
        try {
            // 尝试多种恢复出厂设置的方法
            
            // 方法1: 尝试使用wipeData方法（不同参数签名）
            try {
                // 尝试无参数的wipeData方法
                Method wipeDataMethod = mPowerManager.getClass().getMethod("wipeData");
                wipeDataMethod.invoke(mPowerManager);
                return true;
            } catch (NoSuchMethodException e1) {
                // 方法2: 尝试使用其他参数的wipeData方法
                try {
                    // 尝试使用不同参数类型的wipeData方法
                    Method[] methods = mPowerManager.getClass().getMethods();
                    for (Method method : methods) {
                        if (method.getName().equals("wipeData")) {
                            // 找到wipeData方法，不管参数，尝试调用
                            Class<?>[] paramTypes = method.getParameterTypes();
                            if (paramTypes.length == 0) {
                                method.invoke(mPowerManager);
                                return true;
                            } else if (paramTypes.length == 1) {
                                // 根据参数类型选择合适的默认值
                                if (paramTypes[0] == int.class || paramTypes[0] == Integer.class) {
                                    method.invoke(mPowerManager, 0);
                                } else if (paramTypes[0] == boolean.class || paramTypes[0] == Boolean.class) {
                                    method.invoke(mPowerManager, false);
                                } else {
                                    // 其他参数类型，尝试传入null
                                    method.invoke(mPowerManager, new Object[] { null });
                                }
                                return true;
                            }
                        }
                    }
                } catch (Exception e2) {
                    // 方法3: 尝试使用SystemProperties方法（Android 8.0+）
                    try {
                        Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
                        Method setMethod = systemPropertiesClass.getMethod("set", String.class, String.class);
                        setMethod.invoke(null, "persist.sys.factory_reset", "1");
                        
                        // 重启设备
                        reboot();
                        return true;
                    } catch (Exception e3) {
                        // 方法4: 尝试使用DevicePolicyManager方式
                        try {
                            Class<?> devicePolicyManagerClass = Class.forName("android.app.admin.DevicePolicyManager");
                            Object dpm = mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
                            if (dpm != null) {
                                // 尝试调用wipeData方法
                                Method wipeDataMethod = devicePolicyManagerClass.getMethod("wipeData", int.class);
                                // 获取wipeData的标志常量
                                int WIPE_EXTERNAL_STORAGE = 0;
                                try {
                                    WIPE_EXTERNAL_STORAGE = (int) devicePolicyManagerClass.getField("WIPE_EXTERNAL_STORAGE").get(null);
                                } catch (Exception e) {
                                    // 忽略，使用默认值0
                                }
                                wipeDataMethod.invoke(dpm, WIPE_EXTERNAL_STORAGE);
                                return true;
                            }
                        } catch (Exception e4) {
                            e4.printStackTrace();
                        }
                        
                        // 方法5: 尝试使用RecoverySystem方式
                        try {
                            Class<?> recoverySystemClass = Class.forName("android.os.RecoverySystem");
                            Method rebootWipeDataMethod = recoverySystemClass.getMethod("rebootWipeData", Context.class);
                            rebootWipeDataMethod.invoke(null, mContext);
                            return true;
                        } catch (Exception e5) {
                            e5.printStackTrace();
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}