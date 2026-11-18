package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.JsonObject;

import java.util.Objects;

import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;

final class ReclamationSyncPolicy {
    // New scope deliberately starts without the old, visit-date-limited watermark.
    static final String RESOURCE = "reclamation/list/posting_history_v1";

    private ReclamationSyncPolicy() { }

    static JsonObject request(long from) {
        JsonObject request = new JsonObject();
        request.addProperty("mod", "reclamation");
        request.addProperty("act", "list");
        request.addProperty("dt_change_from", Long.toString(Math.max(0, from)));
        // date_from/date_to filter visit dates on the server, not reclamation posting dates.
        return request;
    }

    static TasksAndReclamationsSDB merge(TasksAndReclamationsSDB server,
                                       TasksAndReclamationsSDB local, Long started) {
        if (local == null) return server;
        if (Objects.equals(local.uploadStatus, 1)) return local;
        boolean pending = Objects.equals(local.voteDtUpload, 0L);
        boolean acknowledgedDuringRequest = local.voteDtUpload != null && local.voteDtUpload > 0
                && (started == null || local.voteDtUpload >= started);
        if (pending || acknowledgedDuringRequest) {
            server.voteScore = local.voteScore;
            server.vinovnikScore = local.vinovnikScore;
            server.vinovnikScoreComment = local.vinovnikScoreComment;
            server.sotrOpinionId = local.sotrOpinionId;
            server.dt_start_fact = local.dt_start_fact;
            server.dt_end_fact = local.dt_end_fact;
        }
        server.voteDtUpload = local.voteDtUpload;
        server.uploadStatus = local.uploadStatus;
        return server;
    }
}
