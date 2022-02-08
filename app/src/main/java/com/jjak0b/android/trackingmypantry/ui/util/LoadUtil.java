package com.jjak0b.android.trackingmypantry.ui.util;


import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.jjak0b.android.trackingmypantry.R;


public class LoadUtil {

    public static Drawable getProgressLoader(@NonNull final Context context) {

        final CircularProgressDrawable drawable = new CircularProgressDrawable(context);
        drawable.setColorSchemeColors(
                context.getColor(R.color.loading_color_start),
                context.getColor(R.color.loading_color_center),
                context.getColor(R.color.loading_color_end)
        );
        drawable.setCenterRadius(0f);
        drawable.setStrokeWidth(context.getResources()
                .getDimensionPixelSize(R.dimen.loading_stroke_width)
        );
        // set all other properties as you would see fit and start it
        drawable.start();
        return drawable;
    }
}
