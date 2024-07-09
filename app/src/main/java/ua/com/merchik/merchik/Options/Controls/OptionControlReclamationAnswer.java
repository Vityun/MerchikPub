package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.Activities.TaskAndReclamations.TARActivity;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
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

public class OptionControlReclamationAnswer<T> extends OptionControl {

    public int OPTION_CONTROL_RECLAMATION_ANSWER_ID = 135330;

    private WpDataDB wpDataDB;
    private CustomerSDB customerSDB;
    private UsersSDB usersSDB;

    private String clientId, documentDate;
    private int userId, addressId, reclamationCount;

    public boolean signal = false;

    public OptionControlReclamationAnswer(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;

        getDocumentVar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            executeOption();
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            wpDataDB = (WpDataDB) document;
            documentDate = Clock.getHumanTimeYYYYMMDD(wpDataDB.getDt().getTime() / 1000);
            clientId = wpDataDB.getClient_id();
            addressId = wpDataDB.getAddr_id();
            userId = wpDataDB.getUser_id();

            customerSDB = SQL_DB.customerDao().getById(clientId);
            usersSDB = SQL_DB.usersDao().getById(userId);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        Log.e("OptionControlTask", "here");
        try {
            // todo использовать дату ДОКУМЕНТ-30 и ДОКУМЕНТ-1 день, а не ТЕКУЩИЙ-30 и ТЕКУЩИЙ-1
            // todo [Док-30 -- Док-НачалоРабот]
            long date1 = Clock.getDatePeriodLong(-30) / 1000;
//            long date2 = Clock.getDatePeriodLong(-1) / 1000;
            long date2 = System.currentTimeMillis() / 1000;
            if (wpDataDB.getVisit_start_dt() > 0) {
                date2 = wpDataDB.getVisit_start_dt();
            }
            List<TasksAndReclamationsSDB> result = new ArrayList<>();
//            List<TasksAndReclamationsSDB> tarList = SQL_DB.tarDao().getTARForOptionControl(0, addressId, userId, 0, date1, date2);  // 0 - рекламация

            List<TasksAndReclamationsSDB> tarList;
            // костыляки для конторки любимой
            if (System.currentTimeMillis() > 1664928000000L) {
                tarList = SQL_DB.tarDao().getTARForOptionControl(0, addressId, userId, 0, date1, date2);
            } else {
                tarList = SQL_DB.tarDao().getTARForOptionControl150822(0, addressId, clientId, userId, 0, date1, date2);
            }

            Globals.writeToMLOG("INFO", "OptionControlTaskAnswer/executeOption/List<TasksAndReclamationsSDB>", "tarList: " + tarList);

            // todo Как лучше это оформить?
            if (tarList == null) {
                return;
            }

            // Получаю список клиентов изза изменения за 11.10.22. Новая строчка ниже
            List<String> customerIds = new ArrayList<>();
            for (TasksAndReclamationsSDB item : tarList) {
                customerIds.add(item.client);
            }
            List<CustomerSDB> customerSDBList = SQL_DB.customerDao().getByIds(customerIds);

            // Убираю мусор с данных
            for (TasksAndReclamationsSDB item : tarList) {
                if (item.client == null || item.client.equals("0")) /*tarList.remove(item)*/
                    continue;
                if (item.dtRealPost > Clock.dateConvertToLong(documentDate)) /*tarList.remove(item)*/
                    continue;

                // изменения за 11.10.22
                // Гемор изза того что я не умею джойнить разные таблички между собой. Или мне впадлу джойнить как я умею.
                CustomerSDB currentCustomer = null;
                if (customerSDBList.stream().filter(listItem -> listItem.id.equals(item.client)).findFirst().orElse(null) != null) {
                    currentCustomer = customerSDBList.stream().filter(listItem -> listItem.id.equals(item.client)).findFirst().get();
                }
                if (currentCustomer != null && currentCustomer.reclReplyMode == 1 && !customerSDB.id.equals(currentCustomer.id)) /*tarList.remove(item)*/
                    continue;    // То самое изменение
                // конец изменений за 11.10.22

                ThemeDB theme = ThemeRealm.getThemeById(String.valueOf(item.themeId));

                if (theme.getTp().equals("2")) {
                    if (item.noNeedReply == 1) {
                        String msg = context.getString(R.string.option_control_135330_no_need_reply);
                        // todo создать и заполнить данные для сообщения пользователю
                        // Признак ошибки = 1
                        // Номер задачи

                        massageToUser = msg;
                        spannableStringBuilder.append(msg).append(": ").append(createLinkedString(item.id1c, item.id)).append("\n");

                        result.add(item);

                    } else if (item.lastAnswer.length() == 0 || item.author.equals(item.lastAnswerUserId)) {
                        String msg = context.getString(R.string.option_control_135330_not_write_tar_comment);
                        // todo создать и заполнить данные для сообщения пользователю
                        // Признак ошибки = 1
                        // Номер задачи

                        massageToUser = msg;
                        spannableStringBuilder.append(msg).append(": ").append(createLinkedString(item.id1c, item.id)).append("\n");

                        result.add(item);

                    } else if (!item.author.equals(item.lastAnswerUserId) && item.lastAnswer.length() < 20) {
                        String msg = context.getString(R.string.option_control_135330_so_short_tar_comment);
                        // todo создать и заполнить данные для сообщения пользователю
                        // Признак ошибки = 1
                        // Номер задачи

                        massageToUser = msg;
                        spannableStringBuilder.append(msg).append(": ").append(createLinkedString(item.id1c, item.id)).append("\n");

                        result.add(item);

                    } else if (theme.need_photo == 1) {
                        String msg = context.getString(R.string.option_control_135330_no_photo);
                        // todo Это проверка ТЕМЫ ЗАДАЧИ на обязательную фото. Не знаю откуда тему брать и что дальше с ней делать
                        // Признак ошибки = 1
                        // Номер задачи

                        String comm = "";

                        List<TARCommentsDB> commentsRealm = null;

                        if (System.currentTimeMillis() < 1675987200000L) {
                            commentsRealm = TARCommentsRealm.getTARCommentsToOptionControl(item.id, item.vinovnik, 1);
                        } else {
                            commentsRealm = TARCommentsRealm.getTARCommentsToOptionControl(item.id, item.vinovnik, null);
                            if (commentsRealm != null) {
                                for (TARCommentsDB tarCommentItem : commentsRealm) {
                                    if (!checkHavePhoto(tarCommentItem)) {
                                        comm = tarCommentItem.comment;
                                        break;
                                    }
                                }
                            }
                        }



                        if (!comm.equals("")) {
                            spannableStringBuilder.append("Для даної теми ви повинні додати ФотоЗвіт до коментаря: <")
                                    .append(comm).append("> (").append(createLinkedString(item.id1c, item.id)).append(" от ").append(Clock.getHumanTimeSecPattern(item.dtRealPost / 1000, "dd-MM")).append(")").append("\n");

                            result.add(item);
                        } else if (commentsRealm != null && commentsRealm.size() == 0) {
                            massageToUser = msg;
                            spannableStringBuilder.append(msg).append(": ").append(createLinkedString(item.id1c, item.id)).append("\n");

                            result.add(item);
                        } else {
                            Globals.writeToMLOG("INFO", "OptionControlTaskAnswer/executeOption/for/data/need_photo",
                                    "Не смог найти комменты в БД комментов по item.id: " + item.id + ", item.vinovnik: " + item.vinovnik);
                        }

                    } else if (theme.need_report == 1) {

                        long timeCreateTAR = item.dtRealPost;
                        List<ReportPrepareDB> rp = ReportPrepareRealm.getRPLastChange(item.client, String.valueOf(item.addr), timeCreateTAR);

                        if (rp == null || rp.size() == 0) {
                            String msg = context.getString(R.string.option_control_135330_no_detailed_report);
                            massageToUser = msg;
                            spannableStringBuilder.append(msg).append(": ").append(createLinkedString(item.id1c, item.id)).append("\n");

                            result.add(item);
                        }
                    } else {
                        // Смотрю в потолок
                    }
                } else {
                    // Смотрю в потолок, бо тема у задачи = 3 и ничего блокироваться не должно
                }
            }

            Globals.writeToMLOG("INFO", "OptionControlTaskAnswer/executeOption/List<TasksAndReclamationsSDB>", "result: " + result);

            reclamationCount = result.size(); // Число задач по которым возникли проблемы.
//            if (reclamationCount > 0) {
//                spannableStringBuilder.append("\n").append(context.getString(R.string.option_control_135330_msg_what_must_do));
//            }
            if (tarList.size() == 0) {
                spannableStringBuilder.append("Активных рекламаций по клиенту(ам) нет.");
                signal = false;
            } else if (result.size() == 0) {
                spannableStringBuilder.append("Обнаружено ").append(String.valueOf(result.size())).append(" активных рекламаций с ответами. Замечаний нет.");
                signal = false;
            } else if (usersSDB.reportDate20 == null && usersSDB.reportCount < 20) {
                spannableStringBuilder.append("Исполнитель ").append(usersSDB.fio).append(" еще не провел своего 20-го отчета.");
                signal = false;
            } else {
                spannableStringBuilder.append("\n").append("Отсутствует ответ на ").append(String.valueOf(result.size())).append(" активных рекламаций. Вы должны сперва исправить " +
                        "замечания, затем написать ответ на указанные рекламации, а потом проводить данный документ!");
                signal = true;
            }


            // Блокировка
//            signal = reclamationCount > 0;
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionDB != null) {
                    if (signal) {
                        optionDB.setIsSignal("1");
                    } else {
                        optionDB.setIsSignal("2");
                    }
                    realm.insertOrUpdate(optionDB);
                }
            });

            setIsBlockOption(signal);    // Установка блокирует ли опция работу приложения или нет
            checkUnlockCode(optionDB);
            Globals.writeToMLOG("INFO", "OptionControlTaskAnswer/executeOption/spannableStringBuilder", "spannableStringBuilder: " + spannableStringBuilder);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlTaskAnswer/executeOption/Exception", "Exception e: " + e);
        }

    }

    public int problemReclamationCount() {
        return reclamationCount;
    }

    private SpannableString createLinkedString(String msg, int id) {
        if (msg.equals("")) {
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

    private boolean checkHavePhoto(TARCommentsDB comment) {
        boolean res = false;

        if (comment.photo != null && !comment.photo.equals("")) {
            res = true;
        }

        if (comment.photo_hash != null && !comment.photo_hash.equals("")) {
            res = true;
        }

        return res;
    }
}
