package ua.com.merchik.merchik.Options.Controls;

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
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class OptionControlAchievements<T> extends OptionControl {
    public int OPTION_CONTROL_ACHIEVEMENTS_ID = 590;

    private boolean signal = true;

    private Integer sumOptionError;
    private Integer minScore = 6;
    private int traineeSignal = 0;

    private StringBuilder optionMsg = new StringBuilder();
    private StringBuilder achievementsMsgList = new StringBuilder();

    private List<AchievementsSDB> resultAchievements = new ArrayList<>();
    private StringBuilder SPIS = new StringBuilder();

    private WpDataDB wpDataDB;
    private CustomerSDB customerSDBDocument;
    private UsersSDB usersSDBDocument;
    private AddressSDB addressSDBDocument;

    private Long dateDocument;  // В секундах
    private Long dateFrom = 0L;
    private Long dateTo = 0L;


    public OptionControlAchievements(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
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

                // dateDocument*1000 -- Делаем такую херь, потому что функция работает в миллисекундах. / 1000 - для перевода в секунды.
                dateFrom = Clock.getDatePeriodLong(dateDocument * 1000, -35) / 1000;
                dateTo = Clock.getDatePeriodLong(dateDocument * 1000, 0) / 1000;
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
            List<AchievementsSDB> achievementsSDBList = SQL_DB.achievementsDao().getForOptionControl(dateFrom, dateTo, customerSDBDocument.id, addressSDBDocument.id);

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
            }

            // 3.4.
            if (achievementsSDBList == null || achievementsSDBList.size() == 0) {
                sumOptionError = 1;
                optionMsg.append("Достижение по клиенту ").append(customerSDBDocument.nm).append(" не выполнено. ").append(trainee);
                SPIS.append(customerSDBDocument.nm).append(", ");
            } else {
                for (AchievementsSDB item : achievementsSDBList) {
                    if (item.dvi == 1) { // значение достижения не утверждено супервайзером
                        item.error = 1;
                        item.note = new StringBuilder().append("у достижения ").append(item.serverId).append(" установлен признак ДВИ=1");
                        SPIS.append(item.note).append(", ");
                    } else if (item.confirmState != 1) {
                        item.error = 1;
                        item.note = new StringBuilder().append("достижение ").append(item.serverId).append(" НЕ утверждено Супервайзером");
                        SPIS.append(item.note).append(", ");
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

                    if (item.error == 0) {
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


                Integer sumError = achievementsSDBList.stream().map(table -> table.error).reduce(0, Integer::sum);
                if (sumError == achievementsSDBList.size()) {
                    sumOptionError = 1;
                } else {
                    for (AchievementsSDB item : achievementsSDBList){
                        if (item.error == 0){
                            resultAchievements.add(item);
                        }
                    }
                }
            }

            //4.0. готовим сообщение и сигнал
            if (sumOptionError == 0 && traineeSignal == 0){
                stringBuilderMsg.append("За период с ").append(Clock.getHumanTimeYYYYMMDD(dateFrom)).append(" по ").append(Clock.getHumanTimeYYYYMMDD(dateTo)).append(" ЕСТЬ утвержденные достижения (с оценкой ")
                        .append(minScore).append(" и более) ").append("???"/*TODO Тут указано ТекПос, откуда я его беру?*/).append(" по ").append(customerSDBDocument.nm)
                        .append(". И переданы клиенту для начисления премии.").append(SPIS);
                signal = false;
            }else if (traineeSignal > 0){
                stringBuilderMsg.append(trainee).append("За период с ").append(Clock.getHumanTimeYYYYMMDD(dateFrom)).append(" по ").append(Clock.getHumanTimeYYYYMMDD(dateTo)).append(" НЕТ утвержденных достижений (с оценкой ")
                        .append(minScore).append(" и более) по ").append(SPIS);
                signal = false;
            }else {
                stringBuilderMsg.append(trainee).append("За период с ").append(Clock.getHumanTimeYYYYMMDD(dateFrom)).append(" по ").append(Clock.getHumanTimeYYYYMMDD(dateTo)).append(" НЕТ утвержденных достижений (с оценкой ")
                        .append(minScore).append(" и более) по ").append(SPIS).append(".");
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