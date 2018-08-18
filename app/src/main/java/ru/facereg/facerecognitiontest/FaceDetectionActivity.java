package ru.facereg.facerecognitiontest;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

import ru.facereg.facerecognitiontest.camera.CameraSourcePreview;
import ru.facereg.facerecognitiontest.camera.GraphicOverlay;

public class FaceDetectionActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 3;

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.overlay);

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCamPermission();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_CAMERA_PERMISSION == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createCameraSource();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("App hasn't permission for use a camera")
                    .setMessage("For use the application need permission for a camera")
                    .setPositiveButton("Close app", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FaceDetectionActivity.this.finish();
                        }
                    });
        }

    }

    private void requestCamPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    private void createCameraSource() {
        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
        detector.setProcessor(new MultiProcessor.Builder<>(
                new GraphicFaceTrackerFactory()).build()

        );
        mCameraSource = new CameraSource.Builder(getApplicationContext(), detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(50.0f)
                .build();
    }

    private void startCameraSource() {
        int isGoogleAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (isGoogleAvailable != ConnectionResult.SUCCESS) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, isGoogleAvailable, 4);
            dialog.show();
        } else {
            if (mCameraSource != null){
                try {
                    mPreview.start(mCameraSource, mGraphicOverlay);
                } catch (IOException e) {
                    e.printStackTrace();
                    mCameraSource.release();
                    mCameraSource = null;
                }
            }
        }
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face>{

        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    private class GraphicFaceTracker extends Tracker<Face>{
        private GraphicOverlay mOverlay;
        private FaceGraphic mGraphic;

        public GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mGraphic = new FaceGraphic(overlay);
        }

        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face face) {
            mOverlay.add(mGraphic);
            mGraphic.updateFace(face);
        }

        @Override
        public void onMissing(Detector.Detections<Face> detections) {
            mOverlay.remove(mGraphic);
        }

        @Override
        public void onDone() {
            mOverlay.remove(mGraphic);
        }
    }
}
