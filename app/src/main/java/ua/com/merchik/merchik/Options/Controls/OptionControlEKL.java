package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

public class OptionControlEKL<T> extends OptionControl {
    public int OPTION_CONTROL_EKL_ID = 84006;

    private WpDataDB wpDataDB;

    private String PTT;


    public OptionControlEKL(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
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
            wpDataDB = (WpDataDB) document;
        }
    }

    private void executeOption() {
        try {
            createTZN();




        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "OptionControlEKL/executeOption/Exception","Exception: " + e);
        }
    }


    /*Тут в теории должен собираться ТЗН "с одной строки" для подальшей работы с ним. Пока сути для
    меня не вижу кроме как указать что ПТТшник может быть пустым настарте и мы его ПОТОМ переопределим*/
    private void createTZN(){
        PTT = "";   // Сбрасываем ПТТшника в режим "любой"

        // Индивидуальный-ЭКЛ (это заглушка, пока на стороне 1С нормально эт не реализовано)
        if (optionDB.getOptionId().equals("151140")){
            Globals.writeToMLOG("INFO", "OptionControlEKL.executeOption.optionDB.getOptionId().equals(\"151140\")", "Вы попали в Заглушку");
        }
    }

}
