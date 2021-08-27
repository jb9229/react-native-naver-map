package com.airbnb.android.react.maps;

import android.os.Handler;
import android.os.Looper;

import java.util.LinkedList;

public class NMapViewChangesTracker {

    private static NMapViewChangesTracker instance;
    private Handler handler;
    private LinkedList<NMapTrackableView> markers = new LinkedList<>();
    private boolean hasScheduledFrame = false;
    private Runnable updateRunnable;
    private final long fps = 2; // FIXME flickering custom view

    private NMapViewChangesTracker() {
        handler = new Handler(Looper.myLooper());
        updateRunnable = () -> {
            hasScheduledFrame = false;
            update();

            if (markers.size() > 0) {
                handler.postDelayed(updateRunnable, 1000 / fps);
            }
        };
    }

    public static NMapViewChangesTracker getInstance() {
        if (instance == null) {
            synchronized (NMapViewChangesTracker.class) {
                instance = new NMapViewChangesTracker();
            }
        }

        return instance;
    }

    public void addMarker(NMapTrackableView marker) {
        markers.add(marker);

        if (!hasScheduledFrame) {
            hasScheduledFrame = true;
            handler.postDelayed(updateRunnable, 1000 / fps);
        }
    }

    public void removeMarker(NMapTrackableView marker) {
        markers.remove(marker);
    }

    public boolean containsMarker(NMapTrackableView marker) {
        return markers.contains(marker);
    }

    private LinkedList<NMapTrackableView> markersToRemove = new LinkedList<>();

    public void update() {
        for (NMapTrackableView marker : markers) {
            if (!marker.updateCustomForTracking()) {
                markersToRemove.add(marker);
            } else {
                marker.update(0, 0);
            }
        }

        // Remove markers that are not active anymore
        if (markersToRemove.size() > 0) {
            markers.removeAll(markersToRemove);
            markersToRemove.clear();
        }
    }
}
