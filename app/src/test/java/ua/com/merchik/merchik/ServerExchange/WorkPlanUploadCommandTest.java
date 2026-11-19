package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.Test;

import ua.com.merchik.merchik.data.TestJsonUpload.StratEndWork.StartEndData;
import ua.com.merchik.merchik.data.TestJsonUpload.StratEndWork.UploadDataSEWork;
import ua.com.merchik.merchik.data.UploadToServ.WpDataUploadToServ;

import java.util.Collections;

import static org.junit.Assert.*;

public class WorkPlanUploadCommandTest {
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    private String legacyJson() {
        return "{\"element_id\":\"7\",\"code_dad2\":\"1191126039283061377\","
                + "\"visit_start_dt\":\"100\",\"client_start_dt\":\"100\","
                + "\"visit_end_dt\":\"200\",\"client_end_dt\":\"200\","
                + "\"client_work_duration\":\"100\",\"status_set\":\"1\",\"set_status\":1,"
                + "\"user_comment\":\"local comment\",\"user_opinion_id\":\"3\"}";
    }

    private void assertDataOnly(JsonObject row) {
        assertFalse(row.has("status_set"));
        assertFalse(row.has("set_status"));
        assertEquals("1191126039283061377", row.get("code_dad2").getAsString());
        assertEquals("100", row.get("visit_start_dt").getAsString());
        assertEquals("100", row.get("client_start_dt").getAsString());
        assertEquals("200", row.get("visit_end_dt").getAsString());
        assertEquals("200", row.get("client_end_dt").getAsString());
    }

    @Test public void ordinaryUploadCannotSerializeLegacyConductCommand() {
        StartEndData row = gson.fromJson(legacyJson(), StartEndData.class);
        UploadDataSEWork request = new UploadDataSEWork();
        request.mod = "plan";
        request.act = "update_data";
        request.data = Collections.singletonList(row);
        JsonObject json = gson.toJsonTree(request).getAsJsonObject();
        assertEquals("update_data", json.get("act").getAsString());
        JsonObject sent = json.getAsJsonArray("data").get(0).getAsJsonObject();
        assertDataOnly(sent);
        assertEquals("local comment", sent.get("user_comment").getAsString());
        assertEquals("3", sent.get("user_opinion_id").getAsString());
        assertEquals("100", sent.get("client_work_duration").getAsString());
    }

    @Test public void legacyMultipartModelCannotReplayConductCommand() {
        WpDataUploadToServ row = gson.fromJson(legacyJson(), WpDataUploadToServ.class);
        assertDataOnly(gson.toJsonTree(row).getAsJsonObject());
    }

    @Test public void legacyConstructorOnlyCarriesWorkData() {
        WpDataUploadToServ row = new WpDataUploadToServ("7", "1191126039283061377", "8", "9", "10",
                "100", "200", "100", "200");
        assertDataOnly(gson.toJsonTree(row).getAsJsonObject());
    }

    @Test public void persistedVisitPayloadCannotReplayConductCommand() {
        JsonObject data = gson.fromJson(legacyJson(), JsonObject.class);
        VisitUploadBatch.Row row = new VisitUploadBatch.Row(VisitUploadBatch.Kind.WP, "7", data);
        Gson journal = new Gson();
        VisitUploadBatch.Row restored = journal.fromJson(journal.toJson(row), VisitUploadBatch.Row.class);
        for (int retry = 0; retry < 2; retry++) {
            JsonObject request = VisitUploadBatch.request(Collections.singletonList(restored));
            assertEquals("plan", request.get("mod").getAsString());
            assertEquals("update_data", request.get("act").getAsString());
            JsonObject sent = request.getAsJsonArray("data").get(0).getAsJsonObject();
            assertDataOnly(sent);
            assertEquals("local comment", sent.get("user_comment").getAsString());
            assertEquals("3", sent.get("user_opinion_id").getAsString());
        }
        assertTrue(restored.data.has("status_set"));
    }
}
