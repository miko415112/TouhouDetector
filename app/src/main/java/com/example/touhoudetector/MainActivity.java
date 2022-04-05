package com.example.touhoudetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView mImageView;
    ImageButton photo_button;
    ImageButton video_button;
    ImageButton find_button;
    ImageButton camera_button;
    Context myContext = this;
    YOLOv4Tiny detector;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        check_permissions();

        if (OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "OpenCV library was loaded successfully");
        }
        else{
            Log.d("OpenCV", "OpenCV library failed");
        }

        detector = new YOLOv4Tiny();
        mImageView = findViewById(R.id.imageView);

        photo_button = findViewById(R.id.imageButton_Photo);
        photo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        find_button = findViewById(R.id.imageButton_Find);
        find_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawResult();
            }
        });

        video_button = findViewById(R.id.imageButton_Video);
        video_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectVideo();
            }
        });


    }



    private void drawResult()
    {
        Bitmap mImage = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
        List<YOLOv4Tiny.Result> results = new ArrayList<YOLOv4Tiny.Result>();
        results.addAll(detector.detect(mImage));

        float scale = mImage.getWidth()/100;

        Paint bboxPaint = new Paint();
        bboxPaint.setStyle(Paint.Style.STROKE);
        bboxPaint.setColor(Color.parseColor("#00FCB2"));
        bboxPaint.setStrokeWidth(2*scale);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#0B3169"));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);;
        textPaint.setTextSize(8*scale);

        Bitmap mutableBitmap = mImage.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);


        for(int i=0;i<results.size();i++) {
            String s = results.get(i).name+String.format("%.02f",results.get(i).confidence);
            float x = (results.get(i).location.left+results.get(i).location.right)/2;
            float y = results.get(i).location.top+8*scale;

            canvas.drawRect(results.get(i).location,bboxPaint);
            canvas.drawText(s,x,y,textPaint);
        }
        mImageView.setImageBitmap(mutableBitmap);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int i =0;i<grantResults.length;i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(MainActivity.this, "Some Permissions Denied,May Not Funtion Well", Toast.LENGTH_LONG).show();
                return;
            }
        }
        Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
    }

    private void check_permissions()
    {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions ,0);
            }
        }

    }

    private void selectImage(){
        //選取檔案 並且位置在相簿
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //篩選檔案類型為圖片
        i.setType("image/*");
        startActivityForResult(i,1);
    }

    private void selectVideo(){

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //篩選檔案類型為影片
        i.setType("video/*");
        startActivityForResult(i,2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if(requestCode==1) {
                //獲得選取圖片的Uri
                Uri uri = data.getData();
                mImageView.setImageURI(uri);
            }
            else if (requestCode==2){
                mySurfaceView.VideoUri = data.getData();
                Intent i = new Intent(this, VideoActivity.class);
                startActivity(i);
            }
    }

}