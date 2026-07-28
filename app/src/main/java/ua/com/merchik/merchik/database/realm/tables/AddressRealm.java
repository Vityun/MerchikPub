package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.AddressDB;
import ua.com.merchik.merchik.database.room.repository.ReferenceDictionaryRepository;


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
        ReferenceDictionaryRepository.upsertAddresses(data);
    }


    /**
     * 17.03.2021
     * Получение строки из адресов по ID
     * */
    public static AddressDB getAddressById(int id){
        return ReferenceDictionaryRepository.getAddressById(id);
    }



    public static List<AddressDB> getAll(){
        return ReferenceDictionaryRepository.getAddresses();
    }


}
