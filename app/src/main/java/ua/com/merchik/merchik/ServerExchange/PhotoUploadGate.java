package ua.com.merchik.merchik.ServerExchange;

import java.util.HashMap;
import java.util.Map;

/** Shared by background, automatic and manual photo uploads in the app process. */
public final class PhotoUploadGate {
    public static final PhotoUploadGate INSTANCE = new PhotoUploadGate();

    private final Map<String, Lease> photos = new HashMap<>();
    private Lease batch;

    public synchronized Lease tryBatch() {
        if (batch != null || !photos.isEmpty()) return null;
        batch = new Lease(null, null);
        return batch;
    }

    public synchronized Lease tryPhoto(int id, String path) {
        String idKey = "id:" + id;
        String pathKey = path == null || path.trim().isEmpty() ? null : "path:" + path.trim();
        if (photos.containsKey(idKey) || (pathKey != null && photos.containsKey(pathKey))) {
            return null;
        }
        Lease lease = new Lease(idKey, pathKey);
        photos.put(idKey, lease);
        if (pathKey != null) photos.put(pathKey, lease);
        return lease;
    }

    public final class Lease implements AutoCloseable {
        private final String idKey;
        private final String pathKey;
        private boolean closed;

        private Lease(String idKey, String pathKey) {
            this.idKey = idKey;
            this.pathKey = pathKey;
        }

        @Override
        public void close() {
            synchronized (PhotoUploadGate.this) {
                if (closed) return;
                closed = true;
                if (idKey == null) {
                    if (batch == this) batch = null;
                } else {
                    if (photos.get(idKey) == this) photos.remove(idKey);
                    if (pathKey != null && photos.get(pathKey) == this) photos.remove(pathKey);
                }
            }
        }
    }
}
