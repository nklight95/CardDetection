package com.exam.nklight.carddetection;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;
import org.opencv.core.Size;

import java.util.List;

/**
 * Created by nk on 5/21/2018.
 */

public class CustomOpenCVCameraVIew extends JavaCameraView {

    public CustomOpenCVCameraVIew(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        setWillNotDraw(false);
    }

    private Paint linePaint;
    public boolean isFocused = false;


    protected void init() {
        Resources r = this.getResources();
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setAlpha(200);
        linePaint.setStrokeWidth(1);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(r.getColor(R.color.colorPrimary));
        linePaint.setShadowLayer(2, 1, 1, r.getColor(R.color.colorAccent));
    }

    public void focusCamera() {
        if (mCamera != null) {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    isFocused = b;
                }
            });
        }
    }

    public void turnOffTheFlash() {
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(params.FLASH_MODE_OFF);
        mCamera.setParameters(params);
    }

    public void turnOnTheFlash() {
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(params.FLASH_MODE_TORCH);
        mCamera.setParameters(params);
    }

    public void setFlash(boolean isOn) {
        if (isOn) {
            turnOnTheFlash();
        } else {
            turnOffTheFlash();
        }
    }

    @Override
    protected Size calculateCameraFrameSize(List<?> supportedSizes, ListItemAccessor accessor, int surfaceWidth, int surfaceHeight) {
        return super.calculateCameraFrameSize(supportedSizes, accessor, surfaceWidth, surfaceHeight);
    }

    @Override
    protected void deliverAndDrawFrame(CvCameraViewFrame frame) {
        super.deliverAndDrawFrame(frame);
    }

    @Override
    protected boolean connectCamera(int width, int height) {
        return super.connectCamera(width, height);
    }

    @Override
    public void onPreviewFrame(byte[] frame, Camera arg1) {
        super.onPreviewFrame(frame, arg1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        int x = canvas.getHeight() / 2;
//        int y = canvas.getWidth() / 2;
//        canvas.rotate(90);
//        linePaint.setStrokeWidth(5);
//        Point deviceSize = MainActivity.getDeviceSize();
//        canvas.drawRoundRect(10, 20, 1000, 1000, 100, 100, linePaint);

//        Rect rect = new Rect(200,200, 500, 1000);
//        rect.centerX();
//        rect.centerY();
//        canvas.drawRect(rect, linePaint);
    }
}
