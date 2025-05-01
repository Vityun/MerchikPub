package ua.com.merchik.merchik.Options.Buttons;

import static ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity.NEED_UPDATE_UI_REQUEST;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.WPDataObj;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.dataLayer.ModeUI;
import ua.com.merchik.merchik.dataLayer.common.VizitShowcaseDataHolder;
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm;
import ua.com.merchik.merchik.features.main.DBViewModels.PlanogrammVizitShowcaseViewModel;
import ua.com.merchik.merchik.features.main.DBViewModels.SamplePhotoSDBViewModel;

public class OptionButPhotoPlanogramm<T> extends OptionControl {
    public int OPTION_BUTTON_PHOTO_PLANOGRAMM_ID = 151139;

    private WpDataDB wpDataDB;
    private final WorkPlan workPlan = new WorkPlan();

    public OptionButPhotoPlanogramm(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
        new Globals().fixMP(wpDataDB, null);// Фиксация Местоположения в таблице ЛогМп
        try {

//            AddressSDB addr = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
//            TradeMarkDB tradeMarkDB = TradeMarkRealm.getTradeMarkRowById(String.valueOf(addr.tpId));
//            String tradeMarkId = tradeMarkDB == null ? "" : tradeMarkDB.getID();
//
//            Intent intent = new Intent(context, FeaturesActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putString("viewModel", SamplePhotoSDBViewModel.class.getCanonicalName());
//            bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_164355.toString());
//            JsonObject dataJson = new JsonObject();
//            dataJson.addProperty("tradeMarkDBId", tradeMarkId);
//            dataJson.addProperty("wpDataDBId", String.valueOf(wpDataDB.getId()));
//            dataJson.addProperty("optionDBId", String.valueOf(optionDB.getID()));
//            bundle.putString("dataJson", new Gson().toJson(dataJson));
//            bundle.putString("title", R.string.title_samplephotosdb + ", ");
//            bundle.putString("subTitle", "В списке представлены образцы фотоотчетов. " +
//                    "Для того, чтобы изготовить 'Фото Планограмы Торговой Точки' нажмите на соответствующую фотографию. " +
//                    "Затем увеличьте ее до размера экрана и выполните фото, нажав на кнопку фотоаппарата в правом нижнем углу. ");
//            intent.putExtras(bundle);
//            context.startActivity(intent);

//            VizitShowcaseDataHolder.Companion.getInstance().clear();
//
//            Intent intent = new Intent(context, FeaturesActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putString("viewModel", PlanogrammVizitShowcaseViewModel.class.getCanonicalName());
//            bundle.putString("contextUI", ContextUI.PLANOGRAMM_VIZIT_SHOWCASE.toString());
//            bundle.putString("modeUI", ModeUI.DEFAULT.toString());
//            JsonObject dataJson = new JsonObject();
//            dataJson.addProperty("clientId", String.valueOf(wpDataDB.getClient_id()));
//            dataJson.addProperty("addressId", wpDataDB.getAddr_id());
//            dataJson.addProperty("wpDataDBId", String.valueOf(wpDataDB.getCode_dad2()));
//            dataJson.addProperty("optionDBId", String.valueOf(optionDB.getID()));
//            bundle.putString("dataJson", new Gson().toJson(dataJson));
//
//            bundle.putString("title", "Планограма > Вітрина");
//            bundle.putString(
//                    "subTitle",
//                    "Для кожної Планограми вкажіть Вітрину, на якiй товар буде викладено згідно поточної планограми. Якщо Фото відповідної вітрини у списку вітрин немає, виберіть Фото Вітрини. Якщо у ТТ немає Вітрини для якої створена ця Планограма, то оцініть цю Планограму низькою оцінкою (нижче 5) і вкажіть коментар"
//            );
//            intent.putExtras(bundle);
//            ActivityCompat.startActivityForResult((Activity) context, intent, NEED_UPDATE_UI_REQUEST, null);

            WPDataObj wpDataObj = workPlan.getKPS(wpDataDB.getId());
            wpDataObj.setPhotoType("5");

            MakePhoto makePhoto = new MakePhoto();
            makePhoto.pressedMakePhotoOldStyle((Activity) context, wpDataObj, wpDataDB, optionDB);
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "OptionButPhotoPlanogramm/executeOption/Exception", "Exception e: " + e);
        }
    }
}
