package ua.com.merchik.merchik.data.RealmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UsersDB extends RealmObject {

    @PrimaryKey
    private Integer id;
    private String nm;
    private Long vpi;
    private Integer author;
    private Integer city;
    private String inn;
    private Integer work_address;
    private String work_firm;

    public UsersDB() {
    }

    public UsersDB(Integer id, String nm, Long vpi, Integer author, Integer city, String inn, Integer work_address, String work_firm) {
        this.id = id;
        this.nm = nm;
        this.vpi = vpi;
        this.author = author;
        this.city = city;
        this.inn = inn;
        this.work_address = work_address;
        this.work_firm = work_firm;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Integer getCity() {
        return city;
    }

    public void setCity(Integer city) {
        this.city = city;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public Integer getWork_address() {
        return work_address;
    }

    public void setWork_address(Integer work_address) {
        this.work_address = work_address;
    }

    public String getWork_firm() {
        return work_firm;
    }

    public void setWork_firm(String work_firm) {
        this.work_firm = work_firm;
    }
}
