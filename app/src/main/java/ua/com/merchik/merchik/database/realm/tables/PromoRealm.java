package ua.com.merchik.merchik.database.realm.tables;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.RealmModels.PromoDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class PromoRealm {
    public static RealmResults<PromoDB> getAllPromoDB() {
        return INSTANCE.where(PromoDB.class)
                .findAll();
    }

    public static PromoDB getPromoDBByNm(String nm) {
        return INSTANCE.where(PromoDB.class)
                .equalTo("nm", nm)
                .findFirst();
    }

    public static PromoDB getPromoDBById(String id) {
        return INSTANCE.where(PromoDB.class)
                .equalTo("ID", id)
                .findFirst();
    }
}
