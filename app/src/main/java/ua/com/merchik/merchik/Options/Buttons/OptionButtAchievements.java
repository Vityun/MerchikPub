package ua.com.merchik.merchik.Options.Buttons;

import static ua.com.merchik.merchik.Globals.userId;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;

public class OptionButtAchievements<T> extends OptionControl {
    public int OPTION_BUTTON_ACHIEVEMENTS_ID = 135159;
    private WpDataDB wpDataDB;
    private final WorkPlan workPlan = new WorkPlan();

    public OptionButtAchievements(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
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
            this.wpDataDB = (WpDataDB) document;
        }
    }

    private void executeOption() {
        try {
            String link = String.format("/mobile.php?mod=images_achieve**act=new_achieve**code_dad2=%sclient_id=%s**addr_id=%s", wpDataDB.getCode_dad2(), wpDataDB.getClient_id(), wpDataDB.getAddr_id());
            AppUsersDB appUser = AppUserRealm.getAppUserById(userId);

            String hash = String.format("%s%s%s", appUser.getUserId(), appUser.getPassword(), "AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3");
            hash = Globals.getSha1Hex(hash);

            String format = String.format("https://merchik.com.ua/sa.php?&u=%s&s=%s&l=%s", userId, hash, link);


            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(format));
            context.startActivity(browserIntent);
        }catch (Exception e){
        }
    }
}
