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
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.LogRealm;
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

/**
 * 31.05.2022
 * Класс OptionControl занимается сбором данных и отписанием пользователю информации о проблеммах
 * которые возникли при обработке опции.
 */
public class OptionControl<T> {

    public Context context;
    private DialogData dialog;
    public T document;
    public OptionsDB optionDB;
    public WpDataDB wpDataDB;
    public OptionMassageType msgType;
    public Options.NNKMode nnkMode;

    public Clicks.clickStatusMsg clickStatusMsg;

    public UnlockCodeResultListener unlockCodeResultListener;

    public interface UnlockCodeResultListener {
        void onUnlockCodeSuccess();

        void onUnlockCodeFailure();
    }

    public String massageToUser = "";    // Для быстрого сообщения   // НЕ ЮЗАЙ БОЛЬШЕ ЕГО
    public StringBuilder stringBuilderMsg = new StringBuilder();
    public SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
    public boolean notCloseSpannableStringBuilderDialog = false;    // Делает так что при клике на текст диалог не будет закрываться
    private boolean block;

    public void showOptionMassage(String msg) {
        try {
            dialog = new DialogData(context);
            unlockCode();
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

                        String optionTitle = "Опция: (" + optionDB.getOptionControlId() + ")\n" + optionDB.getOptionControlTxt();

                        if (stringBuilderMsg.toString().length() > 1) {
                            dialog.setTitle(optionTitle);
                            dialog.setText(msg + stringBuilderMsg);
                            if (block) {
                                dialog.setDialogIco();
                            }
                            dialog.setClose(dialog::dismiss);
                            dialog.show();
                        } else if (spannableStringBuilder.toString().length() > 1) {
//                            DialogData dialog = new DialogData(context);
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
//                            DialogData dialog = new DialogData(context);
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

    public void unlockCode() {
        dialog.setCancel("Отримати код розблокування", () -> {
//            Toast.makeText(dialog.context, "click", Toast.LENGTH_LONG).show();
            WpDataDB wpDataDB1 = WpDataRealm.getWpDataRowByDad2Id(Long.parseLong(optionDB.getCodeDad2()));
            showUnlockCodeDialogInMainThread(wpDataDB1, isBlockOption());
        });
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
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionDB != null) {
                        optionDB.setIsSignal("2");
                        realm.insertOrUpdate(optionDB);
                    }
                });
                dialog.dismiss();
                Toast.makeText(context, "Код прийнято", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String error) {
                setIsBlockOption(signal);
                unlockCodeResultListener.onUnlockCodeFailure();
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionDB != null) {
                        optionDB.setBlockPns("1");
                        optionDB.setIsSignal("1");
                        realm.insertOrUpdate(optionDB);
                    }
                });
                Toast.makeText(context, "Код розблокування НЕ прийнято", Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean checkUnlockCode(OptionsDB optionDB) {
        try {
            // 12.08.24. Виктор. Для будущих поколений.
            // Обращайте внимание - сохраняет ли опция контроля сигнал. Был случай когда не было
            // сохранения/обновления сигнала в БД изза чего мерчер НЕ БЛОКИРОВАЛО то что должно было блокировать.
            if (optionDB != null && optionDB.getIsSignal().equals("1") && optionDB.getBlockPns().equals("1")) {
                if (nnkMode.equals(Options.NNKMode.CHECK) || nnkMode.equals(Options.NNKMode.CHECK_CLICK)){
                    optionDB = OptionsRealm.getOption(optionDB.getCodeDad2(), optionDB.getOptionControlId());
                }

                Long codeODAD = new UnlockCode().codeODAD(optionDB);
                int themeCode = 1285;

                if (codeODAD != null) {
                    LogDB log = LogRealm.getLogByODADandTheme(codeODAD, themeCode);
                    if (log != null) {
                        stringBuilderMsg.append("\n\nАле виконавцю видано код розблокування!");
                        setIsBlockOption(false);
                        OptionsDB finalOptionDB = optionDB;
                        RealmManager.INSTANCE.executeTransaction(realm -> {
                            finalOptionDB.setIsSignal("2");
                            finalOptionDB.setBlockPns("0");
                            realm.insertOrUpdate(finalOptionDB);
                        });
                        return true;
                    }
                }
                return false;
            }else {
                setIsBlockOption(false);
                return true;
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControl/checkUnlockCode", "Exception e: " + e);
            return false;
        }
    }
}
