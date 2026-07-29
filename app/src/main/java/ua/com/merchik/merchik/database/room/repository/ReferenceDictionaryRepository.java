package ua.com.merchik.merchik.database.room.repository;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.merchik.merchik.data.Database.Room.AddressSDB;
import ua.com.merchik.merchik.data.Database.Room.CustomerSDB;
import ua.com.merchik.merchik.data.Database.Room.ImagesTypeListSDB;
import ua.com.merchik.merchik.data.Database.Room.ThemeSDB;
import ua.com.merchik.merchik.data.Database.Room.TradeMarkSDB;
import ua.com.merchik.merchik.data.Database.Room.UsersSDB;
import ua.com.merchik.merchik.data.RealmModels.AddressDB;
import ua.com.merchik.merchik.data.RealmModels.CustomerDB;
import ua.com.merchik.merchik.data.RealmModels.ImagesTypeListDB;
import ua.com.merchik.merchik.data.RealmModels.ThemeDB;
import ua.com.merchik.merchik.data.RealmModels.TradeMarkDB;
import ua.com.merchik.merchik.data.RealmModels.UsersDB;
import ua.com.merchik.merchik.database.realm.RealmManager;
import ua.com.merchik.merchik.database.room.RoomManager;

public final class ReferenceDictionaryRepository {

    private static final String TAG = "ReferenceRoom";

    private ReferenceDictionaryRepository() {
    }

    public static void migrateFromRealmIfNeeded() {
        if (RoomManager.SQL_DB == null || RealmManager.INSTANCE == null) {
            return;
        }

        try {
            if (RoomManager.SQL_DB.themeDao().getCount() == 0) {
                upsertThemes(RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.INSTANCE.where(ThemeDB.class).findAll()
                ));
            }
            if (RoomManager.SQL_DB.tradeMarkDao().getCount() == 0) {
                upsertTradeMarks(RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.INSTANCE.where(TradeMarkDB.class).findAll()
                ));
            }
            if (RoomManager.SQL_DB.imagesTypeListDao().getCount() == 0) {
                upsertImageTypes(RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.INSTANCE.where(ImagesTypeListDB.class).findAll()
                ));
            }
            if (RoomManager.SQL_DB.addressDao().getCount() == 0) {
                upsertAddresses(RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.INSTANCE.where(AddressDB.class).findAll()
                ));
            }
            if (RoomManager.SQL_DB.customerDao().getCount() == 0) {
                upsertCustomers(RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.INSTANCE.where(CustomerDB.class).findAll()
                ));
            }
            if (RoomManager.SQL_DB.usersDao().getCount() == 0) {
                upsertUsers(RealmManager.INSTANCE.copyFromRealm(
                        RealmManager.INSTANCE.where(UsersDB.class).findAll()
                ));
            }
        } catch (Exception e) {
            Log.e(TAG, "Realm to Room migration failed", e);
        }
    }

    public static void upsertThemes(List<ThemeDB> data) {
        if (RoomManager.SQL_DB == null || data == null || data.isEmpty()) {
            return;
        }
        List<ThemeSDB> rows = new ArrayList<>();
        for (ThemeDB item : data) {
            ThemeSDB row = toRoom(item);
            if (row != null) {
                rows.add(row);
            }
        }
        if (!rows.isEmpty()) {
            RoomManager.SQL_DB.themeDao().insertAll(rows);
        }
    }

    public static List<ThemeDB> getThemes() {
        if (RoomManager.SQL_DB == null) {
            return Collections.emptyList();
        }
        List<ThemeDB> result = new ArrayList<>();
        for (ThemeSDB row : RoomManager.SQL_DB.themeDao().getAll()) {
            result.add(toLegacy(row));
        }
        return result;
    }

    public static List<ThemeDB> getOprosThemes() {
        if (RoomManager.SQL_DB == null) {
            return Collections.emptyList();
        }
        List<ThemeDB> result = new ArrayList<>();
        for (ThemeSDB row : RoomManager.SQL_DB.themeDao().getAllOpros()) {
            result.add(toLegacy(row));
        }
        return result;
    }

    public static ThemeDB getThemeById(String id) {
        if (RoomManager.SQL_DB == null || id == null) {
            return null;
        }
        ThemeSDB row = RoomManager.SQL_DB.themeDao().getById(id);
        return row == null ? null : toLegacy(row);
    }

    public static List<ThemeDB> getThemeByIds(String[] ids) {
        String[] safeIds = sanitizeIds(ids);
        if (RoomManager.SQL_DB == null || safeIds.length == 0) {
            return Collections.emptyList();
        }
        List<ThemeDB> result = new ArrayList<>();
        for (ThemeSDB row : RoomManager.SQL_DB.themeDao().getByIds(safeIds)) {
            result.add(toLegacy(row));
        }
        return result;
    }

    public static void upsertTradeMarks(List<TradeMarkDB> data) {
        if (RoomManager.SQL_DB == null || data == null || data.isEmpty()) {
            return;
        }
        List<TradeMarkSDB> rows = new ArrayList<>();
        for (TradeMarkDB item : data) {
            TradeMarkSDB row = toRoom(item);
            if (row != null) {
                rows.add(row);
            }
        }
        if (!rows.isEmpty()) {
            RoomManager.SQL_DB.tradeMarkDao().insertAll(rows);
        }
    }

    public static TradeMarkDB getTradeMarkById(String id) {
        if (RoomManager.SQL_DB == null || id == null) {
            return null;
        }
        TradeMarkSDB row = RoomManager.SQL_DB.tradeMarkDao().getById(id);
        return row == null ? null : toLegacy(row);
    }

    public static List<TradeMarkDB> getTradeMarksByIds(String[] ids) {
        String[] safeIds = sanitizeIds(ids);
        if (RoomManager.SQL_DB == null || safeIds.length == 0) {
            return Collections.emptyList();
        }
        List<TradeMarkDB> result = new ArrayList<>();
        for (TradeMarkSDB row : RoomManager.SQL_DB.tradeMarkDao().getByIds(safeIds)) {
            result.add(toLegacy(row));
        }
        return result;
    }

    public static List<TradeMarkDB> getTradeMarks() {
        if (RoomManager.SQL_DB == null) {
            return Collections.emptyList();
        }
        List<TradeMarkDB> result = new ArrayList<>();
        for (TradeMarkSDB row : RoomManager.SQL_DB.tradeMarkDao().getAll()) {
            result.add(toLegacy(row));
        }
        return result;
    }

    public static void upsertImageTypes(List<ImagesTypeListDB> data) {
        if (RoomManager.SQL_DB == null || data == null || data.isEmpty()) {
            return;
        }
        List<ImagesTypeListSDB> rows = new ArrayList<>();
        for (ImagesTypeListDB item : data) {
            ImagesTypeListSDB row = toRoom(item);
            if (row != null) {
                rows.add(row);
            }
        }
        if (!rows.isEmpty()) {
            RoomManager.SQL_DB.imagesTypeListDao().insertAll(rows);
        }
    }

    public static List<ImagesTypeListDB> getImageTypes() {
        if (RoomManager.SQL_DB == null) {
            return Collections.emptyList();
        }
        List<ImagesTypeListDB> result = new ArrayList<>();
        for (ImagesTypeListSDB row : RoomManager.SQL_DB.imagesTypeListDao().getAll()) {
            result.add(toLegacy(row));
        }
        return result;
    }

    public static ImagesTypeListDB getImageTypeById(int id) {
        if (RoomManager.SQL_DB == null) {
            return null;
        }
        ImagesTypeListSDB row = RoomManager.SQL_DB.imagesTypeListDao().getById(id);
        return row == null ? null : toLegacy(row);
    }

    public static void upsertAddresses(List<AddressDB> data) {
        if (RoomManager.SQL_DB == null || data == null || data.isEmpty()) {
            return;
        }
        List<AddressSDB> rows = new ArrayList<>();
        for (AddressDB item : data) {
            AddressSDB row = toRoom(item);
            if (row != null) {
                rows.add(row);
            }
        }
        if (!rows.isEmpty()) {
            RoomManager.SQL_DB.addressDao().insertAll(rows);
        }
    }

    public static AddressDB getAddressById(int id) {
        if (RoomManager.SQL_DB == null) {
            return null;
        }
        AddressSDB row = RoomManager.SQL_DB.addressDao().getById(id);
        return row == null ? null : toLegacy(row);
    }

    public static List<AddressDB> getAddresses() {
        if (RoomManager.SQL_DB == null) {
            return Collections.emptyList();
        }
        List<AddressDB> result = new ArrayList<>();
        for (AddressSDB row : RoomManager.SQL_DB.addressDao().getAll()) {
            result.add(toLegacy(row));
        }
        return result;
    }

    public static void upsertCustomers(List<CustomerDB> data) {
        if (RoomManager.SQL_DB == null || data == null || data.isEmpty()) {
            return;
        }
        List<CustomerSDB> rows = new ArrayList<>();
        for (CustomerDB item : data) {
            CustomerSDB row = toRoom(item);
            if (row != null) {
                rows.add(row);
            }
        }
        if (!rows.isEmpty()) {
            RoomManager.SQL_DB.customerDao().insertAll(rows);
        }
    }

    public static CustomerDB getCustomerById(String id) {
        if (RoomManager.SQL_DB == null || id == null) {
            return null;
        }
        CustomerSDB row = RoomManager.SQL_DB.customerDao().getById(id);
        return row == null ? null : toLegacy(row);
    }

    public static CustomerDB getCustomerByNm(String nm) {
        if (RoomManager.SQL_DB == null || nm == null) {
            return null;
        }
        CustomerSDB row = RoomManager.SQL_DB.customerDao().getByNm(nm);
        return row == null ? null : toLegacy(row);
    }

    public static List<CustomerDB> getCustomers() {
        if (RoomManager.SQL_DB == null) {
            return Collections.emptyList();
        }
        List<CustomerDB> result = new ArrayList<>();
        for (CustomerSDB row : RoomManager.SQL_DB.customerDao().getAll()) {
            result.add(toLegacy(row));
        }
        return result;
    }

    public static void upsertUsers(List<UsersDB> data) {
        if (RoomManager.SQL_DB == null || data == null || data.isEmpty()) {
            return;
        }
        List<UsersSDB> rows = new ArrayList<>();
        for (UsersDB item : data) {
            UsersSDB row = toRoom(item);
            if (row != null) {
                rows.add(row);
            }
        }
        if (!rows.isEmpty()) {
            RoomManager.SQL_DB.usersDao().insertAll(rows);
        }
    }

    public static UsersDB getUserById(int id) {
        if (RoomManager.SQL_DB == null) {
            return null;
        }
        UsersSDB row = RoomManager.SQL_DB.usersDao().getById(id);
        return row == null ? null : toLegacy(row);
    }

    public static List<UsersDB> getUsers() {
        if (RoomManager.SQL_DB == null) {
            return Collections.emptyList();
        }
        List<UsersDB> result = new ArrayList<>();
        for (UsersSDB row : RoomManager.SQL_DB.usersDao().getAll2()) {
            result.add(toLegacy(row));
        }
        return result;
    }

    private static ThemeSDB toRoom(ThemeDB item) {
        if (item == null || item.getID() == null || item.getID().trim().isEmpty()) {
            return null;
        }
        ThemeSDB row = new ThemeSDB();
        row.id = item.getID();
        row.nm = item.getNm();
        row.comment = item.getComment();
        row.grpId = item.getGrpId();
        row.tp = item.getTp();
        row.needPhoto = item.need_photo;
        row.needReport = item.need_report;
        row.dtUpdate = item.getDtUpdate();
        row.oprosTheme = item.getOprosTheme();
        row.audioFilter = item.getAudioFilter();
        return row;
    }

    private static ThemeDB toLegacy(ThemeSDB row) {
        ThemeDB item = new ThemeDB();
        item.setID(row.id);
        item.setNm(row.nm);
        item.setComment(row.comment);
        item.setGrpId(row.grpId);
        item.setTp(row.tp);
        item.need_photo = row.needPhoto;
        item.need_report = row.needReport;
        item.setDtUpdate(row.dtUpdate);
        item.setOprosTheme(row.oprosTheme);
        item.setAudioFilter(row.audioFilter);
        return item;
    }

    private static TradeMarkSDB toRoom(TradeMarkDB item) {
        if (item == null || item.getID() == null || item.getID().trim().isEmpty()) {
            return null;
        }
        TradeMarkSDB row = new TradeMarkSDB();
        row.id = item.getID();
        row.nm = item.getNm();
        row.dtUpdate = item.getDtUpdate();
        row.sortType = item.getSortType();
        return row;
    }

    private static TradeMarkDB toLegacy(TradeMarkSDB row) {
        TradeMarkDB item = new TradeMarkDB();
        item.setID(row.id);
        item.setNm(row.nm);
        item.setDtUpdate(row.dtUpdate);
        item.setSortType(row.sortType);
        return item;
    }

    private static ImagesTypeListSDB toRoom(ImagesTypeListDB item) {
        if (item == null || item.getId() == null) {
            return null;
        }
        ImagesTypeListSDB row = new ImagesTypeListSDB();
        row.id = item.getId();
        row.nm = item.getNm();
        return row;
    }

    private static ImagesTypeListDB toLegacy(ImagesTypeListSDB row) {
        ImagesTypeListDB item = new ImagesTypeListDB();
        item.setId(row.id);
        item.setNm(row.nm);
        return item;
    }

    private static AddressSDB toRoom(AddressDB item) {
        if (item == null || item.getAddrId() == null) {
            return null;
        }
        AddressSDB row = RoomManager.SQL_DB.addressDao().getById(item.getAddrId());
        if (row == null) {
            row = new AddressSDB();
            row.id = item.getAddrId();
        }
        row.nm = item.getNm();
        row.dtUpdate = item.getDtUpdate();
        row.cityId = item.getCityId();
        row.tpId = item.getTpId();
        row.oblId = item.getOblId();
        row.ttId = item.getTtId();
        return row;
    }

    private static AddressDB toLegacy(AddressSDB row) {
        AddressDB item = new AddressDB();
        item.setAddrId(row.id);
        item.setNm(row.nm);
        item.setDtUpdate(row.dtUpdate);
        item.setCityId(row.cityId);
        item.setTpId(row.tpId);
        item.setOblId(row.oblId);
        item.setTtId(row.ttId);
        return item;
    }

    private static CustomerSDB toRoom(CustomerDB item) {
        if (item == null || item.getId() == null || item.getId().trim().isEmpty()) {
            return null;
        }
        CustomerSDB row = RoomManager.SQL_DB.customerDao().getById(item.getId());
        if (row == null) {
            row = new CustomerSDB();
            row.id = item.getId();
        }
        row.nm = item.getNm();
        row.edrpou = item.getEdrpou();
        row.dtUpdate = item.getVpi();
        return row;
    }

    private static CustomerDB toLegacy(CustomerSDB row) {
        CustomerDB item = new CustomerDB();
        item.setId(row.id);
        item.setNm(row.nm);
        item.setEdrpou(row.edrpou);
        item.setVpi(row.dtUpdate);
        return item;
    }

    private static UsersSDB toRoom(UsersDB item) {
        if (item == null || item.getId() == null) {
            return null;
        }
        UsersSDB row = RoomManager.SQL_DB.usersDao().getById(item.getId());
        if (row == null) {
            row = new UsersSDB();
            row.id = item.getId();
        }
        row.fio = item.getNm();
        row.dtUpdate = item.getVpi();
        row.authorId = item.getAuthor();
        row.cityId = item.getCity();
        row.inn = item.getInn();
        row.workAddrId = item.getWork_address();
        row.clientId = item.getWork_firm();
        return row;
    }

    private static UsersDB toLegacy(UsersSDB row) {
        UsersDB item = new UsersDB();
        item.setId(row.id);
        item.setNm(row.fio);
        item.setVpi(row.dtUpdate);
        item.setAuthor(row.authorId);
        item.setCity(row.cityId);
        item.setInn(row.inn);
        item.setWork_address(row.workAddrId);
        item.setWork_firm(row.clientId);
        return item;
    }

    private static String[] sanitizeIds(String[] ids) {
        if (ids == null || ids.length == 0) {
            return new String[0];
        }
        List<String> result = new ArrayList<>();
        for (String id : ids) {
            if (id != null && !id.trim().isEmpty()) {
                result.add(id.trim());
            }
        }
        return result.toArray(new String[0]);
    }
}
