package ua.com.merchik.merchik.Options.Buttons;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

import static ua.com.merchik.merchik.Globals.userId;

public class OptionButtonAddNewClient<T> extends OptionControl {
    public int OPTION_BUTTON_ADD_NEW_CLIENT_ID = 133382;

    private WpDataDB wpDataDB;

    public OptionButtonAddNewClient(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
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
        dialog.setText("Вы хотите добавить потенциального клиента? \n\nЗа регистрацию потенциального клиента вы можете получить премию, инструкция находится внизу формы добавления нового клиента.");
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

    private String createAddNewClientLink() {
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
}
