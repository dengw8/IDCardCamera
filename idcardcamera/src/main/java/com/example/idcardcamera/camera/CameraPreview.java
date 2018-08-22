package com.example.idcardcamera.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.idcardcamera.util.CameraUtil;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
    private Camera mCamera;

    public CameraPreview(Context context) {
        super(context);
        init(context);
    }
    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    //竖屏拍照时，需要设置旋转90度，否者看到的相机预览方向和界面方向不相同
                    mCamera.setDisplayOrientation(90);
                    parameters.setRotation(90);
                } else {
                    mCamera.setDisplayOrientation(0);
                    parameters.setRotation(0);
                }

                // 设置预览的尺寸
                Camera.Size size = getBestSize(parameters.getSupportedPreviewSizes());
                if(size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                    parameters.setPictureSize(size.width, size.height);
                } else {
                    parameters.setPreviewSize(1920, 1080);
                    parameters.setPictureSize(1920, 1080);
                }
                //设置surfaceHolder
                mCamera.setPreviewDisplay(holder);
                //设置相机参数
                mCamera.setParameters(parameters);
                // 开启预览
                mCamera.startPreview();
                //首次对焦
                CameraUtil.focus();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        //因为设置了固定屏幕方向，所以在实际使用中不会触发这个方法
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //回收释放资源
        release();
    }

    private void init(Context context) {
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCamera = CameraUtil.getCameraInstance(context);
    }

    /**
     * Android相机的预览尺寸都是4:3或者16:9，这里遍历所有支持的预览尺寸，得到16:9的最大尺寸，保证成像清晰度
     *
     * @param sizes
     * @return 最佳尺寸
     */
    private Camera.Size getBestSize(List<Camera.Size> sizes) {
        Camera.Size bestSize = null;
        for (Camera.Size size : sizes) {
            if ((float) size.width / (float) size.height == 16.0f / 9.0f) {
                if (bestSize == null) {
                    bestSize = size;
                } else {
                    if (size.width > bestSize.width) {
                        bestSize = size;
                    }
                }
            }
        }
        return bestSize;
    }

    /**
     * 释放资源
     */
    private void release() {
        if (mCamera != null) {
            mCamera.stopPreview();
            CameraUtil.release();
        }
    }

    /**
     * 开启预览
     */
    public void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }
}
