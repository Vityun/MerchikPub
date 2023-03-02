package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;
import android.text.SpannableStringBuilder;

import java.util.Arrays;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;

public class OptionControlPhotoPromotion<T> extends OptionControl {
    public int OPTION_CONTROL_PROMOTION_ID = 157278;

    private String documentDate, clientId, optionId;
    private int addressId, userId;
    private long dad2;

    public OptionControlPhotoPromotion(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
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
            WpDataDB wpDataDB = (WpDataDB) document;

            documentDate = Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000); //+TODO CHANGE DATE

            clientId = wpDataDB.getClient_id();
            addressId = wpDataDB.getAddr_id();
            userId = wpDataDB.getUser_id();
            dad2 = wpDataDB.getCode_dad2();

        }
    }

    private void executeOption() {
        // values
//        int OSV = 0;            // ОсобоеВнимание
        int signal = 0;         // Сигнал заблокированно или нет
        int err = 0;
        String comment = "";

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


        List<StackPhotoDB> stackPhotoDBS = RealmManager.stackPhotoByDad2AndType(Long.parseLong(optionDB.getCodeDad2()), 28);
        int size = 0;

        // 5.0
        // Тут должена формироваться более подроная информация о том с какими Товарами есть пролема
        int find = 0;
        int totalOSV = 0;
        for (ReportPrepareDB item : reportPrepare) {
            int OSV = 0;

            // ЕСЛИ Аккии нет (2) - пропускаем
            if (item.akciyaId == null || item.akciyaId.equals("")) continue;

            if (Arrays.asList(tovIds).contains(item.getTovarId())) {
                OSV = 1;
                totalOSV++;
                errMsgType1.append("Для товара: ").append(item.tovarId).append(" нужно сделать фото.").append("\n");
            }

            if (stackPhotoDBS != null && stackPhotoDBS.size() > 0) {
                size = stackPhotoDBS.size();
                find++;
            } else {
                err++;
                comment = "Нема світлини Акціонного товару з ОСУ (Особливою Увагою).";
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
            massageToUser = "Товарів, по котрим треба перевіряти наявність Акцції, не знайдено.";
            signal = 1;
        } else if (totalOSV == 0) {
            massageToUser = "Товарів з ОСУ (Особливою увагою), по котрим треба виконати світлини 'Акцційного товару', не знайдено.";
            signal = 2;
        } else if (err > 0) {
            massageToUser = "Не виконані світлини по (" + err + " шт.) з " + totalOSV + " Акційних товарів, які присутні на полицях.";
            signal = 1;
        } else {
            massageToUser = "Зауважень по виготовленню світлин 'Акцційних товарів' нема. Виготовлено " + size + " світлин.";
            signal = 2;
        }

        // 7.0 сохраним сигнал
        if (optionDB.getIsSignal().equals("0")) {
            saveOption(String.valueOf(signal));
        }

        // 8.0 Блокировка проведения
        if (signal == 1) {
            setIsBlockOption(true);
        }
    }

    private void saveOption(String signal) {
        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (optionDB != null) {
                optionDB.setIsSignal(signal);
                realm.insertOrUpdate(optionDB);
            }
        });
    }
}
