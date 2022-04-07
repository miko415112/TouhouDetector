package com.example.touhoudetector;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.view.SurfaceView;
import android.widget.ImageView;

import org.opencv.core.Mat;

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


}
