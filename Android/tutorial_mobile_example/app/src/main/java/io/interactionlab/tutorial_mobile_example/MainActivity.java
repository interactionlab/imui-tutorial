package io.interactionlab.tutorial_mobile_example;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

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

    // Classifier
    private NumberClassifier numberClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        downloadButton = (Button) findViewById(R.id.button_test);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // retrieve saved server url
                SharedPreferences prefs = getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE);
                String serverURL = prefs.getString("server", "http://");

                new FileDownloadTask().execute(serverURL);
            }
        });
        tvResult = (TextView) findViewById(R.id.text_result);

        promptServerUrl();
    }


    private void promptServerUrl() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE);
        String servername = prefs.getString("server", "http://");

        final EditText edtText = new EditText(this);
        edtText.setText(servername);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server address");
        builder.setMessage("Please enter server address:");
        builder.setCancelable(false);
        builder.setView(edtText);
        builder.setNeutralButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE).edit();
                editor.putString("server", edtText.getText().toString());
                editor.apply();
            }
        });
        builder.show();
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


    /**
     * #############################################################################
     * Downloading model from the internet and loading it.
     * #############################################################################
     */
    private static final int progress_bar_type = 0;

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                progrssDialog = new ProgressDialog(this);
                progrssDialog.setMessage("Downloading model. Please wait...");
                progrssDialog.setIndeterminate(false);
                progrssDialog.setMax(100);
                progrssDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progrssDialog.setCancelable(true);
                progrssDialog.show();
                return progrssDialog;
            default:
                return null;
        }
    }


    /**
     * Background Async Task to download file
     */
    class FileDownloadTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);

                URLConnection conection = url.openConnection();
                conection.connect();
                int lenghtOfFile = conection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                File pathBack = MainActivity.this.getExternalFilesDir(null);
                OutputStream output = new FileOutputStream(pathBack.getAbsolutePath() + "/downloaded_model.pb");

                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progrssDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);

            File pathBack = MainActivity.this.getExternalFilesDir(null);

            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(pathBack.getAbsolutePath() + "/downloaded_model.pb");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            numberClassifier = new NumberClassifier(fileInputStream);
        }

    }
}
