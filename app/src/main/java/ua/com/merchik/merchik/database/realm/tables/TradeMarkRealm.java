package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB;
import ua.com.merchik.merchik.database.room.repository.ReferenceDictionaryRepository;

public class TradeMarkRealm {

    public static TradeMarkDB getTradeMarkRowById(String id) {
        return ReferenceDictionaryRepository.getTradeMarkById(id);
    }

    public static List<TradeMarkDB> getTradeMarkByIds(String[] ids) {
        return ReferenceDictionaryRepository.getTradeMarksByIds(ids);
    }

    public static List<TradeMarkDB> getAll() {
        return ReferenceDictionaryRepository.getTradeMarks();
    }
}
