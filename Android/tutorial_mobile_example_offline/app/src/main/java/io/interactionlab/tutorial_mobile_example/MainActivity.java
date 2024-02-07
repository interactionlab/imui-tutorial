package io.interactionlab.tutorial_mobile_example;

import android.app.ProgressDialog;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import io.interactionlab.tutorial_mobile_example.ui.DrawModel;
import io.interactionlab.tutorial_mobile_example.ui.DrawView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    // Variables for the draw view.
    private static final int PIXEL_WIDTH = 28;
    private TextView tvResult;
    private float lastX;
    private float lastY;
    private DrawModel drawModel;
    private DrawView drawView;
    private PointF tmpPoint = new PointF();

    private final static String SHARED_PREF_ID = "tutorial_mobile_example";

    // Download view
    private ProgressDialog progrssDialog;
    private Button downloadButton;
    private Button infoButton;


    // Classifier
    private NumberClassifier numberClassifier;

    // File download path


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File path = MainActivity.this.getExternalFilesDir(null);
        numberClassifier = new NumberClassifier("test_model.pb", this);

        drawModel = new DrawModel(PIXEL_WIDTH, PIXEL_WIDTH);

        drawView = (DrawView) findViewById(R.id.view_draw);
        drawView.setModel(drawModel);
        drawView.setOnTouchListener(this);

        Button classifyButton = (Button) findViewById(R.id.button_classify);
        classifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int classification = classifyNumber();

                if (classification == -1) {
                    tvResult.setText("Please load model first.");
                } else {
                    tvResult.setText("Detected = " + classification);
                }

            }
        });

        Button clearButton = (Button) findViewById(R.id.button_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearClicked();
            }
        });

        tvResult = (TextView) findViewById(R.id.text_result);
    }

    private int classifyNumber() {
        // Retrieve 28x28 image.
        float pixels[] = drawView.getPixelData();

        // Classify.

        int idx = -1;
        if (numberClassifier != null) {
            idx = numberClassifier.classify(pixels);
        }

        return idx;
    }

    @Override
    protected void onResume() {
        drawView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        drawView.onPause();
        super.onPause();
    }


    /**
     * #############################################################################
     * Handling the drawing view.
     * #############################################################################
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN) {
            processTouchDown(event);
            return true;

        } else if (action == MotionEvent.ACTION_MOVE) {
            processTouchMove(event);
            return true;

        } else if (action == MotionEvent.ACTION_UP) {
            processTouchUp();
            return true;
        }
        return false;
    }

    private void processTouchDown(MotionEvent event) {
        lastX = event.getX();
        lastY = event.getY();
        drawView.calcPos(lastX, lastY, tmpPoint);
        float lastConvX = tmpPoint.x;
        float lastConvY = tmpPoint.y;
        drawModel.startLine(lastConvX, lastConvY);
    }

    private void processTouchMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        drawView.calcPos(x, y, tmpPoint);
        float newConvX = tmpPoint.x;
        float newConvY = tmpPoint.y;
        drawModel.addLineElem(newConvX, newConvY);

        lastX = x;
        lastY = y;
        drawView.invalidate();
    }

    private void processTouchUp() {
        drawModel.endLine();
    }

    private void onClearClicked() {
        drawModel.clear();
        drawView.reset();
        drawView.invalidate();

        tvResult.setText("");
    }


}
