package ua.com.merchik.merchik.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public static String fileMd5(String path) throws IOException {
        if (!hasLocalPhoto(path)) return null;
        try (FileInputStream input = new FileInputStream(path)) {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) md5.update(buffer, 0, read);
            return String.format(java.util.Locale.ROOT, "%032x", new java.math.BigInteger(1, md5.digest()));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public static boolean isReadyPhoto(String serverId, String path) {
        return hasServerPhotoId(serverId) || hasLocalPhoto(path);
    }

    public static boolean hasLocalPhoto(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        try {
            File file = new File(path);
            return file.isFile() && file.canRead() && file.length() > 1;
        } catch (SecurityException ignored) {
            return false;
        }
    }

    public static boolean hasPhoto(String serverId, String hash, BooleanSupplier hasHashEvidence) {
        // An uploaded photo need not remain in the device's local cache.
        return hasServerPhotoId(serverId) || (isUsableHash(hash) && hasHashEvidence.getAsBoolean());
    }
}
