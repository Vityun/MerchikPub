package ua.com.merchik.merchik.data.RealmModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class AddressDB extends RealmObject {

    @PrimaryKey
    @SerializedName("addr_id")
    @Expose
    private Integer addrId;
    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("dt_update")
    @Expose
    private Long dtUpdate;
    @SerializedName("city_id")
    @Expose
    private Integer cityId;
    @SerializedName("tp_id")
    @Expose
    private Integer tpId;
    @SerializedName("obl_id")
    @Expose
    private Integer oblId;
    @SerializedName("tt_id")
    @Expose
    private Integer ttId;


    public AddressDB() {
    }

    public AddressDB(Integer id, String nm, Long vpi, Integer author, Integer city) {
        this.addrId = id;
        this.nm = nm;
        this.dtUpdate = vpi;
        this.cityId = city;
    }

    public Integer getAddrId() {
        return addrId;
    }

    public void setAddrId(Integer addrId) {
        this.addrId = addrId;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public Long getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(Long dtUpdate) {
        this.dtUpdate = dtUpdate;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public Integer getTpId() {
        return tpId;
    }

    public void setTpId(Integer tpId) {
        this.tpId = tpId;
    }

    public Integer getOblId() {
        return oblId;
    }

    public void setOblId(Integer oblId) {
        this.oblId = oblId;
    }

    public Integer getTtId() {
        return ttId;
    }

    public void setTtId(Integer ttId) {
        this.ttId = ttId;
    }

    @Override
    public String toString() {
        return nm;
    }
}
