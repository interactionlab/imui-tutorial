package io.interactionlab.tutorial_mobile_example;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

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

    protected MappedByteBuffer loadMappedFile(Context context, String filePath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(filePath);

        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public NumberClassifier(String modelPath, Context context) {
        MappedByteBuffer myMappedBuffer = null;
        try {
            myMappedBuffer = loadMappedFile(context, modelPath);
        } catch (IOException e) {
            Log.e("NumberClassifier", "Error #002: "  + e.toString());
        }

        try {
            interpreter = new Interpreter(myMappedBuffer);
        } catch (Exception e) {
            Log.e("NumberClassifier", "Error #001: "  + e.toString());
        }
    }
    public int classify(float[] pixels) {

        if (interpreter == null){
            Log.w("NumberClassifier", "interpreter not ready.");
            return -1;
        }
        // Node Names
        //String inputName = "dense_1_input";
        //String outputName = "output_node0";

        // Define output nodes
        //String[] outputNodes = new String[]{outputName};
        //float[] outputs = new float[10];
        //inferenceInterface.feed(inputName, pixels, 1, 784);
        //inferenceInterface.run(outputNodes);
        //inferenceInterface.fetch(outputName, outputs);

        float[][] input = {pixels};  // Input tensor shape is [2].
        float[][] output = new float[1][10];  // Output tensor shape is [3, 2].
        interpreter.run(input, output);

        float[] outputs = output[0];

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
