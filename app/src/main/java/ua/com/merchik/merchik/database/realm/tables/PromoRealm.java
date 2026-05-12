package ua.com.merchik.merchik.database.realm.tables;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.RealmModels.PromoDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import java.util.List;

public class PromoRealm {
    public static RealmResults<PromoDB> getAllPromoDB() {
        return INSTANCE.where(PromoDB.class)
                .findAll();
    }

    public static List<PromoDB> getAllPromoDBList() {
        List<PromoDB> result = INSTANCE.where(PromoDB.class)
                .findAll();
        if (result != null) result = INSTANCE.copyFromRealm(result);
        return result;
    }

    public static PromoDB getPromoDBByNm(String nm) {
        return INSTANCE.where(PromoDB.class)
                .equalTo("nm", nm)
                .findFirst();
    }

    public static PromoDB getPromoDBById(String id) {
        PromoDB result = INSTANCE.where(PromoDB.class)
                .equalTo("ID", id)
                .findFirst();
        if (result != null) result = INSTANCE.copyFromRealm(result);
        return result;
    }
}
