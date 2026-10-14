package ua.com.merchik.merchik.database.realm.tables;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Transaction-local index, shared by batch downloads and single-row writes.
final class ReportPrepareIdentityIndex {
    private final Map<Long, Key> byId = new HashMap<>();
    private final Map<Key, Long> byProduct = new HashMap<>();

    static boolean hasIdentity(Long id, String dad2, String tovarId) {
        return id != null && hasProduct(dad2, tovarId);
    }

    static boolean hasProduct(String dad2, String tovarId) {
        return dad2 != null && !dad2.trim().isEmpty()
                && tovarId != null && !tovarId.trim().isEmpty();
    }

    void remember(Long id, String dad2, String tovarId) {
        if (id == null) return;
        Key key = new Key(dad2, tovarId);
        byId.putIfAbsent(id, key);
        if (hasProduct(dad2, tovarId)) byProduct.putIfAbsent(key, id);
    }

    String conflict(Long id, String dad2, String tovarId) {
        String details = "dad2=" + dad2 + ", tovarId=" + tovarId + ", incomingId=" + id;
        if (!hasIdentity(id, dad2, tovarId)) {
            return "reason=INVALID_IDENTITY, " + details + ", action=SKIP";
        }
        Key key = new Key(dad2, tovarId);
        Key existingKey = byId.get(id);
        if (existingKey != null && !existingKey.equals(key)) {
            return "reason=ID_KEY_MISMATCH, " + details + ", keptId=" + id
                    + ", keptDad2=" + existingKey.dad2 + ", keptTovarId=" + existingKey.tovarId
                    + ", action=SKIP_KEEP_EXISTING";
        }
        Long existingId = byProduct.get(key);
        if (existingId != null && !existingId.equals(id)) {
            return "reason=DUPLICATE_PAIR, " + details + ", keptId=" + existingId
                    + ", action=SKIP_KEEP_EXISTING";
        }
        return null;
    }

    private static final class Key {
        final String dad2;
        final String tovarId;

        Key(String dad2, String tovarId) {
            this.dad2 = dad2;
            this.tovarId = tovarId;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Key)) return false;
            Key key = (Key) other;
            return Objects.equals(dad2, key.dad2) && Objects.equals(tovarId, key.tovarId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dad2, tovarId);
        }
    }
}
