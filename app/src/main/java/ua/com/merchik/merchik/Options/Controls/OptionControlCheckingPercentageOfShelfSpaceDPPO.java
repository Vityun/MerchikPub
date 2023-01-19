package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;

import java.util.List;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupClientSDB;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.tables.ReportPrepareRealm;

/**
 * 17.01.23.
 * Опция Контроля: Проверка доли полочного пространства (1455)
 *
 * Сравнивает ПЛАНОВЫЙ показатель ДОЛИ ПП с ФАКТИЧЕСКИМ (расчетным)
 * DPPO - длина полочного пространства
 * */
public class OptionControlCheckingPercentageOfShelfSpaceDPPO<T> extends OptionControl {
    public int OPTION_CONTROL_CHECK_PERCENTAGE_OF_SHELF_SPACE_ID = 1455;

    // document data
    private long dad2 = 0;
    private UsersSDB usersSDB;
    private CustomerSDB customerSDB;
    private AddressSDB addressSDB;
    private TovarGroupSDB tovarGroupSDB;
    private TovarGroupClientSDB tovarGroupClientSDB;

    public OptionControlCheckingPercentageOfShelfSpaceDPPO(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        getDocumentVar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            executeOption();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Build.VERSION.SDK_INT: ").append(Build.VERSION.SDK_INT).append("  Build.VERSION_CODES.N: ").append(Build.VERSION_CODES.N);
            Globals.writeToMLOG("INFO", "OptionControlCheckingPercentageOfShelfSpace", "sb: " + sb);
        }
    }

    private void getDocumentVar() {
        if (document instanceof WpDataDB) {
            WpDataDB wp = (WpDataDB) document;

            dad2 = wp.getCode_dad2();

            usersSDB = SQL_DB.usersDao().getById(wp.getUser_id());
            customerSDB = SQL_DB.customerDao().getById(wp.getClient_id());
            addressSDB = SQL_DB.addressDao().getById(wp.getAddr_id());
            tovarGroupClientSDB = SQL_DB.tovarGroupClientDao().getByAddrId(addressSDB.tpId);
            tovarGroupSDB = SQL_DB.tovarGroupDao().getById(tovarGroupClientSDB.tovarGrpId);
        }
    }

    private void executeOption() {
        // Получаем RP(товары) для дальнейшего анализа.
        List<ReportPrepareDB> reportPrepare = ReportPrepareRealm.getReportPrepareByDad2(dad2);

    }


}
