package com.example.administrator.idcardcamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.idcardcamera.CameraActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.front)
    Button front;
    @BindView(R.id.back)
    Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 使用ButterKnife绑定视图
        ButterKnife.bind(this);
    }

    @OnClick({R.id.front, R.id.back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.front:
                toCameraActivity(CameraActivity.TYPE_IDCARD_FRONT);
                break;
            case R.id.back:
                toCameraActivity(CameraActivity.TYPE_IDCARD_BACK);
                break;
             default:
                 break;
        }
    }

    /**
     * 跳转到拍照的Activity
     * @param type
     */
    private void toCameraActivity(int type) {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        intent.putExtra(CameraActivity.TAKE_TYPE_TAG, type);
        startActivity(intent);
    }
}
