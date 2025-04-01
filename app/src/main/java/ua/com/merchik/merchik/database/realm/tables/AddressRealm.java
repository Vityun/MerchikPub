package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.AddressDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;


/**
 * 17.03.2021
 * "ТЗН" Таблици Адресов
 * */
public class AddressRealm {

    /**
     * 17.03.2021
     * Запись в Таблицу Адресов
     */
    public static void setAddressTable(List<AddressDB> data) {
        INSTANCE.beginTransaction();
        INSTANCE.delete(AddressDB.class);
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }


    /**
     * 17.03.2021
     * Получение строки из адресов по ID
     * */
    public static AddressDB getAddressById(int id){
        AddressDB result = INSTANCE.where(AddressDB.class)
                .equalTo("addrId", id)
                .findFirst();
        if (result != null )
            result = INSTANCE.copyFromRealm(result);
        return result;
    }



    public static List<AddressDB> getAll(){
        List<AddressDB> result = INSTANCE.where(AddressDB.class)
                .findAll();
        if (result != null )
            result = INSTANCE.copyFromRealm(result);
        return result;
    }


}
