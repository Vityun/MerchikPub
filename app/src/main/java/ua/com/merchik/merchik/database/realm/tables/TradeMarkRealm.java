package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB;

public class TradeMarkRealm {

    public static TradeMarkDB getTradeMarkRowById(String id) {
        TradeMarkDB tradeMarkDB = INSTANCE.where(TradeMarkDB.class)
                .equalTo("iD", id)
                .findFirst();
        if (tradeMarkDB != null) tradeMarkDB = INSTANCE.copyFromRealm(tradeMarkDB);
        return tradeMarkDB;
    }

    public static List<TradeMarkDB> getTradeMarkByIds(String[] ids){
        List<TradeMarkDB> tradeMarkDB = INSTANCE.where(TradeMarkDB.class)
                .in("iD", ids)
                .findAll();
        if (tradeMarkDB != null) tradeMarkDB = INSTANCE.copyFromRealm(tradeMarkDB);
        return tradeMarkDB;
    }
}
