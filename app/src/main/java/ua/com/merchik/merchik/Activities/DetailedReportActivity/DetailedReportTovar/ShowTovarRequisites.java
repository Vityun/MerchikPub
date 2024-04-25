package ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar;

import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA;
import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA_ID;
import static ua.com.merchik.merchik.Globals.OptionControlName.DT_EXPIRE;
import static ua.com.merchik.merchik.Globals.OptionControlName.ERROR_ID;
import static ua.com.merchik.merchik.Globals.OptionControlName.PHOTO;
import static ua.com.merchik.merchik.Globals.OptionControlName.UP;
import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Date;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.DoubleSpinner;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.EditTextAndSpinner;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Number;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Text;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.Utils.MySimpleExpandableListAdapter;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.RealmModels.ErrorDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.PromoDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm;
import ua.com.merchik.merchik.database.realm.tables.PromoRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public class ShowTovarRequisites {

    private Context context;
    private WpDataDB wpDataDB;
    private TovarDB tovarDB;

    //    private List<AdditionalRequirementsDB> adList;
    boolean openNext = true;


    private final Options options = new Options();
    private final TovarRequisites tovarRequisites = new TovarRequisites();

    private List<DialogData> dialogList = new ArrayList<>();

    List<TovarOptions> tovOptTplList;

    public ShowTovarRequisites(Context context, WpDataDB wpDataDB, TovarDB tovarDB) {
        this.context = context;
        this.wpDataDB = wpDataDB;
        this.tovarDB = tovarDB;
    }

    public void showDialogs() {
        boolean finalDeletePromoOption = true;  // true - потому что так захотел

        ReportPrepareDB reportPrepareTovar = RealmManager.getTovarReportPrepare(String.valueOf(wpDataDB.getCode_dad2()), tovarDB.getiD());
        List<OptionsDB> optionsList2 = RealmManager.getTovarOptionInReportPrepare(String.valueOf(wpDataDB.getCode_dad2()), tovarDB.getiD());
        tovOptTplList = options.getRequiredOptionsTPL(optionsList2, finalDeletePromoOption);

        if (tovOptTplList.size() > 0) {
            // В Цикле открываем Н количество инфы
            for (int i = tovOptTplList.size() - 1; i >= 0; i--) {
                if (tovOptTplList.get(i).getOptionControlName() != Globals.OptionControlName.AKCIYA) {
                    if (tovOptTplList.get(i).getOptionControlName().equals(AKCIYA_ID) && finalDeletePromoOption) {
                        // втыкаю
                        showDialog(tovarDB, tovOptTplList.get(i), reportPrepareTovar, tovarDB.getiD(), String.valueOf(wpDataDB.getCode_dad2()), wpDataDB.getClient_id(), "", "", true);
                    } else {
                        showDialog(tovarDB, tovOptTplList.get(i), reportPrepareTovar, tovarDB.getiD(), String.valueOf(wpDataDB.getCode_dad2()), wpDataDB.getClient_id(), "", "", true);
                    }
                }
            }


            Collections.reverse(dialogList);

            boolean optionExists = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                String opt = "159707";
                optionExists = optionsList2.stream().anyMatch(
                        optionsDB -> optionsDB.getOptionId().equals(opt) ||
                                optionsDB.getOptionControlId().equals(opt));

                if (optionExists) {

                    Optional<OptionsDB> matchingOption = optionsList2.stream()
                            .filter(optionsDB ->
                                    optionsDB.getOptionId().equals(opt) ||
                                            optionsDB.getOptionControlId().equals(opt))
                            .findFirst();

                    if (matchingOption.isPresent()) {
                        OptionsDB optionsDB = matchingOption.get();
                        // Делайте что-то с объектом OptionsDB
                        System.out.println(optionsDB);
                        dialogList.add(new TovarRequisites(tovarDB, reportPrepareTovar).createDialog(context, wpDataDB, optionsDB, () -> {
                        }));
                    } else {
                        // Обработка случая, когда объект OptionsDB не найден
                        System.out.println("Объект OptionsDB не найден");
                    }
                }
            }
        }

        dialogList.get(0).show();
    }


    //    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showDialog(TovarDB list, TovarOptions tpl, ReportPrepareDB reportPrepareDB, String tovarId, String cd2, String clientId, String finalBalanceData1, String finalBalanceDate1, boolean clickType) {
        try {
            DialogData dialog = new DialogData(context);
            dialog.setTitle("");
            dialog.setText("");
            dialog.setClose(() -> {
                closeDialogRule(dialog, dialog::dismiss);    // Особенное правило закрытия для модального окна с Акцией
            });
            dialog.setLesson(context, true, 802);
            dialog.setVideoLesson(context, true, 803, null, null);
            dialog.setImage(true, tovarRequisites.getPhotoFromDB(list));
            dialog.setAdditionalText(tovarRequisites.setPhotoInfo(reportPrepareDB, tpl, list, finalBalanceData1, finalBalanceDate1));

            // Сделано для того что б можно было контролировать какая опция сейчас открыта
            dialog.tovarOptions = tpl;
            dialog.reportPrepareDB = reportPrepareDB;

            // Устанавливаем дату для операций (в данной реализации только для DoubleSpinner & EditTextAndSpinner)
            switch (tpl.getOptionControlName()) {
                case AKCIYA_ID:
                    dialog.setOperationSpinnerData(setMapData(tpl.getOptionControlName()));
                    dialog.setOperationSpinner2Data(setMapData(Globals.OptionControlName.AKCIYA));

                    PromoDB promoDB = PromoRealm.getPromoDBById(reportPrepareDB.getAkciyaId());
                    dialog.setOperationTextData(promoDB != null ? promoDB.getNm() : reportPrepareDB.getAkciyaId());

                    Map<Integer, String> map = new HashMap<>();
                    map.put(2, "Акция отсутствует");
                    map.put(1, "Есть акция");

                    String akciya = map.get(Integer.parseInt(reportPrepareDB.getAkciya()));

                    dialog.setOperationTextData2(akciya);
                    break;
            }

            if (tpl.getOptionControlName() != null && tpl.getOptionControlName().equals(ERROR_ID)) {    // Работа с ошибками
                String groupPos = null;
                boolean containsOptionId = false;
                boolean containsOptionId2 = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    containsOptionId = tovOptTplList.stream().anyMatch(tovarOptions -> tovarOptions.getOptionId().contains(135591));
                    containsOptionId2 = tovOptTplList.stream().anyMatch(tovarOptions -> tovarOptions.getOptionId().contains(157241));
                }
                if (containsOptionId) {
                    groupPos = "22";
                }

                if (containsOptionId2) {
                    groupPos = "13";
                }
                dialog.setExpandableListView(createExpandableAdapter(dialog.context, groupPos), () -> {
                    if (dialog.getOperationResult() != null) {
                        operetionSaveRPToDB(tpl, reportPrepareDB, dialog.getOperationResult(), dialog.getOperationResult2(), null);
                        dialogShowRule2(list, tpl, reportPrepareDB, tovarId, cd2, clientId, finalBalanceData1, finalBalanceDate1, clickType);
                    }
                });
            } else {
                dialog.setOperation(operationType(tpl), getCurrentData(tpl, cd2, tovarId), setMapData(tpl.getOptionControlName()), () -> {
                    // Сделал удаление даты тут потому что 08.04.24. в setOperation пришлось это убрать.
                    // Мне надо проверять корректность внесенной даты и отталкиваясь от этого выводить модальное окно или нет.
                    OptionsDB option = OptionsRealm.getOption(String.valueOf(cd2), "165276");
                    if (option != null && operationType(tpl).equals(Date)) {
                        openNext = false;

                        long tovExpirationDate = list.expirePeriod * 86400;         // термін придатності товару. (дни перевожу в секунды)
                        long dtCurrentWPData = wpDataDB.getDt().getTime() / 1000;   // дата посещения в секундах
                        long dtUserSetToTovar = 0;                                  // То что указал в Дате окончания срока годности мерчик
                        long resDays = 0;                                           // Дата текущего посещения + срок годности товара

                        if (dialog.getOperationResult() != null && !dialog.getOperationResult().equals("")) {
                            dtUserSetToTovar = Clock.dateConvertToLong(dialog.getOperationResult()) / 1000;
                        }

                        resDays = dtCurrentWPData + tovExpirationDate;
                        int exPer = list.expirePeriod;
                        if (exPer != 0 && dtUserSetToTovar > resDays) {
                            DialogData dialogBadData = new DialogData(dialog.context);
                            dialogBadData.setTitle("Зауваження до Дати");
                            dialogBadData.setText("Ви внесли некоректну дату закінчення терміну придатності! Відмовитись від її збереження?");
                            dialogBadData.setOk("Так", () -> {
                                dialogBadData.dismiss();
                                Toast.makeText(context, "Дата не збережена!", Toast.LENGTH_LONG).show();
                            });
                            dialogBadData.setCancel("Ні", () -> {
                                dialogBadData.dismiss();
                                pushOkButtonRequisites(tpl, reportPrepareDB, dialog, cd2, list, tovarId, clientId, finalBalanceData1, finalBalanceDate1, clickType);
                            });
                            dialogBadData.setClose(dialogBadData::dismiss);
                            dialogBadData.show();
                        } else {
                            openNext = true;
                            dialog.dismiss();
                        }
                    } else if (operationType(tpl).equals(Date)) {
                        dialog.dismiss();
                    }

                    if (openNext && dialog.getOperationResult() != null && !dialog.getOperationResult().equals("")) {

                        pushOkButtonRequisites(tpl, reportPrepareDB, dialog, cd2, list, tovarId, clientId, finalBalanceData1, finalBalanceDate1, clickType);

                        // 08.04.24. Перенес это в отдельную функцию pushOkButtonRequisites
//                            operetionSaveRPToDB(tpl, reportPrepareDB, dialog.getOperationResult(), dialog.getOperationResult2(), null);
//                            Toast.makeText(mContext, "Внесено: " + dialog.getOperationResult(), Toast.LENGTH_LONG).show();
//                            refreshElement(cd2, list.getiD());
//                            dialogShowRule(list, tpl, reportPrepareDB, tovarId, cd2, clientId, finalBalanceData1, finalBalanceDate1, clickType);
                    } else {
                        Toast.makeText(dialog.context, "Внесите корректно данные", Toast.LENGTH_LONG).show();
                    }
                });
            }

            dialog.setCancel("Пропустить", () -> closeDialogRule(dialog, () -> {
                dialog.dismiss();
                dialogShowRule2(list, tpl, reportPrepareDB, tovarId, cd2, clientId, finalBalanceData1, finalBalanceDate1, clickType);
            }));

            dialogList.add(dialog);

        } catch (Exception e) {
            Log.d("test", "test" + e);
        }
    }


    /**
     * 30.03.23.
     * Уникальное событие для Акций.
     * Если модальное окно для внесения Акции и внесён один из реквизитов - запрещаю что-то
     * делать.
     */
    private void closeDialogRule(DialogData dialog, Clicks.clickVoid click) {
        if (dialog.tovarOptions.getOptionControlName().equals(AKCIYA) || dialog.tovarOptions.getOptionControlName().equals(AKCIYA_ID)) {
            if ((dialog.getOperationResult() == null && dialog.getOperationResult2() != null) ||
                    (dialog.getOperationResult() != null && dialog.getOperationResult2() == null) ||
                    ((dialog.getOperationResult() != null && (dialog.getOperationResult().equals("") || dialog.getOperationResult().equals("0"))) &&
                            (dialog.getOperationResult2() != null && (!dialog.getOperationResult2().equals("") && !dialog.getOperationResult2().equals("0")))
                    ) ||
                    ((dialog.getOperationResult2() != null && (dialog.getOperationResult2().equals("") || dialog.getOperationResult2().equals("0"))) &&
                            (dialog.getOperationResult() != null && (!dialog.getOperationResult().equals("") && !dialog.getOperationResult().equals("0")))
                    )
            ) {
                Toast.makeText(dialog.context, "Внесіть, будь-ласка, обидва реквізити!", Toast.LENGTH_LONG).show();
            } else {
                click.click();
            }
        } else {
            click.click();
        }
    }


    /**
     * 29.03.23.
     * Специальное правило по которому отображаю последовательно модальные окошки из
     * списка dialogList.
     */
    private void dialogShowRule(boolean clickType) {
        ReportPrepareDB report = dialogList.get(0).reportPrepareDB;
        dialogList.remove(0);
        if (dialogList.size() > 0) {
            dialogList.get(0).reportPrepareDB = report;
            int face = 0;
            if (dialogList.get(0).reportPrepareDB.face != null && !dialogList.get(0).reportPrepareDB.face.equals(""))
                face = Integer.parseInt(dialogList.get(0).reportPrepareDB.face);
            if (clickType &&
                    dialogList.get(0).tovarOptions.getOptionControlName().equals(ERROR_ID) &&
                    (dialogList.get(0).tovarOptions.getOptionId().contains(157242) ||
                            dialogList.get(0).tovarOptions.getOptionId().contains(157241) ||
                            dialogList.get(0).tovarOptions.getOptionId().contains(157243)) &&
                    face > 0) {
                // НЕ отображаю модальное окно и удаляю его. Уникальное правило потому что потому.
                dialogList.remove(0);
                if (dialogList.size() > 0) {
                    dialogList.get(0).show();
                }
            } else if (clickType &&
                    (dialogList.get(0).tovarOptions.getOptionControlName().equals(UP) ||
                            dialogList.get(0).tovarOptions.getOptionControlName().equals(DT_EXPIRE)) &&
                    face == 0) {
                dialogList.remove(0);
                if (dialogList.size() > 0) {
                    dialogList.get(0).show();
                }
            } else if (clickType &&
                    dialogList.get(0).tovarOptions.getOptionControlName().equals(PHOTO) &&
//                        dialogList.get(0).tovarOptions.getOptionId().contains(159707) &&
                    face != 0
            ) {
                dialogList.remove(0);
                if (dialogList.size() > 0) {
                    dialogList.get(0).show();
                }
            } else if (clickType &&
                    dialogList.get(0).tovarOptions.getOptionControlName().equals(DT_EXPIRE) &&
                    dialogList.get(0).tovarOptions.getOptionId().contains(135591) &&
                    face > 0
            ) {
                dialogList.remove(0);
                if (dialogList.size() > 0) {
                    dialogList.get(0).show();
                }
            } else {
                dialogList.get(0).show();
            }
        }
    }

    private void dialogShowRule2(TovarDB list, TovarOptions tpl, ReportPrepareDB reportPrepareDB, String tovarId, String cd2, String clientId, String finalBalanceData1, String finalBalanceDate1, boolean clickType) {
        Log.e("dialogShowRule", "clickType: " + clickType);
        ReportPrepareDB report = dialogList.get(0).reportPrepareDB;
        dialogList.remove(0);

        boolean option165276 = false;
        OptionsDB option = OptionsRealm.getOption(String.valueOf(cd2), "165276");
        if (option != null) option165276 = true;

        if (dialogList.size() > 0) {
            dialogList.get(0).reportPrepareDB = report;
            int face = 0;
            if (dialogList.get(0).reportPrepareDB.face != null && !dialogList.get(0).reportPrepareDB.face.equals(""))
                face = Integer.parseInt(dialogList.get(0).reportPrepareDB.face);

            if (clickType &&
                    dialogList.get(0).tovarOptions.getOptionControlName().equals(ERROR_ID) &&
                    (dialogList.get(0).tovarOptions.getOptionId().contains(157242) ||
                            dialogList.get(0).tovarOptions.getOptionId().contains(157241) ||
                            dialogList.get(0).tovarOptions.getOptionId().contains(157243)) &&
                    face > 0) {
                // НЕ отображаю модальное окно и удаляю его. Уникальное правило потому что потому.
                dialogList.remove(0);
                if (dialogList.size() > 0) {
                    dialogList.get(0).show();
                }
            } else if (clickType &&
                    (dialogList.get(0).tovarOptions.getOptionControlName().equals(UP) ||
                            dialogList.get(0).tovarOptions.getOptionControlName().equals(DT_EXPIRE)) &&
                    face == 0) {
                dialogList.remove(0);
                if (dialogList.size() > 0) {
                    dialogList.get(0).show();
                }
            } /*else if (clickType &&
                        dialogList.get(0).tovarOptions.getOptionControlName().equals(PHOTO) &&
//                        dialogList.get(0).tovarOptions.getOptionId().contains(159707) &&
                        face != 0
                ) {
                    dialogList.remove(0);
                    if (dialogList.size() > 0) {
                        dialogList.get(0).show();
                    }
                }*/ else if (clickType && (
                    dialogList.get(0).tovarOptions.getOptionControlName().equals(AKCIYA_ID) ||
                            dialogList.get(0).tovarOptions.getOptionControlName().equals(AKCIYA)
            )
            ) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//                    Optional<AdditionalRequirementsDB> result;
//                    result = adList.stream()
//                            .filter(obj -> obj.getOptionId().equals("80977"))
//                            .findFirst();

//                        AdditionalRequirementsDB foundObject = result.get();

//                    if (result.isPresent()) {
//                        // Делайте что-то с найденным объектом
//                        dialogList.get(0).show();
//                    } else {
                    // Обработка случая, когда объект не найден
//                            dialogList.get(0).show();
                    dialogList.remove(0);
                    if (dialogList.size() > 0) {
                        dialogList.get(0).show();
//                        }
                    }
                }


            } else if (clickType
                    && option165276
                    && face > 0
                    && !tpl.getOptionShort().equals("Ш")
            ) {
                OptionsDB optionsDB = OptionsRealm.getOption(String.valueOf(cd2), "165276");
                if ((dialogList.get(0).reportPrepareDB.dtExpire != null
                        && !dialogList.get(0).reportPrepareDB.dtExpire.equals("")
                        && !dialogList.get(0).reportPrepareDB.dtExpire.equals("0000-00-00"))
                        && optionsDB != null
                ) {
                    if (optionsDB.getAmountMax() != null && !optionsDB.getAmountMax().equals("")) {
                        int max = Integer.parseInt(optionsDB.getAmountMax());
                        int colMax = max == 0 ? 30 : max;

                        long dat = wpDataDB.getDt().getTime() / 1000;
                        long colMaxLong = colMax * 86400L;
                        long optionControlDate = dat + colMaxLong;
                        long reportDate = 0;
                        if (dialogList.get(0).reportPrepareDB.dtExpire != null && !dialogList.get(0).reportPrepareDB.dtExpire.equals("") && !dialogList.get(0).reportPrepareDB.dtExpire.equals("0000-00-00")) {
                            reportDate = Clock.dateConvertToLong(dialogList.get(0).reportPrepareDB.dtExpire) / 1000;
                        }

                        // Если ДАТА плохая:
                        if (reportDate <= optionControlDate) {
                            // Мы смотрим на ВОЗВРАТ и ЕСЛИ он 0 - Выводим ОШИБКУ
                            if (dialogList.get(0).reportPrepareDB.expireLeft != null
                                    && (dialogList.get(0).reportPrepareDB.expireLeft.equals("0") || dialogList.get(0).reportPrepareDB.expireLeft.equals(""))) {

                                boolean existError = false;
                                try {
                                    for (DialogData item : dialogList) {
                                        if (item.tovarOptions.getOptionShort().equals("Ш")) {
                                            existError = true;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("dialogShowRule", "Exception e: " + e);
                                }

                                if (!existError && tpl.getOptionShort().equals("В")) {
                                    TovarOptions to = new TovarOptions(ERROR_ID, "Ш", "Ошибка товара", "error_id", "main", 135592, 157242);
                                    showDialog(list, to, reportPrepareDB, tovarId, String.valueOf(cd2), clientId, finalBalanceData1, finalBalanceDate1, true);
                                    dialogList.remove(0);
                                    if (dialogList.size() > 0 /*&& dialogList.get(0).tovarOptions.getOptionShort().equals("P")*/) {
                                        Collections.swap(dialogList, 0, 1);
                                        dialogList.get(0).show();
                                    }
                                } else {
                                    dialogList.remove(0);
                                    if (dialogList.size() > 0) {
                                        dialogList.get(0).show();
                                    }
                                }

                                // Отображаем то что у нас дальше
                            } else {
                                dialogList.remove(0);
                                if (dialogList.size() > 0) {
                                    dialogList.get(0).show();
                                }
                            }
                            // Если ДАТА Хорошая
                        } else {

                            if (tpl.getOptionShort().equals("Д")) {  // Если текущее окно - ДАТА
                                // Удаляем Возврат
                                try {
                                    for (DialogData item : dialogList) {
                                        if (item.tovarOptions.getOptionShort().equals("В")) {
                                            dialogList.remove(item);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("dialogShowRule", "Exception e: " + e);
                                }
                            }


                            // Тут мы не должны указывать ВОЗВРАТ
                            dialogList.get(0).show();
                        }
                    }
                } else {
                    dialogList.get(0).show();
                }
            } else if (clickType &&
                    dialogList.get(0).tovarOptions.getOptionControlName().equals(PHOTO) &&
//                        dialogList.get(0).tovarOptions.getOptionId().contains(159707) &&
                    face != 0
            ) {
                dialogList.remove(0);
                if (dialogList.size() > 0) {
                    dialogList.get(0).show();
                }
            } else {
                dialogList.get(0).show();
            }
        }
    }


    private Map<Integer, String> setMapData(Globals.OptionControlName optionControlName) {
        Map<Integer, String> map = new HashMap<>();
        switch (optionControlName) {
            case ERROR_ID:
                RealmResults<ErrorDB> errorDbList = RealmManager.getAllErrorDb();
                for (int i = 0; i < errorDbList.size(); i++) {
                    if (errorDbList.get(i).getNm() != null && !errorDbList.get(i).getNm().equals("")) {
                        map.put(Integer.valueOf(errorDbList.get(i).getID()), errorDbList.get(i).getNm());
                    }
                }
                return map;

            case AKCIYA_ID:
                RealmResults<PromoDB> promoDbList = RealmManager.getAllPromoDb();
                for (int i = 0; i < promoDbList.size(); i++) {
                    if (promoDbList.get(i).getNm() != null && !promoDbList.get(i).getNm().equals("")) {
                        map.put(Integer.valueOf(promoDbList.get(i).getID()), promoDbList.get(i).getNm());
                    }
                }

                map.put(0, "Оберіть тип акції");

                return map;

            case AKCIYA:
                map.put(2, "Акция отсутствует");
                map.put(1, "Есть акция");

                map.put(0, "Оберіть наявність акції");

                return map;

            default:
                return null;
        }
    }


    private MySimpleExpandableListAdapter createExpandableAdapter(Context context, String groupPos) {

        Map<String, String> map;
        ArrayList<Map<String, String>> groupDataList = new ArrayList<>();

        // список атрибутов групп для чтения
        String[] groupFrom = new String[]{"groupName"};
        // список ID view-элементов, в которые будет помещены атрибуты групп
        int groupTo[] = new int[]{android.R.id.text1};

        // список атрибутов элементов для чтения
        String childFrom[] = new String[]{"itemName"};
        // список ID view-элементов, в которые будет помещены атрибуты
        // элементов
        int childTo[] = new int[]{android.R.id.text1};

        // создаем общую коллекцию для коллекций элементов
        ArrayList<ArrayList<Map<String, String>>> сhildDataList = new ArrayList<>();
        // создаем коллекцию элементов для первой группы
        ArrayList<Map<String, String>> сhildDataItemList = new ArrayList<>();

        // Получение данных с БД
        RealmResults<ErrorDB> errorDbList = RealmManager.getAllErrorDb();
        RealmResults<ErrorDB> errorGroupsDB = errorDbList.where().equalTo("parentId", "0").findAll();

        for (ErrorDB group : errorGroupsDB) {
            map = new HashMap<>();
            map.put("groupName", group.getNm());
            map.put("groupId", group.getID());

            groupDataList.add(map);

            RealmResults<ErrorDB> errorItemsDB = errorDbList.where().equalTo("parentId", group.getID()).findAll();
            if (errorItemsDB != null && errorItemsDB.size() > 0) {
                сhildDataItemList = new ArrayList<>();
                for (ErrorDB item : errorItemsDB) {
                    map = new HashMap<>();
                    map.put("itemName", "* " + item.getNm());
                    сhildDataItemList.add(map);
                }
                сhildDataList.add(сhildDataItemList);
            } else {
                сhildDataItemList = new ArrayList<>();
                map = new HashMap<>();
                map.put("itemName", "* " + group.getNm());
                сhildDataItemList.add(map);
                сhildDataList.add(сhildDataItemList);
            }
        }

        MySimpleExpandableListAdapter adapter = new MySimpleExpandableListAdapter(
                context, groupDataList,
                android.R.layout.simple_expandable_list_item_1, groupFrom,
                groupTo, сhildDataList, android.R.layout.simple_list_item_1,
                childFrom, childTo);

        // Проверка наличия группы с идентификатором 22
        int groupPosition = -1;
        for (int i = 0; i < groupDataList.size(); i++) {
            Map<String, String> groupData = groupDataList.get(i);
            String groupId = groupData.get("groupId"); // Здесь нужно использовать правильный ключ для идентификатора группы
            if (groupId != null && groupId.equals(groupPos)) {
                groupPosition = i;
                break;
            }
        }
        adapter.group = groupPosition;

        return adapter;
    }


    /**
     * 15.01.2021
     * <p>
     * Функционал в зависимости от операции
     */
    private void operetionSaveRPToDB(TovarOptions tpl, ReportPrepareDB rp, String data, String data2, TovarDB tovarDB) {
        if (rp == null) {
            rp = createNewRPRow(tovarDB);
        }

        if (data == null || data.equals("")) {
            Toast.makeText(context, "Для сохранения - внесите данные", Toast.LENGTH_SHORT).show();
            return;
        }

        ReportPrepareDB table = rp;
        switch (tpl.getOptionControlName()) {
            case PRICE:
                Log.e("SAVE_TO_REPORT_OPT", "PRICE: " + data);
                INSTANCE.executeTransaction(realm -> {
                    table.setPrice(data);
                    table.setUploadStatus(1);
                    table.setDtChange(System.currentTimeMillis() / 1000);
                    RealmManager.setReportPrepareRow(table);
                });
                break;

            case FACE:
                Log.e("SAVE_TO_REPORT_OPT", "FACE: " + data);
                INSTANCE.executeTransaction(realm -> {
                    table.setFace(data);
                    table.setUploadStatus(1);
                    table.setDtChange(System.currentTimeMillis() / 1000);
                    RealmManager.setReportPrepareRow(table);
                });
                break;

            case EXPIRE_LEFT:
                Log.e("SAVE_TO_REPORT_OPT", "EXPIRE_LEFT: " + data);
                INSTANCE.executeTransaction(realm -> {
                    table.setExpireLeft(data);
                    table.setUploadStatus(1);
                    table.setDtChange(System.currentTimeMillis() / 1000);
                    RealmManager.setReportPrepareRow(table);
                });
                break;

            case AMOUNT:
                Log.e("SAVE_TO_REPORT_OPT", "AMOUNT: " + data);
                INSTANCE.executeTransaction(realm -> {
                    table.setAmount(Integer.parseInt(data));
                    table.setUploadStatus(1);
                    table.setDtChange(System.currentTimeMillis() / 1000);
                    RealmManager.setReportPrepareRow(table);
                });
                break;

            case OBOROTVED_NUM:
                Log.e("SAVE_TO_REPORT_OPT", "OBOROTVED_NUM: " + data);
                INSTANCE.executeTransaction(realm -> {
                    table.setOborotvedNum(data);
                    table.setUploadStatus(1);
                    table.setDtChange(System.currentTimeMillis() / 1000);
                    RealmManager.setReportPrepareRow(table);
                });
                break;

            case UP:
                Log.e("SAVE_TO_REPORT_OPT", "UP: " + data);
                INSTANCE.executeTransaction(realm -> {
                    table.setUp(data);
                    table.setUploadStatus(1);
                    table.setDtChange(System.currentTimeMillis() / 1000);
                    RealmManager.setReportPrepareRow(table);
                });
                break;

            case DT_EXPIRE:
                Log.e("SAVE_TO_REPORT_OPT", "DT_EXPIRE: " + data);
                INSTANCE.executeTransaction(realm -> {
                    table.setDtExpire(data);
                    table.setUploadStatus(1);
                    table.setDtChange(System.currentTimeMillis() / 1000);
                    RealmManager.setReportPrepareRow(table);
                });
                break;

            case ERROR_ID:
                Log.e("SAVE_TO_REPORT_OPT", "ERROR_ID: " + data);
                Log.e("SAVE_TO_REPORT_OPT", "ERROR_COMMENT: " + data2);
                INSTANCE.executeTransaction(realm -> {
                    table.setErrorId(data);
                    table.setErrorComment(data2);
                    table.setNotes(data2);
                    table.setUploadStatus(1);
                    table.setDtChange(System.currentTimeMillis() / 1000);
                    RealmManager.setReportPrepareRow(table);
                });
                break;

            case AKCIYA_ID:
                Log.e("SAVE_TO_REPORT_OPT", "AKCIYA_ID: " + data);
                Log.e("SAVE_TO_REPORT_OPT", "AKCIYA_ID_А: " + data2);
                INSTANCE.executeTransaction(realm -> {
                    table.setAkciyaId(data);
                    if (data2 != null && !data2.equals("")) {
                        table.setAkciya(data2);
                    }
                    table.setUploadStatus(1);
                    table.setDtChange(System.currentTimeMillis() / 1000);
                    RealmManager.setReportPrepareRow(table);
                });
                break;

//                case AKCIYA:
//                    Log.e("SAVE_TO_REPORT_OPT", "AKCIYA: " + data);
//                    INSTANCE.executeTransaction(realm -> {
//                        table.setAkciya(data2);     // 25.03.23. Поменял с Дата на Дата2 потому что в выпадающем списке ТПЛов боюсь что запутаются данные с AKCIYA_ID. Сделалл так что б было одинаково и там и там
//                        table.setUploadStatus(1);
//                        table.setDtChange(System.currentTimeMillis() / 1000);
//                        RealmManager.setReportPrepareRow(table);
//                    });
//                    break;

            case NOTES:
                Log.e("SAVE_TO_REPORT_OPT", "NOTES: " + data);
                INSTANCE.executeTransaction(realm -> {
                    table.setNotes(data);
                    table.setUploadStatus(1);
                    table.setDtChange(System.currentTimeMillis() / 1000);
                    RealmManager.setReportPrepareRow(table);
                });
                break;

        }
    }


    private ReportPrepareDB createNewRPRow(TovarDB list) {
        ReportPrepareDB rp = new ReportPrepareDB();

        long id = RealmManager.reportPrepareGetLastId();
        id = id + 1;

        rp.setID(id);
        rp.setDt(String.valueOf(System.currentTimeMillis()));
        rp.setDtReport(String.valueOf(System.currentTimeMillis()));
        rp.setKli(wpDataDB.getClient_id());
        rp.setTovarId(list.getiD());
        rp.setAddrId(String.valueOf(wpDataDB.getAddr_id()));
        rp.setPrice("");
        rp.setFace("");
        rp.setAmount(0);
        rp.setDtExpire("");
        rp.setExpireLeft("");
        rp.setNotes("");
        rp.setUp("");
        rp.setAkciya("");
        rp.setAkciyaId("");
        rp.setOborotvedNum("");
        rp.setErrorId("");
        rp.setErrorComment("");
        rp.setCodeDad2(String.valueOf(wpDataDB.getCode_dad2()));

        // TODO сохранение в БД новой строки что б потом работать с ней в getCurrentData()
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(rp);
        INSTANCE.commitTransaction();
        return rp;
    }

    /**
     * 11.01.2021
     * Установкаа типа операции
     */
    private DialogData.Operations operationType(TovarOptions tpl) {
        switch (tpl.getOrderField()) {
            case ("price"):
            case ("face"):
            case ("expire_left"):
            case ("amount"):
            case ("oborotved_num"):
            case ("up"):
                return Number;

            case ("dt_expire"):
                return Date;


            case ("akciya_id"):
//                case ("akciya"):
                return DoubleSpinner;

            case ("error_id"):
                return EditTextAndSpinner;

            case ("notes"):
                return Text;

            default:
                return Text;
        }
    }

    /**
     * 15.01.2021
     * <p>
     * Функционал в зависимости от операции
     */
    private String getCurrentData(TovarOptions tpl, String cd, String id) {
        ReportPrepareDB table = RealmManager.getTovarReportPrepare(cd, id);
        switch (tpl.getOptionControlName()) {
            case PRICE:
                return table.getPrice();

            case FACE:
                return table.getFace();

            case EXPIRE_LEFT:
                return table.getExpireLeft();

            case AMOUNT:
                return String.valueOf(table.getAmount());

            case OBOROTVED_NUM:
                return table.getOborotvedNum();

            case UP:
                return table.getUp();

            case DT_EXPIRE:
                return table.getDtExpire();

            case ERROR_ID:
                return table.getErrorId();

            case AKCIYA_ID:
                return table.getAkciyaId();

            case AKCIYA:
                return table.getAkciya();

            case NOTES:
                return table.getNotes();

        }

        return null;
    }


    private void pushOkButtonRequisites(TovarOptions tpl, ReportPrepareDB reportPrepareDB, DialogData dialog, String cd2, TovarDB list, String tovarId, String clientId, String finalBalanceData1, String finalBalanceDate1, boolean clickType) {
        operetionSaveRPToDB(tpl, reportPrepareDB, dialog.getOperationResult(), dialog.getOperationResult2(), null);
        Toast.makeText(context, "Внесено: " + dialog.getOperationResult(), Toast.LENGTH_LONG).show();
        dialogShowRule2(list, tpl, reportPrepareDB, tovarId, cd2, clientId, finalBalanceData1, finalBalanceDate1, clickType);
    }

}
