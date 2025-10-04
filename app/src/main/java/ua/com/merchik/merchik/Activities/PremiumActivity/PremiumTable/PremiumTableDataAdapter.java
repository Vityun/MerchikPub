package ua.com.merchik.merchik.Activities.PremiumActivity.PremiumTable;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.ExchangeInterface;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.OptionsExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.ReportPrepareExchange;
import ua.com.merchik.merchik.ServerExchange.TablesExchange.WPDataExchange;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.PremiumResponse;
import ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium.Detailed;
import ua.com.merchik.merchik.data.TestJsonUpload.StandartData;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.BlockingProgressDialog;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

// Pika
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import static ua.com.merchik.merchik.Globals.userId;

public class PremiumTableDataAdapter extends RecyclerView.Adapter<PremiumTableDataAdapter.PremiumTableHeaderViewHolder> {

    private List<Detailed> data;
    private PremiumTableHeaderAdapter.PremiumListener listener;

    public PremiumTableDataAdapter(List<Detailed> data) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PremiumTableDataAdapter.PremiumTableHeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PremiumTableDataAdapter.PremiumTableHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.premium_table_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PremiumTableDataAdapter.PremiumTableHeaderViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class PremiumTableHeaderViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layout;
        private TextView name;
        private TextView column1, column5, column2, column3, column4;
        private String dataDownload200, dataDownloadError, dataDownloadWait;

        public PremiumTableHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.premium_table_item);
            name = itemView.findViewById(R.id.name);
            name.setPaintFlags(name.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            column1 = itemView.findViewById(R.id.col1);
            column5 = itemView.findViewById(R.id.col5);
            column2 = itemView.findViewById(R.id.col2);
            column3 = itemView.findViewById(R.id.col3);
            column4 = itemView.findViewById(R.id.col4);

            dataDownload200 = itemView.getContext().getString(R.string.data_download_200);
            dataDownloadError = itemView.getContext().getString(R.string.data_download_error);
            dataDownloadWait = itemView.getContext().getString(R.string.data_download_wait);
        }

        public void bind(Detailed detailed) {
            WpDataDB wpDataDB = findDocument(detailed.codeDad2);
            if (wpDataDB != null) {
                name.setTextColor(-10987432);
            } else {
                name.setTextColor(itemView.getContext().getResources().getColor(R.color.colorToolbar));
            }

            if ((int) detailed.prihod == 0) {
                column2.setVisibility(View.INVISIBLE);
            } else {
                column2.setVisibility(View.VISIBLE);
            }

            if ((int) detailed.rashod == 0) {
                column3.setVisibility(View.INVISIBLE);
            } else {
                column3.setVisibility(View.VISIBLE);
            }


            CharSequence prihodChar = (int) detailed.prihod < 0 ? Html.fromHtml("<font color=red>" + (int) detailed.prihod + "</font>") : "" + (int) detailed.prihod;
            CharSequence rashodChar = (int) detailed.rashod < 0 ? Html.fromHtml("<font color=red>" + (int) detailed.rashod + "</font>") : "" + (int) detailed.rashod;

            name.setText(detailed.docNom + " (" + Clock.getDatePremium(detailed.docDat) + ")");
            column1.setText("");
            column1.setVisibility(View.GONE);
            column5.setText("" + (int) detailed.sumPlan);
            column5.setTextColor(-10987432);
            column2.setText(prihodChar);
            column3.setText(rashodChar);
            column4.setText("");

            name.setOnClickListener(view -> {
                openDoc(wpDataDB, detailed.codeDad2, Clock.getDatePremiumDownloadFormat(detailed.docDat), true);
            });

            layout.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Завантажуються данні...", Toast.LENGTH_SHORT).show();
                getPremiumTextList(detailed, data -> {
                    DialogData dialogData = new DialogData(v.getContext());
                    dialogData.setTitle(data.getTitlle());

                    // Pika
                    // так было...
//                    dialogData.setText(data);

                    // так стало...
                    // теперь строка с основанием data прогоняется через PrepareLinkedTextForMVS и если там есть структура
                    // которую надр превратить в кликабельную ссылку для открытия в МВС, то это выполеяктся и возвращается строка в которой
                    // эта структура заменена на текст HTML
                    String newData=Globals.PrepareLinkedTextForMVS(data.getText());
                    dialogData.setTextScroll();
                    dialogData.setText(Html.fromHtml(newData));
                    dialogData.setClose(dialogData::dismiss);
                    dialogData.show();

                });
            });
        }

        private WpDataDB findDocument(long codeDad2) {
            if (codeDad2 != 0) {
                WpDataDB realmWp = WpDataRealm.getWpDataRowByDad2Id(codeDad2);
                //                    return RealmManager.INSTANCE.copyFromRealm(realmWp);
                return realmWp;
            }
            return null;
        }

        private void openDoc(WpDataDB wpDataDB, Long codeDad2, String datePremiumDownloadFormat, boolean b) {
            if (wpDataDB != null) {
                long otchetId;
                int action = wpDataDB.getAction();
                if (action == 1 || action == 94) {
                    otchetId = wpDataDB.getDoc_num_otchet_id();
                } else {
                    otchetId = wpDataDB.getDoc_num_1c_id();
                }

                String addrTxt;
                if (wpDataDB.getAddr_txt() != null && !wpDataDB.getAddr_txt().equals("")) {
                    addrTxt = wpDataDB.getAddr_txt();
                } else {
                    AddressSDB addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
                    if (addressSDB != null) {
                        addrTxt = addressSDB.nm;
                        wpDataDB.setAddr_location_xd(String.valueOf(addressSDB.locationXd));
                        wpDataDB.setAddr_location_yd(String.valueOf(addressSDB.locationYd));
                    } else {
                        addrTxt = "Адресс не определён";
                    }
                    wpDataDB.setAddr_txt(addrTxt);
                    WpDataRealm.setWpData(Collections.singletonList(wpDataDB));
                }

                String msg = String.format("Дата: %s\nАдрес: %s\nКлиент: %s\nИсполнитель: %s\n", Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000), addrTxt, wpDataDB.getClient_txt(), wpDataDB.getUser_txt());

                DialogData errorMsg = new DialogData(itemView.getContext());
                errorMsg.setTitle("");
                errorMsg.setText(itemView.getContext().getString(R.string.re_questioning_wpdata_err_msg));
                errorMsg.setClose(errorMsg::dismiss);

                DialogData dialog = new DialogData(itemView.getContext());
                dialog.setTitle("Открыть посещение " + wpDataDB.getDoc_num_otchet() + " ?");
                dialog.setText(msg);
                dialog.setOk(null, () -> {
                    if (wpDataDB.getTheme_id() == 1182) {
                        DialogData dialogQuestionOne = new DialogData(itemView.getContext());
                        dialogQuestionOne.setTitle("");
                        dialogQuestionOne.setText(itemView.getContext().getString(R.string.re_questioning_wpdata_first_msg));
                        dialogQuestionOne.setOk("Да", errorMsg::show);
                        dialogQuestionOne.setCancel("Нет", () -> {
                            DialogData dialogQuestionOTwo = new DialogData(itemView.getContext());
                            dialogQuestionOne.dismiss();
                            dialogQuestionOTwo.setTitle("");
                            dialogQuestionOTwo.setText(itemView.getContext().getString(R.string.re_questioning_wpdata_second_msg));
                            dialogQuestionOTwo.setOk("Да", errorMsg::show);
                            dialogQuestionOTwo.setCancel("Нет", () -> {
                                openReportPrepare(wpDataDB, otchetId);
                            });
                            dialogQuestionOTwo.show();
                        });
                        dialogQuestionOne.show();
                    } else {
                        openReportPrepare(wpDataDB, otchetId);
                    }
                });
                dialog.show();
            } else {

                if (b){
                    downloadDoc(codeDad2, datePremiumDownloadFormat);
                }else {
                    DialogData dialog = new DialogData(itemView.getContext());
                    dialog.setTitle("Звіт на поточному приладі не знайдено.");
                    dialog.setText("Звіти на приладі зберігаються до тижня. Якщо вам все ж таки треба з'ясувати питання по цьому звіту - зверніться до свого керівника.");
                    dialog.setClose(dialog::dismiss);
                    dialog.show();
                }



            }
        }

        /**
         * 21.07.23.
         * Тут я буду загружать не достающие данные с сервера, если есть такая возможность и
         * отображать пользователю загруженный документ.
         */
        public void downloadDoc(Long codeDad2, String datePremiumDownloadFormat) {

            wpDownload(codeDad2, datePremiumDownloadFormat, new Clicks.clickVoid() {
                @Override
                public void click() {
                    optionDownload(codeDad2, datePremiumDownloadFormat, new Clicks.clickVoid() {
                        @Override
                        public void click() {
                            reportDownload(codeDad2, datePremiumDownloadFormat, new Clicks.clickVoid() {
                                @Override
                                public void click() {
                                    openDoc(findDocument(codeDad2), codeDad2, datePremiumDownloadFormat, false);
                                }
                            });
                        }
                    });
                }
            });
        }

        private void wpDownload(Long codeDad2, String datePremiumDownloadFormat, Clicks.clickVoid click){
            // План робіт
            BlockingProgressDialog progressDialogWpData = BlockingProgressDialog.show(itemView.getContext(),
                    "Завантаження Плану робіт",
                    dataDownloadWait);
            WPDataExchange wpDataExchange = new WPDataExchange(datePremiumDownloadFormat, datePremiumDownloadFormat, "");
            wpDataExchange.downloadWPData(new ExchangeInterface.ExchangeResponseInterface() {
                @Override
                public <T> void onSuccess(List<T> data) {
                    if (data != null && data.size() > 0) {
                        WpDataRealm.setWpData((List<WpDataDB>) data);
                        Toast.makeText(itemView.getContext(), dataDownload200 + " План робіт", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(itemView.getContext(), dataDownloadError + " План робіт", Toast.LENGTH_LONG).show();
                    }
                    progressDialogWpData.dismiss();
                    click.click();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(itemView.getContext(), dataDownloadError + " План робіт " + error, Toast.LENGTH_LONG).show();
                    progressDialogWpData.dismiss();
                    click.click();
                }
            });
        }

        private void optionDownload(Long codeDad2, String datePremiumDownloadFormat, Clicks.clickVoid click){
            // Опції
            BlockingProgressDialog progressDialogOption = BlockingProgressDialog.show(itemView.getContext(),
                    "Завантаження Опцій",
                    dataDownloadWait);
            OptionsExchange optionsExchange = new OptionsExchange(datePremiumDownloadFormat, datePremiumDownloadFormat, "");
            optionsExchange.downloadOptions(new ExchangeInterface.ExchangeResponseInterface() {
                @Override
                public <T> void onSuccess(List<T> data) {
                    if (data != null && data.size() > 0) {
                        RealmManager.saveDownloadedOptions((List<OptionsDB>) data);
                        Toast.makeText(itemView.getContext(), dataDownload200 + " Опції", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(itemView.getContext(), dataDownloadError + " Опції", Toast.LENGTH_LONG).show();
                    }
                    progressDialogOption.dismiss();
                    click.click();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(itemView.getContext(), dataDownloadError + " Опції " + error, Toast.LENGTH_LONG).show();
                    progressDialogOption.dismiss();
                    click.click();
                }
            });
        }

        private void reportDownload(Long codeDad2, String datePremiumDownloadFormat, Clicks.clickVoid click){
            // Дет. отчёт
            BlockingProgressDialog progressDialogReportPrepare = BlockingProgressDialog.show(itemView.getContext(),
                    "Завантаження Деталізованого звіту",
                    dataDownloadWait);
            ReportPrepareExchange reportPrepareExchange = new ReportPrepareExchange(datePremiumDownloadFormat, datePremiumDownloadFormat, "");
            reportPrepareExchange.downloadReportPrepare(new ExchangeInterface.ExchangeResponseInterface() {
                @Override
                public <T> void onSuccess(List<T> data) {
                    if (data != null && data.size() > 0) {
                        ReportPrepareRealm.setAll((List<ReportPrepareDB>) data);
                        Toast.makeText(itemView.getContext(), dataDownload200 + " деталізований звіт", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(itemView.getContext(), dataDownloadError + " деталізований звіт", Toast.LENGTH_LONG).show();
                    }
                    progressDialogReportPrepare.dismiss();
                    click.click();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(itemView.getContext(), dataDownloadError + " деталізований звіт " + error, Toast.LENGTH_LONG).show();
                    progressDialogReportPrepare.dismiss();
                    click.click();
                }
            });
        }



        private void openReportPrepare(WpDataDB wp, long otchetId) {
            try {
                Intent intent = new Intent(itemView.getContext(), DetailedReportActivity.class);
                intent.putExtra("WpDataDB_ID", wp.getId());
                itemView.getContext().startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(itemView.getContext(), "Помилка: " + e, Toast.LENGTH_SHORT).show();
            }
        }


        private void getPremiumText(Detailed detailed, Clicks.clickText clickText) {
            StandartData data = new StandartData();
            data.mod = "premium";
            data.act = "get_salary_basis";
            data.smeta = detailed.docNom;
            data.doc_type_id = detailed.docDef;

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            retrofit2.Call<PremiumResponse> call = RetrofitBuilder.getRetrofitInterface().Premium_get_salary_basis_RESPONSE(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<PremiumResponse>() {
                @Override
                public void onResponse(Call<PremiumResponse> call, Response<PremiumResponse> response) {

                    if (response.isSuccessful()) {
                        if (response.body() != null && response.body().state) {
                            clickText.click(response.body().basis);
                        } else {
                            clickText.click("Дані отримати не вийшло. Повторіть спробу або зверніться до вашого керівника.");
                        }
                    } else {
                        clickText.click("Проблема із зв'язком. Спробуйте пізніше.");
                    }
                }

                @Override
                public void onFailure(Call<PremiumResponse> call, Throwable t) {
                    Globals.writeToMLOG("ERROR", "getPremiumText", "Throwable t: " + t);
                    clickText.click("Отримати дані не вийшло. Зверніться до керівника за допомогою.");
                }
            });
        }

        private void getPremiumTextList(Detailed detailed, Clicks.clickObject<MessageData> clickText) {
            StandartData data = new StandartData();
            data.mod = "premium";
            data.act = "get_salary_basis";
            data.smeta = detailed.docNom;
            data.doc_type_id = detailed.docDef;

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

//            retrofit2.Call<JsonObject> test = RetrofitBuilder.getRetrofitInterface().TEST_JSON_UPLOAD(RetrofitBuilder.contentType, convertedObject);
//            test.enqueue(new Callback<JsonObject>() {
//                @Override
//                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                    Log.e("test", "response: " + response);
//                }
//
//                @Override
//                public void onFailure(Call<JsonObject> call, Throwable t) {
//
//                }
//            });

// {"act":"get_salary_basis","doc_type_id":5301,"mod":"premium","smeta":"АНач-142540"}

            retrofit2.Call<PremiumResponse> call = RetrofitBuilder.getRetrofitInterface().Premium_get_salary_basis_RESPONSE(RetrofitBuilder.contentType, convertedObject);
            call.enqueue(new Callback<PremiumResponse>() {
                @Override
                public void onResponse(Call<PremiumResponse> call, Response<PremiumResponse> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null && response.body().state) {

                            // Pika
                            // так было...
//                            clickText.click(response.body().basis);

                            // так стало...
                            // (получаю текст основания премии для вывода в TextView из поля result_list, которое в салю очередь представлено массивом
                            // JSON в котором один элемент и свои поля и нас интересует поле osnovanie)
                            String res = "";
                            String title = "";
                            for (PremiumResponse.Result item : response.body().result_list) {
                                if (title.isEmpty()) {
                                    title += item.docNom + " от " + item.docDat;
                                }
                                res += item.sumNZPSotrDoc + " грн. " + item.osnovanie.replaceAll("<", "(").replaceAll(">", ")") + "<br><br>";
                            }
//                            String res=response.body().result_list.get(0).osnovanie;

                            clickText.click(new MessageData(title, res));
                        } else {
                            clickText.click(new MessageData("Дані отримати не вийшло. Повторіть спробу або зверніться до вашого керівника."));
                        }
                    } else {
                        clickText.click(new MessageData("Проблема із зв'язком. Спробуйте пізніше."));
                    }
                }

                @Override
                public void onFailure(Call<PremiumResponse> call, Throwable t) {
                    Globals.writeToMLOG("ERROR", "getPremiumText", "Throwable t: " + t);
                    clickText.click(new MessageData("Отримати дані не вийшло. Зверніться до керівника за допомогою."));
                }
            });
        }

    }

    /*Обработчик кликов по заголовку ПУНКТУ премии*/
    public interface PremiumListener {
        void onClick(View view, Detailed item);
    }
}

class MessageData {

    public MessageData(String titlle, String text) {
        this.titlle = titlle;
        this.text = text;
    }

    public MessageData(String text) {
        this.text = text;
    }

    private String titlle;
    private String text;

    public String getTitlle() {
        return titlle;
    }

    public void setTitlle(String titlle) {
        this.titlle = titlle;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}