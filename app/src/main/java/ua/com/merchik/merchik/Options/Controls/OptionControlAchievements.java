package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

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
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;

public class OptionControlAchievements<T> extends OptionControl {
    public int OPTION_CONTROL_ACHIEVEMENTS_ID = 590;

    public boolean signal = true;

    private int sumOptionError;
    private int minScore = 6;
    private int traineeSignal = 0;

    private StringBuilder optionMsg = new StringBuilder();
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
                minusDay = minusDay + 1;
                dateFrom = Clock.getDatePeriodLong(dateDocument * 1000, -minusDay) / 1000;
                dateTo = Clock.getDatePeriodLong(dateDocument * 1000, 3) / 1000;    // Тут надо указывать на +1 день, потому что функция работает до НАЧАЛА дня, а не до конца
            }

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAchievements/getDocumentVar", "Exception e: " + e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        try {
            // 3.0. Исключения // костыли с 1С
            if (addressSDBDocument.id == 22011) {    // дом игрушек
                if (customerSDBDocument.id.equals("11165")) {    // Энерлайт
                    optionMsg.append("Достижение по клиенту ").append(customerSDBDocument.nm).append(" по адресу ")
                            .append(addressSDBDocument.nm).append(" не проверяем. Там у них только брендовые стойки без основного места рподаж.");
                }
            }

            // 3.1. Получим данные о достижениях.
            // Сразу отсортировали (свежие должны быть сверху)
            List<AchievementsSDB> achievementsSDBList = SQL_DB.achievementsDao().getForOptionControl(dateFrom, dateTo, customerSDBDocument.id, addressSDBDocument.id, themeId);
//            List<AchievementsSDB> achievementsSDBList = SQL_DB.achievementsDao().getForOptionControl(dateFrom, dateTo, customerSDBDocument.id, addressSDBDocument.id);

            List<Integer> ids = new ArrayList<>();
            for (AchievementsSDB item : achievementsSDBList) {
                ids.add(item.serverId);
            }
            List<VoteSDB> voteSDBList = SQL_DB.votesDao().getByIds(ids);

            // 3.2. Не делал. Возможно вернусь. Пока добавляем, как по мне - бестолковые поля.

            // 3.3. Определим практиканта.
            String trainee = ""; // практикант
            if (usersSDBDocument.reportDate20 == null || dateDocument < usersSDBDocument.reportDate20.getTime() / 1000) {
                traineeSignal = 1;
                trainee = "Исполнитель ещё НЕ провёл своего 20-го отчёта. Наличие Достижений не проверяем!";
            } else if ((optionDB.getOptionId().equals("160209") || optionDB.getOptionControlId().equals("160209")) && (usersSDBDocument.reportDate40 == null || dateDocument < usersSDBDocument.reportDate40.getTime() / 1000)) {
                traineeSignal = 1;
                trainee = "Исполнитель ещё НЕ провёл своего 40-го отчёта. Наличие Достижений не проверяем!";
            }

            // 3.4.
            if (achievementsSDBList == null || achievementsSDBList.size() == 0) {
                sumOptionError = 1;
                optionMsg.append("Достижение по клиенту ").append(customerSDBDocument.nm).append(" не выполнено. ").append(trainee);
                SPIS.append(customerSDBDocument.nm).append(", ");
            } else {
                for (AchievementsSDB item : achievementsSDBList) {

                    // 18.04.24. Проверка на тему 595. Мерчикам надо ГОВОРИТь что у них нет нужной темы.
                    if (item.themeId != 595) {
                        String themeTxt = "";
                        String theme595Txt = "";
                        ThemeDB theme = ThemeRealm.getThemeById(String.valueOf(item.themeId));
                        ThemeDB theme595 = ThemeRealm.getThemeById(String.valueOf(595));
                        if (theme != null) themeTxt = theme.getNm();
                        if (theme595 != null) theme595Txt = theme595.getNm();

                        item.error = 1;
                        item.note = new StringBuilder().append("тема досягнення №")
                                .append(item.serverId)
                                .append(" (").append(item.themeId).append("-").append(themeTxt)
                                .append(") не влаштовує! Повинна бути тема: ")
                                .append(" (").append("595").append("-").append(theme595Txt)
                                .append(")");
                        continue;
                    }

                    if ((optionDB.getOptionId().equals("160209") || optionDB.getOptionControlId().equals("160209"))) {
                        item.note = new StringBuilder().append("для опції перевіряем лише наявність досягнень");
                        continue;
                    }
                    if (item.dvi == 1) { // значение достижения не утверждено супервайзером
                        item.error = 1;
                        item.note = new StringBuilder().append("у достижения ").append(item.serverId).append(" установлен признак ДВИ=1");
                        SPIS.append(item.note).append(", ");
//                    } else if (item.confirmState != 1) {
//                        item.error = 1;
//                        item.note = new StringBuilder().append("достижение ").append(item.serverId).append(" НЕ утверждено Супервайзером");
//                        SPIS.append(item.note).append(", ");
                    } else if (item.score.equals("-") || item.score.equals("0")) {
                        item.error = 1;
                        item.note = new StringBuilder().append("достижение ").append(item.serverId).append(" НЕ оценено Территориалом");
                        SPIS.append(item.note).append(", ");
                    } else if (item.dt_ut >= (Clock.getDatePeriodLong(dateDocument * 1000, -1) / 1000)
                            && item.dt_ut <= (Clock.getDatePeriodLong(dateDocument * 1000, +1) / 1000)) {
                        item.currentVisit = 1;
                    } else {
                        item.note = new StringBuilder().append("есть утвержденное достижение ");
                    }

                    if (item.error == null || item.error == 0) {
                        for (VoteSDB voteItem : voteSDBList) {
                            if (!item.serverId.equals(voteItem.serverId)) continue;

                            item.score = String.valueOf(voteItem.score);
                            item.note = new StringBuilder().append("достижение ").append(item.serverId).append(" утверждено и получило оценку ")
                                    .append(voteItem.score).append(" от ").append(voteItem.merchik);

                            if (voteItem.score < minScore) {
                                item.error = 1;
                                item.note = new StringBuilder().append("достижение ").append(item.serverId).append(" утверждено но получило низкую оценку ")
                                        .append(voteItem.score).append(" за: (").append(voteItem.comments).append(") от ").append(voteItem.voterId);    // TODO Вопрос к текстовке Петрова
                                SPIS.append(item.note).append(", ");

                                break;
                            }
                        }
                    }
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
            period.append("За період з ").append(Clock.getHumanTimeYYYYMMDD(dateFrom)).append(" по ").append(Clock.getHumanTimeYYYYMMDD(dateTo));
            //4.0. готовим сообщение и сигнал
            if (sumOptionError == 0 && traineeSignal == 0) {
                stringBuilderMsg.append(period).append(" Є досягнення (з оцінкою ").append(minScore).append(" чи більш) ")
                        .append(wpDataDB.getAddr_txt()).append(" по ").append(customerSDBDocument.nm).append(". Та передані кліенту для нарахування премії.");

//            stringBuilderMsg.append(" ЕСТЬ утвержденные достижения (с оценкой ")
//                        .append(minScore).append(" и более) ").append("???"/*TODO Тут указано ТекПос, откуда я его беру?*/).append(" по ").append(customerSDBDocument.nm)
//                        .append(". И переданы клиенту для начисления премии.").append(SPIS);
                signal = false;

            } else if ((optionDB.getOptionId().equals("160209") || optionDB.getOptionControlId().equals("160209")) && achievementsSDBList.size() == 0 && traineeSignal > 0) {
                stringBuilderMsg.append(period).append(" нема жодного досягнення. Але виконавець ще не провів свого 40-го звіту.");
                signal = false;

            } else if ((optionDB.getOptionId().equals("160209") || optionDB.getOptionControlId().equals("160209")) && achievementsSDBList.size() > 0 && sumError == achievementsSDBList.size()){

            } else if ((optionDB.getOptionId().equals("160209") || optionDB.getOptionControlId().equals("160209")) && achievementsSDBList.size() == 0) {
                stringBuilderMsg.append(period).append(" нема жодного досягнення. ");
                signal = true;
            } else if ((optionDB.getOptionId().equals("160209") || optionDB.getOptionControlId().equals("160209")) && achievementsSDBList.size() > 0) {
                stringBuilderMsg.append(period).append(" створено ").append(achievementsSDBList.size()).append(" досягнень.");
                signal = false;
            } else if (traineeSignal > 0) {
                stringBuilderMsg.append(trainee).append(period).append(" НЕМА досягнень (з оцінкою ")
                        .append(minScore).append(" чи більш) по ").append(SPIS).append(".");
                signal = false;
            } else {
                stringBuilderMsg.append(trainee).append(period).append(" НЕМА досягнень (з оцінкою ")
                        .append(minScore).append(" чи більш) по ").append(SPIS).append(".");
                signal = true;
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
                    stringBuilderMsg.append("\n\n").append("Документ проведен не будет!");
                } else {
                    stringBuilderMsg.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете делать Достижения.");
                }
            }


        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAchievements/executeOption", "Exception e: " + e);
        }
    }
}
