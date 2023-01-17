package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;
import android.os.Build;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

/**
 * 17.01.23.
 * Опция Контроля: Процент Премии (135061)
 *
 * Функция рассчитывает процент премии исполнителю (от стоимости, которую платит клиент) в
 * зависимости от качества работы (количества рекламаций за 30-ь дней от Дат)
 * */
public class OptionControlPercentageOfThePrize<T> extends OptionControl {
    public int OPTION_CONTROL_PERCENTAGE_OF_THE_PRIZE_ID = 135061;

    public OptionControlPercentageOfThePrize(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        getDocumentVar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            executeOption();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Build.VERSION.SDK_INT: ").append(Build.VERSION.SDK_INT).append("  Build.VERSION_CODES.N: ").append(Build.VERSION_CODES.N);
            Globals.writeToMLOG("INFO", "OptionControlPercentageOfThePrize", "sb: " + sb);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            WpDataDB wp = (WpDataDB) document;
        }
    }

    private void executeOption() {
    }
}
