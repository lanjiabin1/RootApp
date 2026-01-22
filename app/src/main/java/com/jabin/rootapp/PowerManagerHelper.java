package com.jabin.rootapp;

import android.content.Context;
import android.os.PowerManager;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
            // 尝试多种恢复出厂设置的方法，适配Android 12+
            
            // 方法1: 尝试使用wipeData方法（不同参数签名）
            try {
                // 尝试不同参数类型的wipeData方法
                Method[] methods = mPowerManager.getClass().getMethods();
                for (Method method : methods) {
                    if (method.getName().equals("wipeData")) {
                        Class<?>[] paramTypes = method.getParameterTypes();
                        System.out.println("Found wipeData method with " + paramTypes.length + " parameters");
                        for (int i = 0; i < paramTypes.length; i++) {
                            System.out.println("  Param " + i + ": " + paramTypes[i].getName());
                        }
                        
                        try {
                            if (paramTypes.length == 0) {
                                System.out.println("Trying wipeData() with no params");
                                method.invoke(mPowerManager);
                                System.out.println("wipeData() with no params succeeded");
                                return true;
                            } else if (paramTypes.length == 1) {
                                if (paramTypes[0] == int.class || paramTypes[0] == Integer.class) {
                                    System.out.println("Trying wipeData(int) with value 0");
                                    method.invoke(mPowerManager, 0);
                                    System.out.println("wipeData(int) with value 0 succeeded");
                                    return true;
                                } else if (paramTypes[0] == boolean.class || paramTypes[0] == Boolean.class) {
                                    System.out.println("Trying wipeData(boolean) with value false");
                                    method.invoke(mPowerManager, false);
                                    System.out.println("wipeData(boolean) with value false succeeded");
                                    return true;
                                } else {
                                    System.out.println("Trying wipeData() with null param for type: " + paramTypes[0].getName());
                                    method.invoke(mPowerManager, new Object[] { null });
                                    System.out.println("wipeData() with null param succeeded");
                                    return true;
                                }
                            } else if (paramTypes.length == 2) {
                                // 某些设备可能有两个参数的wipeData方法
                                System.out.println("Trying wipeData() with two params");
                                Object[] args = new Object[2];
                                for (int i = 0; i < 2; i++) {
                                    if (paramTypes[i] == int.class || paramTypes[i] == Integer.class) {
                                        args[i] = 0;
                                    } else if (paramTypes[i] == boolean.class || paramTypes[i] == Boolean.class) {
                                        args[i] = false;
                                    } else {
                                        args[i] = null;
                                    }
                                }
                                method.invoke(mPowerManager, args);
                                System.out.println("wipeData() with two params succeeded");
                                return true;
                            }
                        } catch (Exception e) {
                            System.out.println("Failed to invoke wipeData method: " + e.getMessage());
                            e.printStackTrace();
                            // 继续尝试其他方法
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to find or invoke wipeData methods: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 方法2: 尝试使用RecoverySystem的rebootWipeData方法（多种参数签名）
            try {
                Class<?> recoverySystemClass = Class.forName("android.os.RecoverySystem");
                System.out.println("Trying RecoverySystem methods");
                
                // 尝试多种rebootWipeData方法签名
                Method[] rsMethods = recoverySystemClass.getMethods();
                for (Method rsMethod : rsMethods) {
                    if (rsMethod.getName().contains("reboot") || rsMethod.getName().contains("wipe") || rsMethod.getName().contains("factory")) {
                        System.out.println("Found RecoverySystem method: " + rsMethod.getName());
                        Class<?>[] paramTypes = rsMethod.getParameterTypes();
                        for (int i = 0; i < paramTypes.length; i++) {
                            System.out.println("  Param " + i + ": " + paramTypes[i].getName());
                        }
                        
                        try {
                            if (paramTypes.length == 1 && paramTypes[0] == Context.class) {
                                System.out.println("Trying RecoverySystem.rebootWipeData(Context)");
                                rsMethod.invoke(null, mContext);
                                System.out.println("RecoverySystem.rebootWipeData(Context) succeeded");
                                return true;
                            } else if (paramTypes.length == 2 && paramTypes[0] == Context.class) {
                                System.out.println("Trying RecoverySystem.rebootWipeData with two params");
                                Object[] args = new Object[2];
                                args[0] = mContext;
                                if (paramTypes[1] == boolean.class || paramTypes[1] == Boolean.class) {
                                    args[1] = false;
                                } else if (paramTypes[1] == int.class || paramTypes[1] == Integer.class) {
                                    args[1] = 0;
                                } else {
                                    args[1] = null;
                                }
                                rsMethod.invoke(null, args);
                                System.out.println("RecoverySystem method with two params succeeded");
                                return true;
                            }
                        } catch (Exception e) {
                            System.out.println("Failed to invoke RecoverySystem method: " + e.getMessage());
                            // 继续尝试其他方法
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to use RecoverySystem: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 方法3: 尝试使用DevicePolicyManager的wipeData方法（多种参数签名）
            try {
                Class<?> devicePolicyManagerClass = Class.forName("android.app.admin.DevicePolicyManager");
                Object dpm = mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
                if (dpm != null) {
                    System.out.println("Trying DevicePolicyManager methods");
                    Method[] dpmMethods = devicePolicyManagerClass.getMethods();
                    for (Method dpmMethod : dpmMethods) {
                        if (dpmMethod.getName().contains("wipe") || dpmMethod.getName().contains("factory") || dpmMethod.getName().contains("reset")) {
                            System.out.println("Found DPM method: " + dpmMethod.getName());
                            Class<?>[] paramTypes = dpmMethod.getParameterTypes();
                            for (int i = 0; i < paramTypes.length; i++) {
                                System.out.println("  Param " + i + ": " + paramTypes[i].getName());
                            }
                            
                            try {
                                if (paramTypes.length == 1 && paramTypes[0] == int.class) {
                                    // 尝试调用wipeData(int)方法
                                    int flag = 0;
                                    try {
                                        flag = (int) devicePolicyManagerClass.getField("WIPE_EXTERNAL_STORAGE").get(null);
                                        System.out.println("Found WIPE_EXTERNAL_STORAGE flag: " + flag);
                                    } catch (Exception e) {
                                        // 尝试其他标志
                                        try {
                                            flag = (int) devicePolicyManagerClass.getField("WIPE_RESET_PROTECTION_DATA").get(null);
                                            System.out.println("Found WIPE_RESET_PROTECTION_DATA flag: " + flag);
                                        } catch (Exception e2) {
                                            System.out.println("Using default flag value: 0");
                                        }
                                    }
                                    System.out.println("Trying DPM method with flag: " + flag);
                                    dpmMethod.invoke(dpm, flag);
                                    System.out.println("DPM method with flag succeeded");
                                    return true;
                                } else if (paramTypes.length == 0) {
                                    System.out.println("Trying DPM method with no params");
                                    dpmMethod.invoke(dpm);
                                    System.out.println("DPM method with no params succeeded");
                                    return true;
                                } else if (paramTypes.length == 2) {
                                    System.out.println("Trying DPM method with two params");
                                    Object[] args = new Object[2];
                                    for (int i = 0; i < 2; i++) {
                                        if (paramTypes[i] == int.class || paramTypes[i] == Integer.class) {
                                            args[i] = 0;
                                        } else if (paramTypes[i] == boolean.class || paramTypes[i] == Boolean.class) {
                                            args[i] = false;
                                        } else {
                                            args[i] = null;
                                        }
                                    }
                                    dpmMethod.invoke(dpm, args);
                                    System.out.println("DPM method with two params succeeded");
                                    return true;
                                }
                            } catch (Exception e) {
                                System.out.println("Failed to invoke DPM method: " + e.getMessage());
                                // 继续尝试其他方法
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to use DevicePolicyManager: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 方法4: 尝试使用SystemProperties设置恢复出厂设置标志，然后重启
            try {
                System.out.println("Trying SystemProperties approach");
                Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
                Method setMethod = systemPropertiesClass.getMethod("set", String.class, String.class);
                
                // 尝试多种恢复出厂设置的属性名
                String[] props = {
                    "persist.sys.factory_reset",
                    "sys.factory_reset",
                    "ctl.start",
                    "persist.service.factory_reset"
                };
                
                for (String prop : props) {
                    for (String value : new String[]{"1", "yes", "start"}) {
                        try {
                            System.out.println("Setting property " + prop + "=" + value);
                            setMethod.invoke(null, prop, value);
                            System.out.println("Set property succeeded, rebooting...");
                            
                            // 重启设备
                            reboot();
                            return true;
                        } catch (Exception e) {
                            System.out.println("Failed to set property " + prop + "=" + value + ": " + e.getMessage());
                            // 继续尝试其他属性值
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to use SystemProperties approach: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 方法5: 尝试使用Intent发送恢复出厂设置广播
            try {
                System.out.println("Trying Intent broadcast approach");
                Class<?> intentClass = Class.forName("android.content.Intent");
                Object intent = intentClass.getConstructor(String.class).newInstance("android.intent.action.FACTORY_RESET");
                
                // 设置必要的标志
                Method addFlagsMethod = intentClass.getMethod("addFlags", int.class);
                int FLAG_ACTIVITY_NEW_TASK = 268435456; // Intent.FLAG_ACTIVITY_NEW_TASK
                addFlagsMethod.invoke(intent, FLAG_ACTIVITY_NEW_TASK);
                
                // 发送广播或启动Activity
                Method sendBroadcastMethod = mContext.getClass().getMethod("sendBroadcast", intentClass);
                sendBroadcastMethod.invoke(mContext, intent);
                System.out.println("Sent factory reset broadcast");
                return true;
            } catch (Exception e) {
                System.out.println("Failed to use Intent broadcast approach: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 方法6: 尝试使用ActivityManager的clearApplicationUserData方法
            try {
                System.out.println("Trying ActivityManager.clearApplicationUserData");
                Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
                Object am = mContext.getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    Method clearUserDataMethod = activityManagerClass.getMethod("clearApplicationUserData");
                    boolean result = (boolean) clearUserDataMethod.invoke(am);
                    System.out.println("clearApplicationUserData result: " + result);
                    if (result) {
                        return true;
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to use clearApplicationUserData: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 方法7: 尝试使用PackageManager的deletePackage方法（删除所有用户应用）
            try {
                System.out.println("Trying PackageManager.deletePackage for system reset");
                Class<?> packageManagerClass = Class.forName("android.content.pm.PackageManager");
                Object pm = mContext.getPackageManager();
                if (pm != null) {
                    Method getInstalledPackagesMethod = packageManagerClass.getMethod("getInstalledPackages", int.class);
                    @SuppressWarnings("unchecked")
                    java.util.List<Object> packages = (java.util.List<Object>) getInstalledPackagesMethod.invoke(pm, 0);
                    
                    // 尝试获取PackageInfo类和相关方法
                    Class<?> packageInfoClass = Class.forName("android.content.pm.PackageInfo");
                    Method getPackageNameMethod = packageInfoClass.getMethod("getPackageName");
                    
                    for (Object pkg : packages) {
                        String packageName = (String) getPackageNameMethod.invoke(pkg);
                        if (!packageName.equals(mContext.getPackageName())) {
                            System.out.println("Trying to delete package: " + packageName);
                            try {
                                Method deletePackageMethod = packageManagerClass.getMethod("deletePackage", String.class, Class.forName("android.content.pm.PackageManager$DeletePackageCallback"), int.class);
                                // 创建DeletePackageCallback代理
                                Class<?> deleteCallbackClass = Class.forName("android.content.pm.PackageManager$DeletePackageCallback");
                                Object callback = Proxy.newProxyInstance(
                                        deleteCallbackClass.getClassLoader(),
                                        new Class<?>[]{deleteCallbackClass},
                                        new java.lang.reflect.InvocationHandler() {
                                            @Override
                                            public Object invoke(Object proxy, java.lang.reflect.Method cbMethod, Object[] args) {
                                                System.out.println("DeletePackageCallback invoked: " + cbMethod.getName());
                                                return null;
                                            }
                                        });
                                deletePackageMethod.invoke(pm, packageName, callback, 0);
                            } catch (Exception e) {
                                System.out.println("Failed to delete package " + packageName + ": " + e.getMessage());
                                // 继续尝试其他方法
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to use PackageManager.deletePackage: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("All factory reset methods failed");
            return false;
        } catch (Exception e) {
            System.out.println("Unexpected exception in factoryReset: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}