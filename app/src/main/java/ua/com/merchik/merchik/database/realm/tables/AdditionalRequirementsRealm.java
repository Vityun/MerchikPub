package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.data.BaseBusinessData;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.AddressDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

/**
 * 21.04.2021
 */
public class AdditionalRequirementsRealm {

    public static void setDataToDB(List<AdditionalRequirementsDB> data) {
        INSTANCE.beginTransaction();
        INSTANCE.delete(AdditionalRequirementsDB.class);
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }

    private static <T> BaseBusinessData getAdditionalRequirementsDocumentData(T document) {
        BaseBusinessData res = new BaseBusinessData();
        if (document instanceof WpDataDB) {
            WpDataDB wp = (WpDataDB) document;

            res.clientId = wp.getClient_id();
            res.addressId = wp.getAddr_id();
            res.themeId = wp.getTheme_id();

        } else if (document instanceof TasksAndReclamationsSDB) {
            WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(((TasksAndReclamationsSDB) document).codeDad2SrcDoc);

            res.clientId = wp.getClient_id();
            res.addressId = wp.getAddr_id();
            res.themeId = wp.getTheme_id();
        }

        return res;
    }

    public static <T> List<AdditionalRequirementsDB> getDocumentAdditionalRequirements(Object document, boolean tovExist, Integer optionId, String dateFrom, String dateTo, Object dt) {
        BaseBusinessData data = getAdditionalRequirementsDocumentData(document);
        AddressDB addressDB = AddressRealm.getAddressById(data.addressId);

        // --------------------
        RealmResults res = INSTANCE.where(AdditionalRequirementsDB.class)
                .equalTo("clientId", data.clientId)
                .equalTo("not_approve", "0")
                .findAll();

        try {
            // --------------------
            res = res.where()
                    .beginGroup()
                    .equalTo("grpId", "0")
                    .equalTo("addrId", "0")
                    .endGroup()
                    .or()
                    .beginGroup()
                    .equalTo("grpId", "0")
                    .equalTo("addrId", String.valueOf(data.addressId))
                    .endGroup()
                    .or()
                    .beginGroup()
                    .equalTo("grpId", "0")
                    .equalTo("addrId", String.valueOf(addressDB.getTpId()))
                    .endGroup()
                    .or()
                    .beginGroup()
                    .equalTo("grpId", String.valueOf(addressDB.getTpId()))
                    .equalTo("addrId", "0")
                    .endGroup()

                    .findAll();

            // --------------------
            if (data.themeId != 998) {
                res = res.where()
                        .equalTo("themeId", String.valueOf(data.themeId))
                        .findAll();
            }

            // --------------------
            if (tovExist) {
                res = res.where()
                        .isNotEmpty("tovarId")
                        .notEqualTo("tovarId", "0")
                        .findAll();
            }
            // --------------------
            if (optionId != null && optionId != 0) {
                res = res.where()
                        .equalTo("optionId", String.valueOf(optionId))
                        .findAll();
            }
        } catch (Exception e) {
            Globals.writeToMLOG("ERR", "getDocumentAdditionalRequirements", "Exception e: " + e);
        }

        List<AdditionalRequirementsDB> result = new ArrayList<>();
        if (res != null) result = INSTANCE.copyFromRealm(res);
        return result;
    }

    /**
     * 07.03.23.
     * Режим работы функции "getData3". В зависимости от енума - данные будут или скрываться или нет.
     */
    public enum AdditionalRequirementsModENUM {
        DEFAULT,            // Штатный режим, отображаю Всё. Такое как было ДО написания этого безобразия. Нужно для Акций и тп..
        HIDE_FOR_USER,      // Скрывать для пользователя. Нужно для Кнопки Доп. Требований. что б не показывался мерчандайзерам всякий мусор.
        HIDE_FOR_CLIENT     // Скрывать для клиента. Добавил просто потому что в БД есть такое поле и может в будущем пригодиться.
    }

    public static <T> List<AdditionalRequirementsDB> getData3(T data, AdditionalRequirementsModENUM mod, Integer ttCategory, String optionId, int mode) {

        Log.e("getData3", "mod: " + mod);

        int themeId, addressId;
        String clientId;
        long dad2;
        Date date = new Date();

        if (data instanceof WpDataDB) {
            addressId = ((WpDataDB) data).getAddr_id();
            clientId = ((WpDataDB) data).getClient_id();
            themeId = ((WpDataDB) data).getTheme_id();
            dad2 = ((WpDataDB) data).getCode_dad2();
            date = ((WpDataDB) data).getDt();
        } else if (data instanceof TasksAndReclamationsSDB) {
            WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(((TasksAndReclamationsSDB) data).codeDad2SrcDoc);
            addressId = wp.getAddr_id();
            clientId = wp.getClient_id();
            themeId = wp.getTheme_id();
            dad2 = wp.getCode_dad2();
        } else {
            return null;
        }

        AddressDB addressDB = AddressRealm.getAddressById(addressId);


        RealmResults realmResults = INSTANCE.where(AdditionalRequirementsDB.class)
                .equalTo("clientId", clientId)
                .equalTo("not_approve", "0")
                .findAll();


        realmResults = realmResults.where()
                .beginGroup()
                .equalTo("grpId", "0")
                .equalTo("addrId", "0")
                .endGroup()
                .or()
                .beginGroup()
                .equalTo("grpId", "0")
                .equalTo("addrId", String.valueOf(addressId))
                .endGroup()
                .or()
                .beginGroup()
                .equalTo("grpId", "0")
                .equalTo("addrId", String.valueOf(addressDB.getTpId()))
                .endGroup()
                .or()
                .beginGroup()
                .equalTo("grpId", String.valueOf(addressDB.getTpId()))
                .equalTo("addrId", "0")
                .endGroup()

                .findAll();

        RealmResults realmResults1 = realmResults.where()
                .equalTo("hideUser", "1")
                .findAll();

        RealmResults realmResults2 = realmResults.where()
                .equalTo("hideClient", "1")
                .findAll();

        Log.e("getData3", "realmResults1: " + realmResults1);
        Log.e("getData3", "realmResults2: " + realmResults2);

        switch (mod) {
            case HIDE_FOR_USER:
                realmResults = realmResults.where()
                        .equalTo("hideUser", "0")
//                        .equalTo("disableScore", "1")
                        .findAll();
                break;

            case HIDE_FOR_CLIENT:
                realmResults = realmResults.where()
                        .equalTo("hideClient", "0")
                        .findAll();
                break;

            case DEFAULT:
//                realmResults = realmResults.where()
//                        .equalTo("hideUser", "1")
//                        .findAll();
                break;
        }

        if (mode == 1) {
            realmResults = realmResults.where()
                    .notEqualTo("disableScore", "1")
                    .findAll();
        }

        if (themeId == 998) {
//            realmResults = realmResults.where()
//                    .notEqualTo("themeId", "1182")
//                    .findAll();

            realmResults = realmResults.where()
                    .equalTo("themeId", "998")
                    .or()
                    .equalTo("themeId", "977")
                    .findAll();
        } else {
            realmResults = realmResults.where()
                    .equalTo("themeId", String.valueOf(themeId))
                    .findAll();
        }

        // Поиск по категориям ТТ
        if (ttCategory != null) {
            realmResults = realmResults.where()
                    .equalTo("addrTTId", ttCategory)
                    .or()
                    .equalTo("addrTTId", 0)
                    .or()
                    .isNull("addrTTId")
                    .findAll();
        }

//        // Фильтрация по дате
//        Date currentDate = new Date(); // текущая дата

        realmResults = realmResults.where()
                .beginGroup()
                .beginGroup()
                .isNull("dtStart") // Проверка на NULL для неограниченной даты
                .or()
                .lessThanOrEqualTo("dtStart", date)
                .endGroup()
                .or()
                .isNull("dtStart") // Проверка на NULL для неограниченной даты
                .endGroup()
                .and()
                .beginGroup()
                .beginGroup()
                .isNull("dtEnd") // Проверка на NULL для неограниченной даты
                .or()
                .greaterThanOrEqualTo("dtEnd", date)
                .endGroup()
                .or()
                .isNull("dtEnd") // Проверка на NULL для неограниченной даты
                .endGroup()
                .findAll();

        if (optionId != null && !optionId.equals("")){
            realmResults = realmResults.where()
                    .equalTo("optionId", optionId)
                    .findAll();
        }


        return RealmManager.INSTANCE.copyFromRealm(realmResults);
    }

    public static List<AdditionalRequirementsDB> getAdditionalRequirements(String clientId, int addressId, int optionId) {
        List<AdditionalRequirementsDB> res = new ArrayList<>();

        res = INSTANCE.where(AdditionalRequirementsDB.class)
                .equalTo("clientId", clientId)
                .equalTo("addrId", String.valueOf(addressId))
                .equalTo("optionId", String.valueOf(optionId))
                .equalTo("not_approve", "0")
                .findAll();

        if (res != null) res = INSTANCE.copyFromRealm(res);

        return res;
    }

    public static AdditionalRequirementsDB getADByClientAdr(String addrId, String clientId) {
        AdditionalRequirementsDB res = INSTANCE.where(AdditionalRequirementsDB.class)
                .equalTo("clientId", clientId)
                .equalTo("addrId", addrId)
                .equalTo("not_approve", "0")
                .notEqualTo("userId", "0")
                .findFirst();

        return res;
    }

    public static AdditionalRequirementsDB getADByClient(String clientId) {
        AdditionalRequirementsDB res = INSTANCE.where(AdditionalRequirementsDB.class)
                .equalTo("clientId", clientId)
                .equalTo("not_approve", "0")
                .notEqualTo("userId", "0")
                .findFirst();

        return res;
    }

    public static List<AdditionalRequirementsDB> getADByClientAll(String clientId, String themeId) {
        List<AdditionalRequirementsDB> res = INSTANCE.where(AdditionalRequirementsDB.class)
                .equalTo("clientId", clientId)
                .equalTo("themeId", themeId)
                .findAll();
        if (res != null) res = INSTANCE.copyFromRealm(res);
        return res;
    }


    /**
     * 20.09.2022
     * <p>
     * Тут мы попробуем сделать запрос к Доп. Требованиям, что б получить ПТТ-шника
     */
    public static RealmResults<AdditionalRequirementsDB> getAdditionalRequirementsDBTest(String clientId, String addrId, String optionId) {
        RealmResults<AdditionalRequirementsDB> res = INSTANCE.where(AdditionalRequirementsDB.class).findAll();

        if (clientId != null && !clientId.equals("0")) {
            res = res.where().equalTo("clientId", clientId).findAll();
        }

        if (addrId != null && !addrId.equals("0")) {
            res = res.where().equalTo("addrId", addrId).findAll();
        }

        if (optionId != null && !optionId.equals("0")) {
            res = res.where().equalTo("optionId", optionId).findAll();
        }

        return res;
    }
}
