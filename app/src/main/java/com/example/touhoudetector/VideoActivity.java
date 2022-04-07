package com.example.touhoudetector;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class VideoActivity extends AppCompatActivity implements Runnable {

    private SurfaceView screen;
    public Uri VideoUri;
    VideoCapture cap = new VideoCapture();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        if (OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "OpenCV library was loaded successfully");
        }
        else{
            Log.d("OpenCV", "OpenCV library failed");
        }



        screen = findViewById(R.id.VideoSurfaceView);
        VideoUri = Uri.parse(getIntent().getExtras().getString("VideoUri"));
        cap.open(getPathFromUri(VideoUri));

        Thread newThread = new Thread(this);
        newThread.start();
    }

    public String getPathFromUri(Uri uri)
    {
        //MediaStore.Images.Media.DATA column就是檔案路徑
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        //透過ContentResolver來讀取照片的資訊
        Cursor myCursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        //記得移到第一個 不然常常跑出範圍
        myCursor.moveToFirst();
        return myCursor.getString(0);
    }

    @Override
    public void run() {
        Mat temp = new Mat();
        int m = 0;
        while(cap.read(temp)) {
            m = (m+1)%3;
            if(m==2) {
                Bitmap mBitmap = Bitmap.createBitmap(temp.width(),temp.height(), Bitmap.Config.ARGB_8888);
                org.opencv.android.Utils.matToBitmap(temp, mBitmap);
                //鎖定畫布
                Canvas mCanvas = screen.getHolder().lockCanvas();
                float width = mCanvas.getWidth();
                float height = (float) mBitmap.getHeight() / (float) mBitmap.getWidth() * width;
                myUtils.drawResult(mBitmap);
                mCanvas.drawBitmap(mBitmap, null, new RectF(0, 0, width, height), null);
                //解鎖並且提交
                screen.getHolder().unlockCanvasAndPost(mCanvas);
            }
        }
    }
}