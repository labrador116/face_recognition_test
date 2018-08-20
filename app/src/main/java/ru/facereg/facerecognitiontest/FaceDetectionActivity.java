package ru.facereg.facerecognitiontest;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import ru.facereg.facerecognitiontest.camera.CameraSourcePreview;
import ru.facereg.facerecognitiontest.camera.GraphicOverlay;
import ru.facereg.facerecognitiontest.utils.FileUtils;

public class FaceDetectionActivity extends AppCompatActivity implements ICreatePicture {
    private static final int REQUEST_CAMERA_PERMISSION = 3;
    private static int PHOTO_NAME = 1;

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private Button mCreatePictureButton;
    private Timer mTimer;
    private LooperThread mLooperThread;
    private boolean isTimerFullyWorked;
    private boolean safeToTakePicture = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLooperThread = new LooperThread();
        mLooperThread.start();
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.overlay);
        mCreatePictureButton = (Button) findViewById(R.id.create_pic_button);
        mCreatePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimer = new Timer();
                mGraphicOverlay.setNeedGetPhoto(true);
                mTimer.schedule(new GetPhotosPeriodTimerTask(), 5000);
            }
        });

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
        if (mTimer != null) {
            mTimer.cancel();
        }
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

    @Override
    public void onCreatePicture() {
            if (mLooperThread.mHandler != null) {
                Message msg = mLooperThread.mHandler.obtainMessage(0);
                mLooperThread.mHandler.sendMessage(msg);
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
            if (mCameraSource != null) {
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

    private void hideButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCreatePictureButton.setVisibility(View.GONE);
            }
        });

    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {

        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mGraphic;

        public GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mGraphic = new FaceGraphic(overlay);
        }

        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face face) {
            mOverlay.add(mGraphic);
            mGraphic.updateFace(face, mCreatePictureButton);
        }

        @Override
        public void onMissing(Detector.Detections<Face> detections) {
            if (!isTimerFullyWorked) {
                clearPhotosIfExist();
            } else {
                isTimerFullyWorked = false;
            }
            PHOTO_NAME = 1;
            mOverlay.setNeedGetPhoto(false);
            hideButton();
            mOverlay.remove(mGraphic);
        }

        @Override
        public void onDone() {
            mOverlay.remove(mGraphic);
        }
    }

    private void clearPhotosIfExist() {
    }

    private class GetPhotosPeriodTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isTimerFullyWorked = true;
                    mGraphicOverlay.setNeedGetPhoto(false);
                    AlertDialog dialog = new AlertDialog.Builder(FaceDetectionActivity.this)
                            .setTitle("Ready!")
                            .setMessage("Photos creation were ready successful")
                            .create();
                    dialog.show();
                }
            });
        }
    }

    private void operation(){
          boolean res =  mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes) {
                    try {
                        FileUtils.saveImage(bytes, String.valueOf(PHOTO_NAME), "png");
                        safeToTakePicture = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            },safeToTakePicture);
          safeToTakePicture = res;
    }

    private class LooperThread extends Thread{
        public Handler mHandler;
        private Object lock = new Object();

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what==0 && safeToTakePicture){
                        operation();
                    }
                }
            };
            Looper.loop();
        }
    }
}
