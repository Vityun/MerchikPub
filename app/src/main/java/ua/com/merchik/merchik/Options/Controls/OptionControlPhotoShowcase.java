package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.DossierSotrSDB;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

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
    private int perShowcase;

    private WpDataDB wpDataDB;
    private UsersSDB usersSDB;

    private List<ShowcaseSDB> showcaseSDBList;
    private List<StackPhotoDB> stackPhotoDBSList;
    private List<AdditionalRequirementsDB> additionalRequirementsDBS;
    private final List<StackPhotoDB> list = new ArrayList<>();

    public OptionControlPhotoShowcase(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        try {
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

            additionalRequirementsDBS = AdditionalRequirementsRealm.getAdditionalRequirements(wpDataDB.getClient_id(), wpDataDB.getAddr_id(), 160568);

            List<Integer> shwAR = new ArrayList<>();
            for (AdditionalRequirementsDB item : additionalRequirementsDBS) {
                shwAR.add(item.showcaseTpId);
            }

            // Тут ещё должен быть фильтр по Дате - 2 дня. Но у меня Дата в Date, а дата у Витрин - Строка
            if (shwAR.size() > 0) {
                showcaseSDBList = SQL_DB.showcaseDao().getByDoc(wpDataDB.getClient_id(), wpDataDB.getAddr_id(), shwAR);
            } else {
                showcaseSDBList = SQL_DB.showcaseDao().getByDoc(wpDataDB.getClient_id(), wpDataDB.getAddr_id());
            }

            // ШЕВА ПРОСИТ 45 ТИП СЮДА ДОБАВИТЬ
            stackPhotoDBSList = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getPhotosByDAD2(dad2, 0)); // 0 - Фото Витрины
            List<StackPhotoDB> stackPhotoDBSList45 = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getPhotosByDAD2(dad2, 45)); // 0 - Фото Витрины
            stackPhotoDBSList.addAll(stackPhotoDBSList45);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPhotoShowcase/getDocumentVar", "Exception e: " + e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        try {
            //3.2. отметим фото для которых витрина определена (для цього використовую СпецКол, щоб не створювати окремоъ колонки)
            // Создаем счетчик для подсчета заполненных showcase_id
            int filledShowcaseIdsCount = 0;
            Set<String> uniqueExampleIds = new HashSet<>(); // Для уникальности example_id
            // Проверяем каждый showcase_id в списке stackPhotoDBSList
            for (StackPhotoDB stackPhotoDB : stackPhotoDBSList) {
                String showcaseIdStack = stackPhotoDB.showcase_id; // Получаем showcase_id из объекта StackPhotoDB
                // Проверяем, является ли showcase_id пустым или null, если нет, увеличиваем счетчик
                if (showcaseIdStack != null && !showcaseIdStack.isEmpty() && !showcaseIdStack.equals("0")) {
//                    stackPhotoDB.specialCol = 1;
                    filledShowcaseIdsCount++;
                    boolean isShowcaseIdPresent = stackPhotoDBSList.stream()
                            .map(StackPhotoDB::getShowcase_id)
                            .map(Integer::valueOf)
                            .anyMatch(showcaseId ->
                                    showcaseSDBList.stream()
                                            .anyMatch(showcaseSDB -> showcaseSDB.id.equals(showcaseId)));

                    if (isShowcaseIdPresent) {
                        String exampleId = stackPhotoDB.getExample_img_id();
                        // Проверяем example_id на уникальность и непустоту
                        if (exampleId != null && !exampleId.isEmpty() && uniqueExampleIds.add(exampleId)) {
                            list.add(stackPhotoDB);
                        }
                    }
                }
            }

            //3.3. підрахуємо відсоток світлин у котррих зазначениа вітрина
            try {
                percentValue = Math.round((float) (100 * list.size()) / showcaseSDBList.size());
            } catch (Exception e) {
                percentValue = 0;
                Globals.writeToMLOG("ERROR", "OptionControlPhotoShowcase/executeOption/percentValue", "Exception e: " + e);
            }

            try {
                perShowcase = (int) 100 * filledShowcaseIdsCount / showcaseSDBList.size();
            } catch (Exception e) {
                perShowcase = 0;
                Globals.writeToMLOG("ERROR", "OptionControlPhotoShowcase/executeOption/perShowcase", "Exception e: " + e);
            }


            //3.4
            int newTT = 0;
            if (stackPhotoDBSList.size() == 0 && list.size() == 0) {
                List<WpDataDB> wpSize = WpDataRealm.getWpDataBy(null, null, null, null, wpDataDB.getClient_id(), null);
                if (wpSize == null || wpSize.size() == 0) {
                    newTT = 1;
                }
            }


//            4.0
            if (stackPhotoDBSList.size() == 0) {
                stringBuilderMsg.append("Не можу знайти світлини стосовні до поточного відвідування.");
                signal = true;
            } /*else if (showcaseSDBList.size() == 0) {
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
            // (вітрина панорамна)
                }
            }*/ else if (showcaseSDBList.size() > 0 && filledShowcaseIdsCount == 0) {
                stringBuilderMsg.append("При виготовленні світлин Ви НЕ обрали жодної з ").append(showcaseSDBList.size()).append(" вітрин.");
                signal = true;
            } else if (colMin > 0 && percentValue < colMin && newTT == 0 && showcaseSDBList.size() > 0) {
                stringBuilderMsg.append("При виготовленні світлин, Ви зазначили вітрини лише у ")
                        .append(list.size())
                        .append(" фото з ")
                        .append(stackPhotoDBSList.size())
                        .append(" (")
                        .append(percentValue).append("%), що МЕНШЕ плану в ").append(colMin).append("%")
                        .append(" Загальна кількість вітрин на ТТ: ")
                        .append(showcaseSDBList.size())
                        .append(", з них фото зроблено ").append(list.size())
                        .append("(").append(perShowcase).append("%)");
                signal = true;
            } else if (showcaseSDBList.size() > 0 && list.size() < showcaseSDBList.size() * colMin / 100) {
                stringBuilderMsg.append("При виготовленні світлин, Ви сфотографували лише у ")
                        .append(list.size())
                        .append(" вітрин з ")
                        .append(showcaseSDBList.size()).append(" присутніх на ТТ (")
                        .append(percentValue)
                        .append("%), що МЕНШЕ плану в ")
                        .append(colMin).append("%.")
                        .append(" Усього зроблено фото ").append(stackPhotoDBSList.size());
                signal = false;

            } else if (showcaseSDBList.size() == 0 && list.size() == 0 && newTT == 1) {
                stringBuilderMsg.append("На момент виконання робіт, Вітрини по даному Кліенту/Адресі ще не визначені. Зауважень нема.");
                signal = true;

            } else if (showcaseSDBList.size() == 0 && list.size() == 0 && newTT == 0) {
                stringBuilderMsg.append("На момент виконання робіт, Вітрини по даному Кліенту/Адресі ще не визначені. " +
                        "Але роботи у ТТ вже виконувались раніше і Вітрини вже повинні були бути створені");
                signal = true;
            } else {
                stringBuilderMsg.append("При виготовленні світлин, Ви зазначили вітрини у ")
                        .append(list.size())
                        .append(" з ")
                        .append(showcaseSDBList.size())
                        .append(" присутнiх на ТТ")
                        .append(" (").append(percentValue)
                        .append("%), що БІЛЬШЕ плану в ").append(colMin).append("%.")
//                        .append(stackPhotoDBSList.size())
//                        .append(" СВІТЛИН ")
//                        .append(" Загальна кількість вітрин на ТТ: ")
//                        .append(showcaseSDBList.size())
//                        .append(" Усього зроблено фото ").append(list.size())
//                        .append("(").append(perShowcase).append("%)")
                        .append(" Зауважень немає.");
                signal = false;
            }

            //4.1. Виключення на випадок, якщо це перша/друга робота у даній ТТ з даним кліснтом
            if (signal) {
                List<DossierSotrSDB> dossierSotrSDBList = SQL_DB.dossierSotrDao().getData(null, 982L, wpDataDB.getCode_iza());
                if (!dossierSotrSDBList.isEmpty()) {
                    Long dataNR;
                    long dataWP = wpDataDB.getDt().getTime() / 1000;
                    if (dossierSotrSDBList.get(0).priznak > 31536000) { //31536000 -> 1971 год
                        dataNR = dossierSotrSDBList.get(0).priznak;
                    } else {
                        dataNR = dataWP;
                    }
                    if (dataNR > dataWP - (14 * 86400)) { // 86400 - 1 день в сек.
                        stringBuilderMsg.append(" але, роботи з цим ІЗА почали ");
                        stringBuilderMsg.append(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date(dataNR * 1000)));
                        stringBuilderMsg.append(". З цього моменту минуло менше двох тижнів, тому зроблено виключення.");
                        signal = false;
                    }
                } else {
                    stringBuilderMsg.append(" але, це перша робота поточного виконавця з зазначеним ІЗА, тому зроблено виключення.");
                    signal = false;
                }
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
            checkUnlockCode(optionDB);
//            if (signal) {
//                unlockCodeResultListener.onUnlockCodeFailure();
//            } else {
//                unlockCodeResultListener.onUnlockCodeSuccess();
//            }

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPhotoShowcase/executeOption", "Exception e: " + e);
        }
    }

    public String getCounter() {
        return list.size() + "/" + showcaseSDBList.size();
    }

}
