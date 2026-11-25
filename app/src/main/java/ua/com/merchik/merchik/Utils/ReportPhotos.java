package ua.com.merchik.merchik.Utils;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

public final class ReportPhotos {
    private ReportPhotos() { }

    public static boolean isReady(StackPhotoDB photo) {
        return photo != null && PhotoEvidencePolicy.isReadyPhoto(photo.getPhotoServerId(), photo.getPhoto_num());
    }

    public static int count(Iterable<StackPhotoDB> photos) {
        int count = 0;
        for (StackPhotoDB photo : photos) if (isReady(photo)) count++;
        return count;
    }

    public static List<StackPhotoDB> ready(Iterable<StackPhotoDB> photos) {
        List<StackPhotoDB> result = new ArrayList<>();
        if (photos != null) {
            for (StackPhotoDB photo : photos) {
                if (isReady(photo)) result.add(photo);
            }
        }
        return result;
    }
}
