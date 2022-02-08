package com.jjak0b.android.trackingmypantry.ui.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.Resource;

import java.io.ByteArrayOutputStream;

public class ImageUtil
{

    @WorkerThread
    public static Bitmap convert(String base64Str) throws IllegalArgumentException
    {
        byte[] decodedBytes = Base64.decode(
                base64Str.substring(base64Str.indexOf(",")  + 1),
                Base64.DEFAULT
        );

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @WorkerThread
    public static String convert(Bitmap bitmap, int quality)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);


        return "data:image/jpeg;base64," + Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }


    public static LiveData<Resource<Bitmap>> getBitmap(String base64Str) {
        AppExecutors appExecutors = AppExecutors.getInstance();
        MutableLiveData<Resource<Bitmap>> mBitmap = new MutableLiveData<>(Resource.loading(null));
        appExecutors.networkIO().execute(() -> {
            try {
                mBitmap.postValue(Resource.success(convert(base64Str)));
            }
            catch (Throwable e) {
                mBitmap.postValue(Resource.error(e, null));
            }
        });
        return mBitmap;
    }


    public static LiveData<Resource<String>> getURI(Bitmap bitmap) {
        return getURI(bitmap, 100);
    }

    public static LiveData<Resource<String>> getURI(Bitmap bitmap, int quality) {
        AppExecutors appExecutors = AppExecutors.getInstance();
        MutableLiveData<Resource<String>> mURI = new MutableLiveData<>(Resource.loading(null));
        appExecutors.networkIO().execute(() -> {
            try {
                mURI.postValue(Resource.success(convert(bitmap, quality)));
            }
            catch (Throwable e) {
                mURI.postValue(Resource.error(e, null));
            }
        });
        return mURI;
    }

    public static class ActivityResultContractTakePicture extends ActivityResultContract<Void, Bitmap> {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Void input) {
            return new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        }

        @Override
        public Bitmap parseResult(int resultCode, @Nullable Intent intent) {
            if (resultCode == Activity.RESULT_OK ) {
                Bundle extras = intent.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                if( imageBitmap != null ){
                    return imageBitmap;
                }
            }
            return null;
        }
    }

    // source: https://stackoverflow.com/a/10600736
    public static Bitmap getBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}