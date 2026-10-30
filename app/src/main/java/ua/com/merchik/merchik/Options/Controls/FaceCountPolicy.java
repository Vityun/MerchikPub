package ua.com.merchik.merchik.Options.Controls;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import ua.com.merchik.merchik.data.Database.Room.ResultsDynamicSDB;

/** Calculations for 1C ПровФейс; database access and dialogs belong to the option. */
public final class FaceCountPolicy {
    private static final ZoneId ZONE = ZoneId.of("Europe/Kiev");
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyMM");
    private static final LocalDate MAX_PRODUCT_CHECK_FROM = LocalDate.of(2026, 10, 30);
    private static final LocalDate BLOCK_AFTER = LocalDate.of(2026, 11, 1);

    private FaceCountPolicy() { }

    public static final class Product {
        public final String id;
        public final BigDecimal faces;
        public final boolean promotion;

        public Product(String id, String faces, String akciya) {
            this(normalizeId(id), number(faces), "1".equals(trim(akciya)));
        }

        private Product(String id, BigDecimal faces, boolean promotion) {
            this.id = id;
            this.faces = faces;
            this.promotion = promotion;
        }
    }

    public static final class Visit {
        public final List<Product> products = new ArrayList<>();
        public BigDecimal faces = BigDecimal.ZERO;
        public int sku;
        public int promotions;
        public BigDecimal facesPerSku = BigDecimal.ZERO;
    }

    public static final class Network {
        public int rows;
        public BigDecimal faces = BigDecimal.ZERO;
        public BigDecimal sku = BigDecimal.ZERO;
        public BigDecimal documents = BigDecimal.ZERO;
        public BigDecimal facesPerSku = BigDecimal.ZERO;
    }

    public static final class Result {
        public boolean signal;
        public boolean blockPns;
        public String message;
    }

    public static final class ProductLink {
        public final String productId;
        public final int start;
        public final int end;

        private ProductLink(String productId, int start, int end) {
            this.productId = productId;
            this.start = start;
            this.end = end;
        }
    }

    public static final class Message {
        public final String text;
        public final List<ProductLink> links;

        private Message(String text, List<ProductLink> links) {
            this.text = text;
            this.links = links;
        }
    }

    public static LocalDate visitDate(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZONE).toLocalDate();
    }

    public static String monthFrom(LocalDate date) {
        return date.minusDays(20).withDayOfMonth(1).format(MONTH);
    }

    public static String monthTo(LocalDate date) {
        return date.format(MONTH);
    }

    //3.1. Среднее количество фейсов на одно СКЮ по ДАННОМУ отчету.
    public static Visit visit(List<Product> rows) {
        Map<String, Product> grouped = new LinkedHashMap<>();
        for (Product row : rows) {
            String key = row.id + ":" + row.promotion;
            Product previous = grouped.get(key);
            grouped.put(key, previous == null ? row : new Product(row.id,
                    previous.faces.add(row.faces), row.promotion));
        }
        Visit result = new Visit();
        result.products.addAll(grouped.values());
        for (Product row : result.products) {
            result.faces = result.faces.add(row.faces);
            if (row.faces.signum() > 0) result.sku++;
            if (row.promotion) result.promotions++;
        }
        result.facesPerSku = ratio(result.faces, BigDecimal.valueOf(result.sku));
        return result;
    }

    //3.2. Суммы по клиенту/сети, а не среднее арифметическое средних по адресам.
    public static Network network(List<ResultsDynamicSDB> rows) {
        Network result = new Network();
        Set<String> addresses = new HashSet<>();
        for (ResultsDynamicSDB row : rows) {
            addresses.add(row.clientId + ":" + row.addrId);
            result.faces = result.faces.add(number(row.totalFace));
            result.sku = result.sku.add(number(row.totalSku));
            result.documents = result.documents.add(number(row.totalDocuments));
        }
        result.rows = addresses.size();
        result.facesPerSku = ratio(result.faces, result.sku);
        return result;
    }

    //3.3. Первый товар после сортировки по убыванию фейсов; при равенстве сохраняем порядок.
    public static Product maximum(Visit visit) {
        Product maximum = null;
        for (Product product : visit.products) {
            if (maximum == null || product.faces.compareTo(maximum.faces) > 0) maximum = product;
        }
        return maximum;
    }

    //4.0. Подведем итог. Порядок веток соответствует ПровФейс от 30.10.2026.
    public static Result evaluate(Visit visit, Network network, Product maximum,
                                  LocalDate date, boolean configuredBlock) {
        Result result = new Result();
        result.blockPns = configuredBlock;
        String current = text(visit.facesPerSku);
        String average = text(network.facesPerSku);
        if (visit.products.size() < 3) {
            result.message = "Данные о наличии товаров на ТТ по данному отчету не репрезентативны. Расчет пропускаем!";
        } else if (network.rows == 0 || network.documents.compareTo(BigDecimal.TEN) < 0) {
            result.message = "Данные о наличии товаров на ТТ по данной сети не репрезентативны. Расчет пропускаем!";
        } else if (visit.facesPerSku.compareTo(network.facesPerSku.multiply(new BigDecimal("0.5"))) < 0) {
            result.message = "Данные о колич. Фейсов/СКЮ (" + current + ") намного меньше, чем среднее по этому Клиент/Сеть: " + average + ". Отчет скорее всего не корректный.";
            result.signal = true;
        } else if (visit.facesPerSku.compareTo(network.facesPerSku.multiply(new BigDecimal("5"))) > 0 && visit.promotions == 0) {
            result.message = "Данные о колич. Фейсов/СКЮ (" + current + ") намного больше, чем среднее по этому Клиент/Сеть: " + average + " и нет акции! Отчет наверняка не корректный.";
            result.signal = true;
            result.blockPns = true;
        } else if (visit.facesPerSku.compareTo(network.facesPerSku.multiply(new BigDecimal("2"))) > 0 && visit.promotions == 0) {
            result.message = "Данные о колич. Фейсов/СКЮ (" + current + ") заметно больше, чем среднее по этому Клиент/Сеть: " + average + " и при этом ни по одному товару не проводится акция! Отчет скорее всего не корректный.";
            result.signal = true;
        } else if (visit.facesPerSku.compareTo(network.facesPerSku.multiply(new BigDecimal("2"))) > 0
                && maximum != null && !maximum.promotion && !date.isBefore(MAX_PRODUCT_CHECK_FROM)) {
            result.message = "Данные о колич. Фейсов/СКЮ (" + current + ") заметно больше, чем среднее по этому Клиент/Сеть: " + average + ". Отчет скорее всего не корректный.";
            result.signal = true;
        } else if (visit.facesPerSku.compareTo(network.facesPerSku.multiply(new BigDecimal("2"))) > 0 && visit.promotions > 0) {
            result.message = "Данные о колич. Фейсов/СКЮ (" + current + ") заметно больше, чем среднее по этому Клиент/Сеть: " + average + " но при этом есть " + visit.promotions + " акций! Отчет не мешало бы проверить!";
        } else {
            result.message = "Данные о колич. Фейсов/СКЮ (" + current + ") мало отличаются от среднего по этому Клиент/Сеть: " + average + ". Зауважень нема.";
        }
        // В 1С строго Дат > '01.11.2026', в том числе для принудительного блока при > x5.
        if (!date.isAfter(BLOCK_AFTER)) result.blockPns = false;
        return result;
    }

    public static Message formatMessage(Result result, Visit visit, Network network,
                                        Function<Product, String> productLabel) {
        StringBuilder message = new StringBuilder(result.message);
        List<ProductLink> links = new ArrayList<>();
        BigDecimal threshold = network.facesPerSku.multiply(new BigDecimal("2"));
        // Diagnostic candidates only; they must not change the ordered signal/block rules.
        if (result.signal && visit.facesPerSku.compareTo(threshold) > 0) {
            List<Product> candidates = new ArrayList<>();
            for (Product product : visit.products) {
                if (!product.promotion && product.faces.compareTo(threshold) > 0) {
                    candidates.add(product);
                }
            }
            candidates.sort((left, right) -> right.faces.compareTo(left.faces));
            if (!candidates.isEmpty()) message.append("\n");
            for (Product product : candidates) {
                message.append("\n");
                int start = message.length();
                message.append(productLabel.apply(product));
                links.add(new ProductLink(product.id, start, message.length()));
                message.append(" - указано ").append(text(product.faces))
                        .append(" фейсов, при этом по товару нет акции.");
            }
        }
        return new Message(message.toString(), links);
    }

    public static String productLabel(String id, String name, String weight) {
        return ("(" + id + ") " + trim(name) + " " + trim(weight)).trim();
    }

    public static String faceInput(String value) {
        BigDecimal faces = new BigDecimal(trim(value).replace(',', '.'));
        if (faces.signum() < 0) throw new IllegalArgumentException("Negative face count");
        return text(faces);
    }

    //6.0. Если сигнал есть - блокируем проведение (если это определено настройками опции).
    public static String conductMessage(boolean signal, boolean blockPns, boolean conducted) {
        if (!signal) return "";
        if (blockPns) return conducted ? " Документ будет ДЕпроведен!" : " Документ проведеним не буде!";
        return " Ви можете отримати БІЛЬШІ преміальні, якщо будете вносити коректно дет.отчет.";
    }

    public static String normalizeId(String id) {
        String value = trim(id);
        return value.isEmpty() ? "" : new BigInteger(value).toString();
    }

    public static String text(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private static BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        return denominator.signum() == 0 ? BigDecimal.ZERO : numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal number(String value) {
        String normalized = trim(value);
        return normalized.isEmpty() ? BigDecimal.ZERO : new BigDecimal(normalized.replace(',', '.'));
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
