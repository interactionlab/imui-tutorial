package io.interactionlab.tutorial_mobile_example;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * This class demonstrates the use of the inference interface of TensorFlow.
 * The model (protobuf file) can either be loaded from the assets folder of the APK, or using an InputStream.
 */
public class NumberClassifier {

    private Interpreter interpreter = null;

    public NumberClassifier(String modelPath, Context context) {
        MappedByteBuffer myMappedBuffer = null;

        try {
            myMappedBuffer = FileUtil.loadMappedFile(context, modelPath);
        } catch (IOException e) {
            Log.e("NumberClassifier", "Error #002: "  + e.toString());
            return;
        }

        try {
            interpreter = new Interpreter(myMappedBuffer);
        } catch (Exception e) {
            Log.e("NumberClassifier", "Error #001: "  + e.toString());
            return;
        }
        Log.v("NumberClassifier", "Load model successful.");

        for (String s : interpreter.getSignatureKeys()) {
            Log.d("NumberClassifier - Model Debug", "SignatureKeys: " + s);
        }
        Log.d("NumberClassifier - Model Debug",  "InputTensorCount: " + interpreter.getInputTensorCount());
        for (int dim : interpreter.getInputTensor(0).shape()) {
            Log.d("NumberClassifier - Model Debug", "Input Shape: " +dim);
        }
        Log.d("NumberClassifier - Model Debug",  "OutputTensorCount: " + interpreter.getOutputTensorCount());
        for (int dim : interpreter.getOutputTensor(0).shape()) {
            Log.d("NumberClassifier - Model Debug", "Output Shape: " +dim);
        }
    }
    public int classify(float[] pixels) {
        Log.d("NumberClassifier", "PixelData - Length:" + pixels.length);
        if (interpreter == null){
            Log.w("NumberClassifier", "interpreter not ready.");
            return -1;
        }

        float[][][] input = new float[1][1][28*28];  // Input tensor shape is [2].
        input[0][0] = pixels;
        float[][][] output = new float[1][1][10];  // Output tensor shape is [3, 2].
        output[0][0]  = new float[]{0,0,0,0,0,0,0,0,0,0};

        Log.v("NumberClassifier", "Feeding into model.");
        try {
            interpreter.run(input, output);
        } catch (Exception e){
            Log.e("NumberClassifier", "Error #003: "  + e.toString());
            return -1;
        }
        Log.v("NumberClassifier", "Inference done.");

        float[] outputs = output[0][0];

        // Convert one-hot encoded result to an int (= detected class)
        float max = Float.MIN_VALUE;
        int idx = -1;
        for (int i = 0; i < outputs.length; i++) {
            if (outputs[i] > max) {
                max = outputs[i];
                idx = i;
            }
        }
        return idx;
    }
}
