package com.example.touhoudetector;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import android.widget.Toast;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class YOLOv4Tiny {

    private ArrayList<String> touhou_names = new ArrayList<>();
    private Net net;
    private float Confidence_Threshold = 0.5f;
    private float NMS_Threshold = 0.4f;
    private int bboxes = 2535; //(26*26+13*13)*3
    private Size inputSize = new Size(192,192);


    class Result
    {
        public String name;
        public Float confidence;
        public RectF location;

        public Result(String name,Float confidence,RectF location)
        {
            this.name = name;
            this.confidence = confidence;
            this.location = location;
        }
    };

    public YOLOv4Tiny()
    {
          try {
                String DirPath = MainActivity.DirPath;
                //載入標籤名稱
                File file = new File(DirPath+"/touhouNames.names");

                InputStream is = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String name;
                while ((name = br.readLine()) != null) {
                    touhou_names.add(name);
                }
                is.close();

            //載入超參數與權重
            net = Dnn.readNetFromDarknet(DirPath+"/touhouCfg.cfg",
                    DirPath+"/touhouWeights.weights");
        }
        catch (Exception e) {
            Log.d("model","model was not found");
        }


    }


    public List<Result> detect(Bitmap bitmap)
    {
        //bitmap轉成Mat
        Mat inputMat = new Mat();
        org.opencv.android.Utils.bitmapToMat(bitmap,inputMat);
        //降低channel數
        Imgproc.cvtColor(inputMat,inputMat,Imgproc.COLOR_RGBA2RGB);
        //輸入前的預處理
        Mat inputBlob = Dnn.blobFromImage(inputMat,1f/255.0f, inputSize,new Scalar(0, 0, 0),false,false);
        //設置輸入
        net.setInput(inputBlob);
        //獲得輸出
        List<Mat> outputMats = new ArrayList<>();
        net.forward(outputMats,getOutputLayerNames(net));

        //找出每個預測框的最大信心與對應類別
        List<Result> candidates = new ArrayList<>();
        for(int i=0;i<outputMats.size();i++) {
            if (outputMats.get(i).empty())
                continue;;
            //一列對應到一個框 第0~3行是框的位置 第4行是框內有物體的信心
            //第5行以後是框內物體屬於各個類別信心
            for (int row = 0; row < outputMats.get(i).rows(); row++) {
                float confidence = -1;
                int objectClass = -1;
                for (int col = 5; col < outputMats.get(i).cols(); col++) {
                    if (outputMats.get(i).get(row, col)[0] > confidence) {
                        confidence = (float) outputMats.get(i).get(row, col)[0];
                        objectClass = col - 5;
                    }
                }
                if (confidence > Confidence_Threshold) {
                    float x = (float) outputMats.get(i).get(row, 0)[0];
                    float y = (float) outputMats.get(i).get(row, 1)[0];
                    float w = (float) outputMats.get(i).get(row, 2)[0];
                    float h = (float) outputMats.get(i).get(row, 3)[0];

                    RectF location = new RectF(
                            Math.max(0, (x - w / 2) * bitmap.getWidth()),
                            Math.max(0, (y - h / 2) * bitmap.getHeight()),
                            Math.min(bitmap.getWidth() - 1, (x + w / 2) * bitmap.getWidth()),
                            Math.min(bitmap.getHeight() - 1, (y + h / 2) * bitmap.getHeight())
                    );
                    candidates.add(new Result(touhou_names.get(objectClass), confidence, location));
                }
            }

        }

        return NMS(candidates);
 }



    private List<Result> NMS(List<Result> candidates){

        List<Result> results = new ArrayList<Result>();
        //創建一個優先佇列 把候選框放進去後就可以自動排列
        PriorityQueue<Result> pq = new PriorityQueue<Result>(
                30,
                new Comparator<Result>() {
                    @Override
                    public int compare(Result a,Result b) {
                        //返回負值代表比較小 會先放在出口 但此處故意相反讓比較大的在出口
                        return Float.compare(b.confidence, a.confidence);
                    }
                });

        //每個class各自處理自己的重複框
        for (int i = 0; i < touhou_names.size(); i++) {
            //把所有偵測為第i個class的結果放進去pq 就會依照信心大小排列 大的在出口
            for (int j = 0; j < candidates.size(); j++) {
                if (candidates.get(j).name == touhou_names.get(i))
                    pq.add(candidates.get(j));
            }

            while(!pq.isEmpty()) {
                Result maxResult = pq.poll();
                results.add(maxResult);

                List<Result> temps = new ArrayList<>();
                temps.addAll(pq);
                pq.clear();

                for(int k=0;k<temps.size();k++){
                    if(box_iou(maxResult.location,temps.get(k).location)<NMS_Threshold)
                        pq.add(temps.get(k));
                }
            }
        }
        return results;
    }


    private List<String> getOutputLayerNames(Net net){
        //獲得輸出層的索引 從1開始
        int[] OutputLayerIDs = net.getUnconnectedOutLayers().toArray();
        //獲得所有層的名稱
        List<String> AllNames = net.getLayerNames();
        //獲得輸出層的名稱
        List<String> OutputLayerNames = new ArrayList<>();
        for(int i = 0;i<OutputLayerIDs.length;i++) {
            OutputLayerNames.add(AllNames.get(OutputLayerIDs[i]-1));
        }
        return OutputLayerNames;
    }

    private float box_iou(RectF a, RectF b) {
        return box_intersection(a, b) / box_union(a, b);
    }

    private float box_intersection(RectF a, RectF b) {
        float w = overlap((a.left + a.right) / 2, a.right - a.left,
                (b.left + b.right) / 2, b.right - b.left);
        float h = overlap((a.top + a.bottom) / 2, a.bottom - a.top,
                (b.top + b.bottom) / 2, b.bottom - b.top);
        if (w < 0 || h < 0) return 0;
        float area = w * h;
        return area;
    }

    private float box_union(RectF a, RectF b) {
        float i = box_intersection(a, b);
        float u = (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i;
        return u;
    }

    private float overlap(float x1, float w1, float x2, float w2) {
        float l1 = x1 - w1 / 2;
        float l2 = x2 - w2 / 2;
        float left = l1 > l2 ? l1 : l2;
        float r1 = x1 + w1 / 2;
        float r2 = x2 + w2 / 2;
        float right = r1 < r2 ? r1 : r2;
        return right - left;
    }

}
