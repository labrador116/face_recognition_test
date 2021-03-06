package ru.facereg.facerecognitiontest.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Markin Andrey on 19.08.2018.
 */
public class FileUtils {
    public static File saveImage(byte[] content, String name, String extension) throws IOException {
        String storageDir = Environment.getExternalStorageDirectory().toString();
        File folder = new File(storageDir + File.separator + "face_img");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File image = new File(folder, name + "." + extension);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(image);
            out.write(content);
            out.close();
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return image;
    }

    public static void clearPhotoFolder() {
        String storageDir = Environment.getExternalStorageDirectory().toString();
        File folder = new File(storageDir + File.separator + "face_img");
        if (folder.isDirectory()) {
            String[] files = folder.list();
            for (int i = 0; i < files.length; i++) {
                new File(folder, files[i]).delete();
            }
        }
    }
}
