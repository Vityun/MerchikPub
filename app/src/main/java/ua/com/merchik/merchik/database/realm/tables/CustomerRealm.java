package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.CustomerDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

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
        INSTANCE.beginTransaction();
        INSTANCE.delete(CustomerDB.class);
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }

    public static List<CustomerDB> getAllCustomerDB(){
        return INSTANCE.where(CustomerDB.class)
                .findAll();
    }


    /**
     * 17.03.2021
     * Получение строки из адресов по ID
     * */
    public static CustomerDB getCustomerById(String id){
        CustomerDB result = INSTANCE.where(CustomerDB.class)
                .equalTo("id", id)
                .findFirst();
        if (result != null )
            result = INSTANCE.copyFromRealm(result);
        return result;
    }

    public static CustomerDB getCustomerByNm(String nm){
        return INSTANCE.where(CustomerDB.class)
                .equalTo("nm", nm)
                .findFirst();
    }

    public static List<CustomerDB> getAll(){
        List<CustomerDB> result = INSTANCE.where(CustomerDB.class)
                .findAll();
        if (result != null )
            result = INSTANCE.copyFromRealm(result);
        return result;
    }

}
