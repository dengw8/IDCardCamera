package com.example.idcardcamera.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.widget.Toast;

/**
 * 相机相关功能类
 * 作用相当于一个工具类，以单例的模式提供给外部使用
 */

public class CameraUtil {
    private static Camera camera;

    public static Camera getCameraInstance(Context context) {
        if(camera == null) {
            if(hasCamera(context)) {
                try {
                    camera = Camera.open(); // attempt to get a Camera instance
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "No camera source", Toast.LENGTH_SHORT).show();
            }
        }
        return camera;
    }

    /**
     * 检查是否有相机
     *
     * @param context
     * @return
     */
    private static boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * 开关闪光灯
     *
     * @return 闪光灯是否开启
     */
    public static boolean switchFlashLight() {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                return true;
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                return false;
            }
        }
        return false;
    }

    /**
     * 对焦，在CameraActivity中触摸对焦
     */
    public static void focus() {
        if (camera != null) {
            camera.autoFocus(null);
        }
    }

    /**
     * 拍摄照片
     *
     * @param pictureCallback 在pictureCallback处理拍照回调
     */
    public static void takePhoto(Camera.PictureCallback pictureCallback) {
        if (camera != null) {
            camera.takePicture(null, null, pictureCallback);
        }
    }

    /**
     * 释放资源
     */
    public static void release() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}