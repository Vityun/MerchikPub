package ua.com.merchik.merchik.dialogs.EKL;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.EKL_SDB;
import ua.com.merchik.merchik.data.TestJsonUpload.DataEKL;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class EKLRequests {

    public void getPTTByAddress(int addressId, Clicks.clickText clickText) {
        StandartData data = new StandartData();
        data.mod = "data_list";
        data.act = "ptt";
        data.addressId = String.valueOf(addressId);

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<JsonObject> call = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e("EKLRequests", "response" + response);
                Log.e("EKLRequests", "response" + response.body());
                Globals.writeToMLOG("INFO", "EKLRequests/getPTTByAddress/onResponse", "response.body(): " + response.body());
                clickText.click("Данні завантажились успішно.");
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("EKLRequests", "test" + t);
                Globals.writeToMLOG("INFO", "EKLRequests/getPTTByAddress/onFailure", "Throwable t: " + t);
                clickText.click("При завантаженні данних виникла помилка: " + t);
            }
        });
    }


    /**
     * Получение на выгрузку и выгрузка Кодов пользователя
     */
    public void responseCheckEKLList() {
        try {
            List<EKL_SDB> list = SQL_DB.eklDao().getEKLToUpload();
            Globals.writeToMLOG("RESP", "EKLRequests.responseCheckEKLList/onSuccess", "list: " + list);
            if (list != null) {
                Globals.writeToMLOG("RESP", "EKLRequests.responseCheckEKLList/onSuccess", "list: " + list.size());
            }
            responseCheckEKLCode(list, new ExchangeInterface.ExchangeResponseInterfaceSingle() {
                @Override
                public <T> void onSuccess(T data) {
                    updateEKLData((DialogEKL.EKLCheckData) data);
                    Globals.writeToMLOG("RESP", "EKLRequests.responseCheckEKLList/onSuccess", "data: " + data);
                }

                @Override
                public void onFailure(String error) {
                    Globals.writeToMLOG("RESP", "EKLRequests.responseCheckEKLList/onFailure", "String error: " + error);
                }
            }, Globals.AppWorkMode.OFFLINE, true);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "EKLRequests.responseCheckEKLList", "Exception e: " + e);
        }
    }


    /**
     * 14.07.2021
     * Отправка кода подтверждения на сервер. Анализ ответа.
     */
    public void responseCheckEKLCode(List<EKL_SDB> eklSdbList, ExchangeInterface.ExchangeResponseInterfaceSingle exchange, Globals.AppWorkMode appMode, boolean sendMode) {

        if (eklSdbList == null || eklSdbList.size() == 0) return;

        StandartData data = new StandartData();
        data.mod = "sms_verification";
        data.act = "verification_check";

        Log.e("DialogEKL", "responseCheckEKLCode_1: " + data);
        if (sendMode) {
            List<DataEKL> list = new ArrayList<>();
            for (EKL_SDB item : eklSdbList) {
                DataEKL ekl = new DataEKL();
                ekl.id = item.id;
                ekl.element_id = item.id;
                ekl.code = item.code;
                list.add(ekl);
            }

            data.data = list;

            if (data.data.size() == 0) {
//                Toast.makeText(context, "Данных на выгрузку нет", Toast.LENGTH_LONG).show();
                return;
            }
        }

        Log.e("DialogEKL", "responseCheckEKLCode_5: " + data);


        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Log.e("DialogEKL", "sendEKL/dataSend: " + convertedObject);


        Log.e("DialogEKL", "LOOP.POS6");
        retrofit2.Call<DialogEKL.EKLCheckData> call = RetrofitBuilder.getRetrofitInterface().EKL_CHECK_DATA_CALL(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<DialogEKL.EKLCheckData>() {
            @Override
            public void onResponse(Call<DialogEKL.EKLCheckData> call, Response<DialogEKL.EKLCheckData> response) {
                if (response.body() != null) {
                    if (response.body().state) {
                        Log.e("DialogEKL", "LOOP.POS7");
                        exchange.onSuccess((DialogEKL.EKLCheckData) response.body());
                    } else {
                        exchange.onFailure("Ошибка со стороны сервера: " + response.body().error);
                    }
                } else {
                    exchange.onFailure("Ответ с сервера пустой. Повторите попытку позже.");
                }
            }

            @Override
            public void onFailure(Call<DialogEKL.EKLCheckData> call, Throwable t) {
                exchange.onFailure(t.toString());
            }
        });
    }


    private void updateEKLData(DialogEKL.EKLCheckData data) {
        try {
            DialogEKL.EKLCheckData res = (DialogEKL.EKLCheckData) data;
            Gson gson = new Gson();
            String json = gson.toJson(res);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
            Globals.writeToMLOG("RESP", "EKLRequests.updateEKLData/", "convertedObject: " + convertedObject);
            if (data.state) {
                Globals.writeToMLOG("RESP", "EKLRequests.updateEKLData/", "Код принят и будет проверен: ");
            } else {
                Globals.writeToMLOG("RESP", "EKLRequests.updateEKLData/", "При проверке кода произошла ошибка: " + data.error);
            }

            for (DialogEKL.EKLCheckData.EKLCheckDataList item : res.list) {
                CompositeDisposable disposable = new CompositeDisposable();
                disposable.add(
                        SQL_DB.eklDao().getById(item.id)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe((EKL_SDB ekl_sdb) -> {
                                            if (item.state) {
                                                ekl_sdb.eklCode = ekl_sdb.eklHashCode;
                                                ekl_sdb.upload = true;
                                            } else {
                                                ekl_sdb.comment = item.error;
                                            }
                                            SQL_DB.eklDao().insertAll(Collections.singletonList(ekl_sdb));
                                            disposable.dispose();
                                        }
                                )
                );
            }
        } catch (Exception e) {
            Globals.writeToMLOG("RESP", "EKLRequests.updateEKLData/", "Exception e: " + e);
        }
    }



    /**
     * 14.07.2021
     * Запрос на отправку СМСки ПТТшнику.
     */
//    public void responseSendPTTEKLCode(ExchangeInterface.ExchangeResponseInterfaceSingle exchange) {
//        StandartData data = new StandartData();
//        data.mod = "sms_verification";
//        data.act = "verification_send";
//
//        data.option_id = 84007;
//        data.sotr_id = String.valueOf(user.id);
//        data.code_dad2 = String.valueOf(wp.getCode_dad2());
//        data.tel_type = telType;
//
//        Gson gson = new Gson();
//        String json = gson.toJson(data);
//        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
//
//        Log.e("DialogEKL", "sendStartEKL/dataSend: " + convertedObject);
//
//        retrofit2.Call<DialogEKL.EKLRespData> call = RetrofitBuilder.getRetrofitInterface().EKL_RESP_DATA_CALL(RetrofitBuilder.contentType, convertedObject);
//        call.enqueue(new Callback<DialogEKL.EKLRespData>() {
//            @Override
//            public void onResponse(Call<DialogEKL.EKLRespData> call, Response<DialogEKL.EKLRespData> response) {
//                if (response.body() != null) {
//                    if (response.body().state) {
//                        exchange.onSuccess(response.body());
//                    } else {
//                        exchange.onFailure("Ошибка со стороны сервера: " + response.body().error);
//                    }
//                } else {
//                    exchange.onFailure("Ответ с сервера пустой. Повторите попытку позже.");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<DialogEKL.EKLRespData> call, Throwable t) {
//                exchange.onFailure(t.toString());
//            }
//        });
//    }
}
