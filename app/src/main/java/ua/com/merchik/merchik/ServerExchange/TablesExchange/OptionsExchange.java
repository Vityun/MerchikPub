package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.RnoVisitDataExchange;
import ua.com.merchik.merchik.data.RetrofitResponse.models.OptionsServer;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class OptionsExchange {

    private String date_from = "";
    private String date_to = "";
    private String code_dad2 = "";

    public OptionsExchange() {
    }

    public OptionsExchange(String date_from, String date_to, String code_dad2) {
        this.date_from = date_from;
        this.date_to = date_to;
        this.code_dad2 = code_dad2;
    }

    public void downloadOptions(ExchangeInterface.ExchangeResponseInterface exchange) {
        Log.e("EX_downloadOptions", "downloadOptions: START, dad2=" + code_dad2);
        JsonObject convertedObject = createRequest(date_from, date_to, code_dad2);
        RnoVisitDataExchange.authorizeTargetedRequest(convertedObject);

        retrofit2.Call<OptionsServer> call = RetrofitBuilder.getRetrofitInterface().GET_OPTIONS(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new retrofit2.Callback<OptionsServer>() {
            @Override
            public void onResponse(retrofit2.Call<OptionsServer> call, retrofit2.Response<OptionsServer> response) {
                Log.e("downloadOptions", "response: code=" + response.code()
                        + ", success=" + response.isSuccessful()
                        + ", state=" + (response.body() != null ? response.body().getState() : null)
                        + ", listSize=" + (response.body() != null && response.body().getList() != null ? response.body().getList().size() : 0));
                if (response.isSuccessful() && response.body() != null
                        && Boolean.TRUE.equals(response.body().getState()) && response.body().getList() != null
                        && !response.body().getList().isEmpty())
                    exchange.onSuccess(response.body().getList());
                else {
                    if (response.body() != null && response.body().getError() != null)
                        exchange.onFailure(response.body().getError());
                    else
                        exchange.onFailure("Виникла помилка");

                }

            }

            @Override
            public void onFailure(retrofit2.Call<OptionsServer> call, Throwable t) {
                Log.e("downloadOptions", "Throwable t: " + t);
                exchange.onFailure("Throwable t: " + t);
            }
        });
    }

    static JsonObject createRequest(String dateFrom, String dateTo, String dad2) {
        StandartData data = new StandartData();
        data.mod = "plan";
        data.act = "options_list";
        data.date_from = dateFrom;
        data.date_to = dateTo;
        data.code_dad2 = dad2;
        // A legacy date-only request has no numeric dad2 to parse.
        return new Gson().toJsonTree(data).getAsJsonObject();
    }
}
