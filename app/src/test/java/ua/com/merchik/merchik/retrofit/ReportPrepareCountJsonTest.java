package ua.com.merchik.merchik.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportPrepareServer;

import static org.junit.Assert.*;

public class ReportPrepareCountJsonTest {
    static Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(String.class, new CleanStringAdapter())
                .registerTypeAdapter(int.class, new FlexibleIntegerTypeAdapter(0))
                .registerTypeAdapter(Integer.class, new FlexibleIntegerTypeAdapter(null))
                .registerTypeAdapter(long.class, new FlexibleLongTypeAdapter(0L))
                .registerTypeAdapter(Long.class, new FlexibleLongTypeAdapter(null))
                .create();
    }

    private ReportPrepareDB row(String json) {
        return gson().fromJson(json, ReportPrepareDB.class);
    }

    @Test public void roundsOnlyTheFourApprovedFields() {
        Set<String> actual = new HashSet<>();
        for (Field field : ReportPrepareDB.class.getDeclaredFields()) {
            JsonAdapter annotation = field.getAnnotation(JsonAdapter.class);
            if (annotation != null && annotation.value() == RoundedCountAdapterFactory.class) {
                actual.add(field.getName());
                assertFalse(annotation.nullSafe());
            }
        }
        assertEquals(new HashSet<>(Arrays.asList("face", "facesPlan", "up", "amount")), actual);
    }

    @Test public void roundsNumbersAndStringsHalfUp() {
        ReportPrepareDB rp = row("{\"face\":17.9,\"faces_plan\":\"17.5\",\"up\":\"17.49\",\"amount\":2.5}");
        assertEquals("18", rp.face);
        assertEquals(Integer.valueOf(18), rp.facesPlan);
        assertEquals("17", rp.up);
        assertEquals(3, rp.amount);
    }

    @Test public void supportsCommaExponentWhitespaceAndIntegralDecimals() {
        ReportPrepareDB rp = row("{\"face\":\" 17,5 \",\"faces_plan\":1.75e1,\"up\":\"3.00\",\"amount\":\"2,49\"}");
        assertEquals("18", rp.face);
        assertEquals(Integer.valueOf(18), rp.facesPlan);
        assertEquals("3", rp.up);
        assertEquals(2, rp.amount);
    }

    @Test public void negativeServerValuesUseMathematicalRoundingNotClamping() {
        ReportPrepareDB rp = row("{\"face\":-17.5,\"faces_plan\":-17.49,\"up\":\"-0.5\",\"amount\":-2.5}");
        assertEquals("-18", rp.face);
        assertEquals(Integer.valueOf(-17), rp.facesPlan);
        assertEquals("-1", rp.up);
        assertEquals(-3, rp.amount);
    }

    @Test public void integralDecimalsPreserveSignAndSupportBothSeparators() {
        for (String value : new String[]{"17.0", "17.000", "17,000", " +0017.00 ", "17."}) {
            String jsonValue = gson().toJson(value);
            ReportPrepareDB rp = row("{\"face\":" + jsonValue + ",\"faces_plan\":" + jsonValue
                    + ",\"up\":" + jsonValue + ",\"amount\":" + jsonValue + "}");
            assertEquals("17", rp.face);
            assertEquals(Integer.valueOf(17), rp.facesPlan);
            assertEquals("17", rp.up);
            assertEquals(17, rp.amount);
        }
        ReportPrepareDB rp = row("{\"face\":\"-17.000\",\"faces_plan\":-17.0,\"up\":\"-0,000\",\"amount\":-0.0}");
        assertEquals("-17", rp.face);
        assertEquals(Integer.valueOf(-17), rp.facesPlan);
        assertEquals("0", rp.up);
        assertEquals(0, rp.amount);
    }

    @Test public void integralDecimalBoundariesAreExact() {
        ReportPrepareDB rp = row("{\"face\":\"2147483647.000\",\"faces_plan\":-2147483648.0,"
                + "\"up\":\"-2147483648,000\",\"amount\":2147483647.00}");
        assertEquals("2147483647", rp.face);
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), rp.facesPlan);
        assertEquals("-2147483648", rp.up);
        assertEquals(Integer.MAX_VALUE, rp.amount);
    }

    @Test public void integralDecimalOverflowDoesNotWrap() {
        for (String field : new String[]{"face", "faces_plan", "up", "amount"}) {
            for (String value : new String[]{"2147483648.0", "-2147483649.000", "+0002147483648,00"}) {
                assertThrows(JsonParseException.class,
                        () -> row("{\"" + field + "\":" + gson().toJson(value) + "}"));
            }
        }
    }

    @Test public void nonzeroFractionsAndExponentsStillUseExactRounding() {
        ReportPrepareDB rp = row("{\"face\":\"17.499999999999999999\",\"faces_plan\":17.500000000000000001,"
                + "\"up\":\"-17.000000000000000001\",\"amount\":1.75e1}");
        assertEquals("17", rp.face);
        assertEquals(Integer.valueOf(18), rp.facesPlan);
        assertEquals("-17", rp.up);
        assertEquals(18, rp.amount);
    }

    @Test public void malformedDecimalsAreNotAcceptedAsIntegerPrefixes() {
        for (String value : new String[]{"17.0.0", "17,0,0", "17. 0", "17.0x", ".", "-.0.0"}) {
            assertThrows(JsonParseException.class, () -> row("{\"amount\":" + gson().toJson(value) + "}"));
        }
    }

    @Test public void preservesMissingAndNullFieldContract() {
        for (String json : new String[]{"{}", "{\"face\":null,\"faces_plan\":null,\"up\":null,\"amount\":null}"}) {
            ReportPrepareDB rp = row(json);
            assertNull(rp.face);
            assertNull(rp.facesPlan);
            assertNull(rp.up);
            assertEquals(0, rp.amount);
        }
    }

    @Test public void preservesEmptyStringAndNullableIntegerContract() {
        ReportPrepareDB rp = row("{\"face\":\"\",\"up\":\" \",\"faces_plan\":\"\",\"amount\":\"null\"}");
        assertEquals("", rp.face);
        assertEquals(" ", rp.up);
        assertNull(rp.facesPlan);
        assertEquals(0, rp.amount);
    }

    @Test public void doesNotRoundMoneyDimensionsOrIdentifiers() {
        ReportPrepareDB rp = row("{\"ID\":4627702434,\"code_dad2\":\"1111126028847061515\",\"price\":\"17.95\","
                + "\"price_min\":\"21.25\",\"price_max\":\"21.99\",\"oborotved_num\":\"1.75\",\"expire_left\":\"2.5\"}");
        assertEquals(Long.valueOf(4627702434L), rp.iD);
        assertEquals("1111126028847061515", rp.codeDad2);
        assertEquals("17.95", rp.price);
        assertEquals("21.25", rp.priceMin);
        assertEquals("21.99", rp.priceMax);
        assertEquals("1.75", rp.oborotvedNum);
        assertEquals("2.5", rp.expireLeft);
        OtherData other = gson().fromJson("{\"amount\":2.75,\"width\":1.25,\"percent\":0.75}", OtherData.class);
        assertEquals(2.75, other.amount, 0);
        assertEquals(1.25, other.width, 0);
        assertEquals(0.75, other.percent, 0);
    }

    @Test public void unrelatedIntegerIdsRemainStrict() {
        assertThrows(JsonParseException.class, () -> row("{\"buyer_order_id\":17.9}"));
        assertThrows(JsonParseException.class, () -> gson().fromJson("{\"type\":2.5}", OtherData.class));
    }

    @Test public void invalidAndOverflowValuesAreNotReplacedWithZero() {
        for (String field : new String[]{"face", "faces_plan", "up", "amount"}) {
            for (String value : new String[]{"2147483647.5", "-2147483648.5", "\"bad\"", "\"NaN\"", "{}", "[]"}) {
                JsonParseException error = assertThrows(JsonParseException.class,
                        () -> row("{\"" + field + "\":" + value + "}"));
                assertTrue(error.toString(), error.toString().contains("$." + field));
            }
        }
    }

    @Test public void roundedBoundaryValuesFitInt() {
        ReportPrepareDB rp = row("{\"amount\":2147483647.49,\"faces_plan\":-2147483648.49}");
        assertEquals(Integer.MAX_VALUE, rp.amount);
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), rp.facesPlan);
    }

    @Test public void keepsAllRowsWhenMiddleRowHasFractions() {
        ReportPrepareServer response = gson().fromJson("{\"state\":true,\"list\":[{\"amount\":1},"
                + "{\"face\":\"17.9\",\"up\":\"2.5\",\"faces_plan\":1.5,\"amount\":3.5},{\"amount\":2}]}",
                ReportPrepareServer.class);
        assertTrue(response.getState());
        assertEquals(3, response.getList().size());
        assertEquals(4, response.getList().get(1).amount);
        assertEquals(2, response.getList().get(2).amount);
    }

    @Test public void annotationsAlsoApplyOutsideRetrofit() {
        ReportPrepareDB rp = new Gson().fromJson("{\"face\":1.5,\"amount\":2.5,\"faces_plan\":3.5,\"up\":4.5}", ReportPrepareDB.class);
        assertEquals("2", rp.face);
        assertEquals(3, rp.amount);
        assertEquals(Integer.valueOf(4), rp.facesPlan);
        assertEquals("5", rp.up);
    }

    @Test public void serializationDoesNotRoundExistingValuesOrChangeJsonTypes() {
        ReportPrepareDB rp = new ReportPrepareDB();
        rp.face = "17.9";
        rp.up = "2.5";
        rp.amount = 3;
        rp.facesPlan = 4;
        com.google.gson.JsonObject json = gson().toJsonTree(rp).getAsJsonObject();
        assertEquals("17.9", json.get("face").getAsString());
        assertEquals("2.5", json.get("up").getAsString());
        assertTrue(json.get("face").getAsJsonPrimitive().isString());
        assertTrue(json.get("amount").getAsJsonPrimitive().isNumber());
        assertTrue(json.get("faces_plan").getAsJsonPrimitive().isNumber());
    }

    static class OtherData {
        double amount;
        double width;
        double percent;
        int type;
    }
}
