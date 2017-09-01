package io.interactionlab.tutorial_mobile_example.ui;

/**
 * Created by Huy on 31/08/2017.
 */

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * This class handles the draw view and was adapted from miyosuda's TensorFlow Android MNIST example
 * for TensorFlow r.0.10. See: https://github.com/miyosuda/TensorFlowAndroidMNIST
 */
public class DrawRenderer {
    /**
     * Draw lines to canvas
     */
    public static void renderModel(Canvas canvas, DrawModel model, Paint paint,
                                   int startLineIndex) {
        paint.setAntiAlias(true);

        int lineSize = model.getLineSize();
        for (int i = startLineIndex; i < lineSize; ++i) {
            DrawModel.Line line = model.getLine(i);
            paint.setColor(Color.BLACK);
            int elemSize = line.getElemSize();
            if (elemSize < 1) {
                continue;
            }
            DrawModel.LineElem elem = line.getElem(0);
            float lastX = elem.x;
            float lastY = elem.y;

            for (int j = 0; j < elemSize; ++j) {
                elem = line.getElem(j);
                float x = elem.x;
                float y = elem.y;
                canvas.drawLine(lastX, lastY, x, y, paint);
                lastX = x;
                lastY = y;
            }
        }
    }
}