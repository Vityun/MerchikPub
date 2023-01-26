package ua.com.merchik.merchik.Options.Buttons;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

public class OptionButtonPercentageOfThePrize<T> extends OptionControl {
    public static int OPTION_BUTTON_PercentageOfThePrize_ID = 135412;

    private WpDataDB wpDataDB;

    public String date; // Період
    public int kps;  // Вып. работ
    public int reclam;   // Получ. реклам.
    public Double reclamPer;    // Проц. реклам.
    public float maxPer; // Макс. проц.:
    public int bonus; //Бонус/Снижение:

    public OptionButtonPercentageOfThePrize(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        getDocumentVar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            executeOption();
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            this.wpDataDB = WpDataRealm.getWpDataRowByDad2Id(((WpDataDB) document).getCode_dad2());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void executeOption() {
        try {
            if (date != null) {
                spannableStringBuilder.append(Html.fromHtml("<b>Відсоток преміальних"));

                spannableStringBuilder.append("\n\n");

                CharSequence valBonus = counter2Text();

                spannableStringBuilder.append(Html.fromHtml("<b>Період: </b>")).append(date).append("\n");
                spannableStringBuilder.append(Html.fromHtml("<b>Вик. робіт: </b>")).append(String.valueOf(kps)).append(" кпс\n");
                spannableStringBuilder.append(Html.fromHtml("<b>Отримано. рек.: </b>")).append(String.valueOf(reclam)).append(" шт.\n");
                spannableStringBuilder.append(Html.fromHtml("<b>Відсоток. рек.: </b>")).append(String.format("%.2f", reclamPer)).append("%\n");
                spannableStringBuilder.append(Html.fromHtml("<b>Макс. відсоток.: </b>")).append(String.valueOf(maxPer)).append("%\n");
                spannableStringBuilder.append(Html.fromHtml(bonus >= 0 ? "<b>Бонус: </b>" : "<b>Зниження: </b>"))
                        .append(bonus >= 0 ? Html.fromHtml("<font color=green>" + bonus + "%</font>") : Html.fromHtml("<font color=red>" + bonus + "%</font>"))
                        .append(", ").append(bonus >= 0 ? Html.fromHtml("<font color=green>" + valBonus + "%</font>") : Html.fromHtml("<font color=red>" + valBonus + "%</font>")).append(" грн.").append("\n\n");

                spannableStringBuilder.append(Html.fromHtml("<b>Пояснення:</b>")).append("\n");
                spannableStringBuilder.append(Html.fromHtml("<b>Період (діб)</b>")).append(" - період, за який розраховуються показники\n");
                spannableStringBuilder.append(Html.fromHtml("<b>Вик. робіт (кпс)</b>")).append(" - кількість виконаних робіт\n");
                spannableStringBuilder.append(Html.fromHtml("<b>Отримано. рек. (рек)</b>")).append(" - кількість отриманих рекламацій \n");
                spannableStringBuilder.append(Html.fromHtml("<b>Відсоток. рек. (%)</b>")).append(" - 100% * ").append(String.valueOf(reclam)).append("/")
                        .append(String.valueOf(kps)).append(" = ").append(String.format("%.2f", reclamPer)).append("%").append("\n");
                spannableStringBuilder.append(Html.fromHtml("<b>Макс. відсоток. (%)</b>")).append(" - максимально допустимий відсоток рекламацій ").append(String.valueOf(maxPer)).append(" %\n\n");

                spannableStringBuilder.append("Ви отримали ").append(String.format("%.2f", reclamPer))
                        .append("% рекламацій (при максимально допустимому показнику ").append(String.valueOf(maxPer)).append("%)").append(" тому ваші преміальні ")
                        .append(bonus >= 0 ? "збільшено" : "зменшено").append(" на ")
                        .append(bonus >= 0 ? Html.fromHtml("<font color=green>" + bonus + "%</font>") : Html.fromHtml("<font color=red>" + bonus + "%</font>"))
                        .append(", ").append(bonus >= 0 ? Html.fromHtml("<font color=green>" + valBonus + "%</font>") : Html.fromHtml("<font color=red>" + valBonus + "%</font>")).append(" грн.");


                spannableStringBuilder.append("");
            }

        } catch (Exception e) {
            Log.e("test_135412", "Exception e: " + e);
            e.printStackTrace();
        }
    }

    public SpannableStringBuilder getMsg() {
        return spannableStringBuilder;
    }

    private CharSequence counter2Text() {
        CharSequence res = "";
        res = "~" + String.format("%.2f", wpDataDB.getCash_zakaz() * 0.08);
        res = Html.fromHtml("" + res + "");
        return res;
    }
}
