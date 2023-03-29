package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA_ID;
import static ua.com.merchik.merchik.Globals.OptionControlName.ERROR_ID;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.PhotoDescriptionText;
import ua.com.merchik.merchik.data.RealmModels.ErrorDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.PromoDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public class OptionControlCheckingReasonOutOfStock<T> extends OptionControl {
    public int OPTION_CONTROL_CheckingReasonOutOfStock_ID = 157241;

    private WpDataDB wpDataDB;
    private boolean signal = false;

    public OptionControlCheckingReasonOutOfStock(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;

        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            this.wpDataDB = (WpDataDB) document;
        }
    }

    private void executeOption(){
        int find = 0;
        int errCnt = 0;
        List<ReportPrepareDB> result = new ArrayList<>();
        SpannableStringBuilder resultMsg = new SpannableStringBuilder();

        resultMsg.append("Для товара с ОСВ (Особым Вниманием) Вы должны обязательно указать ПРИЧИНУ его отсутствия.").append("\n\n");

        // Получение Репорт Препэйра
        List<ReportPrepareDB> detailedReportRPList = ReportPrepareRealm.getReportPrepareByDad2(wpDataDB.getCode_dad2());

        // проверим, по каким из товаров с ОСВ отсутствуют на витрине?
        for (ReportPrepareDB item : detailedReportRPList) {
            if (item.getFace() != null && !item.getFace().equals("") && !item.getFace().equals("0")) {
                continue;
            }


            if (item.getErrorId() != null && !item.getErrorId().equals("") && !item.getErrorId().equals("0")) {
                find++;
            }else {
                errCnt++;
                result.add(item);
            }
        }


        // Подготовка Сообщения
        if (detailedReportRPList.size() == 0) {
            signal = true;
            resultMsg.append("Товаров, по которым надо указать причины отсутствия, не обнаружено.").append("\n\n");
        } else if (errCnt > 0) {
            signal = true;
            resultMsg.append("Не предоставлена информация о ПИЧИНАХ отсутствия товара (в т.ч. с ОСВ (Особым Вниманием)). См. ниже.").append("\n\n");
            for (ReportPrepareDB item : result){
                TovarDB tov = TovarRealm.getById(item.getTovarId());
                String msg = String.format("(%s) %s (%s)\n", item.getTovarId(), tov.getNm(), tov.getWeight());

                resultMsg.append(createLinkedString(msg, item, tov));
            }
//        }else if (find == 0){
//            signal = true;
//            resultMsg.append("Ни у одного ОТСУТСТВУЮЩЕГО товара не указана ПРИЧИНА отсутствия.").append("\n\n");
        }else {
            signal = false;
            resultMsg.append("Замечаний по предоставлению информации о ПРИЧИНАХ отсутствия товаров (в т.ч. с ОСВ (Особым Вниманием)) нет.").append("\n\n");
        }


        // Установка Сигнала
        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (optionDB != null) {
                if (signal){
                    optionDB.setIsSignal("1");
                }else {
                    optionDB.setIsSignal("2");
                }
                realm.insertOrUpdate(optionDB);
            }
        });

        // 8.0 Блокировка проведения
        if (signal) {
            if (optionDB.getBlockPns().equals("1") && wpDataDB.getStatus() == 0){
                resultMsg.append("Документ проведен не будет!").append("\n\n");
            }else {
                resultMsg.append("Вы можете получить Премиальные БОЛЬШЕ, если будете указывать информацию о причинах отсутствия товаров.").append("\n\n");
            }
            setIsBlockOption(true);
        }

        notCloseSpannableStringBuilderDialog = true;    // Делает так что при клике на текст диалог не будет закрываться
        spannableStringBuilder = resultMsg;
    }

    private SpannableString createLinkedString(String msg, ReportPrepareDB reportPrepareDB, TovarDB tov) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
//                Toast.makeText(textView.getContext(), "Функция в разработке. Идентификатор Товара: " + reportPrepareDB.getTovarId(), Toast.LENGTH_LONG).show();

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


    // TODO Это нужно перенести куда-то где можно нормально вызывать по всей приле
    TovarOptions TPL = new TovarOptions(ERROR_ID, "Ш", "Ошибка товара", "error_id", "main", 135592, 157242);

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
