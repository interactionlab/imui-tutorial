package io.interactionlab.tutorial_mobile_example;

import android.content.Context;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.InputStream;

/**
 * Created by Huy on 01/09/2017.
 */

/**
 * This class demonstrates the use of the inference interface of TensorFlow.
 * The model (protobuf file) can either be loaded from the assets folder of the APK, or using an InputStream.
 */
public class NumberClassifier {
    private TensorFlowInferenceInterface inferenceInterface;

    public NumberClassifier(String modelPath, Context context) {
        // Loading model from assets folder.
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), modelPath);
    }

    public NumberClassifier(InputStream inputStream) {
        // Loading the model from an input stream.
        inferenceInterface = new TensorFlowInferenceInterface(inputStream);
    }

    public int classify(float[] pixels) {
        // Node Names
        String inputName = "input";
        String outputName = "output";

        // Define output nodes
        String[] outputNodes = new String[]{outputName};
        float[] outputs = new float[10];

        // Feed image into the model and fetch the results.
        inferenceInterface.feed(inputName, pixels, 1, 28, 28, 1);
        inferenceInterface.run(outputNodes, false);
        inferenceInterface.fetch(outputName, outputs);

        // Convert one-hot encodd result to an int (= detected class)
        float max = outputs[0];
        int idx = -1;
        for (int i = 1; i < outputs.length; i++) {
            if (outputs[i] > max) {
                max = outputs[i];
                idx = i;
            }
        }

        return idx;
    }

}
