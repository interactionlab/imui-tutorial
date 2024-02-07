package io.interactionlab.tutorial_mobile_example;

import android.content.Context;

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
        String inputName = "dense_1_input";
        String outputName = "output_node0";

        // Define output nodes
        String[] outputNodes = new String[]{outputName};
        float[] outputs = new float[10];

        // Feed image into the model and fetch the results.
        inferenceInterface.feed(inputName, pixels, 1, 784);
        inferenceInterface.run(outputNodes);
        inferenceInterface.fetch(outputName, outputs);

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
