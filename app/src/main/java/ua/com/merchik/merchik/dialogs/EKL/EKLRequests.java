package ua.com.merchik.merchik.dialogs.EKL;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.ErrorData;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.EKL_SDB;
import ua.com.merchik.merchik.data.TestJsonUpload.DataEKL;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class EKLRequests {

    class Test {
        public String mod;
        public String act;
        public String addr_id;
    }

    public class PTTRequest {
        @SerializedName("state")
        @Expose
        public Boolean state;

        @SerializedName("list")
        @Expose
        public List<PTT> list;

        @SerializedName("error")
        @Expose
        public String error;
    }

    public class PTT {
        @SerializedName("user_id")
        @Expose
        public String userId;
        @SerializedName("fio")
        @Expose
        public String fio;
        @SerializedName("tel")
        @Expose
        public String tel;
        @SerializedName("tel2")
        @Expose
        public String tel2;
        @SerializedName("department")
        @Expose
        public Object department;
        @SerializedName("otdel_id")
        @Expose
        public Object otdelId;
        @SerializedName("client_id")
        @Expose
        public String clientId;
        @SerializedName("fired")
        @Expose
        public Integer fired;
        @SerializedName("fired_dt")
        @Expose
        public Integer firedDt;
        @SerializedName("fired_reason")
        @Expose
        public Object firedReason;
        @SerializedName("dt_update")
        @Expose
        public Object dtUpdate;
        @SerializedName("author_id")
        @Expose
        public Object authorId;
        @SerializedName("city_id")
        @Expose
        public Integer cityId;
        @SerializedName("work_addr_id")
        @Expose
        public Object workAddrId;
        @SerializedName("inn")
        @Expose
        public String inn;
        @SerializedName("report_count")
        @Expose
        public String reportCount;
        @SerializedName("report_date_01")
        @Expose
        public Object reportDate01;
        @SerializedName("report_date_05")
        @Expose
        public Object reportDate05;
        @SerializedName("report_date_20")
        @Expose
        public Object reportDate20;
        @SerializedName("report_date_40")
        @Expose
        public Object reportDate40;
        @SerializedName("img_personal_photo_thumb")
        @Expose
        public String imgPersonalPhotoThumb;
        @SerializedName("img_personal_photo")
        @Expose
        public String imgPersonalPhoto;
        @SerializedName("send_sms")
        @Expose
        public Integer sendSms;
    }

    //    получение сотрудников из 1С если нет в базе данных
    public void getPTTByAddress(int addressId, Clicks.clickObjectAndStatus click) {
        Test data = new Test();
        data.mod = "data_list";
        data.act = "ptt";
        data.addr_id = String.valueOf(addressId);

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        Log.e("EKLRequests", "convertedObject" + convertedObject);

        Call<PTTRequest> call = RetrofitBuilder.getRetrofitInterface().GET_PTT_LIST(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<PTTRequest>() {
            @Override
            public void onResponse(Call<PTTRequest> call, Response<PTTRequest> response) {
                Log.e("EKLRequests", "response" + response);
                Log.e("EKLRequests", "response" + response.body());
                Globals.writeToMLOG("INFO", "EKLRequests/getPTTByAddress/onResponse", "response.body(): " + response.body());
                if (response.body() != null && response.body().state && response.body().list != null && !response.body().list.isEmpty())
                    click.onSuccess(response.body());
                else {
                    if ((response.body() != null ? response.body().error : null) != null)
                        click.onFailure(response.body().error);
                    else
                        click.onFailure("Виникла помилка");
                }
            }

            @Override
            public void onFailure(Call<PTTRequest> call, Throwable t) {
                Log.e("EKLRequests", "test" + t);
                Globals.writeToMLOG("INFO", "EKLRequests/getPTTByAddress/onFailure", "Throwable t: " + t);
                click.onFailure("При завантаженні данних виникла помилка: " + t);
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
            if (list == null || list.isEmpty()) {
                Globals.writeToMLOG("RESP", "EKLRequests.responseCheckEKLList/onSuccess", "list: " + list.size());
                return;
            }
            responseCheckEKLCode(list, new ExchangeInterface.ExchangeResponseInterfaceSingle() {
                @Override
                public <T> void onSuccess(T data) {
                    updateEKLData((DialogEKL.EKLCheckData) data);
                    Globals.writeToMLOG("RESP", "EKLRequests.responseCheckEKLList/onSuccess", "data: " + data);
                }

                @Override
                public void onFailure(ErrorData errorData) {

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
        Call<DialogEKL.EKLCheckData> call = RetrofitBuilder.getRetrofitInterface().EKL_CHECK_DATA_CALL(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new Callback<DialogEKL.EKLCheckData>() {
            @Override
            public void onResponse(Call<DialogEKL.EKLCheckData> call, Response<DialogEKL.EKLCheckData> response) {
                if (response.body() != null) {
                    if (response.body().state) {
                        Log.e("DialogEKL", "LOOP.POS7");
                        exchange.onSuccess((DialogEKL.EKLCheckData) response.body());
                    } else {
                        String errorType = response.body().error_type != null ? response.body().error_type : "unknown_error";
                        String error = response.body().error != null ? response.body().error : "Не удалось отправить сообщение. Попробуйте повторить отправку через 5 минут.";
                        exchange.onFailure(new ErrorData(errorType, error));
                    }
                } else {
                    exchange.onFailure(new ErrorData("error_send_failed", "Не удалось отправить сообщение. Попробуйте повторить отправку через 5 минут."));
                }
            }

            @Override
            public void onFailure(Call<DialogEKL.EKLCheckData> call, Throwable t) {
                exchange.onFailure(new ErrorData("unknown_error", t.toString()));
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
