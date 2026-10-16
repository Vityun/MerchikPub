package ua.com.merchik.merchik.ServerExchange;

import java.util.List;
import java.util.Objects;

import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RetrofitResponse.photos.PhotoInfoResponseList;
import ua.com.merchik.merchik.data.TestJsonUpload.PhotoFromSite.PhotoInformationData;

/** Field-level updates shared by photo callbacks and local editors. */
public final class StackPhotoSyncPolicy {
    private StackPhotoSyncPolicy() {
    }

    public static boolean samePhoto(StackPhotoDB current, StackPhotoDB snapshot) {
        if (current == null || snapshot == null || current.getId() != snapshot.getId()) return false;
        if (hasValue(snapshot.getPhoto_hash())) {
            return Objects.equals(current.getPhoto_hash(), snapshot.getPhoto_hash());
        }
        if (hasValue(snapshot.getPhotoServerId())) {
            return Objects.equals(current.getPhotoServerId(), snapshot.getPhotoServerId());
        }
        return hasValue(snapshot.getPhoto_num())
                && Objects.equals(current.getPhoto_num(), snapshot.getPhoto_num())
                && current.getCreate_time() == snapshot.getCreate_time();
    }

    public static boolean uploadSucceeded(StackPhotoDB current, StackPhotoDB sent, long now) {
        if (!samePhoto(current, sent)) return false;
        current.setUpload_to_server(Math.max(current.getUpload_to_server(), now));
        current.setError(null);
        return true;
    }

    public static boolean uploadFailed(StackPhotoDB current, StackPhotoDB sent, String error, long now) {
        if (!samePhoto(current, sent)) return false;
        // A late failed attempt must not replace a newer successful upload result.
        if (current.getUpload_to_server() > sent.getUpload_to_server()) return false;
        current.setError(1);
        current.setErrorTime(now);
        current.setErrorTxt(error);
        return true;
    }

    public static boolean receivedServerDetails(StackPhotoDB current, StackPhotoDB sent,
                                                String serverId, String url, long uploadedAt) {
        if (!samePhoto(current, sent) || !hasValue(serverId) || "0".equals(serverId)) return false;
        if (hasValue(current.getPhotoServerId()) && !"0".equals(current.getPhotoServerId())
                && !serverId.equals(current.getPhotoServerId())) return false;
        current.setPhotoServerId(serverId);
        current.setGet_on_server(uploadedAt);
        if (hasValue(url)) current.setPhotoServerURL(url);
        return true;
    }

    public static void editComment(StackPhotoDB current, String text, long nowSeconds) {
        current.setComment(text);
        current.setCommentUpload(true);
        current.setVpi(nowSeconds);
    }

    public static boolean commentAccepted(PhotoInformationData sent, List<PhotoInfoResponseList> response) {
        if (sent == null || response == null) return false;
        boolean accepted = false;
        for (PhotoInfoResponseList item : response) {
            if (item == null || item.elementId == null
                    || !String.valueOf(item.elementId).equals(sent.element_id)) continue;
            if (!item.state) return false;
            accepted = true;
        }
        return accepted;
    }

    public static boolean acknowledgeComment(StackPhotoDB current, PhotoInformationData sent) {
        if (current == null || sent == null || !current.isCommentUpload()
                || !String.valueOf(current.getId()).equals(sent.element_id)
                || !hasValue(sent.id) || "0".equals(sent.id)
                || !Objects.equals(current.getPhotoServerId(), sent.id)
                || !Objects.equals(current.getComment(), sent.text)) return false;
        current.setCommentUpload(false);
        return true;
    }

    private static boolean hasValue(String value) {
        return value != null && !value.isEmpty();
    }
}
