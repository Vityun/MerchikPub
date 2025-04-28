package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.DEFAULT;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.TovarRequisites;
import ua.com.merchik.merchik.Clock;
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

    public boolean signal = true;

    private int userId;
    private long dad2;

    // 1.2
    private Integer[] groups = {434};  // исключаем из отчетов: 434-АТБ
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

            //1.0
            String group = addressSDBDocument.tpId.toString();
            List<String> tovIds = new ArrayList<>();


            //2.0. получим данные о товарах в отчете (если она еще не рассчитана)
            List<ReportPrepareDB> reportPrepare = RealmManager.INSTANCE.copyFromRealm(ReportPrepareRealm.getReportPrepareByDad2(dad2));

            //3.0. получим список товаров с особым вниманием (хранится в Доп.Требованиях)
            List<AdditionalRequirementsDB> additionalRequirements = AdditionalRequirementsRealm.getDocumentAdditionalRequirements(document, true, OPTION_CONTROL_AVAILABILITY_CONTROL_PHOTO_REMAINING_GOODS_ID, null, null, null, null, null, null, null);

            //3.1. получаем список товаров для которых установлен признак ОСВ
            if (additionalRequirements != null && !additionalRequirements.isEmpty()) {
                //3.4 мои наработки
//                tovIds = new String[additionalRequirements.size()];
                for (int i = 0; i < additionalRequirements.size(); i++) {
                    tovIds.add(additionalRequirements.get(i).getTovarId());
                }
//                Arrays.sort(tovIds);
            }
            //3.2. если нет товаров с ОСВ для данной опции, то берем все товары из самого отчета
            else {
                List<AdditionalRequirementsDB> data = AdditionalRequirementsRealm.getData3(wpDataDB, DEFAULT, null, null, 0);
                if (data != null) {
                    for (AdditionalRequirementsDB item : data) {
                        if (item.getTovarId() != null && !item.getTovarId().equals("0") && !item.getTovarId().equals("")) {
                            long startDt = item.dtStart != null ? item.dtStart.getTime() / 1000 : 0;
                            long endDt = item.dtEnd != null ? item.dtEnd.getTime() / 1000 : 0;
                            long docDt = wpDataDB.getDt().getTime() / 1000;
                            long docDtMinus2 = Clock.getDatePeriodLong(docDt, -2);
                            long docDtPlus1 = Clock.getDatePeriodLong(docDt, 1);

                            if ((startDt > 0 && endDt > 0 && docDtMinus2 < endDt) || (startDt > 0 && endDt == 0)) {
                                tovIds.add(item.getTovarId());
                            }
                        }
                    }
                }
//                tovIds = new String[reportPrepare.size()];
//                for (int i = 0; i < reportPrepare.size(); i++) {
//                    tovIds.add(reportPrepare.get(i).getTovarId());
//                }
//                Arrays.sort(tovIds);
            }

            //3.3. получаем список СЕТЕЙ для которых установлен признак ОСВ.
            // Це треба для того, щоб з"ясувати, треба надавати залишки по товарам конкретно з додатку ДАНОЇ мережі чи ні.
            // Таким чином ми можемо у рамках одного кошторису по одним мережам надавати залишки, а по іншим ні.
            List<AdditionalRequirementsDB> additionalRequirementsGroup = AdditionalRequirementsRealm.getAdditionalRequirements(wpDataDB.getClient_id(), OPTION_CONTROL_AVAILABILITY_CONTROL_PHOTO_REMAINING_GOODS_ID);
            boolean found = false;
            for (AdditionalRequirementsDB item : additionalRequirementsGroup) {
                if (group.equals(item.getGrpId())) {
                    found = true;
                    break;
                }
            }


            //4.0. получим данные о размещенных ФОТ по конкретному ДАД2
            String[] tovIdsArray = tovIds.toArray(new String[0]);
            List<StackPhotoDB> stackPhotoList = StackPhotoRealm.getPhoto(null, null, userId, null, null, dad2, 4, tovIdsArray); // Тип фото, Исполнитель, Дад2, Список Товаров . 4-Фото Остатков Товаров,

            //5.2. заполним ее данными ОСВ
            if (!additionalRequirementsGroup.isEmpty() && found) {
                tovs.append("Ви повинні завантажити в нашу систему світлину з залишком товару:").append("\n");
                for (ReportPrepareDB item : reportPrepare) {
                    if (tovIds.isEmpty()) {
                        int face = Integer.parseInt(item.face);
                        if (face == 0 && !stackPhotoList.stream().anyMatch(stackPhoto -> stackPhoto.tovar_id.equals(item.getTovarId()))) {
                            TovarDB tovar = TovarRealm.getById(item.getTovarId());
                            ArticleSDB articleSDB = SQL_DB.articleDao().getByTovId(Integer.parseInt(tovar.getiD()));
                            item.error = 1;

                            String code = tovar.getiD();
                            if (articleSDB != null && articleSDB.vendorCode != null)
                                code = articleSDB.vendorCode;

//                    item.errorNote = "(" + tovar.getiD() + ") " + tovar.getNm() + " отриману з додатку мережі.";  // 14.02.2024 По просьбе Анны меняю тут на Артикула
                            item.errorNote = "(" + code + ") " + tovar.getNm() + " отриману з додатку мережі.";

                            tovs.append(createLinkedString(item.errorNote, item)).append("\n");
                        } else {
                            item.error = 0;
                        }
                    } else if (tovIds.contains(item.getTovarId())) {
                        int face = Integer.parseInt(item.face);
                        if (face == 0 && !stackPhotoList.stream().anyMatch(stackPhoto -> stackPhoto.tovar_id.equals(item.getTovarId()))) {
                            TovarDB tovar = TovarRealm.getById(item.getTovarId());
                            ArticleSDB articleSDB = SQL_DB.articleDao().getByTovId(Integer.parseInt(tovar.getiD()));
                            item.error = 1;

                            String code = tovar.getiD();
                            if (articleSDB != null && articleSDB.vendorCode != null)
                                code = articleSDB.vendorCode;

//                    item.errorNote = "(" + tovar.getiD() + ") " + tovar.getNm() + " отриману з додатку мережі.";  // 14.02.2024 По просьбе Анны меняю тут на Артикула
                            item.errorNote = "(" + code + ") " + tovar.getNm() + " отриману з додатку мережі.";

                            tovs.append(createLinkedString(item.errorNote, item)).append("\n");
                        } else {
                            item.error = 0;
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
                spannableStringBuilder.append("Не надані світлини з ЗАЛИШКАМИ ").append(s).append(" відсутніх товарів. Таким чином Ви повинні підтвердити, що даних товарів нема на залишках.");

                spannableStringBuilder.append("\n\n").append(tovs);
                signal = true;
            } else {
                spannableStringBuilder.append("Зауваженнь по наданню світлин залишків по відсутнім товарам нема.");
                signal = false;
            }

            // 7.0.
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
                }
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

            checkUnlockCode(optionDB);


        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlAvailabilityControlPhotoRemainingGoods/executeOption", "Exception e: " + e);
        }
    }

    private SpannableString createLinkedString(String msg, ReportPrepareDB rp) {
        SpannableString res = new SpannableString(msg);

        try {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    new TovarRequisites(TovarRealm.getById(rp.tovarId), rp).createDialog(context, WpDataRealm.getWpDataRowByDad2Id(Long.parseLong(rp.codeDad2)), null, () -> {
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
