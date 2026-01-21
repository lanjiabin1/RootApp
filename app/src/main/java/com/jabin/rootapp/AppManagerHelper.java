package com.jabin.rootapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

/**
 * 应用管理助手类，提供静默安装和卸载应用功能
 */
public class AppManagerHelper {

    private Context mContext;
    private PackageManager mPackageManager;

    public AppManagerHelper(Context context) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
    }

    /**
     * 静默安装应用
     * @param apkPath APK文件路径
     * @return 是否安装成功
     */
    public boolean silentInstall(String apkPath) {
        try {
            File apkFile = new File(apkPath);
            if (!apkFile.exists()) {
                return false;
            }

            // Android 5.0及以上版本使用PackageInstaller
            return installApkWithPackageInstaller(apkPath);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 使用PackageInstaller安装APK（Android 5.0+）
     * @param apkPath APK文件路径
     * @return 是否安装成功
     */
    private boolean installApkWithPackageInstaller(String apkPath) {
        try {
            PackageInstaller packageInstaller = mPackageManager.getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int sessionId = packageInstaller.createSession(params);

            PackageInstaller.Session session = packageInstaller.openSession(sessionId);
            OutputStream out = session.openWrite("app", 0, -1);
            InputStream in = new FileInputStream(apkPath);
            byte[] buffer = new byte[65536];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            session.fsync(out);
            in.close();
            out.close();

            // 创建安装会话并提交
            session.commit(null);
            session.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 静默卸载应用
     * @param packageName 应用包名
     * @return 是否卸载成功
     */
    public boolean silentUninstall(String packageName) {
        try {
            // Android 5.0及以上版本使用PackageInstaller
            PackageInstaller packageInstaller = mPackageManager.getPackageInstaller();
            packageInstaller.uninstall(packageName, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查应用是否已安装
     * @param packageName 应用包名
     * @return 是否已安装
     */
    public boolean isAppInstalled(String packageName) {
        try {
            mPackageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * 打开应用
     * @param packageName 应用包名
     * @return 是否打开成功
     */
    public boolean openApp(String packageName) {
        try {
            Intent intent = mPackageManager.getLaunchIntentForPackage(packageName);
            if (intent != null) {
                mContext.startActivity(intent);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}