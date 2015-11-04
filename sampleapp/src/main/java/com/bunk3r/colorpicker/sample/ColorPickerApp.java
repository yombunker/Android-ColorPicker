package com.bunk3r.colorpicker.sample;

import android.app.Application;

import com.bunk3r.colorpicker.perf.ColorPickerReporter;
import com.microsoft.perf.PerfManager;

public class ColorPickerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PerfManager.Properties properties = new PerfManager.Properties.Builder()
                .withReporter(new ColorPickerReporter())
                .autoPublish()
                .build();

        PerfManager.setDefaultProperties(properties);
    }
}