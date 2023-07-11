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
import java.util.Map;
import java.util.stream.Collectors;

import io.realm.annotations.Ignore;
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
    public boolean signal = false;
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


        Map<TovarGroupSDB, List<ReportPrepareDB>> groupData = workHorse.stream().collect(Collectors.groupingBy(data -> data.tovarGroupSDB));
        Log.e("OPTION_CONTROL_1455", "groupData: " + groupData.size());

        //4.0. формируем итоговую таблицу (объединяем Результат с Планом) и рассчитываем результат
        List<OptionResultTable> result = new ArrayList<>();
        for (Map.Entry<TovarGroupSDB, List<ReportPrepareDB>> item : groupData.entrySet()) {
            String codeZASG = "" + clientId + addressId + item.getKey().id;

            OptionResultTable optionResultTable = new OptionResultTable();
            optionResultTable.grpId = item.getKey().id;
            optionResultTable.grp = item.getKey().nm;


            optionResultTable.face = item.getValue().stream().map(table -> Integer.parseInt(table.face)).reduce(0, Integer::sum);
            optionResultTable.colSKU = item.getValue().stream().map(table -> table.colSKU).reduce(0, Integer::sum);
            optionResultTable.sizePPF = item.getValue().stream().map(table -> table.shelfSpaceLength).reduce(0d, Double::sum);


//            optionResultTable.plannedShare = null;
//            optionResultTable.shareActual = null;
//            optionResultTable.deflection = null;
//            optionResultTable.deficit = null;
//            optionResultTable.widthPPO = null;
//            optionResultTable.note = null;


            if (shelfSize.stream().filter(itm -> itm.codeZASG.equals(codeZASG)).findFirst().orElse(null) != null) {
                ShelfSizeSDB shelfSizeSDB = shelfSize.stream().filter(itm -> itm.codeZASG.equals(codeZASG)).findFirst().get();
//                ShelfSizeSDB shelfSizeSDB = shelfSize.get(0);
                if (shelfSizeSDB.width == 0) {
                    optionResultTable.plannedShare = Double.valueOf(shelfSizeSDB.planzn);
                    optionResultTable.shareActual = 0d;
                    optionResultTable.deflection = 0d;
                    optionResultTable.deficit = 0;
                    optionResultTable.note = " для (" + optionResultTable.grp + ") не определена Общая Длина ПП (ДППО)";
                } else {
                    optionResultTable.widthPPO = Double.valueOf(shelfSizeSDB.width);
                    optionResultTable.plannedShare = Double.valueOf(shelfSizeSDB.planzn);
                    optionResultTable.shareActual = 100 * optionResultTable.sizePPF / optionResultTable.widthPPO;
                    optionResultTable.deflection = optionResultTable.shareActual - optionResultTable.plannedShare;
                    optionResultTable.deficit = optionResultTable.deflection < -10 ? 1 : 0;
                    optionResultTable.note = optionResultTable.deficit == 1 ? " (" + optionResultTable.grp + ") НЕ выполнен план "
                            + optionResultTable.plannedShare + "%: (" + String.format("%.2f", optionResultTable.widthPPO * optionResultTable.plannedShare / 100) + " м.), а факт: "
                            + String.format("%.1f", optionResultTable.shareActual) + "% (" + String.format("%.2f", optionResultTable.sizePPF) + " м. из " + String.format("%.2f", optionResultTable.widthPPO) + " м.) " : "";
                }
            } else {
                optionResultTable.plannedShare = 0d;
                optionResultTable.shareActual = 0d;
                optionResultTable.deflection = 0d;
                optionResultTable.deficit = 0;
                optionResultTable.note = "для (" + optionResultTable.grp + ") не определена Общая Длина ПП (ДППО)";
            }
            noteOC1455.append(optionResultTable.note).append("");


            result.add(optionResultTable);
        }

        int deficitSum = result.stream().map(table -> table.deficit).reduce(0, Integer::sum);

        //5.0. подведем итог
        if (result.size() == 0){
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

    private class OptionResultTable {
        public Integer grpId;
        public String grp;
        public String note;
        /*
         * 19.01.23.
         * Используется в опции контроля 1455
         * Длина полочного пространства. Должна расчитываться: ( Товар.ширина * Фейс / 1000 )
         * */
        @Ignore
        public Integer shelfSpaceLength;

        /*
         * 19.01.23.
         * Используется в опции контроля 1455
         * ДлинаППО. Общая ДЛИНА ПП всей категории в ТТ (включая конкурентов) (м)
         * */
        @Ignore
        public Double widthPPO;

        /*
         * 19.01.23.
         * Используется в опции контроля 1455
         * ДоляПлан. Плановая ДОЛЯ ПП которую ДОЛЖЕН занимать товар КЛИЕНТА в ТТ (%)
         * */
        @Ignore
        public Double plannedShare;

        /*
         * 19.01.23.
         * Используется в опции контроля 1455
         * ДоляФакт. Фактическая ДОЛЯ ПП которую ЗАНИМАЕТ товар КЛИЕНТА в ТТ (%)
         * */
        @Ignore
        public Double shareActual;

        /*
         * 19.01.23.
         * Используется в опции контроля 1455
         * Отклонение. Отклонение ФАКТИЧЕСКОЙ ДОЛИ ПП от ПЛАНОВОЙ (%)
         * */
        @Ignore
        public Double deflection;

        /*
         * 19.01.23.
         * Используется в опции контроля 1455
         * Недочёт.
         * */
        @Ignore
        public Integer deficit;
        public Integer face;
        public Integer colSKU;
        public Double sizePPF;
    }


}
