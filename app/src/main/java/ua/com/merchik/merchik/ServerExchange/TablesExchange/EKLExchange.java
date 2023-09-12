package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.data.Database.Room.EKL_SDB;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class EKLExchange {
    public void downloadEKLTable(ExchangeInterface.ExchangeResponseInterface exchange){
        try {
            StandartData data = new StandartData();
            data.mod = "sms_verification";
            data.act = "list";
            SynchronizationTimetableDB synchronizationTimetableDB = RealmManager.getSynchronizationTimetableRowByTable("ekl_sql");
            try {
                if (synchronizationTimetableDB != null){
                    String dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app());
                    if (dt_change_from.equals("0")){
                        data.dt_change_from = "0";
                    }else {
                        data.dt_change_from = String.valueOf(synchronizationTimetableDB.getVpi_app()-120);  // минус 2 минуты для "синхрона". Это надо поменять.
                    }
                }else {
                    data.dt_change_from = "0";
                }
            }catch (Exception e){
                data.dt_change_from = "0";
            }



            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<EKLResponse> call = RetrofitBuilder.getRetrofitInterface().GET_EKL_ROOM(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<EKLResponse>() {
                @Override
                public void onResponse(Call<EKLResponse> call, Response<EKLResponse> response) {
                    try {
                        if (response.body() != null){
                            Log.e("downloadEKLTable", "response.body(): " + response.body());
                            Globals.writeToMLOG("INFO", "downloadEKLTable/call.enqueue/onResponse/response.body()", "response.body(): " + response.body().list.size());
                            RealmManager.INSTANCE.executeTransaction(realm -> {
                                if (synchronizationTimetableDB != null){
                                    synchronizationTimetableDB.setVpi_app(System.currentTimeMillis()/1000);
                                    realm.copyToRealmOrUpdate(synchronizationTimetableDB);
                                }

                            });
                            exchange.onSuccess(response.body().list);
                        }else {
                            Globals.writeToMLOG("INFO", "downloadEKLTable/call.enqueue/onResponse/response.body()", "response.body(): NULL");
                            exchange.onFailure("Ошибка при обновлении ЄКЛ(разбор данных). Передайте код ошибки Вашему руководителю. Код ошибки: ");
                        }
                    }catch (Exception e){
                        Globals.writeToMLOG("ERR", "downloadEKLTable/call.enqueue/onResponse/catch", "Exception e: " + e);
                        exchange.onFailure("Ошибка при обновлении ЄКЛ(разбор данных). Передайте код ошибки Вашему руководителю. Код ошибки: " + e);
                    }
                }

                @Override
                public void onFailure(Call<EKLResponse> call, Throwable t) {
                    Globals.writeToMLOG("FAILURE", "downloadEKLTable/call.enqueue/onFailure", "Throwable t: " + t);
                    exchange.onFailure("Ошибка сети. Проверьте интернет или повторите попытку позже. Код ошибки: " + t);
                }
            });

        }catch (Exception e){
            Globals.writeToMLOG("ERR", "downloadEKLTable/catch", "Exception e: " + e);
            exchange.onFailure("Ошибка при обновлении ЄКЛ. Передайте код ошибки Вашему руководителю. Код ошибки: " + e);
        }
    }

    public class EKLResponse {
        @SerializedName("state")
        @Expose
        public Boolean state;

        @SerializedName("list")
        @Expose
        public List<EKL_SDB> list = null;

        @SerializedName("error")
        @Expose
        public String error;

        @SerializedName("server_time")
        @Expose
        public Long serverTime;
    }
}
