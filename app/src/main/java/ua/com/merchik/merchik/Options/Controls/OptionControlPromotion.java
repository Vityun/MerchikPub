package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA_ID;
import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Date;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.DoubleSpinner;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.EditTextAndSpinner;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Number;
import static ua.com.merchik.merchik.dialogs.DialogData.Operations.Text;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.PhotoDescriptionText;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.ErrorDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.PromoDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

/**
 * 06.06.2022
 * <p>
 * ID: 80977
 * Опция контроля наличия Акции у Товаров.
 */
public class OptionControlPromotion<T> extends OptionControl {

    public int OPTION_CONTROL_PROMOTION_ID = 80977;

    public boolean signal = true;


    private String documentDate, clientId, optionId;
    private int addressId, userId;
    private long dad2;

    public OptionControlPromotion(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;

        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            WpDataDB wpDataDB = (WpDataDB) document;

            documentDate = Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime()/1000); //+TODO CHANGE DATE

            clientId = wpDataDB.getClient_id();
            addressId = wpDataDB.getAddr_id();
            userId = wpDataDB.getUser_id();
            dad2 = wpDataDB.getCode_dad2();

        }
    }

    private void executeOption() {
        // values
//        int OSV = 0;            // ОсобоеВнимание
        int signalInt = 0;         // Сигнал заблокированно или нет
        int err = 0;

        // Получение RP по данному документу.
        //2.0. получим данные о товарах в отчете
        List<ReportPrepareDB> reportPrepare = ReportPrepareRealm.getReportPrepareByDad2(dad2);
//        List<ReportPrepareDB> reportRes = new ArrayList<>();

        // Получение Доп. Требований с дополнительными фильтрами.
        List<AdditionalRequirementsDB> additionalRequirements = AdditionalRequirementsRealm.getDocumentAdditionalRequirements(document, true, OPTION_CONTROL_PROMOTION_ID, null, null, null);
        String[] tovIds = new String[additionalRequirements.size()];


        for (int i = 0; i < additionalRequirements.size(); i++) {
            tovIds[i] = additionalRequirements.get(i).getTovarId();
        }
        Arrays.sort(tovIds);


        SpannableStringBuilder errMsgType1 = new SpannableStringBuilder();
        SpannableStringBuilder errMsgType2 = new SpannableStringBuilder();
        int errType1Cnt = 0, errType2Cnt = 0;

        errMsgType1.append("Для следующих товара(ов) с ОСВ (Особым Вниманием) Вы должны обязательно указать наличие (или отсутствие) Акции:").append("\n\n");
        errMsgType2.append("Для следующих товара(ов) с ОСВ (Особым Вниманием) Вы должны обязательно указать ТИП Акции: ").append("\n\n");

        // 5.0
        // Тут должена формироваться более подроная информация о том с какими Товарами есть пролема
        int find = 0;
        int totalOSV = 0;
        for (ReportPrepareDB item : reportPrepare) {
            int OSV = 0;
            if (Arrays.asList(tovIds).contains(item.getTovarId())) {
                OSV = 1;
                totalOSV++;
            }

            TovarDB tov = TovarRealm.getById(item.getTovarId());
            if (tov != null){
                String msg = String.format("(%s) %s (%s)", item.getTovarId(), tov.getNm(), tov.getWeight());

                if (OSV == 1 && (item.getAkciyaId().equals("") || item.getAkciyaId().equals("0"))) {
                    // Для товара с ОСВ (Особым Вниманием) Вы должны обязательно указать ТИП Акции.
                    err++;
                    errType2Cnt++;
                    errMsgType2.append(createLinkedString(msg, item, tov)).append("\n");
                } else if (OSV == 1 && (item.getAkciya() != null  && (item.getAkciya().equals("") || item.getAkciya().equals("0")))) {
                    // Для товара с ОСВ (Особым Вниманием) Вы должны обязательно указать наличие (или отсутствие) Акции.
                    err++;
                    errType1Cnt++;
                    errMsgType1.append(createLinkedString(msg, item, tov)).append("\n");
                } else if (!item.getAkciyaId().equals("") && !item.getAkciyaId().equals("0")) {
                    find = 1;
                }
            }else {
                err++;
                errType1Cnt++;
                errMsgType1.append("Товар з ідентифікатором: (").append(item.getTovarId()).append(") не знайдено").append("\n");
            }

        }

        // Формирование сообщения
        if (errType1Cnt > 0) {
            spannableStringBuilder.append(errMsgType1);
        }
        if (errType2Cnt > 0) {
            spannableStringBuilder.append(errMsgType2);
        }
        if (err > 0) {
            notCloseSpannableStringBuilderDialog = true;    // Делает так что при клике на текст диалог не будет закрываться
            spannableStringBuilder.append("\n").append("Зайдите на закладку Товаров и укажите не внесенные данные.");
        }

        // 6.0
        // Тут формируются более короткие соообшения касательно наличия акций у Товаров
        if (reportPrepare.size() == 0) {
            massageToUser = "Товаров, по которым надо проверять факт наличия Акции, не обнаружено.";
            signalInt = 1;
        }else if (totalOSV == 0){
            massageToUser = "Для данной ТТ, на текущий момент, нет товаров с ОСВ (Особым Вниманием). Контролировать нечего. Замечаний нет.";
            signalInt = 2;
        } else if (err > 0) {
            massageToUser = "Не предоставлена информация о типе и наличии Акции по товару (" + err + " шт.) (в т.ч. с ОСВ (Особым Вниманием)). См. таблицу.";
            signalInt = 1;
//        } else if (find == 0) {
//            massageToUser = "Ни у одного товара не указано тип, наличие (или отсутствие) Акции.";
//            signalInt = 1;
        } else {
            massageToUser = "Замечаний по предоставлению информации о наличии Акций по товарам (в т.ч. с ОСВ (Особым Вниманием)) нет.";
            signalInt = 2;
        }

        if (signalInt == 1){
            signal = true;
        }else {
            signal = false;
        }

        // 7.0 сохраним сигнал
        if (optionDB.getIsSignal().equals("0")) {
            saveOption(String.valueOf(signalInt));
        }

        // 8.0 Блокировка проведения
        if (signalInt == 1) {
            setIsBlockOption(true);
        }else {
            setIsBlockOption(false);
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
                spannableStringBuilder.append("\n\n").append("Документ проведен не будет!");
            } else {
                spannableStringBuilder.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете делать Достижения.");
            }
        }


        // Если есть какой-то сигнал - нужно вывести сообщение

    }

    private SpannableString createLinkedString(String msg, ReportPrepareDB reportPrepareDB, TovarDB tov) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Toast.makeText(textView.getContext(), "id: " + reportPrepareDB.getTovarId(), Toast.LENGTH_LONG).show();

                DialogData dialog = new DialogData(textView.getContext());
                dialog.setTitle("");
                dialog.setText("");
                dialog.setClose(dialog::dismiss);

                dialog.setImage(true, getPhotoFromDB(tov));
                dialog.setAdditionalText(setPhotoInfo(TPL, tov, "", ""));

                dialog.setOperationSpinnerData(setMapData(AKCIYA_ID));
                dialog.setOperationSpinner2Data(setMapData(Globals.OptionControlName.AKCIYA));
                dialog.setOperationTextData(reportPrepareDB.getAkciyaId());
                dialog.setOperationTextData2(reportPrepareDB.getAkciya());

                dialog.setOperation(operationType(TPL), getCurrentData(TPL, reportPrepareDB.getCodeDad2(), reportPrepareDB.getTovarId()), setMapData(TPL.getOptionControlName()), () -> {
                    if (dialog.getOperationResult() != null) {
                        operetionSaveRPToDB(TPL, reportPrepareDB, dialog.getOperationResult(), dialog.getOperationResult2(), null, dialog.context);
                        Toast.makeText(dialog.context, "Внесено: " + dialog.getOperationResult(), Toast.LENGTH_LONG).show();
                    }
                });

                dialog.show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        res.setSpan(clickableSpan, 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return res;
    }

    private void saveOption(String signal) {
        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (optionDB != null) {
                optionDB.setIsSignal(signal);
                realm.insertOrUpdate(optionDB);
            }
        });
    }


    // TODO Это нужно перенести куда-то где можно нормально вызывать по всей приле
    TovarOptions TPL = new TovarOptions(AKCIYA_ID, "А", "Вид акции", "akciya_id", "main", 80977);

    // Нужно для заполенния ТПЛ-ов
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
                return map;

            case AKCIYA:
                map.put(2, "Акция отсутствует");
                map.put(1, "Есть акция");

                return map;

            default:
                return null;
        }
    }

    private File getPhotoFromDB(TovarDB tovar) {
        int id = Integer.parseInt(tovar.getiD());
        StackPhotoDB stackPhotoDB = RealmManager.getTovarPhotoByIdAndType(id, tovar.photoId, 18, false);
        if (stackPhotoDB != null) {
            if (stackPhotoDB.getObject_id() == id) {
                if (stackPhotoDB.getPhoto_num() != null && !stackPhotoDB.getPhoto_num().equals("")) {
                    File file = new File(stackPhotoDB.getPhoto_num());
                    return file;
                }
            }
        }
        return null;
    }

    private PhotoDescriptionText setPhotoInfo(TovarOptions tpl, TovarDB tovar, String finalBalanceData1, String finalBalanceDate1) {
        PhotoDescriptionText res = new PhotoDescriptionText();

        try {
            String weightString = String.format("%s, %s", tovar.getWeight(), tovar.getBarcode()); // составление строк веса и штрихкода для того что б выводить в одно поле

            String title = tpl.getOptionLong();

            if (DetailedReportActivity.rpThemeId == 1178) {
                if (tpl.getOptionId().contains(578) || tpl.getOptionId().contains(1465)) {
                    title = "Кол-во выкуп. товара";
                }

                if (tpl.getOptionId().contains(579)) {
                    title = "Цена выкуп. товара";
                }
            }

            if (DetailedReportActivity.rpThemeId == 33) {
                if (tpl.getOptionId().contains(587)) {
                    title = "Кол-во заказанного товара";
                }
            }

            res.row1Text = title;
            res.row1TextValue = "";
            res.row2TextValue = tovar.getNm();
            res.row3TextValue = weightString;

            res.row4TextValue = RealmManager.getNmById(tovar.getManufacturerId()) != null ? RealmManager.getNmById(tovar.getManufacturerId()).getNm() : "";

            res.row5Text = "Ост.:";
            res.row5TextValue = finalBalanceData1 + " шт на " + finalBalanceDate1;
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "RecycleViewDRAdapterTovar.setPhotoInfo", "Exception e: " + e);
        }
        return res;
    }

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

    private void operetionSaveRPToDB(TovarOptions tpl, ReportPrepareDB rp, String data, String data2, TovarDB tovarDB, Context context) {
        if (data == null || data.equals("")) {
            Toast.makeText(context, "Для сохранения - внесите данные", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tpl.getOptionControlName() == AKCIYA_ID) {
            INSTANCE.executeTransaction(realm -> {
                rp.setAkciyaId(data);
                rp.setAkciya(data2);
                rp.setUploadStatus(1);
                rp.setDtChange(System.currentTimeMillis() / 1000);
                RealmManager.setReportPrepareRow(rp);
            });
        }
    }

}
