package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.RealmModels.OptionsDB;

public class OptionsRealm {

    /**
     * 22.03.2021
     * Получение списка Опций
     *
     * @return Возвращает список Опций по предустановленному коду ДАД2
     */
    public static List<OptionsDB> getOptionsByDAD2(String dad2) {
        return INSTANCE.where(OptionsDB.class)
                .equalTo("codeDad2", dad2)
                .findAll();
    }

    public static OptionsDB getOptionByOptionId(String optId){
        return INSTANCE.where(OptionsDB.class)
                .equalTo("optionControlId", optId)
                .findFirst();
    }


    public static List<OptionsDB> getOptionsButtonByDAD2(String dad2) {
        return INSTANCE.where(OptionsDB.class)
                .equalTo("codeDad2", dad2)
                .and()
                .equalTo("optionGroup", "3161")
                .findAll();
    }

    public static List<OptionsDB> getOptionsNOTButtonByDAD2(String dad2) {
        RealmResults<OptionsDB> realmResults = INSTANCE.where(OptionsDB.class)
                .equalTo("codeDad2", dad2)
                .and()
                .notEqualTo("optionGroup", "3161")
                .findAll();

        if (realmResults != null && realmResults.size() > 0){
            return INSTANCE.copyFromRealm(realmResults);
        }else {
            return null;
        }
    }

    public static OptionsDB getOptionControl(String dad2, String optId){
        OptionsDB optionsDB = INSTANCE.where(OptionsDB.class)
                .equalTo("codeDad2", dad2)
                .and()
//                .equalTo("optionId", optId)
//                .or()
                .equalTo("optionControlId", optId)
                .findFirst();

        if (optionsDB != null) return INSTANCE.copyFromRealm(optionsDB);

        return null;
    }

    public static OptionsDB getOption(String dad2, String optId){
        OptionsDB optionsDB = INSTANCE.where(OptionsDB.class)
                .equalTo("codeDad2", dad2)
                .and()
                .equalTo("optionId", optId)
                .findFirst();
        if (optionsDB != null) return INSTANCE.copyFromRealm(optionsDB);
        return null;
    }



}
