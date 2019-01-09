package com.idcardlibs.camerademo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PictureCallback {

    private SquareCameraPreview surfaceView;
    private Button btnTake;
    private CheckBox lightTogBtn; // 是否 闪光灯
    private CheckBox autoFocusTogBtn; // 是否 自动对焦
    private FocusImageView mFocusImageView;
    private CheckBox light_switch;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private int cameraPosition = 0;
    private int PICTURE_SIZE_MAX_WIDTH;  //照片最大宽度
    private int PICTURE_SIZE_MAX_HIGHT;  //照片最大宽度
    private boolean isPreviewing = true; // 是否 正在预览
    private int cameraCount;
    private CameraOrientationListener mOrientationListener;
    private int rotationCamera;
    private int displayDegree;; //屏幕的选择角度
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        PICTURE_SIZE_MAX_WIDTH = ScreenUtils.getScreenWidth(this);// 以屏幕宽度来设置照片宽度
        PICTURE_SIZE_MAX_HIGHT = ScreenUtils.getScreenHeight(this);// 以屏幕宽度来设置照片宽度

        surfaceView = (SquareCameraPreview) findViewById(R.id.surfaceview);
        lightTogBtn = (CheckBox) findViewById(R.id.btn_flash);
        autoFocusTogBtn = (CheckBox) findViewById(R.id.btn_autoFocus);
        btnTake = (Button) findViewById(R.id.btn_take);
        mFocusImageView = (FocusImageView) findViewById(R.id.focusImageView);
        light_switch = (CheckBox) findViewById(R.id.light_switch);

        mOrientationListener = new CameraOrientationListener(this);
        mOrientationListener.enable();
        init();

        lightTogBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setFlashModeLight(isChecked);
            }
        });
        light_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 setFlashModeTorch(isChecked);
            }


        });
        autoFocusTogBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setFocusStatus(isChecked);

            }
        });
        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPreviewing = false;
                mOrientationListener.rememberOrientation();
                try {
                    //系统返回照片数据
                    camera.takePicture(null, null, null, MainActivity.this);
                } catch (Exception e) {

                }
            }
        });

    }

    private void init() {
        surfaceHolder = surfaceView.getHolder();
        cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
        surfaceView.setmFocusImageView(mFocusImageView);
        surfaceView.setAutoFocusCallback(autoFocusCallback);

    }

    private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //聚焦之后根据结果修改图片
            if (success) {
                surfaceView.mFocusImageView.onFocusSuccess();
            } else {
                //聚焦失败显示的图片，由于未找到合适的资源，这里仍显示同一张图片
                surfaceView.mFocusImageView.onFocusFailed();
            }
        }
    };

    /**
     * 设置闪光灯
     *
     * @param b
     */
    public void setFlashModeLight(boolean b) {
        //判断手机是否支持闪光，支持就显示闪光调节，不支持隐藏闪光调节。
        Camera.Parameters parameters = camera.getParameters();
        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(this, "你的手机不支持闪光灯！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (b) {//打开闪光灯
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);

        } else {//关闭闪光灯
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        camera.setParameters(parameters);
    }
    /**
     * 设置手电筒
     *
     * @param isChecked
     */
    public void setFlashModeTorch(boolean isChecked) {
        Camera.Parameters parameters = camera.getParameters();
        //判断手机是否支持闪光，支持就显示闪光调节，不支持隐藏闪光调节。
        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(this, "你的设备不支持手电筒！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isChecked) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        } else {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        camera.setParameters(parameters);
    }
    /**
     * 设置聚焦状态
     *
     * @param b
     */
    private void setFocusStatus(boolean b) {
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) && b) {
            parameters
                    .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//照连续聚焦
        } else if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters
                    .setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);//自动焦点
        }
        camera.setParameters(parameters);
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (cameraPosition == 0) {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            } else {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }

            surfaceView.setCamera(camera);
            camera.setPreviewDisplay(holder); // 设置 holder
        } catch (Exception e) {
            displayFrameworkBugMessageAndExit();
            camera = null;
        }
    }
    /**
     * 未打开相机提示信息
     */
    private void displayFrameworkBugMessageAndExit() {
        new AlertDialog.Builder(this).setTitle("提示")
                .setMessage("打开相机失败，请确认权限以及相机是否可用！")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        setPreview(false);
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setPreview(true);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        setPreview(false);
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**
     * 设置 相机 预览状态
     *
     * @param is
     */
    private void setPreview(boolean is) {
        if (is) {
            if (!isPreviewing) {
                // goubaihu 添加setupCamera() 12.9
                setupCamera();
                camera.startPreview();
                isPreviewing = true;
                surfaceView.isPreviewing = isPreviewing;
            }

        } else {
            if (isPreviewing && camera != null)
                camera.stopPreview();
            isPreviewing = false;
            surfaceView.isPreviewing = isPreviewing;
        }
    }

    /**
     * 设置相机参数
     */
    private void setupCamera() {
        // 如果是前摄像头，不设置参数
        if (cameraPosition == 1) {
            return;
        }
        // Never keep a global parameters
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        //设置白平衡
        try {
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

            try {
                String[] isoves = getAllISOValuses(parameters);
                if (null != isoves) {
                    for (int i = 0; i < isoves.length; i++) {
                        if ("auto".equals(isoves[i])) {
                            parameters.set("iso", (String) isoves[i]);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("e", e.toString());
            }
        } catch (Exception e) {
            Toast.makeText(this, "平衡模式设置异常", Toast.LENGTH_SHORT).show();
        }
        // 由于界面是竖屏显示  所以需要旋转预览图像  这里设置的后置摄像头
        setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, camera);
        // 先找最合适的照片尺寸
        // 根据屏幕长宽比
        if (Build.VERSION.SDK_INT >= 24) {//7.0版本
            List<Camera.Size> preSizes = parameters.getSupportedPreviewSizes();
            Camera.Size bestPreviewSize = CustomCameraSize.getInstance().getPreviewSize(preSizes,
                    PICTURE_SIZE_MAX_WIDTH, PICTURE_SIZE_MAX_HIGHT);
            if (bestPreviewSize != null) {
                parameters.setPreviewSize(bestPreviewSize.width,
                        bestPreviewSize.height);
            }
            List<Camera.Size> picSizes = parameters.getSupportedPictureSizes();
            Camera.Size bestPicSize = CustomCameraSize.getInstance().getPictureSize(picSizes,
                    PICTURE_SIZE_MAX_WIDTH, PICTURE_SIZE_MAX_HIGHT);
            if (bestPicSize != null) {
                parameters.setPictureSize(bestPicSize.width,
                        bestPicSize.height);
            }
            findViewById(R.id.all).postInvalidate();
        } else {
            CustomCameraSize.getInstance().getPictureAndPreViewSize(parameters,
                    PICTURE_SIZE_MAX_WIDTH, PICTURE_SIZE_MAX_HIGHT);
            List<Camera.Size> preSizes = parameters.getSupportedPreviewSizes();
            Camera.Size bestPreviewSize = CustomCameraSize.getInstance().getPreviewSize(preSizes,
                    PICTURE_SIZE_MAX_WIDTH, PICTURE_SIZE_MAX_HIGHT);
            if (bestPreviewSize != null)
                parameters.setPreviewSize(bestPreviewSize.width,
                        bestPreviewSize.height);
            parameters.setPictureSize(parameters.getPreviewSize().width,
                    parameters.getPreviewSize().height);
        }
        // 判断是否勾选中聚焦
        if (autoFocusTogBtn.isChecked()) {
            // Set continuous picture focus, if it's supported
            if (parameters.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters
                        .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 照连续聚焦
            }
        } else {
            if (parameters.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);// 自动焦点
            }
        }
        if (lightTogBtn.isChecked()) {
            if (parameters.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_ON)) {
                String flashMode = parameters.getFlashMode();
                if (flashMode.equals("off") || flashMode.equals("auto")) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                }
            }
        }
        if (light_switch.isChecked()) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }
        camera.setParameters(parameters);
    }

    /**
     * Android API: Display Orientation Setting
     * Just change screen display orientation,
     * the rawFrame data never be changed.
     */
    private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation(); // 获取屏幕旋转的角度
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayDegree = (info.orientation + degrees) % 360;
            displayDegree = (360 - displayDegree) % 360;  // compensate the mirror
        } else {
            displayDegree = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(displayDegree);
    }

    /**
     * 得到相机的感光度（ISO）
     * @param param
     * @return
     */
    public String[] getAllISOValuses(Camera.Parameters param) {
        String flat = param.flatten();
        String[] isoValues = null;
        String values_keyword = null;
        String iso_keyword = null;
        if (flat.contains("iso-values")) {
            // most used keywords
            values_keyword = "iso-values";
            iso_keyword = "iso";
        } else if (flat.contains("iso-mode-values")) {
            // google galaxy nexus keywords
            values_keyword = "iso-mode-values";
            iso_keyword = "iso";
        } else if (flat.contains("iso-speed-values")) {
            // micromax a101 keywords
            values_keyword = "iso-speed-values";
            iso_keyword = "iso-speed";
        } else if (flat.contains("nv-picture-iso-values")) {
            // LG dual p990 keywords
            values_keyword = "nv-picture-iso-values";
            iso_keyword = "nv-picture-iso";
        }
        // add other eventual keywords here...
        if (iso_keyword != null) {
            // flatten contains the iso key!!
            String iso = flat.substring(flat.indexOf(values_keyword));
            iso = iso.substring(iso.indexOf("=") + 1);
            if (iso.contains(";")) iso = iso.substring(0, iso.indexOf(";"));
            isoValues = iso.split(",");
        } else {
            isoValues = null; // iso not supported in a known way
        }
        return isoValues;
    }
    
    @Override
    public void onPause() {
        mOrientationListener.disable();
        super.onPause();
    }
    

    @Override
    public void onDestroy() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        System.gc();
        super.onDestroy();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        setFlashModeLight(false);
        rotationCamera = mOrientationListener.getRememberedNormalOrientation();
        Bitmap bitmap = decodeSampledBitmapFromByte(this, data);
        rotateImage(displayDegree, bitmap);
        
    }

    public Bitmap decodeSampledBitmapFromByte(Context context,
                                                     byte[] bitmapBytes) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inMutable = true;
        options.inBitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0,
                bitmapBytes.length, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, ScreenUtils.getScreenWidth(this),
                ScreenUtils.getScreenHeight(this));
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false; // If set to true, the decoder will
        options.inPurgeable = true; // Tell to gc that whether it needs free
        options.inInputShareable = true; // Which kind of reference will be used

        return BitmapFactory.decodeByteArray(bitmapBytes, 0,
                bitmapBytes.length, options);
    }

    private  Bitmap rotateImage(int angle,Bitmap bitmap) {
        //旋转图片
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片  
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }
    
    /**
     *  压缩图片
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int initialInSampleSize = computeInitialSampleSize(options, reqWidth, reqHeight);
        int roundedInSampleSize;
        if (initialInSampleSize <= 4) {
            roundedInSampleSize=initialInSampleSize;
        } else {
            roundedInSampleSize = (initialInSampleSize + 3) / 4 * 2;
        }
        return roundedInSampleSize;
    }

    /**
     * 方法名称: calculateInSampleSize
     * <br/>方法详述: 智能计算压缩值
     * <br/>参数:
     * <br/>返回值:  得到智能计算压缩值
     * <br/>异常抛出 Exception:
     * <br/>异常抛出 NullPointerException:
     */
    private static int computeInitialSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final double height = options.outHeight;
        final double width = options.outWidth;

        final long maxNumOfPixels = reqWidth * reqHeight;
        final int minSideLength = Math.min(reqHeight, reqWidth);

        int lowerBound = (maxNumOfPixels < 0) ? 1 : (int) Math.ceil(Math
                .sqrt(width * height / maxNumOfPixels));
        int upperBound = (minSideLength < 0) ? 128 : (int) Math.min(
                Math.floor(width / minSideLength),
                Math.floor(height / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if (maxNumOfPixels < 0 && minSideLength < 0) {
            return 1;
        } else if (minSideLength < 0) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}
