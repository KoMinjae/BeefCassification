package com.example.jpkim.simpletensorflowdemo;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraListener;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {
    // INPUT SIZE, MEAN, STD values are taken from label_image source
    private static final int INPUT_SIZE = 299;
    private static final int IMAGE_MEAN = 0;
    private static final float IMAGE_STD = 255.0f;
    private static final String INPUT_NAME = "Mul";
    private static final String OUTPUT_NAME = "final_result";

    private static final String MODEL_FILE = "file:///android_asset/graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/labels.txt";

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

    private CameraView cameraView;
    private TextView txtResult;
    private ImageView imgResult;
    private Button btnDetect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_camera);


        cameraView  = (CameraView)findViewById(R.id.cameraView);
        cameraView.bringToFront();
        txtResult  = (TextView)findViewById(R.id.txtResult);
        imgResult = (ImageView)findViewById(R.id.imgResult);
        imgResult.bringToFront();

        Button btnGallery = (Button) findViewById(R.id.btnGallery);
        Button btnCamera = (Button) findViewById(R.id.btnCamera);

        btnDetect = (Button)findViewById(R.id.btnDetect);

        cameraView.setVisibility(View.INVISIBLE);  // hides cameraview on startup, shows when camera button clicked
        btnDetect.setVisibility(View.INVISIBLE);   // hide camera detect button on startup

        // btn events delegation
        btnCamera.setOnClickListener(this);
        btnDetect.setOnClickListener(this);

        // initialize tensorflow async
        initTensorFlowAndLoadModel();

        // permission check & request if needed
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();

        // cameraview library has its own permission check method
        cameraView.setPermissions(CameraKit.Constants.PERMISSIONS_PICTURE);

        // invoke tensorflow inference when picture taken from camera
        cameraView.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                super.onPictureTaken(picture);

                Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);

                recognize_bitmap(bitmap);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    public void onClick(View v) {
        // define which methods to call when buttons in view clicked
        int id = v.getId();

        switch(id) {
            case R.id.btnCamera:
                DetectImageFromCamera();
                break;
            case R.id.btnDetect:
                cameraView.captureImage();
                cameraView.setVisibility(View.INVISIBLE);
                btnDetect.setVisibility(View.INVISIBLE);
                imgResult.setVisibility(View.VISIBLE);
                txtResult.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void DetectImageFromCamera(){
        // show cameraview and detect button when source from camera button clicked
        imgResult.setVisibility(View.INVISIBLE);
        txtResult.setVisibility(View.INVISIBLE);
        cameraView.setVisibility(View.VISIBLE);
        btnDetect.setVisibility(View.VISIBLE);
        if(!cameraView.isActivated())cameraView.start();
    }

    // recognize bitmap and get results
    private void recognize_bitmap(Bitmap bitmap) {

        // create a bitmap scaled to INPUT_SIZE
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

        // returned value stores in Classifier.Recognition format
        // which provides various methods to parse the result,
        // but I'm going to show raw result here.
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        final Bitmap finalBitmap = bitmap;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imgResult.setImageBitmap(finalBitmap);
            }
        });
        txtResult.setText(results.toString());
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
