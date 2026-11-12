package ua.com.merchik.merchik.ServerExchange;

import org.junit.Test;

import java.io.IOException;

import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.*;

public class PhotoUploadCallTest {
    @Test public void competingRequestIsNotSentAndDoesNotAcknowledgePhoto() {
        FakeCall first = new FakeCall();
        FakeCall second = new FakeCall();
        RecordingCallback result = new RecordingCallback();
        PhotoUploadCall.enqueue(first, 2001, "2001.jpg", new RecordingCallback());
        PhotoUploadCall.enqueue(second, 2001, "2001.jpg", result);
        assertEquals(0, second.enqueued);
        assertEquals(0, result.responses);
        assertTrue(result.error instanceof PhotoUploadCall.BusyException);
        first.fail();
        assertUnlocked(2001);
    }

    @Test public void guardRemainsHeldUntilAcknowledgementIsSaved() {
        FakeCall call = new FakeCall();
        PhotoUploadCall.enqueue(call, 2002, "2002.jpg", new RecordingCallback() {
            @Override public void onResponse(Call<String> request, Response<String> response) {
                assertNull(PhotoUploadGate.INSTANCE.tryPhoto(2002, "2002.jpg"));
                super.onResponse(request, response);
            }
        });
        call.respond();
        assertUnlocked(2002);
    }

    @Test public void networkFailureReleasesGuard() {
        FakeCall call = new FakeCall();
        RecordingCallback result = new RecordingCallback();
        PhotoUploadCall.enqueue(call, 2003, "2003.jpg", result);
        call.fail();
        assertEquals(1, result.failures);
        assertUnlocked(2003);
    }

    @Test public void synchronousEnqueueFailureReleasesGuard() {
        FakeCall call = new FakeCall();
        call.throwOnEnqueue = true;
        RecordingCallback result = new RecordingCallback();
        PhotoUploadCall.enqueue(call, 2004, "2004.jpg", result);
        assertEquals(1, result.failures);
        assertUnlocked(2004);
    }

    @Test public void exceptionWhileSavingAcknowledgementStillReleasesGuard() {
        FakeCall call = new FakeCall();
        PhotoUploadCall.enqueue(call, 2005, "2005.jpg", new RecordingCallback() {
            @Override public void onResponse(Call<String> request, Response<String> response) {
                throw new IllegalStateException("DB error");
            }
        });
        try {
            call.respond();
            fail("Expected DB error");
        } catch (IllegalStateException expected) {
            assertEquals("DB error", expected.getMessage());
        }
        assertUnlocked(2005);
    }

    @Test public void cancellationWaitsForTransportCallbackBeforeUnlocking() {
        FakeCall call = new FakeCall();
        PhotoUploadCall.enqueue(call, 2006, "2006.jpg", new RecordingCallback());
        call.cancel();
        assertNull(PhotoUploadGate.INSTANCE.tryPhoto(2006, "2006.jpg"));
        call.fail();
        assertUnlocked(2006);
    }

    @Test public void duplicateCallbackDoesNotAcknowledgeTwiceOrUnlockNextRequest() {
        FakeCall first = new FakeCall();
        RecordingCallback result = new RecordingCallback();
        PhotoUploadCall.enqueue(first, 2007, "2007.jpg", result);
        first.respond();
        FakeCall next = new FakeCall();
        PhotoUploadCall.enqueue(next, 2007, "2007.jpg", new RecordingCallback());
        first.respond();
        first.fail();
        assertEquals(1, result.responses);
        assertEquals(0, result.failures);
        assertNull(PhotoUploadGate.INSTANCE.tryPhoto(2007, "2007.jpg"));
        next.fail();
        assertUnlocked(2007);
    }

    @Test public void distinctPhotosCanUseExistingManualUploadsIndependently() {
        FakeCall first = new FakeCall();
        FakeCall second = new FakeCall();
        PhotoUploadCall.enqueue(first, 2008, "2008.jpg", new RecordingCallback());
        PhotoUploadCall.enqueue(second, 2009, "2009.jpg", new RecordingCallback());
        assertEquals(1, first.enqueued);
        assertEquals(1, second.enqueued);
        first.fail();
        second.fail();
        assertUnlocked(2008);
        assertUnlocked(2009);
    }

    private static void assertUnlocked(int id) {
        PhotoUploadGate.Lease lease = PhotoUploadGate.INSTANCE.tryPhoto(id, id + ".jpg");
        assertNotNull(lease);
        lease.close();
    }

    private static class RecordingCallback implements Callback<String> {
        int responses;
        int failures;
        Throwable error;

        @Override public void onResponse(Call<String> call, Response<String> response) { responses++; }
        @Override public void onFailure(Call<String> call, Throwable error) {
            failures++;
            this.error = error;
        }
    }

    private static class FakeCall implements Call<String> {
        Callback<String> callback;
        int enqueued;
        boolean cancelled;
        boolean throwOnEnqueue;

        @Override public void enqueue(Callback<String> callback) {
            if (throwOnEnqueue) throw new IllegalStateException("Enqueue failed");
            this.callback = callback;
            enqueued++;
        }
        void respond() { callback.onResponse(this, Response.success("ok")); }
        void fail() { callback.onFailure(this, new IOException("Network unavailable")); }
        @Override public Response<String> execute() { throw new UnsupportedOperationException(); }
        @Override public boolean isExecuted() { return enqueued > 0; }
        @Override public void cancel() { cancelled = true; }
        @Override public boolean isCanceled() { return cancelled; }
        @Override public Call<String> clone() { return new FakeCall(); }
        @Override public Request request() { return new Request.Builder().url("https://localhost/").build(); }
        @Override public Timeout timeout() { return new Timeout(); }
    }
}
