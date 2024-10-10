package ua.com.merchik.merchik.Options.Buttons;

import static ua.com.merchik.merchik.Globals.userId;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.List;

import kotlin.Pair;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.BonusSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dataLayer.MainRepositoryKt;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public class OptionButtonAddNewFriend<T> extends OptionControl {
    public int OPTION_BUTTON_ADD_NEW_FRIEND_ID = 133382;

    private WpDataDB wpDataDB;

    public OptionButtonAddNewFriend(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;
        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            this.wpDataDB = WpDataRealm.getWpDataRowByDad2Id(((WpDataDB) document).getCode_dad2());
        }
    }

    private void executeOption() {
        DialogData dialog = new DialogData(context);
        dialog.setTitle("Добавление потенциального клиента");
//        dialog.setText("Вы хотите добавить потенциального клиента? \n\n" + additionalText());
        StringBuilder text = new StringBuilder();
        text.append("Пригласить Друга на работу в нашей компании\n" +
                "За sms-приглашение, которое вы отправите кандидату, автоматически начисляются следующие премии:\n");

        List<BonusSDB> bonusList = SQL_DB.bonusDao().getData(null, null, Long.valueOf(optionDB.getOptionId()));
        Pair<String, Float> bonus = MainRepositoryKt.getBonusText(bonusList);

        text.append(bonus.getFirst());

        text.append("\n");

        text.append("Общая сумма премии (при выполнении указанных условий) составит ")
                .append(bonus.getSecond()).append(" грн.\n")
                .append("Обмеження по кількості відправлених запрошень:\n")
                .append("- до 3-х \"СМС-запрошень\" на день\n")
                .append("- до 10-ти \"СМС-запрошень\" на тиждень\n")
                .append("- до 20-ї \"СМС-запрошень\" на місяць\n")
                .append("- якщо ваші \"СМС-запрошення\" не призводять до початку співпраці протягом місяця, преміальні за НОВІ \"СМС-запрошення\" знижуються в 10-ть разів");

        dialog.setText(text);

        dialog.setOk("Да", () -> {
//            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://merchik.com.ua/mobile.php?mod=potential_clients"));
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(createAddNewClientLink()));
            context.startActivity(browserIntent);
        });
        dialog.setClose(dialog::dismiss);
        dialog.show();
    }

    /*
    * Создаю ссылку для того что б пользователь смог перейти в Мобильную Версию Сайта без перелогиниваний
    * и в последствии смог зарегестрировать нового клиента
    *
    *   https://merchik.net/sa.php?&u=USERID&s=PASSWORDHASH&l=/client-reg
    *   https://merchik.net/sa.php?&u=222388&s=4600a1857c7008ab0ebfa8d1a168f26ebe7315b8&l=/client-reg
    *   https://merchik.net/sa.php?&u=222388&s=222388fwwk96662AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3&l=client-reg
    *   https://merchik.net/sa.php?&u=222388&s=222388fwwk96662AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3&l=/potential_clients
    *
    *   https://merchik.net/sa.php?&u=222388&s=222388fwwk96662AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3&l=/merchik.com.ua/mobile.php?mod=potential_clients
    *   https://merchik.net/sa.php?&u=222388&s=222388fwwk96662AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3&l=/merchik.com.ua/mobile.php?mod=/client-reg
        u - ID сотрудника
        s - хэш (описание как считается ниже)
        l - сылка, по которой будет перенаправлен пользователь после авторизации
        Хэш считается как SHA1 от строки (без пробелов между параметрами) (соль для хэша AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3)
        USERID USERPASSWORD AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3
    *
    * !!!
    * в комментарии только просил тебя указать, что если нужно будет перейти на merchik.net  - то
    * сначала нужно будет найти на стороне сервере место редиректа на com.ua
    * !!!
    *
    * */

    public String createAddNewClientLink() {
        String link = "https://merchik.com.ua/mobile.php?mod=potential_clients";
        AppUsersDB appUser = AppUserRealm.getAppUserById(userId);
        if (appUser != null){
            String hash = String.format("%s%s%s", appUser.getUserId(), appUser.getPassword(), "AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3");
            hash = Globals.getSha1Hex(hash);

            String format = String.format("https://merchik.com.ua/sa.php?&u=%s&s=%s&l=/client-reg", userId, hash, link);
            return format;
        }else {
            return link;
        }
    }

    /*Дополнительная подсказка: Потенциальный клиент*/
    public String additionalText() {
        int test = 11000;   // TODO будут мне норм значение передавать "ЗП мерчика"
        String res;

        double requestSend = test * 0.0005;
        double requestRegistered = test * 0.005;
        double presentation = test * 0.01;
        double clientStart = test * 0.1;


        res = String.format("- За регистрацию потенциального клиента (ПК) начисляются следующие премии:\n" +
                "1. При подаче заявки: %s грн.\n" +
                "2. Если и менеджер (после проверки реквизитов) регистрирует этого ПК в нашей базе данных: %s грн.\n" +
                "3. Если для этого ПК проводится презентация: %s грн.\n" +
                "4. Основная премия, при \"запуске\" клиента в работу: %s грн.", requestSend, requestRegistered, presentation, clientStart);

        return res;
    }
}
