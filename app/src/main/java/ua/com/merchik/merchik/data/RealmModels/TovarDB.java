package ua.com.merchik.merchik.data.RealmModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TovarDB extends RealmObject {
    @SerializedName("ID")
    @Expose
    @PrimaryKey
    private String iD;
    @SerializedName("client_id")
    @Expose
    private String clientId;
    @SerializedName("client_id2")
    @Expose
    private String clientId2;
    @SerializedName("nm")
    @Expose
    private String nm;
    @SerializedName("weight")
    @Expose
    private String weight;
    @SerializedName("weight_gr")
    @Expose
    private String weightGr;
    @SerializedName("group_id")
    @Expose
    private String groupId;
    @SerializedName("manufacturer_id")
    @Expose
    private String manufacturerId;
    private TradeMarkDB manufacturer;
    @SerializedName("barcode")
    @Expose
    private String barcode;
    @SerializedName("related_tovar_id")
    @Expose
    private String relatedTovarId;
    @SerializedName("dt_update")
    @Expose
    private String dtUpdate;

    @SerializedName("sortcol")
    @Expose
    private String sortcol;

    @SerializedName("photo_id")
    @Expose
    public String photoId;


    public TovarDB() {
    }


    public String getiD() {
        return iD;
    }

    public void setiD(String iD) {
        this.iD = iD;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId2() {
        return clientId2;
    }

    public void setClientId2(String clientId2) {
        this.clientId2 = clientId2;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getWeightGr() {
        return weightGr;
    }

    public void setWeightGr(String weightGr) {
        this.weightGr = weightGr;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(String manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public TradeMarkDB getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(TradeMarkDB manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getRelatedTovarId() {
        return relatedTovarId;
    }

    public void setRelatedTovarId(String relatedTovarId) {
        this.relatedTovarId = relatedTovarId;
    }

    public String getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(String dtUpdate) {
        this.dtUpdate = dtUpdate;
    }

    public String getSortcol() {
        return sortcol;
    }

    public void setSortcol(String sortcol) {
        this.sortcol = sortcol;
    }

    /*
     public String getiD() {
        return iD;
    }

    public void setID(String iD) {
        this.iD = iD;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId2() {
        return clientId2;
    }

    public void setClientId2(String clientId2) {
        this.clientId2 = clientId2;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getWeightGr() {
        return weightGr;
    }

    public void setWeightGr(String weightGr) {
        this.weightGr = weightGr;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(String manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getRelatedTovarId() {
        return relatedTovarId;
    }

    public void setRelatedTovarId(String relatedTovarId) {
        this.relatedTovarId = relatedTovarId;
    }

    public String getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(String dtUpdate) {
        this.dtUpdate = dtUpdate;
    }*/
}
