package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

public class OptionControlTwoWorkInOneDay<T> extends OptionControl {
    public int OPTION_CONTROL_TwoWorkInOneDay_ID = 166896;

    private static final int TEMA_998 = 998;
    // 1285 = факт ИСПОЛЬЗОВАНИЯ/наличия кода разблокировки (как в 1С)
    private static final int THEME_UNLOCK_USED = 1285;

    public boolean signal = true;

    private Date date;
    private Date oneDayBefore;
    private Date oneDayAfter;
    private int userId;
    private long codeDad2;

    public OptionControlTwoWorkInOneDay(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        try {
            this.context = context;
            this.document = document;
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;
            this.unlockCodeResultListener = unlockCodeResultListener;

            getDocumentVar();
            executeOption();
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlEndAnotherWork/OptionControlEndAnotherWork", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        try {
            if (document instanceof WpDataDB) {
                wpDataDB = (WpDataDB) document;

                date = wpDataDB.getDt();
                userId = wpDataDB.getUser_id();
                codeDad2 = wpDataDB.getCode_dad2();

                // Создаем объект Calendar для работы с датами
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                // 09.05.25 исправил на 3 дня
                calendar.add(Calendar.DAY_OF_MONTH, -5);
                oneDayBefore = calendar.getTime();

                // 09.05.25 исправил на 3 дня
                calendar.setTime(date);
                calendar.add(Calendar.DAY_OF_MONTH, 5);
                oneDayAfter = calendar.getTime();
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlEndAnotherWork/getDocumentVar", "Exception e: " + e);
        }
    }

    private void executeOption() {
        // =========================
        // 2.0. переменные
        // =========================
        int theme = wpDataDB != null ? wpDataDB.getTheme_id() : TEMA_998;
        int fot   = wpDataDB != null ? wpDataDB.getFot_user_id() : 0;
        int dot   = wpDataDB != null ? wpDataDB.getDot_user_id() : 0;

        spannableStringBuilder.clear();

        // Текущий старт (СЕКУНДЫ)
        long currentStartSec = wpDataDB != null ? wpDataDB.getVisit_start_dt() : 0L;

        // День текущего визита (по факту старта)
        LocalDate day = unixSecondsToLocalDateSafe(currentStartSec, wpDataDB != null ? wpDataDB.getDt() : null);

//        if (day == null || currentStartSec <= 0) {
//            // В 1С в новом варианте этого кейса нет, но в приложении лучше не падать.
//            massageToUser = "Немає данних для аналізу (не можу визначити час початку робіт).";
//
//            // Если хочешь — можешь оставить старые исключения, но имей в виду:
//            // в новом 1С они не фигурируют.
//            if (fot != 0 || dot != 0) {
//                massageToUser += " Зроблено виключення для випадку ДОТ/ФОТ.\n";
//                signal = false;
//            } else if (theme != TEMA_998) {
//                massageToUser += " Роботи виконувались по Темі " + theme + ". Зроблено виключення.\n";
//                signal = false;
//            } else {
//                signal = true;
//            }
//
//            spannableStringBuilder.append(massageToUser);
//            RealmManager.INSTANCE.executeTransaction(realm -> {
//                if (optionDB != null) {
//                    optionDB.setIsSignal(signal ? "1" : "2");
//                    realm.insertOrUpdate(optionDB);
//                }
//            });
//
//            setIsBlockOption(signal);
//            checkUnlockCode(optionDB);
//
//            Log.d("test", "massageToUser: " + massageToUser);
//            return;
//        }

        ZoneId zone = ZoneId.systemDefault();
        long dayStartSec = day.atStartOfDay(zone).toEpochSecond();
        long dayEndSec   = day.plusDays(1).atStartOfDay(zone).toEpochSecond() - 1;

        String vfn = unixSecondsToDdMmHm(currentStartSec); // аналог ВФН

        // =========================
        // 3.0. ищем другие визиты в этот же день по тому же ИЗА
        // =========================
        RealmResults<WpDataDB> wp = WpDataRealm.getWpData().where()
                .equalTo("user_id", userId)
                .equalTo("code_iza", wpDataDB.getCode_iza())
                .greaterThanOrEqualTo("visit_start_dt", dayStartSec)
                .lessThanOrEqualTo("visit_start_dt", dayEndSec)
                .notEqualTo("code_dad2", codeDad2)      // <-- исключаем текущий визит (аналог КодДАД в 1С)
                .findAll();

        List<WpDataDB> otherSameDay = RealmManager.INSTANCE.copyFromRealm(wp);

        // =========================
        // 4.0. сообщение и сигнал
        // =========================
        if (otherSameDay.isEmpty()) {
            massageToUser = "Під час поточного відвідування, що фактично було почато " + vfn +
                    ", виконавець здійснював роботи за ОДИН візит. Зауважень нема.\n";
            spannableStringBuilder.append(massageToUser);
            signal = false;
        } else {
            // В 1С берут первую строку (ПолучитьСтроку()) и пишут про нее
            WpDataDB first = otherSameDay.get(0);

            String otherStart = unixSecondsToDdMmHm(first.getVisit_start_dt());
            String otherDad2 = String.valueOf(first.getCode_dad2()); // если String — подставь как есть

            massageToUser = "При спробі виконання поточного відвідування, виявлено що виконавець (у поточному дні) " +
                    "вже виконував роботи за іншим візитом по цьому ж клієнту: " + otherDad2 +
                    " котрі почав " + otherStart + ". Це заборонено!\n";
            signal = true;
            spannableStringBuilder.append(massageToUser);

            // Детализация списком (удобно для пользователя)
            for (WpDataDB item : otherSameDay) {
                String address = item.getAddr_txt();
                if (address == null || address.isEmpty()) address = "адреса не визначена";

                String start = unixSecondsToDdMmHm(item.getVisit_start_dt());

                spannableStringBuilder
                        .append("Дубль-візит: ")
                        .append(createLinkedString(
                                start + ", " + address + ", " + item.getClient_txt() +
                                        "\n(перейдіть до цього візиту)", item))
                        .append("\n\n");
            }
        }

        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (optionDB != null) {
                if (signal) {
                    optionDB.setIsSignal("1");
                } else {
                    optionDB.setIsSignal("2");
                }
                realm.insertOrUpdate(optionDB);
            }
        });

        setIsBlockOption(signal);    // Установка блокирует ли опция работу приложения или нет
        checkUnlockCode(optionDB);

        Log.d("test", "spannableStringBuilder: " + spannableStringBuilder);
        Log.d("test", "massageToUser: " + massageToUser);
    }


    // =========================
// helpers
// =========================
    private static LocalDate unixSecondsToLocalDateSafe(long unixSeconds, Date fallbackDate) {
        try {
            if (unixSeconds > 0) {
                return Instant.ofEpochSecond(unixSeconds)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }
            if (fallbackDate != null) {
                return fallbackDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String unixSecondsToDdMmHm(long unixSeconds) {
        if (unixSeconds <= 0) return "";
        return Instant.ofEpochSecond(unixSeconds)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd.MM HH:mm"));
    }



    private void executeOption2() {
        // 2.0
        // ---  Получаем данные с БД для подальшей обработке
        RealmResults<WpDataDB> wp = WpDataRealm.getWpData();
        wp = wp.where()
                .greaterThanOrEqualTo("dt", oneDayBefore)
                .lessThanOrEqualTo("dt", oneDayAfter)
                .equalTo("user_id", userId)
                .notEqualTo("code_dad2", codeDad2)
                .findAll();
        List<WpDataDB> wpDataDB = RealmManager.INSTANCE.copyFromRealm(wp);

        // ---  Создаём место куда будем писать ошибки
        List<WpDataDB> result = new ArrayList<>();

        // ---  В цикле пробегаем по всему отобранному Плану работ
        for (WpDataDB item : wpDataDB) {
            if (item.getVisit_start_dt() > 0 && item.getVisit_end_dt() == 0) {
                result.add(item);
                Globals.writeToMLOG("INFO", "OptionControlEndAnotherWork/executeOption", "WpDataDB dad2: " + item.getCode_dad2());

                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                String formattedDate = formatter.format(item.getDt());
                String address = item.getAddr_txt();
                if (address == null || address.isEmpty())
                    address = "адреса не визначена";
                // Dialog massage
                spannableStringBuilder
                        .append("Вы еще не закончили (не указали время окончания) ПРЕДЫДУЩЕЙ работы!")
                        .append("\n")
                        .append(createLinkedString(
                                "Перейдіть до цього візиту:\n" +
                                        formattedDate + ", " + address + ", " + item.getClient_txt() +
                                        " та натисніть копку 'Закінчення роботи' або введіть код розблокування", item))
                        .append("\n");
            }
        }

        // ---  Отображаем ОБЫЧНОЕ сообщение. Не развёрнутое.
        if (wpDataDB.size() == 0) {
            massageToUser = "Нет данных для анализа окончания ПРЕДЫДУЩИХ работ.";
            signal = false;
//            unlockCodeResultListener.onUnlockCodeFailure();
//            unlockCodeResultListener.onUnlockCodeSuccess();
        } else if (result.size() == 0) {
            massageToUser = "Замечаний по указанию времени начала/окончания ПРЕДЫДУЩИХ работ нет.";
            signal = false;
//            unlockCodeResultListener.onUnlockCodeFailure();
//            unlockCodeResultListener.onUnlockCodeSuccess();
        } else {
            massageToUser = "Вы еще не закончили (не указали время окончания) ПРЕДЫДУЩУЮ работу!";
            signal = true;
//            unlockCodeResultListener.onUnlockCodeSuccess();
//            unlockCodeResultListener.onUnlockCodeFailure();
        }


        //6.0.
        setIsBlockOption(signal);
        checkUnlockCode(optionDB);

        //8.0. блокировка проведения
        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (optionDB != null) {
                if (signal) {
                    optionDB.setIsSignal("1");
                } else {
                    optionDB.setIsSignal("2");
                }
                realm.insertOrUpdate(optionDB);
            }
        });

        Log.d("test", "spannableStringBuilder: " + spannableStringBuilder);
        Log.d("test", "massageToUser: " + massageToUser);
    }

    public boolean isSignal() {
        return signal;
    }

    private SpannableString createLinkedString(String msg, WpDataDB wpDataDB) {
        SpannableString res = new SpannableString(msg);

        try {
//            long otchetId;
//            int action = wpDataDB.getAction();
//            if (action == 1 || action == 94) {
//                otchetId = wpDataDB.getDoc_num_otchet_id();
//            } else {
//                otchetId = wpDataDB.getDoc_num_1c_id();
//            }

//            WorkPlan workPlan = new WorkPlan();
//            WPDataObj wpDataObj = workPlan.getKPS(wpDataDB.getId());


            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Intent intent = new Intent(context, DetailedReportActivity.class);
                    intent.putExtra("WpDataDB_ID", wpDataDB.getId());
//                    intent.putExtra("dataFromWP", D);
//                    intent.putExtra("rowWP", wpDataDB);
//                    intent.putExtra("dataFromWPObj", wpDataObj);
                    context.startActivity(intent);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            int count = msg.length();
            res.setSpan(new ForegroundColorSpan(Color.BLUE), 0, count, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            res.setSpan(new UnderlineSpan(), 0, count, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            res.setSpan(clickableSpan, 0, count, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlEndAnotherWork/createLinkedString/Exception", "Exception e: " + e);
        }
        return res;
    }


}
