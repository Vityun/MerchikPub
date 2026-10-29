package ua.com.merchik.merchik.retrofit;

import org.junit.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;
import ua.com.merchik.merchik.ServerExchange.ExchangeRunTracker;
import ua.com.merchik.merchik.ServerExchange.ExchangeWork;
import static org.junit.Assert.*;

public class ExchangeCallAdapterFactoryTest {
    private interface Api {
        Call<String> metadata();
        @Multipart Call<String> uploadPhoto();
        Call<okhttp3.ResponseBody> downloadPhoto();
    }

    private static class FakeCall implements Call<String> {
        Callback<String> callback;
        boolean executed;
        boolean cancelled;
        boolean reject;
        public void enqueue(Callback<String> callback) {
            if (reject) throw new IllegalStateException("Rejected");
            this.callback = callback;
            executed = true;
        }
        public Response<String> execute() { return Response.success("ok"); }
        public void cancel() { cancelled = true; }
        public boolean isCanceled() { return cancelled; }
        public boolean isExecuted() { return executed; }
        public Call<String> clone() { return new FakeCall(); }
        public Request request() { return new Request.Builder().url("https://example.com/").build(); }
        public Timeout timeout() { return Timeout.NONE; }
    }

    private Callback<String> callback(Runnable result) {
        return new Callback<String>() {
            public void onResponse(Call<String> call, Response<String> response) { result.run(); }
            public void onFailure(Call<String> call, Throwable error) { result.run(); }
        };
    }

    @Test public void waitsForCallbackAndDatabaseContinuation() {
        FakeCall delegate = new FakeCall();
        ExchangeRunTracker.Ticket[] db = {null};
        ExchangeRunTracker.Ticket root = ExchangeWork.TRACKER.beginRun(() -> { });
        new ExchangeCallAdapterFactory.TrackedCall<>(delegate).enqueue(callback(() -> {
            assertTrue(ExchangeWork.TRACKER.isRunning());
            db[0] = ExchangeWork.TRACKER.beginWork();
        }));
        root.close();
        assertTrue(ExchangeWork.TRACKER.isRunning());
        delegate.callback.onResponse(delegate, Response.success("ok"));
        assertTrue(ExchangeWork.TRACKER.isRunning());
        db[0].close();
        assertFalse(ExchangeWork.TRACKER.isRunning());
    }

    @Test public void failureReleasesRun() {
        FakeCall delegate = new FakeCall();
        ExchangeRunTracker.Ticket root = ExchangeWork.TRACKER.beginRun(() -> { });
        new ExchangeCallAdapterFactory.TrackedCall<>(delegate).enqueue(callback(() -> { }));
        root.close();
        delegate.callback.onFailure(delegate, new IOException("offline"));
        assertFalse(ExchangeWork.TRACKER.isRunning());
    }

    @Test public void cancellationDelegatesAndReleasesOnFailure() {
        FakeCall delegate = new FakeCall();
        Call<String> call = new ExchangeCallAdapterFactory.TrackedCall<>(delegate);
        ExchangeRunTracker.Ticket root = ExchangeWork.TRACKER.beginRun(() -> { });
        call.enqueue(callback(() -> { }));
        root.close();
        call.cancel();
        assertTrue(delegate.cancelled);
        delegate.callback.onFailure(delegate, new IOException("Cancelled"));
        assertFalse(ExchangeWork.TRACKER.isRunning());
    }

    @Test public void throwingCallbackStillReleasesRun() {
        FakeCall delegate = new FakeCall();
        ExchangeRunTracker.Ticket root = ExchangeWork.TRACKER.beginRun(() -> { });
        new ExchangeCallAdapterFactory.TrackedCall<>(delegate).enqueue(callback(() -> {
            throw new IllegalStateException("DB failed");
        }));
        root.close();
        try {
            delegate.callback.onResponse(delegate, Response.success("ok"));
            fail();
        } catch (IllegalStateException expected) { }
        assertFalse(ExchangeWork.TRACKER.isRunning());
    }

    @Test public void enqueueExceptionStillReleasesRun() {
        FakeCall delegate = new FakeCall();
        delegate.reject = true;
        ExchangeRunTracker.Ticket root = ExchangeWork.TRACKER.beginRun(() -> { });
        try {
            new ExchangeCallAdapterFactory.TrackedCall<>(delegate).enqueue(callback(() -> { }));
            fail();
        } catch (IllegalStateException expected) { }
        root.close();
        assertFalse(ExchangeWork.TRACKER.isRunning());
    }

    @Test public void factoryWaitsForAdaptedCallbackExecutor() throws Exception {
        List<Runnable> main = new ArrayList<>();
        ExchangeCallAdapterFactory factory = new ExchangeCallAdapterFactory();
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://example.com/")
                .callbackExecutor(main::add).addCallAdapterFactory(factory).build();
        java.lang.reflect.Method method = Api.class.getDeclaredMethod("metadata");
        @SuppressWarnings("unchecked")
        CallAdapter<String, Call<String>> adapter = (CallAdapter<String, Call<String>>)
                factory.get(method.getGenericReturnType(), method.getAnnotations(), retrofit);
        FakeCall network = new FakeCall();
        ExchangeRunTracker.Ticket root = ExchangeWork.TRACKER.beginRun(() -> { });
        adapter.adapt(network).enqueue(callback(() -> assertTrue(ExchangeWork.TRACKER.isRunning())));
        root.close();
        network.callback.onResponse(network, Response.success("ok"));
        assertTrue(ExchangeWork.TRACKER.isRunning());
        assertEquals(1, main.size());
        main.remove(0).run();
        assertFalse(ExchangeWork.TRACKER.isRunning());
    }

    @Test public void factoryDoesNotTrackImageFilesOrMultipartUploads() throws Exception {
        ExchangeCallAdapterFactory factory = new ExchangeCallAdapterFactory();
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://example.com/")
                .callbackExecutor(Runnable::run).addCallAdapterFactory(factory).build();
        for (String name : new String[]{"uploadPhoto", "downloadPhoto"}) {
            java.lang.reflect.Method method = Api.class.getDeclaredMethod(name);
            assertNull(factory.get(method.getGenericReturnType(), method.getAnnotations(), retrofit));
        }
    }
}
