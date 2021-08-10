package com.jjak0b.android.trackingmypantry.ui.util;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.jetbrains.annotations.Nullable;

public class FragmentHelper {

    @Nullable
    public static FragmentManager getFragmentManager(Context ctx ){

        if( ctx instanceof FragmentActivity){
            return ((FragmentActivity)ctx).getSupportFragmentManager();
        }
        return null;
    }
}
