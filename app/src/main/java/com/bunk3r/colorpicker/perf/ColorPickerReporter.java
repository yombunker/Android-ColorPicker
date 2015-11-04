package com.bunk3r.colorpicker.perf;

import android.util.Log;

import com.microsoft.perf.reporters.Reporter;
import com.microsoft.perf.trackers.Tracker;

public class ColorPickerReporter implements Reporter {
    private static final int ONE_SECONDS_IN_NANO = 1000000;

    public void publishTracker(int groupId, int trackerId, Tracker tracker) {
        switch (tracker.type()) {
            case 1:
                Log.d("Tracker Elapse", TrackerId.getNanme(trackerId) + " ----> " + tracker.result() / ONE_SECONDS_IN_NANO + " milli");
                break;
            case 2:
                Log.d("Tracker AvgFPS", TrackerId.getNanme(trackerId) + " ----> " + tracker.result() + " average fps");
                break;
            default:
                Log.d("Tracker", TrackerId.getNanme(trackerId) + " ----> " + tracker.result());
        }

    }

    public void publishError(int groupId, int trackerId, Exception exception) {
        Log.d("Tracker Error", TrackerId.getNanme(trackerId) + " err:" + exception.getMessage());
    }
}