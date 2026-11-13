package ua.com.merchik.merchik.Activities.PremiumActivity.PremiumTable;

import java.util.function.Consumer;

import ua.com.merchik.merchik.Utils.text.MvsLinkedTextFormatter;
import ua.com.merchik.merchik.data.RetrofitResponse.models.PremiumResponse;

final class PremiumBasisFormatter {
    private static final String LOAD_ERROR =
            "Дані отримати не вийшло. Повторіть спробу або зверніться до вашого керівника.";
    private static final String NO_BASIS = "Сервер не повернув опис підстави нарахування або зниження.";

    private PremiumBasisFormatter() {
    }

    static Result format(PremiumResponse response, String fallbackTitle, Consumer<String> diagnostic) {
        String title = nonNull(fallbackTitle);
        if (response == null || !Boolean.TRUE.equals(response.state)) {
            diagnostic.accept(response == null ? "body=null" : "state=" + response.state
                    + ", error=" + MvsLinkedTextFormatter.logValue(response.error));
            return new Result(title, LOAD_ERROR);
        }
        if (response.result_list == null || response.result_list.isEmpty()) {
            diagnostic.accept("result_list=" + (response.result_list == null ? "null" : "empty"));
            return new Result(title, NO_BASIS);
        }
        StringBuilder text = new StringBuilder();
        boolean titleSelected = false;
        for (int index = 0; index < response.result_list.size(); index++) {
            PremiumResponse.Result item = response.result_list.get(index);
            if (item == null) {
                diagnostic.accept("row=" + index + ", item=null");
                continue;
            }
            if (!titleSelected) {
                String doc = nonNull(item.docNom).trim();
                String date = nonNull(item.docDat).trim();
                if (!doc.isEmpty() || !date.isEmpty()) {
                    title = doc + (doc.isEmpty() || date.isEmpty() ? "" : " от ") + date;
                    titleSelected = true;
                }
            }
            String basis = item.osnovanie;
            if (basis == null || basis.trim().isEmpty()) {
                diagnostic.accept("row=" + index + ", responseDoc="
                        + MvsLinkedTextFormatter.logValue(item.docNom) + ", osnovanie=empty");
                basis = NO_BASIS;
            }
            text.append(item.sumNZPSotrDoc).append(" грн. ")
                    .append(basis.replace('<', '(').replace('>', ')')).append("<br><br>");
        }
        if (text.length() == 0) {
            return new Result(title, NO_BASIS);
        }
        return new Result(title, text.toString());
    }

    private static String nonNull(String value) {
        return value == null ? "" : value;
    }

    static final class Result {
        final String title;
        final String text;

        Result(String title, String text) {
            this.title = title;
            this.text = text;
        }
    }
}
