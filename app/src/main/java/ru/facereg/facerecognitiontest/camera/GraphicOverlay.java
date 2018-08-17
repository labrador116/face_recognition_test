package ru.facereg.facerecognitiontest.camera;

import android.content.Context;
import android.view.View;

import com.google.android.gms.vision.CameraSource;

/**
 * Вьюшка для отображения серии графических изображений
 *
 * @author Markin Andrey on 17.08.2018.
 */
public class GraphicOverlay extends View {

    private final Object mLock = new Object();
    private int mPreviewWidth;
    private float mWidthScaleFactor = 1.0f;
    private int mPreviewHeight;
    private float mHeightScaleFactor = 1.0f;
    private int mFacing = CameraSource.CAMERA_FACING_BACK;

    public GraphicOverlay(Context context) {
        super(context);
    }
}
