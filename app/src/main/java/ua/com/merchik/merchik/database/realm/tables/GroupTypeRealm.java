package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import ua.com.merchik.merchik.data.RealmModels.GroupTypeDB;

public class GroupTypeRealm {
    public static GroupTypeDB getGroupTypeById(int id) {
        GroupTypeDB groupTypeDB = INSTANCE.where(GroupTypeDB.class)
                .equalTo("ID", id)
                .findFirst();

        if (groupTypeDB != null) groupTypeDB = INSTANCE.copyFromRealm(groupTypeDB);

        return groupTypeDB;
    }
}
