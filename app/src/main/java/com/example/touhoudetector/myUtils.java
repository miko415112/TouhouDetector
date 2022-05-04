package com.example.touhoudetector;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;

import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class myUtils {

    static Canvas mCanvas = new Canvas();
    static Paint bboxPaint = new Paint();
    static Paint textPaint = new Paint();
    static YOLOv4Tiny detector = new YOLOv4Tiny();

    static {
        bboxPaint.setStyle(Paint.Style.STROKE);
        bboxPaint.setColor(Color.parseColor("#00FCB2"));
        textPaint.setColor(Color.parseColor("#050690"));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);;
    }

    static public void drawResult(Bitmap mBitmap){

        mCanvas.setBitmap(mBitmap);
        float scale = mBitmap.getWidth()/100;
        bboxPaint.setStrokeWidth(2*scale);
        textPaint.setTextSize(6*scale);

        List<YOLOv4Tiny.Result> results = new ArrayList<YOLOv4Tiny.Result>();
        results.addAll(detector.detect(mBitmap));

        for (int i = 0; i < results.size(); i++) {
            String s = results.get(i).name + String.format("%.02f", results.get(i).confidence);
            float x = (results.get(i).location.left + results.get(i).location.right) / 2;
            float y = results.get(i).location.top + 8 * scale;

            mCanvas.drawRect(results.get(i).location, bboxPaint);
            mCanvas.drawText(s, x, y, textPaint);
        }
    }

    static public List<YOLOv4Tiny.Result> CropAndSave(Bitmap mBitmap,Context myContext){

        List<YOLOv4Tiny.Result> results = new ArrayList<YOLOv4Tiny.Result>();
        results.addAll(detector.detect(mBitmap));

        for (int i = 0; i < results.size(); i++) {
            if(results.get(i).name!="smile"&&results.get(i).name!="depressed"&&results.get(i).name!="calm"&&results.get(i).name!="angry") {
                Bitmap resizedBmp = Bitmap.createBitmap(
                        mBitmap ,
                        (int)results.get(i).location.left,
                        (int)results.get(i).location.top,
                        (int)results.get(i).location.right-(int)results.get(i).location.left,
                        (int)results.get(i).location.bottom-(int)results.get(i).location.top);

                try (FileOutputStream out = new FileOutputStream( "/storage/emulated/0/AR.png")) {
                    resizedBmp.compress(Bitmap.CompressFormat.PNG, 100, out);

                    File file = new File("/storage/emulated/0/AR.txt");
                    FileOutputStream stream = new FileOutputStream(file);
                    stream.write(results.get(i).name.getBytes());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return results;
            }
        }
           return null;
    }


}
