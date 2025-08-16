package ua.com.merchik.merchik.database.room.factory;


import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import ua.com.merchik.merchik.data.Database.Room.WPDataAdditional;
import ua.com.merchik.merchik.data.RealmModels.WpDataDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public final class WPDataAdditionalFactory {

    private WPDataAdditionalFactory() {
    }

    // Генерим очень маловероятный к коллизии отрицательный ID
    public static long newLocalId() {
        return -(System.currentTimeMillis() * 1000L + (long) (Math.random() * 1000L));
    }

    public static WPDataAdditional blankWithDad2(WpDataDB wpDataDB) {
        WPDataAdditional e = new WPDataAdditional();
        e.ID = newLocalId();
        e.dt = System.currentTimeMillis() / 1000;
        e.clientId = Integer.parseInt(wpDataDB.getClient_id());
        e.isp = "";
        e.addrId = wpDataDB.getAddr_id();
        e.codeDad2 = wpDataDB.getCode_dad2();
        e.themeId = wpDataDB.getTheme_id();
        e.userDecision = 1;
        e.confirmDt = 0L;
        e.confirmDecision = 0;
        e.confirmAuto = 0;
        e.comment = null;
        e.kps = 0;
        e.uploadStatus = 1;
        return e;
    }

    public static WPDataAdditional withClientAndAddress(WpDataDB wpDataDB) {
        WPDataAdditional e = new WPDataAdditional();
        e.ID = newLocalId();
        e.dt = System.currentTimeMillis() / 1000;
        e.clientId = Integer.parseInt(wpDataDB.getClient_id());
        e.isp = "";
        e.addrId = wpDataDB.getAddr_id();
        e.codeDad2 = 0L;
        e.themeId = wpDataDB.getTheme_id();
        e.userDecision = 1;
        e.confirmDt = 0L;
        e.confirmDecision = 0;
        e.confirmAuto = 0;
        e.comment = null;
        e.kps = 0;
        e.uploadStatus = 1;
        return e;
    }

    public static List<WPDataAdditional> withAllClientForAddressInfinite(WpDataDB wpDataDB) {
        List<WPDataAdditional> list = new ArrayList<>();
        List<String> clientIds = getUniqueClientIdsForAddr_Fallback(wpDataDB.getAddr_id());
        for (String cl_id : clientIds) {
            WPDataAdditional e = new WPDataAdditional();
            e.ID = newLocalId();
            e.dt = System.currentTimeMillis() / 1000;
            e.clientId = Integer.parseInt(cl_id);
            e.isp = "";
            e.addrId = wpDataDB.getAddr_id();
            e.codeDad2 = 0L;
            e.themeId = wpDataDB.getTheme_id();
            e.userDecision = 1;
            e.confirmDt = 0L;
            e.confirmDecision = 0;
            e.confirmAuto = 0;
            e.comment = null;
            e.kps = 0;
            e.uploadStatus = 1;
            list.add(e);
        }
        return list;
    }

    public static List<WPDataAdditional> withAllClientForAddressOneTime(WpDataDB wpDataDB) {
        List<WPDataAdditional> list = new ArrayList<>();
        List<ClientAndDad2> clientIds = getUniqueClientIdsForAddr_Fallback(wpDataDB.getAddr_id(), wpDataDB.getDt());
        for (ClientAndDad2 cl_dad2 : clientIds) {
            WPDataAdditional e = new WPDataAdditional();
            e.ID = newLocalId();
            e.dt = System.currentTimeMillis() / 1000;
            e.clientId = Integer.parseInt(cl_dad2.clientId);
            e.isp = "";
            e.addrId = wpDataDB.getAddr_id();
            e.codeDad2 = cl_dad2.dad2;
            e.themeId = wpDataDB.getTheme_id();
            e.userDecision = 1;
            e.confirmDt = 0L;
            e.confirmDecision = 0;
            e.confirmAuto = 0;
            e.comment = null;
            e.kps = 0;
            e.uploadStatus = 1;
            list.add(e);
        }
        return list;
    }

    public static List<String> getUniqueClientIdsForAddr_TXT(int addrId) {
        List<WpDataDB> rows = RealmManager.INSTANCE.copyFromRealm(RealmManager.INSTANCE.where(WpDataDB.class)
                .equalTo("addr_id", addrId)
                .equalTo("user_id", 14041)
                .findAll());

        // сохраняем порядок появления и убираем дубли
        Set<String> set = new LinkedHashSet<>();
        for (WpDataDB r : rows) {
            String id = r.getClient_txt();
            if (id != null && !id.isEmpty()) set.add(id);
        }
        return new ArrayList<>(set);
    }

    public static List<String> getUniqueClientIdsForAddr_TXT(int addrId, Date date) {
        List<WpDataDB> rows = RealmManager.INSTANCE.copyFromRealm(RealmManager.INSTANCE.where(WpDataDB.class)
                .equalTo("addr_id", addrId)
                .equalTo("user_id", 14041)
                .equalTo("dt", date)
                .findAll());

        // сохраняем порядок появления и убираем дубли
        Set<String> set = new LinkedHashSet<>();
        for (WpDataDB r : rows) {
            String id = r.getClient_txt();
            if (id != null && !id.isEmpty()) set.add(id);
        }
        return new ArrayList<>(set);
    }

    private static List<String> getUniqueClientIdsForAddr_Fallback(int addrId) {
        Realm realm = Realm.getDefaultInstance();
        List<WpDataDB> rows = realm.copyFromRealm(realm.where(WpDataDB.class)
                .equalTo("addr_id", addrId)
                .equalTo("user_id", 14041)
                .findAll());

        // сохраняем порядок появления и убираем дубли
        Set<String> set = new LinkedHashSet<>();
        for (WpDataDB r : rows) {
            String id = r.getClient_id();
            if (id != null && !id.isEmpty()) set.add(id);
        }
        realm.close();
        return new ArrayList<>(set);
    }

    private static List<ClientAndDad2> getUniqueClientIdsForAddr_Fallback(int addrId, Date date) {
        Realm realm = Realm.getDefaultInstance();
        List<WpDataDB> rows = realm.copyFromRealm(realm.where(WpDataDB.class)
                .equalTo("addr_id", addrId)
                .equalTo("dt", date)
                .equalTo("user_id", 14041)
                .findAll());

        // сохраняем порядок появления и убираем дубли
        Set<ClientAndDad2> set = new LinkedHashSet<>();
        for (WpDataDB r : rows) {
            String id = r.getClient_id();
            Long dad2 = r.getCode_dad2();
            if (id != null && !id.isEmpty()) set.add(new ClientAndDad2(dad2, id));
        }
        realm.close();
        return new ArrayList<>(set);
    }


    private static class ClientAndDad2 {
        long dad2;
        String clientId;

        public ClientAndDad2(Long dad2, String id) {
            this.dad2 = dad2;
            this.clientId = id;
        }
    }

}
