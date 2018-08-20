package ru.facereg.facerecognitiontest.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.vision.CameraSource;

import java.util.HashSet;
import java.util.Set;

import ru.facereg.facerecognitiontest.FaceDetectionActivity;
import ru.facereg.facerecognitiontest.ICreatePicture;

/**
 * Вьюшка для отображения серии графических элементов
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
    private Set<Graphic> mGraphics = new HashSet<>();
    private boolean isNeedGetPhoto = false;

    public static abstract class Graphic {
        private GraphicOverlay mOverlay;

        public Graphic(GraphicOverlay overlay) {
            mOverlay = overlay;
        }

        /**
         * Отрисовываем рамку вокруг лица
         *
         * @param canvas
         */
        public abstract void draw(Canvas canvas);

        public float scaleX(float horizontal) {
            return horizontal * mOverlay.mWidthScaleFactor;
        }

        public float scaleY(float vertiсal) {
            return vertiсal * mOverlay.mHeightScaleFactor;
        }

        public float translateX(float x) {
            if (mOverlay.mFacing == CameraSource.CAMERA_FACING_FRONT) {
                return mOverlay.getWidth() - scaleX(x);
            } else {
                return scaleX(x);
            }
        }

        public float translateY(float y) {
            return scaleY(y);
        }

        /**
         * Перерисовываем графику
         */
        public void postInvalidate() {
            mOverlay.postInvalidate();
        }
    }

    public GraphicOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Отчищаем все overlay
     */
    public void clear() {
        synchronized (mLock) {
            mGraphics.clear();
        }
        postInvalidate();
    }

    public void add(Graphic graphic) {
        synchronized (mLock) {
            mGraphics.add(graphic);
        }
        postInvalidate();
    }

    public void remove(Graphic graphic){
        synchronized (mLock){
            mGraphics.remove(graphic);
        }
        postInvalidate();
    }

    /**
     * Задаем данные о работе камеры
     *
     * @param previewWidth
     * @param previewHeight
     * @param facing
     */
    public void setCameraInfo(int previewWidth, int previewHeight, int facing) {
        synchronized (mLock) {
            mPreviewWidth = previewWidth;
            mPreviewHeight = previewHeight;
            mFacing = facing;
        }
        postInvalidate();
    }

    public void setNeedGetPhoto(boolean needGetPhoto) {
        isNeedGetPhoto = needGetPhoto;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isNeedGetPhoto){
            ICreatePicture createPicture = (ICreatePicture) getContext();
            createPicture.onCreatePicture();
        }

        synchronized (mLock) {
            if (mPreviewWidth != 0
                    && mPreviewHeight != 0) {
                mWidthScaleFactor = (float) canvas.getWidth() / (float) mPreviewWidth;
                mHeightScaleFactor = (float) canvas.getHeight() / (float) mPreviewHeight;
            }
            for (Graphic graphic : mGraphics) {
                graphic.draw(canvas);
            }
        }
    }
}
