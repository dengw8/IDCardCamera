package com.example.idcardcamera.util;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Author       wildma
 * Github       https://github.com/wildma
 * Date         2018/6/10
 * Desc	        ${文件相关工具类}
 */
public class FileUtils {
    /**
     * 随机生成文件名
     */
    public static String getFileName() {
        return UUID.randomUUID().toString();
    }

    /**
     * 得到SD卡根目录，SD卡不可用则获取内部存储的根目录
     */
    public static File getRootPath() {
        File path = null;
        if (sdCardIsAvailable()) {
            path = Environment.getExternalStorageDirectory();  //SD卡根目录    /storage/emulated/0
        } else {
            path = Environment.getDataDirectory();    //内部存储的根目录    /data
        }
        return path;
    }

    /**
     * 判断SD卡是否可用
     */
    private static boolean sdCardIsAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 根据文件路径获取文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    public static File getFileByPath(String filePath) {
        return new File(filePath);
    }

    /**
     * 判断文件路径是否存在
     * @param dirPath 文件路径
     * @return 路径对应的文件夹是否存在
     */
    public static boolean isExists(String dirPath) {
        return getFileByPath(dirPath).exists();
    }


    /**
     * 根据路径创建文件夹
     * @param dirPath 文件路径
     * @return 文件夹是否创建成功
     */
    public static boolean createDirectoryByPath(String dirPath) {
        return new File(dirPath).mkdir();
    }

    /**
     * 根据路径创建文件
     * @param filePath 文件路径
     */
    public static void createFileByPath(File filePath, Bitmap bitmap) {
        try {
            FileOutputStream outStream;
            outStream = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭IO
     *
     * @param closeables closeable
     */
    public static void closeIO(Closeable... closeables) {
        if (closeables == null)
            return;
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
