package com.rachin.app;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private int imgSize = 128;
    private int imgMean = 117;
    private float imgStd = 1;

    private String inputName = "input";
    private String outputName = "output";

    private String modelname = "HorseOrHuman";
    private String labelName = "labels.txt";

    private Classify classify;
    private TextView resultView;
    private Button detectBtn;
    private ImageView iv;

    private Bitmap bm;
    private Executor executor = Executors.newSingleThreadExecutor();
    private boolean isInit = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = findViewById(R.id.resultImage);
        resultView = findViewById(R.id.resultText);
        detectBtn = findViewById(R.id.detectBtn);





        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bm = getBitmap();
                bm.createScaledBitmap(bm, 300, 300, false);
                iv.setImageBitmap(bm);
                Bitmap newBm = bm.copy(Bitmap.Config.ARGB_8888,false);
                List<Classify.Recogonition> results = classify.recImg(newBm);

                String res = "";
                for(Classify.Recogonition rec:results){
                    res+=rec.toString();
                }
                resultView.setText(res);


            }
        });

        loadModel();


    }

    public Bitmap getBitmap() {
        InputStream is = null;
        try {
            is = getAssets().open("image.jpg");
        } catch (IOException e) {
        }

        Bitmap bm = BitmapFactory.decodeStream(is);
       // Bitmap newBm = bm.copy(Bitmap.Config.ARGB_8888,false);
        return bm;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classify.close();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    public void loadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classify = TF.make(getAssets(), modelname, labelName, imgSize);
                } catch (final Exception e) {
                }
            }
        });
    }
}
