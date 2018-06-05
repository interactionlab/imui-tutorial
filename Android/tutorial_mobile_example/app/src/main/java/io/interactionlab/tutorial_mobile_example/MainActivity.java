package io.interactionlab.tutorial_mobile_example;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import static io.interactionlab.tutorial_mobile_example.Constants.SHARED_PREF_ID;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    // Variables for the draw view.
    private static final int PIXEL_WIDTH = 28;
    private TextView tvResult;
    private float lastX;
    private float lastY;
    private DrawModel drawModel;
    private DrawView drawView;
    private PointF tmpPoint = new PointF();

    // Download view
    private ProgressDialog progrssDialog;
    private Button downloadButton;
    private Button infoButton;


    private EditText etOutput;
    private EditText etServer;
    private EditText etInput;

    // Classifier
    private NumberClassifier numberClassifier;

    // File download path

    private String MODEL_FILE = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File path = MainActivity.this.getExternalFilesDir(null);
        MODEL_FILE = path.getAbsolutePath() + "/downloaded_model.pb";


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

        downloadButton = (Button) findViewById(R.id.button_download);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // retrieve saved server url
                SharedPreferences prefs = getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE);
                String serverURL = prefs.getString("server", "http://");

                new FileDownloadTask().execute(serverURL);
            }
        });

        infoButton = (Button) findViewById(R.id.button_info);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Info");
                alertDialog.setMessage("To use the app you need to train your own MNIST models. You can use the following python code to train a model: https://github.com/interactionlab/imui-tutorial/");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Open Link",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String url = "https://github.com/interactionlab/imui-tutorial/";
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);
                                dialog.dismiss();
                            }
                        });


                alertDialog.show();
            }
        });


        tvResult = (TextView) findViewById(R.id.text_result);

        promptModelSettings();
    }


    private void promptModelSettings() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE);
        String servername = prefs.getString("server", "http://");
        String inputNode = prefs.getString("input_node", "dense_1_input");
        String outputNode = prefs.getString("output_node", "output_node0");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //you should edit this to fit your needs
        builder.setTitle("Enter model settings:");

        final EditText etServer = new EditText(this);
        etServer.setHint("Server Address");
        etServer.setText(servername);
        final EditText etInput = new EditText(this);
        etInput.setHint("Input Node");
        etInput.setText(inputNode);
        final EditText etOutput = new EditText(this);
        etOutput.setHint("Output Node");
        etOutput.setText(outputNode);

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(etServer);
        lay.addView(etInput);
        lay.addView(etOutput);
        builder.setView(lay);

        // Set up the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE).edit();
                editor.putString("server", etServer.getText().toString());
                editor.putString("input_node", etInput.getText().toString());
                editor.putString("output_node", etOutput.getText().toString());

                editor.apply();
                dialog.cancel();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        builder.create();
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
                progrssDialog.setCancelable(false);
                progrssDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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
                // delete previous model first if available
                File fdelete = new File(MODEL_FILE);
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        System.out.println("file Deleted.");
                    } else {
                        System.out.println("file not Deleted.");
                    }
                }


                URL url = new URL(f_url[0]);

                URLConnection connection = url.openConnection();
                connection.connect();
                int lenghtOfFile = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream(MODEL_FILE);

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
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Invalid file.");
                        alertDialog.setMessage("Click OK to enter a new server URL.");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        promptModelSettings();
                                    }
                                });
                        alertDialog.show();
                    }
                });
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
                fileInputStream = new FileInputStream(MODEL_FILE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (fileInputStream != null) {
                numberClassifier = new NumberClassifier(fileInputStream, MainActivity.this);
            }

        }

    }
}
