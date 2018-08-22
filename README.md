# IDCardCamera
A demo to learn Android camera


## Camera 实践
Demo实现的功能是一个拍摄身份证正反面的相机应用，首先肯定支持拍摄功能，这个过程就可以完整地体验一下相机应用的开发流程以及相关API的使用，比如：

* 预览
* 闪光灯
* 对焦
* 拍摄

其次呢，我们是为了拍摄身份证正反两面的照片，还需要支持对图片的截取功能，这部分我们通过自定义一个控件实现图片四个角的截取。

### 相机开发的一般流程
1. 检测并访问相机资源，检查手机是否存在相机资源，如果存在则请求访问相机资源。
2. 创建预览界面，创建继承自SurfaceView并实现SurfaceHolder接口的拍摄预览类。有了拍摄预览类，即可创建一个布局文件，将预览画面与设计好的用户界面控件融合在一起，实时显示相机的预览图像。
3. 设置拍照监听器，给用户界面控件绑定监听器，使其能响应用户操作, 开始拍照过程。
4. 拍照并保存文件，将拍摄获得的图像转换成位图文件，最终输出保存成各种常用格式的图片。
5. 释放相机资源，相机是一个共享资源，当相机使用完毕后，必须正确地将其释放，以免其它程序访问使用时发生冲突。

#### 资源权限和权限申请
首先要检查相机资源，获取系统相机的相关信息。


``` java
// 判断是否有相机
context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

// 判断是否有前置相机
context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);

```
或者
``` java
//有多少个摄像头
numberOfCameras = Camera.getNumberOfCameras();

for (int i = 0; i < numberOfCameras; ++i) {
    final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

    Camera.getCameraInfo(i, cameraInfo);
    //后置摄像头
    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
    {
        faceBackCameraId = i;
        faceBackCameraOrientation = cameraInfo.orientation;
    } 
    //前置摄像头
    else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) 
    {
        faceFrontCameraId = i;
        faceFrontCameraOrientation = cameraInfo.orientation;
    }
}
```


另外，相机开发还会设计及到一些权限操作，比如，调用相机拍摄图片的权限，读、写文件的权限等，在demo中需要申请3个权限：
* Manifest.permission.WRITE_EXTERNAL_STORAGE
* Manifest.permission.READ_EXTERNAL_STORAGE
* Manifest.permission.CAMERA

这里学习到了一种同时申请多个权限的方法

* 首先用一个字符串数组保存要申请的权限：
```java
// 需要申请的权限数组
public static String[] permissions = new String[]{
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
};
```

* 检查应用是否已经具有某个权限，如果没有的话就申请这个权限；
```java
/**
 * 检查权限，检查应用所需要的某个权限
 * @param context
 * @param requestCode  请求码
 * @param permission  权限
 * @return
*/
public static boolean checkSinglePermission(Context context,int requestCode, String permission) {
	int permissionCode = ActivityCompat.checkSelfPermission(context, permission);
	// PackageManager.PERMISSION_GRANTED 表示应用已经具有的权限
	if(permissionCode != PackageManager.PERMISSION_GRANTED) {
		ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode);
		return false;
	}
	return true;
}
```

或者说同时检查应用是否已经具有多个权限，如果没有的话就申请其中应用还不具备权限；
```java
/**
 * 检查权限，检查应用所需要的所有权限
 * @param context
 * @param requestCode  请求码
 * @param permissions  权限数组
 * @return
 */
public static boolean checkMultiPermissions(Context context, int requestCode, String []permissions) {
	rejectedPermissions.clear();
	for(String per : permissions) {
		int permissionCode = ActivityCompat.checkSelfPermission(context, per);
		if(permissionCode != PackageManager.PERMISSION_GRANTED) {
	        rejectedPermissions.add(per);
        }
    }
    if(rejectedPermissions.isEmpty()) {
	    return true;
    } else {
	    String[] permissionList = rejectedPermissions.toArray(new String[rejectedPermissions.size()]);
        ActivityCompat.requestPermissions((Activity)context, permissionList, requestCode);
        return false;
    }
}
```
思路就是用一个数组保存还不具备的权限，如果数组不为空的话说明就需要申请这些权限。

* 申请需要的权限

申请一个权限的时候使用的是 `ActivityCompat` 的 `requestPermissions` 方法，该方法的声明为：
```
public static void requestPermissions(final @NonNull Activity activity,
            final @NonNull String[] permissions, final @IntRange(from = 0) int requestCode)
```
其中：
* activity：上下文，
* permissions：权限数组
* requestCode：请求码

并且该方法请求完之后会有一个回调接口，需要在上面传入的上下文中重写该方法，该回调接口的作用就是权限请求的结果：
```java
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
/* callback - no nothing */
}
```

所以，比如当我们  requestPermissions 方法传入的 context 是 CameraActivity ，那么我们就需要在 CameraActivity 中重写 onRequestPermissionsResult 方法，详细见下面链接中给出的项目代码。

#### 打开相机
```
// 默认开启后置相机
camera = Camera.open();

// 当知道某个相机具体的id，参考上面检查是否有相机资源的第二种方法
camera = Camera.open(cameraId);
```

#### 获取相机参数
```
//获取相机参数
Camera.Parameters parameters = camera.getParameters();
```

常见的参数有以下几种:

闪光灯配置参数，可以通过Parameters.getFlashMode()接口获取:
* Camera.Parameters.FLASH_MODE_AUTO 自动模式，当光线较暗时自动打开闪光灯；
* Camera.Parameters.FLASH_MODE_OFF 关闭闪光灯；
* Camera.Parameters.FLASH_MODE_ON 拍照时闪光灯；
* Camera.Parameters.FLASH_MODE_RED_EYE 闪光灯参数，防红眼模式。

对焦模式配置参数，可以通过Parameters.getFocusMode()接口获取。
* Camera.Parameters.FOCUS_MODE_AUTO 自动对焦模式，摄影小白专用模式；
* Camera.Parameters.FOCUS_MODE_FIXED 固定焦距模式，拍摄老司机模式；
* Camera.Parameters.FOCUS_MODE_EDOF 景深模式，文艺女青年最喜欢的模式；
* Camera.Parameters.FOCUS_MODE_INFINITY 远景模式，拍风景大场面的模式；
* Camera.Parameters.FOCUS_MODE_MACRO 微焦模式，拍摄小花小草小蚂蚁专用模式；

场景模式配置参数，可以通过Parameters.getSceneMode()接口获取。

* Camera.Parameters.SCENE_MODE_BARCODE 扫描条码场景，NextQRCode项目会判断并设置为这个场景；
* Camera.Parameters.SCENE_MODE_ACTION 动作场景，就是抓拍跑得飞快的运动员、汽车等场景用的；
* Camera.Parameters.SCENE_MODE_AUTO 自动选择场景；
* Camera.Parameters.SCENE_MODE_HDR 高动态对比度场景，通常用于拍摄晚霞等明暗分明的照片；
* Camera.Parameters.SCENE_MODE_NIGHT 夜间场景；

#### 开启预览
Camera的预览时通过 SurfaceView 的 SurfaceHolder 进行的，这里需要了解几个很关键的类：
* SurfaceView：用于绘制相机预览图像，提供实时预览的图像。
* SurfaceHolder：用于控制Surface的一个抽象接口，它可以控制Surface的尺寸、格式与像素等，并可以监视Surface的变化。
* SurfaceHolder.Callback：用于监听Surface状态变化的接口。

##### SurfaceView和普通的View区别
Surfaceview是视图（view）的一个继承类，这个视图里内嵌了一个专门用于绘制的Surface。

SurfaceView 是在一个新起的单独线程中可以重新绘制画面，而 View 必须在 UI 线程中更新画面。这样，如果绘图任务繁重，使用普通 View 的 onDraw 方法里面的代码要执行好长一段时间，就可能会造成UI主线程阻塞。而 SurfaceView 的机制是在后台线程执行繁重的绘图任务，把所有绘制的东西缓存起来；绘制完毕后，再回到 UI 线程，一次性把所绘制的东西渲染到屏幕上，实质上就是后台线程绘制，UI主线程渲染。

关于 SurfaceView 后面会专门写一篇博客来介绍。

在SurfaceHolder.Callback接口里定义了三个函数：
* surfaceCreated(SurfaceHolder holder); 当Surface第一次创建的时候调用
* surfaceChanged(SurfaceHolder holder, int format, int width, int height); 当Surface的size、format等发生变化的时候调用
* surfaceDestroyed(SurfaceHolder holder); 当Surface被销毁的时候调用

介绍完上面这些可以写我们用于预览的控件了，下面是我们demo中的预览控件：
```
// 继承自 SurfaceView 并实现 SurfaceHolder.Callback 接口
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
	// camera实例
    private Camera mCamera;
	
	// 自定义View的三个构造函数
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

	/**
	 * 依次为重写的 SurfaceHolder.Callback 的三个函数
	 */ 

	// 在这个函数中一般设置预览界面的一些参数，然后开启预览
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

	// 这个函数中一般用来回收资源
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
```
#### 关闭预览
```
camera.stopPreview();
```
#### 开启闪关灯
```
/**
 * 开关闪光灯
 *
 * @return 闪光灯是否开启
 */
public static boolean switchFlashLight() {
	if (camera != null) {
		Camera.Parameters parameters = camera.getParameters();
		if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF))
		{
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
```
这里需要注意的是严谨的话开启闪光灯之前还需要判断是否具有闪光灯，虽然现在没有手机闪光灯的手机基本上没有了。

#### 开启对焦
```
camera.autoFocus(null);
```

#### 拍照
拍照时通过调用Camera的takePicture()方法来完成的，
```
takePicture(ShutterCallback shutter, PictureCallback raw,
            PictureCallback jpeg)
```
该方法有三个参数：

* ShutterCallback shutter：在拍照的瞬间被回调，这里通常可以播放"咔嚓"这样的拍照音效。
* PictureCallback raw：返回未经压缩的图像数据。
PictureCallback jpeg：返回经过JPEG压缩的图像数据。

我们一般用的就是最后一个，实现最后一个PictureCallback即可，在本demo中主要是在 PictureCallback 中进行图片的剪切。
```java
new Camera.PictureCallback() {
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
}
```

#### 释放资源
```
camera.release();
```
