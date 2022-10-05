package ua.com.merchik.merchik.Options;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARSecondFrag;
import ua.com.merchik.merchik.Adapters.TextAdapter;
import ua.com.merchik.merchik.BuildConfig;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto;
import ua.com.merchik.merchik.Options.Buttons.OptionButPhotoPlanogramm;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonAddComment;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonAddNewClient;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonAvailabilityDetailedReport;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPhotoBeforeStartWork;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonReclamationAnswer;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonTaskAnswer;
import ua.com.merchik.merchik.Options.Controls.OptionControlAddComment;
import ua.com.merchik.merchik.Options.Controls.OptionControlAvailabilityDetailedReport;
import ua.com.merchik.merchik.Options.Controls.OptionControlCheckingReasonOutOfStock;
import ua.com.merchik.merchik.Options.Controls.OptionControlCheckingReasonOutOfStockOSV;
import ua.com.merchik.merchik.Options.Controls.OptionControlEKL;
import ua.com.merchik.merchik.Options.Controls.OptionControlEndAnotherWork;
import ua.com.merchik.merchik.Options.Controls.OptionControlFacePlan;
import ua.com.merchik.merchik.Options.Controls.OptionControlPhotoBeforeStartWork;
import ua.com.merchik.merchik.Options.Controls.OptionControlPromotion;
import ua.com.merchik.merchik.Options.Controls.OptionControlReclamationAnswer;
import ua.com.merchik.merchik.Options.Controls.OptionControlRegistrationPotentialClient;
import ua.com.merchik.merchik.Options.Controls.OptionControlTaskAnswer;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.VersionApp;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Data;
import ua.com.merchik.merchik.data.Database.Realm.VirtualAdditionalRequirementsDB;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsJOIN.AdditionalMaterialsJOINAdditionalMaterialsAddressSDB;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsSDB;
import ua.com.merchik.merchik.data.Database.Room.EKL_SDB;
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;
import ua.com.merchik.merchik.data.Database.Room.OpinionThemeSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
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
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements.DialogARMark.DialogARMark;
import ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements.DialogAdditionalRequirements;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.toolbar_menus;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.OFS;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.OOS;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.SKUFact;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.SKUPlan;
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
import static ua.com.merchik.merchik.data.OptionMassageType.Type.DIALOG;
import static ua.com.merchik.merchik.data.OptionMassageType.Type.STRING;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

public class Options {

    private Globals globals = new Globals();

    private Integer[] describedOptions = new Integer[]{132624, 76815, 157241, 157243, 84006, 156928,
            151594, 80977, 135330, 133381, 135329, 138518, 151139, 132623, 133382, 137797, 135809,
            135328, 135327, 157275};

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
    public <T> void optionControl(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {

        try {
            Log.e("OPTION_CONTROL", "HERE(0): " + optionsDB.getOptionControlId());

            int optionControlId = Integer.parseInt(optionsDB.getOptionControlId());

            OptionMassageType newOptionType = new OptionMassageType();
            switch (mode) {
                case NULL:
                    break;

                case CHECK_CLICK:
                    newOptionType.type = DIALOG;
                    break;
            }

            switch (optionControlId) {
                case 157275:
                    OptionControlFacePlan<?> optionControlFacePlan = new OptionControlFacePlan<>(context, dataDB, optionsDB, newOptionType, mode);
                    optionControlFacePlan.showOptionMassage();
                    break;

                case 84006:
                    OptionControlEKL<?> optionControlEKL = new OptionControlEKL<>(context, dataDB, optionsDB, newOptionType, mode);
                    optionControlEKL.showOptionMassage();
                    break;

                case 133381:
                    OptionControlRegistrationPotentialClient<?> optionControlRegistrationPotentialClient = new OptionControlRegistrationPotentialClient<>(context, dataDB, optionsDB, newOptionType, mode);
                    optionControlRegistrationPotentialClient.showOptionMassage();
                    break;

                case 151594:
                    OptionControlPhotoBeforeStartWork<?> optionControlPhotoBeforeStartWork = new OptionControlPhotoBeforeStartWork<>(context, dataDB, optionsDB, newOptionType, mode);
                    optionControlPhotoBeforeStartWork.showOptionMassage();
                    break;

                case 132624:
                    OptionControlAddComment<?> optionControlAddComment = new OptionControlAddComment<>(context, dataDB, optionsDB, newOptionType, mode);
                    optionControlAddComment.showOptionMassage();
                    break;

                case 80977:
                    OptionMassageType type1 = new OptionMassageType();
                    switch (mode) {
                        case NULL:
                            break;

                        case CHECK_CLICK:
                            type1.type = DIALOG;
                            break;
                    }

                    OptionControlPromotion optionControlPromotion = new OptionControlPromotion(context, dataDB, optionsDB, type1, mode);
                    optionControlPromotion.showOptionMassage();
                    break;

                case 84932: // Проверка наличия ФотоОтчётов (id мне дали из 1С) (тип 0)
                    checkPhotoReport(context, dataDB, optionsDB, type, mode);
                    break;

                case 134583:    // ПРоверка наличия фотоотчётов с привязкой к координатам
                    // Нужно дописать
                    checkPhotoReportWithMP(context, dataDB, optionsDB, type, mode);
                    break;

                case 1470:  // Проверка наличия Фото остатков товара (тип 4)
                    checkPhoto(dataDB, optionsDB, "4");
                    break;

                case 132971:  // Проверка наличия Фото тележка с товаром (тип 10)
                    checkPhoto(dataDB, optionsDB, "10");
                    break;

                case 141361:  // Проверка наличия Фото тележка с товаром (тип 31)
                    checkPhoto(dataDB, optionsDB, "31");
                    break;

                case 141886:    // Проверка наличия Фото Документов (3)
                    checkPhoto(dataDB, optionsDB, "3");
                    break;

                case 76815: // Проверка наличия Дет.Отчётов (id мне дали из 1С)
                    Log.e("OPTION_CONTROL", "Проверка наличия Дет.Отчётов" + optionsDB.getOptionControlId());
//                    check76815(dataDB, optionsDB); // Проверка Представленности
                    OptionMassageType type2 = new OptionMassageType();
                    switch (mode) {
                        case NULL:
                            break;

                        case CHECK_CLICK:
                            type2.type = DIALOG;
                            break;
                    }
                    OptionControlAvailabilityDetailedReport optionControlAvailabilityDetailedReport = new OptionControlAvailabilityDetailedReport(context, dataDB, optionsDB, type2, mode);
                    break;

                case 138519:
                    Log.e("OPTION_CONTROL", "checkStartWork: " + optionsDB.getOptionControlId());
//                checkStartWork(context, dataDB, optionsDB, type, mode);
                    optionControlStartWork_138519(context, dataDB, optionsDB, type, mode);
                    break;

                case 138521:
                    Log.e("OPTION_CONTROL", "checkEndWork: " + optionsDB.getOptionControlId());
//                checkEndWork(context, dataDB, optionsDB, type, mode);
                    optionControlEndWork_138521(context, dataDB, optionsDB, type, mode);
                    break;

                case 8299:
                    Log.e("OPTION_CONTROL", "checkMP: " + optionsDB.getOptionControlId());
//                checkMP(context, dataDB, optionsDB, type, mode);
                    optionControlMP_8299(context, dataDB, optionsDB, type, mode);
                    break;

                case 141911:
                    // !!!!!!!
                    checkReceivingAnOrder_141911(context, dataDB, optionsDB, type, mode);
                    break;

                case 141889:
                    // !!!!!!!
                    check_RENAME_2(context, dataDB, optionsDB, type, mode);
                    break;

//                case 84006:
//                    // !!!!!!!
//                    checkEKL(context, dataDB, optionsDB, type, mode);
//                    break;

                case 587:
                    optionControlReceivingAnOrder_587(context, dataDB, optionsDB, null, NNKMode.CHECK);
                    break;

                case 138341:
                    try {
                        optionControlAdditionalRequirements_138341(context, dataDB, optionsDB, null, mode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case 139577:
                    optionControlVersion_139577(context, dataDB, optionsDB, null, mode);
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

    /**
     * 07.06.2022
     * Решил переписать Нажатие На Кнопку(ННК), потому что предыдущая реализация работала не совсем
     * правильно.
     */
    public <T> void optionNNK(Context context, T document, OptionsDB option, OptionMassageType massageType, NNKMode nnkMode, Clicks.clickVoid click) {
        Log.e("optionNNK", "--------------------------------");
        Log.e("optionNNK", "option: " + option);
        Log.e("optionNNK", "option id: " + option.getOptionId());

        // Сброс сигнала Опции
        option.setIsSignal("0");

        // Проход по первой опции блокировки
        if (!option.getOptionBlock1().equals("0")) {
            executeOption(context, document, option, Integer.parseInt(option.getOptionBlock1()), massageType, nnkMode);
        }

        // Проход по второй опции блокировки
        if (!option.getOptionBlock2().equals("0")) {
            executeOption(context, document, option, Integer.parseInt(option.getOptionBlock2()), massageType, nnkMode);
        }

    }

    /**
     * 07.06.2022
     * Контроль опций.
     */
    private <T> void executeOption(Context context, T document, OptionsDB option, int optionId, OptionMassageType massageType, NNKMode nnkMode) {
        String msg = "";
        switch (optionId) {
            case 135329:
                new OptionControlTaskAnswer(context, document, option, massageType, nnkMode);
                break;

            case 80977:
                new OptionControlPromotion(context, document, option, massageType, nnkMode);
                break;

            default:
                msg = "Данная опция находится в разработке.";
                break;
        }

    }


    /*
     * Обработка опций
     * Нажатие На Кнопку (ННК) -- абстрактное название. На самом деле в принципе обработка
     * состояний опций
     * */
    public <T> OptionMassageType NNK(Context context, T dataDB, OptionsDB option, OptionMassageType type, NNKMode mode, Clicks.clickVoid click) {
        OptionMassageType result = new OptionMassageType();
        int res = 0;    // Счётчик для накапливания "блокировок" у данной опции

        //
        option.setIsSignal("0");

        Log.e("NNK", "--------------------------------");
        Log.e("NNK", "option.option_id: " + option.getOptionId());
        Log.e("NNK", "START_res: " + res);

        Log.e("NNK", "option.getOptionBlock1(): " + option.getOptionBlock1());
        Log.e("NNK", "option.getOptionBlock2(): " + option.getOptionBlock2());


        // Проход по первой опции блокировки
        if (!option.getOptionBlock1().equals("0")) {
            res += optControl(context, dataDB, option, Integer.parseInt(option.getOptionBlock1()), type, mode);
        }

        // Проход по второй опции блокировки
        if (!option.getOptionBlock2().equals("0")) {
            res += optControl(context, dataDB, option, Integer.parseInt(option.getOptionBlock2()), type, mode);
        }

        Log.e("NNK", "END_res: " + res);


        if (res > 0) {
            switch (mode) {
                case NULL:

                    break;

                case MAKE:

                    break;

                case CHECK:
                    DialogData dialogData2 = new DialogData(context);
                    dialogData2.setTitle("Блокировка");
                    dialogData2.setText("Данная Опция заблокированна ОПЦИЕЙ: " + option.getOptionBlock1() + "/" + option.getOptionBlock2());
                    dialogData2.setClose(dialogData2::dismiss);

                    result.dialog = dialogData2;

                    return result;

            }
        } else {

            switch (mode) {
                case NULL:
                    break;

                case CHECK:
                    break;

                case MAKE:
                    optControl(context, dataDB, option, Integer.parseInt(option.getOptionId()), type, mode);
                    click.click();
                    result = type;
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
    public void conduct(Context context, WpDataDB wp, List<OptionsDB> options, int optCount, Clicks.click click) {
        int register = 0;

        DialogData dialog = new DialogData(context);

        SpannableStringBuilder optionsSum = new SpannableStringBuilder();
        double optionSumRes = 0;

        OptionMassageType type = new OptionMassageType();
        type.type = STRING;

        for (OptionsDB item : options) {
            Log.e("conduct", "------------------------------START----------------------------------");
            Log.e("conduct", "OptionsDB item.getOptionTxt(): " + item.getOptionTxt());
            Log.e("conduct", "OptionsDB item.getOptionId(): " + item.getOptionId());
            Log.e("conduct", "OptionsDB item.getOptionControlId(): " + item.getOptionControlId());

            // Блокирует опция или нет
            int controlResult = optControl(context, wp, item, Integer.parseInt(item.getOptionControlId()), type, NNKMode.CHECK);

            // Создаю список опций который блокирует
            if (controlResult == 0) {
                Log.e("conduct", "Опция контроля выполнена: " + controlResult);
            } else if (controlResult == 1) {
                Log.e("conduct", "Опция контроля НЕ выполнена: " + controlResult);
                Log.e("conduct", "Я добавил опцию: " + item.getOptionTxt());
                optionNotConduct.add(item);
            } else {
                Log.e("conduct", "Что-то пошло не так: " + controlResult);
            }

            // Если опция описана - добавляю ещё и ДЕНЬГИ в скобочку и считаю итоговую сумму
            if (ArrayUtils.contains(describedOptions, Integer.parseInt(item.getOptionControlId())) && controlResult != 1) {
                if (item.getIsSignal().equals("1")) {
                    StringBuffer msg = new StringBuffer();
                    optionsSum.append(createLinkedString(dialog,
                            msg.append("* ").append(item.getOptionControlTxt())/*.append(" (").append(counter2Text(wp)).append(")").append("\n")*/, item, click)).append(" ").append(Html.fromHtml("<font color=red>(" + counter2Text(wp) + "грн.)</font>")).append("\n");
                    ;

                    optionSumRes += wp.getCash_zakaz() * 0.08;
                }
            }

            Log.e("conduct", "-----------------------------END-----------------------------------");
        }

        Log.e("conduct", "optionNotConduct: " + optionNotConduct);

        if (optionNotConduct.size() > 0) {
//        if (register > 0) {


            // Не все опции(действия) выполнены
            // Не выполнены:

            dialog.setDialogIco();
            dialog.setTitle("Не все опции(действия) выполнены.");


            SpannableStringBuilder resStr = new SpannableStringBuilder();
            resStr.append("Не выполнены: \n\n");
            for (OptionsDB item : optionNotConduct) {
                Log.e("optionNotConduct", "------------------------------START----------------------------------");
                Log.e("optionNotConduct", "OptionsDB item.getOptionTxt(): " + item.getOptionTxt());
                Log.e("optionNotConduct", "OptionsDB item.getOptionId(): " + item.getOptionId());
                Log.e("optionNotConduct", "OptionsDB item.getOptionControlId(): " + item.getOptionControlId());
                StringBuffer msg = new StringBuffer();
                resStr.append(createLinkedString(dialog, msg.append("* ").append(item.getOptionControlTxt()), item, click)).append(" ").append(Html.fromHtml("<font color=red>(блок)</font>")).append("\n");
                Log.e("optionNotConduct", "------------------------------END----------------------------------");
            }
            resStr.append(optionsSum);
            resStr.append("\n\nУстраните указанные ошибки и повторите попытку проведения." + "\n\nВы можете ещё получить: " + "~")
                    .append(String.format("%.2f", optionSumRes)).append("грн, если выполните опции выше.");

            dialog.setText(resStr, () -> {
            });
            dialog.setClose(dialog::dismiss);
            dialog.show();

            // Устраните указанные ошибки и повторите попытку проведения
        } else {
            Toast.makeText(context, "Запрос на проведение создан", Toast.LENGTH_SHORT).show();
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (wp != null) {
                    wp.startUpdate = true;
                    wp.setSetStatus(1);
                    wp.setDt_update(System.currentTimeMillis() / 1000);
                    realm.insertOrUpdate(wp);
                }
            });
        }
    }

    private SpannableString createLinkedString(DialogData dialogData, StringBuffer msg, OptionsDB item, Clicks.click click) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                click.click(item);
                Toast.makeText(textView.getContext(), "Боооожечки, Ви не виконали опцію: " + item.getOptionControlTxt(), Toast.LENGTH_LONG).show();
                dialogData.dismiss();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        res.setSpan(clickableSpan, 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return res;
    }

    private CharSequence counter2Text(WpDataDB wpDataDB) {
        CharSequence res = "";
        res = "~" + String.format("%.2f", wpDataDB.getCash_zakaz() * 0.08);
        res = Html.fromHtml("<font color=red>" + res + "</font>");
        return res;
    }

    /*Проверка Опции*/
    public <T> int optControl(Context context, T dataDB, OptionsDB option, int optionId, OptionMassageType type, NNKMode mode) {

//        try {
        Log.e("NNK", "F/optControl/optionId: " + optionId);
        switch (optionId) {
            case 157274:
            case 157275:
                OptionControlFacePlan<?> optionControlFacePlan = new OptionControlFacePlan<>(context, dataDB, option, type, mode);
                optionControlFacePlan.showOptionMassage();
                return optionControlFacePlan.isBlockOption() ? 1 : 0;

            case 84006:
                OptionControlEKL<?> optionControlEKL = new OptionControlEKL<>(context, dataDB, option, type, mode);
                optionControlEKL.showOptionMassage();
                return optionControlEKL.isBlockOption() ? 1 : 0;

            case 133381:
                OptionControlRegistrationPotentialClient<?> optionControlRegistrationPotentialClient = new OptionControlRegistrationPotentialClient<>(context, dataDB, option, type, mode);
//                if (optionControlRegistrationPotentialClient.isBlockOption()) {
                optionControlRegistrationPotentialClient.showOptionMassage();
//                }
                return optionControlRegistrationPotentialClient.isBlockOption() ? 1 : 0;


            // Потенциальный клиент
            case 133382:
                OptionButtonAddNewClient<?> optionButtonAddNewClient = new OptionButtonAddNewClient<>(context, dataDB, option, type, mode);
                break;


            case 157243:
                OptionControlCheckingReasonOutOfStockOSV<?> optionControlCheckingReasonOutOfStockOSV = new OptionControlCheckingReasonOutOfStockOSV<>(context, dataDB, option, type, mode);
                if (optionControlCheckingReasonOutOfStockOSV.isBlockOption()) {
                    optionControlCheckingReasonOutOfStockOSV.showOptionMassage();
                }
                return optionControlCheckingReasonOutOfStockOSV.isBlockOption() ? 1 : 0;

            case 157241:
                OptionControlCheckingReasonOutOfStock<?> optionControlCheckingReasonOutOfStock = new OptionControlCheckingReasonOutOfStock<>(context, dataDB, option, type, mode);
//                if (optionControlCheckingReasonOutOfStock.isBlockOption()){
                optionControlCheckingReasonOutOfStock.showOptionMassage();
//                }
                return optionControlCheckingReasonOutOfStock.isBlockOption() ? 1 : 0;

            case 135809:
                new OptionButtonPhotoBeforeStartWork<>(context, dataDB, option, type, mode);
                break;

            case 151594:
                OptionControlPhotoBeforeStartWork<?> optionControlPhotoBeforeStartWork = new OptionControlPhotoBeforeStartWork<>(context, dataDB, option, type, mode);
                if (optionControlPhotoBeforeStartWork.isBlockOption()) {
                    optionControlPhotoBeforeStartWork.showOptionMassage();
                }
                return optionControlPhotoBeforeStartWork.isBlockOption() ? 1 : 0;

            case 135328:
                OptionButtonReclamationAnswer<?> optionButtonReclamationAnswer = new OptionButtonReclamationAnswer<>(context, dataDB, option, type, mode);
                break;

            case 135330:
                OptionControlReclamationAnswer<?> optionControlReclamationAnswer = new OptionControlReclamationAnswer<>(context, dataDB, option, type, mode);
                optionControlReclamationAnswer.showOptionMassage();
                return optionControlReclamationAnswer.isBlockOption() ? 1 : 0;

            case 132624:
                OptionControlAddComment<?> optionControlAddComment = new OptionControlAddComment<>(context, dataDB, option, type, mode);
                optionControlAddComment.showOptionMassage();
                return optionControlAddComment.isBlockOption() ? 1 : 0;

            case 132623:
                OptionButtonAddComment<?> optionButtonAddComment = new OptionButtonAddComment<>(context, dataDB, option, type, mode);
                break;

            case 76815:
                OptionControlAvailabilityDetailedReport optionControlAvailabilityDetailedReport = new OptionControlAvailabilityDetailedReport(context, dataDB, option, type, mode);
                if (optionControlAvailabilityDetailedReport.isBlockOption()) {
                    optionControlAvailabilityDetailedReport.showOptionMassage();
                }
//                if (mode.equals(NNKMode.CHECK)){
//                    optionControlAvailabilityDetailedReport.showOptionMassage();
//                }
//                optionControlAvailabilityDetailedReport.showOptionMassage();
                return optionControlAvailabilityDetailedReport.isBlockOption() ? 1 : 0;

            case 151139:
                new OptionButPhotoPlanogramm<>(context, dataDB, option, type, mode);
                break;

            case 80977:     // Контроль Акций

            case 156882:    // Кнопка Акций
                OptionControlPromotion<?> optionControlPromotion = new OptionControlPromotion<>(context, dataDB, option, type, mode);
                optionControlPromotion.showOptionMassage();

                return optionControlPromotion.isBlockOption() ? 1 : 0;

            case 156928:
                OptionControlEndAnotherWork optionControlEndAnotherWork = new OptionControlEndAnotherWork(context, dataDB, option, type, mode);
                optionControlEndAnotherWork.showOptionMassage();
                return optionControlEndAnotherWork.isBlockOption() ? 1 : 0;

            case 135327: // Задача
                OptionButtonTaskAnswer<?> optionButtonTaskAnswer = new OptionButtonTaskAnswer<>(context, dataDB, option, type, mode);
                break;

            case 135329:
                OptionControlTaskAnswer optionControlTask = new OptionControlTaskAnswer(context, dataDB, option, type, mode);
                optionControlTask.showOptionMassage();
                return optionControlTask.isBlockOption() ? 1 : 0;

            // Эти 2 в принципе разные, но для меня на данный момент они занимаются одним и тем же
            case 135742:// Дет. Отчёт по КлиентоАдресу
            case 137797:// Дет. Отчёт по Дад2
//                option135742(context, dataDB, option, type, mode);
                new OptionButtonAvailabilityDetailedReport(context, dataDB, option, type, mode);
                break;

            case 132621:   // Оценка
                option132621(context, dataDB, option, type, mode);
                break;

            case 84003:     // Мнение о сотруднике
                option84003(context, dataDB, option, type, mode);
                break;

            case 138339:
                if (dataDB instanceof WpDataDB) {
                    // Пока что пусто
                } else if (dataDB instanceof TasksAndReclamationsSDB) {
                    // Надо чем-то заполнить
                    option138339(context, dataDB, option, type, mode);
                }
                break;

            // ---

            case 138773:
                optionMP_138773(context, dataDB, option, type, mode);
                break;

//                case 8299:
//                    return optionControlMP_8299(context, dataDB, option, type, mode) ? 1 : 0;

            // ---

            case 138518:
                Log.e("NNK", "F/optControl/138518");
                if (dataDB instanceof WpDataDB) {
                    optionStartWork_138518(context, (WpDataDB) dataDB, option, type, mode);
//                        sendWpData2();
                } else if (dataDB instanceof TasksAndReclamationsSDB) {
                    optionStartWork_138518(context, (TasksAndReclamationsSDB) dataDB, option, type, mode);
                }
                break;

            case 138519:
                return optionControlStartWork_138519(context, dataDB, option, type, mode) ? 0 : 1;


            case 138520:
                if (dataDB instanceof WpDataDB) {
                    optionEndWork_138520(context, (WpDataDB) dataDB, option, type, mode);
//                        sendWpData2();
                } else if (dataDB instanceof TasksAndReclamationsSDB) {
                    optionEndWork_138520(context, (TasksAndReclamationsSDB) dataDB, option, type, mode);
                }
                break;

            case 138521:
                return optionControlEndWork_138521(context, dataDB, option, type, mode) ? 0 : 1;

            case 132968:
                if (dataDB instanceof WpDataDB) {
                    optionMakePhoto0_132968(context, (WpDataDB) dataDB, option, type, mode);
                } else if (dataDB instanceof TasksAndReclamationsSDB) {
                    optionMakePhoto0_132968(context, dataDB, option, type, mode);
                }
                break;

            // --- Опция контроля на Получение заказа в ТТ
            case 587:
                return optionControlReceivingAnOrder_587(context, dataDB, option, type, mode) ? 1 : 0;


            // Контроль Опции Доп. Требований
            case 138341:
                try {
                    optionControlAdditionalRequirements_138341(context, dataDB, option, type, mode);
                } catch (Exception e) {
                }
                break;

            case 139577:
                optionControlVersion_139577(context, dataDB, option, null, NNKMode.CHECK_CLICK);
                break;


            // Контроль фотоотчётов
            case 84932: // Проверка наличия ФотоОтчётов (id мне дали из 1С) (тип 0)
                return checkPhotoReport(context, dataDB, option, type, mode) ? 1 : 0;
//                    break;

            // Доп. Материалы
            case 138340:
                option138340(context, dataDB, option, type, mode);
                break;


            default:

                switch (mode) {
                    case NULL:
                        return 0;

                    case CHECK:
//                        Toast.makeText(context, "Данная Опция находится в РАЗРАБОТКЕ!", Toast.LENGTH_SHORT).show();
                        return 0;

                    case MAKE:
//                            Toast.makeText(context, "Данная Опция находится в РАЗРАБОТКЕ", Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, "" + option.getNotes() + "\n\n" + option.getOptionControlTxt(), Toast.LENGTH_LONG).show();
                        return 0;
                }
        }
//        } catch (Exception e) {
//            Globals.writeToMLOG("ERROR", "optControl2", "Exception: " + e);
//        }

        return 0;
    }


    //#когда не знаешь что такое полиморфизм
    private <T> void option84003(Context context, T dataDB, OptionsDB option, OptionMassageType type, NNKMode mode) {
        if (dataDB instanceof TasksAndReclamationsSDB) {

            TasksAndReclamationsSDB tarDB = (TasksAndReclamationsSDB) dataDB;

            DialogData dialog = new DialogData(context);
            dialog.setTitle("Выбор мнения");
            dialog.setText("Выберите мнение о сотруднике кликнув по нему");
            dialog.setClose(dialog::dismiss);

            List<OpinionThemeSDB> opinionThemeSDB = SQL_DB.opinionThemeDao().getByTheme(tarDB.themeId);
            List<String> ids = new ArrayList<>();
            for (OpinionThemeSDB item : opinionThemeSDB) {
                ids.add(item.mnenieId);
            }

            TextAdapter adapter = new TextAdapter(SQL_DB.opinionDao().getOpinionByIds(ids), new Clicks.click() {
                @Override
                public <T> void click(T data) {
                    String information = (String) data;
                    OpinionSDB opinionSDB = SQL_DB.opinionDao().getOpinionByNm(information);

                    tarDB.sotrOpinionDt = System.currentTimeMillis() / 1000;
                    tarDB.sotrOpinionAuthorId = Globals.userId;
                    tarDB.sotrOpinionId = opinionSDB.id;

                    SQL_DB.tarDao().insertData(Collections.singletonList(tarDB));

                    Exchange.updateTAR(tarDB);

                    Toast.makeText(context, "Вы выбрали: " + data, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            dialog.setRecycler(adapter, new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

            dialog.show();
        } else if (dataDB instanceof WpDataDB) {
            Toast.makeText(context, "Данный функционал в разработке", Toast.LENGTH_SHORT).show();
        }
    }

    // Установка оценки для Задачи
    private <T> void option132621(Context context, T dataDB, OptionsDB option, OptionMassageType type, NNKMode mode) {

        float rating = 0;
        if (dataDB instanceof TasksAndReclamationsSDB) {
            rating = ((TasksAndReclamationsSDB) dataDB).voteScore;

            DialogARMark dialog = new DialogARMark(context);
            dialog.setTitle("Укажите оценку");
            dialog.setClose(dialog::dismiss);
            dialog.setRatingBar(rating, new Clicks.click() {
                @Override
                public <T> void click(T data) {
                    ((TasksAndReclamationsSDB) dataDB).voteScore = (Integer) data;

                    SQL_DB.tarDao().insertData(Collections.singletonList((TasksAndReclamationsSDB) dataDB))
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    Log.d("test", "test");
                                }

                                @Override
                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                    Log.d("test", "test");
                                }
                            });
                }
            });
            dialog.show();
        } else if (dataDB instanceof WpDataDB) {
            Toast.makeText(context, "Данный функционал в разработке", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private <T> void option138339(Context context, T dataDB, OptionsDB option, OptionMassageType type, NNKMode mode) {
        List<AdditionalRequirementsDB> data = AdditionalRequirementsRealm.getData3(dataDB);

        DialogAdditionalRequirements dialogAdditionalRequirements = new DialogAdditionalRequirements(context);

        dialogAdditionalRequirements.setTitle("Доп. требования (" + data.size() + ")");
        dialogAdditionalRequirements.setRecycler(data);

        dialogAdditionalRequirements.setClose(dialogAdditionalRequirements::dismiss);
        dialogAdditionalRequirements.setLesson(context, true, 1232);
        dialogAdditionalRequirements.setVideoLesson(context, true, 1233, () -> {
        });
        dialogAdditionalRequirements.show();
    }

    private <T> void option138340(Context context, T dataDB, OptionsDB option, OptionMassageType type, NNKMode mode) {
        List<AdditionalMaterialsSDB> dataTest = SQL_DB.additionalMaterialsDao().getAllByClientId(option.getClientId());
        String expire = Clock.getHumanTimeYYYYMMDD(System.currentTimeMillis() / 1000);
        List<AdditionalMaterialsSDB> data1 = SQL_DB.additionalMaterialsDao().getAllForOption(option.getClientId(), "1", "0", expire);
        List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> data = SQL_DB.additionalMaterialsDao().getAllForOptionTEST(option.getClientId(), "1", "0");

        DialogAdditionalRequirements dialogAdditionalRequirements = new DialogAdditionalRequirements(context);

        dialogAdditionalRequirements.setTitle("Доп. материалы (" + data.size() + ")");
        dialogAdditionalRequirements.setRecyclerAM(data1);

        dialogAdditionalRequirements.setClose(dialogAdditionalRequirements::dismiss);
        dialogAdditionalRequirements.show();
    }

    private <T> void option135742(Context context, T dataDB, OptionsDB option, OptionMassageType type, NNKMode mode) {
        try {
            WpDataDB wp;
            if (dataDB instanceof WpDataDB) {
                wp = ((WpDataDB) dataDB);

                DialogData dialog = new DialogData(context);
                dialog.setTitle("Представленность");

                String msg = String.format("SKU (План): %s шт.\nSKU (Факт): %s шт.\nОтсутствует: %s шт.\nOOS (out of stock): %s %%\nПредставленность: %s %%\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\tОписание\n\nSKU (План) - количество товарных позиций которые должны быть в торговой точке по плану.\nSKU (Факт) - количество товарных позиций которые фактически стоят на витрине.\nOOS - процент товара, который отсутствует по сравнению с планом\nOOS = 100 - 100*(SKUФакт/SKUПлан) = %s %%\nПредставленность = 100 - OOS = %s %%", (int) SKUPlan, (int) SKUFact, (int) SKUPlan - (int) SKUFact, (int) DetailedReportActivity.OOS, (int) DetailedReportActivity.OFS, (int) DetailedReportActivity.OOS, (int) DetailedReportActivity.OFS);
                dialog.setText(msg);

                dialog.setClose(dialog::dismiss);
                dialog.show();
            } else if (dataDB instanceof TasksAndReclamationsSDB) {
                Long dad2Wp = ((TasksAndReclamationsSDB) dataDB).codeDad2SrcDoc;
                wp = RealmManager.INSTANCE.copyFromRealm(WpDataRealm.getWpDataRowByDad2Id(dad2Wp));

                Data D = new Data(
                        wp.getId(),
                        wp.getAddr_txt(),
                        wp.getClient_txt(),
                        wp.getUser_txt(),
                        wp.getDt(),  //+TODO CHANGE DATE
                        0,
                        "",
                        R.mipmap.merchik);

                WorkPlan workPlan = new WorkPlan();
                WPDataObj wpDataObj = workPlan.getKPS(wp.getId());

                Intent intent = new Intent(context, DetailedReportActivity.class);
                intent.putExtra("dataFromWP", D);
                intent.putExtra("rowWP", (Serializable) wp);
                intent.putExtra("dataFromWPObj", wpDataObj);

                type.msg = "Открыт проверяемый документ. \nНомер: " + wp.getDoc_num_otchet();

                context.startActivity(intent);
            } else {
                return;
            }


        } catch (Exception e) {
            type.msg = "Ошибка: " + e;
        }
    }

    //----------------------------------------------------------------------------------------------

    // Новый набор опций. Переписывание как в 1С (типо правильно)

    /**
     * Опция Контроля
     * Проверка местоположения ( 8299 )
     */
    private <T> boolean optionControlMP_8299(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        boolean res;

        int visitStartGeoDistance = 0;
        if (dataDB instanceof WpDataDB) {
            visitStartGeoDistance = ((WpDataDB) dataDB).getVisit_start_geo_distance();
        }

        // Проверка Опции и запись в БД результата
        if (visitStartGeoDistance < 500 && visitStartGeoDistance > 0) {
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
//                    optionNotConduct.add(optionsDB);
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
    private <T> void optionMP_138773(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {

        WpDataDB wpDataDB;
        if (dataDB instanceof WpDataDB) {
            wpDataDB = ((WpDataDB) dataDB);
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            globals.fixMP();
            Toast.makeText(context, "Местоположение зафиксированно", Toast.LENGTH_SHORT).show();
            return;
        } else {
            return;
        }

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


    /*
            long dad2;
        if (dataDB instanceof WpDataDB){
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
        }else if (dataDB instanceof TasksAndReclamationsSDB){
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
        }else {
            return;
        }

        */

    /**
     * Опция контроля
     * Проверка на Начало работы ( 138519 )
     */
    private <T> boolean optionControlStartWork_138519(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        boolean res;

        long dad2, startWork, endWork;
        if (dataDB instanceof WpDataDB) {
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
            startWork = ((WpDataDB) dataDB).getVisit_start_dt();
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
            startWork = ((TasksAndReclamationsSDB) dataDB).dt_start_fact;
        } else {
            return res = false;
        }

        Log.e("checkStartWork", "ENTER THIS");
        if (startWork > 0) {
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
            case MAKE:
                if (res) {
                    // Всё хорошо с опцией контроля
                } else {
                    // Нужно отобразить сообщение что всё плохо
                    DialogData dialog = new DialogData(context);
                    dialog.setTitle("Ошибка");
                    dialog.setDialogIco();
                    dialog.setText("Прежде чем выполнять данную опцию (действие) вы должны выполнить опцию: " + "Контроль наличия времени начала работ (ВРН)");
                    dialog.setClose(dialog::dismiss);
                    dialog.show();
                }
                break;
            case CHECK:
//                if (!res && optionsDB.getBlockPns().equals("1")) {
//                if (!res) {
//                    optionNotConduct.add(optionsDB);
//                }
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
    private boolean optionStartWork_138518(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        boolean result;
        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.DetailedReportButtons.class.pressStartWork: " + "ENTER" + "\n");
        if (wpDataDB.getVisit_start_dt() > 0) {
            Toast.makeText(context, "Работа уже начата!", Toast.LENGTH_SHORT).show();
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.DetailedReportButtons.class.pressStartWork: " + "Работа уже начата!" + "\n");
            result = true;
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
//                globals.writeToMLOG(Clock.getHumanTime() + "_INFO.DetailedReportButtons.class.pressStartWork: " + "Вы начали работу в: " + startTime + " / отчёт: " + wpDataDB.getDoc_num_otchet() + "\n");
                Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork", "Вы начали работу в: " + startTime + " / отчёт: " + wpDataDB.getDoc_num_otchet());

                Toast.makeText(context, "Вы начали работу в: " + Clock.getHumanTimeOpt(startTime * 1000), Toast.LENGTH_SHORT).show();
                result = true;
            } catch (Exception e) {
                // Set to log error
                Toast.makeText(context, "Возникла ошибка: " + e, Toast.LENGTH_SHORT).show();
                result = false;
            }
        }

        conductOptCheck(mode, result, optionsDB);
        return result;
    }

    private void optionStartWork_138518(Context context, TasksAndReclamationsSDB dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.DetailedReportButtons.class.pressStartWork: " + "ENTER" + "\n");
        if (dataDB.dt_start_fact > 0) {
            Toast.makeText(context, "Работа уже начата!", Toast.LENGTH_SHORT).show();
        } else {
            try {
                long startTime = System.currentTimeMillis() / 1000;
                dataDB.dt_start_fact = startTime;
                dataDB.uploadStatus = 1;
                SQL_DB.tarDao().insertData(Collections.singletonList(dataDB))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                Log.d("test", "test");
                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                Log.d("test", "test");
                            }
                        });

                Toast.makeText(context, "Вы начали работу в: " + Clock.getHumanTimeOpt(startTime * 1000), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // Set to log error
                Toast.makeText(context, "Возникла ошибка: " + e, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //------------------- ОКОНЧАНИЕ РАБОТЫ-------------------------

    /**
     * Опция контроля
     * Проверка на Окончание работы( 138521 )
     */
    private <T> boolean optionControlEndWork_138521(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {

        boolean res;

        long dad2, startWork, endWork;
        if (dataDB instanceof WpDataDB) {
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
            startWork = ((WpDataDB) dataDB).getVisit_start_dt();
            endWork = ((WpDataDB) dataDB).getVisit_end_dt();
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
            startWork = ((TasksAndReclamationsSDB) dataDB).dt_start_fact;
            endWork = ((TasksAndReclamationsSDB) dataDB).dt_end_fact;
        } else {
            return res = false;
        }

        if (endWork > 0) {
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
//                if (!res) {
//                    optionNotConduct.add(optionsDB);
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
    private boolean optionEndWork_138520(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        boolean result;
        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.DetailedReportButtons.class.pressEndWork: " + "ENTER" + "\n");
        if (wpDataDB.getVisit_end_dt() > 0) {
            Toast.makeText(context, "Работа уже окончена!", Toast.LENGTH_SHORT).show();
            result = true;
        } else {
            if (wpDataDB.getVisit_start_dt() > 0) {
                try {
                    long endTime = System.currentTimeMillis() / 1000;
                    RealmManager.INSTANCE.executeTransaction(realm -> {
                        wpDataDB.setDt_update(System.currentTimeMillis() / 1000);
                        wpDataDB.setVisit_end_dt(endTime);
                        wpDataDB.setClient_end_dt(endTime);
                        wpDataDB.startUpdate = true;
                        wpDataDB.client_work_duration = (endTime - wpDataDB.getClient_start_dt());
                        realm.insertOrUpdate(wpDataDB);
                    });
//                    globals.writeToMLOG(Clock.getHumanTime() + "_INFO.DetailedReportButtons.class.pressEndWork: " + "Вы закончили работу в: " + endTime + " / отчёт: " + wpDataDB.getDoc_num_otchet() + "\n");
                    Globals.writeToMLOG("INFO", "_INFO.DetailedReportButtons.class.pressEndWork", "Вы закончили работу в: " + endTime + " / отчёт: " + wpDataDB.getDoc_num_otchet());
                    Toast.makeText(context, "Вы окончили работу в: " + Clock.getHumanTimeOpt(endTime * 1000) + "\n\nНе забудьте нажать 'Провести', что б система проверила текущий документ и начислила Вам премиальные", Toast.LENGTH_SHORT).show();
                    result = true;
                } catch (Exception e) {
                    // Set to log error
                    Toast.makeText(context, "Возникла ошибка: " + e, Toast.LENGTH_SHORT).show();
                    result = false;
                }
            } else {
                Toast.makeText(context, "Вы не можете закончить работу не начав её", Toast.LENGTH_SHORT).show();
                result = false;
            }
        }


//        conductOptCheck(mode, result, optionsDB);
        return result;
    }

    private void optionEndWork_138520(Context context, TasksAndReclamationsSDB dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        globals.writeToMLOG(Clock.getHumanTime() + "_INFO.DetailedReportButtons.class.pressEndWork: " + "ENTER" + "\n");
        if (dataDB.dt_end_fact > 0) {
            Toast.makeText(context, "Работа уже окончена!", Toast.LENGTH_SHORT).show();
        } else {
            if (dataDB.dt_start_fact > 0) {
                try {
                    long endTime = System.currentTimeMillis() / 1000;
                    dataDB.dt_end_fact = endTime;
                    dataDB.uploadStatus = 1;
                    SQL_DB.tarDao().insertData(Collections.singletonList(dataDB))
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    Log.d("test", "test");
                                }

                                @Override
                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                    Log.d("test", "test");
                                }
                            });
                    Toast.makeText(context, "Вы окончили работу в: " + endTime, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    // Set to log error
                    Toast.makeText(context, "Возникла ошибка: " + e, Toast.LENGTH_SHORT).show();
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
    private <T> void optionMakePhoto0_132968(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        if (dataDB instanceof TasksAndReclamationsSDB) {
            TARSecondFrag.TaRID = ((TasksAndReclamationsSDB) dataDB).id;
        }

        MakePhoto makePhoto = new MakePhoto();
//        makePhoto.openCamera((Activity) context);
        makePhoto.pressedMakePhoto((Activity) context, dataDB, "0");    // Фото Витрины
    }


    private void optionMakePhoto0_132968(Context context, WpDataDB dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        MakePhoto makePhoto = new MakePhoto();
        makePhoto.pressedMakePhoto((Activity) context, dataDB, "0");

        // Обычный способ выполенния фото по старинке, через активность
/*        WorkPlan workPlan = new WorkPlan();
        WpDataDB wpDataDB = (WpDataDB) dataDB;
        WPDataObj wpDataObj = workPlan.getKPS(wpDataDB.getId());
        Intent intentPhotoReport = new Intent(context, PhotoReportActivity.class);
        intentPhotoReport.putExtra("dataFromWPObj", wpDataObj);
        context.startActivity(intentPhotoReport);*/

    }


    /**
     * Опция контроля (587)
     */
    private <T> boolean optionControlReceivingAnOrder_587(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        boolean res;

        long dad2, startWork, endWork;
        if (dataDB instanceof WpDataDB) {
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
        } else {
            return res = false;
        }

        List<ReportPrepareDB> rp = ReportPrepareRealm.getReportPrepareByDad2(dad2);

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
//                if (!res && optionsDB.getBlockPns().equals("1")) {
//                    optionNotConduct.add(optionsDB);
//                }

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
    private <T> boolean optionControlAdditionalRequirements_138341(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        boolean res = false;
        try {
            double averageRating = 0;  // Средняя Оценка
            double deviationFromTheMeanSize = 0;    // Отклонение от среднего
            int markSum = 0;
            int nedotochSize = 0;

            StringBuilder msg = new StringBuilder();

            int userId;
            long dad2, startWork, endWork;
            long date;
            String clientId, userTxt;
            if (dataDB instanceof WpDataDB) {
                WpDataDB wp = (WpDataDB) dataDB;

                dad2 = ((WpDataDB) dataDB).getCode_dad2();
                startWork = ((WpDataDB) dataDB).getVisit_start_dt();
                date = Clock.dateConvertToLong(Clock.getHumanTimeYYYYMMDD(wp.getDt().getTime() / 1000));
                userId = ((WpDataDB) dataDB).getUser_id();
                clientId = ((WpDataDB) dataDB).getClient_id();
                userTxt = ((WpDataDB) dataDB).getUser_txt();
            } else if (dataDB instanceof TasksAndReclamationsSDB) {
                TasksAndReclamationsSDB tar = (TasksAndReclamationsSDB) dataDB;

                dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
                startWork = ((TasksAndReclamationsSDB) dataDB).dt_start_fact;
                date = tar.dt;
                userId = ((TasksAndReclamationsSDB) dataDB).vinovnikScoreUserId;
                clientId = ((TasksAndReclamationsSDB) dataDB).client;
                userTxt = ((TasksAndReclamationsSDB) dataDB).sortNm;
            } else {
                return res = false;
            }

            long dt = date;       // Дата документа в Unix
            long dateFrom = Clock.getDatePeriodLong(date, -30) / 1000; // Дата документа -30 дней
            long dateTo = Clock.getDatePeriodLong(date, +3) / 1000;     // Дата документа +3 дня

            // Получаем Доп.Требования.
            RealmResults<AdditionalRequirementsDB> realmResults = AdditionalRequirementsRealm.getData3(dataDB);
            List<AdditionalRequirementsDB> data = RealmManager.INSTANCE.copyFromRealm(realmResults);

            // Получаем Оценки этих Доп. требований.
            RealmResults<AdditionalRequirementsMarkDB> marks = AdditionalRequirementsMarkRealm.getAdditionalRequirementsMarks(dateFrom, dateTo, userId, data);

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
                        .append(CustomerRealm.getCustomerById(clientId))
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
                        .append(userTxt)
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
                        .append(userTxt)
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

            DialogData dialogData = new DialogData(context);
            dialogData.setTitle("");
            dialogData.setText(msg);
            dialogData.setClose(dialogData::dismiss);


            // Начат вывод сообщений
            switch (mode) {
                case MAKE:

                    break;

                case CHECK:
                    dialogData.show();
                    break;

                case CHECK_CLICK:
                    dialogData.show();
//                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    break;
            }



        } catch (Exception e) {
            Log.e("testprint", "Exception e: " + e);
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
    public <T> void optionControlVersion_139577(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
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
    private <T> void check76815(T dataDB, OptionsDB optionsDB) {
        long dad2;
        if (dataDB instanceof WpDataDB) {
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
        } else {
            return;
        }


        List<TovarDB> dataTovar = RealmManager.getTovarListFromReportPrepareByDad2(dad2);    // Это типа моего СКЮ План
        SKUPlan = dataTovar.size();

        // Перебираем товары по плану
        for (TovarDB item : dataTovar) {
            ReportPrepareDB reportPrepareTovar = RealmManager.getTovarReportPrepare(String.valueOf(dad2), item.getiD()); // Есть ли в РП такой товар?
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
    public <T> boolean checkPhotoReport(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        boolean res = false;

        long dad2 = 0;
        if (dataDB instanceof WpDataDB) {
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
        }

        // Получаем даннные о наличии фотоотчёта из Журнала фотоОтчётов
        List<StackPhotoDB> list = RealmManager.getStackPhotoByDad2(dad2);

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

            res = false;
        } else if (list.size() >= 3) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });

            res = true;
        }

        conductOptCheck(mode, res, optionsDB);

        return res;
    }

    /*Опция контроля 134583
     * Проверка наличия ФотоОтчётов с привязкой к Адресу (134583)
     *
     * Сначала получаю список фоток с данным типом по этому адресу, а потом проверяю - все ли
     * сделанны на месте.
     * */
    public <T> void checkPhotoReportWithMP(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {

        long dad2;
        if (dataDB instanceof WpDataDB) {
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
        } else {
            return;
        }

        String photoMinCount = optionsDB.getAmountMin();

        List<StackPhotoDB> list = RealmManager.getStackPhotoByDad2(dad2);
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

    }


    /**
     * 19.04.2021
     * Опция контроля Количество фотографий
     */
    private <T> void checkPhoto(T dataDB, OptionsDB optionsDB, String photoType) {
        try {
            long dad2;
            if (dataDB instanceof WpDataDB) {
                dad2 = ((WpDataDB) dataDB).getCode_dad2();
            } else if (dataDB instanceof TasksAndReclamationsSDB) {
                dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
            } else {
                return;
            }

            RealmResults<StackPhotoDB> list = RealmManager.stackPhotoByDad2(dad2);
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
    private <T> void checkReceivingAnOrder_141911(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
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
    private <T> void check_RENAME_2(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
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


    private <T> void checkEKL(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {

        long dad2, startWork, endWork;
        if (dataDB instanceof WpDataDB) {
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
        } else {
            return;
        }


        List<EKL_SDB> list = SQL_DB.eklDao().getByDad2(dad2);

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
    public String getOptionString(List<OptionsDB> optionsDB, ReportPrepareDB reportPrepareTovar, boolean promotion) {
        String res = ""; // Итоговая строка всех ТПЛ-ов
        StringBuilder tplRequired = new StringBuilder(); // Обязательные ТПЛ-ы
        StringBuilder tplOptional = new StringBuilder(); // Опциональные ТПЛ-ы

        // Получаем список Опций (Ф,Ц,П...) сам список захардкожен.
        List<TovarOptions> listTovOpt = getTovarOptins();

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
                    if (temp.getOptionControlName().equals(AKCIYA_ID) && promotion) {
                        // ничего не делаю
                    } else {
                        Globals.Triple uploaded = checkUploadedTPL(reportPrepareTovar, getTPLData(temp, reportPrepareTovar));
                        tplRequired.append(setOptionTPLColor(temp.getOptionShort(), true, uploaded));
                        temps.add(temp);
                    }
                }
            }

            if (ids.contains(optionControlId)) {
                TovarOptions temp = listTovOpt.get(listTovOpt.indexOf(new TovarOptions(optionControlId)));
                if (!containsName(temps, temp.getOrderField())) {
                    if (temp.getOptionControlName().equals(AKCIYA_ID) && promotion) {
                        // ничего не делаю
                    } else {
                        Globals.Triple uploaded = checkUploadedTPL(reportPrepareTovar, getTPLData(temp, reportPrepareTovar));
                        tplRequired.append(setOptionTPLColor(temp.getOptionShort(), true, uploaded));
                        temps.add(temp);
                    }
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

        if (data == null || data.equals("")) return Globals.Triple.NO_DATA;

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
                    if (temp.getOptionControlName().equals(AKCIYA_ID) || temp.getOptionControlName().equals(AKCIYA)) {
                        // ничего не делаю
                    } else {
                        temps.add(temp);
                    }
                } else {
                    Log.e("dublicateTPL", "ПОВТОРЯЕТСЯ");
                }
            }

            if (ids.contains(optionControlId)) {
                TovarOptions temp = tplOptionsList.get(tplOptionsList.indexOf(new TovarOptions(optionControlId)));
                // Это нужно что б 2 раза не появлялись Диалоги
                // Проверяем "есть ли уже такая опция" ?
                if (!temps.contains(temp)) {
                    if (temp.getOptionControlName().equals(AKCIYA_ID) || temp.getOptionControlName().equals(AKCIYA)) {
                        // ничего не делаю
                    } else {
                        temps.add(temp);
                    }
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
            list.add(new TovarOptions(ERROR_ID, "Ш", "Ошибка товара", "error_id", "main", 135592, 157242));
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


    /**
     * 27.01.2022
     * Установка опции как блокирующей
     */
    public void conductOptCheck(NNKMode mode, boolean status, OptionsDB optionsDB) {
        switch (mode) {
            case CHECK:
//                if (!status && optionsDB.getBlockPns().equals("1")) {
//                    optionNotConduct.add(optionsDB);
//                }
                break;

            case NULL:
                // Ничего делать не буду
                break;
        }
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
