package ua.com.merchik.merchik.Options.Buttons;

import android.content.Context;

import ua.com.merchik.merchik.Activities.DetailedReportActivity.DetailedReportActivity;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.MakePhoto.MakePhoto;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.WorkPlan;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

public class OptionButtonPhotoBeforeStartWork<T> extends OptionControl {
    public int OPTION_BUTTON_PHOTO_BEFORE_START_WORK_ID = 135809;

    private WpDataDB wpDataDB;
    private final WorkPlan workPlan = new WorkPlan();

    public OptionButtonPhotoBeforeStartWork(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode, UnlockCodeResultListener unlockCodeResultListener) {
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
        new Globals().fixMP(wpDataDB, null);// Фиксация Местоположения в таблице ЛогМп
        try {

//            try {
//                AddressSDB addr = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
//                TradeMarkDB tradeMarkDB = TradeMarkRealm.getTradeMarkRowById(String.valueOf(addr.tpId));
//
//                Intent intent = new Intent(context, FeaturesActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("viewModel", SamplePhotoSDBViewModel.class.getCanonicalName());
//                bundle.putString("contextUI", ContextUI.SAMPLE_PHOTO_FROM_OPTION_135809.toString());
//                JsonObject dataJson = new JsonObject();
//                dataJson.addProperty("tradeMarkDBId", tradeMarkDB.getID());
//                dataJson.addProperty("wpDataDBId", String.valueOf(wpDataDB.getId()));
//                dataJson.addProperty("optionDBId", String.valueOf(optionDB.getID()));
//                bundle.putString("dataJson", new Gson().toJson(dataJson));
//                bundle.putString("title", R.string.title_samplephotosdb + ", ");
//                bundle.putString("subTitle", "В списке представлены образцы фотоотчетов. " +
//                        "Для того, чтобы изготовить 'Фото витрины до начала работ' нажмите на соответствующую фотографию. " +
//                        "Затем увеличьте ее до размера экрана и выполните фото, нажав на кнопку фотоаппарата в правом нижнем углу. ");
//                intent.putExtras(bundle);
//                context.startActivity(intent);
//            } catch (Exception e) {
//                Globals.writeToMLOG("ERROR", "OptionButtonPhotoBeforeStartWork/executeOption/Exception", "Exception e: " + e);
//            }


            MakePhoto makePhoto = new MakePhoto();
            makePhoto.pressedMakePhoto((DetailedReportActivity) context, wpDataDB, optionDB, "14"); // Фото До начала Работ

        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "OptionButtonPhotoBeforeStartWork/executeOption/Exception", "Exception e: " + e);
        }
    }
}
