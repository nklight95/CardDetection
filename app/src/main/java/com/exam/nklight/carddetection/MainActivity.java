package com.exam.nklight.carddetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CustomOpenCVCameraVIew mOpenCvCameraView;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    boolean isprocessing = false;
    boolean justRequestFocus;
    boolean focusDone;
    boolean take;
    boolean detectedCard;
    boolean justCap;
    int mWidth;
    int mHeight;

    private ImageView panel;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = null;
    private ImageView btnFlash;
    boolean isFlashOn;
    private Mat captureMat;
    private Mat cropedMat;
    private Rect downedRect;
    private double lastArea = 1;
    private List<Point> detectedPoint;
    private Mat bw;

    private Bitmap capture;

    public static double MIN_AREA = 130101;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
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
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = findViewById(R.id.camera);
        panel = findViewById(R.id.panel);
        btnFlash = findViewById(R.id.btn_flash);
        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFlashOn) {
                    mOpenCvCameraView.setFlash(false);
                } else {
                    mOpenCvCameraView.setFlash(true);
                }
                isFlashOn = !isFlashOn;
            }
        });
        mOpenCvCameraView.setCvCameraViewListener(this);
        startHandlerThread();

    }

    public void startHandlerThread() {
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }



    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mWidth = width;
        mHeight = height;

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        final Mat frame = inputFrame.rgba();
        //
//        ArrayList<MatOfPoint> data = new ArrayList<>();
//        ArrayList<org.opencv.core.Point> points = new ArrayList<>();
//        points.add(new org.opencv.core.Point(mWidth * 0.25, mHeight * 0.25));
//        points.add(new org.opencv.core.Point(mWidth * 0.25, mHeight * 0.75));
//        points.add(new org.opencv.core.Point(mWidth * 0.75, mHeight * 0.75));
//        points.add(new org.opencv.core.Point(mWidth * 0.75, mHeight * 0.25));
//        MatOfPoint mop = new MatOfPoint();
//        mop.fromList(points);
//        data.add(mop);
//        Imgproc.polylines(frame, data, true, new Scalar(0, 255, 0), 2);
        //
//        for (int i = 0; i < 4; i++) {
//            try {
//                if (i == 3) {
//                    Imgproc.line(frame, points.get(i), points.get(0), new Scalar(255, 0, 0));
//                } else {
//                    Imgproc.line(frame, points.get(i), points.get(i + 1), new Scalar(255, 0, 0));
//                }
//            } catch (ArrayIndexOutOfBoundsException e) {
//                Log.d("draw", "out of bound");
//            }
//        }
//        if (!isprocessing) {
//
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        findRectangleT(frame);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        isprocessing = false;
//                    }
//                }
//            });
////            find(frame);
////            recognize(frame);
//
//        }
//        Mat temp = new Mat();
//        recognize(frame);
        if (take) {
            downCropTake(frame);
            return frame;

        }
        try {
            if (isprocessing) {
                return frame;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        findRectangleT(frame);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


//        Imgproc.rectangle(frame, new org.opencv.core.Point(frame.width()/4, frame.height()/4), new org.opencv.core.Point(frame.width()*0.75, frame.height()*0.75), new Scalar(0,0,255));
        return frame;
    }


    private Mat sida(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // blur => gray => adapt
        Mat gray = null;
        Mat contourImage = new Mat();
        Mat downscaled = new Mat();
        Mat upscaled = new Mat();
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Mat hierarchyOutputVector = new Mat();
        gray = inputFrame.gray();
        Mat dst = inputFrame.rgba();
        Imgproc.pyrDown(gray, downscaled, new Size(gray.cols() / 2, gray.rows() / 2));
        Imgproc.pyrUp(downscaled, upscaled, gray.size());
        Imgproc.Canny(upscaled, bw, 0, 255);
        Imgproc.dilate(bw, bw, new Mat(), new Point(-1, 1), 1);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        contourImage = bw.clone();
        Imgproc.findContours(
                contourImage,
                contours,
                hierarchyOutputVector,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE
        );
        // loop over all found contours
        for (MatOfPoint cnt : contours) {
            MatOfPoint2f curve = new MatOfPoint2f(cnt.toArray());

            // approximates a polygonal curve with the specified precision
            Imgproc.approxPolyDP(
                    curve,
                    approxCurve,
                    0.02 * Imgproc.arcLength(curve, true),
                    true
            );

            int numberVertices = (int) approxCurve.total();
            double contourArea = Imgproc.contourArea(cnt);


            // ignore to small areas
            if (Math.abs(contourArea) < 100
                // || !Imgproc.isContourConvex(
                    ) {
                continue;
            }

            // triangle detection

            // rectangle, pentagon and hexagon detection
            if (numberVertices >= 4 && numberVertices <= 6) {

                List<Double> cos = new ArrayList<>();
                for (int j = 2; j < numberVertices + 1; j++) {
                    cos.add(
                            angle(
                                    approxCurve.toArray()[j % numberVertices],
                                    approxCurve.toArray()[j - 2],
                                    approxCurve.toArray()[j - 1]
                            )
                    );
                }
                Collections.sort(cos);

                double mincos = cos.get(0);
                double maxcos = cos.get(cos.size() - 1);

                // rectangle detection
                if (numberVertices == 4 && mincos >= -0.1 && maxcos <= 0.3) {
                    //TODO draw
                    Rect r = Imgproc.boundingRect(cnt);
                    Point pt = new Point(
                            r.x + ((r.width) / 2),
                            r.y + ((r.height) / 2)
                    );
                    Imgproc.rectangle(
                            dst,
                            new Point(r.x, r.y),
                            new Point(r.x + 10, r.y - 10),
                            new Scalar(255, 255, 255),
                            -1
                    );


                }
            }
            // circle detection


        }

        // return the matrix / image to show on the screen
        return dst;

    }

//    public static android.graphics.Point getDeviceSize() {
//        WindowManager wm = (WindowManager) DetectApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        return size;
//    }

    private Point[] sortPoints( Point[] src ) {

        ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(src));

        Point[] result = { null , null , null , null };

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

    private void find(Mat src) {
        Mat gray = new Mat(src.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY, 4);
        Mat edge = new Mat();
        Mat dst = new Mat();
        Imgproc.Canny(gray, edge, 80, 90);
        Imgproc.cvtColor(edge, dst, Imgproc.COLOR_GRAY2RGBA, 4);
    }

    private void findRectangleT(Mat src) throws Exception {
        Mat down = new Mat();
        Imgproc.pyrDown(src, down);
        Mat blurred = down.clone();
        Imgproc.medianBlur(down, blurred, 9);
        Rect rect = null;

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
                            (down.width() + down.height()) / 200, t);
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
//                        Mat frame = new Mat(src.rows(), src.cols(), CvType.CV_8UC4);
//                        for (int i = 0; i < 4; i++) {
//                            try {
//                                if (i == 3) {
//                                    Imgproc.line(frame, curves.get(i), curves.get(0), new Scalar(255, 0, 0),5);
//                                } else {
//                                    Imgproc.line(frame, curves.get(i), curves.get(i + 1), new Scalar(255, 0, 0),5);
//                                }
//                            } catch (ArrayIndexOutOfBoundsException e) {
//                                Log.d("draw", "out of bound");
//                            }
//                        }
//                        final Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
//                        Utils.matToBitmap(frame, bitmap);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                panel.setImageBitmap(bitmap);
//                            }
//                        });

                        for (int j = 2; j < 5; j++) {

                            double cosine = Math.abs(angleT(curves.get(j % 4),
                                    curves.get(j - 2), curves.get(j - 1)));
                            maxCosine = Math.max(maxCosine, cosine);
                        }

                        if (maxCosine < 0.3 && area > 15000) {
                            if (maxArea < area) {
                                maxArea = area;
                                maxId = contours.indexOf(contour);
                                maxCurrves = curves;
                                rect = Imgproc.boundingRect(contour);
                            }
                        }
                    }
                }
            }
        }

        if (maxId >= 0) {
//            Imgproc.drawContours(src, contours, maxId, new Scalar(0, 255, 0,
//                    .8), 3);
//            final Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(src, bitmap);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    panel.setImageBitmap(bitmap);
//                }
//            });
            // add minimum area later
            if (0.95 < (lastArea / maxArea) && (lastArea / maxArea) < 1.05) {
//                if (detectedCard) {
//                    if (justRequestFocus) {
//                        if (mOpenCvCameraView.isFocused) {
//
//                        } else {
//                            mOpenCvCameraView.focusCamera();
//                            justRequestFocus = true;
//                            mHandler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    justRequestFocus = false;
//                                }
//                            }, 4000);
//                        }
//                    } else {
//                        if (mOpenCvCameraView.isFocused) {
//                            return;
//                        }
//                    }
//                }

                if (detectedCard) {
                    if (!mOpenCvCameraView.isFocused) {
                        if (justRequestFocus) {
                            return;
                        } else {
                            mOpenCvCameraView.focusCamera();
                            justRequestFocus = true;
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    justRequestFocus = false;
                                }
                            }, 4000);
                        }
                    } else {
                        if (!focusDone && justRequestFocus) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    focusDone = true;
                                }
                            }, 500);
                            return;
                        }
                    }
                }
//                captureMat = src.clone();
//                downedRect = rect;
//                Mat crop = down.clone();
//                Mat khung = src.clone();
//                Mat croped= crop(crop, maxCurrves);
//                Imgproc.pyrUp(croped, khung);
                //                crop = crop.submat(rect);
//                Mat khung = src.clone();
//                Imgproc.pyrUp(crop, khung);
//                cropedMat = khung.clone();

                if (!detectedCard) {
                    detectedCard = true;
                    return;
                }
                if (!justCap) {
                    justCap = true;
                    detectedPoint = maxCurrves;
                    take = true;
//                    final Bitmap bitmap = Bitmap.createBitmap(khung.width(), khung.height(), Bitmap.Config.ARGB_8888);
//                    Utils.matToBitmap(khung, bitmap);
//                    //
//                    final Intent intent = new Intent(MainActivity.this, ResultActivity.class);
//                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                    byte[] byteArray = stream.toByteArray();
//                    intent.putExtra("crop", byteArray);
//
//                    startActivity(intent);
//                    finish();
                }


                //

                return;
            } else {
                lastArea = maxArea;
            }

            Mat frame = new Mat(down.rows(), down.cols(), CvType.CV_8UC4);
            for (int i = 0; i < 4; i++) {
                try {
                    if (i == 3) {
                        Imgproc.line(frame, maxCurrves.get(i), maxCurrves.get(0), new Scalar(255, 0, 0), 2);
                    } else {
                        Imgproc.line(frame, maxCurrves.get(i), maxCurrves.get(i + 1), new Scalar(255, 0, 0), 2);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.d("draw", "out of bound");
                }
            }
            Mat khung = src.clone();
            Imgproc.pyrUp(frame, khung);
            Log.d("draw==", "draw max area : " + maxArea + " == ");
            final Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(khung, bitmap);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    panel.setImageBitmap(bitmap);
                }
            });

        }
        isprocessing = false;
    }

    private Mat crop(Mat src, List<Point> list) {
        Point[] pts = new Point[list.size()];
        pts = list.toArray(pts);
        return fourPointTransform(src, sortPoints(pts));
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

    private void takeFrame(Mat take) {
//        final Bitmap bitmap = Bitmap.createBitmap(take.width(), take.height(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(take, bitmap);
        //
        final Intent intent = new Intent(MainActivity.this, ResultActivity.class);
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//        byte[] byteArray = stream.toByteArray();

        intent.putExtra("crop", take);

        startActivity(intent);
        finish();
    }

    private void downCropTake(Mat mat) {
        Mat process = mat.clone();
        Mat down = new Mat();
        Imgproc.pyrDown(mat, down);
        Mat crop = crop(down, detectedPoint);
        Imgproc.pyrUp(crop, process);
        final Bitmap bitmap = Bitmap.createBitmap(process.width(), process.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(process, bitmap);
        //
        final Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        intent.putExtra("crop", byteArray);

        startActivity(intent);
        finish();
    }


    private Mat recognize(Mat source) {
        // Copy preview data to a new Mat element
        /*
         * Mat mYuv = new Mat(frameHeight + frameHeight / 2, frameWidth,
		 * CvType.CV_8UC1); mYuv.put(0, 0, data);
		 */
        // height should be 1.5 of original height
        Mat mYuv = source.clone();

        // Convert preview frame to rgba color space
        final Mat mRgba = new Mat();
        Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV2BGR_NV12, 4);
        Mat result = new Mat();
        try {
            result = findRectangle(mYuv);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (result != null) {
            return result;
        }

        return result;
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

    private void shortDetect(Mat src) {
        Mat thr, gray, blur = new Mat();
        Imgproc.medianBlur(src, blur, 8);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
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
        int baseMeasure = height/4;

        int bottomPos = height-baseMeasure;
        int topPos = baseMeasure;
        int leftPos = width/2-baseMeasure;
        int rightPos = width/2+baseMeasure;

        return (
                rp[0].x <= leftPos && rp[0].y <= topPos
                        && rp[1].x >= rightPos && rp[1].y <= topPos
                        && rp[2].x >= rightPos && rp[2].y >= bottomPos
                        && rp[3].x <= leftPos && rp[3].y >= bottomPos

        );
    }
}

