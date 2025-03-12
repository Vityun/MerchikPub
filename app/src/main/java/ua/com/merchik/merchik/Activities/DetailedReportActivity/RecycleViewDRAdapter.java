package ua.com.merchik.merchik.Activities.DetailedReportActivity;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.NEED_UPDATE_UI_REQUEST;
import static ua.com.merchik.merchik.Options.Options.NNKMode.CHECK_CLICK;
import static ua.com.merchik.merchik.Options.Options.NNKMode.NULL;
import static ua.com.merchik.merchik.data.OptionMassageType.Type.DIALOG;
import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.DEFAULT;
import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.HIDE_FOR_USER;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import kotlin.Pair;
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogActivity;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonAddNewClient;
import ua.com.merchik.merchik.Options.Controls.OptionControlAvailabilityControlPhotoRemainingGoods;
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
import ua.com.merchik.merchik.data.Database.Room.SamplePhotoSDB;
import ua.com.merchik.merchik.data.Database.Room.SiteObjectsSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.OptionsButtons;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.dataLayer.MainRepositoryKt;
import ua.com.merchik.merchik.dataLayer.ModeUI;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ImagesTypeListRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR;
import ua.com.merchik.merchik.features.main.DBViewModels.StackPhotoDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.TovarDBViewModel;

public class RecycleViewDRAdapter<T> extends RecyclerView.Adapter<RecycleViewDRAdapter.ViewHolder> {

    private List<OptionsDB> butt;
    private List<OptionsDB> allReportOption;
    private List<SiteObjectsSDB> translate;
    private final Context mContext;
    private Clicks.click click;
    //    private static WpDataDB wpDataDB;
    private T dataDB;

    long dad2, startDt, endDt;

    private final int DAYS = 4;   // ветка дней на склолько раньше проверяем ЭКЛ !!!ПРИ ИЗМЕНЕНИИ РЕАДКТИРОВАТЬ АНАЛОГИЧНОЕ В OptionControlEKL

//    public void blinkItem() {
//        Timer timer = new Timer();
//        timer.schedule(new BlinkTask(), 1000, 1000);
//    }

//    private class BlinkTask extends TimerTask {
//        private boolean blink = true;
//
//        public BlinkTask() {
//        }
//
//        public void run() {
//            if (blink){
//
//                constraintLayout.setBackgroundResource(R.color.greenCol);
//                blink = false;
//            }else {
//                constraintLayout.setBackgroundResource(R.drawable.bg_temp);
//                blink = true;
//            }
//        }
//    }


    /*Определяем ViewHolder*/
    class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout optionButton;
        TextView textTitle;
        TextView textInteger, textInteger2;
        ImageView setCheck;

        private Context getCtx() {
            return optionButton.getContext();
        }

        ViewHolder(View v) {
            super(v);
            optionButton = v.findViewById(R.id.option_item_container);
            textTitle = v.findViewById(R.id.textViewTitle);
            textInteger = v.findViewById(R.id.textViewInteger);
            textInteger2 = v.findViewById(R.id.textViewInteger2);
            setCheck = v.findViewById(R.id.imageViewCheck);
            setCheck.setClickable(true);
        }

        public void animate() {
            int colorFrom = getCtx().getResources().getColor(R.color.green_default);
            int colorTo = getCtx().getResources().getColor(R.color.red_error);
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(250); // milliseconds
            colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
            colorAnimation.setRepeatCount(10);
            colorAnimation.addUpdateListener(animator ->
                    optionButton.setBackgroundColor((int) animator.getAnimatedValue())
            );
            colorAnimation.start();
        }

        public int adjustAlpha(int color, float factor) {
            int alpha = Math.round(Color.alpha(color) * factor);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            return Color.argb(alpha, red, green, blue);
        }

        public void bind(OptionsDB optionsButtons, SiteObjectsSDB siteObjectsSDB) {
            try {
                final int POS = getAdapterPosition();
                boolean describedOption = true;

                Log.e("RViewDRAdapterBind", "optionsButtons: " + optionsButtons);

                textInteger.setVisibility(View.VISIBLE);

                String buttText = optionsButtons.getOptionTxt();
                buttText = buttText.replace("&quot;", "\"");
                buttText = buttText.replace("Кнопка ", "");

                if (siteObjectsSDB != null && !optionsButtons.getOptionTxt().contains("Планограммы")) {
                    Log.e("R_TRANSLATES", "siteObjectsSDB.id: " + siteObjectsSDB.id);
                    Log.e("R_TRANSLATES", "siteObjectsSDB.commentsTranslation: " + siteObjectsSDB.commentsTranslation);
                    buttText = siteObjectsSDB.commentsTranslation;
                }

                Log.e("bindRPA", "=============================================");
                Log.e("bindRPA", "buttText: " + buttText);
                Log.e("bindRPA", "optionsButtons.getOptionId(): " + optionsButtons.getOptionId());
                Log.e("bindRPA", "optionsButtons.getOptionControlId(): " + optionsButtons.getOptionControlId());
                Log.e("bindRPA", "optionsButtons.getOptionBlock1(): " + optionsButtons.getOptionBlock1());
                Log.e("bindRPA", "optionsButtons.getOptionBlock2(): " + optionsButtons.getOptionBlock2());
                Log.e("bindRPA", "optionsButtons.getIsSignal(): " + optionsButtons.getIsSignal());
                Log.e("bindRPA", "optionsButtons.getBlockPns(): " + optionsButtons.getBlockPns());
                Log.e("bindRPA", "optionsButtons.getAmountMin(): " + optionsButtons.getAmountMin());
                Log.e("bindRPA", "optionsButtons.getAmountMax(): " + optionsButtons.getAmountMax());

                // Подсвечивает Кнопки Опций с блоком ПНС() КРАСНЫМ цветом
                if (optionsButtons.getIsSignal().equals("1") && optionsButtons.getBlockPns().equals("1")) {
                    Log.e("bindRPA", "RED");
                    textTitle.setText("" + Html.fromHtml("<font color='#FF0000'>" + buttText + "</font>")); // Должно гореть красным
                } else {
                    Log.e("bindRPA", "NORM");
                    textTitle.setText("" + buttText);
                }

                int optionId = Integer.parseInt(butt.get(getAdapterPosition()).getOptionId());

                // Выделяет жирным ОСОБЕННЫЕ Кнопки Опций
                if (optionId == 132968 || optionId == 158309 || optionId == 158308) { // Фото витрины)
                    textTitle.setTypeface(null, Typeface.BOLD);
                } else {
                    textTitle.setTypeface(null, Typeface.NORMAL);
                }

                // 06.08.2020
                // На данный момент опции делаем "нажимными" по id-шникам.
                // ОПИСАННЫЕ Кнопки Опций
                // TODO заменить на ENUM
                if (optionId == 135809   // Фото витрины ДО начала работ
                        || optionId == 132968   // Фото витрины
                        || optionId == 135158   // Фото Остатков Товаров (ФОТ)
                        || optionId == 132969   // Фото Тележка с Товаром (ФТТ)
                        || optionId == 138518   // Начало работы
                        || optionId == 138520   // Окончание работы
                        || optionId == 138773   // Местоположение
                        || optionId == 137797   // ДеталОтчёт план по товарам
                        || optionId == 138339   // Доп. Требования
                        || optionId == 141360   // Фото товара на складе
                        || optionId == 141910   // Получение заказа в ТТ
                        || optionId == 141888   // Выкуп Товара с ТТ
                        || optionId == 141885   // Фото Документов
                        || optionId == 84007    // ЭКЛ
                        || optionId == 132666   // Стандарт
                        || optionId == 139576   // Версия ПО
                        || optionId == 138767   // Планограмма
                        || optionId == 135742   // "Дет.Отчет" (по Клиенто-Адресу)
                        || optionId == 132621   // Оценка
                        || optionId == 84003    // Мнение о сотруднике
                        || optionId == 138340   // Доп. Материалы
                        || optionId == 135327   // Задача
                        || optionId == 135328   // Рекламация
                        || optionId == 156882   // Акции
                        || optionId == 151139   // Фото планограммы
                        || optionId == 132623   // Комментарий
                        || optionId == 133382   // Потенциальный клиент
                        || optionId == 136100   // Пригласи друга
                        || optionId == 157275   // 1.
                        || optionId == 157276   // 2. Две опции контроля тут на всяк случай. Тестим.
                        || optionId == 157274   // 3. ..три
                        || optionId == 135159   // Достижения
                        || optionId == 157277   // Фото Акционного Товара
                        || optionId == 157353   // Дет отчёт исправление
                        || optionId == 138643   // Подъём товара со склада
                        || optionId == 158243   // Стикеровка
                        || optionId == 135412   // Процент премиальных
                        || optionId == 151748   // ДОЛЯ полочного пространства
                        || optionId == 158309   // "Фото Витрины" (Наближене)
                        || optionId == 158308   // "Фото Витрины" (Панорамне)
                        || optionId == 158604   // ФВ (Наполненность)
                        || optionId == 158605   // ФВ (Корпор. блок)
                        || optionId == 158606   // Дополнительное место продаж
                        || optionId == 157354   // Фото ДМП.
                        || optionId == 157242   // Причина отсутствия товара
                        || optionId == 159726   // Фото торговой точки
                        || optionId == 159706   // Инвентаризация
                        || optionId == 159725   // Кнопка "Фото Торговой Точки (ФТТ)"
                        || optionId == 159799   // Возврат
                        || optionId == 135413   // "Фото Витрины (Оценка)"
                        || optionId == 135719   // "Дет.Отчет" (оценка)
                        || optionId == 143969   // "СМС-код Клиенту" (электронный контрольный лист ЭКЛ)
                        || optionId == 160567   // Витрины
                        || optionId == 164351   // Контроль наявності світлини прикасової зони
                        || optionId == 164355   // "Фото Планограммы ТТ"
                        || optionId == 132812   // Хочу увеличение оплаты
                        || optionId == 165481   // Кнопка ЭФФИ
                        || optionId == 141069   // Кнопка "Сравнение Остатков с Наличием"
                ) {
                    optionButton.setBackgroundResource(R.drawable.bg_temp);
                    textInteger2.setVisibility(View.VISIBLE);
                    if (optionsButtons.getIsSignal().equals("1") && !optionsButtons.getBlockPns().equals("1")) {
                        textInteger2.setText(counter2Text());
                    } else {
                        if (optionId == 133382) {
                            textInteger2.setVisibility(View.VISIBLE);
                            int salary = Globals.getAverageSalary();
                            if (salary == 0)
                                salary = 15700;

                            SpannableString text = new SpannableString("+" + salary / 10 + ".0 грн.");
                            text.setSpan(new UnderlineSpan(), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            textInteger2.setText(text);
                            textInteger2.setOnClickListener(v -> {
                                DialogData dialog = new DialogData(mContext);
                                dialog.setTitle("Добавление потенциального клиента");
                                dialog.setText("Расчет за потенциального клиента \n\n" + OptionButtonAddNewClient.additionalText());
                                dialog.setClose(dialog::dismiss);
                                dialog.show();
                            });
                        } else if (optionId == 84007 && optionsButtons.getIsSignal().equals("1")) {
                            textInteger2.setText(counter2EKLText());
                        } else {
                            textInteger2.setVisibility(View.GONE);
                        }
                    }

                    if (optionId == 136100) {
                        textInteger2.setVisibility(View.VISIBLE);
                        List<BonusSDB> bonusList = SQL_DB.bonusDao().getData(null, null, (long) optionId);
                        Pair<String, Float> bonus = MainRepositoryKt.getBonusText(bonusList);
                        SpannableString text = new SpannableString("+" + bonus.getSecond() + " грн.");
                        text.setSpan(new UnderlineSpan(), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textInteger2.setText(text);
                        textInteger2.setOnClickListener(new View.OnClickListener() {
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
                        });
                    } else {
//                        textInteger2.setOnClickListener(null);
                    }

                } else {
                    describedOption = false;
                    textInteger2.setVisibility(View.GONE);
                    optionButton.setBackgroundResource(R.drawable.button_bg_inactive);
                }


                // color log
                Log.e("ColorLog", "Color.Opt.Id: " + optionsButtons.getID());
                Log.e("ColorLog", "Color.Opt.getOptionTxt: " + optionsButtons.getOptionTxt());
                Log.e("ColorLog", "Color.Opt.getOptionId: " + optionsButtons.getOptionId());
                Log.e("ColorLog", "Color.Opt.getIsSignal: " + optionsButtons.getIsSignal());


                // Определения цвета которым будет гореть СИГНАЛ
                setCheck.setColorFilter(setCheck.getContext().getResources().getColor(R.color.shadow));
                if (describedOption) {
                    setCheck.setVisibility(View.VISIBLE);
                    if (optionsButtons.getIsSignal().equals("1")) {
                        setCheck.setImageResource(R.drawable.ic_exclamation_mark_in_a_circle);
                        setCheck.setColorFilter(setCheck.getContext().getResources().getColor(R.color.red_error));
                    } else if (optionsButtons.getIsSignal().equals("2")) {
                        setCheck.setImageResource(R.drawable.ic_check);
                        setCheck.setColorFilter(setCheck.getContext().getResources().getColor(R.color.green_default));
                    } else {
                        if (optionsButtons.getOptionControlId().equals("0")) {
//                            setCheck.setVisibility(View.INVISIBLE);
                            setCheck.setImageResource(R.drawable.ic_round);
                            setCheck.setColorFilter(setCheck.getContext().getResources().getColor(R.color.shadow));
                        } else {
                            setCheck.setImageResource(R.drawable.ic_round);
                            setCheck.setColorFilter(setCheck.getContext().getResources().getColor(R.color.shadow));
                        }
                    }
                } else {
                    setCheck.setVisibility(View.INVISIBLE);
                    setCheck.setImageResource(R.drawable.ic_round);
                    setCheck.setColorFilter(setCheck.getContext().getResources().getColor(R.color.colorUnselectedTab));
                }

                WpDataDB wp = (WpDataDB) dataDB;

                // =========== СЧЁТЧИК ===========
                // У Каждой кнопки есть какое-то значение, тут я его считаю и вставляю
                // todo textInteger.setText(msg); -- могу ли выводить это нормально 1 раз?
                try {
                    // Вчтавляем "счётчкик"
                    switch (optionId) {

//                        case 84007:
//                            textInteger.setVisibility(View.VISIBLE);
//                            textInteger.setText("");
//                            break;

                        case 135159:
                            int achievementSum = 0;
                            List<AchievementsSDB> achievementsSDBList = SQL_DB.achievementsDao().getByDad2(dad2);
                            if (achievementsSDBList != null) {
                                achievementSum = achievementsSDBList.size();
                            }

                            textInteger.setText(CustomString.coloredString("" + achievementSum, optionsButtons));
                            break;

                        // Start Work
                        case (138518):
                            long startTime;
/*                        if (dataDB instanceof WpDataDB) {
                            startTime = ((WpDataDB) dataDB).getVisit_start_dt();
                        } else {
                            startTime = ((TasksAndReclamationsSDB) dataDB).dt_start_fact;
                        }*/
                            startTime = WpDataRealm.getWpDataRowByDad2Id(Long.parseLong(optionsButtons.getCodeDad2())).getVisit_start_dt();
//                            textInteger.setText(CustomString.coloredString("" + Clock.getHumanTimeOpt(startTime * 1000), optionsButtons));
                            textInteger.setText("" + Clock.getHumanTimeOpt(startTime * 1000));
                            break;
                        case (138520):
                            long endTime;
/*                        if (dataDB instanceof WpDataDB) {
                            endTime = ((WpDataDB) dataDB).getVisit_end_dt();
                        } else {
                            endTime = ((TasksAndReclamationsSDB) dataDB).dt_end_fact;
                        }*/
                            endTime = WpDataRealm.getWpDataRowByDad2Id(Long.parseLong(optionsButtons.getCodeDad2())).getVisit_end_dt();
                            textInteger.setText("" + Clock.getHumanTimeOpt(endTime * 1000));
                            break;

                        case (158309):  // Фото витрины Приближённое
//                            textInteger.setText(
//                                    setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 39)),
//                                    TextView.BufferType.SPANNABLE
//                            );

                            SpannableString spannableString158309 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 39));
                            spannableString158309.setSpan(new UnderlineSpan(), 0, spannableString158309.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            textInteger.setText(spannableString158309);

                            textInteger.setOnClickListener(view -> {
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
                            });
                            break;
                        case (158605):
//                            textInteger.setText(
//                                    setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 40)),
//                                    TextView.BufferType.SPANNABLE
//                            );
//
//                            textInteger.setOnClickListener(view -> {
//                                Intent intent = new Intent(view.getContext(), PhotoLogActivity.class);
//                                intent.putExtra("report_prepare", true);
//                                intent.putExtra("dad2", dad2);
//                                view.getContext().startActivity(intent);
//                            });
                            SpannableString spannableString158605 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 40));
                            spannableString158605.setSpan(new UnderlineSpan(), 0, spannableString158605.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            textInteger.setText(spannableString158605);

                            textInteger.setOnClickListener(view -> {
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
                            });
                            break;
                        case (158308):  // Фото витрины отдалённое

                        case (132968):  // Вставляем количество выполненных Фоток Витрин
//                            textInteger.setText(
//                                    setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 0)),
//                                    TextView.BufferType.SPANNABLE
//                            );
//
//                            textInteger.setOnClickListener(view -> {
//                                Intent intent = new Intent(view.getContext(), PhotoLogActivity.class);
//                                intent.putExtra("report_prepare", true);
//                                intent.putExtra("dad2", dad2);
//                                view.getContext().startActivity(intent);
//                            });
                            SpannableString spannableString132968 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 0));
                            spannableString132968.setSpan(new UnderlineSpan(), 0, spannableString132968.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            textInteger.setText(spannableString132968);

                            textInteger.setOnClickListener(view -> {
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
                            });
                            break;

                        case (157277):  // Вставляем количество выполненных Фото Акционного Товара
//                            textInteger.setText(
//                                    setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 28)),
//                                    TextView.BufferType.SPANNABLE
//                            );

//                            textInteger.setOnClickListener(view -> {
//                                Intent intent = new Intent(view.getContext(), PhotoLogActivity.class);
//                                intent.putExtra("report_prepare", true);
//                                intent.putExtra("dad2", dad2);
//                                view.getContext().startActivity(intent);
//                            });
                            SpannableString spannableString157277 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 26));
                            spannableString157277.setSpan(new UnderlineSpan(), 0, spannableString157277.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            textInteger.setText(spannableString157277);

                            textInteger.setOnClickListener(view -> {
                                Intent intent = new Intent(mContext, FeaturesActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                                bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_157277.toString());
                                bundle.putString("modeUI", ModeUI.DEFAULT.toString());
                                bundle.putString("dataJson", new Gson().toJson(dad2));
                                bundle.putString("title", "Перелік фото звітів");
                                bundle.putString("subTitle", "Справочник Фото" + ": " +
                                        "Акційні товари");
//                                        Objects.requireNonNullElse(ImagesTypeListRealm.getByID(26).getNm(),"Акційні товари"));
                                intent.putExtras(bundle);
                                ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                            });
                            break;

                        case (159726):  // Фото ТТ
                        case (159725):  // Кнопка "Фото Торговой Точки (ФТТ)"
                            textInteger.setText(
                                    setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 37)),
                                    TextView.BufferType.SPANNABLE
                            );

                            textInteger.setOnClickListener(view -> {
                                Intent intent = new Intent(view.getContext(), PhotoLogActivity.class);
                                intent.putExtra("report_prepare", true);
                                intent.putExtra("dad2", dad2);
                                view.getContext().startActivity(intent);
                            });
                            break;

                        case (158606):
//                            textInteger.setText(
//                                    setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 36)),
//                                    TextView.BufferType.SPANNABLE
//                            );
//
//                            textInteger.setOnClickListener(view -> {
//                                Intent intent = new Intent(view.getContext(), PhotoLogActivity.class);
//                                intent.putExtra("report_prepare", true);
//                                intent.putExtra("dad2", dad2);
//                                view.getContext().startActivity(intent);
//                            });
                            SpannableString spannableString158606 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 36));
                            spannableString158606.setSpan(new UnderlineSpan(), 0, spannableString158606.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            textInteger.setText(spannableString158606);
                            textInteger.setOnClickListener(v -> {
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
                            });
                            break;

                        case (157354):
//                            textInteger.setText(
//                                    setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 42)),
//                                    TextView.BufferType.SPANNABLE
//                            );
//
//                            textInteger.setOnClickListener(view -> {
//                                Intent intent = new Intent(view.getContext(), PhotoLogActivity.class);
//                                intent.putExtra("report_prepare", true);
//                                intent.putExtra("dad2", dad2);
//                                view.getContext().startActivity(intent);
//                            });
                            SpannableString spannableString157354 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 42));
                            spannableString157354.setSpan(new UnderlineSpan(), 0, spannableString157354.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            textInteger.setText(spannableString157354);
                            textInteger.setOnClickListener(v -> {
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
                            });
                            break;

                        case 164351:
                            textInteger.setText(
                                    setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 45)),
                                    TextView.BufferType.SPANNABLE
                            );

                            textInteger.setOnClickListener(view -> {
                                Intent intent = new Intent(view.getContext(), PhotoLogActivity.class);
                                intent.putExtra("report_prepare", true);
                                intent.putExtra("dad2", dad2);
                                view.getContext().startActivity(intent);
                            });
                            break;

                        case 151139:
                        case 164355:

                            SpannableString spannableString164355 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 5));
                            spannableString164355.setSpan(new UnderlineSpan(), 0, spannableString164355.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


//                            textInteger.setText(
//                                    setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 5)),
//                                    TextView.BufferType.SPANNABLE
//                            );
                            textInteger.setText(spannableString164355);

//                            textInteger.setOnClickListener(v -> {
//                                Intent intent = new Intent(mContext, FeaturesActivity.class);
//                                Bundle bundle = new Bundle();
//                                bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
//                                bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_164355.toString());
//                                bundle.putString("modeUI", ModeUI.DEFAULT.toString());
//                                bundle.putString("dataJson", new Gson().toJson(dad2));
//                                bundle.putString("title", "Перелік фото звітів");
//                                bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(5).getNm());
//                                intent.putExtras(bundle);
//                                ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
//                            });
                            textInteger.setOnClickListener(view -> {
                                Intent intent = new Intent(view.getContext(), PhotoLogActivity.class);
                                intent.putExtra("report_prepare", true);
                                intent.putExtra("dad2", dad2);
                                view.getContext().startActivity(intent);
                            });

                            break;

                        case (158604):
//                            textInteger.setText(
//                                    setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 41)),
//                                    TextView.BufferType.SPANNABLE
//                            );
                            SpannableString spannableString158604 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 41));
                            spannableString158604.setSpan(new UnderlineSpan(), 0, spannableString158604.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            textInteger.setText(spannableString158604);
                            textInteger.setOnClickListener(v -> {
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
                            });

//                            textInteger.setOnClickListener(view -> {
//                                Intent intent = new Intent(view.getContext(), PhotoLogActivity.class);
//                                intent.putExtra("report_prepare", true);
//                                intent.putExtra("dad2", dad2);
//                                view.getContext().startActivity(intent);
//                            });
                            break;

                        case (135809):  // Вставляем количество выполненных Фото витрины ДО начала работ
//                        textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(dad2, 14));
                            SpannableString spannableString = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 14));
                            spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            textInteger.setText(spannableString);
                            textInteger.setOnClickListener(v -> {
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
                            });
//                            textInteger.setText(
//                                    setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 14)),
//                                    TextView.BufferType.SPANNABLE
//                            );

                            break;
                        case (135158):  // Вставляем количество выполненных Фото Остатков Товаров (ФОТ)
//                            textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(dad2, 4));
                            SpannableString spannableString135158 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 4));
                            spannableString135158.setSpan(new UnderlineSpan(), 0, spannableString135158.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            textInteger.setText(spannableString135158);
//                            textInteger.setText(CustomString.underlineString("" + RealmManager.stackPhotoShowcasePhotoCount(dad2, 4), optionsButtons));
                            textInteger.setOnClickListener(v -> {
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
                            });

                            break;
                        case (132969):  // Вставляем количество выполненных Фото Тележка с Товаром (ФТТ)

                            SpannableString spannableString132969 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 10));
                            spannableString132969.setSpan(new UnderlineSpan(), 0, spannableString132969.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            textInteger.setText(spannableString132969);
//                            textInteger.setText(CustomString.underlineString("" + RealmManager.stackPhotoShowcasePhotoCount(dad2, 10), optionsButtons));
                            textInteger.setOnClickListener(v -> {
                                Intent intent = new Intent(mContext, FeaturesActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("viewModel", StackPhotoDBViewModel.class.getCanonicalName());
                                bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_132969.toString());
                                bundle.putString("modeUI", ModeUI.DEFAULT.toString());
//                                JsonObject dataJson = new JsonObject();
//                                dataJson.addProperty("tradeMarkDBId", tradeMarkId);
//                                dataJson.addProperty("wpDataDBId", String.valueOf(wpDataDB.getId()));
//                                dataJson.addProperty("optionDBId", optionId);
//                                bundle.putString("dataJson", new Gson().toJson(dataJson));
                                bundle.putString("dataJson", new Gson().toJson(dad2));
                                bundle.putString("title", "Перелік фото звітів");
                                bundle.putString("subTitle", "Справочник Фото" + ": " + ImagesTypeListRealm.getByID(10).getNm());
                                intent.putExtras(bundle);
                                ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                            });

                            break;

                        case (141360):
                            SpannableString spannableString141360 = setPhotoCountsMakeAndMust(optionsButtons, RealmManager.stackPhotoShowcasePhotoCount(dad2, 31));
                            spannableString141360.setSpan(new UnderlineSpan(), 0, spannableString141360.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            textInteger.setText(spannableString141360);

//                            textInteger.setText(CustomString.underlineString("" + RealmManager.stackPhotoShowcasePhotoCount(dad2, 31), optionsButtons));
                            textInteger.setOnClickListener(v -> {
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
                            });

                            break;

                        case 137797:    // Остатки
                            String msg = String.format("%s/%s/%s", (int) DetailedReportActivity.SKUPlan, (int) DetailedReportActivity.SKUFact, (int) DetailedReportActivity.OFS);
                            textInteger.setText(msg);
                            break;

                        case 141910:    // "Получение заказа в ТТ"
                            String counter141910 = DetailedReportActivity.rpAmountSum + " шт";
                            textInteger.setText(counter141910);
                            break;

                        case 141888:    // "Выкуп Товара с ТТ"
                            String counter141888 = DetailedReportActivity.rpTotalSumToRedemptionOfGoods + "грн";
                            textInteger.setText(counter141888);
                            break;

                        case 141885:    // Фото Документов
                            textInteger.setText("" + RealmManager.stackPhotoShowcasePhotoCount(dad2, 3));
                            break;

                        case 138339:    // Доп Требования
                            // Устанавливаю в счётчик доп. требований их количество
                            Integer ttCategory = null;
                            AddressSDB addressSDB = SQL_DB.addressDao().getById(wp.getAddr_id());
                            if (addressSDB != null) {
                                ttCategory = addressSDB.ttId;
                            }
                            textInteger.setText(CustomString.coloredString("" + AdditionalRequirementsRealm.getData3(dataDB, HIDE_FOR_USER, ttCategory, null, 1).size(), optionsButtons));
                            break;

                        case 138340:    // Доп Требования
                            // Устанавливаю в счётчик доп. требований их количество
//                        String expire = Clock.getHumanTimeYYYYMMDD(System.currentTimeMillis() / 1000);
//                        textInteger.setText("" + SQL_DB.additionalMaterialsDao().getAllForOptionTEST(optionsButtons.getClientId(), Integer.parseInt(optionsButtons.getAddrId()), "1", "0").size());
//                        textInteger.setText("" + SQL_DB.additionalMaterialsDao().getAllForOptionTEST(optionsButtons.getClientId(), Integer.parseInt(optionsButtons.getAddrId()), "0").size());
                            textInteger.setText(CustomString.coloredString("" + SQL_DB.additionalMaterialsDao().getAllForOptionTEST2(optionsButtons.getClientId(), "0").size(), optionsButtons));
                            break;

                        case 135328:    // Рекламация
                            OptionMassageType type = new OptionMassageType();
                            type.type = OptionMassageType.Type.STRING;
                            OptionControlReclamationAnswer<?> optionControlReclamationAnswer = new OptionControlReclamationAnswer<>(itemView.getContext(), dataDB, optionsButtons, type, NULL, null);

                            textInteger.setText(CustomString.coloredString("" + optionControlReclamationAnswer.problemReclamationCount(), optionsButtons));
                            break;

                        case 135327:    // Задачи
                            type = new OptionMassageType();
                            type.type = OptionMassageType.Type.STRING;
                            OptionControlTaskAnswer<?> optionControlTask = new OptionControlTaskAnswer<>(itemView.getContext(), dataDB, optionsButtons, type, NULL, null);

                            textInteger.setText(CustomString.coloredString("" + optionControlTask.problemTaskCount(), optionsButtons));

                            break;


                        case 141069:    // Сравнение остатков и наличия
                            type = new OptionMassageType();
                            type.type = OptionMassageType.Type.STRING;
                            OptionControlStockBalanceTovar<?> optionControlStockBalanceTovar = new OptionControlStockBalanceTovar<>(itemView.getContext(), dataDB, optionsButtons, type, NULL, null);

                            textInteger.setText(CustomString.underlineString(optionControlStockBalanceTovar.currentStockBalanceCount(), optionsButtons));
                            textInteger.setOnClickListener(v -> {
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
                                }
                                bundle.putString("title", "Товари");
                                bundle.putString("subTitle", "Перечень товаров к текущему посещению");
                                intent.putExtras(bundle);
                                ActivityCompat.startActivityForResult((Activity) mContext, intent, NEED_UPDATE_UI_REQUEST, null);
                            });
                            break;

                        default:
                            textInteger.setVisibility(View.GONE);
//                        textInteger.setText(optionsButtons.getPrice());
                    }
                } catch (Exception e) {
                    // TODO Вставить обработчик
                    e.printStackTrace();
                }


                // Работа с кнопками и нажатиями на них
                final DetailedReportButtons detailedReportButtons = new DetailedReportButtons();

                // Функционал Опций (контроль, NNK..)
                final Options options = new Options();


                // Кликаем по кнопке Опции
                boolean finalDescribedOption = describedOption;
                optionButton.setOnClickListener(view -> {
                    Log.e("notifyItemChanged", "CLICK");
//                animate();

                    // Если эта кнопка НЕ активная - значит она находится в разработке
                    if (finalDescribedOption) {

                        // Обработка нажатия на кнопку
                        OptionMassageType msgType = new OptionMassageType();
                        msgType.type = OptionMassageType.Type.DIALOG;
                        options.setOptionFromDetailedReport(allReportOption);
                        msgType = options.NNK(view, mContext, dataDB, optionsButtons, butt, msgType, Options.NNKMode.MAKE, () -> {
                            try {
                                int test1 = getAdapterPosition();
                                int test2 = getBindingAdapterPosition();
                                int test3 = getAbsoluteAdapterPosition();

                                Log.e("NNK", "test1: " + test1);
                                Log.e("NNK", "test2: " + test2);
                                Log.e("NNK", "test3: " + test3);

                                if (dataDB instanceof WpDataDB) {
                                    detailedReportButtons.buttonClick(
                                            mContext,
                                            (WpDataDB) dataDB,
                                            butt.get(getBindingAdapterPosition()),
                                            0);
                                    setCheck(POS, optionsButtons, NULL);
                                }
                            } catch (Exception e) {

                            }

                            notifyDataSetChanged();
                        });

                        // todo Определить нафиг это сделано
                        // Это нужно для старого отображения ошибки контроля опции
                        if (msgType != null && msgType.dialog != null) {
                            msgType.dialog.setDialogIco();
                            msgType.dialog.show();
                        }

                        // todo Определить нафиг это сделано
                        // Это нужно для старого отображения ошибки контроля опции
                        if (msgType != null && msgType.msg != null && !msgType.msg.equals("")) {
                            Toast.makeText(mContext, msgType.msg, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(view.getContext(), "Данная Опция находится в РАЗРАБОТКЕ", Toast.LENGTH_SHORT).show();
                    }

                });


                // ДОЛГИЙ клик по Кнопке Опции
                OptionsDB test = optionsButtons;
                optionButton.setOnLongClickListener(view -> {
                    if (optionId == 132968 || optionId == 158309 || optionId == 158308) {
                        optionDetailPhotos(test, view.getContext());
                        DialogData dialog = new DialogData(itemView.getContext());
                        dialog.setTitle("Внесите пароль!");
                        dialog.setText("Для продолжения внесите пароль: ");
                        dialog.setClose(dialog::dismiss);
                        dialog.setOperation(DialogData.Operations.TEXT, "", null, () -> {
                        });
                        dialog.setOk("Ok", () -> {
                            Toast.makeText(dialog.context, "Внесли: " + dialog.getOperationResult(), Toast.LENGTH_SHORT).show();

//                            int res = Integer.parseInt(dialog.getOperationResult());
                            String res = dialog.getOperationResult();

                        /* old
                        Calendar calendar = Calendar.getInstance();
                        int day = calendar.get(Calendar.DAY_OF_WEEK);
                        int dat2 = calendar.get(Calendar.DAY_OF_MONTH);
                        int pass = day + dat2;*/

                            // new
//                            Calendar calendar = Calendar.getInstance();
//                            int year = calendar.get(Calendar.YEAR);
//                            int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
//                            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
//                            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
//
//                            double passwordD = (double) year / (dayOfYear + dayOfWeek + dayOfMonth);
//
//                            int pass = Integer.parseInt(String.format("%03d", (int) (passwordD * 100)));
                            String pass = CodeGenerator.getCode();

                            if (res.equals(pass)) {
                                longClickButton(test, optionId, detailedReportButtons, optionsButtons, view.getContext());
                            } else {
                                Toast.makeText(dialog.context, "Внесите корректный пароль", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.show();
                    } else {
                        int optId = Integer.parseInt(butt.get(getAdapterPosition()).getOptionId());
                        longClickButton(test, optId, detailedReportButtons, optionsButtons, view.getContext());
                    }
                    return false;
                });


                // Нажатие на СИГНАЛ Кнопки Опции.
                setCheck.setOnClickListener(v -> {
                    Toast.makeText(v.getContext(), "Проверка статуса данной опции", Toast.LENGTH_SHORT).show();
                    setCheck(POS, optionsButtons, CHECK_CLICK);
                });
            } catch (Exception e) {
                Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/bind", "Exception e: " + e);
                Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/bind", "Exception exception: " + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private CharSequence counter2Text() {
        CharSequence res = "";
        if (dataDB instanceof WpDataDB) {
            WpDataDB wpDataDB = (WpDataDB) dataDB;
            res = "~" + String.format("%.2f", wpDataDB.getCash_zakaz() * 0.08);
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
                float shtraf = 0.32f;
                if (users.last_ekl_date != null) {
                    long ekl_date = convertDateToSeconds(users.last_ekl_date);
                    long countDay = startDt - (DAYS * 24 * 60 * 60);
                    if (ekl_date != -1 && ekl_date > countDay)
                        shtraf = 0.16f;
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
            // Парсим строку в объект Date
            Date date = dateFormat.parse(dateString);
            // Преобразуем Date в миллисекунды и делим на 1000, чтобы получить секунды
            return date.getTime() / 1000;
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // В случае ошибки возвращаем -1
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

    /*Нажатие на проверку статуса опции. Нажатие на сигнал*/
    private void setCheck(int POS, OptionsDB optionsButtons, Options.NNKMode mode) {
        Options options = new Options();
        options.optionControl(mContext, dataDB, optionsButtons, null, mode, new OptionControl.UnlockCodeResultListener() {
            @Override
            public void onUnlockCodeSuccess() {

            }

            @Override
            public void onUnlockCodeFailure() {

            }
        });

        RealmManager.INSTANCE.executeTransaction(realm -> {
            realm.insertOrUpdate(optionsButtons);
        });
        updateSignal(POS);
    }

    private void updateSignal(int position) {
        if (dataDB instanceof WpDataDB) {
            WpDataDB wpDataDB = (WpDataDB) dataDB;
            WpDataDB wp = RealmManager.INSTANCE.copyFromRealm(RealmManager.getWorkPlanRowById(wpDataDB.getId()));
            dataDB = (T) wp;
            notifyItemChanged(position);
        }
    }

    public void setDataButtons(List<OptionsDB> butt) {
        this.butt = butt;
        notifyDataSetChanged();
    }

    /*Определяем конструктор*/
    public RecycleViewDRAdapter(Context context, T dataDB, List<OptionsDB> dataButtons, List<OptionsDB> allReportOption, List<SiteObjectsSDB> list, Clicks.click click) {
//        try {
        this.click = click;
        this.dataDB = dataDB;
        this.butt = dataButtons;
        this.translate = list;
        this.mContext = context;
        this.allReportOption = allReportOption;

        if (dataDB instanceof WpDataDB) {
            WpDataDB wp = (WpDataDB) dataDB;
            dad2 = wp.getCode_dad2();
            startDt = wp.getVisit_start_dt();
            endDt = wp.getVisit_end_dt();
        } else {
            TasksAndReclamationsSDB tar = (TasksAndReclamationsSDB) dataDB;
            dad2 = tar.codeDad2SrcDoc;
            startDt = tar.dt_start_fact;
            endDt = tar.dt_end_fact;
        }
//        } catch (Exception e) {
//            Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/RecycleViewDRAdapter", "Exception e: " + e);
//            Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/RecycleViewDRAdapter", "Exception exception: " + Arrays.toString(e.getStackTrace()));
//        }
    }

    @Override
    public RecycleViewDRAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dr_option_item_button, parent, false);
        Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/onCreateViewHolder", "View v: " + v);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(RecycleViewDRAdapter.ViewHolder viewHolder, int position) {
        try {
            OptionsDB optionsButtons = butt.get(position);
            SiteObjectsSDB siteObjectsSDB = null;
            if (translate != null && translate.size() > 0) {
                for (SiteObjectsSDB item : translate) {

                    Log.e("R_TRANSLATES", "onBindViewHolder.optionsButtons.getOptionId(): " + optionsButtons.getOptionId());
                    Log.e("R_TRANSLATES", "onBindViewHolder.item.additionalId: " + item.additionalId);

                    if (optionsButtons.getOptionId().equals(String.valueOf(item.additionalId))) {
                        siteObjectsSDB = item;
                        break;
                    }
                }
            }

            if (siteObjectsSDB != null) {
                Log.e("R_TRANSLATES", "onBindViewHolder: " + siteObjectsSDB.id);
            } else {
                Log.e("R_TRANSLATES", "onBindViewHolder: " + siteObjectsSDB);
            }

            viewHolder.bind(optionsButtons, siteObjectsSDB);
        } catch (Exception e) {
            Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/onBindViewHolder", "Exception e: " + e);
            Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/onBindViewHolder", "Exception exception: " + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public int getItemCount() {
        try {
            return butt.size();
        } catch (Exception e) {
            Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/getItemCount", "Exception e: " + e);
            Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/getItemCount", "Exception exception: " + Arrays.toString(e.getStackTrace()));
        }
        return 0;
    }

    public int getItemPosition(OptionsDB item) {
        try {
            return butt.indexOf(item);
        } catch (Exception e) {
            Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/getItemPosition", "Exception e: " + e);
            Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/getItemPosition", "Exception exception: " + Arrays.toString(e.getStackTrace()));
        }
        return 0;
    }


    /*Активные на данный момент кнопки*/
    private void setEnabledButtons(Button button, OptionsButtons optionsButtons) {
        int optId = optionsButtons.getOptionId();
        if (optId != 135809 && optId != 132968 && optId != 135158 && optId != 132969) {
            button.setEnabled(false);
            button.setVisibility(View.GONE);
        } else {
            button.setEnabled(true);
        }
    }


    private void optionDetailPhotos(OptionsDB option, Context context) {
        String additionalText = "\n\n";

        String buttText = option.getOptionTxt();
        buttText = buttText.replace("&quot;", "");
        buttText = buttText.replace("Кнопка ", "");

        /*
         * 132968 - 0  - фото витрины Панорамне
         * 135809 - 14 - Фото витрины До начала работ
         * 135158 - 4  - Фото остатков товаров
         * 132969 - 10 - Фото тележка с товаром
         * 141360 - 31 - Фото товара на складе
         * 141885 - 3  - Фото Документов
         * 158605 - 40 - Корпоративный блок
         * 157354 - 42 - Фото ДМП
         * 158606 - 36 - доп. место продажи
         * 158604 - 41 - Наполненность
         * 157277 - 28 - Фото Акционного Товара
         * 159726 - 37 - Фото ТТ
         * 159725 - 37 - Кнопка "Фото Торговой Точки (ФТТ)"
         * 133382 - 25 - Товар потенциального клиента
         * */
        int photoType = 0;
        boolean showPhotoLink = false;
        switch (option.getOptionId()) {
            case "158308":
            case "132968":  // - 0  - фото витрины
                photoType = 0;
                showPhotoLink = true;
                break;

            case "141885":  // - 3  - Фото Документов
                photoType = 3;
                showPhotoLink = true;
                break;

            case "135158":  // - 4  - Фото остатков товаров
                photoType = 4;
                showPhotoLink = true;
                break;

            case "151139":  // - 5  - Фото планограммы
                photoType = 5;
                showPhotoLink = true;
                break;

            case "132969":  // - 10 - Фото тележка с товаром
                photoType = 10;
                showPhotoLink = true;
                break;

            case "135809":  // - 14 - Фото витрины До начала работ
                photoType = 14;
                showPhotoLink = true;
                break;

            case "157277":  // - 28 - Фото Акционного Товара
                photoType = 28;
                showPhotoLink = true;
                break;

            case "141360":  // - 31 - Фото товара на складе
                photoType = 31;
                showPhotoLink = true;
                break;

            case "158606":  //- 36 - доп. место продажи
                photoType = 36;
                showPhotoLink = true;
                break;

            case "158309":  // - 39 - "Фото Витрины" (Наближене)
                photoType = 39;
                showPhotoLink = true;
                break;

            case "159726":  // - 37 - Фото ТТ
            case "159725":  // - 37 - Кнопка "Фото Торговой Точки (ФТТ)"
                photoType = 37;
                showPhotoLink = true;
                break;

            case "158605":  // - 40 - Корпоративный блок
                photoType = 40;
                showPhotoLink = true;
                break;

            case "158604": //- 41 - Наполненность
                photoType = 41;
                showPhotoLink = true;
                break;

            case "157354":  // - 42 - Фото ДМП
                photoType = 42;
                showPhotoLink = true;
                break;

            case "133382":  // - 25 - Фото товара потенциального клиента
                photoType = 25;
                showPhotoLink = true;
                break;
        }


        SpannableStringBuilder ss = new SpannableStringBuilder();
        ss.append(option.getOptionDescr());
        ss.append(additionalText);
        if (showPhotoLink) {
            ss.append("\n\n");
            switch (option.getOptionId()) {
                case "135158":  // - 4  - Фото остатков товаров
                    ss.append(createLinkedStringGal(mContext, "Завантажити фото з галереї", photoType, () -> {
//                        click.click();    // Инициализируем открытие Галереи для выбора фото Товара

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

                case "151139":
                    ss.append(createLinkedStringGal(mContext, "Завантажити фото з галереї", photoType, () -> {
                        click.click(5);
                    }));
                    ss.append("\n\n");
                    break;
            }
            ss.append(createLinkedString(mContext, "Показать образец фото", photoType));
            ss.append("\n");
        }

        DialogData dialog = new DialogData(mContext);
        dialog.setTitle(buttText);
        dialog.setText(ss, () -> {
        });
        dialog.setMerchikIco(mContext);
        dialog.show();
    }

    /*Дополнительная подсказка: Потенциальный клиент*/
    private String additionalText133382() {
        int test = 11000;   // TODO будут мне норм значение передавать "ЗП мерчика"
        String res;

        double requestSend = test * 0.0005;
        double requestRegistered = test * 0.005;
        double presentation = test * 0.01;
        double clientStart = test * 0.1;


        res = String.format("- За регистрацию потенциального клиента (ПК) начисляются следующие премии:\n" +
                "1. При подаче заявки: %s грн.\n" +
                "2. Если и менеджер (после проверки реквизитов) регистрирует этого ПК в нашей базе данных: %s грн.\n" +
                "3. Если для этого ПК проводится презентация: %s грн.\n" +
                "4. Основная премия, при \"запуске\" клиента в работу: %s грн.", requestSend, requestRegistered, presentation, clientStart);

        return res;
    }

    private SpannableString createLinkedString(Context context, String msg, int photoType) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                try {
                    WpDataDB wp = (WpDataDB) dataDB;
                    AddressSDB addressSDB = SQL_DB.addressDao().getById(wp.getAddr_id());

                    List<SamplePhotoSDB> samplePhotoSDBList = SQL_DB.samplePhotoDao().getPhotoLogActiveAndTp(1, photoType, addressSDB.tpId);
                    if (samplePhotoSDBList != null && samplePhotoSDBList.size() > 1) {
                        Intent intent = new Intent(context, PhotoLogActivity.class);
                        intent.putExtra("SamplePhoto", true);
                        intent.putExtra("SamplePhotoActivity", false);
                        intent.putExtra("photoTp", photoType);
                        intent.putExtra("grpId", addressSDB.tpId);
                        context.startActivity(intent);
                    } else if (samplePhotoSDBList != null && samplePhotoSDBList.size() == 1) {
                        // Pika
                        // Если фото образца одно, то тут должен отобразить фото на весь экран и на этом все...
                        try {
                            Globals.writeToMLOG("INFO", "Не могу найти образцы фото SOLO ", "samplePhotoSDBList: " + new Gson().toJson(samplePhotoSDBList));
                            StackPhotoDB photo = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(String.valueOf(samplePhotoSDBList.get(0).photoId));
                            Globals.writeToMLOG("INFO", "Не могу найти образцы фото SOLO ", "photo: " + new Gson().toJson(photo));
                            DialogFullPhotoR dialog = new DialogFullPhotoR(context);
                            dialog.setPhoto(photo);
                            dialog.commentOn = true;
                            // Pika
                            // сделал универсальнее - если в поле "about" есть текст к образцу фото - то вывожу его,
                            // а если нет, то пробую взять из комментов для самих фото по этому фото
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
                        // тут Toast работает но пользователю нгепонятны тексты ошибок Джавы, и Петров сказал, сюда нужно внятное
                        // пользователю сообщение (универсвльное) из глоб модуля, типа "не получается показать фото попробуйте
                        // выполнить синхронизацию и пробуйте снова..."
                        Toast.makeText(context, "Не могу найти образцы фото. " + context.getText(R.string.msg_try_sync), Toast.LENGTH_SHORT).show();
                        Globals.writeToMLOG("ERROR", "Не могу найти образцы фото", "");
                    }
                } catch (Exception e) {
                    // тут Toast работает но пользователю нгепонятны тексты ошибок Джавы, и Петров сказал, сюда нужно внятное
                    // пользователю сообщение (универсвльное) из глоб модуля, типа "не получается показать фото попробуйте
                    // выполнить синхронизацию и пробуйте снова..."
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

    private SpannableString createLinkedStringGal(Context context, String msg, int photoType, Clicks.clickVoid click) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                // Буду открывать и сохранять с галереи мусорку
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


    /**
     * 21.02.23.
     * Отображение кол-ва фоток которые были сделаны и нужно сделать по каждому типу фото.
     */
    private SpannableString setPhotoCountsMakeAndMust(OptionsDB option, int dataBaseCount) {
        SpannableString res = new SpannableString("");
        String data = "";

        String isSignal = option.getIsSignal();

        Log.e("setPhotoCountsMakeAndM", "OptionId: " + option.getOptionId() + " | OptionControlId: " + option.getOptionControlId());
        Log.e("setPhotoCountsMakeAndM", "isSignal: " + option.getIsSignal());
        Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/setPhotoCountsMakeAndMust", "OptionId: " + option.getOptionId() + " | OptionControlId: " + option.getOptionControlId());
        // Если не указано минимальное кол-во фоток у опции, считаем что фоток надо зделать 3шт.
        String min = option.getAmountMin();
        if (min.equals("0")) {
            min = "3";

            if (option.getOptionId().equals("141360")
//                    || option.getOptionId().equals("164355")
                    || option.getOptionId().equals("151139")
                    || option.getOptionId().equals("164351")
                    || option.getOptionControlId().equals("164351")
            ) min = "1";
            else if (option.getOptionId().equals("164355"))
                min = "0";
            else if (option.getOptionId().equals("132969"))
                min = "1";
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
            } catch (Exception e) {
                Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/setPhotoCountsMakeAndMust", "Exception: " + e.getMessage());
            }
        }


        int maxPhotos = Integer.parseInt(min);

        data = "" + dataBaseCount + "/" + maxPhotos;

        res = new SpannableString(data);

        ForegroundColorSpan foregroundSpan = switch (isSignal) {
            case "0" -> new ForegroundColorSpan(Color.GRAY);
            case "1" ->
                    new ForegroundColorSpan(mContext.getResources().getColor(R.color.red_error));
            case "2" ->
                    new ForegroundColorSpan(mContext.getResources().getColor(R.color.green_default));
            default -> new ForegroundColorSpan(Color.YELLOW);
        };
        //        if (dataBaseCount >= maxPhotos) {
//            foregroundSpan = new ForegroundColorSpan(Color.GREEN);
//        } else {
//            foregroundSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.red_error));
//        }

        res.setSpan(foregroundSpan, 0, res.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Globals.writeToMLOG("INFO", "RecycleViewDRAdapter/setPhotoCountsMakeAndMust", "Result: " + data);

        return res;
    }

}
