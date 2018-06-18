package com.exam.nklight.carddetection;

import android.app.Application;
import android.content.Context;

/**
 * Created by nk on 5/21/2018.
 */

public class DetectApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
