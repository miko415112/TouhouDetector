package com.example.touhoudetector;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;

public class mySurfaceView extends SurfaceView {

    public SurfaceHolder mySurfaceHolder;
    //如果要在xml布局中建立 需要使用有這兩個參數的建構子
    public mySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mySurfaceHolder = getHolder();
    }

}
