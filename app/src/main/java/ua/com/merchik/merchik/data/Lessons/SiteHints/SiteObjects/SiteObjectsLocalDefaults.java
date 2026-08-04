package ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects;

import ua.com.merchik.merchik.Globals;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class SiteObjectsLocalDefaults {

    private static final int PLAN_DURATION_ID = 100998;
    private static final int FACT_DURATION_ID = 100999;

    private static final String PLAN_DURATION_NAME = "Тривалість планова";
    private static final String FACT_DURATION_NAME = "Тривалість фактична";

    public static void ensureDurationObjects() {
        ensureRealmObjects();
    }

    private static void ensureRealmObjects() {
        try {
            if (RealmManager.INSTANCE == null) {
                return;
            }

            RealmManager.INSTANCE.executeTransaction(realm -> {
                addRealmObjectIfMissing(PLAN_DURATION_ID, PLAN_DURATION_NAME);
                addRealmObjectIfMissing(FACT_DURATION_ID, FACT_DURATION_NAME);
            });
        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "SiteObjectsLocalDefaults/ensureRealmObjects",
                    "Exception: " + e
            );
        }
    }

    private static void addRealmObjectIfMissing(int id, String name) {
        SiteObjectsDB existingObject = RealmManager.INSTANCE
                .where(SiteObjectsDB.class)
                .equalTo("id", id)
                .findFirst();

        if (existingObject == null) {
            RealmManager.INSTANCE.copyToRealmOrUpdate(createRealmObject(id, name));
        }
    }

    private static SiteObjectsDB createRealmObject(int id, String name) {
        SiteObjectsDB object = new SiteObjectsDB();
        object.setID(id);
        object.setNm(name);
        object.setComments(name);
        object.setNmTranslation(name);
        object.setCommentsTranslation(name);
        object.setLangId(String.valueOf(Globals.langId));
        object.setDtChange(String.valueOf(System.currentTimeMillis() / 1000L));
        return object;
    }
}
