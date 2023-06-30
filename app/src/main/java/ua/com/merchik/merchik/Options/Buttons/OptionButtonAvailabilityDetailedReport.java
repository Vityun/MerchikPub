package ua.com.merchik.merchik.Options.Buttons;

import android.content.Context;

import ua.com.merchik.merchik.Options.Controls.OptionControlAvailabilityDetailedReport;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;

public class OptionButtonAvailabilityDetailedReport<T> extends OptionControl {
    public int OPTION_BUTTON_AVAILABILITY_OF_A_DR_ID = 137797;

    public OptionButtonAvailabilityDetailedReport(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        executeOption();
    }

    private void executeOption() {
        OptionControlAvailabilityDetailedReport optionControl = new OptionControlAvailabilityDetailedReport(context, document, optionDB, msgType, nnkMode);
        optionControl.showOptionMassage("");
    }
}
