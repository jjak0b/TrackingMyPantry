package com.jjak0b.android.trackingmypantry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeScannerActivity extends AppCompatActivity {

    final static String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};
    final static int REQUEST_CODE_PERMISSIONS = 10;

    public static final String BARCODE = "barcode";

    private static String TAG = "CameraXBasic";

    private BarcodeAnalyzer mAnalyzer;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private PreviewView viewFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        viewFinder = findViewById(R.id.viewFinder);

        cameraExecutor = Executors.newSingleThreadExecutor();

        mAnalyzer = new BarcodeAnalyzer();
        mAnalyzer.setOnScanSuccess(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String barcodeValue) {
                Intent result = new Intent();
                result.putExtra(BARCODE, barcodeValue );
                setResult(Activity.RESULT_OK, result );
                finish();
            }
        });

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode( ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY )
                .setTargetResolution( new Size( 1280, 720) )
                .build();

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            ProcessCameraProvider cameraProvider = null;
            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            // Preview
            Preview preview = new Preview.Builder()
                    .build();

            // Select back camera as a default
            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

            preview.setSurfaceProvider( viewFinder.getSurfaceProvider() );

            ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();
            imageAnalyzer.setAnalyzer( cameraExecutor, mAnalyzer );

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind use cases to camera
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalyzer);
                setUpTapToFocus( camera );

            } catch(Exception exc) {
                Log.e(TAG, "Use case binding failed", exc);
            }

        }, ContextCompat.getMainExecutor(this));
    }

    private boolean allPermissionsGranted() {
        boolean allGranted = false;
        for ( String permission : REQUIRED_PERMISSIONS) {
            allGranted = allGranted
                    || ContextCompat.checkSelfPermission( this, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            }
            else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    private void setUpTapToFocus( Camera camera ) {

        viewFinder.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_UP) {
                /* Original post returns false here, but in my experience this makes
                onTouch not being triggered for ACTION_UP event */
                    return true;
                }

                MeteringPoint point = viewFinder.getMeteringPointFactory()
                        .createPoint(event.getX(), event.getY());
                FocusMeteringAction action = new FocusMeteringAction.Builder(point)
                        .build();
                CameraControl cameraControl = camera.getCameraControl();

                cameraControl.startFocusAndMetering(action);
                return true;
            }
        });
    }
}