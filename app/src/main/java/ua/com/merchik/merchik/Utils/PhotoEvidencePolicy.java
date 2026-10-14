package ua.com.merchik.merchik.Utils;

import java.io.File;
import java.util.function.BooleanSupplier;

public final class PhotoEvidencePolicy {
    private static final String EMPTY_FILE_MD5 = "d41d8cd98f00b204e9800998ecf8427e";

    private PhotoEvidencePolicy() {
    }

    public static boolean hasServerPhotoId(String id) {
        if (id == null) return false;
        try {
            return Long.parseLong(id.trim()) > 0;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public static boolean isUsableHash(String hash) {
        return hash != null && !hash.trim().isEmpty() && !"0".equals(hash.trim())
                && !EMPTY_FILE_MD5.equalsIgnoreCase(hash.trim());
    }

    public static boolean isReadableNonEmptyFile(File file) {
        return file != null && file.isFile() && file.canRead() && file.length() > 0;
    }

    public static boolean hasPhoto(String serverId, String hash, BooleanSupplier hasHashEvidence) {
        // An uploaded photo need not remain in the device's local cache.
        return hasServerPhotoId(serverId) || (isUsableHash(hash) && hasHashEvidence.getAsBoolean());
    }
}
