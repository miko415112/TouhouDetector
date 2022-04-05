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

public class mySurfaceView extends SurfaceView implements SurfaceHolder.Callback,Runnable {

    SurfaceHolder mSurfaceHolder;
    Canvas mCanvas;
    public static Uri VideoUri;
    YOLOv4Tiny detector;
    Context myContext;
    VideoCapture cap = new VideoCapture();


    //如果要在xml布局中建立 需要使用有這兩個參數的建構子
    public mySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        myContext = context;
        //控制surface
        mSurfaceHolder = getHolder();
        //設定三個回調函數 要implements SurfaceHolder.Callback
        mSurfaceHolder.addCallback(this);

        cap.open(getPathFromUri(VideoUri));

    }

    public String getPathFromUri(Uri uri)
    {
        //MediaStore.Images.Media.DATA column就是檔案路徑
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        //透過ContentResolver來讀取照片的資訊
        Cursor myCursor = myContext.getContentResolver().query(uri, filePathColumn, null, null, null);
        //記得移到第一個 不然常常跑出範圍
        myCursor.moveToFirst();
        return myCursor.getString(0);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

        if (OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "OpenCV library was loaded successfully");
        }
        else{
            Log.d("OpenCV", "OpenCV library failed");
        }
        detector = new YOLOv4Tiny();
        Thread newThread = new Thread(this);
        newThread.start();

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
    }

    @Override
    public void run() {


        Mat temp = new Mat();
        cap.read(temp);
        Bitmap b = Bitmap.createBitmap(temp.width(),temp.height(), Bitmap.Config.ARGB_8888);

        float scale = temp.width()/100;
        Paint bboxPaint = new Paint();
        bboxPaint.setStyle(Paint.Style.STROKE);
        bboxPaint.setColor(Color.parseColor("#00FCB2"));
        bboxPaint.setStrokeWidth(2*scale);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#0B3169"));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);;
        textPaint.setTextSize(8*scale);

        while(cap.read(temp)){
                //鎖定畫布
                mCanvas = mSurfaceHolder.lockCanvas();
                org.opencv.android.Utils.matToBitmap(temp,b);
                float width = mCanvas.getWidth();
                float height = (float)b.getHeight()/(float)b.getWidth()*width;
                mCanvas.drawBitmap(b,null,new RectF(0,0,width,height),null);

                List<YOLOv4Tiny.Result> results = new ArrayList<YOLOv4Tiny.Result>();
                results.addAll(detector.detect(b));

                for(int i=0;i<results.size();i++) {
                    String s = results.get(i).name+String.format("%.02f",results.get(i).confidence);
                    float x = (results.get(i).location.left+results.get(i).location.right)/2;
                    float y = results.get(i).location.top+8*scale;

                    mCanvas.drawRect(results.get(i).location,bboxPaint);
                    mCanvas.drawText(s,x,y,textPaint);
                }
                //解鎖並且提交
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
     }

}
