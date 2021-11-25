package ua.com.merchik.merchik.data.RealmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ArticleDB extends RealmObject {

    @PrimaryKey
    private String iD;
    private String vendorCode;
    private String tovarId;
    private String addrTpId;
    private String dtUpdate;

    public ArticleDB() {
    }
}
