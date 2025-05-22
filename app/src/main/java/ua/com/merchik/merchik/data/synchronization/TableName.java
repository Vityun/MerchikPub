package ua.com.merchik.merchik.data.synchronization;

import java.util.Objects;

public enum TableName {

    WP_DATA("wp_data"),
    IMAGE_TP("image_tp"),
    CLIENT_GROUP_TP("client_group_tp"),
    LOG_MP("log_mp"),
    CLIENTS("clients"),
    ADDRESS("address"),
    USERS("users"),
    PROMO_LIST("promoList"),
    ERROR_LIST("errorsList"),
    STACK_PHOTO("stack_photo"),
    TASK_AND_RECLAMATION("task_and_reclamations"),
    PLANOGRAMM("planogram");

    private final String table;

    TableName(String table) {
        this.table = table;
    }

    public String getCode() {
        return table;
    }

    public static TableName fromCode(String table) {
        for (TableName status : values()) {
            if (Objects.equals(status.table, table)) return status;
        }
        return WP_DATA;
    }
}
