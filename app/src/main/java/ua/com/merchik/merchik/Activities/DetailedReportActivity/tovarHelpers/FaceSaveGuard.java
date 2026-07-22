package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import static ua.com.merchik.merchik.Options.Controls.OptionControlAvailabilityControlPhotoRemainingGoods.EXCEPTION_BEFORE_DATE;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportTovar.TovarRequisites;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.ArticleSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.database.realm.tables.TovarRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public final class FaceSaveGuard {

    private static final String CHECK_FACE_OPTION_ID = "159707";
    private static final int CHECK_FACE_OPTION_ID_INT = 159707;

    private FaceSaveGuard() {
        // no instance
    }

    public static FaceSaveCheckResult canSaveFace(
            Context context,
            WpDataDB wpDataDB,
            ReportPrepareDB rp,
            String newFaceRaw
    ) {
        try {
            if (wpDataDB == null) {
                showSimpleErrorDialog(
                        context,
                        "Изменения не сохранены",
                        "Документ не найден. Невозможно проверить сохранение фейсов."
                );

                return FaceSaveCheckResult.error();
            }

            if (rp == null) {
                showSimpleErrorDialog(
                        context,
                        "Изменения не сохранены",
                        "Товар не найден. Невозможно проверить сохранение фейсов."
                );

                return FaceSaveCheckResult.error();
            }

            long realWorkEndDateTime = getWorkEndDateTime(wpDataDB);

            if (realWorkEndDateTime <= 0L) {
                return FaceSaveCheckResult.success();
            }

            Integer newFace = parseFace(newFaceRaw);

            if (newFace == null) {
                return FaceSaveCheckResult.success();
            }

            if (newFace != 0) {
                return FaceSaveCheckResult.success();
            }

            return checkOption159707ForSingleProduct(
                    context,
                    wpDataDB,
                    rp,
                    newFace
            );

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/canSaveFace",
                    "Exception e: " + e
            );

            showSimpleErrorDialog(
                    context,
                    "Изменения не сохранены",
                    "Ошибка проверки возможности сохранения фейсов."
            );

            return FaceSaveCheckResult.error();
        }
    }


    private static Integer parseFace(String face) {
        try {
            if (face == null) {
                return null;
            }

            String value = face.trim();

            if (value.isEmpty()) {
                return null;
            }

            return Integer.parseInt(value);

        } catch (Exception e) {
            return null;
        }
    }

    private static long getWorkEndDateTime(WpDataDB wpDataDB) {
        long clientEndDateTime = wpDataDB.getClient_end_dt();

        if (clientEndDateTime > 0L) {
            return clientEndDateTime;
        }

        return wpDataDB.getVisit_end_dt();
    }

    public static final class FaceSaveCheckResult {

        private final boolean success;
        private final String error;

        private FaceSaveCheckResult(boolean success, String error) {
            this.success = success;
            this.error = error;
        }

        public static FaceSaveCheckResult success() {
            return new FaceSaveCheckResult(true, null);
        }

        public static FaceSaveCheckResult error() {
            return new FaceSaveCheckResult(false, "error");
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isError() {
            return !success;
        }

        public String getError() {
            return error;
        }
    }

    private static FaceSaveCheckResult checkOption159707ForSingleProduct(
            Context context,
            WpDataDB wpDataDB,
            ReportPrepareDB rp,
            int newFace
    ) {
        try {
            Long dad2 = wpDataDB.getCode_dad2();
            String tovarId = rp.getTovarId();

            if (tovarId == null || tovarId.trim().isEmpty()) {
                return FaceSaveCheckResult.success();
            }

            if (newFace != 0) {
                return FaceSaveCheckResult.success();
            }

            OptionsDB option159707 =
                    RealmManager.getTovarOptionControlInReportPrepare(
                            dad2,
                            CHECK_FACE_OPTION_ID
                    );

            if (option159707 == null) {
                return FaceSaveCheckResult.success();
            }

            if (hasOption159707Exception(wpDataDB)) {
                return FaceSaveCheckResult.success();
            }

            boolean productShouldBeControlled =
                    isProductControlledByOption159707(wpDataDB, rp);

            if (!productShouldBeControlled) {
                return FaceSaveCheckResult.success();
            }

            boolean hasPhotoRemaining =
                    hasRemainingPhotoForProduct(wpDataDB, rp);

            if (hasPhotoRemaining) {
                return FaceSaveCheckResult.success();
            }

            showOption159707BlockedDialog(
                    context,
                    wpDataDB,
                    rp,
                    option159707
            );

            return FaceSaveCheckResult.error();

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/checkOption159707ForSingleProduct",
                    "Exception e: " + e
            );

            showSimpleErrorDialog(
                    context,
                    "Изменения не сохранены",
                    "Ошибка проверки наличия фото остатков по товару."
            );

            return FaceSaveCheckResult.error();
        }
    }

    private static boolean isProductControlledByOption159707(
            WpDataDB wpDataDB,
            ReportPrepareDB rp
    ) {
        try {
            String tovarId = rp.getTovarId();

            if (tovarId == null || tovarId.trim().isEmpty()) {
                return false;
            }

            /*
             * ВАЖНО:
             * Здесь нужно использовать тот же источник, что и в самой опции:
             *
             * AdditionalRequirementsRealm.getDocumentAdditionalRequirements(
             *      document,
             *      true,
             *      OPTION_CONTROL_AVAILABILITY_CONTROL_PHOTO_REMAINING_GOODS_ID,
             *      ...
             * )
             *
             * Если этот метод принимает WpDataDB как document — передаем wpDataDB.
             * Если нет — нужно передать сюда document отдельным параметром.
             */
            List<AdditionalRequirementsDB> additionalRequirements =
                    AdditionalRequirementsRealm.getDocumentAdditionalRequirements(
                            wpDataDB,
                            true,
                            CHECK_FACE_OPTION_ID_INT,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                    );

            /*
             * Если конкретного списка товаров нет,
             * в executeOption берутся все товары отчета.
             * Значит текущая позиция тоже контролируется.
             */
            if (additionalRequirements == null || additionalRequirements.isEmpty()) {
                return true;
            }

            for (AdditionalRequirementsDB item : additionalRequirements) {
                if (item == null) continue;

                String controlledTovarId = item.getTovarId();

                if (tovarId.equals(controlledTovarId)) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/isProductControlledByOption159707",
                    "Exception e: " + e
            );

            return false;
        }
    }

    private static boolean hasRemainingPhotoForProduct(
            WpDataDB wpDataDB,
            ReportPrepareDB rp
    ) {
        try {
            Long dad2 = wpDataDB.getCode_dad2();
            String tovarId = rp.getTovarId();


            if (tovarId == null || tovarId.trim().isEmpty()) {
                return false;
            }

            String[] tovIdsArray = new String[]{tovarId};

            List<StackPhotoDB> stackPhotoList =
                    StackPhotoRealm.getPhoto(
                            null,
                            null,
                            wpDataDB.getUser_id(),
                            null,
                            null,
                            dad2,
                            4,
                            tovIdsArray
                    );

            if (stackPhotoList == null || stackPhotoList.isEmpty()) {
                return false;
            }

            for (StackPhotoDB photo : stackPhotoList) {
                if (photo == null) continue;

                if (tovarId.equals(photo.tovar_id)) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/hasRemainingPhotoForProduct",
                    "Exception e: " + e
            );

            return false;
        }
    }

    private static boolean hasOption159707Exception(WpDataDB wpDataDB) {
        try {
            /*
             * Исключение по исполнителям.
             */
            if (wpDataDB.getUser_id() == 232545 || wpDataDB.getUser_id() == 189955) {
                return true;
            }

            /*
             * Исключение по 20-му / 5-му отчету.
             *
             */
            UsersSDB usersSDB = SQL_DB.usersDao().getById(wpDataDB.getUser_id());

            if (usersSDB != null) {
                if (usersSDB.reportDate20 == null) {
                    return true;
                }

                if (usersSDB.reportDate05 == null) {
                    return true;
                }
            }

            /*
             * Исключение по сети.
             *
             * В оригинальной опции используется:
             * addressSDBDocument.tpId
             *
             * Если tpId есть в wpDataDB или можно получить AddressSDB по документу —
             * ставим тут реальную реализацию.
             */
            Integer ptId = getTpIdForDocument(wpDataDB);

            if (ptId != null && isExcludedGroup(wpDataDB, ptId)) {
                return true;
            }

            /*
             * Доп. требования по сети:
             * если есть список сетей для ОСВ, но текущей сети в нем нет,
             * то опция не контролирует этот визит.
             */
            if (ptId != null && isNetworkSkippedByAdditionalRequirements(wpDataDB, ptId)) {
                return true;
            }

            return false;

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/hasOption159707Exception",
                    "Exception e: " + e
            );

            return false;
        }
    }

    private static boolean isNetworkSkippedByAdditionalRequirements(
            WpDataDB wpDataDB,
            int ptId
    ) {
        try {
            List<AdditionalRequirementsDB> additionalRequirementsGroup =
                    AdditionalRequirementsRealm.getAdditionalRequirements(
                            wpDataDB.getClient_id(),
                            CHECK_FACE_OPTION_ID_INT
                    );

            /*
             * Если списка сетей нет — ограничений по сети нет.
             */
            if (additionalRequirementsGroup == null || additionalRequirementsGroup.isEmpty()) {
                return false;
            }

            String group = String.valueOf(ptId);

            for (AdditionalRequirementsDB item : additionalRequirementsGroup) {
                if (item == null) continue;

                if (group.equals(item.getGrpId())) {
                    /*
                     * В оригинале found = false.
                     * То есть текущая сеть найдена в списке,
                     * значит контроль разрешен.
                     */
                    return false;
                }
            }

            /*
             * Список сетей есть, но текущей сети в нем нет.
             * Оригинальная опция в этом случае не проверяет товары.
             */
            return true;

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/isNetworkSkippedByAdditionalRequirements",
                    "Exception e: " + e
            );

            return false;
        }
    }

    private static boolean isExcludedGroup(WpDataDB wpDataDB, int ptId) {
        try {
            Date docDate = wpDataDB.getDt();

            if (docDate == null) {
                return false;
            }

            LocalDate planDay =
                    docDate.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

            Integer[] groups = {434};

            if (planDay.isBefore(EXCEPTION_BEFORE_DATE)) {
                groups = new Integer[]{434, 319};
            }

            return Arrays.asList(groups).contains(ptId);

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/isExcludedGroup",
                    "Exception e: " + e
            );

            return false;
        }
    }

    private static Integer getTpIdForDocument(WpDataDB wpDataDB) {
        try {

              AddressSDB address = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
              return address != null ? address.tpId : null;


        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/getTpIdForDocument",
                    "Exception e: " + e
            );

            return null;
        }
    }

    private static String buildSingleProductErrorMessage(ReportPrepareDB rp) {
        try {
            String tovarId = rp.getTovarId();

            TovarDB tovar = TovarRealm.getById(tovarId);

            if (tovar == null) {
                return "Робота вже завершена. Неможливо встановити кількість фейсів 0, оскільки по цьому товару потрібно надати фото залишків.";
            }

            String code = tovar.getiD();

            try {
                ArticleSDB articleSDB =
                        SQL_DB.articleDao().getByTovId(Integer.parseInt(tovar.getiD()));

                if (articleSDB != null && articleSDB.vendorCode != null) {
                    code = articleSDB.vendorCode;
                }
            } catch (Exception ignored) {
            }

            return "Не надані світлини з ЗАЛИШКАМИ по ОДНОМУ відсутньому товару.<br>"
                    + "Ви повинні завантажити в нашу систему світлину з залишком товару:<br>"
                    + "<a href=\"app://click\">(" + code + ") " + tovar.getNm() + "</a> <br>" +
                    "Таким чином Ви повинні підтвердити, що даного товару немає на залишках.";
        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/buildSingleProductErrorMessage",
                    "Exception e: " + e
            );

            return "Робота вже завершена. Неможливо встановити кількість фейсів 0, оскільки по цьому товару потрібно надати фото залишків.";
        }
    }

    private static void showOption159707BlockedDialog(
            Context context,
            WpDataDB wpDataDB,
            ReportPrepareDB rp,
            OptionsDB optionDB
    ) {
        try {
            if (context == null) {
                return;
            }

            Context safeContext = Globals.unwrap(context);

            DialogData dialog = new DialogData(safeContext);

            String optionTitle =
                    "Опция: (" + optionDB.getOptionControlId() + ")\n" + optionDB.getOptionControlTxt();

            SpannableStringBuilder text =
                    buildSingleProductErrorSpannable(
                            safeContext,
                            wpDataDB,
                            rp,
                            optionDB
                    );

            dialog.setTitle(optionTitle);
            dialog.setDialogIco();
            dialog.setCancel("Закрити", () -> {

            });

            /*
             * Важно:
             * no-op нужен, чтобы клик по spannable-тексту не закрывал диалог.
             */
            dialog.setText(text, () -> {
            });

            dialog.setClose(dialog::dismiss);
            dialog.show();

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/showOption159707BlockedDialog",
                    "Exception e: " + e
            );
        }
    }

    private static SpannableStringBuilder buildSingleProductErrorSpannable(
            Context context,
            WpDataDB wpDataDB,
            ReportPrepareDB rp,
            OptionsDB optionDB
    ) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        try {
            String itemText = buildSingleProductItemText(rp);

            builder
                    .append("Не надані світлини з ЗАЛИШКАМИ по ОДНОМУ відсутньому товару. ")
                    .append("Таким чином Ви повинні підтвердити, що даного товару нема на залишках.")
                    .append("\n\n")
                    .append("Ви повинні завантажити в нашу систему світлину з залишком товару:")
                    .append("\n")
                    .append(createLinkedProductString(context, wpDataDB, rp, optionDB, itemText))
                    .append("\n");

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/buildSingleProductErrorSpannable",
                    "Exception e: " + e
            );

            builder.clear();
            builder.append(
                    "Робота вже завершена. Неможливо встановити кількість фейсів 0, " +
                            "оскільки по цьому товару потрібно надати фото залишків."
            );
        }

        return builder;
    }

    private static String buildSingleProductItemText(ReportPrepareDB rp) {
        try {
            String tovarId = rp.getTovarId();

            TovarDB tovar = TovarRealm.getById(tovarId);

            if (tovar == null) {
                return "Товар " + tovarId + " отриману з додатку мережі.";
            }

            String code = tovar.getiD();

            try {
                ArticleSDB articleSDB =
                        SQL_DB.articleDao().getByTovId(Integer.parseInt(tovar.getiD()));

                if (articleSDB != null && articleSDB.vendorCode != null) {
                    code = articleSDB.vendorCode;
                }
            } catch (Exception ignored) {
            }

            return "(" + code + ") " + tovar.getNm() + " отриману з додатку мережі.";

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/buildSingleProductItemText",
                    "Exception e: " + e
            );

            return "Товар отриману з додатку мережі.";
        }
    }

    private static SpannableString createLinkedProductString(
            Context context,
            WpDataDB wpDataDB,
            ReportPrepareDB rp,
            OptionsDB optionDB,
            String msg
    ) {
        SpannableString res = new SpannableString(msg);

        try {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    try {
                        new TovarRequisites(
                                TovarRealm.getById(rp.getTovarId()),
                                rp
                        ).createDialog(
                                context,
                                wpDataDB,
                                optionDB,
                                () -> {
                                }
                        ).show();

                    } catch (Exception e) {
                        Globals.writeToMLOG(
                                "ERROR",
                                "FaceSaveGuard/createLinkedProductString/onClick",
                                "Exception e: " + e
                        );
                    }
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };

            res.setSpan(
                    clickableSpan,
                    0,
                    msg.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/createLinkedProductString",
                    "Exception e: " + e
            );
        }

        return res;
    }

    private static void showSimpleErrorDialog(
            Context context,
            String title,
            String message
    ) {
        try {
            if (context == null) {
                return;
            }

            DialogData dialog = new DialogData(Globals.unwrap(context));
            dialog.setTitle(title);
            dialog.setText(message);
            dialog.setDialogIco();
            dialog.setClose(dialog::dismiss);
            dialog.show();

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/showSimpleErrorDialog",
                    "Exception e: " + e
            );
        }
    }
}
