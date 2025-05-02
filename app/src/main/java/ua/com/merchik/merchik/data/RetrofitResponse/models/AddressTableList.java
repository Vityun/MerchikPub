package ua.com.merchik.merchik.data.RetrofitResponse.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddressTableList {

    @SerializedName("addr_id")
    @Expose
    private Integer addrId;
    @SerializedName("nm")
    @Expose
    private String nm;
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
    @SerializedName("dt_update")
    @Expose
    private Long dtUpdate;


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

    public Long getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(Long dtUpdate) {
        this.dtUpdate = dtUpdate;
    }
}
