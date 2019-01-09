package com.idcardlibs.camerademo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

public class ScreenUtils {
    private ScreenUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 方法名称: getScreenWidth
     * <br/>方法详述: 获得屏幕宽度
     * <br/>参数: context
     * <br/>返回值:  int Width
     * <br/>异常抛出 Exception:
     * <br/>异常抛出 NullPointerException:
     */

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }
   

    /**
     * 方法名称: getScreenWidth
     * <br/>方法详述: 获得屏幕高度
     * <br/>参数: context
     * <br/>返回值:  int height
     * <br/>异常抛出 Exception:
     * <br/>异常抛出 NullPointerException:
     */

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 方法名称: getStatusHeight
     * <br/>方法详述: 获得状态栏的高度
     * <br/>参数: context
     * <br/>返回值: int
     * <br/>异常抛出 Exception:
     * <br/>异常抛出 NullPointerException:
     */

    public static int getStatusHeight(Context context) {

        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * 方法名称: snapShotWithStatusBar
     * <br/>方法详述: 获取当前屏幕截图，包含状态栏
     * <br/>参数: activity
     * <br/>返回值: bitmap
     * <br/>异常抛出 Exception:
     * <br/>异常抛出 NullPointerException:
     */

    public static Bitmap snapShotWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        view.destroyDrawingCache();
        return bp;

    }

    /**
     * 方法名称: snapShotWithoutStatusBar
     * <br/>方法详述: 获取当前屏幕截图，不包含状态栏
     * <br/>参数: activity
     * <br/>返回值: bitmap
     * <br/>异常抛出 Exception:
     * <br/>异常抛出 NullPointerException:
     */
    public static Bitmap snapShotWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height
                - statusBarHeight);
        view.destroyDrawingCache();
        return bp;

    }

}
