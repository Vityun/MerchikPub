package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.UsersDB;
import ua.com.merchik.merchik.database.room.repository.ReferenceDictionaryRepository;

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
        ReferenceDictionaryRepository.upsertUsers(data);
    }

    public static List<UsersDB> getAll(){
        return ReferenceDictionaryRepository.getUsers();
    }


    /**
     * 17.03.2021
     * Получение строки из адресов по ID
     * */
    public static UsersDB getUsersDBById(int id){
        return ReferenceDictionaryRepository.getUserById(id);
    }
}
