package ua.com.merchik.merchik.Options.Controls;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;

/** 1465: ПровДлинПП for an executor's visit. */
final class ShelfLengthPolicy {
    static final String MEASUREMENT_MANUFACTURER = "5491";
    static final String UNCATALOGUED_MEASUREMENT_PRODUCT = "175194";
    static final String UNKNOWN_GROUP = "Не визначено";
    static final int ROBOT_ID = 38791;
    private static final ZoneId KYIV = ZoneId.of("Europe/Kiev");
    private static final BigDecimal THREE = BigDecimal.valueOf(3);

    private ShelfLengthPolicy() {}

    static boolean eligible(String visitClient, String rowClient, String cardClient, String manufacturer) {
        String owner = rowClient == null || rowClient.isEmpty() ? cardClient : rowClient;
        return MEASUREMENT_MANUFACTURER.equals(manufacturer) && visitClient != null
                && !visitClient.isEmpty() && !"0".equals(visitClient) && visitClient.equals(owner);
    }

    static long endExclusive(long documentMillis) {
        return Instant.ofEpochMilli(documentMillis).atZone(KYIV).toLocalDate()
                .plusDays(1).atStartOfDay(KYIV).toEpochSecond();
    }

    static final class History {
        long count;
        BigDecimal total = BigDecimal.ZERO;

        void add(Float width, long measurements) {
            count += measurements;
            if (width != null) total = total.add(new BigDecimal(Float.toString(width))
                    .multiply(BigDecimal.valueOf(measurements)));
        }

        boolean outsideRange(BigDecimal length) {
            if (count < 2 || total.signum() <= 0) return false;
            BigDecimal scaled = length.multiply(BigDecimal.valueOf(count));
            return scaled.multiply(THREE).compareTo(total) < 0 || scaled.compareTo(total.multiply(THREE)) > 0;
        }
    }

    static final class Measurement {
        final Integer groupId;
        BigDecimal faces = BigDecimal.ZERO;
        BigDecimal metres = BigDecimal.ZERO;

        Measurement(Integer groupId) {
            this.groupId = groupId;
        }

        String groupLabel(String name) {
            if (groupId == null) return UNKNOWN_GROUP;
            String actualName = name == null ? "" : name.trim();
            return "(" + (actualName.isEmpty() ? UNKNOWN_GROUP : actualName) + " [" + groupId + "])";
        }

        void add(String face, int amount) {
            if (face != null && !face.trim().isEmpty()) {
                try {
                    faces = faces.add(new BigDecimal(FaceCountPolicy.faceInput(face)));
                } catch (IllegalArgumentException ignored) {
                    // An invalid legacy value is not a supplied measurement.
                }
            }
            metres = metres.add(BigDecimal.valueOf(Math.max(0, amount)));
        }

        // 4.1. One issue per measurement, in the same order as in 1C.
        String issue(String group, History history) {
            if (faces.signum() == 0 && metres.signum() == 0)
                return "У группы:\n" + group + " не указаны ни КОЛИЧЕСТВО Фейсов (ВСЕХ товаров ВСЕХ поставщиков ЭТОЙ группы) ни суммарной ДЛИНЫ всех полок ЭТОЙ товарной группы.";
            if (faces.signum() == 0)
                return "У группы:\n" + group + " в поле ФЕЙСОВ не указано ОБЩЕЕ количество фейсов ВСЕХ товаров ВСЕХ поставщиков ЭТОЙ товарной группы.";
            if (metres.signum() == 0)
                return "У группы:\n" + group + " в поле ДлинаПП не указана приблизительная ДЛИНА всех полок (в метрах), на которых присутствуют товары ЭТОЙ группы.";
            BigDecimal density = faces.divide(metres, 1, RoundingMode.HALF_UP);
            if (density.compareTo(BigDecimal.valueOf(5)) < 0 || density.compareTo(BigDecimal.valueOf(50)) > 0)
                return "Согласно ваших данных у группы:\n" + group + " на одном метре полки стоит " + density.toPlainString()
                        + " фейсов. Это похоже на ошибку. Укажите ПРАВИЛЬНО число фейсов ВСЕХ товаров ВСЕХ поставщиков ЭТОЙ группы.";
            if (history != null && history.outsideRange(metres))
                return group + ": Длина ПП " + metres.toPlainString() + " м. указанная Вами, значительно отличается от среднего для данной сети. Это похоже на обман. Укажите ПРАВИЛЬНО длину ВСЕХ полок ВСЕХ товаров ВСЕХ поставщиков ЭТОЙ товарной группы.";
            return "";
        }
    }

    static boolean signal(boolean hasClient, int reportRows, int issues) {
        return hasClient && (reportRows == 0 || issues > 0);
    }

    static String progress(Collection<Measurement> measurements) {
        int started = 0;
        for (Measurement m : measurements) {
            if (m.faces.signum() > 0 || m.metres.signum() > 0) started++;
        }
        return started + "/" + measurements.size();
    }

    static String summary(boolean hasClient, int reportRows, int planned, int issues) {
        if (!hasClient) return "Не определены клиенты для проверки (возможно проверяемый сотрудник не провел свой 20-й отчет)";
        if (reportRows == 0) return "Данные о Длине полок не заполнены ни для одной товарной группы!";
        if (issues > 0) return "Для (" + issues + ") товарных групп не корректно указаны реквизиты (Фейс или ДлинаПП).";
        return "ДПП и Кол.Фейсов указаны для " + planned + " из " + planned + " товарных групп. Подробности см. в таблице.";
    }
}
