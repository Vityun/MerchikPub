package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RetrofitResponse.PotentialClientResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class PotentialClientTableExchange {

    public void downloadPotentialClientTable(Clicks.clickStatusMsg click){

        StandartData data = new StandartData();
        data.mod = "potential_clients";
        data.act = "list";
        data.dt_change_from = Clock.getDatePeriod(-30);
        data.dt_change_to = Clock.getDatePeriod(1);

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<PotentialClientResponse> call = RetrofitBuilder.getRetrofitInterface().GET_POTENTIAL_CLIENT(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<PotentialClientResponse>() {
            @Override
            public void onResponse(Call<PotentialClientResponse> call, Response<PotentialClientResponse> response) {
                Log.e("test", "test" + response);
                if (response.isSuccessful()){
                    if (response.body() != null){
                        if (response.body().state){

                        }else {
                            click.onFailure("Ошибка запроса. ");
                        }
                    }else {
                        click.onFailure("Ошибка запроса. Тело пришло пустым.");
                    }
                }else {
                    click.onFailure("Ошибка запроса. Код: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PotentialClientResponse> call, Throwable t) {
                Log.e("test", "test" + t);
            }
        });
    }

}
