package com.daniyal.appspro.facedetectionactivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.daniyal.appspro.R;
import com.daniyal.appspro.models.ImageFilesModel;
import com.daniyal.appspro.models.ImageFilesPathModel;
import com.daniyal.appspro.registrationactivity.RegistrationActivity;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

public class FaceDetectionActivity extends AppCompatActivity {
    private ImageView img1, img2, img3;
    public static final int REQUEST_CODE_PERMISSION = 101;
    public static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private TextureView tv;
    private ImageView iv;
    private static final String TAG = "FaceDetectionActivity";
    private Boolean isFaceDetected = false;
    private String imageFirstPath = "", imageSecondPath = "", imageThirdPath = "";
    private ImageFilesModel imageFilesModel;
    private ImageFilesPathModel imageFilesPathModel;
    private TextView tv_face;

    public static CameraX.LensFacing lens = CameraX.LensFacing.FRONT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);

        imageFilesModel = new ImageFilesModel();
        imageFilesPathModel = new ImageFilesPathModel();

        img1 = findViewById(R.id.img1);
        tv_face = findViewById(R.id.tv_face);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);
        tv = findViewById(R.id.face_texture_view);
        iv = findViewById(R.id.face_image_view);
        if (allPermissionsGranted()) {
            tv.post(this::startCamera);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION);
        }
    }

    @SuppressLint("RestrictedApi")
    private void startCamera() {
        initCamera();
        ImageView ibSwitch = findViewById(R.id.btn_switch_face);
        ibSwitch.setOnClickListener(v -> {
            if (lens == CameraX.LensFacing.FRONT)
                lens = CameraX.LensFacing.BACK;
            else
                lens = CameraX.LensFacing.FRONT;
            try {
                Log.i(TAG, "" + lens);
                CameraX.getCameraWithLensFacing(lens);
                initCamera();
            } catch (CameraInfoUnavailableException e) {
                Log.e(TAG, e.toString());
            }
        });
    }

    private void initCamera() {
        CameraX.unbindAll();
        PreviewConfig pc = new PreviewConfig
                .Builder()
                .setTargetResolution(new Size(tv.getWidth(), tv.getHeight()))
                .setLensFacing(lens)
                .build();

        Preview preview = new Preview(pc);
        preview.setOnPreviewOutputUpdateListener(output -> {
            ViewGroup vg = (ViewGroup) tv.getParent();
            vg.removeView(tv);
            vg.addView(tv, 0);
            tv.setSurfaceTexture(output.getSurfaceTexture());
        });

        ImageAnalysisConfig iac = new ImageAnalysisConfig
                .Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setTargetResolution(new Size(tv.getWidth(), tv.getHeight()))
                .setLensFacing(lens)
                .build();

        ImageCaptureConfig icc = new ImageCaptureConfig
                .Builder()
                .setLensFacing(lens)
                .setTargetResolution(new Size(1280, 1280))
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .build();

        ImageCapture imgCap = new ImageCapture(icc);

        findViewById(R.id.btnCapture).setOnClickListener(v -> {
            File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".jpg");
            if (isFaceDetected) {
                imgCap.takePicture(file, (Runnable::run), new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {

                        runOnUiThread(() -> {
                            if (imageFirstPath.isEmpty()) {
                                imageFirstPath = file.getPath();
                                loadImageFirst(file);


                            } else if (imageSecondPath.isEmpty()) {
                                imageSecondPath = file.getPath();
                                loadImageSecond(file);

                            } else {
                                imageThirdPath = file.getPath();

                                loadImageThird(file);

                            }
                        });


                        String msg = "Image is saved at: " + file.getAbsolutePath();
                        Log.i(TAG, msg);
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.ImageCaptureError imageCaptureError, @NonNull String message, @Nullable Throwable cause) {
                        runOnUiThread(() -> Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show());
                    }
                });
            } else {
                Toast.makeText(FaceDetectionActivity.this, "No Face Detected", Toast.LENGTH_SHORT).show();
            }

        });


        ImageAnalysis imageAnalysis = new ImageAnalysis(iac);
        imageAnalysis.setAnalyzer(Runnable::run, new MlKitFacesAnalyzer(tv, iv, lens, isStatus -> {

            isFaceDetected = isStatus;

            if (isStatus) {
                tv_face.setText("Face Detected");
                tv_face.setTextColor(ContextCompat.getColor(this, R.color.colorGreen));

            } else {
                tv_face.setText("No Face Detected");
                tv_face.setTextColor(ContextCompat.getColor(this, R.color.colorRed));
            }

        }));

        CameraX.bindToLifecycle(this, preview, imgCap, imageAnalysis);


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (allPermissionsGranted()) {
                tv.post(this::startCamera);
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void loadImageFirst(File file) {

        try {
            imageFilesModel.setImage1(file);
            File compressedImageFile = new Compressor(this).compressToFile(file);
            imageFilesPathModel.setImage1Path(compressedImageFile.getAbsolutePath());
            Picasso.get().load(compressedImageFile).placeholder(R.drawable.demo).memoryPolicy(MemoryPolicy.NO_CACHE).fit().into(img1);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadImageSecond(File file) {


        try {
            imageFilesModel.setImage2(file);
            File compressedImageFile = new Compressor(this).compressToFile(file);
            imageFilesPathModel.setImage2Path(compressedImageFile.getAbsolutePath());
            Picasso.get().load(compressedImageFile).placeholder(R.drawable.demo).memoryPolicy(MemoryPolicy.NO_CACHE).fit().into(img2);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void loadImageThird(File file) {

        try {
            imageFilesModel.setImage3(file);
            File compressedImageFile = new Compressor(this).compressToFile(file);
            imageFilesPathModel.setImage3Path(compressedImageFile.getAbsolutePath());
            Picasso.get().load(compressedImageFile).placeholder(R.drawable.demo).memoryPolicy(MemoryPolicy.NO_CACHE).fit().into(img3);

        } catch (IOException e) {
            e.printStackTrace();
        }


        Intent intent = new Intent(FaceDetectionActivity.this, RegistrationActivity.class);
        intent.putExtra("obj", imageFilesModel);
        intent.putExtra("objpath", imageFilesPathModel);
        startActivity(intent);
    }
}