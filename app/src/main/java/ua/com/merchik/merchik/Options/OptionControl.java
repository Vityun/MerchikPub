package ua.com.merchik.merchik.Options;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.dialogs.DialogData;

/** 31.05.2022
 *  Класс OptionControl занимается сбором данных и отписанием пользователю информации о проблеммах
 *  которые возникли при обработке опции.
 *
 * */
public class OptionControl <T>{

    public Context context;
    public T document;
    public OptionsDB optionDB;
    public OptionMassageType msgType;
    public Options.NNKMode nnkMode;

    public String massageToUser;    // Для быстрого сообщения   // НЕ ЮЗАЙ БОЛЬШЕ ЕГО
    public StringBuilder stringBuilderMsg = new StringBuilder();
    public SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
    public boolean notCloseSpannableStringBuilderDialog = false;    // Делает так что при клике на текст диалог не будет закрываться
    private boolean block;

    public void showOptionMassage(){
        try {
            if (msgType != null && msgType.type != null){
                switch (msgType.type){
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

                        if (stringBuilderMsg.toString().length() > 1){
                            DialogData dialog = new DialogData(context);
                            dialog.setTitle(optionTitle);
                            dialog.setText(stringBuilderMsg);
                            if (isBlockOption()){
                                dialog.setDialogIco();
                            }
                            dialog.setClose(dialog::dismiss);
                            dialog.show();
                        }else if (spannableStringBuilder.toString().length() > 1){
                            DialogData dialog = new DialogData(context);
                            dialog.setTitle(optionTitle);
                            if (isBlockOption()){
                                dialog.setDialogIco();
                            }
                            if (notCloseSpannableStringBuilderDialog){
                                dialog.setText(spannableStringBuilder, ()->{});
                            }else {
                                dialog.setText(spannableStringBuilder, dialog::dismiss);
                            }
                            dialog.setClose(dialog::dismiss);
                            dialog.show();
                        }else if (massageToUser.length() > 1){  // НЕ ЮЗАЙ ЭТО
                            DialogData dialog = new DialogData(context);
                            dialog.setTitle(optionTitle);
                            dialog.setText(massageToUser);
                            if (isBlockOption()){
                                dialog.setDialogIco();
                            }
                            dialog.setClose(dialog::dismiss);
                            dialog.show();
                        }
                        break;
                }
            }

        }catch (Exception e){
            Globals.writeToMLOG("ERR", "OptionControl.showOptionMassage", "STACK: " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void setIsBlockOption(boolean block){
        this.block = block;
    }

    public boolean isBlockOption(){
        return block;
    }

}
