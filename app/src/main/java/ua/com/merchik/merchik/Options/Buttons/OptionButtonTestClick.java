package ua.com.merchik.merchik.Options.Buttons;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.NEED_UPDATE_UI_REQUEST;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.dataLayer.ModeUI;
import ua.com.merchik.merchik.features.main.DBViewModels.DynamicAchievementSDBViewModel;

public class OptionButtonTestClick<T> extends OptionControl {


    public OptionButtonTestClick(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
    }

    private void executeOption() {
        Log.e("nButtonTestClick", "++++++++++");
        Intent intent = new Intent(context, FeaturesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("viewModel", DynamicAchievementSDBViewModel.class.getCanonicalName());
        bundle.putString("contextUI", ContextUI.DYNAMIC_ACHIEVEMENT.toString());
        bundle.putString("modeUI", ModeUI.DEFAULT.toString());
        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("wpDataDBId", String.valueOf(wpDataDB.getCode_dad2()));
        bundle.putString("dataJson", new Gson().toJson(dataJson));

        bundle.putString("title", "Динаміка досягнень");
        bundle.putString(
                "subTitle",  "Представлена динаміка досягнень за обраною адресою та клієнтом"
        );

        intent.putExtras(bundle);
        ActivityCompat.startActivityForResult((Activity) context, intent, NEED_UPDATE_UI_REQUEST, null);
    }
}
