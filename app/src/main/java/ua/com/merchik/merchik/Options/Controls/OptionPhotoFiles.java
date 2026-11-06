package ua.com.merchik.merchik.Options.Controls;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Utils.PhotoEvidencePolicy;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

/** Photo evidence for option checks only; does not change stored photos or sample queries. */
final class OptionPhotoFiles {
    private OptionPhotoFiles() { }

    static boolean isReady(StackPhotoDB photo) {
        return photo != null && PhotoEvidencePolicy.isReadyPhoto(photo.getPhotoServerId(), photo.getPhoto_num());
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
