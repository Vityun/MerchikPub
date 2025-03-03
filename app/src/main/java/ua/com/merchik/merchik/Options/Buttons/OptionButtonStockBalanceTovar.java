package ua.com.merchik.merchik.Options.Buttons;

import android.content.Context;

import ua.com.merchik.merchik.Options.Controls.OptionControlStockBalanceTovar;
import ua.com.merchik.merchik.Options.Controls.OptionControlTaskAnswer;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;

public class OptionButtonStockBalanceTovar<T> extends OptionControl {

    public int OPTION_BUTTON_STOCK_BALANCE_TOVAR = 141069;

    public OptionButtonStockBalanceTovar(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;

        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
    }

    private void executeOption() {
        OptionControlStockBalanceTovar<?> optionControlStockBalanceTovar = new OptionControlStockBalanceTovar<>(context, document, optionDB, msgType, nnkMode, unlockCodeResultListener);
        optionControlStockBalanceTovar.showOptionMassage("");
    }
}
