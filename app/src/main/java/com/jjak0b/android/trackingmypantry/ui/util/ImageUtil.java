package com.jjak0b.android.trackingmypantry.ui.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.Api;
import com.jjak0b.android.trackingmypantry.AppExecutors;
import com.jjak0b.android.trackingmypantry.data.api.ApiResponse;
import com.jjak0b.android.trackingmypantry.data.api.NetworkBoundResource;
import com.jjak0b.android.trackingmypantry.data.api.Resource;
import com.jjak0b.android.trackingmypantry.data.api.Transformations;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Response;

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
    public static String convert(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);


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
        AppExecutors appExecutors = AppExecutors.getInstance();
        MutableLiveData<Resource<String>> mURI = new MutableLiveData<>(Resource.loading(null));
        appExecutors.networkIO().execute(() -> {
            try {
                mURI.postValue(Resource.success(convert(bitmap)));
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
}