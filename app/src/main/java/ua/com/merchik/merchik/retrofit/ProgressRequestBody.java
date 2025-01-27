package ua.com.merchik.merchik.retrofit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {

    public interface UploadProgressListener {
        void onProgressUpdate(int percentage);
    }

    private final File file;
    private final String contentType;
    private final UploadProgressListener listener;
    private int lastProgress = 0; // Хранение последнего процента

    public ProgressRequestBody(File file, String contentType, UploadProgressListener listener) {
        this.file = file;
        this.contentType = contentType;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public long contentLength() throws IOException {
        return file.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = file.length();
        byte[] buffer = new byte[2048];
        try (FileInputStream inputStream = new FileInputStream(file)) {
            long uploaded = 0;

            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                uploaded += read;
                sink.write(buffer, 0, read);

                // Рассчитать прогресс и отправить обновление
                if (listener != null) {
                    int progress = (int) (100 * uploaded / fileLength);

                    // Уведомляем только при изменении процента
                    if (listener != null && progress != lastProgress) {
                        listener.onProgressUpdate(progress);
                        lastProgress = progress; // Обновляем последний процент
                    }
                }
            }
        }
    }
}
