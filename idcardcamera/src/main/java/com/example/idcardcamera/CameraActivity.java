package com.example.idcardcamera;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.idcardcamera.camera.CameraPreview;
import com.example.idcardcamera.camera.cropper.CropImageView;
import com.example.idcardcamera.camera.cropper.CropListener;
import com.example.idcardcamera.global.Constant;
import com.example.idcardcamera.rxjava.ImageSaveObservable;
import com.example.idcardcamera.rxjava.ImageSaveSubscriber;
import com.example.idcardcamera.util.CameraUtil;
import com.example.idcardcamera.util.PermissionUtil;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class CameraActivity extends Activity implements View.OnClickListener{
    public final static int    TYPE_IDCARD_FRONT      = 1;  //身份证正面
    public final static int    TYPE_IDCARD_BACK       = 2;  //身份证反面
    public final static String TAKE_TYPE_TAG             = "take_type";  //拍摄类型标记
    public static int      mType;//拍摄类型
    private static final int PERMISSION_CODE_SINGLE = 0X11;  //申请单个权限的权限码
    private static final int PERMISSION_CODE_MULTI = 0X12;  //申请多个权限的权限码

    private CropImageView mCropImageView;
    private Bitmap mCropBitmap;
    private CameraPreview mCameraPreview;
    private View mLlCameraCropContainer;
    private ImageView mIvCameraCrop;
    private ImageView mIvCameraFlash;
    private View mLlCameraOption;
    private View mLlCameraResult;
    private TextView mViewCameraCropBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 申请检查所需要的权限
        if(checkPermissions(Constant.permissions)) {
            initActivity();
        }
    }

    /**
     * 检查权限
     * @param permissions 权限数组
     */
    private boolean checkPermissions(String []permissions) {
        if(permissions.length == 1) {
            return PermissionUtil.checkSinglePermission(this, PERMISSION_CODE_SINGLE, permissions[0]);
        } else {
            return PermissionUtil.checkMultiPermissions(this, PERMISSION_CODE_MULTI, permissions);
        }
    }

    /**
     * 处理请求权限的响应
     *
     * @param requestCode  请求码
     * @param permissions  权限数组
     * @param grantResults 请求权限结果数组
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isPermitted = true;
        if (requestCode == PERMISSION_CODE_SINGLE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast(permissions[0] + "权限已申请");
            } else {
                isPermitted = false;
                showToast(permissions[0] + "权限已拒绝");
            }
        } else if (requestCode == PERMISSION_CODE_MULTI){
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    //判断是否勾选禁止后不再询问
                    boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i]);
                    if (showRequestPermission) {
                        isPermitted = false;
                        showToast(permissions[i] + "权限已拒绝");
                    }
                }
            }
        }
        if(isPermitted) {
            initActivity();
        } else {
            finish();
        }
    }

    private void initActivity() {
        setContentView(R.layout.activity_camera);
        // 设置屏幕为水平方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mType = getIntent().getIntExtra(TAKE_TYPE_TAG, 0);
        initView();
        initListener();
    }

    // 初始化控件
    private void initView() {
        mCameraPreview = findViewById(R.id.camera_preview);
        mLlCameraCropContainer = findViewById(R.id.ll_camera_crop_container);
        mIvCameraCrop = findViewById(R.id.camera_crop);
        mIvCameraFlash = findViewById(R.id.camera_flash);
        mLlCameraOption = findViewById(R.id.ll_camera_option);
        mLlCameraResult = findViewById(R.id.ll_camera_result);
        mCropImageView = findViewById(R.id.crop_image_view);
        mViewCameraCropBottom = findViewById(R.id.view_camera_crop_bottom);

        //获取屏幕最小边，设置为cameraPreview较窄的一边
        float screenMinSize = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
        //根据screenMinSize，计算出cameraPreview的较宽的一边，长宽比为标准的16:9
        float maxSize = screenMinSize / 9.0f * 16.0f;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) maxSize, (int) screenMinSize);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mCameraPreview.setLayoutParams(layoutParams);

        float height = (int) (screenMinSize * 0.75);
        float width = (int) (height * 75.0f / 47.0f);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams((int) width, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams cropParams = new LinearLayout.LayoutParams((int) width, (int) height);
        mLlCameraCropContainer.setLayoutParams(containerParams);
        mIvCameraCrop.setLayoutParams(cropParams);

        switch (mType) {
            case TYPE_IDCARD_FRONT:
                mIvCameraCrop.setImageResource(R.mipmap.camera_idcard_front);
                break;
            case TYPE_IDCARD_BACK:
                mIvCameraCrop.setImageResource(R.mipmap.camera_idcard_back);
                break;
        }
    }

    // 初始化控件的事件
    private void initListener() {
        mCameraPreview.setOnClickListener(this);
        mIvCameraFlash.setOnClickListener(this);
        findViewById(R.id.camera_close).setOnClickListener(this);
        findViewById(R.id.camera_take).setOnClickListener(this);
        findViewById(R.id.camera_result_ok).setOnClickListener(this);
        findViewById(R.id.camera_result_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.camera_preview) {
            // 开启对焦
            focus();
        } else if(id == R.id.camera_close) {
            // 推出当前Activity
            finish();
        } else if(id == R.id.camera_take) {
            // 拍照
            takePhoto();
        } else if(id == R.id.camera_flash) {
            // 控制闪光灯逻辑
            switchFlashLight();
        } else if(id == R.id.camera_result_ok) {
            // 确定保存照片
            confirmAndSaveImage();
        } else if(id == R.id.camera_result_cancel) {
            // 拍好照片后取消重新拍摄
            cancelAndRetry();
        }
    }

    /**
     * 对焦
     */
    private void focus() {
        CameraUtil.focus();
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        mCameraPreview.setEnabled(false);
        CameraUtil.takePhoto(new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                camera.stopPreview();
                //子线程处理图片，防止ANR
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                        // 计算裁剪位置
                        float left, top, right, bottom;
                        left = ((float) mLlCameraCropContainer.getLeft() - (float)mCameraPreview.getLeft()) / (float) mCameraPreview.getWidth();
                        top = (float) mIvCameraCrop.getTop() / (float) mCameraPreview.getHeight();
                        right = (float) mLlCameraCropContainer.getRight() / (float) mCameraPreview.getWidth();
                        bottom = (float) mIvCameraCrop.getBottom() / (float) mCameraPreview.getHeight();

                        // 自动裁剪
                        mCropBitmap = Bitmap.createBitmap(bitmap,
                                (int) (left * (float) bitmap.getWidth()),
                                (int) (top * (float) bitmap.getHeight()),
                                (int) ((right - left) * (float) bitmap.getWidth()),
                                (int) ((bottom - top) * (float) bitmap.getHeight()));

                        // 手动裁剪
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //将裁剪区域设置成与扫描框一样大
                                mCropImageView.setLayoutParams(new LinearLayout.LayoutParams(mIvCameraCrop.getWidth(), mIvCameraCrop.getHeight()));
                                setCropLayout();
                                mCropImageView.setImageBitmap(mCropBitmap);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    /**
     * 控制闪光灯的逻辑
     */
    private void switchFlashLight() {
        boolean isFlashOn = CameraUtil.switchFlashLight();
        mIvCameraFlash.setImageResource(isFlashOn ? R.mipmap.camera_flash_on : R.mipmap.camera_flash_off);
    }

    /**
     * 点击确认，返回MainActivity
     */
    private void confirmAndSaveImage() {
        /*裁剪图片*/
        mCropImageView.crop(new CropListener() {
            @Override
            public void onFinish(Bitmap bitmap) {
                // 将Bitmap保存到本地
                saveBitmap(bitmap);
            }
        }, true);
    }
    /**
     * 取消重新拍摄照片
     */
    private void cancelAndRetry() {
        mCameraPreview.setEnabled(true);
        mCameraPreview.startPreview();
        mIvCameraFlash.setImageResource(R.mipmap.camera_flash_off);
        setTakePhotoLayout();
    }

    /**
     * 设置裁剪布局
     */
    private void setCropLayout() {
        mIvCameraCrop.setVisibility(View.GONE);
        mCameraPreview.setVisibility(View.GONE);
        mLlCameraOption.setVisibility(View.GONE);
        mCropImageView.setVisibility(View.VISIBLE);
        mLlCameraResult.setVisibility(View.VISIBLE);
        mViewCameraCropBottom.setText("");
    }

    /**
     * 设置拍照布局
     */
    private void setTakePhotoLayout() {
        mIvCameraCrop.setVisibility(View.VISIBLE);
        mCameraPreview.setVisibility(View.VISIBLE);
        mLlCameraOption.setVisibility(View.VISIBLE);
        mCropImageView.setVisibility(View.GONE);
        mLlCameraResult.setVisibility(View.GONE);
        mViewCameraCropBottom.setText(getString(R.string.touch_to_focus));

        CameraUtil.focus();
    }

    /**
     * 保存 Bitmap 文件到本地
     * @param bitmap
     */
    private void saveBitmap(Bitmap bitmap) {
        rx.Observable.create(new ImageSaveObservable(bitmap))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ImageSaveSubscriber(getApplicationContext()));
    }

    private void showToast(String string){
        Toast.makeText(this,string, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
