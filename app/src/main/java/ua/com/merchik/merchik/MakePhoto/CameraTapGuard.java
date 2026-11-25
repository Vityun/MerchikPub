package ua.com.merchik.merchik.MakePhoto;

final class CameraTapGuard {
    private static final long TAP_WINDOW_MS = 1000;
    private boolean preparing;
    private boolean dispatched;
    private long acceptAfter;

    synchronized boolean begin(long now, boolean hostInForeground) {
        if (preparing || now < acceptAfter || (dispatched && !hostInForeground)) return false;
        preparing = true;
        return true;
    }

    synchronized void dispatched(long now) {
        preparing = false;
        dispatched = true;
        acceptAfter = now + TAP_WINDOW_MS;
    }

    synchronized void reset() {
        preparing = false;
        dispatched = false;
        acceptAfter = 0;
    }
}
