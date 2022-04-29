package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import ua.com.merchik.merchik.data.RealmModels.OptionsDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

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



}
