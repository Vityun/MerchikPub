package ua.com.merchik.merchik.Options.Buttons;

import static ua.com.merchik.merchik.Globals.userId;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

import kotlin.Pair;
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.BonusSDB;
import ua.com.merchik.merchik.data.Database.Room.VacancySDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.dataLayer.MainRepositoryKt;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.features.main.DBViewModels.AdditionalRequirementsDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.VacancySDBViewModel;

public class OptionButtonAddNewFriend<T> extends OptionControl {
    public int OPTION_BUTTON_ADD_NEW_FRIEND_ID = 136100;

    private View view;
    private WpDataDB wpDataDB;

    public OptionButtonAddNewFriend(View view, Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        this.unlockCodeResultListener = unlockCodeResultListener;
        this.view = view;
        getDocumentVar();
        executeOption();
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            this.wpDataDB = WpDataRealm.getWpDataRowByDad2Id(((WpDataDB) document).getCode_dad2());
        }
    }

    private void executeOption() {
        PopupMenu popupMenu = new PopupMenu(this.context, view);

        popupMenu.getMenu().add(1, 101, 1, "Вакансии >");
        popupMenu.getMenu().add(1, 102, 2, "Отправить SMS приглашение >");
        popupMenu.getMenu().add(1, 103, 3, "Инструкции по поиску >");

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case 101:
                    openVacancy();
                    return true;
                case 102:
                    sendSMS();
                    return true;
                case 103:
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private void openVacancy(){
        Intent intent = new Intent(context, FeaturesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("viewModel", VacancySDBViewModel.class.getCanonicalName());
        bundle.putString("contextUI", ContextUI.DEFAULT.toString());
        bundle.putString("title", "Вакансии");
        bundle.putString("subTitle", "Список вакансий");
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private void sendSMS() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(createAddNewClientLink()));
        context.startActivity(browserIntent);
    }

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
