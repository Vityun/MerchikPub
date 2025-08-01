package ua.com.merchik.merchik.Options;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.OFS;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.OOS;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.SKUFact;
import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.SKUPlan;
import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA;
import static ua.com.merchik.merchik.Globals.OptionControlName.AKCIYA_ID;
import static ua.com.merchik.merchik.Globals.OptionControlName.AMOUNT;
import static ua.com.merchik.merchik.Globals.OptionControlName.DT_EXPIRE;
import static ua.com.merchik.merchik.Globals.OptionControlName.ERROR_ID;
import static ua.com.merchik.merchik.Globals.OptionControlName.EXPIRE_LEFT;
import static ua.com.merchik.merchik.Globals.OptionControlName.FACE;
import static ua.com.merchik.merchik.Globals.OptionControlName.NOTES;
import static ua.com.merchik.merchik.Globals.OptionControlName.OBOROTVED_NUM;
import static ua.com.merchik.merchik.Globals.OptionControlName.PRICE;
import static ua.com.merchik.merchik.Globals.OptionControlName.UP;
import static ua.com.merchik.merchik.Globals.distanceMin;
import static ua.com.merchik.merchik.Globals.userId;
import static ua.com.merchik.merchik.Options.Options.ConductMode.DEFAULT_CONDUCT;
import static ua.com.merchik.merchik.data.OptionMassageType.Type.DIALOG;
import static ua.com.merchik.merchik.data.OptionMassageType.Type.STRING;
import static ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm.AdditionalRequirementsModENUM.HIDE_FOR_USER;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;
import static ua.com.merchik.merchik.trecker.Coordinates;
import static ua.com.merchik.merchik.trecker.coordinatesDistanse;
import static ua.com.merchik.merchik.trecker.enabledGPS;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.realm.RealmResults;
import kotlin.Unit;
import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity.TARSecondFrag;
import ua.com.merchik.merchik.Adapters.TextAdapter;
import ua.com.merchik.merchik.BuildConfig;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.Options.Buttons.OptionButPhotoPlanogramm;
import ua.com.merchik.merchik.Options.Buttons.OptionButtAchievements;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonAddComment;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonAddNewClient;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonAddNewFriend;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonAvailabilityDetailedReport;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonHistoryMP;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPhotoAktionTovar;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPhotoBeforeStartWork;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPhotoCassZone;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPhotoDMP;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPhotoEFFIE;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPhotoOfACartWithGoods;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPhotoPOS;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPhotoShowcaseCorporateBlock;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPhotoShowcaseFullness;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPhotoShowcaseNear;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPhotoTT;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonPlanogrammVizit;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonReclamationAnswer;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonStartWork;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonTaskAnswer;
import ua.com.merchik.merchik.Options.Buttons.OptionButtonUserOpinion;
import ua.com.merchik.merchik.Options.Controls.OptionControlAchievements;
import ua.com.merchik.merchik.Options.Controls.OptionControlAddComment;
import ua.com.merchik.merchik.Options.Controls.OptionControlAddOpinion;
import ua.com.merchik.merchik.Options.Controls.OptionControlAdditionalMaterialsMark;
import ua.com.merchik.merchik.Options.Controls.OptionControlAdditionalRequirementsMark;
import ua.com.merchik.merchik.Options.Controls.OptionControlAvailabilityControlPhotoRemainingGoods;
import ua.com.merchik.merchik.Options.Controls.OptionControlAvailabilityDetailedReport;
import ua.com.merchik.merchik.Options.Controls.OptionControlCheckDetailedReport;
import ua.com.merchik.merchik.Options.Controls.OptionControlCheckMarkDetailedReport;
import ua.com.merchik.merchik.Options.Controls.OptionControlCheckMarkPhotoReport;
import ua.com.merchik.merchik.Options.Controls.OptionControlCheckTovarUp;
import ua.com.merchik.merchik.Options.Controls.OptionControlCheckingForAnAchievement;
import ua.com.merchik.merchik.Options.Controls.OptionControlCheckingPercentageOfShelfSpaceDPPO;
import ua.com.merchik.merchik.Options.Controls.OptionControlCheckingReasonOutOfStock;
import ua.com.merchik.merchik.Options.Controls.OptionControlCheckingReasonOutOfStockOSV;
import ua.com.merchik.merchik.Options.Controls.OptionControlEKL;
import ua.com.merchik.merchik.Options.Controls.OptionControlEndAnotherWork;
import ua.com.merchik.merchik.Options.Controls.OptionControlFacePlan;
import ua.com.merchik.merchik.Options.Controls.OptionControlMP;
import ua.com.merchik.merchik.Options.Controls.OptionControlOpinionByController;
import ua.com.merchik.merchik.Options.Controls.OptionControlPercentageOfThePrize;
import ua.com.merchik.merchik.Options.Controls.OptionControlPhoto;
import ua.com.merchik.merchik.Options.Controls.OptionControlPhotoPromotion;
import ua.com.merchik.merchik.Options.Controls.OptionControlPhotoShowcase;
import ua.com.merchik.merchik.Options.Controls.OptionControlPhotoTovarsLeft;
import ua.com.merchik.merchik.Options.Controls.OptionControlPhotoTovarsLeftClient;
import ua.com.merchik.merchik.Options.Controls.OptionControlPlanorammVizit;
import ua.com.merchik.merchik.Options.Controls.OptionControlPromotion;
import ua.com.merchik.merchik.Options.Controls.OptionControlReclamationAnswer;
import ua.com.merchik.merchik.Options.Controls.OptionControlRegistrationPotentialClient;
import ua.com.merchik.merchik.Options.Controls.OptionControlRegistrationPotentialFriend;
import ua.com.merchik.merchik.Options.Controls.OptionControlReturnOfGoods;
import ua.com.merchik.merchik.Options.Controls.OptionControlStockBalanceTovar;
import ua.com.merchik.merchik.Options.Controls.OptionControlStockTovarLeft;
import ua.com.merchik.merchik.Options.Controls.OptionControlTaskAnswer;
import ua.com.merchik.merchik.PhotoReports;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.ServerExchange.Exchange;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.VersionApp;
import ua.com.merchik.merchik.ViewHolders.Clicks;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Database.Realm.VirtualAdditionalRequirementsDB;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsJOIN.AdditionalMaterialsJOINAdditionalMaterialsAddressSDB;
import ua.com.merchik.merchik.data.Database.Room.AdditionalMaterialsSDB;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.EKL_SDB;
import ua.com.merchik.merchik.data.Database.Room.OpinionSDB;
import ua.com.merchik.merchik.data.Database.Room.OpinionThemeSDB;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsMarkDB;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportHintList;
import ua.com.merchik.merchik.data.TovarOptions;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.AdditionalRequirementsRealm;
import ua.com.merchik.merchik.database.realm.tables.AppUserRealm;
import ua.com.merchik.merchik.database.realm.tables.CustomerRealm;
import ua.com.merchik.merchik.database.realm.tables.LogMPRealm;
import ua.com.merchik.merchik.database.realm.tables.OptionsRealm;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;
import ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements.DialogARMark.DialogARMark;
import ua.com.merchik.merchik.dialogs.DialogAdditionalRequirements.DialogAdditionalRequirements;
import ua.com.merchik.merchik.dialogs.DialogData;
import ua.com.merchik.merchik.dialogs.DialogFilter.Click;
import ua.com.merchik.merchik.dialogs.EKL.DialogEKL;
import ua.com.merchik.merchik.dialogs.features.MessageDialogBuilder;
import ua.com.merchik.merchik.dialogs.features.dialogMessage.DialogStatus;
import ua.com.merchik.merchik.features.main.DBViewModels.AdditionalRequirementsDBViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.SamplePhotoSDBViewModel;
import ua.com.merchik.merchik.toolbar_menus;

public class Options {

    private Globals globals = new Globals();

    public static int[] describedOptionsButt = new int[]{135809, 132968, 135158, 132969, 138518,
            138520, 138773, 137797, 138339, 141360, 141910, 141888, 141885, 84007, 132666, 139576,
            138767, 135742, 132621, 84003, 138340, 135327, 135328, 156882, 151139, 132623, 133382, 136100,
            157275, 157276, 157274, 135159, 157277, 157353, 138643, 158243, 135412, 151748, 158309,
            158308, 158604, 158605, 158606, 157354, 157242, 159725, 135413, 135719, 143969, 160567,
            164351, 164355, 165481, 141069
    };

    // Провести отчет
    public static int[] describedOptions = new int[]{132624, 76815, 157241, 157243, 84006, 156928,
            151594, 80977, 135330, 133381, 136101, 135329, 138518, 151139, 132623, 133382, 136100, 137797, 135809,
            135328, 135327, 157275, 138341, 590, 84932, 134583, 157352, 1470, 138644, 1455, 135061,
            158361, 159707, 575, 132971, 135591, 135708, 135595, 143968, 160568, 164352, 164354,
            84005, 84967, 164985, 165276, 165275, 165482, 166528, 157288, 141067};

    /*Сюда записываются Опции которые не прошли проверку, при особенном переданном MOD-e. Сделано
    для того что б потом можно было посмотреть название опций которые не прошли проверку и, возможно,
    в будущем, их пересчитать*/
    private List<OptionsDB> optionNotConduct = new ArrayList();

    private static List<TovarOptions> list = new ArrayList<>();
    private static List<Integer> ids = new ArrayList<>();
    private String spinnerError = "";
    private String spinnerPromo = "";

    Map<String, String> mapSpinnerError = new HashMap<>();
    Map<String, String> mapSpinnerPromo = new HashMap<>();


    // =============================================================================================
    // КОНТРОЛЬ ОПЦИЙ - закладинка або сигнал
    public <T> void optionControl(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {

        try {
            Log.e("OPTION_CONTROL", "=======================================================");
            Log.e("OPTION_CONTROL", "HERE(0): " + optionsDB.getOptionControlId());
            Log.e("OPTION_CONTROL", "NNKMode mode: " + mode);

            Log.e("OPTION_CONTROL", "optionsDB/getOptionId: " + optionsDB.getOptionId());
            Log.e("OPTION_CONTROL", "optionsDB/getOptionTxt: " + optionsDB.getOptionTxt());
            Log.e("OPTION_CONTROL", "optionsDB/getOptionControlId: " + optionsDB.getOptionControlId());
            Log.e("OPTION_CONTROL", "optionsDB/getOptionControlTxt: " + optionsDB.getOptionControlTxt());

            int optionControlId = Integer.parseInt(optionsDB.getOptionControlId());

            OptionMassageType newOptionType = new OptionMassageType();
            switch (mode) {
                case NULL:
                    break;

                case CHECK_CLICK:
                    newOptionType.type = DIALOG;
                    break;
            }

            switch (optionControlId) {

//                case 157288:
//                    OptionControlPromotionTov<?> optionControlPromotionTov = new OptionControlPromotionTov<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
//                    optionControlPromotionTov.showOptionMassage("");
//                    break;

                case 84005, 84967, 164985, 165276:
                    OptionControlCheckingForAnAchievement<?> optionControlCheckingForAnAchievement = new OptionControlCheckingForAnAchievement<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlCheckingForAnAchievement.showOptionMassage("");
                    break;

                case 160568:
                    OptionControlPhotoShowcase<?> optionControlPhotoShowcase = new OptionControlPhotoShowcase<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlPhotoShowcase.showOptionMassage("");
                    break;

                case 135708:
                    OptionControlCheckMarkDetailedReport<?> optionControlCheckMarkDetailedReport = new OptionControlCheckMarkDetailedReport<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlCheckMarkDetailedReport.showOptionMassage("");
                    break;

                case 135595:
                    OptionControlCheckMarkPhotoReport<?> optionControlCheckMarkPhotoReport = new OptionControlCheckMarkPhotoReport<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlCheckMarkPhotoReport.showOptionMassage("");
                    break;

                case 165275:
                case 135591:
                    OptionControlReturnOfGoods<?> optionControlReturnOfGoods = new OptionControlReturnOfGoods<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlReturnOfGoods.showOptionMassage("");
                    break;

                case 159707:
                    OptionControlAvailabilityControlPhotoRemainingGoods<?> optionControlAvailabilityControlPhotoRemainingGoods = new OptionControlAvailabilityControlPhotoRemainingGoods<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlAvailabilityControlPhotoRemainingGoods.showOptionMassage("");
                    break;

                case 1455:
                    OptionControlCheckingPercentageOfShelfSpaceDPPO<?> optionControlCheckingPercentageOfShelfSpaceDPPO = new OptionControlCheckingPercentageOfShelfSpaceDPPO<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlCheckingPercentageOfShelfSpaceDPPO.showOptionMassage("");
                    break;

                case 135061:
                    OptionControlPercentageOfThePrize<?> optionControlPercentageOfThePrize = new OptionControlPercentageOfThePrize<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlPercentageOfThePrize.showOptionMassage("");
                    break;

                case 1470:
                    OptionControlPhotoTovarsLeft<?> optionControlPhotoTovarsLeft = new OptionControlPhotoTovarsLeft<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlPhotoTovarsLeft.showOptionMassage("");
                    break;

                case 158361:
                    OptionControlPhotoTovarsLeftClient<?> optionControlPhotoTovarsLeftClient = new OptionControlPhotoTovarsLeftClient<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlPhotoTovarsLeftClient.showOptionMassage("");
                    break;

                case 138644:
                    OptionControlCheckTovarUp<?> optionControlCheckTovarUp = new OptionControlCheckTovarUp<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlCheckTovarUp.showOptionMassage("");
                    break;

                case 157352:
                    OptionControlCheckDetailedReport<?> optionControlCheckDetailedReport = new OptionControlCheckDetailedReport<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlCheckDetailedReport.showOptionMassage("");
                    break;

                case 590:
                case 160209:
                    OptionControlAchievements<?> optionControlAchievements = new OptionControlAchievements<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlAchievements.showOptionMassage("");
                    break;

                case 157275:
                    OptionControlFacePlan<?> optionControlFacePlan = new OptionControlFacePlan<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlFacePlan.showOptionMassage("");
                    break;

                case 84006:
                case 143968:
                    OptionControlEKL<?> optionControlEKL = new OptionControlEKL<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlEKL.showOptionMassage("");
                    break;

                case 133381:
                    OptionControlRegistrationPotentialClient<?> optionControlRegistrationPotentialClient = new OptionControlRegistrationPotentialClient<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlRegistrationPotentialClient.showOptionMassage("");
                    break;

                case 136101:
                    OptionControlRegistrationPotentialFriend<?> optionControlRegistrationPotentialFriend = new OptionControlRegistrationPotentialFriend<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlRegistrationPotentialFriend.showOptionMassage("");
                    break;

//                case 151594:
//                    OptionControlPhotoBeforeStartWork<?> optionControlPhotoBeforeStartWork = new OptionControlPhotoBeforeStartWork<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
//                    optionControlPhotoBeforeStartWork.showOptionMassage("");
//                    break;

                case 132624:
                    OptionControlAddComment<?> optionControlAddComment = new OptionControlAddComment<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlAddComment.showOptionMassage("");
                    break;

                case 84001:
                    OptionControlAddOpinion<?> optionControlAddOpinion = new OptionControlAddOpinion<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlAddOpinion.showOptionMassage("");
                    break;

                case 168439:
                    OptionControlPlanorammVizit<?> optionControlPlanorammVizit = new OptionControlPlanorammVizit<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlPlanorammVizit.showOptionMassage("");
                    break;

                case 141893:
                    OptionControlOpinionByController<?> optionControlOpinionByController = new OptionControlOpinionByController<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlOpinionByController.showOptionMassage("");
                    break;

                case 80977:
                case 157288:
                    OptionMassageType type1 = new OptionMassageType();
                    switch (mode) {
                        case NULL:
                            break;

                        case CHECK_CLICK:
                            type1.type = DIALOG;
                            break;
                    }

                    OptionControlPromotion optionControlPromotion = new OptionControlPromotion(context, dataDB, optionsDB, type1, mode, unlockCodeResultListener);
                    optionControlPromotion.showOptionMassage("");
                    break;

                case 157278:
                case 166528:
                    OptionControlPhotoPromotion<?> optionControlPhotoPromotion = new OptionControlPhotoPromotion<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlPhotoPromotion.showOptionMassage("");
                    break;

//                case 132971:    // Проверка наличия Фото тележка с товаром (тип 10)
                case 151594:    // 20.01 перевел Контроль наличия фото витрины (до начала работ) на общую схему
                case 134583:
                case 84932:     // Проверка наличия ФотоОтчётов (id мне дали из 1С) (тип 0)
                case 141361:
                case 158606:
                case 158607:
                case 158608:
                case 158609:
                case 159726:    // Фото торговой точки
                case 159725:    // Кнопка "Фото Торговой Точки (ФТТ)"
                case 165482:    // Контроль ЭФФИ
                case 164352:    // Фото Планограмми ТТ
                case 164354:    // Фото Планограмми ТТ
                case 132971:    // Фото товара біля вітрини
                case 169109:    // Контроль наличия Фото POS материалов
                    //                    checkPhotoReport(context, dataDB, optionsDB, type, mode);
                    OptionControlPhoto<?> optionControlPhoto = new OptionControlPhoto<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlPhoto.showOptionMassage("");
                    break;
//
//                case 134583:    // ПРоверка наличия фотоотчётов с привязкой к координатам
//                    // Нужно дописать
//                    checkPhotoReportWithMP(context, dataDB, optionsDB, type, mode);
//                    break;

//                case 1470:  // Проверка наличия Фото остатков товара (тип 4)
//                    checkPhoto(dataDB, optionsDB, "4");
//                    break;

//                case 132971:  // Проверка наличия Фото тележка с товаром (тип 10)
//                    OptionControlPhotoCartWithGoods<?> optionControlPhotoCartWithGoods = new OptionControlPhotoCartWithGoods<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
//                    optionControlPhotoCartWithGoods.showOptionMassage("");
//                    break;

//                case 141361:  // Проверка наличия Фото тележка с товаром (тип 31)
//                    checkPhoto(dataDB, optionsDB, "31");
//                    break;

                case 141886:    // Проверка наличия Фото Документов (3)
                    checkPhoto(dataDB, optionsDB, "3");
                    break;

                case 76815: // Проверка наличия Дет.Отчётов (id мне дали из 1С)
                    Log.e("OPTION_CONTROL", "Проверка наличия Дет.Отчётов" + optionsDB.getOptionControlId());
//                    check76815(dataDB, optionsDB); // Проверка Представленности
                    OptionMassageType type2 = new OptionMassageType();
                    switch (mode) {
                        case NULL:
                            break;

                        case CHECK_CLICK:
                            type2.type = DIALOG;
                            break;
                    }
                    OptionControlAvailabilityDetailedReport optionControlAvailabilityDetailedReport = new OptionControlAvailabilityDetailedReport(context, dataDB, optionsDB, type2, mode, unlockCodeResultListener);
                    optionControlAvailabilityDetailedReport.showOptionMassage("");
                    break;

                case 138519:
                    Log.e("OPTION_CONTROL", "checkStartWork: " + optionsDB.getOptionControlId());
//                checkStartWork(context, dataDB, optionsDB, type, mode);
                    optionControlStartWork_138519(context, dataDB, optionsDB, type, mode, unlockCodeResultListener);
                    break;

                case 138521:
                    Log.e("OPTION_CONTROL", "checkEndWork: " + optionsDB.getOptionControlId());
//                checkEndWork(context, dataDB, optionsDB, type, mode);
                    optionControlEndWork_138521(context, dataDB, optionsDB, type, mode, unlockCodeResultListener);
                    break;

                case 8299:
                    Log.e("OPTION_CONTROL", "checkMP: " + optionsDB.getOptionControlId());
//                checkMP(context, dataDB, optionsDB, type, mode);
//                    optionControlMP_8299(context, dataDB, optionsDB, type, mode, unlockCodeResultListener);
                    OptionMassageType optionControlMPTypeMsg = new OptionMassageType();
                    switch (mode) {
                        case NULL:
                            break;

                        case CHECK_CLICK:
                            optionControlMPTypeMsg.type = DIALOG;
                            break;
                    }
                    OptionControlMP optionControlMP = new OptionControlMP(context, dataDB, optionsDB, optionControlMPTypeMsg, mode, unlockCodeResultListener);
                    optionControlMP.showMassage(false, new Clicks.clickStatusMsg() {
                        @Override
                        public void onSuccess(String data) {

                        }

                        @Override
                        public void onFailure(String error) {

                        }
                    });
                    optionControlMP.showOptionMassage("");
                    break;

                case 141911:
                    // !!!!!!!
                    checkReceivingAnOrder_141911(context, dataDB, optionsDB, type, mode);
                    break;

                case 141889:
                    // !!!!!!!
                    check_RENAME_2(context, dataDB, optionsDB, type, mode);
                    break;

//                case 84006:
//                    // !!!!!!!
//                    checkEKL(context, dataDB, optionsDB, type, mode);
//                    break;

                case 587:
                    optionControlReceivingAnOrder_587(context, dataDB, optionsDB, null, NNKMode.CHECK, unlockCodeResultListener);
                    break;

                case 138341:
                    try {
                        OptionControlAdditionalRequirementsMark<?> optionControlAdditionalRequirementsMark = new OptionControlAdditionalRequirementsMark<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                        optionControlAdditionalRequirementsMark.showOptionMassage("");
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "OptionControlAdditionalRequirementsMark", "Exception e: " + e);
                        Globals.writeToMLOG("ERROR", "OptionControlAdditionalRequirementsMark", "e.printStackTrace(): " + Arrays.toString(e.getStackTrace()));
                    }
                    break;

                case 138342:
                    try {
                        OptionControlAdditionalMaterialsMark<?> optionControlAdditionalMaterialsMark = new OptionControlAdditionalMaterialsMark<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                        optionControlAdditionalMaterialsMark.showOptionMassage("");
                    } catch (Exception e) {
                        Globals.writeToMLOG("ERROR", "OptionControlAdditionalMaterialsMark", "Exception e: " + e);
                        Globals.writeToMLOG("ERROR", "OptionControlAdditionalMaterialsMark", "e.printStackTrace(): " + Arrays.toString(e.getStackTrace()));
                    }
                    break;

                case 139577:
                    optionControlVersion_139577(context, dataDB, optionsDB, null, mode, unlockCodeResultListener);
                    break;

                case 141067:
                    OptionControlStockBalanceTovar<?> optionControlStockBalanceTovar = new OptionControlStockBalanceTovar<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlStockBalanceTovar.showOptionMassage("");
                    break;

                case 2243:
                    OptionControlStockTovarLeft<?> optionControlStockTovarLeft = new OptionControlStockTovarLeft<>(context, dataDB, optionsDB, newOptionType, mode, unlockCodeResultListener);
                    optionControlStockTovarLeft.showOptionMassage("");
                    break;

                default:
//                switch (warningType) {
//                    case 1:
//                        Log.e("MASSAGE_TO_USER", "Какое-то сообщение");
//                        break;
//
//                    case 2:
//                        Toast.makeText(context, "Не могу найти описание опции: " + optionsDB.getOptionControlId(), Toast.LENGTH_LONG).show();
//                        break;
//
//                    default:
//                        Log.e("MASSAGE_TO_USER", "Ничего не выполняем");
//                        break;
//                }
                    break;
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "optionControl", "exeption: " + e);
        }

    }


    //==============================================================================================

    public enum NNKMode {
        CHECK,  // Проверка, вывод сообщения (Надо поменять что б ничего не выводилось)
        CHECK_CLICK,  // Проверка, вывод сообщения
        CHECK_COLLECT_MSG,
        MAKE,   // Выполнения функциональности кнопки
        NULL,    // Ничего не делаем
        BLOCK   // для блокирующих опций
    }

    /**
     * 07.06.2022
     * Решил переписать Нажатие На Кнопку(ННК), потому что предыдущая реализация работала не совсем
     * правильно.
     */
    public <T> void optionNNK(Context context, T document, OptionsDB option, OptionMassageType massageType, NNKMode nnkMode, Clicks.clickVoid click) {
        Log.e("optionNNK", "--------------------------------");
        Log.e("optionNNK", "option: " + option);
        Log.e("optionNNK", "option id: " + option.getOptionId());

        // Сброс сигнала Опции
        option.setIsSignal("0");

        // Проход по первой опции блокировки
        if (!option.getOptionBlock1().equals("0")) {
            executeOption(context, document, option, Integer.parseInt(option.getOptionBlock1()), massageType, nnkMode);
        }

        // Проход по второй опции блокировки
        if (!option.getOptionBlock2().equals("0")) {
            executeOption(context, document, option, Integer.parseInt(option.getOptionBlock2()), massageType, nnkMode);
        }

    }

    /**
     * 07.06.2022
     * Контроль опций.
     */
    private <T> void executeOption(Context context, T document, OptionsDB option, int optionId, OptionMassageType massageType, NNKMode nnkMode) {
    }

    public List<OptionsDB> optionFromDetailedReport;

    public void setOptionFromDetailedReport(List<OptionsDB> optionList) {
        this.optionFromDetailedReport = optionList;
    }


    /**
     * Тут я получаю список Опций которые касаются той самой/
     */
    public List<OptionsDB> getOptionsToControl(OptionsDB optionsDB) {
        List<OptionsDB> optionsDBList = OptionsRealm.getOptionsNOTButtonByDAD2(optionsDB.getCodeDad2());
//        List<OptionsDB> optionsDBList = OptionsRealm.getOptionsButtonByDAD2(optionsDB.getCodeDad2());
        List<OptionsDB> res = new ArrayList<>();
        if (optionsDBList != null) {
            for (OptionsDB item : optionsDBList) {
                if (item.getOptionControlId().equals(optionsDB.getOptionId()) && !item.getOptionId().equals(optionsDB.getOptionControlId())) {
                    res.add(item);
                }
            }
        }
        return res;
    }

    /**
     * Запуск нового протокола проверки ОпцийКонтроля
     */
    int count = 0;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public <T> void optionControlNewAlgo(View view, List<OptionsDB> optionsDBList, Context context, T dataDB, OptionsDB option, List<OptionsDB> optionList, OptionMassageType type, NNKMode mode, boolean check, Clicks.clickVoid click) {
        count = 0;
        List<OptionsDB> success = new ArrayList<>();
        List<OptionsDB> failure = new ArrayList<>();
        int optionId2 = Integer.parseInt(option.getOptionId());
        if (optionsDBList != null && optionsDBList.size() > 0) {
            for (OptionsDB item : optionsDBList) {
                NNKMode nnkMode;
                if (item.getBlockPns().equals("1")) {
                    nnkMode = NNKMode.BLOCK;
                } else if (check) {
                    nnkMode = NNKMode.CHECK;
                } else {
                    nnkMode = NNKMode.MAKE;
                }
                int optionId = Integer.parseInt(item.getOptionId());

                Log.e("NNK_I", "F/New/ITEM_CONTROL_OPTION_optControl/getOptionId: " + item.getOptionId());
                Log.e("NNK_I", "F/New/ITEM_CONTROL_OPTION_optControl/getOptionTxt: " + item.getOptionTxt());
                Log.e("NNK_I", "F/New/ITEM_CONTROL_OPTION_optControl/getOptionControlId: " + item.getOptionControlId());
                Log.e("NNK_I", "F/New/ITEM_CONTROL_OPTION_optControl/getOptionControlTxt: " + item.getOptionControlTxt());

                optControl(view, context, dataDB, option, optionId, item, type, NNKMode.BLOCK, new OptionControl.UnlockCodeResultListener() {
                    @Override
                    public void onUnlockCodeSuccess() {
//                        Toast.makeText(context, "Option: " + item.getOptionTxt() + " execute Success", Toast.LENGTH_LONG).show();
//                        click.click(); +84932/+158608/+8299/+157352/+160568/+134583/139577/+158607
                        count++;
                        success.add(item);
                        if (count == optionsDBList.size()) {
                            click.click();
                            // Обычное выполнение нажатия на кнопку.
                            optControl(view, context, dataDB, option, optionId2, option, type, mode, new OptionControl.UnlockCodeResultListener() {
                                @Override
                                public void onUnlockCodeSuccess() {
                                    click.click();
                                }

                                @Override
                                public void onUnlockCodeFailure() {
                                    click.click();
                                }
                            });
                        }
                    }

                    @Override
                    public void onUnlockCodeFailure() {
                        failure.add(item);
                        click.click();
                    }
                });
            }
        } else {
            // Обычное выполнение нажатия на кнопку.
            optControl(view, context, dataDB, option, optionId2, option, type, mode, new OptionControl.UnlockCodeResultListener() {
                @Override
                public void onUnlockCodeSuccess() {
                    click.click();
                }

                @Override
                public void onUnlockCodeFailure() {
                    click.click();
                }
            });
        }
    }

    /*
     * Обработка опций
     * Нажатие На Кнопку (ННК) -- абстрактное название. На самом деле в принципе обработка
     * состояний опций
     * */
    private int res = 0;    // Счётчик для накапливания "блокировок" у данной опции

    public <T> OptionMassageType NNK(View view, Context context, T dataDB, OptionsDB option, List<OptionsDB> optionList, OptionMassageType type, NNKMode mode, Clicks.clickVoid click) {
        OptionMassageType result = new OptionMassageType();
        try {
            if (!Objects.equals(option.getOptionControlId(), "160209"))
                option.setIsSignal("0");

            List<OptionsDB> testDad2 = RealmManager.INSTANCE.copyFromRealm(OptionsRealm.getOptionsByDAD2(option.getCodeDad2()));

            Log.e("NNK", "---------------START-----------------");
            Log.e("NNK", "option.option_id: " + option.getOptionId());
            Log.e("NNK", "option.getCodeDad2: " + option.getCodeDad2());
            Log.e("NNK", "option.getCodeDad2 testDad2 SIZE: " + testDad2.size());
            Log.e("NNK", "option.getCodeDad2 testDad2: " + new Gson().toJson(testDad2));
            Log.e("NNK", "option.getOptionTxt: " + option.getOptionTxt());
            Log.e("NNK", "option.option_control_id: " + option.getOptionControlId());
            Log.e("NNK", "option.option_control_id: " + option.getOptionControlTxt());
            Log.e("NNK", "START_res: " + res);

            Log.e("NNK", "option.getOptionBlock1(): " + option.getOptionBlock1());
            Log.e("NNK", "option.getOptionBlock2(): " + option.getOptionBlock2());

            Log.e("NNK", "option.optionList(): " + optionList);

            Log.e("NNK", "-------------BLOCK-------------------");

            boolean existOption = true;
            boolean existOption2 = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                OptionsDB testExistOption2 = optionFromDetailedReport.stream().filter(optionListItem -> Objects.equals(optionListItem.getOptionId(), option.getOptionBlock2()))
                        .findAny()
                        .orElse(null);

                if (testExistOption2 == null) existOption2 = false;

                OptionsDB testExistOption1 = optionFromDetailedReport.stream().filter(optionListItem -> Objects.equals(optionListItem.getOptionId(), option.getOptionBlock1()))
                        .findAny()
                        .orElse(null);

                if (testExistOption1 == null) existOption = false;
            }

            // 14301 - трегуб
            // 14840 - Авто комфорт плюс
            // 14843 - Джокер
            // 91276 - БВІ
            // 16898 - Ямуна
            // 13633 - ВС-Импэкс ООО
            // 8523 -  Альянс Краси ПП
            // 91429 - Петровська Елла Олександрівна ФОП
            // 10076 - Триплекс,
            // 78230 - Триумф,
            // 14463 - Креатив,
            // 11034 - Блумі,
            // 82339 - Профекс Груп,
            // 14798 - Еудуко

            // 91332 - Фуд Воркс,
            // 84186 - Уикенд,
            // 69842 - Мега Крисп,
            // 9261  - фабрика бакалійних,
            // 10349 - Гифт-К,
            // 91419 - Чміль Валерій Васильович ФОП

            // 78171 Артхим,
            // 82575 Профит Плюс,
            // 90749 Буко,
            // 75411 Йозера,
            // 10919 Маруся,
            // 10291 Натурпродукт-вега

            // 9443     Кохавинка
            // 91434    Хім опт
            // 38297    Юкойл СП
            // 14504    Традиции вкуса ООО
            // 84667    Парк трейдинг ООО
            // 9029     Родная еда Компания ООО
            // 80786    Марко Продукт ТОВ
            // 91484    Нешнл Трейдинг
            // 14888    Техно-Груп
            // 86566    Троянда-захід .......... вже була
            // 78286    Аумі
            // 14133    ЗБС
            // 8804 львовский хладокомбинат

            // 45654 Аванта-Трейд ПП
            // 82359 Вертикаль-Київ ТОВ
            // 78280 Веранта ООО
            // 38475 Глобал Трейд ЮА
            // 70484 Кідді-Ко ПП
            // 14023 Лаки Шуз ТОВ
            //  9029 Родная еда Компания ООО
            //  8767 Сл Дистрибьюшн
            // 45748 Бико
            // 32246 Ласунка
            //  8463 Житомирський маслозавод
            // 10426 Драйд Фудз
            // 14110 Кр Ингредиентс
            // 88939 Ничога ПП

            // 14072 Сирена Екохім
            // 14526 Юкрейн Трейдинг
            // 10393 Вока-Д
            // 14693 Живий квас
            // 45632 Світ Україна
            // 14156 Ренклод
            // 8219  ВГП ПАО
            // 14839 Вега Украина ТД
            // 14611 Им Текстиль ООО
            // 14365 Флеш
            // 91373 Сондак Роман Михайлович ФОП
            // 91206 ТС Плюс ТОВ

//            14017 Акмега
//            91409 Деревянко
//            89863 Альтернатива-СВ
//            14176 ТСЦ Инженеринг
//            91488 Рікко Трейдінг
//            10822 Єгмонт Україна
//            77764 2К СУНП ТОВ
//            14611 Им Текстиль ООО
//            10275 Прошанский коньячный завод - Украина ООО
//            11060 Фабрика Мороженого Хладопром ООО
//            11165 Энерлайт ООО

            // 91468 Комарницька

            if (
                    !option.getClientId().equals("-9999999")
//                    !option.getClientId().equals("9295") &&     //Бетта
//                    !option.getClientId().equals("9382") &&     //Витмарк
//                    !option.getClientId().equals("38283")       //Юнилайф

//                    option.getClientId().equals("14301") ||     // трегуб
//                    option.getClientId().equals("14840") || // Авто комфорт плюс
//                    option.getClientId().equals("14843") || // Джокер
//                    option.getClientId().equals("91276") || // БВІ
//                    option.getClientId().equals("16898") || // Ямуна
//                    option.getClientId().equals("13633") || // ВС-Импэкс ООО
//                    option.getClientId().equals("8523") ||  // Альянс Краси ПП
//                    option.getClientId().equals("91429") || // Петровська Елла Олександрівна ФОП
//                    option.getClientId().equals("10076") || // Триплекс
//                    option.getClientId().equals("78230") || // Триумф
//                    option.getClientId().equals("14463") || // Креатив
//                    option.getClientId().equals("11034") || // Блумі
//                    option.getClientId().equals("82339") || // Профекс Груп
//                    option.getClientId().equals("14798") || // Еудуко
//                    option.getClientId().equals("91332") || // Фуд Воркс,
//                    option.getClientId().equals("84186") || // Уикенд,
//                    option.getClientId().equals("69842") || // Мега Крисп,
//                    option.getClientId().equals("9261") ||  // фабрика бакалійних,
//                    option.getClientId().equals("10349") || // Гифт-К,
//                    option.getClientId().equals("91419") || // Чміль Валерій Васильович ФОП
//                    option.getClientId().equals("78171") || // Артхим,
//                    option.getClientId().equals("82575") || // Профит Плюс,
//                    option.getClientId().equals("90749") || // Буко,
//                    option.getClientId().equals("75411") || // Йозера,
//                    option.getClientId().equals("10919") || // Маруся,
//                    option.getClientId().equals("10291") || // Натурпродукт-вега
//                    option.getClientId().equals("9443") || // Кохавинка
//                    option.getClientId().equals("91434") || // Хім опт
//                    option.getClientId().equals("38297") || // Юкойл СП
//                    option.getClientId().equals("14504") || // Традиции вкуса ООО
//                    option.getClientId().equals("84677") || // Парк трейдинг ООО
//                    option.getClientId().equals("9029") || // Родная еда Компания ООО
//                    option.getClientId().equals("80786") || // Марко Продукт ТОВ
//                    option.getClientId().equals("91484") || // Нешнл Трейдинг
//                    option.getClientId().equals("14888") || // Техно-Груп
//                    option.getClientId().equals("86566") || // Троянда-захід .......... вже була
//                    option.getClientId().equals("78286") || // Аумі
//                    option.getClientId().equals("14133") ||   // ЗБС
//                    option.getClientId().equals("8804")  ||  //  львовский хладокомбинат
//
//                    option.getClientId().equals("45654")  ||    // Аванта-Трейд ПП
//                    option.getClientId().equals("82359")  ||    // Вертикаль-Київ ТОВ
//                    option.getClientId().equals("78280")  ||    // Веранта ООО
//                    option.getClientId().equals("38475")  ||    // Глобал Трейд ЮА
//                    option.getClientId().equals("70484")  ||    // Кідді-Ко ПП
//                    option.getClientId().equals("14023")  ||    // Лаки Шуз ТОВ
//                    option.getClientId().equals("9029")  ||    // Родная еда Компания ООО ?? Походу манагеры проебались, прошу прощения
//                    option.getClientId().equals("8767")  ||    // Сл Дистрибьюшн
//                    option.getClientId().equals("45748")  ||    // Бико
//                    option.getClientId().equals("32246")  ||    // Ласунка
//                    option.getClientId().equals("8463")  ||    // Житомирський маслозавод;;;
//                    option.getClientId().equals("10426")  ||    // Драйд Фудз
//                    option.getClientId().equals("14110")  ||    // Кр Ингредиентс
//                    option.getClientId().equals("88939")  ||    // Ничога ПП
//
//                    option.getClientId().equals("14072")  ||    // Сирена Екохім
//                    option.getClientId().equals("14526")  ||    // Юкрейн Трейдинг
//                    option.getClientId().equals("10393")  ||    // Вока-Д
//                    option.getClientId().equals("14693")  ||    // Живий квасmkjjj
//                    option.getClientId().equals("45632")  ||    // Світ Україна
//                    option.getClientId().equals("14156")  ||    // Ренклод
//                    option.getClientId().equals("8219")  ||     // ВГП ПАО
//                    option.getClientId().equals("14839")  ||    // Вега Украина ТД
//                    option.getClientId().equals("14611")  ||    // Им Текстиль ООО
//                    option.getClientId().equals("14365")  ||    // Флеш
//                    option.getClientId().equals("91373")  ||    // Сондак Роман Михайлович ФОП
//                    option.getClientId().equals("91206")  ||      // ТС Плюс ТОВ
//
//                    option.getClientId().equals("14017")  ||    // Акмега
//                    option.getClientId().equals("91409")  ||    // Деревянко
//                    option.getClientId().equals("89863")  ||    // Альтернатива-СВ
//                    option.getClientId().equals("14176")  ||    // ТСЦ Инженеринг
//                    option.getClientId().equals("91488")  ||    // Рікко Трейдінг
//                    option.getClientId().equals("10822")  ||    // Єгмонт Україна
//                    option.getClientId().equals("77764")  ||    // СУНП ТОВ
//                    option.getClientId().equals("14611")  ||    // Им Текстиль ООО
//                    option.getClientId().equals("10275")  ||    // Прошанский коньячный завод - Украина ООО
//                    option.getClientId().equals("11060")  ||    // Фабрика Мороженого Хладопром ООО
//                    option.getClientId().equals("11165")  ||      // Энерлайт ООО
//
//                    option.getClientId().equals("91468")        // Комарницька
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    optionControlNewAlgo(view, getOptionsToControl(option), context, dataDB, option, optionList, type, mode, false, click);
                }
            } else {
                // Проход по второй опции блокировки
                if (!option.getOptionBlock2().equals("0")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        OptionsDB optionsDB = optionFromDetailedReport.stream().filter(optionListItem -> Objects.equals(optionListItem.getOptionId(), option.getOptionBlock2()))
                                .findAny()
                                .orElse(null);
                        if (optionsDB != null) {
                            Log.e("NNK", "1. Проверяю опцмю: " + option.getOptionBlock2());
                            res += optControl(view, context, dataDB, option, Integer.parseInt(option.getOptionBlock2()), optionsDB, type, NNKMode.BLOCK, new OptionControl.UnlockCodeResultListener() {
                                @Override
                                public void onUnlockCodeSuccess() {
                                    Log.e("NNK", "Опция БЛОК 2 прошла успешно, надо проверить БЛОК 1");
//                             Проход по первой опции блокировки
                                    if (!option.getOptionBlock1().equals("0")) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            OptionsDB optionsDB = optionFromDetailedReport.stream().filter(optionListItem -> Objects.equals(optionListItem.getOptionId(), option.getOptionBlock1()))
                                                    .findAny()
                                                    .orElse(null);
                                            if (optionsDB != null) {
                                                Log.e("NNK", "2. Проверяю опцмю: " + option.getOptionBlock1());
                                                res += optControl(view, context, dataDB, option, Integer.parseInt(option.getOptionBlock1()), optionsDB, type, NNKMode.BLOCK, new OptionControl.UnlockCodeResultListener() {
                                                    @Override
                                                    public void onUnlockCodeSuccess() {
                                                        Log.e("NNK", "Успешный Успех. Первая и вторая ОК прошли проверку и должно разрешить работу.");
                                                        optControl(view, context, dataDB, option, Integer.parseInt(option.getOptionId()), null, type, mode, new OptionControl.UnlockCodeResultListener() {
                                                            @Override
                                                            public void onUnlockCodeSuccess() {
                                                                Log.e("NNK", "Выполняю опцию");
                                                            }

                                                            @Override
                                                            public void onUnlockCodeFailure() {
                                                                Log.e("NNK", "НЕ Выполняю опцию");
                                                            }
                                                        });
                                                        click.click();
                                                    }

                                                    @Override
                                                    public void onUnlockCodeFailure() {
                                                        Log.e("NNK", "1. Успешный НЕ Успех. Первая проверку прошла, вторая не прошла, значит не даю делать опцию на которую нажали.");
                                                    }
                                                });
                                                Log.e("NNK", "res OK 1: " + res);
                                            } else {
                                                switch (mode) {
                                                    case MAKE:
                                                        Log.e("NNK", "1. МПроверяю опцмю: " + option.getOptionBlock1());
                                                        optControl(view, context, dataDB, option, Integer.parseInt(option.getOptionId()), null, type, mode, new OptionControl.UnlockCodeResultListener() {
                                                            @Override
                                                            public void onUnlockCodeSuccess() {
                                                                Log.e("NNK", "Выполняю опцию. Если опция не найдена.");
                                                            }

                                                            @Override
                                                            public void onUnlockCodeFailure() {
                                                                Log.e("NNK", "НЕ Выполняю опцию. Если опция не найдена.");
                                                            }
                                                        });
                                                        click.click();
                                                        break;
                                                }
                                            }
                                        }
                                    } else {
                                        switch (mode) {
                                            case MAKE:
                                                Log.e("NNK", "2. МПроверяю опцмю: " + option.getOptionBlock1());
                                                optControl(view, context, dataDB, option, Integer.parseInt(option.getOptionId()), null, type, mode, new OptionControl.UnlockCodeResultListener() {
                                                    @Override
                                                    public void onUnlockCodeSuccess() {
                                                        Log.e("NNK", "Success Вторая опция блокировки есть, а первой - нет. Вторая опция УСПЕШНО запершила работу, значит можно выполнять опцию изначальную");
                                                    }

                                                    @Override
                                                    public void onUnlockCodeFailure() {
                                                        Log.e("NNK", "Failure Вторая опция блокировки есть, а первой - нет. Вторая опция УСПЕШНО запершила работу, значит можно выполнять опцию изначальную");
                                                    }
                                                });
                                                click.click();
                                                break;
                                        }
                                    }
                                }

                                @Override
                                public void onUnlockCodeFailure() {
//                             Проход по первой опции блокировки
                                    if (!option.getOptionBlock1().equals("0")) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            OptionsDB optionsDB = optionFromDetailedReport.stream().filter(optionListItem -> Objects.equals(optionListItem.getOptionId(), option.getOptionBlock1()))
                                                    .findAny()
                                                    .orElse(null);
                                            if (optionsDB != null) {
                                                Log.e("NNK", "3. Проверяю опцмю: " + option.getOptionBlock1());
                                                res += optControl(view, context, dataDB, option, Integer.parseInt(option.getOptionBlock1()), optionsDB, type, NNKMode.BLOCK, new OptionControl.UnlockCodeResultListener() {
                                                    @Override
                                                    public void onUnlockCodeSuccess() {
                                                        Log.e("NNK", "НЕ Успешный Успех. Первая проверку НЕ прошла, вторая прошла, значит не даю делать опцию на которую нажали.");
                                                    }

                                                    @Override
                                                    public void onUnlockCodeFailure() {
                                                        Log.e("NNK", "НЕ Успешный НЕ Успех. Первая проверку НЕ прошла, вторая НЕ прошла, значит не даю делать опцию на которую нажали.");
                                                    }
                                                });
                                                Log.e("NNK", "res OK 1: " + res);
                                            } else {
                                                Log.e("NNK", "Блок 2 не выполнен, а Блок 1 нет в отчёте - Ничего не делаю. (должно отрисовать сообщение Блока2)");
                                            }
                                        }
                                    }
                                }
                            });
                            Log.e("NNK", "res OK 2: " + res);
                        } else {
                            if (!option.getOptionBlock1().equals("0")) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    OptionsDB optionsDBELSE = optionFromDetailedReport.stream().filter(optionListItem -> Objects.equals(optionListItem.getOptionId(), option.getOptionBlock1()))
                                            .findAny()
                                            .orElse(null);
                                    if (optionsDBELSE != null) {
                                        Log.e("NNK", "Проверяю опцмюoptionsDBELSE: " + optionsDBELSE.getOptionBlock1());
                                        res += optControl(view, context, dataDB, option, Integer.parseInt(option.getOptionBlock1()), optionsDBELSE, type, NNKMode.BLOCK, new OptionControl.UnlockCodeResultListener() {
                                            @Override
                                            public void onUnlockCodeSuccess() {
                                                Log.e("NNK", "Успешный Успех. Если первая опция пустая Первая и вторая ОК прошли проверку и должно разрешить работу.");

                                                optControl(view, context, dataDB, option, Integer.parseInt(option.getOptionId()), null, type, mode, new OptionControl.UnlockCodeResultListener() {
                                                    @Override
                                                    public void onUnlockCodeSuccess() {

                                                    }

                                                    @Override
                                                    public void onUnlockCodeFailure() {

                                                    }
                                                });
                                                click.click();
                                            }

                                            @Override
                                            public void onUnlockCodeFailure() {
                                                Log.e("NNK", "2. Успешный НЕ Успех. Первая проверку прошла, вторая не прошла, значит не даю делать опцию на которую нажали.");
                                            }
                                        });
                                        Log.e("NNK", "res OK 1: " + res);
                                    } else {
                                        switch (mode) {
                                            case MAKE:
                                                Log.e("NNK", "3. МПроверяю опцмю: " + option.getOptionBlock1());
                                                optControl(view, context, dataDB, option, Integer.parseInt(option.getOptionId()), null, type, mode, new OptionControl.UnlockCodeResultListener() {
                                                    @Override
                                                    public void onUnlockCodeSuccess() {
                                                        Log.e("NNK", "Блок2 - нет, Блок1 - нет. Success");
                                                    }

                                                    @Override
                                                    public void onUnlockCodeFailure() {
                                                        Log.e("NNK", "Блок2 - нет, Блок1 - нет. Failure");
                                                    }
                                                });
                                                click.click();
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (!option.getOptionBlock1().equals("0")) {
                    //Проход по первой опции блокировки если второй нет
                    if (!option.getOptionBlock1().equals("0")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            OptionsDB optionsDB = optionFromDetailedReport.stream().filter(optionListItem -> Objects.equals(optionListItem.getOptionId(), option.getOptionBlock1()))
                                    .findAny()
                                    .orElse(null);
                            if (optionsDB != null) {
                                Log.e("NNK", "4. Проверяю опцмю: " + option.getOptionBlock1());
                                res += optControl(view, context, dataDB, option, Integer.parseInt(option.getOptionBlock1()), optionsDB, type, NNKMode.BLOCK, new OptionControl.UnlockCodeResultListener() {
                                    @Override
                                    public void onUnlockCodeSuccess() {
                                        Log.e("NNK", "Успешный Успех. Если первая опция пустая Первая и вторая ОК прошли проверку и должно разрешить работу.");
                                        optControl(view, context, dataDB, option, Integer.parseInt(option.getOptionId()), null, type, mode, new OptionControl.UnlockCodeResultListener() {
                                            @Override
                                            public void onUnlockCodeSuccess() {

                                            }

                                            @Override
                                            public void onUnlockCodeFailure() {

                                            }
                                        });
                                        click.click();
                                    }

                                    @Override
                                    public void onUnlockCodeFailure() {
                                        Log.e("NNK", "3. Успешный НЕ Успех. Первая проверку прошла, вторая не прошла, значит не даю делать опцию на которую нажали.");
                                    }
                                });
                                Log.e("NNK", "res OK 1: " + res);
                            } else {
                                switch (mode) {
                                    case MAKE:
                                        Log.e("NNK", "4. МПроверяю опцмю: " + option.getOptionBlock1());
                                        optControl(view, context, dataDB, option, Integer.parseInt(option.getOptionId()), null, type, mode, new OptionControl.UnlockCodeResultListener() {
                                            @Override
                                            public void onUnlockCodeSuccess() {
                                                Log.e("NNK", "Блок 2 не указан вообще, Блок 1 указан, но в отчёте нет, буду делать опцию.");
                                            }

                                            @Override
                                            public void onUnlockCodeFailure() {
                                                Log.e("NNK", "Блок 2 не указан вообще, Блок 1 указан, но в отчёте нет, буду делать опцию.");
                                            }
                                        });
                                        click.click();
                                        break;
                                }
                            }
                        }
                    }
                }

                if ((option.getOptionBlock2().equals("0") || !existOption2) && (option.getOptionBlock1().equals("0") || !existOption)) {
                    switch (mode) {
                        case MAKE:
                            Log.e("NNK", "5. Проверяю опцмю: " + option.getOptionBlock1());
                            optControl(view, context, dataDB, option, Integer.parseInt(option.getOptionId()), null, type, mode, new OptionControl.UnlockCodeResultListener() {
                                @Override
                                public void onUnlockCodeSuccess() {
                                    Log.e("NNK", "onUnlockCodeSuccess Потеряли 2 опции");
                                }

                                @Override
                                public void onUnlockCodeFailure() {
                                    Log.e("NNK", "onUnlockCodeFailure Потеряли 2 опции");
                                }
                            });
                            click.click();
                            break;
                    }
                }
            }
            Log.e("NNK", "-------------END-------------------");
        } catch (Exception e) {
            Log.e("NNK", "Exception e: " + e);
        }

        result = type;
        return result;
    }


    /**
     * 07.08.23.
     * Проверяю первую Опцию блокировки.
     */
    private <T> int checkBlockOption(Context context, T dataDB, OptionsDB option, int parseInt, OptionMassageType type, NNKMode block) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            OptionsDB optionsDB = optionFromDetailedReport.stream().filter(optionListItem -> Objects.equals(optionListItem.getOptionId(), option.getOptionBlock2()))
                    .findAny()
                    .orElse(null);
            if (optionsDB != null) {
                return optControl(context, dataDB, option, Integer.parseInt(option.getOptionBlock2()), optionsDB, type, NNKMode.BLOCK, new OptionControl.UnlockCodeResultListener() {
                    @Override
                    public void onUnlockCodeSuccess() {

                    }

                    @Override
                    public void onUnlockCodeFailure() {

                    }
                });
            }
        }

        return 0;
    }

    /**
     * 15.03.23
     * В нашем случае этот ENUM будет работать как режим для "Проведения документа". На момент
     * написания должно быть 2 режима в которых должна работать эта функция.
     * В первом случае - классическое нажатие на кнопку "Провести", во втором случае это нажатие на
     * снижение в отчёте исполнителя на Главной закладке.
     */
    public enum ConductMode {
        DEFAULT_CONDUCT,        // Первый режим в котором отрабатывает обычный клик по кнопке провести.
        SALARY_CUT              // Клик на Снижение. Отображает только снижающие опции. НЕ начинает проведение.
    }

    /**
     * 23.07.21
     * "Нажатие" на "Провести"
     * <p>
     * Перепроверка всех опций. В зависимости от их статуса (блокирующая или нет) отображает список
     * пользователям (блок) опций и опций со (~снижением)
     * <p>
     * 15.03.23
     * Начал использовать и на закладке "Главная" в отчёте исполнителя. Там, при клика на число
     * снижения, люди должны увидеть какие именно опции им делают снижения. Блокирующие опции
     * отображать не надо (да и особо нет смысла) + не надо отправлять запрос на проведение.
     * Для реализации этого функционала я введу режим специальный и в зависимости от него буду
     * настраивать опцию.
     */
    public void conduct(Context context, WpDataDB wp, List<OptionsDB> options, ConductMode mode, Clicks.click click) {
        try {
            int register = 0;

            DialogData dialog = new DialogData(context);

            SpannableStringBuilder optionsSum = new SpannableStringBuilder();
            double optionSumRes = 0;

            OptionMassageType type = new OptionMassageType();
            type.type = STRING;
            Globals.writeToMLOG("INFO", "Options.conduct", "ConductMode: " + mode.name() +
                    "WpDataDB: " + new Gson().fromJson(new Gson().toJson(wp), JsonObject.class));

            Log.e("!", "ConductMode: " + mode.name() +
                    "WpDataDB: " + new Gson().fromJson(new Gson().toJson(wp), JsonObject.class) +
                    "List<OptionsDB> size: " + options.size());
            for (OptionsDB item : options) {
                Log.e("conduct", "------------------------------START----------------------------------");
                Log.e("conduct", "OptionsDB item.getOptionId(): " + item.getOptionId());
                Log.e("conduct", "OptionsDB item.getOptionTxt(): " + item.getOptionTxt());
                Log.e("conduct", "OptionsDB item.getOptionControlId(): " + item.getOptionControlId());
                Log.e("conduct", "OptionsDB item.getOptionControlTxt(): " + item.getOptionControlTxt());

                // Блокирует опция или нет
                final int[] controlResult = {0};
                optControl(context, wp, item, Integer.parseInt(item.getOptionControlId()), null, type, NNKMode.CHECK, new OptionControl.UnlockCodeResultListener() {
                    @Override
                    public void onUnlockCodeSuccess() {
                        controlResult[0] = 0;
                    }

                    @Override
                    public void onUnlockCodeFailure() {
                        controlResult[0] = 1;
                        Globals.writeToMLOG("INFO", "Options.conduct.onUnlockCodeFailure",
                                "options: " + new Gson().fromJson(new Gson().toJson(item), JsonObject.class));
                    }
                });

                if (new OptionControl<>().checkUnlockCode(item)) {
                    controlResult[0] = 0;
                    Log.e("checkUnlockCode", "item T: " + item.getOptionTxt());
                    Log.e("checkUnlockCode", "item T: " + item.getOptionControlTxt());
                } else {
                    Log.e("checkUnlockCode", "item F: " + item.getOptionTxt());
                    Log.e("checkUnlockCode", "item F: " + item.getOptionControlTxt());
                }

                // Создаю список опций который блокирует
                if (controlResult[0] == 0) {
                    Log.e("conduct", "Опция контроля выполнена: " + controlResult[0]);
                } else if (controlResult[0] == 1 && item.getBlockPns().equals("1")) {
                    Log.e("conduct", "Опция контроля НЕ выполнена: " + controlResult[0]);
                    Log.e("conduct", "Я добавил опцию: " + item.getOptionTxt());
                    optionNotConduct.add(item);
                } else {
                    Log.e("conduct", "Что-то пошло не так: " + controlResult[0]);
                }

                // Если опция описана - добавляю ещё и ДЕНЬГИ в скобочку и считаю итоговую сумму
                if (ArrayUtils.contains(describedOptions, Integer.parseInt(item.getOptionControlId())) && controlResult[0] != 1) {
                    if (item.getIsSignal().equals("1")) {
                        StringBuffer msg = new StringBuffer();
                        optionsSum.append(createLinkedString(dialog,
                                msg.append("* ").append(item.getOptionControlTxt())/*.append(" (").append(counter2Text(wp)).append(")").append("\n")*/, item, click)).append(" ").append(Html.fromHtml("<font color=red>(" + counter2Text(wp) + "грн.)</font>")).append("\n");
                        optionSumRes += wp.getCash_zakaz() * 0.08;
                    }
                }

                Log.e("conduct", "-----------------------------END-----------------------------------");
            }

            Log.e("conduct", "optionNotConduct: " + new Gson().fromJson(new Gson().toJson(optionNotConduct), JsonArray.class));
            Globals.writeToMLOG("INFO", "Options.conduct.block",
                    "optionNotConduct: " + new Gson().fromJson(new Gson().toJson(optionNotConduct), JsonArray.class));

            switch (mode) {
                case SALARY_CUT:
                    dialog.setDialogIco();
                    dialog.setTitle("Вы получаете снижения по следущим опциям: ");

                    SpannableStringBuilder salaryCutText = new SpannableStringBuilder();
                    salaryCutText.append(optionsSum);
                    salaryCutText.append("\n\nВы можете ещё получить: " + "~")
                            .append(String.format("%.2f", optionSumRes))
                            .append("грн, если выполните опции выше.");

                    dialog.setText(salaryCutText, () -> {
                    });
                    dialog.show();

                    break;

                case DEFAULT_CONDUCT:
                    if (!optionNotConduct.isEmpty()) {
                        // Не все опции(действия) выполнены
                        // Не выполнены:
                        dialog.setDialogIco();
                        dialog.setTitle("Не все опции(действия) выполнены.\n\nВаш отчёт не будет проведён и не будет начислена за него оплата!");

                        SpannableStringBuilder resStr = new SpannableStringBuilder();
                        resStr.append("Не выполнены: \n\n");
                        for (OptionsDB item : optionNotConduct) {
                            Log.e("optionNotConduct", "------------------------------START----------------------------------");
                            Log.e("optionNotConduct", "OptionsDB item.getOptionTxt(): " + item.getOptionTxt());
                            Log.e("optionNotConduct", "OptionsDB item.getOptionId(): " + item.getOptionId());
                            Log.e("optionNotConduct", "OptionsDB item.getOptionControlId(): " + item.getOptionControlId());
                            StringBuffer msg = new StringBuffer();
                            resStr.append(createLinkedString(dialog, msg.append("* ").append(item.getOptionControlTxt()), item, click)).append(" ").append(Html.fromHtml("<font color=red>(блок)</font>")).append("\n");
                            Log.e("optionNotConduct", "------------------------------END----------------------------------");
                        }
                        resStr.append(optionsSum);
                        resStr.append("\n\nУстраните указанные ошибки и повторите попытку проведения." + "\n\nВы можете ещё получить: " + "~")
                                .append(String.format("%.2f", optionSumRes)).append("грн, если выполните опции выше.");

                        dialog.setText(resStr, () -> {
                        });
                        dialog.show();

                    } else {
//                        dialog.setTitle("Блокирующие опции не обнаружены.");
//                        dialog.setText("Создаю запрос на проведение документа.");

//                        Toast.makeText(context, "Запрос на проведение создан", Toast.LENGTH_SHORT).show();
//                        DialogData dialogData = new DialogData(context);
//                        dialogData.setClose(dialogData::dismiss);

                        Globals.writeToMLOG("INFO", "Options.conduct",
                                "optionNotConduct: optionNotConduct.isEmpty()");


                        List<StackPhotoDB> res = RealmManager.INSTANCE.copyFromRealm(RealmManager.getStackPhotoPhotoToUpload());
                        if (!res.isEmpty()) {
                            new MessageDialogBuilder(unwrap(context))
                                    .setTitle("## Данные не выгружены на сервер")
                                    .setStatus(DialogStatus.ALERT)
                                    .setMessage("## " +
                                            "Внимание! Перед проведением документа, рекомендуем выполнить процедуру выгрузки Фото отчетов (" + res.size() + " шт.) на Сервер. \n" +
                                            "Пока Вы не выполните эту процедуру, сервер не сможет проверить и провести Ваш отчет.\n" +
                                            "Для того, чтобы выполнить обмен данными с Сервером, нажмите кнопку \"Синхронизация\" на текущей форме, или воспользуйтесь стандартным пунктом в меню.\n" +
                                            "Если связь плохая, найдите место с лучшим приёмом и повторите попытку." +
                                            "")
                                    .setOnCancelAction(() -> Unit.INSTANCE)
                                    .setOnConfirmAction("Cинхронизация", () -> {
                                                new PhotoReports(context).uploadPhotoReports(PhotoReports.UploadType.AUTO);
                                                new TablesLoadingUnloading().uploadReportPrepareToServer();
                                                new Exchange().startExchange();
                                                return Unit.INSTANCE;
                                            }
                                    )
                                    .show();
                        } else {
//                            new PhotoReports(context).uploadPhotoReports(PhotoReports.UploadType.AUTO);
                            new TablesLoadingUnloading().uploadReportPrepareToServer();
                            Exchange.conductingOnServerWpData(wp, wp.getCode_dad2(), new Click() {
                                @Override
                                public <T> void onSuccess(T data) {
//                                    dialogData.setTitle("Команда на проведення звіту. ");
                                    try {
//                                        Spanned spanned = Html.fromHtml((String) data);
                                        Globals.writeToMLOG("INFO", "Options/conductingOnServerWpData/onSuccess", "data: " + data);

                                        if (data instanceof String)
                                            new MessageDialogBuilder(unwrap(context))
                                                    .setTitle("Команда на проведення звіту.")
                                                    .setSubTitle("Відповідь від сервера")
                                                    .setMessage((String) data)
                                                    .setOnConfirmAction(() -> {
                                                        if (((String) data).contains("#134583")) {
                                                            DialogData dialogData = new DialogData(context);
                                                            dialogData.setTitle("Если Вы видите это сообщение то, скорее всего, Вам надо просто повторить попытку проведения через пару минут, для того, чтобы сервер успел проверить полученную от приложения информацию (фото и пр. данные).");
                                                            dialogData.show();
                                                        }
                                                        return Unit.INSTANCE;
                                                    })
                                                    .show();
                                        else
                                            new MessageDialogBuilder(unwrap(context))
                                                    .setTitle("Команда на проведення звіту.")
                                                    .setStatus(DialogStatus.NORMAL)
                                                    .setSubTitle("Надіслано на сервер")
                                                    .setMessage("Запит на проведення звіту успішно прийнятий сервером та буде автоматично проведений")
                                                    .setOnConfirmAction(() -> {
                                                        return Unit.INSTANCE;
                                                    })
                                                    .show();
//                                    String regex = "- ?[Oo]пц[иi]я";
//                                    // Компилируем регулярное выражение
//                                    Pattern pattern = Pattern.compile(regex);
//                                    Matcher matcher = pattern.matcher(spanned);
//
//                                    // Строим новую строку с заменами
//                                    StringBuffer result = new StringBuffer();
//                                    while (matcher.find()) {
//                                        matcher.appendReplacement(result, "\n" + matcher.group());
//                                    }
//                                    matcher.appendTail(result);

//                                        dialogData.setText(spanned);
                                    } catch (Exception e) {
                                        Globals.writeToMLOG("ERROR", "Options/conductingOnServerWpData/onSuccess", "Exception e: " + e);
                                    }
//                                    dialogData.show();
                                }

                                @Override
                                public void onFailure(String error) {
                                    Globals.writeToMLOG("ERROR", "Exchange.conductingOnServerWpData", "error: " + error);
                                    new MessageDialogBuilder(unwrap(context))
                                            .setTitle("Проведення звіту...")
                                            .setStatus(DialogStatus.ERROR)
                                            .setMessage("Зараз передати команду на проведення звіту на сервер не вдалося. Але ця команда збережена на вашому пристрої та буде передана на сервер під час наступного обміну данними." +
                                                    "<br> Ответ от сервера: " + error)
                                            .setOnConfirmAction(() -> Unit.INSTANCE)
                                            .show();

//                                    dialogData.setTitle("Проведення звіту...");
//                                    dialogData.setText("Зараз передати команду на проведення звіту на сервер не вдалося. Але ця команда збережена на вашому пристрої та буде передана на сервер під час наступного обміну данними.");
//                                    dialogData.show();

                                    RealmManager.INSTANCE.executeTransaction(realm -> {
                                        if (wp != null) {
                                            wp.startUpdate = true;
                                            wp.setSetStatus(1);
                                            wp.setDt_update(System.currentTimeMillis() / 1000);
                                            realm.insertOrUpdate(wp);
                                        }
                                    });
                                }
                            });
                            new Exchange().startExchange();

                        }
                    }
                    break;

                default:
                    break;
            }

//            dialog.setClose(dialog::dismiss);
//            dialog.show();

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "Options/conduct/catch", "Exception e: " + e);
        }
    }

    private SpannableString createLinkedString(DialogData dialogData, StringBuffer msg, OptionsDB item, Clicks.click click) {
        SpannableString res = new SpannableString(msg);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                click.click(item);
                Toast.makeText(textView.getContext(), "Боооожечки, Ви не виконали опцію: " + item.getOptionControlTxt(), Toast.LENGTH_LONG).show();
                dialogData.dismiss();
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

    private CharSequence counter2Text(WpDataDB wpDataDB) {
        CharSequence res = "";
        res = "~" + String.format("%.2f", wpDataDB.getCash_zakaz() * 0.08);
        res = Html.fromHtml("<font color=red>" + res + "</font>");
        return res;
    }

    public <T> int optControl(Context context, T dataDB, OptionsDB optionCurrent, int optionId, OptionsDB optionBlock, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        return optControl(null, context, dataDB, optionCurrent, optionId, optionBlock, type, mode, unlockCodeResultListener);
    }

    /* Проверка Опции - кнопка, провести отчет */
    public <T> int optControl(View view, Context context, T dataDB, OptionsDB optionCurrent, int optionId, OptionsDB optionBlock, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        OptionsDB option = optionCurrent;   // Текущая Опция на которую нажали

        String block = "";
        if (mode.equals(NNKMode.BLOCK)) {
            block = "Ви не можете натиснути на кнопку [" + optionCurrent.getOptionTxt().replace("Кнопка ", "") + "] поки не виконаєте наступну вимогу: \n\n";
            option = optionBlock;   // Мой стыд, где я для того что б всё работало подменяю текущую опцию по которой кликнули - на опцию блокировки
        }

//        try {
        Log.e("NNK", "F/optControl/optionId: " + optionId);
        Log.e("NNK", "F/New/optControl/getOptionId: " + optionCurrent.getOptionId());
        Log.e("NNK", "F/New/optControl/getOptionTxt: " + optionCurrent.getOptionTxt());
        Log.e("NNK", "F/New/optControl/getOptionControlId: " + optionCurrent.getOptionControlId());
        Log.e("NNK", "F/New/optControl/getOptionControlTxt: " + optionCurrent.getOptionControlTxt());

        Log.e("NNK", "F/optControl/NNKMode mode: " + mode);
        switch (optionId) {

//            case 156882:
//                if (optionCurrent.getOptionControlId().equals("80977")) {
//                    OptionControlPromotion<?> optionControlPromotion = new OptionControlPromotion<>(context, dataDB, option, type, mode, unlockCodeResultListener);
//                    if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlPromotion.isBlockOption()))
//                        optionControlPromotion.showOptionMassage(block);
//                    if (mode.equals(NNKMode.BLOCK) && optionControlPromotion.signal && optionControlPromotion.isBlockOption()) {
//                        optionControlPromotion.showOptionMassage(block);
//                    }
//
//                    return optionControlPromotion.isBlockOption2() ? 1 : 0;
//                } else if (optionCurrent.getOptionControlId().equals("157288")) {
//                    OptionControlPromotionTov<?> optionControlPromotionTov = new OptionControlPromotionTov<>(context, dataDB, option, type, mode, unlockCodeResultListener);
//                    if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlPromotionTov.isBlockOption()))
//                        optionControlPromotionTov.showOptionMassage(block);
//
//                    if (mode.equals(NNKMode.BLOCK) && optionControlPromotionTov.signal && optionControlPromotionTov.isBlockOption()) {
//                        optionControlPromotionTov.showOptionMassage(block);
//                    }
//                    return optionControlPromotionTov.isBlockOption2() ? 1 : 0;
//                }
//                break;

//            case 157288:
//                OptionControlPromotionTov<?> optionControlPromotionTov = new OptionControlPromotionTov<>(context, dataDB, option, type, mode, unlockCodeResultListener);
//                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlPromotionTov.isBlockOption()))
//                    optionControlPromotionTov.showOptionMassage(block);
//
//                if (mode.equals(NNKMode.BLOCK) && optionControlPromotionTov.signal && optionControlPromotionTov.isBlockOption()) {
//                    optionControlPromotionTov.showOptionMassage(block);
//                }
//                return optionControlPromotionTov.isBlockOption2() ? 1 : 0;


            case 165481:
                OptionButtonPhotoEFFIE<?> optionButtonPhotoEFFIE = new OptionButtonPhotoEFFIE<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 84005, 84967, 164985, 165276:
                OptionControlCheckingForAnAchievement<?> optionControlCheckingForAnAchievement = new OptionControlCheckingForAnAchievement<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlCheckingForAnAchievement.isBlockOption()))
                    optionControlCheckingForAnAchievement.showOptionMassage(block);

                if (mode.equals(NNKMode.BLOCK) && optionControlCheckingForAnAchievement.signal && optionControlCheckingForAnAchievement.isBlockOption()) {
                    optionControlCheckingForAnAchievement.showOptionMassage(block);
                }
                return optionControlCheckingForAnAchievement.isBlockOption2() ? 1 : 0;

            case 132812:    // Хочу увеличение оплаты

                WpDataDB wp = (WpDataDB) dataDB;

                String link = String.format("/merchik.com.ua/mobile.php?mod=ticket&act=create&doc_num=%s&doc_type=report&theme_id=%s&option_id=132812&menu_close_only"
                        , wp.getDoc_num(), wp.getTheme_id());
                AppUsersDB appUser = AppUserRealm.getAppUserById(userId);
                String hash = String.format("%s%s%s", appUser.getUserId(), appUser.getPassword(), "AvgrgsYihSHp6Ok9yQXfSHp6Ok9nXdXr3OSHp6Ok9UPBTzTjrF20Nsz3");
                hash = Globals.getSha1Hex(hash);

                link = link.replace("&", "**");

                String format = String.format("https://merchik.com.ua/sa.php?&u=%s&s=%s&l=/%s", userId, hash, link);

                Intent site = new Intent(Intent.ACTION_VIEW, Uri.parse(format));
                context.startActivity(site);
                break;
            case 164351:
                OptionButtonPhotoCassZone<?> optionButtonPhotoCassZone = new OptionButtonPhotoCassZone<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 160567:
            case 160568:
                OptionControlPhotoShowcase<?> optionControlPhotoShowcase =
                        new OptionControlPhotoShowcase<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlPhotoShowcase.isBlockOption()))
                    optionControlPhotoShowcase.showOptionMassage(block);

                if (mode.equals(NNKMode.BLOCK) && optionControlPhotoShowcase.signal && optionControlPhotoShowcase.isBlockOption()) {
                    optionControlPhotoShowcase.showOptionMassage(block);
                }
                return optionControlPhotoShowcase.isBlockOption2() ? 1 : 0;

            case 143969:
                Log.e("DR_BUTTON_CLICK", "optionId: " + optionId);
                //  Взятие ЭКЛ-а
                DialogEKL dialogEKL = new DialogEKL(context, (WpDataDB) dataDB);
                dialogEKL.setTitle("Электронный Контрольный Лист (ЭКЛ)");

                dialogEKL.setLesson(context, true, 1273);
                dialogEKL.setVideoLesson(context, true, 1274, () -> {
                });
                dialogEKL.setImgBtnCall(context);
                dialogEKL.setClose(dialogEKL::dismiss);
                dialogEKL.show();
                break;

            case 135719:    // КНОПКА "Дет.Отчет" (оценка)
            case 135708:    // КОНТРОЛЬ
                OptionControlCheckMarkDetailedReport<?> optionControlCheckMarkDetailedReport =
                        new OptionControlCheckMarkDetailedReport<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                optionControlCheckMarkDetailedReport.showOptionMassage(block);
                break;

            case 135413:    // КНОПКА "Фото Витрины (Оценка)"
            case 135595:    // КОНТРОЛЬ
                OptionControlCheckMarkPhotoReport<?> optionControlCheckMarkPhotoReport =
                        new OptionControlCheckMarkPhotoReport<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                optionControlCheckMarkPhotoReport.showOptionMassage(block);
                break;

            case 165275:
            case 159799:// Кнопка "Возврат"
            case 135591:// Выполняется проверка НАЛИЧИЯ данных о количестве ВОЗВРАТА товара или запись в поле "ошибка" о том, что его "возвращать НЕ нужно".
                OptionControlReturnOfGoods<?> optionControlReturnOfGoods =
                        new OptionControlReturnOfGoods<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlReturnOfGoods.isBlockOption()))
                    optionControlReturnOfGoods.showOptionMassage(block);

                if (mode.equals(NNKMode.BLOCK) && optionControlReturnOfGoods.signal && optionControlReturnOfGoods.isBlockOption()) {
                    optionControlReturnOfGoods.showOptionMassage(block);
                }
                return optionControlReturnOfGoods.isBlockOption2() ? 1 : 0;

//            case 132971:  // Проверка наличия Фото тележка с товаром (тип 10)
//                OptionControlPhotoCartWithGoods<?> optionControlPhotoCartWithGoods =
//                        new OptionControlPhotoCartWithGoods<>(context, dataDB, option, type, mode, unlockCodeResultListener);
//                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlPhotoCartWithGoods.isBlockOption()))
//                    optionControlPhotoCartWithGoods.showOptionMassage(block);
//                if (mode.equals(NNKMode.BLOCK) && optionControlPhotoCartWithGoods.signal && optionControlPhotoCartWithGoods.isBlockOption()) {
//                    optionControlPhotoCartWithGoods.showOptionMassage(block);
//                }
//                return optionControlPhotoCartWithGoods.isBlockOption2() ? 1 : 0;

            case 135158:
                try {
                    WpDataDB wpdata = (WpDataDB) dataDB;

                    AddressSDB addr = SQL_DB.addressDao().getById(wpdata.getAddr_id());
                    TradeMarkDB tradeMarkDB = TradeMarkRealm.getTradeMarkRowById(String.valueOf(addr.tpId));
                    String tradeMarkId = tradeMarkDB == null ? "" : tradeMarkDB.getID();

                    Intent intent = new Intent(context, FeaturesActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("viewModel", SamplePhotoSDBViewModel.class.getCanonicalName());
                    bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_135158.toString());
                    JsonObject dataJson = new JsonObject();
                    dataJson.addProperty("tradeMarkDBId", tradeMarkId);
                    dataJson.addProperty("wpDataDBId", String.valueOf(wpdata.getId()));
                    dataJson.addProperty("optionDBId", String.valueOf(option.getID()));
                    bundle.putString("dataJson", new Gson().toJson(dataJson));
                    bundle.putString("title", R.string.title_samplephotosdb + ", ");
                    bundle.putString("subTitle", "В списке представлены образцы фотоотчетов. " +
                            "Для того, чтобы изготовить 'Фото Остатков Товаров (ФОТ)' нажмите на соответствующую фотографию. " +
                            "Затем увеличьте ее до размера экрана и выполните фото, нажав на кнопку фотоаппарата в правом нижнем углу. ");
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Log.e("2222", "error", e);
                }

//                OptionButtonPhotoFOT<?> optionButtonPhotoFOT = new OptionButtonPhotoFOT<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 159725:
                OptionButtonPhotoTT<?> optionButtonPhotoTT = new OptionButtonPhotoTT<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 159707:
                OptionControlAvailabilityControlPhotoRemainingGoods<?> optionControlAvailabilityControlPhotoRemainingGoods =
                        new OptionControlAvailabilityControlPhotoRemainingGoods<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlAvailabilityControlPhotoRemainingGoods.isBlockOption()))
                    optionControlAvailabilityControlPhotoRemainingGoods.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlAvailabilityControlPhotoRemainingGoods.signal && optionControlAvailabilityControlPhotoRemainingGoods.isBlockOption()) {
                    optionControlAvailabilityControlPhotoRemainingGoods.showOptionMassage(block);
                }
                return optionControlAvailabilityControlPhotoRemainingGoods.isBlockOption2() ? 1 : 0;

            case 135412:
//                new OptionButtonPercentageOfThePrize<>(context, dataDB, option, type, mode);

                DialogData dialogData = new DialogData(context);
                dialogData.setTitle(option.getOptionTxt());
                dialogData.setText(option.getOptionDescr());
                dialogData.setClose(dialogData::dismiss);
                dialogData.show();

                break;

            case 1455:
                OptionControlCheckingPercentageOfShelfSpaceDPPO<?> optionControlCheckingPercentageOfShelfSpaceDPPO =
                        new OptionControlCheckingPercentageOfShelfSpaceDPPO<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlCheckingPercentageOfShelfSpaceDPPO.isBlockOption()))
                    optionControlCheckingPercentageOfShelfSpaceDPPO.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlCheckingPercentageOfShelfSpaceDPPO.signal && optionControlCheckingPercentageOfShelfSpaceDPPO.isBlockOption()) {
                    optionControlCheckingPercentageOfShelfSpaceDPPO.showOptionMassage(block);
                }
                return optionControlCheckingPercentageOfShelfSpaceDPPO.isBlockOption2() ? 1 : 0;

            case 135061:
//
//                OptionControlCheckingPercentageOfShelfSpaceDPPO<?> test = new OptionControlCheckingPercentageOfShelfSpaceDPPO<>(context, dataDB, option, type, mode);
//                test.showOptionMassage();

                OptionControlPercentageOfThePrize<?> optionControlPercentageOfThePrize =
                        new OptionControlPercentageOfThePrize<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlPercentageOfThePrize.isBlockOption()))
                    optionControlPercentageOfThePrize.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlPercentageOfThePrize.signal && optionControlPercentageOfThePrize.isBlockOption()) {
                    optionControlPercentageOfThePrize.showOptionMassage(block);
                }
                return optionControlPercentageOfThePrize.isBlockOption2() ? 1 : 0;

            // Контроль фотоотчётов
            case 151594:    // 20.01 перевел Контроль наличия фото витрины (до начала работ) на общую схему
            case 132971:    // Проверка наличия Фото тележка с товаром (тип 10)
            case 134583:
            case 141361:
            case 158606:
            case 158607:
            case 158608:
            case 158609:
            case 84932:     // Проверка наличия ФотоОтчётов (id мне дали из 1С) (тип 0)
            case 159726:    // Фото торговой точки
            case 165482:
            case 164354:    // Фото Планограмми ТТ
            case 164352:    // Контроль наявності світлини прикасової зони
            case 169109:    // Контроль наличия Фото POS материалов
//            case 159725:    // Кнопка "Фото Торговой Точки (ФТТ)"
                OptionControlPhoto<?> optionControlPhoto = new OptionControlPhoto<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlPhoto.isBlockOption()))
                    optionControlPhoto.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlPhoto.signal && optionControlPhoto.isBlockOption()) {
                    optionControlPhoto.showOptionMassage(block);
                }
                return optionControlPhoto.isBlockOption2() ? 1 : 0;

            case 1470:
                OptionControlPhotoTovarsLeft<?> optionControlPhotoTovarsLeft =
                        new OptionControlPhotoTovarsLeft<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlPhotoTovarsLeft.isBlockOption()))
                    optionControlPhotoTovarsLeft.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlPhotoTovarsLeft.signal && optionControlPhotoTovarsLeft.isBlockOption()) {
                    optionControlPhotoTovarsLeft.showOptionMassage(block);
                }
                return optionControlPhotoTovarsLeft.isBlockOption2() ? 1 : 0;

            case 158361:
                OptionControlPhotoTovarsLeftClient<?> optionControlPhotoTovarsLeftClient =
                        new OptionControlPhotoTovarsLeftClient<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlPhotoTovarsLeftClient.isBlockOption()))
                    optionControlPhotoTovarsLeftClient.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlPhotoTovarsLeftClient.signal && optionControlPhotoTovarsLeftClient.isBlockOption()) {
                    optionControlPhotoTovarsLeftClient.showOptionMassage(block);
                }
                return optionControlPhotoTovarsLeftClient.isBlockOption2() ? 1 : 0;

            case 138644:
                OptionControlCheckTovarUp<?> optionControlCheckTovarUp = new OptionControlCheckTovarUp<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlCheckTovarUp.isBlockOption()))
                    optionControlCheckTovarUp.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlCheckTovarUp.signal && optionControlCheckTovarUp.isBlockOption()) {
                    optionControlCheckTovarUp.showOptionMassage(block);
                }
                return optionControlCheckTovarUp.isBlockOption2() ? 1 : 0;

            case 157352:
                OptionControlCheckDetailedReport<?> optionControlCheckDetailedReport =
                        new OptionControlCheckDetailedReport<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlCheckDetailedReport.isBlockOption()))
                    optionControlCheckDetailedReport.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlCheckDetailedReport.signal && optionControlCheckDetailedReport.isBlockOption()) {
                    optionControlCheckDetailedReport.showOptionMassage(block);
                }
                return optionControlCheckDetailedReport.isBlockOption2() ? 1 : 0;

            case 590:
            case 160209:
                OptionControlAchievements<?> optionControlAchievements = new OptionControlAchievements<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK)) && optionControlAchievements.isBlockOption()) {
                    optionControlAchievements.showOptionMassage(block);
                }
                if (mode.equals(NNKMode.BLOCK) && optionControlAchievements.signal && optionControlAchievements.isBlockOption()) {
                    optionControlAchievements.showOptionMassage(block);
                }
                return optionControlAchievements.isBlockOption2() ? 1 : 0;

            case 135159:
                OptionButtAchievements<?> optionButtAchievements = new OptionButtAchievements<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 157274:
            case 157275:
                OptionControlFacePlan<?> optionControlFacePlan = new OptionControlFacePlan<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlFacePlan.isBlockOption()))
                    optionControlFacePlan.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlFacePlan.signal && optionControlFacePlan.isBlockOption()) {
                    optionControlFacePlan.showOptionMassage(block);
                }
                return optionControlFacePlan.isBlockOption2() ? 1 : 0;

            case 84006:
            case 143968:
                OptionControlEKL<?> optionControlEKL = new OptionControlEKL<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlEKL.isBlockOption())) {
                    optionControlEKL.showOptionMassage(block);
//                    return optionControlEKL.isBlockOption2() ? 1 : 0;
                }

//                if (mode.equals(NNKMode.BLOCK)) {
                if (mode.equals(NNKMode.BLOCK) && optionControlEKL.signal && optionControlEKL.isBlockOption()) {
                    optionControlEKL.showOptionMassage(block);
//                    return optionControlEKL.isBlockOption2() ? 1 : 0;
                }

                return optionControlEKL.isBlockOption2() ? 1 : 0;
//                break;


            case 133381:
                OptionControlRegistrationPotentialClient<?> optionControlRegistrationPotentialClient =
                        new OptionControlRegistrationPotentialClient<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlRegistrationPotentialClient.isBlockOption()))
                    optionControlRegistrationPotentialClient.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlRegistrationPotentialClient.signal && optionControlRegistrationPotentialClient.isBlockOption()) {
                    optionControlRegistrationPotentialClient.showOptionMassage(block);
                }
                return optionControlRegistrationPotentialClient.isBlockOption2() ? 1 : 0;


            // Потенциальный клиент
            case 133382:
                OptionButtonAddNewClient<?> optionButtonAddNewClient = new OptionButtonAddNewClient<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 136101:
                OptionControlRegistrationPotentialFriend<?> optionControlRegistrationPotentialFriend =
                        new OptionControlRegistrationPotentialFriend<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlRegistrationPotentialFriend.isBlockOption()))
                    optionControlRegistrationPotentialFriend.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlRegistrationPotentialFriend.signal && optionControlRegistrationPotentialFriend.isBlockOption()) {
                    optionControlRegistrationPotentialFriend.showOptionMassage(block);
                }
                return optionControlRegistrationPotentialFriend.isBlockOption2() ? 1 : 0;

            // Пригласи друга
            case 136100:
                OptionButtonAddNewFriend<?> optionButtonAddNewFriend = new OptionButtonAddNewFriend<>(view, context, dataDB, option, type, mode, unlockCodeResultListener);
                break;


            case 157243:
                OptionControlCheckingReasonOutOfStockOSV<?> optionControlCheckingReasonOutOfStockOSV =
                        new OptionControlCheckingReasonOutOfStockOSV<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (optionControlCheckingReasonOutOfStockOSV.isBlockOption()) {
                    if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlCheckingReasonOutOfStockOSV.isBlockOption()))
                        optionControlCheckingReasonOutOfStockOSV.showOptionMassage(block);
                    if (mode.equals(NNKMode.BLOCK) && optionControlCheckingReasonOutOfStockOSV.signal && optionControlCheckingReasonOutOfStockOSV.isBlockOption()) {
                        optionControlCheckingReasonOutOfStockOSV.showOptionMassage(block);
                    }
                }
                return optionControlCheckingReasonOutOfStockOSV.isBlockOption2() ? 1 : 0;

            case 157242:
            case 157241:
                OptionControlCheckingReasonOutOfStock<?> optionControlCheckingReasonOutOfStock =
                        new OptionControlCheckingReasonOutOfStock<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlCheckingReasonOutOfStock.isBlockOption()))
                    optionControlCheckingReasonOutOfStock.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlCheckingReasonOutOfStock.signal && optionControlCheckingReasonOutOfStock.isBlockOption()) {
                    optionControlCheckingReasonOutOfStock.showOptionMassage(block);
                }
                return optionControlCheckingReasonOutOfStock.isBlockOption2() ? 1 : 0;

            case 135809:
                new OptionButtonPhotoBeforeStartWork<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 132969:
                new OptionButtonPhotoOfACartWithGoods<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

//            case 151594:
//                OptionControlPhotoBeforeStartWork<?> optionControlPhotoBeforeStartWork =
//                        new OptionControlPhotoBeforeStartWork<>(context, dataDB, option, type, mode, unlockCodeResultListener);
//                if (optionControlPhotoBeforeStartWork.isBlockOption()) {
//                    if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlPhotoBeforeStartWork.isBlockOption()))
//                        optionControlPhotoBeforeStartWork.showOptionMassage(block);
//                    if (mode.equals(NNKMode.BLOCK) && optionControlPhotoBeforeStartWork.signal && optionControlPhotoBeforeStartWork.isBlockOption()) {
//                        optionControlPhotoBeforeStartWork.showOptionMassage(block);
//                    }
//                }
//                return optionControlPhotoBeforeStartWork.isBlockOption2() ? 1 : 0;

            case 135328:
                OptionButtonReclamationAnswer<?> optionButtonReclamationAnswer =
                        new OptionButtonReclamationAnswer<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 135330:
                OptionControlReclamationAnswer<?> optionControlReclamationAnswer =
                        new OptionControlReclamationAnswer<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlReclamationAnswer.isBlockOption()))
                    optionControlReclamationAnswer.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlReclamationAnswer.signal && optionControlReclamationAnswer.isBlockOption()) {
                    optionControlReclamationAnswer.showOptionMassage(block);
                }
                return optionControlReclamationAnswer.isBlockOption2() ? 1 : 0;

            case 132624:
                OptionControlAddComment<?> optionControlAddComment = new OptionControlAddComment<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlAddComment.isBlockOption()))
                    optionControlAddComment.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlAddComment.signal && optionControlAddComment.isBlockOption()) {
                    optionControlAddComment.showOptionMassage(block);
                }
                return optionControlAddComment.isBlockOption2() ? 1 : 0;

            case 84001:
                OptionControlAddOpinion<?> optionControlAddOpinion = new OptionControlAddOpinion<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlAddOpinion.isBlockOption()))
                    optionControlAddOpinion.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlAddOpinion.signal && optionControlAddOpinion.isBlockOption()) {
                    optionControlAddOpinion.showOptionMassage(block);
                }
                return optionControlAddOpinion.isBlockOption2() ? 1 : 0;

            case 168439:
                OptionControlPlanorammVizit<?> optionControlPlanorammVizit = new OptionControlPlanorammVizit<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlPlanorammVizit.isBlockOption()))
                    optionControlPlanorammVizit.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlPlanorammVizit.signal && optionControlPlanorammVizit.isBlockOption()) {
                    optionControlPlanorammVizit.showOptionMassage(block);
                }
                return optionControlPlanorammVizit.isBlockOption2() ? 1 : 0;

            case 141893:
                OptionControlOpinionByController<?> optionControlOpinionByController = new OptionControlOpinionByController<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlOpinionByController.isBlockOption()))
                    optionControlOpinionByController.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlOpinionByController.signal && optionControlOpinionByController.isBlockOption()) {
                    optionControlOpinionByController.showOptionMassage(block);
                }
                return optionControlOpinionByController.isBlockOption2() ? 1 : 0;

            case 132623:
                OptionButtonAddComment<?> optionButtonAddComment = new OptionButtonAddComment<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 76815:
                OptionControlAvailabilityDetailedReport optionControlAvailabilityDetailedReport =
                        new OptionControlAvailabilityDetailedReport(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (optionControlAvailabilityDetailedReport.isBlockOption()) {
                    if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlAvailabilityDetailedReport.isBlockOption()))
                        optionControlAvailabilityDetailedReport.showOptionMassage(block);
                    if (mode.equals(NNKMode.BLOCK) && optionControlAvailabilityDetailedReport.signal && optionControlAvailabilityDetailedReport.isBlockOption()) {
                        optionControlAvailabilityDetailedReport.showOptionMassage(block);
                    }
                }
                return optionControlAvailabilityDetailedReport.isBlockOption2() ? 1 : 0;

            case 151139:
                new OptionButtonPlanogrammVizit<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;
            case 164355:
                new OptionButPhotoPlanogramm<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 158309:
                new OptionButtonPhotoShowcaseNear<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 158604:
                new OptionButtonPhotoShowcaseCorporateBlock<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 158605:
                new OptionButtonPhotoShowcaseFullness<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 157277:
                new OptionButtonPhotoAktionTovar<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 157354:
                new OptionButtonPhotoDMP<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 169108:
                new OptionButtonPhotoPOS<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 80977:     // Контроль Акций
            case 156882:    // Кнопка Акций
            case 157288:
                OptionControlPromotion<?> optionControlPromotion = new OptionControlPromotion<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlPromotion.isBlockOption()))
                    optionControlPromotion.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlPromotion.signal && optionControlPromotion.isBlockOption()) {
                    optionControlPromotion.showOptionMassage(block);
                }

                return optionControlPromotion.isBlockOption2() ? 1 : 0;

            case 157278:
            case 166528:
                OptionControlPhotoPromotion<?> optionControlPhotoPromotion =
                        new OptionControlPhotoPromotion<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (optionControlPhotoPromotion.isBlockOption()) {
                    if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlPhotoPromotion.isBlockOption()))
                        optionControlPhotoPromotion.showOptionMassage(block);
                    if (mode.equals(NNKMode.BLOCK) && optionControlPhotoPromotion.signal && optionControlPhotoPromotion.isBlockOption()) {
                        optionControlPhotoPromotion.showOptionMassage(block);
                    }
                }
                return optionControlPhotoPromotion.isBlockOption2() ? 1 : 0;

            case 156928:
                OptionControlEndAnotherWork optionControlEndAnotherWork =
                        new OptionControlEndAnotherWork(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlEndAnotherWork.isBlockOption()))
                    optionControlEndAnotherWork.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlEndAnotherWork.signal && optionControlEndAnotherWork.isBlockOption()) {
                    optionControlEndAnotherWork.showOptionMassage(block);
                }
                return optionControlEndAnotherWork.isBlockOption2() ? 1 : 0;

            case 135327: // Задача
                new OptionButtonTaskAnswer<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 135329:
                OptionControlTaskAnswer optionControlTask = new OptionControlTaskAnswer(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlTask.isBlockOption()))
                    optionControlTask.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlTask.signal && optionControlTask.isBlockOption()) {
                    optionControlTask.showOptionMassage(block);
                }
                return optionControlTask.isBlockOption2() ? 1 : 0;

            // Эти 2 в принципе разные, но для меня на данный момент они занимаются одним и тем же
            case 135742:// Дет. Отчёт по КлиентоАдресу
            case 137797:// Дет. Отчёт по Дад2
//                option135742(context, dataDB, option, type, mode);
                new OptionButtonAvailabilityDetailedReport(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 132621:   // Оценка
                option132621(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 84003:     // Мнение о сотруднике
                option84003(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 138339:
                if (dataDB instanceof WpDataDB) {
                    option138339(context, dataDB, option, type, mode, unlockCodeResultListener);
                } else if (dataDB instanceof TasksAndReclamationsSDB) {
                    option138339(context, dataDB, option, type, mode, unlockCodeResultListener);
                }
                break;

            // ---

            case 168598:
                new OptionButtonUserOpinion<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 138773:
                new OptionButtonHistoryMP<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 138518:
                new OptionButtonStartWork<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;

            case 138519:
                return optionControlStartWork_138519(context, dataDB, option, type, mode, unlockCodeResultListener) ? 0 : 1;

            case 138520:
                if (dataDB instanceof WpDataDB && ((WpDataDB) dataDB).getVisit_end_dt() > 0)
                    new MessageDialogBuilder(unwrap(context))
                            .setTitle(context.getString(R.string.your_click))
                            .setSubTitle(context.getString(R.string.end_work))
                            .setStatus(DialogStatus.ALERT)
                            .setMessage("Работа вже завершена")
                            .setOnConfirmAction(() -> Unit.INSTANCE)
                            .show();
                else {
                    OptionsDB finalOption = option;
                    long timeInMillis = System.currentTimeMillis();
                    Globals.writeToMLOG("INFO", "Options.clicked:138520", "currentTimeMillis: " + timeInMillis);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String currentTime = sdf.format(new Date(timeInMillis));
                    new MessageDialogBuilder(unwrap(context))
                            .setTitle(context.getString(R.string.your_click))
                            .setSubTitle(context.getString(R.string.end_work))
                            .setStatus(DialogStatus.ALERT)
                            .setMessage("Зареєструвати закінчення робіт у " + currentTime)
                            .setOnConfirmAction("TAK", () -> {
                                if (dataDB instanceof WpDataDB) {
                                    optionEndWork_138520(context, (WpDataDB) dataDB, finalOption, type, mode, unlockCodeResultListener);
                                } else if (dataDB instanceof TasksAndReclamationsSDB) {
                                    optionEndWork_138520(context, (TasksAndReclamationsSDB) dataDB, finalOption, type, mode, unlockCodeResultListener);
                                }
                                return Unit.INSTANCE;
                            })
                            .setOnCancelAction("Hi", () -> Unit.INSTANCE)
                            .show();
                }
                break;

            case 138521:
                return optionControlEndWork_138521(context, dataDB, option, type, mode, unlockCodeResultListener) ? 0 : 1;

            case 158308:
            case 132968:
                if (dataDB instanceof WpDataDB) {
                    optionMakePhoto0_132968(context, (WpDataDB) dataDB, option, type, mode, unlockCodeResultListener);
                } else if (dataDB instanceof TasksAndReclamationsSDB) {
                    optionMakePhoto0_132968(context, dataDB, option, type, mode, unlockCodeResultListener);
                }
                break;

            // --- Опция контроля на Получение заказа в ТТ
            case 587:
                return optionControlReceivingAnOrder_587(context, dataDB, option, type, mode, unlockCodeResultListener) ? 1 : 0;

            case 8299:
//                optionControlMP_8299(context, dataDB, option, type, mode, unlockCodeResultListener);
                OptionControlMP optionControlMP = new OptionControlMP(context, dataDB, option, type, mode, unlockCodeResultListener);
                optionControlMP.showMassage(false, new Clicks.clickStatusMsg() {
                    @Override
                    public void onSuccess(String data) {

                    }

                    @Override
                    public void onFailure(String error) {

                    }
                });
//                optionControlMP.showOptionMassage("");

                return optionControlMP.isBlockOption2() ? 1 : 0;


            // Контроль Опции Доп. Требований
            case 138341:
                try {
                    OptionControlAdditionalRequirementsMark<?> optionControlAdditionalRequirementsMark =
                            new OptionControlAdditionalRequirementsMark<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                    if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlAdditionalRequirementsMark.isBlockOption()))
                        optionControlAdditionalRequirementsMark.showOptionMassage(block);
                    if (mode.equals(NNKMode.BLOCK) && optionControlAdditionalRequirementsMark.signal && optionControlAdditionalRequirementsMark.isBlockOption()) {
                        optionControlAdditionalRequirementsMark.showOptionMassage(block);
                    }
                    return optionControlAdditionalRequirementsMark.isBlockOption2() ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }

            case 138342:
                try {
                    OptionControlAdditionalMaterialsMark<?> optionControlAdditionalMaterialsMark =
                            new OptionControlAdditionalMaterialsMark<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                    if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlAdditionalMaterialsMark.isBlockOption()))
                        optionControlAdditionalMaterialsMark.showOptionMassage(block);
                    if (mode.equals(NNKMode.BLOCK) && optionControlAdditionalMaterialsMark.signal && optionControlAdditionalMaterialsMark.isBlockOption()) {
                        optionControlAdditionalMaterialsMark.showOptionMassage(block);
                    }
                    return optionControlAdditionalMaterialsMark.isBlockOption2() ? 1 : 0;
                } catch (Exception e) {
                }
                break;

            case 139577:
                optionControlVersion_139577(context, dataDB, option, null, NNKMode.CHECK_CLICK, unlockCodeResultListener);
                break;


            // Доп. Материалы
            case 138340:
                option138340(context, dataDB, option, type, mode, unlockCodeResultListener);
                break;


            //  Сравнение остатков и наличия
            case 141067:
                OptionControlStockBalanceTovar<?> optionControlStockBalanceTovar = new OptionControlStockBalanceTovar<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlStockBalanceTovar.isBlockOption()))
                    optionControlStockBalanceTovar.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlStockBalanceTovar.signal && optionControlStockBalanceTovar.isBlockOption()) {
                    optionControlStockBalanceTovar.showOptionMassage(block);
                }

                return optionControlStockBalanceTovar.isBlockOption2() ? 1 : 0;

            case 2243:
                OptionControlStockTovarLeft<?> optionControlStockTovarLeft = new OptionControlStockTovarLeft<>(context, dataDB, option, type, mode, unlockCodeResultListener);
                if (mode.equals(NNKMode.MAKE) || (mode.equals(NNKMode.CHECK) && optionControlStockTovarLeft.isBlockOption()))
                    optionControlStockTovarLeft.showOptionMassage(block);
                if (mode.equals(NNKMode.BLOCK) && optionControlStockTovarLeft.signal && optionControlStockTovarLeft.isBlockOption()) {
                    optionControlStockTovarLeft.showOptionMassage(block);
                }

                return optionControlStockTovarLeft.isBlockOption2() ? 1 : 0;


            default:
                unlockCodeResultListener.onUnlockCodeSuccess();

                switch (mode) {
                    case NULL:
                        return 0;

                    case CHECK:
                        return 0;

                    case MAKE:
                        Toast.makeText(context, "" + option.getNotes() + "\n\n" + option.getOptionControlTxt(), Toast.LENGTH_LONG).show();
                        return 0;
                }
        }

        return 0;
    }


    //#когда не знаешь что такое полиморфизм
    private <T> void option84003(Context context, T dataDB, OptionsDB option, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        if (dataDB instanceof TasksAndReclamationsSDB) {

            TasksAndReclamationsSDB tarDB = (TasksAndReclamationsSDB) dataDB;

            DialogData dialog = new DialogData(context);
            dialog.setTitle("Выбор мнения");
            dialog.setText("Выберите мнение о сотруднике кликнув по нему");
            dialog.setClose(dialog::dismiss);

            List<OpinionThemeSDB> opinionThemeSDB = SQL_DB.opinionThemeDao().getByTheme(tarDB.themeId);
            List<String> ids = new ArrayList<>();
            for (OpinionThemeSDB item : opinionThemeSDB) {
                ids.add(item.mnenieId);
            }

            TextAdapter adapter = new TextAdapter(SQL_DB.opinionDao().getOpinionByIds(ids), new Clicks.click() {
                @Override
                public <T> void click(T data) {
                    String information = (String) data;
                    OpinionSDB opinionSDB = SQL_DB.opinionDao().getOpinionByNm(information);

                    tarDB.sotrOpinionDt = System.currentTimeMillis() / 1000;
                    tarDB.sotrOpinionAuthorId = userId;
                    tarDB.sotrOpinionId = opinionSDB.id;

                    SQL_DB.tarDao().insertData(Collections.singletonList(tarDB));

                    Exchange.updateTAR(tarDB);

                    Toast.makeText(context, "Вы выбрали: " + data, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            dialog.setRecycler(adapter, new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

            dialog.show();
        } else if (dataDB instanceof WpDataDB) {
            Toast.makeText(context, "Данный функционал в разработке", Toast.LENGTH_SHORT).show();
        }
    }

    // Установка оценки для Задачи
    private <T> void option132621(Context context, T dataDB, OptionsDB option, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {

        float rating = 0;
        if (dataDB instanceof TasksAndReclamationsSDB) {
            rating = ((TasksAndReclamationsSDB) dataDB).voteScore;

            DialogARMark dialog = new DialogARMark(context);
            dialog.setTitle("Укажите оценку");
            dialog.setClose(dialog::dismiss);
            dialog.setRatingBarAR(rating, new Clicks.click() {
                @Override
                public <T> void click(T data) {
                    ((TasksAndReclamationsSDB) dataDB).voteScore = (Integer) data;

                    SQL_DB.tarDao().insertData(Collections.singletonList((TasksAndReclamationsSDB) dataDB))
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    Log.d("test", "test");
                                }

                                @Override
                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                    Log.d("test", "test");
                                }
                            });
                }
            });
            dialog.show();
        } else if (dataDB instanceof WpDataDB) {
            Toast.makeText(context, "Данный функционал в разработке", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    // Это вроде нигде не работает, некст раз прочитаешь - проверь. Работает с DetailedButtons
    private <T> void option138339(Context context, T dataDB, OptionsDB option, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        Intent intent = new Intent(context, FeaturesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("viewModel", AdditionalRequirementsDBViewModel.class.getCanonicalName());
        bundle.putString("contextUI", ContextUI.ADD_REQUIREMENTS_FROM_OPTIONS.toString());
        bundle.putString("dataJson", new Gson().toJson(dataDB));
        bundle.putString("title", "Доп. требования");
        bundle.putString("subTitle", "Список дополнительных требований, которые должен выполнить исполнитель во время посещения");
        intent.putExtras(bundle);
        context.startActivity(intent);

//        WpDataDB wpDataDB = (WpDataDB) dataDB;
//
//        Integer ttCategory = null;
//        AddressSDB addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
//        if (addressSDB != null){
//            ttCategory = addressSDB.ttId;
//        }
//
//        List<AdditionalRequirementsDB> data = AdditionalRequirementsRealm.getData3(wpDataDB, HIDE_FOR_USER, ttCategory, null,0);
//        Log.e("AdditionalRequirements", "data2.size(): " + data.size());
//
//
//        DialogAdditionalRequirements dialogAdditionalRequirements = new DialogAdditionalRequirements(context);
//
//        dialogAdditionalRequirements.setTitle("Доп. требования (" + data.size() + ")");
//        dialogAdditionalRequirements.setRecycler(wpDataDB, data);
//
//        dialogAdditionalRequirements.setClose(dialogAdditionalRequirements::dismiss);
//        dialogAdditionalRequirements.setLesson(context, true, 1232);
//        dialogAdditionalRequirements.setVideoLesson(context, true, 1233, () -> {
//        });
//        dialogAdditionalRequirements.show();
    }

    private <T> void option138340(Context context, T dataDB, OptionsDB option, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        String expire = Clock.getHumanTimeYYYYMMDD(System.currentTimeMillis() / 1000);
        List<AdditionalMaterialsSDB> data1 = SQL_DB.additionalMaterialsDao().getAllForOption(option.getClientId(), "1", "0", expire);
//        List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> data = SQL_DB.additionalMaterialsDao().getAllForOptionTEST(option.getClientId(), Integer.parseInt(option.getAddrId()), "1", "0");
//        List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> data = SQL_DB.additionalMaterialsDao().getAllForOptionTEST(option.getClientId(), Integer.parseInt(option.getAddrId()), "0");
        List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> data = SQL_DB.additionalMaterialsDao().getAllForOptionTEST2(option.getClientId(), "0");

        DialogAdditionalRequirements dialogAdditionalRequirements = new DialogAdditionalRequirements(context);

        if (data != null && data.size() > 0) {
            dialogAdditionalRequirements.setTitle("Доп. материалы (" + data.size() + ")");
            dialogAdditionalRequirements.setRecyclerAM((WpDataDB) dataDB, data);
        } else if (data1 != null && data1.size() > 0) {
            dialogAdditionalRequirements.setTitle("Доп. материалы (" + data1.size() + ")");
            dialogAdditionalRequirements.setRecyclerAM((WpDataDB) dataDB, amToAma(data1));
        }


        dialogAdditionalRequirements.setClose(dialogAdditionalRequirements::dismiss);
        dialogAdditionalRequirements.show();
    }

    private List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> amToAma(List<AdditionalMaterialsSDB> AM) {
        List<AdditionalMaterialsJOINAdditionalMaterialsAddressSDB> AMA = new ArrayList<>(); // создаем новый список
        for (AdditionalMaterialsSDB am : AM) {
            AdditionalMaterialsJOINAdditionalMaterialsAddressSDB ama = new AdditionalMaterialsJOINAdditionalMaterialsAddressSDB();

            // заполняем поля в новом экземпляре AdditionalMaterialsJOINAdditionalMaterialsAddressSDB из am
            ama.id = am.id;
            ama.client = am.client;
            ama.expire = am.expire;
            ama.state = am.state;
            ama.approve = am.approve;
            ama.user_id = am.user_id;
            ama.dt = am.dt;
            ama.fileArchive = am.fileArchive;
            ama.fileExt = am.fileExt;
            ama.fileSize = am.fileSize;
            ama.txt = am.txt;
            ama.score = am.score;
            ama.scoreCnt = am.scoreCnt;
            ama.scoreSum = am.scoreSum;

            // добавляем новый экземпляр в список
            AMA.add(ama);
        }

        return AMA;
    }

    private <T> void option135742(Context context, T dataDB, OptionsDB option, OptionMassageType type, NNKMode mode) {
        try {
            WpDataDB wp;
            if (dataDB instanceof WpDataDB) {
                wp = ((WpDataDB) dataDB);

                DialogData dialog = new DialogData(context);
                dialog.setTitle("Представленность");

                String msg = String.format("SKU (План): %s шт.\nSKU (Факт): %s шт.\nОтсутствует: %s шт.\nOOS (out of stock): %s %%\nПредставленность: %s %%\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\tОписание\n\nSKU (План) - количество товарных позиций которые должны быть в торговой точке по плану.\nSKU (Факт) - количество товарных позиций которые фактически стоят на витрине.\nOOS - процент товара, который отсутствует по сравнению с планом\nOOS = 100 - 100*(SKUФакт/SKUПлан) = %s %%\nПредставленность = 100 - OOS = %s %%", (int) SKUPlan, (int) SKUFact, (int) SKUPlan - (int) SKUFact, (int) DetailedReportActivity.OOS, (int) DetailedReportActivity.OFS, (int) DetailedReportActivity.OOS, (int) DetailedReportActivity.OFS);
                dialog.setText(msg);

                dialog.setClose(dialog::dismiss);
                dialog.show();
            } else if (dataDB instanceof TasksAndReclamationsSDB) {
                Long dad2Wp = ((TasksAndReclamationsSDB) dataDB).codeDad2SrcDoc;
                wp = WpDataRealm.getWpDataRowByDad2Id(dad2Wp);

                Intent intent = new Intent(context, DetailedReportActivity.class);
                intent.putExtra("WpDataDB_ID", wp.getId());

                type.msg = "Открыт проверяемый документ. \nНомер: " + wp.getDoc_num_otchet();

                context.startActivity(intent);
            } else {
                return;
            }


        } catch (Exception e) {
            type.msg = "Ошибка: " + e;
        }
    }

    //----------------------------------------------------------------------------------------------

    // Новый набор опций. Переписывание как в 1С (типо правильно)

    /**
     * Опция Контроля
     * Проверка местоположения ( 8299 )
     */
    private <T> boolean optionControlMP_8299(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        boolean res = false;


        int visitStartGeoDistance = 0;
        WpDataDB wpDataDB = null;
        if (dataDB instanceof WpDataDB) {
            wpDataDB = (WpDataDB) dataDB;
            visitStartGeoDistance = ((WpDataDB) dataDB).getVisit_start_geo_distance();
        }

        if (mode != NNKMode.NULL) {
            Globals.fixMP(wpDataDB, context);
        }

        try {
//            long visitStart = wpDataDB.getVisit_start_dt();
//            long visitEnd = wpDataDB.getVisit_end_dt() > 0 ? wpDataDB.getVisit_end_dt() : System.currentTimeMillis() / 1000;
//            List<LogMPDB> logs = LogMPRealm.getLogMPByDtAndDistance(visitStart*1000, visitEnd*1000, 500);
//            List<LogMPDB> logs = LogMPRealm.getLogMPByDad2Distance(wpDataDB.getCode_dad2(), 500);
//            List<LogMPDB> logs = LogMPRealm.getLogMPByDad2(wpDataDB.getCode_dad2());

            int minutes30 = 1800; // - обычные мерчики
            int minutes45 = 2700; // - СУППР Ласунка
            int minutes60 = 3600; // - Харьков
            int minutes180 = 10800; // - СУППР ПремьерСокс (13799)

            int time = 0;
            AddressSDB addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
            AppUsersDB appUsersDB = AppUserRealm.getAppUserById(wpDataDB.getUser_id());
            if (appUsersDB.user_work_plan_status.equals("our")) {
                time = 1800;
            } else if (appUsersDB.user_work_plan_status.equals("foreign")) {
                time = 2700;
            } else if (addressSDB.cityId == 41) {
                time = 3600;
            } else if (wpDataDB.getClient_id().equals("13799")) {
                time = 10800;
            }

            long startTime = (wpDataDB.getVisit_start_dt() > 0)
                    ? wpDataDB.getVisit_start_dt() - time
                    : (System.currentTimeMillis() / 1000) - time;

            long endTime = wpDataDB.getVisit_end_dt() > 0 ? wpDataDB.getVisit_end_dt() : System.currentTimeMillis() / 1000;

            List<LogMPDB> logs = LogMPRealm.getLogMPTime(startTime * 1000, endTime * 1000);

            Globals.writeToMLOG("INFO", "optionControlMP_8299", "onUnlockCodeSuccess: " + logs.size());

            List<LogMPDB> logsRes = new ArrayList<>();
            float coordAddrX = 0f, coordAddrY = 0f;
//            AddressSDB addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
            if (addressSDB != null) {
                coordAddrX = addressSDB.locationXd;
                coordAddrY = addressSDB.locationYd;
            } else {
                try {
                    if (wpDataDB != null) {
                        coordAddrX = Float.parseFloat(wpDataDB.getAddr_location_xd());
                        coordAddrY = Float.parseFloat(wpDataDB.getAddr_location_yd());
                    }
                } catch (Exception e) {
                }
            }

            for (LogMPDB log : logs) {
                if (log.distance == 0) {
                    double distance = coordinatesDistanse(coordAddrX, coordAddrY, log.CoordX, log.CoordY);
                    log.distance = (int) distance;
                }

                if (log.distance > 1 && log.distance < 1000) {
                    logsRes.add(log);
                }
            }

            LogMPRealm.setLogMP(logsRes);   // Сохраняю посчитанное расстояние в БД, если его не было. Скорее всего оно не посчитано для данных полученных с сайта

            Globals.writeToMLOG("INFO", "optionControlMP_8299", "onUnlockCodeSuccess: " + logsRes.size());

            if (logsRes != null && logsRes.size() > 0) {
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionsDB != null) {
                        optionsDB.setIsSignal("2");
                        realm.insertOrUpdate(optionsDB);
                    }
                });
                Globals.writeToMLOG("INFO", "optionControlMP_8299", "onUnlockCodeSuccess: " + logs.size());
                unlockCodeResultListener.onUnlockCodeSuccess();
            } else {
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionsDB != null) {
                        optionsDB.setIsSignal("1");
                        realm.insertOrUpdate(optionsDB);
                    }
                });
                Globals.writeToMLOG("INFO", "optionControlMP_8299", "onUnlockCodeFailure");
                Toast.makeText(context, "У Вас відсутня історія місцеположень", Toast.LENGTH_LONG).show();
                unlockCodeResultListener.onUnlockCodeFailure();
            }
            return true;
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "optionControlMP_8299", "Exception e: " + e);
            Globals.writeToMLOG("ERROR", "optionControlMP_8299", "onUnlockCodeFailure");
            Toast.makeText(context, "Виникла помилка. Зверніться до керівника!", Toast.LENGTH_LONG).show();
            unlockCodeResultListener.onUnlockCodeFailure();
            return true;
        }


        // Проверка Опции и запись в БД результата
//        if (visitStartGeoDistance < 500 && visitStartGeoDistance > 0) {
//            RealmManager.INSTANCE.executeTransaction(realm -> {
//                if (optionsDB != null) {
//                    optionsDB.setIsSignal("2");
//                    realm.insertOrUpdate(optionsDB);
//                }
//            });
//            unlockCodeResultListener.onUnlockCodeSuccess();
//            res = true;
//        } else {
//            RealmManager.INSTANCE.executeTransaction(realm -> {
//                if (optionsDB != null) {
//                    optionsDB.setIsSignal("1");
//                    realm.insertOrUpdate(optionsDB);
//                }
//            });
//            unlockCodeResultListener.onUnlockCodeFailure();
//            res = false;
//        }
//
//        // Обработка режима который вернулся
//        switch (mode) {
//            case CHECK:
//                if (!res && optionsDB.getBlockPns().equals("1")) {
////                    optionNotConduct.add(optionsDB);
//                }
//                break;
//
//            case NULL:
//                // Ничего делать не буду
//                break;
//        }


//        return res;
    }


    /**
     * Опция
     * Нажатие на кнопку моего местоположения ( 138773 )
     */
    private <T> void optionMP_138773(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {

        WpDataDB wpDataDB;
        if (dataDB instanceof WpDataDB) {
            wpDataDB = ((WpDataDB) dataDB);
            globals.fixMP(wpDataDB, null);
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            globals.fixMP(null, null);
//            Toast.makeText(context, "Местоположение зафиксированно", Toast.LENGTH_SHORT).show();
            return;
        } else {
            return;
        }

        // Вывод диалога с МП
        if (context instanceof toolbar_menus) {
            ((toolbar_menus) context).dialogMap();
        }

//        // Запись в таблицу Местоположений
//        LogMPDB log = new LogMPDB(RealmManager.logMPGetLastId() + 1, globals.POST_10());
//        RealmManager.setLogMpRow(log);


        DialogData dialog = new DialogData(context);
        StringBuilder title = new StringBuilder();
        StringBuilder text = new StringBuilder();

        // 24.08.23. "Обновляем данные ГПС"
        Coordinates(wpDataDB);
        AddressSDB addressSDB = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());

        // 24.08.23. Проверяем включён ли ГПС.
        title.append("Місцеположення");
        boolean okMP = false;
        if (enabledGPS) {
            if (addressSDB != null && addressSDB.locationXd > 0 && addressSDB.locationYd > 0) {
                if (Globals.CoordX != 0 && Globals.CoordY != 0) {
                    double distance = coordinatesDistanse(addressSDB.locationXd, addressSDB.locationYd, Globals.CoordX, Globals.CoordY);
                    if (distance < distanceMin) {
                        text.append("Проблем із місцеположенню немає!");
                        okMP = true;
                    } else {
                        text.append("Ви знаходитесь задалеко від торгівельної точки!").append("\n")
                                .append("За даними системи ви знаходитесь на відстані ").append(distance).append(" метрів від ТТ ").append(addressSDB.nm)
                                .append(", що більше допустимих ").append(distanceMin).append("\n\n")
                                .append("Якщо ви в дійсності знаходитесь на ТТ - зверніться за допомогою до свого керівника або в службу підтримки merchik.");
                    }
                } else {
                    text.append("Не можу визначити ваше місцезнаходження!").append("\n").append("Спробуйте підійти до вікна або вийти на вулицю. Через хвилину місцеположення буде визначене і можна продовжувати виконувати роботу.");
                }
            } else {
                text.append("У магазині в якому Ви працюєте не встановлені координати!").append("\n").append("Зверніться до Вашого керівника для виправлення цієї проблеми.");
            }
        } else {
            text.append("У вас ввимкнено GPS!").append("\n").append("Будь-ласка увімкніть GPS, почекайте поки з`явиться Ваше місцеположення та продовжуйте роботу.");
        }

        if (!okMP) {
            dialog.setTitle(title);
            dialog.setText(text);
            dialog.setDialogIco();
            dialog.setClose(dialog::dismiss);
            dialog.show();
        }


        // Запись в План работ
        int distance;
        if (Globals.measure.equals("м")) {
            distance = (int) Globals.distanceAB;
        } else if (Globals.measure.equals("км")) {
            distance = (int) Globals.distanceAB * 1000;
        } else {
            distance = 0;
        }

        RealmManager.INSTANCE.executeTransaction(realm -> {
            if (wpDataDB != null) {
                wpDataDB.setVisit_start_geo_distance(distance);
                realm.insertOrUpdate(wpDataDB);
//                Toast.makeText(context, "Данные о местоположении внесены.", Toast.LENGTH_LONG).show();
            }
        });
    }

    //--------------------- НАЧАЛО РАБОТЫ -----------------------


    /*
            long dad2;
        if (dataDB instanceof WpDataDB){
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
        }else if (dataDB instanceof TasksAndReclamationsSDB){
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
        }else {
            return;
        }

        */

    /**
     * Опция контроля
     * Проверка на Начало работы ( 138519 )
     */
    private <T> boolean optionControlStartWork_138519(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        Globals.writeToMLOG("ERROR", "Option.optionControlStartWork_138519", "++");
        boolean res;
        long dad2, startWork;
        if (dataDB instanceof WpDataDB) {
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
            startWork = ((WpDataDB) dataDB).getVisit_start_dt();
            globals.fixMP((WpDataDB) dataDB, null);
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
            startWork = ((TasksAndReclamationsSDB) dataDB).dt_start_fact;
        } else {
            unlockCodeResultListener.onUnlockCodeFailure();
            return res = false;
        }

        if (startWork == 0 && dad2 != 0) {
            Globals.writeToMLOG("INFO", "optionControlEndWork_138521", "endWork == 0 and RealmManager.getWorkPlanRowByCodeDad2 by dad2: " + dad2);
            WpDataDB dataBaseWP = RealmManager.getWorkPlanRowByCodeDad2(dad2);
            if (dataBaseWP != null)
                startWork = dataBaseWP.getVisit_start_dt();
        }


        Globals.writeToMLOG("ERROR", "Option.optionControlStartWork_138519", "startWork: " + startWork);
        if (startWork > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            unlockCodeResultListener.onUnlockCodeSuccess();
            res = true;
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            unlockCodeResultListener.onUnlockCodeFailure();
            res = false;
        }

        Globals.writeToMLOG("ERROR", "Option.optionControlStartWork_138519", "mode: " + mode.name());

        // Обработка режима который вернулся
        switch (mode) {
            case BLOCK:
            case MAKE:
                if (res) {
                    // Всё хорошо с опцией контроля
                } else {
                    // Нужно отобразить сообщение что всё плохо
                    DialogData dialog = new DialogData(context);
                    dialog.setTitle("Ошибка");
                    dialog.setDialogIco();
                    dialog.setText("Прежде чем выполнять данную опцию (действие) вы должны выполнить опцию: " + "Контроль наличия времени начала работ (ВРН)");
                    dialog.setClose(dialog::dismiss);
                    dialog.show();
                }
                break;

            case CHECK:

                if (res) {
                    // Всё хорошо с опцией контроля
                } else {
                    // Нужно отобразить сообщение что всё плохо
                    DialogData dialog = new DialogData(context);
                    dialog.setTitle("Ошибка. Проверка.");
                    dialog.setDialogIco();
                    dialog.setText("Прежде чем выполнять данную опцию (действие) вы должны выполнить опцию: " + "Контроль наличия времени начала работ (ВРН)");
                    dialog.setClose(dialog::dismiss);
                    dialog.show();
                }
                break;

            case NULL:
                // Ничего делать не буду
                break;
        }

        return res;
    }


    /**
     * Опция
     * Нажатие на кнопку Для установки начала рабочего дня ( 138518 )
     */
    private boolean optionStartWork_138518(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        boolean result;
        globals.fixMP(wpDataDB, null);
        Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork", "ENTER. wpDataDB.codeDAD2: " + wpDataDB.getCode_dad2());
        if (wpDataDB.getVisit_start_dt() > 0) {
            Toast.makeText(context, "Работа уже начата!", Toast.LENGTH_SHORT).show();
            globals.writeToMLOG("_INFO.DetailedReportButtons.class.pressStartWork: " + "Работа уже начата!" + "\n");
            unlockCodeResultListener.onUnlockCodeSuccess();
            result = true;
        } else {
            try {
                long startTime = System.currentTimeMillis() / 1000;
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    wpDataDB.setDt_update(System.currentTimeMillis() / 1000);
                    wpDataDB.setVisit_start_dt(startTime);
                    wpDataDB.setClient_start_dt(startTime);
                    wpDataDB.startUpdate = true;
                    realm.insertOrUpdate(wpDataDB);
                });

                // Это жосткие костыли
                Exchange exchange = new Exchange();
                exchange.sendWpDataToServer(new Click() {
                    @Override
                    public <T> void onSuccess(T data) {
                        String msg = (String) data;
                        Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork.onSuccess", "msg: " + msg);
                    }

                    @Override
                    public void onFailure(String error) {
                        Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork.onFailure", "error: " + error);
                    }
                });

                Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork", "Вы начали работу в: " + startTime + " / отчёт: " + wpDataDB.getDoc_num_otchet() + " wpDataDB.getCode_dad2(): " + wpDataDB.getCode_dad2());

                Toast.makeText(context, "Вы начали работу в: " + Clock.getHumanTimeOpt(startTime * 1000), Toast.LENGTH_SHORT).show();
                unlockCodeResultListener.onUnlockCodeSuccess();
                result = true;
            } catch (Exception e) {
                // Set to log error
                Toast.makeText(context, "Возникла ошибка: " + e, Toast.LENGTH_SHORT).show();
                Globals.writeToMLOG("ERROR", "DetailedReportButtons.class.pressStartWork", "wpDataDB.getCode_dad2(): " + wpDataDB.getCode_dad2() + "Exception e: " + e);
                unlockCodeResultListener.onUnlockCodeFailure();
                result = false;
            }
        }


        Exchange exchange = new Exchange();
        exchange.sendWpDataToServer(new Click() {
            @Override
            public <T> void onSuccess(T data) {
                String msg = (String) data;
                Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork.onSuccess", "msg: " + msg);
            }

            @Override
            public void onFailure(String error) {
                Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork.onFailure", "error: " + error);
            }
        });

//        conductOptCheck(mode, result, optionsDB);
        return result;
    }

    private void optionStartWork_138518(Context context, TasksAndReclamationsSDB dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        globals.writeToMLOG("_INFO.DetailedReportButtons.class.pressStartWork: " + "ENTER" + "\n");
        if (dataDB.dt_start_fact > 0) {
            Toast.makeText(context, "Работа уже начата!", Toast.LENGTH_SHORT).show();
        } else {
            try {
                long startTime = System.currentTimeMillis() / 1000;
                dataDB.dt_start_fact = startTime;
                dataDB.uploadStatus = 1;
                SQL_DB.tarDao().insertData(Collections.singletonList(dataDB))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                Log.d("test", "test");
                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                Log.d("test", "test");
                            }
                        });

                Toast.makeText(context, "Вы начали работу в: " + Clock.getHumanTimeOpt(startTime * 1000), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // Set to log error
                Toast.makeText(context, "Возникла ошибка: " + e, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //------------------- ОКОНЧАНИЕ РАБОТЫ-------------------------

    /**
     * Опция контроля
     * Проверка на Окончание работы( 138521 )
     */
    private <T> boolean optionControlEndWork_138521(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {

        boolean res;

        if (dataDB != null)
            Globals.writeToMLOG("INFO", "optionControlEndWork_138521",
                    "dataDB: " + new Gson().fromJson(new Gson().toJson(dataDB), JsonObject.class));
        else
            Globals.writeToMLOG("INFO", "optionControlEndWork_138521",
                    "dataDB: is NULL");


        long dad2, startWork, endWork = 0L;
        if (dataDB instanceof WpDataDB) {
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
            startWork = ((WpDataDB) dataDB).getVisit_start_dt();
            endWork = ((WpDataDB) dataDB).getVisit_end_dt();
            Globals.writeToMLOG("INFO", "optionControlEndWork_138521", "dataDB instanceof WpDataDB, endWork: " + endWork);

        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
            startWork = ((TasksAndReclamationsSDB) dataDB).dt_start_fact;
            endWork = ((TasksAndReclamationsSDB) dataDB).dt_end_fact;
            Globals.writeToMLOG("INFO", "optionControlEndWork_138521", "dataDB instanceof TasksAndReclamationsSDB, endWork: " + endWork);
        } else {
            Globals.writeToMLOG("INFO", "optionControlEndWork_138521", "unlockCodeResultListener.onUnlockCodeFailure(), endWork: " + endWork);
            unlockCodeResultListener.onUnlockCodeFailure();
            return res = false;
        }
        if (endWork == 0 && dad2 != 0) {
            Globals.writeToMLOG("INFO", "optionControlEndWork_138521", "endWork == 0 and RealmManager.getWorkPlanRowByCodeDad2 by dad2: " + dad2);
            WpDataDB dataBaseWP = RealmManager.getWorkPlanRowByCodeDad2(dad2);
            if (dataBaseWP != null)
                endWork = dataBaseWP.getVisit_end_dt();
        }

        if (endWork > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            Globals.writeToMLOG("INFO", "optionControlEndWork_138521", "unlockCodeResultListener.onUnlockCodeSuccess(), endWork: " + endWork);
            unlockCodeResultListener.onUnlockCodeSuccess();
            res = true;
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            Globals.writeToMLOG("INFO", "optionControlEndWork_138521", "unlockCodeResultListener.onUnlockCodeFailure(), endWork: " + endWork);
            unlockCodeResultListener.onUnlockCodeFailure();
            res = false;
        }

        // Обработка режима который вернулся
//        switch (mode) {
//            case CHECK:
//                if (!res && optionsDB.getBlockPns().equals("1")) {
////                if (!res) {
////                    optionNotConduct.add(optionsDB);
//                }
//                break;
//
//            case NULL:
//                // Ничего делать не буду
//                break;
//        }

        return res;
    }


    /**
     * Опция
     * Нажатие на кнопку Для установки окончания рабочего дня ( 138520 )
     */
    private boolean optionEndWork_138520(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        boolean result = false;
        try {
            Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressEndWork", "ENTER. wpDataDB.codeDAD2: " + wpDataDB.getCode_dad2());
            if (wpDataDB.getVisit_end_dt() > 0) {
                Toast.makeText(context, "Работа уже окончена!", Toast.LENGTH_SHORT).show();
                Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressEndWork", "Работа уже окончена!. wpDataDB.codeDAD2: " + wpDataDB.getCode_dad2());
                unlockCodeResultListener.onUnlockCodeSuccess();
                result = true;
            } else {
                if (wpDataDB.getVisit_start_dt() > 0) {
                    try {
                        // Сохраняю время
                        long endTime = System.currentTimeMillis() / 1000;

                        wpDataDB.setDt_update(System.currentTimeMillis() / 1000);
                        wpDataDB.setVisit_end_dt(endTime);
                        wpDataDB.setClient_end_dt(endTime);
                        wpDataDB.startUpdate = true;
                        wpDataDB.client_work_duration = (endTime - wpDataDB.getClient_start_dt());

                        RealmManager.INSTANCE.executeTransaction(realm -> {
                            realm.insertOrUpdate(wpDataDB);
                        });
                        // Это жосткие костыли
                        // При нажатии на "Конец работы" - начинаю выгружать данные палана работ,
                        // что б мерчик не ждал следующего автоматического обмена
                        Exchange exchange = new Exchange();
                        exchange.sendWpDataToServer(new Click() {
                            @Override
                            public <T> void onSuccess(T data) {
                                String msg = (String) data;
                                Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork.onSuccess", "msg: " + msg);
                                WorkPlan workPlan = new WorkPlan();
                                List<OptionsDB> opt = workPlan.getOptionButtons2(workPlan.getWpOpchetId(wpDataDB), wpDataDB.getId());
                                new Options().conduct(context, wpDataDB, opt, DEFAULT_CONDUCT, new Clicks.click() {
                                    @Override
                                    public <T> void click(T data) {
                                        OptionsDB optionsDB = (OptionsDB) data;
                                        OptionMassageType msgType = new OptionMassageType();
                                        msgType.type = OptionMassageType.Type.DIALOG;
                                        new Options().optControl(context, wpDataDB, optionsDB, Integer.parseInt(optionsDB.getOptionControlId()), null, msgType, Options.NNKMode.CHECK, new OptionControl.UnlockCodeResultListener() {
                                            @Override
                                            public void onUnlockCodeSuccess() {

                                            }

                                            @Override
                                            public void onUnlockCodeFailure() {

                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onFailure(String error) {
                                Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressStartWork.onFailure", "error: " + error);
                                WorkPlan workPlan = new WorkPlan();
                                List<OptionsDB> opt = workPlan.getOptionButtons2(workPlan.getWpOpchetId(wpDataDB), wpDataDB.getId());
                                new Options().conduct(context, wpDataDB, opt, DEFAULT_CONDUCT, new Clicks.click() {
                                    @Override
                                    public <T> void click(T data) {
                                        OptionsDB optionsDB = (OptionsDB) data;
                                        OptionMassageType msgType = new OptionMassageType();
                                        msgType.type = OptionMassageType.Type.DIALOG;
                                        new Options().optControl(context, wpDataDB, optionsDB, Integer.parseInt(optionsDB.getOptionControlId()), null, msgType, Options.NNKMode.CHECK, new OptionControl.UnlockCodeResultListener() {
                                            @Override
                                            public void onUnlockCodeSuccess() {

                                            }

                                            @Override
                                            public void onUnlockCodeFailure() {

                                            }
                                        });
                                    }
                                });
                            }
                        });

                        Globals.writeToMLOG("INFO", "_INFO.DetailedReportButtons.class.pressEndWork", "Вы закончили работу в: " + endTime + " / отчёт: " + wpDataDB.getDoc_num_otchet());
                        Toast.makeText(context, "Вы окончили работу в: " + Clock.getHumanTimeOpt(endTime * 1000) + "\n\nНе забудьте нажать 'Провести', что б система проверила текущий документ и начислила Вам премиальные", Toast.LENGTH_SHORT).show();
                        unlockCodeResultListener.onUnlockCodeSuccess();
                        result = true;
                    } catch (Exception e) {
                        // Set to log error
                        Toast.makeText(context, "Возникла ошибка: " + e, Toast.LENGTH_SHORT).show();
                        Globals.writeToMLOG("ERROR", "DetailedReportButtons.class.pressEndWork", "wpDataDB.codeDAD2: " + wpDataDB.getCode_dad2() + "Exception e: " + e);
                        unlockCodeResultListener.onUnlockCodeFailure();
                        result = false;
                    }
                } else {
                    Toast.makeText(context, "Вы не можете закончить работу не начав её", Toast.LENGTH_SHORT).show();
                    Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressEndWork", "Вы не можете закончить работу не начав её. wpDataDB.codeDAD2: " + wpDataDB.getCode_dad2());
                    unlockCodeResultListener.onUnlockCodeFailure();
                    result = false;
                }
            }
            Globals.writeToMLOG("INFO", "DetailedReportButtons.class.pressEndWork", "OUT. wpDataDB.codeDAD2: " + wpDataDB.getCode_dad2());

        } catch (Exception e) {
            Log.e("optionEndWork_138520", "Exception e: " + e);
            Globals.writeToMLOG("ERROR", "DetailedReportButtons.class.pressEndWork", "Exception: " + e.getMessage());

        }
        return result;
    }

    private void optionEndWork_138520(Context context, TasksAndReclamationsSDB dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        globals.writeToMLOG("_INFO.DetailedReportButtons.class.pressEndWork: " + "ENTER" + "\n");
        if (dataDB.dt_end_fact > 0) {
            Toast.makeText(context, "Работа уже окончена!", Toast.LENGTH_SHORT).show();
        } else {
            if (dataDB.dt_start_fact > 0) {
                try {
                    long endTime = System.currentTimeMillis() / 1000;
                    dataDB.dt_end_fact = endTime;
                    dataDB.uploadStatus = 1;
                    SQL_DB.tarDao().insertData(Collections.singletonList(dataDB))
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    Log.d("test", "test");
                                }

                                @Override
                                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                    Log.d("test", "test");
                                }
                            });
                    Toast.makeText(context, "Вы окончили работу в: " + endTime, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    // Set to log error
                    Toast.makeText(context, "Возникла ошибка: " + e, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Вы не можете закончить работу не начав её", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //------------------- ФОТООТЧЁТЫ -------------------------

    /**
     * Опция
     * Выполнение фотоотчёта
     */
    private <T> void optionMakePhoto0_132968(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        if (dataDB instanceof TasksAndReclamationsSDB) {
            TARSecondFrag.TaRID = ((TasksAndReclamationsSDB) dataDB).id;
        }

        MakePhoto makePhoto = new MakePhoto();
        makePhoto.pressedMakePhoto((DetailedReportActivity) context, dataDB, optionsDB, "0");    // Фото Витрины
    }


    private void optionMakePhoto0_132968(Context context, WpDataDB dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        MakePhoto makePhoto = new MakePhoto();
        makePhoto.pressedMakePhoto((DetailedReportActivity) context, dataDB, optionsDB, "0");
    }


    /**
     * Опция контроля (587)
     */
    private <T> boolean optionControlReceivingAnOrder_587(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        boolean res;

        long dad2, startWork, endWork;
        if (dataDB instanceof WpDataDB) {
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
        } else {
            unlockCodeResultListener.onUnlockCodeFailure();
            return res = false;
        }

        List<ReportPrepareDB> rp = ReportPrepareRealm.getReportPrepareByDad2(dad2);

        int SKU = 0;
        int sum = 0;
        int codes = 0;
        for (ReportPrepareDB item : rp) {

            if (item.getAmount() > 0) {
                sum += item.getAmount();
                SKU++;
            }

            if (item.buyerOrderId > 0) {
                codes = item.buyerOrderId;
            }
        }

        if (sum > 0 && codes > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            unlockCodeResultListener.onUnlockCodeSuccess();
            res = true;
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            unlockCodeResultListener.onUnlockCodeFailure();
            res = false;
        }

        // Обработка режима который вернулся
        switch (mode) {
            case CHECK:
//                if (!res && optionsDB.getBlockPns().equals("1")) {
//                    optionNotConduct.add(optionsDB);
//                }

                if (rp.size() == 0) {
                    Toast.makeText(context, "Товаров, по которым надо проверять заказ, не обнаружено.", Toast.LENGTH_LONG).show();
                }

                if (sum == 0) {
                    Toast.makeText(context, "Товар в ТТ НЕ заказан! Если это не так, то вы должны указать Количество закаанного товара, отдельно по каждой позиции.", Toast.LENGTH_LONG).show();
                }

                if (codes == 0) {
                    Toast.makeText(context, "Вы не указали номер заказа. Его надо получить у Баера у которого делали этот заказ.", Toast.LENGTH_LONG).show();
                }

                if (sum > 0 && codes > 0) {
                    Toast.makeText(context, "Заказано: " + SKU + " товаров (кол " + sum + " шт.) согласно заказа № " + codes, Toast.LENGTH_LONG).show();
                }
                break;

            case NULL:
                // Ничего делать не буду
                break;
        }

        return res;
    }


    /**
     * Опция контроля 138341
     */
    private <T> boolean optionControlAdditionalRequirements_138341(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        boolean res = false;
        try {
            double averageRating = 0;  // Средняя Оценка
            double deviationFromTheMeanSize = 0;    // Отклонение от среднего
            int markSum = 0;
            int nedotochSize = 0;

            StringBuilder msg = new StringBuilder();

            int userId;
            long dad2, startWork, endWork;
            long date;
            String clientId, userTxt;
            if (dataDB instanceof WpDataDB) {
                WpDataDB wp = (WpDataDB) dataDB;

                dad2 = ((WpDataDB) dataDB).getCode_dad2();
                startWork = ((WpDataDB) dataDB).getVisit_start_dt();
                date = Clock.dateConvertToLong(Clock.getHumanTimeYYYYMMDD(wp.getDt().getTime() / 1000));
                userId = ((WpDataDB) dataDB).getUser_id();
                clientId = ((WpDataDB) dataDB).getClient_id();
                userTxt = ((WpDataDB) dataDB).getUser_txt();
            } else if (dataDB instanceof TasksAndReclamationsSDB) {
                TasksAndReclamationsSDB tar = (TasksAndReclamationsSDB) dataDB;

                dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
                startWork = ((TasksAndReclamationsSDB) dataDB).dt_start_fact;
                date = tar.dt;
                userId = ((TasksAndReclamationsSDB) dataDB).vinovnikScoreUserId;
                clientId = ((TasksAndReclamationsSDB) dataDB).client;
                userTxt = ((TasksAndReclamationsSDB) dataDB).sortNm;
            } else {
                return res = false;
            }

            long dt = date;       // Дата документа в Unix
            long dateFrom = Clock.getDatePeriodLong(date, -30) / 1000; // Дата документа -30 дней
            long dateTo = Clock.getDatePeriodLong(date, +3) / 1000;     // Дата документа +3 дня

            // Получаем Доп.Требования.
//            RealmResults<AdditionalRequirementsDB> realmResults = AdditionalRequirementsRealm.getData3(dataDB, HIDE_FOR_USER, null, 0);
//            List<AdditionalRequirementsDB> data = RealmManager.INSTANCE.copyFromRealm(realmResults);

            List<AdditionalRequirementsDB> data = AdditionalRequirementsRealm.getData3(dataDB, HIDE_FOR_USER, null, null, 0);

            // Получаем Оценки этих Доп. требований.
            List<AdditionalRequirementsMarkDB> marks = AdditionalRequirementsMarkRealm.getAdditionalRequirementsMarks(dateFrom, dateTo, userId, "1", data);

            Gson gson = new Gson();

            String json = gson.toJson(data);

            Type listType = new TypeToken<ArrayList<VirtualAdditionalRequirementsDB>>() {
            }.getType();
            List<T> test = new Gson().fromJson(json, listType);
            List<VirtualAdditionalRequirementsDB> virtualTable = (List<VirtualAdditionalRequirementsDB>) test;

            Log.e("testprint", "data: " + data);
            Log.e("testprint", "marks: " + marks);
            Log.e("testprint", "test: " + test);
            Log.e("testprint", "virtualTable: " + virtualTable);

            for (VirtualAdditionalRequirementsDB item : virtualTable) {
                if (marks.get(0).getScore() != null && !marks.get(0).getScore().equals("") && !marks.get(0).getScore().equals("0")) {
                    item.mark = Integer.valueOf(marks.get(0).getScore());
                    item.dtChange = String.valueOf(marks.get(0).getDt());
                }

                if (Long.parseLong(item.dtChange) >= dt) {
                    item.nedotoch = 0;
                    item.notes = "ДТ измененно ПОСЛЕ проведения работ и проверке не подлежит";
                } else if (item.dtEnd != null && item.dtEnd.getTime() == dt) {
                    item.nedotoch = 0;
                    item.notes = "у ДТ заканчивается срок действия и голосование по нему проверке не подлежит";
                } else if (item.mark == 0) {
                    item.nedotoch = 1;
                    item.notes = "";
                } else {
                    item.nedotoch = 0;
                    item.notes = "";
                }


                nedotochSize = nedotochSize + item.nedotoch;
                markSum = markSum + item.mark;
            }


            try {
                averageRating = markSum / (virtualTable.size() - nedotochSize);
            } catch (Exception e) {
                averageRating = 0;
            }

            for (VirtualAdditionalRequirementsDB item : virtualTable) {
                item.deviationFromTheMean = Math.abs(averageRating - item.mark);
                deviationFromTheMeanSize = deviationFromTheMeanSize + item.deviationFromTheMean;
            }
            // Математика закончена


            // Установка сигналов.
            // У меня 2 это 0 в 1С, а 1 это 1 в 1С
            if (virtualTable.size() == 0) {

                msg.append("У клиента ")
                        .append(CustomerRealm.getCustomerById(clientId))
                        .append(" нет доп. требований по этому адресу");

                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionsDB != null) {
                        optionsDB.setIsSignal("2");
                        realm.insertOrUpdate(optionsDB);
                    }
                });
                res = true;
            } else if (nedotochSize > 0) {

                msg.append("За период с ")
                        .append(Clock.getHumanTime3(dateFrom))
                        .append(" по ")
                        .append(Clock.getHumanTime3(dateTo))
                        .append(" ")
                        .append(userTxt)
                        .append(" НЕ поставил оценку(и) по ")
                        .append(nedotochSize)
                        .append(" Доп.требованиям. ")
                ;

                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionsDB != null) {
                        optionsDB.setIsSignal("1");
                        realm.insertOrUpdate(optionsDB);
                    }
                });
                res = false;
            } else if (virtualTable.size() > 1 && deviationFromTheMeanSize < 0.5) {

                msg.append("Вы оценили Все (")
                        .append(virtualTable.size())
                        .append(") Доп.требований ОДНОЙ и той-же оценкой (")
                        .append(averageRating)
                        .append(")! Это НЕ даёт возможность улучшить их качество! " +
                                "Оценивайте эти требования ОБЬЕКТИВНО!");

                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionsDB != null) {
                        optionsDB.setIsSignal("1");
                        realm.insertOrUpdate(optionsDB);
                    }
                });
                res = false;
            } else {
                msg.append("За период с ")
                        .append(Clock.getHumanTime3(dateFrom))
                        .append(" по ")
                        .append(Clock.getHumanTime3(dateTo))
                        .append(" ")
                        .append(userTxt)
                        .append(" поставил оценку(и) по ")
                        .append(virtualTable.size())
                        .append(" Доп.требованиям. Замечаний по выполнению опции нет.")
                ;

                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionsDB != null) {
                        optionsDB.setIsSignal("2");
                        realm.insertOrUpdate(optionsDB);
                    }
                });
                res = true;
            }

            DialogData dialogData = new DialogData(context);
            dialogData.setTitle("");
            dialogData.setText(msg);
            dialogData.setClose(dialogData::dismiss);


            // Начат вывод сообщений
            switch (mode) {
                case MAKE:

                    break;

                case CHECK:
                    dialogData.show();
                    break;

                case CHECK_CLICK:
                    dialogData.show();
//                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    break;
            }


        } catch (Exception e) {
            Log.e("testprint", "Exception e: " + e);
        }


        return res;
    }


    /**
     * Опция нажатие на номер версии приложения 139576
     */
    public void optionVersion_139576(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        Long currentVer = Long.valueOf(BuildConfig.VERSION_NAME);
        Long minimalVer = VersionApp.VERSION_APP;

        StringBuilder msg = new StringBuilder();
        msg.append("Текущая версия приложения: ")
                .append(currentVer)
                .append(" \nПоследняя актуальная версия: ")
                .append(minimalVer);

        DialogData dialog = new DialogData(context);
        dialog.setTitle("Версия ПО");
        dialog.setText(msg);
        dialog.setClose(dialog::dismiss);
        dialog.show();
    }


    /**
     * Опция нажатие на номер версии приложения 139577
     */
    public <T> void optionControlVersion_139577(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode, OptionControl.UnlockCodeResultListener unlockCodeResultListener) {
        long currentVer = Long.parseLong(BuildConfig.VERSION_NAME);
        Long minimalVer = VersionApp.VERSION_APP;

        if (optionsDB != null && optionsDB.getAmountMin() != null && !optionsDB.getAmountMin().isEmpty()) {
            try {
                // Пробуем преобразовать строку в число
                long amountMinValue = Long.parseLong(optionsDB.getAmountMin());

                // Если число положительное, формируем minimalVer
                if (amountMinValue > 0) {
                    minimalVer = Long.parseLong("202" + amountMinValue + "00");
                    Globals.writeToMLOG("INFO", "optionControlVersion_139577.checkMinimalVersion",
                            "minimalVer: " + minimalVer);
                }
            } catch (NumberFormatException e) {
                // Логируем ошибку, если строка не является числом
                Globals.writeToMLOG("ERROR", "optionControlVersion_139577.checkMinimalVersion",
                        "Invalid amountMin format: " + optionsDB.getAmountMin());
            }
        }

        try {
            if (currentVer >= minimalVer) {
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionsDB != null) {
                        optionsDB.setIsSignal("2");
                        realm.insertOrUpdate(optionsDB);
                    }
                });
                unlockCodeResultListener.onUnlockCodeSuccess();
            } else {
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionsDB != null) {
                        optionsDB.setIsSignal("1");
                        realm.insertOrUpdate(optionsDB);
                    }
                });

                new MessageDialogBuilder(unwrap(context))
                        .setTitle("Версія додатку")
                        .setStatus(DialogStatus.ERROR)
                        .setMessage("У вас СТАРА версія додатку(" + currentVer + "), Вам потрібно оновитися до: " + minimalVer + "!")
                        .setOnConfirmAction("Оновити", () -> {
                            try {
                                // Пытаемся открыть через Google Play App
                                unwrap(context).startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=" + context.getPackageName())));
                            } catch (android.content.ActivityNotFoundException e) {
                                // Если приложения Google Play нет, открываем в браузере
                                unwrap(context).startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
                            }
                            unwrap(context).finishAffinity();
                            return Unit.INSTANCE;
                        })
                        .show();

                unlockCodeResultListener.onUnlockCodeFailure();
            }

        } catch (Exception e) {
//            DialogData dialog = new DialogData(context);
//            dialog.setTitle("Версія додатку");
//            dialog.setText("При обрахунку опції виникла помилка, зверніться до Вашого керівника\n\nПомилка: " + e);
//            dialog.setClose(dialog::dismiss);
//            dialog.show();
            Toast.makeText(context, "Версія додатку не визначена!", Toast.LENGTH_LONG).show();
            unlockCodeResultListener.onUnlockCodeFailure();
            Globals.writeToMLOG("ERROR", "optionControlVersion_139577", "Проблема с версией приложения в опции контроля. : " + e);
        }

    }
    //==============================================================================================

    /**
     * 06.04.2021
     * Опция контроля. Проверка представленности
     */
    private <T> void check76815(T dataDB, OptionsDB optionsDB) {
        long dad2;
        if (dataDB instanceof WpDataDB) {
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
        } else {
            return;
        }


        List<TovarDB> dataTovar = RealmManager.INSTANCE.copyFromRealm(RealmManager.getTovarListFromReportPrepareByDad2(dad2));    // Это типа моего СКЮ План
        SKUPlan = dataTovar.size();

        // Перебираем товары по плану
        for (TovarDB item : dataTovar) {
            ReportPrepareDB reportPrepareTovar = RealmManager.getTovarReportPrepare(String.valueOf(dad2), item.getiD()); // Есть ли в РП такой товар?
            if (reportPrepareTovar != null) {
                if (reportPrepareTovar.getFace() != null && !reportPrepareTovar.getFace().equals("") && !reportPrepareTovar.getFace().equals("0")) {     // Если у этого товара Фейсы не Null и заполенны чем-то - добавляем +1 к предствленности
                    SKUFact++;
                }
            }
        }

        OOS = 100 - 100 * (SKUFact / SKUPlan);
        OFS = 100 * (SKUFact / SKUPlan);
    }


    // 19.08.2020
    // Опция контроля
    // Проверка Место Положения (8299)
    private void checkMP(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, int warningType, int mode) {
        if (wpDataDB.getVisit_start_geo_distance() < 500 && wpDataDB.getVisit_start_geo_distance() > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }
    }


    // Опция контроля
    // Проверка наличия ФотоОтчётов (84932)
    public <T> boolean checkPhotoReport(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        boolean res = false;

        long dad2 = 0;
        if (dataDB instanceof WpDataDB) {
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
        }

        // Получаем даннные о наличии фотоотчёта из Журнала фотоОтчётов
        List<StackPhotoDB> list = RealmManager.getStackPhotoByDad2(dad2);

        Log.e("OPTION_CONTROL", "PHOTO_LIST: " + list.size());

        // Анализируем таблицу
        // Делаем отметки (записи) в строку WpDataDB wpDataDB, OptionsDB optionsDB,
        if (list.size() < 3) {
            Log.e("OPTION_CONTROL", "id: " + optionsDB.getID() + " |Signal: " + optionsDB.getIsSignal());
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });

            res = false;
        } else if (list.size() >= 3) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });

            res = true;
        }

        conductOptCheck(mode, res, optionsDB);

        return res;
    }

    /*Опция контроля 134583
     * Проверка наличия ФотоОтчётов с привязкой к Адресу (134583)
     *
     * Сначала получаю список фоток с данным типом по этому адресу, а потом проверяю - все ли
     * сделанны на месте.
     * */
    public <T> void checkPhotoReportWithMP(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {

        long dad2;
        if (dataDB instanceof WpDataDB) {
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
        } else {
            return;
        }

        String photoMinCount = optionsDB.getAmountMin();

        List<StackPhotoDB> list = RealmManager.getStackPhotoByDad2(dad2);
        if (photoMinCount != null && photoMinCount.equals(list.size()) || list.size() < 3) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
            return;
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }

    }


    /**
     * 19.04.2021
     * Опция контроля Количество фотографий
     */
    private <T> void checkPhoto(T dataDB, OptionsDB optionsDB, String photoType) {
        try {
            long dad2;
            if (dataDB instanceof WpDataDB) {
                dad2 = ((WpDataDB) dataDB).getCode_dad2();
            } else if (dataDB instanceof TasksAndReclamationsSDB) {
                dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
            } else {
                return;
            }

            RealmResults<StackPhotoDB> list = RealmManager.stackPhotoByDad2(dad2);
            List<StackPhotoDB> res = list.where().equalTo("photo_type", Integer.parseInt(photoType)).findAll();

            Log.e("OPTION_CONTROL", "checkPhoto id: " + optionsDB.getID() + " |Signal: " + optionsDB.getIsSignal());

            if (res.size() < 1) {
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionsDB != null) {
                        optionsDB.setIsSignal("1");
                        realm.insertOrUpdate(optionsDB);
                    }
                });
            } else if (list.size() >= 1) {
                RealmManager.INSTANCE.executeTransaction(realm -> {
                    if (optionsDB != null) {
                        optionsDB.setIsSignal("2");
                        realm.insertOrUpdate(optionsDB);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("OPTION_CONTROL", "checkPhoto e: " + e);
        }
    }


    // Опция контроля
    // Проверка начала работы (138519)

    /**
     * Начало работы
     */
    private void checkStartWork(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, int warningType, int mode) {
        Log.e("checkStartWork", "ENTER");
        if (wpDataDB.getVisit_start_dt() > 0) {
            Log.e("checkStartWork", "2");
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        } else {
            Log.e("checkStartWork", "1");
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }
    }

    // Опция контроля
    // Проверка окончания работы (138521)

    /**
     * Окончания работы
     */
    private void checkEndWork(Context context, WpDataDB wpDataDB, OptionsDB optionsDB, int warningType, int mode) {
        if (wpDataDB.getVisit_end_dt() > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }
    }


    /**
     * 29.06.2021
     * Опция контроля   (141911)
     * Проверка "Получение заказа в ТТ" (141910) ReceivingAnOrder
     */
    private <T> void checkReceivingAnOrder_141911(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        if (DetailedReportActivity.rpAmountSum > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }
    }

    /**
     * 29.06.2021
     * Опция контроля   (141888)
     * Проверка "Выкуп Товара с ТТ" (141889)
     */
    private <T> void check_RENAME_2(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {
        if (DetailedReportActivity.rpTotalSumToRedemptionOfGoods > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }
    }


    private <T> void checkEKL(Context context, T dataDB, OptionsDB optionsDB, OptionMassageType type, NNKMode mode) {

        long dad2, startWork, endWork;
        if (dataDB instanceof WpDataDB) {
            dad2 = ((WpDataDB) dataDB).getCode_dad2();
        } else if (dataDB instanceof TasksAndReclamationsSDB) {
            dad2 = ((TasksAndReclamationsSDB) dataDB).codeDad2;
        } else {
            return;
        }


        List<EKL_SDB> list = SQL_DB.eklDao().getByDad2(dad2);

//        List<EKL_SDB> list2 = ;

        int count = 0;
        for (EKL_SDB item : list) {
            if (item.eklCode != null && item.eklHashCode != null) {
                count++;
                break;
            }
        }

        if (count > 0) {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("2");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        } else {
            RealmManager.INSTANCE.executeTransaction(realm -> {
                if (optionsDB != null) {
                    optionsDB.setIsSignal("1");
                    realm.insertOrUpdate(optionsDB);
                }
            });
        }
    }


    // ----------------------------------

    public void checkingSignalsOfTheExecutorReport(Context context, WpDataDB wpDataDB, int warningType, int mode) {
//        Log.e("OPTION_CONTROL", "OPTION_CONTROL: " + "ENTER");
//
//        // Получить Опции (Таблицу)
//        WorkPlan workPlan = new WorkPlan();
//        try {
//            List<OptionsDB> list = RealmManager.getOptionsButton(workPlan.getWpOpchetId(wpDataDB));
//            for (int i = 0; i < list.size(); i++) {
//                optionControl(context, wpDataDB, list.get(i), warningType, mode);
//            }
//        } catch (Exception e) {
//            Globals.addLog();// Не работает
//        }
    }


    // =============================================================
    // ОПЦИИ ДЛЯ ДЕТАЛ. ОТЧЁТА (ОТЧЁТА ИСПОЛНИТЕЛЯ / REPORT PREPARE)
    // =============================================================

    /**
     * 26.01.2021
     */
    // 1. Допустим Опции у нас уже отсортированы пришли так как надо
    public String getOptionString(List<OptionsDB> optionsDB, ReportPrepareDB reportPrepareTovar, boolean promotion) {
        String res = ""; // Итоговая строка всех ТПЛ-ов
        StringBuilder tplRequired = new StringBuilder(); // Обязательные ТПЛ-ы
        StringBuilder tplOptional = new StringBuilder(); // Опциональные ТПЛ-ы

        // Получаем список Опций (Ф,Ц,П...) сам список захардкожен.
        List<TovarOptions> listTovOpt = getTovarOptins();

        // Создаём временный список Опций ТПЛ-ов
        List<TovarOptions> temps = new ArrayList<>();

        // Пробегаемся по всем опциям данного документа
        for (OptionsDB option : optionsDB) {
            int optionId = Integer.parseInt(option.getOptionId()); // Получаем ID опции
            int optionControlId = Integer.parseInt(option.getOptionControlId()); // Получаем ID опции

            Log.e("getOptionString", "------------------------------------------");
            Log.e("getOptionString", "optionId: " + optionId);
            Log.e("getOptionString", "optionControlId: " + optionControlId);
            Log.e("getOptionString", "option so: " + option.getSo());
            Log.e("getOptionString", "option: " + option.getOptionTxt());

            // Есть ли данная опция среди Опций ТПЛ-ов (та что захардкожена)
            // todo нельзя ли использовать сейчас listTovOpt?
            /*19.10.21 -- Внезапно оказалось что Ф.Ц.К.. могут не только в optionId находиться, но
             * и в optionControlId. На данный момент ситуация выглядит следущим образом: Есть опции
             * по типу "Начало работы" и тп.., а есть опции Фейс, Цена, Кол-во и тп..
             * Ф.Ц.К.. должны отображаться как и из "прихардкоженного" второго варианта, так и из
             * опции контроля из первого типа опций*/
            // TODO НОРМАЛЬНО НАПИСАТЬ ЭТОТ МОМЕНТ
            if (ids.contains(optionId)) {
                // Получаю подробную инфу о текущей ТПЛке
                TovarOptions temp = listTovOpt.get(listTovOpt.indexOf(new TovarOptions(optionId)));

                Log.e("getOptionString", "tempO: " + new Gson().toJson(temp));

                // Должен добавить в 'temps' элемент + записывать в опцию её символ
                // todo должен написать функцию.
                if (!containsName(temps, temp.getOrderField())) {
                    if ((temp.getOptionControlName().equals(AKCIYA_ID) || temp.getOptionControlName().equals(AKCIYA)) && promotion) {
                        // ничего не делаю
                    } else {
                        Globals.Triple uploaded = checkUploadedTPL(reportPrepareTovar, getTPLData(temp, reportPrepareTovar));
                        tplRequired.append(setOptionTPLColor(temp.getOptionShort(), true, uploaded));
                        temps.add(temp);
                    }
                }
            }

            // 14830 Петрикор Вайн
            // 77643 Си Джи Трейд ООО
            // 78230 Триумф
            // 91166 Міні мелст Україна ООО
            // 9382 Витмарк
            // менять парралельно с getRequiredOptionsTPL
/*            if (!option.getClientId().equals("14830") &&
                    !option.getClientId().equals("77643") &&
                    !option.getClientId().equals("78230") &&
                    !option.getClientId().equals("91166")){
                if (ids.contains(optionControlId)) {
                    TovarOptions temp = listTovOpt.get(listTovOpt.indexOf(new TovarOptions(optionControlId)));

                    Log.e("getOptionString", "tempC: " + new Gson().toJson(temp));

                    if (!containsName(temps, temp.getOrderField())) {
                        if ((temp.getOptionControlName().equals(AKCIYA_ID) || temp.getOptionControlName().equals(AKCIYA)) && promotion) {
                            // ничего не делаю
                        } else {
                            Globals.Triple uploaded = checkUploadedTPL(reportPrepareTovar, getTPLData(temp, reportPrepareTovar));
                            tplRequired.append(setOptionTPLColor(temp.getOptionShort(), true, uploaded));
                            temps.add(temp);
                        }
                    }
                }
            }*/
        }

        // После пробега по опциям данного документа (они являются обязательными), я должен
        // разобраться с опциональными. + разукрасить их в зависимости от того как они и чем заполнены
        for (TovarOptions option : listTovOpt) {
            boolean flag = false;
            for (TovarOptions temp : temps) {
                if (option.equals(temp)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                Globals.Triple uploaded = checkUploadedTPL(reportPrepareTovar, getTPLData(option, reportPrepareTovar));
                Log.e("TPL_UPLOAD", "2: " + option.getOptionShort());
                Log.e("TPL_UPLOAD", "2.uploaded: " + uploaded);
                tplOptional.append(setOptionTPLColor(option.getOptionShort(), false, uploaded));
//                tplOptional.append(option.getOptionShort());
            }
        }


        res = tplRequired + "/" + tplOptional;

        return res;
    }

    /***/
    public static boolean optionConstraintTPL(List<OptionsDB> optionsDB) {
        boolean res = false;
        List<TovarOptions> listTovOpt = getTovarOptins();
        for (OptionsDB option : optionsDB) {
            int optionId = Integer.parseInt(option.getOptionId()); // Получаем ID опции
            int optionControlId = Integer.parseInt(option.getOptionControlId()); // Получаем ID опции

            Log.e("optionConstraintTPL", "optionId: " + optionId);
            Log.e("optionConstraintTPL", "optionControlId: " + optionControlId);

            Log.e("optionConstraintTPL", "ids: " + ids);

            if (ids.contains(optionId)) {
                Log.e("optionConstraintTPL", "НАШЕЛ: optionId: " + optionId);
                res = true;
            }

            if (ids.contains(optionControlId)) {
                Log.e("optionConstraintTPL", "НАШЕЛ: optionControlId: " + optionControlId);
                res = true;
            }
        }
        return res;
    }


    /**
     * 26.01.2021
     */
    private String getTPLData(TovarOptions tovarOptions, ReportPrepareDB tableRow) {
        if (tableRow == null) return "";
        switch (tovarOptions.getOptionControlName()) {
            case PRICE:
                return tableRow.getPrice();
            case FACE:
                return tableRow.getFace();
            case EXPIRE_LEFT:
                return tableRow.getExpireLeft();
            case AMOUNT:
                return String.valueOf(tableRow.getAmount());
            case UP:
                return tableRow.getUp();
            case DT_EXPIRE:
                return tableRow.getDtExpire();
            case OBOROTVED_NUM:
                return tableRow.getOborotvedNum();
            case ERROR_ID:
                return tableRow.getErrorId();
            case AKCIYA_ID:
                return tableRow.getAkciyaId();
            case AKCIYA:
                return tableRow.getAkciya();
            case NOTES:
                return tableRow.getNotes();

            default:
                return "";
        }
    }


    /**
     * 26.01.2021
     * Определение "выгруженности"
     */
    private Globals.Triple checkUploadedTPL(ReportPrepareDB tableRow, String data) {

        Log.e("checkUploadedTPL2", "data: " + data);

        if (tableRow == null) return Globals.Triple.NO_DATA;

        if (data == null || data.equals("")) return Globals.Triple.NO_DATA;

        if (tableRow.getUploadStatus() == 0 && !data.equals("0") && !data.equals("0000-00-00")) {
            return Globals.Triple.TRUE;
        } else if (tableRow.getUploadStatus() == 1 && !data.equals("0")) {
            return Globals.Triple.FALSE;
        } else if (data.equals("0") || data.equals("0000-00-00")) { // todo чо ругаешься
            return Globals.Triple.NO_DATA;
        } else {
            return Globals.Triple.NO_DATA;
        }
    }


    /**
     * 26.01.2021
     * Разукрашивание опций.
     */
    private String setOptionTPLColor(String res, boolean required, Globals.Triple uploaded) {
        String green = "<font color=green>TPL</font>";
        String yellow = "<font color=yellow>TPL</font>";
        String req = "<b>TPL</b>";

        if (required) res = req.replace("TPL", res);

        if (uploaded.equals(Globals.Triple.TRUE)) res = green.replace("TPL", res);
        else if (uploaded.equals(Globals.Triple.FALSE)) res = yellow.replace("TPL", res);

        return res;
    }


    /**
     * 20.07.2020
     * <p>
     * Узнаём - есть ли уже опция тпл-ов уже в листе.
     * <p>
     * (нужно изза того что некоторые опции имеют несколько id из-за чего могут
     * лишний раз дублироваться)
     * <p>
     * true - если уже есть такая опция
     */
    private boolean containsName(final List<TovarOptions> list, final String orderField) {
        for (TovarOptions o : list)
            if (o.getOrderField().equals(orderField)) return true;
        return false;
    }


    /**
     * 20.07.2020
     * <p>
     * Нужно для определения - есть ли подсказки в Опциях ТПЛ или нет
     */
    private boolean containsNameReportHint(final List<ReportHintList> list, final String field) {
        for (ReportHintList o : list)
            if (o.getField().equals(field)) return true;
        return false;
    }


    // Передача инфы об обязательных Опциях ТПЛов
    public List<TovarOptions> getRequiredOptionsTPL(List<OptionsDB> optionsDB, boolean promotion) {
        List<TovarOptions> tplOptionsList = getTovarOptins();

        List<TovarOptions> temps = new ArrayList<>();
        for (OptionsDB option : optionsDB) {
            int optionId = Integer.parseInt(option.getOptionId());
            int optionControlId = Integer.parseInt(option.getOptionControlId());
            if (ids.contains(optionId)) {
                int optId = Integer.parseInt(option.getOptionId());
                TovarOptions temp = tplOptionsList.get(tplOptionsList.indexOf(new TovarOptions(optId)));

                // Это нужно что б 2 раза не появлялись Диалоги
                // Проверяем "есть ли уже такая опция" ?
                if (!temps.contains(temp)) {
                    if ((temp.getOptionControlName().equals(AKCIYA_ID) || temp.getOptionControlName().equals(AKCIYA)) && promotion) {
                        // ничего не делаю
//                        temps.add(temp);
                    } else {
                        temps.add(temp);
                    }
                } else {
                    Log.e("dublicateTPL", "ПОВТОРЯЕТСЯ");
                }
            }

/*            // Менять парралельно с строками
            if (!option.getClientId().equals("14830") && !option.getClientId().equals("9382")) {
                if (ids.contains(optionControlId)) {
                    TovarOptions temp = tplOptionsList.get(tplOptionsList.indexOf(new TovarOptions(optionControlId)));
                    // Это нужно что б 2 раза не появлялись Диалоги
                    // Проверяем "есть ли уже такая опция" ?
                    if (!temps.contains(temp)) {
                        if ((temp.getOptionControlName().equals(AKCIYA_ID) || temp.getOptionControlName().equals(AKCIYA)) && promotion) {
                            // ничего не делаю
                        } else {
                            temps.add(temp);
                        }
                    }
                }
            }*/
        }
        return temps;
    }

    public List<TovarOptions> getAllOptionsTPL() {
        return getTovarOptins();
    }


    /*отвечаю на вопрос: зачем там array idшников опций?
    - Потому что, в зависимости от этой опции, колонка в базе данных (тут говорим про колонку из
    таблички ReportPrepare (дет. отчёт)) она будет себя вести по разному. Разный текст ил разное
    поведение. На данный момент это зависит от ТЕМЫ данного посещения (вроде из WPData)
    */
    private static List<TovarOptions> getTovarOptins() {
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
            list.add(new TovarOptions(PRICE, "Ц", "Цена товара", "price", "main", 579));
            list.add(new TovarOptions(FACE, "Ф", "Кол. фейсов", "face", "main", 576, 76815));
            list.add(new TovarOptions(EXPIRE_LEFT, "В", "Возврат", "expire_left", "main", 135591, 165275));
            list.add(new TovarOptions(AMOUNT, "К", "Кол. на витрине", "amount", "main", 578, 587, 1465, 158244));
            list.add(new TovarOptions(UP, "П", "Поднято товара", "up", "main", 138644));
            list.add(new TovarOptions(DT_EXPIRE, "Д", "Дата ок. ср. год", "dt_expire", "main", 84005, 84967, 164985, 165276));
            list.add(new TovarOptions(OBOROTVED_NUM, "О", "Остаток по учёту", "oborotved_num", "main", 2243, 135448));
            list.add(new TovarOptions(ERROR_ID, "Ш", "Ошибка товара", "error_id", "main", 135592, 157242));
            list.add(new TovarOptions(AKCIYA_ID, "А", "Вид акции", "akciya_id", "main", 80977));
            list.add(new TovarOptions(AKCIYA, "Н", "Наличие акции", "akciya", "main", 80977));
            list.add(new TovarOptions(NOTES, "П", "Примечание", "notes", "main", 135590));

//            list.add(new TovarOptions(NOTES, "П", "Примечание", "notes", "main", 135590));
//            list.add(new TovarOptions(AKCIYA, "Н", "Наличие акции", "akciya", "main", 80977));
//            list.add(new TovarOptions(AKCIYA_ID, "А", "Вид акции", "akciya_id", "main", 80977));
//            list.add(new TovarOptions(ERROR_ID, "Ш", "Ошибка товара", "error_id", "main", 135592));
//            list.add(new TovarOptions(OBOROTVED_NUM, "О", "Остаток по учёту", "oborotved_num", "main", 2243, 135448));
//            list.add(new TovarOptions(DT_EXPIRE, "Д", "Дата ок. ср. год", "dt_expire", "main", 84005, 84967));
//            list.add(new TovarOptions(UP, "П", "Поднято товара", "up", "main", 138644));
//            list.add(new TovarOptions(AMOUNT, "К", "Кол. на витрине", "amount", "main", 578, 587, 1465));
//            list.add(new TovarOptions(EXPIRE_LEFT, "В", "Возврат", "expire_left", "main", 135591));
//            list.add(new TovarOptions(FACE, "Ф", "Кол. фейсов", "face", "main", 576, 76815));
//            list.add(new TovarOptions(PRICE, "Ц", "Цена товара", "price", "main", 579));

            for (TovarOptions to : list) {
                ids.addAll(to.getOptionId());
            }
        }
        return list;
    }


    /**
     * 27.01.2022
     * Установка опции как блокирующей
     */
    public void conductOptCheck(NNKMode mode, boolean status, OptionsDB optionsDB) {
        switch (mode) {
            case CHECK:
//                if (!status && optionsDB.getBlockPns().equals("1")) {
//                    optionNotConduct.add(optionsDB);
//                }
                break;

            case NULL:
                // Ничего делать не буду
                break;
        }
    }

    private Activity unwrap(Context context) {
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        assert context instanceof Activity;
        return (Activity) context;
    }

}




/*
$options_list_tpl=array(
        array( Число
            'options'    =>array(579),
            'sign'       =>'Ц',
            'sign_descr' =>'Цена товара',
            'order_field'=>'price'
        ),
        array( Число
            'options'    =>array(576, 76815),
            'sign'       =>'Ф',
            'sign_descr' =>'Кол. фейсов',
            'order_field'=>'face'
        ),
        array( Число
            'options'    =>array(135591),
            'sign'       =>'В',
            'sign_descr' =>'Возврат',
            'order_field'=>'expire_left',
            'show_always'=>true
        ),
        array( Числ о
            'options'    =>array(578, 1465),
            'sign'       =>'К',
            'sign_descr' =>'Кол. на витрине',
            'order_field'=>'amount'
        ),
        array( ДАта
            'options'    =>array(84005, 84967),
            'sign'       =>'Д',
            'sign_descr' =>'Дата ок. ср. год',
            'order_field'=>'dt_expire'
        ),
        array( Число
            'options'    =>array(135448, 2243),
            'sign'       =>'О',
            'sign_descr' =>'Остаток по учёту',
            'order_field'=>'oborotved_num'
        ),
        array( Число, предопределённый тип (это надо получать) Справочник Ошибок
            'options'    =>array(135592),
            'sign'       =>'Ш',
            'sign_descr' =>'Ошибка товара',
            'order_field'=>'error_id',
            'show_always'=>true
        ),
        array(  Справочник Акций, выбор
            'options'    =>array(80977),
            'sign'       =>'А',
            'sign_descr' =>'Вид акции',
            'order_field'=>'akciya_id'
        ),
        array( 0 - не выбрано, 1 - есть , 2 - нет
            'options'    =>array(80977),
            'sign'       =>'Н',
            'sign_descr' =>'Наличие акции',
            'order_field'=>'akciya'
        ),
        array(  Строка
            'options'    =>array(135590),
            'sign'       =>'П',
            'sign_descr' =>'Примечание',
            'order_field'=>'notes',
            'show_always'=>true
        )


(PRICE,         "Ц", "Цена товара"              , "price", "main", 579));
(FACE,          "Ф", "Кол. фейсов"              , "face", "main", 576, 76815));
(EXPIRE_LEFT,   "В", "Возврат"                  , "expire_left", "main", 135591, 165275));
(AMOUNT,        "К", "Кол. на витрине"          , "amount", "main", 578, 1465));
(UP,            "П", "Поднято товара"           , "up", "main", 138644));
(DT_EXPIRE,     "Д", "Дата ок. ср. год"         , "dt_expire", "main", 84005, 84967, 164985, 165276));
(OBOROTVED_NUM, "О", "Остаток по учёту"         , "oborotved_num", "main", 2243, 135448));
(ERROR_ID,      "Ш", "Ошибка товара"            , "error_id", "main", 135592, 157242, !157241, !157243));
(AKCIYA_ID,     "А", "Вид акции"                , "akciya_id", "main", 80977));
(AKCIYA,        "Н", "Наличие акции"            , "akciya", "main", 80977));
(NOTES,         "П", "Примечание"               , "notes", "main", 135590));



        );*/
