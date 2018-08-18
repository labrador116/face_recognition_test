package ru.facereg.facerecognitiontest;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.vision.face.Face;

import ru.facereg.facerecognitiontest.camera.GraphicOverlay;

/**
 * Сущность для отображения рамки вокруг лица
 *
 * @author Markin Andrey on 18.08.2018.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {

    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private Paint mFacePositionPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private float mFaceHapiness;

    public FaceGraphic(GraphicOverlay overlay) {
        super(overlay);
        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(Color.RED);
        mBoxPaint = new Paint();
        mBoxPaint.setColor(Color.RED);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mFace == null) {
            return;
        }

        //рисуем точку по центру
        float x = translateX(mFace.getPosition().x + mFace.getWidth() / 2);
        float y = translateY(mFace.getPosition().y + mFace.getHeight() / 2);
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);

        //рисуем рамку
        float xOffset = scaleX(mFace.getWidth() / 2.0f);
        float yOffset = scaleY(mFace.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);
    }

    public void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }
}
