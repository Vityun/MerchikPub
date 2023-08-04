package ua.com.merchik.merchik.Global;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dialogs.DialogData;

/**
 * Код для разблокировки
 * ==========================
 * Автор Пика С.А. 21.06.2023
 * Возвращает код разблокировки (4 Символа) от Хеш МД5, сформированого по определенным условиям
 * date - дата (дата) (в секундах)
 * user - сотрудник (UsersSDB)
 * dad2 - КодДАД2
 * option - опция (элемент справочника "Товары" или число/строка - код опции)
 * mode - режим формирования кода (число) 1 - по Коду ДАД2 и Опции, иначе - по сотруднику и дате
 */
public class UnlockCode {

    public enum UnlockCodeMode {
        CODE_DAD_2_AND_OPTION, DATE_AND_USER
    }

    public String unlockCode(long date, UsersSDB user, long dad2, OptionsDB option, UnlockCodeMode mode) {
        String res = "";

        String salt = "Lfd3naKsjdh3";
        String code = "";

        switch (mode) {
            case CODE_DAD_2_AND_OPTION:
                String dad2Str = "";
                if (dad2 != 0) {
                    dad2Str = String.valueOf(dad2);
                }

                String optionCode = "";
                if (option != null && option.getOptionId() != null && !option.getOptionId().isEmpty()) {
                    optionCode = option.getOptionId();
                }

                code = dad2Str + "-" + optionCode + "-" + salt;
                break;

            case DATE_AND_USER:
                String dateStr = "";
                if (date != 0) {
                    dateStr = Clock.getHumanTimeYYYYMMDD(date);
                }

                String userStr = "";
                if (user != null && user.id != 0) {
                    userStr = String.valueOf(user.id);
                }

                code = userStr + "-" + dateStr + "-" + salt;
                break;
        }

        HashMD5 hashMD5 = new HashMD5();
        res = hashMD5.getMD5fromString(code).substring(0, 4).toLowerCase();

        return res;
    }


    public void showDialogUnlockCode(Context context, WpDataDB wp, OptionsDB option, UnlockCodeMode mode, Clicks.clickStatusMsg click) {
        DialogData dialog = new DialogData(context);
        dialog.setTitle("Внесіть пароль!");
        dialog.setText("Для продовження внесіть пароль: ");
        dialog.setClose(dialog::dismiss);
        dialog.setOperation(DialogData.Operations.TEXT, "", null, () -> {
        });
        dialog.setOkNotClose("Ok", () -> {
            String res = dialog.getOperationResult();

            long date = wp.getDt().getTime() / 1000;
            UsersSDB user = SQL_DB.usersDao().getUserById(wp.getUser_id());
            long dad2 = wp.getCode_dad2();

            Log.e("UnlockCode", "date: " + Clock.getHumanTimeYYYYMMDD(date));
            Log.e("UnlockCode", "user: " + user.id);
            Log.e("UnlockCode", "dad2: " + dad2);
            Log.e("UnlockCode", "option: " + option.getOptionId());

            String unlockCode = new UnlockCode().unlockCode(date, user, dad2, option, mode);

//            String unlockCode = new UnlockCode().unlockCode(date, user, dad2, option, CODE_DAD_2_AND_OPTION);
//            String unlockCode2 = new UnlockCode().unlockCode(date, user, dad2, option, DATE_AND_USER);
//
//            Log.e("UnlockCode", "unlockCode: " + unlockCode);
//            Log.e("UnlockCode", "unlockCode2: " + unlockCode2);

            if (res.equals(unlockCode)) {
                Toast.makeText(context, "Код прийнято", Toast.LENGTH_LONG).show();
                click.onSuccess("");
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Код не вірний!", Toast.LENGTH_LONG).show();
                click.onFailure("");
            }
        });
        dialog.show();
    }

}
