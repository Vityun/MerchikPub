package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import io.realm.RealmResults;
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
    public static RealmResults<AdditionalRequirementsDB> getData3(WpDataDB wp) {

        int themeId = wp.getTheme_id();
        String clientId = wp.getClient_id();
        AddressDB addressDB = AddressRealm.getAddressById(wp.getAddr_id());


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
                .equalTo("addrId", String.valueOf(wp.getAddr_id()))
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

}
