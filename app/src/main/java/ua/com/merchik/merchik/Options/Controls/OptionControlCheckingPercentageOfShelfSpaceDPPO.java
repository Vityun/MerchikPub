package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.ShelfSizeSDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupClientSDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;

/**
 * 17.01.23.
 * Опция Контроля: Проверка доли полочного пространства (1455)
 * <p>
 * Сравнивает ПЛАНОВЫЙ показатель ДОЛИ ПП с ФАКТИЧЕСКИМ (расчетным)
 * DPPO - длина полочного пространства
 */
public class OptionControlCheckingPercentageOfShelfSpaceDPPO<T> extends OptionControl {
    public int OPTION_CONTROL_CHECK_PERCENTAGE_OF_SHELF_SPACE_ID = 1455;

    // data option
    private boolean signal = false;
    private StringBuilder noteOC1455 = new StringBuilder();

    // document data
    private long dad2 = 0;
    private Date date;
    private String clientId;
    private Integer addressId;

    private UsersSDB usersSDB;
    private CustomerSDB customerSDB;
    private AddressSDB addressSDB;
    private TovarGroupSDB tovarGroupSDB;
    private TovarGroupClientSDB tovarGroupClientSDB;

    public OptionControlCheckingPercentageOfShelfSpaceDPPO(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        try {
            Log.e("OPTION_CONTROL_1455", "OptionControlCheckingPercentageOfShelfSpaceDPPO");
            Globals.writeToMLOG("INFO", "OPTION_CONTROL_1455", "OptionControlCheckingPercentageOfShelfSpaceDPPO");
            this.context = context;
            this.document = document;
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;
            getDocumentVar();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                executeOption();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Build.VERSION.SDK_INT: ").append(Build.VERSION.SDK_INT).append("  Build.VERSION_CODES.N: ").append(Build.VERSION_CODES.N);
                Globals.writeToMLOG("INFO", "OPTION_CONTROL_1455", "sb: " + sb);
            }
        }catch (Exception e){
            Log.e("OPTION_CONTROL_1455", "Exception e: " + e);

            Globals.writeToMLOG("ERROR", "OPTION_CONTROL_1455", "Exception e: " + e);

            e.printStackTrace();
        }
    }

    private void getDocumentVar() {
        Log.e("OPTION_CONTROL_1455", "getDocumentVar");
        Globals.writeToMLOG("INFO", "OPTION_CONTROL_1455", "getDocumentVar");
        if (document instanceof WpDataDB) {
            WpDataDB wp = (WpDataDB) document;

            dad2 = wp.getCode_dad2();
            date = wp.getDt();
            clientId = wp.getClient_id();
            addressId = wp.getAddr_id();

            usersSDB = SQL_DB.usersDao().getById(wp.getUser_id());
            customerSDB = SQL_DB.customerDao().getById(wp.getClient_id());
            addressSDB = SQL_DB.addressDao().getById(wp.getAddr_id());
            tovarGroupClientSDB = SQL_DB.tovarGroupClientDao().getByAddrId(addressSDB.tpId);
            if (tovarGroupClientSDB != null){
                tovarGroupSDB = SQL_DB.tovarGroupDao().getById(tovarGroupClientSDB.tovarGrpId);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        Log.e("OPTION_CONTROL_1455", "executeOption");
        Globals.writeToMLOG("INFO", "OPTION_CONTROL_1455", "executeOption");

        // Получаем RP(товары) для дальнейшего анализа.
        List<ReportPrepareDB> reportPrepare = ReportPrepareRealm.joinWithTovarsAndTovGroups(RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(dad2)));
        Log.e("OPTION_CONTROL_1455", "reportPrepare: " + reportPrepare);

        Globals.writeToMLOG("INFO", "OPTION_CONTROL_1455", "reportPrepare: " + new Gson().toJson(reportPrepare));

        //2.2. получим список категорий, сперва удалив пустые
        List<ReportPrepareDB> workHorse = new ArrayList<>();
        for (ReportPrepareDB item : reportPrepare) {
            if (item.tovarGroupSDB != null) {
                workHorse.add(item);
            }
        }
        Log.e("OPTION_CONTROL_1455", "workHorse: " + new Gson().toJson(workHorse));
        Log.e("OPTION_CONTROL_1455", "workHorse size: " + workHorse.size());

        Globals.writeToMLOG("INFO", "OPTION_CONTROL_1455", "workHorse: " + new Gson().toJson(workHorse));
        Globals.writeToMLOG("INFO", "OPTION_CONTROL_1455", "workHorse size: " + workHorse.size());

        //3.0. получим данные о ОБЩИХ ДЛИНАХ ПП соответствующих категорий
        List<ShelfSizeSDB> shelfSize = SQL_DB.shelfSizeDao().getBy(clientId, addressId);
        for (ShelfSizeSDB item : shelfSize) {
            item.codeZASG = "" + item.clientId + item.addrId + item.grpId;
        }

        Log.e("OPTION_CONTROL_1455", "shelfSize size: " + shelfSize.size());
        Globals.writeToMLOG("INFO", "OPTION_CONTROL_1455", "shelfSize size: " + shelfSize.size());

        //4.0. формируем итоговую таблицу (объединяем Результат с Планом) и рассчитываем результат
        for (ReportPrepareDB item : workHorse) {
            String codeZASG = "" + clientId + addressId + item.tovarGroupSDB.id;
            if (shelfSize.stream().filter(itm -> itm.codeZASG.equals(codeZASG)).findFirst().orElse(null) != null) {
                ShelfSizeSDB shelfSizeSDB = shelfSize.get(0);
                if (shelfSizeSDB.width == 0) {
                    item.plannedShare = Double.valueOf(shelfSizeSDB.planzn);
                    item.shareActual = 0d;
                    item.deflection = 0d;
                    item.deficit = 0;
                    item.note = " для (" + item.tovarGroupSDB.nm + ") не определена Общая Длина ПП (ДППО)";
                } else {
                    item.widthPPO = Double.valueOf(shelfSizeSDB.width);
                    item.plannedShare = Double.valueOf(shelfSizeSDB.planzn);
                    item.shareActual = 100 * item.tovarDB.width / item.widthPPO;
                    item.deflection = item.shareActual - item.plannedShare;
                    item.deficit = item.deflection < -10 ? 1 : 0;
                    item.note = item.deficit == 1 ? "(" + item.tovarGroupSDB.nm + ") НЕ выполнен план "
                            + item.plannedShare + "%: (" + item.widthPPO * item.plannedShare / 100 + " м.), а факт: "
                            + item.shareActual + "% (" + item.tovarDB.width + " м. из " + item.widthPPO + " м.) " : "Недоточ=0";
                }
            } else {
                item.plannedShare = 0d;
                item.shareActual = 0d;
                item.deflection = 0d;
                item.deficit = 0;
                item.note = "для (" + item.tovarGroupSDB.nm + ") не определена Общая Длина ПП (ДППО)";
            }
            noteOC1455.append(item.note).append("\n");
        }

        int deficitSum = workHorse.stream().map(table -> table.deficit).reduce(0, Integer::sum);

        //5.0. подведем итог
        if (workHorse.size() == 0){
            stringBuilderMsg.append("Данные о Длине ПП не заполнены ни для одной товарной группы!");
            signal = true;
        }else if (deficitSum > 0) {
            stringBuilderMsg.append("Для (").append(deficitSum).append(") категорий товаров не выполнены планы: ").append(noteOC1455);
            signal = true;
        }else {
            stringBuilderMsg.append("Замечаний по выполнению Планов ДПП нет.");
            signal = false;
        }

        Log.e("OPTION_CONTROL_1455", "noteOC1455: " + noteOC1455);
        Globals.writeToMLOG("INFO", "OPTION_CONTROL_1455", "noteOC1455: " + noteOC1455);

        saveOptionResultInDB();
        if (signal) {
            if (optionDB.getBlockPns().equals("1")) {
                setIsBlockOption(signal);
                stringBuilderMsg.append("\n\n").append("Документ проведен не будет!");
            } else {
                stringBuilderMsg.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете вносить корректно ДПП.");
            }
        }

        Log.e("OPTION_CONTROL_1455", "stringBuilderMsg: " + stringBuilderMsg);
        Globals.writeToMLOG("INFO", "OPTION_CONTROL_1455", "stringBuilderMsg: " + stringBuilderMsg);
    }


    /**
     * Сохранение данных об опции контроля в БД
     */
    private void saveOptionResultInDB() {
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
    }


}
