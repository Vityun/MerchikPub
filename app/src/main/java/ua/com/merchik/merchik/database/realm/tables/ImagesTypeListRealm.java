package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;
import ua.com.merchik.merchik.database.room.repository.ReferenceDictionaryRepository;

public class ImagesTypeListRealm {

    public static List<ImagesTypeListDB> getAll() {
        return ReferenceDictionaryRepository.getImageTypes();
    }

    public static ImagesTypeListDB getByID(int id) {
        return ReferenceDictionaryRepository.getImageTypeById(id);
    }
}
