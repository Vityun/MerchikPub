package ua.com.merchik.merchik.Utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;

public final class JsonLogUtils {

    private JsonLogUtils() {
    }

    public static String photoUploadResponse(JsonObject response) {
        if (response == null) {
            return "body=null";
        }

        StringBuilder builder = new StringBuilder();
        appendField(builder, "state", response.get("state"));
        appendField(builder, "error", response.get("error"));
        appendField(builder, "notice", response.get("notice"));

        JsonElement list = response.get("list");
        if (list != null && list.isJsonArray()) {
            append(builder, "list.size", String.valueOf(list.getAsJsonArray().size()));
            if (list.getAsJsonArray().size() > 0 && list.getAsJsonArray().get(0).isJsonObject()) {
                JsonObject first = list.getAsJsonArray().get(0).getAsJsonObject();
                appendField(builder, "list[0].state", first.get("state"));
                appendField(builder, "list[0].errorType", first.get("errorType"));
                appendField(builder, "list[0].error", first.get("error"));
            }
        }

        JsonElement move = response.get("move");
        if (move != null && move.isJsonObject()) {
            append(builder, "move.keys", String.valueOf(move.getAsJsonObject().entrySet().size()));
        }

        return builder.length() == 0 ? "body.keys=" + response.entrySet().size() : builder.toString();
    }

    public static String stackPhoto(StackPhotoDB photo) {
        if (photo == null) {
            return "photo=null";
        }
        return "id=" + photo.getId()
                + ", type=" + photo.getPhoto_type()
                + ", dad2=" + photo.getCode_dad2()
                + ", file=" + photo.getPhoto_num()
                + ", hashEmpty=" + (photo.getPhoto_hash() == null || photo.getPhoto_hash().isEmpty());
    }

    private static void appendField(StringBuilder builder, String key, JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return;
        }
        append(builder, key, trim(value.isJsonPrimitive() ? value.getAsString() : value.toString()));
    }

    private static void append(StringBuilder builder, String key, String value) {
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(key).append('=').append(value);
    }

    private static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= 160 ? value : value.substring(0, 160) + "...";
    }
}
