package ua.com.merchik.merchik.Options.Buttons;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.NEED_UPDATE_UI_REQUEST;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.dataLayer.ModeUI;
import ua.com.merchik.merchik.features.main.DBViewModels.PlanogrammVizitShowcaseViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.QuestionAnswerSDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.ThemeDBViewModel;

public class OptionButtonQuestionAnswer<T> extends OptionControl {
    public int OPTION_BUTTON_PHOTO_PLANOGRAMM_ID = 151122;

    private WpDataDB wpDataDB;
    private final WorkPlan workPlan = new WorkPlan();

    public OptionButtonQuestionAnswer(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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


//            Intent intent = new Intent(context, FeaturesActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putString("viewModel", QuestionAnswerSDBViewModel.class.getCanonicalName());
//            bundle.putString("contextUI", ContextUI.QUESTION_ANSWER_INFO.toString());
//            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
//            bundle.putString("dataJson", new Gson().toJson(wpDataDB.getCode_dad2()));
//            bundle.putString("title", "Жалобы, замечания, предложения");
//            bundle.putString(
//                    "subTitle",
//                    "Перечень пожеланий, предложений, замечаний по работе системы. " +
//                            "Замечания которые вы здесь оставите будут передану руководству компании, " +
//                            "проанализированы, и вы получите ответ."
//            );
//            intent.putExtras(bundle);
//
//            ActivityCompat.startActivityForResult(
//                    (Activity) context,
//                    intent,
//                    NEED_UPDATE_UI_REQUEST,
//                    null
//            );

//            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent2 = new Intent(context, FeaturesActivity.class);
                Bundle bundle2 = new Bundle();
                bundle2.putString("viewModel", ThemeDBViewModel.class.getCanonicalName());
                bundle2.putString("contextUI", ContextUI.ADD_THEME_QUESTION_ANSWER.toString());
                bundle2.putString("modeUI", ModeUI.ONE_SELECT.toString());
                bundle2.putString("dataJson", new Gson().toJson(wpDataDB.getCode_dad2()));
                bundle2.putString("title", "Жалобы, Замечания, Предложения (Жилетка)");
                bundle2.putString("subTitle", "Выберите тему из списка. Благодаря анализу вашего мнения мы сможем улучшить работу нашего предприятия и тем самым увеличить ваши доходы.");

                intent2.putExtras(bundle2);

                ActivityCompat.startActivityForResult(
                        (Activity) context,
                        intent2,
                        NEED_UPDATE_UI_REQUEST,
                        null
                );
//            }, 10);
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionButPhotoPlanogramm/executeOption/Exception", "Exception e: " + e);
        }
    }
}
