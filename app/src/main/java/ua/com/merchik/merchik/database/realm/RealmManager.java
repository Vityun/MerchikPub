package ua.com.merchik.merchik.database.realm;

import static ua.com.merchik.merchik.Globals.APP_PREFERENCES;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import ua.com.merchik.merchik.Clock;
import ua.com.merchik.merchik.Globals;
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

public class RealmManager {

    public static Realm INSTANCE;
    private static Globals globals = new Globals();


    private static SharedPreferences sharedPreferences;

    public static void init(Context context) {
        Realm.init(context);

        RealmConfiguration config = new RealmConfiguration.Builder().name("myrealm.realm")
                .deleteRealmIfMigrationNeeded()
//                .schemaVersion(21)
                .allowWritesOnUiThread(true)
                .allowQueriesOnUiThread(true)
                .migration(new MyMigration()).build();
        Realm.setDefaultConfiguration(config);
        INSTANCE = Realm.getInstance(Realm.getDefaultConfiguration());


        sharedPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("realm", false)) {
            List<SynchronizationTimetableDB> synchronizationTimetableDBList = RealmManager.getSynchronizationTimetable();
            if (synchronizationTimetableDBList == null) {
                addSynchronizationTimetable();
            }
        } else {
            sharedPreferences.edit().putBoolean("realm", true).apply();
            addSynchronizationTimetable();
        }

    }

    public static void addSynchronizationTimetable() {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(1, "wp_data", 600, 0, 0, 0, 0, "План робіт", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(2, "image_tp", 36000, 0, 0, 0, 0, "Типи фото", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(3, "client_group_tp", 36000, 0, 0, 0, 0, "Групи товарів", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(4, "log_mp", 600, 0, 0, 0, 0, "Лог місцеположення", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(5, "clients", 36000, 0, 0, 0, 0, "Кліенти", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(6, "address", 36000, 0, 0, 0, 0, "Адреси", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(7, "users", 36000, 0, 0, 0, 0, "Користувачі", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(8, "promoList", 3600000, 0, 0, 0, 0, "Акії", 0));     // Акции
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(9, "errorsList", 3600000, 0, 0, 0, 0, "Помилки", 0));     // Ошибки
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(10, "stack_photo", 36000, 0, 0, 0, 0, "Журнал фото", 0));     // стэк фото
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(11, "task_and_reclamations", 600, 0, 0, 0, 0, "Задачі та рекламації", 0));     // ЗИР
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(12, "planogram", 36000, 0, 0, 0, 0, "Планограми", 0));     // Планограммы
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(13, "address_sql", 36000, 0, 0, 0, 0, "Адреси", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(14, "clients_sql", 36000, 0, 0, 0, 0, "Клієнти", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(15, "users_sql", 36000, 0, 0, 0, 0, "Користувачі", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(16, "city_sql", 36000, 0, 0, 0, 0, "Міста", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(17, "oblast_sql", 36000, 0, 0, 0, 0, "Області", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(18, "sample_photo", 604800, 0, 0, 0, 0, "Зразки фото", 0));    // Образцы фото    604800с = 7 дней
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(19, "ekl_sql", 36000, 0, 0, 0, 0, "ЕКЛ", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(20, "location", 86400, 0, 0, 0, 0, "Місцеположення", 0));    // 86400 - 1 day

        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(21, "photo_tovar", 86400, 0, 0, 0, 0, "Фото товара", 1));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(22, "photo_sample", 86400, 0, 0, 0, 0, "Ідентифікатори фото", 1));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(23, "photo_planogram", 86400, 0, 0, 0, 0, "Фото планограм", 1));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(24, "photo_showcase", 86400, 0, 0, 0, 0, "Фото зразків", 1));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(25, "coments_to_photo", 86400, 0, 0, 0, 0, "Коментарі до фото", 1));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(26, "photo_user_from_serv", 86400, 0, 0, 0, 0, "Завантажити мої старі фото", 1));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(27, "upload_ekl", 86400, 0, 0, 0, 0, "Вивантажити ЄКЛ", 1));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(28, "photo_tar", 86400, 0, 0, 0, 0, "Фото Задач та Рекламацій", 1));

        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(29, "dossier_sotr", 86400, 0, 0, 0, 0, "Досье сотрудника", 1));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(30, "vacancy", 86400, 0, 0, 0, 0, "Вакансии", 1));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(31, "bonus", 86400, 0, 0, 0, 0, "Бонусы", 1));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(32, "site_url", 86400, 0, 0, 0, 0, "Сайты", 1));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(33, "site_account", 86400, 0, 0, 0, 0, "Аккаунты", 1));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(34, "sms_log", 86400, 0, 0, 0, 0, "SMS звіти", 0));     // smsLogExchange
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(35, "achievements", 600, 0, 0, 0, 0, "Досягнення", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(36, "sms_log", 86400, 0, 0, 0, 0, "SMS звіти", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(37, "standart_table", 86400, 0, 0, 0, 0, "Tаблиці стандартів", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(38, "content_table", 86400, 0, 0, 0, 0, "Tаблиці контент", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(39, "report_prepare", 86400, 0, 0, 0, 0, "Tаблиці report_prepare", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(40, "vizit_showcase_list", 86400, 0, 0, 0, 0, "Tаблиці vizit_showcase_list", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(41, "images_vote", 86400, 0, 0, 0, 0, "Tаблиці images_vote", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(42, "theme_list", 86400, 0, 0, 0, 0, "Tаблиці theme_list", 0));

        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(43, "options_list", 86400, 0, 0, 0, 0, "Tаблиці options_list", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(44, "additional_requirements", 86400, 0, 0, 0, 0, "Tаблиці AdditionalRequirementsDB", 0));
        INSTANCE.copyToRealmOrUpdate(new SynchronizationTimetableDB(45, "tovar_list", 86400, 0, 0, 0, 0, "Tаблиці TovarDB", 0));

        INSTANCE.commitTransaction();
    }


    /**
     * 04.06.2025 обновленная запись Плана работ
     */

    public static void updateWorkPlanFromServer(List<WpDataDB> serverData) {
        // Получаем текущие данные из Realm

        Globals.writeToMLOG("INFO", "updateWorkPlanFromServer.start", "List<WpDataDB>.size: " + serverData.size());
        RealmResults<WpDataDB> realmResults = INSTANCE.where(WpDataDB.class).findAll();
        List<WpDataDB> localData = INSTANCE.copyFromRealm(realmResults); // Конвертируем в List<WpDataDB>
        Globals.writeToMLOG("INFO", "updateWorkPlanFromServer.localData", "List<WpDataDB>.size: " + localData.size());

        // Создаем мапу существующих данных для быстрого поиска по code_dad2
        Map<Long, WpDataDB> localDataMap = new HashMap<>();
        for (WpDataDB item : localData) {
            localDataMap.put(item.getCode_dad2(), item);
        }

        // Создаем список данных для обновления/добавления
        List<WpDataDB> dataToUpdate = new ArrayList<>();

        for (WpDataDB serverItem : serverData) {
            long codeDad2 = serverItem.getCode_dad2();

            Globals.writeToMLOG("INFO", "updateWorkPlanFromServer.localData", "serverItem codeDad2: " + codeDad2);
            // Проверяем, есть ли такая запись в локальной базе
            WpDataDB localItem = localDataMap.get(codeDad2);

            if (localItem != null) {
                // Проверяем, начал ли пользователь работы по этой записи
                boolean workStarted = (localItem.getVisit_start_dt() > 0 &&
                        localItem.getClient_start_dt() > 0) ||
                        (localItem.getVisit_end_dt() > 0 &&
                                localItem.getClient_end_dt() > 0);
                Globals.writeToMLOG("INFO", "updateWorkPlanFromServer", "SKIPPED -> WpDataDB dad2: " + serverItem.getCode_dad2());
                boolean isStatus = serverItem.getStatus() == 0;
                Globals.writeToMLOG("INFO", "updateWorkPlanFromServer.localData", "local work: " + workStarted + "serverItem isStatus: " + isStatus);
                if (workStarted && isStatus) {
                    // Пропускаем записи, по которым уже начаты работы
                    continue;
                }

                // Копируем ID существующей записи, чтобы обновить ее, а не создать новую
                Globals.writeToMLOG("INFO", "updateWorkPlanFromServer", "WpDataDB dad2: " + serverItem.getCode_dad2() +
                        " | id local: " + localItem.getId() + " | id server: " + serverItem.getId());
                serverItem.setId(localItem.getId());
            }


            dataToUpdate.add(serverItem);
        }
        Globals.writeToMLOG("INFO", "updateWorkPlanFromServer.final", "List<WpDataDB>.size: " + dataToUpdate.size());

        // Сохраняем данные в Realm в транзакции
        INSTANCE.executeTransaction(r -> {
            // Вставляем или обновляем данные
            r.insertOrUpdate(dataToUpdate);
        });
    }

    /**
     * Запись в Реалм Плана работ
     *
     * @param wpData список данных с сервера
     */
    public static void setWpData(List<WpDataDB> wpData) {
        Globals.writeToMLOG("INFO", "RealmManager.setWpData", "wpDataDBList.size(): " + wpData.size());
        INSTANCE.beginTransaction();
        INSTANCE.delete(WpDataDB.class);
        INSTANCE.copyToRealmOrUpdate(wpData);
        INSTANCE.commitTransaction();
        Log.e("REALM_DB_UPDATE", "WP_DATA_END");
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

    public static ArrayList<WpDataDB> setWpDataAutoNew(List<WpDataDB> serverData) {
        ArrayList<WpDataDB> sendOnServer = new ArrayList<>();

        RealmResults<WpDataDB> wpDataDBList1 = INSTANCE.where(WpDataDB.class) // Получаем всю БД (на данный момент она должна быть обновлена с сервера)
                .findAll();

        Log.e("WP_DATA_UPDATE", "Количество данных что пришло с сервера: " + serverData.size());
        Log.e("WP_DATA_UPDATE", "Количество данных в приложении ДО Ц1: " + wpDataDBList1.size());

        Globals.writeToMLOG("INFO", "setWpDataAuto", "Количество данных что пришло с сервера: " + serverData.size());
        Globals.writeToMLOG("INFO", "setWpDataAuto", "Количество данных в приложении ДО Ц1: " + wpDataDBList1.size());

        // 1 цикл. Прогоняем данные которые пришли с сервераи надо обновить или добавить по ВПИ
        // По скольку с ID не одноначная ситуация (это не надёжный параметр который может внезапно меняться)
        // мне стоит для однозначного сравнения использовать 4 поля: 'code_dad2', 'user_id', 'client_id', 'isp'
        INSTANCE.beginTransaction();
        for (WpDataDB wp : serverData) {
            WpDataDB row = INSTANCE.where(WpDataDB.class).equalTo("code_dad2", wp.getCode_dad2())
                    .equalTo("user_id", wp.getUser_id())
                    .equalTo("client_id", wp.getClient_id())
                    .equalTo("isp", wp.getIsp())
                    .findFirst();

            Globals.writeToMLOG("INFO", "setWpDataAuto", "План работ с СЕРВЕРА: " + new Gson().toJson(wp));

            if (row != null) {   // Если запись в бд есть
                WpDataDB debug = INSTANCE.copyFromRealm(row);
                Globals.writeToMLOG("INFO", "setWpDataAuto", "План работ с ПРИЛОЖЕНИЯ: " + new Gson().toJson(debug));

                // Это делаем для Лога, возможно нужно будет имплементировать в работу
                if ((wp.getVisit_start_dt() == 0 && row.getVisit_start_dt() > 0) || (wp.getVisit_end_dt() == 0 && row.getVisit_end_dt() > 0)) {
                    Globals.writeToMLOG("INFO", "setWpDataAuto", "Ситуация для отладки");
                    Globals.writeToMLOG("INFO", "setWpDataAuto", "План работ с СЕРВЕРА: " + new Gson().toJson(wp));
                    Globals.writeToMLOG("INFO", "setWpDataAuto", "План работ с ПРИЛОЖЕНИЯ: " + new Gson().toJson(debug));
                }

                if (wp.getDt_update() >= row.getDt_update()) {    // Если на сервере данные более новые - обновляю(перезаписываю)
                    Log.e("setWpDataAuto", "Данные с сервера с большим VPI");
                    Log.e("WP_DATA_UPDATE", "MUST UPDATE (" + wp.getDt_update() + "/" + row.getDt_update() + ")" + wp.getCode_dad2());

                    Globals.writeToMLOG("INFO", "setWpDataAuto", "Данные с сервера с большим VPI");
                    Globals.writeToMLOG("INFO", "setWpDataAuto", "MUST UPDATE (" + wp.getDt_update() + "/" + row.getDt_update() + ")");

                    if (wp.getVisit_start_dt() == 0 && wp.getClient_start_dt() == 0 && row.getVisit_start_dt() > 0 && row.getClient_start_dt() > 0) {
                        wp.setVisit_start_dt(row.getVisit_start_dt());
                        wp.setClient_start_dt(row.getClient_start_dt());
                    } else if (wp.getVisit_end_dt() == 0 && wp.getClient_end_dt() == 0 && row.getVisit_end_dt() > 0 && row.getClient_end_dt() > 0) {
                        wp.setVisit_end_dt(row.getVisit_end_dt());
                        wp.setClient_end_dt(row.getClient_end_dt());
                    }

                    INSTANCE.copyToRealmOrUpdate(wp);
                } else {
                    // ТУТ ДЕЛАЮ ВЫГРУЗКУ НОВЫХ ДАННЫХ. ИЛИ СОБИРАЮ ДАННЫЕ ДЛЯ ТОГО ЧТО Б ПОТОМ ВЫГРУЗИТЬ.
                    Log.e("setWpDataAuto", "Эти данные на моей стороне более новые. Надо выгружать");

                    Globals.writeToMLOG("INFO", "setWpDataAuto", "Данные у пользователя новее. (" + wp.getDt_update() + "/" + row.getDt_update() + ")" + wp.getCode_dad2());

                    sendOnServer.add(row);
                    Log.e("WP_DATA_UPDATE", "Данные у пользователя новее. (" + wp.getDt_update() + "/" + row.getDt_update() + ")"); // Не нужно ли тут начать выгрузку этих самых данных?
                }
            } else {
                Log.e("WP_DATA_UPDATE", "Новые данные. Запись в БД.");
                // Если записи в БД нет - просто записываем её туда.
                Log.e("setWpDataAuto", "Такой записи в БД не было. Записываю к себе" + wp.getCode_dad2());
                Globals.writeToMLOG("INFO", "setWpDataAuto", "Такой записи в БД не было. Записываю к себе" + wp.getCode_dad2());

                INSTANCE.copyToRealmOrUpdate(wp);
            }
        }
        INSTANCE.commitTransaction();

        // 2 цикл. Прогоняем данные которые надо удалить.
        RealmResults<WpDataDB> localData = INSTANCE.where(WpDataDB.class) // Получаем всю БД (на данный момент она должна быть обновлена с сервера)
                .findAll();


        INSTANCE.beginTransaction();
        for (WpDataDB local : localData) {
            if (!serverData.contains(local)) {
                local.deleteFromRealm();
            }
        }
        INSTANCE.commitTransaction();

        return sendOnServer;
    }

    public static ArrayList<WpDataDB> setWpDataAuto2(List<WpDataDB> serverData) {
        ArrayList<WpDataDB> sendOnServer = new ArrayList<>();

        // Логирование входящих данных
        Log.e("WP_DATA_UPDATE", "С сервера получено: " + serverData.size());
        Globals.writeToMLOG("INFO", "setWpDataAuto", "С сервера получено: " + serverData.size());

        // Получаем все локальные данные и строим map для быстрого поиска
        RealmResults<WpDataDB> localData = INSTANCE.where(WpDataDB.class).findAll();
        Log.e("WP_DATA_UPDATE", "Локальных данных до обновления: " + localData.size());
        Globals.writeToMLOG("INFO", "setWpDataAuto", "Локальных данных до обновления: " + localData.size());

        Map<String, WpDataDB> localMap = new HashMap<>();
        for (WpDataDB local : localData) {
            String key = generateKey(local);
            localMap.put(key, local);
        }

        INSTANCE.beginTransaction();
        for (WpDataDB wp : serverData) {
            String key = generateKey(wp);
            WpDataDB local = localMap.get(key);

            Globals.writeToMLOG("INFO", "setWpDataAuto", "Серверная запись: " + new Gson().toJson(wp));

            if (local != null) {
                WpDataDB debug = INSTANCE.copyFromRealm(local);
                Globals.writeToMLOG("INFO", "setWpDataAuto", "Локальная запись: " + new Gson().toJson(debug));

                // Лог-граничные ситуации
                if ((wp.getVisit_start_dt() == 0 && local.getVisit_start_dt() > 0) ||
                        (wp.getVisit_end_dt() == 0 && local.getVisit_end_dt() > 0)) {
                    Globals.writeToMLOG("INFO", "setWpDataAuto", "Случай для отладки: Сервер и Локальная запись различны по visit");
                    Globals.writeToMLOG("INFO", "setWpDataAuto", "Серверная: " + new Gson().toJson(wp));
                    Globals.writeToMLOG("INFO", "setWpDataAuto", "Локальная: " + new Gson().toJson(debug));
                }

                if (wp.getDt_update() >= local.getDt_update()) {
                    Log.e("setWpDataAuto", "Обновляю локальную запись новыми данными");
                    Globals.writeToMLOG("INFO", "setWpDataAuto", "Обновляю локальную запись (" + wp.getDt_update() + " >= " + local.getDt_update() + ")");

                    // Копируем локальные временные значения, если серверные не заданы
                    // 13.05.25 Петров предложил если работы начаты или окончены, оставляем те данные что в приложении
                    if (wp.getVisit_start_dt() == 0 && wp.getClient_start_dt() == 0 &&
                            local.getVisit_start_dt() > 0 && local.getClient_start_dt() > 0) {
                        wp = local;
                    } else if (wp.getVisit_end_dt() == 0 && wp.getClient_end_dt() == 0 &&
                            local.getVisit_end_dt() > 0 && local.getClient_end_dt() > 0) {
                        wp = local;
                    }

                    INSTANCE.copyToRealmOrUpdate(wp);
                } else {
                    Log.e("setWpDataAuto", "Локальная запись новее, добавляю в sendOnServer");
                    Globals.writeToMLOG("INFO", "setWpDataAuto", "Локальная запись новее (" + wp.getDt_update() + " < " + local.getDt_update() + ")");
                    sendOnServer.add(local);
                }
            } else {
                Log.e("setWpDataAuto", "Новая запись, добавляю в БД: " + wp.getCode_dad2());
                Globals.writeToMLOG("INFO", "setWpDataAuto", "Новая запись, добавляю в БД: " + wp.getCode_dad2());
                INSTANCE.copyToRealmOrUpdate(wp);
            }
        }
        INSTANCE.commitTransaction();

        // Удаление локальных записей, которых нет на сервере
        Set<String> serverKeys = serverData.stream()
                .map(RealmManager::generateKey)
                .collect(Collectors.toSet());

        INSTANCE.beginTransaction();
        for (WpDataDB local : localData) {
            String key = generateKey(local);
            if (!serverKeys.contains(key)) {
                Globals.writeToMLOG("INFO", "setWpDataAuto", "Удаляю локальную запись: " + local.getCode_dad2());
                local.deleteFromRealm();
            }
        }
        INSTANCE.commitTransaction();

        return sendOnServer;
    }

    private static String generateKey(WpDataDB data) {
        return data.getCode_dad2() + "_";
    }

    public static ArrayList<WpDataDB> setWpDataAuto(List<WpDataDB> serverData) {
        ArrayList<WpDataDB> sendOnServer = new ArrayList<>();

        RealmResults<WpDataDB> wpDataDBList1 = INSTANCE.where(WpDataDB.class) // Получаем всю БД (на данный момент она должна быть обновлена с сервера)
                .findAll();

        Log.e("WP_DATA_UPDATE", "Количество данных что пришло с сервера: " + serverData.size());
        Log.e("WP_DATA_UPDATE", "Количество данных в приложении ДО Ц1: " + wpDataDBList1.size());

        Globals.writeToMLOG("INFO", "setWpDataAuto", "Количество данных что пришло с сервера: " + serverData.size());
        Globals.writeToMLOG("INFO", "setWpDataAuto", "Количество данных в приложении ДО Ц1: " + wpDataDBList1.size());

        // 1 цикл. Прогоняем данные которые пришли с сервераи надо обновить или добавить по ВПИ
        // По скольку с ID не одноначная ситуация (это не надёжный параметр который может внезапно меняться)
        // мне стоит для однозначного сравнения использовать 4 поля: 'code_dad2', 'user_id', 'client_id', 'isp'
        INSTANCE.beginTransaction();
        for (WpDataDB wp : serverData) {
            WpDataDB row = INSTANCE.where(WpDataDB.class).equalTo("code_dad2", wp.getCode_dad2())
                    .equalTo("user_id", wp.getUser_id())
                    .equalTo("client_id", wp.getClient_id())
                    .equalTo("isp", wp.getIsp())
                    .findFirst();

            Globals.writeToMLOG("INFO", "setWpDataAuto", "План работ с СЕРВЕРА: " + new Gson().toJson(wp));

            if (row != null) {   // Если запись в бд есть
                WpDataDB debug = INSTANCE.copyFromRealm(row);
                Globals.writeToMLOG("INFO", "setWpDataAuto", "План работ с ПРИЛОЖЕНИЯ: " + new Gson().toJson(debug));

                // Это делаем для Лога, возможно нужно будет имплементировать в работу
                if ((wp.getVisit_start_dt() == 0 && row.getVisit_start_dt() > 0) || (wp.getVisit_end_dt() == 0 && row.getVisit_end_dt() > 0)) {
                    Globals.writeToMLOG("INFO", "setWpDataAuto", "Ситуация для отладки");
                    Globals.writeToMLOG("INFO", "setWpDataAuto", "План работ с СЕРВЕРА: " + new Gson().toJson(wp));
                    Globals.writeToMLOG("INFO", "setWpDataAuto", "План работ с ПРИЛОЖЕНИЯ: " + new Gson().toJson(debug));
                }

                if (wp.getDt_update() >= row.getDt_update()) {    // Если на сервере данные более новые - обновляю(перезаписываю)
                    Log.e("setWpDataAuto", "Данные с сервера с большим VPI");
                    Log.e("WP_DATA_UPDATE", "MUST UPDATE (" + wp.getDt_update() + "/" + row.getDt_update() + ")" + wp.getCode_dad2());

                    Globals.writeToMLOG("INFO", "setWpDataAuto", "Данные с сервера с большим VPI");
                    Globals.writeToMLOG("INFO", "setWpDataAuto", "MUST UPDATE (" + wp.getDt_update() + "/" + row.getDt_update() + ")");

                    if (wp.getVisit_start_dt() == 0 && wp.getClient_start_dt() == 0 && row.getVisit_start_dt() > 0 && row.getClient_start_dt() > 0) {
                        wp.setVisit_start_dt(row.getVisit_start_dt());
                        wp.setClient_start_dt(row.getClient_start_dt());
                    } else if (wp.getVisit_end_dt() == 0 && wp.getClient_end_dt() == 0 && row.getVisit_end_dt() > 0 && row.getClient_end_dt() > 0) {
                        wp.setVisit_end_dt(row.getVisit_end_dt());
                        wp.setClient_end_dt(row.getClient_end_dt());
                    }

                    INSTANCE.copyToRealmOrUpdate(wp);
                } else {
                    // ТУТ ДЕЛАЮ ВЫГРУЗКУ НОВЫХ ДАННЫХ. ИЛИ СОБИРАЮ ДАННЫЕ ДЛЯ ТОГО ЧТО Б ПОТОМ ВЫГРУЗИТЬ.
                    Log.e("setWpDataAuto", "Эти данные на моей стороне более новые. Надо выгружать");

                    Globals.writeToMLOG("INFO", "setWpDataAuto", "Данные у пользователя новее. (" + wp.getDt_update() + "/" + row.getDt_update() + ")" + wp.getCode_dad2());

                    sendOnServer.add(row);
                    Log.e("WP_DATA_UPDATE", "Данные у пользователя новее. (" + wp.getDt_update() + "/" + row.getDt_update() + ")"); // Не нужно ли тут начать выгрузку этих самых данных?
                }
            } else {
                Log.e("WP_DATA_UPDATE", "Новые данные. Запись в БД.");
                // Если записи в БД нет - просто записываем её туда.
                Log.e("setWpDataAuto", "Такой записи в БД не было. Записываю к себе" + wp.getCode_dad2());
                Globals.writeToMLOG("INFO", "setWpDataAuto", "Такой записи в БД не было. Записываю к себе" + wp.getCode_dad2());

                INSTANCE.copyToRealmOrUpdate(wp);
            }
        }
        INSTANCE.commitTransaction();

        // 2 цикл. Прогоняем данные которые надо удалить.
        RealmResults<WpDataDB> localData = INSTANCE.where(WpDataDB.class) // Получаем всю БД (на данный момент она должна быть обновлена с сервера)
                .findAll();


        INSTANCE.beginTransaction();
        for (WpDataDB local : localData) {
            if (!serverData.contains(local)) {
                local.deleteFromRealm();
            }
        }
        INSTANCE.commitTransaction();

        return sendOnServer;
    }

    /**
     * Запись в Реалм Типов Фото
     */
    public static boolean setImagesTp(List<ImagesTypeListDB> ImageTp) {
        Log.e("REALM_DB_UPDATE", "TYPE_START");
        INSTANCE.beginTransaction();
        INSTANCE.delete(ImagesTypeListDB.class);
        INSTANCE.copyToRealmOrUpdate(ImageTp);
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
//        INSTANCE.delete(GroupTypeDB.class);
        INSTANCE.copyToRealmOrUpdate(groupTypeDB);
        INSTANCE.commitTransaction();
        Log.e("REALM_DB_UPDATE", "setGroupType_E");
    }

    public static boolean setGroupTypeV2(List<GroupTypeDB> customerGroupsListDB) {
        Log.e("REALM_DB_UPDATE", "setGroupType_S");
        INSTANCE.beginTransaction();
        INSTANCE.delete(GroupTypeDB.class);
        INSTANCE.copyToRealm(customerGroupsListDB);
        INSTANCE.commitTransaction();
        Log.e("REALM_DB_UPDATE", "setGroupType_E");
        return true;
    }

    /**
     * Запись в Реалм Опций
     */
    public static boolean setOptions2(List<OptionsDB> optionsDBS) {
        globals.writeToMLOG("_INFO.setOptions.sizeBefore: " + optionsDBS.size());

        // Удаление дубликатов по ключу
        Map<String, OptionsDB> map = new LinkedHashMap<>();
        for (OptionsDB item : optionsDBS) {
            if (item.getID() != null) {
                map.put(item.getID(), item); // последний с таким ключом "побеждает"
            }
        }
        List<OptionsDB> uniqueList = new ArrayList<>(map.values());
        globals.writeToMLOG("_INFO.setOptions.sizeAfter: " + uniqueList.size());

        INSTANCE.executeTransaction(realm -> {
            realm.insertOrUpdate(uniqueList); // безопасное сохранение без дубликатов
        });

        return true;
    }

    public static boolean setOptions(List<OptionsDB> optionsDBS) {
        globals.writeToMLOG("_INFO.RealmManager.class.setOptions.Размер списка: " + optionsDBS.size() + "\n");

        INSTANCE.beginTransaction();
        INSTANCE.delete(OptionsDB.class);
        List<OptionsDB> res = INSTANCE.copyToRealmOrUpdate(optionsDBS);
        globals.writeToMLOG("_INFO.RealmManager.class.setOptions.Размер сохранённого списка: " + res.size() + "\n");
        INSTANCE.commitTransaction();
        return true;
    }

    public static void saveDownloadedOptions(List<OptionsDB> optionsDBS) {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(optionsDBS);
        INSTANCE.commitTransaction();
    }

    /**
     * Запись в Реалм РепортПр
     */
    public static boolean setReportPrepare(List<ReportPrepareDB> reportPrepare) {
        Log.e("REALM_DB_UPDATE", "REPORT_S");
        INSTANCE.beginTransaction();
//        INSTANCE.delete(ReportPrepareDB.class);
        INSTANCE.copyToRealmOrUpdate(reportPrepare);
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
            globals.writeToMLOG("_INFO.RealmManager.class.setTovar.Размер списка: " + list.size() + "\n");
            Log.e("REALM_DB_UPDATE", "setTovar list.size(): " + list.size());
        } catch (Exception e) {
            globals.writeToMLOG("_INFO.RealmManager.class.setTovar.Ошибка1: " + e + "\n");
            Log.e("REALM_DB_UPDATE", "setTovar Ошибка1: " + e);
        }

        INSTANCE.beginTransaction();
        INSTANCE.delete(TovarDB.class);
        List<TovarDB> res = INSTANCE.copyToRealmOrUpdate(list);


        try {
            globals.writeToMLOG("_INFO.RealmManager.class.setTovar.Размер сохранённого списка: " + res.size() + "\n");
            Log.e("REALM_DB_UPDATE", "setTovar res.size(): " + res.size());
        } catch (Exception e) {
            globals.writeToMLOG("_INFO.RealmManager.class.setTovar.Ошибка2: " + e + "\n");
            Log.e("REALM_DB_UPDATE", "setTovar Ошибка2: " + e);
        }

        INSTANCE.commitTransaction();

        Log.e("REALM_DB_UPDATE", "setTovar_E");
        return true;
    }

    public static void setTovarAsync(List<TovarDB> list) {
        Log.e("REALM_DB_UPDATE", "setTovar_S");

        try {
            globals.writeToMLOG("_INFO.RealmManager.class.setTovar.Размер списка: " + list.size() + "\n");
            Log.e("REALM_DB_UPDATE", "setTovar list.size(): " + list.size());
        } catch (Exception e) {
            globals.writeToMLOG("_INFO.RealmManager.class.setTovar.Ошибка1: " + e + "\n");
            Log.e("REALM_DB_UPDATE", "setTovar Ошибка1: " + e);
        }

        // Асинхронная транзакция
        Realm.getInstanceAsync(Realm.getDefaultConfiguration(), new Realm.Callback() {
            @Override
            public void onSuccess(Realm realm) {
                realm.executeTransactionAsync(bgRealm -> {
                    // Сохраняем данные в фоновом потоке
                    bgRealm.copyToRealmOrUpdate(list);
                }, () -> {
                    // Успешное завершение транзакции
                    Log.e("REALM_DB_UPDATE", "setTovar_E: Данные успешно сохранены");
                    globals.writeToMLOG("_INFO.RealmManager.class.setTovar.Данные успешно сохранены\n");

                    // Закрываем Realm после завершения
                    realm.close();
                }, error -> {
                    // Обработка ошибки
                    Log.e("REALM_DB_UPDATE", "setTovar_E: Ошибка при сохранении данных", error);
                    globals.writeToMLOG("_INFO.RealmManager.class.setTovar.Ошибка при сохранении данных: " + error + "\n");

                    // Закрываем Realm в случае ошибки
                    realm.close();
                });
            }
        });
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
        INSTANCE.copyToRealmOrUpdate(list);
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
        INSTANCE.copyToRealmOrUpdate(list);
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
        INSTANCE.copyToRealmOrUpdate(list);
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
        INSTANCE.copyToRealmOrUpdate(list);
        INSTANCE.commitTransaction();
        return true;
    }


    //==============================================================================================
    //==============================================================================================
    //==============================================================================================


    // APP_USER_DB START ------------------------------------

    public static AppUsersDB getAppUser() {
        AppUsersDB appUsersDB = INSTANCE.where(AppUsersDB.class).findFirst();
        if (appUsersDB != null) appUsersDB = INSTANCE.copyFromRealm(appUsersDB);
        return appUsersDB;
    }

    public static AppUsersDB getAppUserById(String ids) {
        int id = Integer.parseInt(ids);
        AppUsersDB appUsersDB = INSTANCE.where(AppUsersDB.class).equalTo("userId", id).findFirst();
        if (appUsersDB != null) appUsersDB = INSTANCE.copyFromRealm(appUsersDB);
        return appUsersDB;
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


    public static RealmResults<WpDataDB> getAllWorkPlanForRNO() {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.where(WpDataDB.class)
                    .equalTo("user_id", 14041)
                    .sort(new String[]{"dt_start", "addr_id"},
                            new Sort[]{Sort.ASCENDING, Sort.ASCENDING})
                    .findAll(); // <- unmanaged
        }
    }

    public static RealmResults<WpDataDB> getAllWorkPlanWithOutRNO() {
        try (Realm realm = Realm.getDefaultInstance()) {
            return realm.where(WpDataDB.class)
                    .notEqualTo("user_id", 14041)
                    .sort(new String[]{"dt_start", "addr_id"},
                            new Sort[]{Sort.ASCENDING, Sort.ASCENDING})
                    .findAll(); // <- unmanaged
        }
    }

    public static List<WpDataDB> getAllWorkPlanWithOutRNO_LIST() {
        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<WpDataDB> res = realm.where(WpDataDB.class)
                    .notEqualTo("user_id", 14041)
                    .sort(new String[]{"dt_start", "addr_id"},
                            new Sort[]{Sort.ASCENDING, Sort.ASCENDING})
                    .findAll();
            return realm.copyFromRealm(res); // <- unmanaged
        }
    }

    public static RealmResults<WpDataDB> getAllWorkPlanMAP() {
        return INSTANCE.where(WpDataDB.class).sort("dt_start", Sort.ASCENDING).distinct("addr_id").findAll();
    }

    public static WpDataDB getWorkPlanRowById(long id) {
        //"SELECT * FROM wp_data WHERE id = " + wpId + ";"
        return INSTANCE.where(WpDataDB.class).equalTo("ID", id).findFirst();
    }

    public static WpDataDB getWorkPlanRowByCodeDad2(long codeDad2) {
        //"SELECT * FROM wp_data WHERE id = " + wpId + ";"
        return INSTANCE.where(WpDataDB.class).equalTo("code_dad2", codeDad2).findFirst();
    }

    public static int getWpDataDate(String dt) {
        Date date = Clock.stringDateConvertToDate(dt);
        return INSTANCE.where(WpDataDB.class).equalTo("dt", date).findAll().size();
    }

    /**
     * 29.12.2020
     * Попытка создать "универсальный" запрос к БД
     */
    public static int getWpData(int status, String dt) {    //TODO query CHANGE DATE

        Date date = Clock.stringDateConvertToDate(dt);

        return INSTANCE.where(WpDataDB.class).equalTo("status", status).equalTo("dt", date).findAll().size();
    }

    // CUST GRP TYPE:-------------------------------------------------------------------------------
    public static RealmResults<GroupTypeDB> getAllGroupTypeByCustomerId(String customer_id) {
//"SELECT * FROM client_group_tp WHERE client_id = '" + customer_id + "';"
        Log.e("TAG_TEST_GRP", "cli_id(0): " + customer_id);
        int cli = Integer.parseInt(customer_id);
        return INSTANCE.where(GroupTypeDB.class).equalTo("client_id", cli).findAll();
    }


    // OPTIONS:-------------------------------------------------------------------------------------
    public static RealmResults<OptionsDB> getOptionsNOButton(long otchetId) {
        String sOtchetId = String.valueOf(otchetId);
        return INSTANCE.where(OptionsDB.class).equalTo("docId", sOtchetId).and().notEqualTo("optionGroup", "3161").and().equalTo("deleted", "0").findAll();
    }

    public static RealmResults<OptionsDB> getOptionsButton(long otchetId) {
        String sOtchetId = String.valueOf(otchetId);
        return INSTANCE.where(OptionsDB.class)
                .beginGroup()
                .equalTo("docId", sOtchetId)
                .and()
                .equalTo("optionGroup", "3161")
                .and()
                .equalTo("deleted", "0")
                .endGroup()
                .or()
                .equalTo("docId", sOtchetId)
                .and()
                .equalTo("deleted", "0")
                .and()
                .equalTo("optionId", "2243")
                .findAll();
    }

    public static RealmResults<OptionsDB> getOptionsButtonRED(long otchetId) {
        String sOtchetId = String.valueOf(otchetId);
        return INSTANCE.where(OptionsDB.class)
                .equalTo("docId", sOtchetId)
                .and()
                .equalTo("optionGroup", "3161")
                .equalTo("isSignal", "1")
                .findAll();
    }

    public static List<OptionsDB> getOptionsButtonRED2(long otchetId) {
        String sOtchetId = String.valueOf(otchetId);
        RealmResults<OptionsDB> realmResults = INSTANCE.where(OptionsDB.class)
                .equalTo("docId", sOtchetId)
                .and()
                .equalTo("optionGroup", "3161")
                .equalTo("isSignal", "1")
                .findAll();

        if (realmResults != null && realmResults.size() > 0) {
            return INSTANCE.copyFromRealm(realmResults);
        } else {
            return null;
        }
    }

    public static OptionsDB getOptionById(String id) {
        return INSTANCE.where(OptionsDB.class).equalTo("iD", id).findFirst();
    }

    public static String getOptionNameByOptionId(String optionId) {
        OptionsDB optionsDB = INSTANCE.where(OptionsDB.class)
                .equalTo("optionId", optionId)
                .findFirst();
        if (optionsDB != null) {
            return optionsDB.getOptionTxt();
        } else return "";
    }

    public static RealmResults<OptionsDB> getOptionsByOtchetId(long otchetId, String codeDad2) {
        String sOtchetId = String.valueOf(otchetId);
        return INSTANCE.where(OptionsDB.class).equalTo("docId", sOtchetId).and().equalTo("codeDad2", codeDad2).findAll();
    }


    // IMAGES TP:-----------------------------------------------------------------------------------
    public static RealmResults<ImagesTypeListDB> getAllImagesTypeList() {
        //"SELECT * FROM images_tp;"
        return INSTANCE.where(ImagesTypeListDB.class).findAll();
    }

    // STACK PHOTO:---------------------------------------------------------------------------------
    public static void stackPhotoSavePhoto(StackPhotoDB stackPhotoDB) {
        INSTANCE.executeTransaction(realm -> {
                    try {
                        realm.copyToRealmOrUpdate(stackPhotoDB);
                    } catch (Exception e) {
                        Log.e("RealmError", "Ошибка сохранения: " + e.getMessage());
                    }
                }
//                realm.copyToRealmOrUpdate(stackPhotoDB)
        );
    }

    public static void stackPhotoSavePhoto(List<StackPhotoDB> stackPhotoDB) {
        INSTANCE.executeTransaction(realm -> realm.copyToRealmOrUpdate(stackPhotoDB));
    }

    public static long stackPhotoGetLastId(Realm realm) {
        Number maxId = realm.where(StackPhotoDB.class).max("id");
        return maxId != null ? maxId.longValue() : 0;
    }

    public static int stackPhotoGetLastIdAsync() {
        Realm realm = Realm.getDefaultInstance(); // Создаем локальный экземпляр Realm
        try {
            RealmResults<StackPhotoDB> realmResults = realm.where(StackPhotoDB.class).findAll();

            if (realmResults.isEmpty()) {
                return 0; // Возвращаем 0, если список пуст
            } else {
                StackPhotoDB stackPhotoDB = realmResults.last(); // Получаем последний объект
                return stackPhotoDB.getId(); // Возвращаем его ID
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "RealmManager/stackPhotoGetLastId", "Exception e: " + e);
            return 0;
        } finally {
            if (realm != null) {
                realm.close(); // Закрываем Realm после использования
            }
        }
    }

    public static int stackPhotoGetLastId() {
//        #######################
        try {

            StackPhotoDB lastItem = INSTANCE.where(StackPhotoDB.class)
                    .sort("id", Sort.DESCENDING)
                    .findFirst();

            int lastId = 0;

            if (lastItem != null) {
                StackPhotoDB unmanaged = INSTANCE.copyFromRealm(lastItem); // !!! копируем
                lastId = unmanaged.getId(); // теперь точно работает
            }

            return lastId;

//            RealmResults<StackPhotoDB> realmResults = INSTANCE.where(StackPhotoDB.class).findAll();

//            if (realmResults.isEmpty()) {
//                return 0; // Возвращаем 0, если список пуст
//            } else {
//                StackPhotoDB stackPhotoDB = INSTANCE.copyFromRealm(realmResults.last());
//                return stackPhotoDB.getId();
//            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "RealmManager/stackPhotoGetLastId", "Exception e: " + e);
            return 0;
        }


//        StackPhotoDB stackPhotoDB = INSTANCE.copyFromRealm(realmResults.last());
//        try {
//            return Objects.requireNonNull(stackPhotoDB).getId();
//        } catch (Exception e) {
//            return 0;
//        }
    }

    public static boolean chechPhotoExist(String photoUri) {
        RealmResults<StackPhotoDB> realmResults = INSTANCE.where(StackPhotoDB.class).equalTo("photo_num", photoUri).findAll();
        return realmResults.isEmpty();
    }

    //    "SELECT count(*) FROM stack_photo WHERE upload_to_server = '' AND get_on_server = '';"
    public static int stackPhotoNotUploadedPhotosCount() {
        long count = INSTANCE.where(StackPhotoDB.class).equalTo("upload_to_server", 0).equalTo("get_on_server", 0)
//                .notEqualTo("photo_type", 18)     // 08.01.24 Надо на сервер выгружать фото Товаров сделанные с ЗИР, да и вообще
                .isNotNull("photo_hash").isNotNull("client_id").isNotNull("addr_id").isNotNull("time_event").count();
        if (count > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE; // или кинь исключение, или логируй
        }
        return (int) count;
    }

    // Количество фото витрины
    // Зачем так криво? Надо будет отказаться от просто получения числа
    public static int stackPhotoShowcasePhotoCount(long codeDad2, int photoType) {
        long count =  INSTANCE.where(StackPhotoDB.class).equalTo("code_dad2", codeDad2).equalTo("photo_type", photoType).count();
//        return realmResults.size();
        if (count > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE; // или кинь исключение, или логируй
        }
        return (int) count;
    }

    public static int stackPhotoPhotoCount(long codeDad2) {
        long count =  INSTANCE.where(StackPhotoDB.class)
                .equalTo("code_dad2", codeDad2)
                .count();
        if (count > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) count;
    }

    public static List<StackPhotoDB> stackPhotoByDad2AndType(long codeDad2, int photoType) {
        RealmResults<StackPhotoDB> realmResults = INSTANCE.where(StackPhotoDB.class)
                .equalTo("code_dad2", codeDad2)
                .equalTo("photo_type", photoType)
                .isNotNull("photo_hash").findAll();
        if (realmResults != null && !realmResults.isEmpty()) {
            return INSTANCE.copyFromRealm(realmResults);
        } else {
            return null;
        }
    }


    /**
     * 19.04.2021
     * Получение списка фотографий по данному посещению.
     *
     * @return
     */
    public static RealmResults<StackPhotoDB> stackPhotoByDad2(long codeDad2) {
        RealmResults<StackPhotoDB> realmResults = INSTANCE.where(StackPhotoDB.class).equalTo("code_dad2", codeDad2).isNotNull("photo_hash").findAll();
        return realmResults;
    }


    // Получене фотографий для выгрузки на сервер
    //"SELECT * FROM stack_photo WHERE upload_to_server = '';"
    public static RealmResults<StackPhotoDB> getStackPhotoPhotoToUpload() {
        Globals.writeToMLOG("INFO", "startExchange/SamplePhotoExchange", "OK");

        RealmResults<StackPhotoDB> realmResults = INSTANCE.where(StackPhotoDB.class).equalTo("upload_to_server", 0)
                .equalTo("get_on_server", 0)
//                .notEqualTo("photo_type", 18)// 08.01.24 Надо на сервер выгружать фото Товаров сделанные с ЗИР, да и вообще
                .and()
                .isNotNull("client_id")
                .isNotNull("addr_id")
                .isNotNull("photo_hash")
                .isNotNull("time_event")
                .findAll();
        Globals.writeToMLOG("INFO", "RealmResults.getStackPhotoPhotoToUpload", "size: " + realmResults.size());
        if (!realmResults.isEmpty()) {
            List<StackPhotoDB> list = INSTANCE.copyFromRealm(realmResults);
            for (StackPhotoDB st : list) {
                Globals.writeToMLOG("INFO", "RealmResults.getStackPhotoPhotoToUpload", "StackPhotoDB id: "
                        + st.getId());
            }
        }
        return realmResults;
    }


    public static RealmResults<StackPhotoDB> getStackPhoto() {
        return INSTANCE.where(StackPhotoDB.class)
//                .notEqualTo("photo_type", 18)
                .notEqualTo("photo_type", 29)
//                .notEqualTo("photo_type", 5)
                .notEqualTo("photo_type", 35)
                .isNotNull("client_id")
                .isNotNull("addr_id")
                .isNotNull("photo_num")
//                .isNull("showcase_id")  // Показываем мерчу в ЖФ фото НЕ "ВИТРИН"
                .findAll();
    }

    public static RealmResults<StackPhotoDB> getStackPhotoLogByDad2(long dad2) {
        return INSTANCE.where(StackPhotoDB.class)
//                .notEqualTo("photo_type", 18)
                .notEqualTo("photo_type", 29)
//                .notEqualTo("photo_type", 5)
                .notEqualTo("photo_type", 35)
                .equalTo("code_dad2", dad2)
                .isNotNull("client_id")
                .isNotNull("addr_id")
//                .isNull("showcase_id")  // Показываем мерчу в ЖФ фото НЕ "ВИТРИН"
                .findAll();
    }

    public static void stackPhotoDeletePhoto() {
        long timeDeadline = System.currentTimeMillis() - 30000;
        long time = Globals.startOfDay(timeDeadline);

        long deleteTimeLessThan = Clock.getDatePeriodLong(System.currentTimeMillis(), -14);
        long deleteTimeLessThanPhotoType0 = Clock.getDatePeriodLong(System.currentTimeMillis(), -3);

        RealmResults<StackPhotoDB> realmResults = INSTANCE.where(StackPhotoDB.class).lessThan("create_time", deleteTimeLessThanPhotoType0).and().notEqualTo("get_on_server", 0).equalTo("photo_type", 0).findAll();

        RealmResults<StackPhotoDB> test = INSTANCE.where(StackPhotoDB.class).lessThan("create_time", deleteTimeLessThan).and().notEqualTo("get_on_server", 0).findAll();

        if (realmResults != null && realmResults.size() > 0) {
            Globals.writeToMLOG("INFO", "stackPhotoDeletePhoto", "realmResults.size(): " + realmResults.size());
            INSTANCE.beginTransaction();
            realmResults.deleteAllFromRealm();
            INSTANCE.commitTransaction();
        }

        if (test != null && test.size() > 0) {
            Globals.writeToMLOG("INFO", "stackPhotoDeletePhoto", "test.size(): " + test.size());
            INSTANCE.beginTransaction();
            test.deleteAllFromRealm();
            INSTANCE.commitTransaction();
        }
    }

    public static RealmResults<StackPhotoDB> stackPhotoGetHashs() {
        // SELECT * FROM stack_photo WHERE get_on_server = '';
        return INSTANCE.where(StackPhotoDB.class).equalTo("get_on_server", 0)
//                .notEqualTo("photo_type", 18)

                // 09.01.24. Ниже добавил что б Товары что я делаю в приложении фиксировались выгруенными
                .isNotNull("client_id")
                .isNotNull("addr_id")
                .isNotNull("photo_hash")
                .isNotNull("time_event").findAll();
    }


    public static StackPhotoDB getTovarPhotoByIdAndType(Integer id, String photoServerId, Integer type, boolean size) {
        String photoSize = "small";
        if (size) photoSize = "full";

        Log.e("GET_TOV_PHOTO", "Data: " + id + "/" + photoServerId + "/" + type + "/" + size);

        RealmQuery<StackPhotoDB> query = INSTANCE.where(StackPhotoDB.class);
        if (photoServerId != null && !photoServerId.equals("0")) {
            query.equalTo("photoServerId", photoServerId)
                    .equalTo("photo_type", type)
                    .equalTo("comment", photoSize)
                    .sort("approve", Sort.DESCENDING, "photoServerId", Sort.DESCENDING);
        } else {
            query.equalTo("object_id", id)
                    .equalTo("photo_type", type)
                    .equalTo("comment", photoSize)
                    .sort("approve", Sort.DESCENDING, "photoServerId", Sort.DESCENDING);
        }

        StackPhotoDB st;
        if (query.findFirst() != null) {
            st = INSTANCE.copyFromRealm(query.findFirst());
        } else {
            st = null;
        }

        return st;
    }

    public static StackPhotoDB getPhotoByPhotoId(String photoServerId) {

        RealmQuery<StackPhotoDB> query = INSTANCE.where(StackPhotoDB.class);

        query.equalTo("photoServerId", photoServerId)
                .isNotNull("photo_num");

        StackPhotoDB st;
        if (query.findFirst() != null) {
            st = INSTANCE.copyFromRealm(query.findFirst());
        } else {
            st = null;
        }
        return st;
    }

    public static StackPhotoDB getPhotoByHash(String hash) {

        RealmQuery<StackPhotoDB> query = INSTANCE.where(StackPhotoDB.class);

        query.equalTo("photo_hash", hash)
                .isNotNull("photo_num");

        StackPhotoDB st;
        if (query.findFirst() != null) {
            st = INSTANCE.copyFromRealm(query.findFirst());
        } else {
            st = null;
        }
        return st;
    }

    public static StackPhotoDB getPhotoByIdAndType(Integer id, String photoServerId, int photoType) {

        Log.e("GET_TOV_PHOTO 2", "Data: " + id + "/" + photoServerId + "/" + photoType);

        RealmQuery<StackPhotoDB> query = INSTANCE.where(StackPhotoDB.class);
        if (photoServerId != null
                && !photoServerId.equals("0")
        ) {
            query.equalTo("photoServerId", photoServerId)
                    .equalTo("photo_type", photoType)
                    .isNotNull("photo_num")
            ;//                    .equalTo("comment", photoSize)
//                    .sort("approve", Sort.DESCENDING, "photoServerId", Sort.DESCENDING);
        } else {
            query.equalTo("object_id", id)
                    .equalTo("photo_type", photoType);
//                    .equalTo("comment", photoSize)
//                    .sort("approve", Sort.DESCENDING, "photoServerId", Sort.DESCENDING);
        }

        StackPhotoDB st;
        if (query.findFirst() != null) {
            st = INSTANCE.copyFromRealm(query.findFirst());
        } else {
            st = null;
        }
        return st;
    }

    public static StackPhotoDB getPhotoById(Integer id, String photoServerId) {

        RealmQuery<StackPhotoDB> query = INSTANCE.where(StackPhotoDB.class);
        if (photoServerId != null && !photoServerId.equals("0")) {
            query.equalTo("photoServerId", photoServerId);
        } else {
            query.equalTo("object_id", id);
        }

        StackPhotoDB st;
        if (query.findFirst() != null) {
            st = INSTANCE.copyFromRealm(query.findFirst());
        } else {
            st = null;
        }

        return st;
    }


    public static ArrayList<TovarDB> getTovarListPhotoToDownload(List<TovarDB> list, String photoSize) {
        ArrayList<TovarDB> res = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            StackPhotoDB stackPhotoDB = INSTANCE.where(StackPhotoDB.class).equalTo("object_id", Integer.parseInt(list.get(i).getiD())).equalTo("comment", photoSize).findFirst();
            if (stackPhotoDB == null) {
                res.add(list.get(i));
            }
        }
        return res;
    }

    public static List<StackPhotoDB> getStackPhotoByDad2(long dad2) {
        return INSTANCE.where(StackPhotoDB.class).equalTo("code_dad2", dad2).findAll();
    }


    /**
     * 21.08.2020
     * Проверка - есть ли такая фотка в БД или нет.
     * Если она будет - true.
     */
    public static boolean stackPhotoExistByObjectId(int objectId) {
        return INSTANCE.where(StackPhotoDB.class).equalTo("object_id", objectId).findFirst() != null;
    }

    public static boolean stackPhotoExistByObjectId(int objectId, String type) {
        boolean res = false;

        StackPhotoDB stackPhotoDB = INSTANCE.where(StackPhotoDB.class)
                .equalTo("object_id", objectId)
                .equalTo("comment", type)
                .findFirst();

        if (stackPhotoDB != null) stackPhotoDB = INSTANCE.copyFromRealm(stackPhotoDB);
        if (stackPhotoDB != null && stackPhotoDB.getId() != 0) res = true;

        return res;
    }

    /**
     * 26.02.2021
     * Получение всех данных >= пришедшей ВПИ
     */
    public static List<StackPhotoDB> stackPhotoGetNewDataByDVI(long vpi) {
        return INSTANCE.where(StackPhotoDB.class).greaterThanOrEqualTo("vpi", vpi).findAll();
    }

    /**
     * 27.02.2021
     */
    public static StackPhotoDB stackPhotoGetPhotoById(String id) {
        return INSTANCE.where(StackPhotoDB.class).equalTo("id", Integer.parseInt(id)).findFirst();
    }

    public static List<StackPhotoDB> stackPhotoDBListGetDVIToUpload() {
        return INSTANCE.copyFromRealm(INSTANCE.where(StackPhotoDB.class).isNotNull("photoServerId").notEqualTo("photoServerId", "").equalTo("dviUpload", true).findAll());
    }


    public static List<StackPhotoDB> stackPhotoDBListGetCommentToUpload() {
        return INSTANCE.copyFromRealm(INSTANCE.where(StackPhotoDB.class)
                .isNotNull("photoServerId")
                .notEqualTo("photoServerId", "")
                .equalTo("commentUpload", true)
                .findAll());
    }


    public static List<StackPhotoDB> stackPhotoDBListGetRatingToUpload() {
        return INSTANCE.copyFromRealm(INSTANCE.where(StackPhotoDB.class).isNotNull("photoServerId").notEqualTo("photoServerId", "").equalTo("markUpload", true).findAll());
    }

    //==============================================================================================
    // STACK PHOTO END:-----------------------------------------------------------------------------
    //==============================================================================================


    // Synchronization Timetable:---------------------------------------------------------------------------------
    public static RealmResults<SynchronizationTimetableDB> getSynchronizationTimetable() {
        return INSTANCE.where(SynchronizationTimetableDB.class).findAll();
    }

    public static SynchronizationTimetableDB getSynchronizationTimetableRowByTable(String tableName) {
        return INSTANCE.where(SynchronizationTimetableDB.class).equalTo("table_name", tableName).findFirst();
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
        LogDB rp = INSTANCE.where(LogDB.class).equalTo("id", id).findFirst();

        return RealmManager.INSTANCE.copyFromRealm(rp);
    }

    public static int getLastIdLogDB() {
        RealmResults<LogDB> realmResults = INSTANCE.where(LogDB.class).findAll();
        try {
            Log.e("TAG_REALM_LOG", "int: " + Objects.requireNonNull(realmResults.last().getId()));

            return Objects.requireNonNull(realmResults.last().getId());
        } catch (Exception e) {
            return 0;
        }
    }

    public static ArrayList<LogUploadToServ> getLogToSend() {

        List<LogDB> list = INSTANCE.where(LogDB.class).isNull("dt").findAll();

        ArrayList<LogUploadToServ> logList = new ArrayList<>();

        if (list != null) {
            for (LogDB l : list) {
                logList.add(new LogUploadToServ(String.valueOf(l.getId()), String.valueOf(l.getDt_action()), l.getComments(), String.valueOf(l.getTp()), l.getClient_id(), String.valueOf(l.getAddr_id()), String.valueOf(l.getObj_id()), String.valueOf(System.currentTimeMillis() / 1000)));

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
                    wpDataList.add(new WpDataUploadToServ(String.valueOf(l.getId()), String.valueOf(l.getCode_dad2()), String.valueOf(l.getUser_id()), String.valueOf(l.getClient_id()), String.valueOf(l.getIsp()),

                            String.valueOf(l.getVisit_start_dt()), String.valueOf(l.getVisit_end_dt()), String.valueOf(l.getClient_start_dt()), String.valueOf(l.getClient_end_dt()),

                            String.valueOf(l.getSetStatus())));
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
        List<WpDataDB> list = RealmManager.INSTANCE.copyFromRealm(WpDataRealm.getWpDataToUpdate());
        List<StartEndData> res = new ArrayList<>();

        int id = 0;
        if (list != null && !list.isEmpty()) {
            for (WpDataDB l : list) {
                if (l.startUpdate || l.getSetStatus() > 0) {
                    id++;
                    StartEndData item = new StartEndData();
                    item.temp_id = id;
                    item.element_id = String.valueOf(l.getId());
                    item.code_dad2 = String.valueOf(l.getCode_dad2());
                    item.user_id = String.valueOf(l.getUser_id());
                    item.client_id = String.valueOf(l.getClient_id());
                    item.isp = String.valueOf(l.getIsp());
                    item.dt_update = l.getDt_update();

                    if (l.startUpdate) {
                        item.visit_start_dt = String.valueOf(l.getVisit_start_dt());
                        item.visit_end_dt = String.valueOf(l.getVisit_end_dt());
                        item.client_start_dt = String.valueOf(l.getClient_start_dt());
                        item.client_end_dt = String.valueOf(l.getClient_end_dt());
                        item.client_work_duration = String.valueOf(l.client_work_duration);
                        item.status_set = String.valueOf(l.getSetStatus());

                        item.user_comment = l.user_comment;
                        item.user_comment_author_id = l.user_comment_author_id;
                        item.user_comment_dt_update = l.user_comment_dt_update;

                        item.user_opinion_id = l.getUser_opinion_id();
                        item.user_opinion_author_id = l.getUser_opinion_author_id();
                        item.user_opinion_dt_update = l.getUser_opinion_dt_update();
                    }

                    res.add(item);
                } else {
                    Log.e("getWpDataStartEndWork", "l.getId: " + l.getId());
                    Log.e("getWpDataStartEndWork", "l.getCode_dad2: " + l.getCode_dad2());
                    Log.e("getWpDataStartEndWork", "l.startUpdate: " + l.startUpdate);
                    Log.e("getWpDataStartEndWork", "l.getSetStatus(): " + l.getSetStatus());
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
                if (l.startUpdate) {
                    StartEndData item = new StartEndData();

                    item.element_id = String.valueOf(l.getId());
                    item.code_dad2 = String.valueOf(l.getCode_dad2());
                    item.user_id = String.valueOf(l.getUser_id());
                    item.client_id = String.valueOf(l.getClient_id());
                    item.isp = String.valueOf(l.getIsp());
                    item.dt_update = System.currentTimeMillis() / 1000; // TODO DELETE THIS, ONLY DEBUG

                    switch (uploadMode) {
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
        RealmResults<LogDB> realmResults = INSTANCE.where(LogDB.class).isNull("dt").findAll();


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
        CustomerDB realmResults = INSTANCE.where(CustomerDB.class).equalTo("id", id).findFirst();
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
        AddressDB realmResults = INSTANCE.where(AddressDB.class).equalTo("addrId", id).findFirst();
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
        UsersDB realmResults = INSTANCE.where(UsersDB.class).equalTo("id", id).findFirst();
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
//        Log.e("TovarListFromRPByDad2", "Start: " + dad2);
        Globals.writeToMLOG("INFO", "getTovarListFromReportPrepareByDad2", "Start dad2: " + dad2);

        ArrayList<String> listRpTovId = new ArrayList<>();
        ArrayList<String> listTovId = new ArrayList<>();

        RealmResults<ReportPrepareDB> realmResults = INSTANCE.where(ReportPrepareDB.class).equalTo("codeDad2", String.valueOf(dad2)).findAll();

        List<ReportPrepareDB> reportPrepareDBList = INSTANCE.copyFromRealm(realmResults);

        Globals.writeToMLOG("INFO", "getTovarListFromReportPrepareByDad2", "reportPrepareDBList.size(): " + reportPrepareDBList.size());

        String[] list = new String[reportPrepareDBList.size()];
        for (int i = 0; i < reportPrepareDBList.size(); i++) {
            list[i] = reportPrepareDBList.get(i).getTovarId();
            listRpTovId.add(reportPrepareDBList.get(i).getTovarId());
        }

//        Log.e("TovarListFromRPByDad2", "list: " + list);

        RealmResults<TovarDB> realmResults2 = INSTANCE.where(TovarDB.class).in("iD", list).equalTo("deleted", 0)      // Не показывать удалённые Товары
                .findAll();

        Globals.writeToMLOG("INFO", "getTovarListFromReportPrepareByDad2", "realmResults2.size(): " + realmResults2.size());

        try {
            for (TovarDB item : realmResults2) {
                listTovId.add(item.getiD());
            }

            // TODO Загрузка недостающих Товаров (ФОТО)
//            ArrayList<String> tovarsToDownload = neededTovars(listRpTovId, listTovId);
//            if (tovarsToDownload.size() > 0) {
//                TablesLoadingUnloading tablesLoadingUnloading = new TablesLoadingUnloading();
//                tablesLoadingUnloading.downloadTovarTable(null, tovarsToDownload);
//            }
        } catch (Exception e) {
            Log.e("TovarListFromRPByDad2", "ERR: " + e);
            Globals.writeToMLOG("ERROR", "getTovarListFromReportPrepareByDad2", "Exception e: " + e);
            return null;
        }


        Log.e("TovarListFromRPByDad2", "3");


        for (TovarDB item : RealmManager.INSTANCE.copyFromRealm(realmResults2)) {
            if (item.getManufacturer() == null) {
                String id = item.getManufacturerId();
                TradeMarkDB tm = TradeMarkRealm.getTradeMarkRowById(id);
                String sortCol = item.getSortcol();
                String data = sortCol.toLowerCase();

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

    /**
     * 09.08.2024
     * Тоже самое что и
     */
    public static List<TovarDB> getTovarListFromReportPrepareByDad2Copy(long dad2) {
        ArrayList<String> listRpTovId = new ArrayList<>();
        ArrayList<String> listTovId = new ArrayList<>();
        RealmResults<ReportPrepareDB> realmResults = INSTANCE.where(ReportPrepareDB.class).equalTo("codeDad2", String.valueOf(dad2)).findAll();
        List<ReportPrepareDB> reportPrepareDBList = INSTANCE.copyFromRealm(realmResults);
        String[] list = new String[reportPrepareDBList.size()];
        for (int i = 0; i < reportPrepareDBList.size(); i++) {
            list[i] = reportPrepareDBList.get(i).getTovarId();
            listRpTovId.add(reportPrepareDBList.get(i).getTovarId());
        }
        RealmResults<TovarDB> realmResults2 = INSTANCE.where(TovarDB.class).in("iD", list).equalTo("deleted", 0)      // Не показывать удалённые Товары
                .findAll();
        try {
            for (TovarDB item : realmResults2) {
                listTovId.add(item.getiD());
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERROR", "getTovarListFromReportPrepareByDad2", "Exception e: " + e);
            return null;
        }

        for (TovarDB item : RealmManager.INSTANCE.copyFromRealm(realmResults2)) {
            if (item.getManufacturer() == null) {
                String id = item.getManufacturerId();
                TradeMarkDB tm = TradeMarkRealm.getTradeMarkRowById(id);
                String sortCol = item.getSortcol();
                String data = sortCol.toLowerCase();

                INSTANCE.executeTransaction(realm -> {
                    item.setSortcol(data);
                    item.setManufacturer(tm);
                    INSTANCE.copyToRealmOrUpdate(item);
                });
            }
        }
        realmResults2 = realmResults2.sort("manufacturer.nm", Sort.ASCENDING, "sortcol", Sort.ASCENDING);
        return RealmManager.INSTANCE.copyFromRealm(realmResults2);
    }


    private static ArrayList<String> neededTovars(ArrayList<String> listRpTovId, ArrayList<String> listTovId) {
        ArrayList<String> res = new ArrayList<>(listRpTovId);
        res.removeAll(listTovId);
        return res;
    }


    public static RealmResults<ReportPrepareDB> getRPBYDAD2() {
        return INSTANCE.where(ReportPrepareDB.class).equalTo("codeDad2", "1220421036217049444").findAll();
    }


    // Получение по ДАД_2 с плана работ списка id товаров

    /**
     * 30.03.2021
     * Такая же функция как выше, но отбирает вторую половину данных
     */
    public static RealmResults<TovarDB> getTovarListFromReportPrepareByDad2Not(long dad2) {
        RealmResults<ReportPrepareDB> realmResults = INSTANCE.where(ReportPrepareDB.class).notEqualTo("codeDad2", String.valueOf(dad2)).findAll();

        String[] list = new String[realmResults.size()];
        for (int i = 0; i < realmResults.size(); i++) {
            list[i] = realmResults.get(i).getTovarId();
        }

        RealmResults<TovarDB> realmResults2 = INSTANCE.where(TovarDB.class).in("iD", list).sort("manufacturerId", Sort.ASCENDING, "weight", Sort.DESCENDING).findAll();

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
                .equalTo("deleted", 0)      // Не показывать удалённые Товары
                .findAll();

        res = res.sort("sortcol", Sort.ASCENDING);
        return res;
    }


    // Получение листа Опций по данному товару
    public static List<OptionsDB> getTovarOptionInReportPrepare(String dad2, String tovarId) {
        RealmResults<OptionsDB> optionsDBS = INSTANCE.where(OptionsDB.class).equalTo("codeDad2", dad2).sort("so").findAll();
        List<OptionsDB> res = new ArrayList<>();
        if (optionsDBS != null) res = INSTANCE.copyFromRealm(optionsDBS);
        return res;
    }

    // Получение листа Опций по данному товару
    public static TovarDB getTovar(String tovarId) {
        TovarDB optionsDBS = INSTANCE.where(TovarDB.class)
                .equalTo("iD", tovarId)
                .findFirst();
        TovarDB res = null;
        if (optionsDBS != null) res = INSTANCE.copyFromRealm(optionsDBS);
        return res;
    }

    // Получение строки с ReportPrepare для записи туда данных
    public static ReportPrepareDB getTovarReportPrepare(String dad2, String tovarId) {
        ReportPrepareDB prepareDB = INSTANCE.where(ReportPrepareDB.class).equalTo("tovarId", tovarId).and().equalTo("codeDad2", dad2).findFirst();

        ReportPrepareDB res = null;
        if (prepareDB != null) res = INSTANCE.copyFromRealm(prepareDB);
        return res;
    }


    // Получение строки из ReportPrepare по ID
    public static ReportPrepareDB getReportPrepareRowById(String idS) {
        long id;
        try {
            id = Long.parseLong(idS); // Костыль ибо напортачено с id шками на стороне сервера и тут
        } catch (Exception e) {
            id = 0L;
        }
//        ReportPrepareDB rp = INSTANCE.where(ReportPrepareDB.class).equalTo("iD", id).findFirst();
        return INSTANCE.where(ReportPrepareDB.class).equalTo("iD", id).findFirst();
    }


    public static void setReportPrepareRow(ReportPrepareDB reportPrepare) {
        INSTANCE.copyToRealmOrUpdate(reportPrepare);
    }


    public static ArrayList<ReportPrepareServ> getReportPrepareToUpload() {

        List<ReportPrepareDB> rp = INSTANCE.where(ReportPrepareDB.class).equalTo("uploadStatus", 1).findAll();

        rp = INSTANCE.copyFromRealm(rp);

        ArrayList<ReportPrepareServ> reportPrepareServ = new ArrayList<>();

        if (rp != null && !rp.isEmpty()) {

            for (int i = 0; i < rp.size(); i++) {
                Log.e("REPORT_PREPARE_SEND", "DATABASE + ");
                reportPrepareServ.add(new ReportPrepareServ(String.valueOf(rp.get(i).getID()), rp.get(i).getDtChange(), rp.get(i).getDtReport(), rp.get(i).getKli(), rp.get(i).getTovarId(), rp.get(i).getAddrId(), rp.get(i).getPrice(), rp.get(i).getFace(), rp.get(i).getAmount(), rp.get(i).getDtExpire(), rp.get(i).getExpireLeft(), rp.get(i).getNotes(), rp.get(i).getUp(), rp.get(i).getAkciya(), rp.get(i).getAkciyaId(), rp.get(i).getOborotvedNum(), rp.get(i).getErrorId(), rp.get(i).getErrorComment(), rp.get(i).getCodeDad2(), String.valueOf(rp.get(i).buyerOrderId)));
            }
        } else {
            Log.e("REPORT_PREPARE_SEND", "DATABASE - ");
        }


        Log.e("REPORT_PREPARE_SEND", "SIZE UPLOAD: " + reportPrepareServ.size());
        return reportPrepareServ;
    }


    public static long reportPrepareGetLastId() {
        RealmResults<ReportPrepareDB> realmResults = INSTANCE.where(ReportPrepareDB.class).findAll();
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
        return INSTANCE.where(TradeMarkDB.class).equalTo("iD", id).findFirst();
    }


    // ERROR DB
    public static RealmResults<ErrorDB> getAllErrorDb() {
        return INSTANCE.where(ErrorDB.class).findAll();
    }

    // PROMO DB
    public static RealmResults<PromoDB> getAllPromoDb() {
        return INSTANCE.where(PromoDB.class).findAll();
    }


    /**
     * 30.09.2020
     * <p>
     * РАБОТА С LOG_MP_DB
     * Получаем последнюю запись
     */
    public static int logMPGetLastId() {
        Number maxId = INSTANCE.where(LogMPDB.class).max("id");
        if (maxId != null) {
            return maxId.intValue();
        } else {
            return 0;
        }
    }

    /**
     * 30.09.2020
     * Запись строки в БД
     */
    public static void setLogMpRow(LogMPDB row) {
        INSTANCE.beginTransaction();
        try {
            INSTANCE.copyToRealmOrUpdate(row);
            INSTANCE.commitTransaction();
        } catch (Exception e) {
            INSTANCE.cancelTransaction();
            e.printStackTrace();
        }
    }

    /**
     * 30.09.2020
     * Получение всей таблици для выгруки на сервер
     */
    public static RealmResults<LogMPDB> getAllLogMPDB() {
        return INSTANCE.where(LogMPDB.class).findAll();
    }

    public static RealmResults<LogMPDB> getNOTUploadLogMPDB() {
        return INSTANCE.where(LogMPDB.class).equalTo("upload", 0).equalTo("serverId", 0).findAll();
    }

    public static RealmResults<LogMPDB> getNOTUploadLogMPDBTEST() {

        Integer[] ids = {432, 431, 430, 429, 428, 427, 426, 425, 424};

        return INSTANCE.where(LogMPDB.class).in("id", ids).findAll();
    }

    public static RealmResults<MenuItemFromWebDB> getSiteMenu() {
        return INSTANCE.where(MenuItemFromWebDB.class).findAll();
    }


    //    public RealmQuery<MenuItemFromWebDB> in(String fieldName, Integer[] values);
    public static RealmResults<MenuItemFromWebDB> getSiteMenuItems(Integer[] ids) {
        return INSTANCE.where(MenuItemFromWebDB.class).in("id", ids).findAll();
    }

    public static MenuItemFromWebDB getSiteMenuItem(Integer id) {
        return INSTANCE.where(MenuItemFromWebDB.class).equalTo("id", id).findFirst();
    }


    public static SiteObjectsDB getLesson(int id) {
        return INSTANCE.where(SiteObjectsDB.class).equalTo("id", id).findFirst();
    }

    public static List<SiteObjectsDB> getLesson(Integer[] ids) {
        RealmResults<SiteObjectsDB> res = INSTANCE.where(SiteObjectsDB.class).in("id", ids)
//                .sort("lesson_id", Sort.ASCENDING)
                .findAll();
        if (res != null) {
            return RealmManager.INSTANCE.copyFromRealm(res);
        } else {
            return null;
        }
    }

    public static List<SiteHintsDB> getAllVideoLessons() {
        return INSTANCE.copyFromRealm(INSTANCE.where(SiteHintsDB.class).findAll());
    }

    public static SiteHintsDB getVideoLesson(int id) {
        return INSTANCE.where(SiteHintsDB.class).equalTo("id", id).findFirst();
    }

    public static List<SiteHintsDB> getVideoLesson(Integer[] ids) {
        RealmResults<SiteHintsDB> res = INSTANCE.where(SiteHintsDB.class).in("id", ids).findAll();

        if (res != null) {
            return RealmManager.INSTANCE.copyFromRealm(res);
        } else {
            return null;
        }
    }


    public static LangListDB getLangList(String type) {
        return INSTANCE.where(LangListDB.class).equalTo("nmShort", type).findFirst();
    }

    public static List<LangListDB> getAllLangList() {
        return INSTANCE.where(LangListDB.class).findAll();
    }

//    public static List<SiteTranslationsList> getAllSiteTranslationsList() {
//        return INSTANCE.where(SiteTranslationsList.class)
//                .findAll();
//    }

    public static List<SiteTranslationsList> getSiteTranslationsList(String id) {
        return INSTANCE.where(SiteTranslationsList.class).equalTo("langId", id).findAll();
    }

}
