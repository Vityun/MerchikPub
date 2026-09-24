package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.merchik.merchik.Activities.PhotoLogActivity.PhotoLogPhotoAdapter;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.DossierSotrSDB;
import ua.com.merchik.merchik.data.Database.Room.ShowcaseSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogFullPhoto;
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR;

/**
 * Контроль изготовления ФОТО с указанием витрин к которым они относятся.
 */
public class OptionControlPhotoShowcase<T> extends OptionControl {

    public int OPTION_CONTROL_PhotoShowcase_ID = 160568;

    public boolean signal = true;
    private int colMin = 50;
    private Date date;
    private Date dateFrom;
    private Date dateTo;
    private long dad2;
    private int percentValue;
    private int perShowcase;

    private WpDataDB wpDataDB;
    private UsersSDB usersSDB;

    private List<ShowcaseSDB> showcaseSDBList;
    private List<StackPhotoDB> stackPhotoDBSList;
    private List<AdditionalRequirementsDB> additionalRequirementsDBS;
    private final List<StackPhotoDB> list = new ArrayList<>();
    private final List<ShowcaseSDB> showcaseSDBListNotCreated = new ArrayList<>();

    public OptionControlPhotoShowcase(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
            logOptionError("init", e);
        }
    }

    private void getDocumentVar() {
        try {
            //1.0. определим переменные
            int min = Integer.parseInt(optionDB.getAmountMin());
            int max = Integer.parseInt(optionDB.getAmountMax());

            colMin = min > 0 ? min : 50;

            //1.3. определим переменные в зависимости от документа
            wpDataDB = (WpDataDB) document;
            date = wpDataDB.getDt();
            dad2 = wpDataDB.getCode_dad2();

            dateFrom = date;
            dateTo = date;
            usersSDB = SQL_DB.usersDao().getById(wpDataDB.getUser_id());


            // 2.1
            additionalRequirementsDBS = AdditionalRequirementsRealm.getAdditionalRequirements(wpDataDB.getClient_id(), wpDataDB.getAddr_id(), 160568);

            List<Integer> shwAR = new ArrayList<>();
            for (AdditionalRequirementsDB item : additionalRequirementsDBS) {
                shwAR.add(item.showcaseTpId);
            }

            // 2.2
            // Тут ещё должен быть фильтр по Дате - 2 дня. Но у меня Дата в Date, а дата у Витрин - Строка
            if (shwAR.size() > 0) {
                showcaseSDBList = SQL_DB.showcaseDao().getByDoc(wpDataDB.getClient_id(), wpDataDB.getAddr_id(), shwAR);
            } else {
                showcaseSDBList = SQL_DB.showcaseDao().getByDoc(wpDataDB.getClient_id(), wpDataDB.getAddr_id());
            }

            Integer mainOptionId = parseIntOrNull(wpDataDB.getMain_option_id());
            int showcasesBeforeMainFilter = showcaseSDBList == null ? -1 : showcaseSDBList.size();
            String mainFilter = "SKIPPED";

            if (mainOptionId != null && showcaseSDBList != null) {

                Calendar calendar = Calendar.getInstance();
                calendar.set(2026, Calendar.SEPTEMBER, 13, 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                Date dateFromNewLogic = calendar.getTime();
                Date wpDate = wpDataDB.getDt();

                boolean useNewLogic = wpDate != null && !wpDate.before(dateFromNewLogic);
                mainFilter = useNewLogic ? "MAIN_OR_ZERO" : "MAIN_ONLY";

                List<ShowcaseSDB> filteredList = showcaseSDBList.stream()
                        .filter(item -> {
                            if (item == null) {
                                return false;
                            }

                            if (useNewLogic) {
                                // С 13.09.2026: основная опция + опция 0
                                return Objects.equals(item.mainOptionId, mainOptionId)
                                        || Objects.equals(item.mainOptionId, 0);
                            } else {
                                // До 13.09.2026: старое поведение
                                return Objects.equals(item.mainOptionId, mainOptionId);
                            }
                        })
                        .collect(Collectors.toList());

                if (!filteredList.isEmpty()) {
                    showcaseSDBList = filteredList;
                } else {
                    mainFilter += "_EMPTY_KEEP_ORIGINAL";
                }
            }

            // 3.1
            // ШЕВА ПРОСИТ 45 ТИП СЮДА ДОБАВИТЬ
            stackPhotoDBSList = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getPhotosByDAD2(dad2, 0)); // 0 - Фото Витрины
            List<StackPhotoDB> stackPhotoDBSList45 = RealmManager.INSTANCE.copyFromRealm(StackPhotoRealm.getPhotosByDAD2(dad2, 45)); // 0 - Фото Витрины
            stackPhotoDBSList.addAll(stackPhotoDBSList45);
            Globals.writeToMLOG("INFO", "OptionControlPhotoShowcase/input",
                    describeOption(optionDB) + ", mode=" + nnkMode
                            + ", doc=" + wpDataDB.getDoc_num_otchet() + ", date=" + date
                            + ", client=" + wpDataDB.getClient_id() + ", addr=" + wpDataDB.getAddr_id()
                            + ", colMin=" + colMin + ", amountMin=" + min + ", amountMax=" + max
                            + ", osvCount=" + additionalRequirementsDBS.size()
                            + ", mainOption=" + mainOptionId + ", mainFilter=" + mainFilter
                            + ", showcasesBeforeMainFilter=" + showcasesBeforeMainFilter
                            + ", showcases=" + (showcaseSDBList == null ? -1 : showcaseSDBList.size())
                            + ", showcaseSample(first8,id/mainOption)=" + (showcaseSDBList == null ? "null"
                            : showcaseSDBList.stream().limit(8)
                            .map(s -> s == null ? "null" : s.id + "/" + s.mainOptionId).collect(Collectors.toList()))
                            + ", photos0=" + (stackPhotoDBSList.size() - stackPhotoDBSList45.size())
                            + ", photos45=" + stackPhotoDBSList45.size()
                            + ", photoSample(first8,id/serverId/showcaseId/exampleImgId)="
                            + stackPhotoDBSList.stream().limit(8)
                            .map(p -> p == null ? "null" : p.getId() + "/" + p.getPhotoServerId()
                                                           + "/" + p.getShowcase_id() + "/" + p.getExample_img_id())
                            .collect(Collectors.toList()));
        } catch (Exception e) {
            logOptionError("getDocumentVar", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        String resultBranch = "CALCULATION_NOT_FINISHED";
        try {
            //3.2. отметим фото для которых витрина определена (для цього використовую СпецКол, щоб не створювати окремоъ колонки)
            // Фото с витриной считаем все, список сфотографированных витрин - без повторов.
            list.clear();
            showcaseSDBListNotCreated.clear();
            int filledShowcaseIdsCount = 0;
            Set<Integer> uniqueShowcaseIds = new HashSet<>();
            for (StackPhotoDB stackPhotoDB : stackPhotoDBSList) {
                Integer showcaseIdStack = parseIntOrNull(stackPhotoDB.getShowcase_id());
                if (showcaseIdStack != null && showcaseIdStack > 0) {
                    filledShowcaseIdsCount++;
                    boolean isShowcaseIdPresent = showcaseSDBList.stream()
                            .anyMatch(showcaseSDB -> Objects.equals(showcaseSDB.id, showcaseIdStack));

                    if (isShowcaseIdPresent && uniqueShowcaseIds.add(showcaseIdStack)) {
                        list.add(stackPhotoDB);
                    }
                }
            }
            // ДОБАВЛЯЕМ ВИТРИНЫ, КОТОРЫХ НЕТ В СФОТОГРАФИРОВАННЫХ
            for (ShowcaseSDB showcase : showcaseSDBList) {
                if (!uniqueShowcaseIds.contains(showcase.id)) {
                    showcaseSDBListNotCreated.add(showcase);
                }
            }

            //3.3. підрахуємо відсоток світлин у котррих зазначениа вітрина
            //3.3.1. відсоток СВІТЛИН, у котрих вказані вітрини (до загальної кількості світлин)
            percentValue = stackPhotoDBSList.isEmpty() ? 0
                    : Math.round(100f * filledShowcaseIdsCount / stackPhotoDBSList.size());

            //3.3.2. відсоток ВІТРИН, котрі сфотографовані (до загальної кількості вітрин)
            perShowcase = showcaseSDBList.isEmpty() ? 0
                    : Math.round(100f * uniqueShowcaseIds.size() / showcaseSDBList.size());

            //3.4. Якщо вітрин немає, перевіримо попередні проведені роботи за клієнтом/адресою.
            int newTT = 0;
            if (showcaseSDBList.isEmpty() && uniqueShowcaseIds.isEmpty()) {
                Calendar historyFrom = Calendar.getInstance();
                historyFrom.setTime(date);
                historyFrom.add(Calendar.DAY_OF_MONTH, -20);
                List<WpDataDB> wpSize = WpDataRealm.getWpDataBy(historyFrom.getTime(), date, 1,
                        wpDataDB.getAddr_id(), wpDataDB.getClient_id(), null);
                boolean hasPreviousWork = wpSize != null && wpSize.stream()
                        .anyMatch(wp -> wp.getCode_dad2() != dad2);
                if (!hasPreviousWork) {
                    newTT = 1;
                }
            }

            Globals.writeToMLOG("INFO", "OptionControlPhotoShowcase/calculation",
                    "dad2=" + dad2 + ", mode=" + nnkMode + ", colMin=" + colMin
                            + ", totalPhotos=" + stackPhotoDBSList.size()
                            + ", filledShowcaseIdsCount=" + filledShowcaseIdsCount
                            + ", uniqueShowcaseIds=" + uniqueShowcaseIds.size()
                            + ", showcases=" + showcaseSDBList.size()
                            + ", missingShowcases=" + showcaseSDBListNotCreated.size()
                            + ", percentValue=" + percentValue + ", perShowcase=" + perShowcase
                            + ", newTT=" + newTT);

            //4.0. обработаем результат
            if (stackPhotoDBSList.isEmpty()) {
                resultBranch = "NO_PHOTOS";
                spannableStringBuilder.append("Не можу знайти світлини стосовні до поточного відвідування.");
                signal = true;
            } else if (!showcaseSDBList.isEmpty() && filledShowcaseIdsCount == 0) {
                resultBranch = "NO_SHOWCASE_SELECTED";
                spannableStringBuilder.append("При виготовленні світлин Ви НЕ обрали жодної з ").append(String.valueOf(showcaseSDBList.size())).append(" вітрин.");
                signal = true;
            } else if (colMin > 0 && percentValue < colMin && newTT == 0 && showcaseSDBList.size() > 0) {
                resultBranch = "PHOTO_PERCENT_BELOW_MIN";
                spannableStringBuilder.append("При виготовленні світлин, Ви зазначили вітрини лише у ")
                        .append(String.valueOf(filledShowcaseIdsCount))
                        .append(" фото з ")
                        .append(String.valueOf(stackPhotoDBSList.size()))
                        .append(" (")
                        .append(String.valueOf(percentValue)).append("%), що МЕНШЕ плану в ")
                        .append(String.valueOf(colMin)).append("%")
                        .append(" Загальна кількість вітрин на ТТ: ")
                        .append(String.valueOf(showcaseSDBList.size()))
                        .append(", з них фото зроблено ").append(String.valueOf(uniqueShowcaseIds.size()))
                        .append(" (").append(String.valueOf(perShowcase)).append("%).");
                signal = true;
            } else if (colMin > 0 && percentValue < colMin && newTT == 0 && showcaseSDBList.isEmpty()) {
                resultBranch = "PHOTO_PERCENT_BELOW_MIN_NO_SHOWCASES";
                spannableStringBuilder.append("При виготовленні світлин, Ви зазначили вітрини лише у ")
                        .append(String.valueOf(filledShowcaseIdsCount))
                        .append(" фото з ").append(String.valueOf(stackPhotoDBSList.size()))
                        .append(" (").append(String.valueOf(percentValue))
                        .append("%), що МЕНШЕ плану в ").append(String.valueOf(colMin))
                        .append("%, але на момент відвідування, вітрини ще не були створені, тому зауважень немає.");
                signal = false;
            } else if (!showcaseSDBList.isEmpty() && uniqueShowcaseIds.size() < showcaseSDBList.size() * (double) colMin / 100) {
                resultBranch = "SHOWCASE_COUNT_BELOW_MIN";
                spannableStringBuilder.append("При виготовленні світлин, Ви сфотографували лише у ")
                        .append(String.valueOf(uniqueShowcaseIds.size()))
                        .append(" вітрин з ")
                        .append(String.valueOf(showcaseSDBList.size())).append(" присутніх на ТТ (")
                        .append(String.valueOf(perShowcase))
                        .append("%), що МЕНШЕ плану в ")
                        .append(String.valueOf(colMin)).append("%.")
                        .append(" Усього зроблено фото ").append(String.valueOf(stackPhotoDBSList.size()));
                signal = true;

            } else if (showcaseSDBList.isEmpty() && uniqueShowcaseIds.isEmpty() && newTT == 1) {
                resultBranch = "NO_SHOWCASES_NEW_TT_1";
                spannableStringBuilder.append("На момент виконання робіт, Вітрини по даному Кліенту/Адресі ще не визначені. Зауважень нема.");
                signal = false;

            } else if (showcaseSDBList.isEmpty() && uniqueShowcaseIds.isEmpty() && newTT == 0) {
                resultBranch = "NO_SHOWCASES_NEW_TT_0";
                spannableStringBuilder.append("На момент виконання робіт, Вітрини по даному Кліенту/Адресі ще не визначені. " +
                        "Але роботи у ТТ вже виконувались раніше і Вітрини вже повинні були створені");
                signal = true;
            } else {
                resultBranch = "ENOUGH_SHOWCASES";
                spannableStringBuilder.append("При виготовленні світлин, Ви зазначили вітрини у ")
                        .append(String.valueOf(uniqueShowcaseIds.size()))
                        .append(" з ")
                        .append(String.valueOf(showcaseSDBList.size()))
                        .append(" присутнiх на ТТ")
                        .append(" (").append(String.valueOf(perShowcase))
                        .append("%), що БІЛЬШЕ плану в ").append(String.valueOf(colMin)).append("%.")
                        .append(" Зауважень немає.");

                signal = false;
            }

            //4.1. Виключення на випадок, якщо це перша/друга робота у даній ТТ з даним кліснтом
            boolean signalBeforeExceptions = signal;
            String dossierInfo = "DISABLED_UNCONFIRMED";
            String exceptionReason = "DOSSIER_CHECK_DISABLED";

            if (signal) {
                List<DossierSotrSDB> dossierSotrSDBList = SQL_DB.dossierSotrDao().getData(null, 982L, wpDataDB.getCode_iza());
                if (dossierSotrSDBList.isEmpty())
                    dossierSotrSDBList = SQL_DB.dossierSotrDao().getDataByClientAddress(982L, Long.valueOf(wpDataDB.getAddr_id()), wpDataDB.getClient_id());

                dossierInfo = "rows=" + dossierSotrSDBList.size();
                if (!dossierSotrSDBList.isEmpty()) {
                    Long dataNR;
                    long dataWP = wpDataDB.getDt().getTime() / 1000;
                    if (dossierSotrSDBList.get(0).priznak > 31536000) { //31536000 -> 1971 год
                        dataNR = dossierSotrSDBList.get(0).priznak;
                    } else {
                        dataNR = dataWP;
                    }
                    dossierInfo += ", rawStart=" + dossierSotrSDBList.get(0).priznak
                            + ", effectiveStart=" + dataNR + ", visitSec=" + dataWP;
                    if (dataNR > dataWP - (14 * 86400)) { // 86400 - 1 день в сек.
                        exceptionReason = "WORK_LESS_THAN_14_DAYS";
                        spannableStringBuilder.append(" але, роботи з цим ІЗА почали ");
                        spannableStringBuilder.append(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date(dataNR * 1000)));
                        spannableStringBuilder.append(". З цього моменту минуло менше двох тижнів, тому зроблено виключення.");
                        signal = false;
                    }
                } else {
                    exceptionReason = "NO_DOSSIER";
                    spannableStringBuilder.append(" Але, це перша робота поточного виконавця з зазначеним ІЗА, тому зроблено виключення.");
                    signal = false;
                }
            }

            if (!showcaseSDBListNotCreated.isEmpty()) {
                spannableStringBuilder.append("\nНе виготовлені світлини по:\n");
                for (ShowcaseSDB showcase : showcaseSDBListNotCreated) {
                    String msg = showcase.photoId + " (" + showcase.nm + ")";
                    spannableStringBuilder
                            .append(createLinkedString(msg, showcase))
                            .append("\n");
                }
            }

            // Сохранение
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

            if (signal) {
                if (optionDB.getBlockPns().equals("1")) {
                    setIsBlockOption(signal);
                    spannableStringBuilder.append("\n\n").append("Документ проведен не будет!");
                } else {
                    spannableStringBuilder.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете делать Достижения.");
                }
            }
            checkUnlockCode(optionDB);
            Globals.writeToMLOG("INFO", "OptionControlPhotoShowcase/result",
                    describeOption(optionDB) + ", mode=" + nnkMode + ", branch=" + resultBranch
                            + ", noPhotos=" + stackPhotoDBSList.isEmpty()
                            + ", signalBeforeExceptions=" + signalBeforeExceptions + ", signalAfterExceptions=" + signal
                            + ", exception=" + exceptionReason + ", codeIza=" + wpDataDB.getCode_iza()
                            + ", dossier={" + dossierInfo + "}, blocked=" + isBlockOption());
//            if (signal) {
//                unlockCodeResultListener.onUnlockCodeFailure();
//            } else {
//                unlockCodeResultListener.onUnlockCodeSuccess();
//            }

        } catch (Exception e) {
            logOptionError("executeOption/" + resultBranch, e);
        }
    }

    private SpannableString createLinkedString(String msg, ShowcaseSDB showcaseSDB) {
        SpannableString res = new SpannableString(msg);
        StackPhotoDB stackPhotoDB = RealmManager.getPhotoByPhotoId(String.valueOf(showcaseSDB.photoId));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                if (showcaseSDB != null) {
                    DialogFullPhoto dialogFullPhoto = new DialogFullPhoto(context);
                    dialogFullPhoto.setWpDataDB(wpDataDB);
                    dialogFullPhoto.setPhotos(0, Collections.singletonList(stackPhotoDB), new PhotoLogPhotoAdapter.OnPhotoClickListener() {
                        @Override
                        public void onPhotoClicked(Context context, StackPhotoDB photoDB) {
                            try {
                                DialogFullPhotoR dialogFullPhoto = new DialogFullPhotoR(context);
                                dialogFullPhoto.setPhoto(stackPhotoDB);

                                // Pika
                                dialogFullPhoto.setComment(stackPhotoDB.getComment());

                                dialogFullPhoto.setClose(dialogFullPhoto::dismiss);
                                dialogFullPhoto.show();
                            } catch (Exception e) {
                                Log.e("ShowcaseAdapter", "Exception e: " + e);
                                logOptionError("onPhotoClicked", e);
                            }
                        }
                    }, () -> {
                    });
                    dialogFullPhoto.setClose(dialogFullPhoto::dismiss);
                    dialogFullPhoto.show();
                }
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

    public String getCounter() {
        return list.size() + "/" + showcaseSDBList.size();
    }

    private static Integer parseIntOrNull(String value) {
        try {
            return value != null ? Integer.parseInt(value.trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
