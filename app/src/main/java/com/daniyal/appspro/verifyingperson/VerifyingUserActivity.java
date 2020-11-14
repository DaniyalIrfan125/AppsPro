package com.daniyal.appspro.verifyingperson;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.daniyal.appspro.facedetectionactivity.MlKitFacesAnalyzer;
import com.daniyal.appspro.models.RecoginizeApiResponse;
import com.daniyal.appspro.networklibrary.ApiClient;
import com.daniyal.appspro.networklibrary.ApiInterface;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyingUserActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_PERMISSION = 101;
    public static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private TextureView tv;
    private ImageView iv;
    private static final String TAG = "FaceDetectionActivity";
    private Boolean isFaceDetected = false, isApiCalled = false;
    private TextView tv_name, tv_face;
    private Button btnCapture;
    TextToSpeech t1;


    public static CameraX.LensFacing lens = CameraX.LensFacing.FRONT;
    private RelativeLayout progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifying_user);

        t1 = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                t1.setLanguage(Locale.UK);
            }
        });

        progress = findViewById(R.id.progress);
        tv_name = findViewById(R.id.tv_name);
        tv_face = findViewById(R.id.tv_face);
        btnCapture = findViewById(R.id.btnCapture);

        tv = findViewById(R.id.face_texture_view);
        iv = findViewById(R.id.face_image_view);
        if (allPermissionsGranted()) {
            tv.post(this::startCamera);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION);
        }


    }

    public void onPause() {
        if (t1 != null) {
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
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


        btnCapture.setOnClickListener(v -> {
            if (isFaceDetected) {
                File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".jpg");

                imgCap.takePicture(file, (Runnable::run), new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        String msg = "Image is saved at: " + file.getAbsolutePath();
                        runOnUiThread(() -> {
                            if (!isApiCalled) {

                                try {
                                    File compressedImageFile = new Compressor(VerifyingUserActivity.this).compressToFile(file);
                                    callApi(compressedImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        Log.i(TAG, msg);
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.ImageCaptureError imageCaptureError, @NonNull String message, @Nullable Throwable cause) {
                        runOnUiThread(() -> Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show());
                    }
                });
            } else {
                Toast.makeText(this, "No face detetcted", Toast.LENGTH_SHORT).show();
            }

        });


        ImageAnalysis imageAnalysis = new ImageAnalysis(iac);
        imageAnalysis.setAnalyzer(Runnable::run, new MlKitFacesAnalyzer(tv, iv, lens, isStatus -> {

            if (isStatus) {
                isFaceDetected = isStatus;
                tv_face.setText("Face Detected");
                tv_face.setTextColor(ContextCompat.getColor(this, R.color.colorGreen));

            } else {
                isFaceDetected = isStatus;
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

    private void callApi(File file) {
        isApiCalled = true;
        progress.setVisibility(View.VISIBLE);
        // Map is used to multipart the file using okhttp3.RequestBody
        Map<String, RequestBody> map = new HashMap<>();
        //       File file = new File(FileUtils.getPath(getContext(), imageUri));
        // Parsing any Media type file
        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
        map.put("file\"; filename=\"" + file.getName() + "\"", requestBody);

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<RecoginizeApiResponse> call = apiService.recoginizePerson(map);
        call.enqueue(new Callback<RecoginizeApiResponse>() {
            @Override
            public void onResponse(Call<RecoginizeApiResponse> call, Response<RecoginizeApiResponse> response) {

                progress.setVisibility(View.GONE);
                isApiCalled = false;

                if (response.isSuccessful()) {
                    String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    t1.speak("Welcome " + response.body().getData().getName() + " Current time is " + currentTime, TextToSpeech.QUEUE_FLUSH, null);
                    tv_name.setText("Welcome " + response.body().getData().getName() + " Current time is " + currentTime);
                } else {
                    tv_name.setText(response.message());
                    t1.speak(response.message(), TextToSpeech.QUEUE_FLUSH, null);

                }
            }

            @Override
            public void onFailure(Call<RecoginizeApiResponse> call, Throwable t) {
                // Log error here since request failed
                Log.e("test", t.toString());
                progress.setVisibility(View.GONE);
                isApiCalled = false;
                Toast.makeText(VerifyingUserActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


//    @Override
//    protected void onDestroy() {
//        if (fileCompressor != null) {
//            fileCompressor.dispose();
//        }
//        super.onDestroy();
//    }


}