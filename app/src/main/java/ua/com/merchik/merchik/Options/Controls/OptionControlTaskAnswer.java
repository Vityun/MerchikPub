package ua.com.merchik.merchik.Options.Controls;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARActivity;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TARCommentsDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TARCommentsRealm;
import ua.com.merchik.merchik.database.realm.tables.ThemeRealm;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

/**
 * Опция контроля 135329
 * Контроль наличия ответа на Задачу
 */
public class OptionControlTaskAnswer<T> extends OptionControl {

    public int OPTION_CONTROL_TASK_ANSWER_ID = 135329;

    private WpDataDB wpDataDB;
    private String clientId, documentDate;
    private int userId, addressId, taskCount;

    private boolean signal = false;

    public OptionControlTaskAnswer(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;

        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
        Log.e("OptionControlTask", "here");
        if (document instanceof WpDataDB) {
            wpDataDB = (WpDataDB) document;

            Globals.writeToMLOG("INFO", "OptionControlTaskAnswer/getDocumentVar/WpDataDB", "WpDataDB: " + wpDataDB);

            documentDate = Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime()/1000);     //+TODO CHANGE DATE

            clientId = wpDataDB.getClient_id();
            addressId = wpDataDB.getAddr_id();
            userId = wpDataDB.getUser_id();
        }
    }

    private void executeOption() {
        Log.e("OptionControlTask", "here");
        try {
            // todo использовать дату ДОКУМЕНТ-30 и ДОКУМЕНТ-1 день, а не ТЕКУЩИЙ-30 и ТЕКУЩИЙ-1
            // todo [Док-30 -- Док-НачалоРабот]
            long date1 = Clock.getDatePeriodLong(-30) / 1000;
//            long date2 = Clock.getDatePeriodLong(-1) / 1000;
            long date2 = System.currentTimeMillis()/1000;
            if (wpDataDB.getVisit_start_dt() > 0){
                date2 = wpDataDB.getVisit_start_dt();
            }
            List<TasksAndReclamationsSDB> result = new ArrayList<>();

            List<TasksAndReclamationsSDB> tarList;
            // костыляки для конторки любимой
            if (System.currentTimeMillis() > 1664928000000L){
                tarList = SQL_DB.tarDao().getTARForOptionControl(1, addressId, userId, 0, date1, date2);
            }else {
                tarList = SQL_DB.tarDao().getTARForOptionControl150822(1, addressId, clientId, userId, 0, date1, date2);
            }
            Globals.writeToMLOG("INFO", "OptionControlTaskAnswer/executeOption/List<TasksAndReclamationsSDB>", "tarList(" + (tarList != null ? tarList.size() : "null") + "): ");

            // todo Как лучше это оформить?
            if (tarList == null) {
                return;
            }

            // Убираю мусор с данных
            for (TasksAndReclamationsSDB item : tarList) {
                if (item.client == null || item.client.equals("0")) tarList.remove(item);
                if (item.dtRealPost > Clock.dateConvertToLong(documentDate)) tarList.remove(item);

                ThemeDB theme = ThemeRealm.getThemeById(String.valueOf(item.themeId));

                Globals.writeToMLOG("INFO", "OptionControlTaskAnswer/executeOption/for/data", "item: " + item.id);
                Globals.writeToMLOG("INFO", "OptionControlTaskAnswer/executeOption/for/data", "theme: " + theme.getID());

                if (theme.getTp().equals("2")) {
                    if (item.noNeedReply == 1) {
                        String msg = context.getString(R.string.option_control_135329_no_need_reply);
                        // todo создать и заполнить данные для сообщения пользователю
                        // Признак ошибки = 1
                        // Номер задачи

                        massageToUser = msg;
                        spannableStringBuilder.append(msg).append(": ").append(createLinkedString(item.id1c, item.id)).append("\n");

                        result.add(item);

//                    } else if (item.lastAnswer.length() == 0 || item.author.equals(item.lastAnswerUserId)) {
                    } else if (item.lastAnswer.length() == 0 || userId != item.lastAnswerUserId) {
                        String msg = context.getString(R.string.option_control_135329_not_write_tar_comment);
                        // todo создать и заполнить данные для сообщения пользователю
                        // Признак ошибки = 1
                        // Номер задачи

                        massageToUser = msg;
                        spannableStringBuilder.append(msg).append(": ").append(createLinkedString(item.id1c, item.id)).append("\n");

                        result.add(item);

                    } else if (!item.author.equals(item.lastAnswerUserId) && item.lastAnswer.length() < 20) {
                        String msg = context.getString(R.string.option_control_135329_so_short_tar_comment);
                        // todo создать и заполнить данные для сообщения пользователю
                        // Признак ошибки = 1
                        // Номер задачи

                        massageToUser = msg;
                        spannableStringBuilder.append(msg).append(": ").append(createLinkedString(item.id1c, item.id)).append("\n");

                        result.add(item);

                    } else if (theme.need_photo == 1) {
                        String msg = context.getString(R.string.option_control_135329_no_photo);
                        List<TARCommentsDB> commentsRealm = TARCommentsRealm.getTARCommentsToOptionControl(item.id, item.vinovnik);

                        if (commentsRealm != null && commentsRealm.size() == 0) {
                            Globals.writeToMLOG("INFO", "OptionControlTaskAnswer/executeOption/for/data", "commentsRealm: " + commentsRealm.size());

                            massageToUser = msg;
                            spannableStringBuilder.append(msg).append(": ").append(createLinkedString(item.id1c, item.id)).append("\n");

                            result.add(item);
                        }

                    } else if (theme.need_report == 1) {

                        long timeCreateTAR = item.dtRealPost;
                        RealmResults<ReportPrepareDB> rp = ReportPrepareRealm.getRPLastChange(item.client, item.addr, timeCreateTAR);

                        if (rp == null || rp.size() == 0){
                            Globals.writeToMLOG("INFO", "OptionControlTaskAnswer/executeOption/for/data", "rp: " + rp.size());
                            String msg = context.getString(R.string.option_control_135329_no_detailed_report);
                            massageToUser = msg;
                            spannableStringBuilder.append(msg).append(": ").append(createLinkedString(item.id1c, item.id)).append("\n");

                            result.add(item);
                        }
                    } else {
                        // Смотрю в потолок
                    }
                }else {
                    // Смотрю в потолок, бо тема у задачи = 3 и ничего блокироваться не должно
                }
            }

            Globals.writeToMLOG("INFO", "OptionControlTaskAnswer/executeOption/List<TasksAndReclamationsSDB>", "result: " + result.size());

            taskCount = result.size(); // Число задач по которым возникли проблемы.
            if (taskCount > 0) {
                spannableStringBuilder.append("\n").append(context.getString(R.string.option_control_135329_msg_what_must_do));
            }


            // Блокировка
            signal = taskCount > 0;
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionDB != null) {
                    if (signal){
                        optionDB.setIsSignal("1");
                    }else {
                        optionDB.setIsSignal("2");
                    }
                    realm.insertOrUpdate(optionDB);
                }
            });
            setIsBlockOption(signal);    // Установка блокирует ли опция работу приложения или нет


            Globals.writeToMLOG("INFO", "OptionControlTaskAnswer/executeOption/spannableStringBuilder", "spannableStringBuilder: " + spannableStringBuilder);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlTaskAnswer/executeOption/Exception", "Exception e: " + e);
        }
    }

    public int problemTaskCount() {
        return taskCount;
    }

    private SpannableString createLinkedString(String msg, int id) {
        if (msg.equals("")){
            msg = String.valueOf(id);
        }

        SpannableString res = new SpannableString(msg);

        try {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Intent intent = new Intent(context, TARActivity.class);
                    intent.putExtra("TAR_ID", id);
                    context.startActivity(intent);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            int count = msg.length();
            res.setSpan(clickableSpan, 0, count, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlTaskAnswer/createLinkedString/Exception", "Exception e: " + e);
        }
        return res;
    }
}
