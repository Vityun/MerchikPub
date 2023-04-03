package ua.com.merchik.merchik.database.realm.tables;

import static ua.com.merchik.merchik.database.realm.RealmManager.INSTANCE;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import ua.com.merchik.merchik.data.RealmModels.PPADB;
import ua.com.merchik.merchik.data.RealmModels.TovarDB;

public class PPADBRealm {


    /**
     * 03.04.23.
     * Получаю Товары по ППА
     * */
    public static List<TovarDB> getTovarListByPPA(String client_id, int addr_id, String isp) {
        List<TovarDB> res = new ArrayList<>();

        RealmResults<PPADB> data = INSTANCE.where(PPADB.class).findAll();

        if (client_id != null && !client_id.equals("")){
            data = data.where().equalTo("client", client_id).findAll();
        }

        String addrId = String.valueOf(addr_id);
        if (addrId != null && !addrId.equals("")){
            data = data.where().equalTo("addrId", addrId).findAll();
        }

        if (isp != null && !isp.equals("")){
            data = data.where().equalTo("isp", isp).findAll();
        }

        ArrayList<String> listRpTovId = new ArrayList<>();
        if (data != null && data.size() > 0){
            String[] list = new String[data.size()];
            for (int i = 0; i < data.size(); i++) {
                list[i] = data.get(i).getTovarId();
                listRpTovId.add(data.get(i).getTovarId());
            }

            RealmResults<TovarDB> realmResults2 = INSTANCE.where(TovarDB.class)
                    .in("iD", list)
                    .findAll();

            if (realmResults2 != null && realmResults2.size() > 0){
                res = INSTANCE.copyFromRealm(realmResults2);
            }
        }
        return res;
    }

}
