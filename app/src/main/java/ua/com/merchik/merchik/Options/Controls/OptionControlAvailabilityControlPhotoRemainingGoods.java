package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.TovarRequisites;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.ArticleSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
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
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;


/**
 * 04.05.23.
 * // 30.04.2023 Петров Создал на основании ПровФотоОстТовКли
 * // Контроль наличия Фото Остатков Товаров (по ОТСУТСТВУЮЩИМ товарам, ... если указано ОСВ то проверяем их, а если нет то проверяем ВСЕ ОТСУТСТВУЮЩИЕ).
 * Крое этой есть еще такая-же но с контролем наличия остатков по всем клиентам и отдельно по КОНКРЕТНОМУ клиенту
 * // Вызывается из функции КонтрольОпций.
 * // ДокИст - документ - источник типа Задача, ОтчетИсполнителя, ОтчетОСтажировке и пр. к которому подчинена данная опция
 * // ДокОпц - документ - набор опций (на момент передачи в єту функцию позиционирован на конкретную строку с ДАННОЙ опцией)
 */
public class OptionControlAvailabilityControlPhotoRemainingGoods<T> extends OptionControl {

    public int OPTION_CONTROL_AVAILABILITY_CONTROL_PHOTO_REMAINING_GOODS_ID = 159707;

    public static final LocalDate EXCEPTION_BEFORE_DATE = LocalDate.of(2026, 1, 3);

    public boolean signal = true;

    private int userId;
    private long dad2;

//    private String[] tovIds;    // Список Товаров с ОСВ.

    private WpDataDB wpDataDB;
    private AddressSDB addressSDBDocument;
    private CustomerSDB customerSDBDocument;

    private UsersSDB usersSDB;

    private SpannableStringBuilder tovs = new SpannableStringBuilder();

    public OptionControlAvailabilityControlPhotoRemainingGoods(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        try {
            this.context = context;
            this.document = document;
            this.optionDB = optionDB;
            this.msgType = msgType;
            this.nnkMode = nnkMode;
            this.unlockCodeResultListener = unlockCodeResultListener;
            getDocumentVar();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                executeOption();
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAvailabilityControlPhotoRemainingGoods", "Exception e: " + e);
        }
    }

    private void getDocumentVar() {
        try {
            if (document instanceof WpDataDB) {
                this.wpDataDB = (WpDataDB) document;

                userId = wpDataDB.getUser_id();
                dad2 = wpDataDB.getCode_dad2();
                customerSDBDocument = SQL_DB.customerDao().getById(wpDataDB.getClient_id());
                addressSDBDocument = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
                usersSDB = SQL_DB.usersDao().getById(wpDataDB.getUser_id());
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAvailabilityControlPhotoRemainingGoods/getDocumentVar", "Exception e: " + e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        try {
            LocalDate planDay = wpDataDB.getDt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            //1.0
            String group = addressSDBDocument.tpId.toString();
            int ptId = addressSDBDocument.tpId;
            List<String> tovIds = new ArrayList<>();

            // 1.2
            Integer[] groups = {434};  // исключаем из отчетов: 434-АТБ
            if (planDay.isBefore(EXCEPTION_BEFORE_DATE))
                groups = new Integer[]{434, 319};

            //2.0. получим данные о товарах в отчете (если она еще не рассчитана)
            List<ReportPrepareDB> reportPrepareRaw = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(dad2));
            if (reportPrepareRaw == null) {
                reportPrepareRaw = new ArrayList<>();
            }
            List<ReportPrepareDB> reportPrepare = selectReportPrepareByTovar(reportPrepareRaw);

            //3.0. получим список товаров с особым вниманием (хранится в Доп.Требованиях)
            List<AdditionalRequirementsDB> additionalRequirements = AdditionalRequirementsRealm.getDocumentAdditionalRequirements(document, true, OPTION_CONTROL_AVAILABILITY_CONTROL_PHOTO_REMAINING_GOODS_ID, null, null, null, null, null, null, null);
            boolean hasExplicitRequirements = additionalRequirements != null && !additionalRequirements.isEmpty();

            //3.1. получаем список товаров для которых установлен признак ОСВ
            if (hasExplicitRequirements) {
                //3.4 мои наработки
//                tovIds = new String[additionalRequirements.size()];
                for (int i = 0; i < additionalRequirements.size(); i++) {
                    AdditionalRequirementsDB item = additionalRequirements.get(i);
                    if (item != null) {
                        addUniqueTovarId(tovIds, item.getTovarId());
                    }
                }
//                Arrays.sort(tovIds);
            }
            //3.2. если нет товаров с ОСВ для данной опции, то берем все товары из самого отчета
            else {
                // 20.05.2026 закомментировал этот кусок, так как он лишний и не соответствует 1С
//                List<AdditionalRequirementsDB> data = AdditionalRequirementsRealm.getData3(wpDataDB, DEFAULT, null, null, 0);
//                if (data != null) {
//                    for (AdditionalRequirementsDB item : data) {
//                        if (item.getTovarId() != null && !item.getTovarId().equals("0") && !item.getTovarId().equals("")) {
//                            long startDt = item.dtStart != null ? item.dtStart.getTime() / 1000 : 0;
//                            long endDt = item.dtEnd != null ? item.dtEnd.getTime() / 1000 : 0;
//                            long docDt = wpDataDB.getDt().getTime() / 1000;
//                            long docDtMinus2 = Clock.getDatePeriodLong(docDt, -2);
//                            long docDtPlus1 = Clock.getDatePeriodLong(docDt, 1);
//
//                            if ((startDt > 0 && endDt > 0 && docDtMinus2 < endDt) || (startDt > 0 && endDt == 0)) {
//                                tovIds.add(item.getTovarId());
//                            }
//                        }
//                    }
//                }
                if (tovIds.isEmpty()){

                    for (int i = 0; i < reportPrepare.size(); i++) {
                        addUniqueTovarId(tovIds, reportPrepare.get(i).getTovarId());
                    }

                }

            }

            //3.3. получаем список СЕТЕЙ для которых установлен признак ОСВ.
            // Це треба для того, щоб з"ясувати, треба надавати залишки по товарам конкретно з додатку ДАНОЇ мережі чи ні.
            // Таким чином ми можемо у рамках одного кошторису по одним мережам надавати залишки, а по іншим ні.
            List<AdditionalRequirementsDB> additionalRequirementsGroup = AdditionalRequirementsRealm.getAdditionalRequirements(wpDataDB.getClient_id(), OPTION_CONTROL_AVAILABILITY_CONTROL_PHOTO_REMAINING_GOODS_ID);
            boolean found = true;
            if (additionalRequirementsGroup == null) {
                additionalRequirementsGroup = new ArrayList<>();
            } else {
                for (AdditionalRequirementsDB item : additionalRequirementsGroup) {
                    if (item != null && group.equals(item.getGrpId())) {
                        found = false;
                        break;
                    }
                }
            }


            //4.0. получим данные о размещенных ФОТ по конкретному ДАД2
            String[] tovIdsArray = tovIds.toArray(new String[0]);
            List<StackPhotoDB> stackPhotoList = StackPhotoRealm.getPhoto(null, null, userId, null, null, dad2, 4, tovIdsArray); // Тип фото, Исполнитель, Дад2, Список Товаров . 4-Фото Остатков Товаров,

            //5.2. заполним ее данными ОСВ
            if ((!additionalRequirementsGroup.isEmpty() && found)) {
                Log.e("!!!","-");
            } else {
                tovs.append("Ви повинні завантажити в нашу систему світлину з залишком товару:").append("\n");
                Globals.writeToMLOG("INFO", "OptionControlAvailabilityControlPhotoRemainingGoods/executeOption",
                        "reportPrepare raw size: " + reportPrepareRaw.size()
                                + ", unique size: " + reportPrepare.size()
                                + ", tovIds size: " + tovIds.size()
                                + ", hasExplicitRequirements: " + hasExplicitRequirements);
                for (ReportPrepareDB item : reportPrepare) {
                    if (item == null) continue;

                    item.error = 0;
                    String tovarId = item.getTovarId();
                    if (!isControlledTovar(tovIds, tovarId)) {
                        continue;
                    }

                    int face = parseIntSafe(item.face);
                    if (face == 0 && !hasPhotoForTovar(stackPhotoList, tovarId)) {
                        TovarDB tovar = TovarRealm.getById(tovarId);
                        if (tovar != null) {
                            item.error = 1;
                            item.errorNote = buildTovarErrorNote(tovar);

                            Globals.writeToMLOG("INFO", "OptionControlAvailabilityControlPhotoRemainingGoods/executeOption",
                                    "error tovar: " + tovar.getiD()
                                            + ", rpId: " + item.getID()
                                            + ", face: " + item.face
                                            + ", oborotvedNum: " + item.oborotvedNum
                                            + ", amount: " + item.amount
                                            + ", " + item.errorNote);
                            tovs.append(createLinkedString(item.errorNote, item)).append("\n");
                        }
                    }
                }
            }

            // Итоговое количество нарушений
            Integer errorSum = reportPrepare.stream()
                    .mapToInt(rp -> rp.error != null ? rp.error : 0)
                    .sum();

            //6.0. готовим сообщение и сигнал
            if (reportPrepare.size() == 0) {
                spannableStringBuilder.append("Товарів, не знайдено.");
                signal = true;
            } else if (errorSum != null && errorSum > 0) {
                String s = String.valueOf(errorSum);
                spannableStringBuilder.append("Не надані світлини з ЗАЛИШКАМИ по ").append(s).append(" відсутніх товарів. Таким чином Ви повинні підтвердити, що даних товарів нема на залишках.");

                spannableStringBuilder.append("\n\n").append(tovs);
                signal = true;
            } else {
                spannableStringBuilder.append("Зауваженнь по наданню світлин залишків по відсутнім товарам нема.");
                signal = false;
            }

            // 7.0 исключения
            if (signal) {
                if (wpDataDB.getUser_id() == 232545 || wpDataDB.getUser_id() == 189955) {
                    spannableStringBuilder.append(", але для цього виконавця зроблено виключення.");
                    signal = false;
                } else if (usersSDB.reportDate20 == null/* usersSDB.reportDate20 != null && usersSDB.reportDate20.getTime() <= wpDataDB.getDt().getTime()*/) {
                    spannableStringBuilder.append(", але виконавець не провів ще свого 20-го звіту. Сигнал прибрано.");
                    signal = false;
                } else if (usersSDB.reportDate05 == null/*usersSDB.reportDate05 != null && usersSDB.reportDate05.getTime() <= wpDataDB.getDt().getTime()*/) {
                    spannableStringBuilder.append(", але виконавець не провів ще свого 5-го звіту. Сигнал прибрано.");
                    signal = false;
                } else if (Arrays.asList(groups).contains(ptId)) {
                    stringBuilderMsg.append(", але не перевіряю наявність ФОТ (фото залишків товару) для цієї мережі. ");
                    signal = false;
                }
            }

            // Сохранение
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionDB != null) {
                    if (signal) {
                        double penalty = wpDataDB.getCash_zakaz() * 0.07693;
                        optionDB.setIsSignal("1");
                        optionDB.setSumPenalty(String.valueOf(penalty));
                    } else {
                        optionDB.setIsSignal("2");
                        optionDB.setSumPenalty("0.00");
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
            checkUnlockCode(optionDB);


        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAvailabilityControlPhotoRemainingGoods/executeOption", "Exception e: " + e);
        }
    }

    private List<ReportPrepareDB> selectReportPrepareByTovar(List<ReportPrepareDB> reportPrepare) {
        Map<String, ReportPrepareDB> map = new LinkedHashMap<>();

        if (reportPrepare == null) {
            return new ArrayList<>();
        }

        for (ReportPrepareDB item : reportPrepare) {
            if (item == null || isEmpty(item.getTovarId())) continue;

            String tovarId = item.getTovarId();
            ReportPrepareDB current = map.get(tovarId);
            if (current == null || shouldReplaceReportPrepare(current, item)) {
                map.put(tovarId, item);
            }
        }

        return new ArrayList<>(map.values());
    }

    private boolean shouldReplaceReportPrepare(ReportPrepareDB current, ReportPrepareDB candidate) {
        int currentScore = getReportPrepareScore(current);
        int candidateScore = getReportPrepareScore(candidate);

        if (candidateScore != currentScore) {
            return candidateScore > currentScore;
        }

        long currentDtChange = current != null ? current.getDtChange() : 0;
        long candidateDtChange = candidate != null ? candidate.getDtChange() : 0;
        if (candidateDtChange != currentDtChange) {
            return candidateDtChange > currentDtChange;
        }

        long currentId = current != null && current.getID() != null ? current.getID() : 0;
        long candidateId = candidate != null && candidate.getID() != null ? candidate.getID() : 0;
        return candidateId > currentId;
    }

    private int getReportPrepareScore(ReportPrepareDB item) {
        if (item == null) return 0;

        int score = 0;
        if (parseIntSafe(item.face) > 0) score += 1000;
        if (parseIntSafe(item.oborotvedNum) > 0) score += 100;
        if (item.amount > 0) score += 50;
        if (parseIntSafe(item.up) > 0) score += 10;
        return score;
    }

    private void addUniqueTovarId(List<String> tovIds, String tovarId) {
        if (tovIds == null || isEmpty(tovarId) || tovIds.contains(tovarId)) return;
        tovIds.add(tovarId);
    }

    private boolean isControlledTovar(List<String> tovIds, String tovarId) {
        return !isEmpty(tovarId) && tovIds != null && tovIds.contains(tovarId);
    }

    private boolean hasPhotoForTovar(List<StackPhotoDB> stackPhotoList, String tovarId) {
        if (stackPhotoList == null || isEmpty(tovarId)) return false;

        for (StackPhotoDB stackPhoto : stackPhotoList) {
            if (stackPhoto != null && tovarId.equals(stackPhoto.tovar_id)) {
                return true;
            }
        }

        return false;
    }

    private String buildTovarErrorNote(TovarDB tovar) {
        String code = tovar.getiD();
        try {
            ArticleSDB articleSDB = SQL_DB.articleDao().getByTovId(Integer.parseInt(tovar.getiD()));
            if (articleSDB != null && articleSDB.vendorCode != null && !articleSDB.vendorCode.trim().isEmpty()) {
                code = articleSDB.vendorCode;
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAvailabilityControlPhotoRemainingGoods/buildTovarErrorNote", "Exception e: " + e);
        }

        return "(" + code + ") " + tovar.getNm();
    }

    private int parseIntSafe(String value) {
        if (isEmpty(value)) return 0;
        String normalizedValue = value.trim().replace(',', '.');

        try {
            return Integer.parseInt(normalizedValue);
        } catch (Exception e) {
            try {
                double result = Double.parseDouble(normalizedValue);
                if (result > 0 && result < 1) return 1;
                return (int) result;
            } catch (Exception ignored) {
                return 0;
            }
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private SpannableString createLinkedString(String msg, ReportPrepareDB rp) {
        SpannableString res = new SpannableString(msg);

        try {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    new TovarRequisites(TovarRealm.getById(rp.tovarId), rp).createDialog(context, WpDataRealm.getWpDataRowByDad2Id(Long.parseLong(rp.codeDad2)), optionDB, () -> {
                    }).show();
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
//                    ds.setColor(Color.GREEN);
                }
            };
            int count = msg.length();
            res.setSpan(clickableSpan, 0, count, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAvailabilityControlPhotoRemainingGoods/executeOption/createLinkedString/Exception", "Exception e: " + e);
        }
        return res;
    }
}
