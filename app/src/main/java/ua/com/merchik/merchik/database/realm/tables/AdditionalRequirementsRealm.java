package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB;
import ua.com.merchik.merchik.data.RealmModels.AdditionalRequirementsDB;
import ua.com.merchik.merchik.data.RealmModels.AddressDB;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

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


    //    public static List<AdditionalRequirementsDB> getData3(String clientId, String addrId, int themeId) {
    public static <T> RealmResults<AdditionalRequirementsDB> getData3(T data) {
        int themeId, addressId;
        String clientId;
        if (data instanceof WpDataDB) {
            addressId = ((WpDataDB) data).getAddr_id();
            clientId = ((WpDataDB) data).getClient_id();
            themeId = ((WpDataDB) data).getTheme_id();
        } else if (data instanceof TasksAndReclamationsSDB) {
            WpDataDB wp = WpDataRealm.getWpDataRowByDad2Id(((TasksAndReclamationsSDB) data).codeDad2SrcDoc);
//            addressId = ((TasksAndReclamationsSDB) data).addr;
//            clientId = ((TasksAndReclamationsSDB) data).client;
//            themeId = ((TasksAndReclamationsSDB) data).themeId;
            addressId = wp.getAddr_id();
            clientId = wp.getClient_id();
            themeId = wp.getTheme_id();
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

        if (themeId != 998) {
            realmResults = realmResults.where()
                    .equalTo("themeId", String.valueOf(themeId))
                    .findAll();
        }


        return realmResults;
    }


    public static AdditionalRequirementsDB getADByClient(String addrId, String clientId){
        AdditionalRequirementsDB res = INSTANCE.where(AdditionalRequirementsDB.class)
                .equalTo("clientId", clientId)
                .equalTo("addrId", addrId)
                .equalTo("not_approve", "0")
                .notEqualTo("userId", "0")
                .findFirst();

        return res;
    }
}
