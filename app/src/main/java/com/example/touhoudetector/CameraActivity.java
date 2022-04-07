package com.example.touhoudetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    JavaCameraView mJavaCameraView;
    Camera mCamera = Camera.open(0);;
    int m = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "OpenCV library was loaded successfully");
        }
        else{
            Log.d("OpenCV", "OpenCV library failed");
        }

        mJavaCameraView = findViewById(R.id.mJavaCameraView);
        mJavaCameraView = findViewById(R.id.mJavaCameraView);
        mJavaCameraView.setCameraPermissionGranted();
        mJavaCameraView.enableView();
        mJavaCameraView.setVisibility(SurfaceView.VISIBLE);
        mJavaCameraView.setCvCameraViewListener(this);
    }


    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Bitmap mBitmap = Bitmap.createBitmap(inputFrame.rgba().width(), inputFrame.rgba().height(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(inputFrame.rgba(),mBitmap);
        myUtils.drawResult(mBitmap);
        Mat newMat = new Mat();
        org.opencv.android.Utils.bitmapToMat(mBitmap, newMat);
        return newMat;
    }
}