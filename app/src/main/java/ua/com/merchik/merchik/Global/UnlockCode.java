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
import java.util.LinkedHashSet;
import java.util.Set;

import io.realm.Realm;

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

    private static final int UNLOCK_USED_THEME = 1285;

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
            String string = String.format("Для розблокування опції %s (%s) внесіть код. Цей код Ви можете отримати у свого керівника. Якщо зв'язку з керівником нема - можна звернутися до керівника відділку.",
                    option.getOptionControlTxt(), option.getOptionControlId());


            SpannableString spannableString = new SpannableString(string);

            ClickableSpan clickableSpan1 = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    UsersSDB usersSDB = SQL_DB.usersDao().getById(wp.getSuper_id());
                    String tel = usersSDB.tel;
                    if (usersSDB.tel2Corp == 1) tel = usersSDB.tel2;
                    Globals.telephoneCall(widget.getContext(), tel);
                }
            };

            ClickableSpan clickableSpan2 = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    UsersSDB usersSDB = SQL_DB.usersDao().getById(wp.getNop_id());
                    String tel = usersSDB.tel2;
//                    if (usersSDB.tel2Corp == 1) tel = usersSDB.tel2;
                    Globals.telephoneCall(widget.getContext(), tel);
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

                if (res != null) {
//            long date = wp.getDt().getTime() / 1000;
//            UsersSDB user = SQL_DB.usersDao().getUserById(wp.getUser_id());
//            long dad2 = wp.getCode_dad2();

                    Log.e("UnlockCode", "date: " + Clock.getHumanTimeYYYYMMDD(date));
                    Log.e("UnlockCode", "user: " + user_id);
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
                                        String.valueOf(wpDate),
                                        null,
                                        null)));

                        applyUnlockSignals(option);
                        Toast.makeText(context, "Код прийнято", Toast.LENGTH_LONG).show();
                        click.onSuccess("");
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "Код не вірний!", Toast.LENGTH_LONG).show();
                        click.onFailure("");
                        dialog.dismiss();
                    }
                }
            });
            dialog.show();
        } else {
            // Pika Если в логе есть за сегодня этот код разблокировки, то тут делаю вид, что успешно внесен правильный код разблокировки
            applyUnlockSignals(option);
            click.onSuccess("");

        }
    }

    public Long codeODAD(OptionsDB optionsDB) {
        return optionsDB == null ? null
                : OptionUnlockPolicy.objectId(optionsDB.getCodeDad2(), optionsDB.getOptionId());
    }

    private String getControlId(OptionsDB option) {
        return option == null ? null : OptionUnlockPolicy.controlId(
                option.getOptionId(), option.getOptionControlId(), option.getOptionGroup());
    }

    private Set<String> getUnlockOptionIds(OptionsDB option) {
        Set<String> ids = new LinkedHashSet<>();
        String controlId = getControlId(option);
        if (controlId == null || option.getCodeDad2() == null) return ids;
        ids.add(controlId);
        if (OptionUnlockPolicy.BUTTON_GROUP.equals(option.getOptionGroup())) ids.add(option.getOptionId());
        for (OptionsDB button : RealmManager.INSTANCE.where(OptionsDB.class)
                .equalTo("codeDad2", option.getCodeDad2())
                .equalTo("optionGroup", OptionUnlockPolicy.BUTTON_GROUP)
                .equalTo("optionControlId", controlId).findAll()) {
            ids.add(button.getOptionId());
        }
        return ids;
    }

    public boolean applyStoredUnlockCode(OptionsDB source) {
        String controlId = getControlId(source);
        if (controlId == null || source.getCodeDad2() == null) return false;
        String dad2 = source.getCodeDad2();
        Long matchedObjectId = null;
        for (String id : getUnlockOptionIds(source)) {
            Long objectId = OptionUnlockPolicy.objectId(dad2, id);
            if (objectId != null && LogRealm.getLogByODADandTheme(objectId, UNLOCK_USED_THEME) != null) {
                matchedObjectId = objectId;
                break;
            }
        }
        if (matchedObjectId == null) return false;

        applyUnlockSignals(source);
        return true;
    }

    // Only update signals after a code is accepted; the LogDB key belongs to the original option.
    private void applyUnlockSignals(OptionsDB source) {
        String controlId = getControlId(source);
        if (controlId == null || source.getCodeDad2() == null) return;
        String dad2 = source.getCodeDad2();
        int[] changed = {0};
        Realm.Transaction updateSignals = realm -> {
            for (OptionsDB row : realm.where(OptionsDB.class).equalTo("codeDad2", dad2)
                    .beginGroup().equalTo("optionId", controlId)
                    .or().beginGroup().equalTo("optionGroup", OptionUnlockPolicy.BUTTON_GROUP)
                    .equalTo("optionControlId", controlId).endGroup().endGroup().findAll()) {
                if (OptionUnlockPolicy.isRelated(dad2, controlId, row.getCodeDad2(),
                        row.getOptionId(), row.getOptionControlId(), row.getOptionGroup())) {
                    if (!"2".equals(row.getIsSignal())) changed[0]++;
                    row.setIsSignal("2");
                }
            }
            // The UI often holds a detached copy. Do not upsert that stale object in full.
            source.setIsSignal("2");
        };
        if (RealmManager.INSTANCE.isInTransaction()) updateSignals.execute(RealmManager.INSTANCE);
        else RealmManager.INSTANCE.executeTransaction(updateSignals);

        if (changed[0] > 0) {
            Globals.writeToMLOG("INFO", "OptionControl/unlockApplied",
                    "dad2=" + dad2 + ", sourceOption=" + source.getOptionId()
                            + ", controlOption=" + controlId + ", updated=" + changed[0]);
        }
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
