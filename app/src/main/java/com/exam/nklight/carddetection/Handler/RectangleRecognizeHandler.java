package com.exam.nklight.carddetection.Handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by nk on 5/30/2018.
 */

public class RectangleRecognizeHandler extends Handler {
    private static final String TAG = "RRHandler";
    //    private final Handler mUiHandler;
    private final Context mContext;
    private boolean mBugRotate;
    private boolean colorMode = false;
    private boolean filterMode = true;
    private double colorGain = 1.5;       // contrast
    private double colorBias = 0;         // bright
    private int colorThresh = 110;        // threshold
    private Size mPreviewSize;
    private Point[] mPreviewPoints;
    private boolean isProcessing;
    private ResultListener resultListener;

    private static int ratio = 5;
    private static int MINIMUM_AREA = 15000;

    //old


    public interface ResultListener {
        void onComplete(boolean hasResult, DetectedRectangle result);
    }

    public RectangleRecognizeHandler(Looper looper, Context context) {
        super(looper);
//        this.mUiHandler = mUiHandler;
        this.mContext = context;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        mBugRotate = sharedPref.getBoolean("bug_rotate", false);
    }

    public RectangleRecognizeHandler(Looper looper, Context context, ResultListener listener) {
        super(looper);
//        this.mUiHandler = mUiHandler;
        this.mContext = context;
        this.resultListener = listener;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        mBugRotate = sharedPref.getBoolean("bug_rotate", false);
    }

    public void run(Mat inputRGBA) {
        DetectedRectangle detect = detectRectangle(inputRGBA);
        if (resultListener == null) {
            // throw exception
            return;
        }
        if (detect == null) {
            resultListener.onComplete(false, null);
        } else {
            Mat card = crop(detect.resizedMat, detect.detectedPoints);
//            Imgproc.pyrUp(card, detect.src);
            Mat result = new Mat();
            Imgproc.pyrUp(card, result);
            detect.src = result.clone();
            resultListener.onComplete(true, detect);
        }
        isProcessing = false;
    }

    public void run(Bitmap src) {
        isProcessing = true;
        Mat mat = new Mat();
        Utils.bitmapToMat(src, mat);
        run(mat);
    }


    private DetectedRectangle detectRectangle(Mat src) {
        Mat grayImage = null;
        Mat cannedImage = null;
        Mat resizedImage = null;
        double maxArea = 0;
        int maxId = -1;
        List<Point> maxCurves = new ArrayList<>();
        List<MatOfPoint> contours = new ArrayList<>();
        MatOfPoint2f approxCurve;

        int height = Double.valueOf(src.size().height / ratio).intValue();
        int width = Double.valueOf(src.size().width / ratio).intValue();
        Size size = new Size(width, height);

        /***new***/

//        resizedImage = new Mat(size, CvType.CV_8UC4);
//        grayImage = new Mat(size, CvType.CV_8UC4);
//        cannedImage = new Mat(size, CvType.CV_8UC1);
//        Imgproc.resize(src, resizedImage, size);
//        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4);
//        Imgproc.medianBlur(grayImage, grayImage, 9);
//        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);
//        Imgproc.Canny(grayImage, cannedImage, 75, 200);
//
//        colorThresh(cannedImage, colorThresh);
//
//        Imgproc.findContours(cannedImage, contours, new Mat(),
//                Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//        for (MatOfPoint contour : contours) {
//            MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());
//            //calculate area
//            double area = Imgproc.contourArea(contour);
//            approxCurve = new MatOfPoint2f();
//            //highlight the viền
//            Imgproc.approxPolyDP(temp, approxCurve,
//                    Imgproc.arcLength(temp, true) * 0.02, true);
//
//            // có 4 cạnh
//            if (approxCurve.total() == 4 && area >= maxArea) {
//                double maxCosine = 0;
//                List<org.opencv.core.Point> curves = approxCurve.toList();
//                for (int j = 2; j < 5; j++) {
//                    double cosine = Math.abs(angle(curves.get(j % 4),
//                            curves.get(j - 2), curves.get(j - 1)));
//                    maxCosine = Math.max(maxCosine, cosine);
//                }
//                // cos < 0.3 => angle > 72°
//                if (maxCosine < 0.3 && area > MINIMUM_AREA) {
//                    if (maxArea < area) {
//                        maxArea = area;
//                        maxId = contours.indexOf(contour);
//                        maxCurves = curves;
//                    }
//                }
//            }
//        }

        /*** old***/
        Mat resized = new Mat();
        Imgproc.pyrDown(src, resized);
        Mat blurred = resized.clone();
        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U);
        Mat gray = new Mat();
        List<Mat> blurredChannel = new ArrayList<>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<>();
        gray0Channel.add(gray0);


        for (int c = 0; c < 3; c++) {
            int ch[] = {c, 0};
            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));

            int thresholdLevel = 1;
            for (int t = 0; t < thresholdLevel; t++) {
                if (t == 0) {
                    Imgproc.Canny(gray0, gray, 10, 20, 3, true); // true ?
                    Imgproc.dilate(gray, gray, new Mat(), new org.opencv.core.Point(-1, -1), 1); // 1
                    // ?
                } else {
                    Imgproc.adaptiveThreshold(gray0, gray, thresholdLevel,
                            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                            Imgproc.THRESH_BINARY,
                            (resized.width() + resized.height()) / 200, t);
                }

                Imgproc.findContours(gray, contours, new Mat(),
                        Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                for (MatOfPoint contour : contours) {
                    MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());
                    //tính diện tích
                    double area = Imgproc.contourArea(contour);
                    approxCurve = new MatOfPoint2f();
                    //bo viền
                    Imgproc.approxPolyDP(temp, approxCurve,
                            Imgproc.arcLength(temp, true) * 0.02, true);

                    // 4 cạnh
                    if (approxCurve.total() == 4 && area >= maxArea) {

                        double maxCosine = 0;

                        List<org.opencv.core.Point> curves = approxCurve.toList();

                        for (int j = 2; j < 5; j++) {

                            double cosine = Math.abs(angle(curves.get(j % 4),
                                    curves.get(j - 2), curves.get(j - 1)));
                            maxCosine = Math.max(maxCosine, cosine);
                        }

                        if (maxCosine < 0.3 ) {
                            if (maxArea < area) {
                                maxArea = area;
                                maxId = contours.indexOf(contour);
                                maxCurves = curves;
                            }
                        }
                    }
                }
            }
        }
        DetectedRectangle result = null;

        if (maxId >= 0) {

            result = new DetectedRectangle(src, maxCurves, resized);
        }

        return result;
    }

    private double angle(org.opencv.core.Point p1, org.opencv.core.Point p2, org.opencv.core.Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (dx1 * dx2 + dy1 * dy2)
                / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)
                + 1e-10);
    }

    private void colorThresh(Mat src, int threshold) {
        Size srcSize = src.size();
        int size = (int) (srcSize.height * srcSize.width) * 3;
        byte[] d = new byte[size];
        src.get(0, 0, d);

        for (int i = 0; i < size; i += 3) {

            // the "& 0xff" operations are needed to convert the signed byte to double

            // avoid unneeded work
            if ((double) (d[i] & 0xff) == 255) {
                continue;
            }

            double max = Math.max(Math.max((double) (d[i] & 0xff), (double) (d[i + 1] & 0xff)),
                    (double) (d[i + 2] & 0xff));
            double mean = ((double) (d[i] & 0xff) + (double) (d[i + 1] & 0xff)
                    + (double) (d[i + 2] & 0xff)) / 3;

            if (max > threshold && mean < max * 0.8) {
                d[i] = (byte) ((double) (d[i] & 0xff) * 255 / max);
                d[i + 1] = (byte) ((double) (d[i + 1] & 0xff) * 255 / max);
                d[i + 2] = (byte) ((double) (d[i + 2] & 0xff) * 255 / max);
            } else {
                d[i] = d[i + 1] = d[i + 2] = 0;
            }
        }
        src.put(0, 0, d);
    }

    private boolean insideArea(Point[] rp, Size size) {

        int width = Double.valueOf(size.width).intValue();
        int height = Double.valueOf(size.height).intValue();
        int baseMeasure = height / 4;

        int bottomPos = height - baseMeasure;
        int topPos = baseMeasure;
        int leftPos = width / 2 - baseMeasure;
        int rightPos = width / 2 + baseMeasure;

        return (
                rp[0].x <= leftPos && rp[0].y <= topPos
                        && rp[1].x >= rightPos && rp[1].y <= topPos
                        && rp[2].x >= rightPos && rp[2].y >= bottomPos
                        && rp[3].x <= leftPos && rp[3].y >= bottomPos

        );
    }

    private Point[] sortPoints(Point[] src) {

        ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(src));

        Point[] result = {null, null, null, null};

        Comparator<Point> sumComparator = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y + lhs.x).compareTo(rhs.y + rhs.x);
            }
        };

        Comparator<Point> diffComparator = new Comparator<Point>() {

            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y - lhs.x).compareTo(rhs.y - rhs.x);
            }
        };

        // top-left corner = minimal sum
        result[0] = Collections.min(srcPoints, sumComparator);

        // bottom-right corner = maximal sum
        result[2] = Collections.max(srcPoints, sumComparator);

        // top-right corner = minimal diference
        result[1] = Collections.min(srcPoints, diffComparator);

        // bottom-left corner = maximal diference
        result[3] = Collections.max(srcPoints, diffComparator);

        return result;
    }

    private Mat crop(Mat src, List<Point> list) {
        Point[] pts = new Point[list.size()];
        pts = list.toArray(pts);
        return fourPointTransform(src, sortPoints(pts));
    }

    private Mat fourPointTransform(Mat src, Point[] pts) {
        double ratio = 1;

        Point tl = pts[0];
        Point tr = pts[1];
        Point br = pts[2];
        Point bl = pts[3];

        double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
        double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));

        double dw = Math.max(widthA, widthB) * ratio;
        int maxWidth = Double.valueOf(dw).intValue();


        double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
        double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));

        double dh = Math.max(heightA, heightB) * ratio;
        int maxHeight = Double.valueOf(dh).intValue();


        Mat doc = new Mat(maxHeight, maxWidth, CvType.CV_8UC4);

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        src_mat.put(0, 0, tl.x * ratio, tl.y * ratio, tr.x * ratio, tr.y * ratio, br.x * ratio, br.y * ratio, bl.x * ratio, bl.y * ratio);
        dst_mat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh);

        Mat m = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Imgproc.warpPerspective(src, doc, m, doc.size());


        return doc;
    }
}
