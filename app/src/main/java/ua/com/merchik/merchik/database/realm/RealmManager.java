package ua.com.merchik.merchik.database.realm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.ServerExchange.TablesLoadingUnloading;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteHintsDB;
import ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects.SiteObjectsDB;
import ua.com.merchik.merchik.data.RealmModels.AddressDB;
import ua.com.merchik.merchik.data.RealmModels.AppUsersDB;
import ua.com.merchik.merchik.data.RealmModels.ArticleDB;
import ua.com.merchik.merchik.data.RealmModels.CustomerDB;
import ua.com.merchik.merchik.data.RealmModels.ErrorDB;
import ua.com.merchik.merchik.data.RealmModels.GroupTypeDB;
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;
import ua.com.merchik.merchik.data.RealmModels.LogDB;
import ua.com.merchik.merchik.data.RealmModels.LogMPDB;
import ua.com.merchik.merchik.data.RealmModels.MenuItemFromWebDB;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;
import ua.com.merchik.merchik.data.RealmModels.PPADB;
import ua.com.merchik.merchik.data.RealmModels.PromoDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.StackPhotoDB;
import ua.com.merchik.merchik.data.RealmModels.SynchronizationTimetableDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB;
import ua.com.merchik.merchik.data.RealmModels.UsersDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.data.TestJsonUpload.StratEndWork.StartEndData;
import ua.com.merchik.merchik.data.Translation.LangListDB;
import ua.com.merchik.merchik.data.Translation.SiteTranslationsList;
import ua.com.merchik.merchik.data.UploadToServ.LogUploadToServ;
import ua.com.merchik.merchik.data.UploadToServ.ReportPrepareServ;
import ua.com.merchik.merchik.data.UploadToServ.WpDataUploadToServ;
import ua.com.merchik.merchik.database.realm.tables.TradeMarkRealm;
import ua.com.merchik.merchik.database.realm.tables.WpDataRealm;

import static ua.com.merchik.merchik.Globals.APP_PREFERENCES;

public class RealmManager {

    public static Realm INSTANCE;
    private static Globals globals = new Globals();


    private static SharedPreferences sharedPreferences;
    public static void init(Context context) {
        Realm.init(context);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("myrealm.realm")
                .deleteRealmIfMigrationNeeded()
//                .schemaVersion(15)
                .allowWritesOnUiThread(true)
                .allowQueriesOnUiThread(true)
                .migration(new MyMigration())
                .build();
        Realm.setDefaultConfiguration(config);
        INSTANCE = Realm.getInstance(Realm.getDefaultConfiguration());


        sharedPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("realm", false)){
            List<SynchronizationTimetableDB> synchronizationTimetableDBList = RealmManager.getSynchronizationTimetable();
            if (synchronizationTimetableDBList == null){
                addSynchronizationTimetable();
            }
        }else {
            sharedPreferences.edit().putBoolean("realm", true).apply();
            addSynchronizationTimetable();
        }

    }

    private static void addSynchronizationTimetable() {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(1, "wp_data", 600, 0, 0, 0, 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(2, "image_tp", 36000, 0, 0, 0, 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(3, "client_group_tp", 36000, 0, 0, 0, 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(4, "log_mp", 600, 0, 0, 0, 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(5, "clients", 36000, 0, 0, 0, 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(6, "address", 36000, 0, 0, 0, 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(7, "users", 36000, 0, 0, 0, 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(8, "promoList", 3600000, 0, 0, 0, 0));     // Акции
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(9, "errorsList", 3600000, 0, 0, 0, 0));     // Ошибки
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(10, "stack_photo", 36000, 0, 0, 0, 0));     // стэк фото
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(11, "task_and_reclamations", 600, 0, 0, 0, 0));     // ЗИР
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(12, "planogram", 36000, 0, 0, 0, 0));     // Планограммы
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(13, "address_sql", 36000, 0, 0, 0, 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(14, "clients_sql", 36000, 0, 0, 0, 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(15, "users_sql", 36000, 0, 0, 0, 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(16, "city_sql", 36000, 0, 0, 0, 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(17, "oblast_sql", 36000, 0, 0, 0, 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(18, "sample_photo", 604800, 0, 0, 0, 0));    // Образцы фото    604800с = 7 дней
        INSTANCE.commitTransaction();
    }


/*    interface DB{
        set(SiteHintsDB data);
        delete();
    }

    generic<E implements DB>
    e.set();*/

/*    public static <T> boolean setData(SiteHintsDB<T> list){
        Log.e("SERV", "setGroupType_S");

        INSTANCE.beginTransaction();
        INSTANCE.delete(GroupTypeDB.class);
        for (int i = 0; i < list.size(); i++) {
            Log.e("SERV", "setGroupType: " + i + "\nID: " + list.get(i).getID());
            INSTANCE.copyToRealm(list.get(i));
        }
        INSTANCE.commitTransaction();

//        Globals globals = new Globals();
//        globals.deniedProgressBar(2);

        Log.e("SERV", "setGroupType_E");
        return true;
    }*/


    /**
     * Запись в Реалм Плана работ
     *
     * @param wpData список данных с сервера
     */
    public static boolean setWpData(List<WpDataDB> wpData) {
        Log.e("REALM_DB_UPDATE", "WP_DATA_START");
        INSTANCE.beginTransaction();
        INSTANCE.delete(WpDataDB.class);
        INSTANCE.copyToRealmOrUpdate(wpData);

//        int count = 0;
//        for (WpDataDB item : wpData){
//            JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(item), JsonObject.class);
//            Log.e("REALM_DB_UPDATE", "WP_DATA: item("+count+"): " + convertedObject);
//            count++;
//        }

        INSTANCE.commitTransaction();
        Log.e("REALM_DB_UPDATE", "WP_DATA_END");
        return true;
    }

    /**
     * 12.08.2020
     * <p>
     * Обновление Плана работ 2мя циклами.
     * Первой прогонкой мы получаем данные которые обновились
     * Второй прогонкой - удаляем устаревшие или удалённые данные
     *
     * @return
     */
    public static ArrayList<WpDataDB> setWpDataAuto(List<WpDataDB> serverData) {
        ArrayList<WpDataDB> sendOnServer = new ArrayList<>();

        try {
            RealmResults<WpDataDB> wpDataDBList1 = INSTANCE.where(WpDataDB.class) // Получаем всю БД (на данный момент она должна быть обновлена с сервера)
                    .findAll();

            Log.e("WP_DATA_UPDATE", "Количество данных что пришло с сервера: " + serverData.size());
            Log.e("WP_DATA_UPDATE", "Количество данных в приложении ДО Ц1: " + wpDataDBList1.size());

            // 1 цикл. Прогоняем данные которые пришли с сервераи надо обновить или добавить по ВПИ
            // По скольку с ID не одноначная ситуация (это не надёжный параметр который может внезапно меняться)
            // мне стоит для однозначного сравнения использовать 4 поля: 'code_dad2', 'user_id', 'client_id', 'isp'
            INSTANCE.beginTransaction();
            for (WpDataDB wp : serverData) {
                WpDataDB row = INSTANCE.where(WpDataDB.class)
                        .equalTo("code_dad2", wp.getCode_dad2())
                        .equalTo("user_id", wp.getUser_id())
                        .equalTo("client_id", wp.getClient_id())
                        .equalTo("isp", wp.getIsp())
                        .findFirst();
                if (row != null) {   // Если запись в бд есть
                    if (wp.getDt_update() >= row.getDt_update()) {    // Если на сервере данные более новые - обновляю(перезаписываю)
                        Log.e("setWpDataAuto", "Данные с сервера с большим VPI");
                        Log.e("WP_DATA_UPDATE", "MUST UPDATE (" + wp.getDt_update() + "/" + row.getDt_update() + ")");
                        INSTANCE.copyToRealmOrUpdate(wp);
                    } else {
                        // ТУТ ДЕЛАЮ ВЫГРУЗКУ НОВЫХ ДАННЫХ. ИЛИ СОБИРАЮ ДАННЫЕ ДЛЯ ТОГО ЧТО Б ПОТОМ ВЫГРУЗИТЬ.
                        Log.e("setWpDataAuto", "Эти данные на моей стороне более новые. Надо выгружать");
                        sendOnServer.add(row);
                        Log.e("WP_DATA_UPDATE", "Данные у пользователя новее. (" + wp.getDt_update() + "/" + row.getDt_update() + ")"); // Не нужно ли тут начать выгрузку этих самых данных?
                    }
                } else {
                    Log.e("WP_DATA_UPDATE", "Новые данные. Запись в БД.");
                    // Если записи в БД нет - просто записываем её туда.
                    Log.e("setWpDataAuto", "Такой записи в БД не было. Записываю к себе");
                    INSTANCE.copyToRealmOrUpdate(wp);
                }
            }
            INSTANCE.commitTransaction();

            // 2 цикл. Прогоняем данные которые надо удалить.
            RealmResults<WpDataDB> localData = INSTANCE.where(WpDataDB.class) // Получаем всю БД (на данный момент она должна быть обновлена с сервера)
                    .findAll();
/*        Log.e("WP_DATA_UPDATE", "Количество данных в приложении ПОСЛЕ Ц1: " + localData.size());

        for (WpDataDB localWP : localData) {
            Log.e("WP_DATA_UPDATE", "DELETE.start.=======================================");
            Log.e("WP_DATA_UPDATE", "DELETE.localWP: " + localWP.getId() + " cd2: " + localWP.getCode_dad2() + " user_id: " + localWP.getUser_id() + " client_id: " + localWP.getClient_id() + " isp: " + localWP.getIsp());
            Log.e("WP_DATA_UPDATE", "DELETE.------------------------------------------------");
            for (WpDataDB serverWP : serverData) {
                Log.e("WP_DATA_UPDATE", "DELETE.serverWP: " + serverWP.getId() + " cd2: " + serverWP.getCode_dad2() + " user_id: " + serverWP.getUser_id() + " client_id: " + serverWP.getClient_id() + " isp: " + serverWP.getIsp());
                Log.e("WP_DATA_UPDATE", "DELETE.equals: " + localWP.equals(serverWP));
            }
            Log.e("WP_DATA_UPDATE", "DELETE.end.=======================================");
        }*/

            INSTANCE.beginTransaction();
            for (WpDataDB local : localData) {
                if (!serverData.contains(local)) {
                    local.deleteFromRealm();
                }
            }
            INSTANCE.commitTransaction();


            Log.e("setWpDataAuto", "return: " + sendOnServer);

        }catch (Exception e){
            Globals.writeToMLOG("ERROR", "setWpDataAuto", "Exception e: " + e);
        }
        return sendOnServer;
    }


    /**
     * Запись в Реалм Типов Фото
     */
    public static boolean setImagesTp(List<ImagesTypeListDB> ImageTp) {
        Log.e("REALM_DB_UPDATE", "TYPE_START");

        INSTANCE.beginTransaction();
        INSTANCE.delete(ImagesTypeListDB.class);
        for (int i = 0; i < ImageTp.size(); i++) {
//            Log.e("SERV", "TYPE" + i);
            INSTANCE.copyToRealmOrUpdate(ImageTp.get(i));
        }
        INSTANCE.commitTransaction();
        Log.e("REALM_DB_UPDATE", "TYPE_END");
        return true;
    }


    /**
     * Запись в Реалм Груп Товаров
     *
     * @param groupTypeDB
     */
    public static void setGroupType(List<GroupTypeDB> groupTypeDB) {
        Log.e("REALM_DB_UPDATE", "setGroupType_S");

        INSTANCE.beginTransaction();
        INSTANCE.delete(GroupTypeDB.class);
        for (int i = 0; i < groupTypeDB.size(); i++) {
//            Log.e("SERV", "setGroupType: " + i + "\nID: " + groupTypeDB.get(i).getID());
            INSTANCE.copyToRealmOrUpdate(groupTypeDB.get(i));
        }
        INSTANCE.commitTransaction();
        Log.e("REALM_DB_UPDATE", "setGroupType_E");
    }

    public static boolean setGroupTypeV2(List<GroupTypeDB> customerGroupsListDB) {
        Log.e("REALM_DB_UPDATE", "setGroupType_S");

        INSTANCE.beginTransaction();
        INSTANCE.delete(GroupTypeDB.class);
        for (int i = 0; i < customerGroupsListDB.size(); i++) {
//            Log.e("SERV", "setGroupType: " + i + "\nID: " + customerGroupsListDB.get(i).getID());
            INSTANCE.copyToRealm(customerGroupsListDB.get(i));
        }
        INSTANCE.commitTransaction();
        Log.e("REALM_DB_UPDATE", "setGroupType_E");
        return true;
    }

    /**
     * Запись в Реалм Опций
     */
    public static boolean setOptions(List<OptionsDB> optionsDBS) {
        Log.e("REALM_DB_UPDATE", "OPTION_S");


        try {
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.RealmManager.class.setOptions.Размер списка: " + optionsDBS.size() + "\n");
        }catch (Exception e){
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.RealmManager.class.setOptions.Ошибка1: " + e + "\n");
        }

        INSTANCE.beginTransaction();
        INSTANCE.delete(OptionsDB.class);
        List<OptionsDB> res = INSTANCE.copyToRealmOrUpdate(optionsDBS);

        try {
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.RealmManager.class.setOptions.Размер сохранённого списка: " + res.size() + "\n");
        }catch (Exception e){
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.RealmManager.class.setOptions.Ошибка2: " + e + "\n");
        }



        INSTANCE.commitTransaction();
        Log.e("REALM_DB_UPDATE", "OPTION_E");
        return true;
    }

    public static void saveDownloadedOptions(List<OptionsDB> optionsDBS) {
        INSTANCE.beginTransaction();
        List<OptionsDB> res = INSTANCE.copyToRealmOrUpdate(optionsDBS);
        INSTANCE.commitTransaction();
    }

    /**
     * Запись в Реалм РепортПр
     */
    public static boolean setReportPrepare(List<ReportPrepareDB> reportPrepare) {
        Log.e("REALM_DB_UPDATE", "REPORT_S");
        INSTANCE.beginTransaction();
        INSTANCE.delete(ReportPrepareDB.class);
        INSTANCE.copyToRealmOrUpdate(reportPrepare);

//        int count = 0;
//        for (ReportPrepareDB item : reportPrepare){
//            JsonObject convertedObject = new Gson().fromJson(new Gson().toJson(item), JsonObject.class);
//            Log.e("REALM_DB_UPDATE", "ReportPrepareDB: item("+count+"): " + convertedObject);
//            count++;
//        }

        INSTANCE.commitTransaction();
        Log.e("REALM_DB_UPDATE", "REPORT_E");
        return true;
    }

    /**
     * Запись в Реалм Товары
     */
    public static boolean setTovar(List<TovarDB> list) {
        Log.e("REALM_DB_UPDATE", "setTovar_S");

        try {
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.RealmManager.class.setTovar.Размер списка: " + list.size() + "\n");
        }catch (Exception e){
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.RealmManager.class.setTovar.Ошибка1: " + e + "\n");
        }

        INSTANCE.beginTransaction();
//        INSTANCE.delete(TovarDB.class);
        List<TovarDB> res = INSTANCE.copyToRealmOrUpdate(list);


        try {
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.RealmManager.class.setTovar.Размер сохранённого списка: " + res.size() + "\n");
        }catch (Exception e){
            globals.writeToMLOG(Clock.getHumanTime() + "_INFO.RealmManager.class.setTovar.Ошибка2: " + e + "\n");
        }


        INSTANCE.commitTransaction();

        Log.e("REALM_DB_UPDATE", "setTovar_E");
        return true;
    }

    /**
     * Запись в Реалм ППА
     */
    public static void setPPA(List<PPADB> list) {
        Log.e("REALM_DB_UPDATE", "setPPA_S");

        INSTANCE.beginTransaction();
        INSTANCE.delete(PPADB.class);

        INSTANCE.copyToRealmOrUpdate(list);

        INSTANCE.commitTransaction();

        Log.e("REALM_DB_UPDATE", "setPPA_E");
    }

    /**
     * Запись в Реалм Артикула
     *
     * @param list
     */
    public static void setArticle(List<ArticleDB> list) {
        Log.e("REALM_DB_UPDATE", "setArticle_S");
        INSTANCE.beginTransaction();
        INSTANCE.delete(ArticleDB.class);
        for (int i = 0; i < list.size(); i++) {
            INSTANCE.copyToRealmOrUpdate(list.get(i));
        }
        INSTANCE.commitTransaction();
        Log.e("REALM_DB_UPDATE", "setArticle_E");
    }

    /**
     * Запись в Реалм Акции
     *
     * @param list
     */
    public static boolean setPromo(List<PromoDB> list) {
        Log.e("REALM_DB_UPDATE", "setPromo_S");
        INSTANCE.beginTransaction();
        INSTANCE.delete(PromoDB.class);
//        Log.e("PromoDB", "SIZE: " + list.size());
        for (int i = 0; i < list.size(); i++) {
//            Log.e("PromoDB", i + ": " + list.get(i).getID());
            INSTANCE.copyToRealmOrUpdate(list.get(i));
        }
        INSTANCE.commitTransaction();
        Log.e("REALM_DB_UPDATE", "setPromo_E");
        return true;
    }

    /**
     * Запись в Реалм Ошибки
     *
     * @param list
     */
    public static boolean setError(List<ErrorDB> list) {
        Log.e("REALM_DB_UPDATE", "setError_S");

        INSTANCE.beginTransaction();
        INSTANCE.delete(ErrorDB.class);
//        Log.e("ErrorDB", "SIZE: " + list.size());
        for (int i = 0; i < list.size(); i++) {
//            Log.e("ErrorDB", i + ": " + list.get(i).getID());
            INSTANCE.copyToRealmOrUpdate(list.get(i));
        }
        INSTANCE.commitTransaction();
        Log.e("REALM_DB_UPDATE", "setError_E");

        return true;
    }


    /**
     * Запись информации в таблицу ТОРГОВЫХ МАРОК
     */
    public static boolean setTradeMarks(List<TradeMarkDB> list) {
        Log.e("REALM_DB_UPDATE", "setError_S");
        INSTANCE.beginTransaction();
        INSTANCE.delete(TradeMarkDB.class);
//        Log.e("TradeMarkDB", "SIZE: " + list.size());
        for (int i = 0; i < list.size(); i++) {
//            Log.e("TradeMarkDB", i + ": " + list.get(i).getID());
            INSTANCE.copyToRealmOrUpdate(list.get(i));
        }
        INSTANCE.commitTransaction();
        Log.e("REALM_DB_UPDATE", "setError_E");
        return true;
    }


    //==============================================================================================
    //==============================================================================================
    //==============================================================================================


    // APP_USER_DB START ------------------------------------

    public static AppUsersDB getAppUser() {
        return INSTANCE.where(AppUsersDB.class)
                .findFirst();
    }

    public static AppUsersDB getAppUserById(String ids) {
        int id = Integer.parseInt(ids);
        return INSTANCE.where(AppUsersDB.class)
                .equalTo("userId", id)
                .findFirst();
    }

    public static void setAppUser(AppUsersDB appUsersDB) {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(appUsersDB);
        INSTANCE.commitTransaction();
    }

    // APP_USER_DB END --------------------------------------


    // ПЛАН РАБОТ:----------------------------------------------------------------------------------

    /**
     * Получение Всего плана работ
     */
    public static RealmResults<WpDataDB> getAllWorkPlan() {
        return INSTANCE.where(WpDataDB.class)
                .sort("dt_start", Sort.ASCENDING, "addr_id", Sort.ASCENDING)
                .findAll();
    }

    public static RealmResults<WpDataDB> getAllWorkPlanMAP() {
        return INSTANCE.where(WpDataDB.class)
                .sort("dt_start", Sort.ASCENDING)
                .distinct("addr_id")
                .findAll();
    }

    public static WpDataDB getWorkPlanRowById(long id) {
        //"SELECT * FROM wp_data WHERE id = " + wpId + ";"
        return INSTANCE.where(WpDataDB.class)
                .equalTo("ID", id)
                .findFirst();
    }


    public static int getWpDataDate(String dt) {
        Date date = Clock.stringDateConvertToDate(dt);
        return INSTANCE.where(WpDataDB.class)
                .equalTo("dt", date)
                .findAll().size();
    }

    /**
     * 29.12.2020
     * Попытка создать "универсальный" запрос к БД
     */
    public static int getWpData(int status, String dt) {    //TODO query CHANGE DATE

        Date date = Clock.stringDateConvertToDate(dt);

        return INSTANCE.where(WpDataDB.class)
                .equalTo("status", status)
                .equalTo("dt", date)
                .findAll().size();
    }

    // CUST GRP TYPE:-------------------------------------------------------------------------------
    public static RealmResults<GroupTypeDB> getAllGroupTypeByCustomerId(String customer_id) {
//"SELECT * FROM client_group_tp WHERE client_id = '" + customer_id + "';"
        Log.e("TAG_TEST_GRP", "cli_id(0): " + customer_id);
        int cli = Integer.parseInt(customer_id);
        return INSTANCE.where(GroupTypeDB.class)
                .equalTo("client_id", cli)
                .findAll();
    }


    // OPTIONS:-------------------------------------------------------------------------------------
    public static RealmResults<OptionsDB> getOptionsNOButton(long otchetId) {
        String sOtchetId = String.valueOf(otchetId);
        return INSTANCE.where(OptionsDB.class)
                .equalTo("docId", sOtchetId)
                .and()
                .notEqualTo("optionGroup", "3161")
                .findAll();
    }

    public static RealmResults<OptionsDB> getOptionsButton(long otchetId) {
        String sOtchetId = String.valueOf(otchetId);
        return INSTANCE.where(OptionsDB.class)
                .equalTo("docId", sOtchetId)
                .and()
                .equalTo("optionGroup", "3161")
                .findAll();
    }

    public static OptionsDB getOptionById(String id) {
        return INSTANCE.where(OptionsDB.class)
                .equalTo("iD", id)
                .findFirst();
    }

    public static RealmResults<OptionsDB> getOptionsByOtchetId(long otchetId, String codeDad2) {
        String sOtchetId = String.valueOf(otchetId);
        return INSTANCE.where(OptionsDB.class)
                .equalTo("docId", sOtchetId)
                .and()
                .equalTo("codeDad2", codeDad2)
                .findAll();
    }


    // IMAGES TP:-----------------------------------------------------------------------------------
    public static RealmResults<ImagesTypeListDB> getAllImagesTypeList() {
        //"SELECT * FROM images_tp;"
        return INSTANCE.where(ImagesTypeListDB.class)
                .findAll();
    }

    // STACK PHOTO:---------------------------------------------------------------------------------
    public static void stackPhotoSavePhoto(StackPhotoDB stackPhotoDB) {
        INSTANCE.beginTransaction();
        Log.e("TAG_TABLE", "PHOTO_TOVAR_URL_path_id: " + stackPhotoDB.getId());
        INSTANCE.copyToRealmOrUpdate(stackPhotoDB);
        INSTANCE.commitTransaction();
    }

    public static void stackPhotoSavePhoto(List<StackPhotoDB> stackPhotoDB) {
        INSTANCE.beginTransaction();
        Log.e("TAG_TABLE", "PHOTO_TOVAR_URL_path_id.List: " + stackPhotoDB.size());


//        for (StackPhotoDB item: stackPhotoDB){
//            Log.e("stackPhotoSavePhoto", "(I)item.getAddrId: " + item.getAddrId());
//        }

        List<StackPhotoDB> i = INSTANCE.copyToRealmOrUpdate(stackPhotoDB);

//        for (StackPhotoDB item: i){
//            Log.e("stackPhotoSavePhoto", "(O)item.getAddrId: " + item.getAddrId());
//        }

        INSTANCE.commitTransaction();
    }

    public static int stackPhotoGetLastId() {
        RealmResults<StackPhotoDB> realmResults = INSTANCE.where(StackPhotoDB.class)
//                .notEqualTo("photo_type", 18)
                .findAll();
        try {
            return Objects.requireNonNull(realmResults.last()).getId();
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean chechPhotoExist(String photoUri) {
        RealmResults<StackPhotoDB> realmResults = INSTANCE.where(StackPhotoDB.class)
                .equalTo("photo_num", photoUri)
                .findAll();
        return realmResults.isEmpty();
    }

    //    "SELECT count(*) FROM stack_photo WHERE upload_to_server = '' AND get_on_server = '';"
    public static int stackPhotoNotUploadedPhotosCount() {
        RealmResults<StackPhotoDB> realmResults = INSTANCE.where(StackPhotoDB.class)
                .equalTo("upload_to_server", 0)
                .equalTo("get_on_server", 0)
                .notEqualTo("photo_type", 18)
                .isNotNull("photo_hash")
                .findAll();
        return realmResults.size();
    }

    // Количество фото витрины
    // Зачем так криво? Надо будет отказаться от просто получения числа
    public static int stackPhotoShowcasePhotoCount(long codeDad2, int photoType) {
        RealmResults<StackPhotoDB> realmResults = INSTANCE.where(StackPhotoDB.class)
                .equalTo("code_dad2", codeDad2)
                .equalTo("photo_type", photoType)
                .isNotNull("photo_hash")
                .findAll();
        return realmResults.size();
    }


    /**
     * 19.04.2021
     * Получение списка фотографий по данному посещению.
     *
     * @return*/
    public static RealmResults<StackPhotoDB> stackPhotoByDad2(long codeDad2) {
        RealmResults<StackPhotoDB> realmResults = INSTANCE.where(StackPhotoDB.class)
                .equalTo("code_dad2", codeDad2)
                .isNotNull("photo_hash")
                .findAll();
        return realmResults;
    }


    // Получене фотографий для выгрузки на сервер
    //"SELECT * FROM stack_photo WHERE upload_to_server = '';"
    public static RealmResults<StackPhotoDB> getStackPhotoPhotoToUpload() {
        return INSTANCE.where(StackPhotoDB.class)
                .equalTo("upload_to_server", 0)
                .equalTo("get_on_server", 0)
                .notEqualTo("photo_type", 18)
                .and()
                .isNotNull("client_id")
                .isNotNull("addr_id")
                .isNotNull("photo_hash")
                .isNotNull("time_event")
                .findAll();
    }


    public static RealmResults<StackPhotoDB> getStackPhoto() {
        return INSTANCE.where(StackPhotoDB.class)
                .notEqualTo("photo_type", 18)
                .notEqualTo("photo_type", 29)
                .notEqualTo("photo_type", 5)
                .notEqualTo("photo_type", 35)
                .findAll();
    }

    public static RealmResults<StackPhotoDB> getStackPhotoLogByDad2(long dad2) {
        return INSTANCE.where(StackPhotoDB.class)
                .notEqualTo("photo_type", 18)
                .notEqualTo("photo_type", 29)
                .notEqualTo("photo_type", 5)
                .notEqualTo("photo_type", 35)
                .equalTo("code_dad2", dad2)
                .findAll();
    }

    public static void stackPhotoDeletePhoto() {

        // 1608285918335
        // 1608415200262

        long timeDeadline = System.currentTimeMillis() - 30000;
        long time = Globals.startOfDay(timeDeadline);

        Log.e("stackPhotoDeletePhoto", "timeDeadline: " + timeDeadline);
        Log.e("stackPhotoDeletePhoto", "time: " + time);

        RealmResults<StackPhotoDB> realmResults = INSTANCE.where(StackPhotoDB.class)
//                .lessThan("create_time", Globals.startOfDay(System.currentTimeMillis() - 172800000))
                .lessThan("create_time", time)
                .and()
                .notEqualTo("get_on_server", 0)
                .findAll();

        Log.e("stackPhotoDeletePhoto", "realmResults: " + realmResults.size());

        if (realmResults.size() > 0) {
            INSTANCE.beginTransaction();
//        realmResults.deleteAllFromRealm();
            Log.e("stackPhotoDeletePhoto", "realmResults.deleteAllFromRealm(): " + realmResults.deleteAllFromRealm());
            INSTANCE.commitTransaction();
        }
    }

    public static RealmResults<StackPhotoDB> stackPhotoGetHashs() {
        // SELECT * FROM stack_photo WHERE get_on_server = '';
        return INSTANCE.where(StackPhotoDB.class)
                .equalTo("get_on_server", 0)
                .notEqualTo("photo_type", 18)
                .findAll();
    }


    public static StackPhotoDB getTovarPhotoByIdAndType(Integer id, String photoServerId, Integer type, boolean size) {
        String photoSize = "small";
        if (size) photoSize = "full";

        Log.e("GET_TOV_PHOTO", "Data: " + id + "/" + type + "/" + size);
//        StackPhotoDB res = INSTANCE.where(StackPhotoDB.class)
//                .equalTo("object_id", id)
//                .equalTo("photoServerId", photoServerId)
//                .equalTo("photo_type", type)
//                .equalTo("comment", photoSize)
//                .findFirst();

        RealmQuery<StackPhotoDB> query = INSTANCE.where(StackPhotoDB.class)
                .equalTo("object_id", id)
                .equalTo("photo_type", type)
                .equalTo("comment", photoSize);

        if (photoServerId != null && !photoServerId.equals("0")){
            query.equalTo("photoServerId", photoServerId);
        }

        return query.findFirst();
    }


    public static ArrayList<TovarDB> getTovarListPhotoToDownload(List<TovarDB> list, String photoSize) {
        ArrayList<TovarDB> res = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            StackPhotoDB stackPhotoDB = INSTANCE.where(StackPhotoDB.class)
                    .equalTo("object_id", Integer.parseInt(list.get(i).getiD()))
                    .equalTo("comment", photoSize)
                    .findFirst();
            if (stackPhotoDB == null) {
                res.add(list.get(i));
            }
        }
        return res;
    }

    public static List<StackPhotoDB> getStackPhotoByDad2(long dad2) {
        return INSTANCE.where(StackPhotoDB.class)
                .equalTo("code_dad2", dad2)
                .findAll();
    }


    /**
     * 21.08.2020
     * Проверка - есть ли такая фотка в БД или нет.
     * Если она будет - true.
     */
    public static boolean stackPhotoExistByObjectId(int objectId) {
        return INSTANCE.where(StackPhotoDB.class)
                .equalTo("object_id", objectId)
                .findFirst() != null;
    }

    public static boolean stackPhotoExistByObjectId(int objectId, String type) {
        return INSTANCE.where(StackPhotoDB.class)
                .equalTo("object_id", objectId)
                .equalTo("comment", type)
                .findFirst() != null;
    }

    /**
     * 26.02.2021
     * Получение всех данных >= пришедшей ВПИ
     */
    public static List<StackPhotoDB> stackPhotoGetNewDataByDVI(long vpi) {
        return INSTANCE.where(StackPhotoDB.class)
                .greaterThanOrEqualTo("vpi", vpi)
                .findAll();
    }

    /**
     * 27.02.2021
     */
    public static StackPhotoDB stackPhotoGetPhotoById(String id) {
        return INSTANCE.where(StackPhotoDB.class)
                .equalTo("id", Integer.parseInt(id))
                .findFirst();
    }

    public static List<StackPhotoDB> stackPhotoDBListGetDVIToUpload() {
        return INSTANCE.where(StackPhotoDB.class)
                .isNotNull("photoServerId")
                .notEqualTo("photoServerId", "")
                .equalTo("dviUpload", true)
                .findAll();
    }


    public static List<StackPhotoDB> stackPhotoDBListGetCommentToUpload() {
        return INSTANCE.where(StackPhotoDB.class)
                .isNotNull("photoServerId")
                .notEqualTo("photoServerId", "")
                .equalTo("commentUpload", true)
                .findAll();
    }


    public static List<StackPhotoDB> stackPhotoDBListGetRatingToUpload() {
        return INSTANCE.where(StackPhotoDB.class)
                .isNotNull("photoServerId")
                .notEqualTo("photoServerId", "")
                .equalTo("markUpload", true)
                .findAll();
    }

    //==============================================================================================
    // STACK PHOTO END:-----------------------------------------------------------------------------
    //==============================================================================================


    // Synchronization Timetable:---------------------------------------------------------------------------------
    public static RealmResults<SynchronizationTimetableDB> getSynchronizationTimetable() {
        return INSTANCE.where(SynchronizationTimetableDB.class)
                .findAll();
    }

    public static SynchronizationTimetableDB getSynchronizationTimetableRowByTable(String tableName) {
        return INSTANCE.where(SynchronizationTimetableDB.class)
                .equalTo("table_name", tableName)
                .findFirst();
    }

    public static void setToSynchronizationTimetableDB(SynchronizationTimetableDB synchronizationTimetableDB) {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(synchronizationTimetableDB);
        INSTANCE.commitTransaction();
    }

// Synchronization Timetable END:---------------------------------------------------------------------------------


    // LOG:----------------------------START-----------------------------------------------

    public static void setRowToLog(List<LogDB> log) {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(log);
        INSTANCE.commitTransaction();
    }

    public static LogDB getLogRowById(String idS) {
        Integer id = Integer.valueOf(idS); // Костыль ибо напортачено с id шками на стороне сервера и тут
        LogDB rp = INSTANCE.where(LogDB.class)
                .equalTo("id", id)
                .findFirst();

        return RealmManager.INSTANCE.copyFromRealm(rp);
    }

    public static int getLastIdLogDB() {
        RealmResults<LogDB> realmResults = INSTANCE.where(LogDB.class)
                .findAll();
        try {
            Log.e("TAG_REALM_LOG", "int: " + Objects.requireNonNull(realmResults.last().getId()));

            return Objects.requireNonNull(realmResults.last().getId());
        } catch (Exception e) {
            return 0;
        }
    }

    public static ArrayList<LogUploadToServ> getLogToSend() {

        List<LogDB> list = INSTANCE.where(LogDB.class)
                .isNull("dt")
                .findAll();

        ArrayList<LogUploadToServ> logList = new ArrayList<>();

        if (list != null) {
            for (LogDB l : list) {
                logList.add(new LogUploadToServ(
                        String.valueOf(l.getId()),
                        String.valueOf(l.getDt_action()),
                        l.getComments(),
                        String.valueOf(l.getTp()),
                        l.getClient_id(),
                        String.valueOf(l.getAddr_id()),
                        String.valueOf(l.getObj_id()),
                        String.valueOf(System.currentTimeMillis() / 1000)
                ));

            }
        } else {
            // Данных нет, нужно логировать.
        }

        return logList;
    }


    /**
     * 17.08.2020
     * <p>
     * Сбор данных(План Работ) для выгрузки на сервер
     */
    public static ArrayList<WpDataUploadToServ> getWpDataToSend() {
        List<WpDataDB> list = INSTANCE.where(WpDataDB.class)
//                .isNull("dt")
                .findAll();

        ArrayList<WpDataUploadToServ> wpDataList = new ArrayList<>();
        if (list != null) {
            for (WpDataDB l : list) {
                if (l.getVisit_start_dt() > 0 || l.getVisit_end_dt() > 0) {
                    wpDataList.add(new WpDataUploadToServ(
                            String.valueOf(l.getId()),
                            String.valueOf(l.getCode_dad2()),
                            String.valueOf(l.getUser_id()),
                            String.valueOf(l.getClient_id()),
                            String.valueOf(l.getIsp()),

                            String.valueOf(l.getVisit_start_dt()),
                            String.valueOf(l.getVisit_end_dt()),
                            String.valueOf(l.getClient_start_dt()),
                            String.valueOf(l.getClient_end_dt()),

                            String.valueOf(l.getSetStatus())
                    ));
                }
            }
        } else {
            // Данных нет, нужно логировать.
        }
        return wpDataList;
    }


    /**
     * 31.03.2021
     * Сбор данных о начале/конце работы С ПЛАНА РАБОТ для выгрузки на сервер.
     * Выше написан старый метод который занимался почти 1:1 этим же
     * Это сделано сейчас для потому что переделываем выгрузку под новый стандарт
     */
    public static List<StartEndData> getWpDataStartEndWork() {
        List<WpDataDB> list = WpDataRealm.getWpData();
        List<StartEndData> res = new ArrayList<>();

        if (list != null) {
            for (WpDataDB l : list) {
                if (l.startUpdate || l.getSetStatus() > 0){
                    StartEndData item = new StartEndData();

                    item.element_id = String.valueOf(l.getId());
                    item.code_dad2 = String.valueOf(l.getCode_dad2());
                    item.user_id = String.valueOf(l.getUser_id());
                    item.client_id = String.valueOf(l.getClient_id());
                    item.isp = String.valueOf(l.getIsp());
                    item.dt_update = l.getDt_update();

                    if (l.startUpdate){
                        item.visit_start_dt = String.valueOf(l.getVisit_start_dt());
                        item.visit_end_dt = String.valueOf(l.getVisit_end_dt());
                        item.client_start_dt = String.valueOf(l.getClient_start_dt());
                        item.client_end_dt = String.valueOf(l.getClient_end_dt());
                        item.client_work_duration = String.valueOf(l.client_work_duration);
                        item.status_set = String.valueOf(l.getSetStatus());

                        item.user_comment = l.user_comment;
                        item.user_comment_dt_update = l.user_comment_dt_update;
                    }

                    res.add(item);
                }
            }
        }
        return res;
    }

    public enum WpDataUpload {COMMENT}

    public static List<StartEndData> getUploadWpData(WpDataUpload uploadMode) {
        List<WpDataDB> list = WpDataRealm.getWpData();
        List<StartEndData> res = new ArrayList<>();

        if (list != null) {
            for (WpDataDB l : list) {
                if (l.startUpdate){
                    StartEndData item = new StartEndData();

                    item.element_id = String.valueOf(l.getId());
                    item.code_dad2 = String.valueOf(l.getCode_dad2());
                    item.user_id = String.valueOf(l.getUser_id());
                    item.client_id = String.valueOf(l.getClient_id());
                    item.isp = String.valueOf(l.getIsp());
                    item.dt_update = System.currentTimeMillis()/1000; // TODO DELETE THIS, ONLY DEBUG

                    switch (uploadMode){
                        case COMMENT:
                            item.user_comment = l.user_comment;
                            item.user_comment_dt_update = l.user_comment_dt_update;
                            break;

                    }

                    res.add(item);
                }
            }
        }
        return res;
    }


    public static ArrayList<Map<String, String>> getLogToSend2() {

        Map<Integer, Map<String, String>> map = new HashMap<>();
        RealmResults<LogDB> realmResults = INSTANCE.where(LogDB.class)
                .isNull("dt")
                .findAll();


        ArrayList<Map<String, String>> LOG = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Map<String, String> MAP = new HashMap<>();
            MAP.put("id", String.valueOf(realmResults.get(i).getId()));
            MAP.put("dt_action", String.valueOf(realmResults.get(i).getDt_action()));
            MAP.put("comments", String.valueOf(realmResults.get(i).getComments()));
            MAP.put("tp", String.valueOf(realmResults.get(i).getTp()));
            MAP.put("client_id", String.valueOf(realmResults.get(i).getClient_id()));
            MAP.put("addr_id", String.valueOf(realmResults.get(i).getAddr_id()));
            MAP.put("obj_id", String.valueOf(realmResults.get(i).getObj_id()));
            MAP.put("author", String.valueOf(realmResults.get(i).getAuthor()));
            MAP.put("dt", String.valueOf(realmResults.get(i).getDt()));
            MAP.put("session", String.valueOf(realmResults.get(i).getSession()));
            MAP.put("obj_date", String.valueOf(realmResults.get(i).getObj_date()));

            LOG.add(i, MAP);
        }


        return LOG;

    }

/*    public static ArrayList<LogDB> getLogToSend3(){
        return INSTANCE.where(LogDB.class)
                .isNull("dt")
                .findAll();
    }*/

    // LOG:----------------------------END-----------------------------------------------


    // CUSTOMER:----------------------------START-----------------------------------------------

    public static boolean setRowToCustomer(List<CustomerDB> list) {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(list);
        INSTANCE.commitTransaction();
        return true;
    }

    public static String getCustomerNm(String id) {
        CustomerDB realmResults = INSTANCE.where(CustomerDB.class)
                .equalTo("id", id)
                .findFirst();
        return realmResults.getNm();
    }
    // CUSTOMER:----------------------------END-----------------------------------------------


    // ADDRESS:----------------------------START-----------------------------------------------
    public static boolean setRowToAddress(List<AddressDB> list) {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(list);
        INSTANCE.commitTransaction();
        return true;
    }

    public static String getAddressNm(Integer id) {
        AddressDB realmResults = INSTANCE.where(AddressDB.class)
                .equalTo("addrId", id)
                .findFirst();
        return realmResults.getNm();
    }
    // ADDRESS:----------------------------END-----------------------------------------------


    // USERS:----------------------------START-----------------------------------------------
    public static boolean setRowToUsers(List<UsersDB> list) {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(list);
        INSTANCE.commitTransaction();
        return true;
    }

    public static String getUsersNm(Integer id) {
        UsersDB realmResults = INSTANCE.where(UsersDB.class)
                .equalTo("id", id)
                .findFirst();
        Log.e("PHOTO_REPORT", "userNmText(0): " + realmResults.getNm());
        return realmResults.getNm();
    }
    // USERS:----------------------------END-----------------------------------------------


    // REPORT_PREPARE:----------------------------START---------------------------------------------

    // Получение по ДАД_2 с плана работ списка id товаров
    /*
    * Получение ВСЕХ ReportPrepareDB по ДАД2 данного документа, создание списка ID-шников Товаров
    * полученных из этого ReportPrepareDB. Получение по списку ID-шников Товаров ТЗН TovarDB.
    * Добавление в ТЗН TovarDB TradeMarkDB по id-шникам из ТЗН TovarDB. Сортировка полученного
    * компота по sort("manufacturer.nm", Sort.ASCENDING, "sortcol", Sort.ASCENDING); (ПО ВОЗРАСТАНИЮ)
    * */
    public static RealmResults<TovarDB> getTovarListFromReportPrepareByDad2(long dad2) {
        Log.e("TovarListFromRPByDad2", "Start: " + dad2);

        ArrayList<String> listRpTovId = new ArrayList<>();
        ArrayList<String> listTovId = new ArrayList<>();

        RealmResults<ReportPrepareDB> realmResults = INSTANCE.where(ReportPrepareDB.class)
                .equalTo("codeDad2", String.valueOf(dad2))
                .findAll();

        Log.e("TovarListFromRPByDad2", "realmResults: " + realmResults);

        String[] list = new String[realmResults.size()];
        for (int i = 0; i < realmResults.size(); i++) {
            list[i] = realmResults.get(i).getTovarId();
            listRpTovId.add(realmResults.get(i).getTovarId());
        }

        Log.e("TovarListFromRPByDad2", "list: " + list);

        RealmResults<TovarDB> realmResults2 = INSTANCE.where(TovarDB.class)
                .in("iD", list)
                .findAll();


        try {
            for (TovarDB item : realmResults2){
                listTovId.add(item.getiD());
            }

            ArrayList<String> tovarsToDownload = neededTovars(listRpTovId, listTovId);
            if (tovarsToDownload.size() > 0){
                TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading();
                tablesLoadingUnloading.downloadTovarTable(null, tovarsToDownload);
            }
        }catch (Exception e){
            Log.e("TovarListFromRPByDad2", "ERR: " + e);
            return null;
        }


        Log.e("TovarListFromRPByDad2", "3");



        for (TovarDB item : realmResults2){
            if (item.getManufacturer() == null){
                String id = item.getManufacturerId();
                TradeMarkDB tm = TradeMarkRealm.getTradeMarkRowById(id);
                String data = item.getSortcol().toLowerCase();

                INSTANCE.executeTransaction(realm -> {
                    item.setSortcol(data);
                    item.setManufacturer(tm);
                    INSTANCE.copyToRealmOrUpdate(item);
                });
            }
        }

        Log.e("TovarListFromRPByDad2", "4: " + realmResults2.size());

        realmResults2 = realmResults2.sort("manufacturer.nm", Sort.ASCENDING, "sortcol", Sort.ASCENDING);

        Log.e("TovarListFromRPByDad2", "6: " + realmResults2.size());


        return realmResults2;
    }


    private static ArrayList<String> neededTovars(ArrayList<String> listRpTovId, ArrayList<String> listTovId){
        ArrayList<String> res = new ArrayList<>(listRpTovId);
        res.removeAll(listTovId);
        return res;
    }


    public static  RealmResults<ReportPrepareDB> getRPBYDAD2(){
        return INSTANCE.where(ReportPrepareDB.class)
                .equalTo("codeDad2", "1220421036217049444")
                .findAll();
    }


    // Получение по ДАД_2 с плана работ списка id товаров

    /**
     * 30.03.2021
     * Такая же функция как выше, но отбирает вторую половину данных
     */
    public static RealmResults<TovarDB> getTovarListFromReportPrepareByDad2Not(long dad2) {
        RealmResults<ReportPrepareDB> realmResults = INSTANCE.where(ReportPrepareDB.class)
                .notEqualTo("codeDad2", String.valueOf(dad2))
                .findAll();

        String[] list = new String[realmResults.size()];
        for (int i = 0; i < realmResults.size(); i++) {
            list[i] = realmResults.get(i).getTovarId();
        }

        RealmResults<TovarDB> realmResults2 = INSTANCE.where(TovarDB.class)
                .in("iD", list)
                .sort("manufacturerId", Sort.ASCENDING, "weight", Sort.DESCENDING)
                .findAll();

        return realmResults2;
    }

    /**
     * 22.01.2021
     */
    public static RealmResults<TovarDB> getTovarListByCustomer(String id) {
        RealmResults<TovarDB> res = INSTANCE.where(TovarDB.class)
                .equalTo("clientId", id)
                .or()
                .equalTo("clientId2", id)
                .sort("manufacturerId", Sort.ASCENDING, "weight", Sort.DESCENDING)
                .findAll();

        res = res.sort("sortcol", Sort.ASCENDING);
        return res;
    }


    // Получение листа Опций по данному товару
    public static List<OptionsDB> getTovarOptionInReportPrepare(String dad2, String tovarId) {
        List<OptionsDB> optionsDBS = INSTANCE.where(OptionsDB.class)
                .equalTo("codeDad2", dad2)
                .sort("so")
                .findAll();
        Log.e("getTovarOption", "optionsDBS: " + optionsDBS.size());
        return optionsDBS;
    }

    // Получение строки с ReportPrepare для записи туда данных
    public static ReportPrepareDB getTovarReportPrepare(String dad2, String tovarId) {
        return INSTANCE.where(ReportPrepareDB.class)
                .equalTo("tovarId", tovarId)
                .and()
                .equalTo("codeDad2", dad2)
                .findFirst();
    }


    // Получение строки из ReportPrepare по ID
    public static ReportPrepareDB getReportPrepareRowById(String idS) {
        Integer id = Integer.valueOf(idS); // Костыль ибо напортачено с id шками на стороне сервера и тут
        ReportPrepareDB rp = INSTANCE.where(ReportPrepareDB.class)
                .equalTo("iD", id)
                .findFirst();

        return RealmManager.INSTANCE.copyFromRealm(rp);
    }


    public static void setReportPrepareRow(ReportPrepareDB reportPrepare) {
        INSTANCE.copyToRealmOrUpdate(reportPrepare);
    }


    public static ArrayList<ReportPrepareServ> getReportPrepareToUpload() {

        List<ReportPrepareDB> rp = INSTANCE.where(ReportPrepareDB.class)
                .equalTo("uploadStatus", 1)
                .findAll();

        ArrayList<ReportPrepareServ> reportPrepareServ = new ArrayList<>();

        if (rp != null) {
            for (int i = 0; i < rp.size(); i++) {
                Log.e("REPORT_PREPARE_SEND", "DATABASE + ");
                reportPrepareServ.add(new ReportPrepareServ(
                        String.valueOf(rp.get(i).getID()),
                        rp.get(i).getDtChange(),
                        rp.get(i).getDtReport(),
                        rp.get(i).getKli(),
                        rp.get(i).getTovarId(),
                        rp.get(i).getAddrId(),
                        rp.get(i).getPrice(),
                        rp.get(i).getFace(),
                        rp.get(i).getAmount(),
                        rp.get(i).getDtExpire(),
                        rp.get(i).getExpireLeft(),
                        rp.get(i).getNotes(),
                        rp.get(i).getUp(),
                        rp.get(i).getAkciya(),
                        rp.get(i).getAkciyaId(),
                        rp.get(i).getOborotvedNum(),
                        rp.get(i).getErrorId(),
                        rp.get(i).getErrorComment(),
                        rp.get(i).getCodeDad2(),
                        String.valueOf(rp.get(i).buyerOrderId)));
            }
        } else {
            Log.e("REPORT_PREPARE_SEND", "DATABASE - ");
        }


        Log.e("REPORT_PREPARE_SEND", "SIZE UPLOAD: " + reportPrepareServ.size());
        return reportPrepareServ;
    }


    public static long reportPrepareGetLastId() {
        RealmResults<ReportPrepareDB> realmResults = INSTANCE.where(ReportPrepareDB.class)
                .findAll();
        try {
            return Objects.requireNonNull(realmResults.last()).getID();
        } catch (Exception e) {
            return 0;
        }
    }


    // REPORT_PREPARE:----------------------------END-----------------------------------------------


    // TOVAR:----------------------------START---------------------------------------------

    // TOVAR:----------------------------END-----------------------------------------------


    // TRADE_MARK_DB

    /**
     * 15.09
     * Получение данных Manufacturer NM по ID
     */
    public static TradeMarkDB getNmById(String id) {
        return INSTANCE.where(TradeMarkDB.class)
                .equalTo("iD", id)
                .findFirst();
    }


    // ERROR DB
    public static RealmResults<ErrorDB> getAllErrorDb() {
        return INSTANCE.where(ErrorDB.class)
                .findAll();
    }

    // PROMO DB
    public static RealmResults<PromoDB> getAllPromoDb() {
        return INSTANCE.where(PromoDB.class)
                .findAll();
    }


    /**
     * 30.09.2020
     * <p>
     * РАБОТА С LOG_MP_DB
     * Получаем последнюю запись
     */
    public static int logMPGetLastId() {
        RealmResults<LogMPDB> realmResults = INSTANCE.where(LogMPDB.class)
                .findAll();
        try {
            return Objects.requireNonNull(realmResults.last()).getId();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 30.09.2020
     * Запись строки в БД
     */
    public static void setLogMpRow(LogMPDB row) {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(row);
        INSTANCE.commitTransaction();
    }

    /**
     * 30.09.2020
     * Получение всей таблици для выгруки на сервер
     */
    public static RealmResults<LogMPDB> getAllLogMPDB() {
        return INSTANCE.where(LogMPDB.class)
                .findAll();
    }

    public static RealmResults<MenuItemFromWebDB> getSiteMenu() {
        return INSTANCE.where(MenuItemFromWebDB.class)
                .findAll();
    }


    //    public RealmQuery<MenuItemFromWebDB> in(String fieldName, Integer[] values);
    public static RealmResults<MenuItemFromWebDB> getSiteMenuItems(Integer[] ids) {
        return INSTANCE.where(MenuItemFromWebDB.class)
                .in("id", ids)
                .findAll();
    }


    public static SiteObjectsDB getLesson(int id) {
        return INSTANCE.where(SiteObjectsDB.class)
                .equalTo("id", id)
                .findFirst();
    }

    public static SiteHintsDB getVideoLesson(int id) {
        return INSTANCE.where(SiteHintsDB.class)
                .equalTo("id", id)
                .findFirst();
    }


    public static LangListDB getLangList(String type) {
        return INSTANCE.where(LangListDB.class)
                .equalTo("nmShort", type)
                .findFirst();
    }

    public static List<LangListDB> getAllLangList() {
        return INSTANCE.where(LangListDB.class)
                .findAll();
    }

//    public static List<SiteTranslationsList> getAllSiteTranslationsList() {
//        return INSTANCE.where(SiteTranslationsList.class)
//                .findAll();
//    }

    public static List<SiteTranslationsList> getSiteTranslationsList(String id) {
        return INSTANCE.where(SiteTranslationsList.class)
                .equalTo("langId", id)
                .findAll();
    }

}
