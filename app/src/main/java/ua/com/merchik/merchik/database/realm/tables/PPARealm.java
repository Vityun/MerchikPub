package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.PPADB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class PPARealm {

    /**
     * 05.03.2021
     * СОхранение ППА
     */
    public static void setPPA(List<PPADB> list) {
        INSTANCE.beginTransaction();
        INSTANCE.delete(PPADB.class);
        INSTANCE.copyToRealmOrUpdate(list);
        INSTANCE.commitTransaction();
    }


    /**
     * 05.03.2021
     * Получение списка по IZA
     * @return
     */
    public static List<PPADB> getPPAIZAList(String iza, String client, String addrId) {
        return INSTANCE.where(PPADB.class)
                .equalTo("codeIza", iza)
                .equalTo("client", client)
                .equalTo("addrId", addrId)
                .findAll();
    }

    public static List<PPADB> getPPAList(String client, String addrId) {
        return INSTANCE.where(PPADB.class)
                .equalTo("client", client)
                .equalTo("addrId", addrId)
                .findAll();
    }

    public static PPADB getPPAIZA(String iza, String client, String addrId, String tovarId) {
        return INSTANCE.where(PPADB.class)
                .equalTo("codeIza", iza)
                .equalTo("client", client)
                .equalTo("addrId", addrId)
                .equalTo("tovarId", tovarId)
                .findFirst();
    }
}
