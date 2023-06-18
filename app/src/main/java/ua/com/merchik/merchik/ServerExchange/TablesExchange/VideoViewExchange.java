package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.data.RetrofitResponse.ViewListResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class VideoViewExchange {

    public void downloadVideoViewTable(ExchangeInterface.ExchangeResponseInterface exchange){
        try {
            StandartData data = new StandartData();
            data.mod = "lesson";
            data.act = "view_list";

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<ViewListResponse> call = RetrofitBuilder.getRetrofitInterface().View_List_RESPONSE(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<ViewListResponse>() {
                @Override
                public void onResponse(Call<ViewListResponse> call, Response<ViewListResponse> response) {
                    try {
                        if (response.body() != null){
                            Globals.writeToMLOG("INFO", "downloadVideoViewTable/call.enqueue/onResponse/response.body()", "response.body(): " + response.body().list.size());

                            exchange.onSuccess(response.body().list);
                        }else {
                            Globals.writeToMLOG("INFO", "downloadVideoViewTable/call.enqueue/onResponse/response.body()", "response.body(): NULL");
//                            exchange.onFailure("Ошибка при обновлении Сотрудников(разбор данных). Передайте код ошибки Вашему руководителю. Код ошибки: ");
                        }
                    }catch (Exception e){
                        Globals.writeToMLOG("ERR", "downloadVideoViewTable/call.enqueue/onResponse/catch", "Exception e: " + e);
//                        exchange.onFailure("Ошибка при обновлении Сотрудников(разбор данных). Передайте код ошибки Вашему руководителю. Код ошибки: " + e);
                    }
                }

                @Override
                public void onFailure(Call<ViewListResponse> call, Throwable t) {
                    Globals.writeToMLOG("FAILURE", "downloadVideoViewTable/call.enqueue/onFailure", "Throwable t: " + t);
//                    exchange.onFailure("Ошибка сети. Проверьте интернет или повторите попытку позже. Код ошибки: " + t);
                }
            });

        }catch (Exception e){
            Globals.writeToMLOG("ERR", "downloadVideoViewTable/catch", "Exception e: " + e);
//            exchange.onFailure("Ошибка при обновлении Сотрудников. Передайте код ошибки Вашему руководителю. Код ошибки: " + e);
        }
    }
}
