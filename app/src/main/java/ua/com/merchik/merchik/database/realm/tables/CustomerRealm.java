package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.CustomerDB;
import ua.com.merchik.merchik.database.room.repository.ReferenceDictionaryRepository;

/**
 * 17.03.2021
 * "ТЗН" Таблици Клиентов
 * */
public class CustomerRealm {

    /**
     * 17.03.2021
     *
     */
    public static void setAddressTable(List<CustomerDB> data) {
        ReferenceDictionaryRepository.upsertCustomers(data);
    }

    public static List<CustomerDB> getAllCustomerDB(){
        return ReferenceDictionaryRepository.getCustomers();
    }


    /**
     * 17.03.2021
     * Получение строки из адресов по ID
     * */
    public static CustomerDB getCustomerById(String id){
        return ReferenceDictionaryRepository.getCustomerById(id);
    }

    public static CustomerDB getCustomerByNm(String nm){
        return ReferenceDictionaryRepository.getCustomerByNm(nm);
    }

    public static List<CustomerDB> getAll(){
        return ReferenceDictionaryRepository.getCustomers();
    }

}
