package ua.com.merchik.merchik.ServerExchange;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.List;
import java.util.function.Function;

import ua.com.merchik.merchik.data.RetrofitResponse.models.OptionsServer;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportPrepareServer;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ThemeTableRespose;
import ua.com.merchik.merchik.data.RetrofitResponse.models.TovarTableResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.models.WpDataServer;

import static org.junit.Assert.*;

public class StartupResponseModelsTest {
    private static final String LEGACY_EMPTY = "{\"state\":false,\"error\":\""
            + "\u0421\u043f\u0438\u0441\u043e\u043a \u043f\u0443\u0441\u0442\"}";

    @Test public void planResponsesPreserveEmptyAndErrorDistinction() {
        checkVisit(WpDataServer.class, WpDataServer::getState, WpDataServer::getError, WpDataServer::getList);
    }

    @Test public void optionResponsesPreserveEmptyAndErrorDistinction() {
        checkVisit(OptionsServer.class, OptionsServer::getState, OptionsServer::getError, OptionsServer::getList);
    }

    @Test public void reportPrepareResponsesPreserveEmptyAndErrorDistinction() {
        checkVisit(ReportPrepareServer.class, ReportPrepareServer::getState,
                ReportPrepareServer::getError, ReportPrepareServer::getList);
    }

    @Test public void productResponsesPreserveEmptyAndErrorDistinction() {
        checkVisit(TovarTableResponse.class, TovarTableResponse::getState,
                TovarTableResponse::getError, TovarTableResponse::getList);
    }

    @Test public void themeModelRetainsLegacyErrorSoEmptyDeltaCanBeRecognized() {
        ThemeTableRespose body = new Gson().fromJson(LEGACY_EMPTY, ThemeTableRespose.class);
        assertNotNull(body.getError());
        assertTrue(VisitDataResponsePolicy.requireDictionary(200,
                body.getState(), body.getError(), body.getList(), 499).isEmpty());
    }

    @Test(expected = IllegalStateException.class) public void emptyThemeResponseDoesNotCompleteFirstDownload() {
        ThemeTableRespose body = new Gson().fromJson(LEGACY_EMPTY, ThemeTableRespose.class);
        VisitDataResponsePolicy.requireDictionary(200, body.getState(), body.getError(), body.getList(), 0);
    }

    private static <T> void checkVisit(Class<T> type, Function<T, Boolean> state,
                                       Function<T, String> error, Function<T, List<?>> list) {
        Gson gson = new Gson();
        for (String json : new String[]{LEGACY_EMPTY, "{\"state\":true,\"list\":[]}"}) {
            T body = gson.fromJson(json, type);
            assertTrue(VisitDataResponsePolicy.requireList(200,
                    state.apply(body), error.apply(body), list.apply(body)).isEmpty());
        }
        for (String json : new String[]{"{}", "{\"state\":true}",
                "{\"state\":false,\"error\":\"no access\",\"list\":[]}"}) {
            T body = gson.fromJson(json, type);
            try {
                VisitDataResponsePolicy.requireList(200, state.apply(body), error.apply(body), list.apply(body));
                fail(type.getSimpleName() + " accepted an invalid response");
            } catch (IllegalStateException expected) {
                assertNotNull(expected.getMessage());
            }
        }
    }
}
