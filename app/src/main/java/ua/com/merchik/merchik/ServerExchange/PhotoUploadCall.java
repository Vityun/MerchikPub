package ua.com.merchik.merchik.ServerExchange;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class PhotoUploadCall {
    private PhotoUploadCall() { }

    public static final class BusyException extends IOException {
        public BusyException() {
            super("Фото вже передається на сервер. Дочекайтеся завершення.");
        }
    }

    public static <T> void enqueue(Call<T> call, int photoId, String path, Callback<T> callback) {
        PhotoUploadGate.Lease lease = PhotoUploadGate.INSTANCE.tryPhoto(photoId, path);
        if (lease == null) {
            callback.onFailure(call, new BusyException());
            return;
        }
        AtomicBoolean completed = new AtomicBoolean();
        Callback<T> guarded = new Callback<T>() {
            @Override
            public void onResponse(Call<T> request, Response<T> response) {
                if (!completed.compareAndSet(false, true)) return;
                try {
                    callback.onResponse(request, response);
                } finally {
                    // Keep the lease until the caller has persisted the upload acknowledgement.
                    lease.close();
                }
            }

            @Override
            public void onFailure(Call<T> request, Throwable error) {
                if (!completed.compareAndSet(false, true)) return;
                try {
                    callback.onFailure(request, error);
                } finally {
                    lease.close();
                }
            }
        };
        try {
            call.enqueue(guarded);
        } catch (RuntimeException error) {
            guarded.onFailure(call, error);
        }
    }
}
