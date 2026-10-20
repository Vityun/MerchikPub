package ua.com.merchik.merchik.Options.Controls;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import ua.com.merchik.merchik.Utils.PhotoEvidencePolicy;

final class RemainingGoodsPhotoIndex {
    private final Map<String, Set<String>> hashesByTovar = new LinkedHashMap<>();
    private final Map<String, Set<String>> tovarsByHash = new LinkedHashMap<>();
    private final Map<String, FirstPhoto> firstPhotoByHash = new LinkedHashMap<>();

    void addPhoto(int localId, long createTime, String tovarId, String hash) {
        String tovar = normalizeTovarId(tovarId);
        if (tovar.isEmpty()) return;

        String normalizedHash = normalizeHash(hash);
        hashesByTovar.computeIfAbsent(tovar, key -> new LinkedHashSet<>()).add(normalizedHash);
        if (normalizedHash != null) {
            tovarsByHash.computeIfAbsent(normalizedHash, key -> new LinkedHashSet<>()).add(tovar);
            FirstPhoto candidate = new FirstPhoto(localId, createTime, tovar);
            FirstPhoto first = firstPhotoByHash.get(normalizedHash);
            if (first == null || candidate.isBefore(first)) {
                firstPhotoByHash.put(normalizedHash, candidate);
            }
        }
    }

    boolean hasPhotoForTovar(String tovarId) {
        String tovar = normalizeTovarId(tovarId);
        Set<String> hashes = hashesByTovar.get(tovar);
        if (hashes == null) return false;

        for (String hash : hashes) {
            // Missing hashes cannot prove duplication; preserve the existing photo check.
            if (hash == null || firstPhotoByHash.get(hash).tovarId.equals(tovar)) return true;
        }
        return false;
    }

    boolean hasDuplicatePhotoForTovar(String tovarId) {
        String tovar = normalizeTovarId(tovarId);
        Set<String> hashes = hashesByTovar.get(tovar);
        if (hashes == null) return false;

        for (String hash : hashes) {
            if (hash != null && !firstPhotoByHash.get(hash).tovarId.equals(tovar)) return true;
        }
        return false;
    }

    Set<String> getTovarsForHash(String hash) {
        Set<String> tovars = tovarsByHash.get(normalizeHash(hash));
        return tovars == null ? Collections.emptySet() : Collections.unmodifiableSet(tovars);
    }

    int getOriginalPhotoId(String hash) {
        FirstPhoto first = firstPhotoByHash.get(normalizeHash(hash));
        return first == null ? 0 : first.localId;
    }

    private static String normalizeHash(String hash) {
        return PhotoEvidencePolicy.isUsableHash(hash) ? hash.trim().toLowerCase(Locale.ROOT) : null;
    }

    private static String normalizeTovarId(String tovarId) {
        if (tovarId == null || "0".equals(tovarId.trim())) return "";
        return tovarId.trim();
    }

    private static final class FirstPhoto {
        final int localId;
        final long createTime;
        final String tovarId;

        FirstPhoto(int localId, long createTime, String tovarId) {
            this.localId = localId;
            // Unknown timestamps follow known ones; local ID breaks ties.
            this.createTime = createTime > 0 ? createTime : Long.MAX_VALUE;
            this.tovarId = tovarId;
        }

        boolean isBefore(FirstPhoto other) {
            return createTime < other.createTime
                    || (createTime == other.createTime && localId < other.localId);
        }
    }
}
