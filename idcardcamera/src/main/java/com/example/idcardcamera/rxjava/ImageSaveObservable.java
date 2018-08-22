package com.example.idcardcamera.rxjava;

import android.graphics.Bitmap;
import android.os.Environment;

import com.example.idcardcamera.global.Constant;
import com.example.idcardcamera.util.FileUtils;

import java.io.File;

import rx.Observable;
import rx.Subscriber;

public class ImageSaveObservable implements Observable.OnSubscribe<String> {

    private Bitmap drawingCache;

    public ImageSaveObservable(Bitmap drawingCache) {
        this.drawingCache = drawingCache;
    }

    @Override
    public void call(Subscriber<? super String> subscriber) {
        if (drawingCache == null) {
            subscriber.onError(new NullPointerException("imageview的 bitmap 获取为null,请确认 imageview 显示图片了"));
        } else {
            // 获取要存储照片的全路径名
            String DIR_ROOT = Constant.DIR_ROOT;
            //判断当前路径是否存在，如果不存在则创建该文件夹
            if (!(FileUtils.isExists(DIR_ROOT) || FileUtils.createDirectoryByPath(DIR_ROOT))) {
                subscriber.onError(new NullPointerException("文件夹创建失败！"));
            } else {
                // 保存图片
                File filePath = new File(DIR_ROOT + FileUtils.getFileName() + ".jpg");
                FileUtils.createFileByPath(filePath, drawingCache);
                subscriber.onNext(DIR_ROOT);
                subscriber.onCompleted();
            }
        }
    }
}