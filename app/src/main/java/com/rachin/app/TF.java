package com.rachin.app;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TF implements Classify {

    FirebaseCustomRemoteModel cloudModel;
    FirebaseCustomLocalModel localModel;
    FirebaseModelInterpreter interpreter;
    FirebaseModelInputOutputOptions inoutOptions;
    FirebaseModelInputs inputs;

    float[] probs = new float[1];
    String [] labels = new String[2];

    public TF(){}

    public static Classify make(AssetManager am, String modelName, String labelName, int imgSize) throws IOException {

        final TF tf = new TF();


        tf.cloudModel = new FirebaseCustomRemoteModel.Builder(modelName).build();
        tf.localModel = new FirebaseCustomLocalModel.Builder().setAssetFilePath("model.tflite").build();

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().requireWifi().build();
        FirebaseModelManager.getInstance().download(tf.cloudModel,conditions).addOnSuccessListener(new OnSuccessListener<Void>() {

            public void onSuccess(Void v){

            }
        });

        FirebaseModelManager.getInstance().isModelDownloaded(tf.cloudModel).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean downloaded) {
                 FirebaseModelInterpreterOptions options;
                if (downloaded) {
                     options = new FirebaseModelInterpreterOptions.Builder(tf.cloudModel).build();
                }else{
                     options = new FirebaseModelInterpreterOptions.Builder(tf.localModel).build();
                }
                try {
                    tf.interpreter = FirebaseModelInterpreter.getInstance(options);
                } catch (FirebaseMLException e) { }

            }
        });

        try {
                tf.inoutOptions = new FirebaseModelInputOutputOptions.Builder()
                    .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, imgSize, imgSize, 3})
                    .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 1}).build();

        } catch (FirebaseMLException e) { }

        BufferedReader br = new BufferedReader(new InputStreamReader(am.open(labelName)));

        String line;
        int i = 0;
        while ((line = br.readLine()) != null) {
            tf.labels[i] = line;
            i++;
        }
        br.close();
        return tf;
    }

    @Override
    public List<Recogonition> recImg(Bitmap bm) {
        bm.createScaledBitmap(bm, 128, 128, false);

        int batchNum = 0;
        float[][][][]input = new float[1][128][128][3];
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                int pixel = bm.getPixel(x, y);
                input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 128.0f;
                input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 128.0f;
                input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 128.0f;
            }
        }

        try {
            inputs = new FirebaseModelInputs.Builder().add(input).build();
             } catch (FirebaseMLException e) { }
        interpreter.run(inputs, inoutOptions).addOnSuccessListener(new OnSuccessListener<FirebaseModelOutputs>() {
            @Override
            public void onSuccess(FirebaseModelOutputs result) {
                float[][] results;
                results = result.getOutput(0);
                probs = results[0];
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) { }
        });

        ArrayList<Recogonition> finalResult = new ArrayList<>();

            if(probs[0]<0.5f){
                finalResult.add(new Recogonition("","Horse",probs[0]));
            }
            else{
                finalResult.add(new Recogonition("","Human",probs[0]));
            }


        return finalResult;

    }

    public void close() {
            interpreter.close();
        }
    }


