package ua.com.merchik.merchik.database.realm.tables;

import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class TradeMarkRealm {

    public static TradeMarkDB getTradeMarkRowById(String id) {
        return INSTANCE.where(TradeMarkDB.class)
                .equalTo("iD", id)
                .findFirst();
    }
}
