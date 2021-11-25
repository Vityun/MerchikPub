package ua.com.merchik.merchik.data.RealmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CustomerDB extends RealmObject {

    @PrimaryKey
    private String id;
    private String nm;
    private Long vpi;
    private Integer author;
    private String edrpou;

    public CustomerDB() {
    }

    public CustomerDB(String id, String nm, Long vpi, Integer author, String edrpou) {
        this.id = id;
        this.nm = nm;
        this.vpi = vpi;
        this.author = author;
        this.edrpou = edrpou;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public Long getVpi() {
        return vpi;
    }

    public void setVpi(Long vpi) {
        this.vpi = vpi;
    }

    public Integer getAuthor() {
        return author;
    }

    public void setAuthor(Integer author) {
        this.author = author;
    }

    public String getEdrpou() {
        return edrpou;
    }

    public void setEdrpou(String edrpou) {
        this.edrpou = edrpou;
    }


    @Override
    public String toString() {
        return nm;
    }
}
