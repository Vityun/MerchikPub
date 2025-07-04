package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARActivity;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AchievementsSDB;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.Database.Room.VoteSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm;
import ua.com.merchik.merchik.dialogs.DialogAchievement.DialogAchievement;
import ua.com.merchik.merchik.dialogs.DialogAchievement.DialogCreateAchievement;

public class OptionControlAchievements<T> extends OptionControl {
    public int OPTION_CONTROL_ACHIEVEMENTS_ID = 590; //

    public boolean signal = true;

    private int sumOptionError;
    private int minScore = 6;
    private int traineeSignal = 0;

    private SpannableStringBuilder optionMsg = new SpannableStringBuilder();
    private StringBuilder achievementsMsgList = new StringBuilder();

    private List<AchievementsSDB> resultAchievements = new ArrayList<>();
    private StringBuilder SPIS = new StringBuilder();

    private WpDataDB wpDataDB;
    private CustomerSDB customerSDBDocument;
    private UsersSDB usersSDBDocument;
    private AddressSDB addressSDBDocument;
    private Integer themeId;

    private Long dateDocument;  // В секундах
    private Long dateFrom = 0L;
    private Long dateTo = 0L;

    private int sumError = 0;

    public OptionControlAchievements(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;
        getDocumentVar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            executeOption();
        }
    }


    private void getDocumentVar() {
        try {
            if (optionDB.getAmountMin() != null && !optionDB.getAmountMin().equals("0")) {
                minScore = Integer.valueOf(optionDB.getAmountMin());
            }

            if (document instanceof WpDataDB) {
                this.wpDataDB = (WpDataDB) document;

                customerSDBDocument = SQL_DB.customerDao().getById(wpDataDB.getClient_id());
                usersSDBDocument = SQL_DB.usersDao().getById(wpDataDB.getUser_id());
                addressSDBDocument = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
                dateDocument = wpDataDB.getDt().getTime() / 1000;

//                if (System.currentTimeMillis() > 1681603200000L) {
//                    themeId = 595;
//                }

                // dateDocument*1000 -- Делаем такую херь, потому что функция работает в миллисекундах. / 1000 - для перевода в секунды.
                int minusDay = Integer.parseInt(optionDB.getAmountMax()) > 0 ? Integer.parseInt(optionDB.getAmountMax()) : 30;
//                minusDay = minusDay + 1;
                dateFrom = Clock.getDatePeriodLong(dateDocument * 1000, -minusDay) / 1000;
                dateTo = Clock.getDatePeriodLong(dateDocument * 1000, 3) / 1000;    // Тут надо указывать на +1 день, потому что функция работает до НАЧАЛА дня, а не до конца
            }

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAchievements/getDocumentVar", "Exception e: " + e);
        }
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        try {
            // 3.0. Исключения // костыли с 1С
            if (addressSDBDocument.id == 22011) {    // дом игрушек
                if (customerSDBDocument.id.equals("11165")) {    // Энерлайт
                    optionMsg.append("Достижение по клиенту ").append(customerSDBDocument.nm).append(" по адресу ")
                            .append(addressSDBDocument.nm).append(" не проверяем. Там у них только брендовые стойки без основного места продаж.");
                }
            }


//            if (optionDB.getOptionId().equals("590") || optionDB.getOptionControlId().equals("590")) {
            // Получение Доп. Требований с дополнительными фильтрами.
            long dateChangeTo;
            if (wpDataDB.getUser_id() == 143565)
                dateChangeTo = Clock.getDatePeriodLong(dateDocument * 1000, -3) / 1000;
            else
                dateChangeTo = Clock.getDatePeriodLong(dateDocument * 1000, -2) / 1000;


            Log.e("document", "client: " + wpDataDB.getClient_id());
            Log.e("document", "control: " + optionDB.getOptionControlId());

            int controlId = Integer.parseInt(optionDB.getOptionControlId());
            if (wpDataDB.getClient_id().equals("9382") && controlId == 138520)
                controlId = 160209;


            // 3.1. Получим данные о достижениях.
            // Сразу отсортировали (свежие должны быть сверху)
            List<AchievementsSDB> achievementsSDBList = SQL_DB.achievementsDao().getForOptionControl(dateFrom, dateTo, customerSDBDocument.id, addressSDBDocument.id, themeId);
//            List<AchievementsSDB> achievementsSDBList = SQL_DB.achievementsDao().getForOptionControl(dateFrom, dateTo, customerSDBDocument.id, addressSDBDocument.id);

            List<Integer> ids = new ArrayList<>();
            for (AchievementsSDB item : achievementsSDBList) {
                ids.add(item.serverId);
            }
            List<VoteSDB> voteSDBList = SQL_DB.votesDao().getByIds(ids);

            // 3.2.
            // OCВ Не делал. Возможно вернусь. Пока добавляем, как по мне - бестолковые поля.
            // 05.04.2025 получаем список товаров и ТМ ОСВ которые есть на ТТ
            List<String> spisTovOSV = new ArrayList<>();
            List<String> spisTMOSV = new ArrayList<>();
            List<TovarDB> tovarFromReportPrepare = RealmManager.INSTANCE.copyFromRealm(Objects.requireNonNull(RealmManager.getTovarListFromReportPrepareByDad2(wpDataDB.getCode_dad2())));
            Log.e("Tovar", "size: " + tovarFromReportPrepare.size());
            for (TovarDB tovarDB : tovarFromReportPrepare) {
                Log.e("!!!DBOSV!!!", "tovar name: " + tovarDB.getNm() + " | id: " + tovarDB.getiD());
            }
            List<AdditionalRequirementsDB> additionalRequirements = AdditionalRequirementsRealm.getDocumentAdditionalRequirements(document, true, controlId, null, null, null, null, null, null, dateChangeTo);

            // 06.02.2025 добавил проверку что бы товар был в репорт препеа, что бы исключить случай когда товара вообще нет в ТТ, а требуют достижение по нему
            Set<String> manufacturerIds = tovarFromReportPrepare.stream()
                    .map(TovarDB::getManufacturerId)
                    .collect(Collectors.toSet());
            List<AdditionalRequirementsDB> filteredAdditionalRequirements = new ArrayList<>(additionalRequirements).stream()
                    .filter(req -> manufacturerIds.contains(req.getManufacturerId()))
                    .collect(Collectors.toList());

            for (AdditionalRequirementsDB item : filteredAdditionalRequirements) {
                if (!item.getTovarId().equals("0")) spisTovOSV.add(item.getTovarId());
                if (!item.getManufacturerId().equals("0")) spisTMOSV.add(item.getManufacturerId());
            }
            spisTovOSV.sort(null);  // Сортирует по возрастанию // аналог Collections.sort(spisTovOSV);
            spisTMOSV.sort(null);


            // 3.3. (3.4) Определим практиканта.
            String trainee = ""; // практикант
            if (usersSDBDocument.reportDate20 == null || dateDocument < usersSDBDocument.reportDate20.getTime() / 1000) {
                traineeSignal = 1;
                trainee = ", але виконавець ще не провів свого 20-го звіту. Наявність Досягнень не перевіряємо!";
            } else if ((optionDB.getOptionId().equals("160209") || optionDB.getOptionControlId().equals("160209")) && (usersSDBDocument.reportDate40 == null || dateDocument < usersSDBDocument.reportDate40.getTime() / 1000)) {
                traineeSignal = 1;
                trainee = ", але виконавець ще не провів свого 40-го звіту. Наявність Досягнень не перевіряємо!";
            }

            // 3.4. (3.5) Расчт результата
            if (achievementsSDBList == null || achievementsSDBList.size() == 0) {
                sumOptionError = 1;
                optionMsg.append("Достижение по клиенту ").append(customerSDBDocument.nm).append(" не выполнено. ").append(trainee);
                SPIS.append(customerSDBDocument.nm).append(", ");
            } else {
                for (AchievementsSDB item : achievementsSDBList) {
                    Log.e("!!!!!!!!", "spisTovOSV: " + spisTovOSV.size());
                    Log.e("!!!!!!!!", "spisTovOSV: " + spisTovOSV);

                    // 18.04.24. Проверка на тему 595. Мерчикам надо ГОВОРИТь что у них нет нужной темы.
                    if (item.themeId != 595) {
                        String themeTxt = "";
                        String theme595Txt = "";
                        ThemeDB theme = ThemeRealm.getThemeById(String.valueOf(item.themeId));
                        ThemeDB theme595 = ThemeRealm.getThemeById(String.valueOf(595));
                        if (theme != null) themeTxt = theme.getNm();
                        if (theme595 != null) theme595Txt = theme595.getNm();

                        item.error = 1;
                        item.note = new SpannableStringBuilder()
                                .append("\n")
                                .append(createLinkedString("Досягнення #" + item.serverId,item))
                                .append(" тема досягнення ")
                                .append(String.valueOf(item.themeId)).append(" - ").append(themeTxt)
                                .append(" не влаштовує! Повинна бути тема: ")
                                .append("595").append(" - ").append(theme595Txt)
                                .append("");
                        resultAchievements.add(item);
                        continue;
                    } else if (item.tovar_id == null)
                        item.tovar_id = 0;
                    if (item.manufacturer == null)
                        item.manufacturer = 0;
                    //09.09.2024 якщо по даному клієнту є товари (або ТМ) по котрим встановлени признак ОсобливаУвага (ОСВ) то перевіряємо виготовлення Досягнень конкретно по ДАНИМ Товарам/ТМ
                    if ((!spisTovOSV.isEmpty() || !spisTMOSV.isEmpty()) &&
                            (!spisTovOSV.contains(item.tovar_id.toString())) &&
                            (!spisTMOSV.contains(item.manufacturer.toString()))) {
                        List<TovarDB> spisTovarDBOSV = RealmManager.INSTANCE.copyFromRealm(TovarRealm.getByIds(spisTovOSV.toArray(new String[spisTovOSV.size()])));

                        String spisTovarName = "";
                        for (TovarDB tovar : spisTovarDBOSV) {
                            spisTovarName = spisTovarName + tovar.getNm() + ",";
                            Log.e("!!!DBOSV!!!", "tovar name: " + tovar.getNm() + " | id: " + tovar.getiD());
                        }

                        List<TradeMarkDB> spisTradeMarkDBOSV = TradeMarkRealm.getTradeMarkByIds(spisTMOSV.toArray(new String[spisTMOSV.size()]));

                        String spisTMName = "";
                        for (TradeMarkDB tm : spisTradeMarkDBOSV) {
                            spisTMName = spisTMName + tm.getNm() + ",";
                            Log.e("!!!DBOSV!!!", "tovar name: " + tm.getNm() + " | id: " + tm.getID());
                        }

                        item.error = 1;
                        item.note = new SpannableStringBuilder()
                                .append("")
                                .append("Клієнт вимагає створення Досягнення по")
                                .append(!spisTovarDBOSV.isEmpty() ? ("Товару: " + spisTovarName) : " ")
                                .append(!spisTMOSV.isEmpty() ? ("ТМ: " + spisTMName) : " ");
                    } else if ((optionDB.getOptionId().equals("160209") || optionDB.getOptionControlId().equals("160209"))) {
                        item.note = new SpannableStringBuilder().append("для опції перевіряем лише наявність досягнень");
                        continue;
                    } else if (item.dvi == 1) { // значение достижения не утверждено супервайзером
                        item.error = 1;
                        item.note = new SpannableStringBuilder().append("у достижения ").append(String.valueOf(item.serverId)).append(" установлен признак ДВИ=1");
                        SPIS.append(item.note).append(", ");
//                    } else if (item.confirmState != 1) {
//                        item.error = 1;
//                        item.note = new StringBuilder().append("достижение ").append(item.serverId).append(" НЕ утверждено Супервайзером");
//                        SPIS.append(item.note).append(", ");
                    } else if (item.score.equals("-") || item.score.equals("0")) {
                        item.error = 1;
                        item.note = new SpannableStringBuilder().append("достижение ").append(String.valueOf(item.serverId)).append(" НЕ оценено Территориалом");
                        SPIS.append(item.note).append(", ");
                    } else if (item.dt_ut >= (Clock.getDatePeriodLong(dateDocument * 1000, -1) / 1000)
                            && item.dt_ut <= (Clock.getDatePeriodLong(dateDocument * 1000, +1) / 1000)) {
                        item.currentVisit = 1;
                    } else {
                        item.note = new SpannableStringBuilder().append("есть утвержденное достижение ");
                    }

                    if (item.error == null || item.error == 0) {
                        for (VoteSDB voteItem : voteSDBList) {
                            if (!item.serverId.equals(voteItem.serverId)) continue;

                            item.score = String.valueOf(voteItem.score);
                            item.note = new SpannableStringBuilder().append("достижение ").append(String.valueOf(item.serverId)).append(" утверждено и получило оценку ")
                                    .append(String.valueOf(voteItem.score)).append(" от ").append(String.valueOf(voteItem.merchik));

                            if (voteItem.score < minScore) {
                                item.error = 1;
                                item.note = new SpannableStringBuilder().append("достижение ").append(String.valueOf(item.serverId)).append(" утверждено но получило низкую оценку ")
                                        .append(String.valueOf(voteItem.score)).append(" за: (").append(voteItem.comments).append(") от ").append(String.valueOf(voteItem.voterId));    // TODO Вопрос к текстовке Петрова
                                SPIS.append(item.note).append(", ");

                                break;
                            }
                        }
                    }
                    resultAchievements.add(item);
                }// end for


                try {
                    sumError = achievementsSDBList.stream().map(table -> table.error).reduce(0, Integer::sum);
                } catch (Exception ignored) {
                }
                if (sumError == achievementsSDBList.size()) {
                    sumOptionError = 1;
                } else {
                    for (AchievementsSDB item : achievementsSDBList) {
                        if (item.error == null || item.error == 0) {
                            resultAchievements.add(item);
                        }
                    }
                }
            }

            StringBuilder period = new StringBuilder();
            period.append("За період з ").append(Clock.getHumanTimeYYYYMMDD(dateFrom)).append(" по ").append(Clock.getHumanTimeYYYYMMDD(dateTo - 86400L)); // добавил вычитание 1 дня, что бы привести к 1С потому, что функция работает до НАЧАЛА дня, а не до конца
            //4.0. готовим сообщение и сигнал
            if (sumOptionError == 0 && traineeSignal == 0) {
                spannableStringBuilder.append(period).append(" Є досягнення (з оцінкою ").append(String.valueOf(minScore)).append(" чи більш) ")
                        .append(wpDataDB.getAddr_txt()).append(" по ").append(customerSDBDocument.nm).append(". Та передані кліенту для нарахування премії.");

//            stringBuilderMsg.append(" ЕСТЬ утвержденные достижения (с оценкой ")
//                        .append(minScore).append(" и более) ").append("???"/*TODO Тут указано ТекПос, откуда я его беру?*/).append(" по ").append(customerSDBDocument.nm)
//                        .append(". И переданы клиенту для начисления премии.").append(SPIS);
                signal = false;

            } else if ((optionDB.getOptionId().equals("160209") || optionDB.getOptionControlId().equals("160209")) && achievementsSDBList.size() == 0 && traineeSignal > 0) {
                spannableStringBuilder.append(period).append(" нема жодного досягнення. Але виконавець ще не провів свого 40-го звіту.");
                signal = false;

            } else if ((optionDB.getOptionId().equals("160209") || optionDB.getOptionControlId().equals("160209")) && achievementsSDBList.size() > 0 && sumError == achievementsSDBList.size()) {
                Log.e("!", "_");
                period.append(" є ").append(achievementsSDBList.size())
                        .append(" досягнення");
                spannableStringBuilder.append(period);
                if (!resultAchievements.isEmpty())
                    spannableStringBuilder.append(", але: ")
                            .append(resultAchievements.stream()
                                    .filter(item -> item.error == 1)
                                    .map(item -> item.note)
                                    .findFirst()
                                    .orElse(new SpannableStringBuilder()));
                signal = true;
            } else if ((optionDB.getOptionId().equals("160209") || optionDB.getOptionControlId().equals("160209")) && achievementsSDBList.size() == 0) {
                spannableStringBuilder.append(period).append(" нема жодного досягнення. ");
                signal = true;
            } else if ((optionDB.getOptionId().equals("160209") || optionDB.getOptionControlId().equals("160209")) && achievementsSDBList.size() > 0) {
                spannableStringBuilder.append(period).append(" створено ").append((char) achievementsSDBList.size()).append(" досягнень.");
                signal = false;
            }
//            else if (traineeSignal > 0) {
//                stringBuilderMsg.append(trainee).append(period).append(" НЕМА досягнень (з оцінкою ")
//                        .append(minScore).append(" чи більш) по ").append(SPIS).append(".");
//                signal = false;
//            }
            else {
                spannableStringBuilder.append(trainee).append(period).append(" НЕМА досягнень (з оцінкою ")
                        .append((char) minScore).append(" чи більш) по ").append(SPIS).append(".");
                signal = true;
            }

            if (traineeSignal > 0) {
                spannableStringBuilder.append(trainee);
                signal = false;
            }

            // Сохранение
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

            if (signal) {
                if (optionDB.getBlockPns().equals("1")) {
                    setIsBlockOption(signal);
//                    showUnlockCodeDialogInMainThread(wpDataDB, signal);
                    spannableStringBuilder.append("\n\n").append("Документ проведен не будет!");
                } else {
                    spannableStringBuilder.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете делать Достижения.");
                }
            }

            checkUnlockCode(optionDB);

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAchievements/executeOption", "Exception e: " + e);
        }
    }

    private SpannableString createLinkedString(String msg, AchievementsSDB data) {


        SpannableString res = new SpannableString(msg);

        try {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    DialogAchievement dialogAchievement = new DialogAchievement(context);
                    dialogAchievement.setClose(dialogAchievement::dismiss);
                    dialogAchievement.setAchievement(data);
                    dialogAchievement.setOk("Створити ДІНДОС", ()->{
                        try {
                            DialogCreateAchievement dialogCreateAchievement = new DialogCreateAchievement(context);
                            dialogCreateAchievement.setData(data);
                            dialogCreateAchievement.setClose(dialogCreateAchievement::dismiss);
                            dialogCreateAchievement.setTitle("Створення Досягнення на основі створеного");
                            dialogCreateAchievement.buttonPhotoTo();
                            dialogCreateAchievement.buttonPhotoAfter();
                            dialogCreateAchievement.show();
                        }catch (Exception e){
                            Globals.writeToMLOG("ERROR", "bindACHIEVEMENTS/create", "Exception e: " + e);
                            Globals.writeToMLOG("ERROR", "bindACHIEVEMENTS/create", "Exception es: " + Arrays.toString(e.getStackTrace()));
                        }
                    });
                    dialogAchievement.show();
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.GREEN);
                    ds.setUnderlineText(true);
                }
            };
            int count = msg.length();
            res.setSpan(clickableSpan, 0, count, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlTaskAnswer/createLinkedString/Exception", "Exception e: " + e);
        }
        return res;
    }
}
