package ua.com.merchik.merchik.Global;

import static ua.com.merchik.merchik.Globals.HELPDESK_PHONE_NUMBER;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Collections;
import java.util.Date;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.LogRealm;
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

        // Pika подготовка данных для работы с кодом разблокировки
        int tema_id = 1285; // код темы для лога для сохранения кода разблокировки
        Date wpDate = wp.getDt(); // дата работ из плана работ
        long date = wpDate.getTime() / 1000; // дата работ из плана работ в Юниксе
        long dad2 = wp.getCode_dad2(); // код ДАД2 из плана работ
        String dad2str = String.valueOf(dad2); // код ДАД2 из плана работ в виде строки
        int addr_id = wp.getAddr_id(); // код адреса из плана работ
        String client_id = wp.getClient_id(); // код клиента из плана работ
        int user_id = wp.getUser_id(); // код сотрудника из плана работ
        UsersSDB user = SQL_DB.usersDao().getUserById(user_id); // сотрудник (объект класса)
        String opt_id = option.getOptionId(); // код опции в виде строки
        String s = "";

        // Pika Формирую строку для записи в поле "КодОбъекта" лога
        int len = opt_id.length();
        String kodObstr = "1" + opt_id.substring(len - 3, len) + dad2str.substring(1, 5) + dad2str.substring(6, 7) + dad2str.substring(8, 13) + dad2str.substring(14, 19);
        long kodOb = Long.valueOf(kodObstr);

        // Pika Получаю из таблицы лога приложения инфо или вносился код уже сегодня для данных параметров
        LogDB logDBRec = LogRealm.getLogDbByKodOb(kodOb);
        if (logDBRec != null) {
            s = logDBRec.getComments();
        }
        String passAlreadyExists = ""; // строка с кодом разблокировки 4 симв
        if (!s.isEmpty()) {
            len = s.length();
            passAlreadyExists = s.substring(len - 4, len);
        }

        // Pika Если код разблокировки еще не вносился, то вызываю диалог его внесения и сохраняю потом в лог приложения
        // а если вносился, то пропускаю диалог внесения кода разблокировки
        if (passAlreadyExists == "") {

            DialogData dialog = new DialogData(context);
            dialog.setTitle("Внесіть код розблокування!");
            String string = "Для розблокування внесіть код. Цей код Ви можете отримати у всого керівника. Якщо зв'язку з керівником нема - можна звернутися до керівника відділку.";

            SpannableString spannableString = new SpannableString(string);

            ClickableSpan clickableSpan1 = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
//                    Toast.makeText(widget.getContext(), "Керівник", Toast.LENGTH_SHORT).show();
                    UsersSDB usersSDB = SQL_DB.usersDao().getById(wp.getSuper_id());
                    Globals.telephoneCall(widget.getContext(), usersSDB.tel2);
                }
            };

            ClickableSpan clickableSpan2 = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
//                    Toast.makeText(widget.getContext(), "Керівник відділку", Toast.LENGTH_SHORT).show();
                    UsersSDB usersSDB = SQL_DB.usersDao().getById(wp.getNop_id());
                    Globals.telephoneCall(widget.getContext(), usersSDB.tel2);
                }
            };

            int start1 = string.indexOf("керівника");
            int end1 = start1 + "керівника".length();
            int start2 = string.indexOf("керівника відділку");
            int end2 = start2 + "керівника відділку".length();

            spannableString.setSpan(clickableSpan1, start1, end1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(clickableSpan2, start2, end2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);




            dialog.setText(spannableString);
            dialog.setClose(() -> {
                click.onFailure("");
                dialog.dismiss();
            });
            dialog.setOperation(DialogData.Operations.TEXT, "", null, () -> {
            });
            dialog.setOkNotClose("Ok", () -> {
                String res = dialog.getOperationResult();

//            long date = wp.getDt().getTime() / 1000;
//            UsersSDB user = SQL_DB.usersDao().getUserById(wp.getUser_id());
//            long dad2 = wp.getCode_dad2();

                Log.e("UnlockCode", "date: " + Clock.getHumanTimeYYYYMMDD(date));
                Log.e("UnlockCode", "user: " + user.id);
                Log.e("UnlockCode", "dad2: " + dad2);
                Log.e("UnlockCode", "option: " + option.getOptionId());

                String unlockCode = new UnlockCode().unlockCode(date, user, dad2, option, mode);

//            String unlockCode = new UnlockCode().unlockCode(date, user, dad2, option, CODE_DAD_2_AND_OPTION);
//            String unlockCode2 = new UnlockCode().unlockCode(date, user, dad2, option, DATE_AND_USER);
//
                Log.e("UnlockCode", "unlockCode: " + unlockCode);
//            Log.e("UnlockCode", "unlockCode2: " + unlockCode2);

                if (res.equals(unlockCode)) {
                    // Pika сохраняю код в лог приложения
                    RealmManager.setRowToLog(Collections.singletonList(
                            new LogDB(
                                    RealmManager.getLastIdLogDB() + 1,
                                    System.currentTimeMillis() / 1000,
                                    "використання коду розблокування " + res,
                                    tema_id,
                                    client_id,
                                    addr_id,
                                    kodOb,
                                    user_id,
                                    null,
                                    Globals.session,
                                    String.valueOf(wpDate))));

                    Toast.makeText(context, "Код прийнято", Toast.LENGTH_LONG).show();
                    click.onSuccess("");
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Код не вірний!", Toast.LENGTH_LONG).show();
                    click.onFailure("");
                    dialog.dismiss();
                }
            });
            dialog.show();
        } else {
            // Pika Если в логе есть за сегодня этот код разблокировки, то тут делаю вид, что успешно внесен правильный код разблокировки
            click.onSuccess("");

        }
    }

    public Long codeODAD(OptionsDB optionsDB) {
        Long res = null;

        String dad2str = optionsDB.getCodeDad2();
        String optId = optionsDB.getOptionId();
        int len = optionsDB.getOptionId().length();

        String kodObstr = "1" + optId.substring(len - 3, len) + dad2str.substring(1, 5) + dad2str.substring(6, 7) + dad2str.substring(8, 13) + dad2str.substring(14, 19);
        res = Long.valueOf(kodObstr);

        return res;
    }

    private SpannableString createLinkedString(String msg) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                try {
                    Globals.telephoneCall(textView.getContext(), HELPDESK_PHONE_NUMBER);
                } catch (Exception e) {

                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        res.setSpan(clickableSpan, msg.length() - 10, msg.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return res;
    }

}
