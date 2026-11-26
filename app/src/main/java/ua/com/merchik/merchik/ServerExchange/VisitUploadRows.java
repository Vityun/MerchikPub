package ua.com.merchik.merchik.ServerExchange;

import static ua.com.merchik.merchik.ServerExchange.VisitUploadBatch.Kind.*;
import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Utils.ReportPhotos;
import ua.com.merchik.merchik.data.Database.Room.*;
import ua.com.merchik.merchik.data.Database.Room.Planogram.PlanogrammVizitShowcaseSDB;
import ua.com.merchik.merchik.data.QuestionAnswerDB;
import ua.com.merchik.merchik.data.RealmModels.*;
import ua.com.merchik.merchik.data.UploadToServ.QuestionAnswerUpload;
import ua.com.merchik.merchik.data.UploadToServ.WPDataAdditionalMapper;
import ua.com.merchik.merchik.data.UploadToServ.LogUploadToServ;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.WpWorkTimes;

/** Main-thread snapshots and compare-before-ack writes; no reference-table downloads. */
final class VisitUploadRows {
    private static final Gson JSON = new Gson();
    private VisitUploadRows() { }

    static JsonObject object(Object value) { return JSON.toJsonTree(value).getAsJsonObject(); }

    static JsonObject data(Object... pairs) {
        JsonObject result = new JsonObject();
        for (int i = 0; i < pairs.length; i += 2) {
            if (pairs[i + 1] != null) result.add((String) pairs[i], JSON.toJsonTree(pairs[i + 1]));
        }
        return result;
    }

    static List<VisitUploadBatch.Row> collect(WpDataDB wp, VisitUploadBatch batch) {
        long dad2 = wp.getCode_dad2();
        List<VisitUploadBatch.Row> result = new ArrayList<>();
        if (!WpWorkTimes.valid(wp)) throw new IllegalStateException("Invalid visit work times");
        add(result, WP, wp.getId(), work(wp));
        for (ReportPrepareDB row : INSTANCE.where(ReportPrepareDB.class)
                .equalTo("codeDad2", String.valueOf(dad2)).findAll()) {
            if (row.getUploadStatus() != 1 && !batch.tracks(RP, row.getID())) continue;
            JsonObject upload = object(ReportPrepareRealm.uploadSnapshot(row));
            add(result, RP, VisitUploadBatch.string(upload, "element_id"), upload);
        }
        for (StackPhotoDB photo : photos(wp, batch)) {
            boolean required = photo.getCode_dad2() == dad2 && VisitPhotoUploadPolicy.requiresWarehouseComment(photo);
            if (required || photo.isCommentUpload() || batch.tracks(PHOTO_COMMENT, photo.getId())) {
                VisitUploadBatch.Row comment = new VisitUploadBatch.Row(PHOTO_COMMENT, photo.getId(), photo(photo, PHOTO_COMMENT));
                comment.requiredWarehouseComment = required;
                result.add(comment);
            }
            if (photo.isDviUpload() || batch.tracks(PHOTO_DVI, photo.getId())) add(result, PHOTO_DVI, photo.getId(), photo(photo, PHOTO_DVI));
            if (photo.isMarkUpload() || batch.tracks(PHOTO_SCORE, photo.getId())) add(result, PHOTO_SCORE, photo.getId(), photo(photo, PHOTO_SCORE));
        }
        for (AchievementsSDB row : SQL_DB.achievementsDao().getByDad2(dad2)) {
            if (row.serverId == null || row.serverId <= 0) result.add(achievementRow(row));
        }
        for (VoteSDB row : SQL_DB.votesDao().getAll()) {
            if (Objects.equals(row.codeDad2, dad2) && (Objects.equals(row.dtUpload, 0L) || batch.tracks(VOTE, row.id))) add(result, VOTE, row.id, vote(row));
        }
        for (WPDataPauseSDB row : SQL_DB.wpDataPauseDao().getAllByDad2(dad2)) {
            if (row.uploadStatus == 1 || batch.tracks(PAUSE, row.dtStart)) add(result, PAUSE, row.dtStart, pause(row));
        }
        for (PlanogrammVizitShowcaseSDB row : SQL_DB.planogrammVizitShowcaseDao().getByCodeDad2(dad2)) {
            if (Objects.equals(row.uploadStatus, 1) || batch.tracks(PLANOGRAM, row.id)) add(result, PLANOGRAM, row.id, planogram(row));
        }
        for (EKL_SDB row : SQL_DB.eklDao().getByDad2(dad2)) {
            if ((row.eklCode != null && !Boolean.TRUE.equals(row.upload)) || batch.tracks(EKL, row.id)) {
                add(result, EKL, row.id, data("id", row.id, "element_id", row.id, "code", row.code));
            }
        }
        Set<String> tasks = new HashSet<>();
        for (TasksAndReclamationsSDB row : relatedTasks(wp)) {
            tasks.add(String.valueOf(row.id));
            boolean create = (row.codeDad2 == null || row.codeDad2 == 0) && Objects.equals(row.uploadStatus, 1);
            if (create) add(result, TAR_CREATE, row.id, task(row, true));
            if ((!create && Objects.equals(row.uploadStatus, 1)) || Objects.equals(row.voteDtUpload, 0L) || batch.tracks(TAR, row.id)) {
                add(result, TAR, row.id, task(row, false));
            }
        }
        for (TARCommentsDB row : INSTANCE.where(TARCommentsDB.class).findAll()) {
            if (tasks.contains(row.getRId()) && (row.startUpdate || batch.tracks(TAR_COMMENT, row.getID()))) {
                VisitUploadBatch.Row pending = new VisitUploadBatch.Row(TAR_COMMENT, row.getID(), comment(row));
                pending.authorId = row.getWho();
                result.add(pending);
            }
        }
        for (LogMPDB row : INSTANCE.where(LogMPDB.class).equalTo("codeDad2", dad2)
                .equalTo("upload", 0L).findAll()) {
            add(result, LOCATION, row.getId(), data("gp", row.getGp()));
        }
        for (LogDB row : INSTANCE.where(LogDB.class).isNull("dt").findAll()) {
            // Unlock/photo-control logs use a derived object ID, not dad2.
            if (Objects.equals(row.getObj_id(), dad2) || String.valueOf(dad2).equals(row.getObj_id2())
                    || (Objects.equals(row.getAddr_id(), wp.getAddr_id())
                    && Objects.equals(row.getClient_id(), wp.getClient_id()))) {
                add(result, LOG, row.getId(), log(row));
            }
        }
        // These user queues have no visit FK. Include pending rows instead of silently dropping them.
        for (AdditionalRequirementsMarkDB row : AdditionalRequirementsMarkRealm.getToUpload(Globals.getCurrentUserId())) {
            add(result, AR, row.getId(), mark(row));
        }
        // Marks get a new local ID on each edit. Refresh tracked identities even if cron already sent them.
        Map<String, AdditionalRequirementsMarkDB> latestMarks = new HashMap<>();
        for (AdditionalRequirementsMarkDB row : INSTANCE.copyFromRealm(INSTANCE.where(AdditionalRequirementsMarkDB.class)
                .equalTo("userId", String.valueOf(batch.userId)).findAll())) {
            if (!AdditionalRequirementsMarkUploadPolicy.hasIdentity(row)) continue;
            String key = row.getTp() + ":" + row.getItemId();
            AdditionalRequirementsMarkDB previous = latestMarks.get(key);
            if (previous == null || AdditionalRequirementsMarkUploadPolicy.compareNewestFirst(row, previous) < 0) latestMarks.put(key, row);
        }
        for (VisitUploadBatch.Row queued : batch.rows.values()) {
            if (queued.kind != AR) continue;
            AdditionalRequirementsMarkDB latest = latestMarks.get(VisitUploadBatch.string(queued.data, "tp_id")
                    + ":" + VisitUploadBatch.string(queued.data, "element_id"));
            if (latest != null) add(result, AR, latest.getId(), mark(latest));
        }
        for (QuestionAnswerDB row : SQL_DB.questionAnswerDao().getPendingForUpload(
                Math.max(answerCursor(), QuestionAnswerAckPolicy.LOCAL_ID_MIN - 1),
                Globals.getCurrentUserId())) {
            add(result, ANSWER, row.getId(), object(QuestionAnswerUpload.fromDb(row)));
        }
        for (WPDataAdditional row : SQL_DB.wpDataAdditionalDao().getByCodeDad2Sync(dad2)) {
            if (row.uploadStatus == 1 || batch.tracks(PLAN_BUDGET, row.ID)) add(result, PLAN_BUDGET, row.ID,
                    object(WPDataAdditionalMapper.mapAll(Collections.singletonList(row), Globals.getCurrentUserId()).get(0)));
        }
        return result;
    }

    private static void add(List<VisitUploadBatch.Row> rows, VisitUploadBatch.Kind kind, Object id, JsonObject data) {
        rows.add(new VisitUploadBatch.Row(kind, id, data));
    }

    private static List<TasksAndReclamationsSDB> relatedTasks(WpDataDB wp) {
        List<TasksAndReclamationsSDB> result = new ArrayList<>();
        for (TasksAndReclamationsSDB row : SQL_DB.tarDao().getAll()) {
            if (Objects.equals(row.codeDad2SrcDoc, wp.getCode_dad2()) || Objects.equals(row.codeDad2, wp.getCode_dad2())
                    || (Objects.equals(row.addr, wp.getAddr_id()) && Objects.equals(row.client, wp.getClient_id())
                    && (Objects.equals(row.vinovnik, Globals.getCurrentUserId())
                    || Objects.equals(row.author, Globals.getCurrentUserId())
                    || Objects.equals(row.vinovnik2, Globals.getCurrentUserId())
                    || Objects.equals(row.zamenaUserId, Globals.getCurrentUserId())))) result.add(row);
        }
        return result;
    }

    static List<StackPhotoDB> photos(WpDataDB wp) {
        return photos(wp, null);
    }

    private static List<StackPhotoDB> photos(WpDataDB wp, VisitUploadBatch batch) {
        Set<String> hashes = new HashSet<>();
        Set<Integer> localIds = new HashSet<>();
        Set<String> serverIds = new HashSet<>();
        Set<String> tasks = new HashSet<>();
        for (PlanogrammVizitShowcaseSDB row : SQL_DB.planogrammVizitShowcaseDao().getByCodeDad2(wp.getCode_dad2())) {
            if (row.photo_do_hash != null && !row.photo_do_hash.isEmpty()) hashes.add(row.photo_do_hash);
            if (row.photo_do_id != null && row.photo_do_id > 0) serverIds.add(String.valueOf(row.photo_do_id));
        }
        for (AchievementsSDB row : SQL_DB.achievementsDao().getByDad2(wp.getCode_dad2())) {
            if (row.img_before_hash != null && !row.img_before_hash.isEmpty()) hashes.add(row.img_before_hash);
            if (row.img_after_hash != null && !row.img_after_hash.isEmpty()) hashes.add(row.img_after_hash);
        }
        for (TasksAndReclamationsSDB row : relatedTasks(wp)) {
            tasks.add(String.valueOf(row.id));
            if (row.photoHash != null && !row.photoHash.isEmpty()) hashes.add(row.photoHash);
            if (row.photo != null && row.photo > 0) {
                if (row.codeDad2 == null || row.codeDad2 == 0) localIds.add(row.photo);
                else serverIds.add(String.valueOf(row.photo));
            }
        }
        for (TARCommentsDB row : INSTANCE.where(TARCommentsDB.class).findAll()) {
            if (tasks.contains(row.getRId()) && row.photo_hash != null && !row.photo_hash.isEmpty()) hashes.add(row.photo_hash);
            if (tasks.contains(row.getRId()) && row.getPhoto() != null && !row.getPhoto().isEmpty()
                    && !"0".equals(row.getPhoto())) serverIds.add(row.getPhoto());
        }
        List<StackPhotoDB> result = new ArrayList<>();
        for (StackPhotoDB photo : INSTANCE.where(StackPhotoDB.class).findAll()) {
            if (photo.getCode_dad2() == wp.getCode_dad2() || hashes.contains(photo.getPhoto_hash())
                    || localIds.contains(photo.getId()) || serverIds.contains(photo.getPhotoServerId())) {
                if (ReportPhotos.isReady(photo) || (photo.getCode_dad2() == wp.getCode_dad2()
                        && VisitPhotoUploadPolicy.requiresWarehouseComment(photo)
                        && VisitPhotoUploadPolicy.alreadyUploaded(photo))) result.add(INSTANCE.copyFromRealm(photo));
                else if (batch != null) batch.discardUnavailablePhoto(photo.getId());
            }
        }
        return result;
    }

    private static JsonObject work(WpDataDB wp) {
        return data("temp_id", 1, "element_id", String.valueOf(wp.getId()), "code_dad2", String.valueOf(wp.getCode_dad2()),
                "user_id", String.valueOf(wp.getUser_id()), "client_id", wp.getClient_id(), "isp", wp.getIsp(),
                "dt_update", wp.getDt_update(), "visit_start_dt", String.valueOf(wp.getVisit_start_dt()),
                "visit_end_dt", String.valueOf(wp.getVisit_end_dt()), "client_start_dt", String.valueOf(wp.getClient_start_dt()),
                "client_end_dt", String.valueOf(wp.getClient_end_dt()), "client_work_duration", String.valueOf(wp.client_work_duration),
                "user_comment", wp.user_comment, "user_comment_author_id", wp.user_comment_author_id,
                "user_comment_dt_update", wp.user_comment_dt_update, "user_opinion_id", wp.getUser_opinion_id(),
                "user_opinion_author_id", wp.getUser_opinion_author_id(), "user_opinion_dt_update", wp.getUser_opinion_dt_update());
        // status_set is deliberately absent. Only document_complete may authorize conduct.
    }

    private static JsonObject photo(StackPhotoDB row, VisitUploadBatch.Kind kind) {
        return data("element_id", String.valueOf(row.getId()), "id", row.getPhotoServerId(),
                kind == PHOTO_COMMENT ? "text" : kind == PHOTO_DVI ? "state" : "score",
                kind == PHOTO_COMMENT ? row.getComment() : kind == PHOTO_DVI ? String.valueOf(row.isDvi()) : row.getMark());
    }

    static VisitUploadBatch.Row achievementRow(AchievementsSDB r) {
        JsonObject payload = data("element_id", r.id, "dt", r.dt_ut, "client_id", r.clientId, "addr_id", r.addrId,
                "theme_id", r.themeId, "code_dad2", r.codeDad2, "dvi", r.dvi, "comment_dt", r.commentDt,
                "comment_user_id", r.commentUser, "comment_txt", r.commentTxt, "img_before_hash", r.img_before_hash,
                "img_after_hash", r.img_after_hash, "add_requirement_id", r.addRequirementId, "tovar_id", r.tovar_id,
                "manufacturer", r.manufacturer, "showcase_id", r.showcaseId, "showcase_nm", r.showcaseNm);
        return new VisitUploadBatch.Row(ACHIEVEMENT, r.id, payload);
    }

    private static JsonObject vote(VoteSDB r) {
        return data("element_id", String.valueOf(r.id), "id", String.valueOf(r.photoId), "comment", r.comments,
                "score", String.valueOf(r.score), "vote_class", r.voteClass, "dt", r.dt, "isp", r.isp,
                "theme_id", r.themeId, "code_dad2", r.codeDad2, "addr_id", r.addrId, "kli", r.kli, "merchik", r.merchik);
    }

    private static JsonObject pause(WPDataPauseSDB r) {
        return data("code_dad2", r.codeDad2, "dt_start", r.dtStart, "dt_end", r.dtEnd, "dt_update_client", r.dtUpdateClient);
    }

    private static JsonObject planogram(PlanogrammVizitShowcaseSDB r) {
        JsonObject result = object(r);
        result.remove("uploadStatus");
        result.remove("score");
        result.remove("color");
        return result;
    }

    private static JsonObject task(TasksAndReclamationsSDB r, boolean create) {
        if (create) return data("element_id", r.id, "tp", r.tp, "addr_id", r.addr, "user_id", r.author,
                "client_id", r.client, "vinovnik_id", r.vinovnik, "date", ua.com.merchik.merchik.Clock.getDateString(r.dt),
                "photo_id", r.photo, "photo_hash", r.photoHash, "theme_id", r.themeId, "comment", r.comment);
        return data("element_id", r.id, "code_dad2", r.codeDad2, "vote_score", r.voteScore,
                "vinovnik_score", r.vinovnikScore, "vinovnik_score_comment", r.vinovnikScoreComment,
                "sotr_opinion_id", r.sotrOpinionId, "dt_start_fact", r.dt_start_fact, "dt_end_fact", r.dt_end_fact);
    }

    private static JsonObject comment(TARCommentsDB r) {
        return data("id", r.getRId(), "comment", r.getComment(), "dt", r.getDt(),
                "comment_id", r.commentId != null && r.commentId > 0 ? String.valueOf(r.commentId) : null,
                "photo_id", r.getPhoto(), "photo_hash", r.photo_hash, "report_id", r.getReportId());
    }

    private static JsonObject mark(AdditionalRequirementsMarkDB r) {
        return data("id", String.valueOf(r.getItemId()), "element_id", r.getItemId(),
                "score", r.getScore(), "tp_id", r.getTp(), "comment", r.comment);
    }

    private static JsonObject log(LogDB r) {
        return object(new LogUploadToServ(String.valueOf(r.getId()), String.valueOf(r.getDt_action()), r.getComments(),
                String.valueOf(r.getTp()), r.getClient_id(), String.valueOf(r.getAddr_id()), String.valueOf(r.getObj_id()),
                null, r.getObj_id2(), r.getObj_id2_theme_id()));
    }

    static void resolvePhotoReferences(VisitUploadBatch batch) {
        for (VisitUploadBatch.Row row : new ArrayList<>(batch.rows.values())) {
            if (row.kind != TAR_CREATE || row.acknowledgement == null) continue;
            String id = VisitUploadBatch.string(row.acknowledgement, "id");
            TasksAndReclamationsSDB task = SQL_DB.tarDao().getById(Integer.parseInt(id));
            Long dad2 = task == null ? null : task.codeDad2;
            if (VisitUploadBatch.positiveId(row.acknowledgement.get("code_dad2"))) {
                dad2 = row.acknowledgement.get("code_dad2").getAsLong();
            }
            batch.reconcileCreatedTask(row.id, id, dad2);
        }
        for (VisitUploadBatch.Row row : new ArrayList<>(batch.rows.values())) {
            if (row.acknowledgement != null) continue;
            if (row.kind == PHOTO_COMMENT || row.kind == PHOTO_DVI || row.kind == PHOTO_SCORE) {
                StackPhotoDB photo = INSTANCE.where(StackPhotoDB.class).equalTo("id", Integer.parseInt(row.id)).findFirst();
                if (!VisitUploadBatch.positiveId(row.data.get("id")) && photo != null
                        && VisitUploadBatch.positiveId(JSON.toJsonTree(photo.getPhotoServerId()))) {
                    row.data.addProperty("id", photo.getPhotoServerId());
                }
            } else if (row.kind == TAR_COMMENT && !row.data.has("comment_id")) {
                TARCommentsDB match = null;
                for (TARCommentsDB comment : INSTANCE.where(TARCommentsDB.class)
                        .equalTo("rId", VisitUploadBatch.string(row.data, "id"))
                        .equalTo("dt", VisitUploadBatch.string(row.data, "dt")).findAll()) {
                    if (comment.commentId == null || comment.commentId <= 0) continue;
                    if (!Objects.equals(row.authorId, comment.getWho())) continue;
                    if (match != null) throw new IllegalStateException("Ambiguous TAR comment acknowledgement");
                    match = comment;
                }
                if (match != null) {
                    row.data = comment(match);
                    batch.reconcileCommentId(row, String.valueOf(match.commentId));
                }
            }
        }
    }

    static void acknowledge(VisitUploadBatch.Row sent, JsonObject ack) {
        long now = System.currentTimeMillis() / 1000;
        switch (sent.kind) {
            case RP:
                Map<String, JsonElement> rows = new HashMap<>();
                rows.put(sent.id, sent.data);
                ReportPrepareRealm.acknowledgeUpload(rows, Collections.singleton(sent.id));
                break;
            case WP:
                INSTANCE.executeTransaction(realm -> {
                    WpDataDB row = realm.where(WpDataDB.class).equalTo("ID", Long.parseLong(sent.id)).findFirst();
                    if (row != null && work(row).equals(sent.data)) row.startUpdate = false;
                });
                break;
            case PHOTO_COMMENT: case PHOTO_DVI: case PHOTO_SCORE:
                INSTANCE.executeTransaction(realm -> {
                    StackPhotoDB row = realm.where(StackPhotoDB.class).equalTo("id", Integer.parseInt(sent.id)).findFirst();
                    if (row == null || !photo(row, sent.kind).equals(sent.data)) return;
                    if (sent.kind == PHOTO_COMMENT) row.setCommentUpload(false);
                    else if (sent.kind == PHOTO_DVI) row.setDviUpload(false);
                    else row.setMarkUpload(false);
                });
                break;
            case LOCATION:
                INSTANCE.executeTransaction(realm -> {
                    LogMPDB row = realm.where(LogMPDB.class).equalTo("id", Integer.parseInt(sent.id)).findFirst();
                    if (row != null && data("gp", row.getGp()).equals(sent.data)) {
                        row.serverId = Long.parseLong(VisitUploadBatch.string(ack, "geo_id"));
                        row.upload = now;
                    }
                });
                break;
            case LOG:
                INSTANCE.executeTransaction(realm -> {
                    LogDB row = realm.where(LogDB.class).equalTo("id", Integer.parseInt(sent.id)).findFirst();
                    if (row != null && log(row).equals(sent.data)) row.setDt(now);
                });
                break;
            case AR:
                for (AdditionalRequirementsMarkDB row : AdditionalRequirementsMarkRealm.getToUpload(Globals.getCurrentUserId())) {
                    if (row.getId().equals(sent.id) && mark(row).equals(sent.data)) {
                        AdditionalRequirementsMarkRealm.acknowledgeUpload(Collections.singletonList(row), Collections.singleton(row.getItemId()));
                    }
                }
                break;
            case TAR_COMMENT:
                JsonObject info = ack.has("info") && ack.get("info").isJsonObject() ? ack.getAsJsonObject("info") : ack;
                String commentId = VisitUploadBatch.string(info, "comment_id");
                if (commentId.isEmpty()) throw new IllegalStateException("Missing TAR comment id");
                INSTANCE.executeTransaction(realm -> {
                    TARCommentsDB row = realm.where(TARCommentsDB.class).equalTo("id", sent.id).findFirst();
                    if (row == null) return;
                    boolean same = comment(row).equals(sent.data);
                    // Preserve newer text/photo edits when replacing the temporary primary key.
                    TARCommentsDB copy = realm.copyFromRealm(row);
                    copy.setID(commentId);
                    copy.commentId = Integer.valueOf(commentId);
                    copy.startUpdate = !same;
                    if (!same) copy.dtUpdate = now;
                    row.deleteFromRealm();
                    realm.insertOrUpdate(copy);
                });
                break;
            case ANSWER:
                // Per-row ACK stays in this journal; never advance the global cursor over other rows.
                break;
            default:
                SQL_DB.runInTransaction(() -> acknowledgeRoom(sent, ack, now));
                if (sent.kind == TAR_CREATE) {
                    String id = VisitUploadBatch.string(ack, "id");
                    INSTANCE.executeTransaction(realm -> {
                        for (TARCommentsDB comment : realm.copyFromRealm(realm.where(TARCommentsDB.class)
                                .equalTo("rId", sent.id).findAll())) {
                            comment.setRId(id);
                            realm.insertOrUpdate(comment);
                        }
                    });
                }
        }
    }

    private static void acknowledgeRoom(VisitUploadBatch.Row sent, JsonObject ack, long now) {
        switch (sent.kind) {
            case ACHIEVEMENT:
                SQL_DB.achievementsDao().acknowledgeCreate(Integer.parseInt(sent.id),
                        sent.data.get("code_dad2").getAsLong(), ack.get("id").getAsInt());
                break;
            case VOTE:
                for (VoteSDB row : SQL_DB.votesDao().getAllToUpload()) {
                    if (String.valueOf(row.id).equals(sent.id) && vote(row).equals(sent.data)) {
                        row.dtUpload = now;
                        SQL_DB.votesDao().insertAll(Collections.singletonList(row));
                    }
                }
                break;
            case PAUSE:
                WPDataPauseSDB pause = SQL_DB.wpDataPauseDao().getByIdSync(sent.data.get("code_dad2").getAsLong(), Long.parseLong(sent.id));
                if (pause != null && pause(pause).equals(sent.data)) {
                    pause.uploadStatus = 0;
                    SQL_DB.wpDataPauseDao().insertSync(pause);
                }
                break;
            case PLANOGRAM:
                PlanogrammVizitShowcaseSDB planogram = SQL_DB.planogrammVizitShowcaseDao().getById(Integer.parseInt(sent.id));
                if (planogram != null && planogram(planogram).equals(sent.data)) {
                    planogram.uploadStatus = 0;
                    SQL_DB.planogrammVizitShowcaseDao().update(planogram);
                }
                break;
            case EKL:
                for (EKL_SDB row : SQL_DB.eklDao().getAll()) {
                    if (String.valueOf(row.id).equals(sent.id) && Objects.equals(row.code, VisitUploadBatch.string(sent.data, "code"))) {
                        row.eklCode = row.eklHashCode;
                        row.upload = true;
                        SQL_DB.eklDao().insertAll(Collections.singletonList(row));
                    }
                }
                break;
            case TAR_CREATE:
                TasksAndReclamationsSDB created = SQL_DB.tarDao().getById(Integer.parseInt(sent.id));
                if (created != null) {
                    int serverId = Integer.parseInt(VisitUploadBatch.string(ack, "id"));
                    boolean same = task(created, true).equals(sent.data);
                    if (!same) throw new IllegalStateException("TAR creation changed during upload; local edits retained");
                    TasksAndReclamationsSDB existing = SQL_DB.tarDao().getById(serverId);
                    if (serverId != created.id && existing != null) {
                        if (Objects.equals(created.voteDtUpload, 0L)) {
                            existing.voteScore = created.voteScore;
                            existing.vinovnikScore = created.vinovnikScore;
                            existing.vinovnikScoreComment = created.vinovnikScoreComment;
                            existing.sotrOpinionId = created.sotrOpinionId;
                            existing.dt_start_fact = created.dt_start_fact;
                            existing.dt_end_fact = created.dt_end_fact;
                            existing.voteDtUpload = 0L;
                            SQL_DB.tarDao().insertData(Collections.singletonList(existing)).blockingAwait();
                        }
                        SQL_DB.tarDao().deleteByIdSync(created.id);
                    } else {
                        int oldId = created.id;
                        created.id = serverId;
                        if (VisitUploadBatch.positiveId(ack.get("code_dad2"))) created.codeDad2 = ack.get("code_dad2").getAsLong();
                        created.uploadStatus = 0;
                        // Scores/opinions/work times are acknowledged separately by update_data.
                        SQL_DB.tarDao().insertData(Collections.singletonList(created)).blockingAwait();
                        if (oldId != serverId) SQL_DB.tarDao().deleteByIdSync(oldId);
                    }
                }
                break;
            case TAR:
                TasksAndReclamationsSDB task = SQL_DB.tarDao().getById(Integer.parseInt(sent.id));
                if (task != null && task(task, false).equals(sent.data)) {
                    task.uploadStatus = 0;
                    task.voteDtUpload = now;
                    SQL_DB.tarDao().insertData(Collections.singletonList(task)).blockingAwait();
                }
                break;
            case PLAN_BUDGET:
                WPDataAdditional row = SQL_DB.wpDataAdditionalDao().getByIdSync(Long.parseLong(sent.id));
                if (row != null && object(WPDataAdditionalMapper.mapAll(Collections.singletonList(row), Globals.getCurrentUserId()).get(0)).equals(sent.data)) {
                    SQL_DB.wpDataAdditionalDao().replaceLocalIdWithServerIdSync(row.ID,
                            Long.parseLong(VisitUploadBatch.string(ack, "id")));
                }
                break;
            default: throw new IllegalStateException("Unsupported visit queue " + sent.kind);
        }
    }

    static void acknowledgeAnswers(VisitUploadBatch batch) {
        SynchronizationTimetableDB table = RealmManager.getSynchronizationTimetableRowByTable("question_answer");
        if (table == null) return; // Per-row acknowledgements are already durable in this journal.
        long previous = answerCursor();
        long through = previous;
        for (QuestionAnswerDB answer : SQL_DB.questionAnswerDao().getPendingForUpload(
                Math.max(previous, QuestionAnswerAckPolicy.LOCAL_ID_MIN - 1), batch.userId)) {
            VisitUploadBatch.Row row = batch.rows.get(ANSWER + ":" + answer.getId());
            if (row == null || row.acknowledgement == null || !row.data.equals(object(QuestionAnswerUpload.fromDb(answer)))) break;
            through = answer.getId();
        }
        if (through > previous) {
            table.setVpo_app(through);
            RealmManager.setToSynchronizationTimetableDB(table);
        }
    }

    private static long answerCursor() {
        SynchronizationTimetableDB table = RealmManager.getSynchronizationTimetableRowByTable("question_answer");
        return table == null ? 0 : QuestionAnswerAckPolicy.uploadedThrough(table.getVpi_app(), table.getVpo_app());
    }
}
