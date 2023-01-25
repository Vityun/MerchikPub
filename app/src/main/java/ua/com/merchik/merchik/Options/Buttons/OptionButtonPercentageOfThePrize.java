package ua.com.merchik.merchik.Options.Buttons;

import android.content.Context;
import android.text.Html;

import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

public class OptionButtonPercentageOfThePrize<T> extends OptionControl {
    public static int OPTION_BUTTON_PercentageOfThePrize_ID = 135412;

    private WpDataDB wpDataDB;

    public OptionButtonPercentageOfThePrize(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            this.wpDataDB = WpDataRealm.getWpDataRowByDad2Id(((WpDataDB) document).getCode_dad2());
        }
    }

    private void executeOption() {
        spannableStringBuilder.append(Html.fromHtml("<b>Відсоток преміальних\n\n"));

        spannableStringBuilder.append(Html.fromHtml("<b>Період: </b>"))            .append("...").append("\n");
        spannableStringBuilder.append(Html.fromHtml("<b>Вып. работ: </b>"))        .append("...").append(" кпс\n");
        spannableStringBuilder.append(Html.fromHtml("<b>Получ. реклам.: </b>"))    .append("...").append("шт.\n");
        spannableStringBuilder.append(Html.fromHtml("<b>Проц. реклам.: </b>"))     .append("...").append("%\n");
        spannableStringBuilder.append(Html.fromHtml("<b>Макс. проц.: </b>"))       .append("...").append("%\n");
        spannableStringBuilder.append(Html.fromHtml("<b>Бонус/Снижение: </b>"))    .append("...").append("%\n\n");

        spannableStringBuilder.append(Html.fromHtml("<b>Описание:</b>\n"));
        spannableStringBuilder.append(Html.fromHtml("<b>Период (дней)</b>")).append(" - период, за который рассчитываются показатели\n");
        spannableStringBuilder.append(Html.fromHtml("<b>Вып. работ (кпс)</b>")).append(" - количество выполненных работ\n");
        spannableStringBuilder.append(Html.fromHtml("<b>Получ. реклам. (рек)</b>")).append(" - количество полученных рекламаций \n");
        spannableStringBuilder.append(Html.fromHtml("<b>Проц. реклам. (%)</b>")).append(" - 100*рек/кпс\n");
        spannableStringBuilder.append(Html.fromHtml("<b>Макс. проц. (%)</b>")).append(" - максимально допустимый процент рекламаций ... %\n\n");

        spannableStringBuilder.append("Вы получили ... % рекламаций поэтому ваши премиальные снижены/увеличены на ... %");

        showOptionMassage();
    }
}
