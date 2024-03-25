package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.MakePhoto.MakePhoto.CAMERA_REQUEST_PROMOTION_TOV_PHOTO;
import static ua.com.merchik.merchik.data.RealmModels.StackPhotoDB.PHOTO_PROMOTION_TOV;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.List;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;

public class OptionControlPhotoPromotion<T> extends OptionControl {
    public int OPTION_CONTROL_PROMOTION_ID = 157278;

    public static WpDataDB wpDataDBOPTION_CONTROL_PROMOTION_ID;
    public static TovarDB tovarDBOPTION_CONTROL_PROMOTION_ID;

    private WpDataDB wp;
    private String documentDate, clientId, optionId;
    private int addressId, userId;
    private long dad2;

    public boolean signal = false;

    public OptionControlPhotoPromotion(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        try {
            this.context = context;
            this.document = document;
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;
            this.unlockCodeResultListener = unlockCodeResultListener;

            getDocumentVar();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    executeOption();
                } catch (Exception e) {
                    Globals.writeToMLOG("INFO", "OptionControlPhotoPromotion/executeOption", "Exception e: " + e);
                }
            }
        }catch (Exception e){
            Globals.writeToMLOG("INFO", "OptionControlPhotoPromotion/", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            WpDataDB wpDataDB = (WpDataDB) document;

            wp = wpDataDB;

            documentDate = Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000); //+TODO CHANGE DATE

            clientId = wpDataDB.getClient_id();
            addressId = wpDataDB.getAddr_id();
            userId = wpDataDB.getUser_id();
            dad2 = wpDataDB.getCode_dad2();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        // values
//        int OSV = 0;            // ОсобоеВнимание
        int signalInt = 0;         // Сигнал заблокированно или нет
        int err = 0;
        String comment = "";

        // Получение RP по данному документу.
        //2.0. получим данные о товарах в отчете
        List<ReportPrepareDB> reportPrepare = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(dad2));
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

        errMsgType1.append("Для следующих товара(ов) с ОСВ (Особым Вниманием) Вы должны обязательно сделать Фото:").append("\n\n");
//        errMsgType2.append("Для следующих товара(ов) с ОСВ (Особым Вниманием) Вы должны обязательно указать ТИП Акции: ").append("\n\n");


        errMsgType1.append("Для товара: ").append("\n");


        List<StackPhotoDB> stackPhotoDBS = RealmManager.stackPhotoByDad2AndType(Long.parseLong(optionDB.getCodeDad2()), PHOTO_PROMOTION_TOV);
        int size = 0;

        // 5.0
        // Тут должена формироваться более подроная информация о том с какими Товарами есть пролема
        int find = 0;
        int totalOSV = 0;
        boolean showTovList = false;
        for (ReportPrepareDB item : reportPrepare) {
            int OSV = 0;

            String akciya = item.akciyaId;
            // ЕСЛИ Аккии нет (2) - пропускаем
            if (akciya == null || akciya.equals("")) continue;

            if (Arrays.asList(tovIds).contains(item.getTovarId())) {
                if (stackPhotoDBS != null && stackPhotoDBS.size() > 0) {
                    StackPhotoDB currentTovPhoto = stackPhotoDBS.stream().filter(listItem -> listItem.tovar_id.equals(item.getTovarId())).findFirst().orElse(null);
                    if (currentTovPhoto != null) {
                        showTovList = true;
                        OSV = 1;
                        totalOSV++;
                        errMsgType1.append(createLinkedString(item, currentTovPhoto)).append("\n");
                    } else {
                        showTovList = true;
                        err++;
                        errType1Cnt++;
                        OSV = 1;
                        totalOSV++;
                        errMsgType1.append(createLinkedString(item, null)).append("\n");
                    }
                } else {
                    showTovList = true;
                    err++;
                    errType1Cnt++;
                    OSV = 1;
                    totalOSV++;
                    errMsgType1.append(createLinkedString(item, null)).append("\n");
                }
            }

            showTovList = true;

            if (stackPhotoDBS != null && stackPhotoDBS.size() > 0) {
                size = stackPhotoDBS.size();
                find++;
            } else {
                err++;
                comment = "Нема світлини Акціонного товару з ОСУ (Особливою Увагою).";
//                errMsgType1.append("Товар з ідентифікатором: (").append(item.getTovarId()).append(") не знайдено").append("\n");
            }
        }

        Log.e("test", "test" + errType1Cnt);

        // Формирование сообщения
        if (errType1Cnt > 0 || showTovList) {
            errMsgType1.append("нужно сделать фото.").append("\n");
            spannableStringBuilder.append(errMsgType1);
        }
        if (errType2Cnt > 0) {
            spannableStringBuilder.append(errMsgType2);
        }
        if (err > 0) {
            notCloseSpannableStringBuilderDialog = true;    // Делает так что при клике на текст диалог не будет закрываться
//            spannableStringBuilder.append("\n").append("Зайдите на закладку Товаров и укажите не внесенные данные.");
        }


        // 6.0
        // Тут формируются более короткие соообшения касательно наличия акций у Товаров
        if (reportPrepare.size() == 0) {
            massageToUser = "Товарів, по котрим треба перевіряти наявність Акцції, не знайдено.";
            signalInt = 1;
            signal = true;
//            unlockCodeResultListener.onUnlockCodeFailure();
        } else if (totalOSV == 0) {
            massageToUser = "Товарів з ОСУ (Особливою увагою), по котрим треба виконати світлини 'Акційного товару', не знайдено.";
            signalInt = 2;
            signal = false;
//            unlockCodeResultListener.onUnlockCodeSuccess();
        } else if (err > 0) {
            massageToUser = "Не виконані світлини по (" + errType1Cnt + " шт.) з " + totalOSV + " Акційних товарів, які присутні на полицях.";
            signalInt = 1;
            signal = true;
//            unlockCodeResultListener.onUnlockCodeFailure();
        } else {
            massageToUser = "Зауважень по виготовленню світлин 'Акцційних товарів' нема. Виготовлено " + size + " світлин.";
            signalInt = 2;
            signal = false;
//            unlockCodeResultListener.onUnlockCodeSuccess();
        }

        spannableStringBuilder.append("\n\n").append(massageToUser);

        // 7.0 сохраним сигнал
//        if (optionDB.getIsSignal().equals("0")) {
        saveOption(String.valueOf(signalInt));
//        }

        // 8.0 Блокировка проведения
        if (signalInt == 1) {
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


    /**
     * 05.03.23.
     * Делает кликабельным Товар, что б при клике можно было б сделать фото.
     *
     * @param stackPhotoDB: Фото из БД. Нужно для того что б правильно отрисовывать цвета товарам.
     */
    private SpannableString createLinkedString(ReportPrepareDB item, StackPhotoDB stackPhotoDB) {
        TovarDB tov = TovarRealm.getById(item.getTovarId());
        String tovName = tov.getNm().replace("&quot;", "\"");
        String msg = String.format("(%s) %s (%s)", tov.getBarcode(), tovName, tov.getWeight());
        SpannableString res = new SpannableString(msg);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Toast.makeText(textView.getContext(), "Виготовлення світлини по товару(" + tov.getBarcode() + "): " + tovName, Toast.LENGTH_LONG).show();

                wpDataDBOPTION_CONTROL_PROMOTION_ID = wp;
                tovarDBOPTION_CONTROL_PROMOTION_ID = tov;

                Globals.writeToMLOG("INFO", "OptionControlPhotoPromotion/createLinkedString", "wp_dad2: " + wp.getCode_dad2());
                Globals.writeToMLOG("INFO", "OptionControlPhotoPromotion/createLinkedString", "tov.id: " + tov.getiD());

                new MakePhoto().openCamera((Activity) context, CAMERA_REQUEST_PROMOTION_TOV_PHOTO);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                if (stackPhotoDB != null) {
                    if (stackPhotoDB.get_on_server != 0) {
                        ds.setColor(Color.GREEN);
                    } else if (stackPhotoDB.create_time != 0 && stackPhotoDB.upload_to_server != 0) {
                        ds.setColor(Color.YELLOW);
                    } else {
                        ds.setColor(Color.RED);
                    }
                } else {
                    ds.setColor(Color.GRAY);
                }
                ds.setUnderlineText(true);
            }
        };


        res.setSpan(clickableSpan, 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return res;
    }


}
