package com.example.idcardcamera.global;

import android.Manifest;

import com.example.idcardcamera.util.FileUtils;

import java.io.File;

public class Constant {
    // 需要申请的权限数组
    public static String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    // 图片路经相关
    public static final String APP_NAME = "IDCardCamera";  //app名称
    public static final String BASE_DIR = APP_NAME + File.separator;  // 图片的父文件夹
    public static final String DIR_ROOT = FileUtils.getRootPath() + File.separator + Constant.BASE_DIR;   //全路径
}