package ua.com.merchik.merchik.data.UploadToServ;

import com.google.gson.Gson;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.TimeZone;

import ua.com.merchik.merchik.data.QuestionAnswerDB;

import static org.junit.Assert.*;

public class QuestionAnswerUploadTest {
    @Test public void localUnixSecondsAreNotJanuary1970() {
        assertEquals("2026-11-03", QuestionAnswerUpload.formatObjectDate(seconds("2026-11-03")));
    }

    @Test public void oldMillisecondValuesAreSupported() {
        assertEquals("2026-11-03", QuestionAnswerUpload.formatObjectDate(seconds("2026-11-03") * 1000));
    }

    @Test public void nullZeroAndNegativeDatesAreEmpty() {
        assertEquals("", QuestionAnswerUpload.formatObjectDate(null));
        assertEquals("", QuestionAnswerUpload.formatObjectDate(0L));
        assertEquals("", QuestionAnswerUpload.formatObjectDate(-1L));
    }

    @Test public void visitDateUsesKyivRegardlessOfDeviceTimeZone() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
            assertEquals("2026-11-03", QuestionAnswerUpload.formatObjectDate(seconds("2026-11-03")));
            assertEquals("2026-07-03", QuestionAnswerUpload.formatObjectDate(seconds("2026-07-03")));
        } finally {
            TimeZone.setDefault(original);
        }
    }

    @Test public void uploadKeepsAmountAndOtherFieldsUnchanged() {
        QuestionAnswerDB row = new QuestionAnswerDB();
        row.setId(1793625964568L);
        row.setAnswer("70.00");
        row.setAvgAnswer("1.5");
        row.setObjectDate(seconds("2026-11-03"));
        row.setDt(1793625964L);
        row.setObjectId("1031126021314061597");
        QuestionAnswerUpload upload = QuestionAnswerUpload.fromDb(row);
        assertEquals("70.00", upload.answer);
        assertEquals("1.5", upload.avgAnswer);
        assertEquals(row.getDt(), upload.dt);
        assertEquals(row.getObjectId(), upload.objectId);
        assertEquals("1793625964568", upload.elementId);
        assertEquals("2026-11-03", upload.objectDate);
        assertTrue(new Gson().toJson(upload).contains("\"answer\":\"70.00\""));
    }

    private static long seconds(String date) {
        return LocalDate.parse(date).atStartOfDay(ZoneId.of("Europe/Kiev")).toEpochSecond();
    }
}
