package com.daniyal.appspro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.daniyal.appspro.facedetectionactivity.FaceDetectionActivity;
import com.daniyal.appspro.verifyingperson.VerifyingUserActivity;

public class MainActivity extends AppCompatActivity {

    private Button btn_register, btn_verify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialising();
        clickListener();

    }

    private void clickListener() {

        btn_register.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FaceDetectionActivity.class));
        });

        btn_verify.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, VerifyingUserActivity.class));
        });

    }

    private void initialising() {
        btn_register= findViewById(R.id.btn_register);
        btn_verify = findViewById(R.id.btn_verify);

    }
}