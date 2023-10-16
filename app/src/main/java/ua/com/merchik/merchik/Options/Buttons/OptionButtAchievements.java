package ua.com.merchik.merchik.Options.Buttons;

import static ua.com.merchik.merchik.Globals.userId;
import static ua.com.merchik.merchik.toolbar_menus.internetStatus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.dialogs.DialogData;

public class OptionButtAchievements<T> extends OptionControl {
    public int OPTION_BUTTON_ACHIEVEMENTS_ID = 135159;
    private WpDataDB wpDataDB;
    private final WorkPlan workPlan = new WorkPlan();

    public OptionButtAchievements(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
        Log.e("OptionControlTask", "here");
        if (document instanceof WpDataDB) {
            this.wpDataDB = (WpDataDB) document;
        }
    }

    private void executeOption() {
        try {
            if (internetStatus == 1){
                String dateFrom = Clock.getHumanTimeSecPattern(Clock.getDatePeriodLong(wpDataDB.getDt().getTime(), -31)/1000, "yyyy-MM-dd");
                String dateTo = Clock.getHumanTimeSecPattern(Clock.getDatePeriodLong(wpDataDB.getDt().getTime(), +2)/1000, "yyyy-MM-dd");

                String link = String.format("/mobile.php?mod=images_achieve**act=list_achieve**code_dad2_create=%s**client_id=%s**addr_id=%s**date_from=%s**date_to=%s", wpDataDB.getCode_dad2(), wpDataDB.getClient_id(), wpDataDB.getAddr_id(), dateFrom, dateTo);
                AppUsersDB appUser = AppUserRealm.getAppUserById(userId);

                String hash = String.format("%s%s%s", appUser.getUserId(), appUser.getPassword(), "AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3");
                hash = Globals.getSha1Hex(hash);

                String format = String.format("https://merchik.com.ua/sa.php?&u=%s&s=%s&l=%s", userId, hash, link);

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(format));
                context.startActivity(browserIntent);
            }else {
                DialogData dialogData = new DialogData(context);
                dialogData.setTitle("Помилка!");
                dialogData.setText("Обнаружена проблема с сетью, проверьте интернет соединение и повторите попытку позже.");
                dialogData.setClose(dialogData::dismiss);
                dialogData.show();
                Globals.writeToMLOG("RESP", "OptionButtAchievements/executeOption", "Обнаружена проблема с сетью, проверьте интернет соединение и повторите попытку позже.");
            }
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "OptionButtAchievements/executeOption", "Exception e: " + e);
        }
    }
}
