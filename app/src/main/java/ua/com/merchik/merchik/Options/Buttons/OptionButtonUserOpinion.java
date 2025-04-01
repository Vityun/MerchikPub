package ua.com.merchik.merchik.Options.Buttons;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.NEED_UPDATE_UI_REQUEST;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.realm.Realm;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.OpinionDataHolder;
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.Translate;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.dataLayer.ModeUI;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.features.main.DBViewModels.OpinionSDBViewModel;

public class OptionButtonUserOpinion<T> extends OptionControl {
    public int OPTION_BUTTON_ADD_OPINION = 168598;

    private int opinionID;
    private String opinionName;
    private OpinionDataHolder opinionDataHolder;
    public static Clicks.OnUpdateUI onUpdateUI;

    public OptionButtonUserOpinion(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
            this.wpDataDB = (WpDataDB) document;
        }
        opinionDataHolder = OpinionDataHolder.Companion.instance();

    }

    private void executeOption() {

        opinionDataHolder.init();

        Intent intent = new Intent(context, FeaturesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("viewModel", OpinionSDBViewModel.class.getCanonicalName());
        bundle.putString("contextUI", ContextUI.ADD_OPINION_FROM_DETAILED_REPORT.toString());
        bundle.putString("modeUI", ModeUI.ONE_SELECT.toString());
        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("opinionID", Integer.parseInt(wpDataDB.getUser_opinion_id()));
        dataJson.addProperty("themeID", wpDataDB.getTheme_id());
        bundle.putString("dataJson", new Gson().toJson(dataJson));

        bundle.putString("title", Translate.translationText(8038, "Думка стосовно вiдвiдування"));
        bundle.putString(
                "subTitle",
                Translate.translationText(8039, "Виберіть думку, яку Ви бажаєте залишити про цей візит")
        );
        intent.putExtras(bundle);

        if (context instanceof Activity)
            ActivityCompat.startActivityForResult((Activity) context, intent, NEED_UPDATE_UI_REQUEST, null);
         else
            context.startActivity(intent);

        saveOpinionResult();

    }

    private void saveOpinionResult(){
        onUpdateUI = () -> {
            if (opinionDataHolder.getOpinionID() != null
                    && opinionDataHolder.getOpinionName() != null) {

                opinionID = opinionDataHolder.getOpinionID();
                opinionName = opinionDataHolder.getOpinionName();
                long startTime = System.currentTimeMillis() / 1000;

                RealmManager.INSTANCE.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        wpDataDB.setDt_update(startTime);
                        wpDataDB.setUser_opinion_id(String.valueOf(opinionID));
                        wpDataDB.setUser_opinion_author_id(String.valueOf(wpDataDB.getUser_id()));
                        wpDataDB.setUser_opinion_dt_update(startTime);
                        wpDataDB.startUpdate = true;
                        realm.insertOrUpdate(wpDataDB);
                    }
                });
            }

        };
    }
}
