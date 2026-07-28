package ua.com.merchik.merchik.database.realm.tables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;
import ua.com.merchik.merchik.database.room.repository.ReferenceDictionaryRepository;

public class PhotoTypeRealm {

    public static List<ImagesTypeListDB> getPhotoType() {
        return ReferenceDictionaryRepository.getImageTypes();
    }

    public static ImagesTypeListDB getPhotoTypeById(int id) {
        return ReferenceDictionaryRepository.getImageTypeById(id);
    }

    public static Map<Integer, String> getPhotoTypeMap() {
        Map<Integer, String> result = new HashMap<>();

        List<ImagesTypeListDB> list = getPhotoType();
        for (ImagesTypeListDB item : list) {
            if (item.getId() != null && item.getNm() != null) {
                result.put(item.getId(), item.getNm());
            }
        }

        return result;
    }
}
