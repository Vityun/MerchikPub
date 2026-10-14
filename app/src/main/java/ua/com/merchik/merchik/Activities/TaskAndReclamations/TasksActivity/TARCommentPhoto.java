package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity;

import java.io.File;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.PhotoEvidencePolicy;
import ua.com.merchik.merchik.Utils.PhotoPickerUtils;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public final class TARCommentPhoto {
    private TARCommentPhoto() {
    }

    public static boolean hasPhoto(TARCommentsDB comment) {
        if (comment == null) return false;
        boolean valid;
        try {
            valid = PhotoEvidencePolicy.hasPhoto(comment.photo, comment.photo_hash, () -> {
                // Identical files can have several local records; one stale copy must not hide a valid one.
                for (StackPhotoDB photo : RealmManager.INSTANCE.where(StackPhotoDB.class)
                        .equalTo("photo_hash", comment.photo_hash.trim()).findAll()) {
                    if (isUsablePhoto(photo)) return true;
                }
                return false;
            });
        } catch (RuntimeException e) {
            Globals.writeToMLOG("ERROR", "TARCommentPhoto/check", "Photo lookup failed: commentId="
                    + comment.getID() + ", tarId=" + comment.rId + ", error=" + e);
            return false;
        }
        if (!valid) {
            Globals.writeToMLOG("INFO", "TARCommentPhoto/check", "Missing valid photo: commentId="
                    + comment.getID() + ", tarId=" + comment.rId + ", photoId=" + comment.photo
                    + ", hash=" + comment.photo_hash);
        }
        return valid;
    }

    public static boolean isUsablePhoto(StackPhotoDB photo) {
        if (photo == null) return false;
        return PhotoEvidencePolicy.hasPhoto(photo.getPhotoServerId(), photo.getPhoto_hash(), () -> {
            String path = photo.getPhoto_num();
            return path != null && !path.trim().isEmpty()
                    && PhotoPickerUtils.isReadableImageFile(new File(path));
        });
    }
}
