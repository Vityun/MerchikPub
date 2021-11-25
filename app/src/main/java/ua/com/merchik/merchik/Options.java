package ua.com.merchik.merchik;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Realm.VirtualAdditionalRequirementsDB;
import ua.com.merchik.merchik.data.Database.Room.EKL_SDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.ReportHintList;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA;
import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA_ID;
import static ua.com.merchik.merchik.Globals.OptionControlName.AMOUNT;
import static ua.com.merchik.merchik.Globals.OptionControlName.DT_EXPIRE;
import static ua.com.merchik.merchik.Globals.OptionControlName.ERROR_ID;
import static ua.com.merchik.merchik.Globals.OptionControlName.EXPIRE_LEFT;
import static ua.com.merchik.merchik.Globals.OptionControlName.FACE;
import static ua.com.merchik.merchik.Globals.OptionControlName.NOTES;
import static ua.com.merchik.merchik.Globals.OptionControlName.OBOROTVED_NUM;
import static ua.com.merchik.merchik.Globals.OptionControlName.PRICE;
import static ua.com.merchik.merchik.Globals.OptionControlName.UP;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class Options {

    private Globals globals = new Globals();

    /*Сюда записываются Опции которые не прошли проверку, при особенном переданном MOD-e. Сделано
    для того что б потом можно было посмотреть название опций которые не прошли проверку и, возможно,
    в будущем, их пересчитать*/
    private List<OptionsDB> optionNotConduct = new ArrayList();

    private static List<TovarOptions> list = new ArrayList<>();
    private static List<Integer> ids = new ArrayList<>();
    private String spinnerError = "";
    private String spinnerPromo = "";

    Map<String, String> mapSpinnerError = new HashMap<>();
    Map<String, String> mapSpinnerPromo = new HashMap<>();


    // =============================================================================================
    // КОНТРОЛЬ ОПЦИЙ
    public void optionControl(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {

        try {
            Log.e("OPTION_CONTROL", "HERE(0): " + optionsDB.getOptionControlId());

            int optionControlId = Integer.parseInt(optionsDB.getOptionControlId());

            switch (optionControlId) {
                case 84932: // Проверка наличия ФотоОтчётов (id мне дали из 1С) (тип 0)
                    checkPhotoReport(context, wpDataDB, optionsDB, type, mode);
                    break;

                case 134583:    // ПРоверка наличия фотоотчётов с привязкой к координатам
                    // Нужно дописать
                    checkPhotoReportWithMP(context, wpDataDB, optionsDB, type, mode);
                    break;

                case 1470:  // Проверка наличия Фото остатков товара (тип 4)
                    checkPhoto(wpDataDB, optionsDB, "4");
                    break;

                case 132971:  // Проверка наличия Фото тележка с товаром (тип 10)
                    checkPhoto(wpDataDB, optionsDB, "10");
                    break;

                case 141361:  // Проверка наличия Фото тележка с товаром (тип 31)
                    checkPhoto(wpDataDB, optionsDB, "31");
                    break;

                case 141886:    // Проверка наличия Фото Документов (3)
                    checkPhoto(wpDataDB, optionsDB, "3");
                    break;

                case 76815: // Проверка наличия Дет.Отчётов (id мне дали из 1С)
                    Log.e("OPTION_CONTROL", "Проверка наличия Дет.Отчётов" + optionsDB.getOptionControlId());
                    check76815(wpDataDB, optionsDB); // Проверка Представленности
                    break;

                case 138519:
                    Log.e("OPTION_CONTROL", "checkStartWork: " + optionsDB.getOptionControlId());
//                checkStartWork(context, wpDataDB, optionsDB, type, mode);
                    optionControlStartWork_138519(context, wpDataDB, optionsDB, type, mode);
                    break;

                case 138521:
                    Log.e("OPTION_CONTROL", "checkEndWork: " + optionsDB.getOptionControlId());
//                checkEndWork(context, wpDataDB, optionsDB, type, mode);
                    optionControlEndWork_138521(context, wpDataDB, optionsDB, type, mode);
                    break;

                case 8299:
                    Log.e("OPTION_CONTROL", "checkMP: " + optionsDB.getOptionControlId());
//                checkMP(context, wpDataDB, optionsDB, type, mode);
                    optionControlMP_8299(context, wpDataDB, optionsDB, type, mode);
                    break;

                case 141911:
                    // !!!!!!!
                    checkReceivingAnOrder_141911(context, wpDataDB, optionsDB, type, mode);
                    break;

                case 141889:
                    // !!!!!!!
                    check_RENAME_2(context, wpDataDB, optionsDB, type, mode);
                    break;

                case 84006:
                    // !!!!!!!
                    checkEKL(context, wpDataDB, optionsDB, type, mode);
                    break;

                case 587:
                    optionControlReceivingAnOrder_587(context, wpDataDB, optionsDB, null, NNKMode.CHECK);
                    break;

                case 138341:
                    try {
                        optionControlAdditionalRequirements_138341(context, wpDataDB, optionsDB, null, mode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case 139577:
                    optionControlVersion_139577(context, wpDataDB, optionsDB, null, mode);
                    break;


                default:
//                switch (warningType) {
//                    case 1:
//                        Log.e("MASSAGE_TO_USER", "Какое-то сообщение");
//                        break;
//
//                    case 2:
//                        Toast.makeText(context, "Не могу найти описание опции: " + optionsDB.getOptionControlId(), Toast.LENGTH_LONG).show();
//                        break;
//
//                    default:
//                        Log.e("MASSAGE_TO_USER", "Ничего не выполняем");
//                        break;
//                }
                    break;
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "optionControl", "exeption: " + e);
        }

    }


    //==============================================================================================

    public enum NNKMode {
        CHECK,  // Проверка, вывод сообщения (Надо поменять что б ничего не выводилось)
        CHECK_CLICK,  // Проверка, вывод сообщения
        CHECK_COLLECT_MSG,
        MAKE,   // Выполнения функциональности кнопки
        NULL    // Ничего не делаем
    }

    /*
     * Обработка опций
     * Нажатие На Кнопку (ННК) -- абстрактное название. На самом деле в принципе обработка
     * состояний опций
     * */
    public OptionMassageType NNK(Context context, WpDataDB wp, OptionsDB option, OptionMassageType type, NNKMode mode, Clicks.clickVoid click) {
        OptionMassageType result = new OptionMassageType();
        int res = 0;    // Счётчик для накапливания "блокировок" у данной опции

        Log.e("NNK", "--------------------------------");
        Log.e("NNK", "option.option_id: " + option.getOptionId());
        Log.e("NNK", "START_res: " + res);

        // Проход по первой опции блокировки
        if (!option.getOptionBlock1().equals("0")) {
            res += optControl(context, wp, option, Integer.parseInt(option.getOptionBlock1()), type, mode);
        }

        // Проход по второй опции блокировки
        if (!option.getOptionBlock2().equals("0")) {
            res += optControl(context, wp, option, Integer.parseInt(option.getOptionBlock2()), type, mode);
        }

        Log.e("NNK", "END_res: " + res);


        if (res > 0) {
            switch (mode) {
                case NULL:

                    result.msg = "OK";

                    return result;

                case MAKE:
                    DialogData dialogData = new DialogData(context);
                    dialogData.setTitle("Ошибка");
                    dialogData.setText("Прежде чем выполнять данную опцию (действие) вы должны выполнить опцию: " + OptionsRealm.getOptionByOptionId(option.getOptionBlock1()).getOptionControlTxt());
                    dialogData.setClose(dialogData::dismiss);

                    // Прежде чем выполнять данную опцию(действие) вы должны выполнить опцию: *формула название опции*

                    result.dialog = dialogData;

                    return result;

                case CHECK:
                    DialogData dialogData2 = new DialogData(context);
                    dialogData2.setTitle("Блокировка");
                    dialogData2.setText("Данная Опция заблокированна ОПЦИЕЙ: " + option.getOptionBlock1() + "/" + option.getOptionBlock2());
                    dialogData2.setClose(dialogData2::dismiss);


                    result.dialog = dialogData2;

                    return result;
            }
        } else {
            result.msg = "NOT OK";

            switch (mode) {
                case NULL:
                    break;

                case CHECK:
//                    Toast.makeText(context, "ЧЕК ЧЕК", Toast.LENGTH_SHORT).show();
                    break;

                case MAKE:
//                    Toast.makeText(context, "КЛИК КЛИК", Toast.LENGTH_SHORT).show();
                    optControl(context, wp, option, Integer.parseInt(option.getOptionId()), type, mode);
                    click.click();
                    break;
            }


            return result;
        }


        return null;
    }


    /**
     * 23.07.21
     * "Нажатие" на "Провести"
     */
    public void conduct(Context context, WpDataDB wp, List<OptionsDB> options, int optCount) {
        int register = 0;
        for (OptionsDB item : options) {
            Log.e("conduct", "----------------------------------------------------------------");
            Log.e("conduct", "OptionsDB item.getOptionControlId(): " + item.getOptionControlId());

            int controlResult = optControl(context, wp, item, Integer.parseInt(item.getOptionControlId()), new OptionMassageType(), NNKMode.CHECK);

            if (controlResult == 0) {
                Log.e("conduct", "Опция контроля НЕ выполнена: " + controlResult);
            } else if (controlResult == 1) {
                Log.e("conduct", "Опция контроля ВЫПолнена: " + controlResult);
                register++;
            } else {
                Log.e("conduct", "Что-то пошло не так: " + controlResult);
            }
        }

        Log.e("conduct", "optionNotConduct: " + optionNotConduct);

        if (optionNotConduct.size() > 0) {
            DialogData dialog = new DialogData(context);
            dialog.setDialogIco();
            dialog.setTitle("Не все опции прошли проверку.");

            StringBuffer msg = new StringBuffer();
            for (OptionsDB item : optionNotConduct) {
                msg.append("* ").append(item.getOptionControlTxt()).append("\n");
            }

            dialog.setText("Не прошли проверку: \n\n" + msg);
            dialog.setClose(dialog::dismiss);
            dialog.show();
        } else {
            Toast.makeText(context, "Запрос на проведение создан", Toast.LENGTH_SHORT).show();

            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (wp != null) {
                    wp.setSetStatus(1);
                    realm.insertOrUpdate(wp);
                }
            });
        }


    }


    /*Проверка Опции*/
    private int optControl(Context context, WpDataDB wp, OptionsDB option, int optionId, OptionMassageType type, NNKMode mode) {

        try {
            Log.e("NNK", "F/optControl/optionId: " + optionId);
            switch (optionId) {

                // ---

                case 138773:
                    optionMP_138773(context, wp, option, type, mode);
                    break;

                case 8299:
                    return optionControlMP_8299(context, wp, option, type, mode) ? 1 : 0;

                // ---

                case 138518:
                    Log.e("NNK", "F/optControl/138518");
                    optionStartWork_138518(context, wp, option, type, mode);
                    break;

                case 138519:
                    Log.e("NNK", "F/optControl/138519");
                    return optionControlStartWork_138519(context, wp, option, type, mode) ? 0 : 1;


                // ---

                case 138520:
                    optionEndWork_138520(context, wp, option, type, mode);
                    break;

                case 138521:
                    return optionControlEndWork_138521(context, wp, option, type, mode) ? 1 : 0;

                // ---

                case 132968:
                    optionMakePhoto0_132968(context, wp, option, type, mode);
                    break;

                // --- Опция контроля на Получение заказа в ТТ
                case 587:
                    return optionControlReceivingAnOrder_587(context, wp, option, type, mode) ? 1 : 0;


                // Контроль Опции Доп. Требований
                case 138341:
                    try {
                        optionControlAdditionalRequirements_138341(context, wp, option, type, mode);
                    } catch (Exception e) {
                    }
                    break;

                case 139577:
                    optionControlVersion_139577(context, wp, option, null, NNKMode.CHECK_CLICK);
                    break;


//                // Контроль фотоотчётов
//                case 84932: // Проверка наличия ФотоОтчётов (id мне дали из 1С) (тип 0)
//                    checkPhotoReport(context, wp, option, type, mode);
//                    break;


                default:

                    switch (mode) {
                        case NULL:
                            return 0;

                        case CHECK:
//                        Toast.makeText(context, "Данная Опция находится в РАЗРАБОТКЕ!", Toast.LENGTH_SHORT).show();
                            return 0;

                        case MAKE:
                            Toast.makeText(context, "Данная Опция находится в РАЗРАБОТКЕ", Toast.LENGTH_SHORT).show();
                            return 0;
                    }
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "optControl2", "Exception: " + e);
        }

        return 0;
    }

    //----------------------------------------------------------------------------------------------

    // Новый набор опций. Переписывание как в 1С (типо правильно)

    /**
     * Опция Контроля
     * Проверка местоположения ( 8299 )
     */
    private boolean optionControlMP_8299(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        boolean res;

        // Проверка Опции и запись в БД результата
        if (wpDataDB.getVisit_start_geo_distance() < 500 && wpDataDB.getVisit_start_geo_distance() > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            res = true;
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            res = false;
        }

        // Обработка режима который вернулся
        switch (mode) {
            case CHECK:
                if (!res && optionsDB.getBlockPns().equals("1")) {
                    optionNotConduct.add(optionsDB);
                }
                break;

            case NULL:
                // Ничего делать не буду
                break;
        }


        return res;
    }


    /**
     * Опция
     * Нажатие на кнопку моего местоположения ( 138773 )
     */
    private void optionMP_138773(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {

        // Запись в таблицу Местоположений
        LogMPDB log = new LogMPDB(RealmManager.logMPGetLastId() + 1, globals.POST_10());
        RealmManager.setLogMpRow(log);


        // Вывод диалога с МП
        if (context instanceof toolbar_menus) {
            ((toolbar_menus) context).dialogMap();
        }

        // Запись в План работ
        int distance;
        if (Globals.measure.equals("м")) {
            distance = (int) Globals.distanceAB;
        } else if (Globals.measure.equals("км")) {
            distance = (int) Globals.distanceAB * 1000;
        } else {
            distance = 0;
        }

        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (wpDataDB != null) {
                wpDataDB.setVisit_start_geo_distance(distance);
                realm.insertOrUpdate(wpDataDB);
                Toast.makeText(context, "Данные о местоположении внесены.", Toast.LENGTH_LONG).show();
            }
        });
    }

    //--------------------- НАЧАЛО РАБОТЫ -----------------------

    /**
     * Опция контроля
     * Проверка на Начало работы ( 138519 )
     */
    private boolean optionControlStartWork_138519(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        boolean res;

        Log.e("checkStartWork", "ENTER THIS");
        if (wpDataDB.getVisit_start_dt() > 0) {
            Log.e("checkStartWork", "2 wpDataDB.getVisit_start_dt(): " + wpDataDB.getVisit_start_dt());
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            res = true;
        } else {
            Log.e("checkStartWork", "1 wpDataDB.getVisit_start_dt(): " + wpDataDB.getVisit_start_dt());
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            res = false;
        }

        // Обработка режима который вернулся
        switch (mode) {
            case CHECK:
                if (!res && optionsDB.getBlockPns().equals("1")) {
                    optionNotConduct.add(optionsDB);
                }
                break;

            case NULL:
                // Ничего делать не буду
                break;
        }

        return res;
    }


    /**
     * Опция
     * Нажатие на кнопку Для установки начала рабочего дня ( 138518 )
     */
    private void optionStartWork_138518(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.DetailedReportButtons.class.pressStartWork: " + "ENTER" + "\n");
        if (wpDataDB.getVisit_start_dt() > 0) {
            Toast.makeText(context, "Работа уже начата!", Toast.LENGTH_SHORT).show();
        } else {
            try {
                long startTime = System.currentTimeMillis() / 1000;
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    wpDataDB.setDt_update(System.currentTimeMillis() / 1000);
                    wpDataDB.setVisit_start_dt(startTime);
                    wpDataDB.setClient_start_dt(startTime);
                    wpDataDB.startUpdate = true;
                    realm.insertOrUpdate(wpDataDB);
                });
                Toast.makeText(context, "Вы начали работу в: " + Clock.getHumanTimeOpt(startTime * 1000), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // Set to log error
            }
        }
    }

    //------------------- ОКОНЧАНИЕ РАБОТЫ-------------------------

    /**
     * Опция контроля
     * Проверка на Окончание работы( 138521 )
     */
    private boolean optionControlEndWork_138521(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {

        boolean res;

        if (wpDataDB.getVisit_end_dt() > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            res = true;
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            res = false;
        }

        // Обработка режима который вернулся
        switch (mode) {
            case CHECK:
                if (!res && optionsDB.getBlockPns().equals("1")) {
                    optionNotConduct.add(optionsDB);
                }
                break;

            case NULL:
                // Ничего делать не буду
                break;
        }

        return res;
    }


    /**
     * Опция
     * Нажатие на кнопку Для установки окончания рабочего дня ( 138520 )
     */
    private void optionEndWork_138520(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.DetailedReportButtons.class.pressEndWork: " + "ENTER" + "\n");
        if (wpDataDB.getVisit_end_dt() > 0) {
            Toast.makeText(context, "Работа уже окончена!", Toast.LENGTH_SHORT).show();
        } else {
            if (wpDataDB.getVisit_start_dt() > 0) {
                try {
                    long endTime = System.currentTimeMillis() / 1000;
                    RealmManager.INSTANCE.executeTransaction(realm -> {
                        wpDataDB.setDt_update(System.currentTimeMillis() / 1000);
                        wpDataDB.setVisit_end_dt(endTime);
                        wpDataDB.setClient_end_dt(endTime);
                        wpDataDB.startUpdate = true;
                        realm.insertOrUpdate(wpDataDB);
                    });
                    Toast.makeText(context, "Вы окончили работу в: " + endTime, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    // Set to log error
                }
            } else {
                Toast.makeText(context, "Вы не можете закончить работу не начав её", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //------------------- ФОТООТЧЁТЫ -------------------------

    /**
     * Опция
     * Выполнение фотоотчёта
     */

    private void optionMakePhoto0_132968(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
//        Intent intentPhotoReport = new Intent(context, PhotoReportActivity.class);
//        WPDataObj wpDataObj = new WorkPlan().getKPS(wpDataDB.getId());
//
//        intentPhotoReport.putExtra("dataFromWPObj", wpDataObj);
//        MakePhoto.startToMakePhoto(context, wpDataObj);
    }


    /**
     * Опция контроля (587)
     */
    private boolean optionControlReceivingAnOrder_587(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        boolean res;

        List<ReportPrepareDB> rp = ReportPrepareRealm.getReportPrepareByDad2(wpDataDB.getCode_dad2());

        int SKU = 0;
        int sum = 0;
        int codes = 0;
        for (ReportPrepareDB item : rp) {

            if (item.getAmount() > 0) {
                sum += item.getAmount();
                SKU++;
            }

            if (item.buyerOrderId > 0) {
                codes = item.buyerOrderId;
            }
        }

        if (sum > 0 && codes > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            res = true;
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            res = false;
        }

        // Обработка режима который вернулся
        switch (mode) {
            case CHECK:
                if (!res && optionsDB.getBlockPns().equals("1")) {
                    optionNotConduct.add(optionsDB);
                }

                if (rp.size() == 0) {
                    Toast.makeText(context, "Товаров, по которым надо проверять заказ, не обнаружено.", Toast.LENGTH_LONG).show();
                }

                if (sum == 0) {
                    Toast.makeText(context, "Товар в ТТ НЕ заказан! Если это не так, то вы должны указать Количество закаанного товара, отдельно по каждой позиции.", Toast.LENGTH_LONG).show();
                }

                if (codes == 0) {
                    Toast.makeText(context, "Вы не указали номер заказа. Его надо получить у Баера у которого делали этот заказ.", Toast.LENGTH_LONG).show();
                }

                if (sum > 0 && codes > 0) {
                    Toast.makeText(context, "Заказано: " + SKU + " товаров (кол " + sum + " шт.) согласно заказа № " + codes, Toast.LENGTH_LONG).show();
                }
                break;

            case NULL:
                // Ничего делать не буду
                break;
        }

        return res;
    }


    /**
     * Опция контроля 138341
     */
    private <T> boolean optionControlAdditionalRequirements_138341(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        boolean res = false;
        double averageRating = 0;  // Средняя Оценка
        double deviationFromTheMeanSize = 0;    // Отклонение от среднего
        int markSum = 0;
        int nedotochSize = 0;

        StringBuilder msg = new StringBuilder();


        String date = wpDataDB.getDt();

        long dt = Clock.dateConvertToLong(wpDataDB.getDt()) / 1000;       // Дата документа в Unix
        long dateFrom = Clock.dateConvertToLong(date) / 1000 - 60 * 60 * 24 * 30; // Дата документа -30 дней
        long dateTo = Clock.dateConvertToLong(date) / 1000 + 60 * 60 * 24 * 3;    // Дата документа +3 дня

        // Получаем Доп.Требования.
        RealmResults<AdditionalRequirementsDB> realmResults = AdditionalRequirementsRealm.getData3(wpDataDB);
        List<AdditionalRequirementsDB> data = RealmManager.INSTANCE.copyFromRealm(realmResults);

        // Получаем Оценки этих Доп. требований.
        RealmResults<AdditionalRequirementsMarkDB> marks = AdditionalRequirementsMarkRealm.getAdditionalRequirementsMarks(dateFrom, dateTo, wpDataDB.getUser_id(), data);

        Gson gson = new Gson();

        String json = gson.toJson(data);

        Type listType = new TypeToken<ArrayList<VirtualAdditionalRequirementsDB>>() {
        }.getType();
        List<T> test = new Gson().fromJson(json, listType);
        List<VirtualAdditionalRequirementsDB> virtualTable = (List<VirtualAdditionalRequirementsDB>) test;

        Log.e("testprint", "data: " + data);
        Log.e("testprint", "marks: " + marks);
        Log.e("testprint", "test: " + test);
        Log.e("testprint", "virtualTable: " + virtualTable);

        for (VirtualAdditionalRequirementsDB item : virtualTable) {
            if (marks.get(0).getScore() != null && !marks.get(0).getScore().equals("") && !marks.get(0).getScore().equals("0")) {
                item.mark = Integer.valueOf(marks.get(0).getScore());
                item.dtChange = String.valueOf(marks.get(0).getDt());
            }

            if (Long.parseLong(item.dtChange) >= dt) {
                item.nedotoch = 0;
                item.notes = "ДТ измененно ПОСЛЕ проведения работ и проверке не подлежит";
            } else if (Clock.dateConvertToLong(item.dtEnd) == dt) {
                item.nedotoch = 0;
                item.notes = "у ДТ заканчивается срок действия и голосование по нему проверке не подлежит";
            } else if (item.mark == 0) {
                item.nedotoch = 1;
                item.notes = "";
            } else {
                item.nedotoch = 0;
                item.notes = "";
            }


            nedotochSize = +item.nedotoch;
            markSum = +item.mark;
        }


        try {
            averageRating = markSum / (virtualTable.size() - nedotochSize);
        } catch (Exception e) {
            averageRating = 0;
        }

        for (VirtualAdditionalRequirementsDB item : virtualTable) {
            item.deviationFromTheMean = Math.abs(averageRating - item.mark);
            deviationFromTheMeanSize = +item.deviationFromTheMean;
        }
        // Математика закончена


        // Установка сигналов.
        // У меня 2 это 0 в 1С, а 1 это 1 в 1С
        if (virtualTable.size() == 0) {

            msg.append("У клиента ")
                    .append(CustomerRealm.getCustomerById(wpDataDB.getClient_id()))
                    .append(" нет доп. требований по этому адресу");

            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            res = true;
        } else if (nedotochSize > 0) {

            msg.append("За период с ")
                    .append(Clock.getHumanTime3(dateFrom))
                    .append(" по ")
                    .append(Clock.getHumanTime3(dateTo))
                    .append(" ")
                    .append(wpDataDB.getUser_txt())
                    .append(" НЕ поставил оценку(и) по ")
                    .append(nedotochSize)
                    .append(" Доп.требованиям. ")
            ;

            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            res = false;
        } else if (virtualTable.size() > 1 && deviationFromTheMeanSize < 0.5) {

            msg.append("Вы оценили Все (")
                    .append(virtualTable.size())
                    .append(") Доп.требований ОДНОЙ и той-же оценкой (")
                    .append(averageRating)
                    .append(")! Это НЕ даёт возможность улучшить их качество! " +
                            "Оценивайте эти требования ОБЬЕКТИВНО!");

            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            res = false;
        } else {
            msg.append("За период с ")
                    .append(Clock.getHumanTime3(dateFrom))
                    .append(" по ")
                    .append(Clock.getHumanTime3(dateTo))
                    .append(" ")
                    .append(wpDataDB.getUser_txt())
                    .append(" поставил оценку(и) по ")
                    .append(virtualTable.size())
                    .append(" Доп.требованиям. Замечаний по выполнению опции нет.")
            ;

            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            res = true;
        }


        // Начат вывод сообщений
        switch (mode) {
            case CHECK_CLICK:
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                break;
        }


        return res;
    }


    /**
     * Опция нажатие на номер версии приложения 139576
     */
    public void optionVersion_139576(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        Long currentVer = Long.valueOf(BuildConfig.VERSION_NAME);
        Long minimalVer = VersionApp.VERSION_APP;

        StringBuilder msg = new StringBuilder();
        msg.append("Текущая версия приложения: ")
                .append(currentVer)
                .append(" \nПоследняя актуальная версия: ")
                .append(minimalVer);

        DialogData dialog = new DialogData(context);
        dialog.setTitle("Версия ПО");
        dialog.setText(msg);
        dialog.setClose(dialog::dismiss);
        dialog.show();
    }


    /**
     * Опция нажатие на номер версии приложения 139577
     */
    public void optionControlVersion_139577(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        Long currentVer = Long.valueOf(BuildConfig.VERSION_NAME);
        Long minimalVer = VersionApp.VERSION_APP;

        try {
            if (currentVer >= minimalVer) {
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionsDB != null) {
                        optionsDB.setIsSignal("2");
                        realm.insertOrUpdate(optionsDB);
                    }
                });
            } else {
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionsDB != null) {
                        optionsDB.setIsSignal("1");
                        realm.insertOrUpdate(optionsDB);
                    }
                });
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "optionControlVersion_139577", "Проблема с версией приложения в опции контроля. : " + e);
        }

    }
    //==============================================================================================

    /**
     * 06.04.2021
     * Опция контроля. Проверка представленности
     */
    public static double SKUPlan = 0;
    public static double SKUFact = 0;
    public static double OFS = 0;   // % сколько нет товаров
    public static double OOS = 0;   // Представленность %

    private void check76815(WpDataDB wpDataDB, OptionsDB optionsDB) {
        List<TovarDB> dataTovar = RealmManager.getTovarListFromReportPrepareByDad2(wpDataDB.getCode_dad2());    // Это типа моего СКЮ План
        SKUPlan = dataTovar.size();

        // Перебираем товары по плану
        for (TovarDB item : dataTovar) {
            ReportPrepareDB reportPrepareTovar = RealmManager.getTovarReportPrepare(String.valueOf(wpDataDB.getCode_dad2()), item.getiD()); // Есть ли в РП такой товар?
            if (reportPrepareTovar != null) {
                if (reportPrepareTovar.getFace() != null && !reportPrepareTovar.getFace().equals("") && !reportPrepareTovar.getFace().equals("0")) {     // Если у этого товара Фейсы не Null и заполенны чем-то - добавляем +1 к предствленности
                    SKUFact++;
                }
            }
        }

        OOS = 100 - 100 * (SKUFact / SKUPlan);
        OFS = 100 * (SKUFact / SKUPlan);
    }


    // 19.08.2020
    // Опция контроля
    // Проверка Место Положения (8299)
    private void checkMP(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, int warningType, int mode) {
        if (wpDataDB.getVisit_start_geo_distance() < 500 && wpDataDB.getVisit_start_geo_distance() > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }
    }


    // Опция контроля
    // Проверка наличия ФотоОтчётов (84932)
    public void checkPhotoReport(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        // Получаем даннные о наличии фотоотчёта из Журнала фотоОтчётов
        List<StackPhotoDB> list = RealmManager.getStackPhotoByDad2(wpDataDB.getCode_dad2());

        Log.e("OPTION_CONTROL", "PHOTO_LIST: " + list.size());

        // Анализируем таблицу
        // Делаем отметки (записи) в строку WpDataDB wpDataDB, OptionsDB optionsDB,
        if (list.size() < 3) {
            Log.e("OPTION_CONTROL", "id: " + optionsDB.getID() + " |Signal: " + optionsDB.getIsSignal());
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        } else if (list.size() >= 3) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }
    }

    /*Опция контроля 134583
    * Проверка наличия ФотоОтчётов с привязкой к Адресу (134583)
    *
    * Сначала получаю список фоток с данным типом по этому адресу, а потом проверяю - все ли
    * сделанны на месте.
    * */
    public void checkPhotoReportWithMP(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode){

        String photoMinCount = optionsDB.getAmountMin();

        List<StackPhotoDB> list = RealmManager.getStackPhotoByDad2(wpDataDB.getCode_dad2());
        if (photoMinCount != null && photoMinCount.equals(list.size()) || list.size() < 3) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            return;
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }

//        for (StackPhotoDB item : list){
//
//        }
    }


    /**
     * 19.04.2021
     * Опция контроля Количество фотографий
     */
    private void checkPhoto(WpDataDB wpDataDB, OptionsDB optionsDB, String photoType) {
        try {
            RealmResults<StackPhotoDB> list = RealmManager.stackPhotoByDad2(wpDataDB.getCode_dad2());
            List<StackPhotoDB> res = list.where().equalTo("photo_type", Integer.parseInt(photoType)).findAll();

            Log.e("OPTION_CONTROL", "checkPhoto id: " + optionsDB.getID() + " |Signal: " + optionsDB.getIsSignal());

            if (res.size() < 1) {
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionsDB != null) {
                        optionsDB.setIsSignal("1");
                        realm.insertOrUpdate(optionsDB);
                    }
                });
            } else if (list.size() >= 1) {
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionsDB != null) {
                        optionsDB.setIsSignal("2");
                        realm.insertOrUpdate(optionsDB);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("OPTION_CONTROL", "checkPhoto e: " + e);
        }
    }


    // Опция контроля
    // Проверка начала работы (138519)

    /**
     * Начало работы
     */
    private void checkStartWork(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, int warningType, int mode) {
        Log.e("checkStartWork", "ENTER");
        if (wpDataDB.getVisit_start_dt() > 0) {
            Log.e("checkStartWork", "2");
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        } else {
            Log.e("checkStartWork", "1");
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }
    }

    // Опция контроля
    // Проверка окончания работы (138521)

    /**
     * Окончания работы
     */
    private void checkEndWork(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, int warningType, int mode) {
        if (wpDataDB.getVisit_end_dt() > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }
    }


    /**
     * 29.06.2021
     * Опция контроля   (141911)
     * Проверка "Получение заказа в ТТ" (141910) ReceivingAnOrder
     */
    private void checkReceivingAnOrder_141911(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        if (DetailedReportActivity.rpAmountSum > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }
    }

    /**
     * 29.06.2021
     * Опция контроля   (141888)
     * Проверка "Выкуп Товара с ТТ" (141889)
     */
    private void check_RENAME_2(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        if (DetailedReportActivity.rpTotalSumToRedemptionOfGoods > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }
    }


    private void checkEKL(Context context, WpDataDB wp, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        List<EKL_SDB> list = SQL_DB.eklDao().getByDad2(wp.getCode_dad2());

//        List<EKL_SDB> list2 = ;

        int count = 0;
        for (EKL_SDB item : list) {
            if (item.eklCode != null && item.eklHashCode != null) {
                count++;
                break;
            }
        }

        if (count > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }
    }


    // ----------------------------------

    public void checkingSignalsOfTheExecutorReport(Context context, WpDataDB wpDataDB, int warningType, int mode) {
//        Log.e("OPTION_CONTROL", "OPTION_CONTROL: " + "ENTER");
//
//        // Получить Опции (Таблицу)
//        WorkPlan workPlan = new WorkPlan();
//        try {
//            List<OptionsDB> list = RealmManager.getOptionsButton(workPlan.getWpOpchetId(wpDataDB));
//            for (int i = 0; i < list.size(); i++) {
//                optionControl(context, wpDataDB, list.get(i), warningType, mode);
//            }
//        } catch (Exception e) {
//            Globals.addLog();// Не работает
//        }
    }


    // =============================================================
    // ОПЦИИ ДЛЯ ДЕТАЛ. ОТЧЁТА (ОТЧЁТА ИСПОЛНИТЕЛЯ / REPORT PREPARE)
    // =============================================================

    /**
     * 26.01.2021
     */
    // 1. Допустим Опции у нас уже отсортированы пришли так как надо
    public String getOptionString(List<OptionsDB> optionsDB, ReportPrepareDB reportPrepareTovar) {
        String res = ""; // Итоговая строка всех ТПЛ-ов
        StringBuilder tplRequired = new StringBuilder(); // Обязательные ТПЛ-ы
        StringBuilder tplOptional = new StringBuilder(); // Опциональные ТПЛ-ы

        // Получаем список Опций (Ф,Ц,П...) сам список захардкожен.
        List<TovarOptions> listTovOpt = getTovarOptins();
//        Collections.reverse(listTovOpt);

        // Создаём временный список Опций ТПЛ-ов
        List<TovarOptions> temps = new ArrayList<>();

        // Пробегаемся по всем опциям данного документа
        for (OptionsDB option : optionsDB) {
            int optionId = Integer.parseInt(option.getOptionId()); // Получаем ID опции
            int optionControlId = Integer.parseInt(option.getOptionControlId()); // Получаем ID опции

            // Есть ли данная опция среди Опций ТПЛ-ов (та что захардкожена)
            // todo нельзя ли использовать сейчас listTovOpt?
            /*19.10.21 -- Внезапно оказалось что Ф.Ц.К.. могут не только в optionId находиться, но
             * и в optionControlId. На данный момент ситуация выглядит следущим образом: Есть опции
             * по типу "Начало работы" и тп.., а есть опции Фейс, Цена, Кол-во и тп..
             * Ф.Ц.К.. должны отображаться как и из "прихардкоженного" второго варианта, так и из
             * опции контроля из первого типа опций*/
            // TODO НОРМАЛЬНО НАПИСАТЬ ЭТОТ МОМЕНТ
            if (ids.contains(optionId)) {
                // Получаю подробную инфу о текущей ТПЛке
                TovarOptions temp = listTovOpt.get(listTovOpt.indexOf(new TovarOptions(optionId)));

                // Должен добавить в 'temps' элемент + записывать в опцию её символ
                // todo должен написать функцию.
                if (!containsName(temps, temp.getOrderField())) {
                    Globals.Triple uploaded = checkUploadedTPL(reportPrepareTovar, getTPLData(temp, reportPrepareTovar));
                    tplRequired.append(setOptionTPLColor(temp.getOptionShort(), true, uploaded));
                    temps.add(temp);
                }
            }

            if (ids.contains(optionControlId)) {
                TovarOptions temp = listTovOpt.get(listTovOpt.indexOf(new TovarOptions(optionControlId)));
                if (!containsName(temps, temp.getOrderField())) {
                    Globals.Triple uploaded = checkUploadedTPL(reportPrepareTovar, getTPLData(temp, reportPrepareTovar));
                    tplRequired.append(setOptionTPLColor(temp.getOptionShort(), true, uploaded));
                    temps.add(temp);
                }
            }
        }

        // После пробега по опциям данного документа (они являются обязательными), я должен
        // разобраться с опциональными. + разукрасить их в зависимости от того как они и чем заполнены
        for (TovarOptions option : listTovOpt) {
            boolean flag = false;
            for (TovarOptions temp : temps) {
                if (option.equals(temp)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                Globals.Triple uploaded = checkUploadedTPL(reportPrepareTovar, getTPLData(option, reportPrepareTovar));
                Log.e("TPL_UPLOAD", "2: " + option.getOptionShort());
                Log.e("TPL_UPLOAD", "2.uploaded: " + uploaded);
                tplOptional.append(setOptionTPLColor(option.getOptionShort(), false, uploaded));
//                tplOptional.append(option.getOptionShort());
            }
        }


        res = tplRequired + "/" + tplOptional;

        return res;
    }


    /**
     * 26.01.2021
     */
    private String getTPLData(TovarOptions tovarOptions, ReportPrepareDB tableRow) {
        if (tableRow == null) return "";
        switch (tovarOptions.getOptionControlName()) {
            case PRICE:
                return tableRow.getPrice();
            case FACE:
                return tableRow.getFace();
            case EXPIRE_LEFT:
                return tableRow.getExpireLeft();
            case AMOUNT:
                return String.valueOf(tableRow.getAmount());
            case UP:
                return tableRow.getUp();
            case DT_EXPIRE:
                return tableRow.getDtExpire();
            case OBOROTVED_NUM:
                return tableRow.getOborotvedNum();
            case ERROR_ID:
                return tableRow.getErrorId();
            case AKCIYA_ID:
                return tableRow.getAkciyaId();
            case AKCIYA:
                return tableRow.getAkciya();
            case NOTES:
                return tableRow.getNotes();

            default:
                return "";
        }
    }


    /**
     * 26.01.2021
     * Определение "выгруженности"
     */
    private Globals.Triple checkUploadedTPL(ReportPrepareDB tableRow, String data) {

        Log.e("checkUploadedTPL2", "data: " + data);

        if (tableRow == null) return Globals.Triple.NO_DATA;

        if (data.equals("")) return Globals.Triple.NO_DATA;

        if (tableRow.getUploadStatus() == 0 && !data.equals("0") && !data.equals("0000-00-00")) {
            return Globals.Triple.TRUE;
        } else if (tableRow.getUploadStatus() == 1 && !data.equals("0")) {
            return Globals.Triple.FALSE;
        } else if (data.equals("0") || data.equals("0000-00-00")) { // todo чо ругаешься
            return Globals.Triple.NO_DATA;
        } else {
            return Globals.Triple.NO_DATA;
        }
    }


    /**
     * 26.01.2021
     * Разукрашивание опций.
     */
    private String setOptionTPLColor(String res, boolean required, Globals.Triple uploaded) {
        String green = "<font color=green>TPL</font>";
        String yellow = "<font color=yellow>TPL</font>";
        String req = "<b>TPL</b>";

        if (required) res = req.replace("TPL", res);

        if (uploaded.equals(Globals.Triple.TRUE)) res = green.replace("TPL", res);
        else if (uploaded.equals(Globals.Triple.FALSE)) res = yellow.replace("TPL", res);

        return res;
    }


    /**
     * 20.07.2020
     * <p>
     * Узнаём - есть ли уже опция тпл-ов уже в листе.
     * <p>
     * (нужно изза того что некоторые опции имеют несколько id из-за чего могут
     * лишний раз дублироваться)
     * <p>
     * true - если уже есть такая опция
     */
    private boolean containsName(final List<TovarOptions> list, final String orderField) {
        for (TovarOptions o : list)
            if (o.getOrderField().equals(orderField)) return true;
        return false;
    }


    /**
     * 20.07.2020
     * <p>
     * Нужно для определения - есть ли подсказки в Опциях ТПЛ или нет
     */
    private boolean containsNameReportHint(final List<ReportHintList> list, final String field) {
        for (ReportHintList o : list)
            if (o.getField().equals(field)) return true;
        return false;
    }


    // Передача инфы об обязательных Опциях ТПЛов
    public List<TovarOptions> getRequiredOptionsTPL(List<OptionsDB> optionsDB) {
        List<TovarOptions> tplOptionsList = getTovarOptins();

        List<TovarOptions> temps = new ArrayList<>();
        for (OptionsDB option : optionsDB) {
            int optionId = Integer.parseInt(option.getOptionId());
            int optionControlId = Integer.parseInt(option.getOptionControlId());
            if (ids.contains(optionId)) {
                int optId = Integer.parseInt(option.getOptionId());
                TovarOptions temp = tplOptionsList.get(tplOptionsList.indexOf(new TovarOptions(optId)));

                // Это нужно что б 2 раза не появлялись Диалоги
                // Проверяем "есть ли уже такая опция" ?
                if (!temps.contains(temp)) {
                    temps.add(temp);
                } else {
                    Log.e("dublicateTPL", "ПОВТОРЯЕТСЯ");
                }
            }

            if (ids.contains(optionControlId)) {
                TovarOptions temp = tplOptionsList.get(tplOptionsList.indexOf(new TovarOptions(optionControlId)));
                // Это нужно что б 2 раза не появлялись Диалоги
                // Проверяем "есть ли уже такая опция" ?
                if (!temps.contains(temp)) {
                    temps.add(temp);
                }
            }
        }
        return temps;
    }

    public List<TovarOptions> getAllOptionsTPL() {
        return getTovarOptins();
    }


    /*отвечаю на вопрос: зачем там array idшников опций?
    - Потому что, в зависимости от этой опции, колонка в базе данных (тут говорим про колонку из
    таблички ReportPrepare (дет. отчёт)) она будет себя вести по разному. Разный текст ил разное
    поведение. На данный момент это зависит от ТЕМЫ данного посещения (вроде из WPData)
    */
    private List<TovarOptions> getTovarOptins() {
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
            list.add(new TovarOptions(PRICE, "Ц", "Цена товара", "price", "main", 579));
            list.add(new TovarOptions(FACE, "Ф", "Кол. фейсов", "face", "main", 576, 76815));
            list.add(new TovarOptions(EXPIRE_LEFT, "В", "Возврат", "expire_left", "main", 135591));
            list.add(new TovarOptions(AMOUNT, "К", "Кол. на витрине", "amount", "main", 578, 587, 1465));
            list.add(new TovarOptions(UP, "П", "Поднято товара", "up", "main", 138644));
            list.add(new TovarOptions(DT_EXPIRE, "Д", "Дата ок. ср. год", "dt_expire", "main", 84005, 84967));
            list.add(new TovarOptions(OBOROTVED_NUM, "О", "Остаток по учёту", "oborotved_num", "main", 2243, 135448));
            list.add(new TovarOptions(ERROR_ID, "Ш", "Ошибка товара", "error_id", "main", 135592));
            list.add(new TovarOptions(AKCIYA_ID, "А", "Вид акции", "akciya_id", "main", 80977));
            list.add(new TovarOptions(AKCIYA, "Н", "Наличие акции", "akciya", "main", 80977));
            list.add(new TovarOptions(NOTES, "П", "Примечание", "notes", "main", 135590));

//            list.add(new TovarOptions(NOTES, "П", "Примечание", "notes", "main", 135590));
//            list.add(new TovarOptions(AKCIYA, "Н", "Наличие акции", "akciya", "main", 80977));
//            list.add(new TovarOptions(AKCIYA_ID, "А", "Вид акции", "akciya_id", "main", 80977));
//            list.add(new TovarOptions(ERROR_ID, "Ш", "Ошибка товара", "error_id", "main", 135592));
//            list.add(new TovarOptions(OBOROTVED_NUM, "О", "Остаток по учёту", "oborotved_num", "main", 2243, 135448));
//            list.add(new TovarOptions(DT_EXPIRE, "Д", "Дата ок. ср. год", "dt_expire", "main", 84005, 84967));
//            list.add(new TovarOptions(UP, "П", "Поднято товара", "up", "main", 138644));
//            list.add(new TovarOptions(AMOUNT, "К", "Кол. на витрине", "amount", "main", 578, 587, 1465));
//            list.add(new TovarOptions(EXPIRE_LEFT, "В", "Возврат", "expire_left", "main", 135591));
//            list.add(new TovarOptions(FACE, "Ф", "Кол. фейсов", "face", "main", 576, 76815));
//            list.add(new TovarOptions(PRICE, "Ц", "Цена товара", "price", "main", 579));

            for (TovarOptions to : list) {
                ids.addAll(to.getOptionId());
            }
        }
        return list;
    }


}




/*
$options_list_tpl=array(
        array( Число
            'options'    =>array(579),
            'sign'       =>'Ц',
            'sign_descr' =>'Цена товара',
            'order_field'=>'price'
        ),
        array( Число
            'options'    =>array(576, 76815),
            'sign'       =>'Ф',
            'sign_descr' =>'Кол. фейсов',
            'order_field'=>'face'
        ),
        array( Число
            'options'    =>array(135591),
            'sign'       =>'В',
            'sign_descr' =>'Возврат',
            'order_field'=>'expire_left',
            'show_always'=>true
        ),
        array( Числ о
            'options'    =>array(578, 1465),
            'sign'       =>'К',
            'sign_descr' =>'Кол. на витрине',
            'order_field'=>'amount'
        ),
        array( ДАта
            'options'    =>array(84005, 84967),
            'sign'       =>'Д',
            'sign_descr' =>'Дата ок. ср. год',
            'order_field'=>'dt_expire'
        ),
        array( Число
            'options'    =>array(135448, 2243),
            'sign'       =>'О',
            'sign_descr' =>'Остаток по учёту',
            'order_field'=>'oborotved_num'
        ),
        array( Число, предопределённый тип (это надо получать) Справочник Ошибок
            'options'    =>array(135592),
            'sign'       =>'Ш',
            'sign_descr' =>'Ошибка товара',
            'order_field'=>'error_id',
            'show_always'=>true
        ),
        array(  Справочник Акций, выбор
            'options'    =>array(80977),
            'sign'       =>'А',
            'sign_descr' =>'Вид акции',
            'order_field'=>'akciya_id'
        ),
        array( 0 - не выбрано, 1 - есть , 2 - нет
            'options'    =>array(80977),
            'sign'       =>'Н',
            'sign_descr' =>'Наличие акции',
            'order_field'=>'akciya'
        ),
        array(  Строка
            'options'    =>array(135590),
            'sign'       =>'П',
            'sign_descr' =>'Примечание',
            'order_field'=>'notes',
            'show_always'=>true
        )


(PRICE,         "Ц", "Цена товара"              , "price", "main", 579));
(FACE,          "Ф", "Кол. фейсов"              , "face", "main", 576, 76815));
(EXPIRE_LEFT,   "В", "Возврат"                  , "expire_left", "main", 135591));
(AMOUNT,        "К", "Кол. на витрине"          , "amount", "main", 578, 1465));
(UP,            "П", "Поднято товара"           , "up", "main", 138644));
(DT_EXPIRE,     "Д", "Дата ок. ср. год"         , "dt_expire", "main", 84005, 84967));
(OBOROTVED_NUM, "О", "Остаток по учёту"         , "oborotved_num", "main", 2243, 135448));
(ERROR_ID,      "Ш", "Ошибка товара"            , "error_id", "main", 135592));
(AKCIYA_ID,     "А", "Вид акции"                , "akciya_id", "main", 80977));
(AKCIYA,        "Н", "Наличие акции"            , "akciya", "main", 80977));
(NOTES,         "П", "Примечание"               , "notes", "main", 135590));



        );*/
