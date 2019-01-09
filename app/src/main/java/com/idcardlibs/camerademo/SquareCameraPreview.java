package com.idcardlibs.camerademo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SquareCameraPreview extends SurfaceView {

    public static final String TAG = SquareCameraPreview.class.getSimpleName();
    private static final int INVALID_POINTER_ID = -1;

    private static final int ZOOM_OUT = 0;
    private static final int ZOOM_IN = 1;
    private static final int ZOOM_DELTA = 1;

    private static final int FOCUS_SQR_SIZE = 100;
    private static final int FOCUS_MAX_BOUND = 1000;
    private static final int FOCUS_MIN_BOUND = -FOCUS_MAX_BOUND;

    private static final double ASPECT_RATIO = 3.0 / 4.0;
    private Camera mCamera;

    private float mLastTouchX;
    private float mLastTouchY;

    // For scaling
    private int mMaxZoom;
    private boolean mIsZoomSupported;
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mScaleFactor = 1;
    private ScaleGestureDetector mScaleDetector;

    // For focus
    public boolean mIsFocus;
    public boolean mIsOtherBtn = true;
    private Camera.Area mFocusArea;
    private ArrayList<Camera.Area> mFocusAreas;
    private  Camera.AutoFocusCallback  autoFocusCallback;
    public boolean isPreviewing = false;
    private Context mContext;
    public void setAutoFocusCallback(Camera.AutoFocusCallback autoFocusCallback) {
        this.autoFocusCallback = autoFocusCallback;
    }

    public void setmFocusImageView(FocusImageView mFocusImageView) {
        this.mFocusImageView = mFocusImageView;
    }

    public FocusImageView mFocusImageView;

    public SquareCameraPreview(Context context) {
        super(context);
        init(context);
    }

    public SquareCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SquareCameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @SuppressLint("NewApi")
    private void init(Context context) {
        this.mContext = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mFocusArea = new Camera.Area(new Rect(), 1000);
        mFocusAreas = new ArrayList<Camera.Area>();
        mFocusAreas.add(mFocusArea);
    }

    /**
     * Measure the view and its content to determine the measured width and the
     * measured height
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        setMeasuredDimension(width, height);
    }

    public int getViewWidth() {
        return getWidth();
    }

    public int getViewHeight() {
        return getHeight();
    }

    public void setCamera(Camera camera) {
        mCamera = camera;

        if (camera != null) {
            Camera.Parameters params = camera.getParameters();
            mIsZoomSupported = params.isZoomSupported();
            if (mIsZoomSupported) {
                mMaxZoom = params.getMaxZoom();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                mIsFocus = true;

                mLastTouchX = event.getX();
                mLastTouchY = event.getY();

                mActivePointerId = event.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_UP: {
                Point point=new Point((int)event.getX(),(int)event.getY());
                if (mIsFocus) {
                    if (mIsOtherBtn) {
                        handleFocus(mCamera.getParameters(), point);
                    }

                }
                mFocusImageView.startFocus(point);
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (mCamera != null && this.getVisibility() == VISIBLE && isPreviewing) {
                    mCamera.cancelAutoFocus();
                }
                mIsFocus = false;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
        }

        return true;
    }

    private void handleZoom(Camera.Parameters params) {
        int zoom = params.getZoom();
        if (mScaleFactor == ZOOM_IN) {
            if (zoom < mMaxZoom) zoom += ZOOM_DELTA;
        } else if (mScaleFactor == ZOOM_OUT) {
            if (zoom > 0) zoom -= ZOOM_DELTA;
        }
        params.setZoom(zoom);
        mCamera.setParameters(params);
    }

    @SuppressLint("NewApi")
    private void handleFocus(Camera.Parameters params, Point point) {
        float x = mLastTouchX;
        float y = mLastTouchY;
        Camera.Parameters parameters=mCamera.getParameters();
        if (parameters.getMaxNumFocusAreas()<=0) {
            mCamera.autoFocus(autoFocusCallback);
            return;
        }
        mCamera.cancelAutoFocus();
        List<Camera.Area> areas=new ArrayList<Camera.Area>();
        List<Camera.Area> areasMetrix=new ArrayList<Camera.Area>();
        Camera.Size previewSize = parameters.getPreviewSize();
        Rect focusRect = calculateTapArea(point.x, point.y, 1.0f, previewSize);
        Rect metrixRect = calculateTapArea(point.x, point.y, 1.5f, previewSize);

        areas.add(new Camera.Area(focusRect, 1000));
        areasMetrix.add(new Camera.Area(metrixRect,1000));
        parameters.setMeteringAreas(areasMetrix);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setFocusAreas(areas);
        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        try {
            mCamera.setParameters(parameters);
            mCamera.autoFocus(autoFocusCallback);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
    //2018.9.20 点击区域聚焦，计算区域位置
    //这里设置的横屏，拿到屏幕的宽高和点击位置，计算矩形位置坐标，
    //相机参数设置MeteringAreas时，屏幕坐标和原始坐标不同，分四象限
    private Rect calculateTapArea(float x, float y, float coefficient, Camera.Size previewSize) {
        Display display=((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int screenHeight=display.getHeight();
        int screenWidth=display.getWidth();
        float focusAreaSize = 200;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerY =0;
        int  centerX=0;
        centerY = (int) (y / screenHeight*2000 - 1000);
        centerX= (int) (x / screenWidth*2000 - 1000);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }
    
    @SuppressLint("NewApi")
    private boolean setFocusBound(float x, float y) {
        int left = (int) (x - FOCUS_SQR_SIZE / 2);
        int right = (int) (x + FOCUS_SQR_SIZE / 2);
        int top = (int) (y - FOCUS_SQR_SIZE / 2);
        int bottom = (int) (y + FOCUS_SQR_SIZE / 2);

        //        if (FOCUS_MIN_BOUND > left || left > FOCUS_MAX_BOUND) return false;
        //        if (FOCUS_MIN_BOUND > right || right > FOCUS_MAX_BOUND) return false;
        //        if (FOCUS_MIN_BOUND > top || top > FOCUS_MAX_BOUND) return false;
        //        if (FOCUS_MIN_BOUND > bottom || bottom > FOCUS_MAX_BOUND) return false;

                if (FOCUS_MIN_BOUND > left)
                    left = 0;
                if (left > FOCUS_MAX_BOUND)
                    left = FOCUS_MAX_BOUND - 75;
                if (FOCUS_MIN_BOUND > right)
                    right = 75;
                if (right > FOCUS_MAX_BOUND)
                    right = FOCUS_MAX_BOUND;
                if (FOCUS_MIN_BOUND > top)
                    top = 0;
                if (top > FOCUS_MAX_BOUND)
                    top = FOCUS_MAX_BOUND - 75;
                if (FOCUS_MIN_BOUND > bottom)
                    bottom = 75;
                if (bottom > FOCUS_MAX_BOUND)
                    bottom = FOCUS_MAX_BOUND;

        mFocusArea.rect.set(left, top, right, bottom);

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor = (int) detector.getScaleFactor();
            if (mCamera != null && isPreviewing) {
                handleZoom(mCamera.getParameters());
            }
            return true;
        }
    }
}
