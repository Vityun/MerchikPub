package ua.com.merchik.merchik.ServerExchange.TablesExchange;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.QuestionAnswerDB;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.QuestionAnswerResponse;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.data.UploadToServ.QuestionAnswerUpload;
import ua.com.merchik.merchik.data.UploadToServ.QuestionAnswerUploadResponse;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

public class QuestionExchange {



    public void downloadQuestionAnswer() {
        try {
            StandartData data = new StandartData();
            data.mod = "quest_data";
            data.act = "list";
//            data.code_iza = getIZAList();

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);
            Log.e("downloadQuestionAnswer", "quest_data.list start");
            RetrofitBuilder.getRetrofitInterface().GET_QUESTION_LIST(RetrofitBuilder.contentType, convertedObject)
                    .enqueue(new Callback<QuestionAnswerResponse>() {
                        @Override
                        public void onResponse(Call<QuestionAnswerResponse> call, Response<QuestionAnswerResponse> response) {
                            Log.e("downloadQuestionAnswer", "quest_data.list onResponse");
                            QuestionAnswerResponse body = response.body();
                            int listSize = body != null && body.getList() != null ? body.getList().size() : 0;
                            Boolean state = body != null ? body.getState() : null;
                            String error = body != null ? body.getError() : null;

                            Globals.writeToMLOG(
                                    "INFO",
                                    "downloadQuestionAnswer/onResponse",
                                    "quest_data.list code=" + response.code()
                                            + ", successful=" + response.isSuccessful()
                                            + ", state=" + state
                                            + ", listSize=" + listSize
                                            + ", error=" + error
                            );

                            if (response.isSuccessful()
                                    && body != null
                                    && Boolean.TRUE.equals(body.getState())
                                    && body.getList() != null
                                    && !body.getList().isEmpty()) {
                                Log.e("downloadQuestionAnswer", "quest_data.list size: " + listSize);
                                SQL_DB.questionAnswerDao().insertAll(body.getList());
                            }
//                            if (response.isSuccessful() && response.body() != null &&
//                            response.body())
                        }

                        @Override
                        public void onFailure(Call<QuestionAnswerResponse> call, Throwable t) {
                            Log.e("downloadQuestionAnswer", "quest_data.list onFailure: " + t.getMessage());
                            Globals.writeToMLOG(
                                    "ERROR",
                                    "downloadQuestionAnswer/onFailure",
                                    "quest_data.list Throwable: " + t + "\n" + Log.getStackTraceString(t)
                            );

                        }
                    });
//            RetrofitBuilder.getRetrofitInterface().averageSalary(RetrofitBuilder.contentType, convertedObject)
//                    .enqueue(new Callback<JsonObject>() {
//                        @Override
//                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                            Log.e("!!!!!!!!","+++++++++++");
//                        }
//
//                        @Override
//                        public void onFailure(Call<JsonObject> call, Throwable t) {
//                            Log.e("!!!!!!!!","+++++++++++");
//
//                        }
//                    });



        } catch (Exception e) {
            Log.e("MenuMainTest", "Exception e.t:" + e);
        }
    }

    private boolean isQuestionAnswersUploading = false;

    private static final long TEST_MIN_QUESTION_ANSWER_ID = 1781248749450L;

    public void uploadQuestionAnswers() {
        if (isQuestionAnswersUploading) return;

        isQuestionAnswersUploading = true;

        try {


            StandartData data = new StandartData();
            data.mod = "quest_data";
            data.act = "add_row";

            SynchronizationTimetableDB synchronizationTimetableDB = INSTANCE.copyFromRealm(RealmManager.getSynchronizationTimetableRowByTable("question_answer"));

            final List<QuestionAnswerDB> list =
                    SQL_DB.questionAnswerDao().getAllForUploadTest(synchronizationTimetableDB.getVpi_app());

            if (list == null || list.isEmpty()) {
                Log.e("QuestionAnswerUpload", "Нет данных для выгрузки");
                isQuestionAnswersUploading = false;
                return;
            }

            List<QuestionAnswerUpload> dataList = new ArrayList<>();

            for (QuestionAnswerDB item : list) {
                dataList.add(QuestionAnswerUpload.fromDb(item));
            }

            data.data = dataList;

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = gson.fromJson(json, JsonObject.class);

            Log.e("QuestionAnswerUpload", "request: " + convertedObject);

            Globals.writeToMLOG(
                    "INFO",
                    "uploadQuestionAnswersTest",
                    "request: " + convertedObject
            );

            Call<QuestionAnswerUploadResponse> call =
                    RetrofitBuilder.getRetrofitInterface()
                            .QUESTION_ANSWER_UPLOAD(
                                    RetrofitBuilder.contentType,
                                    convertedObject
                            );

            call.enqueue(new Callback<QuestionAnswerUploadResponse>() {
                @Override
                public void onResponse(
                        Call<QuestionAnswerUploadResponse> call,
                        Response<QuestionAnswerUploadResponse> response
                ) {
                    try {
                        Log.e("QuestionAnswerUpload", "response.code(): " + response.code());

                        QuestionAnswerUploadResponse body = response.body();

                        Log.e("QuestionAnswerUpload", "response.body(): " + new Gson().toJson(body));

                        Globals.writeToMLOG(
                                "INFO",
                                "uploadQuestionAnswersTest/onResponse",
                                "response.code(): " + response.code()
                        );

                        Globals.writeToMLOG(
                                "INFO",
                                "uploadQuestionAnswersTest/onResponse",
                                "response.body(): " + new Gson().toJson(body)
                        );

                        if (response.isSuccessful()
                                && body != null
                                && Boolean.TRUE.equals(body.state)) {

                            Log.e("QuestionAnswerUpload", "Выгрузка успешна. Кол-во: " + list.size());

                            Globals.writeToMLOG(
                                    "INFO",
                                    "uploadQuestionAnswersTest/onResponse",
                                    "successful, count: " + list.size()
                            );

                            // Пока для теста ничего локально не обновляем.
                            // Когда поймём точный ответ сервера, тут можно будет
                            // сохранить mnenie_id / server id в локальную БД.
                            long lastId;

                            if (!list.isEmpty()) {
                                lastId = list.get(list.size() - 1).getId();
                            } else {
                                lastId = 10000000L;
                            }
                            INSTANCE.executeTransaction(realm -> {
                                synchronizationTimetableDB.setVpi_app(lastId);
                                realm.copyToRealmOrUpdate(synchronizationTimetableDB);
                            });

                        } else {
                            String error = body != null ? body.error : "body is null";

                            Log.e("QuestionAnswerUpload", "Ошибка выгрузки: " + error);

                            Globals.writeToMLOG(
                                    "ERROR",
                                    "uploadQuestionAnswersTest/onResponse",
                                    "upload error: " + error
                            );
                        }

                    } catch (Exception e) {
                        Log.e("QuestionAnswerUpload", "Exception: " + e);

                        Globals.writeToMLOG(
                                "ERROR",
                                "uploadQuestionAnswersTest/onResponse/catch",
                                "Exception: " + e
                        );

                    } finally {
                        isQuestionAnswersUploading = false;
                    }
                }

                @Override
                public void onFailure(Call<QuestionAnswerUploadResponse> call, Throwable t) {
                    Log.e("QuestionAnswerUpload", "Throwable: " + t);

                    Globals.writeToMLOG(
                            "ERROR",
                            "uploadQuestionAnswersTest/onFailure",
                            "Throwable: " + t
                    );

                    isQuestionAnswersUploading = false;
                }
            });

        } catch (Exception e) {
            Log.e("QuestionAnswerUpload", "Exception: " + e);

            Globals.writeToMLOG(
                    "ERROR",
                    "uploadQuestionAnswersTest/catch",
                    "Exception: " + e
            );

            isQuestionAnswersUploading = false;
        }
    }
}
