package ua.com.merchik.merchik.retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;
import ua.com.merchik.merchik.ServerExchange.ExchangeRunTracker;
import ua.com.merchik.merchik.ServerExchange.ExchangeWork;

/** Wraps the adapted Call so Android's main-thread callback is included in the lifetime. */
public final class ExchangeCallAdapterFactory extends CallAdapter.Factory {
    @Override
    public CallAdapter<?, ?> get(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(type) != Call.class) return null;
        for (Annotation annotation : annotations) {
            if (annotation instanceof Multipart) return null;
        }
        @SuppressWarnings("unchecked")
        CallAdapter<Object, Call<Object>> delegate = (CallAdapter<Object, Call<Object>>)
                retrofit.nextCallAdapter(this, type, annotations);
        if (getRawType(delegate.responseType()) == ResponseBody.class) return null;
        return new CallAdapter<Object, Call<Object>>() {
            @Override public Type responseType() { return delegate.responseType(); }
            @Override public Call<Object> adapt(Call<Object> call) {
                return new TrackedCall<>(delegate.adapt(call));
            }
        };
    }

    static final class TrackedCall<T> implements Call<T> {
        private final Call<T> delegate;

        TrackedCall(Call<T> delegate) { this.delegate = delegate; }

        @Override public void enqueue(Callback<T> callback) {
            ExchangeRunTracker.Ticket ticket = ExchangeWork.TRACKER.beginWork();
            try {
                delegate.enqueue(new Callback<T>() {
                    @Override public void onResponse(Call<T> call, Response<T> response) {
                        try { callback.onResponse(TrackedCall.this, response); }
                        finally { ticket.close(); }
                    }
                    @Override public void onFailure(Call<T> call, Throwable error) {
                        try { callback.onFailure(TrackedCall.this, error); }
                        finally { ticket.close(); }
                    }
                });
            } catch (RuntimeException | Error error) {
                ticket.close();
                throw error;
            }
        }

        // Synchronous callers track their enclosing task, including the subsequent DB commit.
        @Override public Response<T> execute() throws IOException { return delegate.execute(); }
        @Override public void cancel() { delegate.cancel(); }
        @Override public boolean isCanceled() { return delegate.isCanceled(); }
        @Override public boolean isExecuted() { return delegate.isExecuted(); }
        @Override public Call<T> clone() { return new TrackedCall<>(delegate.clone()); }
        @Override public Request request() { return delegate.request(); }
        @Override public Timeout timeout() { return delegate.timeout(); }
    }
}
