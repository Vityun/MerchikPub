package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.ReportPrepareSync;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportPrepareServer;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class ReportPrepareExchange {

    private String date_from = "";
    private String date_to = "";
    private String code_dad2 = "";

    public ReportPrepareExchange() {
    }

    public ReportPrepareExchange(String date_from, String date_to, String code_dad2) {
        this.date_from = date_from;
        this.date_to = date_to;
        this.code_dad2 = code_dad2;
    }

    public void downloadReportPrepare(ExchangeInterface.ExchangeResponseInterface exchange) {
        SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.getSynchronizationTimetableRowByTable("report_prepare");
        JsonObject convertedObject = createRequest(date_from, date_to, code_dad2,
                synchronizationTimetableDB == null ? 0 : synchronizationTimetableDB.getVpi_app());

        ReportPrepareSync.download(convertedObject, false, new retrofit2.Callback<ReportPrepareServer>() {
            @Override
            public void onResponse(retrofit2.Call<ReportPrepareServer> call, retrofit2.Response<ReportPrepareServer> response) {
                Log.e("downloadReportPrepare", "response: " + response.body());
                if (response.isSuccessful() && response.body() != null && Boolean.TRUE.equals(response.body().getState()) &&
                        response.body().getList() != null) {
                    exchange.onSuccess(response.body().getList());
                } else exchange.onFailure("Throwable: Список пуст");


            }

            @Override
            public void onFailure(retrofit2.Call<ReportPrepareServer> call, Throwable t) {
                Log.e("downloadReportPrepare", "Throwable t: " + t);
                exchange.onFailure("Throwable t: " + t);
            }
        });
    }

    static JsonObject createRequest(String dateFrom, String dateTo, String dad2, long storedVpi) {
        StandartData data = new StandartData();
        data.mod = "report_prepare";
        data.act = "list_data";
        data.date_from = dateFrom;
        data.date_to = dateTo;
        data.code_dad2 = dad2;
        // A missing visit needs its full report, not only changes since the global cursor.
        data.dt_change_from = dad2 != null && !dad2.trim().isEmpty() ? "0" : String.valueOf(storedVpi);
        return new Gson().toJsonTree(data).getAsJsonObject();
    }
}
