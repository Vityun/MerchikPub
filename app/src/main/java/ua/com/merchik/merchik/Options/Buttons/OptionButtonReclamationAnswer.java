package ua.com.merchik.merchik.Options.Buttons;

import android.content.Context;

import ua.com.merchik.merchik.Options.Controls.OptionControlReclamationAnswer;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;

public class OptionButtonReclamationAnswer<T> extends OptionControl {

    public int OPTION_BUTTON_RECLAMATION_ANSWER_ID = 135328;

    public OptionButtonReclamationAnswer(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
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
        OptionControlReclamationAnswer<?> optionControlReclamationAnswer = new OptionControlReclamationAnswer<>(context, document, optionDB, msgType, nnkMode);
        optionControlReclamationAnswer.showOptionMassage();
    }
}
