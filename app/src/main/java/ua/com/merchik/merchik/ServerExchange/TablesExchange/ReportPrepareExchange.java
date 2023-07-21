package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.data.RetrofitResponse.ReportPrepareServer;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

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

    public void downloadReportPrepare(ExchangeInterface.ExchangeResponseInterface exchange){
        StandartData data = new StandartData();
        data.mod = "report_prepare";
        data.act = "list_data";
        data.date_from = date_from;
        data.date_to = date_to;
        data.code_dad2 = code_dad2;

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<ReportPrepareServer> call = RetrofitBuilder.getRetrofitInterface().ReportPrepareServer_RESPONSE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new retrofit2.Callback<ReportPrepareServer>() {
            @Override
            public void onResponse(retrofit2.Call<ReportPrepareServer> call, retrofit2.Response<ReportPrepareServer> response) {
                Log.e("downloadReportPrepare", "response: " + response.body());
                exchange.onSuccess(response.body().getList());
            }

            @Override
            public void onFailure(retrofit2.Call<ReportPrepareServer> call, Throwable t) {
                Log.e("downloadReportPrepare", "Throwable t: " + t);
                exchange.onFailure("Throwable t: " + t);
            }
        });
    }
}
