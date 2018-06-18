package com.exam.nklight.carddetection.Handler;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nk on 5/30/2018.
 */

public class DetectedRectangle implements Serializable {
    public Mat src;
    public List<Point> detectedPoints;
    public Mat resizedMat;

    private List<android.graphics.Point> result = new ArrayList<>();


    public DetectedRectangle(Mat src, List<Point> detectedPoints, Mat downScaled) {
        this.src = src;
        this.detectedPoints = detectedPoints;
        this.resizedMat = downScaled;
    }

    public List<android.graphics.Point> getAndroidGraphicPoint() {
        double ratioW = src.width() / resizedMat.width();
        double ratioH = src.height() / resizedMat.height();
        detectedPoints.clear();
        for (Point point : detectedPoints) {
            int height = Double.valueOf(point.y * ratioH).intValue();
            int width = Double.valueOf(point.x * ratioW).intValue();
            result.add(new android.graphics.Point(width, height));
        }
        return result;
    }
}
