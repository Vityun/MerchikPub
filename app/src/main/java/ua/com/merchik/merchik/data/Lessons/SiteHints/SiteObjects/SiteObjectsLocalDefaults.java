package ua.com.merchik.merchik.data.Lessons.SiteHints.SiteObjects;

import io.realm.Realm;
import io.realm.RealmQuery;
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

    public static boolean hasDownloadedObjects() {
        return RealmManager.INSTANCE != null && downloadedObjects(RealmManager.INSTANCE).count() > 0;
    }

    public static boolean hasDownloadedLanguage(Realm realm, String langId) {
        long count = downloadedObjects(realm).count();
        return count > 0 && downloadedObjects(realm).equalTo("langId", langId).count() == count;
    }

    private static RealmQuery<SiteObjectsDB> downloadedObjects(Realm realm) {
        return realm.where(SiteObjectsDB.class)
                .greaterThan("id", 0)
                .notEqualTo("id", PLAN_DURATION_ID)
                .notEqualTo("id", FACT_DURATION_ID);
    }

    // Called inside the caller's transaction, on the Realm instance owned by that thread.
    public static void ensureDurationObjects(Realm realm, int langId) {
        addRealmObjectIfMissing(realm, PLAN_DURATION_ID, PLAN_DURATION_NAME, langId);
        addRealmObjectIfMissing(realm, FACT_DURATION_ID, FACT_DURATION_NAME, langId);
    }

    private static void ensureRealmObjects() {
        try {
            if (RealmManager.INSTANCE == null) {
                return;
            }

            RealmManager.INSTANCE.executeTransaction(realm -> ensureDurationObjects(realm, Globals.langId));
        } catch (Exception e) {
            Globals.writeToMLOG(
                    "ERROR",
                    "SiteObjectsLocalDefaults/ensureRealmObjects",
                    "Exception: " + e
            );
        }
    }

    private static void addRealmObjectIfMissing(Realm realm, int id, String name, int langId) {
        SiteObjectsDB existingObject = realm
                .where(SiteObjectsDB.class)
                .equalTo("id", id)
                .findFirst();

        if (existingObject == null) {
            realm.copyToRealmOrUpdate(createRealmObject(id, name, langId));
        }
    }

    private static SiteObjectsDB createRealmObject(int id, String name, int langId) {
        SiteObjectsDB object = new SiteObjectsDB();
        object.setID(id);
        object.setNm(name);
        object.setComments(name);
        object.setNmTranslation(name);
        object.setCommentsTranslation(name);
        object.setLangId(String.valueOf(langId));
        object.setDtChange(String.valueOf(System.currentTimeMillis() / 1000L));
        return object;
    }
}
