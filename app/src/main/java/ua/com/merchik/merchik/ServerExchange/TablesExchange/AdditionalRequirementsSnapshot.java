package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.JsonObject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.ServerData.TARCommentsData.AdditionalRequirements.AdditionalRequirementsServerData;

enum AdditionalRequirementsSnapshot {
    DAILY(-2, 4), FULL(-120, 7);

    private final int from, to;

    AdditionalRequirementsSnapshot(int from, int to) {
        this.from = from;
        this.to = to;
    }

    static LocalDate dayAt(long epochSeconds) {
        return Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.of("Europe/Kiev")).toLocalDate();
    }

    boolean alreadyDownloaded(LocalDate day, String lastDailyCheck) {
        return this == DAILY && day.toString().equals(lastDailyCheck);
    }

    JsonObject request(LocalDate day) {
        JsonObject request = new JsonObject();
        request.addProperty("mod", "additional_requirements");
        request.addProperty("act", "list");
        request.addProperty("date_from", day.plusDays(from).toString());
        request.addProperty("date_to", day.plusDays(to).toString());
        return request;
    }

    List<AdditionalRequirementsDB> rows(AdditionalRequirementsServerData body) {
        if (body == null || !Boolean.TRUE.equals(body.getState()) || body.getList() == null
                || (body.getError() != null && !body.getError().trim().isEmpty()))
            throw new IllegalStateException("Missing or unsuccessful requirements list");
        for (AdditionalRequirementsDB row : body.getList()) {
            if (row == null || row.getId() == null || row.getId() <= 0)
                throw new IllegalStateException("Requirement without a valid ID");
        }
        return body.getList();
    }
}
