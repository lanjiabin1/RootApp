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
            // 通过反射调用PowerManager的wipeData方法
            Method wipeDataMethod = mPowerManager.getClass().getMethod("wipeData",
                    int.class);
            wipeDataMethod.invoke(mPowerManager, 0); // 0表示恢复出厂设置，不保留数据
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}