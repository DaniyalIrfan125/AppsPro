package com.daniyal.appspro.registrationactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.daniyal.appspro.FileUtils;
import com.daniyal.appspro.MainActivity;
import com.daniyal.appspro.R;
import com.daniyal.appspro.facedetectionactivity.FaceDetectionActivity;
import com.daniyal.appspro.models.AddPersonResponse;
import com.daniyal.appspro.models.ImageFilesModel;
import com.daniyal.appspro.models.ImageFilesPathModel;
import com.daniyal.appspro.networklibrary.ApiClient;
import com.daniyal.appspro.networklibrary.ApiInterface;
import com.google.android.gms.common.util.Base64Utils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity {

    private EditText ed_id, ed_name;
    private Button btn_send;
    private ImageView img1, img2, img3;
    //private ImageFilesModel imageFilesModel;
    private ImageFilesPathModel imageFilesPathModel;
    private RelativeLayout progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);


        //  imageFilesModel = (ImageFilesModel) getIntent().getSerializableExtra("obj");
        imageFilesPathModel = (ImageFilesPathModel) getIntent().getSerializableExtra("objpath");

        initialising();
        clickListener();
    }

    private void clickListener() {
        btn_send.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(ed_id.getText())) {

                if (!TextUtils.isEmpty(ed_name.getText())) {

                    callApi();


                } else {
                    ed_name.setError("Please write name!");
                }

            } else {
                ed_id.setError("Please write id!");
            }
        });
    }

    private void callApi() {
        progress.setVisibility(View.VISIBLE);
        // Map is used to multipart the file using okhttp3.RequestBody
        Map<String, RequestBody> map = new HashMap<>();
        File file = new File(imageFilesPathModel.getImage1Path());
        File file2 = new File(imageFilesPathModel.getImage2Path());
        File file3 = new File(imageFilesPathModel.getImage3Path());


        // Parsing any Media type file
        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
        map.put("image1\"; filename=\"" + file.getName() + "\"", requestBody);

        RequestBody requestBody2 = RequestBody.create(MediaType.parse("*/*"), file2);
        map.put("image2\"; filename=\"" + file2.getName() + "\"", requestBody2);

        RequestBody requestBody3 = RequestBody.create(MediaType.parse("*/*"), file3);
        map.put("image3\"; filename=\"" + file3.getName() + "\"", requestBody3);

        RequestBody name = RequestBody.create(MediaType.parse("text/plain"),
                ed_name.getText()
                        .toString());
        RequestBody id = RequestBody.create(MediaType.parse("text/plain"),
                ed_id.getText()
                        .toString());


        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<AddPersonResponse> call = apiService.addPerson(map, id, name);
        call.enqueue(new Callback<AddPersonResponse>() {
            @Override
            public void onResponse(Call<AddPersonResponse> call, Response<AddPersonResponse> response) {

                progress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    Toast.makeText(RegistrationActivity.this, "Successfully registered!", Toast.LENGTH_SHORT).show();


                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<AddPersonResponse> call, Throwable t) {

                progress.setVisibility(View.GONE);
                Toast.makeText(RegistrationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initialising() {
        ed_id = findViewById(R.id.ed_id);
        ed_name = findViewById(R.id.ed_name);
        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);
        progress = findViewById(R.id.progress);

        btn_send = findViewById(R.id.btn_register);


        if (imageFilesPathModel != null) {
            File file = new File(imageFilesPathModel.getImage1Path());
            File file2 = new File(imageFilesPathModel.getImage2Path());
            File file3 = new File(imageFilesPathModel.getImage3Path());
            loadImageFirst(file);
            loadImageSecond(file2);
            loadImageThird(file3);
        }
    }

    private void loadImageFirst(File file) {
        Picasso.get().load(file).memoryPolicy(MemoryPolicy.NO_CACHE).fit().into(img1);
    }

    private void loadImageSecond(File file) {
        Picasso.get().load(file).memoryPolicy(MemoryPolicy.NO_CACHE).fit().into(img2);
    }

    private void loadImageThird(File file) {
        Picasso.get().load(file).memoryPolicy(MemoryPolicy.NO_CACHE).fit().into(img3);
    }
}