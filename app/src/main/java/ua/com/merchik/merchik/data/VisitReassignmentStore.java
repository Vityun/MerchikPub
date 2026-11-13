package ua.com.merchik.merchik.data;

import android.content.Context;
import android.content.SharedPreferences;

import ua.com.merchik.merchik.Activities.MyApplication;

/** Retains the reason for a missing visit, including links from old cached cards. */
public final class VisitReassignmentStore {
    private static VisitReassignmentStore instance;
    private final SharedPreferences preferences;

    VisitReassignmentStore(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public static synchronized VisitReassignmentStore get() {
        if (instance == null) {
            instance = new VisitReassignmentStore(MyApplication.getAppContext()
                    .getSharedPreferences("reassigned_visits", Context.MODE_PRIVATE));
        }
        return instance;
    }

    public synchronized void record(int user, long dad2, long localId) {
        if (user <= 0 || dad2 <= 0) return;
        String key = dad2Key(user, dad2);
        long previousId = preferences.getLong(key, 0);
        long id = localId != 0 ? localId : previousId;
        if (preferences.contains(key) && previousId == id
                && (id == 0 || preferences.getLong(idKey(user, id), 0) == dad2)) return;
        SharedPreferences.Editor editor = preferences.edit();
        if (previousId != 0 && previousId != id
                && preferences.getLong(idKey(user, previousId), 0) == dad2) {
            editor.remove(idKey(user, previousId));
        }
        editor.putLong(key, id);
        if (id != 0) editor.putLong(idKey(user, id), dad2);
        editor.apply();
    }

    public synchronized void clear(int user, long dad2) {
        if (user <= 0 || dad2 <= 0) return;
        String key = dad2Key(user, dad2);
        long id = preferences.getLong(key, 0);
        SharedPreferences.Editor editor = preferences.edit().remove(key).remove(notifiedKey(user, dad2));
        if (id != 0 && preferences.getLong(idKey(user, id), 0) == dad2) {
            editor.remove(idKey(user, id));
        }
        editor.apply();
    }

    public boolean wasReassigned(int user, long id, long dad2) {
        if (user <= 0) return false;
        if (dad2 > 0) return preferences.contains(dad2Key(user, dad2));
        return id != 0 && preferences.getLong(idKey(user, id), 0) > 0;
    }

    /** Called only when the notice is about to be displayed, never during silent deletion. */
    public synchronized boolean claimNotification(int user, long id, long dad2) {
        if (!wasReassigned(user, id, dad2)) return false;
        long visitDad2 = dad2 > 0 ? dad2 : preferences.getLong(idKey(user, id), 0);
        String key = notifiedKey(user, visitDad2);
        if (preferences.contains(key)) return false;
        preferences.edit().putLong(key, 1).apply();
        return true;
    }

    public static boolean isVisitChange(String key, int user) {
        return user > 0 && key != null && key.startsWith(user + ":dad2:");
    }

    public void addListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void removeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    private static String dad2Key(int user, long dad2) {
        return user + ":dad2:" + dad2;
    }

    private static String idKey(int user, long id) {
        return user + ":id:" + id;
    }

    private static String notifiedKey(int user, long dad2) {
        return user + ":notified:" + dad2;
    }
}
