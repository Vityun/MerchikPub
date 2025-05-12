package ua.com.merchik.merchik.Options.Buttons;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ua.com.merchik.merchik.Activities.Features.FeaturesActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.R;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.dataLayer.ContextUI;
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm;
import ua.com.merchik.merchik.features.main.DBViewModels.SamplePhotoSDBViewModel;

public class OptionButtonPhotoShowcaseNear<T> extends OptionControl {
    public static int OPTION_BUTTON_PhotoShowcaseCorporateBlock_ID = 158309;

    private WpDataDB wpDataDB;
    private final WorkPlan workPlan = new WorkPlan();

    public OptionButtonPhotoShowcaseNear(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
            try {
                AddressSDB addr = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
                TradeMarkDB tradeMarkDB = TradeMarkRealm.getTradeMarkRowById(String.valueOf(addr.tpId));

                Intent intent = new Intent(context, FeaturesActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("viewModel", SamplePhotoSDBViewModel.class.getCanonicalName());
                bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_158309.toString());
                JsonObject dataJson = new JsonObject();
                dataJson.addProperty("tradeMarkDBId", tradeMarkDB != null ? tradeMarkDB.getID() : "0");
                dataJson.addProperty("wpDataDBId", String.valueOf(wpDataDB.getId()));
                dataJson.addProperty("optionDBId", String.valueOf(optionDB.getID()));
                bundle.putString("dataJson", new Gson().toJson(dataJson));
                bundle.putString("title", R.string.title_samplephotosdb + ", ");
                bundle.putString("subTitle", "В списке представлены образцы фотоотчетов. " +
                        "Для того, чтобы изготовить 'Фото витрины наближене' нажмите на соответствующую фотографию. " +
                        "Затем увеличьте ее до размера экрана и выполните фото, нажав на кнопку фотоаппарата в правом нижнем углу. ");
                intent.putExtras(bundle);
                context.startActivity(intent);
            } catch (Exception e) {
                Globals.writeToMLOG("ERROR", "OptionButtonPhotoBeforeStartWork/executeOption/Exception", "Exception e: " + e);
            }

//            WPDataObj wpDataObj = workPlan.getKPS(wpDataDB.getId());
//            wpDataObj.setPhotoType("39");
//
//            MakePhoto makePhoto = new MakePhoto();
//            makePhoto.pressedMakePhotoOldStyle((Activity) context, wpDataObj, wpDataDB, optionDB);
        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "OptionButPhotoPlanogramm/executeOption/Exception", "Exception e: " + e);
        }
    }
}
