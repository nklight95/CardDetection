package com.exam.nklight.carddetection;

import org.opencv.core.Mat;

/**
 * Created by nk on 5/29/2018.
 */

public interface TakeFrameListener {
    void onTake(Mat mat);
}
