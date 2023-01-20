package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;
import static ua.com.merchik.merchik.database.room.RoomManager.SQL_DB;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.Database.Room.TovarGroupSDB;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;
import ua.com.merchik.merchik.database.realm.RealmManager;

public class ReportPrepareRealm {

    public static void setAll(List<ReportPrepareDB> data) {
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }

    public static RealmResults<ReportPrepareDB> getAll() {
        return INSTANCE.where(ReportPrepareDB.class)
                .findAll();
    }

    public static RealmResults<ReportPrepareDB> getByIds(Long[] id) {
        return INSTANCE.where(ReportPrepareDB.class)
                .in("iD", id)
                .findAll();
    }

    public static RealmResults<ReportPrepareDB> getReportPrepareByDad2(long dad2) {
        return INSTANCE.where(ReportPrepareDB.class)
                .equalTo("codeDad2", String.valueOf(dad2))
                .findAll();
    }

    public static RealmResults<ReportPrepareDB> getRPLastChange(String clientId, int addrId, long dtChange) {
        return INSTANCE.where(ReportPrepareDB.class)
                .equalTo("kli", clientId)
                .equalTo("addrId", addrId)
                .greaterThan("dtChange", dtChange)
                .findAll();
    }

    public static ReportPrepareDB getReportPrepareByTov(String dad2, String tovarId) {
        return INSTANCE.where(ReportPrepareDB.class)
                .equalTo("tovarId", tovarId)
                .and()
                .equalTo("codeDad2", dad2)
                .findFirst();
    }


    public static List<ReportPrepareDB> joinWithTovarsAndTovGroups(List<ReportPrepareDB> reportPrepareDBList) {
        List<String> tovarIdsList = new ArrayList<>();
        for (ReportPrepareDB item : reportPrepareDBList) {
            tovarIdsList.add(item.tovarId); // Собираем для JOIN-а идентификаторы Товаров

            item.colSKU = Integer.parseInt(item.face) > 0 ? 1 : 0;  // Расчитываем заодно и СКЮ
        }

        String[] tovIds = tovarIdsList.toArray(new String[0]);  // Получаю идентификаторы Товаров для запроса
        List<TovarDB> tovarDBList = RealmManager.INSTANCE.copyFromRealm(TovarRealm.getByIds(tovIds));

        List<Integer> tovarGrpIdsList = new ArrayList<>();
        for (ReportPrepareDB item : reportPrepareDBList) {
            for (TovarDB tovItem : tovarDBList) {
                int id1 = Integer.parseInt(item.tovarId);
                int id2 = Integer.parseInt(tovItem.getiD());
                if (id1 == id2) {
                    item.tovarDB = tovItem;
                    item.shelfSpaceLength = (int) (tovItem.width * Integer.parseInt(item.face) / 1000);
                    tovarGrpIdsList.add(Integer.valueOf(tovItem.getGroupId()));
                    break;
                }
            }
        }

        List<TovarGroupSDB> tovarGroupSDBList = SQL_DB.tovarGroupDao().getAllByIds(tovarGrpIdsList);
        for (ReportPrepareDB item : reportPrepareDBList) {
            for (TovarGroupSDB tovGrpItem : tovarGroupSDBList) {
                if (item.tovarDB != null) {
                    int id1 = Integer.parseInt(item.tovarDB.getGroupId());
                    int id2 = tovGrpItem.id;
                    if (id1 == id2){
                        item.tovarGroupSDB = tovGrpItem;
                        break;
                    }
                }
            }
        }

        return reportPrepareDBList;
    }


}
