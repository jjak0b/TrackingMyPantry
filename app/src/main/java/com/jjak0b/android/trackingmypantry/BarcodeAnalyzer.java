package com.jjak0b.android.trackingmypantry;

import android.annotation.SuppressLint;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;

import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class BarcodeAnalyzer implements ImageAnalysis.Analyzer {

    private BarcodeScanner scanner;

    private OnSuccessListener<String> onScanSuccess;
    private OnFailureListener onScanFailure;

    BarcodeAnalyzer() {
        // Pass image to an ML Kit Vision API
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        this.scanner = BarcodeScanning.getClient(options);
    }

    public void setOnScanSuccess( OnSuccessListener<String> onSuccessListener ) {
        onScanSuccess = onSuccessListener;
    }

    public void setOnScanFailure( OnFailureListener onFailureListener ) {
        onScanFailure = onFailureListener;
    }

    @Override
    public void analyze(ImageProxy imageProxy) {

        @SuppressLint("UnsafeOptInUsageError")
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {
            InputImage image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            Task<List<Barcode>> result = scanner.process(image)
                    .addOnSuccessListener( (List<Barcode> barcodes) -> {
                            // Task completed successfully
                            // ...
                            for (Barcode barcode: barcodes) {
                                if( isSupportedBarcode(barcode) ){
                                    if( onScanSuccess != null) {
                                        onScanSuccess.onSuccess(barcode.getRawValue());
                                    }
                                    break;
                                }
                            }

                        }
                    )
                    .addOnFailureListener((@NonNull Exception e) -> {
                        Log.e( "CameraXBasic", "Scan failure: " + e );
                        if( onScanFailure != null) {
                            onScanFailure.onFailure(e);
                        }
                    })
                    .addOnCompleteListener((@NonNull Task<List<Barcode>> task) -> {
                         imageProxy.close();
                    });
        }
        /*
            Note: If you are using the CameraX API, make sure to close the ImageProxy when finish using it,
            e.g., by adding an OnCompleteListener to the Task returned from the process method.
            See the VisionProcessorBase class in the quickstart sample app for an example.
        * */
    }

    boolean isSupportedBarcode(Barcode barcode ) {
        Log.d( "CameraXBasic", "Detected new barcode of type "
                + "'" + barcode.getValueType() + "'"
                + " with value: '" + barcode.getRawValue()
                + "' = '" + barcode.getDisplayValue() + "'" );

        boolean result = false;
        switch ( barcode.getValueType() ) {
            // supported types
            case Barcode.TYPE_TEXT:
            case Barcode.TYPE_PRODUCT:
                result = true;
                break;
            // unsupported types
            default:
                Log.e( "scan", "Unsupported product type");
                result = false;
                break;
        }
        return result;
    }

    void onScanFailure(Exception e ) {

    }
}
