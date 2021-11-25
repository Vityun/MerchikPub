package ua.com.merchik.merchik.data.RealmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GroupTypeDB extends RealmObject {

//    @PrimaryKey
//    private int id;
    private int ID;
    private String nm;
    private int parent;
    private int client_id;

    public GroupTypeDB() {
    }

    public GroupTypeDB(int ID, String nm, int parent, int client_id) {
//        this.id = id;
        this.ID = ID;
        this.nm = nm;
        this.parent = parent;
        this.client_id = client_id;
    }

//    public int getAddrId() {
//        return id;
//    }
//
//    public void setAddrId(int id) {
//        this.id = id;
//    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }
}
