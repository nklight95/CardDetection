package com.exam.nklight.carddetection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.exam.nklight.carddetection.Handler.DetectedRectangle;
import com.exam.nklight.carddetection.Handler.RectangleRecognizeHandler;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResultActivity extends AppCompatActivity implements RectangleRecognizeHandler.ResultListener {

    Button btn;
    ImageView imageView;
    Bitmap original;
    Mat receivedMat;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = null;
    private RectangleRecognizeHandler mCustomHandler = null;
    private ProgressDialog progressDialog;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        btn = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView);
        progressDialog = new ProgressDialog(ResultActivity.this);
        progressDialog.setMessage("Processing");
        progressDialog.setCanceledOnTouchOutside(false);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        excute();
//                    }
//                });
                setProgressDialog(true);
                usingHandler();
            }
        });
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        //

//        try {
//            receivedMat = (Mat) getIntent().getExtras().getSerializable("crop");
//        } catch (Exception e) {
//            receivedMat = null;
//        }
//        if (receivedMat != null) {
//            Utils.matToBitmap(receivedMat, original);
//            imageView.setImageBitmap(original);
//        } else {
//            choosePhoto();
//        }
        byte[] byteArray = getIntent().getByteArrayExtra("crop");
        Bitmap result = null;
        if (byteArray != null) {
            result = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            original = result;
            imageView.setImageBitmap(result);
            btn.setVisibility(View.GONE);
        } else {
            choosePhoto();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void setProgressDialog(final boolean isShow) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isShow) {
                    progressDialog.show();
                } else {
                    progressDialog.hide();
                }
            }
        });
    }

    private void choosePhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 33);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 33 && resultCode == RESULT_OK && data != null) {
            imageView.setImageURI(data.getData());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        original = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Toast.makeText(ResultActivity.this, "fail", Toast.LENGTH_SHORT).show();
        }
    }

    private void usingHandler() {
        mCustomHandler = new RectangleRecognizeHandler(mHandlerThread.getLooper(), ResultActivity.this, ResultActivity.this);
        mCustomHandler.run(original);
    }

    private void excute() {
//        Mat originalMat = new Mat();
//        Mat newMat = new Mat();
//        Utils.bitmapToMat(original, originalMat);
//        try {
//            newMat = findRectangle(originalMat);
//            final Bitmap bitmap = Bitmap.createBitmap(originalMat.width(), originalMat.height(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(newMat, bitmap);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    imageView.setImageBitmap(bitmap);
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //
//        Bitmap result = null;
//        try {
//            result = findRectangle(original);
//        } catch (Exception e) {
//            Toast.makeText(ResultActivity.this, "cant find", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//            return;
//        }
//        final Bitmap finalResult = result;
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                imageView.setImageBitmap(finalResult);
//            }
//        });
        //
        Mat a = new Mat();
        Utils.bitmapToMat(original, a);
//        find(a);
        scaleDownT(a);
    }

    private void scaleDown(Mat src) {
        List<Point> points = new ArrayList<>();
        MatOfPoint2f approxCurve;
        double maxArea = 0;
        Mat frame = src.clone();
        List<MatOfPoint> cor = new ArrayList<>();
        Mat blur = new Mat(src.size(), CvType.CV_8U);
        Mat gray = new Mat(src.size(), CvType.CV_8U);
        Mat adaptive = new Mat(src.size(), CvType.CV_8U);
        Imgproc.medianBlur(src, blur, 9);
        Imgproc.cvtColor(blur, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(gray, adaptive, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY,
                15, 40);
        Imgproc.Canny(adaptive, adaptive, 0, 255);
        Imgproc.findContours(adaptive, cor, new Mat(),
                Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        for (MatOfPoint contour : cor) {
            MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());

            double area = Imgproc.contourArea(contour);
            approxCurve = new MatOfPoint2f();
            Imgproc.approxPolyDP(temp, approxCurve,
                    Imgproc.arcLength(temp, true) * 0.02, true);

            if (approxCurve.total() == 4) {
                double maxCosine = 0;
                List<org.opencv.core.Point> curves = approxCurve.toList();
                for (int j = 2; j < 5; j++) {
                    double cosine = Math.abs(angleT(curves.get(j % 4),
                            curves.get(j - 2), curves.get(j - 1)));
                    maxCosine = Math.max(maxCosine, cosine);
                }
                if (maxCosine < 0.3) {
                    if (maxArea < area) {
                        maxArea = area;
                        points = curves;
                    }
                }
            }
        }
        if (points.isEmpty()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ResultActivity.this, "cant find any card", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        for (int i = 0; i < 4; i++) {
            try {
                if (i == 3) {
                    Imgproc.line(frame, points.get(i), points.get(0), new Scalar(255, 0, 0), 5);
                } else {
                    Imgproc.line(frame, points.get(i), points.get(i + 1), new Scalar(255, 0, 0), 5);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.d("draw", "out of bound");
            }
        }
        final Bitmap bitmap = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame, bitmap);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    private void harrisCorner(Mat src) {
        Mat gray = new Mat();
        Mat result = src.clone();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cornerHarris(gray, result, 2, 3, 0.04);
        final Bitmap bitmap = original.copy(original.getConfig(), false);
        Utils.matToBitmap(result, bitmap);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
        });
        mHandler.removeCallbacksAndMessages(null);
    }

    private void find(Mat src) {
        Mat blurred = src.clone();
        Imgproc.medianBlur(src, blurred, 9);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

        List<MatOfPoint> contours = new ArrayList<>();

        List<Mat> blurredChannel = new ArrayList<>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<>();
        gray0Channel.add(gray0);

        MatOfPoint2f approxCurve;
        double maxArea = 0;
        int maxId = -1;
        List<org.opencv.core.Point> maxCurrves = new ArrayList<>();

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
                            (src.width() + src.height()) / 200, t);
                }

                Imgproc.findContours(gray, contours, new Mat(),
                        Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                for (MatOfPoint contour : contours) {
                    MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());

                    double area = Imgproc.contourArea(contour);
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(temp, approxCurve,
                            Imgproc.arcLength(temp, true) * 0.02, true);

                    if (approxCurve.total() == 4 && area >= maxArea) {
                        double maxCosine = 0;
                        List<org.opencv.core.Point> curves = approxCurve.toList();
                        for (int j = 2; j < 5; j++) {
                            double cosine = Math.abs(angleT(curves.get(j % 4),
                                    curves.get(j - 2), curves.get(j - 1)));
                            maxCosine = Math.max(maxCosine, cosine);
                        }
                        if (maxCosine < 0.3) {
                            if (maxArea < area) {
                                maxArea = area;
                                maxId = contours.indexOf(contour);
                                maxCurrves = curves;
                            }
                        }
                    }
                }
            }
        }

        if (maxId >= 0) {
            Mat frame = src.clone();
            for (int i = 0; i < 4; i++) {
                try {
                    if (i == 3) {
                        Imgproc.line(frame, maxCurrves.get(i), maxCurrves.get(0), new Scalar(255, 0, 0), 5);
                    } else {
                        Imgproc.line(frame, maxCurrves.get(i), maxCurrves.get(i + 1), new Scalar(255, 0, 0), 5);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.d("draw", "out of bound");
                }
            }
            Log.d("draw==", "draw max area : " + maxArea + " == ");
            final Bitmap bitmap = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(frame, bitmap);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });

        }
    }

    private void scaleDownT(Mat src) {
        int downTime = 4;
//        Mat downScaled = new Mat();
//        Imgproc.pyrDown(src, downScaled);

        Mat blurred = src.clone();

        Imgproc.medianBlur(src, blurred, 9);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

        List<MatOfPoint> contours = new ArrayList<>();

        List<Mat> blurredChannel = new ArrayList<>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<>();
        gray0Channel.add(gray0);


        MatOfPoint2f approxCurve;
        double maxArea = 0;
        int maxId = -1;
        List<org.opencv.core.Point> maxCurrves = new ArrayList<>();

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
                            (src.width() + src.height()) / 200, t);
                }


                Imgproc.findContours(gray, contours, new Mat(),
                        Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                for (MatOfPoint contour : contours) {
                    MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());

                    double area = Imgproc.contourArea(contour);
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(temp, approxCurve,
                            Imgproc.arcLength(temp, true) * 0.02, true);

                    if (approxCurve.total() == 4 && area >= maxArea) {
                        double maxCosine = 0;
                        List<org.opencv.core.Point> curves = approxCurve.toList();
                        for (int j = 2; j < 5; j++) {
                            double cosine = Math.abs(angleT(curves.get(j % 4),
                                    curves.get(j - 2), curves.get(j - 1)));
                            maxCosine = Math.max(maxCosine, cosine);
                        }
                        if (maxCosine < 0.3) {
                            if (maxArea < area) {
                                maxArea = area;
                                maxId = contours.indexOf(contour);
                                maxCurrves = curves;
                            }
                        }
                    }
                }
            }
        }
        if (maxId >= 0) {
//            Mat frame = src.clone();
            Mat frame = crop(src, maxCurrves);
//            Mat frame = Imgcodecs.imdecode(img, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
//            for (int i = 0; i < 4; i++) {
//                try {
//                    if (i == 3) {
//                        Imgproc.line(frame, maxCurrves.get(i), maxCurrves.get(0), new Scalar(255, 0, 0), 5);
//                    } else {
//                        Imgproc.line(frame, maxCurrves.get(i), maxCurrves.get(i + 1), new Scalar(255, 0, 0), 5);
//                    }
//                } catch (ArrayIndexOutOfBoundsException e) {
//                    Log.d("draw", "out of bound");
//                }
//            }
//            Imgproc.pyrUp(crop, frame);
            Log.d("draw==", "draw max area : " + maxArea + " == ");
            final Bitmap bitmap = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(frame, bitmap);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });

        }
    }

    private double angleT(org.opencv.core.Point p1, org.opencv.core.Point p2, org.opencv.core.Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (dx1 * dx2 + dy1 * dy2)
                / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)
                + 1e-10);
    }

    private static Bitmap findRectangle(Bitmap image) throws Exception {
        Mat tempor = new Mat();
        Mat src = new Mat();
        Utils.bitmapToMat(image, tempor);

        Imgproc.cvtColor(tempor, src, Imgproc.COLOR_BGR2RGB);

        Mat blurred = src.clone();
        Imgproc.medianBlur(src, blurred, 9);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        List<Mat> blurredChannel = new ArrayList<Mat>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<Mat>();
        gray0Channel.add(gray0);

        MatOfPoint2f approxCurve;

        double maxArea = 0;
        int maxId = -1;

        for (int c = 0; c < 3; c++) {
            int ch[] = {c, 0};
            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));

            int thresholdLevel = 1;
            for (int t = 0; t < thresholdLevel; t++) {
                if (t == 0) {
                    Imgproc.Canny(gray0, gray, 10, 20, 3, true); // true ?
                    Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1), 1); // 1
                    // ?
                } else {
                    Imgproc.adaptiveThreshold(gray0, gray, thresholdLevel,
                            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                            Imgproc.THRESH_BINARY,
                            (src.width() + src.height()) / 200, t);
                }

                Imgproc.findContours(gray, contours, new Mat(),
                        Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                for (MatOfPoint contour : contours) {
                    MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());

                    double area = Imgproc.contourArea(contour);
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(temp, approxCurve,
                            Imgproc.arcLength(temp, true) * 0.02, true);

                    if (approxCurve.total() == 4 && area >= maxArea) {
                        double maxCosine = 0;

                        List<Point> curves = approxCurve.toList();
                        for (int j = 2; j < 5; j++) {

                            double cosine = Math.abs(angle(curves.get(j % 4),
                                    curves.get(j - 2), curves.get(j - 1)));
                            maxCosine = Math.max(maxCosine, cosine);
                        }

                        if (maxCosine < 0.3) {
                            maxArea = area;
                            maxId = contours.indexOf(contour);
                        }
                    }
                }
            }
        }

        if (maxId >= 0) {
            Rect rect = Imgproc.boundingRect(contours.get(maxId));

            Imgproc.rectangle(src, rect.tl(), rect.br(), new Scalar(255, 0, 0,
                    .8), 4);

        }

        Bitmap bmp;
        bmp = Bitmap.createBitmap(src.cols(), src.rows(),
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, bmp);


        return bmp;

    }

    private static Mat findRectangle(Mat result) throws Exception {

        Mat src = new Mat();

        Imgproc.cvtColor(result, src, Imgproc.COLOR_BGR2RGB);

        Mat blurred = src.clone();
        Imgproc.medianBlur(src, blurred, 9);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        List<Mat> blurredChannel = new ArrayList<Mat>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<Mat>();
        gray0Channel.add(gray0);

        MatOfPoint2f approxCurve;

        double maxArea = 0;
        int maxId = -1;

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
                            (src.width() + src.height()) / 200, t);
                }

                Imgproc.findContours(gray, contours, new Mat(),
                        Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                for (MatOfPoint contour : contours) {
                    MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());

                    double area = Imgproc.contourArea(contour);
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(temp, approxCurve,
                            Imgproc.arcLength(temp, true) * 0.02, true);

                    if (approxCurve.total() == 4 && area >= maxArea) {
                        double maxCosine = 0;

                        List<org.opencv.core.Point> curves = approxCurve.toList();
                        for (int j = 2; j < 5; j++) {

                            double cosine = Math.abs(angle(curves.get(j % 4),
                                    curves.get(j - 2), curves.get(j - 1)));
                            maxCosine = Math.max(maxCosine, cosine);
                        }

                        if (maxCosine < 0.3) {
                            maxArea = area;
                            maxId = contours.indexOf(contour);
                        }
                    }
                }
            }
        }

        if (maxId >= 0) {
            Rect rect = Imgproc.boundingRect(contours.get(maxId));

            Imgproc.rectangle(src, rect.tl(), rect.br(), new Scalar(255, 0, 0,
                    .8), 4);


            int mDetectedWidth = rect.width;
            int mDetectedHeight = rect.height;

            Log.d("draw", "Rectangle width :" + mDetectedWidth + " Rectangle height :" + mDetectedHeight);

        }

        return src;

    }

    private static double angle(org.opencv.core.Point p1, org.opencv.core.Point p2, org.opencv.core.Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (dx1 * dx2 + dy1 * dy2)
                / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)
                + 1e-10);
    }

    /***
     *
     * @param src
     * @param rect
     * @return
     */
    private Mat crop(Mat src, Rect rect) {
        return new Mat(src, rect);
    }

    private Mat crop(Mat src, List<Point> list) {
        Point[] pts = new Point[list.size()];
        pts = list.toArray(pts);
        return fourPointTransform(src, sortPoints(pts));
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

    private Mat fourPointTransform(Mat src, Point[] pts) {

        double ratio = 1;
//        int height = Double.valueOf(src.size().height / ratio).intValue();
//        int width = Double.valueOf(src.size().width / ratio).intValue();

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

        Mat customSize = new Mat(Double.valueOf(76.2).intValue(), Double.valueOf(63.5).intValue(), CvType.CV_8UC4);

        Mat doc = new Mat(maxHeight, maxWidth, CvType.CV_8UC4);

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        src_mat.put(0, 0, tl.x * ratio, tl.y * ratio, tr.x * ratio, tr.y * ratio, br.x * ratio, br.y * ratio, bl.x * ratio, bl.y * ratio);
        dst_mat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh);

        Mat m = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Imgproc.warpPerspective(src, doc, m, doc.size());


        return doc;
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

    private void showBitmap(Mat mat) {
        final Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    @Override
    public void onComplete(final boolean hasResult, final DetectedRectangle result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setProgressDialog(false);
                if (hasResult) {
                    Toast.makeText(ResultActivity.this, "success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ResultActivity.this, "no result", Toast.LENGTH_SHORT).show();
                }
            }
        });
       if (hasResult) {
           mHandler.post(new Runnable() {
               @Override
               public void run() {
                   showBitmap(result.src);
               }
           });
       }
    }
}
