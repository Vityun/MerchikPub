package ua.com.merchik.merchik.Activities.DetailedReportActivity.tovarHelpers;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public final class FaceSaveGuard {

    private static final String CHECK_FACE_OPTION_ID = "159707";

    private FaceSaveGuard() {
        // no instance
    }

    public static FaceSaveCheckResult canSaveFace(
            WpDataDB wpDataDB,
            ReportPrepareDB rp,
            String newFaceRaw
    ) {
        try {
            if (wpDataDB == null) {
                return FaceSaveCheckResult.error(
                        "Документ не найден. Невозможно проверить сохранение фейсов."
                );
            }

            if (rp == null) {
                return FaceSaveCheckResult.error(
                        "Товар не найден. Невозможно проверить сохранение фейсов."
                );
            }

            long realWorkEndDateTime = wpDataDB.getVisit_end_dt();

            /*
             * Если работа НЕ завершена — можно сохранять любое значение.
             */
            if (realWorkEndDateTime <= 0L) {
                return FaceSaveCheckResult.success();
            }

            Integer newFace = parseFace(newFaceRaw);

            /*
             * Пустое/нечисловое значение здесь не блокируем.
             * Этот guard отвечает только за запрет именно нуля после завершения работы.
             */
            if (newFace == null) {
                return FaceSaveCheckResult.success();
            }

            /*
             * Главное условие:
             * после завершения работы нельзя сохранить именно 0.
             * Не <= 0, а == 0.
             */
            if (newFace != 0) {
                return FaceSaveCheckResult.success();
            }

            boolean option159707Exists =
                    RealmManager.hasTovarOptionControlInReportPrepare(
                            wpDataDB.getCode_dad2(),
                            CHECK_FACE_OPTION_ID
                    );

            if (!option159707Exists) {
                return FaceSaveCheckResult.success();
            }

            return FaceSaveCheckResult.error(
                    "Робота вже завершена. Неможливо встановити кількість фейсів 0."
            );

        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "FaceSaveGuard/canSaveFace",
                    "Exception e: " + e
            );

            return FaceSaveCheckResult.error(
                    "Ошибка проверки возможности сохранения фейсов."
            );
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

        public static FaceSaveCheckResult error(String error) {
            return new FaceSaveCheckResult(false, error);
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
}