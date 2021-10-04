package com.jjak0b.android.trackingmypantry.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapbox.maps.MapInitOptions;
import com.mapbox.maps.MapView;

public class InterceptedTouchMapView extends MapView {

    public InterceptedTouchMapView(@NonNull Context context) {
        super(context);
    }

    public InterceptedTouchMapView(@NonNull Context context, @NonNull MapInitOptions mapInitOptions) {
        super(context, mapInitOptions);
    }

    public InterceptedTouchMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptedTouchMapView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Disallow ScrollView to intercept touch events.
                this.getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_UP:
                // Allow ScrollView to intercept touch events.
                this.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        // Handle MapView's touch events.
        super.onTouchEvent(ev);
        return true;
    }
}
