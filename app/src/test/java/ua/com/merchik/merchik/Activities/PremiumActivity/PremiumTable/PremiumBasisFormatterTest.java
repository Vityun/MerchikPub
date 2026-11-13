package ua.com.merchik.merchik.Activities.PremiumActivity.PremiumTable;

import com.google.gson.Gson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.Utils.text.MvsLinkedTextFormatter;
import ua.com.merchik.merchik.data.RetrofitResponse.models.PremiumResponse;

import static org.junit.Assert.*;

public class PremiumBasisFormatterTest {
    private final List<String> issues = new ArrayList<>();

    private PremiumResponse response() {
        PremiumResponse response = new PremiumResponse();
        response.state = true;
        return response;
    }

    private PremiumBasisFormatter.Result format(PremiumResponse response) {
        return PremiumBasisFormatter.format(response, "Requested document", issues::add);
    }

    @Test public void nullBodyAndMissingStateProduceMessage() {
        assertFalse(format(null).text.isEmpty());
        assertFalse(format(new PremiumResponse()).text.isEmpty());
        assertEquals(Arrays.asList("body=null", "state=null, error=null"), issues);
    }

    @Test public void falseStateDoesNotDisplayUntrustedResult() {
        PremiumResponse response = response();
        response.state = false;
        response.error = "No data";
        assertEquals("Requested document", format(response).title);
        assertTrue(issues.get(0).contains("state=false, error=No data"));
    }

    @Test public void nullAndEmptyListsHaveVisibleFallback() {
        PremiumResponse response = response();
        String missing = format(response).text;
        response.result_list = Collections.emptyList();
        assertEquals(missing, format(response).text);
        assertFalse(missing.isEmpty());
        assertEquals(Arrays.asList("result_list=null", "result_list=empty"), issues);
    }

    @Test public void allNullRowsHaveVisibleFallback() {
        PremiumResponse response = response();
        response.result_list = Arrays.asList(null, null);
        assertFalse(format(response).text.isEmpty());
        assertEquals(2, issues.size());
    }

    @Test public void missingRowDoesNotHideOtherRows() {
        PremiumResponse response = response();
        PremiumResponse.Result row = response.new Result();
        row.docNom = "DOC-1";
        row.docDat = "2026-11-13";
        row.sumNZPSotrDoc = -10.5f;
        row.osnovanie = "Reduction";
        response.result_list = Arrays.asList(null, row, null);
        PremiumBasisFormatter.Result actual = format(response);
        assertEquals("DOC-1 от 2026-11-13", actual.title);
        assertEquals("-10.5 грн. Reduction<br><br>", actual.text);
        assertEquals(2, issues.size());
    }

    @Test public void nullBasisDoesNotHideAmountOrNextExplanation() {
        PremiumResponse response = response();
        PremiumResponse.Result missing = response.new Result();
        missing.sumNZPSotrDoc = -5;
        missing.docNom = "DOC-1";
        PremiumResponse.Result valid = response.new Result();
        valid.osnovanie = "Next explanation";
        response.result_list = Arrays.asList(missing, valid);
        PremiumBasisFormatter.Result actual = format(response);
        assertTrue(actual.text.startsWith("-5.0 грн. "));
        assertTrue(actual.text.contains("Next explanation"));
        assertFalse(actual.text.contains("null"));
        assertEquals("row=0, responseDoc=DOC-1, osnovanie=empty", issues.get(0));
    }

    @Test public void blankBasisHasSameFallbackAsNull() {
        PremiumResponse response = response();
        PremiumResponse.Result row = response.new Result();
        response.result_list = Collections.singletonList(row);
        String missing = format(response).text;
        row.osnovanie = " \n ";
        assertEquals(missing, format(response).text);
    }

    @Test public void missingTitleFieldsDoNotRenderNull() {
        PremiumResponse response = response();
        PremiumResponse.Result row = response.new Result();
        row.osnovanie = "Text";
        response.result_list = Collections.singletonList(row);
        assertEquals("Requested document", format(response).title);
        row.docDat = "2026-11-13";
        assertEquals("2026-11-13", format(response).title);
        row.docDat = null;
        row.docNom = "DOC-1";
        assertEquals("DOC-1", format(response).title);
        row.docNom = null;
        assertEquals("", PremiumBasisFormatter.format(response, null, issues::add).title);
    }

    @Test public void retainsOrderAmountsAndLegacyAngleBracketConversion() {
        PremiumResponse response = response();
        PremiumResponse.Result first = response.new Result();
        first.osnovanie = "<first>";
        first.sumNZPSotrDoc = -10.25f;
        PremiumResponse.Result second = response.new Result();
        second.osnovanie = "second";
        second.sumNZPSotrDoc = 20;
        response.result_list = Arrays.asList(first, second);
        assertEquals("-10.25 грн. (first)<br><br>20.0 грн. second<br><br>", format(response).text);
    }

    @Test public void parsesJsonAndFormatsThreeFieldLinksAlongsideInvalidMarkers() {
        String json = "{\"state\":true,\"result_list\":[null,{\"DocNom\":\"DOC-1\","
                + "\"SumNZPSotrDoc\":-10.5,\"Osnovanie\":\"Before {bad} {фото|123|Photo} after\"}]}";
        PremiumBasisFormatter.Result formatted = format(new Gson().fromJson(json, PremiumResponse.class));
        String html = MvsLinkedTextFormatter.format(formatted.text, path -> "https://example.test/123", issues::add);
        assertEquals("-10.5 грн. Before {bad} <a href=\"https://example.test/123\">Photo</a> after<br><br>", html);
        assertEquals("DOC-1", formatted.title);
        assertEquals(2, issues.size());
    }
}
