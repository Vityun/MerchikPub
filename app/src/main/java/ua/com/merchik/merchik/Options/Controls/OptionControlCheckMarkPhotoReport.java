package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.Database.Room.VoteSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.StackPhotoRealm;
import ua.com.merchik.merchik.dialogs.DialogFullPhotoR;

public class OptionControlCheckMarkPhotoReport<T> extends OptionControl {
    public int OPTION_CONTROL_CheckMarkPhotoReport_ID = 135595;

    private boolean signal = true;

    private WpDataDB wpDataDB;
    private CustomerSDB customerSDBDocument;
    private UsersSDB usersSDBDocument;
    private AddressSDB addressSDBDocument;

    private Long dateDocument;  // В секундах
    private Long dateFrom = 0L;
    private Long dateTo = 0L;

    private int maxRating = 5;
    private int averageRating;
    private int averageRatingMin = 6;   // минимальная СРЕДНЯЯ оценка, ниже которой, операторы начинают "страдать"
    private int averageRatingMax = 8;   // максимальная СРЕДНЯЯ оценка, выше которой, операторы начинают "страдать"

    public OptionControlCheckMarkPhotoReport(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
            Globals.writeToMLOG("ERROR", "OptionControlCheckMarkPhotoReport/getDocumentVar", "Exception e: " + e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        try {
            List<VoteSDB> votes = SQL_DB.votesDao().getAll(dateFrom, dateTo, maxRating, wpDataDB.getCode_dad2(), wpDataDB.getClient_id(), wpDataDB.getAddr_id(), 0);

            List<VoteSDB> uniqueVotes = votes.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(vote -> vote.photoId, vote -> vote, (existing, replacement) -> existing),
                            map -> new ArrayList<>(map.values())
                    ));

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

            if ((uniqueVotes == null || uniqueVotes.size() == 0) && wpDataDB.getTheme_id() == 95) {
                signal = true;
                spannableStringBuilder.append("Низких оценок по ФотоОтчетам исполнителя ").append(usersSDBDocument.fio).append(" нет.");
            } else if (uniqueVotes.size() == 0 && wpDataDB.getTheme_id() == 95) {
                signal = false;
                spannableStringBuilder.append("Нарушений в оценивании ФО за период с ")
                        .append(Clock.getHumanTimeSecPattern(dateFrom, "dd.MM.yy")).append(" по ")
                        .append(Clock.getHumanTimeSecPattern(dateTo, "dd.MM.yy")).append(" нет.");
            } else if (uniqueVotes.size() == 0) {
                signal = false;
                spannableStringBuilder.append("Низких оценок по ФотоОтчетам исполнителя ").append(usersSDBDocument.fio).append(" нет.");
            } else if (vote == null) {
                signal = false;
                spannableStringBuilder.append("Низких оценок по ФотоОтчетам исполнителя ").append(usersSDBDocument.fio).append(" нет.");
            } else if (wpDataDB.getTheme_id() == 1147) {
                signal = true;
                spannableStringBuilder.append("Обнаружено ").append(String.valueOf(uniqueVotes.size())).append(" нарушение в оценивании ФотоОтчетов (подробности см. в таблице)");
            } else {
                signal = true;
                spannableStringBuilder.append("Обнаружено ").append(String.valueOf(uniqueVotes.size()))
                        .append(" низких оценок по ДетОтчетам от ").append(vote.authorVote).append("\n\n");

                for (VoteSDB item : uniqueVotes){
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

            spannableStringBuilder.append("У Вас ").append(String.valueOf(uniqueVotes.size())).append(" плохих оценок. По фото.");

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlCheckMarkPhotoReport/executeOption", "Exception e: " + e);
        }
    }

    private SpannableStringBuilder createLinkedString(SpannableStringBuilder msg, VoteSDB vote) {
        SpannableStringBuilder res = new SpannableStringBuilder(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Toast.makeText(textView.getContext(), "Ідентифікатор оцінки: " + vote.serverId, Toast.LENGTH_LONG).show();
                try {
                    StackPhotoDB stackPhotoDB = StackPhotoRealm.stackPhotoDBGetPhotoBySiteId(String.valueOf(vote.photoId));
                    DialogFullPhotoR dialog = new DialogFullPhotoR(textView.getContext());
                    dialog.setPhoto(stackPhotoDB);
                    dialog.setComment(vote.comments);
                    dialog.setClose(dialog::dismiss);
                    dialog.show();
                }catch (Exception e){
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
