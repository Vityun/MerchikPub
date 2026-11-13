package ua.com.merchik.merchik.Activities.PremiumActivity.PremiumTable;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.app.Activity;
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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
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
import ua.com.merchik.merchik.Utils.text.MvsLinkedTextFormatter;
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
import ua.com.merchik.merchik.database.realm.tables.WpWorkTimes;
import ua.com.merchik.merchik.dialogs.BlockingProgressDialog;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.features.LoadingDialogWithPercent;
import ua.com.merchik.merchik.dialogs.features.dialogLoading.ProgressViewModel;
import ua.com.merchik.merchik.retrofit.RetrofitBuilder;

// Pika
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import static ua.com.merchik.merchik.Globals.userId;

public class PremiumTableDataAdapter extends RecyclerView.Adapter<PremiumTableDataAdapter.PremiumTableHeaderViewHolder> {

    private List<Detailed> data;
    private PremiumTableHeaderAdapter.PremiumListener listener;
    private PremiumBasisLoading basisLoading;

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

    private final class PremiumBasisLoading implements LifecycleEventObserver {
        private final Activity activity;
        private final Lifecycle lifecycle;
        private final Call<PremiumResponse> call;
        private final ProgressViewModel progress = new ProgressViewModel(1);
        private final LoadingDialogWithPercent dialog;
        private boolean finished;

        PremiumBasisLoading(Activity activity, Call<PremiumResponse> call) {
            this.activity = activity;
            this.lifecycle = ((LifecycleOwner) activity).getLifecycle();
            this.call = call;
            dialog = new LoadingDialogWithPercent(activity, progress);
            dialog.setOnDismissListener(this::cancel);
        }

        void show(String document) {
            lifecycle.addObserver(this);
            progress.reset("Завантаження підстави: " + document);
            dialog.show();
            progress.onNextEvent("Завантаження підстави: " + document, 10000L);
        }

        boolean isActive() {
            return !finished && !call.isCanceled() && !activity.isFinishing() && !activity.isDestroyed();
        }

        boolean finish() {
            boolean deliverResult = isActive();
            if (finished) return false;
            finished = true;
            lifecycle.removeObserver(this);
            progress.reset("");
            dialog.dismiss();
            if (basisLoading == this) basisLoading = null;
            return deliverResult;
        }

        void cancel() {
            call.cancel();
            finish();
        }

        @Override
        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
            if (event == Lifecycle.Event.ON_DESTROY) cancel();
        }
    }

    public class PremiumTableHeaderViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layout;
        private TextView name;
        private TextView column1, column5, column2, column3, column4;
        private String dataDownloadError, dataDownloadWait;

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
                Globals.writeToMLOG("INFO", "PremiumVisit/open", premiumContext(detailed));
                if (detailed.codeDad2 == null || detailed.codeDad2 <= 0) {
                    Globals.writeToMLOG("WARNING", "PremiumVisit/noVisit", premiumContext(detailed));
                    Toast.makeText(view.getContext(), "Документ не містить посилання на відвідування. Показуємо підставу нарахування.", Toast.LENGTH_LONG).show();
                    showPremiumBasis(detailed);
                    return;
                }
                openDoc(findDocument(detailed.codeDad2), detailed.codeDad2,
                        Clock.getDatePremiumDownloadFormat(detailed.docDat), true);
            });

            layout.setOnClickListener(v -> showPremiumBasis(detailed));
        }

        private void showPremiumBasis(Detailed detailed) {
            getPremiumTextList(detailed, data -> {
                DialogData dialogData = new DialogData(itemView.getContext());
                dialogData.setTitle(data.getTitlle());

                String newData = Globals.PrepareLinkedTextForMVS(data.getText(), premiumContext(detailed)
                        + ", responseTitle=" + MvsLinkedTextFormatter.logValue(data.getTitlle()));
                dialogData.setTextScroll();
                dialogData.setText(Html.fromHtml(newData));
                dialogData.setClose(dialogData::dismiss);
                dialogData.show();
            });
        }

        private WpDataDB findDocument(Long codeDad2) {
            if (codeDad2 != null && codeDad2 > 0) {
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
                    WpWorkTimes.updateFields(wpDataDB, false, current -> {
                        current.setAddr_txt(wpDataDB.getAddr_txt());
                        current.setAddr_location_xd(wpDataDB.getAddr_location_xd());
                        current.setAddr_location_yd(wpDataDB.getAddr_location_yd());
                    });
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
            if (codeDad2 == null || codeDad2 <= 0) {
                visitDownloadFailed("target", codeDad2, "Документ не містить посилання на відвідування.");
                return;
            }

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
            Globals.writeToMLOG("INFO", "PremiumVisit/plan", "dad2=" + codeDad2 + ", date=" + datePremiumDownloadFormat);
            WPDataExchange wpDataExchange = new WPDataExchange(datePremiumDownloadFormat, datePremiumDownloadFormat, String.valueOf(codeDad2));
            wpDataExchange.downloadWPData(new ExchangeInterface.ExchangeResponseInterface() {
                @Override
                public <T> void onSuccess(List<T> data) {
                    boolean found = false;
                    try {
                        List<WpDataDB> visits = new ArrayList<>();
                        if (data != null) {
                            for (WpDataDB visit : (List<WpDataDB>) data) {
                                if (visit != null && codeDad2.equals(visit.getCode_dad2())) visits.add(visit);
                            }
                        }
                        if (!visits.isEmpty()) {
                            RealmManager.updateWorkPlanFromServer(visits);
                            found = findDocument(codeDad2) != null;
                        }
                    } catch (RuntimeException error) {
                        visitDownloadFailed("plan/save", codeDad2, error.toString());
                        return;
                    } finally {
                        progressDialogWpData.dismiss();
                    }
                    if (!found) {
                        visitDownloadFailed("plan", codeDad2, "Відвідування не знайдено у відповіді сервера або воно більше не доступне.");
                        return;
                    }
                    click.click();
                }

                @Override
                public void onFailure(String error) {
                    progressDialogWpData.dismiss();
                    visitDownloadFailed("plan", codeDad2, error);
                }
            });
        }

        private void optionDownload(Long codeDad2, String datePremiumDownloadFormat, Clicks.clickVoid click){
            // Опції
            BlockingProgressDialog progressDialogOption = BlockingProgressDialog.show(itemView.getContext(),
                    "Завантаження Опцій",
                    dataDownloadWait);
            Globals.writeToMLOG("INFO", "PremiumVisit/options", "dad2=" + codeDad2);
            OptionsExchange optionsExchange = new OptionsExchange(datePremiumDownloadFormat, datePremiumDownloadFormat, String.valueOf(codeDad2));
            optionsExchange.downloadOptions(new ExchangeInterface.ExchangeResponseInterface() {
                @Override
                public <T> void onSuccess(List<T> data) {
                    try {
                        List<OptionsDB> options = new ArrayList<>();
                        if (data != null) {
                            for (OptionsDB option : (List<OptionsDB>) data) {
                                if (option != null && String.valueOf(codeDad2).equals(option.getCodeDad2())) options.add(option);
                            }
                        }
                        if (options.isEmpty()) {
                            visitDownloadFailed("options", codeDad2, "Сервер не повернув опції відвідування.");
                            return;
                        }
                        RealmManager.saveDownloadedOptions(options);
                    } catch (RuntimeException error) {
                        visitDownloadFailed("options/save", codeDad2, error.toString());
                        return;
                    } finally {
                        progressDialogOption.dismiss();
                    }
                    click.click();
                }

                @Override
                public void onFailure(String error) {
                    progressDialogOption.dismiss();
                    visitDownloadFailed("options", codeDad2, error);
                }
            });
        }

        private void reportDownload(Long codeDad2, String datePremiumDownloadFormat, Clicks.clickVoid click){
            // Дет. отчёт
            BlockingProgressDialog progressDialogReportPrepare = BlockingProgressDialog.show(itemView.getContext(),
                    "Завантаження Деталізованого звіту",
                    dataDownloadWait);
            Globals.writeToMLOG("INFO", "PremiumVisit/report", "dad2=" + codeDad2);
            ReportPrepareExchange reportPrepareExchange = new ReportPrepareExchange(datePremiumDownloadFormat, datePremiumDownloadFormat, String.valueOf(codeDad2));
            reportPrepareExchange.downloadReportPrepare(new ExchangeInterface.ExchangeResponseInterface() {
                @Override
                public <T> void onSuccess(List<T> data) {
                    progressDialogReportPrepare.dismiss();
                    click.click();
                }

                @Override
                public void onFailure(String error) {
                    progressDialogReportPrepare.dismiss();
                    visitDownloadFailed("report", codeDad2, error);
                }
            });
        }

        private void visitDownloadFailed(String stage, Long dad2, String error) {
            Globals.writeToMLOG("WARNING", "PremiumVisit/" + stage,
                    "dad2=" + dad2 + ", error=" + MvsLinkedTextFormatter.logValue(error));
            Toast.makeText(itemView.getContext(), dataDownloadError + " " + error, Toast.LENGTH_LONG).show();
        }


        private void openReportPrepare(WpDataDB wp, long otchetId) {
            try {
                Intent intent = new Intent(itemView.getContext(), DetailedReportActivity.class);
                intent.putExtra("WpDataDB_ID", wp.getId());
                intent.putExtra("WpDataDB_DAD2", wp.getCode_dad2());
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
            if (basisLoading != null) return;
            Activity activity = Globals.unwrap(itemView.getContext());
            if (!(activity instanceof LifecycleOwner) || activity.isFinishing() || activity.isDestroyed()) return;

            String context = premiumContext(detailed);
            StandartData data = new StandartData();
            data.mod = "premium";
            data.act = "get_salary_basis";
            data.smeta = detailed.docNom;
            data.doc_type_id = detailed.docDef;

            Gson gson = new Gson();
            String json = gson.toJson(data);
            JsonObject convertedObject = new Gson().fromJson(json, JsonObject.class);

            Globals.writeToMLOG("INFO", "PremiumBasis/request", context);
            retrofit2.Call<PremiumResponse> call = RetrofitBuilder.getRetrofitInterface().Premium_get_salary_basis_RESPONSE(RetrofitBuilder.contentType, convertedObject);
            PremiumBasisLoading loading = new PremiumBasisLoading(activity, call);
            basisLoading = loading;
            try {
                loading.show(detailed.docNom);
                call.enqueue(new Callback<PremiumResponse>() {
                    @Override
                    public void onResponse(Call<PremiumResponse> call, Response<PremiumResponse> response) {
                        if (!loading.isActive()) {
                            loading.finish();
                            return;
                        }
                        MessageData message;
                        try {
                            PremiumResponse body = response.body();
                            Globals.writeToMLOG("INFO", "PremiumBasis/response", context
                                    + ", http=" + response.code() + ", state=" + (body == null ? null : body.state)
                                    + ", rows=" + (body == null || body.result_list == null ? "null" : body.result_list.size()));
                            if (response.isSuccessful()) {
                                PremiumBasisFormatter.Result formatted = PremiumBasisFormatter.format(body, detailed.docNom,
                                        issue -> Globals.writeToMLOG("WARNING", "PremiumBasis/invalidResponse", context + ", " + issue));
                                message = new MessageData(formatted.title, formatted.text);
                            } else {
                                Globals.writeToMLOG("WARNING", "PremiumBasis/httpError", context + ", http=" + response.code());
                                message = new MessageData(detailed.docNom, "Проблема із зв'язком. Спробуйте пізніше.");
                            }
                        } catch (RuntimeException error) {
                            Globals.writeToMLOG("ERROR", "PremiumBasis/formatFailure", context + ", error=" + error.getClass().getSimpleName());
                            message = new MessageData(detailed.docNom, "Отримати дані не вийшло. Зверніться до керівника за допомогою.");
                        }
                        if (loading.finish()) clickText.click(message);
                    }

                    @Override
                    public void onFailure(Call<PremiumResponse> call, Throwable t) {
                        if (!loading.finish()) return;
                        Globals.writeToMLOG("ERROR", "PremiumBasis/failure", context + ", error=" + t.getClass().getSimpleName());
                        clickText.click(new MessageData(detailed.docNom, "Отримати дані не вийшло. Зверніться до керівника за допомогою."));
                    }
                });
            } catch (RuntimeException error) {
                boolean deliverResult = loading.finish();
                call.cancel();
                Globals.writeToMLOG("ERROR", "PremiumBasis/startFailure", context + ", error=" + error.getClass().getSimpleName());
                if (deliverResult) clickText.click(new MessageData(detailed.docNom, "Отримати дані не вийшло. Зверніться до керівника за допомогою."));
            }
        }

        private String premiumContext(Detailed detailed) {
            return "mod=premium, act=get_salary_basis, document=" + MvsLinkedTextFormatter.logValue(detailed.docNom)
                    + ", doc_type_id=" + detailed.docDef + ", dad2=" + detailed.codeDad2;
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
