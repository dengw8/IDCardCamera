package com.example.idcardcamera.rxjava;

import android.content.Context;
import android.widget.Toast;

import rx.Subscriber;

public class ImageSaveSubscriber extends Subscriber<String> {
    private Context context;

    public ImageSaveSubscriber(Context context) {
        this.context = context;
    }

    @Override
    public void onCompleted() {
        Toast.makeText(context, "图片保存成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(Throwable e) {
        Toast.makeText(context, "图片保存失败" + e.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNext(String s) {
        Toast.makeText(context, "图片保存路径为:" + s, Toast.LENGTH_SHORT).show();
    }
}
