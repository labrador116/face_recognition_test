package ru.facereg.facerecognitiontest;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import ru.facereg.facerecognitiontest.activities.ProcessingActivity;
import ru.facereg.facerecognitiontest.utils.FileUtils;

/**
 * @author Markin Andrey on 21.08.2018.
 */
public class ProcessingService extends IntentService {
    public static final String SUCCESS_RESULT = "SUCCESS";
    public static final String FAILED_RESULT = "FAILED";

    private int mFirstName = 0;
    private int mSecondName = 1;

    public ProcessingService() {
        super("ProcessingService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String storageDir = Environment.getExternalStorageDirectory().toString();
        File folder = new File(storageDir + File.separator + "face_img");
        final File[] files = folder.listFiles();
        String result = SUCCESS_RESULT;
        for (int i = 0; i < files.length; i++) {
            Bitmap bitmap = BitmapFactory.decodeFile(files[i].getAbsolutePath());
            Bitmap newBitmap = convertColorIntoBlackAndWhiteImage(bitmap);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            newBitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
            final byte[] bytes = byteArrayOutputStream.toByteArray();
            try {
                FileUtils.saveImage(bytes,
                        String.valueOf(mFirstName) + "_" + String.valueOf(mSecondName),
                        "png");
                if (mSecondName >= 5) {
                    mSecondName = 1;
                    mFirstName++;
                } else {
                    mSecondName++;
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = FAILED_RESULT;
            }
        }

        Intent intentResult = new Intent(ProcessingActivity.BROADCAST_FILTER);
        intentResult.putExtra(ProcessingActivity.SERVICE_RESULT, result);

        sendBroadcast(intentResult);

    }

    private Bitmap convertColorIntoBlackAndWhiteImage(Bitmap orginalBitmap) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);

        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(
                colorMatrix);

        Bitmap blackAndWhiteBitmap = orginalBitmap.copy(
                Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setColorFilter(colorMatrixFilter);

        Canvas canvas = new Canvas(blackAndWhiteBitmap);
        canvas.drawBitmap(blackAndWhiteBitmap, 0, 0, paint);

        return blackAndWhiteBitmap;
    }
}
