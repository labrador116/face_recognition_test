package ru.facereg.facerecognitiontest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.vision.CameraSource;

public class FaceDetectionActivity extends AppCompatActivity {
    private CameraSource mCameraSource = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
