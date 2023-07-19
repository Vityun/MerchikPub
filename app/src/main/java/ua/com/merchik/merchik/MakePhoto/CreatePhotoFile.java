package ua.com.merchik.merchik.MakePhoto;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreatePhotoFile {


    public File createDefaultPhotoFile(Activity activity, Uri uri) {
        try {
            File photo = null;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPG_FROM_GALLERY" + timeStamp + "_";

                File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                try {
                    photo = File.createTempFile(imageFileName, ".jpg", storageDir);
                } catch (IOException e) {
                    // Обработка ошибки создания временного файла
                    e.printStackTrace();
                    // Можно выбросить исключение на уровень выше или использовать обратный вызов (callback)
                }

                if (photo != null && uri != null) {
                    try {
                        InputStream inputStream = activity.getContentResolver().openInputStream(uri);
                        OutputStream outputStream = new FileOutputStream(photo);

                        byte[] buffer = new byte[1024];
                        int bytesRead;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        outputStream.close();
                        inputStream.close();

                        long fileSize = photo.length();
                        Log.e("createDefaultPhotoFile", "fileSize: " + fileSize);

                        Uri res = FileProvider.getUriForFile(activity, "ua.com.merchik.merchik.provider", photo);

                        return photo;
                    } catch (IOException e) {
                        // Обработка ошибки записи данных в файл
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            // Обработка остальных исключений
            e.printStackTrace();
        }
        return null;
    }

}
