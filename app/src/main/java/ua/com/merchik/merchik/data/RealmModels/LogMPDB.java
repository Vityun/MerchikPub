package ua.com.merchik.merchik.data.RealmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class LogMPDB extends RealmObject {

    @PrimaryKey
    private int id;
    private String gp;

    public LogMPDB() {
    }

    public LogMPDB(int id, String gp) {
        this.id = id;
        this.gp = gp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGp() {
        return gp;
    }

    public void setGp(String gp) {
        this.gp = gp;
    }
}
