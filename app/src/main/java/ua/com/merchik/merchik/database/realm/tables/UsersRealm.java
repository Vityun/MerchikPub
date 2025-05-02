package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.UsersDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

/**
 * 17.03.2021
 * "ТЗН" Таблици Пользователей
 * */
public class UsersRealm {

    /**
     * 17.03.2021
     *
     */
    public static void setAddressTable(List<UsersDB> data) {
        INSTANCE.beginTransaction();
//        INSTANCE.delete(UsersDB.class);
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }

    public static List<UsersDB> getAll(){
        return INSTANCE.where(UsersDB.class)
                .findAll();
    }


    /**
     * 17.03.2021
     * Получение строки из адресов по ID
     * */
    public static UsersDB getUsersDBById(int id){
        UsersDB result =  INSTANCE.where(UsersDB.class)
                .equalTo("id", id)
                .findFirst();
        if (result != null )
            result = INSTANCE.copyFromRealm(result);
        return result;
    }
}
