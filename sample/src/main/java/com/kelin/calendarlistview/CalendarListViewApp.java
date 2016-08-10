package com.kelin.calendarlistview;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by kelin on 16-4-12.
 */
public class CalendarListViewApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
