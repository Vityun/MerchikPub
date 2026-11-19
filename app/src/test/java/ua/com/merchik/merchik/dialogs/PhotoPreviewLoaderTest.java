package ua.com.merchik.merchik.dialogs;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class PhotoPreviewLoaderTest {
    private final List<PhotoPreviewLoader.Callback<String>> requests = new ArrayList<>();
    private final List<String> shown = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private boolean loading;
    private final PhotoPreviewLoader<String> loader = new PhotoPreviewLoader<>(
            new PhotoPreviewLoader.Source<String>() {
                @Override public boolean isReady(String value) { return value != null && value.startsWith("full:"); }
                @Override public void download(String value, PhotoPreviewLoader.Callback<String> callback) { requests.add(callback); }
            }, new PhotoPreviewLoader.Listener<String>() {
                @Override public void showPhoto(String photo) { shown.add(photo); }
                @Override public void setLoading(boolean value) { loading = value; }
                @Override public void showError(String error) { errors.add(error); }
            });

    @Test public void originalReplacesPreviewInTheSameDialog() {
        loader.open("small:1");
        assertEquals(List.of("small:1"), shown);
        assertTrue(loading);
        requests.get(0).onSuccess("full:1");
        assertEquals(List.of("small:1", "full:1"), shown);
        assertFalse(loading);
        assertTrue(errors.isEmpty());
    }

    @Test public void cachedOriginalDoesNotDownloadAgain() {
        loader.open("full:1");
        assertEquals(List.of("full:1"), shown);
        assertTrue(requests.isEmpty());
        assertFalse(loading);
    }

    @Test public void missingPreviewCanLoadMetadataAndOriginal() {
        loader.open(null);
        assertTrue(shown.isEmpty());
        assertTrue(loading);
        requests.get(0).onSuccess("full:1");
        assertEquals(List.of("full:1"), shown);
    }

    @Test public void errorKeepsPreviewAndAllowsRetry() {
        loader.open("small:1");
        requests.get(0).onFailure("HTTP 404");
        assertFalse(loading);
        assertEquals(List.of("small:1"), shown);
        assertEquals(List.of("HTTP 404"), errors);
        loader.retry();
        assertTrue(loading);
        requests.get(1).onSuccess("full:1");
        assertEquals(List.of("small:1", "full:1"), shown);
    }

    @Test public void retryDoesNotDuplicateAnActiveDownload() {
        loader.open("small:1");
        loader.retry();
        loader.retry();
        assertEquals(1, requests.size());
    }

    @Test public void closedDialogIgnoresLateResultsAndRetry() {
        loader.open("small:1");
        loader.close();
        requests.get(0).onSuccess("full:1");
        requests.get(0).onFailure("timeout");
        loader.retry();
        assertEquals(List.of("small:1"), shown);
        assertEquals(1, requests.size());
        assertTrue(errors.isEmpty());
    }

    @Test public void previousPhotoCannotOverwriteANewerSelection() {
        loader.open("small:1");
        loader.open("small:2");
        requests.get(0).onSuccess("full:1");
        requests.get(0).onFailure("timeout");
        requests.get(1).onSuccess("full:2");
        assertEquals(List.of("small:1", "small:2", "full:2"), shown);
        assertTrue(errors.isEmpty());
    }

    @Test public void invalidSuccessDoesNotClaimOriginalIsReady() {
        loader.open("small:1");
        requests.get(0).onSuccess(null);
        assertEquals(1, errors.size());
        assertFalse(loading);
        assertEquals(List.of("small:1"), shown);
        loader.retry();
        requests.get(1).onSuccess("small:1");
        assertEquals(2, errors.size());
    }

    @Test public void lateFailureAfterSuccessDoesNotReplaceIt() {
        loader.open("small:1");
        requests.get(0).onSuccess("full:1");
        requests.get(0).onFailure("late error");
        assertEquals(List.of("small:1", "full:1"), shown);
        assertTrue(errors.isEmpty());
    }

    @Test public void recycledCommentCannotUpdateReboundPhoto() {
        loader.open("small:1");
        loader.close();
        loader.open("small:2");
        requests.get(0).onSuccess("full:1");
        requests.get(0).onFailure("old comment failed");
        assertTrue(loading);
        assertEquals(List.of("small:1", "small:2"), shown);
        assertTrue(errors.isEmpty());
        requests.get(1).onSuccess("full:2");
        assertEquals(List.of("small:1", "small:2", "full:2"), shown);
        assertFalse(loading);
    }

    @Test public void oldAttemptCannotStopRetryOrReplaceItsPhoto() {
        loader.open("small:1");
        requests.get(0).onFailure("timeout");
        loader.retry();
        requests.get(0).onSuccess("full:old");
        requests.get(0).onFailure("late timeout");
        assertTrue(loading);
        assertEquals(List.of("timeout"), errors);
        assertEquals(List.of("small:1"), shown);
        requests.get(1).onSuccess("full:1");
        assertFalse(loading);
        assertEquals(List.of("small:1", "full:1"), shown);
    }

    @Test public void synchronousFailureStopsLoadingAndKeepsPreview() {
        PhotoPreviewLoader<String> failing = new PhotoPreviewLoader<>(new PhotoPreviewLoader.Source<String>() {
            @Override public boolean isReady(String value) { return false; }
            @Override public void download(String value, PhotoPreviewLoader.Callback<String> callback) {
                throw new IllegalStateException("missing metadata");
            }
        }, new PhotoPreviewLoader.Listener<String>() {
            @Override public void showPhoto(String value) { shown.add(value); }
            @Override public void setLoading(boolean value) { loading = value; }
            @Override public void showError(String error) { errors.add(error); }
        });
        failing.open("small:1");
        assertFalse(loading);
        assertEquals(List.of("small:1"), shown);
        assertEquals(1, errors.size());
        failing.retry();
        assertFalse(loading);
        assertEquals(2, errors.size());
    }
}
