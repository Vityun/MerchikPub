package ua.com.merchik.merchik.Options.Controls;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

public class OptionControlPhotoTovarAndPrice<T> extends OptionControl {
    public static final int OPTION_BUTTON_PHOTO_TOVAR_AND_PRICE_ID = 175015;
    public static final int OPTION_CONTROL_PHOTO_TOVAR_AND_PRICE_ID = 175016;
    private static final int PHOTO_TOVAR_AND_PRICE = StackPhotoDB.PHOTO_TOVAR_AND_PRICE;
    private static final LocalDate EXCEPTION_UNTIL_DATE = LocalDate.of(2026, 9, 10);

    private WpDataDB wp;
    private String optionId;
    private long dad2;

    public boolean signal = false;

    public OptionControlPhotoTovarAndPrice(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
                    Globals.writeToMLOG("ERROR", "OptionControlPhotoTovarAndPrice/executeOption", "Exception e: " + e);
                }
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPhotoTovarAndPrice", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            WpDataDB wpDataDB = (WpDataDB) document;
            this.wpDataDB = wpDataDB;
            this.wp = wpDataDB;

            dad2 = wpDataDB.getCode_dad2();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        if (wp == null || wp.getDt() == null) {
            spannableStringBuilder.append("Не вдалося визначити відвідування для перевірки Фото товару з цінником.");
            signal = false;
            return;
        }

        //1.0. определим переменные
        int signalInt = 0;
        int err = 0;
        int requiredCount = 0;
        int foundPhotoCount = 0;
        int totalOsvInReport = 0;
        int totalPresentOnShowcase = 0;
        int colMin = parseIntSafe(optionDB != null ? optionDB.getAmountMin() : null);
        int photoType = PHOTO_TOVAR_AND_PRICE;
        LocalDate planDay = wp.getDt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        optionId = String.valueOf(resolveOptionForAdditionalRequirements());

        //2.0. получим данные о товарах в отчете (если она еще не рассчитана)
        List<ReportPrepareDB> reportPrepareRaw = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(dad2));
        if (reportPrepareRaw == null) {
            reportPrepareRaw = new ArrayList<>();
        }

        //3.0. получим список товаров с особым вниманием (хранится в Доп.Требованиях).
        List<String> spisTovOSV = getAdditionalRequirementTovarIds(wp, optionDB);

        //4.0. проверим товары отчета с учетом ОСВ/КолМин и наличия на витрине.
        //4.1. подготовим итоговую таблицу
        List<ReportPrepareDB> reportPrepare = selectReportPrepareByTovar(reportPrepareRaw);

        //5.0. проверим наличие фото. Ступенями для того, чтобы ускорить проведение документа
        List<StackPhotoDB> stackPhotoDBS = RealmManager.stackPhotoByDad2AndType(dad2, photoType);
        if (stackPhotoDBS == null) {
            stackPhotoDBS = new ArrayList<>();
        }

        SpannableStringBuilder errMsgType1 = new SpannableStringBuilder();
        int errType1Cnt = 0;
        errMsgType1.append("Для наступних товарів потрібно зробити Фото товару з цінником:").append("\n\n");

        //6.0. заполним ее данными ОСВ
        for (ReportPrepareDB item : reportPrepare) {
            //6.1. позначаємо ОСВ
            boolean isOsv = spisTovOSV.contains(item.getTovarId());
            if (isOsv) {
                totalOsvInReport++;
            }

            //6.2.
            int face = parseIntSafe(item.face);
            if (face <= 0) { //товара на вітрині нема ... перевіряти нема чого
                continue;
            }
            totalPresentOnShowcase++;

            if (!isPhotoTovarAndPriceRequired(item, spisTovOSV, colMin, requiredCount)) {
                continue;
            }

            requiredCount++;

            //6.3. проверим, есть ли фото ДАННОГО товара?
            StackPhotoDB currentTovPhoto = findPhotoByTovar(stackPhotoDBS, item.getTovarId());
            if (currentTovPhoto != null) {
                foundPhotoCount++;
            } else {
                err++;
                errType1Cnt++;
                item.error = 1;
                item.errorNote = "Нема світлини товару з цінником (що присутен на вітрині).";
                errMsgType1.append(createLinkedString(item, null)).append("\n");
            }
        }

        //7.0. готовим сообщение и сигнал
        if (errType1Cnt > 0) {
            notCloseSpannableStringBuilderDialog = true;
            errMsgType1.append("потрібно зробити фото.").append("\n\n");
            spannableStringBuilder.append(errMsgType1);
        }

        if (reportPrepare.isEmpty()) {
            spannableStringBuilder.append("Товарів, по котрим треба надати фото (з цінниками), не знайдено.");
            signalInt = 1;
            signal = true;
        } else if (!spisTovOSV.isEmpty() && totalOsvInReport == 0) {
            spannableStringBuilder.append("Товарів, по котрим треба виконати світлини (з цінниками), не знайдено. Зауважень нема.");
            signalInt = 2;
            signal = false;
        } else if (requiredCount == 0) {
            spannableStringBuilder.append("Товарів, по котрим треба виконати світлини (з цінниками), не знайдено. Зауважень нема.");
            signalInt = 2;
            signal = false;
        } else if (err > 0) {
            spannableStringBuilder.append("Не виконані світлини товарів (з цінниками) по ").append(String.valueOf(err)).append(" СКЮ. Див. таблицю.");
            signalInt = 1;
            signal = true;
        } else {
            spannableStringBuilder.append("Зауважень по виготовленню світлин товарів (з цінниками) нема. Виготовлено ").append(String.valueOf(foundPhotoCount)).append(" світлин.");
            signalInt = 2;
            signal = false;
        }

        if (massageToUser != null && massageToUser.length() > 0) {
            spannableStringBuilder.append("\n\n").append(massageToUser);
        }

        // 02.10.2024 Петров Добавил автоматическую проверку на выданный КодРазблокировки.
        // В приложении проверка выполняется общим методом checkUnlockCode(optionDB) после сохранения сигнала.

//        if (signal && !planDay.isAfter(EXCEPTION_UNTIL_DATE)) {
//            spannableStringBuilder.append("\nАле, до '10.09.2026' зроблено виключення. Зауважень немає.");
//            signalInt = 2;
//            signal = false;
//        }

        //8.0. сохраним сигнал
        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (optionDB != null) {
                if (signal) {
                    double penalty = calculatePenalty(wpDataDB);
                    optionDB.setIsSignal("1");
                    optionDB.setSumPenalty(String.valueOf(penalty));
                } else {
                    optionDB.setIsSignal("2");
                    optionDB.setSumPenalty("0.00");
                }
                realm.insertOrUpdate(optionDB);
            }
        });

        //9.0. блокировка проведения
        if (signalInt == 1) {
            setIsBlockOption(true);
            if (optionDB != null && !"1".equals(optionDB.getBlockPns())) {
                spannableStringBuilder.append(" Ви можете отримати Преміальні БІЛЬШЕ, якщо будете виготовляти світлини товарів з цінниками.");
            }
        }

        //10.0. Если есть сигнал - показываем сообщение. Разблокировку проверяет общий механизм.
        checkUnlockCode(optionDB);

        Globals.writeToMLOG(
                "INFO",
                "OptionControlPhotoTovarAndPrice/result",
                "dad2=" + dad2 +
                        ", optionId=" + optionId +
                        ", photoType=" + photoType +
                        ", reportRaw=" + reportPrepareRaw.size() +
                        ", reportUnique=" + reportPrepare.size() +
                        ", osv=" + spisTovOSV.size() +
                        ", present=" + totalPresentOnShowcase +
                        ", required=" + requiredCount +
                        ", foundPhoto=" + foundPhotoCount +
                        ", errors=" + err +
                        ", signal=" + signal
        );
    }

    private int resolveOptionForAdditionalRequirements() {
        return resolveOptionForAdditionalRequirements(optionDB);
    }

    public static int getRequiredTovarAndPricePhotoCount(WpDataDB wpDataDB, OptionsDB optionDB) {
        try {
            if (wpDataDB == null || wpDataDB.getDt() == null) {
                return 0;
            }

            List<String> spisTovOSV = getAdditionalRequirementTovarIds(wpDataDB, optionDB);
            List<ReportPrepareDB> reportPrepareRaw = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(wpDataDB.getCode_dad2()));
            if (reportPrepareRaw == null) {
                reportPrepareRaw = new ArrayList<>();
            }

            int requiredCount = 0;
            int colMin = parseIntSafe(optionDB != null ? optionDB.getAmountMin() : null);
            for (ReportPrepareDB item : selectReportPrepareByTovar(reportPrepareRaw)) {
                if (isPhotoTovarAndPriceRequired(item, spisTovOSV, colMin, requiredCount)) {
                    requiredCount++;
                }
            }

            return requiredCount;
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPhotoTovarAndPrice/getRequiredTovarAndPricePhotoCount", "Exception e: " + e);
            return 0;
        }
    }

    private static int resolveOptionForAdditionalRequirements(OptionsDB optionDB) {
        int optionControl = parseIntSafe(optionDB != null ? optionDB.getOptionControlId() : null);
        if (optionControl == OPTION_CONTROL_PHOTO_TOVAR_AND_PRICE_ID) {
            return optionControl;
        }

        int option = parseIntSafe(optionDB != null ? optionDB.getOptionId() : null);
        if (option == OPTION_BUTTON_PHOTO_TOVAR_AND_PRICE_ID) {
            return OPTION_CONTROL_PHOTO_TOVAR_AND_PRICE_ID;
        }

        if (option > 0) {
            return option;
        }

        if (optionControl > 0) {
            return optionControl;
        }

        return OPTION_CONTROL_PHOTO_TOVAR_AND_PRICE_ID;
    }

    private static List<String> getAdditionalRequirementTovarIds(WpDataDB wpDataDB, OptionsDB optionDB) {
        List<String> result = new ArrayList<>();
        if (wpDataDB == null || wpDataDB.getDt() == null) {
            return result;
        }

        try {
            int optionId = resolveOptionForAdditionalRequirements(optionDB);
            List<AdditionalRequirementsDB> additionalRequirements = AdditionalRequirementsRealm.getDocumentAdditionalRequirements(
                    wpDataDB,
                    true,
                    optionId,
                    null,
                    wpDataDB.getDt(),
                    wpDataDB.getDt(),
                    null,
                    null,
                    null,
                    null
            );

            if (additionalRequirements != null) {
                for (AdditionalRequirementsDB item : additionalRequirements) {
                    if (item != null) {
                        addUniqueTovarId(result, item.getTovarId());
                    }
                }
            }
            result.sort(null);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPhotoTovarAndPrice/additionalRequirements", "Exception e: " + e);
        }

        return result;
    }

    private static List<ReportPrepareDB> selectReportPrepareByTovar(List<ReportPrepareDB> reportPrepareRaw) {
        Map<String, ReportPrepareDB> reportPrepareByTovar = new LinkedHashMap<>();
        if (reportPrepareRaw == null) {
            return new ArrayList<>();
        }

        for (ReportPrepareDB item : reportPrepareRaw) {
            if (item == null || isEmpty(item.getTovarId())) {
                continue;
            }

            ReportPrepareDB current = reportPrepareByTovar.get(item.getTovarId());
            if (current == null || shouldUseReportPrepare(item, current)) {
                reportPrepareByTovar.put(item.getTovarId(), item);
            }
        }

        return new ArrayList<>(reportPrepareByTovar.values());
    }

    private static boolean isPhotoTovarAndPriceRequired(ReportPrepareDB item, List<String> spisTovOSV, int colMin, int currentRequiredCount) {
        if (item == null) {
            return false;
        }

        int face = parseIntSafe(item.face);
        if (face <= 0) {
            return false;
        }

        boolean hasOsvList = spisTovOSV != null && !spisTovOSV.isEmpty();
        if (hasOsvList && !spisTovOSV.contains(item.getTovarId())) {
            return false;
        }

        return hasOsvList || colMin <= 0 || currentRequiredCount < colMin;
    }

    private static boolean shouldUseReportPrepare(ReportPrepareDB candidate, ReportPrepareDB current) {
        int candidateFace = parseIntSafe(candidate.face);
        int currentFace = parseIntSafe(current.face);
        if (candidateFace != currentFace) {
            return candidateFace > currentFace;
        }

        long candidateDtChange = candidate.getDtChange();
        long currentDtChange = current.getDtChange();
        if (candidateDtChange != currentDtChange) {
            return candidateDtChange > currentDtChange;
        }

        long candidateId = candidate.getID() != null ? candidate.getID() : 0L;
        long currentId = current.getID() != null ? current.getID() : 0L;
        return candidateId > currentId;
    }

    private static void addUniqueTovarId(List<String> ids, String tovarId) {
        if (isEmpty(tovarId) || "0".equals(tovarId)) {
            return;
        }

        if (!ids.contains(tovarId)) {
            ids.add(tovarId);
        }
    }

    private static StackPhotoDB findPhotoByTovar(List<StackPhotoDB> photos, String tovarId) {
        if (photos == null || isEmpty(tovarId)) {
            return null;
        }

        for (StackPhotoDB photo : photos) {
            if (photo != null && tovarId.equals(photo.tovar_id)) {
                return photo;
            }
        }

        return null;
    }

    private static int parseIntSafe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }

        String normalizedValue = value.trim().replace(',', '.');
        try {
            return Integer.parseInt(normalizedValue);
        } catch (Exception e) {
            try {
                double result = Double.parseDouble(normalizedValue);
                if (result > 0 && result < 1) {
                    return 1;
                }
                return (int) result;
            } catch (Exception ignored) {
                return 0;
            }
        }
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Делает кликабельным Товар, чтобы при клике можно было сделать фото.
     *
     * @param stackPhotoDB: Фото из БД. Нужно для того, чтобы правильно отрисовывать цвета товарам.
     */
    private SpannableString createLinkedString(ReportPrepareDB item, StackPhotoDB stackPhotoDB) {
        TovarDB tov = null;
        try {
            tov = TovarRealm.getById(item.getTovarId());
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlPhotoTovarAndPrice/createLinkedString", "tovarId=" + item.getTovarId() + ", Exception e: " + e);
        }

        String barcode = tov != null && tov.getBarcode() != null ? tov.getBarcode() : item.getTovarId();
        String tovName = tov != null && tov.getNm() != null ? tov.getNm().replace("&quot;", "\"") : "Товар " + item.getTovarId();
        String weight = tov != null && tov.getWeight() != null ? tov.getWeight() : "";
        String msg = String.format("(%s) %s (%s)", barcode, tovName, weight);
        SpannableString res = new SpannableString(msg);
        TovarDB finalTov = tov;

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Toast.makeText(textView.getContext(), "Виготовлення світлини товару з цінником (" + barcode + "): " + tovName, Toast.LENGTH_LONG).show();

                try {
                    Activity activity = findActivity(textView.getContext());
                    if (activity == null) {
                        activity = findActivity(context);
                    }
                    if (activity == null || finalTov == null) {
                        Toast.makeText(textView.getContext(), "Не вдалося відкрити камеру для цього товару.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    new MakePhoto().pressedMakePhoto(
                            activity,
                            wp,
                            optionDB,
                            String.valueOf(PHOTO_TOVAR_AND_PRICE),
                            finalTov.getiD(),
                            () -> {
                            }
                    );

                    Globals.writeToMLOG("INFO", "OptionControlPhotoTovarAndPrice/createLinkedString", "wp_dad2: " + wp.getCode_dad2() + ", tovarId: " + finalTov.getiD());
                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "OptionControlPhotoTovarAndPrice/createLinkedString", "Exception e: " + e);
                }
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
                    ds.setColor(Color.BLUE);
                }
                ds.setUnderlineText(true);
            }
        };

        res.setSpan(clickableSpan, 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return res;
    }

    private Activity findActivity(Context sourceContext) {
        Context current = sourceContext;
        while (current instanceof ContextWrapper) {
            if (current instanceof Activity) {
                return (Activity) current;
            }
            current = ((ContextWrapper) current).getBaseContext();
        }
        return current instanceof Activity ? (Activity) current : null;
    }
}
