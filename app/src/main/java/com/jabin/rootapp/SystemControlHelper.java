package com.jabin.rootapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * 系统控制助手类，提供设置系统时间和截图功能
 */
public class SystemControlHelper {

    private Context mContext;
    private MediaProjectionManager mMediaProjectionManager;

    public SystemControlHelper(Context context) {
        this.mContext = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mMediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }
    }

    /**
     * 设置系统时间
     * @param year 年
     * @param month 月（1-12）
     * @param day 日（1-31）
     * @param hour 时（0-23）
     * @param minute 分（0-59）
     * @param second 秒（0-59）
     * @return 是否设置成功
     */
    public boolean setSystemTime(int year, int month, int day, int hour, int minute, int second) {
        try {
            // 创建Calendar对象并设置时间
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1); // 月份从0开始
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);

            // 设置系统时间
            long millis = calendar.getTimeInMillis();
            SystemClock.setCurrentTimeMillis(millis);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置系统时区
     * @param timezone 时区ID，如"Asia/Shanghai"
     * @return 是否设置成功
     */
    public boolean setTimeZone(String timezone) {
        try {
            // 设置系统时区
            TimeZone.setDefault(TimeZone.getTimeZone(timezone));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 截图功能（需要MediaProjection权限）
     * @param mediaProjection MediaProjection对象
     * @param callback 截图回调
     */
    public void takeScreenshot(MediaProjection mediaProjection, final ScreenshotCallback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            callback.onScreenshotFailed(new Exception("Screenshot requires Android 5.0 or higher"));
            return;
        }

        try {
            // 获取屏幕尺寸
            DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            int densityDpi = metrics.densityDpi;

            // 创建ImageReader用于捕获截图
            final ImageReader imageReader = ImageReader.newInstance(width, height, android.graphics.PixelFormat.RGBA_8888, 1);

            // 创建VirtualDisplay
            VirtualDisplay virtualDisplay = mediaProjection.createVirtualDisplay(
                    "Screenshot",
                    width, height, densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.getSurface(),
                    null, null);

            // 等待截图完成
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    FileOutputStream fos = null;
                    Bitmap bitmap = null;

                    try {
                        // 获取Image对象
                        image = reader.acquireLatestImage();
                        if (image != null) {
                            Image.Plane[] planes = image.getPlanes();
                            ByteBuffer buffer = planes[0].getBuffer();
                            int pixelStride = planes[0].getPixelStride();
                            int rowStride = planes[0].getRowStride();
                            int rowPadding = rowStride - pixelStride * width;

                            // 创建Bitmap
                            bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                            bitmap.copyPixelsFromBuffer(buffer);

                            // 保存Bitmap到文件
                            File screenshotDir = new File(Environment.getExternalStorageDirectory(), "Screenshots");
                            if (!screenshotDir.exists()) {
                                screenshotDir.mkdirs();
                            }
                            String fileName = "screenshot_" + System.currentTimeMillis() + ".png";
                            File screenshotFile = new File(screenshotDir, fileName);
                            fos = new FileOutputStream(screenshotFile);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                            // 调用回调
                            callback.onScreenshotSuccess(screenshotFile.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        callback.onScreenshotFailed(e);
                    } finally {
                        // 释放资源
                        if (image != null) {
                            image.close();
                        }
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (bitmap != null) {
                            bitmap.recycle();
                        }
                        if (virtualDisplay != null) {
                            virtualDisplay.release();
                        }
                        imageReader.close();
                    }
                }
            }, new Handler(Looper.getMainLooper()));
        } catch (Exception e) {
            callback.onScreenshotFailed(e);
        }
    }

    /**
     * 获取MediaProjectionManager（用于启动截图功能）
     * @return MediaProjectionManager对象
     */
    public MediaProjectionManager getMediaProjectionManager() {
        return mMediaProjectionManager;
    }

    /**
     * 截图回调接口
     */
    public interface ScreenshotCallback {
        /**
         * 截图成功回调
         * @param screenshotPath 截图文件路径
         */
        void onScreenshotSuccess(String screenshotPath);

        /**
         * 截图失败回调
         * @param e 失败原因
         */
        void onScreenshotFailed(Exception e);
    }
}