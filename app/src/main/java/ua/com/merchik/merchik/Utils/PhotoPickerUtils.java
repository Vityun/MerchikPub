package ua.com.merchik.merchik.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class PhotoPickerUtils {

    private static final int ANDROID_13 = 33;
    private static final String ACTION_PICK_IMAGES = "android.provider.action.PICK_IMAGES";
    private static final String MIME_IMAGE = "image/*";
    private static final String DEFAULT_EXTENSION = ".jpg";

    private PhotoPickerUtils() {
    }

    public static Intent createSingleImageIntent() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= ANDROID_13) {
            intent = new Intent(ACTION_PICK_IMAGES);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        }

        intent.setType(MIME_IMAGE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    public static Intent createSingleImageChooser() {
        return Intent.createChooser(createSingleImageIntent(), "Select Picture");
    }

    public static void persistReadPermissionIfPossible(@NonNull Context context, Intent data) {
        if (data == null || data.getData() == null) {
            return;
        }

        try {
            int flags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            if (flags != 0) {
                context.getContentResolver().takePersistableUriPermission(data.getData(), flags);
            }
        } catch (Exception ignored) {
            // Photo Picker grants are temporary; ACTION_OPEN_DOCUMENT grants can be persisted.
        }
    }

    public static File copyPickedImageToFile(@NonNull Context context, @NonNull Uri uri) throws IOException {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            storageDir = context.getCacheDir();
        }
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw new IOException("Can not create image storage dir: " + storageDir);
        }

        File photo = File.createTempFile(
                "picked_image_" + System.currentTimeMillis() + "_",
                resolveExtension(context, uri),
                storageDir
        );

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(photo)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Can not open picked image: " + uri);
            }

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        if (photo.length() <= 0) {
            throw new IOException("Picked image is empty: " + uri);
        }
        return photo;
    }

    private static String resolveExtension(Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        String extension = mimeType == null ? null : MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        if (extension == null || extension.trim().isEmpty()) {
            return DEFAULT_EXTENSION;
        }
        return "." + extension;
    }
}
