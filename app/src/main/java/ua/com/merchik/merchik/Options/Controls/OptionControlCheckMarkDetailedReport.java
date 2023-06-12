package ua.com.merchik.merchik.Options.Controls;

import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.Options.OptionControl;
import ua.com.merchik.merchik.Options.Options;
import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.OptionMassageType;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;


/**
 * 12.06.23.
 * Выполняется проверка УРОВНЯ ОЦЕНКИ ДетОтчета по данному посещению. Для начисления снижения исполнителю, если есть низкие оценки по его ДетОтчету
 * Кроме данной функции есть еще проверка НАЛИЧИЯ ОЦЕНКИ ДетОтчетов исполнителем. ПровНаличОценДетОтч
 * Задумана для ОС так, чтобы старший не мог провести свой ОС не ОЦЕНИВ ДетОтчеты исполнителя. Но, можно применять и в других случаях
 * */
public class OptionControlCheckMarkDetailedReport<T> extends OptionControl {
    public int OPTION_CONTROL_CheckMarkDetailedReport_ID = 135708;

    private boolean signal = true;

    private WpDataDB wpDataDB;
    private CustomerSDB customerSDBDocument;
    private UsersSDB usersSDBDocument;
    private AddressSDB addressSDBDocument;

    private Long dateDocument;  // В секундах
    private Long dateFrom = 0L;
    private Long dateTo = 0L;

    private int averageRating;
    private int averageRatingMin = 6;   // минимальная СРЕДНЯЯ оценка, ниже которой, операторы начинают "страдать"
    private int averageRatingMax = 8;   // максимальная СРЕДНЯЯ оценка, выше которой, операторы начинают "страдать"

    public OptionControlCheckMarkDetailedReport(Context context, T document, OptionsDB optionDB, OptionMassageType msgType, Options.NNKMode nnkMode) {
        this.context = context;
        this.document = document;
        this.optionDB = optionDB;
        this.msgType = msgType;
        this.nnkMode = nnkMode;
        getDocumentVar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            executeOption();
        }
    }

    private void getDocumentVar() {
        try {
            if (document instanceof WpDataDB) {
                this.wpDataDB = (WpDataDB) document;

                customerSDBDocument = SQL_DB.customerDao().getById(wpDataDB.getClient_id());
                usersSDBDocument = SQL_DB.usersDao().getById(wpDataDB.getUser_id());
                addressSDBDocument = SQL_DB.addressDao().getById(wpDataDB.getAddr_id());
                dateDocument = wpDataDB.getDt().getTime() / 1000;

                dateFrom = Clock.getDatePeriodLong(dateDocument * 1000, -2) / 1000;
                dateTo = Clock.getDatePeriodLong(dateDocument * 1000, 7) / 1000;
            }

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlCheckMarkDetailedReport/getDocumentVar", "Exception e: " + e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void executeOption() {
        try {

        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "OptionControlCheckMarkDetailedReport/executeOption", "Exception e: " + e);
        }
    }


}
