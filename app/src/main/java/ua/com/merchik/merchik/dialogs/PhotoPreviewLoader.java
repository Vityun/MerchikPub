package ua.com.merchik.merchik.dialogs;

/** Coordinates a preview and its original on the UI thread. Late callbacks cannot revive a closed dialog. */
public final class PhotoPreviewLoader<T> {
    public interface Callback<T> {
        void onSuccess(T photo);
        void onFailure(String error);
    }

    public interface Source<T> {
        boolean isReady(T photo);
        void download(T photo, Callback<T> callback);
    }

    public interface Listener<T> {
        void showPhoto(T photo);
        void setLoading(boolean loading);
        void showError(String error);
    }

    private final Source<T> source;
    private final Listener<T> listener;
    private T current;
    private int generation;
    private boolean closed;
    private boolean loading;

    public PhotoPreviewLoader(Source<T> source, Listener<T> listener) {
        this.source = source;
        this.listener = listener;
    }

    public void open(T preview) {
        generation++;
        closed = false;
        loading = false;
        current = preview;
        if (preview != null) listener.showPhoto(preview);
        if (source.isReady(preview)) listener.setLoading(false);
        else retry();
    }

    public void retry() {
        if (closed || loading) return;
        loading = true;
        int request = ++generation;
        listener.setLoading(true);
        Callback<T> callback = new Callback<T>() {
            private boolean accepts() { return !closed && loading && request == generation; }

            @Override public void onSuccess(T photo) {
                if (!accepts()) return;
                if (!source.isReady(photo)) {
                    onFailure("Downloaded photo is not available");
                    return;
                }
                loading = false;
                current = photo;
                listener.showPhoto(photo);
                listener.setLoading(false);
            }

            @Override public void onFailure(String error) {
                if (!accepts()) return;
                loading = false;
                listener.setLoading(false);
                listener.showError(error);
            }
        };
        try {
            source.download(current, callback);
        } catch (RuntimeException error) {
            callback.onFailure(error.toString());
        }
    }

    public void close() {
        closed = true;
        loading = false;
        generation++;
    }
}
