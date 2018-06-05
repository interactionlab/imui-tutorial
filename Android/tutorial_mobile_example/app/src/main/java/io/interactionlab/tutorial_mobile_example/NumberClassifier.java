package io.interactionlab.tutorial_mobile_example;

import android.content.Context;
import android.content.SharedPreferences;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.InputStream;

import static android.content.Context.MODE_PRIVATE;
import static io.interactionlab.tutorial_mobile_example.Constants.SHARED_PREF_ID;

/**
 * Created by Huy on 01/09/2017.
 */

/**
 * This class demonstrates the use of the inference interface of TensorFlow.
 * The model (protobuf file) can either be loaded from the assets folder of the APK, or using an InputStream.
 */
public class NumberClassifier {
    private TensorFlowInferenceInterface inferenceInterface;

    String inputName = "input";
    String outputName = "output";

    public NumberClassifier(String modelPath, Context context) {
        // Loading model from assets folder.
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), modelPath);
    }

    public NumberClassifier(InputStream inputStream, Context context) {
        // Loading the model from an input stream.
        inferenceInterface = new TensorFlowInferenceInterface(inputStream);

        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE);
        inputName = prefs.getString("input_node", "dense_1_input");
        outputName = prefs.getString("output_node", "output_node0");
    }

    public int classify(float[] pixels) {
        // Define output nodes
        String[] outputNodes = new String[]{outputName};
        float[] outputs = new float[10];

        // Feed image into the model and fetch the results.
        inferenceInterface.feed(inputName, pixels, 1, 784);
        inferenceInterface.run(outputNodes, false);
        inferenceInterface.fetch(outputName, outputs);

        // Convert one-hot encodd result to an int (= detected class)
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
