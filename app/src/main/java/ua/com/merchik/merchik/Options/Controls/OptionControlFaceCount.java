package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.RealmObject;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.TovarRequisites;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers.FaceSaveGuard;
import ua.com.merchik.merchik.Global.UnlockCode;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.ResultsDynamicSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

/** 576: ПровФейс, алгоритм 1С от 30.10.2026. */
public class OptionControlFaceCount<T> extends OptionControl {
    public static final int OPTION_CONTROL_FACE_COUNT_ID = 576;
    public boolean signal;

    private long dad2;
    private LocalDate date;
    private String clientId;
    private AddressSDB address;
    private String messagePrefix = "";

    public OptionControlFaceCount(Context context, T document, OptionsDB optionDB,
                                  OptionMassageType msgType, Options.NNKMode nnkMode,
                                  UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;
        notCloseSpannableStringBuilderDialog = true;
        getDocumentVar();
        executeOption();
    }

    @Override
    public void showOptionMassage(String msg) {
        messagePrefix = msg;
        super.showOptionMassage(msg);
    }

    //1.0. Определим переменные.
    private void getDocumentVar() {
        if (!(document instanceof WpDataDB) || optionDB == null) {
            throw new IllegalArgumentException("Face count control requires a visit and option");
        }
        wpDataDB = (WpDataDB) document;
        dad2 = wpDataDB.getCode_dad2();
        date = FaceCountPolicy.visitDate(wpDataDB.getDt().getTime());
        clientId = FaceCountPolicy.normalizeId(wpDataDB.getClient_id());
        address = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
    }

    private void executeOption() {
        stringBuilderMsg.setLength(0);
        spannableStringBuilder.clear();
        spannableStringBuilder.clearSpans();
        //2.0. Получим данные о товарах в отчете. Не используем кэш другого визита.
        List<ReportPrepareDB> reportPrepare = ReportPrepareRealm.getReportPrepareByDad2_LIST(dad2);
        List<FaceCountPolicy.Product> products = new ArrayList<>();
        for (ReportPrepareDB row : reportPrepare) {
            products.add(new FaceCountPolicy.Product(row.getTovarId(), row.getFace(), row.getAkciya()));
        }

        //3.0. Сравним фейсы на СКЮ по текущему отчету с другими точками этой же сети.
        //3.1. Подсчитаем среднее кол-во фейсов на одно СКЮ по ДАННОМУ отчету.
        FaceCountPolicy.Visit visit = FaceCountPolicy.visit(products);

        //3.2. РезультатыВДинамике: НачМесяца(Дат-20)..Дат, клиент и сеть адреса.
        String monthFrom = FaceCountPolicy.monthFrom(date);
        String monthTo = FaceCountPolicy.monthTo(date);
        List<ResultsDynamicSDB> networkRows = Collections.emptyList();
        if (address != null && address.tpId != null && address.tpId > 0 && !clientId.isEmpty()) {
            networkRows = SQL_DB.resultsDynamicDao().getForFaceCount(monthFrom, monthTo, clientId, address.tpId);
        } else {
            Globals.writeToMLOG("WARNING", "OptionControlFaceCount/data",
                    "dad2=" + dad2 + ", missing client/network, addr=" + wpDataDB.getAddr_id());
        }
        FaceCountPolicy.Network network = FaceCountPolicy.network(networkRows);

        //3.3. Проверим акцию у товара с МАКСИМАЛЬНЫМ количеством фейсов.
        FaceCountPolicy.Product maximum = FaceCountPolicy.maximum(visit);

        //4.0. Подведем итог, сохраняя последовательность условий 1С.
        FaceCountPolicy.Result result = FaceCountPolicy.evaluate(visit, network, maximum,
                date, "1".equals(optionDB.getBlockPns()));
        FaceCountPolicy.Message formatted = FaceCountPolicy.formatMessage(result, visit, network, this::productText);
        signal = result.signal;
        OptionsDB calculated = RealmObject.isManaged(optionDB)
                ? RealmManager.INSTANCE.copyFromRealm(optionDB) : optionDB;
        calculated.setIsSignal(signal ? "1" : "2");
        calculated.setNotes(formatted.text);
        calculated.setAmount(FaceCountPolicy.text(visit.faces));
        calculated.setPrice(FaceCountPolicy.text(visit.facesPerSku));
        calculated.setBlockPns(result.blockPns ? "1" : "0");

        //05.04.2024. Проверим факт ИСПОЛЬЗОВАНИЯ кода разблокировки (тема 1285).
        String message = formatted.text;
        boolean unlocked = signal && result.blockPns && new UnlockCode().applyStoredUnlockCode(calculated);
        if (unlocked) {
            message += ", Але виконавцю виданий разовий код розблокування.";
            result.blockPns = false;
            calculated.setBlockPns("0");
        }

        //6.0. Если сигнал есть - блокируем проведение (если это определено настройками опции).
        setIsBlockOption(signal && result.blockPns);
        String conductMessage = FaceCountPolicy.conductMessage(signal, result.blockPns, wpDataDB.getStatus() == 1);
        if (!conductMessage.isEmpty()) message += "\n\n" + conductMessage.trim();

        //7.0. Сообщение показывается общим showOptionMassage согласно режиму вызова.
        int offset = 0;
        for (FaceCountPolicy.ProductLink link : formatted.links) {
            spannableStringBuilder.append(message, offset, link.start)
                    .append(createLinkedString(message.substring(link.start, link.end), link.productId));
            offset = link.end;
        }
        spannableStringBuilder.append(message, offset, message.length());

        //8.0. Сохранение результата, как у остальных опций контроля приложения.
        saveOptionResultInDB(calculated);
        Globals.writeToMLOG("INFO", "OptionControlFaceCount/result", "dad2=" + dad2
                + ", mode=" + nnkMode + ", date=" + date + ", client=" + clientId
                + ", network=" + (address == null ? null : address.tpId)
                + ", months=" + monthFrom + ".." + monthTo
                + ", rpRows=" + reportPrepare.size() + ", products=" + visit.products.size()
                + ", faces=" + visit.faces + ", sku=" + visit.sku + ", facesPerSku=" + visit.facesPerSku
                + ", networkRows=" + network.rows + ", networkFaces=" + network.faces
                + ", networkSku=" + network.sku + ", documents=" + network.documents
                + ", average=" + network.facesPerSku + ", promotions=" + visit.promotions
                + ", maxProduct=" + (maximum == null ? "" : maximum.id)
                + ", maxFaces=" + (maximum == null ? "" : maximum.faces)
                + ", maxPromotion=" + (maximum != null && maximum.promotion)
                + ", signal=" + signal + ", unlocked=" + unlocked + ", blocked=" + isBlockOption()
                + ", storedSignal=" + optionDB.getIsSignal() + ", message=" + message);
    }

    private String productText(FaceCountPolicy.Product product) {
        TovarDB tovar = TovarRealm.getById(product.id);
        return FaceCountPolicy.productLabel(product.id,
                tovar == null ? null : tovar.getNm(), tovar == null ? null : tovar.getWeight());
    }

    private SpannableString createLinkedString(String label, String productId) {
        SpannableString link = new SpannableString(label);
        link.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                showFaceEditor(textView.getContext(), productId);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        }, 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return link;
    }

    private void showFaceEditor(Context editorContext, String productId) {
        ReportPrepareDB current = RealmManager.getTovarReportPrepare(String.valueOf(dad2), productId);
        if (current == null) {
            Toast.makeText(editorContext, "Запись товара не найдена. Обновите список товаров.", Toast.LENGTH_LONG).show();
            return;
        }
        TovarDB tovar = TovarRealm.getById(productId);
        TovarOptions tpl = new TovarOptions(Globals.OptionControlName.FACE, "F", "Количество фейсов",
                "face", "main", OPTION_CONTROL_FACE_COUNT_ID);
        DialogData editor = new DialogData(editorContext);
        editor.setTitle("Количество фейсов");
        editor.setText(FaceCountPolicy.productLabel(productId,
                tovar == null ? null : tovar.getNm(), tovar == null ? null : tovar.getWeight()));
        editor.setClose(editor::dismiss);
        editor.tovarOptions = tpl;
        editor.reportPrepareDB = current;
        if (tovar != null) editor.setImage(true, new TovarRequisites().getPhotoFromDB(tovar));
        editor.setOperation(DialogData.Operations.Number, current.getFace(), Collections.emptyMap(), () -> {
            String rawValue = editor.getOperationResult();
            if (rawValue == null || rawValue.trim().isEmpty()) {
                Toast.makeText(editorContext, "Для сохранения - внесите данные", Toast.LENGTH_SHORT).show();
                return;
            }
            String value;
            try {
                value = FaceCountPolicy.faceInput(rawValue);
            } catch (IllegalArgumentException e) {
                Toast.makeText(editorContext, "Укажите корректное количество фейсов: число не меньше 0.", Toast.LENGTH_LONG).show();
                return;
            }
            ReportPrepareDB latest = RealmManager.getTovarReportPrepare(String.valueOf(dad2), productId);
            WpDataDB visit = WpDataRealm.getWpDataRowByDad2Id(dad2);
            if (FaceSaveGuard.canSaveFace(editorContext, visit, latest, value).isError()) return;
            boolean saved = ReportPrepareRealm.updateFields(latest, row -> {
                row.setFace(value);
                row.setUploadStatus(1);
                row.setDtChange(System.currentTimeMillis() / 1000);
            });
            if (!saved) {
                Toast.makeText(editorContext, "Запись товара не найдена. Обновите список товаров.", Toast.LENGTH_LONG).show();
                return;
            }
            wpDataDB = visit;
            executeOption();
            dismissOptionMessage();
            showOptionMassage(isBlockOption() ? messagePrefix : "");
            Toast.makeText(editorContext, "Внесено фейсов: " + value, Toast.LENGTH_SHORT).show();
        });
        editor.show();
    }

    private void saveOptionResultInDB(OptionsDB calculated) {
        RealmManager.INSTANCE.executeTransaction(realm -> {
            optionDB.setIsSignal(calculated.getIsSignal());
            optionDB.setNotes(calculated.getNotes());
            optionDB.setAmount(calculated.getAmount());
            optionDB.setPrice(calculated.getPrice());
            optionDB.setBlockPns(calculated.getBlockPns());
            optionDB.setSumPenalty(signal ? String.valueOf(calculatePenalty(wpDataDB)) : "0.00");
            realm.insertOrUpdate(optionDB);
        });
    }
}
