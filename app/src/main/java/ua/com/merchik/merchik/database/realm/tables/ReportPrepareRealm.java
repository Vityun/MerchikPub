package ua.com.merchik.merchik.database.realm.tables;

import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

public class ReportPrepareRealm {

    public static void setAll(List<ReportPrepareDB> data){
        INSTANCE.beginTransaction();
        INSTANCE.copyToRealmOrUpdate(data);
        INSTANCE.commitTransaction();
    }

    public static RealmResults<ReportPrepareDB> getAll(){
        return INSTANCE.where(ReportPrepareDB.class)
                .findAll();
    }

    public static RealmResults<ReportPrepareDB> getByIds(Long[] id){
        return INSTANCE.where(ReportPrepareDB.class)
                .in("iD", id)
                .findAll();
    }

    public static RealmResults<ReportPrepareDB> getReportPrepareByDad2(long dad2){
        return INSTANCE.where(ReportPrepareDB.class)
                .equalTo("codeDad2", String.valueOf(dad2))
                .findAll();
    }

    public static RealmResults<ReportPrepareDB> getRPLastChange(String clientId, int addrId, long dtChange){
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



}
