package ua.com.merchik.merchik.features.main.options;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.NEED_UPDATE_UI_REQUEST;
import static ua.com.merchik.merchik.Options.Controls.OptionControlQuestionAnswer.THEME_IDEA;
import static ua.com.merchik.merchik.Options.Controls.OptionControlQuestionAnswer.THEME_MANAGER_WRONG;
import static ua.com.merchik.merchik.Options.Controls.OptionControlQuestionAnswer.THEME_NO_COMPLAINTS;
import static ua.com.merchik.merchik.Options.Controls.OptionControlQuestionAnswer.THEME_OTHER;
import static ua.com.merchik.merchik.Options.Controls.OptionControlQuestionAnswer.THEME_PAYMENT_INCREASE;
import static ua.com.merchik.merchik.Options.Options.NNKMode.CHECK_CLICK;
import static ua.com.merchik.merchik.Options.Options.NNKMode.NULL;
import static ua.com.merchik.merchik.data.OptionMassageType.Type.DIALOG;
import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.DEFAULT;
import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.HIDE_FOR_USER;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;

import io.realm.RealmResults;
import kotlin.Pair;
import kotlin.Unit;
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Global.OptionUnlockPolicy;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonAddNewClient;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPauseWork;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonUserOpinion;
import ua.com.merchik.merchik.Options.Controls.OptionControlAddOpinion;
import ua.com.merchik.merchik.Options.Controls.OptionControlAvailabilityControlPhotoRemainingGoods;
import ua.com.merchik.merchik.Options.Controls.OptionControlPhotoExpirationDate;
import ua.com.merchik.merchik.Options.Controls.OptionControlPhotoTovarAndPrice;
import ua.com.merchik.merchik.Options.Controls.OptionControlPhotoShowcase;
import ua.com.merchik.merchik.Options.Controls.OptionControlPlanorammVizit;
import ua.com.merchik.merchik.Options.Controls.OptionControlReclamationAnswer;
import ua.com.merchik.merchik.Options.Controls.OptionControlStockBalanceTovar;
import ua.com.merchik.merchik.Options.Controls.OptionControlTaskAnswer;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.Utils.CodeGenerator;
import ua.com.merchik.merchik.Utils.CustomString;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.BonusSDB;
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.QuestionAnswerDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.dataLayer.MainRepositoryKt;
import ua.com.merchik.merchik.dataLayer.ModeUI;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ImagesTypeListRealm;
import ua.com.merchik.merchik.database.realm.tables.LogMPRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.database.room.RoomManager;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;
import ua.com.merchik.merchik.features.main.DBViewModels.LogMPDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.PlanogrammVizitShowcaseViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.QuestionAnswerSDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.SamplePhotoSDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.ShowcaseDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.StackPhotoDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.TovarDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.WpDataPauseSDBViewModel;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportButtons;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.PauseWorkStateHolder;

/**
 * Legacy option calculations/actions, detached from RecyclerView rendering.
 * Keep branches aligned with RecycleViewDRAdapter while the legacy comparison path exists.
 * Created and used on the main thread: controls still use the legacy Realm API.
 */
public final class OptionsRowFactory<T> {
    private final Context mContext;
    private final T dataDB;
    private final List<OptionsDB> butt;
    private final List<OptionsDB> allReportOption;
    private final Clicks.click click;
    private final Runnable refresh;
    private final BooleanSupplier hostAttached;
    private final long dad2;
    private final long startDt;
    private final int DAYS = 4;
    private boolean attached = true;

    public OptionsRowFactory(Context context, T dataDB, List<OptionsDB> buttons,
            List<OptionsDB> allOptions, Clicks.click gallery, BooleanSupplier hostAttached, Runnable refresh) {
        this.mContext = context;
        this.dataDB = dataDB;
        this.butt = buttons;
        this.allReportOption = allOptions;
        this.click = gallery;
        this.refresh = refresh;
        this.hostAttached = hostAttached;
        WpDataDB wp = (WpDataDB) dataDB;
        dad2 = wp.getCode_dad2();
        startDt = wp.getVisit_start_dt();
    }

    public void detach() { attached = false; }

    private boolean isAttached() {
        return attached && hostAttached.getAsBoolean() && (!(mContext instanceof Activity)
                || (!((Activity) mContext).isFinishing() && !((Activity) mContext).isDestroyed()));
    }

    private void setCheck(OptionsDB option, Options.NNKMode mode) {
        if (!isAttached()) return;
        new Options().optionControl(mContext, dataDB, option, null, mode,
                new OptionControl.UnlockCodeResultListener() {
                    @Override public void onUnlockCodeSuccess() { if (isAttached()) refresh.run(); }
                    @Override public void onUnlockCodeFailure() { if (isAttached()) refresh.run(); }
                });
        refresh.run();
    }

    public OptionItemState create(OptionsDB optionsButtons, SiteObjectsSDB siteObjectsSDB) {
        OptionItemState row = new OptionItemState(String.valueOf(optionsButtons.getID()),
                optionsButtons.getOptionId(), optionsButtons.getOptionControlId());
        OptionItemState.TextPart textTitle = row.title;
        OptionItemState.TextPart textInteger = row.counter;
        OptionItemState.TextPart textInteger2 = row.secondaryCounter;
        OptionItemState.Signal setCheck = row.signal;
        try {

            boolean describedOption = true;

            textInteger.visibility = View.VISIBLE;

            String buttText = optionsButtons.getOptionTxt();
            buttText = buttText.replace("&quot;", "\"");
            buttText = buttText.replace("Кнопка ", "");

            if (siteObjectsSDB != null && !optionsButtons.getOptionTxt().contains("Планограммы")) {
                buttText = siteObjectsSDB.commentsTranslation;
            }

            buttText = buttText == null ? "" : buttText.replace("&quot;", "").replace("\"", "");

            if (optionsButtons.getIsSignal().equals("1") && optionsButtons.getBlockPns().equals("1")) {
                textTitle.text = "" + Html.fromHtml("<font color='#FF0000'>" + buttText + "</font>");
            } else {
                textTitle.text = "" + buttText;
            }

            int optionId = Integer.parseInt(optionsButtons.getOptionId());

            if (optionId == 132968 || optionId == 158309 || optionId == 158308) {
                textTitle.bold = true;
            } else {
                textTitle.bold = false;
            }

            if (optionId == 135809 // Фото витрины ДО начала работ
                    || optionId == 132968 // Фото витрины
                    || optionId == 135158 // Фото Остатков Товаров (ФОТ)
                    || optionId == 132969 // Фото Тележка с Товаром (ФТТ)
                    || optionId == 138518 // Начало работы
                    || optionId == 138520 // Окончание работы
                    || optionId == 138773 // Местоположение
                    || optionId == 137797 // ДеталОтчёт план по товарам
                    || optionId == 138339 // Доп. Требования
                    || optionId == 141360 // Фото товара на складе
                    || optionId == 141910 // Получение заказа в ТТ
                    || optionId == 141888 // Выкуп Товара с ТТ
                    || optionId == 141885 // Фото Документов
                    || optionId == 84007 // ЭКЛ
                    || optionId == 132666 // Стандарт
                    || optionId == 139576 // Версия ПО
                    || optionId == 138767 // Планограмма
                    || optionId == 135742 // "Дет.Отчет" (по Клиенто-Адресу)
                    || optionId == 132621 // Оценка
                    || optionId == 84003 // Мнение о сотруднике
                    || optionId == 138340 // Доп. Материалы
                    || optionId == 135327 // Задача
                    || optionId == 135328 // Рекламация
                    || optionId == 156882 // Акции
                    || optionId == 151139 // Фото планограммы
                    || optionId == 132623 // Комментарий
                    || optionId == 133382 // Потенциальный клиент
                    || optionId == 136100 // Пригласи друга
                    || optionId == 157275 // 1.
                    || optionId == 157276 // 2. Две опции контроля тут на всяк случай. Тестим.
                    || optionId == 157274 // 3. ..три
                    || optionId == 135159 // Достижения
                    || optionId == 157277 // Фото Акционного Товара
                    || optionId == 157353 // Дет отчёт исправление
                    || optionId == 138643 // Подъём товара со склада
                    || optionId == 158243 // Стикеровка
                    || optionId == 135412 // Процент премиальных
                    || optionId == 151748 // ДОЛЯ полочного пространства
                    || optionId == 158309 // "Фото Витрины" (Наближене)
                    || optionId == 158308 // "Фото Витрины" (Панорамне)
                    || optionId == 158604 // ФВ (Наполненность)
                    || optionId == 158605 // ФВ (Корпор. блок)
                    || optionId == 158606 // Дополнительное место продаж
                    || optionId == 157354 // Фото ДМП.
                    || optionId == 157242 // Причина отсутствия товара
                    || optionId == 159726 // Фото торговой точки
                    || optionId == 159706 // Инвентаризация
                    || optionId == 159725 // Кнопка "Фото Торговой Точки (ФТТ)"
                    || optionId == 159799 // Возврат
                    || optionId == 135413 // "Фото Витрины (Оценка)"
                    || optionId == 135719 // "Дет.Отчет" (оценка)
                    || optionId == 143969 // "СМС-код Клиенту" (электронный контрольный лист ЭКЛ)
                    || optionId == 160567 // Витрины
                    || optionId == 164351 // Контроль наявності світлини прикасової зони
                    || optionId == 164355 // "Фото Планограммы ТТ"
                    || optionId == 132812 // Хочу увеличение оплаты
                    || optionId == 165481 // Кнопка ЭФФИ
                    || optionId == 141069 // Кнопка "Сравнение Остатков с Наличием"
                    || optionId == 168598 // Кнопка "Мнение о посещении"
                    || optionId == 169108 // фото POS материалов (46)
                    || optionId == 2243
                    || optionId == 172100 // Фото вітрини з акційними цінниками
                    || optionId == 151122 // Жилетка: жалобы на условия работ
                    || optionId == 174213
                    || optionId == 174546 // кнопка пауза
                    || optionId == 174878 // фото товаров СИСГ (с истекающим/истекшим сроком годности)
                    || optionId == 139337 // для теста кнопка Индульгенция
                    || optionId == 175014
                    || optionId == 175015 //  фото единиці товара+ценик
            ) {
                row.backgroundRes = R.drawable.bg_temp;
                textInteger2.visibility = View.VISIBLE;
                if (optionsButtons.getIsSignal().equals("1") && !optionsButtons.getBlockPns().equals("1")) {
                    if (optionId == 132666 || optionId == 132812)
                        textInteger2.visibility = View.INVISIBLE;
                    else {
                        textInteger2.monetary = true;
                        textInteger2.text = counter2Text(optionId);
                    }
                } else {
                    if (optionId == 133382) {
                        textInteger2.visibility = View.VISIBLE;
                        int salary = Globals.getAverageSalary();
                        if (salary == 0)
                            salary = 15700;

                        SpannableString text = CustomString.underlineString("+" + salary / 10 + ".0 грн.", optionsButtons);
                        text.setSpan(new UnderlineSpan(), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger2.monetary = true;
                        textInteger2.text = text;
                        textInteger2.onClick = v -> {
                            DialogData dialog = new DialogData(mContext);
                            dialog.setTitle("Добавление потенциального клиента");
                            dialog.setText("Расчет за потенциального клиента \n\n" + OptionButtonAddNewClient.additionalText());
                            dialog.setClose(dialog::dismiss);
                            dialog.show();
                        };
                    } else if (optionId == 84007 && optionsButtons.getIsSignal().equals("1")) {
                        textInteger2.monetary = true;
                        textInteger2.text = counter2EKLText();
                    } else {
                        textInteger2.visibility = View.GONE;
                    }
                }

                if (optionId == 136100) {
                    textInteger2.visibility = View.VISIBLE;
                    List<BonusSDB> bonusList = SQL_DB.bonusDao().getData(null, null, (long) optionId);
                    Pair<String, Float> bonus = MainRepositoryKt.getBonusText(bonusList);

                    SpannableString text = CustomString.underlineString("+" + bonus.getSecond() + " грн.", optionsButtons);
                    text.setSpan(new UnderlineSpan(), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textInteger2.monetary = true;
                    textInteger2.text = text;
                    textInteger2.onClick = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DialogData dialog = new DialogData(mContext);
                            dialog.setTitle("Пригласить Друга");

                            StringBuilder text = new StringBuilder();
                            text.append("Пригласить Друга на работу в нашей компании\n" +
                                    "За sms-приглашение, которое вы отправите кандидату, автоматически начисляются следующие премии:\n");

                            text.append(bonus.getFirst());

                            text.append("\n\n");

                            text.append("Общая сумма премии (при выполнении указанных условий) составит ")
                                    .append(bonus.getSecond()).append(" грн.\n")
                                    .append("Обмеження по кількості відправлених запрошень:\n")
                                    .append("- до 3-х \"СМС-запрошень\" на день\n")
                                    .append("- до 10-ти \"СМС-запрошень\" на тиждень\n")
                                    .append("- до 20-ї \"СМС-запрошень\" на місяць\n")
                                    .append("- якщо ваші \"СМС-запрошення\" не призводять до початку співпраці протягом місяця, преміальні за НОВІ \"СМС-запрошення\" знижуються в 10-ть разів");

                            dialog.setText(Html.fromHtml(text.toString().replaceAll("strong", "b").replaceAll("\n", "<br>")));
                            dialog.show();
                        }
                    };
                } else {

                }

            } else {
                describedOption = false;
                textInteger2.visibility = View.GONE;
                row.backgroundRes = R.drawable.button_bg_inactive;
            }

            setCheck.tint = mContext.getResources().getColor(R.color.shadow);
            if (describedOption) {
                setCheck.visibility = View.VISIBLE;
                if (optionsButtons.getIsSignal().equals("1")) {
                    setCheck.iconRes = R.drawable.ic_exclamation_mark_in_a_circle;
                    setCheck.tint = mContext.getResources().getColor(R.color.red_error);
                } else if (optionsButtons.getIsSignal().equals("2")) {
                    setCheck.iconRes = R.drawable.ic_check;
                    setCheck.tint = mContext.getResources().getColor(R.color.green_default);
                } else if (OptionUnlockPolicy.SIGNAL_UNLOCKED.equals(optionsButtons.getIsSignal())) {
                    setCheck.iconRes = R.drawable.ic_exclamation_mark_in_a_circle;
                    setCheck.tint = mContext.getResources().getColor(R.color.colorInetYellow);
                } else {
                    if (optionsButtons.getOptionControlId().equals("0")) {

                        setCheck.iconRes = R.drawable.ic_round;
                        setCheck.tint = mContext.getResources().getColor(R.color.shadow);
                    } else {
                        setCheck.iconRes = R.drawable.ic_round;
                        setCheck.tint = mContext.getResources().getColor(R.color.shadow);
                    }
                }
            } else {
                setCheck.visibility = View.INVISIBLE;
                setCheck.iconRes = R.drawable.ic_round;
                setCheck.tint = mContext.getResources().getColor(R.color.colorUnselectedTab);
            }
            if (optionId == OptionButtonPauseWork.OPTION_BUTTON_PAUSE_WORK_ID && describedOption) {
                applyPauseWorkSignalIcon(setCheck);
            }

            WpDataDB wp = (WpDataDB) dataDB;

            try {

                switch (optionId) {

                    case 138773:
                        List<LogMPDB> logMPList = new ArrayList<>();
                        int validTime = 1800;
                        long startT =
                                (wp != null &&
                                        wp.getVisit_start_dt() > 0 && wp.getVisit_end_dt() > 0) ? wp.getVisit_start_dt() - validTime :
                                        (System.currentTimeMillis() / 1000) - validTime;
                        long endT = wp.getVisit_end_dt() > 0 ? wp.getVisit_end_dt() : System.currentTimeMillis() / 1000;
                        logMPList = LogMPRealm.getLogMPTime(startT * 1000, endT * 1000);
                        int loMPonPoint = 0;
                        for (LogMPDB log : logMPList) {
                            if (log.distance != 0 && log.distance < Globals.distanceMin) {
                                loMPonPoint++;
                            }
                        }

                        textInteger.text = CustomString.underlineString(logMPList.size() + "/" + loMPonPoint, optionsButtons);
                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", LogMPDBViewModel.class.getCanonicalName());
                            bundle.putString("dataJson", new Gson().toJson(wp));
                            bundle.putString("title", "Історія місцеположення");
                            bundle.putString("subTitle", "Дані розташування по ТТ" + ": " + wp.getAddr_txt() +
                                    " за період з " + Clock.getHumanTime2(startT) + " по " + Clock.getHumanTime2(endT));
                            intent.putExtras(bundle);
                            mContext.startActivity(intent);
                        };
                        break;

                    case 135159:
                        int achievementSum = 0;
                        List<AchievementsSDB> achievementsSDBList = SQL_DB.achievementsDao().getByDad2(dad2);
                        if (achievementsSDBList != null) {
                            achievementSum = achievementsSDBList.size();
                        }

                        textInteger.text = CustomString.coloredString("" + achievementSum, optionsButtons);
                        break;

                    case (138518):
                        long startTime;

                        startTime = WpDataRealm.getWpDataRowByDad2Id(Long.parseLong(optionsButtons.getCodeDad2())).getVisit_start_dt();
                        textInteger.text = CustomString.coloredString("" + Clock.getHumanTimeOpt(startTime * 1000), optionsButtons);

                        break;
                    case (138520):
                        long endTime;

                        endTime = WpDataRealm.getWpDataRowByDad2Id(Long.parseLong(optionsButtons.getCodeDad2())).getVisit_end_dt();
                        textInteger.text = CustomString.coloredString("" + Clock.getHumanTimeOpt(endTime * 1000), optionsButtons);

                        break;

                    case (158309): // Фото витрины Приближённое

                        SpannableString spannableString158309 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 39));
                        spannableString158309.setSpan(new UnderlineSpan(), 0, spannableString158309.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger.text = spannableString158309;

                        textInteger.onClick = view -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(39).getNm());
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };
                        break;
                    case (158605):

                        SpannableString spannableString158605 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 40));
                        spannableString158605.setSpan(new UnderlineSpan(), 0, spannableString158605.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger.text = spannableString158605;

                        textInteger.onClick = view -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.STACK_PHOTO_FROM_OPTION_158605.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(40).getNm());
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };
                        break;
                    case (158308): // Фото витрины отдалённое

                    case (132968): // Вставляем количество выполненных Фоток Витрин

                        SpannableString spannableString132968 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 0));
                        spannableString132968.setSpan(new UnderlineSpan(), 0, spannableString132968.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger.text = spannableString132968;

                        textInteger.onClick = view -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.STACK_PHOTO_AFTER_FROM_ACHIEVEMENT.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(0).getNm());
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };
                        break;

                    case (157277): // Вставляем количество выполненных Фото Акционного Товара

                        SpannableString spannableString157277 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 28));
                        spannableString157277.setSpan(new UnderlineSpan(), 0, spannableString157277.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger.text = spannableString157277;

                        textInteger.onClick = view -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " +
                                    "Акційні товари");

                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };
                        break;

                    case (159726): // Фото ТТ
                    case (159725): // Кнопка "Фото Торговой Точки (ФТТ)"
                        textInteger.text =
                                setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 37));

                        textInteger.onClick = view -> {
                            Intent intent = new Intent(view.getContext(), PhotoLogActivity.class);
                            intent.putExtra("report_prepare", true);
                            intent.putExtra("dad2", dad2);
                            view.getContext().startActivity(intent);
                        };
                        break;

                    case (174878):
                        int photoCount174878 = RealmManager.stackPhotoShowcasePhotoCount(dad2, StackPhotoDB.PHOTO_EXPIRATION_DATE);
                        int requiredPhotoCount174878 = dataDB instanceof WpDataDB
                                ? OptionControlPhotoExpirationDate.getRequiredExpirationDatePhotoCount((WpDataDB) dataDB, optionsButtons)
                                : 0;
                        SpannableString spannableString174878 = setPhotoCountsMakeAndMust(optionsButtons, photoCount174878, requiredPhotoCount174878);
                        spannableString174878.setSpan(new UnderlineSpan(), 0, spannableString174878.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger.text = spannableString174878;

                        textInteger.onClick = view -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_174878.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " +
                                    "Фото товаров СИСГ (с истекающим/истекшим сроком годности)");

                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };

                        break;

                    case (175015):
                        int photoCount175015 = RealmManager.stackPhotoShowcasePhotoCount(dad2, StackPhotoDB.PHOTO_TOVAR_AND_PRICE);
                        int requiredPhotoCount175015 = dataDB instanceof WpDataDB
                                ? OptionControlPhotoTovarAndPrice.getRequiredTovarAndPricePhotoCount((WpDataDB) dataDB, optionsButtons)
                                : 0;
                        SpannableString spannableString175015 = setPhotoCountsMakeAndMust(optionsButtons, photoCount175015, requiredPhotoCount175015);
                        spannableString175015.setSpan(new UnderlineSpan(), 0, spannableString175015.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger.text = spannableString175015;

                        textInteger.onClick = view -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            ImagesTypeListDB photoType = ImagesTypeListRealm.getByID(StackPhotoDB.PHOTO_TOVAR_AND_PRICE);
                            String photoTypeName = photoType != null && photoType.getNm() != null
                                    ? photoType.getNm()
                                    : "# фото единиці товара+ценик";
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_175015.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + photoTypeName);
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };

                        break;

                    case (174546):
                        int pauseCount174546 = dad2 > 0 ? SQL_DB.wpDataPauseDao().getAllByDad2(dad2).size() : 0;
                        boolean pauseActive174546 = isPauseWorkActiveForCurrentVisit();
                        SpannableString spannableString174546 = new SpannableString(String.valueOf(pauseCount174546));
                        spannableString174546.setSpan(new UnderlineSpan(), 0, spannableString174546.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannableString174546.setSpan(
                                new ForegroundColorSpan(mContext.getResources().getColor(
                                        pauseActive174546 ? R.color.green_default : R.color.shadow
                                )),
                                0,
                                spannableString174546.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        );

                        textInteger.text = spannableString174546;
                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            JsonObject dataJson = new JsonObject();
                            dataJson.addProperty("codeDad2", dad2);
                            bundle.putString("viewModel", WpDataPauseSDBViewModel.class.getCanonicalName());

                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", dataJson.toString());
                            bundle.putString("title", "Перелік пауз у роботі");
                            bundle.putString("subTitle", "Довідник всіх пауз з поточної роботи");
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };
                        break;
                    case (158606):
                        SpannableString spannableString158606 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 36));
                        spannableString158606.setSpan(new UnderlineSpan(), 0, spannableString158606.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger.text = spannableString158606;
                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_157354.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(36).getNm());
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };
                        break;

                    case (160567):

                        OptionMassageType type = new OptionMassageType();
                        type.type = OptionMassageType.Type.STRING;
                        OptionControlPhotoShowcase<?> optionControlPhotoShowcase = new OptionControlPhotoShowcase<>(mContext, dataDB, optionsButtons, type, NULL, null);

                        SpannableString spannableString160567 = CustomString.coloredString(optionControlPhotoShowcase.getCounter(), optionsButtons);
                        spannableString160567.setSpan(new UnderlineSpan(), 0, spannableString160567.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger.text = spannableString160567;
                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", ShowcaseDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SHOWCASE_COMPLETED_CHECK.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            JsonObject dataJson = new JsonObject();

                            dataJson.addProperty("wpDataDBId", String.valueOf(dad2));

                            bundle.putString("dataJson", new Gson().toJson(dataJson));

                            bundle.putString("title", "Список витрин");
                            bundle.putString("subTitle", "Представленi усі вітрини на цій торговій точці, зеленим кольором відзначені вітрини за якими виготовлені фотографії, червоним – за якими немає");
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };
                        break;

                    case (157354):

                        SpannableString spannableString157354 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 42));
                        spannableString157354.setSpan(new UnderlineSpan(), 0, spannableString157354.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger.text = spannableString157354;
                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_157354.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(42).getNm());
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };
                        break;

                    case 164351:
                        textInteger.text =
                                setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 45));

                        textInteger.onClick = view -> {
                            Intent intent = new Intent(view.getContext(), PhotoLogActivity.class);
                            intent.putExtra("report_prepare", true);
                            intent.putExtra("dad2", dad2);
                            view.getContext().startActivity(intent);
                        };
                        break;

                    case 151139:
                        SpannableString spannableString151139 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 5));
                        spannableString151139.setSpan(new UnderlineSpan(), 0, spannableString151139.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger.text = spannableString151139;

                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", PlanogrammVizitShowcaseViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.PLANOGRAMM_VIZIT_SHOWCASE.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            JsonObject dataJson = new JsonObject();
                            dataJson.addProperty("clientId", String.valueOf(wp.getClient_id()));
                            dataJson.addProperty("addressId", wp.getAddr_id());
                            dataJson.addProperty("wpDataDBId", String.valueOf(wp.getCode_dad2()));
                            dataJson.addProperty("optionDBId", String.valueOf(optionsButtons.getID()));
                            bundle.putString("dataJson", new Gson().toJson(dataJson));
                            bundle.putString("title", "Планограма > Вітрина");
                            bundle.putString(
                                    "subTitle",
                                    "Для кожної Планограми вкажіть Вітрину, на якiй товар буде викладено згідно поточної планограми. Якщо Фото відповідної вітрини у списку вітрин немає, виберіть Фото Вітрини. Якщо у ТТ немає Вітрини для якої створена ця Планограма, то оцініть цю Планограму низькою оцінкою (нижче 5) і вкажіть коментар"
                            );
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };
                        break;
                    case 164355:

                        SpannableString spannableString164355 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 5));
                        spannableString164355.setSpan(new UnderlineSpan(), 0, spannableString164355.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger.text = spannableString164355;

                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_164355.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(5).getNm());
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };

                        break;

                    case 169108:
                        SpannableString spannableString169108 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 47));
                        spannableString169108.setSpan(new UnderlineSpan(), 0, spannableString169108.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger.text = spannableString169108;
                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_169108.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(47).getNm());
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };
                        break;

                    case 174213:
                        SpannableString spannableString174213 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 49));
                        spannableString174213.setSpan(new UnderlineSpan(), 0, spannableString174213.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ImagesTypeListDB imageType = ImagesTypeListRealm.getByID(49);

                        String name = imageType != null && imageType.getNm() != null
                                ? imageType.getNm()
                                : "Тип фото не определен";

                        textInteger.text = spannableString174213;
                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_174213.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + name);
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };
                        break;

                    case (158604):

                        SpannableString spannableString158604 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 41));
                        spannableString158604.setSpan(new UnderlineSpan(), 0, spannableString158604.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger.text = spannableString158604;
                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(41).getNm());
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };

                        break;

                    case (135809): // Вставляем количество выполненных Фото витрины ДО начала работ

                        SpannableString spannableString = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 14));
                        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        textInteger.text = spannableString;
                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.STACK_PHOTO_TO_FROM_ACHIEVEMENT.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(14).getNm());
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };

                        break;
                    case (135158): // Вставляем количество выполненных Фото Остатков Товаров (ФОТ)

                        SpannableString spannableString135158 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 4));
                        spannableString135158.setSpan(new UnderlineSpan(), 0, spannableString135158.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textInteger.text = spannableString135158;

                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(4).getNm());
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };

                        break;

                    case (151122): // Жилетка: жалобы на условия работ

                        String isSignal = optionsButtons.getIsSignal();
                        ForegroundColorSpan foregroundSpan = switch (isSignal) {
                            case "0" -> new ForegroundColorSpan(Color.GRAY);
                            case "1" ->
                                    new ForegroundColorSpan(mContext.getResources().getColor(R.color.red_error));
                            case "2" ->
                                    new ForegroundColorSpan(mContext.getResources().getColor(R.color.green_default));
                            case OptionUnlockPolicy.SIGNAL_UNLOCKED ->
                                    new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorInetYellow));
                            default -> new ForegroundColorSpan(Color.YELLOW);
                        };

                        List<Integer> themeIds = Arrays.asList(
                                THEME_OTHER,
                                THEME_IDEA,
                                THEME_PAYMENT_INCREASE,
                                THEME_MANAGER_WRONG,
                                THEME_NO_COMPLAINTS
                        );

                        List<QuestionAnswerDB> answerDBSList = SQL_DB.questionAnswerDao().getComplaintsByUserAndPeriod(
                                Globals.userId,
                                wp.getDt().getTime() / 1000 - 30 * 24L * 60L * 60L,
                                wp.getDt().getTime() / 1000 + 3 * 24L * 60L * 60L,
                                themeIds
                        );

                        SpannableString spannableString151122 = SpannableString.valueOf(answerDBSList.size() + "/1");
                        spannableString151122.setSpan(foregroundSpan, 0, spannableString151122.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannableString151122.setSpan(new UnderlineSpan(), 0, spannableString151122.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textInteger.text = spannableString151122;
                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", QuestionAnswerSDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.QUESTION_ANSWER_INFO.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Жалобы, Замечания, Предложения (Жилетка)");
                            bundle.putString("subTitle", "Перечень пожеланий, предложений, замечаний по работе системы. " +
                                    "Замечания которые вы здесь оставите будут передану руководству компании, проанализированы, и вы получите ответ. Для подачи нового обращения нажмите на кнопку +");
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };

                        break;

                    case (172100): // Фото вітрини з акційними цінниками

                        SpannableString spannableString172100 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 48));
                        spannableString172100.setSpan(new UnderlineSpan(), 0, spannableString172100.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textInteger.text = spannableString172100;
                        ImagesTypeListDB it = ImagesTypeListRealm.getByID(48);
                        String name172100 = (it != null && it.getNm() != null && !it.getNm().trim().isEmpty())
                                ? it.getNm()
                                : "Фото Biтрини з Aкційними Цінниками";

                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_172100.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + name172100);
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };

                        break;
                    case (132969): // Вставляем количество выполненных Фото Тележка с Товаром (ФТТ)

                        SpannableString spannableString132969 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 10));
                        spannableString132969.setSpan(new UnderlineSpan(), 0, spannableString132969.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textInteger.text = spannableString132969;

                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());

                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(10).getNm());
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };

                        break;

                    case (141360):
                        SpannableString spannableString141360 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 31));
                        spannableString141360.setSpan(new UnderlineSpan(), 0, spannableString141360.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textInteger.text = spannableString141360;

                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            bundle.putString("dataJson", new Gson().toJson(dad2));
                            bundle.putString("title", "Перелік фото звітів");
                            bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(31).getNm());
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };

                        break;

                    case 137797: // Остатки
                        String msg = String.format("%s/%s/%s", (int) DetailedReportActivity.SKUPlan, (int) DetailedReportActivity.SKUFact, (int) DetailedReportActivity.OFS);
                        textInteger.text = CustomString.coloredString("" + msg, optionsButtons);

                        break;

                    case 141910: // "Получение заказа в ТТ"
                        String counter141910 = DetailedReportActivity.rpAmountSum + " шт";
                        textInteger.text = counter141910;
                        break;

                    case 141888: // "Выкуп Товара с ТТ"
                        String counter141888 = DetailedReportActivity.rpTotalSumToRedemptionOfGoods + "грн";
                        textInteger.monetary = true;
                        textInteger.text = counter141888;
                        break;

                    case 141885: // Фото Документов
                        textInteger.text = "" + RealmManager.stackPhotoShowcasePhotoCount(dad2, 3);
                        break;

                    case 138339: // Доп Требования

                        Integer ttCategory = null;
                        AddressSDB addressSDB = SQL_DB.addressDao().getById(wp.getAddr_id());
                        if (addressSDB != null) {
                            ttCategory = addressSDB.ttId;
                        }
                        textInteger.text = CustomString.coloredString("" + AdditionalRequirementsRealm.getData3(dataDB, HIDE_FOR_USER, ttCategory, null, 1).size(), optionsButtons);
                        break;

                    case 138340: // Доп Требования

                        textInteger.text = CustomString.coloredString("" + SQL_DB.additionalMaterialsDao().getAllForOptionTEST2(optionsButtons.getClientId(), "0").size(), optionsButtons);
                        break;

                    case 135328: // Рекламация
                        type = new OptionMassageType();
                        type.type = OptionMassageType.Type.STRING;
                        OptionControlReclamationAnswer<?> optionControlReclamationAnswer = new OptionControlReclamationAnswer<>(mContext, dataDB, optionsButtons, type, NULL, null);

                        textInteger.text = CustomString.coloredString("" + optionControlReclamationAnswer.problemReclamationCount(), optionsButtons);
                        break;

                    case 135327: // Задачи
                        type = new OptionMassageType();
                        type.type = OptionMassageType.Type.STRING;
                        OptionControlTaskAnswer<?> optionControlTask = new OptionControlTaskAnswer<>(mContext, dataDB, optionsButtons, type, NULL, null);

                        textInteger.text = CustomString.coloredString("" + optionControlTask.problemTaskCount(), optionsButtons);

                        break;

                    case 168439:
                        type = new OptionMassageType();
                        type.type = OptionMassageType.Type.STRING;
                        OptionControlPlanorammVizit<?> optionControlPlanorammVizit = new OptionControlPlanorammVizit<>(mContext, dataDB, optionsButtons, type, NULL, null);
                        break;

                    case 168598: // Мнение
                        type = new OptionMassageType();
                        type.type = OptionMassageType.Type.STRING;
                        OptionControlAddOpinion<?> optionControlAddOpinion = new OptionControlAddOpinion<>(mContext, dataDB, optionsButtons, type, NULL, null);

                        textInteger.text = CustomString.underlineString(optionControlAddOpinion.currentOpinionStatus(), optionsButtons);

                        if (dataDB instanceof WpDataDB wpDataDB) {
                            int wpDataUserOpinionID = wpDataDB.getUser_opinion_id() != null ? Integer.parseInt(wpDataDB.getUser_opinion_id()) : 0;

                            String opinionName;
                            if (wpDataUserOpinionID > 0) {
                                OpinionSDB opinion = RoomManager.SQL_DB.opinionDao().getOpinionById(wpDataUserOpinionID);
                                opinionName = opinion.nm;
                            } else {
                                opinionName = "Ви ще не встановлювали думку щодо цього відвідування, ви можете виправити це зараз і вибрати потрібну думку.";
                            }
                            textInteger.onClick = v -> {
                                new MessageDialogBuilder((Activity) mContext)
                                        .setTitle("Думка виконавця")
                                        .setStatus(DialogStatus.NORMAL)
                                        .setSubTitle("Поточна думка, ви можете її змінити")
                                        .setMessage(opinionName)
                                        .setOnConfirmAction(() -> Unit.INSTANCE)
                                        .setOnCancelAction("Змiнити", () -> {
                                            new OptionButtonUserOpinion<>(mContext, dataDB, optionsButtons, type, NULL, null);
                                            return Unit.INSTANCE;
                                        })
                                        .show();
                            };
                        }
                        break;

                    case 141069: // Сравнение остатков и наличия
                        type = new OptionMassageType();
                        type.type = OptionMassageType.Type.STRING;
                        OptionControlStockBalanceTovar<?> optionControlStockBalanceTovar = new OptionControlStockBalanceTovar<>(mContext, dataDB, optionsButtons, type, NULL, null);

                        textInteger.text = CustomString.underlineString(optionControlStockBalanceTovar.currentStockBalanceCount(), optionsButtons);
                        textInteger.onClick = v -> {
                            Intent intent = new Intent(mContext, FeaturesActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("viewModel", TovarDBViewModel.class.getCanonicalName());
                            bundle.putString("contextUI", ContextUI.TOVAR_FROM_ACHIEVEMENT.toString());
                            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                            try {
                                bundle.putString("dataJson", new Gson().toJson(
                                        new JSONObject()
                                                .put("codeDad2", Long.toString(dad2))
                                                .put("clientId", wp.getClient_id()))
                                );
                            } catch (Exception ignored) {
                                ignored.printStackTrace();
                            }
                            bundle.putString("title", "Товари");
                            bundle.putString("subTitle", "Перечень товаров к текущему посещению");
                            intent.putExtras(bundle);
                            ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                        };
                        break;

                    default:
                        textInteger.visibility = View.GONE;

                }
            } catch (Exception e) {

                e.printStackTrace();
            }

            // Row click, long click and signal use the existing NNK/control pipeline.
        final DetailedReportButtons detailedReportButtons = new DetailedReportButtons();

            final Options options = new Options();

            boolean finalDescribedOption = describedOption;
            row.onClick = view -> {
                if (!isAttached()) return;

                if (finalDescribedOption) {

                    OptionMassageType msgType = new OptionMassageType();
                    msgType.type = OptionMassageType.Type.DIALOG;
                    options.setOptionFromDetailedReport(allReportOption);
                    msgType = options.NNK(view, mContext, dataDB, optionsButtons, butt, msgType, Options.NNKMode.MAKE, () -> {
                        try {
                            if (isAttached()) {
                                detailedReportButtons.buttonClick(mContext, (WpDataDB) dataDB, optionsButtons, 0);
                                setCheck(optionsButtons, NULL);
                            }
                        } catch (Exception e) {
                            Globals.writeToMLOG("ERROR", "OptionsRowFactory/click", "option=" + optionsButtons.getOptionId() + ", error=" + e);
                        }

                        refresh.run();
                    });

                    if (msgType != null && msgType.dialog != null) {
                        msgType.dialog.setDialogIco();
                        msgType.dialog.show();
                    }

                    if (msgType != null && msgType.msg != null && !msgType.msg.equals("")) {
                        Toast.makeText(mContext, msgType.msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(view.getContext(), "Данная Опция находится в РАЗРАБОТКЕ", Toast.LENGTH_SHORT).show();
                }

            };

            OptionsDB test = optionsButtons;
            row.onLongClick = view -> {
                if (!isAttached()) return true;
                if (optionId == 132968 || optionId == 158309 || optionId == 158308) {

                    String pass = CodeGenerator.getCode();
                    String longClickDialogText = "Для продолжения внесите пароль: ";

                    try {
                        UsersSDB currentUser = SQL_DB.usersDao().getUserById(Globals.userId);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String currentDate = dateFormat.format(new Date());
                        long userDate = currentUser.reportDate01 != null ? currentUser.reportDate01.getTime() : 0L;
                        if (userDate > 1754006400000L) {
                            longClickDialogText = "Для Вас цей функціонал - недоступний.";
                            pass = CodeGenerator.sha256(currentDate);
                            pass = pass.substring(pass.length() - 5);
                        }
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "longClickDialog", "Exception e: " + e);
                    }

                    final String unlockCode = pass;

                    optionDetailPhotos(test, view.getContext());
                    DialogData dialog = new DialogData(mContext);
                    dialog.setTitle("Внесите пароль!");
                    dialog.setText(longClickDialogText);
                    dialog.setClose(dialog::dismiss);
                    dialog.setOperation(DialogData.Operations.TEXT, "", null, () -> {
                    });
                    dialog.setOk("Ok", () -> {
                        if (!isAttached()) return;
                        Toast.makeText(dialog.context, "Внесли: " + dialog.getOperationResult(), Toast.LENGTH_SHORT).show();

                        String res = dialog.getOperationResult();

                        if (res.equals(unlockCode)) {
                            longClickButton(test, optionId, detailedReportButtons, optionsButtons, view.getContext());
                        } else {
                            Toast.makeText(dialog.context, "Внесите корректный пароль", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.show();
                } else {
                    int optId = Integer.parseInt(optionsButtons.getOptionId());
                    longClickButton(test, optId, detailedReportButtons, optionsButtons, view.getContext());
                }
                return false;
            };

            if (optionId == OptionButtonPauseWork.OPTION_BUTTON_PAUSE_WORK_ID) {
                setCheck.onClick = v -> row.onClick.onClick(v);
            } else {
                setCheck.onClick = v -> {
                    Toast.makeText(v.getContext(), "Проверка статуса данной опции", Toast.LENGTH_SHORT).show();
                    setCheck(optionsButtons, CHECK_CLICK);
                };
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionsRowFactory/create", "Exception e: " + e);
            Globals.writeToMLOG("ERROR", "OptionsRowFactory/create", "Stack: " + Arrays.toString(e.getStackTrace()));
        }
        return row;
    }

    private void applyPauseWorkSignalIcon(OptionItemState.Signal setCheck) {
        boolean pauseActive = isPauseWorkActiveForCurrentVisit();

        setCheck.visibility = View.VISIBLE;
        setCheck.iconRes = pauseActive ? R.drawable.ic_play_circle_solid : R.drawable.ic_pause_work_toolbar;
        setCheck.tint = mContext.getResources().getColor(
                pauseActive ? R.color.green_default : R.color.shadow
        );
    }

    private boolean isPauseWorkActiveForCurrentVisit() {
        if (dataDB instanceof WpDataDB) {
            long codeDad2 = ((WpDataDB) dataDB).getCode_dad2();
            return codeDad2 > 0 && PauseWorkStateHolder.hasPauseFor(codeDad2);
        }
        return false;
    }

    private CharSequence counter2Text(int optionId) {
        CharSequence res = "";
        if (dataDB instanceof WpDataDB) {
            WpDataDB wpDataDB = (WpDataDB) dataDB;
            if (optionId == 135412)
                res = "~" + String.format("%.2f", wpDataDB.getCash_zakaz() * Globals.OPTION_CONTROL_PENALTY_RATE * 2);
            else
                res = "~" + String.format("%.2f", wpDataDB.getCash_zakaz() * Globals.OPTION_CONTROL_PENALTY_RATE);
            res = Html.fromHtml("<font color=red>" + res + " грн" + "</font>");
        }
        return res;
    }

    private CharSequence counter2EKLText() {
        CharSequence res = "";
        if (dataDB instanceof WpDataDB) {
            WpDataDB wpDataDB = (WpDataDB) dataDB;
            UsersSDB users = SQL_DB.usersDao().getUserById(wpDataDB.getUser_id());
            if (users != null) {
                float shtraf = 0.308f;
                if (users.last_ekl_date != null) {
                    long ekl_date = convertDateToSeconds(users.last_ekl_date);
                    long countDay = startDt - (DAYS * 24 * 60 * 60);
                    if (ekl_date != -1 && ekl_date > countDay)
                        shtraf = 0.154f;
                }
                res = "~" + String.format("%.2f", wpDataDB.getCash_zakaz() * shtraf);
                res = Html.fromHtml("<font color=red>" + res + "грн" + "</font>");
            }
        }
        return res;
    }

    public static long convertDateToSeconds(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {

            Date date = dateFormat.parse(dateString);

            return date.getTime() / 1000;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void longClickButton(OptionsDB test, int optId, DetailedReportButtons detailedReportButtons, OptionsDB optionsButtons, Context context) {
        optionDetailPhotos(test, context);

        if (optId == 132968 || optId == 158309 || optId == 158308) {
            if (dataDB instanceof WpDataDB) {
                WpDataDB wpDataDB = (WpDataDB) dataDB;
                detailedReportButtons.buttonClick(mContext, wpDataDB, optionsButtons, 1);
            }
        }
    }

    private void optionDetailPhotos(OptionsDB option, Context context) {
        String additionalText = "\n\n";

        String buttText = option.getOptionTxt();
        buttText = buttText.replace("&quot;", "");
        buttText = buttText.replace("Кнопка ", "");

        int photoType = 0;
        boolean showPhotoLink = false;
        switch (option.getOptionId()) {
            case "158308":
            case "132968": // - 0  - фото витрины
                photoType = 0;
                showPhotoLink = true;
                break;

            case "141885": // - 3  - Фото Документов
                photoType = 3;
                showPhotoLink = true;
                break;

            case "135158": // - 4  - Фото остатков товаров
                photoType = 4;
                showPhotoLink = true;
                break;

            case "164355": // - 5  - Фото планограммы
                photoType = 5;
                showPhotoLink = true;
                break;

            case "132969": // - 10 - Фото тележка с товаром
                photoType = 10;
                showPhotoLink = true;
                break;

            case "135809": // - 14 - Фото витрины До начала работ
                photoType = 14;
                showPhotoLink = true;
                break;

            case "157277": // - 28 - Фото Акционного Товара
                photoType = 28;
                showPhotoLink = true;
                break;

            case "141360": // - 31 - Фото товара на складе
                photoType = 31;
                showPhotoLink = true;
                break;

            case "158606": //- 36 - доп. место продажи
                photoType = 36;
                showPhotoLink = true;
                break;

            case "158309": // - 39 - "Фото Витрины" (Наближене)
                photoType = 39;
                showPhotoLink = true;
                break;

            case "159726": // - 37 - Фото ТТ
            case "159725": // - 37 - Кнопка "Фото Торговой Точки (ФТТ)"
                photoType = 37;
                showPhotoLink = true;
                break;

            case "158605": // - 40 - Корпоративный блок
                photoType = 40;
                showPhotoLink = true;
                break;

            case "158604": //- 41 - Наполненность
                photoType = 41;
                showPhotoLink = true;
                break;

            case "157354": // - 42 - Фото ДМП
                photoType = 42;
                showPhotoLink = true;
                break;

            case "133382": // - 25 - Фото товара потенциального клиента
                photoType = 25;
                showPhotoLink = true;
                break;

            case "172100": // - 48 - Фото вітрини з акційними цінниками
                photoType = 48;
                showPhotoLink = true;
                break;
        }

        SpannableStringBuilder ss = new SpannableStringBuilder();
        ss.append(option.getOptionDescr());
        ss.append(additionalText);
        if (showPhotoLink) {
            ss.append("\n\n");
            switch (option.getOptionId()) {
                case "135158": // - 4  - Фото остатков товаров
                    ss.append(createLinkedStringGal(mContext, "Завантажити фото з галереї", photoType, () -> {

                        OptionMassageType newOptionType = new OptionMassageType();
                        newOptionType.type = DIALOG;

                        OptionControlAvailabilityControlPhotoRemainingGoods<?> optionControlAvailabilityControlPhotoRemainingGoods =
                                new OptionControlAvailabilityControlPhotoRemainingGoods<>(context, (WpDataDB) dataDB, option, newOptionType, Options.NNKMode.CHECK, null);
                        if (optionControlAvailabilityControlPhotoRemainingGoods.signal && option.getOptionControlId().equals("159707")) {
                            optionControlAvailabilityControlPhotoRemainingGoods.showOptionMassage("");
                        } else {
                            click.click(4);
                        }
                    }));

                    ss.append("\n\n");
                    break;

                case "164355": // - 5  - Фото планограммы

                    ss.append(createLinkedStringGal(mContext, "Завантажити фото з галереї", photoType, () -> {
                        click.click(5);
                    }));
                    ss.append("\n\n");
                    break;
            }
            ss.append(createLinkedString(mContext, "Показать образец фото", photoType, option));
            ss.append("\n");
        }

        DialogData dialog = new DialogData(mContext);
        dialog.setTitle(buttText);
        dialog.setText(ss, () -> {
        });
        dialog.setMerchikIco(mContext);
        dialog.show();
    }

    private SpannableString createLinkedString(Context context, String msg, int photoType, OptionsDB optionsDB) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                try {
                    WpDataDB wp = (WpDataDB) dataDB;
                    AddressSDB addressSDB = SQL_DB.addressDao().getById(wp.getAddr_id());

                    ImagesTypeListDB imagesTypeDB = ImagesTypeListRealm.getByID(photoType);
                    String imagesTypeName = imagesTypeDB != null && imagesTypeDB.getNm() != null
                            ? imagesTypeDB.getNm()
                            : "Тип фото " + photoType;

                    List<SamplePhotoSDB> samplePhotoSDBList = SQL_DB.samplePhotoDao().getPhotoLogActiveAndTp(1, photoType, addressSDB.tpId);
                    if (samplePhotoSDBList != null && samplePhotoSDBList.size() > 1) {

                        ContextUI samplePhotoContextUI = resolveSamplePhotoContextUI(photoType, optionsDB);
                        Intent intent = new Intent(context, FeaturesActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("viewModel", SamplePhotoSDBViewModel.class.getCanonicalName());
                        bundle.putString("contextUI", samplePhotoContextUI.toString());
                        JsonObject dataJson = new JsonObject();
                        dataJson.addProperty("tradeMarkDBId", String.valueOf(addressSDB.tpId));
                        dataJson.addProperty("photoType", photoType);
                        dataJson.addProperty("optionId", optionsDB != null ? optionsDB.getOptionId() : "");
                        dataJson.addProperty("wpDataDBId", String.valueOf(wp.getId()));
                        dataJson.addProperty("optionDBId", optionsDB != null ? String.valueOf(optionsDB.getID()) : "");
                        bundle.putString("dataJson", new Gson().toJson(dataJson));
                        bundle.putString("title", context.getString(R.string.title_samplephotosdb) + " " + imagesTypeName);
                        bundle.putString("subTitle", "В списке представлены образцы фотоотчетов. " +
                                "Для того, чтобы изготовить '" + imagesTypeName + "' нажмите на соответствующую фотографию. " +
                                "Затем увеличьте ее до размера экрана и выполните фото, нажав на кнопку фотоаппарата в правом нижнем углу. ");
                        intent.putExtras(bundle);
                        ActivityCompat.startActivityForResult((Activity) context, intent, NEED_UPDATE_UI_REQUEST, null);

                    } else if (samplePhotoSDBList != null && samplePhotoSDBList.size() == 1) {

                        try {
                            Globals.writeToMLOG("INFO", "Не могу найти образцы фото SOLO ", "samplePhotoSDBList: " + new Gson().toJson(samplePhotoSDBList));
                            StackPhotoDB photo = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(String.valueOf(samplePhotoSDBList.get(0).photoId));
                            Globals.writeToMLOG("INFO", "Не могу найти образцы фото SOLO ", "photo: " + new Gson().toJson(photo));
                            DialogFullPhotoR dialog = new DialogFullPhotoR(context);
                            dialog.setPhoto(photo);
                            dialog.commentOn = true;

                            String commentPhoto = samplePhotoSDBList.get(0).about;
                            if (commentPhoto != null && commentPhoto != "") {
                                dialog.setComment(commentPhoto);
                            } else dialog.setComment(photo.getComment());
                            dialog.scaleType(ImageView.ScaleType.FIT_CENTER);
                            dialog.setClose(dialog::dismiss);
                            dialog.show();
                        } catch (Exception e) {
                            Globals.writeToMLOG("ERROR", "Не могу найти образцы фото SOLO ", "Exception e: " + e);
                        }

                    } else {

                        Toast.makeText(context, "Не могу найти образцы фото. " + context.getText(R.string.msg_try_sync), Toast.LENGTH_SHORT).show();
                        Globals.writeToMLOG("ERROR", "Не могу найти образцы фото", "");
                    }
                } catch (Exception e) {

                    Toast.makeText(context, "Не могу отобразить образец фото. " + context.getText(R.string.msg_try_sync), Toast.LENGTH_SHORT).show();
                    Globals.writeToMLOG("ERROR", "Не могу отобразить образец фото по причине", "Exception e: " + e);
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        res.setSpan(clickableSpan, 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return res;
    }

    private ContextUI resolveSamplePhotoContextUI(int photoType, OptionsDB optionsDB) {
        String optionId = optionsDB != null ? optionsDB.getOptionId() : null;

        if ("135158".equals(optionId) || photoType == 4)
            return ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158;
        if ("164355".equals(optionId) || photoType == 5)
            return ContextUI.SAMPLE_PHOTO_FROM_OPTION_164355;
        if ("141360".equals(optionId) || photoType == 31)
            return ContextUI.SAMPLE_PHOTO_FROM_OPTION_141360;
        if ("132969".equals(optionId) || photoType == 10)
            return ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969;
        if ("135809".equals(optionId) || photoType == 14)
            return ContextUI.SAMPLE_PHOTO_FROM_OPTION_135809;
        if ("158309".equals(optionId) || photoType == 39)
            return ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309;
        if ("158604".equals(optionId) || photoType == 41)
            return ContextUI.SAMPLE_PHOTO_FROM_OPTION_158604;
        if ("157277".equals(optionId) || photoType == 28)
            return ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277;
        if ("157354".equals(optionId) || photoType == 42)
            return ContextUI.SAMPLE_PHOTO_FROM_OPTION_157354;
        if ("169108".equals(optionId) || photoType == 47)
            return ContextUI.SAMPLE_PHOTO_FROM_OPTION_169108;
        if ("172100".equals(optionId) || photoType == 48)
            return ContextUI.SAMPLE_PHOTO_FROM_OPTION_172100;
        if ("174213".equals(optionId) || photoType == 49)
            return ContextUI.SAMPLE_PHOTO_FROM_OPTION_174213;

        Globals.writeToMLOG(
                "INFO",
                "RecycleViewDRAdapter.resolveSamplePhotoContextUI",
                "Use generic contextUI, optionId=" + optionId + ", photoType=" + photoType
        );
        return ContextUI.SAMPLE_PHOTO_FROM_OPTION_GENERIC;
    }

    private SpannableString createLinkedStringGal(Context context, String msg, int photoType, Clicks.clickVoid click) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

                click.click();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        res.setSpan(clickableSpan, 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return res;
    }

    private SpannableString setPhotoCountsMakeAndMust(OptionsDB option, int dataBaseCount) {
        return setPhotoCountsMakeAndMust(option, dataBaseCount, null);
    }

    private SpannableString setPhotoCountsMakeAndMust(OptionsDB option, int dataBaseCount, Integer exactRequiredCount) {
        SpannableString res = new SpannableString("");
        String data = "";

        String isSignal = option.getIsSignal();

        Log.e("setPhotoCountsMakeAndM", "OptionId: " + option.getOptionId() + " | OptionControlId: " + option.getOptionControlId());
        Log.e("setPhotoCountsMakeAndM", "isSignal: " + option.getIsSignal());
        Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/setPhotoCountsMakeAndMust", "OptionId: " + option.getOptionId() + " | OptionControlId: " + option.getOptionControlId()
                + " | code_dad2: " + option.getCodeDad2());

        String min = exactRequiredCount != null ? String.valueOf(Math.max(0, exactRequiredCount)) : option.getAmountMin();
        if (min == null || min.trim().isEmpty()) {
            min = "0";
        }
        if (exactRequiredCount == null && min.equals("0")) {
            min = "3";

            if (option.getOptionId().equals("151139")
                    || option.getOptionId().equals("164351")
                    || option.getOptionControlId().equals("164351")
                    || option.getOptionId().equals("169108")
            ) min = "1";
            else if (option.getOptionId().equals("164355"))
                min = "0";
            else if (option.getOptionId().equals("132969"))
                min = "1";
            else if (option.getOptionId().equals("135158"))
                min = "1";
            else if (option.getOptionId().equals("174213"))
                min = "1";
            else if (option.getOptionId().equals("141360")) {
                RealmResults<StackPhotoDB> stackPhotoDB = StackPhotoRealm.getPhotosByDAD2(dad2, 31);
                long count = stackPhotoDB.where()
                        .equalTo("example_id", "78")
                        .count();
                if (count > 0)
                    min = "2";
                else
                    min = "1";
            }
            try {
                if (option.getOptionId().equals("157277")) {
                    List<ReportPrepareDB> reportPrepare = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(dad2));
                    List<AdditionalRequirementsDB> ad = AdditionalRequirementsRealm.getData3((WpDataDB) dataDB, DEFAULT, null, null, 0);
                    String[] tovIds = new String[ad.size()];
                    for (int i = 0; i < ad.size(); i++) {
                        tovIds[i] = ad.get(i).getTovarId();
                    }
                    Arrays.sort(tovIds);
                    int count = 0;
                    for (ReportPrepareDB item : reportPrepare) {
                        String akciya = item.akciyaId;
                        if (akciya == null || akciya.equals("")) continue;

                        if (Arrays.asList(tovIds).contains(item.getTovarId())) {
                            count++;
                        }
                    }

                    if (ad != null && ad.size() > 0) {
                        min = String.valueOf(count);
                    }
                }

                if (dataDB != null && ((dataDB instanceof WpDataDB && "164352".equals(option.getOptionId())
                        || (dataDB instanceof WpDataDB && "164351".equals(option.getOptionId()))))) {
                    WpDataDB wpDataDB = (WpDataDB) dataDB;
                    AddressSDB addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
                    if ("77190".equals(wpDataDB.getClient_id())
                            && addressSDB != null) {
                        int m = 0;
                        int kolKass = addressSDB.kolKass != null
                                ? addressSDB.kolKass
                                : 0;

                        int kolKassSo = addressSDB.kolKassSo != null
                                ? addressSDB.kolKassSo
                                : 0;

                        m = Math.max(m, kolKass - kolKassSo);
                        min = String.valueOf(m);
                    }
                }
            } catch (Exception e) {
                Log.e("!!!", "error: " + e.getMessage());
                Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/setPhotoCountsMakeAndMust", "Exception: " + e.getMessage());
            }
        }

        int maxPhotos = Integer.parseInt(min);
        if (exactRequiredCount != null)
            maxPhotos = Math.max(0, exactRequiredCount);
        else if (option.getOptionId().equals("135158") || option.getOptionId().equals("141360") || option.getOptionId().equals("132969") ||
                (dataDB != null && dataDB instanceof WpDataDB && ((WpDataDB) dataDB).getSku() < 5))
            maxPhotos = maxPhotos;
        else
            maxPhotos = maxPhotos + 1;

        if (exactRequiredCount == null && option.getOptionId().equals("158308"))
            maxPhotos = maxPhotos + 1;

        data = "" + dataBaseCount + "/" + maxPhotos;

        res = new SpannableString(data);

        ForegroundColorSpan foregroundSpan = switch (isSignal) {
            case "0" -> new ForegroundColorSpan(Color.GRAY);
            case "1" ->
                    new ForegroundColorSpan(mContext.getResources().getColor(R.color.red_error));
            case "2" ->
                    new ForegroundColorSpan(mContext.getResources().getColor(R.color.green_default));
            case OptionUnlockPolicy.SIGNAL_UNLOCKED ->
                    new ForegroundColorSpan(mContext.getResources().getColor(R.color.colorInetYellow));
            default -> new ForegroundColorSpan(Color.YELLOW);
        };

        res.setSpan(foregroundSpan, 0, res.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/setPhotoCountsMakeAndMust", "Result: " + data);

        return res;
    }

}
