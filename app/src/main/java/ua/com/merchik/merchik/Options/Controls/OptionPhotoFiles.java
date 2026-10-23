package ua.com.merchik.merchik.Options.Controls;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

/** File evidence for option checks only; does not change stored photos or sample queries. */
final class OptionPhotoFiles {
    private OptionPhotoFiles() { }

    static boolean isReady(StackPhotoDB photo) {
        if (photo == null) return false;
        String path = photo.getPhoto_num();
        if (path == null || path.trim().isEmpty()) return false;
        try {
            File file = new File(path);
            // Upload state and server ID do not tell us whether this file is a camera placeholder.
            return file.isFile() && file.length() > 0;
        } catch (SecurityException ignored) {
            return false;
        }
    }

    static List<StackPhotoDB> ready(List<StackPhotoDB> photos) {
        List<StackPhotoDB> result = new ArrayList<>();
        if (photos != null) {
            for (StackPhotoDB photo : photos) {
                if (isReady(photo)) result.add(photo);
            }
        }
        return result;
    }
}
