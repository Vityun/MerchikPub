package ua.com.merchik.merchik.Options;

import static ua.com.merchik.merchik.Global.UnlockCode.UnlockCodeMode.CODE_DAD_2_AND_OPTION;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;

import ua.com.merchik.merchik.Global.UnlockCode;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dialogs.DialogData;

/**
 * 31.05.2022
 * Класс OptionControl занимается сбором данных и отписанием пользователю информации о проблеммах
 * которые возникли при обработке опции.
 */
public class OptionControl<T> {

    public Context context;
    public T document;
    public OptionsDB optionDB;
    public OptionMassageType msgType;
    public Options.NNKMode nnkMode;

    public Clicks.clickStatusMsg clickStatusMsg;

    public UnlockCodeResultListener unlockCodeResultListener;

    public interface UnlockCodeResultListener {
        void onUnlockCodeSuccess();

        void onUnlockCodeFailure();
    }

    public String massageToUser;    // Для быстрого сообщения   // НЕ ЮЗАЙ БОЛЬШЕ ЕГО
    public StringBuilder stringBuilderMsg = new StringBuilder();
    public SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
    public boolean notCloseSpannableStringBuilderDialog = false;    // Делает так что при клике на текст диалог не будет закрываться
    private boolean block;

    public void showOptionMassage(String msg) {
        try {
            if (msgType != null && msgType.type != null) {
                switch (msgType.type) {
                    case STRING:
                        Log.e("OptionControl", "(original) Massage to Log: " + stringBuilderMsg);
                        break;

                    case TOAST:
                        Log.e("OptionControl", "(toast) Massage to Log: " + massageToUser);
                        Toast.makeText(context, massageToUser, Toast.LENGTH_LONG).show();
                        break;

                    case DIALOG:
                        Log.e("OptionControl", "(dialog) Massage to Log: " + stringBuilderMsg);

                        String optionTitle = "Опция: (" + optionDB.getOptionId() + ")\n" + optionDB.getOptionTxt();

                        if (stringBuilderMsg.toString().length() > 1) {
                            DialogData dialog = new DialogData(context);
                            dialog.setTitle(optionTitle);
                            dialog.setText(msg + stringBuilderMsg);
                            if (block) {
                                dialog.setDialogIco();
                            }
                            dialog.setClose(dialog::dismiss);
                            dialog.show();
                        } else if (spannableStringBuilder.toString().length() > 1) {
                            DialogData dialog = new DialogData(context);
                            dialog.setTitle(optionTitle);
                            if (block) {
                                dialog.setDialogIco();
                            }
                            SpannableStringBuilder text = new SpannableStringBuilder();
                            text.append(msg).append(spannableStringBuilder);
                            if (notCloseSpannableStringBuilderDialog) {
                                dialog.setText(text, () -> {
                                });
                            } else {
                                dialog.setText(text, dialog::dismiss);
                            }
                            dialog.setClose(dialog::dismiss);
                            dialog.show();
                        } else if (massageToUser.length() > 1) {  // НЕ ЮЗАЙ ЭТО
                            DialogData dialog = new DialogData(context);
                            dialog.setTitle(optionTitle);
                            dialog.setText(massageToUser);
                            if (block) {
                                dialog.setDialogIco();
                            }
                            dialog.setClose(dialog::dismiss);
                            dialog.show();
                        }
                        break;
                }
            }

        } catch (Exception e) {
            Globals.writeToMLOG("ERR", "OptionControl.showOptionMassage", "STACK: " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void setIsBlockOption(boolean block) {
        this.block = block;
    }

    public boolean isBlockOption() {
        return block;
    }

    public boolean isBlockOption2() {
        if (block) {
            unlockCodeResultListener.onUnlockCodeFailure();
        } else {
            unlockCodeResultListener.onUnlockCodeSuccess();
        }
        return block;
    }


    public void showUnlockCodeDialogInMainThread(WpDataDB wpDataDB, boolean signal) {
        new UnlockCode().showDialogUnlockCode(context, wpDataDB, optionDB, CODE_DAD_2_AND_OPTION, new Clicks.clickStatusMsg() {
            @Override
            public void onSuccess(String data) {
                setIsBlockOption(false);
                unlockCodeResultListener.onUnlockCodeSuccess();
            }

            @Override
            public void onFailure(String error) {
                setIsBlockOption(signal);
                stringBuilderMsg.append("\n\n").append("Документ проведен не будет!");
                spannableStringBuilder.append(stringBuilderMsg);
                showOptionMassage("");
                unlockCodeResultListener.onUnlockCodeFailure();
            }
        });
    }
}
