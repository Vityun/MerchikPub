package ua.com.merchik.merchik.Options.Buttons;

import android.content.Context;

import ua.com.merchik.merchik.Options.Controls.OptionControlTaskAnswer;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;

public class OptionButtonTaskAnswer<T> extends OptionControl {

    public int OPTION_BUTTON_RECLAMATION_ANSWER_ID = 135327;

    public OptionButtonTaskAnswer(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;

        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
    }

    private void executeOption() {
        OptionControlTaskAnswer<?> optionControlTaskAnswer = new OptionControlTaskAnswer<>(context, document, optionDB, msgType, nnkMode);
        optionControlTaskAnswer.showOptionMassage();
    }
}
