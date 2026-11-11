package ua.com.merchik.merchik.retrofit;

import com.google.gson.Gson;
import org.junit.Test;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportPrepareServer;

public class ReportPrepareCountLargeJsonTest {
    @Test public void retainsAllFiftyThousandRowsWithFractionalCounts() {
        check(50_000);
    }

    @Test public void retainsAllHundredThousandRowsWithFractionalCounts() {
        check(100_000);
    }

    private void check(int rows) {
        Gson gson = ReportPrepareCountJsonTest.gson();
        String json = ReportPrepareJsonFixtures.response(rows, "fractional");
        ReportPrepareServer response = gson.fromJson(json, ReportPrepareServer.class);
        ReportPrepareJsonFixtures.verify(response, rows, "fractional", true);
    }
}
