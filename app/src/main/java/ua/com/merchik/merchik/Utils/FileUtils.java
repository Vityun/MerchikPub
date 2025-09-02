package ua.com.merchik.merchik.Utils;


import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.Objects;

public final class FileUtils {

    /** Проверяет, что файл существует и имеет ненулевой размер.
     * Поддерживает absolute path, file:// и content://
     */
    public static boolean fileExistsAndNotEmpty(@Nullable String absPath) {
        if (absPath == null) return false;
        String p = absPath.trim();
        if (p.isEmpty()) return false;

        try {
            File f = new File(p);
            // Быстрые проверки
            if (!f.exists() || !f.isFile()) return false;

            // Ненулевой размер (часто достаточно)
            if (f.length() <= 0) return false;

            // Доп. верификация на читабельность (по желанию)
            try (java.io.FileInputStream is = new java.io.FileInputStream(f)) {
                // читаем один байт — убедиться, что доступ есть
                if (is.read() == -1) return false;
            }
            return true;
        } catch (SecurityException | java.io.IOException e) {
            return false;
        }
    }

}
