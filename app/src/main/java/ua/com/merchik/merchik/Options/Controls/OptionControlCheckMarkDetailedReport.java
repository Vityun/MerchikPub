package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.stream.Collectors;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.Database.Room.VoteSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.dialogs.DialogData;


/**
 * 12.06.23.
 * Выполняется проверка УРОВНЯ ОЦЕНКИ ДетОтчета по данному посещению. Для начисления снижения исполнителю, если есть низкие оценки по его ДетОтчету
 * Кроме данной функции есть еще проверка НАЛИЧИЯ ОЦЕНКИ ДетОтчетов исполнителем. ПровНаличОценДетОтч
 * Задумана для ОС так, чтобы старший не мог провести свой ОС не ОЦЕНИВ ДетОтчеты исполнителя. Но, можно применять и в других случаях
 */
public class OptionControlCheckMarkDetailedReport<T> extends OptionControl {
    public int OPTION_CONTROL_CheckMarkDetailedReport_ID = 135708;

    private boolean signal = true;

    private WpDataDB wpDataDB;
    private CustomerSDB customerSDBDocument;
    private UsersSDB usersSDBDocument;
    private AddressSDB addressSDBDocument;

    private Long dateDocument;  // В секундах
    private Long dateFrom = 0L;
    private Long dateTo = 0L;

    private int averageRating;
    private int maxRating = 5;
    private int averageRatingMin = 6;   // минимальная СРЕДНЯЯ оценка, ниже которой, операторы начинают "страдать"
    private int averageRatingMax = 8;   // максимальная СРЕДНЯЯ оценка, выше которой, операторы начинают "страдать"

    public OptionControlCheckMarkDetailedReport(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
        try {
            if (document instanceof WpDataDB) {
                this.wpDataDB = (WpDataDB) document;

                customerSDBDocument = SQL_DB.customerDao().getById(wpDataDB.getClient_id());
                usersSDBDocument = SQL_DB.usersDao().getById(wpDataDB.getUser_id());
                addressSDBDocument = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
                dateDocument = wpDataDB.getDt().getTime() / 1000;

                dateFrom = Clock.getDatePeriodLong(dateDocument * 1000, -2) / 1000;
                dateTo = Clock.getDatePeriodLong(dateDocument * 1000, 7) / 1000;
            }

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlCheckMarkDetailedReport/getDocumentVar", "Exception e: " + e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        try {
//            List<VoteSDB> votes = SQL_DB.votesDao().getAll(dateFrom, dateTo, 5, wpDataDB.getCode_dad2());   // ДатаС, ДатаПо, Оценка < 5, dad2

            List<VoteSDB> votes = SQL_DB.votesDao().getAll(dateFrom, dateTo, maxRating, wpDataDB.getCode_dad2(), wpDataDB.getClient_id(), wpDataDB.getAddr_id(), 1);

            List<VoteSDB> uniqueVotes = votes.stream()
                    .filter(vote -> vote.photoId == null)
                    .collect(Collectors.toList());

            VoteSDB vote = null;
            if (uniqueVotes != null && uniqueVotes.size() > 0) {
                vote = uniqueVotes.get(0);
                vote.error = 1;

                if (wpDataDB.getTheme_id() == 95) {
                    vote.note = "Вы нашли " + uniqueVotes.size() + " нарушений в ДетОтчете " + wpDataDB.getDoc_num_otchet() + ". За это Вам положена премия.";
                } else {
                    UsersSDB userScore = SQL_DB.usersDao().getUserById(vote.voterId);
                    vote.authorVote = userScore.fio;
                    vote.note = "Вы получили низкую оценку " + vote.score + " " + Clock.getHumanTimeSecPattern(vote.dt, "dd.MM.yy") +
                            " по своему ДетОтчету от " + userScore.fio + " (" + vote.comments + ")";
                }
            }

            if (uniqueVotes == null) {
                signal = false;
                spannableStringBuilder.append("Не могу определить документ для проверки оценок Дет.Отчетов");
            } else if (vote == null) {
                signal = false;
                spannableStringBuilder.append("Низких оценок по ДетОтчету нет.");
            } else {
                signal = true;
                spannableStringBuilder.append("Обнаружено ").append(String.valueOf(uniqueVotes.size()))
                        .append(" низких оценок по ДетОтчетам от ").append(vote.authorVote).append("\n\n");

                for (VoteSDB item : uniqueVotes) {
                    UsersSDB userScore = SQL_DB.usersDao().getUserById(item.voterId);
                    SpannableStringBuilder link = new SpannableStringBuilder();
                    link.append("(").append(String.valueOf(item.id)).append(") ").append(item.comments);
                    spannableStringBuilder.append(createLinkedString(link, item)).append("\n\n");
                }
            }

            // Сохранение
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

            if (signal) {
                if (optionDB.getBlockPns().equals("1")) {
                    setIsBlockOption(signal);
                    spannableStringBuilder.append("\n\n").append("Документ проведен не будет!");
                } else {
                    spannableStringBuilder.append("\n\n").append("Вы можете получить Премиальные БОЛЬШЕ, если будете делать Достижения.");
                }
            }
            checkUnlockCode(optionDB);

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlCheckMarkDetailedReport/executeOption", "Exception e: " + e);
        }
    }


    private SpannableStringBuilder createLinkedString(SpannableStringBuilder msg, VoteSDB vote) {
        SpannableStringBuilder res = new SpannableStringBuilder(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Toast.makeText(textView.getContext(), "Ідентифікатор оцінки: " + vote.serverId, Toast.LENGTH_LONG).show();

                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(wpDataDB.getAddr_txt()).append("\n").append(wpDataDB.getClient_txt()).append("\n");
                    DialogData dialog = new DialogData(textView.getContext());
                    dialog.setTitle(context.getString(R.string.open_visit) + "?");
                    dialog.setText(stringBuilder);
                    dialog.setOk(null, () -> {
                        Intent intent = new Intent(textView.getContext(), DetailedReportActivity.class);
                        intent.putExtra("WpDataDB_ID", wpDataDB.getId());
                        textView.getContext().startActivity(intent);
                    });
                    dialog.setClose(dialog::dismiss);
                    dialog.show();

                } catch (Exception e) {
                    Globals.writeToMLOG("ERROR", "OptionControlCheckMarkPhotoReport/createLinkedString/onClick", "Exception e: " + e);
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        res.setSpan(clickableSpan, 0, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return res;
    }


}
