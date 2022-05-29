package com.example.touhoudetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.ContentResolver;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView mImageView;
    ImageButton photo_button;
    ImageButton video_button;
    ImageButton find_button;
    ImageButton camera_button;

    Context myContext = this;
    public static String DirPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        check_permissions();

        init_data();

        if (OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "OpenCV library was loaded successfully");
        }
        else{
            Log.d("OpenCV", "OpenCV library failed");
        }

        mImageView = findViewById(R.id.imageView);

        photo_button = findViewById(R.id.imageButton_Photo);
        photo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //選取檔案 並且位置在相簿
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //篩選檔案類型為圖片
                i.setType("image/*");
                startActivityForResult(i,1);

            }
        });

        find_button = findViewById(R.id.imageButton_Find);
        find_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //imageView得到的Bitmap是Mutable
                Bitmap mBitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
                Bitmap mutableBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
                myUtils.drawResult(mutableBitmap);
                mImageView.setImageBitmap(mutableBitmap);

            }
        });

        video_button = findViewById(R.id.imageButton_Video);
        video_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //篩選檔案類型為影片
                i.setType("video/*");
                startActivityForResult(i,2);

            }
        });

        camera_button = findViewById(R.id.imageButton_Camera);
        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(myContext, CameraActivity.class);
                startActivity(i);
            }
        });
    }


    public void init_data(){
        DirPath = getExternalFilesDir("yolov4Tiny").getAbsolutePath();
        File namesFileDst = new File(DirPath,"touhouNames.names");
        Uri nameUriDst = Uri.fromFile(namesFileDst);
        Uri namesUriSrc = Uri.parse("android.resource://com.example.touhoudetector/raw/touhou_names");
        if(!namesFileDst.exists()){
            copyFile(getContentResolver(),namesUriSrc,nameUriDst);
        }
        File weightsFileDst = new File(DirPath,"touhouWeights.weights");
        Uri weightsUriDst = Uri.fromFile(weightsFileDst);
        Uri weightsUriSrc = Uri.parse("android.resource://com.example.touhoudetector/raw/yolov4_tiny_touhou_weights");
        if(!weightsFileDst.exists()){
            copyFile(getContentResolver(),weightsUriSrc,weightsUriDst);
        }
        File cfgFileDst = new File(DirPath,"touhouCfg.cfg");
        Uri cfgUriDst = Uri.fromFile(cfgFileDst);
        Uri cfgUriSrc = Uri.parse("android.resource://com.example.touhoudetector/raw/yolov4_tiny_touhou_cfg");
        if(!cfgFileDst.exists()){
            copyFile(getContentResolver(),cfgUriSrc,cfgUriDst);
        }
    }

    public void copyFile(ContentResolver r, Uri from, Uri to)  {
        try{
            InputStream is = r.openInputStream(from);
            OutputStream os = r.openOutputStream(to);
            byte[] b = new byte[4096];
            int read;
            while ((read = is.read(b)) != -1) {
                os.write(b, 0, read);
            }
            os.flush();
            os.close();
            is.close();
        }
        catch(Exception e){};
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int i =0;i<grantResults.length;i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(MainActivity.this, "Some Permissions are Denied,May Not Funtion Well", Toast.LENGTH_LONG).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if(requestCode==1) {
                //獲得選取圖片的Uri
                Uri uri = data.getData();
                mImageView.setImageURI(uri);
            }
            else if (requestCode==2){
                Intent i = new Intent(this, VideoActivity.class);
                i.putExtra("VideoUri",data.getData().toString());
                startActivity(i);
            }
    }

}