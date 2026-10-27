package ua.com.merchik.merchik.Options.Controls;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class SmsControlDiagnosticsTest {
    private static final long DAD2 = 1261026024655061457L;

    @Test public void logsMetadataButNotMessageOrContacts() {
        JsonObject row = JsonParser.parseString("{\"id\":12,\"ID\":345,\"state\":2,\"scnt\":1,"
                + "\"txt\":\"private text\",\"txt_long\":\"private long text\",\"sender_nm\":\"name\","
                + "\"recipient_tel\":\"phone\",\"recipient_email\":\"email\",\"recipient_list\":\"contacts\","
                + "\"msg_hash\":\"hash\",\"sms_err\":\"error with contacts\"}").getAsJsonObject();
        JsonObject result = SmsControlDiagnostics.metadata(row, DAD2, 246114, 246114);
        assertEquals(12, result.get("id").getAsInt());
        assertEquals(345, result.get("ID").getAsInt());
        assertEquals(2, result.get("state").getAsInt());
        assertEquals(1, result.get("scnt").getAsInt());
        assertTrue(result.get("hasSmsErrorText").getAsBoolean());
        for (String field : new String[]{"txt", "txt_long", "sender_nm", "recipient_tel", "recipient_email",
                "recipient_list", "msg_hash", "sms_err"}) assertFalse(field, result.has(field));
        assertFalse(result.toString().contains("private"));
        assertFalse(result.toString().contains("contacts"));
    }

    @Test public void keepsExactLongVisitAndChecksBothUsers() {
        JsonObject row = new JsonObject();
        row.addProperty("code_dad2", DAD2);
        row.addProperty("sender", 246114);
        JsonObject result = SmsControlDiagnostics.metadata(row, DAD2, 246114, 100);
        assertEquals(DAD2, result.get("code_dad2").getAsLong());
        assertTrue(result.get("sameVisit").getAsBoolean());
        assertTrue(result.get("sameVisitUser").getAsBoolean());
        assertFalse(result.get("sameCurrentUser").getAsBoolean());
        row.addProperty("code_dad2", DAD2 + 1);
        assertFalse(SmsControlDiagnostics.metadata(row, DAD2, 246114, 100).get("sameVisit").getAsBoolean());
    }

    @Test public void preservesRawTimesAndAddsKyivTimes() {
        JsonObject row = new JsonObject();
        row.addProperty("dt", 1792360800L);
        row.addProperty("dt_act", 1792940072L);
        row.addProperty("dt_check", 0);
        JsonObject result = SmsControlDiagnostics.metadata(row, DAD2, 246114, 246114);
        assertEquals(1792360800L, result.get("dt").getAsLong());
        assertEquals("2026-10-19 01:00:00", result.get("dt_kyiv").getAsString());
        assertEquals("2026-10-25 16:54:32", result.get("dt_act_kyiv").getAsString());
        assertEquals("unset", result.get("dt_check_kyiv").getAsString());
    }

    @Test public void absentStateInSmsLogIsNotInvented() {
        JsonObject row = JsonParser.parseString("{\"code_dad2\":null,\"sender\":null,\"dt\":null}").getAsJsonObject();
        JsonObject result = SmsControlDiagnostics.metadata(row, DAD2, 246114, 246114);
        assertFalse(result.get("sameVisit").getAsBoolean());
        assertFalse(result.get("sameVisitUser").getAsBoolean());
        assertFalse(result.has("state"));
        assertFalse(result.has("hasSmsErrorText"));
        assertEquals("unknown", result.get("dt_kyiv").getAsString());
    }
}
