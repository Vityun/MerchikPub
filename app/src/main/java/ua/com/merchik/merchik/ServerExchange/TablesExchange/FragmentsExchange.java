package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.data.Database.Room.FragmentSDB;
import ua.com.merchik.merchik.data.RetrofitResponse.FragmentsResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

/**
 * 11.04.23.
 * Загрузка Фрагментов.
 * Фрагменты это специальные области на фотографиях на которых мерчам отмечено важная часть.
 *
 * Ниже описание от программиста сайта:
 * утра, добавлена новая точка входа для получения выделенных участков на фото
 *
 * mod=images_view
 * act=list_image_region
 *
 * фильтры:
 * dt_change_from - впи с
 * dt_change_to - впи по
 *
 * не уверен, что будет нужно,но на всякий доп фильтры по аналогии с фотоотчётами
 * hash_list - по хэшу фоток
 * code_dad2 - по дад2
 * id - ID фото
 * date_from - дата с (фотографии) по дефолту 1 месяц назад
 * date_to - дата по (фотографии) по дефолту сегодняшний день
 * */
public class FragmentsExchange {

    public void downloadFragmentsTable(ExchangeInterface.ExchangeResponseInterface exchange){
        StandartData data = new StandartData();
        data.mod = "images_view";
        data.act = "list_image_region";

        List<FragmentSDB> fragment = SQL_DB.fragmentDao().getAll();
        if (fragment != null && fragment.size() > 0){
            data.dt_change_from = String.valueOf(SQL_DB.fragmentDao().getLastDtUpdate());
        }else {
            data.dt_change_from = String.valueOf(Clock.getDatePeriodLong(System.currentTimeMillis(), -60) / 1000);
        }

        data.dt_change_to = String.valueOf(Clock.getDatePeriodLong(System.currentTimeMillis(), 1) / 1000);

        Gson gson = new Gson();
        String json = gson.toJson(data);
        JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

        retrofit2.Call<FragmentsResponse> call = RetrofitBuilder.getRetrofitInterface().FRAGMENTS_TABLE_RESPONSE(RetrofitBuilder.contentType, convertedObject);
        call.enqueue(new retrofit2.Callback<FragmentsResponse>() {
            @Override
            public void onResponse(Call<FragmentsResponse> call, Response<FragmentsResponse> response) {
                Log.e("test", "response: " + response);
                if (response.body() != null){
                    SQL_DB.fragmentDao().insertData(response.body().list)
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    Log.e("downloadFragmentsTable", "onComplete");
                                }

                                @Override
                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                    Log.e("downloadFragmentsTable", "onError/Throwable: " + e);
                                }
                            });
                }
            }

            @Override
            public void onFailure(retrofit2.Call<FragmentsResponse> call, Throwable t) {
                Log.e("test", "Throwable: " + t);
            }
        });
    }

}
