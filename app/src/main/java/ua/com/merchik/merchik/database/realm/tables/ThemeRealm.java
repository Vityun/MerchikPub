package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.database.room.repository.ReferenceDictionaryRepository;

public class ThemeRealm {

    public static void setThemeDBTable(List<ThemeDB> data) {
        ReferenceDictionaryRepository.upsertThemes(data);
    }

    public static List<ThemeDB> getAll() {
        return ReferenceDictionaryRepository.getThemes();
    }

    public static List<ThemeDB> getAllOpros() {
        return ReferenceDictionaryRepository.getOprosThemes();
    }

    public static ThemeDB getThemeById(String id) {
        return ReferenceDictionaryRepository.getThemeById(id);
    }

    public static List<ThemeDB> getThemeByIds(String[] ids) {
        return ReferenceDictionaryRepository.getThemeByIds(ids);
    }

    public static List<ThemeDB> getTARTheme() {
        return ReferenceDictionaryRepository.getThemes();
    }
}
