package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;

/**
 * Контроль изготовления ФОТО с указанием витрин к которым они относятся.
 */
public class OptionControlPhotoShowcase<T> extends OptionControl {

    public int OPTION_CONTROL_PhotoShowcase_ID = 160568;

    public boolean signal = true;
    private int colMin = 50;
    private Date date;
    private Date dateFrom;
    private Date dateTo;
    private long dad2;
    private int percentValue;

    private WpDataDB wpDataDB;
    private UsersSDB usersSDB;

    private List<ShowcaseSDB> showcaseSDBList;
    private List<StackPhotoDB> stackPhotoDBSList;

    public OptionControlPhotoShowcase(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
            Globals.writeToMLOG("ERROR", "OptionControlPhotoShowcase", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        try {
            //1.0. определим переменные
            int min = Integer.parseInt(optionDB.getAmountMin());
            int max = Integer.parseInt(optionDB.getAmountMax());

            colMin = min > 0 ? min : 50;

            //1.3. определим переменные в зависимости от документа
            wpDataDB = (WpDataDB) document;
            date = wpDataDB.getDt();
            dad2 = wpDataDB.getCode_dad2();

            dateFrom = date;
            dateTo = date;
            usersSDB = SQL_DB.usersDao().getById(wpDataDB.getUser_id());

            // Тут ещё должен быть фильтр по Дате - 2 дня. Но у меня Дата в Date, а дата у Витрин - Строка
            showcaseSDBList = SQL_DB.showcaseDao().getByDoc(wpDataDB.getClient_id(), wpDataDB.getAddr_id());
            stackPhotoDBSList = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getPhotosByDAD2(dad2, 0)); // 0 - Фото Витрины
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPhotoShowcase/getDocumentVar", "Exception e: " + e);
        }
    }

    private void executeOption() {
        try {
            //3.2. отметим фото для которых витрина определена (для цього використовую СпецКол, щоб не створювати окремоъ колонки)
            List<StackPhotoDB> list = new ArrayList<>();
            // Создаем счетчик для подсчета заполненных showcase_id
            int filledShowcaseIdsCount = 0;
            // Проверяем каждый showcase_id в списке stackPhotoDBSList
            for (StackPhotoDB stackPhotoDB : stackPhotoDBSList) {
                String showcaseId = stackPhotoDB.showcase_id; // Получаем showcase_id из объекта StackPhotoDB
                // Проверяем, является ли showcase_id пустым или null, если нет, увеличиваем счетчик
                if (showcaseId != null && !showcaseId.isEmpty() && !showcaseId.equals("0")) {
                    filledShowcaseIdsCount++;
                    list.add(stackPhotoDB);
                }
            }

            //3.3. підрахуємо відсоток світлин у котррих зазначениа вітрина
            try {
                percentValue = 100 * filledShowcaseIdsCount / stackPhotoDBSList.size();
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "OptionControlPhotoShowcase/executeOption/percentValue", "Exception e: " + e);
            }


            if (stackPhotoDBSList.size() == 0) {
                stringBuilderMsg.append("Не можу знайти світлини стосовні до поточного відвідування.");
                signal = true;
            } else if (showcaseSDBList.size() == 0) {
                stringBuilderMsg.append("Не можу знайти жодної вітрини у даній Адресі (для даного Клієнта).");

                // создание объекта для даты '01.01.2024'
                Calendar calendar = Calendar.getInstance();
                calendar.set(2024, Calendar.FEBRUARY, 1);
                Date specificDate = calendar.getTime();

                int comparisonResult = date.compareTo(specificDate);

                // НОвого клиента запустили и у него нет витрин
                // Дата - 40 (начала работ клиента)

                if (comparisonResult < 0) {
                    signal = false;
                } else {
                    signal = true;
                }
            } else if (showcaseSDBList.size() > 0 && filledShowcaseIdsCount == 0) {
                stringBuilderMsg.append("При виготовленні світлин Ви НЕ обрали жодної з ").append(showcaseSDBList.size()).append(" вітрин.");
                signal = true;
            } else if (colMin > 0 && percentValue < colMin) {
                stringBuilderMsg.append("При виготовленні світлин, Ви зазначили вітрини лише у ")
                        .append(filledShowcaseIdsCount)
                        .append(" фото з ")
                        .append(stackPhotoDBSList.size()).append(" (")
                        .append(percentValue).append("%) що МЕНШЕ плану в ").append(colMin).append("%");
                signal = true;
            } else {
                stringBuilderMsg.append("При виготовленні світлин, Ви зазначили вітрини у ")
                        .append(filledShowcaseIdsCount)
                        .append(" фото з ")
                        .append(stackPhotoDBSList.size()).append(" (").append(percentValue).append("%) що БІЛЬШЕ плану в ")
                        .append(colMin).append("%. Зауважень немає.");
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
                    stringBuilderMsg.append("\n\n").append("Документ проведен не будет!");
                } else {
                    stringBuilderMsg.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете делать Достижения.");
                }
            }

            if (signal){
                unlockCodeResultListener.onUnlockCodeFailure();
            }else {
                unlockCodeResultListener.onUnlockCodeSuccess();
            }

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPhotoShowcase/executeOption", "Exception e: " + e);
        }
    }

}
